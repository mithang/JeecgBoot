import { useGlobSetting } from '/@/hooks/setting';
import { merge, random } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { FormSchema } from '/@/components/Form';
import { h, reactive, ref } from "vue";
import { getTenantId, getToken } from "/@/utils/auth";
import { useUserStoreWithOut } from "/@/store/modules/user";
import dayjs from 'dayjs';
import Big from 'big.js';

import { Modal } from "ant-design-vue";
import { defHttp } from "@/utils/http/axios";
import { useI18n } from "@/hooks/web/useI18n";
import {$electron} from "@/electron";
import {router} from "@/router";
import {encryptByBase64} from "@/utils/cipher";
//Mảng lưu đường dẫn phòng ban
const departNamePath = ref<Record<string, string>>({});
// Tên đường dẫn phòng ban đang yêu cầu, tránh gọi trùng đồng thời cùng tham số
const departPathNameInflight = new Map<string, Promise<any>>();

/**
 * Sau khi thay đổi cấp tổ chức, xóa cache đường dẫn phòng ban
 */
export function clearDepartPathNameCache() {
  departNamePath.value = {};
  departPathNameInflight.clear();
}

const globSetting = useGlobSetting();
const baseApiUrl = globSetting.domainUrl;
/**
 *  Lấy đường dẫn truy cập dịch vụ tệp
 * @param fileUrl Đường dẫn tệp
 * @param prefix(Mặc địnhhttp)  Tiền tố đường dẫn tệp http/https
 */
export const getFileAccessHttpUrl = (fileUrl, prefix = 'http') => {
  let result = fileUrl;
  try {
    if (fileUrl && fileUrl.length > 0 && !fileUrl.startsWith(prefix)) {
      //Kiểm tra có phải định dạng mảng không
      let isArray = fileUrl.indexOf('[') != -1;
      if (!isArray) {
        let prefix = `${baseApiUrl}/sys/common/static/`;
        // Kiểm tra đã chứa tiền tố chưa
        if (!fileUrl.startsWith(prefix)) {
          result = `${prefix}${fileUrl}`;
        }
      }
    }
  } catch (err) {}
  return result;
};
/**
 *  Lấy desktopwpsđường dẫn truy cập dịch vụ tệp của
 * @param fileUrl Đường dẫn tệp
 */
export const getElectronFileUrl = (url) => {
  let fileUrl: any = url;
  if (url && $electron.isElectron()) {
    fileUrl = router.resolve({path: '/onlinePreview', query: {url: encryptByBase64(getFileAccessHttpUrl(url))}}).href;
  }
  return fileUrl;
};

/**
 * Kích hoạt window.resize
 */
export function triggerWindowResizeEvent() {
  let event: any = document.createEvent('HTMLEvents');
  event.initEvent('resize', true, true);
  event.eventType = 'message';
  window.dispatchEvent(event);
}

/**
 * Lấy số ngẫu nhiên
 *  @param length Số chữ số
 */
export const getRandom = (length: number = 1) => {
  return '-' + parseInt(String(Math.random() * 10000 + 1), length);
};

/**
 * Tạo chuỗi ngẫu nhiên
 * @param length Độ dài chuỗi
 * @param chats Khoảng chuỗi tùy chọn (chỉ tạo các ký tự trong chuỗi được truyền vào)
 * @return string Chuỗi đã tạo
 */
export function randomString(length: number, chats?: string) {
  if (!length) length = 1;
  if (!chats) {
    // noinspection SpellCheckingInspection
    chats = '0123456789qwertyuioplkjhgfdsazxcvbnm';
  }
  let str = '';
  for (let i = 0; i < length; i++) {
    let num = random(0, chats.length - 1);
    str += chats[num];
  }
  return str;
}

/**
 * Chuyển dữ liệu danh sách thông thường thànhtreeCấu trúc
 * @param array treeDữ liệu
 * @param opt  Cấu hình tham số
 * @param startPid Nút cha
 */
