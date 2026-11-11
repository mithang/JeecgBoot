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
  import { BasicTable, useTable, TableAction, BasicColumn } from '/@/components/Table';
  import { PageWrapper } from '/@/components/Page';
  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
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
  export default defineComponent({
    components: { BasicTable, TableAction, PageWrapper },
    setup() {
      const { tableContext } = useListPage({
        designScope: 'basic-table-demo',
        tableProps: {
          api: demoListApi,
          title: 'Demo bảng có thể mở rộng',
          titleHelpMessage: ['Đã kích hoạtexpandRowByClick', 'Đã kích hoạtstopButtonPropagation'],
          columns: columns,
          rowKey: 'id',
          canResize: false,
          expandRowByClick: true,
          actionColumn: {
            width: 160,
            title: 'Action',
            dataIndex: 'action',
          },
          useSearchForm: false,
        },
      });
      //Đăng ký dữ liệu table
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
