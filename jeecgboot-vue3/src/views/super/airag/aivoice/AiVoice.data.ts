import { FormSchema } from '@/components/Form';

// Biểu mẫu điều khiển giọng nói bên trái
export const voiceFormSchemas: FormSchema[] = [
  /*  {
    label: 'Mô hình',
    field: 'model',
    component: 'JDictSelectTag',
    required: true,
    defaultValue: 'voice-generation-1',
    componentProps: {
      placeholder: 'Vui lòng chọn mô hình giọng nói',
      dictCode: "airag_model where model_type = 'VOICE' and activate_flag = 1,name,id",
    },
  },*/
  {
    label: 'Tốc độ phát',
    field: 'speed',
    component: 'Slider',
    defaultValue: 1,
    colProps: {
      span: 24,
    },
    componentProps: {
      min: 0.25,
      max: 4,
      step: 0.1,
      marks: {
        0.5: '0.5x',
        1: '1x',
        1.5: '1.5x',
        2: '2x',
        3: '3x',
        4: '4x',
      },
      tooltip: {
        formatter: (value: number) => `${value.toFixed(1)}x`,
      },
    },
  },
  {
    label: 'Tăng âm lượng(dB)',
    field: 'volume',
    component: 'Slider',
    defaultValue: 0,
    colProps: {
      span: 24,
    },
    componentProps: {
      min: -10,
      max: 10,
      step: 1,
      marks: {
        '-10': '-10',
        0: '0',
        10: '10',
      },
    },
  },
  {
    label: 'Âm sắc',
    field: 'voice',
    component: 'Select',
    required: true,
    defaultValue: 'tongtong',
    componentProps: {
      options: [
        { label: 'Đồng Đồng', value: 'tongtong' },
        { label: 'Chùy Chùy', value: 'chuichui' },
        { label: 'Tiểu Trần', value: 'xiaochen' },
        { label: 'Jam', value: 'jam' },
        { label: 'Kazi', value: 'kazi' },
        { label: 'Douji', value: 'douji' },
        { label: 'Luodo', value: 'luodo' },
      ],
      placeholder: 'Vui lòng chọn âm sắc',
    },
  },
  {
    label: 'Nội dung văn bản',
    field: 'text',
    component: 'InputTextArea',
    required: true,
    colProps: {
      span: 24,
    },
    componentProps: {
      rows: 6,
      placeholder: 'Vui lòng nhập nội dung văn bản muốn tổng hợp',
      maxlength: 500,
      showCount: true,
    },
  },
];

/**
 * Cấu hình cột bảng lịch sử
 */
export const historyColumns = [
  {
    title: 'Nội dung văn bản',
    dataIndex: 'text',
    width: 100,
    ellipsis: true,
  },
  {
    title: 'Thời gian tạo',
    dataIndex: 'createTime',
    width: 100,
  },
  {
    title: 'Thao tác',
    dataIndex: 'action',
    width: 100,
    fixed: 'right',
  },
];
