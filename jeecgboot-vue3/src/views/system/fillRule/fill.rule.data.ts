import { BasicColumn, FormSchema } from '/@/components/Table';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';

export const columns: BasicColumn[] = [
  {
    title: 'Tên quy tắc',
    dataIndex: 'ruleName',
    width: 200,
    align: 'center',
  },
  {
    title: 'Mã quy tắc',
    dataIndex: 'ruleCode',
    width: 200,
    align: 'center',
  },
  {
    title: 'Lớp triển khai quy tắc',
    dataIndex: 'ruleClass',
    width: 300,
    align: 'center',
  },
  {
    title: 'Tham số quy tắc',
    dataIndex: 'ruleParams',
    width: 200,
    align: 'center',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'ruleName',
    label: 'Tên quy tắc',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    field: 'ruleCode',
    label: 'Mã quy tắc',
    component: 'JInput',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'ruleName',
    label: 'Tên quy tắc',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleCode',
    label: 'Mã quy tắc',
    component: 'Input',
    colProps: { span: 24 },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
    dynamicRules: ({ model }) => {
      return [
        {
          required: true,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (!value) {
                return reject('Vui lòng nhập mã quy tắc!');
              }
              let params = {
                tableName: 'sys_fill_rule',
                fieldName: 'rule_code',
                fieldVal: value,
                dataId: model.id,
              };
              duplicateCheckDelay(params)
                .then((res) => {
                  res.success ? resolve() : reject('Mã quy tắc đã tồn tại!');
                })
                .catch((err) => {
                  reject(err.message || 'Kiểm tra không hợp lệ');
                });
            });
          },
        },
      ];
    },
  },
  {
    field: 'ruleClass',
    label: 'Lớp triển khai quy tắc',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'ruleParams',
    label: 'Tham số quy tắc',
    colProps: { span: 24 },
    component: 'JAddInput',
    componentProps: {
      min: 0,
    },
  },
];
