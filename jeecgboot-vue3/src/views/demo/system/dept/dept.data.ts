import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';

export const columns: BasicColumn[] = [
  {
    title: 'Tên phòng ban',
    dataIndex: 'deptName',
    width: 160,
    align: 'left',
  },
  {
    title: 'Sắp xếp',
    dataIndex: 'orderNo',
    width: 50,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 80,
    customRender: ({ record }) => {
      const status = record.status;
      const enable = ~~status === 0;
      const color = enable ? 'green' : 'red';
      const text = enable ? 'Kích hoạt' : 'Vô hiệu hóa';
      return h(Tag, { color: color }, () => text);
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
    field: 'deptName',
    label: 'Tên phòng ban',
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
    field: 'deptName',
    label: 'Tên phòng ban',
    component: 'Input',
    required: true,
  },
  {
    field: 'parentDept',
    label: 'Phòng ban cấp trên',
    component: 'TreeSelect',

    componentProps: {
      replaceFields: {
        title: 'deptName',
        key: 'id',
        value: 'id',
      },
      getPopupContainer: () => document.body,
    },
    required: true,
  },
  {
    field: 'orderNo',
    label: 'Sắp xếp',
    component: 'InputNumber',
    required: true,
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
    required: true,
  },
  {
    label: 'Ghi chú',
    field: 'remark',
    component: 'InputTextArea',
  },
];
