<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">Gói mặc định của đơn vị thuê
        </a-button>
        <a-button
          v-if="selectedRowKeys.length > 0"
          preIcon="ant-design:delete-outlined"
          type="primary"
          @click="handlePackBatch"
          style="margin-right: 5px"
        >Xóa hàng loạt
        </a-button>
        <span style="color: #666; font-size: 14px; margin-left: 12px; margin-top: 4px; display: inline-block;">
          Gói mặc định của đơn vị thuê là gói cơ bản tự động có khi tạo mới đơn vị thuê, tất cả đơn vị thuê đều sẽ kế thừa các quyền của những gói này
        </span>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <!--  Gói dịch vụ  -->
    <TenantPackMenuModal @register="registerPackMenuModal" @success="handleSuccess"/>
    <PackPermissionDrawer @register="registerPackPermDrawer" @success="handleSuccess"/>
  </div>
</template>
<script lang="ts" name="tenant-default-pack" setup>
  import { ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { deleteTenantPack, packList } from '../tenant.api';
  import { defalutPackColumns, defaultPackFormSchema } from "../tenant.data";
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import PackPermissionDrawer from './PackPermissionDrawer.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useUserStore } from '/@/store/modules/user';
  import {Modal} from "ant-design-vue";
  import {useDrawer} from "@/components/Drawer";

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerPackMenuModal, { openModal: packModal }] = useModal();
  const userStore = useUserStore();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      api: packList,
      columns: defalutPackColumns,
      formConfig: {
        schemas: defaultPackFormSchema,
      },
      beforeFetch: (params) => {
        return Object.assign(params, { packType: 'default' });
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  const [registerPackPermDrawer, { openDrawer: openPackPermDrawer }] = useDrawer();

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Cấp quyền',
        onClick: handleRolePrem.bind(null, record),
      },
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa gói dịch vụ của đơn vị thuê?',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ];
  }

  /**
   * Chỉnh sửa gói dịch vụ
   */ 
  function handleAdd() {
    packModal(true, {
      isUpdate: false,
      packType:'default',
      showFooter: true
    });
  }
  
  
  /**
   * Xóa gói dịch vụ mặc định
   */ 
  async function handleDelete(id) {
    await deleteTenantPack({ ids: id }, handleSuccess);
  }
  /**
   * Chỉnh sửa
   */
  function handleEdit(record) {
    packModal(true, {     
      isUpdate: true,
      record: record,
      packType:'default',
      showFooter: true
    });
  }

  /**
   * Thêm mới gói dịch vụ
   */
  async function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Vui lòng chọn một mục');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
    });
  }

  /**
   * Sự kiện callback sau khi xóa thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Xóa hàng loạt gói dịch vụ
   */
  async function handlePackBatch() {
    Modal.confirm({
      title: 'Xóa gói dịch vụ của đơn vị thuê',
      content: 'Xóa gói dịch vụ của đơn vị thuê?',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        await deleteTenantPack({ ids: selectedRowKeys.value.join(',')}, handleSuccess);
      }
    })
  }

  /**
   * Cấp quyền
   * 
   * @param record
   */
  function handleRolePrem(record) {
    openPackPermDrawer(true,{
      packId: record.id,
      permissionIds: record.permissionIds
    })
  }
</script>
