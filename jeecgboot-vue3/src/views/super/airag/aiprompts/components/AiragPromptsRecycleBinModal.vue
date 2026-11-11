<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thùng rác prompt" :showOkBtn="false" width="900px" destroyOnClose @fullScreen="handleFullScreen">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :scroll="scroll">
      <!--slot:tiêu đề table-->
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
                Khôi phục hàng loạt
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>Thao tác hàng loạt
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
  import { recycleColumns } from '../AiragPrompts.data';
  import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from '../AiragPrompts.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createConfirm } = useMessage();
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);

  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });

  const scroll = ref({ y: 0 });

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
      fixed: undefined,
    },
  });

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
        scroll.value = { y: scroll.value.y - 50 };
      } else if (checkedKeys.value.length == 0 && oldValue.length) {
        scroll.value = { y: scroll.value.y + 50 };
      }
    },
    { deep: true }
  );

  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };

  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * Khôi phục
   * @param record
   */
  async function handleRevert(record) {
    await putRecycleBin({ ids: record.id }, reload);
    await onSelectChange([])
    emit('success');
  }

  /**
   * Khôi phục hàng loạt
   */
  function batchHandleRevert() {
    handleRevert({ id: toRaw(unref(checkedKeys)).join(',') });
  }

  /**
   * Xóa
   * @param record
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ ids: record.id }, reload);
    await onSelectChange([])
  }

  function batchHandleDelete() {
    createConfirm({
      iconType: 'warning',
      title: 'Xóa',
      content: 'Xác nhận xóa vĩnh viễn? Sau khi xóa sẽ không thể khôi phục!',
      onOk: () => handleDelete({ id: toRaw(unref(checkedKeys)).join(',') }),
      onCancel() {},
    });
  }

  function getTableAction(record) {
    return [
      {
        label: 'Lấy lại',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Có xác nhận khôi phục hay không',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Xóa triệt để',
        icon: 'ant-design:scissor-outlined',
        popConfirm: {
          title: 'Có xác nhận xóa hay không',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
