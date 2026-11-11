<template>
  <div class="p-4">
    <BasicTable @register="registerTable" @edit-end="handleEditEnd" @edit-cancel="handleEditCancel" :beforeEditSubmit="beforeEditSubmit" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, BasicColumn } from '/@/components/Table';
  import { optionsListApi } from '/@/api/demo/select';

  import { demoListApi } from '/@/api/demo/table';
  import { treeOptionsListApi } from '/@/api/demo/tree';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'Hộp nhập',
      dataIndex: 'name',
      edit: true,
      editComponentProps: {
        prefix: '$',
      },
      width: 200,
    },
    {
      title: 'Trạng thái nhập mặc định',
      dataIndex: 'name7',
      edit: true,
      editable: true,
      width: 200,
    },
    {
      title: 'Kiểm chứng ô nhập',
      dataIndex: 'name1',
      edit: true,
      // Kiểm chứng bắt buộc mặc định
      editRule: true,
      width: 200,
    },
    {
      title: 'Kiểm chứng hàm của ô nhập',
      dataIndex: 'name2',
      edit: true,
      editRule: async (text) => {
        if (text === '2') {
          return 'Không thể nhập giá trị này';
        }
        return '';
      },
      width: 200,
    },
    {
      title: 'Ô nhập số',
      dataIndex: 'id',
      edit: true,
      editRule: true,
      editComponent: 'InputNumber',
      width: 200,
    },
    {
      title: 'Dropdown',
      dataIndex: 'name3',
      edit: true,
      editComponent: 'Select',
      editComponentProps: {
        options: [
          {
            label: 'Option1',
            value: '1',
          },
          {
            label: 'Option2',
            value: '2',
          },
        ],
      },
      width: 200,
    },
    {
      title: 'Dropdown từ xa',
      dataIndex: 'name4',
      edit: true,
      editComponent: 'ApiSelect',
      editComponentProps: {
        api: optionsListApi,
        resultField: 'list',
        labelField: 'name',
        valueField: 'id',
      },
      width: 200,
    },
    {
      title: 'Cây dropdown từ xa',
      dataIndex: 'name71',
      edit: true,
      editComponent: 'ApiTreeSelect',
      editRule: false,
      editComponentProps: {
        api: treeOptionsListApi,
        resultField: 'list',
      },
      width: 200,
    },
    {
      title: 'Chọn ngày',
      dataIndex: 'date',
      edit: true,
      editComponent: 'DatePicker',
      editComponentProps: {
        valueFormat: 'YYYY-MM-DD',
        format: 'YYYY-MM-DD',
      },
      width: 200,
    },
    {
      title: 'Chọn thời gian',
      dataIndex: 'time',
      edit: true,
      editComponent: 'TimePicker',
      editComponentProps: {
        valueFormat: 'HH:mm',
        format: 'HH:mm',
      },
      width: 200,
    },
    {
      title: 'Hộp kiểm',
      dataIndex: 'name5',
      edit: true,
      editComponent: 'Checkbox',
      editValueMap: (value) => {
        return value ? 'Có' : 'Không';
      },
      width: 200,
    },
    {
      title: 'Công tắc',
      dataIndex: 'name6',
      edit: true,
      editComponent: 'Switch',
      editValueMap: (value) => {
        return value ? 'Bật' : 'Tắt';
      },
      width: 200,
    },
  ];
  export default defineComponent({
    components: { BasicTable },
    setup() {
      // Tham số, phương thức chung của trang danh sách
      const { tableContext } = useListPage({
        designScope: 'basic-table-demo',
        tableProps: {
          title: 'Ví dụ ô có thể chỉnh sửa',
          api: demoListApi,
          columns: columns,
          showIndexColumn: false,
          bordered: true,
          showActionColumn: false,
          useSearchForm: false,
        },
      });
      //Đăng ký dữ liệu table
      const [registerTable] = tableContext;
      const { createMessage } = useMessage();

      function handleEditEnd({ record, index, key, value }: Recordable) {
        console.log(record, index, key, value);
        return false;
      }

      // Mô phỏng lưu dữ liệu chỉ định
      function feakSave({ value, key, id }) {
        createMessage.loading({
          content: `Đang mô phỏng lưu${key}`,
          key: '_save_fake_data',
          duration: 0,
        });
        return new Promise((resolve) => {
          setTimeout(() => {
            if (value === '') {
              createMessage.error({
                content: 'Lưu thất bại: Không được để trống',
                key: '_save_fake_data',
                duration: 2,
              });
              resolve(false);
            } else {
              createMessage.success({
                content: `Đã lưu ${key} của bản ghi ${id}`,
                key: '_save_fake_data',
                duration: 2,
              });
              resolve(true);
            }
          }, 2000);
        });
      }

      async function beforeEditSubmit({ record, index, key, value }) {
        console.log('Dữ liệu ô đang chuẩn bị gửi', { record, index, key, value });
        return await feakSave({ id: record.id, key, value });
      }

      function handleEditCancel() {
        console.log('cancel');
      }

      return {
        registerTable,
        handleEditEnd,
        handleEditCancel,
        beforeEditSubmit,
      };
    },
  });
</script>
