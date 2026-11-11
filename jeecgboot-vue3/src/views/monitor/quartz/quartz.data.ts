import { BasicColumn, FormSchema } from '/@/components/Table';
import { render } from '/@/utils/common/renderUtils';
import JCronValidator from '/@/components/Form/src/jeecg/components/JEasyCron/validator';

export const columns: BasicColumn[] = [
  {
    title: 'Tên lớp nhiệm vụ',
    dataIndex: 'jobClassName',
    width: 200,
    align: 'left',
  },
  {
    title: 'Biểu thức Cron',
    dataIndex: 'cronExpression',
    width: 200,
  },
  {
    title: 'Tham số',
    dataIndex: 'parameter',
    width: 200,
  },
  {
    title: 'Mô tả',
    dataIndex: 'description',
    width: 200,
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 100,
    customRender: ({ text }) => {
      const color = text == '0' ? 'green' : text == '-1' ? 'red' : 'gray';
      return render.renderTag(render.renderDict(text, 'quartz_status'), color);
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'jobClassName',
    label: 'Tên lớp nhiệm vụ',
    component: 'Input',
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Trạng thái nhiệm vụ',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      stringToNumber: true,
    },
    colProps: { span: 6 },
  },
   {
    field: 'description',
    label: 'Mô tả',
    component: 'JInput',
    colProps: { span: 6 },
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'jobClassName',
    label: 'Tên lớp nhiệm vụ',
    component: 'Input',
    required: true,
  },
  {
    field: 'cronExpression',
    label: 'Biểu thức Cron',
    component: 'JEasyCron',
    defaultValue: '* * * * * ? *',
    rules: [{ required: true, message: 'Vui lòng nhập biểu thức Cron' }, { validator: JCronValidator }],
  },
  {
    field: 'paramterType',
    label: 'Loại tham số',
    component: 'Select',
    defaultValue: 'string',
    componentProps: {
      options: [
        { label: 'Chuỗi', value: 'string' },
        { label: 'Đối tượng JSON', value: 'json' },
      ],
    },
  },
  {
    field: 'parameter',
    label: 'Tham số',
    component: 'InputTextArea',
    ifShow: ({ values }) => {
      return values.paramterType == 'string';
    },
  },
  {
    field: 'parameter',
    label: 'Tham số',
    component: 'JAddInput',
    helpMessage: 'Điền dạng cặp key-value',
    ifShow: ({ values }) => {
      return values.paramterType == 'json';
    },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'quartz_status',
      type: 'radioButton',
      stringToNumber: true,
      dropdownStyle: {
        maxHeight: '6vh',
      },
    },
  },
  {
    field: 'description',
    label: 'Mô tả',
    component: 'InputTextArea',
  },
];
