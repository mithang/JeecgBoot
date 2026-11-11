<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'system:sys_ugroup:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới</a-button>
        <a-button type="primary" v-auth="'system:sys_ugroup:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
        <j-upload-button type="primary" v-auth="'system:sys_ugroup:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls"
          >Nhập (import)</j-upload-button
        >
        <a-tooltip placement="right" :overlayInnerStyle="{ width: '520px' }">
          <template #title>
            <div>Nhóm người dùng được áp dụng trong các chức năng sau:</div>
            <div>1. Khi xử lý quy trình, chỉ định người thao tác bước tiếp theo, chọn nhân viên theo nhóm người dùng.</div>
            <div>2. Khi component nhân viên trong trình thiết kế biểu mẫu chọn nhân viên, chọn theo nhóm người dùng.</div>
            <div>3. OAKhi tạo mới việc khởi xướng trong cộng tác làm việc ở module, chọn nhân viên theo nhóm người dùng.</div>
          </template>
          <Icon icon="ant-design:question-circle-outlined" style="color: #8c8c8c; cursor: help" />
        </a-tooltip>

        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'system:sys_ugroup:deleteBatch'"
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
    <!-- Khu vực biểu mẫu -->
    <SysUgroupModal @register="registerModal" @success="handleSuccess"></SysUgroupModal>
    <!--Bảng người dùng của vai trò-->
    <GroupUserTable @register="groupUserDrawer" />
  </div>
</template>

<script lang="ts" name="system-sysUgroup" setup>
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { columns, searchFormSchema } from './ugroup.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './ugroup.api';
  import SysUgroupModal from './components/SysUgroupModal.vue';
  import GroupUserTable from './components/GroupUserTable.vue';
  import { useDrawer } from '/@/components/Drawer';
  //Đăng kýmodel
  const [registerModal, { openModal }] = useModal();
  //Đăng ký dữ liệu table
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      title: 'Bảng nhóm người dùng',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
      },
      actionColumn: {
        width: 150,
        fixed: 'right',
      },
    },
    exportConfig: {
      name: 'Nhóm người dùng',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  const [groupUserDrawer, { openDrawer: openGroupUserDrawer }] = useDrawer();
  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }
  /**
   * Chi tiết
   */
  function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, handleSuccess);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
  }
  /**
   * Callback thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * Người dùng của nhóm người dùng
   */
  function handleUser(record) {
    openGroupUserDrawer(true, record);
  }
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Người dùng',
        onClick: handleUser.bind(null, record),
      },
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        auth: 'system:sys_ugroup:edit',
      },
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'system:sys_ugroup:delete',
      },
    ];
  }
</script>

<style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
