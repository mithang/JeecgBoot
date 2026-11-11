import dayjs from 'dayjs';
import { useUserStore } from '/@/store/modules/user';
import { defHttp } from '/@/utils/http/axios';
import { replaceAll, _eval } from '/@/utils';
import * as CustomExpression from '/@/utils/desform/customExpression';
import weekOfYear from 'dayjs/plugin/weekOfYear';
import quarterOfYear from 'dayjs/plugin/quarterOfYear';
dayjs.extend(weekOfYear);
dayjs.extend(quarterOfYear);
// Lấy của mọi biểu thức tùy chỉnh của người dùngKey
const ceKeys = Object.keys(CustomExpression);
// sẽkeyNối bằng dấu phẩy, có thể nối thành tham số phương thức, ví dụ:a,b,c --> function(a,b,c){}
const ceJoin = ceKeys.join(',');
// Lấy biểu thức tùy chỉnh của người dùng theokeyĐặt thứ tự của vào mảng, có thể sử dụng apply Truyền cho phương thức để gọi trực tiếp
const $CE$ = ceKeys.map((key) => CustomExpression[key]);

/** Biểu thức quy tắc thông thường #{...} */
const normalRegExp = /#{([^}]+)?}/g;
/** Biểu thức quy tắc tùy chỉnh của người dùng {{...}} */
const customRegExp = /{{([^}]+)?}}/g;
/** Biểu thức quy tắc điền giá trị ${...} */
const fillRuleRegExp = /\${([^}]+)?}/g;

/** action Loại */
export const ACTION_TYPES = { ADD: 'add', EDIT: 'edit', DETAIL: 'detail', RELOAD: 'reload' };

/**
 * Đưa bảng chính/bảng con một-một Lưu tạm thông tin cấu hình giá trị mặc định
 * @param field
 * @param item
 * @param config
 */
export function initDefValueConfig(field, item, config) {
  if (hasEffectiveValue(item.defVal)) {
    const obj = { field: field, type: item.type, value: item.defVal, view: item.view, fieldExtendJson: item.fieldExtendJson }
    // Tránh thêm trùng lặp
    const index = config.findIndex((c) => c.field === field);
    if (index === -1) {
      config.push(obj);
    } else {
      config[index] = obj;
    }
  }
}

/**
 * Đưa bảng con một-nhiều Lưu tạm thông tin cấu hình giá trị mặc định
 * @param item
 * @param config
 */
export function initSubTableDefValueConfig(item, config) {
  if (hasEffectiveValue(item.fieldDefaultValue)) {
    config.push({ field: item.key, type: item.type, value: item.fieldDefaultValue });
  }
}

/**
 * TảiformGiá trị mặc định của component-Chỉ dùng cho trang thêm mới
 * @param properties Cấu hình trường
 * @param callback Callback truyền giá trị
 * @param formData Giá trị biểu mẫu 
 */
export async function loadFormFieldsDefVal(properties, callback, formData?) {
  if (Array.isArray(properties) && properties.length > 0) {
    let formValues = {};
    for (let prop of properties) {
      let { value, type, field } = prop;
      value = await handleDefaultValue(value, ACTION_TYPES.ADD, formData||{});
      // Xử lý loại số, nếutype=numberđồng thờivalueCó giá trị
      if ('number' === type && value) {
        // parseFloat() Có thể xử lý trực tiếp chuỗi, số nguyên, số thập phân,nullvàundefined，
        // Loại không phải số thì trả về trực tiếpNaN, không cần lo báo lỗi
        value = Number.parseFloat(value);
      }
      // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-321】Component ngày(date)giá trị mặc định của các định dạng như năm, năm-tháng, năm-tuần, năm-quý đã thiết lập trong đó cần chuyển đổi thànhYYYY-MM-DD
      value = transformDefValDate(prop, value);
      // update-end--author:liaozhiyang---date:20240517---for：【TV360X-321】Component ngày(date)giá trị mặc định của các định dạng như năm, năm-tháng, năm-tuần, năm-quý đã thiết lập trong đó cần chuyển đổi thànhYYYY-MM-DD
      formValues[field] = value;
    }
    callback(formValues);
  }
}

/**
 * 2024-05-22
 * liaozhiyang
 * Component ngày(date)giá trị mặc định của các định dạng như năm, năm-tháng, năm-tuần, năm-quý đã thiết lập trong đó cần chuyển đổi thànhYYYY-MM-DD
 */
