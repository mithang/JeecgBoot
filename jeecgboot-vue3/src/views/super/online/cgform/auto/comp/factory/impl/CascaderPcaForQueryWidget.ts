import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * Trình thiết kế biểu mẫu-Truy vấn tỉnh/thành/quận huyện
 */
export default class CascaderPcaForQueryWidget extends IFormSchema {

  schema: Recordable;
  // Cấp liên động tỉnh/thành/huyện
  areaLevel: number;
  // Có cho phép thay đổi cấp bậc hay không
  allowChangeLevel: boolean;

  constructor(key: string, data: Recordable, queryItem: Recordable) {
    super(key, data);
    this.schema = data
    this.areaLevel = data['areaLevel'] ?? 3;
    // Chỉ khi bằng và không bằng mới có thể thay đổi cấp bậc
    this.allowChangeLevel = ['eq', 'ne'].includes(queryItem?.rule)
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'CascaderPcaInFilter',
      componentProps:{
        areaLevel: this.areaLevel,
        allowChangeLevel: this.allowChangeLevel,
        placeholder: 'Vui lòng chọn…',
        style: {
          width: '100%',
        }
      }
    });
  }

}
