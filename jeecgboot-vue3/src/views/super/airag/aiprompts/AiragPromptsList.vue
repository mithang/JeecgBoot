<template>
  <div>
    <!--Bảng trích dẫn-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--slot:tiêu đề table-->
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">Thêm mới</a-button>
        <a-button type="primary" @click="openRecycleBinModal(true, {})" preIcon="ant-design:hdd-outlined">Thùng rác</a-button>

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
    <AiragPromptsModal @register="registerModal" @success="handleSuccess"></AiragPromptsModal>
    <!-- Khu vực biểu mẫu -->
    <AiPromptSettingModal @register="registerSettingModal" @success="handleSuccess"></AiPromptSettingModal>
    <!-- Thùng rác -->
    <AiragPromptsRecycleBinModal @register="registerRecycleBinModal" @success="handleSuccess"></AiragPromptsRecycleBinModal>
  </div>
</template>

<script lang="ts" name="prompts-airagPrompts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import AiragPromptsModal from './components/AiragPromptsModal.vue';
  import AiPromptSettingModal from './components/AiPromptSettingModal.vue';
  import AiragPromptsRecycleBinModal from './components/AiragPromptsRecycleBinModal.vue';
  import { columns, searchFormSchema } from './AiragPrompts.data';
  import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './AiragPrompts.api';
  //Đăng ký model
  const [registerModal, { openModal }] = useModal();
  //Đăng ký model
  const [registerSettingModal, { openModal: openSettingModal }] = useModal();
  //model thùng rác
  const [registerRecycleBinModal, { openModal: openRecycleBinModal }] = useModal();
  //Đăng ký dữ liệu table
  const { tableContext } = useListPage({
    tableProps: {
      title: 'airag_prompts',
      api: list,
      columns,
      canResize: true,
      formConfig: {
        //labelWidth: 120,
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
    exportConfig: {
      name: 'airag_prompts',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
      success: handleSuccess,
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
    openSettingModal(true, {
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
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Cấu hình',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
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
