<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { demoListApi } from '/@/api/demo/table';
  import { BasicColumn } from '/@/components/Table/src/types/table';
  /** 
    Ưu điểm so với trước đây:
    1, khi kéo tiêu đề cột Table, hàng tổng không bị lệch vị trí;
    2, hàng tổng cộng được render bằng phương thức TableSummary;
    3, hỗ trợ slot tùy chỉnh hiển thị dòng tổng;
    4, cột thêm hàm render tùy chỉnh customSummaryRender
    5, có thể giải quyết vấn đề khi cột được lọc trong trường hợp api không được cấu hình thì giá trị thống kê của hàng tổng cộng không được cập nhật
  */
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
            _index: 'Tổng cộng',
            age: totalAge,
            score: totalScore,
          },
        ];
      }
      const getBasicColumns = () :BasicColumn[] => {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            fixed: 'left',
            width: 200,
            resizable: true,
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
            resizable: true,
            customSummaryRender: ({ text }) => {
              // return <span style="color: red;">{text}</span>;
              return h('span', { style: { color: 'red' } }, [text]);
            },
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
        title: 'Ví dụ về thanh tổng kết gốc (tổng cuối bảng)',
        api: demoListApi,
        rowSelection: { type: 'checkbox' },
        columns: getBasicColumns(),
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
