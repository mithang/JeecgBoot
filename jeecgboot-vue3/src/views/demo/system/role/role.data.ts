import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Switch } from 'ant-design-vue';
import { setRoleStatus } from '/@/api/demo/system';
import { useMessage } from '/@/hooks/web/useMessage';

export const columns: BasicColumn[] = [
  {
    title: 'Tên vai trò',
    dataIndex: 'roleName',
    width: 200,
  },
  {
    title: 'Giá trị vai trò',
    dataIndex: 'roleValue',
    width: 180,
  },
  {
    title: 'Sắp xếp',
    dataIndex: 'orderNo',
    width: 50,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 120,
    customRender: ({ record }) => {
      if (!Reflect.has(record, 'pendingStatus')) {
        record.pendingStatus = false;
      }
      return h(Switch, {
        checked: record.status === '1',
        checkedChildren: 'Đã kích hoạt',
        unCheckedChildren: 'Đã vô hiệu hóa',
        loading: record.pendingStatus,
        onChange(checked: boolean) {
          record.pendingStatus = true;
          const newStatus = checked ? '1' : '0';
          const { createMessage } = useMessage();
          setRoleStatus(record.id, newStatus)
            .then(() => {
              record.status = newStatus;
              createMessage.success(`Đã sửa trạng thái vai trò thành công`);
            })
            .catch(() => {
              createMessage.error('Sửa trạng thái vai trò thất bại');
            })
            .finally(() => {
              record.pendingStatus = false;
            });
        },
      });
    },
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 180,
  },
  {
    title: 'Ghi chú',
    dataIndex: 'remark',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'roleNme',
    label: 'Tên vai trò',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: {
      options: [
        { label: 'Kích hoạt', value: '0' },
        { label: 'Vô hiệu hóa', value: '1' },
      ],
    },
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'roleName',
    label: 'Tên vai trò',
    required: true,
    component: 'Input',
  },
  {
    field: 'roleValue',
    label: 'Giá trị vai trò',
    required: true,
    component: 'Input',
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'RadioButtonGroup',
    defaultValue: '0',
    componentProps: {
      options: [
        { label: 'Kích hoạt', value: '0' },
        { label: 'Vô hiệu hóa', value: '1' },
      ],
    },
  },
  {
    label: 'Ghi chú',
    field: 'remark',
    component: 'InputTextArea',
  },
  {
    label: ' ',
    field: 'menu',
    slot: 'menu',
    component: 'Input',
  },
];
