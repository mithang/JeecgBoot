import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Thời gian gọi',
    dataIndex: 'callTime',
    align: 'center',
    width: 170,
  },
  {
    title: 'Phương thức yêu cầu',
    dataIndex: 'requestMethod',
    align: 'center',
    width: 90,
  },
  {
    title: 'Đường dẫn yêu cầu',
    dataIndex: 'requestPath',
    align: 'left',
    width: 220,
    ellipsis: true,
  },
  {
    title: 'AK của bên gọi',
    dataIndex: 'callerAk',
    align: 'center',
    width: 180,
    ellipsis: true,
  },
  {
    title: 'IP nguồn',
    dataIndex: 'ip',
    align: 'center',
    width: 140,
  },
  {
    title: 'Mã trạng thái',
    dataIndex: 'responseCode',
    align: 'center',
    width: 90,
  }
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'AK của bên gọi',
    field: 'callerAk',
    component: 'JInput',
  },
  {
    label: 'IP nguồn',
    field: 'ip',
    component: 'JInput',
  },
  {
    label: 'Đường dẫn yêu cầu',
    field: 'requestPath',
    component: 'JInput',
  },
  {
    label: 'Phương thức yêu cầu',
    field: 'requestMethod',
    component: 'JSearchSelect',
    componentProps: {
      dictOptions: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH','HEAD','OPTIONS','TRACE'].map((v) => ({ text: v, value: v })),
    },
  },
  {
    label: 'Mã trạng thái phản hồi',
    field: 'responseCode',
    component: 'Input',
  },
  {
    label: 'Thời gian gọi',
    field: 'callTime',
    component: 'RangePicker',
    componentProps: { valueType: 'Date', showTime: true },
  },
];
