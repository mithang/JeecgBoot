<!--Thùng rác-->
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thùng rác người dùng" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa hàng loạt
              </a-menu-item>
              <a-menu-item key="2" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Khôi phục hàng loạt
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup name="tenant-recycle-bin-modal">
  import { BasicTable, TableAction } from '/@/components/Table';
  import { recycleBinPageList, deleteLogicDeleted, revertTenantLogic } from '../tenant.api';
  import { recycleColumns, searchRecycleFormSchema } from '../tenant.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Modal } from 'ant-design-vue';
  import { toRaw, unref } from 'vue';

  const { createMessage: $message } = useMessage();
  const [registerModal] = useModalInner(() => {});
  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    tableProps: {
      api: recycleBinPageList,
      columns: recycleColumns,
      size: 'small',
      formConfig: {
        schemas: searchRecycleFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      ellipsis: true,
    },
  });
  const emit = defineEmits(['success', 'register']);
  //Đăng ký dữ liệu table
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  //Lấy sự kiện của thanh thao tác
  function getTableAction(record) {
    return [
      {
        label: 'Khôi phục',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Xác nhận khôi phục?',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Xóa vĩnh viễn',
        icon: 'ant-design:scissor-outlined',
        popConfirm: {
          title: 'Xác nhận xóa vĩnh viễn?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Khôi phục
   * @param record
   */
  function handleRevert(record) {
    revertTenantLogic({ ids: record.id }, handleSuccess);
    emit('success');
  }

  /**
   * Làm mới bảng thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Xóa vĩnh viễn
   * @param record
   */
  async function handleDelete(record) {
    await deleteLogicDeleted({ ids: record.id }, handleSuccess);
  }

  /**
   * Xóa vĩnh viễn hàng loạt
   */
  function batchHandleDelete() {
    Modal.confirm({
      title: 'Xóa vĩnh viễn',
      content: 'Xác nhận xóa vĩnh viễn?',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        deleteLogicDeleted({ ids: toRaw(unref(selectedRowKeys)).join(',') }, handleSuccess);
      },
    });
  }

  /**
   * Khôi phục hàng loạt
   */
  function batchHandleRevert() {
    Modal.confirm({
      title: 'Khôi phục',
      content: 'Xác nhận khôi phục?',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        revertTenantLogic({ ids: toRaw(unref(selectedRowKeys)).join(',') }, handleSuccess);
        emit('success');
      },
    });
  }
</script>

<style lang="less" scoped>
  :deep(.ant-popover-inner-content) {
    width: 185px !important;
  }
</style>
