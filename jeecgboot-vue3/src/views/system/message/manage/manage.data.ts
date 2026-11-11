import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tiêu đề tin nhắn',
    dataIndex: 'esTitle',
    width: 140,
  },
  {
    title: 'Nội dung gửi',
    dataIndex: 'esContent',
    width: 200,
    // slots: { customRender: 'esContent' },
  },
  {
    title: 'Người nhận',
    dataIndex: 'esReceiver',
    width: 140,
  },
  {
    title: 'Số lần gửi',
    dataIndex: 'esSendNum',
    width: 120,
  },
  {
    title: 'Trạng thái gửi',
    dataIndex: 'esSendStatus_dictText',
    width: 120,
  },
  {
    title: 'Thời gian gửi',
    dataIndex: 'esSendTime',
    width: 140,
  },
  {
    title: 'Phương thức gửi',
    dataIndex: 'esType_dictText',
    width: 120,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tiêu đề tin nhắn',
    field: 'esTitle',
    component: 'Input',
  },
  {
    label: 'Trạng thái gửi',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'msgSendStatus',
    },
  },
  {
    label: 'Phương thức gửi',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'messageType',
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
    label: 'Tiêu đề tin nhắn',
    field: 'esTitle',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Nội dung gửi',
    field: 'esContent',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
  {
    label: 'Tham số gửi',
    field: 'esParam',
    component: 'Input',
    componentProps: { readOnly: true },
  },

  {
    label: 'Người nhận',
    field: 'esReceiver',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Phương thức gửi',
    field: 'esType',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'messageType' },
  },
  {
    label: 'Thời gian gửi',
    field: 'esSendTime',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Trạng thái gửi',
    field: 'esSendStatus',
    component: 'JDictSelectTag',
    componentProps: { disabled: true, dictCode: 'msgSendStatus' },
  },
  {
    label: 'Số lần gửi',
    field: 'esSendNum',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Nguyên nhân gửi thất bại',
    field: 'esResult',
    component: 'Input',
    componentProps: { readOnly: true },
  },
  {
    label: 'Ghi chú',
    field: 'remark',
    component: 'InputTextArea',
    componentProps: { readOnly: true },
  },
];
