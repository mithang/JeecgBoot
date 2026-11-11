<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #action="{ record }">
        <TableAction
          :actions="[
            {
              label: 'Chỉnh sửa',
              onClick: handleEdit.bind(null, record),
              auth: 'other', // Điều khiển việc hiển thị theo quyền hạn: Không có quyền hạn, không hiển thị
            },
            {
              label: 'Xóa',
              icon: 'ic:outline-delete-outline',
              onClick: handleDelete.bind(null, record),
              auth: 'super', // Điều khiển việc hiển thị theo quyền hạn: Có quyền hạn, sẽ hiển thị
            },
          ]"
          :dropDownActions="[
            {
              label: 'Kích hoạt',
              popConfirm: {
                title: 'Có kích hoạt không?',
                confirm: handleOpen.bind(null, record),
              },
              ifShow: (_action) => {
                return record.status !== 'enable'; // Điều khiển việc hiển thị theo nghiệp vụ: Trạng thái không phải enable thì không hiển thị nút kích hoạt
              },
            },
            {
              label: 'Vô hiệu hóa',
              popConfirm: {
                title: 'Có vô hiệu hóa không?',
                confirm: handleOpen.bind(null, record),
              },
              ifShow: () => {
                return record.status === 'enable'; // Điều khiển việc hiển thị theo nghiệp vụ: enableNút vô hiệu hóa hiển thị khi trạng thái là enable
              },
            },
            {
              label: 'Điều khiển đồng thời',
              popConfirm: {
                title: 'Có hiển thị động không?',
                confirm: handleOpen.bind(null, record),
              },
              auth: 'super', // Đồng thời điều khiển hiển thị theo quyền hạn và nghiệp vụ
              ifShow: () => {
                return true;
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
  const columns: BasicColumn[] = [
    {
      title: 'Mã',
      dataIndex: 'no',
      width: 100,
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      auth: 'test', // Điều khiển việc hiển thị theo quyền hạn: Không có quyền hạn, không hiển thị
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      auth: 'super', // Đồng thời điều khiển hiển thị theo quyền hạn và nghiệp vụ
      ifShow: (_column) => {
        return true;
      },
    },
    {
      title: 'Thời gian bắt đầu',
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
      const [registerTable] = useTable({
        title: 'TableActionVí dụ component TableAction và cột cố định',
        api: demoListApi,
        columns: columns,
        bordered: true,
        actionColumn: {
          width: 250,
          title: 'Action',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });
      function handleEdit(record: Recordable) {
        console.log('Đã nhấp Chỉnh sửa', record);
      }
      function handleDelete(record: Recordable) {
        console.log('Đã nhấp Xóa', record);
      }
      function handleOpen(record: Recordable) {
        console.log('Đã nhấp Kích hoạt', record);
      }
      return {
        registerTable,
        handleEdit,
        handleDelete,
        handleOpen,
      };
    },
  });
</script>
