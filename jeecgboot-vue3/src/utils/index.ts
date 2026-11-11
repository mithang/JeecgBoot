import type { RouteLocationNormalized, RouteRecordNormalized } from 'vue-router';
import type { App, Plugin } from 'vue';
import type { FormSchema, FormActionType } from "@/components/Form";

import { unref } from 'vue';
import { isObject, isFunction, isString } from '/@/utils/is';
import { dynamicPages } from './dynamicPages';
import Big from 'big.js';
import dayjs from "dayjs";
// Giải thích logic mã: 【VUEN-656】Cấu hình URL bên ngoài không mở được, nguyên nhân là do có ký tự #, cần thay thế
export const URL_HASH_TAB = `__AGWE4H__HASH__TAG__PWHRG__`;

export const noop = () => {};

/**
 * @description:  Set ui mount node
 */
export function getPopupContainer(node?: HTMLElement): HTMLElement {
  return (node?.parentNode as HTMLElement) ?? document.body;
}

/**
 * Add the object as a parameter to the URL
 * @param baseUrl url
 * @param obj
 * @returns {string}
 * eg:
 *  let obj = {a: '3', b: '4'}
 *  setObjToUrlParams('www.baidu.com', obj)
 *  ==>www.baidu.com?a=3&b=4
 */
export function setObjToUrlParams(baseUrl: string, obj: any): string {
  let parameters = '';
  for (const key in obj) {
    parameters += key + '=' + encodeURIComponent(obj[key]) + '&';
  }
  parameters = parameters.replace(/&$/, '');
  return /\?$/.test(baseUrl) ? baseUrl + parameters : baseUrl.replace(/\/?$/, '?') + parameters;
}

export function deepMerge<T = any>(src: any = {}, target: any = {}): T {
  let key: string;
  for (key in target) {
    // Giải thích logic mã: 【QQYUN-7872】Tối ưu label dài của form online
    if (isObject(src[key]) && isObject(target[key])) {
      src[key] = deepMerge(src[key], target[key]);
    } else {
      src[key] = target[key];
    }
  }
  return src;
}

export function openWindow(url: string, opt?: { target?: TargetContext | string; noopener?: boolean; noreferrer?: boolean }) {
  const { target = '__blank', noopener = true, noreferrer = true } = opt || {};
  const feature: string[] = [];

  noopener && feature.push('noopener=yes');
  noreferrer && feature.push('noreferrer=yes');

  window.open(url, target, feature.join(','));
}

// dynamic use hook props
export function getDynamicProps<T, U>(props: T): Partial<U> {
  const ret: Recordable = {};

  // @ts-ignore
  Object.keys(props).map((key) => {
    ret[key] = unref((props as Recordable)[key]);
  });

  return ret as Partial<U>;
}

/**
 * Lấy kiểu dữ liệu của giá trị trường biểu mẫu
 * @param props
 * @param field
 * @updateBy:zyf
 */
export function getValueType(props, field) {
  let formSchema = unref(unref(props)?.schemas)
  let valueType = 'string';
  if (formSchema) {
    let schema = formSchema.filter((item) => item.field === field)[0];
    // Giải thích logic mã: 【issues/8976】Trong truy vấn useListPage, khi componentProps là hàm thì không lấy được valueType
    if (schema && schema.componentProps) {
      if (isFunction(schema.componentProps)) {
        try {
          const result = schema.componentProps({ schema, tableAction: {}, formModel: {}, formActionType: {} });
          valueType = result?.valueType ?? valueType;
        } catch (err) {}
      } else {
        valueType = schema.componentProps.valueType ? schema.componentProps.valueType : valueType;
      }
    }
  }
  return valueType;
}

/**
 * Lấy kiểu dữ liệu của giá trị trường biểu mẫu
 * @param schema
 * @param formAction
 */