function transformDefValDate(prop, value, mode?) {
  const { type, field, view, fieldExtendJson } = prop;
  if (view == 'date' && fieldExtendJson) {
    const extendJson = JSON.parse(fieldExtendJson);
    const { picker } = extendJson;
    if (picker && picker != 'default' && value) {
      // Giá trị mặc định của truy vấn theo phạm vi, dạng như "2023,2024" / "2023-01,2023-12" / "2023-01-01,2023-12-31"
      if (mode === 'group' && typeof value === 'string' && value.indexOf(',') !== -1) {
        return value
          .split(',')
          .map((v) => transformPickerValue(picker, v))
          .join(',');
      }
      return transformPickerValue(picker, value);
    }
    return value;
  }
  return value;
}

/**
 * Đưa năm / Tháng / Tuần / Chuyển đổi chuỗi định dạng quý thành YYYY-MM-DD
 * @param picker year | month | week | quarter
 * @param value  Ví dụ "2023" / "2023-07" / "2023-19Tuần" / "2024-Q4"
 */
function transformPickerValue(picker, value) {
  try {
    if (picker === 'year') {
      // Năm (2023)
      const data = value.split('-');
      const y = data[0];
      return dayjs().year(y).format('YYYY-MM-DD');
    }
    if (picker === 'month') {
      // Năm - Tháng (2023-07)，dayjs.month là 0 Chỉ mục,7 Chỉ mục tương ứng của tháng 6
      const data = value.split('-');
      const y = data[0];
      const m = +data[1] - 1;
      return dayjs().year(y).month(m).format('YYYY-MM-DD');
    }
    if (picker === 'week') {
      // Năm - Tuần (2023-19Tuần), tương thích không có"Tuần"Hậu tố
      const data = value.split('-');
      const y = data[0];
      const matchResult = data[1].match(/^(\d+)Tuần?$/);
      const w = matchResult ? matchResult[1] : data[1];
      return dayjs().year(y).week(w).format('YYYY-MM-DD');
    }
    if (picker === 'quarter') {
      // Năm - Quý (2024-Q4)
      const data = value.split('-');
      const y = data[0];
      const matchResult = data[1].match(/^[Qq](\d)$/);
      const q = matchResult ? matchResult[1] : data[1];
      return dayjs().year(y).quarter(q).format('YYYY-MM-DD');
    }
  } catch (error) {
    return value;
  }
  return value;
}

export async function loadOneFieldDefVal(field, item, formValues) {
  let { defVal, type } = item;
  if (hasEffectiveValue(defVal)) {
    let value = await handleDefaultValue(defVal, ACTION_TYPES.ADD, {});
    if ('number' === type && value) {
      // update-begin--author:liaozhiyang---date:20240618---for：onlineTruy vấn theo phạm vi với giá trị mặc định của truy vấn thông thường không hoạt động
      if (item.mode == 'group' && typeof value === 'string' && value.indexOf(',') != -1) {
        const arr = value.split(',');
        value = [];
        if (arr[0]) {
          value.push(Number.parseFloat(arr[0]));
        }
        if (arr[1]) {
          value.push(Number.parseFloat(arr[1]));
        }
      } else {
        value = Number.parseFloat(value);
      }
      // update-end--author:liaozhiyang---date:20240618---for：onlineTruy vấn theo phạm vi với giá trị mặc định của truy vấn thông thường không hoạt động
    }
    value = transformDefValDate(item, value, item.mode);
    formValues[field] = value;
  }
}

/**
 * Phán đoán giá trị cho trước có hợp lệ hay không
 */
function hasEffectiveValue(val) {
  if (val || val === 0) {
    return true;
  }
  return false;
}

/** TảiJEditableTableGiá trị mặc định của component */
export function loadFieldDefValForSubTable({ subForms, subTable, row, action, getFormData }) {
  if (subTable && Array.isArray(subTable.columns) && subTable.columns.length > 0) {
    subTable.columns.forEach(async (column) => {
      let { key, fieldDefaultValue: defVal } = column;
      eachHandler(
        defVal,
        action,
        (value) => {
          if (subForms.form) {
            subForms.form.setFieldsValue({ [key]: value });
          } else {
            // update-begin---author:sunjianlei  Date:20200725 for：onlineKiểm thử chức năng, thao tác hàng chuyển thành chỉnh sửa hàng mới-----------
            let v = [{ rowKey: row.id, values: { [key]: value } }];
            (subForms.jvt || subForms.jet).setValues(v);
            // update-end---author:sunjianlei    Date:20200725 for：onlineKiểm thử chức năng, thao tác hàng chuyển thành chỉnh sửa hàng mới------------
          }
        },
        getFormData
      );
    });
  }
}

async function eachHandler(defVal, action, callback, getFormData) {
  if (defVal != null) {
    // Kiểm tra loại, nếu loại sai thì không tiếp tục chạy
    if (checkExpressionType(defVal)) {
      let value = await getDefaultValue(defVal, action, getFormData);
      if (value != null) {
        callback(value);
        return value;
      }
    } else {
      // Biểu thức không hợp lệ thì trả về trực tiếp, không phân tích
      callback(defVal);
    }
  }
}

