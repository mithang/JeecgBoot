import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Tìm kiếm trong dropdown
 */
export default class SelectSearchWidget extends IFormSchema {
  dict: string;
  type: number;
  constructor(key, data) {
    super(key, data);
    if (data.dictTable && data.dictText && data.dictCode) {
      // Bảng từ điển
      this.dict = this.genDictTableCode(data.dictTable, data.dictText, data.dictCode);
      this.type = 1;
    } else {
      // Từ điển dữ liệu
      this.dict = encodeURI(`${data.dictCode}`);
      this.type = 0;
    }
  }

  setFormRef(ref) {
    super.setFormRef(ref);
    this.handleDictTableParams();
  }

  updateDictTable(dictTable: string) {
    this.formRef.value.updateSchema(({
      field: this.field,
      componentProps: {
        dict: this.genDictTableCode(dictTable, this._data.dictText, this._data.dictCode),
      }
    }))
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let popContainer = this.getPopContainer();
    // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
    const extendData = this.getExtendData();
    // update-end--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
    return Object.assign({}, item, {
      component: 'JSearchSelect',
      componentProps: {
        dict: this.dict,
        pageSize: 10,
        // update-begin--author:liaozhiyang---date:20240628---for：【issues/6336】onlineÔ tìm kiếm dropdown: thiết lập từ điển dữ liệu, popup chỉnh sửa báo lỗi
        async: this.type ? true : false,
        // update-end--author:liaozhiyang---date:20240628---for：【issues/6336】onlineÔ tìm kiếm dropdown: thiết lập từ điển dữ liệu, popup chỉnh sửa báo lỗi
        useDicColor: true,
        popContainer: popContainer,
        // update-begin--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
        multiple: extendData.multiple ?? false,
        // update-end--author:liaozhiyang---date:20260826---for:【LHZP-261】onlineTìm kiếm dropdown hỗ trợ chọn nhiều
      },
    });
  }
}
