import { FormSchema } from '/@/components/Form';

export const formSchema: FormSchema[] = [
  {
    field: 'passwordOld',
    label: 'Mật khẩu hiện tại',
    component: 'InputPassword',
    required: true,
  },
  {
    field: 'passwordNew',
    label: 'Mật khẩu mới',
    component: 'StrengthMeter',
    componentProps: {
      placeholder: 'Mật khẩu mới',
    },
    rules: [
      {
        required: true,
        message: 'Vui lòng nhập mật khẩu mới',
      },
    ],
  },
  {
    field: 'confirmPassword',
    label: 'Xác nhận mật khẩu',
    component: 'InputPassword',

    dynamicRules: ({ values }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            if (!value) {
              return Promise.reject('Không được trống');
            }
            if (value !== values.passwordNew) {
              return Promise.reject('Mật khẩu nhập hai lần không khớp!');
            }
            return Promise.resolve();
          },
        },
      ];
    },
  },
];
