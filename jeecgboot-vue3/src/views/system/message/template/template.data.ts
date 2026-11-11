import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { filterDictTextByCache } from '/@/utils/dict/JDictSelectUtil';

export const columns: BasicColumn[] = [
  {
    title: 'Tiêu đề mẫu',
    dataIndex: 'templateName',
    width: 80,
  },
  {
    title: 'Mã mẫu',
    dataIndex: 'templateCode',
    width: 100,
  },
  {
    title: 'Mẫu thông báo',
    dataIndex: 'templateContent',
    width: 150,
  },
  {
    title: 'Loại mẫu',
    dataIndex: 'templateType',
    width: 100,
    customRender: ({ text }) => filterDictTextByCache('msgType', text),
  },
  {
    title: 'Có áp dụng không',
    dataIndex: 'useStatus',
    width: 90,
    customRender: function ({ text }) {
      if (text == '1') {
        return 'Có';
      } else {
        return 'Không';
      }
    },
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tiêu đề mẫu',
    field: 'templateName',
    component: 'Input',
  },
  {
    label: 'Mã mẫu',
    field: 'templateCode',
    component: 'Input',
  },
  {
    label: 'Loại mẫu',
    field: 'templateType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgType',
    },
  },
];

export const formSchemas: FormSchema[] = [
  {
    label: 'ID',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tiêu đề mẫu',
    field: 'templateName',
    component: 'Input',
    required: true,
  },
  {
    label: 'Mã mẫu',
    field: 'templateCode',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => {
      return [ ...rules.duplicateCheckRule('sys_sms_template', 'template_code', model, schema, true)];
    },
    // Ở chế độ chỉnh sửa không thể sửa mã
    dynamicDisabled: (params) => !!params.values.id,
  },
  {
    label: 'Loại mẫu',
    field: 'templateType',
    component: 'JDictSelectTag',
    defaultValue: '1',
    componentProps: {
      dictCode: 'msgType',
      type: 'radio',
      placeholder: 'Vui lòng chọn loại mẫu',
    },
    required: true,
  },
  {
    label: 'Phân loại mẫu',
    field: 'templateCategory',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgCategory',
      placeholder: 'Vui lòng chọn phân loại mẫu',
    }
  },
  {
    label: 'Có áp dụng không',
    field: 'useStatus',
    component: 'JSwitch',
    componentProps: {
      options: ['1', '0'],
    },
  },
  {
    label: 'Nội dung mẫu',
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: {
      autoSize: {
        minRows: 8,
        maxRows: 8,
      },
    },
    ifShow: ({ values }) => {
      return !['2', '4', '5'].includes(values.templateType);
    },
  },

  {
    label: 'Nội dung mẫu',
    field: 'templateContent',
    component: 'JEditor',
    ifShow: ({ values }) => {
      return ['2', '4'].includes(values.templateType);
    },
  },
  {
    label: 'Nội dung mẫu',
    field: 'templateContent',
    component: 'JMarkdownEditor',
    ifShow: ({ values }) => {
      return ['5'].includes(values.templateType);
    },
  },
];

export const sendTestFormSchemas: FormSchema[] = [
  {
    label: 'Mã mẫu',
    field: 'templateCode',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tiêu đề mẫu',
    field: 'templateName',
    component: 'Input',
    componentProps: { disabled: true },
  },
  {
    label: 'Nội dung mẫu',
    field: 'templateContent',
    component: 'InputTextArea',
    componentProps: { disabled: true, rows: 5 },
  },
  {
    label: 'Dữ liệu kiểm tra',
    field: 'testData',
    component: 'InputTextArea',
    required: true,
    helpMessage: 'JSONDữ liệu',
    defaultValue: '{}',
    componentProps: {
      placeholder: 'Vui lòng nhập dữ liệu kiểm tra định dạng JSON',
      rows: 5,
    },
  },
  {
    label: 'Loại tin nhắn',
    field: 'msgType',
    component: 'JDictSelectTag',
    required: true,
    defaultValue:'system',
    componentProps: { dictCode: 'messageType',type:'radio' },
  },
  {
    label: 'Bên nhận tin nhắn',
    field: 'receiver',
    required: true,
    component: 'JSelectUser',
    componentProps: {
      labelKey: 'username',
      rowKey: 'username',
    },
  },
];
