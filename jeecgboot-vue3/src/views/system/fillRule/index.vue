<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd">Thêm mới</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                <span>Xóa</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Thao tác hàng loạt</span>
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <FillRuleModal @register="registerModal" @success="reload" />
  </div>
</template>

<script name="system-fillrule" lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getFillRuleList, exportUrl, importUrl, deleteFillRule, batchDeleteFillRule, handleTest } from '/@/views/system/fillRule/fill.rule.api';
  import { columns, searchFormSchema } from '/@/views/system/fillRule/fill.rule.data';
  import { useModal } from '/@/components/Modal';
  import { ActionItem } from '/@/components/Table';
  const [registerModal, { openModal }] = useModal();
  import FillRuleModal from '/@/views/system/fillRule/FillRuleModal.vue';

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, createMessage, createSuccessModal, onExportXls, onImportXls } = useListPage({
    designScope: 'fill-rule',
    tableProps: {
      title: 'Trang quản lý quy tắc điền giá trị',
      api: getFillRuleList,
      columns: columns,
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: exportUrl,
      name: 'Danh sách quy tắc điền giá trị',
    },
    importConfig: {
      url: importUrl,
      success: () => reload(),
    },
  });
  // Đăng ký ListTable
  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

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
    console.log('record....', record);
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    console.log(12345, record);
    await deleteFillRule({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteFillRule({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * Kiểm tra chức năng
   */
  function testRule(record) {
    let params = { ruleCode: record.ruleCode };
    handleTest(params).then((res) => {
      if (res.success) {
        createSuccessModal({
          title: 'Kiểm tra chức năng quy tắc điền giá trị',
          content: 'Kết quả tạo:' + res.result,
        });
      } else {
        createMessage.warn(res.message);
      }
    });
  }

  /**
   * Chỉnh sửa
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Chỉnh sửa', onClick: handleEdit.bind(null, record) }];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Kiểm tra chức năng', onClick: testRule.bind(null, record) },
      {
        label: 'Xóa',
        color: 'error',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
