import { defHttp } from '/@/utils/http/axios';
import { isArray } from '/@/utils/is';

export enum Api {
  enhanceJs = '/online/cgform/head/enhanceJs/',
  enhanceButton = '/online/cgform/head/enhanceButton/',
}

export enum EnhanceJavaApi {
  enhanceJava = '/online/cgform/head/enhanceJava',
  deleteBatch = '/online/cgform/head/deleteBatchEnhanceJava',
}

export enum EnhanceSqlApi {
  enhanceSql = '/online/cgform/head/enhanceSql',
  deleteBatch = '/online/cgform/head/deletebatchEnhanceSql',
}

/**
 * LấyJSTăng cường
 * @param code
 * @param type
 * @param params
 */
export async function getEnhanceJsByCode(code: string, type: string, params?) {
  let { success, result } = await defHttp.get(
    {
      url: Api.enhanceJs + code,
      params: {
        ...params,
        type,
      },
    },
    { isTransformResponse: false }
  );
  if (!success) {
    result = { cgJs: '' };
  }
  return result;
}

/**
 * Lưu JS Tăng cường
 * @param code
 * @param params
 * @param isUpdate Có cập nhật hay không
 */
export const saveEnhanceJs = (code: string, params, isUpdate: boolean) => {
  let url = `${Api.enhanceJs}${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params }, { successMessageMode: 'none' });
  } else {
    return defHttp.post({ url, params }, { successMessageMode: 'none' });
  }
};

// TảiJavaDữ liệu tăng cường
export async function getEnhanceJavaByCode(code: string, params) {
  // Tải nút trước
  let btnRes = await defHttp.get({ url: Api.enhanceButton + code }, { isTransformResponse: false });
  let btnList = [];
  if (btnRes.success && isArray(btnRes.result)) {
    // Lọc nút javaPhần tăng cường chỉ xemactionNút
    btnList = btnRes.result.filter((item) => item.optType == 'action');
  }
  // Tải lại dữ liệu
  let path = `${EnhanceJavaApi.enhanceJava}/${code}`;
  let dataSource = await defHttp.get({ url: path, params });
  return { btnList, dataSource };
}

// Thực thi thao tác xóa
export function doEnhanceJavaBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: EnhanceJavaApi.deleteBatch,
      params: {
        ids: idList.join(','),
      },
    },
    { joinParamsToUrl: true }
  );
}

/**
 * Lưu Java Tăng cường
 * @param code
 * @param params
 * @param isUpdate Có cập nhật hay không
 */
export const saveEnhanceJava = (code: string, params, isUpdate: boolean) => {
  let url = `${EnhanceJavaApi.enhanceJava}/${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params });
  } else {
    return defHttp.post({ url, params });
  }
};

// TảiSqlDữ liệu tăng cường
export async function getEnhanceSqlByCode(code: string, params) {
  // Tải nút trước
  let btnRes = await defHttp.get({ url: Api.enhanceButton + code }, { isTransformResponse: false });
  let btnList = [];
  if (btnRes.success && isArray(btnRes.result)) {
    // Lọc nút javaPhần tăng cường chỉ xemactionNút
    btnList = btnRes.result.filter((item) => item.optType == 'action');
  }
  // Tải lại dữ liệu
  let path = `${EnhanceSqlApi.enhanceSql}/${code}`;
  let dataSource = await defHttp.get({ url: path, params });
  return { btnList, dataSource };
}

// Thực thi thao tác xóa
export function doEnhanceSqlBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: EnhanceSqlApi.deleteBatch,
      params: {
        ids: idList.join(','),
      },
    },
    { joinParamsToUrl: true }
  );
}

/**
 * Lưu Java Tăng cường
 * @param code
 * @param params
 * @param isUpdate Có cập nhật hay không
 */
export const saveEnhanceSql = (code: string, params, isUpdate: boolean) => {
  let url = `${EnhanceSqlApi.enhanceSql}/${code}`;
  if (isUpdate) {
    return defHttp.put({ url, params });
  } else {
    return defHttp.post({ url, params });
  }
};
