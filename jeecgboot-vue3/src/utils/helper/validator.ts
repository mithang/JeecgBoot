import { dateUtil } from '/@/utils/dateUtil';
import { duplicateCheck } from '/@/views/system/user/user.api';
import { defHttp } from '/@/utils/http/axios';

export const rules = {
  rule(type, required) {
    if (type === 'email') {
      return this.email(required);
    }
    if (type === 'phone') {
      return this.phone(required);
    }
  },
  email(required) {
    return [
      {
        required: required ? required : false,
        validator: async (_rule, value) => {
          if (required == true && !value) {
            return Promise.reject('Vui lòng nhập email!');
          }
          if (
            value &&
            !new RegExp(
              /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
            ).test(value)
          ) {
            return Promise.reject('Vui lòng nhập đúng định dạng email!');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ] as ArrayRule;
  },
  phone(required) {
    return [
      {
        required: required,
        validator: async (_, value) => {
          if (required && !value) {
            return Promise.reject('Vui lòng nhập số điện thoại!');
          }
          if (!/^1[3456789]\d{9}$/.test(value)) {
            return Promise.reject('Định dạng số điện thoại sai');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  startTime(endTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('Vui lòng chọn thời gian bắt đầu');
          }
          if (endTime && value && dateUtil(endTime).isBefore(value)) {
            return Promise.reject('Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  endTime(startTime, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (required && !value) {
            return Promise.reject('Vui lòng chọn thời gian kết thúc');
          }
          if (startTime && value && dateUtil(value).isBefore(startTime)) {
            return Promise.reject('Thời gian kết thúc phải lớn hơn thời gian bắt đầu');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ];
  },
  confirmPassword(values, required) {
    return [
      {
        required: required ? required : false,
        validator: (_, value) => {
          if (!value) {
            return Promise.reject('Mật khẩu không được trống');
          }
          if (value !== values.password) {
            return Promise.reject('Mật khẩu nhập hai lần không khớp!');
          }
          return Promise.resolve();
        },
      },
    ];
  },
  duplicateCheckRule(tableName, fieldName, model, schema, required?) {
    return [
      {
        validator: (_, value) => {
          if (!value && required) {
            return Promise.reject(`Vui lòng nhập${schema.label}`);
          }
          return new Promise<void>((resolve, reject) => {
            duplicateCheck({
              tableName,
              fieldName,
              fieldVal: value,
              dataId: model.id,
            })
              .then((res) => {
                res.success ? resolve() : reject(res.message || 'Kiểm tra thất bại');
              })
              .catch((err) => {
                reject(err.message || 'Kiểm tra thất bại');
              });
          });
        },
      },
    ] as ArrayRule;
  },
};

//update-begin---author:wangshuai ---date:2026-06-29  for: 【QQYUN-16619]Hàm tiện ích kiểm tra độ mạnh mật khẩu theo tiêu chuẩn bảo mật cấp 3-----------
/**
 * Kiểm tra mật khẩu có tồn tại không3ký tự trở lên là các số hoặc chữ cái tăng liên tiếp
 */
function hasConsecutiveChars(password: string): boolean {
  const arr = password.split('');
  for (let i = 1; i < arr.length - 1; i++) {
    const first = arr[i - 1].charCodeAt(0);
    const second = arr[i].charCodeAt(0);
    const third = arr[i + 1].charCodeAt(0);
    if ((third - second === 1) && (second - first === 1)) {
      return true;
    }
  }
  return false;
}

/**
 * Kiểm tra mật khẩu đơn giản:8ký tự trở lên, chứa chữ cái+Số+Ký tự đặc biệt
 */
export function validateSimplePassword(value: string, oldPassword?: string) {
  if (!value) {
    return Promise.resolve();
  }
  if (oldPassword && value === oldPassword) {
    return Promise.reject('Không được dùng mật khẩu hệ thống làm mật khẩu mới!');
  }
  const reg = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/;
  if (!reg.test(value)) {
    return Promise.reject('Mật khẩu do 8 ký tự trở lên, gồm số, chữ hoa, chữ thường và ký tự đặc biệt!');
  }
  return Promise.resolve();
}

/**
 * Kiểm tra mật khẩu mạnh bảo mật cấp 3:8ký tự trở lên, phải chứa số+Chữ hoa+Chữ thường+Ký tự đặc biệt, không cho phép3ký tự liên tiếp/Ký tự trùng lặp
 */
export function validateStrongPassword(value: string, oldPassword?: string) {
  if (!value) {
    return Promise.resolve();
  }
  if (oldPassword && value === oldPassword) {
    return Promise.reject('Không được dùng mật khẩu hệ thống làm mật khẩu mới!');
  }
  if (value.length < 8) {
    return Promise.reject('Độ dài mật khẩu không được ít hơn8ký tự');
  }
  const hasNumber = /[0-9]/.test(value);
  const hasLowercase = /[a-z]/.test(value);
  const hasUppercase = /[A-Z]/.test(value);
  const hasSpecialChar = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?~`]/.test(value);
  if (!(hasNumber && hasLowercase && hasUppercase && hasSpecialChar)) {
    return Promise.reject('Mật khẩu phải chứa số, chữ hoa, chữ thường và ký tự đặc biệt');
  }
  if (hasConsecutiveChars(value)) {
    return Promise.reject('Mật khẩu không được xuất hiện3ký tự trở lên là các số hoặc chữ cái liên tiếp');
  }
  if (/(.)\1{2,}/.test(value)) {
    return Promise.reject('Mật khẩu không được xuất hiện ký tự giống nhau liên tiếp3ký tự trở lên');
  }
  return Promise.resolve();
}

/**
 * Cache giá trị công tắc mật khẩu bảo mật cấp 3 (chỉ từ API Lấy một lần)
 */
let _enableStrongPwdCache: boolean | null = null;

async function fetchEnableStrongPwd(): Promise<boolean> {
  if (_enableStrongPwdCache !== null) {
    return _enableStrongPwdCache;
  }
  try {
    const data = await defHttp.get({ url: '/sys/user/getUserInfo' });
    _enableStrongPwdCache = data?.enableStrongPwd ?? false;
  } catch {
    _enableStrongPwdCache = false;
  }
  return _enableStrongPwdCache;
}

/**
 * Tạo kiểm tra mật khẩu theo công tắc bảo mật cấp 3 validator(Từ API Lấy cấu hình và cache)
 * @param oldPassword Mật khẩu cũ (tùy chọn, dùng để cấm trùng với mật khẩu cũ)
 */
export function createPasswordValidator(oldPassword?: string) {
  return async (_: any, value: string) => {
    const enableStrongPwd = await fetchEnableStrongPwd();
    if (enableStrongPwd) {
      return validateStrongPassword(value, oldPassword);
    }
    return validateSimplePassword(value, oldPassword);
  };
}
//update-end---author:wangshuai ---date:2026-06-29  for: 【QQYUN-16619]Hàm tiện ích kiểm tra độ mạnh mật khẩu theo tiêu chuẩn bảo mật cấp 3-----------

/**
 * Hàm kiểm tra duy nhất, cho native<a-form>Sử dụng,vbenkhuyến nghị kiểm tra biểu mẫu dùng như trênrules
 * @param tableName Tên bảng
 * @param fieldName Tên trường
 * @param fieldVal Giá trị trường
 * @param dataId Dữ liệuID
 */
export async function duplicateValidate(tableName, fieldName, fieldVal, dataId) {
  try {
    let params = {
      tableName,
      fieldName,
      fieldVal,
      dataId: dataId,
    };
    const res = await duplicateCheck(params);
    if (res.success) {
      return Promise.resolve();
    } else {
      return Promise.reject(res.message || 'Kiểm tra thất bại');
    }
  } catch (e) {
    return Promise.reject('Kiểm tra thất bại,Có thể là kiểm tra thất bại do mất mạng và các vấn đề khác');
  }
}
