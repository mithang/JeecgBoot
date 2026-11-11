<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="onAdd">Thêm mới</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>
        <a-dropdown v-if="showBatchBtn">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteBatch">
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
    <TemplateModal @register="registerModal" @success="reload" />
    <TemplateTestModal @register="registerTestModal" />
  </div>
</template>

<script lang="ts" setup name="message-template">
  import { unref, computed, toRaw } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TemplateModal from './TemplateModal.vue';
  import TemplateTestModal from './TemplateTestModal.vue';
  import { Api, saveOrUpdate, list, deleteBatch } from './template.api';
  import { columns, searchFormSchema } from './template.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, onExportXls, onImportXls, tableContext } = useListPage({
    designScope: 'message-template',
    tableProps: {
      title: 'Dữ liệu danh sách mẫu của trung tâm tin nhắn',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
    },
    exportConfig: {
      url: Api.exportXls,
      name: 'Danh sách mẫu của trung tâm tin nhắn',
    },
    importConfig: {
      url: Api.importXls,
      success: () => reload(),
    },
  });

  // Đăng ký ListTable
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  const [registerModal, { openModal }] = useModal();
  const [registerTestModal, testModal] = useModal();
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  function onAdd() {
    openModal(true, {
      title: 'Thêm mới mẫu tin nhắn',
      isUpdate: false,
      showFooter: true,
      record: {},
    });
  }

  function onEdit(record) {
    if (record.useStatus === '1') {
      createMessage.warning('Mẫu này đã được ứng dụng, cấm chỉnh sửa!');
      return;
    }
    openModal(true, {
      title: 'Sửa mẫu tin nhắn',
      isUpdate: true,
      record: record,
      showFooter: true,
    });
  }

  function onDelete(record) {
    if (record) {
      // Giải thích logic mã: VUEN-1652【bug】Không cho phép xóa khi đang ở trạng thái ứng dụng
      if(record.useStatus == '1'){
        createMessage.warning('Mẫu này đã được ứng dụng, cấm xóa!');
        return;
      }
      doDeleteDepart([record.id], false);
    }
  }

  /**
   * Xóa hàng loạt theo ids
   * @param idListRef array
   * @param confirm Có hiển thị hộp thoại nhắc xác nhận không
   */
  async function doDeleteDepart(idListRef, confirm = true) {
    const idList = unref(idListRef);
    if (idList.length > 0) {
      try {
        setLoading(true);
        await deleteBatch({ ids: idList.join(',') }, confirm);
        await reload();
      } finally {
        setLoading(false);
      }
    }
  }

  async function onDeleteBatch() {
    try {
      // Giải thích logic mã: VUEN-1652【bug】Không cho phép xóa khi đang ở trạng thái ứng dụng
      let arr = toRaw(selectedRows.value);
      let temp = arr.filter(item=>item.useStatus=='1')
      if(temp.length>0){
        createMessage.warning('Mẫu đã chọn đã được ứng dụng, cấm xóa!');
        return;
      }
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
    }
  }

  // Gửi tin nhắn kiểm tra
  function onSendTest(record) {
    testModal.openModal(true, { record });
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    // Giải thích logic mã: [VUEN-2807]Thêm chức năng xem cho mẫu tin nhắn------------
    return [{ label: 'Xem', onClick: handleDetail.bind(null, record)}, { label: 'Chỉnh sửa', onClick: onEdit.bind(null, record) }];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Ứng dụng', onClick: handleUse.bind(null, record) },
      { label: 'Ngừng sử dụng', onClick: handleNotUse.bind(null, record) },
      { label: 'Gửi kiểm tra', onClick: onSendTest.bind(null, record) },
      {
        label: 'Xóa',
        color: 'error',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn xóa không?',
          confirm: onDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Ứng dụng
   */
  async function handleUse(record) {
    let param = { id: record.id, useStatus: '1' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * Ngừng sử dụng
   */
  async function handleNotUse(record) {
    let param = { id: record.id, useStatus: '0' };
    await saveOrUpdate(param, true);
    await reload();
  }

  /**
   * Xem
   * @param record
   */
  function handleDetail(record) {
    openModal(true,{
      title: "Chi tiết mẫu tin nhắn",
      isUpdate: true,
      showFooter: false,
      record:record
    })
  }
</script>

<style lang="less">
  @import 'index';
</style>
