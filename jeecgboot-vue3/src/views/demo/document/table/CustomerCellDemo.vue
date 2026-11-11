<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #id="{ record }"> ID: {{ record.id }} </template>
      <template #bodyCell="{ column, record }">
        <Avatar v-if="column.key === 'avatar'" :size="60" :src="record.avatar" />
        <Tag v-if="column.key === 'no'" color="green">
          {{ record.no }}
        </Tag>
      </template>
      <template #img="{ text }">
        <TableImg :size="60" :simpleShow="true" :imgList="text" />
      </template>
      <template #imgs="{ text }"> <TableImg :size="60" :imgList="text" /> </template>

      <template #category="{ record }">
        <Tag color="green">
          {{ record.no }}
        </Tag>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn, TableImg } from '/@/components/Table';
  import { Tag, Avatar } from 'ant-design-vue';
  import { demoListApi } from '/@/api/demo/table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      slots: { customRender: 'id' },
    },
    {
      title: 'Ảnh đại diện',
      dataIndex: 'avatar',
      width: 100,
    },
    {
      title: 'Phân loại',
      dataIndex: 'category',
      width: 80,
      align: 'center',
      defaultHidden: true,
      slots: { customRender: 'category' },
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 120,
    },
    {
      title: 'Danh sách hình ảnh1',
      dataIndex: 'imgArr',
      helpMessage: ['Đây là danh sách hình ảnh ở chế độ đơn giản', 'Chỉ hiển thị một hình trong bảng', 'Nhưng khi nhấn có thể xem trước nhiều hình ảnh'],
      width: 140,
      slots: { customRender: 'img' },
    },
    {
      title: 'Danh sách ảnh2',
      dataIndex: 'imgs',
      width: 160,
      slots: { customRender: 'imgs' },
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
    },
    {
      title: 'Mã',
      dataIndex: 'no',
    },
    {
      title: 'Thời gian bắt đầu',
      dataIndex: 'beginTime',
    },
    {
      title: 'Thời gian kết thúc',
      dataIndex: 'endTime',
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableImg, Tag, Avatar },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'Nội dung cột tùy chỉnh',
          titleHelpMessage: 'Tất cả avatar, hình ảnh trong bảng đều được tạo bằng mock, chỉ dùng để demo placeholder hình ảnh',
          api: demoListApi,
          columns: columns,
          bordered: true,
          showTableSetting: false,
          showActionColumn: false,
          useSearchForm: false,
        },
      });
      //Đăng ký dữ liệu table
      const [registerTable] = tableContext;
      return {
        registerTable,
      };
    },
  });
</script>