export const listToTree = (array, opt, startPid) => {
  const obj = {
    primaryKey: opt.primaryKey || 'key',
    parentKey: opt.parentKey || 'parentId',
    titleKey: opt.titleKey || 'title',
    startPid: opt.startPid || '',
    currentDept: opt.currentDept || 0,
    maxDept: opt.maxDept || 100,
    childKey: opt.childKey || 'children',
  };
  if (startPid) {
    obj.startPid = startPid;
  }
  return toTree(array, obj.startPid, obj.currentDept, obj);
};
/**
 *  Xây dựng đệ quytree
 * @param list
 * @param startPid
 * @param currentDept
 * @param opt
 * @returns {Array}
 */
export const toTree = (array, startPid, currentDept, opt) => {
  if (opt.maxDept < currentDept) {
    return [];
  }
  let child = [];
  if (array && array.length > 0) {
    child = array
      .map((item) => {
        // Sàng lọc dữ liệu thỏa điều kiện (khóa chính = startPid)
        if (typeof item[opt.parentKey] !== 'undefined' && item[opt.parentKey] === startPid) {
          // Thỏa điều kiện thì đệ quy
          const nextChild = toTree(array, item[opt.primaryKey], currentDept + 1, opt);
          // Lưu thông tin nút
          if (nextChild.length > 0) {
            item['isLeaf'] = false;
            item[opt.childKey] = nextChild;
          } else {
            item['isLeaf'] = true;
          }
          item['title'] = item[opt.titleKey];
          item['label'] = item[opt.titleKey];
          item['key'] = item[opt.primaryKey];
          item['value'] = item[opt.primaryKey];
          return item;
        }
      })
      .filter((item) => {
        return item !== undefined;
      });
  }
  return child;
};

/**
 * Phương thức tiện ích tổng ở dưới cùng bảng
 * @param tableData Dữ liệu bảng
 * @param fieldKeys Trường cột cần tính tổng
 */
export function mapTableTotalSummary(tableData: Recordable[], fieldKeys: string[]) {
  let totals: any = { _row: 'Tổng cộng', _index: 'Tổng cộng' };
  fieldKeys.forEach((key) => {
    totals[key] = tableData.reduce((prev, next) => {
      // Giải thích logic mã: 【QQYUN-7891】PR Phương thức tiện ích tổng cộng, chuyển thànhNuberLoại tính lại
      const value = Number(next[key]);
      if (!Number.isNaN(value)) {
        // Giải thích logic mã: 【issues/7830]Độ chính xác tính toán số thập phân tổng cộng
        prev = Big(prev).plus(value).toString();
      }
      return prev;
    }, 0);
    // Giải thích logic mã: 【issues/7830]Độ chính xác tính toán số thập phân tổng cộng
    totals[key] = +totals[key];
  });
  return totals;
}

/**
 * Triển khai đơn giản phương thức debounce
 *
 * Debounce(debounce)Hàm khi được kích hoạt lần đầu với hàm đã cho, không thực thi ngay mà đưa ra một giá trị thời hạn(delay), ví dụ100ms.
 * Nếu100msBên trong thực thi hàm lần nữa, thì bắt đầu tính giờ lại, đến khi hết giờ mới thực sự thực thi hàm.
 * Lợi ích của cách này là nếu cùng một sự kiện được kích hoạt nhiều lần trong thời gian ngắn, chỉ thực thi hàm một lần.
 *
 * @param fn Hàm cần debounce
 * @param delay Số mili giây debounce
 * @returns {Function}
 */
export function simpleDebounce(fn, delay = 100) {
  let timer: any | null = null;
  return function () {
    let args = arguments;
    if (timer) {
      clearTimeout(timer);
    }
    timer = setTimeout(() => {
      // @ts-ignore
      fn.apply(this, args);
    }, delay);
  };
}

/**
 * Định dạng ngày
 * @param date Ngày
 * @param block Định dạng chuỗi
 */
