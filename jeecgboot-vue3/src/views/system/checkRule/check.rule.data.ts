import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import { duplicateCheckDelay } from '/@/views/system/user/user.api';
import { validateCheckRule } from '/@/views/system/checkRule/check.rule.api';
import { array } from 'vue-types';

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
    title: 'Mô tả quy tắc',
    dataIndex: 'ruleDescription',
    width: 300,
    align: 'center',
    customRender: function ({ text }) {
      return render.renderTip(text, 30);
    },
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
                tableName: 'sys_check_rule',
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
    field: 'ruleDescription',
    label: 'Mô tả quy tắc',
    colProps: { span: 24 },
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Vui lòng nhập mô tả quy tắc',
      rows: 2,
    },
  },
];

export const checkRuleInput: FormSchema[] = [
  {
    label: '123',
    field: 'ruleCode',
    component: 'Input',
    show: false,
  },
  {
    field: 'testValue',
    label: 'Giá trị cần kiểm tra:',
    component: 'Input',
    componentProps: ({ formModel }) => {
      return {
        onChange: (e) => {
          formModel.testValue = e.target.value;
        },
      };
    },
    dynamicRules: ({ model }) => {
      const { ruleCode } = model;
      return [
        {
          required: false,
          validator: (_, value) => {
            return new Promise((resolve, reject) => {
              if (ruleCode && value) {
                /*console.log({ruleCode,value})*/
                validateCheckRule(ruleCode, value)
                  .then((res) => {
                    //console.log(1233, res)
                    res['success'] ? resolve() : reject(res['message']);
                  })
                  .catch((err) => {
                    reject(err.message || err);
                  });
              } else {
                resolve();
              }
            });
          },
        },
      ];
    },
  },
];
