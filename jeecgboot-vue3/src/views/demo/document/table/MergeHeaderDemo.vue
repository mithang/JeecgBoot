<template>
  <div class="p-4">
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicColumn, BasicTable, useTable } from '/@/components/Table';

  import { demoListApi } from '/@/api/demo/table';
  //Tính toán tiêu đề hợp nhất

  export default defineComponent({
    components: { BasicTable },
    setup() {
      const [registerTable] = useTable({
        title: 'Ví dụ tiêu đề bảng nhóm',
        api: demoListApi,
        columns: getMergeHeaderColumns(),
        bordered: true,
        useSearchForm: false,
      });

      function getMergeHeaderColumns(): BasicColumn[] {
        return [
          {
            title: 'ID',
            dataIndex: 'id',
            width: 300,
          },
          {
            title: 'Họ tên',
            dataIndex: 'name',
            width: 300,
          },
          {
            title: 'Địa chỉ',
            width: 120,
            children: [
              {
                title: 'Địa chỉ',
                dataIndex: 'address',
                key: 'address',
                width: 200,
              },
              {
                title: 'Mã',
                dataIndex: 'no',
                key: 'no',
              },
            ],
          },
          {
            title: 'Thời gian bắt đầu',
            dataIndex: 'beginTime',
            width: 200,
          },
          {
            title: 'Thời gian kết thúc',
            dataIndex: 'endTime',
            width: 200,
          },
        ];
      }
      return {
        registerTable,
      };
    },
  });
</script>
