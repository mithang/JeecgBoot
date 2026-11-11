import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tên tệp',
    dataIndex: 'fileName',
    width: 120,
  },
  {
    title: 'Địa chỉ tệp',
    dataIndex: 'url',
    width: 100,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên tệp',
    field: 'fileName',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    label: 'Địa chỉ tệp',
    field: 'url',
    component: 'Input',
    colProps: { span: 6 },
  },
];
