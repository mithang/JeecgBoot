import { FormSchema } from '/@/components/Form';

const basicOptions: LabelValueOptions = [
  {
    label: 'Phó Hiểu Hiểu',
    value: '1',
  },
  {
    label: 'Châu Mao Mao',
    value: '2',
  },
];

const storeTypeOptions: LabelValueOptions = [
  {
    label: 'Riêng tư',
    value: '1',
  },
  {
    label: 'Công khai',
    value: '2',
  },
];

export const schemas: FormSchema[] = [
  {
    field: 'f1',
    component: 'Input',
    label: 'Tên kho',
    required: true,
  },
  {
    field: 'f2',
    component: 'Input',
    label: 'Tên miền kho',
    required: true,
    componentProps: {
      addonBefore: 'http://',
      addonAfter: 'com',
    },
    colProps: {
      offset: 2,
    },
  },
  {
    field: 'f3',
    component: 'Select',
    label: 'Quản trị viên kho',
    componentProps: {
      options: basicOptions,
    },
    required: true,
    colProps: {
      offset: 2,
    },
  },
  {
    field: 'f4',
    component: 'Select',
    label: 'Người duyệt',
    componentProps: {
      options: basicOptions,
    },
    required: true,
  },
  {
    field: 'f5',
    component: 'RangePicker',
    label: 'Ngày có hiệu lực',
    required: true,
    colProps: {
      offset: 2,
    },
  },
  {
    field: 'f6',
    component: 'Select',
    label: 'Loại kho',
    componentProps: {
      options: storeTypeOptions,
    },
    required: true,
    colProps: {
      offset: 2,
    },
  },
];
export const taskSchemas: FormSchema[] = [
  {
    field: 't1',
    component: 'Input',
    label: 'Tên nhiệm vụ',
    required: true,
  },
  {
    field: 't2',
    component: 'Input',
    label: 'Mô tả nhiệm vụ',
    required: true,
    colProps: {
      offset: 2,
    },
  },
  {
    field: 't3',
    component: 'Select',
    label: 'Người thực hiện',
    componentProps: {
      options: basicOptions,
    },
    required: true,
    colProps: {
      offset: 2,
    },
  },
  {
    field: 't4',
    component: 'Select',
    label: 'Người chịu trách nhiệm',
    componentProps: {
      options: basicOptions,
    },
    required: true,
  },
  {
    field: 't5',
    component: 'TimePicker',
    label: 'Ngày có hiệu lực',
    required: true,
    componentProps: {
      style: { width: '100%' },
    },
    colProps: {
      offset: 2,
    },
  },
  {
    field: 't6',
    component: 'Select',
    label: 'Loại nhiệm vụ',
    componentProps: {
      options: storeTypeOptions,
    },
    required: true,
    colProps: {
      offset: 2,
    },
  },
];
