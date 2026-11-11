<template>
  <a-card :bordered="false" style="height: 100%">
    <div class="j-table-operator" style="width: 100%;display: flex;align-items: center">
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddDepart">Thêm mới</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAddChildDepart()">Thêm cấp dưới</a-button>
      <a-upload name="file" :showUploadList="false" :customRequest="onImportXls" v-if="!isTenantDepart">
        <a-button type="primary" preIcon="ant-design:import-outlined">Nhập (import)</a-button>
      </a-upload>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" v-if="!isTenantDepart">Xuất (export)</a-button>
      <template v-if="checkedKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
                <icon icon="ant-design:delete-outlined" />
                <span>Xóa</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Thao tác hàng loạt </span>
            <icon icon="akar-icons:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
      <Icon icon="ant-design:question-circle-outlined" style="margin-left: 10px;cursor: pointer" @click="tipShow = true"></Icon>
      <div v-if="loginTenantName" style="margin-left: 10px;"
      >Đơn vị thuê đang đăng nhập: <span class="tenant-name">{{ loginTenantName }}</span>
      </div>
    </div>
    <a-alert type="info" show-icon class="alert" style="margin-bottom: 8px">
      <template #message>
        <template v-if="checkedKeys.length > 0">
          <span>Đã chọn {{ checkedKeys.length }} bản ghi</span>
          <a-divider type="vertical" />
          <a @click="checkedKeys = []">Xóa sạch</a>
        </template>
        <template v-else>
          <span>Chưa chọn bất kỳ dữ liệu nào</span>
        </template>
      </template>
    </a-alert>
    <a-spin :spinning="loading">
      <a-input-search v-model:value="searchKeyword" placeholder="Tìm kiếm theo tên phòng ban…" style="margin-bottom: 10px" @search="onSearch" />
      <!--Cây tổ chức-->
      <template v-if="treeData.length > 0">
        <a-tree
          checkable
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :checkStrictly="checkStrictly"
          :load-data="isSearchResult ? undefined : loadChildrenTreeData"
          :checkedKeys="checkedKeys"
          v-model:expandedKeys="expandedKeys"
          @check="onCheck"
          @select="onSelect"
          draggable
          @drop="onDrop"
          @dragstart="onDragStart"
          style="overflow-y: auto;height: calc(100vh - 330px);"
        >
          <template #title="{ key: treeKey, title, dataRef, data }">
            <a-dropdown :trigger="['contextmenu']">
              <Popconfirm
                :open="visibleTreeKey === treeKey"
                title="Bạn có chắc chắn muốn xóa không?"
                ok-text="Xác nhận"
                cancel-text="Hủy"
                placement="rightTop"
                @confirm="onDelete(dataRef)"
                @openChange="onVisibleChange"
              >
                <TreeIcon :orgCategory="dataRef.orgCategory" :title="getTreeTitle(dataRef, title)"></TreeIcon>
              </Popconfirm>

              <template #overlay>
                <a-menu @click="">
                  <a-menu-item key="1" @click="onAddChildDepart(dataRef)" v-if="data.orgCategory !== '3'">Thêm cấp dưới</a-menu-item>
                  <a-menu-item key="2" @click="visibleTreeKey = treeKey">
                    <span style="color: red">Xóa</span>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
        </a-tree>
      </template>
      <a-empty v-else description="Không có dữ liệu" />
    </a-spin>
    <DepartFormModal :rootTreeData="treeData" @register="registerModal" @success="loadRootTreeData" />
  </a-card>
  <a-modal v-model:open="tipShow" :footer="null" title="Giải thích quy tắc phòng ban" :width="800">
      <ul class="departmentalRulesTip">
        <li>Cài đặt cơ quan phòng ban hiện tại hỗ trợ sơ đồ tổ chức tập đoàn, cấp thứ nhất mặc định là công ty, cấp dưới có thể tạo công ty con, phòng ban và vị trí.</li>
        <li><br/></li>
        <li>1, dưới vị trí không thể thêm cấp dưới.</li>
        <li>2, dưới phòng ban không thể trực tiếp thêm công ty con.</li>
        <li>3, dưới công ty con có thể tiếp tục thêm công ty con.</li>
        <li>4, vị trí cần cấu hình cấp bậc chức danh, mức cao thấp và quan hệ cấp trên/cấp dưới của vị trí đều căn cứ theo cấp bậc chức danh và cài đặt vị trí cấp trên.</li>
        <li>5, vị trí Chủ tịch chỉ có thể chọn mọi vị trí của các phòng ban thuộc công ty cấp trên (công ty con hoặc tổng công ty) làm vị trí cấp trên.</li>
        <li>6, vị trí không phải Chủ tịch chỉ có thể chọn vị trí có cấp bậc cao hơn trong phòng ban cha hiện tại và trong chính phòng ban này làm vị trí cấp trên.</li>
        <li><br/></li>
        <li><b>Giải thích đặc biệt:</b>Logic liên quan đến Chủ tịch được viết cứng cố định, cách diễn đạt cấp bậc chức danh 'Chủ tịch' vui lòng không sửa đổi.</li>
      </ul>
    <div style="height: 10px"></div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { inject, ref, unref, defineEmits, h } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { Api, deleteBatchDepart, queryAllParentId, queryDepartAndPostTreeSync, searchByDepartManage, updateChangeDepart } from '../depart.api';
  import { initDictOptions } from '/@/utils/dict/JDictSelectUtil';
  import DepartFormModal from '/@/views/system/depart/components/DepartFormModal.vue';
  import { Modal, Popconfirm } from 'ant-design-vue';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select', 'rootTreeData', 'dragSuccess']);
  const { createMessage } = useMessage();
  const { handleImportXls, handleExportXls } = useMethods();
  const props = defineProps({
    //Có phải phòng ban của đơn vị thuê không
    isTenantDepart: { default: false, type: Boolean },
    //Đơn vị thuê đang đăng nhập
    loginTenantName: { default: "", type: String },
  })

  const loading = ref<boolean>(false);
  // Dữ liệu danh sách cây phòng ban
  const treeData = ref<any[]>([]);
  // Mục đang được chọn
  const checkedKeys = ref<any[]>([]);
  // Mục đang mở rộng
  const expandedKeys = ref<any[]>([]);
  // Mục đang được chọn
  const selectedKeys = ref<any[]>([]);
  // Cây cha-con có liên kết hay không
  const checkStrictly = ref<boolean>(true);
  // Phòng ban đang chọn
  const currentDepart = ref<any>(null);
  // Kiểm soát hộp thoại nhắc xác nhận xóa có hiển thị hay không
  const visibleTreeKey = ref<any>(null);
  // Từ khóa tìm kiếm
  const searchKeyword = ref('');
  // Có đang hiển thị kết quả tìm kiếm hay không, kết quả tìm kiếm chỉ hiển thị các nút trúng và cấp trên của chúng
  const isSearchResult = ref<boolean>(false);
  // Hộp thoại nhắc có hiển thị hay không
  const tipShow = ref<boolean>(false);
  // Tên cấp bậc chức danh, lấy một lần khi tải trang, tránh truy vấn theo từng vị trí
  const positionNameMap = ref<Record<string, string>>({});

  // Đăng ký modal
  const [registerModal, { openModal }] = useModal();

  // Tải thông tin phòng ban cấp cao nhất
  async function loadRootTreeData() {
    try {
      loading.value = true;
      treeData.value = [];
      const result = await queryDepartAndPostTreeSync();
      if (Array.isArray(result)) {
        treeData.value = result;
      }
      if (expandedKeys.value.length === 0) {
        autoExpandParentNode();
      } else {
        if (selectedKeys.value.length === 0) {
          let item = treeData.value[0];
          if (item) {
            // Mặc định chọn mục đầu tiên
            setSelectedKey(item.id, item);
          }
        } else {
          emit('select', currentDepart.value);
        }
      }
      emit('rootTreeData', treeData.value);
    } finally {
      loading.value = false;
    }
  }

  loadPositionNames();
  loadRootTreeData();

  /**
   * Tải một lần từ điển cấp bậc chức danh, việc kết xuất nút cây chỉ tra cứu trong bộ nhớ
   * @author scott
   * @since 2026-08-06 【Không】Nút vị trí hiển thị cấp bậc chức danh
   */
  async function loadPositionNames() {
    try {
      const options = await initDictOptions('sys_position,name,id');
      if (Array.isArray(options)) {
        positionNameMap.value = Object.fromEntries(options.map((item) => [item.value, item.text]));
      }
    } catch (e) {
      console.error('Tải từ điển cấp bậc chức danh thất bại', e);
    }
  }

  /**
   * Nút vị trí hiển thị thêm cấp bậc chức danh, các nút khác giữ nguyên tên
   * @author scott
   * @since 2026-08-06 【Không】Nút vị trí hiển thị cấp bậc chức danh
   */
  function getTreeTitle(data, title: string) {
    if (data.orgCategory !== '3' || !data.positionId) {
      return title;
    }
    const positionName = positionNameMap.value[data.positionId];
    return positionName ? `${title}（${positionName}）` : title;
  }

  // Tải thông tin phòng ban cấp con
  async function loadChildrenTreeData(treeNode) {
    try {
      const result = await queryDepartAndPostTreeSync({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        treeNode.dataRef.children = result;
        if (expandedKeys.value.length > 0) {
          // Kiểm tra trong các mục con lấy được có mục đang mở rộng hay không
          let subKeys: any[] = [];
          for (let key of expandedKeys.value) {
            if (result.findIndex((item) => item.id === key) !== -1) {
              subKeys.push(key);
            }
          }
          if (subKeys.length > 0) {
            expandedKeys.value = [...expandedKeys.value];
          }
        }
      }
      treeData.value = [...treeData.value];
      emit('rootTreeData', treeData.value);
    } catch (e) {
      console.error(e);
    }
    return Promise.resolve();
  }

  // Tự động mở rộng nút cha, chỉ mở rộng một cấp
  function autoExpandParentNode() {
    let item = treeData.value[0];
    if (item) {
      if (!item.isLeaf) {
        expandedKeys.value = [item.key];
      }
      // Mặc định chọn mục đầu tiên
      setSelectedKey(item.id, item);
    } else {
      emit('select', null);
    }
  }

  /**
   * Đặt hàng đang được chọn
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    if (data) {
      currentDepart.value = data;
      emit('select', data);
    }
  }

  // Thêm phòng ban cấp một
  function onAddDepart() {
    openModal(true, { isUpdate: false, isChild: false });
  }

  // Thêm phòng ban cấp con
  function onAddChildDepart(data = currentDepart.value) {
    if (data == null) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
      return;
    }
    if(data.orgCategory === '3'){
      createMessage.warning('Không thể thêm cấp dưới vào vị trí!');
      return;
    }
    const record = { parentId: data.id, orgCategory: data.orgCategory };
    openModal(true, { isUpdate: false, isChild: true, record });
  }

  // Sự kiện tìm kiếm
  async function onSearch(value: string) {
    if (value) {
      try {
        loading.value = true;
        isSearchResult.value = true;
        treeData.value = [];
        // update-begin--author:wangshuai---date:20260813---for:【LHZP-1075】【Quản lý hệ thống】Phòng ban: sau khi tìm kiếm, nếu là công ty hoặc phòng ban thì không mở rộng được cấp dưới
        let result = await searchByDepartManage({ keyWord: value, orgCategory: "1,2,3,4" });
        // update-end--author:wangshuai---date:20260813---for:【LHZP-1075】【Quản lý hệ thống】Phòng ban: sau khi tìm kiếm, nếu là công ty hoặc phòng ban thì không mở rộng được cấp dưới
        if (Array.isArray(result)) {
          treeData.value = await buildSearchTreeWithParents(result);
        }
        expandSearchResult();
      } finally {
        loading.value = false;
      }
    } else {
      isSearchResult.value = false;
      expandedKeys.value = [];
      await loadRootTreeData();
    }
    searchKeyword.value = value;
  }

  // Dùng API truy vấn tổ tiên hiện có để bổ sung cấp trên của các nút trúng, đồng thời lắp ráp cây ở frontend
  async function buildSearchTreeWithParents(searchResult: any[]) {
    const departIds = searchResult.map((item) => item.id).filter(Boolean);
    if (departIds.length === 0) {
      return [];
    }
    const parentResult = await queryAllParentId({ departId: departIds.join(',') });
    const nodeMap = new Map<string, any>();
    Object.values(parentResult ?? {}).forEach((parentData: any) => {
      Object.values(parentData?.parentMap ?? {}).forEach((depart: any) => {
        nodeMap.set(depart.id, createSearchTreeNode(depart));
      });
    });
    searchResult.forEach((depart) => nodeMap.set(depart.id, createSearchTreeNode(depart)));
    const result: any[] = [];
    nodeMap.forEach((node) => {
      const parent = nodeMap.get(node.parentId);
      if (parent) {
        parent.children.push(node);
        parent.isLeaf = false;
      } else {
        result.push(node);
      }
    });
    sortSearchTree(result);
    return result;
  }

  // Chuyển thực thể phòng ban thành nút cây
  function createSearchTreeNode(depart: any) {
    return {
      ...depart,
      key: depart.key ?? depart.id,
      value: depart.value ?? depart.id,
      title: depart.title ?? depart.departName,
      isLeaf: depart.isLeaf ?? depart.izLeaf !== 0,
      children: [],
    };
  }

  // Kết quả tìm kiếm kế thừa sắp xếp của phòng ban
  function sortSearchTree(nodes: any[]) {
    nodes.sort((first, second) => (first.departOrder ?? 0) - (second.departOrder ?? 0));
    nodes.forEach((node) => {
      sortSearchTree(node.children);
      node.isLeaf = node.children.length === 0;
    });
  }

  // Mở rộng đường dẫn tổ tiên đầy đủ trong kết quả tìm kiếm, không kích hoạt tải lười cấp dưới
  function expandSearchResult() {
    const keys: string[] = [];
    const stack = [...treeData.value];
    while (stack.length > 0) {
      const node = stack.pop();
      if (node?.children?.length > 0) {
        keys.push(node.key);
        stack.push(...node.children);
      }
    }
    expandedKeys.value = keys;
    const item = treeData.value[0];
    if (item) {
      setSelectedKey(item.id, item);
    } else {
      emit('select', null);
    }
  }

  // Sự kiện chọn hộp kiểm của cây
  function onCheck(e) {
    if (Array.isArray(e)) {
      checkedKeys.value = e;
    } else {
      checkedKeys.value = e.checked;
    }
  }

  // Sự kiện lựa chọn trên cây
  function onSelect(selKeys, event) {
    console.log('select: ', selKeys, event);
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // Làm vậy có thể ngăn người dùng hủy lựa chọn
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  /**
   * Xóa phòng ban theo ids
   * @param idListRef array
   * @param confirm Có hiển thị hộp thoại nhắc xác nhận không
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        loading.value = true;
        await deleteBatchDepart({ ids: idList.join(',') }, confirm);
        await loadRootTreeData();
      } finally {
        loading.value = false;
      }
    }
  }

  // Xóa một phòng ban
  async function onDelete(data) {
    if (data) {
      onVisibleChange(false);
      doDeleteDepart([data.id], false);
    }
  }

  // Xóa hàng loạt phòng ban
  async function onDeleteBatch() {
    try {
      await doDeleteDepart(checkedKeys);
      checkedKeys.value = [];
    } finally {
    }
  }

  function onVisibleChange(visible) {
    if (!visible) {
      visibleTreeKey.value = null;
    }
  }

  function onImportXls(d) {
    handleImportXls(d, Api.importExcelUrl, () => {
      loadRootTreeData();
    });
  }

  function onExportXls() {
    // Giải thích logic mã: 【TV360X-1671】Quản lý phòng ban không hỗ trợ xuất các bản ghi đã chọn---
    let params = {}
    if(checkedKeys.value && checkedKeys.value.length > 0) {
      params['selections'] = checkedKeys.value.join(',')
    }
    handleExportXls('Thông tin phòng ban', Api.exportXlsUrl,params);
  }

  /**
   * Khi bắt đầu kéo thả, chỉ đóng nút hiện tại đang bị kéo thả
   * 
   * @param info
   */
  function onDragStart(info: any) {
    const dragKey = info.node?.key;
    if (!dragKey){
      return;
    }
    // Chỉ đóng nút hiện tại đang bị kéo thả, không đóng các nút con của nó
    if (expandedKeys.value.includes(dragKey)) {
      expandedKeys.value = expandedKeys.value.filter(key => key !== dragKey);
    }
  }
  
  /**
   * Kết thúc kéo thả
   * @param info
   */
  function onDrop (info){
    const dropKey = info.node.key;
    const dragKey = info.dragNode.key;
    const dropPos = info.node.pos.split('-');
    const dropPosition = info.dropPosition - Number(dropPos[dropPos.length - 1]);
    const dropTitle = info.node.title;
    const dragTitle = info.dragNode.title;
    //Cấm kéo thả vào nút con
    if (isDescendant(info.dragNode, info.node.key)) {
      createMessage.warning('Không thể kéo thả vào hậu duệ của chính nó');
      return;
    }
    if(dropKey === dragKey){
      createMessage.warning('Không thể tự kéo thả vào chính nó');
      return;
    }
    let pos = " vào trong";
    if(dropPosition === -1){
      pos = " ở phía trên";
    }else if (dropPosition === 1){
      pos = " ở phía dưới";
    }
    let text = "Di chuyển【" + dragTitle + "】 tới【" + dropTitle + "】" + pos + "？";
    Modal.confirm({
      title: 'Xác nhận di chuyển',
      content: h('div', {}, [
        h('p', { style: { marginBottom: '12px', fontSize: '14px' } }, text),
        h('p', { 
          style: { 
            color: '#ff4d4f', 
            fontSize: '13px',
            margin: '0'
          } 
        }, 'Sau khi di chuyển: mã cơ quan sẽ thay đổi, dữ liệu nghiệp vụ lịch sử giữ nguyên mã cơ quan cũ, thao tác này không thể hoàn tác!')
      ]),
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        updateChangeDepart({ dragId: dragKey, dropId: dropKey, dropPosition: dropPosition, sort: info.dropPosition }).then(async res=>{
          if(res.success){
            createMessage.success('Điều chỉnh thứ tự phòng ban thành công');
            //Tải lại cây
            treeData.value = [];
            selectedKeys.value = [];
            await loadRootTreeData();
            emit('dragSuccess');
          } else {
            createMessage.error(res.message);
          }
        }).catch(e=>{
          createMessage.error(e.message);
        })
      }
    })
  }

  /**
   * Kiểm tra nút đích có nằm trong cây con của nút đang kéo thả hay không (tránh tham chiếu vòng tròn)
   * 
   * @param dragNode
   * @param targetKey
   */
  function isDescendant(dragNode, targetKey) {
    const stack = [...(dragNode.children ?? [])];
    while (stack.length) {
      const node = stack.pop()!;
      if (node.key === targetKey){
        return true;
      }
      if (node.children){
        stack.push(...node.children);
      }
    }
    return false;
  }
  
  defineExpose({
    loadRootTreeData,
  });
</script>

<style lang="less" scoped>
  .departmentalRulesTip{
    margin: 20px;
    background-color: #f8f9fb;
    color: #99a1a9;
    border-radius: 4px;
    padding: 12px;
  }
  .tenant-name {
    text-decoration: underline;
    margin: 5px;
    font-size: 15px;
  }

  :deep(.ant-tree-treenode) {
    width: 100%;
  }

  :deep(.ant-tree-node-content-wrapper) {
    flex: 1;
    min-width: 0;
  }
</style>
