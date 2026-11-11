<template>
  <div>
    <a-alert
      message="Kiểm tra nhiều instance bảng (Issue #8792 Sửa)"
      description="Ví dụ này minh họa component cha và con đồng thời dùng BasicTable, các chức năng như cấu hình cột, điều chỉnh độ rộng cột đều hoạt động bình thường, hai instance bảng không ảnh hưởng lẫn nhau"
      type="info"
      show-icon
      class="mb-4"
    />

    <a-card title="Bảng của component cha" class="mb-4">
      <BasicTable
        @register="registerParentTable"
        :columns="parentColumns"
        :dataSource="parentData"
        :pagination="false"
        showTableSetting
        :canResize="false"
      >
        <template #toolbar>
          <a-button type="primary" @click="testParentTable">Kiểm tra bảng cha</a-button>
        </template>
      </BasicTable>
    </a-card>

    <a-card title="Bảng của component con">
      <BasicTable
        @register="registerChildTable"
        :columns="childColumns"
        :dataSource="childData"
        :pagination="false"
        showTableSetting
        :canResize="false"
      >
        <template #toolbar>
          <a-button type="primary" @click="testChildTable">Kiểm tra bảng con</a-button>
        </template>
      </BasicTable>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, BasicColumn } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();

  // Cấu hình bảng cha
  const parentColumns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: 'Bảng cha-Họ tên',
      dataIndex: 'name',
      width: 150,
    },
    {
      title: 'Bảng cha-Tuổi',
      dataIndex: 'age',
      width: 100,
    },
    {
      title: 'Bảng cha-Địa chỉ',
      dataIndex: 'address',
      width: 200,
    },
  ];

  const parentData = ref([
    { id: 1, name: 'Bảng cha-Trương Tam', age: 25, address: 'Quận Triều Dương, Bắc Kinh' },
    { id: 2, name: 'Bảng cha-Lý Tứ', age: 30, address: 'Quận Phố Đông, thành phố Thượng Hải' },
    { id: 3, name: 'Bảng cha-Vương Ngũ', age: 28, address: 'Quận Thiên Hà, Quảng Châu' },
  ]);

  const [registerParentTable, { getColumns: getParentColumns }] = useTable({
    columns: parentColumns,
    dataSource: parentData.value,
  });

  // Cấu hình bảng con
  const childColumns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: 'Bảng con-Tên sản phẩm',
      dataIndex: 'product',
      width: 150,
    },
    {
      title: 'Bảng con-Giá',
      dataIndex: 'price',
      width: 100,
    },
    {
      title: 'Bảng con-Tồn kho',
      dataIndex: 'stock',
      width: 100,
    },
    {
      title: 'Bảng con-Phân loại',
      dataIndex: 'category',
      width: 150,
    },
  ];

  const childData = ref([
    { id: 1, product: 'Bảng con-Sản phẩmA', price: 99, stock: 100, category: 'Sản phẩm điện tử' },
    { id: 2, product: 'Bảng con-Sản phẩmB', price: 199, stock: 50, category: 'Đồ dùng gia đình' },
    { id: 3, product: 'Bảng con-Sản phẩmC', price: 299, stock: 30, category: 'Quần áo, giày dép và túi xách' },
  ]);

  const [registerChildTable, { getColumns: getChildColumns }] = useTable({
    columns: childColumns,
    dataSource: childData.value,
  });

  function testParentTable() {
    const cols = getParentColumns();
    createMessage.success(`Số cột bảng cha: ${cols.length}, vui lòng xem console`);
    console.log('Cấu hình cột của bảng cha:', cols);
  }

  function testChildTable() {
    const cols = getChildColumns();
    createMessage.success(`Số cột bảng con: ${cols.length}, vui lòng xem console`);
    console.log('Cấu hình cột bảng con:', cols);
  }
</script>

<style scoped>
</style>
