import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';

//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Đối tượng ủy quyền',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: 'Khóa truy cập (AK)',
    align: 'center',
    dataIndex: 'ak',
    ellipsis: true,
  },
  {
    title: 'Người tạo',
    align: 'center',
    dataIndex: 'createBy',
  },
  {
    title: 'Thời gian tạo',
    align: 'center',
    dataIndex: 'createTime',
  },
];

//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Đối tượng ủy quyền',
    field: 'name',
    component: 'JInput',
  },
  {
    label: 'Khóa truy cập',
    field: 'ak',
    component: 'JInput',
  },
];

//Dữ liệu biểu mẫu ủy quyền
export const authFormSchema: FormSchema[] = [
  {
    label: 'Đối tượng ủy quyền',
    field: 'name',
    component: 'Input',
    required: true,
  },
  {
    label: '',
    field: 'ak',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'sk',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: '',
    field: 'systemUserId',
    component: 'Input',
    show: false,
  },
];

// Dữ liệu tìm kiếm nâng cao
export const superQuerySchema = {
  name: { title: 'Đối tượng ủy quyền', order: 0, view: 'text', type: 'string' },
  ak: { title: 'Khóa truy cập (AK)', order: 1, view: 'text', type: 'string' },
  sk: { title: 'Khóa ký (SK)', order: 2, view: 'text', type: 'string' },
  createBy: { title: 'Người tạo', order: 3, view: 'text', type: 'string' },
  createTime: { title: 'Thời gian tạo', order: 4, view: 'datetime', type: 'string' },
};
