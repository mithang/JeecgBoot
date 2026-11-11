<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thùng rác định tuyến" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable">
      <template #status="{ record, text }">
        <a-tag color="pink" v-if="text == 0">Vô hiệu hóa</a-tag>
        <a-tag color="#87d068" v-if="text == 1">Bình thường</a-tag>
      </template>
      <!-- Thanh thao tác -->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { columns } from '../route.data';
  import { deleteRouteList, putRecycleBin, deleteRecycleBin } from '../route.api';
  // Khai báo Emits
  const emit = defineEmits(['success', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal] = useModalInner(() => {
    checkedKeys.value = [];
  });
  // Đăng ký dữ liệu bảng
  const [registerTable, { reload }] = useTable({
    rowKey: 'id',
    api: deleteRouteList,
    columns: columns,
    striped: true,
    useSearchForm: false,
    bordered: true,
    showIndexColumn: false,
    pagination: false,
    tableSetting: { fullScreen: true },
    canResize: false,
    actionColumn: {
      width: 150,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: 'right',
    },
  });

  /**
   * Sự kiện khôi phục
   */
  async function handleRevert(record) {
    await putRecycleBin({ ids: record.id }, reload);
    emit('success');
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteRecycleBin({ ids: record.id }, reload);
  }

  // Lấy sự kiện thanh thao tác
  function getTableAction(record) {
    return [
      {
        label: 'Thu hồi',
        icon: 'ant-design:redo-outlined',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn thu hồi không?',
          confirm: handleRevert.bind(null, record),
        },
      },
      {
        label: 'Xóa hoàn toàn',
        icon: 'ant-design:scissor-outlined',
        color: 'error',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