export function dateFormat(date, block) {
  if (!date) {
    return '';
  }
  let format = block || 'yyyy-MM-dd';
  date = new Date(date);
  const map = {
    M: date.getMonth() + 1, // Tháng
    d: date.getDate(), // ngày
    h: date.getHours(), // Giờ
    m: date.getMinutes(), // phút
    s: date.getSeconds(), // giây
    q: Math.floor((date.getMonth() + 3) / 3), // Quý
    S: date.getMilliseconds(), // Mili giây
  };
  format = format.replace(/([yMdhmsqS])+/g, (all, t) => {
    let v = map[t];
    if (v !== undefined) {
      if (all.length > 1) {
        v = `0${v}`;
        v = v.substr(v.length - 2);
      }
      return v;
    } else if (t === 'y') {
      return date
        .getFullYear()
        .toString()
        .substr(4 - all.length);
    }
    return all;
  });
  return format;
}

/**
 * Lấy đường dẫn nổi bọt sự kiện, tương thích IE11, Edge, Chrome, Firefox, Safari
 * Nơi đang dùng hiện tại:JVxeTable SpanChế độ
 */
export function getEventPath(event) {
  let target = event.target;
  let path = (event.composedPath && event.composedPath()) || event.path;

  if (path != null) {
    return path.indexOf(window) < 0 ? path.concat(window) : path;
  }

  if (target === window) {
    return [window];
  }

  let getParents = (node, memo) => {
    const parentNode = node.parentNode;

    if (!parentNode) {
      return memo;
    } else {
      return getParents(parentNode, memo.concat(parentNode));
    }
  };
  return [target].concat(getParents(target, []), window);
}

/**
 * Nếu giá trị không tồn tại thì push Vào mảng, ngược lại không xử lý
 * @param array Dữ liệu cần thao tác
 * @param value Giá trị cần thêm
 * @param key Có thể trống, nếu so sánh đối tượng, có thể địa chỉ khác nhau nhưng giá trị thực tế giống nhau, có thể truyền trường này để kiểm tra trường duy nhất trong đối tượng, ví dụ id. Nếu không truyền thì so sánh trực tiếp giá trị thực tế
 * @returns {boolean} Thành công push Trả về true, không xử lý trả về false
 */
export function pushIfNotExist(array, value, key?) {
  for (let item of array) {
    if (key && item[key] === value[key]) {
      return false;
    } else if (item === value) {
      return false;
    }
  }
  array.push(value);
  return true;
}
/**
 * Lọc thuộc tính trống trong đối tượng
 * @param obj
 * @returns {*}
 */
export function filterObj(obj) {
  if (!(typeof obj == 'object')) {
    return;
  }

  for (let key in obj) {
    if (obj.hasOwnProperty(key) && (obj[key] == null || obj[key] == undefined || obj[key] === '')) {
      delete obj[key];
    }
  }
  return obj;
}

/**
 * Chuyển gạch dưới sang camelCase
 * @param string
 */
export function underLine2CamelCase(string: string) {
  return string.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase());
}

/**
 * Tìm cấu trúc cây
 * @param treeList
 * @param fn Tìm phương thức
 * @param childrenKey
 */
export function findTree(treeList: any[], fn: Fn, childrenKey = 'children') {
  //update-begin---wangshuai---date:20260721  for: [LHZP-1135]Phòng ban của tôi Thông tin hiển thị bên phải không đúng, console báo lỗi------------
  if(!treeList){
    return null;
  }
  //update-end---author:wangshuai ---date:20260721  for: [LHZP-1135]Phòng ban của tôi Thông tin hiển thị bên phải không đúng, console báo lỗi------------
  for (let i = 0; i < treeList.length; i++) {
    let item = treeList[i];
    if (fn(item, i, treeList)) {
      return item;
    }
    let children = item[childrenKey];
    if (isArray(children)) {
      let findResult = findTree(children, fn, childrenKey);
      if (findResult) {
        return findResult;
      }
    }
  }
  return null;
}

