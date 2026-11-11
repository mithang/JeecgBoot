import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/dataLog/list',
  queryDataVerList = '/sys/dataLog/queryDataVerList',
  queryCompareList = '/sys/dataLog/queryCompareList',
}

/**
 * Truy vấn danh sách nhật ký dữ liệu
 * @param params
 */
export const getDataLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Truy vấn danh sách nhật ký dữ liệu
 * @param params
 */
export const queryDataVerList = (params) => {
  return defHttp.get({ url: Api.queryDataVerList, params });
};

/**
 * Truy vấn dữ liệu so sánh
 * @param params
 */
export const queryCompareList = (params) => {
  return defHttp.get({ url: Api.queryCompareList, params });
};
