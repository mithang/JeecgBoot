import { BasicColumn, FormSchema } from '@/components/Table';

//Bảng ocr
export const columns: BasicColumn[] = [
  {
    title: 'Số hiệu',
    dataIndex: 'id',
    ifShow: false,
  },
  {
    title: 'Tiêu đề',
    dataIndex: 'title',
    ellipsis: true,
    width: 300,
  },
  {
    title: 'Prompt',
    dataIndex: 'prompt',
    ellipsis: true,
    width: 300,
  },
];

//Biểu mẫu ocr
export const schemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tiêu đề',
    field: 'title',
    component: 'Input',
    required: true,
  },
  {
    label: 'Prompt',
    field: 'prompt',
    component: 'InputTextArea',
    componentProps: {
      row: 4,
      autosize: { minRows: 4, maxRows: 6 },
    },
    required: true,
  },
];

//Biểu mẫu phân tích ocr
export const analysisSchemas: FormSchema[] = [
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Hình ảnh',
    field: 'url',
    component: 'JImageUpload',
    required: true,
  },
  {
    label: 'Prompt',
    field: 'prompt',
    component: 'InputTextArea',
    componentProps: {
      row: 2,
      autosize: { minRows: 2, maxRows: 2 },
      placeholder:"Vui lòng điền prompt, ví dụ trích xuất chữ trong hình ảnh"
    }
  },
  {
    label: 'Kết quả phân tích',
    field: 'analysisResult',
    component: 'InputTextArea',
    componentProps: {
      row: 10,
      autosize: { minRows: 10, maxRows: 10 },
      readonly: true,
      placeholder:"Kết quả phân tích sẽ hiển thị ở đây"
    },
  },
];