/**
 * Xử lý giá trị mặc định
 * @param defVal
 * @param action
 * @param getFormData
 */
async function handleDefaultValue(defVal, action, getFormData) {
  if (defVal != null) {
    // Kiểm tra loại, nếu loại sai thì không tiếp tục chạy
    if (checkExpressionType(defVal)) {
      let value = await getDefaultValue(defVal, action, getFormData);
      if (value != null) {
        return value;
      }
    }
  }
  return defVal;
}

/**
 * Kiểm tra loại biểu thức có hợp pháp hay không, quy tắc:
 * 1、biểu thức quy tắc điền giá trị không được dùng lẫn với các biểu thức khác
 * 2、mỗi lần chỉ được điền một biểu thức quy tắc điền giá trị
 * 3、biểu thức thông thường và biểu thức tùy chỉnh của người dùng có thể dùng lẫn nhau
 */
export function checkExpressionType(defVal) {
  // Lấy số lượng của từng biểu thức
  let normalCount = 0,
    customCount = 0,
    fillRuleCount = 0;
  defVal.replace(fillRuleRegExp, () => fillRuleCount++);
  if (fillRuleCount > 1) {
    logWarn(`Biểu thức[${defVal}]Không hợp lệ: chỉ được điền đồng thời một biểu thức quy tắc điền giá trị!`);
    return false;
  }
  defVal.replace(normalRegExp, () => normalCount++);
  defVal.replace(customRegExp, () => customCount++);
  // Số lượng các quy tắc khác ngoài quy tắc điền giá trị
  let fillRuleOtherCount = normalCount + customCount;
  if (fillRuleCount > 0 && fillRuleOtherCount > 0) {
    logWarn(`Biểu thức[${defVal}]Không hợp lệ: biểu thức quy tắc điền giá trị không được dùng lẫn với các biểu thức khác!`);
    return false;
  }
  return true;
}

/** Lấy mọi biểu thức khớp */
function getRegExpMap(text, exp) {
  let map = new Map();
  text.replace(exp, function (match, param) {
    map.set(match, param.trim());
    return match;
  });
  return map;
}

/** Lấy giá trị mặc định, có thể thực thi biểu thức, có thể thực thi phương thức tùy chỉnh của người dùng, có thể lấy bất đồng bộ thông tin người dùng, v.v. */
async function getDefaultValue(defVal, action, getFormData) {
  // Chỉ khi ở add và reload Chỉ ở chế độ này mới thực thi quy tắc điền giá trị
  if (action === ACTION_TYPES.ADD || action === ACTION_TYPES.RELOAD) {
    // Phán đoán có phải biểu thức quy tắc điền giá trị hay không, nếu phải thì thực thi quy tắc điền giá trị
    if (fillRuleRegExp.test(defVal)) {
      let arr: any[] = [getFormData];
      return await executeRegExp(defVal, fillRuleRegExp, executeFillRuleExpression, arr);
    }
  }
  // Chỉ khi ở add Chỉ ở chế độ này mới thực thi các biểu thức khác
  if (action === ACTION_TYPES.ADD) {
    // Lấy và thay thế mọi biểu thức thông thường
    defVal = await executeRegExp(defVal, normalRegExp, executeNormalExpression);
    // Lấy và thay thế mọi biểu thức tùy chỉnh của người dùng
    defVal = await executeRegExp(defVal, customRegExp, executeCustomExpression);
    return defVal;
  }
  return null;
}

async function executeRegExp(defVal, regExp, execFun, otherParams: any[] = []) {
  let map = getRegExpMap(defVal, regExp);
  for (let origin of map.keys()) {
    let exp = map.get(origin);
    let result = await execFun.apply(null, [exp, origin, ...otherParams]);
    // Nếu chỉ có một biểu thức, thì không thay thế (vì một khi thay thế, kiểu sẽ bị chuyển thànhString) , trực tiếp trả về kết quả thực thi, bảo đảm loại trả về không thay đổi
    if (origin === defVal) {
      return result;
    }
    defVal = replaceAll(defVal, origin, result);
  }
  return defVal;
}

/** Thực thi 【biểu thức thông thường】#{xxx} */
async function executeNormalExpression(expression, origin) {
  switch (expression) {
    case 'date':
      return dayjs().format('YYYY-MM-DD');
    case 'time':
      return dayjs().format('HH:mm:ss');
    case 'datetime':
      return dayjs().format('YYYY-MM-DD HH:mm:ss');
    default:
      // Lấy thông tin của người dùng đang đăng nhập hiện tại
      let result = getUserInfoByExpression(expression);
      if (result != null) {
        return result;
      }
      // Không có biểu thức thỏa mãn điều kiện, trả về giá trị gốc
      return origin;
  }
}

