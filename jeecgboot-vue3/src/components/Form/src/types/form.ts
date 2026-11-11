import type { NamePath, RuleObject, ValidateOptions } from 'ant-design-vue/lib/form/interface';
import type { VNode, ComputedRef } from 'vue';
import type { ButtonProps as AntdButtonProps } from '/@/components/Button';
import type { FormItem } from './formItem';
import type { ColEx, ComponentType } from './index';
import type { TableActionType } from '/@/components/Table/src/types/table';
import type { CSSProperties } from 'vue';
import type { RowProps } from 'ant-design-vue/lib/grid/Row';

export type FieldMapToTime = [string, [string, string], string?][];
export type FieldMapToNumber = [string, [string, string]][];

export type Rule = RuleObject & {
  trigger?: 'blur' | 'change' | ['change', 'blur'];
};

export interface RenderCallbackParams {
  schema: FormSchema;
  values: Recordable;
  model: Recordable;
  field: string;
}

export interface ButtonProps extends AntdButtonProps {
  text?: string;
}

export interface FormActionType {
  submit: () => Promise<void>;
  setFieldsValue: <T>(values: T) => Promise<void>;
  resetFields: () => Promise<void>;
  getFieldsValue: () => Recordable;
  clearValidate: (name?: string | string[]) => Promise<void>;
  updateSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  resetSchema: (data: Partial<FormSchema> | Partial<FormSchema>[]) => Promise<void>;
  setProps: (formProps: Partial<FormProps>) => Promise<void>;
  getProps: ComputedRef<Partial<FormProps>>;
  getSchemaByField: (field: string) => Nullable<FormSchema>;
  removeSchemaByFiled: (field: string | string[]) => Promise<void>;
  appendSchemaByField: (schema: FormSchema, prefixField: string | undefined, first?: boolean | undefined) => Promise<void>;
  validateFields: (nameList?: NamePath[], options?: ValidateOptions) => Promise<any>;
  validate: (nameList?: NamePath[]) => Promise<any>;
  scrollToField: (name: NamePath, options?: ScrollOptions) => Promise<void>;
  getSchemaComponentProps: (schema: FormSchema) => Recordable
}

export type RegisterFn = (formInstance: FormActionType) => void;

export type UseFormReturnType = [RegisterFn, FormActionType];

export interface FormProps {
  layout?: 'vertical' | 'inline' | 'horizontal';
  // Form value
  model?: Recordable;
  // The width of all items in the entire form
  labelWidth?: number | string;
  //alignment
  labelAlign?: 'left' | 'right';
  //Row configuration for the entire form
  rowProps?: RowProps;
  // Submit form on reset
  submitOnReset?: boolean;
  // Col configuration for the entire form
  labelCol?: Partial<ColEx> | null;
  // Col configuration for the entire form
  wrapperCol?: Partial<ColEx> | null;

  // General row style
  baseRowStyle?: CSSProperties;

  // General col configuration
  baseColProps?: Partial<ColEx>;

  // Form configuration rules
  schemas?: FormSchema[];
  // Function values used to merge into dynamic control form items
  mergeDynamicData?: Recordable;
  // Compact mode for search forms
  compact?: boolean;
  // Blank line span
  emptySpan?: number | Partial<ColEx>;
  // Internal component size of the form
  size?: 'default' | 'small' | 'large';
  // Whether to disable
  disabled?: boolean;
  // Time interval fields are mapped into multiple
  fieldMapToTime?: FieldMapToTime;
  // number interval fields are mapped into multiple
  fieldMapToNumber?: FieldMapToNumber;
  // Placeholder is set automatically
  autoSetPlaceHolder?: boolean;
  // Auto submit on press enter on input
  autoSubmitOnEnter?: boolean;
  // Check whether the information is added to the label
  rulesMessageJoinLabel?: boolean;
  // Có hiển thị nút mở rộng/thu gọn không
  showAdvancedButton?: boolean;
  // Whether to focus on the first input box, only works when the first form item is input
  autoFocusFirstItem?: boolean;
  // Nếu showAdvancedButton là true, khi vượt quá số cột quy định sẽ mặc định thu gọn, mặc định là3
  autoAdvancedCol?: number;
  // Nếu showAdvancedButton là true, vượt quá số hàng chỉ định thì mặc định thu gọn
  // Giải thích logic mã: 【issues/7261]Cấu hình autoAdvancedLine của mục truy vấn phía trên bảng không có hiệu lực (xóa autoAdvancedLine)
  // autoAdvancedLine?: number;
  // Số dòng luôn hiển thị khi thu gọn
  alwaysShowLines?: number;
  // Whether to show the operation button
  showActionButtonGroup?: boolean;

