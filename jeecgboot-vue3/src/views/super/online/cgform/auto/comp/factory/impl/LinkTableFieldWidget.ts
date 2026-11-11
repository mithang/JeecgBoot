import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * trường bảng khác
 */
export default class LinkTableFieldWidget extends IFormSchema {

  dictTable: string;
  dictText: string;

  constructor(key, data) {
    super(key, data);
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
  }
  
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      componentProps: {
        readOnly: true,
        allowClear: false,
        disabled: true,
        style:{
          background: 'none',
          color:'rgba(0, 0, 0, 0.85)',
          border:'none'
        }
      }
    });
    return item;
  }

  /**
   * Lấy thông tin liên kết của trường bảng khác
   */
  getLinkFieldInfo(){
    let arr = [this.dictTable, `${this.field},${this.dictText}`];
    return arr;
  }
}
