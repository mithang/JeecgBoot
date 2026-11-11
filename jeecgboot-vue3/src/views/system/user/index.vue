<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" > Xuất (export)</a-button>
<!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls" v-auth="'system:user:import'">Nhập (import)</j-upload-button>-->
        <import-excel-progress :upload-url="getImportUrl" @success="reload"></import-excel-progress>
        <a-button type="primary" @click="openModal(true, {})" preIcon="ant-design:hdd-outlined"> Thùng rác</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
              <a-menu-item key="2" @click="batchFrozen(2)">
                <Icon icon="ant-design:lock-outlined"></Icon>
                Đóng băng
              </a-menu-item>
              <a-menu-item key="3" @click="batchFrozen(1)">
                <Icon icon="ant-design:unlock-outlined"></Icon>
                Bỏ đóng băng
              </a-menu-item>
              <a-menu-item v-if="hasPermission('system:user:resetPassword')" key="4" @click="batchResetPassword()">
                <Icon icon="ant-design:reload-outlined"></Icon>
                Đặt lại mật khẩu
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
          >Thao tác hàng loạt
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--Ngăn kéo người dùng-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--Đổi mật khẩu-->
    <PasswordModal @register="registerPasswordModal" @success="reload" />
    <!--Thùng rác-->
    <UserRecycleBinModal @register="registerModal" @success="reload" />
    <!-- Hộp thoại danh sách nhân viên nghỉ việc -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="system-user" setup>
  //tsCú pháp
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
  import UserDrawer from './UserDrawer.vue';
  import UserRecycleBinModal from './UserRecycleBinModal.vue';
  import PasswordModal from './PasswordModal.vue';
  import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
  import UserQuitModal from './UserQuitModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from './user.data';
  import { listNoCareTenant, deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch, resetPassword } from './user.api';
  import { batchPreloadDepartPathNames } from '/@/utils/common/compUtils';
  import { usePermission } from '/@/hooks/web/usePermission';
  import ImportExcelProgress from './components/ImportExcelProgress.vue';

  const { createMessage, createConfirm } = useMessage();
  const { isDisabledAuth, hasPermission } = usePermission();
  
  //Đăng kýdrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //Thùng rácmodel
  const [registerModal, { openModal }] = useModal();
  //Mật khẩumodel
  const [registerPasswordModal, { openModal: openPasswordModal }] = useModal();
  //Người đại diệnmodel
  const [registerAgentModal, { openModal: openAgentModal }] = useModal();
  //Người được ủy quyền khi nghỉ việcmodel
  const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
  //Danh sách người dùng nghỉ việcmodel
  const [registerQuitModal, { openModal: openQuitModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: 'Danh sách người dùng',
      api: listNoCareTenant,
      columns: columns,
      canResize: true,
      size: 'small',
      formConfig: {
        // labelWidth: 200,
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      afterFetch: async (result) => {
        // Tải trước hàng loạt tên đường dẫn phòng ban/vị trí, tránh việc mỗi ô lại yêu cầu API từng cái một
        await batchPreloadDepartPathNames(result);
        return result;
      },
      beforeFetch: (params) => {
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
      defSort: {
        column: "",
        order: ""
      },
    },
    exportConfig: {
      name: 'Danh sách người dùng',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //Đăng ký dữ liệu table
  const [registerTable, { reload, updateTableDataRecord, clearSelectedRowKeys }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      showFooter: true,
      tenantSaas: false,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
      tenantSaas: false,
    });
  }
  /**
   * Chi tiết
   */
  async function handleDetail(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
      tenantSaas: false,
    });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    if ('admin' == record.username) {
      createMessage.warning('Tài khoản quản trị viên không được phép thực hiện thao tác này!');
      return;
    }
    await deleteUser({ id: record.id }, reload);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    let hasAdmin = unref(selectedRows).filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning('Tài khoản quản trị viên không được phép thực hiện thao tác này!');
      return;
    }
    await batchDeleteUser({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }
  /**
   * Callback thành công
   */
  function handleSuccess() {
    reload();
  }

  /**
   * Mở hộp thoại đổi mật khẩu
   */
  function handleChangePassword(username) {
    openPasswordModal(true, { username });
  }
  /**
   * Đóng băng/Bỏ đóng băng
   */
  async function handleFrozen(record, status) {
    if ('admin' == record.username) {
      createMessage.warning('Tài khoản quản trị viên không được phép thực hiện thao tác này!');
      return;
    }
    await frozenBatch({ ids: record.id, status: status }, reload);
  }
  /**
   * Đóng băng/Bỏ đóng băng hàng loạt
   */
  function batchFrozen(status) {
    let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning('Tài khoản quản trị viên không được phép thực hiện thao tác này!');
      return;
    }
    createConfirm({
      iconType: 'warning',
      title: 'Xác nhận thao tác',
      content: 'Có phải' + (status == 1 ? 'Bỏ đóng băng' : 'Đóng băng') + 'Tài khoản đã chọn?',
      onOk: async () => {
        await frozenBatch({ ids: unref(selectedRowKeys).join(','), status: status }, reload);
      },
    });
  }
  /**
   * Đặt lại mật khẩu hàng loạt
   */
  function batchResetPassword() {
    let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
    if (unref(hasAdmin).length > 0) {
      createMessage.warning('Trong số người dùng đã chọn có quản trị viên, tài khoản quản trị viên không được phép đặt lại mật khẩu!!');
      return;
    }
    if (selectedRows.value.length > 0) {
      createConfirm({
        iconType: 'warning',
        title: 'Xác nhận thao tác',
        content: 'Có đặt lại mật khẩu của tài khoản đã chọn không?',
        onOk: async () => {
          const usernames = selectedRows.value.map((item) => item.username).join(',');
          await resetPassword({ usernames: usernames }, ()=>{reload();clearSelectedRowKeys();});
        },
      });
    }
  }


  /**
   *Đồng bộ callback của DingTalk và WeChat
   */
  function onSyncFinally({ isToLocal }) {
    // Khi đồng bộ về máy cục bộ thì làm mới dữ liệu
    if (isToLocal) {
      reload();
    }
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        // ifShow: () => hasPermission('system:user:edit'),
      },
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Mật khẩu',
        //auth: 'user:changepwd',
        onClick: handleChangePassword.bind(null, record.username),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'Đóng băng',
        ifShow: record.status == 1,
        popConfirm: {
          title: 'Bạn có chắc muốn đóng băng không?',
          confirm: handleFrozen.bind(null, record, 2),
        },
      },
      {
        label: 'Bỏ đóng băng',
        ifShow: record.status == 2,
        popConfirm: {
          title: 'Bạn có chắc muốn bỏ đóng băng không?',
          confirm: handleFrozen.bind(null, record, 1),
        },
      },
    ];
  }

</script>

<style scoped></style>
