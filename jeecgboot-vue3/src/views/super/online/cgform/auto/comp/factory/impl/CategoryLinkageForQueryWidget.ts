import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/** Truy vấn nâng cao của component liên động trong trình thiết kế biểu mẫu. */
export default class CategoryLinkageForQueryWidget extends IFormSchema {
  getItem(): FormSchema {
    const item = super.getItem();
    return Object.assign({}, item, {
      component: 'JCategoryLinkage',
      componentProps: {
        category: this._data.category,
        placeholder: 'Vui lòng chọn…',
        style: { width: '100%' },
      },
    });
  }
}
