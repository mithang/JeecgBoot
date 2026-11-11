<template>
  <div class="ocr-page p-2">
    <AiOcrAnalysisModal />
<!--    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      &lt;!&ndash;Slot:tiêu đề table&ndash;&gt;
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">Thêm mới</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>-->
<!--    <AiOcrModal @register="registerModal" @success="reload()"></AiOcrModal>-->
  </div>
</template>
<script lang="ts" name="site-list" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { list, deleteOcrById } from './AiOcr.api';
  import { columns } from './AiOcr.data';
  import AiOcrModal from './components/AiOcrModal.vue';
  import AiOcrAnalysisModal from './components/AiOcrAnalysisModal.vue';

  const [registerModal, { openModal }] = useModal();
  const [registerAnalysisModal, { openModal: openAnalysisModal }] = useModal();
  
  // Tham số, phương thức chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: list,
      columns,
      useSearchForm: false,
      pagination: false,
      actionColumn: {
        width: 120,
      },
      canResize: false,
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Thêm mới
   */
  function handleCreate() {
    openModal(true, {});
  }

  /**
   * Chỉnh sửa
   */
  function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * Xóa
   */
  async function handleDelete(id) {
    await deleteOcrById({ id: id });
    reload();
  }

  /**
   * Phân tích
   * @param record
   */
  function handleAnalysis(record){
    openAnalysisModal(true,{
      isUpdate: true,
      record
    })
  }
  
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Phân tích',
        onClick: handleAnalysis.bind(null, record),
      },
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          placement: 'left',
          confirm: handleDelete.bind(null, record.id),
        },
      },
    ];
  }
</script>
<style lang="less" scoped>
  .ocr-page{
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
</style>
