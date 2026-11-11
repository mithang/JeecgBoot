import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

enum DateFormat {
  datetime = 'YYYY-MM-DD HH:mm:ss',
  date = 'YYYY-MM-DD',
}

/**
 * Ngày, thời gian
 */
export default class DateWidget extends IFormSchema {
  format: string;
  showTime: boolean;
  picker: string | undefined;

  allowSelectRange: boolean;

  constructor(key, data, queryItem) {
    super(key, data);
    this.format = DateFormat[data.view];
    this.showTime = data.view == 'date' ? false : true;
    // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
    let fieldExtendJson = data.fieldExtendJson;
    if (data.view == 'date' && fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        this.picker = fieldExtendJson.picker;
      } else {
        this.picker = undefined;
      }
    }
    // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
    // Chỉ khi bằng và không bằng mới có thể chọn phạm vi đặt trước (hôm nay, hôm qua, tuần này, v.v.)
    this.allowSelectRange = ['eq', 'ne'].includes(queryItem?.rule)
  }

  getItem(): FormSchema {
    let item = super.getItem();
    return Object.assign({}, item, {
      component: 'DatePickerInFilter',
      componentProps: {
        placeholder: `Vui lòng chọn${this.label}`,
        showTime: this.showTime,
        valueFormat: this.format,
        allowSelectRange: this.allowSelectRange,
        // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        picker: this.picker,
        // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        style: {
          width: '100%',
        },
        getPopupContainer: (_node) => {
          return this.getModalAsContainer();
        },
      },
    });
  }
}
