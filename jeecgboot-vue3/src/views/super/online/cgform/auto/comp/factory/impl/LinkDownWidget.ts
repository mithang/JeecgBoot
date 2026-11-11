import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Liên động dropdown- Nguyên lý là:
 * Sử dụngJDictSelectTagComponent(2022-03-09Bản khả thi để kiểm thử Sau này nếu có thay đổi, xin lưu ý)
 * Lắng nghe của biểu mẫuchangesự kiện, xóa giá trị biểu mẫu cấp dưới, đồng thời thay đổiprops
 * Vấn đề nằm ở1.Không cócodekhi không cần thiết lập tùy chọn
 * Ưu điểm nằm ở: Có thể không cần xét vị trí component (nhưng cần sửa API phía backend)
 */
export default class LinkDownWidget extends IFormSchema {
  /*title-value*/
  options: any[];
  next: string;
  type: string;
  table: string;
  txt: string;
  store: string;
  pidField: string;
  idField: string;
  origin: boolean;
  condition: string;

  constructor(key, data) {
    super(key, data);
    const { dictTable, dictText, dictCode, pidField, idField, origin, condition } = data;
    this.table = dictTable;
    this.txt = dictText;
    this.store = dictCode;
    this.idField = idField;
    this.pidField = pidField;
    this.origin = origin;
    this.condition = condition;
    // Đều là mảng rỗng
    this.options = [];
    this.next = data.next || '';
    this.type = data.type;
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let componentProps = this.getComponentProps();
    return Object.assign({}, item, {
      component: 'OnlineSelectCascade',
      componentProps,
    });
  }

  getComponentProps() {
    let baseProp = {
      table: this.table,
      txt: this.txt,
      store: this.store,
      pidField: this.pidField,
      idField: this.idField,
      origin: this.origin,
      pidValue: '-1',
      style: {
        width: '100%',
      },
      onChange: (value) => {
        console.log('Component phân cấp-onChange', value);
        this.valueChange(value);
      },
      onNext: (pidValue) => {
        console.log('Component phân cấp-onNext', pidValue);
        this.nextOptionsChange(pidValue);
      },
    };
    if (this._data.origin === true) {
      baseProp['condition'] = this.condition;
    }
    return baseProp;
  }

  async nextOptionsChange(pidValue) {
    if (!this.formRef) {
      console.error('Không tìm thấy tham chiếu biểu mẫu');
      return;
    }
    if (!this.next) {
      return;
    }
    let ref = this.formRef.value;
    await ref.updateSchema({
      field: this.next,
      componentProps: {
        pidValue,
      },
    });
  }

  async valueChange(value) {
    if (!this.formRef) {
      console.error('Không tìm thấy tham chiếu biểu mẫu');
      return;
    }
    // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-1856】Component liên động cuối cùngjsTăng cườngonchangPhương thức không có hiệu lực
    let ref = this.formRef.value;
    // Kích hoạtformThuộc về cấp độchangesự kiện
    ref.$formValueChange(this.field, value);
    if (this.next) {
      // Đặt lạivalue
      await ref.setFieldsValue({ [this.next]: '' });
    }
    // update-end--author:liaozhiyang---date:20240717---for：【TV360X-1856】Component liên động cuối cùngjsTăng cườngonchangPhương thức không có hiệu lực
  }
}