/** Lấy mapFormSchema Phương thức */
export function bindMapFormSchema<T>(spanMap, spanTypeDef: T) {
  return function (s: FormSchema, spanType: T = spanTypeDef) {
    return merge(
      {
        disabledLabelWidth: true,
      } as FormSchema,
      spanMap[spanType],
      s
    );
  };
}

/**
 * Chuỗi có phải là khôngnullhoặcnullChuỗi
 * @param str
 * @return {boolean}
 */
export function stringIsNull(str) {
  // Hai == Có thể kiểm tra đồng thời null và undefined
  return str == null || str === 'null' || str === 'undefined';
}

/**
 * [Nhiều component có thể gây vấn đề hiệu năng] Lấy popupdiv, mount các component như dropdown, ngày tháng vàomodaltrên, giải quyết vấn đề popup bị che khuất
 * @param node
 */
export function getAutoScrollContainer(node: HTMLElement) {
  let element: Nullable<HTMLElement> = node
  while (element != null) {
    if (element.classList.contains('scrollbar__view')) {
      // Kiểm tra có thanh cuộn không
      if (element.clientHeight < element.scrollHeight) {
        // Khi có thanh cuộn, mount vào cấp cha, giải quyết vấn đề cuộn
        return node.parentElement
      } else {
        // Khi không có thanh cuộn, mount vàobodytrên, giải quyết vấn đề dropdown bị che khuất
        return document.body
      }
    } else {
      element = element.parentElement
    }
  }
  // Không nằm trong popup, đi theo logic mặc định
  return node.parentElement
}

/**
 * Kiểm tra submenu có ẩn hết không
 * @param menuTreeItem
 */
export  function checkChildrenHidden(menuTreeItem){
  //Có phải route tổng hợp không
  let alwaysShow=menuTreeItem.alwaysShow;
  if(alwaysShow){
    return false;
  }
  if(!menuTreeItem.children){
    return false
  }
  return menuTreeItem.children?.find((item) => item.hideMenu == false) != null;
}

/**
 * Tính kích thước tệp
 * @param fileSize
 * @param unit
 * @return Trả về kích thước và hậu tố
 */
export function calculateFileSize(fileSize, unit?) {
  let unitArr = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
  if (unit && unit.length > 0) {
    unitArr = unit;
  }
  let size = fileSize;
  let unitIndex = 0;
  while (size >= 1024 && unitIndex < unitArr.length - 1) {
    size /= 1024;
    unitIndex++;
  }
  //Giữ hai chữ số thập phân, làm tròn
  size = Math.round(size * 100) / 100;
  return size + unitArr[unitIndex];
}

/**
 * Lấy uploadheader
 */
export function getHeaders() {
  let tenantId = getTenantId();
  return reactive({
    'X-Access-Token': getToken(),
    'X-Tenant-Id': tenantId ? tenantId : '0',
  });
}

/** Lấy thông tin người dùng tương ứng theo biểu thức */
export function getUserInfoByExpression(expression) {
  if (!expression) {
    return expression;
  }
  // Ngày hiện tại
  if (expression === 'sys_date' || expression === 'sysDate') {
    return dayjs().format('YYYY-MM-DD');
  }
  // Thời gian hiện tại
  if (expression === 'sys_time' || expression === 'sysTime') {
    return dayjs().format('HH:mm:ss');
  }
  const userStore = useUserStoreWithOut();
  let userInfo = userStore.getUserInfo;
  if (userInfo) {
    switch (expression) {
      case 'sysUserId':
        return userInfo.id;
      // Tài khoản đăng nhập của người dùng đang đăng nhập
      case 'sysUserCode':
      case 'sys_user_code':
        return userInfo.username;
      // Tên thật của người dùng đang đăng nhập
      case 'sysUserName':
        return userInfo.realname;
      // Mã phòng ban của người dùng đang đăng nhập
      case 'sysOrgCode':
      case 'sys_org_code':
        return userInfo.orgCode;
    }
  }
  return expression;
}

