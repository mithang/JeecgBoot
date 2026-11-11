import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'ID định tuyến',
    dataIndex: 'routerId',
    width: 200,
    align: 'left',
  },
  {
    title: 'Tên định tuyến',
    dataIndex: 'name',
    width: 200,
  },
  {
    title: 'URI định tuyến',
    dataIndex: 'uri',
    width: 200,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    slots: { customRender: 'status' },
    width: 150,
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'ID định tuyến',
    component: 'Input',
    required: true,
  },
  {
    field: 'name',
    label: 'Tên định tuyến',
    component: 'InputNumber',
    required: true,
  },
  {
    field: 'uri',
    label: 'URI định tuyến',
    component: 'Input',
  },
  {
    field: 'predicates',
    label: 'Điều kiện định tuyến',
    slot: 'predicates',
    component: 'Input',
  },
];