export function getValueTypeBySchema(schema: FormSchema, formAction: FormActionType) {
  let valueType = 'string';
  if (schema) {
    const componentProps = formAction.getSchemaComponentProps(schema);
    // Giải thích logic mã: 【issues/8738】khi componentProps là hàm thì không lấy được valueType
    if (isFunction(componentProps)) {
      try {
        const result = componentProps({ schema, tableAction: {}, formModel: {}, formActionType: {} });
        valueType = result?.valueType ?? valueType;
      } catch (err) {}
    } else {
      valueType = componentProps?.valueType ? componentProps?.valueType : valueType;
    }
  }
  return valueType;
}

/**
 * Lấy dữ liệu ngày qua thuộc tính picker
 * @param data
 * @param picker
 */
export function getDateByPicker(data, picker) {
  if (!data || !picker) {
    return data;
  }
  const date = dayjs(data);
  if (!date.isValid()) {
    return null;
  }
  /**
   * Cần đặt năm, năm-tháng thành ngày đầu tiên trong khoảng thời gian này ([năm-quý] không cần xử lý vì antd trả về chính là ngày đầu tiên của quý đó, [năm-tuần] cũng không xử lý)
   * Ví dụ định dạng ngày là năm, thời gian truyền cho cơ sở dữ liệu phải là 20240101
   * Ví dụ định dạng ngày là năm-tháng (đã chọn 202502), thời gian truyền cho cơ sở dữ liệu phải là 20250201
   */
  if (picker === 'year') {
    return dayjs(data).set('month', 0).set('date', 1).format('YYYY-MM-DD');
  } else if (picker === 'month') {
    return dayjs(data).set('date', 1).format('YYYY-MM-DD');
  } else if (picker === 'week') {
    return dayjs(data).startOf('week').format('YYYY-MM-DD');
  }
  return data;
}

export function getRawRoute(route: RouteLocationNormalized): RouteLocationNormalized {
  if (!route) return route;
  const { matched, ...opt } = route;
  return {
    ...opt,
    matched: (matched
      ? matched.map((item) => ({
          meta: item.meta,
          name: item.name,
          path: item.path,
        }))
      : undefined) as RouteRecordNormalized[],
  };
}
/**
 * Sao chép sâu (deep clone) đối tượng, mảng
 * @param obj Đối tượng được sao chép
 * @return Đối tượng sau khi sao chép
 */
export function cloneObject(obj) {
  return JSON.parse(JSON.stringify(obj));
}

export const withInstall = <T>(component: T, alias?: string) => {
  //console.log("---Khởi tạo---", component)

  const comp = component as any;
  comp.install = (app: App) => {
    // @ts-ignore
    app.component(comp.name || comp.displayName, component);
    if (alias) {
      app.config.globalProperties[alias] = component;
    }
  };
  return component as T & Plugin;
};

/**
 * Lấy tham số địa chỉ URL
 * @param paraName
 */
export function getUrlParam(paraName) {
  let url = document.location.toString();
  let arrObj = url.split('?');

  if (arrObj.length > 1) {
    let arrPara = arrObj[1].split('&');
    let arr;

    for (let i = 0; i < arrPara.length; i++) {
      arr = arrPara[i].split('=');

      if (arr != null && arr[0] == paraName) {
        return arr[1];
      }
    }
    return '';
  } else {
    return '';
  }
}

/**
 * Tạm dừng (phiên bản promise của setTimeout)
 * @param ms Thời gian tạm dừng, đơn vị: mili giây
 * @param fn callback, có thể để trống
 * @return Promise
 */
export function sleep(ms: number, fn?: Fn) {
  return new Promise<void>((resolve) =>
    setTimeout(() => {
      fn && fn();
      resolve();
    }, ms)
  );
}

/**
 * Thay thế tất cả giá trị mà không dùng regex
 * @param text Chuỗi bị thay thế
 * @param checker  Nội dung trước khi thay thế
 * @param replacer Nội dung sau khi thay thế
 * @returns {String} Chuỗi sau khi thay thế
 */
export function replaceAll(text, checker, replacer) {
  let lastText = text;
  text = text.replace(checker, replacer);
  if (lastText !== text) {
    return replaceAll(text, checker, replacer);
  }
  return text;
}

