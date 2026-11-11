<template>
  <BasicModal
    title="Chọn người dùng"
    @register="registerModal"
    width="100%"
    wrapClassName="tabs-select-user-modal"
    style="top: 10px"
    @ok="handleSelectSuccess"
    :canFullscreen="true"
    keyboard
    defaultFullscreen
  >
    <a-row>
      <!-- Chuyển đổi nhiều tabs bên trái -->
      <a-col :xs="24" :sm="6">
        <a-card class="left-select-card" :bordered="true" :bodyStyle="{ overflow: 'hidden', padding: 0 }">
          <a-tabs class="left-select-tabs" v-model:activeKey="activeTab" size="small" centered :tabBarStyle="{ margin: 10 }" @change="onTabChange">
            <!-- Tab phòng ban -->
            <a-tab-pane key="depart" tab="Phòng ban">
              <div class="left-tab-content">
                <a-alert type="info" :showIcon="true" class="alert-info">
                  <template #message>
                    <div class="alert-message-content">
                      <span class="alert-label">Lựa chọn hiện tại:</span>
                      <span v-if="departInfo.currentSelectRow.title" class="selected-title">{{ departInfo.currentSelectRow.title }}</span>
                      <a v-if="departInfo.currentSelectRow.title" class="clear-link" @click="onClearSelectedDepart">Hủy lựa chọn</a>
                    </div>
                  </template>
                </a-alert>
                <a-input-search placeholder="Tìm kiếm theo tên phòng ban…" :allowClear="true" style="margin-bottom: 10px" @search="onSearchDepart" v-model:value="departSearchText" />
                <!--Tổ chức-->
                <div class="tree-container" v-if="departInfo.treeData && departInfo.treeData.length > 0">
                  <a-directory-tree
                    selectable
                    :selectedKeys="departInfo.selectedKeys"
                    :checkStrictly="true"
                    @select="onSelectDepart"
                    :dropdownStyle="{ maxHeight: '200px', overflow: 'auto' }"
                    :load-data="onLoadTreeData"
                    :treeData="departInfo.treeData"
                    :showIcon="false"
                    :expandedKeys="expandedDepartKeys"
                    @expand="onDepartExpand"
                  >
                    <template #title="{ orgCategory, title }">
                      <TreeIcon :orgCategory="orgCategory" :title="title"></TreeIcon>
                    </template>
                  </a-directory-tree>
                </div>
                <a-empty v-else description="Không có thông tin phòng ban" />
              </div>
            </a-tab-pane>

            <!-- Tab vị trí công tác -->
            <a-tab-pane key="position" tab="Vị trí công tác">
              <div class="left-tab-content">
                <a-alert type="info" :showIcon="true" class="alert-info">
                  <template #message>
                    <div class="alert-message-content">
                      <span class="alert-label">Lựa chọn hiện tại:</span>
                      <span v-if="positionInfo.currentSelectRow.title" class="selected-title">{{ positionInfo.currentSelectRow.title }}</span>
                      <a v-if="positionInfo.currentSelectRow.title" class="clear-link" @click="onClearSelectedPosition">Hủy lựa chọn</a>
                    </div>
                  </template>
                </a-alert>
                <a-input-search
                  placeholder="Tìm kiếm theo tên vị trí…"
                  style="margin-bottom: 10px"
                  allowClear
                  @search="onSearchPosition"
                  v-model:value="positionSearchText"
                />
                <!--Danh sách vị trí công tác-->
                <div class="position-list">
                  <PostRankRelation :treeData="positionInfo.treeData" @select="onSelectPosition" />
                </div>
              </div>
            </a-tab-pane>

            <!-- Tab nhóm người dùng -->
            <a-tab-pane key="userGroup" tab="Nhóm người dùng">
              <div class="left-tab-content">
                <a-alert type="info" :showIcon="true" class="alert-info">
                  <template #message>
                    <div class="alert-message-content">
                      <span class="alert-label">Lựa chọn hiện tại:</span>
                      <span v-if="userGroupInfo.currentSelectRow.title" class="selected-title">{{ userGroupInfo.currentSelectRow.title }}</span>
                      <a v-if="userGroupInfo.currentSelectRow.title" class="clear-link" @click="onClearSelectedUserGroup">Hủy lựa chọn</a>
                    </div>
                  </template>
                </a-alert>
                <a-input-search
                  placeholder="Tìm kiếm theo tên nhóm người dùng…"
                  style="margin-bottom: 10px"
                  allowClear
                  @search="onSearchUserGroup"
                  v-model:value="userGroupSearchText"
                />
                <!--Danh sách nhóm người dùng-->
                <div class="user-group-list" v-if="userGroupInfo.data && userGroupInfo.data.length > 0">
                  <div
                    v-for="item in userGroupInfo.data"
                    :key="item.id"
                    class="user-group-item"
                    :class="{ 'user-group-item-selected': userGroupInfo.selectedKeys.includes(item.id) }"
                    @click="onSelectUserGroup(item)"
                  >
                    <span class="user-group-title">{{ item.title }}</span>
                  </div>
                </div>
                <a-empty v-else description="Không có thông tin nhóm người dùng" />
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>

      <!-- Danh sách ở giữa-Hiển thị thông tin người dùng -->
      <a-col :xs="24" :sm="showSelected ? 12 : 18">
        <a-card title="Chọn nhân sự" :bordered="true" :bodyStyle="{ paddingTop: '1px' }">
          <BasicTable @register="registerTable" :rowSelection="rowSelection" />
        </a-card>
      </a-col>

      <!-- Bên phải hiển thị người dùng đã chọn, hỗ trợ điều chỉnh thứ tự -->
      <a-col :xs="24" :sm="6" v-if="showSelected">
        <a-card title="Người dùng đã chọn" :bordered="true">
          <BasicTable @register="registerSelectedUserTable">
            <!--Thanh thao tác-->
            <template #action="{ record }">
              <a-button type="primary" size="small" @click="handleDelete(record)" preIcon="ant-design:delete">Xóa</a-button>
            </template>
          </BasicTable>
        </a-card>
      </a-col>
    </a-row>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { ref, unref, reactive, toRaw, watch } from 'vue';
  import {
    getDepartTreeData,
    getDepartUserList,
    getUserList,
    queryALLRankRelation,
    searchByKeywords,
    columns,
    selectedUserColumns,
    searchFormSchema,
  } from './useSelectUser';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TreeIcon from '@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue';
  import { defHttp } from '/@/utils/http/axios';
  import PostRankRelation from './component/PostRankRelation.vue';

  const props = defineProps({
    multi: {
      type: Boolean,
      default: true,
    },
    showSelected: {
      type: Boolean,
      default: true,
    },
    //Trả về tên trường value
    rowKey: {
      type: String,
      default: 'username',
    },
  });
  const emit = defineEmits(['selected', 'register']);
  const [registerModal, { closeModal }] = useModalInner((data) => {
    showSelectedValue(data);
  });

  // Tab hiện đang kích hoạt
  const activeTab = ref('depart');
  // Trạng thái tải
  const loading = ref(false);
  // Văn bản tìm kiếm
  const departSearchText = ref('');
  const positionSearchText = ref('');
  const userGroupSearchText = ref('');

  // Cache dữ liệu gốc
  const originalDepartData = ref<any[]>([]);
  const originalPositionData = ref<any[]>([]);
  const originalUserGroupData = ref<any[]>([]);
  /*-----------------Phòng ban---begin----------------*/
  const departInfo = reactive({
    treeData: [] as any[],
    selectedKeys: [] as any[],
    currentSelectRow: {
      title: '',
    } as any,
  });
  function onSelectDepart(data, { node }) {
    departInfo.selectedKeys[0] = data[0];
    departInfo.currentSelectRow = toRaw(node.dataRef);
    console.log(departInfo);
    reload();
  }
  function onClearSelectedDepart() {
    departInfo.selectedKeys = [];
    departInfo.currentSelectRow = { title: '' };
    reload();
  }
  // của cây phòng ban đã mở rộngkeys
  const expandedDepartKeys = ref<any[]>([]);

  // Sự kiện mở rộng cây phòng ban
  function onDepartExpand(expandedKeys) {
    expandedDepartKeys.value = expandedKeys;
    // Kích hoạt tính toán lại vùng cuộn sau một khoảng trễ
    setTimeout(() => {
      const treeContainer = document.querySelector('.tree-container') as HTMLElement | null;
      if (treeContainer) {
        // Buộc kích hoạt sắp xếp lại
        treeContainer.style.overflow = 'hidden';
        setTimeout(() => {
          treeContainer.style.overflow = 'auto';
        }, 10);
      }
    }, 100);
  }
  async function loadRootDepart() {
    const result = await getDepartTreeData();
    if (Array.isArray(result)) {
      //update-begin-author:liusq---date:2025-12-05--for: JHHB-1175 Chỉ định người thao tác tiếp theo, nhân viên cần sắp xếp theo thứ tự của người dùng, phòng ban thêm thanh cuộn, hiển thị tên viết tắt của phòng ban
      result.forEach((item) => {
        item.title = item.departNameAbbr || item.title;
      });
      //update-end-author:liusq---date:2025-12-05--for: JHHB-1175 Chỉ định người thao tác tiếp theo, nhân viên cần sắp xếp theo thứ tự của người dùng, phòng ban thêm thanh cuộn, hiển thị tên viết tắt của phòng ban
      departInfo.treeData = result;
      originalDepartData.value = result;
    }
  }

  // Tìm kiếm phòng ban
  async function onSearchDepart(value) {
    departSearchText.value = value;
    if (!value) {
      departInfo.treeData = originalDepartData.value;
      return;
    }
    try {
      departInfo.treeData = [];
      let result = await searchByKeywords({ keyWord: value, orgCategory: '1,2' });
      if (Array.isArray(result)) {
        departInfo.treeData = result;
      }
    } finally {
      loading.value = false;
    }
  }
  async function onLoadTreeData(treeNode) {
    try {
      const result = await getDepartTreeData({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        //update-begin-author:liusq---date:2025-12-05--for: JHHB-1175 Chỉ định người thao tác tiếp theo, nhân viên cần sắp xếp theo thứ tự của người dùng, phòng ban thêm thanh cuộn, hiển thị tên viết tắt của phòng ban
        result.forEach((item) => {
          item.title = item.departNameAbbr || item.title;
        });
        //update-end-author:liusq---date:2025-12-05--for: JHHB-1175 Chỉ định người thao tác tiếp theo, nhân viên cần sắp xếp theo thứ tự của người dùng, phòng ban thêm thanh cuộn, hiển thị tên viết tắt của phòng ban
        treeNode.dataRef.children = result;
      }
      // departInfo.treeData = [...departInfo.treeData]
      // Sau khi tải dữ liệu xong, trì hoãn kích hoạt tính toán lại vùng cuộn
      // setTimeout(() => {
      //   const treeContainer = document.querySelector('.tree-container');
      //   if (treeContainer) {
      //     // Buộc kích hoạt reflow, giữ nguyên vị trí cuộn hiện tại
      //     const scrollTop = treeContainer.scrollTop;
      //     treeContainer.style.overflow = 'hidden';
      //     setTimeout(() => {
      //       treeContainer.style.overflow = 'auto';
      //       treeContainer.scrollTop = scrollTop;
      //     }, 10);
      //   }
      // }, 50);
    } catch (e) {
      console.error('Tải nút con của cây phòng ban thất bại', e);
    }
    return Promise.resolve();
  }
  /*-----------------Phòng ban---end----------------*/

  /*-----------------Vị trí công tác---begin----------------*/
  const positionInfo = reactive({
    treeData: [] as any[],
    selectedKeys: [] as any[],
    currentSelectRow: { title: '' } as any,
  });
  // Hàm tìm kiếm đệ quy, hỗ trợ tìm kiếm nút ở mọi cấp
  const findNodeInTree = (treeData: any[], targetId: any): any => {
    for (const node of treeData) {
      if (node.id == targetId) {
        return node;
      }
      if (node.children && Array.isArray(node.children)) {
        const found = findNodeInTree(node.children, targetId);
        if (found) {
          return found;
        }
      }
    }
    return null;
  };

  function onSelectPosition(data) {
    // Tránh lỗi tham chiếu vòng lặp, chỉ dùng dữ liệu an toàn
    positionInfo.selectedKeys[0] = data[0];
    let obj = findNodeInTree(positionInfo.treeData, data[0]);
    positionInfo.currentSelectRow = obj ? { ...obj } : { title: '' };
    reload();
  }

  function onClearSelectedPosition() {
    positionInfo.selectedKeys = [];
    positionInfo.currentSelectRow = { title: '' };
    reload();
  }

  async function loadPositionList() {
    try {
      const result = await queryALLRankRelation();
      if (result && Array.isArray(result) && result.length > 0) {
        positionInfo.treeData = result;
        originalPositionData.value = result;
      }
    } catch (error) {
      console.error('Lấy danh sách vị trí công tác thất bại', error);
    }
  }
  // Tìm kiếm vị trí công tác
  function onSearchPosition(value) {
    positionSearchText.value = value;
    if (!value) {
      positionInfo.treeData = originalPositionData.value;
      return;
    }

    const searchText = value.toLowerCase();

    // Lọc cây đệ quy: nếu bản thân nút khớp hoặc bất kỳ nút con cháu nào khớp thì giữ lại nút đó (và chỉ giữ lại các nút con khớp)
    function filterTree(nodes: any[]): any[] {
      if (!Array.isArray(nodes)) return [];
      const result: any[] = [];
      for (const node of nodes) {
        const title = (node.title || '').toLowerCase();
        let matched = title.includes(searchText);
        let filteredChildren = [];
        if (node.children && Array.isArray(node.children)) {
          filteredChildren = filterTree(node.children);
          if (filteredChildren.length > 0) matched = true;
        }
        if (matched) {
          // Sao chép nông nút để tránh sửa dữ liệu gốc
          const newNode: any = { ...node };
          if (filteredChildren.length > 0) {
            newNode.children = filteredChildren;
          } else {
            // Đảm bảo khi không có nút con khớp thì không mang theo children hoặc xóa sạch nó
            delete newNode.children;
          }
          result.push(newNode as any);
        }
      }
      return result;
    }

    positionInfo.treeData = filterTree(originalPositionData.value || []);
  }
  /*-----------------Vị trí công tác---end----------------*/

  /*-----------------Nhóm người dùng---begin----------------*/
  const userGroupInfo = reactive({
    data: [] as any[],
    selectedKeys: [] as string[],
    currentSelectRow: {
      title: '',
    } as any,
  });

  function onSelectUserGroup(data) {
    userGroupInfo.selectedKeys[0] = data.id;
    userGroupInfo.currentSelectRow = data;
    reload();
  }

  function onClearSelectedUserGroup() {
    userGroupInfo.selectedKeys = [];
    userGroupInfo.currentSelectRow = { title: '' };
    reload();
  }

  async function loadUserGroupList() {
    try {
      const result = await defHttp.get({ url: '/sys/ugroup/list' });
      if (result.records && Array.isArray(result.records) && result.records.length > 0) {
        const userGroupData = result.records.map((item) => ({
          key: item.id,
          title: item.groupName,
          id: item.id,
        }));
        userGroupInfo.data = userGroupData;
        originalUserGroupData.value = userGroupData;
      }
    } catch (error) {
      console.error('Lấy danh sách nhóm người dùng thất bại', error);
    }
  }
  // Tìm kiếm nhóm người dùng
  function onSearchUserGroup(value) {
    userGroupSearchText.value = value;
    if (!value) {
      userGroupInfo.data = originalUserGroupData.value;
      return;
    }

    const searchText = value.toLowerCase();
    const filteredData = originalUserGroupData.value.filter((item) => item.title.toLowerCase().includes(searchText));
    userGroupInfo.data = filteredData;
  }
  /*-----------------Nhóm người dùng---end----------------*/

  /*-----------------Danh sách người dùng---begin----------------*/
  async function queryUserList(params) {
    params['column'] = 'sort';
    params['order'] = 'ASC';
    params['status'] = '1';
    if(departInfo.selectedKeys.length == 0 && positionInfo.selectedKeys.length == 0 && userGroupInfo.selectedKeys.length == 0 && params.realname){
      params['realname'] = `*${params.realname.trim()}*`;
    }
    // Truy vấn theo tab đang được kích hoạt
    if (activeTab.value === 'depart') {
      let arr = departInfo.selectedKeys;
      if (arr.length > 0) {
        //Truy vấn theo phòng ban
        params['id'] = arr[0];
        let result = await getDepartUserList(params);
        if (params.username) {
          result.records = result.records.filter((item) => {
            return item.username.indexOf(params.username) != -1;
          });
        }
        return Promise.resolve(result);
      } else {
        return getUserList(params);
      }
    } else if (activeTab.value === 'position') {
      let arr = positionInfo.selectedKeys;
      if (arr.length > 0) {
        //Truy vấn theo vị trí công tác
        params['orgCode'] = positionInfo.currentSelectRow?.orgCode;
        return defHttp.get({ url: '/sys/user/queryDepartPostByOrgCode', params });
      } else {
        return getUserList(params);
      }
    } else if (activeTab.value === 'userGroup') {
      let arr = userGroupInfo.selectedKeys;
      if (arr.length > 0) {
        //Tìm kiếm theo nhóm người dùng
        params['groupId'] = arr[0];
        return defHttp.get({ url: '/sys/user/groupUserList', params });
      } else {
        return getUserList(params);
      }
    } else {
      return getUserList(params);
    }
  }
  const { tableContext } = useListPage({
    designScope: 'tabs-select-user',
    pagination: true,
    tableProps: {
      title: '',
      api: queryUserList,
      columns: columns,
      showActionColumn: false,
      showTableSetting: false,
      canResize: false,
      clickToRowSelect: true,
      formConfig: {
        labelWidth: '90px',
        schemas: searchFormSchema,
        autoAdvancedCol: 4,
        //update-begin-author:liusq---date:2024-06-11--for: Popup chỉ định người ký duyệt bị ô tìm kiếm che khuất
        baseColProps: { xs: 24, sm: 24, md: 24, lg: 12, xl: 8, xxl: 8 },
        actionColOptions: { xs: 24, sm: 24, md: 24, lg: 12, xl: 8, xxl: 8 },
        //update-end-author:liusq---date:2024-06-11--for:Popup chỉ định người ký duyệt bị ô tìm kiếm che khuất
      },
    },
  });
  const [registerTable, { reload, deleteSelectRowByKey }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  watch(
    () => props.multi,
    (val) => {
      if (val === false) {
        rowSelection.type = 'radio';
      } else {
        rowSelection.type = 'checkbox';
      }
    }
  );

  /*-----------------Danh sách người dùng--end-----------------*/
  const selectedUserList = ref<any[]>([]);
  //update-begin-author:liusq---date:2024-06-11--for: TV360X-1047 Chỉ định người thực hiện bước tiếp theo/Sao chép cho, component chọn người không thể đa chọn.
  watch(
    selectedRows,
    () => {
      let arr = [];
      for (let row of unref(selectedRows)) {
        arr.push({
          realname: row.realname,
          username: row.username,
          id: row.id,
        });
      }
      selectedUserList.value = arr;
    },
    { deep: true }
  );
  //update-end-author:liusq---date:2024-06-11--for: TV360X-1047 Chỉ định người thực hiện bước tiếp theo/Sao chép cho, component chọn người không thể đa chọn.

  const { tableContext: selectedTableContext } = useListPage({
    designScope: 'bpm-select-user',
    pagination: false,
    tableProps: {
      title: '',
      columns: selectedUserColumns,
      pagination: false,
      dataSource: selectedUserList,
      showActionColumn: true,
      showTableSetting: false,
      canResize: false,
      useSearchForm: false,
    },
  });
  const [registerSelectedUserTable] = selectedTableContext;
  function handleDelete(record) {
    let id = record.id;
    let arr = selectedUserList.value;
    arr = arr.filter((item) => item.id != id);
    selectedUserList.value = arr;
    deleteSelectRowByKey(record.id);
  }

  function handleSelectSuccess() {
    let arr = toRaw(selectedUserList.value);
    emit('selected', arr);
    closeModal();
  }

  /**
   * Khi popup mở, hiển thị lại dữ liệu đã chọn trong dropdown
   * @param data
   */
  function showSelectedValue(data) {
    let selectedValue = data.selected;
    if (!selectedValue || selectedValue.length == 0) {
      selectedUserList.value = [];
      selectedRows.value = [];
      selectedRowKeys.value = [];
    } else {
      let arr1 = [],
        arr2 = [],
        arr3 = [];
      for (let item of selectedValue) {
        arr1.push(item.id);
        arr2.push({ ...item });
        arr3.push({ ...item });
      }
      selectedRowKeys.value = arr1;
      selectedUserList.value = arr2;
      selectedRows.value = arr3;
    }
  }

  /**
   * tabChuyển tab, hủy lựa chọn
   * @param key
   */
  function onTabChange() {
    onClearSelectedDepart();
    onClearSelectedPosition();
    onClearSelectedUserGroup();
  }
  // Khởi tạo và tải tất cả dữ liệu
  loadRootDepart();
  loadPositionList();
  loadUserGroupList();
</script>

<style scoped>
  :global(.tabs-select-user-modal .jeecg-modal-content > .scroll-container > .scrollbar__wrap) {
    margin-bottom: 0 !important;
    overflow-y: hidden !important;
  }

  :global(.tabs-select-user-modal .jeecg-modal-content > .scroll-container > .scrollbar__bar.is-vertical) {
    display: none;
  }

  .left-select-card :deep(.ant-card-body) {
    height: calc(100vh - 140px);
  }

  .left-select-tabs {
    height: 100%;
  }

  .left-select-tabs :deep(.ant-tabs-content-holder),
  .left-select-tabs :deep(.ant-tabs-content),
  .left-select-tabs :deep(.ant-tabs-tabpane) {
    min-height: 0;
    height: 100%;
  }

  .left-tab-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    padding: 14px;
  }

  .position-list {
    flex: 1;
    min-height: 0;
    overflow: hidden;
  }

  .position-list :deep(.ant-spin-nested-loading),
  .position-list :deep(.ant-spin-container),
  .position-list :deep(.post-rank-tree) {
    height: 100%;
    min-height: 0;
  }

  .user-group-list {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    margin-top: 8px;
  }

  .user-group-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    margin-bottom: 4px;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: #fff;
  }

  .user-group-item:hover {
    border-color: #40a9ff;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
  }

  .user-group-item-selected {
    border-color: #1890ff;
    background-color: #f0f8ff;
    color: #1890ff;
    font-weight: 500;
  }

  .user-group-item-selected:hover {
    border-color: #096dd9;
    background-color: #e6f7ff;
  }

  .user-group-title {
    flex: 1;
    font-size: 14px;
    line-height: 1.5;
  }

  .selected-icon {
    color: #52c41a;
    font-size: 14px;
    margin-left: 8px;
  }
  .alert-info {
    margin-bottom: 5px;
  }
  /**Kiểu thanh cuộn của component cây phòng ban*/
  .tree-container {
    flex: 1;
    min-height: 0;
    overflow-y: auto;                /* Cuộn dọc */
    overflow-x: hidden;              /* Ẩn cuộn ngang */
    position: relative;              /* Định vị tương đối */
  }
  /* Bộ chọn sâu tối ưu kiểu nội bộ của component cây */
  .tree-container :deep(.ant-tree) {
    width: 100%;
    padding: 8px 4px;
  }

  .tree-container :deep(.ant-tree-list-holder) {
    overflow: visible !important;
  }
  .tree-container::-webkit-scrollbar {
    width: 6px;
  }

  .tree-container::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 3px;
  }

  .tree-container::-webkit-scrollbar-thumb {
    background: #c1c1c1;
    border-radius: 3px;
  }

  .tree-container::-webkit-scrollbar-thumb:hover {
    background: #a8a8a8;
  }
  .selected-title {
    display: inline-block; /* Phải đặt thành phần tử block mới áp dụng được giới hạn chiều rộng */
    flex: 1;
    min-width: 0;
    max-width: 180px; /* Giới hạn chiều rộng tối đa */
    overflow: hidden; /* Ẩn phần vượt quá */
    text-overflow: ellipsis; /* Hiển thị dấu ba chấm */
    white-space: nowrap; /* Không cho xuống dòng */
    vertical-align: middle; /* Căn giữa theo chiều dọc */
  }
  /* AlertCăn ngang nội dung tin nhắn */
  .alert-message-content {
    display: flex; /* Bố cục flex */
    align-items: center; /* Căn giữa theo chiều dọc */
    gap: 8px; /* Khoảng cách phần tử */
    flex-wrap: nowrap; /* Cho phép xuống dòng */
  }

  .alert-label {
    color: #000000d9; /* Màu nhãn */
    font-weight: 500; /* Chữ đậm */
    white-space: nowrap; /* Không cho xuống dòng */
  }

  .clear-link {
    flex-shrink: 0;
    color: #1890ff; /* Màu liên kết */
    text-decoration: none; /* Bỏ gạch chân */
    white-space: nowrap; /* Không cho xuống dòng */
    cursor: pointer; /* Con trỏ hình bàn tay */
  }

  .clear-link:hover {
    color: #40a9ff; /* Màu khi di chuột qua */
    text-decoration: underline; /* Gạch chân khi di chuột */
  }
</style>
