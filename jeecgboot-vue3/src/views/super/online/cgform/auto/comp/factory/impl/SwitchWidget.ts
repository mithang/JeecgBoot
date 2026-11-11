import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';
import { isArray, isObject } from '/@/utils/is';

/**
 * Công tắc
 */
export default class SwitchWidget extends IFormSchema {
  constructor(key, data) {
    super(key, data);
    // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-54】Công tắc chỉ đọc (readonly) không có hiệu lực
    // this.hasChange = false;
    // update-end--author:liaozhiyang---date:20240517---for：【TV360X-54】Công tắc chỉ đọc (readonly) không có hiệu lực
  }
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSwitch',
      componentProps,
    });
  }

  getComponentProps() {
    let { fieldExtendJson } = this._data;
    let options = ['Y', 'N'];
    if (fieldExtendJson) {
      if (typeof fieldExtendJson == 'string') {
        // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
        const json = JSON.parse(fieldExtendJson);
        if (isArray(json) && json.length == 2) {
          options = json;
        } else if (isObject(json) && isArray(json.switchOptions)) {
          options = json.switchOptions;
        }
        // update-end--author:liaozhiyang---date:20240522---for：【TV360X-25】Bổ sung cấu hình tùy chọn công tắc có/không trong cấu hình tham số mở rộng
      }
    }
    return {
      options,
    };
  }
}
