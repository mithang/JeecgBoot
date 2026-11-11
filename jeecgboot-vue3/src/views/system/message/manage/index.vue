<template>
  <div :class="prefixCls">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
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
    <ManageDrawer @register="registerDrawer" />
  </div>
</template>

<script lang="ts" setup name="message-manage">
  import { unref, computed } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import ManageDrawer from './ManageDrawer.vue';
  import { Api, list, deleteBatch } from './manage.api';
  import { columns, searchFormSchema } from './manage.data';

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'message-manage',
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
  const [registerTable, { reload, setLoading }, { rowSelection, selectedRowKeys }] = tableContext;
  const showBatchBtn = computed(() => selectedRowKeys.value.length > 0);

  const [registerDrawer, { openDrawer }] = useDrawer();

  function onDetail(record) {
    openDrawer(true, { record: record });
  }

  function onDelete(record) {
    if (record) {
      doDeleteDepart([record.id], false);
    }
  }

  async function onDeleteBatch() {
    try {
      await doDeleteDepart(selectedRowKeys);
      selectedRowKeys.value = [];
    } finally {
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

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Chi tiết', onClick: onDetail.bind(null, record) }];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
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
</script>

<style lang="less">
  @import 'index';
</style>
