import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Ô nhập-Chữ số
 */
export default class NumberWidget extends IFormSchema {
  dbPointLength: number;

  constructor(key, data) {
    super(key, data);
    this.dbPointLength = data.dbPointLength;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    const safeIntRule = {
      validator: (_rule, value) => {
        if (value !== null && value !== undefined && value !== '') {
          if (value > Number.MAX_SAFE_INTEGER || value < Number.MIN_SAFE_INTEGER) {
            return Promise.reject(`Giá trị số vượt quá phạm vi an toàn (${Number.MIN_SAFE_INTEGER}~${Number.MAX_SAFE_INTEGER}) , độ chính xác sẽ bị mất, vui lòng nhập lại`);
          }
        }
        return Promise.resolve();
      },
    };
    const existingRules = item.rules || [];
    return Object.assign({}, item, {
      component: 'InputNumber',
      componentProps,
      // update-begin--author:liaozhiyang---date:20260413---for:【QQYUN-9790】onlineloại số trong đó vượt quájsPhạm vi giá trị ngôn ngữ thêm nhắc nhở
      rules: [...existingRules, safeIntRule],
      // update-end--author:liaozhiyang---date:20260413---for:【QQYUN-9790】onlineloại số trong đó vượt quájsPhạm vi giá trị ngôn ngữ thêm nhắc nhở
    });
  }

  getComponentProps() {
    const props = {
      style: {
        width: '100%',
      },
    };
    if (this.dbPointLength >= 0) {
      props['precision'] = this.dbPointLength;
    }
    if (this.onlyInteger) {
      props['precision'] = 0;
    }
    return props;
  }
}
