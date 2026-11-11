import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Ngày, thời gian
 */
export default class TimeWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'TimePicker',
      componentProps: {
        placeholder: `Vui lòng chọn${this.label}`,
        valueFormat: 'HH:mm:ss',
        getPopupContainer: (_node) => {
          return this.getModalAsContainer();
        },
        style: {
          width: '100%',
        },
      },
    });
  }
}
