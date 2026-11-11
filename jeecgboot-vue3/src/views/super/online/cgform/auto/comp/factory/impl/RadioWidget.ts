import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * radio
 * Không có sẵn Chỉ có thể mượn dùngJDictSelectTag
 */
export default class RadioWidget extends IFormSchema {
  dictTable: string;
  dictText: string;
  dictCode: string;

  constructor(key, data) {
    super(key, data);
    // Có thể lấy từ trong này nhưng đổi thành cái được tải tạm thời
    //this.options = this.getOptions(data['enum'])
    this.dictTable = data['dictTable'];
    this.dictText = data['dictText'];
    this.dictCode = data['dictCode'];
  }

  setFormRef(ref) {
    super.setFormRef(ref);
    this.handleDictTableParams();
  }

  updateDictTable(dictTable: string) {
    this.formRef.value.updateSchema(({
      field: this.field,
      componentProps: {
        dictCode: this.genDictTableCode(dictTable, this.dictText, this.dictCode),
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JDictSelectTag',
      componentProps,
    });
  }

  getComponentProps() {
    if (!this.dictTable && !this.dictCode) {
      // Bảng từ điển và từ điển Đều chưa điền dữ liệu
      return {};
    } else {
      if (!this.dictTable) {
        return {
          // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
          useDicColor: true,
          // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
          dictCode: this.dictCode,
          type: 'radio',
        };
      } else {
        return {
          dictCode: this.genDictTableCode(this.dictTable, this.dictText, this.dictCode),
          type: 'radio',
        };
      }
    }
  }
}
