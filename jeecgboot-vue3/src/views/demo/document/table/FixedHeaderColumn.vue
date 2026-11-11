<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              label: 'Xóa',
              icon: 'ic:outline-delete-outline',
              onClick: handleDelete.bind(null, record),
            },
          ]"
          :dropDownActions="[
            {
              label: 'Kích hoạt',
              popConfirm: {
                title: 'Có kích hoạt không?',
                confirm: handleOpen.bind(null, record),
              },
            },
          ]"
        />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn, TableAction } from '/@/components/Table';

  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      fixed: 'left',
      width: 280,
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 260,
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
    },
    {
      title: 'Mã',
      dataIndex: 'no',
      width: 300,
    },
    {
      title: 'Thời gian bắt đầu',
      width: 200,
      dataIndex: 'beginTime',
    },
    {
      title: 'Thời gian kết thúc',
      dataIndex: 'endTime',
      width: 200,
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'Ví dụ tiêu đề và cột cố định',
          api: demoListApi,
          columns: columns,
          canResize: false,
          scroll: { y: 200 },
          actionColumn: {
            width: 160,
            title: 'Action',
            dataIndex: 'action',
          },
          useSearchForm: false,
        },
      });

      const [registerTable] = tableContext;
      function handleDelete(record: Recordable) {
        console.log('Đã nhấp Xóa', record);
      }
      function handleOpen(record: Recordable) {
        console.log('Đã nhấp Kích hoạt', record);
      }
      return {
        registerTable,
        handleDelete,
        handleOpen,
      };
    },
  });
</script>
