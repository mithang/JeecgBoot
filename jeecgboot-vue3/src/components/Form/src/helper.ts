import type { ValidationRule } from 'ant-design-vue/lib/form/Form';
import type { ComponentType } from './types/index';
import { useI18n } from '/@/hooks/web/useI18n';
import { dateUtil } from '/@/utils/dateUtil';
import { isNumber, isObject } from '/@/utils/is';

const { t } = useI18n();

/**
 * @description: Tạoplaceholder
 */
export function createPlaceholderMessage(component: ComponentType) {
  if (component.includes('Input') || component.includes('Complete')) {
    return t('common.inputText');
  }
  if (component.includes('Picker')) {
    return t('common.chooseText');
  }
  if (
    component.includes('Select') ||
    component.includes('Cascader') ||
    component.includes('Checkbox') ||
    component.includes('Radio') ||
    component.includes('Switch')
  ) {
    // return `Vui lòng chọn${label}`;
    return t('common.chooseText');
  }
  //update-begin---author:liaozhiyang ---date:20260518  for：[issues/9638]BasicFormTrong đó dùng JUpload và khi bắt buộc thì kiểm chứng luôn nhắc chưa tải lên, bổ sung tiền tố nhắc bắt buộc của loại Upload-----------
  if (component.includes('Upload')) {
    return t('common.uploadText');
  }
  //update-end---author:liaozhiyang ---date:20260518  for：[issues/9638]BasicFormTrong đó dùng JUpload và khi bắt buộc thì kiểm chứng luôn nhắc chưa tải lên, bổ sung tiền tố nhắc bắt buộc của loại Upload-----------
  return '';
}

const DATE_TYPE = ['DatePicker', 'MonthPicker', 'WeekPicker', 'TimePicker'];

function genType() {
  return [...DATE_TYPE, 'RangePicker'];
}

export function setComponentRuleType(rule: ValidationRule, component: ComponentType, valueFormat: string) {
  //https://github.com/vbenjs/vue-vben-admin/pull/3082 githubVăn bản gốc đã sửa
  if (Reflect.has(rule, 'type')) {
    return;
  }
  if (['DatePicker', 'MonthPicker', 'WeekPicker', 'TimePicker'].includes(component)) {
    rule.type = valueFormat ? 'string' : 'object';
  } else if (['RangePicker', 'Upload', 'CheckboxGroup', 'TimePicker'].includes(component)) {
    rule.type = 'array';
  } else if (['InputNumber'].includes(component)) {
    rule.type = 'number';
  }
}

export function processDateValue(attr: Recordable, component: string) {
  const { valueFormat, value } = attr;
  if (valueFormat) {
    attr.value = isObject(value) ? dateUtil(value).format(valueFormat) : value;
  } else if (DATE_TYPE.includes(component) && value) {
    attr.value = dateUtil(attr.value);
  }
}

export function handleInputNumberValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  if (['Input', 'InputPassword', 'InputSearch', 'InputTextArea'].includes(component)) {
    return val && isNumber(val) ? `${val}` : val;
  }
  return val;
}
/** 
*liaozhiyang
*2023-12-26
*Một số component truyền giá trị cần chuyển kiểu chuỗi thành kiểu số
*/ 
export function handleInputStringValue(component?: ComponentType, val?: any) {
  if (!component) return val;
  // Giải thích logic mã: 【TV360X-13] InputNumber thiết lập chính xác 3 chữ số thập phân khi truyền vào''đã trở thành0.00
  if (['InputNumber'].includes(component) && typeof val === 'string' && val != '') {
    return Number(val);
  }
  return val;
}

/**
 * Trường thời gian
 */
export const dateItemType = genType();