  // Reset button configuration
  resetButtonOptions?: Partial<ButtonProps>;

  // Confirm button configuration
  submitButtonOptions?: Partial<ButtonProps>;

  // Operation column configuration
  actionColOptions?: Partial<ColEx>;

  // Show reset button
  showResetButton?: boolean;
  // Show confirmation button
  showSubmitButton?: boolean;

  resetFunc?: () => Promise<void>;
  submitFunc?: () => Promise<void>;
  transformDateFunc?: (date: any) => string;
  colon?: boolean;
}
export interface FormSchema {
  // Field name
  field: string;
  // Event name triggered by internal value change, default change
  changeEvent?: string;
  // Variable name bound to v-model Default value
  valueField?: string;
  // Label name
  // Giải thích logic mã: 【issues/6908]Khi chuyển đổi đa ngôn ngữ không cần làm mới, giá trị trong BasicColumn và FormSchema không thể chuyển đổi bình thường
  label: string | VNode | Fn;
  // Auxiliary text
  subLabel?: string;
  // Help text on the right side of the text
  helpMessage?: string | string[] | ((renderCallbackParams: RenderCallbackParams) => string | string[]);
  // BaseHelp component props
  helpComponentProps?: Partial<HelpComponentProps>;
  // Label width, if it is passed, the labelCol and WrapperCol configured by itemProps will be invalid
  labelWidth?: string | number;
  // Disable the adjustment of labelWidth with global settings of formModel, and manually set labelCol and wrapperCol by yourself
  disabledLabelWidth?: boolean;
  // render component
  component: ComponentType;
  // Component parameters
  componentProps?:
    | ((opt: { schema: FormSchema; tableAction: TableActionType; formActionType: FormActionType; formModel: Recordable }) => Recordable)
    | object;
  // Required
  required?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  suffix?: string | number | VueNode | ((values: RenderCallbackParams) => string | number | VueNode);
  // 【QQYUN-12876]Có phải suffix dạng gọn hay không (khi chiều rộng component chưa chiếm hết, có thể áp sát phía phải của component)
  suffixCompact?: boolean;

  // Validation rules
  rules?: Rule[];
  // Check whether the information is added to the label
  rulesMessageJoinLabel?: boolean;

  // Reference formModelItem
  itemProps?: Partial<FormItem> | ((renderCallbackParams: RenderCallbackParams) => Partial<FormItem>);

  // col configuration outside formModelItem
  colProps?: Partial<ColEx>;

  // Giá trị mặc định
  defaultValue?: any;
  isAdvanced?: boolean;

  // Matching details components
  span?: number;

  ifShow?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  show?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  // Render the content in the form-item tag
  render?: (renderCallbackParams: RenderCallbackParams) => VNode | VNode[] | string;

  // Rendering col content requires outer wrapper form-item
  renderColContent?: (renderCallbackParams: RenderCallbackParams) => VNode | VNode[] | string;

  renderComponentContent?: ((renderCallbackParams: RenderCallbackParams) => any) | VNode | VNode[] | string;

  // Custom slot, in from-item
  slot?: string;

  // Custom slot, similar to renderColContent
  colSlot?: string;

  dynamicDisabled?: boolean | ((renderCallbackParams: RenderCallbackParams) => boolean);

  dynamicRules?: (renderCallbackParams: RenderCallbackParams) => Rule[];
  // Đặt key của props componentkey
  dynamicPropskey?: string;
  dynamicPropsVal?: ((renderCallbackParams: RenderCallbackParams) => any);

  // Thuộc tính tùy chỉnh này dùng cho nghiệp vụ tùy chỉnh, ví dụ khi biểu mẫu mở ra thì sửa trạng thái vô hiệu hóa của biểu mẫu, nhưng không thể viết lại componentProps vì nội dung của nó quá nhiều, nên dùng dynamicDisabled và buss để thực hiện
  buss?: any;
  
  //labelKiểm soát số ký tự (chiều rộng label)
  labelLength?: number;
  // update-begin--author:liaozhiyang---date:20240529---for【TV360X-460] basicForm hỗ trợv-authChỉ thị(Kiểm soát quyền hiện/ẩn)
  auth?: string;
  // update-end--author:liaozhiyang---date:20240529---for【TV360X-460] basicForm hỗ trợv-authChỉ thị(Kiểm soát quyền hiện/ẩn)
}
export interface HelpComponentProps {
  maxWidth: string;
  // Whether to display the serial number
  showIndex: boolean;
  // Text list
  text: any;
  // colour
  color: string;
  // Tooltip background colour
  backgroundColor?: string;
  // font size
  fontSize: string;
  icon: string;
  absolute: boolean;
  // Positioning
  position: any;
}
