import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
//Dữ liệu danh sách
export const columns: BasicColumn[] = [
  {
    title: 'Tên mẫu',
    align: 'center',
    dataIndex: 'name',
  },
  {
    title: 'Mã mẫu',
    align: 'center',
    dataIndex: 'code',
  },
  // {
  //   title: 'Đầu trang',
  //   align: 'center',
  //   dataIndex: 'header',
  //   ifShow: false,
  // },
  // {
  //   title: 'Chân trang',
  //   align: 'center',
  //   dataIndex: 'footer',
  //   ifShow: false,
  // },
  // {
  //   title: 'Nội dung chủ thể',
  //   align: 'center',
  //   dataIndex: 'main',
  //   ifShow: false,
  // },
  // {
  //   title: 'Lề trang',
  //   align: 'center',
  //   dataIndex: 'margins',
  //   ifShow: false,
  // },
  // {
  //   title: 'Độ rộng',
  //   align: 'center',
  //   dataIndex: 'width',
  //   ifShow: false,
  // },
  // {
  //   title: 'Chiều cao',
  //   align: 'center',
  //   dataIndex: 'height',
  //   ifShow: false,
  // },
  // {
  //   title: 'Hướng giấy',
  //   align: 'center',
  //   dataIndex: 'paperDirection',
  //   ifShow: false,
  // },
  // {
  //   title: 'Hình mờ',
  //   align: 'center',
  //   dataIndex: 'watermark',
  //   ifShow: false,
  // },
];
//Truy vấn dữ liệu
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Tên mẫu',
    field: 'name',
    component: 'JInput',
  },
  {
    label: 'Mã mẫu',
    field: 'code',
    component: 'JInput',
  },
];
//Dữ liệu biểu mẫu
export const formSchema: FormSchema[] = [
  {
    label: 'Tên mẫu',
    field: 'name',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [{ required: true, message: 'Vui lòng nhập tên mẫu!' }];
    },
  },
  {
    label: 'Mã mẫu',
    field: 'code',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: 'Vui lòng nhập mã mẫu!' },
        { pattern: /^[A-Za-z_]+$/, message: 'Chỉ được nhập tiếng Anh và gạch dưới!' },
      ];
    },
  },
  {
    label: 'Đầu trang',
    field: 'header',
    component: 'Input',
    show: false,
  },
  {
    label: 'Chân trang',
    field: 'footer',
    component: 'Input',
    show: false,
  },
  {
    label: 'Nội dung chủ thể',
    field: 'main',
    component: 'Input',
    show: false,
  },
  {
    label: 'Lề trang',
    field: 'margins',
    component: 'Input',
    show: false,
  },
  {
    label: 'Độ rộng',
    field: 'width',
    component: 'InputNumber',
    show: false,
  },
  {
    label: 'Chiều cao',
    field: 'height',
    component: 'InputNumber',
    show: false,
  },
  {
    label: 'Hướng giấy',
    field: 'paperDirection',
    component: 'Input',
    show: false,
  },
  {
    label: 'Hình mờ',
    field: 'watermark',
    component: 'Input',
    show: false,
  },
  // TODO Trường ẩn khóa chính, hiện viết cứng là ID
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
];

// Dữ liệu truy vấn nâng cao
export const superQuerySchema = {
  name: { title: 'Tên mẫu', order: 0, view: 'text', type: 'string' },
  code: { title: 'Mã mẫu', order: 1, view: 'text', type: 'string' },
  header: { title: 'Đầu trang', order: 2, view: 'text', type: 'string' },
  footer: { title: 'Chân trang', order: 3, view: 'text', type: 'string' },
  main: { title: 'Nội dung chủ thể', order: 4, view: 'text', type: 'string' },
  margins: { title: 'Lề trang', order: 5, view: 'text', type: 'string' },
  width: { title: 'Độ rộng', order: 6, view: 'number', type: 'number' },
  height: { title: 'Chiều cao', order: 7, view: 'number', type: 'number' },
  paperDirection: { title: 'Hướng giấy', order: 8, view: 'text', type: 'string' },
  watermark: { title: 'Hình mờ', order: 9, view: 'text', type: 'string' },
};

/**
 * Biểu mẫu quy trình gọi phương thức này để lấy formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // Mặc định giữ nhất quán với biểu mẫu gốc. Nếu trong quy trình có cấu hình dữ liệu quyền, chỗ này cần xử lý riêng formSchema
  return formSchema;
}
