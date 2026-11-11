<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">Thêm mới</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" /> Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>Thao tác hàng loạt<Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <template #status="{ text }">
        <a-tag color="pink" v-if="text == 0">Vô hiệu hóa</a-tag>
        <a-tag color="#87d068" v-if="text == 1">Kích hoạt</a-tag>
      </template>
      <template #relationType="{ text, record }">
        <span>{{ record.roleCode == 'DEF_INDEX_ALL' ? 'Mặc định toàn cục' : text }}</span>
      </template>
      <template #roleCode="{ text, record }">
        <span>{{ record.roleCode == 'DEF_INDEX_ALL' ? 'Trang chủ mặc định của menu' : text || record.roleCode }}</span>
      </template>
    </BasicTable>
    <!--Cấu hình trang chủ của vai trò-->
    <HomeConfigModal @register="register" @success="reload" />
  </div>
</template>
<script lang="ts" name="home-config" setup>
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import HomeConfigModal from './components/HomeConfigModal.vue';
  import { columns, searchFormSchema } from './home.data';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { list, deleteIndex, batchDelete, changeStatus } from './home.api';

  const { createMessage } = useMessage();

  //Cấu hình hộp thoại
  const [register, { openModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { tableContext } = useListPage({
    designScope: 'home-config',
    tableProps: {
      title: 'Cấu hình trang chủ',
      api: list,
      columns: columns,
      formConfig: {
        labelAlign: 'left',
        labelWidth: 80,
        schemas: searchFormSchema,
        baseRowStyle: {
          marginLeft: '2px',
        },
      },
      actionColumn: {
        width: 140,
      },
      //Tùy chỉnh sắp xếp mặc định
      defSort: {
        column: 'id',
        order: 'desc',
      },
    },
  });
  const [registerTable, { reload, clearSelectedRowKeys }, { rowSelection, selectedRowKeys }] = tableContext;

  /**
   * Sự kiện thêm mới
   */
  async function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record) {
    openModal(true, {
      isUpdate: true,
      values: record,
    });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteIndex({ id: record.id }, () => {
      reload();
    });
  }
  /**
   * Sự kiện bật/tắt
   */
  async function handleChangeStatus(record) {
    const newStatus = record.status == 1 ? 0 : 1;
    await changeStatus({ id: record.id, status: newStatus }, () => {
      createMessage.success(newStatus == 1 ? 'Kích hoạt thành công' : 'Vô hiệu hóa thành công');
      reload();
    });
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, () => {
      clearSelectedRowKeys();
      reload();
    });
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: record.status == 1 ? 'Vô hiệu hóa' : 'Kích hoạt',
        popConfirm: {
          title: `Có phải${record.status == 1 ? 'Vô hiệu hóa' : 'Kích hoạt'}？`,
          confirm: handleChangeStatus.bind(null, record),
        },
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
