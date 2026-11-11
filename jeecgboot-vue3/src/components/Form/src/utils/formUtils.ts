import { unref } from 'vue';
import { dateUtil } from '/@/utils/dateUtil';

/**
 * Chuyển đổi trường giá trị số thời gian theo khoảng của biểu mẫu
 * @param props
 * @param values
 */
export function handleRangeValue(props, values) {
  //Kiểm tra đã cấu hình và xử lý chưafieldMapToTime
  const fieldMapToTime = unref(props)?.fieldMapToTime;
  fieldMapToTime && (values = handleRangeTimeValue(props, values));
  //Kiểm tra đã cấu hình và xử lý chưafieldMapToNumber
  const fieldMapToNumber = unref(props)?.fieldMapToNumber;
  fieldMapToNumber && (values = handleRangeNumberValue(props, values));
  return values;
}
/**
 * Xử lý chuyển thời gian thành 2 trường
 * @param props
 * @param values
 */
export function handleRangeTimeValue(props, values) {
  const fieldMapToTime = unref(props).fieldMapToTime;
  if (!fieldMapToTime || !Array.isArray(fieldMapToTime)) {
    return values;
  }
  for (const [field, [startTimeKey, endTimeKey], format = 'YYYY-MM-DD'] of fieldMapToTime) {
    if (!field || !startTimeKey || !endTimeKey || !values[field]) {
      continue;
    }

    // 【issues/I53G9Y] Component khoảng ngày có thể là chuỗi
    let timeValue = values[field];
    if (!Array.isArray(timeValue)) {
      timeValue = timeValue.split(',');
    }
    const [startTime, endTime]: string[] = timeValue;
    // Giải thích logic mã: 【issues/7216]Khi giá trị của component RangePicker cho phép bắt đầu/Khi kết thúc để trống,Xử lý bất thường của fieldMapToTime trong biểu mẫu---
    startTime && (values[startTimeKey] = dateUtil(startTime).format(format));
    endTime && (values[endTimeKey] = dateUtil(endTime).format(format));
    Reflect.deleteProperty(values, field);
  }
  return values;
}
/**
 * Xử lý chuyển số thành 2 trường
 * @param props
 * @param values
 * @updateby liusq
 * @updateDate:2021-09-16
 */
export function handleRangeNumberValue(props, values) {
  const fieldMapToNumber = unref(props).fieldMapToNumber;
  if (!fieldMapToNumber || !Array.isArray(fieldMapToNumber)) {
    return values;
  }
  for (const [field, [startNumberKey, endNumberKey]] of fieldMapToNumber) {
    if (!field || !startNumberKey || !endNumberKey || !values[field]) {
      continue;
    }
    // Giải thích logic mã: Dùng cho truy vấn theo khoảng giá trị, không biết khâu chuyển đổi trung gian của định dạng mảng đã xảy ra vấn đề ở đâu, tại đây sẽ biến thành chuỗi, cần ép chuyển lại thành mảng
    let temp = values[field];
    if (typeof temp === 'string') {
      temp = temp.split(',');
    }
    const [startNumber, endNumber]: number[] = temp;
    values[startNumberKey] = startNumber;
    values[endNumberKey] = endNumber;
    Reflect.deleteProperty(values, field);
  }
  return values;
}
