<template>
  <a-card :bordered="false">
    <BasicTable @register="registerTable" :expandedRowKeys="expandedRowKeys" :rowSelection="rowSelection" @expand="handleExpand">
      <template #tableTitle>
        <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined"> Thêm mới</a-button>
      </template>
      <template #expandedRowRender>
        <BasicTable bordered size="middle" rowKey="id" :canResize="false" :columns="innerColumns" :dataSource="innerData" :pagination="false">
        </BasicTable>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <JVxeTableModal @register="registerModal" @success="reload()"></JVxeTableModal>
  </a-card>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import JVxeTableModal from '/@/views/demo/vextable/jvxetable/JVxeTableModal.vue';
  //APIurl
  const url = {
    list: '/test/order/orderList',
    delete: '/test/order/delete',
    deleteBatch: '/test/order/deleteBatch',
    customerListByMainId: '/test/order/listOrderCustomerByMainId',
  };
  // Mở rộngkey
  const expandedRowKeys = ref<any[]>([]);
  // Chọnkey
  const checkedKeys = ref<any[]>([]);
  // Dữ liệu bảng con
  const innerData = ref<any[]>([]);
  // Tiêu đề bảng chính
  const columns = [
    {
      title: 'Số đơn hàng',
      align: 'center',
      dataIndex: 'orderCode',
      width: 100,
    },
    {
      title: 'Loại đơn hàng',
      align: 'center',
      dataIndex: 'ctype',
      width: 100,
      customRender: ({ text }) => {
        let re = '';
        if (text === '1') {
          re = 'Đơn hàng nội địa';
        } else if (text === '2') {
          re = 'Đơn hàng quốc tế';
        }
        return re;
      },
    },
    {
      title: 'Ngày đặt hàng',
      align: 'center',
      width: 100,
      dataIndex: 'orderDate',
    },
    {
      title: 'Số tiền đơn hàng',
      align: 'center',
      dataIndex: 'orderMoney',
      width: 100,
    },
    {
      title: 'Ghi chú đơn hàng',
      align: 'center',
      dataIndex: 'content',
      width: 100,
    },
  ];
  // Tiêu đề bảng con
  const innerColumns = [
    {
      title: 'Tên khách hàng',
      align: 'center',
      width: 100,
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Giới tính',
      align: 'center',
      dataIndex: 'sex',
      customRender: function (text) {
        //console.log(typeof  text )
        //console.log(text)
        if (text.value == '1') {
          return 'Nam';
        } else if (text.value == '2') {
          return 'Nữ';
        } else {
          return text;
        }
      },
    },
    {
      title: 'Số chứng minh nhân dân',
      align: 'center',
      dataIndex: 'idcard',
    },
    {
      title: 'Điện thoại',
      dataIndex: 'telphone',
      align: 'center',
    },
  ];
  const list = (params) => defHttp.get({ url: url.list, params });
  const [registerModal, { openModal }] = useModal();
  const [registerTable, { reload }] = useTable({
    columns,
    api: list,
    rowKey: 'id',
    striped: true,
    useSearchForm: false,
    showTableSetting: true,
    clickToRowSelect: false,
    bordered: true,
    actionColumn: {
      width: 110,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * Cấu hình cột lựa chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 30,
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
   * Sự kiện mở rộng
   * */
  function handleExpand(expanded, record) {
    expandedRowKeys.value = [];
    innerData.value = [];
    if (expanded === true) {
      expandedRowKeys.value.push(record.id);
      defHttp.get({ url: url.customerListByMainId, params: { orderId: record.id } }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
          innerData.value = res.result.records;
        }
      });
    }
  }
  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Sự kiện xóa
   */
  function handleDelete(record) {
    defHttp.delete({ url: url.delete, data: { id: record.id } }, { joinParamsToUrl: true }).then(() => {
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
        label: 'Xóa',
        popConfirm: {
          title: 'Có xác nhận xóa không',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>
<style scoped>
  .ant-card-body .table-operator {
    margin-bottom: 18px;
  }

  .ant-table-tbody .ant-table-row td {
    padding-top: 15px;
    padding-bottom: 15px;
  }

  .anty-row-operator button {
    margin: 0 5px;
  }

  .ant-btn-danger {
    background-color: #ffffff;
  }

  .ant-modal-cust-warp {
    height: 100%;
  }

  .ant-modal-cust-warp .ant-modal-body {
    height: calc(100% - 110px) !important;
    overflow-y: auto;
  }

  .ant-modal-cust-warp .ant-modal-content {
    height: 90% !important;
    overflow-y: hidden;
  }
</style>
