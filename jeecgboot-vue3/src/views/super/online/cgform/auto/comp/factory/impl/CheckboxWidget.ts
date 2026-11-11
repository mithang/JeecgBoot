import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * checkbox
 */
export default class CheckboxWidget extends IFormSchema {
  /*title-value*/
  options: any[];
  constructor(key, data) {
    super(key, data);
    this.options = this.getOptions(data['enum']);
  }

  setFormRef(ref) {
    super.setFormRef(ref);
    this.handleDictTableParams();
  }

  updateDictTable(dictTable: string) {
    this.formRef.value.updateSchema(({
      field: this.field,
      componentProps: {
        options:[],
        dictCode: this.genDictTableCode(dictTable, this._data.dictText, this._data.dictCode),
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'JCheckbox',
      componentProps: {
        options: this.options,
        triggerChange: true,
        // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
        useDicColor: true,
        // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
      },
    });
  }

  getOptions(array) {
    if (!array || array.length == 0) {
      return [];
    }
    let arr: any[] = [];
    for (let item of array) {
      arr.push({
        value: item.value,
        label: item.title,
        // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
        color: item.color,
        // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
      });
    }
    return arr;
  }
}