/**
 * Lấy tham số trên URL
 * @param url
 */
export function getQueryVariable(url) {
  if (!url) return;

  var t,
    n,
    r,
    i = url.split('?')[1],
    s = {};
  (t = i.split('&')), (r = null), (n = null);
  for (var o in t) {
    var u = t[o].indexOf('=');
    u !== -1 && ((r = t[o].substr(0, u)), (n = t[o].substr(u + 1)), (s[r] = n));
  }
  return s;
}
/**
 * Xác định có hiển thị nút xử lý hay không
 * @param bpmStatus
 * @returns {*}
 */
export function showDealBtn(bpmStatus) {
  if (bpmStatus != '1' && bpmStatus != '3' && bpmStatus != '4') {
    return true;
  }
  return false;
}
/**
 * Chuyển đổi số thành chữ số viết hoa
 * @param value
 * @returns {*}
 */
export function numToUpper(value) {
  if (value != '') {
    let unit = new Array('仟', '佰', '拾', '', '仟', '佰', '拾', '', '角', '分');
    const toDx = (n) => {
      switch (n) {
        case '0':
          return '零';
        case '1':
          return '壹';
        case '2':
          return '贰';
        case '3':
          return '叁';
        case '4':
          return '肆';
        case '5':
          return '伍';
        case '6':
          return '陆';
        case '7':
          return '柒';
        case '8':
          return '捌';
        case '9':
          return '玖';
      }
    };
    let lth = value.toString().length;
    // Giải thích logic mã: 【issues/7493】Khắc phục lỗi trả về của phương thức numToUpper
    value = new Big(value).times(100);
    value += '';
    let length = value.length;
    if (lth <= 8) {
      let result = '';
      for (let i = 0; i < length; i++) {
        if (i == 2) {
          result = '元' + result;
        } else if (i == 6) {
          result = '万' + result;
        }
        if (value.charAt(length - i - 1) == 0) {
          if (i != 0 && i != 1) {
            if (result.charAt(0) != '零' && result.charAt(0) != '元' && result.charAt(0) != '万') {
              result = '零' + result;
            }
          }
          continue;
        }
        result = toDx(value.charAt(length - i - 1)) + unit[unit.length - i - 1] + result;
      }
      result += result.charAt(result.length - 1) == '元' ? '整' : '';
      return result;
    } else {
      return null;
    }
  }
  return null;
}

// Giải thích logic mã: Giải quyết vấn đề cú pháp import động file của vue2 cũ mà vite không hỗ trợ
export function importViewsFile(path): Promise<any> {
  if (path.startsWith('/')) {
    path = path.substring(1);
  }
  let page = '';
  if (path.endsWith('.vue')) {
    page = `../views/${path}`;
  } else {
    page = `../views/${path}.vue`;
  }
  return new Promise((resolve, reject) => {
    let flag = true;
    for (const path in dynamicPages) {
      if (path == page) {
        flag = false;
        dynamicPages[path]().then((mod) => {
          console.log(path, mod);
          resolve(mod);
        });
      }
    }
    if (flag) {
      reject('Tệp này không tồn tại:' + page);
    }
  });
}


/**
 * Chuyển đến trang xem trước của báo cáo Jimu
 * @param url
 * @param id
 * @param token
 */
export function goJmReportViewPage(url, id, token) {
  // URL hỗ trợ biến giữ chỗ {{ window.xxx }}
  url = url.replace(/{{([^}]+)?}}/g, (_s1, s2) => _eval(s2))
  if (url.includes('?')) {
    url += '&'
  } else {
    url += '?'
  }
  url += `id=${id}`
  url += `&token=${token}`
  window.open(url)
}

/**
 * Lấy màu ngẫu nhiên
 */
