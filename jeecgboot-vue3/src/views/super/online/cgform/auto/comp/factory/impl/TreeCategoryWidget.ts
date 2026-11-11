import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Từ điển phân loại
 */
export default class TreeCategoryWidget extends IFormSchema {
  pid: string;
  multi: boolean;
  textField: string;
  pcode: string;

  constructor(key, data) {
    super(key, data);
    this.multi = false;
    this.pid = data['pidValue'];
    this.pcode = data['pcode'];
    this.textField = data['textField'];
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      componentProps,
      component: 'JCategorySelect',
    });
  }

  /**
   * 1. không có giá trị trả về
   * 2. Có trả về văn bản
   */
  getComponentProps() {
    // VUEN-1049 Sau khi lưu từ điển phân loại, danh sách không hiển thị bảng đơn Bảng cây --> Sau khi cấu hình sai mã, giao diện biểu mẫu vẫn hiển thị tùy chọn từ điển phân loại, có thể trực tiếp không hiển thị tùy chọn từ điển
    let param = {
      placeholder: 'Vui lòng chọn' + this.label
    }
    if(this.pcode){
      param['pcode'] = this.pcode;
    }else{
      let pidValue = this.pid || 'EMPTY_PID';
      param['pid'] = pidValue;
    }
    if (!this.textField) {
      return {
        multiple: this.multi,
        ...param
      };
    } else {
      return {
        loadTriggleChange: true,
        multiple: this.multi,
        ...param,
        back: this.textField,
        onChange: (val, backVal) => {
          if (this.formRef) {
            this.formRef.value.setFieldsValue(backVal);
            this.formRef.value.$formValueChange(this.field, val)
          }
        },
      };
    }
  }

  getRelatedHideFields(): string[] {
    let arr: string[] = [];
    if (this.textField) {
      arr.push(this.textField);
    }
    return arr;
  }
}
