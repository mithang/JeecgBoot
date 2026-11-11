<template>
  <div>
    <!--Bảng-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :searchInfo="searchInfo">
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
    <!-- Vùng biểu mẫu -->
    <JeecgOrderCustomerModal @register="registerModal" @success="handleSuccess"></JeecgOrderCustomerModal>
  </div>
</template>

<script lang="ts" setup>
  //tsCú pháp
  import type { ComputedRef } from 'vue';
  import { ref, computed, unref, watch, inject } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import JeecgOrderCustomerModal from './components/JeecgOrderCustomerModal.vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { customColumns } from './erplist.data';
  import { customList, deleteCustomer, deleteBatchCustomer } from './erplist.api';
  import { isEmpty } from '/@/utils/is';
  import { useMessage } from '/@/hooks/web/useMessage';
  //Nhận bảng chínhid
  const orderId = inject<ComputedRef<string>>(
    'orderId',
    computed(() => '')
  );
  //Popup nhắc nhở
  const $message = useMessage();
  //Popupmodel
  const [registerModal, { openModal }] = useModal();
  const searchInfo = {};
  // Tham số, phương thức chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    tableProps: {
      api: getCustomList,
      tableSetting:{
        cacheKey:'customer'
      },
      columns: customColumns,
      canResize: false,
      useSearchForm: false,
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
  const [registerTable, { reload, setSelectedRowKeys }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(orderId, () => {
    searchInfo['orderId'] = unref(orderId);
    reload();
    // Khi id bảng chính thay đổi, xóa trạng thái chọn của bảng con
    setSelectedRowKeys([]);
  });

  async function getCustomList(params) {
    let { orderId } = params;
    // Khi Id bảng chính rỗng, không truy vấn dữ liệu bảng con, trả về trực tiếp mảng rỗng
    if (orderId == null || isEmpty(orderId)) {
      return [];
    }
    return await customList(params);
  }

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    console.log('orderId=====》', orderId);
    if (isEmpty(unref(orderId))) {
      $message.createMessage.warning('Vui lòng chọn một thông tin đơn hàng');
      return;
    }
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
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteCustomer({ id: record.id }, reload);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await deleteBatchCustomer({ ids: selectedRowKeys.value }, () => {
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