/**
 * Thay thế biểu thức (#{xxx}) là thông tin người dùng
 * @param expression
 */
export function replaceUserInfoByExpression(expression: string | any[]) {
  if (!expression) {
    return expression;
  }
  const isString = typeof expression === 'string';
  const isArray = Array.isArray(expression)
  if (!isString && !isArray) {
    return expression;
  }
  const reg = /#{(.*?)}/g;
  const replace = (str) => {
    if (typeof str !== 'string') {
      return str;
    }
    let result = str.match(reg);
    if (result && result.length > 0) {
      result.forEach((item) => {
        let userInfo = getUserInfoByExpression(item.substring(2, item.length - 1));
        str = str.replace(item, userInfo);
      });
    }
    return str;
  };
  // @ts-ignore
  return isString ? replace(expression) : expression.map(replace);
}

/**
 * Cài đặt cache tenant, khi tenant thoát
 * 
 * @param tenantId
 */
export async function userExitChangeLoginTenantId(tenantId){
  const userStore = useUserStoreWithOut();
  //step 1 Lấy tenant người dùng
  const url = '/sys/tenant/getCurrentUserTenant'
  let currentTenantId = null;
  const data = await defHttp.get({ url });
  if(data && data.list){
    let arr = data.list;
    if(arr.length>0){
      //step 2.Kiểm tra hiện tạiidCó tồn tại trong tenant người dùng không
      let filterTenantId = arr.filter((item) => item.id == tenantId);
      //Tồn tại nghĩa là không phải thoát, không phải tenant hiện tại, cứ dùng tenant đã dùng là được
      if(filterTenantId && filterTenantId.length>0){
        currentTenantId = tenantId;
      }else{
        //Không tồn tại thì mặc định lấy cái đầu tiên
        currentTenantId = arr[0].id
      }
    }
  }
  let loginTenantId = getTenantId();
  userStore.setTenant(currentTenantId);

  //Tenant trống, nghĩa là không còn tenant, cần làm mới trang. Hoặc tenant hiện tại và tenant đã thoát giống nhau thì cần làm mới trình duyệt
  if(!currentTenantId || tenantId == loginTenantId){
    window.location.reload();
  }
}

/**
 * Module tenant của tôi cần bật gợi ý đa tenant
 * 
 * @param title Tiêu đề
 */
export function tenantSaasMessage(title){
  let tenantId = getTenantId();
  if(!tenantId){
    Modal.confirm({
      title:title,
      content: 'Menu này cần dùng trong chế độ đa tenant, nếu không dữ liệu sẽ rối loạn',
      okText: 'Xác nhận',
      okType: 'danger',
      // @ts-ignore
      cancelButtonProps: { style: { display: 'none' } },
    })
  }
}

/**
 * Kiểm tra ngày và thời gian hiện tại có phải cùng một ngày không
 * @param dateStr
 */
export function sameDay(dateStr) {
  if (!dateStr) {
    return false;
  }
  // Lấy ngày hiện tại
  let currentDate = new Date();
  let currentDay = currentDate.getDate();
  let currentMonth = currentDate.getMonth();
  let currentYear = currentDate.getFullYear();

  //Tạo một ngày khác để so sánh
  let otherDate = new Date(dateStr);
  let otherDay = otherDate.getDate();
  let otherMonth = otherDate.getMonth();
  let otherYear = otherDate.getFullYear();

  //So sánh ngày
  if (currentDay === otherDay && currentMonth === otherMonth && currentYear === otherYear) {
    return true;
  } else {
    return false;
  }
}


/**
 * Dịch tên menu
 * 2024-02-28
 * liaozhiyang
 * @param data
 */
export function translateTitle(data) {
  if (data?.length) {
    const { t } = useI18n();
    data.forEach((item) => {
      if (item.slotTitle) {
        if (item.slotTitle.includes("t('") && t) {
          item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
        }
      }
      if (item.children?.length) {
        translateTitle(item.children);
      }
    });
  }
  return data;
}

