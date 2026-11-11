<template>
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
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
      <div style="margin-left: 10px;margin-top: 5px">Đơn vị thuê đang đăng nhập: <span class="tenant-name">{{loginTenantName}}</span> </div>
    </template>
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <!--Bảng người dùng của vai trò-->
  <RoleUserTable @register="roleUserDrawer" :disableUserEdit="true"/>
  <!--Ngăn kéo chỉnh sửa vai trò-->
  <RoleDrawer @register="registerDrawer" @success="reload" :showFooter="showFooter" />
  <!--Chi tiết vai trò-->
  <RoleDesc @register="registerDesc"></RoleDesc>
</template>
<script lang="ts" name="tenant-role-list" setup>
  import { onMounted, ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useModal } from '/@/components/Modal';
  import RoleDesc from './components/RoleDesc.vue';
  import RoleDrawer from './components/RoleDrawer.vue';
  import RoleUserTable from './components/RoleUserTable.vue';
  import { columns, searchTenantRoleFormSchema } from './role.data';
  import { listByTenant, deleteRole, batchDeleteRole, getExportUrl, getImportUrl } from './role.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getLoginTenantName } from "/@/views/system/tenant/tenant.api";
  import { tenantSaasMessage } from "@/utils/common/compUtils";
  
  const showFooter = ref(true);
  const [roleUserDrawer, { openDrawer: openRoleUserDrawer }] = useDrawer();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerModal, { openModal }] = useModal();
  const [registerDesc, { openDrawer: openRoleDesc }] = useDrawer();
  
  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
    designScope: 'role-template',
    tableProps: {
      title: 'Danh sách vai trò đơn vị thuê',
      api: listByTenant,
      columns: columns,
      formConfig: {
        schemas: searchTenantRoleFormSchema,
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

  const loginTenantName = ref<string>('');
  
  getTenantName();
  
  async function getTenantName(){
    loginTenantName.value = await getLoginTenantName();
  }

  onMounted(()=>{
    tenantSaasMessage('Vai trò đơn vị thuê')
  })
</script>

<style scoped lang="less">
  .tenant-name{
    text-decoration:underline;
    margin: 5px;
    font-size: 15px;
  }
</style>
