import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

export const columns: BasicColumn[] = [
  {
    title: 'Tên phân loại',
    dataIndex: 'name',
    width: 350,
    align: 'left',
  },
  {
    title: 'Mã phân loại',
    dataIndex: 'code',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên',
    field: 'name',
    component: 'JInput',
    colProps: { span: 6 },
  },
  {
    label: 'Mã',
    field: 'code',
    component: 'Input',
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
    label: 'Nút cấp cha',
    field: 'pid',
    component: 'TreeSelect',
    componentProps: {
      // Giải thích logic mã: replaceFields đã lỗi thời, dùng fieldNames thay thế------------
      fieldNames: {
        value: 'key',
      },
      dropdownStyle: {
        maxHeight: '50vh',
      },
      getPopupContainer: () => document.body,
    },
    show: ({ values }) => {
      return values.pid !== '0';
    },
    dynamicDisabled: ({ values }) => {
      return !!values.id;
    },
  },
  {
    label: 'Tên phân loại',
    field: 'name',
    required: true,
    component: 'Input',
  },
];
