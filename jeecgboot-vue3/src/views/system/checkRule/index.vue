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

    <CheckRuleTestModal @register="testModal"></CheckRuleTestModal>
    <CheckRuleModal @register="registerModal" @success="reload"></CheckRuleModal>
  </div>
</template>

<script name="system-fillrule" lang="ts" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { useModal } from '/@/components/Modal';
  import {
    getCheckRuleList,
    exportUrl,
    importUrl,
    deleteCheckRule,
    batchDeleteCheckRule,
    validateCheckRule,
  } from '/@/views/system/checkRule/check.rule.api';
  import { columns, searchFormSchema } from '/@/views/system/checkRule/check.rule.data';
  import { ActionItem } from '/@/components/Table';

  import CheckRuleTestModal from '/@/views/system/checkRule/CheckRuleTestModal.vue';
  const [testModal, { openModal: openTestModal }] = useModal();

  import CheckRuleModal from '/@/views/system/checkRule/CheckRuleModal.vue';
  const [registerModal, { openModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, createMessage, onExportXls, onImportXls, createSuccessModal } = useListPage({
    designScope: 'check-rule',
    tableProps: {
      title: 'Trang quản lý quy tắc kiểm tra mã',
      api: getCheckRuleList,
      columns: columns,
      showIndexColumn: true,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: exportUrl,
      name: 'Danh sách quy tắc kiểm tra mã',
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
    await deleteCheckRule({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteCheckRule({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * Kiểm tra chức năng
   */
  function testRule(record) {
    openTestModal(true, { ruleCode: record.ruleCode });
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
