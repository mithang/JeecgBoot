import { FormSchema } from '/@/components/Form';

export const step1Schemas: FormSchema[] = [
  {
    field: 'account',
    component: 'Select',
    label: 'Tài khoản thanh toán',
    required: true,
    defaultValue: '1',
    componentProps: {
      options: [
        {
          label: 'anncwb@126.com',
          value: '1',
        },
      ],
    },
  },
  {
    field: 'fac',
    component: 'InputGroup',
    label: 'Tài khoản nhận tiền',
    required: true,
    defaultValue: 'test@example.com',
    slot: 'fac',
  },
  {
    field: 'pay',
    component: 'Input',
    label: '',
    defaultValue: 'zfb',
    show: false,
  },
  {
    field: 'payeeName',
    component: 'Input',
    label: 'Tên người nhận tiền',
    defaultValue: 'Jeecg',
    required: true,
  },
  {
    field: 'money',
    component: 'Input',
    label: 'Số tiền chuyển khoản',
    defaultValue: '500',
    required: true,
    renderComponentContent: () => {
      return {
        prefix: () => '￥',
      };
    },
  },
];

export const step2Schemas: FormSchema[] = [
  {
    field: 'pwd',
    component: 'InputPassword',
    label: 'Mật khẩu thanh toán',
    required: true,
    defaultValue: '123456',
  },
];
