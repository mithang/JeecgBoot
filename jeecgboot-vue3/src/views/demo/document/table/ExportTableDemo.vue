<template>
  <div class="p-4">
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Import</j-upload-button>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { ActionItem, BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { defHttp } from '/@/utils/http/axios';
  //Định nghĩa cột bảng
  const columns: BasicColumn[] = [
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 170,
      align: 'left',
      resizable: true,
      sorter: {
        multiple: 1,
      },
    },
    {
      title: 'Từ khóa',
      dataIndex: 'keyWord',
      width: 130,
      resizable: true,
    },
    {
      title: 'Thời gian chấm công',
      dataIndex: 'punchTime',
      width: 140,
      resizable: true,
    },
    {
      title: 'Lương',
      dataIndex: 'salaryMoney',
      width: 140,
      resizable: true,
      sorter: {
        multiple: 2,
      },
    },
    {
      title: 'Tiền thưởng',
      dataIndex: 'bonusMoney',
      width: 140,
      resizable: true,
    },
    {
      title: 'Giới tính',
      dataIndex: 'sex',
      sorter: {
        multiple: 3,
      },
      filters: [
        { text: 'Nam', value: '1' },
        { text: 'Nữ', value: '2' },
      ],
      customRender: ({ record }) => {
        return record.sex ? (record.sex == '1' ? 'Nam' : 'Nữ') : '';
      },
      width: 120,
      resizable: true,
    },
    {
      title: 'Ngày sinh',
      dataIndex: 'birthday',
      width: 120,
      resizable: true,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      width: 120,
      resizable: true,
    },
  ];

  //ajaxYêu cầu API
  const demoListApi = (params) => {
    return defHttp.get({ url: '/test/jeecgDemo/list', params });
  };
  // Tham số, phương thức chung của trang danh sách
  const { tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'basic-table-demo-filter',
    tableProps: {
      title: 'Tìm kiếm biểu mẫu',
      api: demoListApi,
      columns: columns,
      showActionColumn: false,
      useSearchForm: false,
    },
    exportConfig: {
      name: 'Danh sách ví dụ',
      url: '/test/jeecgDemo/exportXls',
    },
    importConfig: {
      url: '/test/jeecgDemo/importExcel',
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, { reload }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;
  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  function handleEdit(record) {
    console.log(record);
  }
</script>

<style scoped></style>
