<template>
  <div>
    <!--Bảng trích dẫn-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tiêu đề table-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới</a-button>
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
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- Khu vực biểu mẫu -->
    <AiragExtDataModal @register="registerModal" @success="handleSuccess"></AiragExtDataModal>
    <!-- Tập đánh giá -->
    <AiragDataSetModal @register="registerDataSetModal" @success="handleSuccess"></AiragDataSetModal>
    <!-- Cấu hình bộ đánh giá -->
    <AiEvaluatorSettingModal @register="registerEvaluatorModal" @success="handleSuccess"></AiEvaluatorSettingModal>
    <!-- Cấu hình gỡ lỗi -->
    <AiEvaluatorDebugModal @register="registerDebugModal" @success="handleSuccess"></AiEvaluatorDebugModal>
    <!-- Bản ghi gọi -->
    <AiragInvokeRecordsDrawer @register="registerRecordsModal" @success="handleSuccess"></AiragInvokeRecordsDrawer>
  </div>
</template>

<script lang="ts" name="prompts-airagExtData" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import AiragExtDataModal from './components/AiragExtDataModal.vue';
  import AiragDataSetModal from './components/AiragDataSetModal.vue';
  import AiEvaluatorSettingModal from './components/AiEvaluatorSettingModal.vue';
  import AiEvaluatorDebugModal from './components/AiEvaluatorDebugModal.vue';
  import AiragInvokeRecordsDrawer from './components/AiragInvokeRecordsDrawer.vue';
  import { columns, searchFormSchema } from './AiragExtData.data';
  import { list, deleteOne, batchDelete } from './AiragExtData.api';
  //Đăng ký model
  const [registerModal, { openModal }] = useModal();
  //Đăng ký model tập đánh giá
  const [registerDataSetModal, { openModal:openDataSetModal }] = useModal();
  //Đăng ký model bộ đánh giá
  const [registerEvaluatorModal, { openModal:openEvaluatorModal }] = useModal();
  //Đăng ký model bộ đánh giá
  const [registerDebugModal, { openModal:openDebugModal }] = useModal();
  //Đăng ký model bản ghi gọi
  const [registerRecordsModal, { openDrawer:openRecordsDrawer }] = useDrawer();
  const { tableContext } = useListPage({
    tableProps: {
      title: 'airag_ext_data',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        schemas: searchFormSchema,
        autoSubmitOnEnter: true,
        showAdvancedButton: true,
        fieldMapToNumber: [],
        fieldMapToTime: [],
      },
      actionColumn: {
        width: 120,
        fixed: 'right',
      },
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Thêm mới sự kiện
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
    openEvaluatorModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }
  /**
   * Chi tiết
   */
  function handleDateSet(record: Recordable) {
    openDataSetModal(true, {
      record,
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
   * Sự kiện gỡ lỗi
   */
  async function handleDebug(record) {
    openDebugModal(true, {
      record,
    });
  }
  /**
   * Sự kiện gỡ lỗi
   */
  async function handleRecords(record) {
    openRecordsDrawer(true, {
      record,
    });
  }
  /**
   * Callback thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Cấu hình',
        onClick: handleEdit.bind(null, record),
      }
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Tập đánh giá',
        onClick: handleDateSet.bind(null, record),
      },
      {
        label: 'Gỡ lỗi',
        onClick: handleDebug.bind(null, record),
      },
      {
        label: 'Bản ghi gọi',
        onClick: handleRecords.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
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
