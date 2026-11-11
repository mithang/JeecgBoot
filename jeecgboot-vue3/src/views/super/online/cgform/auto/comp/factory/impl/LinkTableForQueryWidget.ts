import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Trình thiết kế biểu mẫu-Truy vấn bản ghi liên kết Sử dụng tìm kiếm dropdown
 */
export default class LinkTableForQueryWidget extends IFormSchema {

  code: string;
  titleField: string;
  multi: boolean;
  
  constructor(key, data) {
    super(key, data);
    this.code = data['code'];
    this.titleField = data['titleField'];
    this.multi = data['multi']||false;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'LinkTableForQuery',
      componentProps:{
        code: this.code,
        multi: this.multi,
        field: this.titleField,
        style: {
          width: '100%',
        }
      }
    });
  }

}
