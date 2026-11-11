import { FormSchema } from '@/components/Form';

/**
 * Thay trang phục AI - Biểu mẫu tạo hình ảnh
 */
export const clothImageFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: ['1. Cần chọn mô hình hình ảnh đã kích hoạt', '2. Hiện tại khuyên dùng mô hình Tongyi Wanxiang (wan2.5-i2i-preview)', '3. Khuyên tải lên ảnh người mẫu và ảnh trang phục rõ nét để đạt hiệu quả tốt nhất'],
    componentProps: {
      dictCode: "airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id",
      placeholder: 'Vui lòng chọn mô hình hình ảnh',
    },
  },
  {
    field: 'modelImage',
    label: 'Hình ảnh người mẫu',
    component: 'JImageUpload',
    required: true,
    componentProps: {
      fileMax: 1,
      text: 'Tải lên người mẫu',
    },
    helpMessage: ['Tải lên ảnh người mẫu, khuyên dùng ảnh toàn thân, mặt trước rõ nét'],
  },

  {
    field: 'clothUpload',
    label: 'Trang phục',
    slot: 'clothUpload',
    component: 'Input',
    required: false,
  },
  {
    field: 'userPrompt',
    label: 'Prompt',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: 'Nhập prompt của bạn tại đây, hoặc dùng ví dụ để điền nhanh',
    },
    required: true,
  },
];

/**
 * Thay trang phục AI - Biểu mẫu tạo video
 */
export const clothVideoFormSchema: FormSchema[] = [
  {
    field: 'drawModelId',
    label: 'Mô hình',
    component: 'JDictSelectTag',
    required: true,
    helpMessage: ['1. Cần chọn mô hình video đã kích hoạt', '2. Khuyên chọn mô hình hỗ trợ tạo video từ hình ảnh'],
    componentProps: {
      dictCode: "airag_model where model_type = 'VIDEO' and activate_flag = 1,name,id",
      placeholder: 'Vui lòng chọn mô hình video',
    },
  },
  {
    field: 'modelImage',
    label: 'Hình ảnh người mẫu',
    component: 'JImageUpload',
    componentProps: {
      fileMax: 1,
      text: 'Tải lên người mẫu',
    },
    helpMessage: ['Tải lên ảnh người mẫu, khuyên dùng ảnh toàn thân, mặt trước rõ nét'],
  },
  {
    field: 'clothUpload',
    label: '',
    slot: 'clothUpload',
    component: 'Input',
    required: false,
  },
  {
    field: 'userPrompt',
    label: 'Prompt tùy chỉnh',
    component: 'InputTextArea',
    componentProps: {
      rows: 4,
      placeholder: 'Nhập prompt của bạn tại đây, hoặc dùng ví dụ phía dưới để điền nhanh',
    },
    required: false,
  },
];
