import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Lựa chọn của người dùng
 */
export default class SelectUser2Widget extends IFormSchema {

  multi: boolean;
  store: string;
  query: boolean;

  constructor(key, data) {
    super(key, data);
    this.multi = data.multi === true ? true : false;
    this.store = data.store||'';
    // Có phải điều kiện truy vấn hay không, điều kiện truy vấn hiển thị theo kiểu ô nhập
    this.query = data.query||false;
  }
  
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'UserSelect',
      componentProps
    });
  }

  getComponentProps() {
    let props = {
      multi: this.multi,
      store: this.store,
      query: this.query,
    }
    // Giải quyết truy vấn nâng cao của trình thiết kế biểu mẫu popupPopup của component dẫn đến truy vấn nâng caopopĐóng
    if(this.inPopover === true){
      props['getContainer'] = ()=>{
        return this.getModalAsContainer();
      }
    }
    return props;
  }
}
