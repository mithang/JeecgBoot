import { FormSchema } from '/@/components/Table';

//Cấu hình cột
export const columns = [
  {
    title: 'Loại liên kết(Người dùng/Vai trò)',
    dataIndex: 'relationType_dictText',
    width: 80,
    slots: { customRender: 'relationType' },
  },
  {
    title: 'Người dùng/Vai trò',
    dataIndex: 'relationName',
    width: 80,
    slots: { customRender: 'roleCode' },
  },
  {
    title: 'Định tuyến trang chủ',
    dataIndex: 'url',
    width: 100,
  },
  {
    title: 'Địa chỉ component',
    dataIndex: 'component',
    width: 100,
  },
  {
    title: 'Có bật không',
    dataIndex: 'status',
    slots: { customRender: 'status' },
    width: 60,
  },
];
//Truy vấn cấu hình
export const searchFormSchema: FormSchema[] = [
  {
    field: 'relationType',
    label: 'Loại liên kết',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: 'relation_type',
    },
  },
  {
    field: 'route',
    label: 'Có phải menu định tuyến không',
    helpMessage: 'Menu phi định tuyến được đặt làm trang chủ thì cần bật',
    component: 'Switch',
    show: false,
  },
];

export const formSchema: FormSchema[] = [
  {
    field: 'id',
    label: '',
    component: 'Input',
    show: false,
  },
  {
    field: 'relationType',
    label: 'Loại liên kết',
    component: 'JDictSelectTag',
    required: true,
    defaultValue: 'ROLE',
    componentProps: {
      dictCode: 'relation_type',
      type: 'radioButton',
    },
  },
  {
    label: 'Mã vai trò',
    field: 'roleCode',
    component: 'JSelectRole',
    required: true,
    componentProps: {
      rowKey: 'roleCode',
      isRadioSelection: false,
    },
    ifShow: ({ values }) => values.relationType == 'ROLE',
  },
  {
    label: 'Mã người dùng',
    field: 'userCode',
    component: 'JSelectUser',
    required: true,
    componentProps: {
      isRadioSelection: false,
    },
    ifShow: ({ values }) => values.relationType == 'USER',
  },
  {
    label: 'Định tuyến trang chủ',
    field: 'url',
    component: 'Input',
    required: true,
  },
  {
    label: 'Địa chỉ component',
    field: 'component',
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập component frontend',
    },
    required: true,
  },
  {
    label: 'Mức ưu tiên',
    field: 'priority',
    component: 'InputNumber',
  },
  {
    field: 'route',
    label: 'Có phải menu định tuyến không',
    helpMessage: 'Menu phi định tuyến được đặt làm trang chủ thì cần bật',
    component: 'Switch',
    defaultValue: true,
    show: false,
  },
  {
    label: 'Có bật không',
    field: 'status',
    component: 'JSwitch',
    defaultValue: '1',
    componentProps: {
      options: ['1', '0'],
    },
  },
];
