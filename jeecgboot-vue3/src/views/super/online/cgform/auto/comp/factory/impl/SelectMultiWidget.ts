import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Ô dropdown chọn nhiều
 */
export default class SelectMultiWidget extends IFormSchema {
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
        dictCode: this.genDictTableCode(dictTable, this.dictText, this.dictCode)
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'JSelectMultiple',
      componentProps: componentProps,
    });
  }

  getComponentProps() {
    if (!this.dictTable && !this.dictCode) {
      // Bảng từ điển và từ điển Đều chưa điền dữ liệu
      return {};
    } else {
      let props = {};
      if (!this.dictTable) {
        props['dictCode'] = this.dictCode;
        // update-begin--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
        props['useDicColor'] = true;
        // update-end--author:liaozhiyang---date:20230110---for：【QQYUN-7799】Component từ điển (trừ component gốc) thêm cấu hình màu sắc
      } else {
        props['dictCode'] = this.genDictTableCode(this.dictTable, this.dictText, this.dictCode);
        // update-begin--author:liaozhiyang---date:20260204---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
        // Mặc định tải cuộn dữ liệu bảng từ điển
        props['scrollLoad'] = true;
        // update-end--author:liaozhiyang---date:20260204---for:【issues/9307】onlineDropdown khi tải từ điển bảng cần tải theo cuộn
      }
      props['triggerChange'] = true;
      props['popContainer'] = this.getPopContainer();
      return props;
    }
  }
}
