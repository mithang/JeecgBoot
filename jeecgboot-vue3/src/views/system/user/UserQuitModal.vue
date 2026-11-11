<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thông tin nhân viên nghỉ việc" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Hủy hàng loạt
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

<script lang="ts" setup name="user-quit-modal">
  import { ref, toRaw, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getQuitList, putCancelQuit, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { Modal } from 'ant-design-vue';
  import { defHttp } from '/@/utils/http/axios';

  const { createConfirm } = useMessage();
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //Đăng ký dữ liệu table
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: getQuitList,
      columns: recycleColumns,
      rowKey: 'id',
      canResize: false,
      useSearchForm: false,
      actionColumn: {
        width: 120,
      },
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Sự kiện hủy nghỉ việc
   * @param record
   */
  async function handleCancelQuit(record) {
    await putCancelQuit({ userIds: record.id, usernames: record.username }, reload);
    emit('success');
  }
  /**
   * Sự kiện hủy nghỉ việc hàng loạt
   */
  function batchHandleRevert() {
    Modal.confirm({
      title: 'Hủy nghỉ việc',
      content: 'Khi hủy nghỉ việc, người bàn giao cũng bị xóa',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        let rowValue = selectedRows.value;
        let rowData: any = [];
        for (const value of rowValue) {
          rowData.push(value.username);
        }
        handleCancelQuit({ id: toRaw(unref(selectedRowKeys)).join(','), username: rowData.join(',') });
      },
    });
  }

  //Lấy sự kiện của thanh thao tác
  function getTableAction(record) {
    return [
      {
        label: 'Hủy nghỉ việc',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Có hủy nghỉ việc không,Khi hủy nghỉ việc, người bàn giao cũng bị xóa',
          confirm: handleCancelQuit.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped lang="less">
:deep(.ant-popover-inner-content){
  width: 185px !important;
}
</style>
