import IFormSchema from '../IFormSchema';
import { FormSchema } from '/@/components/Form';

/**
 * Tỉnh/Thành/Quận huyện
 */
export default class PcaWidget extends IFormSchema {
  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JAreaSelect',
    });
  }
}
