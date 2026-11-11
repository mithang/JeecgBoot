import { BasicColumn, FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';

export const columns: BasicColumn[] = [
  // {
  //   title: 'Mã chức danh',
  //   dataIndex: 'code',
  //   width: 200,
  //   align: 'left',
  // },
  {
    title: 'Cấp bậc chức danh',
    dataIndex: 'name',
    align: 'left'
    // width: 200,
  },
  {
    title: 'Số thứ tự cấp bậc (số càng nhỏ, cấp bậc càng cao)',
    dataIndex: 'postLevel',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'name',
    label: 'Cấp bậc chức danh',
    component: 'JInput',
    colProps: { span: 8 },
  },
];

export const formSchema: FormSchema[] = [
  {
    label: 'Khóa chính',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    field: 'name',
    label: 'Cấp bậc chức danh',
    component: 'Input',
    required: true,
  },
  {
    label: 'Số thứ tự cấp bậc',
    field: 'postLevel',
    component: 'InputNumber',
    required: true,
    componentProps: {
      min: 1,
      max: 99
    },
    itemProps: {
      extra: 'Số càng nhỏ, cấp bậc càng cao; quy trình chỉ nhận dạng 1—6 cấp bậc định sẵn, các cấp khác không thể dùng cho biểu thức cấp bậc của quy trình.',
    },
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: 'Vui lòng nhập số thứ tự cấp bậc!' }];
    },
  },
  // {
  //   field: 'code',
  //   label: 'Mã chức danh',
  //   component: 'Input',
  //   required: true,
  //   dynamicDisabled: ({ values }) => {
  //     return !!values.id;
  //   },
  //   dynamicRules: ({ model, schema }) => {
  //     return rules.duplicateCheckRule('sys_position', 'code', model, schema, true);
  //   },
  // },
];
