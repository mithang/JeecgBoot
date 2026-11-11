/**
 * Từ điển util
 * author: scott
 * date: 20190109
 */

import { ajaxGetDictItems, getDictItemsByCode } from './index';

/**
 * Lấy mảng từ điển
 * [Hiện tại chỉ trang designer biểu mẫu dùng phương thức này]
 * @param dictCode Từ điểnCode
 * @param isTransformResponse Có chuyển đổi kết quả trả về không
 * @return List<Map>
 */
export async function initDictOptions(dictCode, isTransformResponse = true) {
  if (!dictCode) {
    return 'Từ điểnCodeKhông được trống!';
  }
  //Ưu tiên đọc cấu hình từ điển từ cache
  if (getDictItemsByCode(dictCode)) {
    let res = {};
    res.result = getDictItemsByCode(dictCode);
    res.success = true;
    if (isTransformResponse) {
      return res.result;
    } else {
      return res;
    }
  }
  //Lấy mảng từ điển
  return await ajaxGetDictItems(dictCode, {}, { isTransformResponse });
}

/**
 * Phương thức chung thay thế văn bản bằng giá trị từ điển
 * @param dictOptions  Mảng từ điển
 * @param text  Giá trị từ điển
 * @return String
 */
export function filterDictText(dictOptions, text) {
  // --update-begin----author:sunjianlei---date:20200323------for: Dịch từ điển text Cho phép tách bằng dấu phẩy ---
  if (text != null && Array.isArray(dictOptions)) {
    let result = [];
    // Cho phép tách bằng nhiều dấu phẩy, cho phép truyền đối tượng mảng
    let splitText;
    if (Array.isArray(text)) {
      splitText = text;
    } else {
      splitText = text.toString().trim().split(',');
    }
    for (let txt of splitText) {
      let dictText = txt;
      for (let dictItem of dictOptions) {
        // Giải thích logic mã: 【TV360X-469]Tương thích dữ liệunullGiá trị tránh báo lỗi
        if (dictItem == null) continue;
        if (dictItem.value == null) continue;
        if (txt.toString() === dictItem.value.toString()) {
          dictText = dictItem.text || dictItem.title || dictItem.label;
          break;
        }
      }
      result.push(dictText);
    }
    return result.join(',');
  }
  return text;
  // --update-end----author:sunjianlei---date:20200323------for: Dịch từ điển text Cho phép tách bằng dấu phẩy ---
}

/**
 * Phương thức chung thay thế văn bản bằng giá trị từ điển(Chọn nhiều)
 * @param dictOptions  Mảng từ điển
 * @param text  Giá trị từ điển
 * @return String
 */
export function filterMultiDictText(dictOptions, text) {
  //js “!text” Cho rằng0Trống nên xử lý trước
  if (text === 0 || text === '0') {
    if (dictOptions) {
      for (let dictItem of dictOptions) {
        if (text == dictItem.value) {
          return dictItem.text;
        }
      }
    }
  }

  if (!text || text == 'undefined' || text == 'null' || !dictOptions || dictOptions.length == 0) {
    return '';
  }
  let re = '';
  text = text.toString();
  let arr = text.split(',');
  dictOptions.forEach(function (option) {
    if (option) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i] === option.value) {
          re += option.text + ',';
          break;
        }
      }
    }
  });
  if (re == '') {
    return text;
  }
  return re.substring(0, re.length - 1);
}

/**
 * Phương thức chung thay thế văn bản bằng giá trị từ điển(Chọn nhiều)
 * @param dictOptions  Mảng từ điển
 * @param val  Giá trị từ điển
 * @return {*[]} Trả về đối tượng gốc của mục từ điển
 */
export function filterMultiDictObjs(dictOptions, val) {
  val = val?.toString?.() ?? '';
  if (!val || !dictOptions || dictOptions.length === 0) {
    return [];
  }
  const objs = [];
  const vals = val.split(',');
  for (const item of vals) {
    const option = dictOptions.find((option) => option && option.value === item);
    if (option) {
      objs.push({
        value: item,
        text: option.text || option.title || option.label,
        color: option.color,
        hasColor: !!option.color,
      });
    }
  }
  return objs;
}

/**
 * Văn bản tương ứng với giá trị trường dịch
 * @param children
 * @returns string
 */
export function filterDictTextByCache(dictCode, key) {
  if (key == null || key.length == 0) {
    return;
  }
  if (!dictCode) {
    return 'Từ điểnCodeKhông được trống!';
  }
  //Ưu tiên đọc cấu hình từ điển từ cache
  if (getDictItemsByCode(dictCode)) {
    let item = getDictItemsByCode(dictCode).filter((t) => t['value'] == key);
    if (item && item.length > 0) {
      return item[0]['text'];
    }
  }
}

/** Thông quacodeLấy mảng từ điển */
export async function getDictItems(dictCode, params) {
  //Ưu tiên đọc cấu hình từ điển từ cache
  if (getDictItemsByCode(dictCode)) {
    let desformDictItems = getDictItemsByCode(dictCode).map((item) => ({
      ...item,
      label: item.text,
    }));
    return Promise.resolve(desformDictItems);
  }

  //Trong cache không có, thì yêu cầu backend
  return await ajaxGetDictItems(dictCode, params)
    .then((result) => {
      if (result.length) {
        let res = result.map((item) => ({ ...item, label: item.text }));
        console.log('------- TừDBđã lấy được từ điển trong-------dictCode : ', dictCode, res);
        return Promise.resolve(res);
      } else {
        console.error('getDictItems error: : ', res);
        return Promise.resolve([]);
      }
    })
    .catch((res) => {
      console.error('getDictItems error: ', res);
      return Promise.resolve([]);
    });
}
