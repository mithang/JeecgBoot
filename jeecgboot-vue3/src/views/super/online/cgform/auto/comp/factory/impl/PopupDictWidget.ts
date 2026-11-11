import {unref} from 'vue'
import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * popupDict
 */
export default class PopupDictWidget extends IFormSchema {
  dictCode: string;
  multi: boolean;
  constructor(key, data) {
    super(key, data);
    this.dictCode = `${data['code']},${data['destFields']},${data['orgFields']}`;
    this.multi = data['popupMulti'];
  }

  getItem(): FormSchema {
    const item = super.getItem();
    const componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JPopupDict',
      componentProps,
    });
  }

  getComponentProps() {
    const props = {
      dictCode: this.dictCode,
      multi: this.multi,
    };
    // Giải quyết truy vấn nâng cao của trình thiết kế biểu mẫu popupPopup của component dẫn đến truy vấn nâng caopopĐóng
    if (this.inPopover) {
      props['getContainer'] = () => {
        return this.getModalAsContainer();
      };
    }

    // Lấy dữ liệu biểu mẫu
    props['getFormValues'] = () => unref(this.formRef).getFieldsValue();

    return props;
  }
}
