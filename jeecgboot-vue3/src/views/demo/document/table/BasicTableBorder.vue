<template>
  <!--Bảng tham chiếu-->
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'name'">
          <a>{{ text }}</a>
        </template>
      </template>
      <template #footer>Chân trang</template>
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
      width: 300,
    },
    {
      title: 'Tuổi',
      dataIndex: 'age',
      key: 'age',
      width: 300,
    },
    {
      title: 'Địa chỉ thường trú',
      dataIndex: 'address',
      key: 'address',
      ellipsis: true,
    },
    {
      title: 'Cột nội dung dài',
      dataIndex: 'address',
      key: 'address 2',
      ellipsis: true,
    },
    {
      title: 'Cột nội dung dài',
      dataIndex: 'address',
      key: 'address 3',
      ellipsis: true,
    },
  ];
  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'Bảng có viền',
      dataSource: [
        {
          key: '1',
          name: 'Trương Tam',
          age: 32,
          address: 'Trung Quốc, Bắc Kinh, quận Triều Dương, đường Đại Đồn, Khoa Học Viện Nam Lý, tòa 1, đơn nguyên 3401',
        },
        {
          key: '2',
          name: 'Lưu Tư',
          age: 32,
          address: 'Trung Quốc, Bắc Kinh, quận Xương Bình, đường Thuận Sa, Thượng Hồ Thế Gia, tòa 2, đơn nguyên 7503',
        },
      ],
      columns: columns,
      showActionColumn: false,
      useSearchForm: false,
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable] = tableContext;
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
