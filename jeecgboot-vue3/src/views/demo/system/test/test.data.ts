import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tên',
    dataIndex: 'testName',
    width: 200,
  },
  {
    title: 'Giá trị',
    dataIndex: 'testValue',
    width: 180,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 180,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'testName',
    label: 'Tên',
    component: 'Input',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'testName',
    label: 'Tên',
    required: true,
    component: 'Input',
  },
  {
    field: 'testValue',
    label: 'Giá trị',
    required: true,
    component: 'Input',
  },

  {
    label: ' ',
    field: 'menu',
    slot: 'menu',
    component: 'Input',
  },
];
