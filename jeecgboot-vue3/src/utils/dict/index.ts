import { defHttp } from '/@/utils/http/axios';
import { useUserStore } from '/@/store/modules/user';
import { getAuthCache } from '/@/utils/auth';
import { DB_DICT_DATA_KEY } from '/@/enums/cacheEnum';

/**
 * Lấy cấu hình từ điển từ cache
 * @param code
 */
export const getDictItemsByCode = (code) => {
  // Giải thích logic mã: 【QQYUN-6417]Vấn đề từ điển chậm trong môi trường production
  const userStore = useUserStore();
  const dictItems = userStore.getAllDictItems;
  if (null != dictItems && typeof dictItems === 'object' && dictItems[code]) {
    return dictItems[code];
  }
  //Tương thích cách viết cũ trước đây
  if (getAuthCache(DB_DICT_DATA_KEY) && getAuthCache(DB_DICT_DATA_KEY)[code]) {
    return getAuthCache(DB_DICT_DATA_KEY)[code];
  }


};
/**
 * Lấy từ cachePopCấu hình từ điển
 * @param text
 * @param code
 */
export const getPopDictByCode = (text, codeStr) => {
  const [code, dictCode, dictText] = codeStr.split(',');
  if (!code || !dictCode || !dictText) {
    return [];
  }
  return defHttp.get(
    { url: `/online/api/cgreportGetDataPackage`, params: { code, dictText, dictCode, dataList: text } },
    { isTransformResponse: false }
  );
};
/**
 * Lấy mảng từ điển
 * @param dictCode Từ điểnCode
 * @return List<Map>
 */
export const initDictOptions = (code) => {
  //1.Ưu tiên đọc cấu hình từ điển từ cache
  if (getDictItemsByCode(code)) {
    return new Promise((resolve, reject) => {
      resolve(getDictItemsByCode(code));
    });
  }
  //2.Lấy mảng từ điển
  // Giải thích logic mã: Trước khi yêu cầu dữ liệu từ điển, mã hóa tham số, nhưng không thể mã hóa trực tiếp vì có thể trước đây đã mã hóa rồi
  if (code.indexOf(',') > 0 && code.indexOf(' ') > 0) {
    // Sau khi mã hóa tương tựsys_user%20where%20username%20like%20xxx' là không chứa khoảng trắng,Ở đây phán đoán nếu có khoảng trắng và dấu phẩy nghĩa là cần xử lý mã hóa
    code = encodeURI(code);
  }
  return defHttp.get({ url: `/sys/dict/getDictItems/${code}` });
};
/**
 * Lấy mảng từ điển
 * @param code Từ điểnCode
 * @param params Tham số truy vấn
 * @param options Cấu hình truy vấn
 * @return List<Map>
 */
export const ajaxGetDictItems = (code, params, options?) => defHttp.get({ url: `/sys/dict/getDictItems/${code}`, params }, options);
