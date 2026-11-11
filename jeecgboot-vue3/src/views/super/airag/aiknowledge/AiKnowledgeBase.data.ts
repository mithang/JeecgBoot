import { FormSchema } from '@/components/Form';
import { BasicColumn } from '@/components/Table';

/**
 * Biểu mẫu
 */
export const formSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên kho tri thức',
    field: 'name',
    required: true,
    componentProps: {
      placeholder: 'Vui lòng nhập tên kho tri thức',
      // Có hiển thị số ký tự hay không
      showCount: true,
      maxlength: 64,
    },
    component: 'Input',
  },
  {
    label: 'Mô tả kho tri thức',
    field: 'descr',
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Mô tả nội dung của kho tri thức, mô tả chi tiết sẽ giúp AI hiểu sâu nội dung của kho tri thức này, có thể truy xuất nội dung chính xác hơn, nâng cao tỷ lệ trúng của kho tri thức này.',
      // Có hiển thị số ký tự hay không
      showCount: true,
      maxlength: 256,
    },
  },
  {
    label: 'Mô hình vector',
    field: 'embedId',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "airag_model where model_type = 'EMBED' and activate_flag = 1,name,id",
    },
  },
  {
    label: 'Trạng thái',
    field: 'status',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: 'Kích hoạt', value: 'enable' },
        { label: 'Vô hiệu hóa', value: 'disable' },
      ],
      type: 'radioButton',
    },
    defaultValue: 'enable',
  },
  {
    label: 'Loại',
    field: 'type',
    required: true,
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: 'Kho tri thức', value: 'knowledge' },
        { label: 'Kho bộ nhớ', value: 'memory' },
      ],
      type: 'radioButton',
    },
    defaultValue: 'knowledge',
  },
  {
    label: 'Chiến lược phân đoạn',
    field: 'enableSegment',
    component: 'Switch',
    defaultValue: false,
    ifShow: ({ values }) => values.type !== 'memory',
    componentProps: {
      checkedChildren: 'Bật',
      unCheckedChildren: 'Đóng',
    },
    helpMessage: 'Sau khi bật, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn này; tài liệu cũng có thể cấu hình riêng chiến lược phân đoạn của mình',
  },
  {
    label: 'Chế độ phân đoạn',
    field: 'segmentStrategy',
    component: 'RadioGroup',
    defaultValue: 'auto',
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      options: [
        { label: 'Tự động phân đoạn và làm sạch', value: 'auto' },
        { label: 'Tùy chỉnh', value: 'custom' },
      ],
    },
  },
  {
    label: 'Định danh phân đoạn',
    field: 'separator',
    component: 'Select',
    defaultValue: '\\n',
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.segmentStrategy === 'custom',
    componentProps: {
      getPopupContainer: () => document.body,
      options: [
        { label: 'Xuống dòng', value: '\\n' },
        { label: '2 lần xuống dòng', value: '\\n\\n' },
        { label: 'Dấu chấm tiếng Trung', value: '。' },
        { label: 'Dấu chấm than tiếng Trung', value: '！' },
        { label: 'Dấu chấm hỏi tiếng Trung', value: '？' },
        { label: 'Dấu chấm tiếng Anh', value: '.' },
        { label: 'Dấu chấm than tiếng Anh', value: '!' },
        { label: 'Dấu chấm hỏi tiếng Anh', value: '?' },
        { label: 'Tùy chỉnh', value: 'custom' },
      ],
    },
  },
  {
    label: 'Dấu phân cách tùy chỉnh',
    field: 'customSeparator',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.separator === 'custom' && values.segmentStrategy !== 'auto',
  },
  {
    label: 'Độ dài tối đa của đoạn',
    field: 'maxSegment',
    component: 'InputNumber',
    defaultValue: 800,
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      min: 100,
      max: 5000,
    },
  },
  {
    label: 'Mức chồng lấp phân đoạn%',
    field: 'overlap',
    component: 'InputNumber',
    defaultValue: 10,
    required: true,
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true,
    componentProps: {
      min: 0,
      max: 90,
    },
  },
  {
    label: 'Quy tắc tiền xử lý văn bản',
    field: 'textRules',
    component: 'CheckboxGroup',
    defaultValue: [],
    ifShow: ({ values }) => values.type !== 'memory' && values.enableSegment === true && values.segmentStrategy === 'custom',
    componentProps: {
      options: [
        { label: 'Thay thế các dấu cách liên tiếp, ký tự xuống dòng và ký tự tab', value: 'cleanSpaces' },
        { label: 'Xóa tất cả URL và địa chỉ email', value: 'removeUrlsEmails' },
      ],
    },
  },
];