/**
 *
 * Đóng băng sâu đối tượng
 * @param obj Object or Array
 */
export function freezeDeep(obj: Recordable | Recordable[]) {
  if (obj != null) {
    if (Array.isArray(obj)) {
      obj.forEach(item => freezeDeep(item))
    } else if (typeof obj === 'object') {
      Object.values(obj).forEach(value => {
        freezeDeep(value)
      })
    }
    Object.freeze(obj)
  }
  return obj
}

/**
 * Lấy tên cấp cha
 * 
 * @param orgCode của phòng ban hiện tạicode
 * @param label Giá trị hiển thị mặc định hiện tại
 * @param depId depId
 * @return Tên phòng ban
 */
export async function getDepartPathNameByOrgCode(orgCode, label, depId){
  if (orgCode) {
    depId = "";
  }
  const cacheKey = `${orgCode || ''}_${depId || ''}`;
  // Nếu đã có yêu cầu cùng tham số đang chạy, tái sử dụng trực tiếp, tránh gọi trùng đồng thời
  if (departPathNameInflight.has(cacheKey)) {
    return departPathNameInflight.get(cacheKey);
  }
  const promise = defHttp.get({ url: "/sys/sysDepart/getDepartPathNameByOrgCode", params:{ orgCode: orgCode, depId: depId } }, { isTransformResponse: false })
    .then((result) => {
      departPathNameInflight.delete(cacheKey);
      if (result.success) {
        return result.result;
      }
      return label;
    })
    .catch((error) => {
      departPathNameInflight.delete(cacheKey);
      throw error;
    });
  departPathNameInflight.set(cacheKey, promise);
  return promise;
}

/**
 * Kiểm tra có hợp lệ khôngID(Loại trừ dữ liệu bẩn như văn bản tiếng Trung)
 */
function isValidId(val: string): boolean {
  // Hiệu lựcIDlà chữ và số thuần (số dàiIDhoặcUUIDĐịnh dạng), không chứa ký tự đặc biệt như tiếng Trung
  return /^[a-zA-Z0-9]+$/.test(val);
}

/**
 * Tải trước hàng loạt tên đường dẫn phòng ban vào cache
 * Thu thập tất cả phòng ban từ dữ liệu bảng/Vị trí công tácID, gọi một lần API batch để truy vấn, tránh request từng mục
 * @param records Dữ liệu trang hiện tại của bảng
 * @param fields Cần trích xuấtIDmảng tên trường của
 */
export async function batchPreloadDepartPathNames(records: any[], fields: string[] = ['belongDepIds', 'departIds', 'mainDepPostId', 'otherDepPostId']) {
  const idSet = new Set<string>();
  for (const record of records) {
    for (const field of fields) {
      const val = record[field];
      if (!val) continue;
      const ids = typeof val === 'string' ? val.split(',') : (Array.isArray(val) ? val : [val]);
      ids.forEach((id) => {
        const trimmed = String(id).trim();
        if (trimmed && isValidId(trimmed) && !departNamePath.value[trimmed]) {
          idSet.add(trimmed);
        }
      });
    }
  }
  if (idSet.size === 0) return;
  try {
    const depIds = Array.from(idSet).join(',');
    const result = await defHttp.get({ url: '/sys/sysDepart/batchGetDepartPathName', params: { depIds } }, { isTransformResponse: false });
    if (result.success && result.result) {
      const map = result.result;
      for (const id of Object.keys(map)) {
        if (map[id]) {
          departNamePath.value[id] = map[id];
        }
      }
    }
  } catch (e) {
    console.warn('Tải trước hàng loạt tên đường dẫn phòng ban vào cache thất bại', e);
  }
}

/**
 * Lấy tên đường dẫn phòng ban
 * @param title
 * @param key Phòng bancodehoặc phòng banid
 * @param izOrgCode Có phải mã cơ quan không
 */