export function getRandomColor(index?) {

  const colors = [
    'rgb(100, 181, 246)',
    'rgb(77, 182, 172)',
    'rgb(255, 183, 77)',
    'rgb(229, 115, 115)',
    'rgb(149, 117, 205)',
    'rgb(161, 136, 127)',
    'rgb(144, 164, 174)',
    'rgb(77, 208, 225)',
    'rgb(129, 199, 132)',
    'rgb(255, 138, 101)',
    'rgb(133, 202, 205)',
    'rgb(167, 214, 118)',
    'rgb(254, 225, 89)',
    'rgb(251, 199, 142)',
    'rgb(239, 145, 139)',
    'rgb(169, 181, 255)',
    'rgb(231, 218, 202)',
    'rgb(252, 128, 58)',
    'rgb(254, 161, 172)',
    'rgb(194, 163, 205)',
  ];
  return index && index < 19 ? colors[index] : colors[Math.floor((Math.random()*(colors.length-1)))];
}

export function getRefPromise(componentRef) {
  return new Promise((resolve) => {
    (function next() {
      const ref = componentRef.value;
      if (ref) {
        resolve(ref);
      } else {
        setTimeout(() => {
          next();
        }, 100);
      }
    })();
  });
}

/**
 * 2023-09-04
 * liaozhiyang
 * Dùng new Function thay cho eval
 */
export function _eval(str: string) {
 return new Function(`return ${str}`)();
}

/**
 * 2024-04-30
 * liaozhiyang
 * Lấy năm, tháng, tuần, quý tương ứng của antd qua thời gian hoặc timestamp.
 */
export function getWeekMonthQuarterYear(date) {
  // update-begin--author:liaozhiyang---date:20260713---for: 【LHZP-15】Form nhập tuần 26 năm 2026, danh sách hiển thị tuần 25 năm 2026
  // Lấy số tuần ISO 8601 và năm tương ứng của tuần đó
  const getISOWeek = (date) => {
    // Chuẩn hóa về 0 giờ trong ngày, tránh nhiễu giờ/phút/giây
    const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    // ISO quy định thứ Hai là ngày bắt đầu tuần: Thứ Hai=0 ... Chủ Nhật=6
    const dayNr = (target.getDay() + 6) % 7;
    // Di chuyển đến thứ Năm của tuần này (thứ Năm quyết định năm của tuần đó)
    target.setDate(target.getDate() - dayNr + 3);
    const isoYear = target.getFullYear();
    // Thứ Năm của tuần chứa thứ Năm đầu tiên của năm ISO đó
    const firstThursday = new Date(isoYear, 0, 4);
    const firstDayNr = (firstThursday.getDay() + 6) % 7;
    firstThursday.setDate(firstThursday.getDate() - firstDayNr + 3);
    const oneWeek = 7 * 86400000; // Số mili giây của một tuần
    const week = 1 + Math.round((target.getTime() - firstThursday.getTime()) / oneWeek);
    return { week, isoYear };
  };
  // Chuyển timestamp thành đối tượng ngày
  const dateObj = new Date(date);
  // Tính tuần
  const { week, isoYear } = getISOWeek(dateObj);
  // update-end--author:liaozhiyang---date:20260713---for: 【LHZP-15】Form nhập tuần 26 năm 2026, danh sách hiển thị tuần 25 năm 2026
  // Tính tháng
  const month = dateObj.getMonth() + 1; // Tháng bắt đầu từ 0 nên phải cộng 1
  // Tính quý
  const quarter = Math.floor(dateObj.getMonth() / 3) + 1;
  // Tính năm
  const year = dateObj.getFullYear();
  return {
    year: `${year}`,
    month: `${year}-${month.toString().padStart(2, '0')}`,
    week: `${isoYear}-${week}tuần`,
    quarter: `${year}-Q${quarter}`,
  };
}

/**
 * 2024-05-17
 * liaozhiyang
 * Có thể có nhiều phần tử modal được mount, cần tìm phần tử tương ứng.
 */
export const setPopContainer = (node, selector) => {
  if (typeof selector === 'string') {
    const targetEles = Array.from(document.querySelectorAll(selector));
    if (targetEles.length > 1) {
      const retrospect = (node, elems) => {
        let ele = node.parentNode;
        while (ele) {
          const findParentNode = elems.find(item => item === ele);
          if (findParentNode) {
            ele = null;
            return findParentNode;
          } else {
            ele = ele.parentNode;
          }
        }
        return null;
      };
      const elem = retrospect(node, targetEles);
      if (elem) {
        return elem;
      } else {
        return document.querySelector(selector);
      }
    } else {
      return document.querySelector(selector);
    }
  } else {
    return selector;
  }
};

