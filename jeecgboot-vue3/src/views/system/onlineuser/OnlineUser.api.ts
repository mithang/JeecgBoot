import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/online/list',
  forceLogout = '/sys/online/forceLogout'
}

/**
 * Danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa hàng loạt vai trò
 * @param params
 */
export const forceLogout = (params) => {
  return defHttp.post({url:Api.forceLogout,params},{isTransformResponse:false})
};
