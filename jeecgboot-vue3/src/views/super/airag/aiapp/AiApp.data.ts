import { FormSchema } from '@/components/Form';

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
    label: 'Tên ứng dụng',
    field: 'name',
    required: true,
    componentProps: {
      //Có trưng bày số chữ hay không
      showCount: true,
      maxlength: 64,
    },
    component: 'Input',
  },
  {
    label: 'Mô tả ứng dụng',
    field: 'descr',
    component: 'InputTextArea',
    componentProps: {
      placeholder: 'Mô tả kịch bản ứng dụng và mục đích sử dụng của ứng dụng này',
      rows: 4,
      //Có trưng bày số chữ hay không
      showCount: true,
      maxlength: 256,
    },
  },
  {
    label: 'Biểu tượng ứng dụng',
    field: 'icon',
    component: 'JImageUpload',
  },
  {
    label: 'Chọn loại ứng dụng',
    field: 'type',
    component: 'Input',
    show:({ values })=>{
      return !values.id;
    },
    slot: 'typeSlot',
  },
];

/**
 * Biểu mẫu lệnh tắt
 */
export const quickCommandFormSchema: FormSchema[] = [
  {
    label: 'key',
    field: 'key',
    component: 'Input',
    show: false,
  },
  {
    label: 'Tên nút',
    field: 'name',
    required: true,
    component: 'Input',
    componentProps: {
      showCount: true,
      maxLength: 10,
    },
  },
  {
    label: 'Biểu tượng nút',
    field: 'icon',
    component: 'IconPicker',
  },
  {
    label: 'Nội dung lệnh',
    field: 'descr',
    required: true,
    component: 'InputTextArea',
    componentProps: {
      autosize: { minRows: 4, maxRows: 4 },
      showCount: true,
      maxLength: 100,
    }
  },
];