/**
 * 2024-06-14
 * liaozhiyang
 * Theo điều kiện hiển thị của control
 * label, value dùng chung; title, val dùng cho quản lý quyền hạn
 */
export function useConditionFilter() {

  // Điều kiện chung
  const commonConditionOptions = [
    {label: 'Trống', value: 'empty', val: 'EMPTY'},
    {label: 'Không trống', value: 'not_empty', val: 'NOT_EMPTY'},
  ]

  // Số
  const numberConditionOptions = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    { label: 'Lớn hơn', value: 'gt', val: '>' },
    { label: 'Lớn hơn hoặc bằng', value: 'ge', val: '>=' },
    { label: 'Nhỏ hơn', value: 'lt', val: '<' },
    { label: 'Nhỏ hơn hoặc bằng', value: 'le', val: '<=' },
    ...commonConditionOptions,
  ];

  // Ngày, ngày giờ, giờ: không hỗ trợ "Trong..." (bao gồm)
  const dateConditionOptions = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    { label: 'Lớn hơn', value: 'gt', val: '>' },
    { label: 'Lớn hơn hoặc bằng', value: 'ge', val: '>=' },
    { label: 'Nhỏ hơn', value: 'lt', val: '<' },
    { label: 'Nhỏ hơn hoặc bằng', value: 'le', val: '<=' },
    ...commonConditionOptions,
  ];

  // Văn bản, văn bản nhiều dòng
  const inputConditionOptions = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Mờ', value: 'like', val: 'LIKE' },
    { label: 'Bắt đầu bằng..', value: 'right_like', title: 'Mờ phải', val: 'RIGHT_LIKE' },
    { label: 'Kết thúc bằng..', value: 'left_like', title: 'Mờ trái', val: 'LEFT_LIKE' },
    { label: 'Trong...', value: 'in', val: 'IN', title: 'Bao gồm' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    ...commonConditionOptions,
  ];
  // -update-begin--author:liaozhiyang---date:20260810---for: 【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao
  // Control chọn đơn trị: dropdown, tỉnh/thành/quận huyện, radio, switch, tìm kiếm dropdown, từ điển phân loại, cây tùy chỉnh
  const singleSelectConditionOptions = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    ...commonConditionOptions,
  ];
  // -update-end--author:liaozhiyang---date:20260810---for: 【LHZP-523】Điều chỉnh điều kiện truy vấn của component truy vấn nâng cao

  // Control chọn hỗ trợ nhiều giá trị: chọn nhiều, người dùng, phòng ban, bản ghi liên kết, popup, popupDict, dropdown nhiều lựa chọn
  const multiSelectConditionOptions = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Trong...', value: 'in', val: 'IN', title: 'Bao gồm' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    ...commonConditionOptions,
  ];

  const def = [
    { label: 'Bằng', value: 'eq', val: '=' },
    { label: 'Mờ', value: 'like', val: 'LIKE' },
    { label: 'Bắt đầu bằng..', value: 'right_like', title: 'Mờ phải', val: 'RIGHT_LIKE' },
    { label: 'Kết thúc bằng..', value: 'left_like', title: 'Mờ trái', val: 'LEFT_LIKE' },
    { label: 'Trong...', value: 'in', val: 'IN', title: 'Bao gồm' },
    { label: 'Không bằng', value: 'ne', val: '!=' },
    { label: 'Lớn hơn', value: 'gt', val: '>' },
    { label: 'Lớn hơn hoặc bằng', value: 'ge', val: '>=' },
    { label: 'Nhỏ hơn', value: 'lt', val: '<' },
    { label: 'Nhỏ hơn hoặc bằng', value: 'le', val: '<=' },
    ...commonConditionOptions,
  ];

  const filterCondition = (data) => {
    if (data.view == 'text' && data.fieldType == 'number') {
      data.view = 'number';
    }
    switch (data.view) {
      case 'file':
      case 'image':
      case 'umeditor':
      case 'markdown':
      case 'password':
        return commonConditionOptions;
      case 'text':
      case 'textarea':
        return inputConditionOptions;
      case 'checkbox':
      case 'sel_user':
      case 'sel_depart':
      case 'link_table':
      case 'popup_dict':
      case 'popup':
      case 'list_multi':
        return multiSelectConditionOptions;
      case 'list':
      case 'radio':
      case 'switch':
      case 'pca':
      case 'sel_search':
      case 'cat_tree':
      case 'sel_tree':
        return singleSelectConditionOptions;
      case 'date':
      case 'datetime':
      case 'time':
        return dateConditionOptions;
      // number là ảo
      case 'number':
        return numberConditionOptions;
      default:
        return def;
    }
  };
  return { filterCondition };
}
// Lấy tham số trong URL
export const getUrlParams = (url) => {
  const result = {
    url: '',
    params: {},
  };
  const list = url.split('?');
  result.url = list[0];
  const params = list[1];
  if (params) {
    const list = params.split('&');
    list.forEach((ele) => {
      const dic = ele.split('=');
      const label = dic[0];
      result.params[label] = dic[1];
    });
  }
  return result;
};

