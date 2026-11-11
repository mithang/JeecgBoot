<template>
  <div class="bg-white m-4 mr-0 overflow-hidden">
    <div v-if="userIdentity === '2'" class="j-table-operator" style="width: 100%">
      <a-button type="primary" :disabled="treeData.length === 0" preIcon="ant-design:plus-outlined" @click="onAddChildDepart">Thêm cấp dưới</a-button>
      <!--      <a-button type="primary" preIcon="ant-design:edit-outlined" @click="editDepart">Chỉnh sửa</a-button>-->
      <a-button :disabled="!(checkedKeys && checkedKeys.length > 0)" preIcon="ant-design:delete-outlined" @click="onDeleteBatch">Xóa</a-button>
    </div>
    <a-spin :spinning="loading">
      <template v-if="userIdentity === '2'">
        <a-input-search placeholder="Tìm kiếm theo tên phòng ban…" style="margin-bottom: 10px" @search="onSearch" />
        <!--Cây tổ chức-->
        <BasicTree
          v-if="!treeReloading && treeData.length > 0"
          :toolbar="false"
          :search="false"
          :showLine="false"
          :clickRowToExpand="false"
          :multiple="false"
          :checkStrictly="true"
          :treeData="treeData"
          :checkedKeys="checkedKeys"
          :selectedKeys="selectedKeys"
          :expandedKeys="expandedKeys"
          :autoExpandParent="autoExpandParent"
          :beforeRightClick="getRightMenuList"
          @select="onSelect"
          @expand="onExpand"
          @check="onCheck"
        />
        <a-empty v-else-if="!treeReloading && !loading" 
                 description="Tạm thời không có quyền phòng ban phụ trách, vui lòng liên hệ quản trị viên tới 'Quản lý người dùng' để cấu hình phòng ban phụ trách." />
      </template>
      <a-empty v-else description="Nhân viên thường không có quyền này" />
    </a-spin>
    <DepartFormModal :rootTreeData="treeData" @register="registerModal" @success="handleSuccess" />
  </div>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BasicTree, ContextMenuItem } from '/@/components/Tree';
  import { queryMyDepartTreeList, searchByKeywords } from '../depart.user.api';
  import DepartFormModal from '@/views/system/depart/components/DepartFormModal.vue';
  import { useModal } from '@/components/Modal';
  import { deleteBatchDepart } from '@/views/system/depart/depart.api';

  const prefixCls = inject('prefixCls');
  const emit = defineEmits(['select']);
  const { createMessage } = useMessage();

  let loading = ref<boolean>(false);
  // Phòng ban phụ tráchID
  let myDepIds = ref<any[]>([]);
  // Dữ liệu danh sách cây phòng ban
  let treeData = ref<any[]>([]);
  // Mục đang mở rộng
  let expandedKeys = ref<any[]>([]);
  // Mục đang được chọn
  let selectedKeys = ref<any[]>([]);
  // Mục đang được chọn
  let selectedNode = ref<any>({});
  // Mục đang được chọn
  let checkedKeys = ref<any[]>([]);
  // Có tự động mở rộng cấp cha không
  let autoExpandParent = ref<boolean>(true);
  // Danh tính người dùng(1:Nhân viên thường  2: Cấp trên)
  let userIdentity = ref<string>('2');
  // Tải lại component cây
  let treeReloading = ref<boolean>(false);
  // Số thứ tự yêu cầu của cây, ngăn yêu cầu tìm kiếm và đặt lại ghi lại không theo thứ tự
  let treeRequestId = 0;
  // Đăng ký modal
  const [registerModal, { openModal }] = useModal();
  // Tải thông tin phòng ban
  async function loadDepartTreeData() {
    const requestId = ++treeRequestId;
    loading.value = true;
    treeReloading.value = true;
    resetTreeState();
    try {
      const res = await queryMyDepartTreeList();
      if (requestId !== treeRequestId) return;
      if (res.success) {
        userIdentity.value = res.message;
        if (Array.isArray(res.result)) {
          treeData.value = res.result;
          myDepIds.value = res.result.map((item) => item.id);
          autoExpandParentNode();
        }
      } else {
        createMessage.warning(res.message);
      }
    } finally {
      if (requestId === treeRequestId) {
        await nextTick();
        loading.value = false;
        treeReloading.value = false;
      }
    }
  }

  loadDepartTreeData();

  // Tự động mở rộng nút cha, chỉ mở rộng một cấp
  function autoExpandParentNode() {
    let keys: Array<any> = [];
    treeData.value.forEach((item, index) => {
      if (item.children && item.children.length > 0) {
        keys.push(item.key);
      }
      if (index === 0) {
        // Mặc định chọn mục đầu tiên
        setSelectedKey(item.id, item);
      }
    });
    expandedKeys.value = keys;
  }

  // Thêm phòng ban cấp con
  function onAddChildDepart() {
    if (selectedKeys.value && selectedKeys.value.length === 0) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
      return;
    }
    //update-begin---author:wangshuai---date:20260807---for:【LHZP-1152】Khi thêm cấp dưới, dưới phòng ban không thể thêm công ty con nữa---
    const record = { parentId: selectedKeys.value[0], orgCategory: selectedNode.value.orgCategory };
    //update-end---author:wangshuai---date:20260807---for:【LHZP-1152】Khi thêm cấp dưới, dưới phòng ban không thể thêm công ty con nữa---
    openModal(true, { isUpdate: false, isChild: true, record });
  }

  // Chỉnh sửa phòng ban
  function editDepart() {
    if (selectedKeys.value && selectedKeys.value.length === 0) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
      return;
    }
    if (myDepIds.value.includes(selectedKeys.value[0])) {
      createMessage.warning('Không thể chỉnh sửa phòng ban phụ trách');
      return;
    }
    console.log('selectedNode', selectedNode.value);
    openModal(true, { isUpdate: false, isChild: true, record: { ...selectedNode.value } });
  }

  // Xóa phòng ban
  async function onDeleteBatch() {
    const idList = checkedKeys.value;
    if (myDepIds.value.includes(idList[0])) {
      createMessage.warning('Không thể xóa phòng ban phụ trách');
      return;
    }
    if (idList.length > 0) {
      try {
        loading.value = true;
        await deleteBatchDepart({ ids: idList.join(',') }, true);
        await loadDepartTreeData();
      } finally {
        loading.value = false;
      }
    }
  }
  // Dọn dẹp trạng thái kiểm soát của cây cũ, tránh key cũ không khớp với dữ liệu cây mới
  function resetTreeState() {
    treeData.value = [];
    expandedKeys.value = [];
    selectedKeys.value = [];
    checkedKeys.value = [];
    selectedNode.value = {};
  }

  /**
   * Đặt hàng đang được chọn
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    checkedKeys.value = [key];
    if (data) {
      selectedNode.value = { ...data };
      emit('select', data);
    }
  }

  // Sự kiện tìm kiếm
  async function onSearch(value: string) {
    if (value) {
      const requestId = ++treeRequestId;
      loading.value = true;
      treeReloading.value = true;
      resetTreeState();
      try {
        const result = await searchByKeywords({ keyWord: value, myDeptSearch: '1' });
        if (requestId !== treeRequestId) return;
        if (Array.isArray(result)) {
          treeData.value = result;
        } else {
          createMessage.warning('Không truy vấn được thông tin phòng ban');
        }
      } finally {
        if (requestId === treeRequestId) {
          await nextTick();
          loading.value = false;
          treeReloading.value = false;
        }
      }
    } else {
      await loadDepartTreeData();
    }
  }

  // Sự kiện lựa chọn trên cây
  function onSelect(selKeys, event) {
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // Làm vậy có thể ngăn người dùng hủy lựa chọn
      setSelectedKey(selectedKeys.value[0]);
    }
    checkedKeys.value = [selectedKeys.value[0]];
  }

  // Sự kiện chọn trên cây
  function onCheck(keys) {
    if (keys.checked && keys.checked.length > 0) {
      checkedKeys.value = [...keys.checked];
    } else {
      checkedKeys.value = [];
    }
  }

  // Sự kiện mở rộng cây
  function onExpand(keys) {
    expandedKeys.value = keys;
    autoExpandParent.value = false;
  }

  //Callback thành công
  async function handleSuccess() {
    await loadDepartTreeData();
  }
  /**
   *
   * @param node
   */
  function getRightMenuList(node: any): ContextMenuItem[] {
    return [
      {
        label: 'Thêm cấp dưới',
        handler: () => {
          //update-begin---author:wangshuai---date:20260807---for:【LHZP-1152】Khi nhấp chuột phải vào phòng ban và thêm cấp dưới, Phòng Quản lý tổng hợp không thêm được cấp dưới nhưng nút phía trên thì được---
          // Nút nhấp chuột phải cần đồng bộ dữ liệu đầy đủ, hộp thoại thêm mới căn cứ vào đó để giới hạn loại cơ quan có thể chọn
          setSelectedKey(node.key, node.dataRef);
          //update-begin---author:wangshuai---date:20260807---for:【LHZP-1152】Khi nhấp chuột phải vào phòng ban và thêm cấp dưới, Phòng Quản lý tổng hợp không thêm được cấp dưới nhưng nút phía trên thì được---
          onAddChildDepart();
        },
        icon: 'ant-design:plus-outlined',
      },
      {
        label: 'Chỉnh sửa',
        disabled: myDepIds.value.includes(node.key),
        handler: () => {
          setSelectedKey(node.key);
          const record = { ...node.dataRef };
          openModal(true, { isUpdate: true, record, isChild: true });
        },
        icon: 'ant-design:edit-outlined',
      },
    ];
  }
</script>
<style lang="less" scoped>
  /*Sau khi nâng cấp antd3, ô truy vấn và cây dính quá gần nhau, tối ưu kiểu*/
  :deep(.jeecg-tree-header) {
    margin-bottom: 6px;
  }
</style>
