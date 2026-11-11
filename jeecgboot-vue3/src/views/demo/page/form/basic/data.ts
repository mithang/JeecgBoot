import { FormSchema } from '/@/components/Form';

export const schemas: FormSchema[] = [
  {
    field: 'title',
    component: 'Input',
    label: 'Tiêu đề',
    componentProps: {
      placeholder: 'Đặt tên cho mục tiêu',
    },
    required: true,
  },
  {
    field: 'time',
    component: 'RangePicker',
    label: 'Ngày bắt đầu - kết thúc',
    required: true,
  },
  {
    field: 'target',
    component: 'InputTextArea',
    label: 'Mô tả mục tiêu',
    componentProps: {
      placeholder: 'Vui lòng nhập mục tiêu công việc theo giai đoạn của bạn',
      rows: 4,
    },
    required: true,
  },
  {
    field: 'metrics',
    component: 'InputTextArea',
    label: 'Tiêu chuẩn đánh giá',
    componentProps: {
      placeholder: 'Vui lòng nhập tiêu chuẩn đo lường',
      rows: 4,
    },
    required: true,
  },
  {
    field: 'client',
    component: 'Input',
    label: 'Khách hàng',
    helpMessage: 'Đối tượng phục vụ mục tiêu',
    subLabel: '( Không bắt buộc )',
    componentProps: {
      placeholder: 'Vui lòng mô tả khách hàng bạn phục vụ, khách hàng nội bộ trực tiếp @Họ tên／Mã nhân viên',
    },
  },
  {
    field: 'inviteer',
    component: 'Input',
    label: 'Người mời đánh giá',
    subLabel: '( Không bắt buộc )',
    componentProps: {
      placeholder: 'Vui lòng trực tiếp @Họ tên／Mã nhân viên, tối đa mời được 5 người',
    },
  },
  {
    field: 'weights',
    component: 'InputNumber',
    label: 'Trọng số',
    subLabel: '( Không bắt buộc )',
    componentProps: {
      formatter: (value: string) => (value ? `${value}%` : ''),
      parser: (value: string) => value.replace('%', ''),
      placeholder: 'Vui lòng nhập',
    },
  },
  {
    field: 'disclosure',
    component: 'RadioGroup',
    label: 'Mục tiêu công khai',
    itemProps: {
      extra: 'Khách hàng, người được mời đánh giá được chia sẻ theo mặc định',
    },
    componentProps: {
      options: [
        {
          label: 'Công khai',
          value: '1',
        },
        {
          label: 'Công khai một phần',
          value: '2',
        },
        {
          label: 'Không công khai',
          value: '3',
        },
      ],
    },
  },
  {
    field: 'disclosurer',
    component: 'Select',
    label: ' ',
    show: ({ model }) => {
      return model.disclosure === '2';
    },
    componentProps: {
      placeholder: 'Công khai cho',
      mode: 'multiple',
      options: [
        {
          label: 'Đồng nghiệp1',
          value: '1',
        },
        {
          label: 'Đồng nghiệp2',
          value: '2',
        },
        {
          label: 'Đồng nghiệp3',
          value: '3',
        },
      ],
    },
  },
];
