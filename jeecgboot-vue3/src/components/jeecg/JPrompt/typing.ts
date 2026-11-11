import { ModalOptionsPartial } from '/@/hooks/web/useMessage';
import { RenderCallbackParams, Rule } from '/@/components/Form';

export interface JPromptProps extends ModalOptionsPartial {
  // Ô nhập có bắt buộc hay không
  required?: boolean;
  // Kiểm chứng
  rules?: Rule[];
  // Kiểm chứng động
  dynamicRules?: (renderCallbackParams: RenderCallbackParams) => Rule[];
  // Ký tự giữ chỗ
  placeholder?: string;
  // Giá trị mặc định của ô nhập
  defaultValue?: string;
}