/* 20250325
 * liaozhiyang
 * Tách chuỗi URL thành mảng
 * 【issues/7990】Tham số hình ảnh chứa dấu phẩy sẽ bị nhận dạng nhầm thành nhiều ảnh
 * */
export const split = (str) => {
  if (isString(str)) {
    const text = str.trim();
    if (text.startsWith('http')) {
      const parts = str.split(',');
      const urls: any = [];
      let currentUrl = '';
      for (const part of parts) {
        if (part.startsWith('http://') || part.startsWith('https://')) {
          // Nếu gặp phần bắt đầu URL mới, lưu URL hiện tại và bắt đầu URL mới
          if (currentUrl) {
            urls.push(currentUrl);
          }
          currentUrl = part;
        } else {
          // Ngược lại, là một phần của URL hiện tại (ví dụ tham số)
          currentUrl += ',' + part;
        }
      }
      // Thêm URL cuối cùng
      if (currentUrl) {
        urls.push(currentUrl);
      }
      return urls;
    } else {
      return str.split(',');
    }
  }
  return str;
};
/**
 * Giải mã thực thể HTML (ví dụ &lt;thead&gt; → <thead>), dùng để sửa nội dung định dạng hỗn hợp
 * update-begin--author:liaozhiyang---date:20260807---for:【LHZP-1128】Khắc phục thẻ thead không được phân tích
 */
