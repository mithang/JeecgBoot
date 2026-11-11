import {BasicColumn, FormSchema} from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

const statusOptions = [
  {label: 'Vô hiệu hóa', value: '0'},
  {label: 'Kích hoạt', value: '1'},
]

export const columns: BasicColumn[] = [
  {
    title: 'Tên bảng được phép',
    dataIndex: 'tableName',
  },
  {
    title: 'Tên trường được phép',
    dataIndex: 'fieldName',
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    customRender({text}) {
      const find = statusOptions.find(opt => opt.value === text);
      return find?.label || 'Không xác định';
    }
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
  }
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên bảng được phép',
    field: 'tableName',
    component: 'JInput',
  },
  {
    label: 'Tên trường được phép',
    field: 'fieldName',
    component: 'JInput',
  },
  {
    label: 'Trạng thái',
    field: 'status',
    component: 'Select',
    componentProps: {
      options: statusOptions,
    },
  },
];

export const formSchema: FormSchema[] = [
  {label: '', field: 'id', component: 'Input', show: false},
  {
    label: 'Tên bảng được phép',
    field: 'tableName',
    component: 'Input',
    required: true,
    dynamicRules: ({ model, schema }) => rules.duplicateCheckRule('sys_table_white_list', 'table_name', model, schema, true),
  },
  {
    label: 'Tên trường được phép',
    field: 'fieldName',
    component: 'Input',
    required: true,
    helpMessage: 'Nhiều giá trị phân tách bằng dấu phẩy',
  },
  {
    label: 'Trạng thái',
    field: 'status',
    component: 'Select',
    defaultValue: '1',
    componentProps: {
      options: statusOptions,
    },
  },
];
