import { BasicColumn, FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Nội dung nhật ký',
    dataIndex: 'logContent',
    width: 200,
    align: 'left',
    ellipsis: true,
  },
  {
    title: 'ID người thao tác',
    dataIndex: 'userid',
    width: 80,
  },
  {
    title: 'Người thao tác',
    dataIndex: 'username',
    width: 80,
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 60,
  },
  {
    title: 'Thời gian xử lý (mili giây)',
    dataIndex: 'costTime',
    width: 50,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    sorter: true,
    width: 80,
  },
  {
    title: 'Loại client',
    dataIndex: 'clientType_dictText',
    width: 50,
  },
];

export const loginLogColumns: BasicColumn[] = columns.filter((column) => column.dataIndex !== 'costTime');

/**
 * Nhật ký thao tác cần loại thao tác
 */
export const operationLogColumn: BasicColumn[] = [
  ...columns,
  {
    title: 'Loại thao tác',
    dataIndex: 'operateType_dictText',
    width: 40,
  },
];

export const exceptionColumns: BasicColumn[] = [
  {
    title: 'Tiêu đề ngoại lệ',
    dataIndex: 'logContent',
    width: 200,
    align: 'left',
    ellipsis: true,
  },
  {
    title: 'Địa chỉ yêu cầu',
    dataIndex: 'requestUrl',
    width: 140,
    align: 'left',
    ellipsis: true,
  },
  {
    title: 'Phương thức yêu cầu',
    dataIndex: 'method',
    width: 120,
    align: 'left',
    ellipsis: true,
  },
  {
    title: 'Người thao tác',
    dataIndex: 'username',
    width: 80,
    customRender: ({ record }) => {
      const pname = record.username;
      const pid = record.userid;
      if (!pname && !pid) {
        return '';
      }
      return pname + ' (' + pid + ')';
    },
  },
  {
    title: 'IP',
    dataIndex: 'ip',
    width: 60,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    sorter: true,
    width: 80,
  },
  {
    title: 'Loại client',
    dataIndex: 'clientType_dictText',
    width: 50,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyWord',
    label: 'Tìm kiếm nhật ký',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    field: 'fieldTime',
    component: 'RangePicker',
    label: 'Thời gian tạo',
    componentProps: {
      valueType: 'Date',
    },
    colProps: {
      span: 6,
    },
  },
];

export const operationSearchFormSchema: FormSchema[] = [
  ...searchFormSchema,
  {
    field: 'operateType',
    label: 'Loại thao tác',
    component: 'JDictSelectTag',
    colProps: { span: 4 },
    componentProps: {
      dictCode: 'operate_type',
    },
  },
];
