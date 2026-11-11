<template>
  <div class="p-4">
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  //Định nghĩa cột bảng
  const columns: BasicColumn[] = [
    {
      title: 'Họ tên',
      dataIndex: 'name',
      key: 'name',
      resizable: true,
    },
    {
      title: 'Tuổi',
      dataIndex: 'age',
      key: 'age',
    },
    {
      title: 'Địa chỉ thường trú',
      dataIndex: 'address',
      key: 'address',
    },
  ];
  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'Bảng có thể chọn',
      dataSource: [
        {
          id: '1',
          name: 'Hồ Ca',
          age: 32,
          address: 'Số 1 đường Lâm Thúy, quận Triều Dương',
        },
        {
          id: '2',
          name: 'Lưu Thi Thi',
          age: 32,
          address: 'Số 1 đường Bạch Sa, quận Xương Bình',
        },
      ],
      columns: columns,
      rowSelection: { type: 'checkbox' }, //Mặc định là checkbox chọn nhiều, có thể đặt thành radio chọn một
      useSearchForm: false,
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
    console.log(selectedRows.value);
    console.log(selectedRowKeys.value);
  }
</script>

<style scoped></style>
