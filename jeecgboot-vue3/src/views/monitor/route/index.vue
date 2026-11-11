<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :indexColumnProps="indexColumnProps">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">Thêm mới</a-button>
        <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined"> Thùng rác</a-button>
      </template>
      <template #status="{ record, text }">
        <a-tag color="pink" v-if="text == 0">Vô hiệu hóa</a-tag>
        <a-tag color="#87d068" v-if="text == 1">Bình thường</a-tag>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <RouteModal @register="registerDrawer" @success="reload" />
    <!--Popup thùng rác-->
    <RouteRecycleBinModal @register="registerRecycleModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-route" setup>
  import { ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getRouteList, deleteRoute, copyRoute } from './route.api';
  import { columns } from './route.data';
  import RouteModal from './RouteModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import RouteRecycleBinModal from './components/RouteRecycleBinModal.vue';
  const { createMessage } = useMessage();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const checkedKeys = ref<Array<string | number>>([]);

  // Modal thùng rác
  const [registerRecycleModal, { openModal: openRecycleModal }] = useModal();
  // Tham số, phương thức chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'router-template',
    tableProps: {
      title: 'Danh sách định tuyến',
      api: getRouteList,
      useSearchForm: false,
      columns: columns,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
  /**
   * Cấu hình cột số thứ tự
   */
  const indexColumnProps = {
    dataIndex: 'index',
    width: '15px',
  };

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Sao chép',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn sao chép không?',
          confirm: handleCopy.bind(null, record),
        },
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }

  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openDrawer(true, {
      isUpdate: false,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Sao chép
   */
  async function handleCopy(record) {
    await copyRoute({ id: record.id }, reload);
    createMessage.success('Sao chép thành công');
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteRoute({ id: record.id }, reload);
  }
</script>
