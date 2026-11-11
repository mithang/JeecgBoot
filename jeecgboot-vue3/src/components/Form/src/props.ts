import type { FieldMapToTime, FormSchema } from './types/form';
import type { CSSProperties, PropType } from 'vue';
import type { ColEx } from './types';
import type { TableActionType } from '/@/components/Table';
import type { ButtonProps } from 'ant-design-vue/es/button/buttonTypes';
import type { RowProps } from 'ant-design-vue/lib/grid/Row';
import dayjs from "dayjs";
import { propTypes } from '/@/utils/propTypes';
import componentSetting from '/@/settings/componentSetting';

const { form } = componentSetting;
export const basicProps = {
  model: {
    type: Object as PropType<Recordable>,
    default: {},
  },
  // Chiều rộng của nhãn, chiều rộng cố định
  labelWidth: {
    type: [Number, String] as PropType<number | string>,
    default: 0,
  },
  fieldMapToTime: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  fieldMapToNumber: {
    type: Array as PropType<FieldMapToTime>,
    default: () => [],
  },
  compact: propTypes.bool,
  // Quy tắc cấu hình biểu mẫu
  schemas: {
    type: [Array] as PropType<FormSchema[]>,
    default: () => [],
  },
  mergeDynamicData: {
    type: Object as PropType<Recordable>,
    default: null,
  },
  baseRowStyle: {
    type: Object as PropType<CSSProperties>,
  },
  baseColProps: {
    type: Object as PropType<Partial<ColEx>>,
  },
  autoSetPlaceHolder: propTypes.bool.def(true),
  // Khi nhấn Enter trên component INPUT, có tự động gửi hay không
  autoSubmitOnEnter: propTypes.bool.def(false),
  submitOnReset: propTypes.bool,
  size: propTypes.oneOf(['default', 'small', 'large']).def('default'),
  // Vô hiệu hóa biểu mẫu
  disabled: propTypes.bool,
  emptySpan: {
    type: [Number, Object] as PropType<number>,
    default: 0,
  },
  // Có hiển thị nút thu gọn/mở rộng không
  showAdvancedButton: propTypes.bool,
  // Thời gian chuyển đổi
  transformDateFunc: {
    type: Function as PropType<Fn>,
    default: (date: any) => {
      // Kiểm tra có phải instance của dayjs hay không
      return dayjs.isDayjs(date) ? date?.format('YYYY-MM-DD HH:mm:ss') : date;
    },
  },
  rulesMessageJoinLabel: propTypes.bool.def(true),
  // [jeecg] Vượt quá 3 cột tự động gấp lại
  autoAdvancedCol: propTypes.number.def(3),
  // Quá 3 dòng sẽ tự động thu gọn
  // autoAdvancedLine: propTypes.number.def(3),
  // Số dòng không bị ảnh hưởng bởi việc gấp lại
  alwaysShowLines: propTypes.number.def(1),

  // Có hiển thị nút thao tác không
  showActionButtonGroup: propTypes.bool.def(true),
  // Cấu hình Col của cột thao tác
  actionColOptions: Object as PropType<Partial<ColEx>>,
  // Hiển thị nút đặt lại
  showResetButton: propTypes.bool.def(true),
  // Có focus vào ô nhập đầu tiên hay không, chỉ có tác dụng khi mục biểu mẫu đầu tiên là input
  autoFocusFirstItem: propTypes.bool,
  // Cấu hình nút đặt lại
  resetButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // Hiển thị nút xác nhận
  showSubmitButton: propTypes.bool.def(true),
  // Cấu hình nút xác nhận
  submitButtonOptions: Object as PropType<Partial<ButtonProps>>,

  // Hàm đặt lại tùy chỉnh
  resetFunc: Function as PropType<() => Promise<void>>,
  submitFunc: Function as PropType<() => Promise<void>>,

  // Sau đây là mặc địnhprops
  hideRequiredMark: propTypes.bool,

  labelCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.labelCol,
  },

  layout: propTypes.oneOf(['horizontal', 'vertical', 'inline']).def('horizontal'),
  tableAction: {
    type: Object as PropType<TableActionType>,
  },

  wrapperCol: {
    type: Object as PropType<Partial<ColEx>>,
    default: form.wrapperCol,
  },

  colon: propTypes.bool.def(form.colon),

  labelAlign: propTypes.string,

  rowProps: Object as PropType<RowProps>,
  
  // Khi biểu mẫu là điều kiện tìm kiếm, khi biểu mẫu thay đổi sẽ tự động tìm kiếm, không cần nhấp nút tìm kiếm
  autoSearch: propTypes.bool.def(false),
};
