<template>
  <div class="p-4">
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable">
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
  //Định nghĩa cột bảng
  const columns: BasicColumn[] = [
    {
      title: 'Họ tên',
      dataIndex: 'name',
      key: 'name',
      resizable: false,
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
      title: 'Danh sách người dùng',
      dataSource: [
        {
          key: '1',
          name: 'Hồ Ca',
          age: 32,
          address: 'Số 1 đường Lâm Thúy, quận Triều Dương',
        },
        {
          key: '2',
          name: 'Lưu Thi Thi',
          age: 32,
          address: 'Số 1 đường Bạch Sa, quận Xương Bình',
        },
      ],
      columns: columns,
      size: 'large', //Bảng gọn large
      striped: false, //Cài đặt vân ngựa vằn false
      showActionColumn: true,
      useSearchForm: false,
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable, methods] = tableContext;
  console.log('methods', methods);
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
