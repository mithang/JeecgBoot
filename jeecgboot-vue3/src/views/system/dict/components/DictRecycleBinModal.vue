<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thùng rác từ điển" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa hàng loạt
              </a-menu-item>
              <a-menu-item key="2" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                Thu hồi hàng loạt
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
<script lang="ts" setup>
  import { ref, toRaw } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { recycleBincolumns } from '../dict.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin, batchPutRecycleBin, batchDeleteRecycleBin } from '../dict.api';
  const { createConfirm } = useMessage();
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { setModalProps, closeModal }] = useModalInner(() => {
    checkedKeys.value = [];
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload }] = useTable({
    rowKey: 'id',
    api: getRecycleBinList,
    columns: recycleBincolumns,
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 100,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  /**
   * Cấu hình cột chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }
  /**
   * Sự kiện khôi phục
   */
  async function handleRevert(record) {
    await putRecycleBin(record.id, reload);
    emit('success');
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteRecycleBin(record.id, reload);
  }
  /**
   * Sự kiện khôi phục hàng loạt
   */
  function batchHandleRevert() {
    createConfirm({
      iconType: 'warning',
      title: 'Thu hồi hàng loạt',
      content: 'Bạn có chắc muốn thu hồi từ điển đã chọn không?',
      onOk: () => batchPutRecycleBin({ ids: toRaw(checkedKeys.value).join(',') }, () => {
        // Giải thích logic mã: 【TV360X-1663】Bổ sung chức năng hàng loạt cho thu hồi từ điển dữ liệu
        reload();
        checkedKeys.value = [];
        emit('success');
      }),
    });
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: 'Xóa hàng loạt',
      content: 'Bạn có chắc muốn xóa vĩnh viễn từ điển đã chọn không? Sau khi xóa sẽ không thể khôi phục!',
      onOk: () => batchDeleteRecycleBin({ ids: toRaw(checkedKeys.value).join(',') }, () => {
        // Giải thích logic mã: 【TV360X-1663】Bổ sung chức năng hàng loạt cho thu hồi từ điển dữ liệu
        checkedKeys.value = [];
        reload();
      }),
    });
  }
  //Lấy sự kiện của thanh thao tác
  function getTableAction(record) {
    return [
      {
        label: 'Thu hồi',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Xác nhận thu hồi?',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Xóa vĩnh viễn',
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
