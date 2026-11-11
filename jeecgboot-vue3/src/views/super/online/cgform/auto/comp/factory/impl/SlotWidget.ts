import { FormSchema } from '/@/components/Form';
import IFormSchema from '../IFormSchema';

/**
 * slot
 */
export default class SlotWidget extends IFormSchema {
  slot: string;
  picker: string | undefined;
  precision: number | undefined;

  constructor(key, data) {
    super(key, data);
    this.slot = '';
    // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-180】Truy vấn theo phạm vi theo năm, năm-tháng, tuần, quý
    let fieldExtendJson = data.fieldExtendJson;
    if (data.view == 'date' && fieldExtendJson) {
      fieldExtendJson = JSON.parse(fieldExtendJson);
      if (fieldExtendJson.picker && fieldExtendJson.picker != 'default') {
        this.picker = fieldExtendJson.picker;
      } else {
        this.picker = undefined;
      }
    }
    // update-end--author:liaozhiyang---date:20240520---for：【TV360X-180】Truy vấn theo phạm vi theo năm, năm-tháng, tuần, quý
    // update-begin--author:liaozhiyang---date:20240606---for：【TV360X-214】Control truy vấn theo phạm vi không định dạng theo cấu hình
    this.precision = data.dbPointLength;
    // update-end--author:liaozhiyang---date:20240606---for：【TV360X-214】Control truy vấn theo phạm vi không định dạng theo cấu hình
  }

  getItem(): FormSchema {
    let item = super.getItem();
    let slot = this.slot;
    const componentProps: any = {};
    this.picker && (componentProps.picker = this.picker);
    // update-begin--author:liaozhiyang---date:20240606---for：【TV360X-214】Control truy vấn theo phạm vi không định dạng theo cấu hình
    this.precision && (componentProps.precision = this.precision);
    // update-end--author:liaozhiyang---date:20240606---for：【TV360X-214】Control truy vấn theo phạm vi không định dạng theo cấu hình
    // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-180】Truy vấn theo phạm vi theo năm, năm-tháng, tuần, quý
    return Object.assign({}, item, {
      slot,
      componentProps,
    });
    // update-end--author:liaozhiyang---date:20240520---for：【TV360X-180】Truy vấn theo phạm vi theo năm, năm-tháng, tuần, quý
  }

  groupDate() {
    this.slot = 'groupDate';
    return this;
  }

  groupDatetime() {
    this.slot = 'groupDatetime';
    return this;
  }

  groupTime() {
    // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn
    this.slot = 'groupTime';
    return this;
    // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn
  }

  groupNumber() {
    this.slot = 'groupNumber';
    return this;
  }
}
