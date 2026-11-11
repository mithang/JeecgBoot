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
    title: 'Mô tả chức năng',
    align: 'center',
    dataIndex: 'description',
  },
  // {
  //   title: 'Trạng thái',
  //   align: 'center',
  //   dataIndex: 'status',
  // },
  // {
  //   title: 'Người gửi gần nhất',
  //   align: 'center',
  //   dataIndex: 'updateBy',
  // },
  // {
  //   title: 'Thời gian gửi gần nhất',
  //   align: 'center',
  //   dataIndex: 'updateTime',
  // },
  // {
  //   title: 'Thời gian tạo',
  //   align: 'center',
  //   dataIndex: 'createTime',
  // }
];
//Dữ liệu danh sách thùng rác
export const recycleColumns: BasicColumn[] = [
  {
    title: 'Tên',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: 'Mô tả chức năng',
    align: 'center',
    dataIndex: 'description',
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
      label: 'Tên',
      field: 'name',
      component: 'JInput',
    },
];
// Độ dài tối đa của tên
export const NAME_MAX_LENGTH = 40;
// Độ dài tối đa của mã
export const CODE_MAX_LENGTH = 50;
//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: 'Tên',
    field: 'name',
    component: 'Input',
    componentProps: () => {
      return {
        placeholder: 'Ví dụ: chuyển đổi SQL',
        maxlength: 40,
        showCount: true
      };
    },
    dynamicRules() {
      return [
        {required: true, message: 'Vui lòng nhập tên prompt'},
        {
          max: NAME_MAX_LENGTH,
          message: `Độ dài tên không được vượt quá${NAME_MAX_LENGTH}ký tự`,
        },
      ];
    }
  },
  {
    label: 'Mô tả chức năng prompt',
    field: 'description',
    component: 'InputTextArea',
    componentProps: {
      rows: 5,
    },
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
