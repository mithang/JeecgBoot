<template>
  <div>
    <!--Bảng chính-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Slot:tableTiêu đề-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                Xóa
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >Thao tác hàng loạt
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!--Bảng contab-->
    <a-tabs defaultActiveKey="1" style="margin: 10px">
      <a-tab-pane tab="Thông tin khách hàng" key="1">
        <JeecgOrderCustomerList />
      </a-tab-pane>
      <a-tab-pane tab="Thông tin vé máy bay" key="2" forceRender>
        <JeecgOrderTicketList />
      </a-tab-pane>
    </a-tabs>
  </div>
  <!-- Vùng biểu mẫu -->
  <JeecgOrderModal @register="registerModal" @success="handleSuccess"></JeecgOrderModal>
</template>

<script lang="ts" name="tab-list" setup>
  //tsCú pháp
  import { ref, computed, unref, watch, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import JeecgOrderModal from './components/JeecgOrderModal.vue';
  import JeecgOrderCustomerList from './JeecgOrderCustomerList.vue';
  import JeecgOrderTicketList from './JeecgOrderTicketList.vue';
  import { columns, searchFormSchema } from './erplist.data';
  import { list, deleteOne, batchDelete } from './erplist.api';

  //Popupmodel
  const [registerModal, { openModal }] = useModal();

  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      tableSetting:{
        cacheKey:'erp_main'
      },
      columns: columns,
      canResize: false,
      rowSelection: { type: 'radio' },
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 180,
      },
      pagination: {
        current: 1,
        pageSize: 5,
        pageSizeOptions: ['5', '10', '20'],
      },
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  const orderId = computed(() => (unref(selectedRowKeys).length > 0 ? unref(selectedRowKeys)[0] : ''));
  //Gửi xuống orderId,Component con nhận
  provide('orderId', orderId);

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
      showFooter: true,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: true,
    });
  }

  /**
   * Chi tiết
   */
  async function handleDetail(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteOne({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDelete({ ids: selectedRowKeys.value }, () => {
      selectedRowKeys.value = [];
      reload();
    });
  }

  /**
   * Callback thành công
   */
  function handleSuccess() {
    reload();
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
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa không',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
