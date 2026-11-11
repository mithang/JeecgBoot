import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Tên',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: 'Thông tin mô tả',
    align: 'center',
    dataIndex: 'descr',
  },
];
//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên',
    field: 'name',
    component: 'JInput',
    colProps: {
      span: 6,
    },
  },
];
//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: 'Định danh loại nghiệp vụ',
    field: 'bizType',
    component: 'Input',
    defaultValue: 'evaluator',
    show: false,
  },
  {
    label: 'Tên',
    field: 'name',
    component: 'Input',
  },
  {
    label: 'Thông tin mô tả',
    field: 'descr',
    component: 'InputTextArea',
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

/**
 * Biểu mẫu quy trình gọi phương thức này để lấy formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // Mặc định giữ nhất quán với biểu mẫu gốc. Nếu trong quy trình có cấu hình dữ liệu quyền, chỗ này cần xử lý riêng formSchema
  return formSchema;
}
