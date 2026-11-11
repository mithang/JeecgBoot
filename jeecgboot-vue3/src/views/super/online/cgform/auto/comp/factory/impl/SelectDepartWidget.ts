import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Chọn phòng ban
 */
export default class SelectDepartWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSelectDept',
      componentProps,
    });
  }

  getComponentProps() {
    let extend = this.getExtendData();
    let props = {
      // update-begin--author:liaozhiyang---date:20260414---for:【QQYUN-9801】Khắc phụconlineNhấp mở rộng tất cả, nút cây không mở rộng hết
      sync: false,
      // update-end--author:liaozhiyang---date:20260414---for:【QQYUN-9801】Khắc phụconlineNhấp mở rộng tất cả, nút cây không mở rộng hết
      checkStrictly: true,
      showButton: false,
    };
    if (extend.text) {
      props['labelKey'] = extend.text;
    }
    if (extend.store) {
      props['rowKey'] = extend.store;
    }
    if (extend.multiSelect === false) {
      props['multiple'] = false;
    }
    
    if (extend.multiSelect === true) {
      props['multiple'] = true;
    }
    props['maxTagCount'] = 3;

     // Giải quyết truy vấn nâng cao của trình thiết kế biểu mẫu popupPopup của component dẫn đến truy vấn nâng caopopĐóng
    if(this.inPopover === true){
      props['getContainer'] = ()=>{
        return this.getModalAsContainer();
      }
    }
    return props;
  }
}
