<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">Thêm mới</a-button>
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
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <PositionModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="TenantPositionList" setup>
  import { onMounted } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getPositionList, deletePosition, batchDeletePosition, getExportUrl, getImportUrl } from './position.api';
  import { columns, searchFormSchema } from './position.data';
  import PositionModal from './PositionModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { tenantSaasMessage } from '@/utils/common/compUtils';
  import { getTenantId } from '@/utils/auth';
  const [registerModal, { openModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { onExportXls, onImportXls, tableContext } = useListPage({
    tableProps: {
      title: 'Danh sách chức danh đơn vị thuê',
      api: getPositionList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 180,
      },
      showIndexColumn: true,
      defSort: {
        column: '',
        order: '',
      },
      beforeFetch(params) {
        return Object.assign({ tenantId: getTenantId() }, params);
      },
    },
    exportConfig: {
      name: 'Danh sách chức danh đơn vị thuê',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
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

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deletePosition({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeletePosition({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  onMounted(() => {
    //Thông tin thông báo
    tenantSaasMessage('Chức danh đơn vị thuê');
  });
</script>
