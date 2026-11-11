<template>
  <BasicDrawer @register="registerBaseDrawer" title="Người dùng của nhóm người dùng" width="800" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handleSelect"> Thêm người dùng</a-button>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="bx:bx-unlink"></Icon>
                Hủy bỏ liên kết
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--Ngăn kéo thao tác người dùng-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--Hộp thoại chọn người dùng-->
    <UseSelectModal @register="registerModal" @select="selectOk" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, watch, unref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import UserDrawer from '../../user/UserDrawer.vue';
  import UseSelectModal from '@/views/system/role/components/UseSelectModal.vue';
  import { userList, deleteUserGroup, batchDeleteUserGroup, addUserGroup } from '../ugroup.api';
  import { userColumns, searchUserFormSchema } from '../ugroup.data';

  const emit = defineEmits(['register', 'hideUserList']);
  const props = defineProps({
    disableUserEdit: { type: Boolean, default: false },
  });

  const checkedKeys = ref<Array<string | number>>([]);
  const groupId = ref('');
  const [registerBaseDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    groupId.value = data.id;
    setProps({ searchInfo: { groupId: data.id } });
    reload();
  });
  //Đăng kýdrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //Đăng kýdrawer
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload, updateTableDataRecord, setProps }] = useTable({
    title: 'Danh sách người dùng',
    api: userList,
    columns: userColumns,
    formConfig: {
      // Giải thích logic mã: 【QQYUN-5685】3、Trong vai trò của đơn vị thuê,Tìm kiếm hiển thị ở bên trái
      labelWidth: 60,
      schemas: searchUserFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    // 【issues/1064】Thuộc về cài đặt cột cacheKey
    tableSetting: { fullScreen: true, cacheKey: 'group_user_table' },
    canResize: false,
    rowKey: 'id',
    actionColumn: {
      width: 180,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * Cấu hình cột chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[], selectionRows) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * Chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteUserGroup({ userId: record.id, groupId: groupId.value }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteUserGroup({ userIds: checkedKeys.value.join(','), groupId: groupId.value }, () => {
      // Giải thích logic mã: 【TV360X-1655】Sau khi hủy liên kết hàng loạt thì xóa các bản ghi đã chọn
      reload();
      checkedKeys.value = [];
    });
  }

  /**
   * Callback thành công
   */
  function handleSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }
  /**
   * Chọn người dùng hiện có
   */
  function handleSelect() {
    openModal(true);
  }
  /**
   * Thêm người dùng hiện có
   */
  async function selectOk(val) {
    await addUserGroup({ groupId: groupId.value, userIdList: val }, reload);
  }
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        ifShow: () => !props.disableUserEdit,
      },
      {
        label: 'Hủy bỏ liên kết',
        popConfirm: {
          title: 'Xác nhận hủy liên kết?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped>
  /*update-begin---author:wangshuai ---date:20230703  for：【QQYUN-5685】3、Trong vai trò của đơn vị thuê,Tìm kiếm hiển thị ở bên trái*/
  :deep(.ant-form-item-control-input-content) {
    text-align: left;
  }
  /*update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】3、Trong vai trò của đơn vị thuê,Tìm kiếm hiển thị ở bên trái*/
</style>
