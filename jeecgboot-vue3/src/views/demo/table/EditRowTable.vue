<template>
  <div class="p-4">
    <BasicTable @register="registerTable" @edit-change="onEditChange">
      <template #action="{ record, column }">
        <TableAction :actions="createActions(record, column)" />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, useTable, TableAction, BasicColumn, ActionItem, EditRecordRow } from '/@/components/Table';
  import { optionsListApi } from '/@/api/demo/select';

  import { demoListApi } from '/@/api/demo/table';
  import { treeOptionsListApi } from '/@/api/demo/tree';
  import { cloneDeep } from 'lodash-es';
  import { useMessage } from '/@/hooks/web/useMessage';

  const columns: BasicColumn[] = [
    {
      title: 'Hộp nhập',
      dataIndex: 'name',
      editRow: true,
      editComponentProps: {
        prefix: '$',
      },
      width: 150,
    },
    {
      title: 'Trạng thái nhập mặc định',
      dataIndex: 'name7',
      editRow: true,
      width: 150,
    },
    {
      title: 'Kiểm chứng ô nhập',
      dataIndex: 'name1',
      editRow: true,
      align: 'left',
      // Kiểm chứng bắt buộc mặc định
      editRule: true,
      width: 150,
    },
    {
      title: 'Kiểm chứng hàm của ô nhập',
      dataIndex: 'name2',
      editRow: true,
      align: 'right',
      editRule: async (text) => {
        if (text === '2') {
          return 'Không thể nhập giá trị này';
        }
        return '';
      },
    },
    {
      title: 'Ô nhập số',
      dataIndex: 'id',
      editRow: true,
      editRule: true,
      editComponent: 'InputNumber',
      width: 150,
    },
    {
      title: 'Dropdown',
      dataIndex: 'name3',
      editRow: true,
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
          {
            label: 'Option3',
            value: '3',
          },
        ],
      },
      width: 200,
    },
    {
      title: 'Dropdown từ xa',
      dataIndex: 'name4',
      editRow: true,
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
      dataIndex: 'name8',
      editRow: true,
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
      editRow: true,
      editComponent: 'DatePicker',
      editComponentProps: {
        valueFormat: 'YYYY-MM-DD',
        format: 'YYYY-MM-DD',
      },
      width: 150,
    },
    {
      title: 'Chọn thời gian',
      dataIndex: 'time',
      editRow: true,
      editComponent: 'TimePicker',
      editComponentProps: {
        valueFormat: 'HH:mm',
        format: 'HH:mm',
      },
      width: 100,
    },
    {
      title: 'Hộp kiểm',
      dataIndex: 'name5',
      editRow: true,

      editComponent: 'Checkbox',
      editValueMap: (value) => {
        return value ? 'Có' : 'Không';
      },
      width: 100,
    },
    {
      title: 'Công tắc',
      dataIndex: 'name6',
      editRow: true,
      editComponent: 'Switch',
      editValueMap: (value) => {
        return value ? 'Bật' : 'Tắt';
      },
      width: 100,
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction },
    setup() {
      const { createMessage: msg } = useMessage();
      const currentEditKeyRef = ref('');
      const [registerTable] = useTable({
        title: 'Ví dụ hàng có thể chỉnh sửa',
        titleHelpMessage: ['Sửa trong ví dụ này[Ô nhập số]cột này, cùng dòng[Dropdown từ xa]Dữ liệu chỉnh sửa hiện tại của cột cũng sẽ thay đổi đồng bộ'],
        api: demoListApi,
        columns: columns,
        showIndexColumn: false,
        showTableSetting: true,
        tableSetting: { fullScreen: true },
        actionColumn: {
          width: 160,
          title: 'Action',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });

      function handleEdit(record: EditRecordRow) {
        currentEditKeyRef.value = record.key;
        record.onEdit?.(true);
      }

      function handleCancel(record: EditRecordRow) {
        currentEditKeyRef.value = '';
        record.onEdit?.(false, false);
      }

      async function handleSave(record: EditRecordRow) {
        // Kiểm chứng
        msg.loading({ content: 'Đang lưu......', duration: 0, key: 'saving' });
        const valid = await record.onValid?.();
        if (valid) {
          try {
            const data = cloneDeep(record.editValueRefs);
            console.log(data);
            //TODO Tại đây gửi dữ liệu lên máy chủ để lưu
            // ...
            // Sau khi lưu thì gửi trạng thái chỉnh sửa
            const pass = await record.onEdit?.(false, true);
            if (pass) {
              currentEditKeyRef.value = '';
            }
            msg.success({ content: 'Dữ liệu đã lưu', key: 'saving' });
          } catch (error) {
            msg.error({ content: 'Lưu thất bại', key: 'saving' });
          }
        } else {
          msg.error({ content: 'Vui lòng điền dữ liệu chính xác', key: 'saving' });
        }
      }

      function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
        if (!record.editable) {
          return [
            {
              label: 'Chỉnh sửa',
              disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.key : false,
              onClick: handleEdit.bind(null, record),
            },
          ];
        }
        return [
          {
            label: 'Lưu',
            onClick: handleSave.bind(null, record, column),
          },
          {
            label: 'Hủy',
            popConfirm: {
              title: 'Có hủy chỉnh sửa không',
              confirm: handleCancel.bind(null, record, column),
            },
          },
        ];
      }

      function onEditChange({ column, value, record }) {
        // Ví dụ này
        if (column.dataIndex === 'id') {
          record.editValueRefs.name4.value = `${value}`;
        }
        console.log(column, value, record);
      }

      return {
        registerTable,
        handleEdit,
        createActions,
        onEditChange,
      };
    },
  });
</script>
