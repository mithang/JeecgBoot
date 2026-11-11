import { defHttp } from '/@/utils/http/axios';

enum Api {
  //Truy vấn phiên bản app
  queryAppVersion = '/sys/version/app3version',
  //Lưu phiên bản app
  saveAppVersion = '/sys/version/saveVersion',
}
/**
 * Truy vấn phiên bản APP
 * @param params
 */
export const queryAppVersion = (params) => defHttp.get({ url: Api.queryAppVersion, params });
/**
 * Lưu phiên bản APP
 * @param params
 */
export const saveAppVersion = (params) => {
  return defHttp.post({ url: Api.saveAppVersion, params });
};
