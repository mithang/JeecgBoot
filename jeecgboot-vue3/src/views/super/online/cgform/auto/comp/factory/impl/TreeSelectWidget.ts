import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Cây tùy chỉnh
 */
export default class TreeSelectWidget extends IFormSchema {
  /*Tên bảng, trường hiển thị, trường lưu trữ*/
  dict: string;
  /*Cấp chaIDtên trường của*/
  pidField: string;
  /*Cấp chaIDgiá trị trường của*/
  pidValue: string;
  /*Có nút con hay không*/
  hasChildField: string;
  /*Có chuyển đổi (đảo ngược) giá trị có nút con hay không*/
  converIsLeafVal: number;

  constructor(key, data) {
    super(key, data);
    this.dict = data['dict'];
    this.pidField = data['pidField'];
    this.pidValue = data['pidValue'];
    // update-begin--author:liaozhiyang---date:20240509---for：【issues/6197】Giải quyết chức năng kiểm tra component cây tùy chỉnh có chứa nút con hay không không có hiệu lực
    this.hasChildField = data['hasChildField'];
    // update-end--author:liaozhiyang---date:20240509---for：【issues/6197】Giải quyết chức năng kiểm tra component cây tùy chỉnh có chứa nút con hay không không có hiệu lực
    this.converIsLeafVal = data['converIsLeafVal'];
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JTreeSelect',
      componentProps: {
        dict: this.dict,
        pidField: this.pidField,
        pidValue: this.pidValue,
        // update-begin--author:liaozhiyang---date:20240509---for：【issues/6197】Giải quyết chức năng kiểm tra component cây tùy chỉnh có chứa nút con hay không không có hiệu lực
        hasChildField: this.hasChildField,
        // update-end--author:liaozhiyang---date:20240509---for：【issues/6197】Giải quyết chức năng kiểm tra component cây tùy chỉnh có chứa nút con hay không không có hiệu lực
        converIsLeafVal: this.converIsLeafVal,
      },
    });
  }
}