/** Lấy thông tin người dùng tương ứng theo biểu thức */
function getUserInfoByExpression(expression) {
  const userStore = useUserStore();
  let userInfo = userStore.getUserInfo;
  if (userInfo) {
    switch (expression) {
      case 'sysUserId':
        return userInfo.id;
      // Tài khoản đăng nhập của người dùng đang đăng nhập hiện tại
      case 'sysUserCode':
      case 'sys_user_code':
        return userInfo.username;
      // Tên thật của người dùng đang đăng nhập hiện tại
      case 'sysUserName':
        return userInfo.realname;
      // Mã phòng ban của người dùng đang đăng nhập hiện tại
      case 'sysOrgCode':
      case 'sys_org_code':
        return userInfo.orgCode;
    }
  }
  return null;
}

/** Thực thi 【biểu thức tùy chỉnh của người dùng】 {{xxx}} */
async function executeCustomExpression(expression, origin) {
  // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
  // Sử dụng eval Tạo ra một phương thức, tham số của phương thức này chính là tất cả biểu thức tùy chỉnh của người dùng
  let fn = _eval(`(function (${ceJoin}){ return ${expression} })`);
  // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
  try {
    // Sau đó gọi phương thức này, đồng thời truyền biểu thức vào, từ đó hoàn thành việc thực thi biểu thức
    return fn.apply(null, $CE$);
  } catch (e) {
    // Thực thi thất bại, xuất lỗi và trả về giá trị gốc
    logError(e);
    return origin;
  }
}

/** Thực thi 【biểu thức quy tắc điền giá trị】 ${xxx} */
async function executeFillRuleExpression(expression, origin, getFormData) {
  let formData = {};
  if (typeof getFormData === 'function') {
    formData = getFormData();
  }else if(getFormData){
    formData = {...getFormData}
  }
  // Phân tích (parse) url Tham số
  expression = handleFillRuleQueryString(expression).exp;
  let url = `/sys/fillRule/executeRuleByCode/${expression}`;
  let { success, message, result } = await defHttp.put({ url, params: formData }, { isTransformResponse: false });
  if (success) {
    return result;
  } else {
    logError(`Quy tắc điền giá trị (${expression}) thực thi thất bại:${message}`);
    return origin;
  }
}

// Xử lý quy tắc điền giá trị queryString Tham số
export function handleFillRuleQueryString(expression: string) {
  let arr = expression.split('?');
  if (arr.length > 1) {
    let queryString = '';
    let watchFields: string[] = [];
    let str = arr[1];
    let pairs = str.split('&');
    pairs.forEach((pair, idx) => {
      let [key, value] = pair.split('=');
      value = value.trim();
      // Lấy ra các trường đang được theo dõi, nhiều trường thì phân tách bằng dấu phẩy
      if (key === 'onl_watch') {
        watchFields = value.split(',');
      } else {
        queryString += `${key}=${value}`;
        if (idx < pairs.length - 1) {
          queryString += '&';
        }
      }
    });
    return {
      exp: arr[0] + (queryString === '' ? '' : ('?' + queryString)),
      watchFields: watchFields,
    };
  }
  return {exp: expression, watchFields: []};
}

export function handleFillRuleWatchKeysMap(properties: Recordable[]) {
  const watchKeyMap = new Map<string, string[]>();
  if (Array.isArray(properties) && properties.length > 0) {
    for (let prop of properties) {
      let {value: defVal, field} = prop;
      if (defVal == null || defVal == '') {
        continue;
      }
      // Kiểm tra loại, nếu loại sai thì không tiếp tục chạy
      if (!checkExpressionType(defVal)) {
        continue;
      }
      // Phán đoán có phải quy tắc điền giá trị hay không, nếu phải thì phân tích quy tắc điền giá trị
      if (fillRuleRegExp.test(defVal)) {
        let map = getRegExpMap(defVal, fillRuleRegExp);
        for (let origin of map.keys()) {
          let exp = map.get(origin);
          const {watchFields} = handleFillRuleQueryString(exp);
          for (const watchField of watchFields) {
            let arr = watchKeyMap.get(watchField)
            if (!Array.isArray(arr)) {
              arr = []
              watchKeyMap.set(watchField, arr)
            }
            if (arr.includes(field)) {
              continue;
            }
            arr.push(field);
          }
        }
      }
    }
  }
  return watchKeyMap;
}

function logWarn(message) {
  console.warn('[loadFieldDefVal]:', message);
}

function logError(message) {
  console.error('[loadFieldDefVal]:', message);
}
