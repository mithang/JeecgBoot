import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/log/list',
  exportXls = '/sys/log/exportXls',
}

/**
 * Truy vấn danh sách nhật ký
 * @param params
 */
export const getLogList = (params) => {
  return defHttp.get({ url: Api.list, params });
};


/**
 * API xuất
 * @param params
 */
export const getExportUrl = Api.exportXls;
