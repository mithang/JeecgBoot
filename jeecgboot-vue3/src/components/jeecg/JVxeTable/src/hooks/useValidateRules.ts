import { VxeTablePropTypes } from 'vxe-table';
import { isArray } from '/@/utils/is';
import { HandleArgs } from './useColumns';
import { replaceProps } from '../utils/enhancedUtils';

export function useValidateRules(args: HandleArgs) {
  const { data } = args;
  const col = args.col!;
  let rules: VxeTablePropTypes.EditRules[] = [];
  if (isArray(col.validateRules)) {
    for (let rule of col.validateRules) {
      let replace = {
        message: replaceProps(col, rule.message),
      };
      if (rule.unique || rule.pattern === 'only') {
        // Bộ kiểm chứng duy nhất
        rule.validator = uniqueValidator(args);
      } else if (rule.pattern) {
        // Không rỗng
        if (rule.pattern === fooPatterns[0].value) {
          rule.required = true;
          delete rule.pattern;
        } else {
          // Tương thích quy tắc đặc biệt của biểu mẫu Online
          let reg: any = rule.pattern;
          for (let foo of fooPatterns) {
            if (foo.value === rule.pattern) {
              reg = foo.pattern;
              break;
            }
          }
          // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-172]Khi kiểm chứng pattern của chỉnh sửa hàng mà giá trị trống thì không nên kích hoạt, chỉ kiểm chứng khi có giá trị (không trống do quy tắc required chịu trách nhiệm)
          const regExp = reg instanceof RegExp ? reg : new RegExp(reg);
          delete rule.pattern;
          rule.validator = emptySkipPatternValidator(regExp);
          // update-end--author:liaozhiyang---date:20260714---for：【LHZP-172]Khi kiểm chứng pattern của chỉnh sửa hàng mà giá trị trống thì không nên kích hoạt, chỉ kiểm chứng khi có giá trị (không trống do quy tắc required chịu trách nhiệm)
        }
      } else if (typeof rule.handler === 'function') {
        // Kiểm chứng hàm tùy chỉnh
        rule.validator = handlerConvertToValidator;
      }
      rules.push(Object.assign({}, rule, replace));
    }
  }
  data.innerEditRules[col.key] = rules;
}

/** Bộ kiểm chứng duy nhất */
function uniqueValidator({ methods }: HandleArgs) {
  return function (event) {
    const { cellValue, column, rule } = event;
    // Giải thích logic mã: 【TV360X-299]Component JVxetable kiểm chứng duy nhất sẽ lọc bỏ chuỗi rỗng
    if (cellValue == '') return Promise.resolve();
    let tableData = methods.getTableData();
    let findCount = 0;
    for (let rowData of tableData) {
      if (rowData[column.params.key] === cellValue) {
        if (++findCount >= 2) {
          return Promise.reject(new Error(rule.message));
        }
      }
    }
    return Promise.resolve();
  };
}

/** Chuyển handler phiên bản cũ thành phiên bản mớiValidator */
function handlerConvertToValidator(event) {
  const { column, rule } = event;
  return new Promise((resolve, reject) => {
    rule.handler(event, (flag, msg) => {
      let message = rule.message;
      if (typeof msg === 'string') {
        message = replaceProps(column.params, msg);
      }
      if (flag == null) {
        resolve(message);
      } else if (!!flag) {
        resolve(message);
      } else {
        reject(new Error(message));
      }
    });
  });
}

// update-begin--author:liaozhiyang---date:20260714---for：【LHZP-172]Khi kiểm chứng pattern của chỉnh sửa hàng mà giá trị trống thì không nên kích hoạt, chỉ kiểm chứng khi có giá trị (không trống do quy tắc required chịu trách nhiệm)
/** pattern Trình kiểm chứng: giá trị rỗng không kiểm chứng (giá trị không rỗng giao cho quy tắc required), chỉ khi có giá trị mới kiểm chứng bằng regex */
function emptySkipPatternValidator(regExp: RegExp) {
  return function (event) {
    const { cellValue, rule } = event;
    if (cellValue === '' || cellValue === null || cellValue === undefined) {
      return Promise.resolve();
    }
    return regExp.test(String(cellValue)) ? Promise.resolve() : Promise.reject(new Error(rule.message));
  };
}
// update-end--author:liaozhiyang---date:20260714---for：【LHZP-172]Khi kiểm chứng pattern của chỉnh sửa hàng mà giá trị trống thì không nên kích hoạt, chỉ kiểm chứng khi có giá trị (không trống do quy tắc required chịu trách nhiệm)

// Tương thích với quy tắc của online
const fooPatterns = [
  { title: 'Không rỗng', value: '*', pattern: /^.+$/ },
  { title: '6đến 16 chữ số', value: 'n6-16', pattern: /^\d{6,16}$/ },
  { title: '6đến 16 ký tự bất kỳ', value: '*6-16', pattern: /^.{6,16}$/ },
  { title: '6đến 18 chữ cái', value: 's6-18', pattern: /^[a-z|A-Z]{6,18}$/ },
  // Giải thích logic mã: VUEN-1160 Đối với nhiều bảng con, kiểm chứng URL không đúng
  {
    title: 'URL',
    value: 'url',
    pattern: /^((ht|f)tps?):\/\/[\w\-]+(\.[\w\-]+)+([\w\-.,@?^=%&:\/~+#]*[\w\-@?^=%&\/~+#])?$/,
  },
  // Giải thích logic mã: 【TV360X-466] Quy tắc kiểm chứng email thống nhất với quy tắc kiểm chứng một-một đầu tiên
  { title: 'Email', value: 'e', pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/ },
  { title: 'Số điện thoại', value: 'm', pattern: /^1[3456789]\d{9}$/ },
  { title: 'Mã bưu chính', value: 'p', pattern: /^\d{6}$/ },
  { title: 'Chữ cái', value: 's', pattern: /^[A-Z|a-z]+$/ },
  { title: 'Số', value: 'n', pattern: /^-?\d+(\.?\d+|\d?)$/ },
  { title: 'Số nguyên', value: 'z', pattern: /^-?\d+$/ },
  {
    title: 'Số tiền',
    value: 'money',
    pattern: /^(([1-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,5}))$/,
  },
];