//Biểu mẫu văn bản tài liệu
export const docTextSchema: FormSchema[] = [
  {
    label: 'id',
    field: 'id',
    component: 'Input',
    show: false,
  },
  {
    label: 'id kho tri thức',
    field: 'knowledgeId',
    show: false,
    component: 'Input',
  },
  {
    label: 'Tiêu đề',
    field: 'title',
    required: true,
    component: 'Input',
  },
  {
    label: 'Loại',
    field: 'type',
    required: true,
    component: 'Input',
    show: false
  },
  {
    label: 'Nội dung',
    field: 'content',
    rules: [{ required: true, message: 'Vui lòng nhập nội dung' }],
    component: 'JMarkdownEditor',
    componentProps: {
      placeholder: "Vui lòng nhập nội dung",
      preview:{ mode: 'view', action: [] }
    },
    ifShow:({ values})=>{
      if(values.type === 'text'){
        return true;
      }
      return false;
    }
  },
  {
    label: 'Tệp',
    field: 'filePath',
    rules: [{ required: true, message: 'Vui lòng tải lên tệp' }],
    component: 'JUpload',
    helpMessage:'Hỗ trợ txt, markdown, pdf, docx, xlsx, pptx',
    componentProps:{
      fileType: 'file',
      maxCount: 1,
      multiple: false,
      text: 'Tải lên tài liệu'
    },
    ifShow:({ values })=>{
      if(values.type === 'file'){
        return true;
      }
      return false;
    }
  },
  {
    label: 'Địa chỉ trang web',
    field: 'website',
    rules: [
      { required: true, message: 'Vui lòng nhập URL trang web' },
      { pattern: /^https?:\/\//, message: 'Vui lòng nhập địa chỉ trang web chính xác, bắt đầu bằng http:// hoặc https://' },
    ],
    component: 'Input',
    componentProps: {
      placeholder: 'Vui lòng nhập URL trang web, ví dụ: https://help.jeecg.com/',
    },
    ifShow:({ values })=>{
      if(values.type === 'web'){
        return true;
      }
      return false;
    }
  },
];

/**
 * Biểu mẫu chiến lược phân đoạn
 */
export const docSegmentSchema: FormSchema[] = [
  {
    label: 'Chiến lược phân đoạn',
    field: 'segmentStrategy',
    component: 'RadioGroup',
    defaultValue: 'auto',
    componentProps: {
      options: [
        { label: 'Tự động phân đoạn và làm sạch', value: 'auto' },
        { label: 'Tùy chỉnh', value: 'custom' },
      ],
    },
  },
  {
    label: 'Định danh phân đoạn',
    field: 'separator',
    component: 'Select',
    defaultValue: '\\n',
    required: true,
    ifShow: ({ values }) => values.segmentStrategy === 'custom',
    componentProps: {
      getPopupContainer: () => document.body,
      options: [
        { label: 'Xuống dòng', value: '\\n' },
        { label: '2 lần xuống dòng', value: '\\n\\n' },
        { label: 'Dấu chấm tiếng Trung', value: '。' },
        { label: 'Dấu chấm than tiếng Trung', value: '！' },
        { label: 'Dấu chấm hỏi tiếng Trung', value: '？' },
        { label: 'Dấu chấm tiếng Anh', value: '.' },
        { label: 'Dấu chấm than tiếng Anh', value: '!' },
        { label: 'Dấu chấm hỏi tiếng Anh', value: '?' },
        { label: 'Tùy chỉnh', value: 'custom' },
      ],
    },
  },
  {
    label: '',
    field: 'customSeparator',
    component: 'Input',
    required: true,
    ifShow: ({ values }) => values.separator === 'custom' && values.segmentStrategy !== 'auto',
  },
  {
    label: 'Độ dài tối đa của đoạn',
    field: 'maxSegment',
    component: 'InputNumber',
    defaultValue: 800,
    required: true,
    componentProps: {
      min: 100,
      max: 5000,
    },
  },
  {
    label: 'Mức chồng lấp phân đoạn%',
    field: 'overlap',
    component: 'InputNumber',
    defaultValue: 10,
    componentProps: {
      min: 0,
      max: 90,
    },
    required: true,
  },
  {
    label: 'Quy tắc tiền xử lý văn bản',
    field: 'textRules',
    component: 'CheckboxGroup',
    defaultValue: [],
    ifShow: ({ values }) => values.segmentStrategy === 'custom',
    componentProps: {
      options: [
        { label: 'Thay thế các dấu cách liên tiếp, ký tự xuống dòng và ký tự tab', value: 'cleanSpaces' },
        { label: 'Xóa tất cả URL và địa chỉ email', value: 'removeUrlsEmails' },
      ],
    },
  },
];