export const decodeHtmlEntities = (html: string): string => {
  if (!html || !/&(?:#x?[0-9a-f]+|[a-z]+);/i.test(html)) {
    return html;
  }
  const textarea = document.createElement('textarea');
  textarea.innerHTML = html;
  return textarea.value;
};
// update-end--author:liaozhiyang---date:20260807---for:【LHZP-1128】Khắc phục thẻ thead không được phân tích

/**
  * Xử lý thẻ o:p trong tài liệu Word
  * @param html
 */
export const removeSpecialTags = (html: string): string => {
  if (!html) return '';
  try {
    const BORDER = '1px solid #8c8c8c';

    // ===================================================================
    // Bước 1: Loại bỏ các thẻ rác của Office (chuỗi thuần)
    // ===================================================================
    // Thẻ o:p (dạng escaped và dạng thường)
    html = html.replace(/&lt;o:p[^&]*?&gt;.*?&lt;\/o:p&gt;/gis, '');
    html = html.replace(/&lt;o:p[^&]*?\/?&gt;/gis, '');
    html = html.replace(/&lt;\/o:p&gt;/gis, '');
    html = html.replace(/<o:p[^>]*>.*?<\/o:p>/gis, '');
    html = html.replace(/<o:p[^>]*\/?>/gis, '');
    html = html.replace(/<\/o:p>/gis, '');

    // Thẻ style (dạng escaped và dạng thường)
    html = html.replace(/&lt;style[^&]*?&gt;.*?&lt;\/style&gt;/gis, '');
    html = html.replace(/&lt;style[^&]*?\/?&gt;/gis, '');
    html = html.replace(/&lt;\/style&gt;/gis, '');
    html = html.replace(/<style[^>]*>.*?<\/style>/gis, '');

    // Chú thích điều kiện và thẻ namespace Office
    html = html.replace(/<!--\[if[^>]*>.*?<!\[endif\]-->/gis, '');
    html = html.replace(/<\/?w:[^>]*>/gis, '');
    html = html.replace(/<\/?xml[^>]*>/gis, '');
    html = html.replace(/<\/?v:[^>]*>/gis, '');

    // ===================================================================
    // Bước 2: Dọn dẹp DOM (chỉ dọn đoạn trống, hoàn toàn không động đến cấu trúc hàng của bảng)
    //   ★ Quan trọng: đặt trước khi xử lý viền, tránh mất !important khi serialize DOM
    // ===================================================================
    try {
      const DOMParserCtor: any =
          typeof DOMParser !== 'undefined' ? DOMParser : (globalThis as any).DOMParser;
      if (DOMParserCtor) {
        const parser = new DOMParserCtor() as DOMParser;
        const doc = parser.parseFromString(html, 'text/html') as Document;

        // Chỉ dọn các thẻ <p> trống trong ô
        const cells = doc.querySelectorAll('td,th');
        for (let ci = 0; ci < cells.length; ci++) {
          const cell = cells[ci] as HTMLElement;
          if (!cell) continue;
          const ps = cell.querySelectorAll('p');
          for (let pi = ps.length - 1; pi >= 0; pi--) {
            const p = ps[pi] as HTMLParagraphElement;
            if (!p) continue;
            const content = (p.innerHTML || '').replace(/&nbsp;|\u00A0/g, '').trim();
            if (content === '') {
              p.remove();
            } else {
              const frag = doc.createDocumentFragment();
              while (p.firstChild) frag.appendChild(p.firstChild);
              if (p.parentNode) p.parentNode.replaceChild(frag, p);
            }
          }
        }

        html = doc.body.innerHTML;
      }
    } catch (_) {
      // DOM không khả dụng, bỏ qua dọn đoạn (không ảnh hưởng sửa viền)
    }

    // ===================================================================
    // Bước 3: Chèn style cho bảng <style> (bảo đảm dự phòng)
    //   ★ Đặt sau khi serialize DOM, đảm bảo thẻ <style> không bị DOM ghi đè
    //   ★ Selector phải giới hạn trong .jeecg-rich-content: <style> trong v-html sẽ vào document và có hiệu lực toàn cục,
    //     nếu viết selector table/img trần sẽ làm ô nhiễm các style bảng khác trên trang như Ant Design Table.
    // ===================================================================
    const RICH_CONTENT_CLASS = 'jeecg-rich-content';
    const scopedCSS = `<style>
.${RICH_CONTENT_CLASS} table{border-collapse:collapse!important;border-spacing:0!important;width:100%!important;max-width:100%!important;}
.${RICH_CONTENT_CLASS} table td,.${RICH_CONTENT_CLASS} table th{border-top:${BORDER}!important;border-right:${BORDER}!important;border-bottom:${BORDER}!important;border-left:${BORDER}!important;padding:6px 8px!important;vertical-align:middle!important;box-sizing:border-box!important;empty-cells:show!important;}
.${RICH_CONTENT_CLASS} table td p,.${RICH_CONTENT_CLASS} table th p{margin:0!important;padding:0!important;line-height:1.4!important;}
.${RICH_CONTENT_CLASS} img{max-width:100%!important;height:auto!important;display:block!important;}
</style>`;
    // update-begin--author:liaozhiyang---date:20260803---for:【LHZP-1239】Trang "Tin nhắn của tôi" bấm nút xem dữ liệu thông báo thì style bảng của trang bị thay đổi
    // Dùng container có phạm vi bao nội dung chính, đảm bảo selector trên chỉ áp dụng trong rich text
    html = `${scopedCSS}<div class="${RICH_CONTENT_CLASS}">${html}</div>`;
    // update-end--author:liaozhiyang---date:20260803---for:【LHZP-1239】Trang "Tin nhắn của tôi" bấm nút xem dữ liệu thông báo thì style bảng của trang bị thay đổi
    // ===================================================================
    // Bước 4: Xử lý inline style bằng chuỗi thuần (sửa viền cốt lõi)
    //   ★ Thực hiện cuối cùng! Đảm bảo !important trong HTML đầu ra không bị mất bởi bất kỳ xử lý nào sau đó
    //
    //   Chiến lược: tách giá trị style theo dấu chấm phẩy, xét từng thuộc tính,
    //         lọc bỏ mọi khai báo border* và mso-*,
    //         sau đó thêm khai báo viền thống nhất (kèm !important)
    // ===================================================================

    /**
     * Loại bỏ mọi khai báo border* và mso-* khỏi chuỗi CSS style,
     * giữ lại các thuộc tính liên quan đến bố cục khác (width, height, padding, text-align, v.v.)
     */
    function stripBorderAndMso(styleStr: string): string {
      return styleStr
          .split(';')
          .filter(function (decl) {
            const trimmed = decl.trim();
            if (!trimmed) return false;
            const colonIdx = trimmed.indexOf(':');
            if (colonIdx === -1) return true; // Giữ lại khai báo không hợp lệ (để an toàn)
            const prop = trimmed.substring(0, colonIdx).trim().toLowerCase();
            // Loại bỏ mọi thuộc tính bắt đầu bằng border và mso-
            if (prop.startsWith('border') || prop.startsWith('mso-')) return false;
            return true;
          })
          .join(';');
    }

    // 4a. Loại bỏ thuộc tính HTML border="0" trên <table>
    html = html.replace(/(<table\b[^>]*?)\sborder\s*=\s*['"]?0['"]?/gi, '$1');

    // 4b. Xử lý inline style của thẻ <table>
    html = html.replace(/<table(\s[^>]*)>/gi, function (fullMatch, attrs) {
      try {
        const sm = attrs.match(/\sstyle\s*=\s*(["'])([\s\S]*?)\1/i);
        if (sm) {
          const q = sm[1];
          const cleaned = stripBorderAndMso(sm[2]);
          const newStyle = cleaned
              + ';border-collapse:collapse!important'
              + ';border-spacing:0!important'
              + ';width:100%!important';
          return '<table' + attrs.replace(sm[0], ` style=${q}${newStyle}${q}`) + '>';
        }
        return '<table' + attrs
            + ' style="border-collapse:collapse!important;border-spacing:0!important;width:100%!important">';
      } catch (_) { return fullMatch; }
    });

    // 4c. Xử lý inline style của thẻ <td>/<th>
    const cellBorderCSS =
        `border-top:${BORDER}!important;` +
        `border-right:${BORDER}!important;` +
        `border-bottom:${BORDER}!important;` +
        `border-left:${BORDER}!important;` +
        'padding:6px 8px!important;' +
        'vertical-align:middle!important;' +
        'box-sizing:border-box!important';

    html = html.replace(/<(td|th)(\s[^>]*)>/gi, function (fullMatch, tag, attrs) {
      try {
        const sm = attrs.match(/\sstyle\s*=\s*(["'])([\s\S]*?)\1/i);
        if (sm) {
          const q = sm[1];
          const cleaned = stripBorderAndMso(sm[2]);
          const newStyle = (cleaned ? cleaned + ';' : '') + cellBorderCSS;
          return '<' + tag + attrs.replace(sm[0], ` style=${q}${newStyle}${q}`) + '>';
        }
        return '<' + tag + attrs + ` style="${cellBorderCSS}">`;
      } catch (_) { return fullMatch; }
    });

    return html;
  } catch (_) {
    return html;
  }
};

