import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/sys/user/queryDepartUserByOrgCode',
  positionList = '/sys/position/list',
  queryDepartTreeSync = '/sys/sysDepart/queryDepartTreeSync',
}
/**
 * Lấy danh sách cây phòng ban
 */
export const queryDepartTreeSync = (params?) => defHttp.get({ url: Api.queryDepartTreeSync, params });
/**
 * Thông tin người dùng của phòng ban
 */
export const list = (params?) => defHttp.get({ url: Api.list, params });
/**
 * Chức danhlist
 */
export const positionList = (params?) => defHttp.get({ url: Api.positionList, params });