export function getDepartPathName(title,key,izOrgCode) {
  if (!key) {
    return title || '';
  }
  // NếukeyKhông hợp lệID(Ví dụ dữ liệu bẩn tiếng Trung"Phó tổng giám đốc Quốc Cự"), trả về trực tiếpkeyBản thân làm văn bản hiển thị
  if (!isValidId(String(key))) {
    return key;
  }
  if (departNamePath.value[key]) {
    return departNamePath.value[key];
  }
  if(izOrgCode){
    getDepartPathNameByOrgCode(key, title, "").then(result => {
      departNamePath.value[key] = result;
    });
  }else{
    getDepartPathNameByOrgCode("", title, key).then(result => {
      departNamePath.value[key] = result;
    });
  }
  return '';
}

/**
 * Lấy tên đường dẫn nhiều phòng ban
 * @param title
 * @param id
 */
export function getMultiDepartPathName(title,id) {
  if(!id || id.length === 0){
    return '';
  }
  let postIds:any = "";
  if(id instanceof Array){
    postIds = id;
  } else {
    postIds = id.split(",")
  }
  let postNames = "";
  postIds.forEach((postId)=>{
    postNames += getDepartPathName(title,postId,false) + ",";
  });
  if(postNames.endsWith(",")){
    postNames = postNames.substring(0,postNames.length - 1);
  }
  return postNames;
}

/**
 * Lấy tên phòng ban Trả vềh
 * @param departNamePath Đường dẫn phòng ban
 */
export function getDepartName(departNamePath) {
  if(departNamePath){
    let names = departNamePath.split(",");
    let textElements:any = [];
    for (let i = 0; i < names.length; i++) {
      textElements.push(h("p", { style: { marginBottom: '2px'} }, names[i]));
    }
    // Kết hợp chuỗi nội dung đầy đủ để dùng chotitleThuộc tính
    const fullContent = names.join('\n');
    return h("div",{
      style: {
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        display: '-webkit-box',
        WebkitLineClamp: 3,
        WebkitBoxOrient: 'vertical',
        lineHeight: '1.5em',
        maxHeight: '4.5em',
        width: '100%',
        whiteSpace: 'normal'
      },
      // Di chuột hiển thị toàn bộ nội dung
      title: fullContent
    },textElements)
  }
  return departNamePath;
}

/**
 * Lấy bảng tệp
 * @param fileUrl
 */
export function getFileIcon(fileUrl) {
  if(!fileUrl) {
    return 'ant-design:file-outlined';
  }
  const suffix = fileUrl.substring(fileUrl.lastIndexOf('.') + 1).toLowerCase();
  if(['xls','xlsx','csv'].includes(suffix)) {
    return 'ant-design:file-excel-filled';
  }
  if(['doc','docx'].includes(suffix)) {
    return 'ant-design:file-word-filled';
  }
  if(['pdf'].includes(suffix)) {
    return 'ant-design:file-pdf-filled';
  }
  if(['ppt','pptx'].includes(suffix)) {
    return 'ant-design:file-ppt-filled';
  }
  if(['txt'].includes(suffix)) {
    return 'ant-design:file-text-filled';
  }
  if(['md'].includes(suffix)) {
    return 'ant-design:file-markdown-filled';
  }
  return 'ant-design:file-unknown-filled';
}

/**
 * Lấy màu icon tệp
 *
 * @param fileUrl
 */
export function getFileIconColor(fileUrl) {
  if(!fileUrl) {
    return '#999';
  }
  const suffix = fileUrl.substring(fileUrl.lastIndexOf('.') + 1).toLowerCase();
  if(['xls','xlsx','csv'].includes(suffix)) {
    return '#52c41a';
  }
  if(['doc','docx'].includes(suffix)) {
    return '#1890ff';
  }
  if(['pdf'].includes(suffix)) {
    return '#ff4d4f';
  }
  if(['ppt','pptx'].includes(suffix)) {
    return '#fa8c16';
  }
  if(['txt'].includes(suffix)) {
    return '#666';
  }
  if(['md'].includes(suffix)) {
    return '#000';
  }
  return '#999';
}
