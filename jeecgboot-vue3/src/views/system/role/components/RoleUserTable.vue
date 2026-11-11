<template>
  <BasicDrawer @register="registerBaseDrawer" title="Người dùng của vai trò" width="800" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate" v-if="!disableUserEdit"> Thêm mới người dùng</a-button>
        <a-button type="primary" @click="handleSelect"> Người dùng hiện có</a-button>

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
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/src/components/Drawer';
  import { useModal } from '/src/components/Modal';
  import UserDrawer from '../../user/UserDrawer.vue';
  import UseSelectModal from './UseSelectModal.vue';
  import { userList, deleteUserRole, batchDeleteUserRole, addUserRole } from '../role.api';
  import { userColumns, roleUserSearchFormSchema } from '../role.data';
  import { getUserRoles } from '../../user/user.api';

  const emit = defineEmits(['register', 'hideUserList']);
  const props = defineProps({
    disableUserEdit: {type:Boolean,default:false}
  })
  
  const checkedKeys = ref<Array<string | number>>([]);
  const roleId = ref('');
  const [registerBaseDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    roleId.value = data.id;
    setProps({ searchInfo: { roleId: data.id } });
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
      schemas: roleUserSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    // 【issues/1064】Thuộc về cài đặt cột cacheKey
    tableSetting: { fullScreen: true, cacheKey: 'role_user_table' },
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
   * Thêm mới
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      selectedroles: [roleId.value],
      isRole: true,
    });
  }
  /**
   * Chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    try {
      const userRoles = await getUserRoles({ userid: record.id });
      if (userRoles && userRoles.length > 0) {
        record.selectedroles = userRoles;
      }
    } catch (e) {
      console.log(e);
    }
    openDrawer(true, {
      record,
      isUpdate: true,
      isRole: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteUserRole({ userId: record.id, roleId: roleId.value }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteUserRole({ userIds: checkedKeys.value.join(','), roleId: roleId.value }, () => {
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
    await addUserRole({ roleId: roleId.value, userIdList: val }, reload);
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
  :deep(.ant-form-item-control-input-content){
    text-align: left;
  }
  /*update-end---author:wangshuai ---date:20230703  for：【QQYUN-5685】3、Trong vai trò của đơn vị thuê,Tìm kiếm hiển thị ở bên trái*/
</style>
