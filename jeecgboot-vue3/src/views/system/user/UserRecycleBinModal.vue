<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thùng rác người dùng" :showOkBtn="false" width="1000px" destroyOnClose @fullScreen="handleFullScreen">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :scroll="scroll">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa hàng loạt
              </a-menu-item>
              <a-menu-item key="1" @click="batchHandleRevert">
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
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, toRaw, unref, watch } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { recycleColumns } from './user.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from './user.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createConfirm } = useMessage();
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  const scroll = ref({ y: 0 });
  //Đăng ký dữ liệu table
  const [registerTable, { reload }] = useTable({
    api: getRecycleBinList,
    columns: recycleColumns,
    rowKey: 'id',
    striped: true,
    useSearchForm: false,
    showTableSetting: false,
    clickToRowSelect: false,
    bordered: true,
    showIndexColumn: false,
    pagination: true,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 150,
      title: 'Thao tác',
      dataIndex: 'action',
      // slots: { customRender: 'action' },
      fixed: undefined,
    },
  });
  // Giải thích logic mã: 【TV360X-1657】Phân trang trong hộp thoại thùng rác người dùng hệ thống hiển thị trong vùng nhìn thấy được
  const handleFullScreen = (maximize) => {
    setTableHeight(maximize);
  };
  const setTableHeight = (maximize) => {
    const clientHeight = document.documentElement.clientHeight;
    scroll.value = {
      y: clientHeight - (maximize ? 300 : 500),
    };
  };
  setTableHeight(false);
  watch(
    checkedKeys,
    (newValue, oldValue) => {
      if (checkedKeys.value.length && oldValue.length == 0) {
        scroll.value = {
          y: scroll.value.y - 50,
        };
      } else if (checkedKeys.value.length == 0 && oldValue.length) {
        scroll.value = {
          y: scroll.value.y + 50,
        };
      }
    },
    { deep: true }
  );
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
    await putRecycleBin({ userIds: record.id }, reload);
    emit('success');
  }
  /**
   * Sự kiện khôi phục hàng loạt
   */
  function batchHandleRevert() {
    handleRevert({ id: toRaw(unref(checkedKeys)).join(',') });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ userIds: record.id }, reload);
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: 'Xóa',
      content: 'Bạn có chắc muốn xóa vĩnh viễn không? Sau khi xóa sẽ không thể khôi phục!',
      onOk: () => handleDelete({ id: toRaw(unref(checkedKeys)).join(',') }),
      onCancel() {},
    });
  }
  //Lấy sự kiện của thanh thao tác
  function getTableAction(record) {
    return [
      {
        label: 'Thu hồi',
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
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
