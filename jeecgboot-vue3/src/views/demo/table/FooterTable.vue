<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { BasicColumn } from '/@/components/Table/src/types/table';
  import { demoListApi } from '/@/api/demo/table';

  export default defineComponent({
    components: { BasicTable },
    setup() {
      function handleSummary(tableData: Recordable[]) {
        const totalAge = tableData.reduce((prev, next) => {
          prev += next.age;
          return prev;
        }, 0);
        const totalScore = tableData.reduce((prev, next) => {
          prev += next.score;
          return prev;
        }, 0);
        return [
          {
            _row: 'Tổng cộng',
            _index: 'Giá trị trung bình',
            age: Math.round(totalAge / tableData.length),
            score: Math.round(totalScore / tableData.length),
          },
          {
            _row: 'Tổng cộng',
            _index: 'Giá trị trung bình',
            age: totalAge,
            score: totalScore,
          },
        ];
      }
      const getBasicColumns = (): BasicColumn[] => {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            fixed: 'left',
            width: 200,
          },
          {
            title: 'Họ tên',
            dataIndex: 'name',
            width: 150,
            filters: [
              { text: 'Male', value: 'male' },
              { text: 'Female', value: 'female' },
            ],
          },
          {
            title: 'Tuổi',
            dataIndex: 'age',
            width: 100,
          },
          {
            title: 'Điểm số',
            dataIndex: 'score',
            width: 100,
            resizable: true,
          },
          {
            title: 'Địa chỉ',
            dataIndex: 'address',
            width: 300,
          },
          {
            title: 'Mã',
            dataIndex: 'no',
            width: 150,
            sorter: true,
            defaultHidden: true,
          },
          {
            title: 'Thời gian bắt đầu',
            width: 150,
            sorter: true,
            dataIndex: 'beginTime',
          },
          {
            title: 'Thời gian kết thúc',
            width: 150,
            sorter: true,
            dataIndex: 'endTime',
          },
        ];
      };
      const [registerTable] = useTable({
        title: 'Ví dụ tổng cộng hàng cuối bảng',
        api: demoListApi,
        rowSelection: { type: 'checkbox' },
        columns: getBasicColumns(),
        // showSummary: trueĐang dùng cách tính tổng hàng chân bảng tùy chỉnh, nếu không đặt hoặc đặt là false thì dùngantd
        showSummary: true,
        summaryFunc: handleSummary,
        scroll: { x: 1000 },
        canResize: false,
      });

      return {
        registerTable,
      };
    },
  });
</script>
