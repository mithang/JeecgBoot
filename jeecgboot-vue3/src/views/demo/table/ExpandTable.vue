<template>
  <PageWrapper
    title="Bảng có thể mở rộng"
    content="Không được dùng chung với scroll. Component TableAction có thể cấu hình stopButtonPropagation để ngăn sự kiện click của nút thao tác nổi bọt, nhằm phối hợp với component Table củaexpandRowByClick"
  >
    <BasicTable @register="registerTable">
      <template #expandedRowRender="{ record }">
        <span>No: {{ record.no }} </span>
      </template>
      <template #action="{ record }">
        <TableAction
          stopButtonPropagation
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
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { getBasicColumns } from './tableData';

  import { demoListApi } from '/@/api/demo/table';

  export default defineComponent({
    components: { BasicTable, TableAction, PageWrapper },
    setup() {
      const [registerTable] = useTable({
        api: demoListApi,
        title: 'Demo bảng có thể mở rộng',
        titleHelpMessage: ['Đã kích hoạtexpandRowByClick', 'Đã kích hoạtstopButtonPropagation'],
        columns: getBasicColumns(),
        rowKey: 'id',
        canResize: false,
        expandRowByClick: true,
        actionColumn: {
          width: 160,
          title: 'Action',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });
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
