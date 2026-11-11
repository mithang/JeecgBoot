<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              Xóa
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >Thao tác hàng loạt
          <Icon icon="mdi:chevron-down"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <!--Bảng người dùng của vai trò-->
  <RoleUserTable @register="roleUserDrawer" />
  <!--Ngăn kéo chỉnh sửa vai trò-->
  <RoleDrawer @register="registerDrawer" @success="reload" :showFooter="showFooter" />
  <!--Chi tiết vai trò-->
  <RoleDesc @register="registerDesc"></RoleDesc>
  <!--Ngăn kéo cấp quyền menu của vai trò-->
  <RolePermissionDrawer @register="rolePermissionDrawer" />
  <!--Cấu hình trang chủ của vai trò-->
  <RoleIndexModal @register="registerIndexModal" />
</template>
<script lang="ts" name="system-role" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import RoleDrawer from './components/RoleDrawer.vue';
  import RoleDesc from './components/RoleDesc.vue';
  import RolePermissionDrawer from './components/RolePermissionDrawer.vue';
  import RoleIndexModal from './components/RoleIndexModal.vue';
  import RoleUserTable from './components/RoleUserTable.vue';
  import { columns, searchFormSchema } from './role.data';
  import { list, deleteRole, batchDeleteRole, getExportUrl, getImportUrl } from './role.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  const showFooter = ref(true);
  const [roleUserDrawer, { openDrawer: openRoleUserDrawer }] = useDrawer();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerIndexModal, { openModal: openIndexModal }] = useModal();
  const [rolePermissionDrawer, { openDrawer: openRolePermissionDrawer }] = useDrawer();
  const [registerDesc, { openDrawer: openRoleDesc }] = useDrawer();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
    designScope: 'role-template',
    tableProps: {
      title: 'Danh sách vai trò hệ thống',
      api: list,
      columns: columns,
      formConfig: {
        // Giải thích logic mã: 【QQYUN-5873】Nhãn (label) của khu vực truy vấn mặc định căn trái
        labelWidth:65,
        rowProps: { gutter: 24 },
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      rowSelection: null,
      //Tùy chỉnh sắp xếp mặc định
      defSort: {
        column: 'id',
        order: 'desc',
      },
    },
    exportConfig: {
      name: 'Danh sách vai trò',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Thêm mới
   */
  function handleCreate() {
    showFooter.value = true;
    openDrawer(true, {
      isUpdate: false,
    });
  }
  /**
   * Chỉnh sửa
   */
  function handleEdit(record: Recordable) {
    showFooter.value = true;
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Chi tiết
   */
  function handleDetail(record) {
    showFooter.value = false;
    openRoleDesc(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteRole({ id: record.id }, reload);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteRole({ ids: selectedRowKeys.value }, reload);
  }
  /**
   * Hộp thoại cấp quyền vai trò
   */
  function handlePerssion(record) {
    openRolePermissionDrawer(true, { roleId: record.id });
  }
  /**
   * Hộp thoại cấu hình trang chủ
   */
  function handleIndexConfig(roleCode) {
    openIndexModal(true, { roleCode });
  }
  /**
   * Người dùng của vai trò
   */
  function handleUser(record) {
    //onSelectChange(selectedRowKeys)
    openRoleUserDrawer(true, record);
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
      {
        label: 'Cấp quyền',
        onClick: handlePerssion.bind(null, record),
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
        },
      },
    ];
  }
</script>
