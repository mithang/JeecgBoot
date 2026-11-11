<template>
  <PageWrapper>
    <BasicTable @register="registerTable" :striped="true" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { BasicTable, useTable } from '/@/components/Table';
  import { mapTableTotalSummary } from '/@/utils/common/compUtils';
  const dataSource = ref<any>([]);
  setTimeout(() => {
    dataSource.value = [
      { id: 0, name: 'Trương Tam', point: 23, level: 3, updateTime: '2019-8-14' },
      { id: 1, name: 'Tiểu Lộc', point: 33, level: 9, updateTime: '2019-8-10' },
      { id: 2, name: 'Tiểu Vương', point: 6, level: 1, updateTime: '2019-8-13' },
      { id: 3, name: 'Lý Tứ', point: 53, level: 8, updateTime: '2019-8-12' },
      { id: 4, name: 'Tiểu Hồng', point: 44, level: 5, updateTime: '2019-8-11' },
      { id: 5, name: 'Vương Ngũ', point: 97, level: 10, updateTime: '2019-8-10' },
      { id: 6, name: 'Tiểu Minh', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 7, name: 'Tiểu Trương', point: 33, level: 4, updateTime: '2019-8-10' },
      { id: 8, name: 'Tiểu Lục', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 9, name: 'Tiểu Ngũ', point: 33, level: 7, updateTime: '2019-8-10' },
      { id: 10, name: 'Tiểu Triệu', point: 33, level: 2, updateTime: '2019-8-10' },
      { id: 11, name: 'Lý Hoa', point: 33, level: 8, updateTime: '2019-8-10' },
      { id: 12, name: 'Tiểu Khang', point: 33, level: 5, updateTime: '2019-8-10' },
    ];
  }, 1e3);
  const [registerTable] = useTable({
    rowKey: 'id',
    bordered: true,
    canResize: true,
    columns: [
      { title: 'Họ tên', width: 500, dataIndex: 'name' },
      { title: 'Điểm đóng góp', width: 500, dataIndex: 'point' },
      { title: 'Cấp độ', width: 500, dataIndex: 'level' },
      { title: 'Thời gian cập nhật', width: 500, dataIndex: 'updateTime' },
    ],
    dataSource: dataSource,
    // Hiển thị tổng cộng ở cuối
    showSummary: true,
    striped: true,
    // Phương thức tính tổng cộng ở cuối
    summaryFunc: onSummary,
  });

  function onSummary(tableData: Recordable[]) {
    // Có thể dùng phương thức công cụ để tự động tính tổng
    const totals = mapTableTotalSummary(tableData, ['point', 'level']);
    return [
      totals,
      {
        _row: 'Trung bình',
        _index: 'Trung bình',
        // Tính giá trị trung bình
        point: (totals.point / tableData.length).toFixed(2),
        level: (totals.level / tableData.length).toFixed(0),
      },
    ];
  }
</script>
