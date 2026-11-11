import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/online/graphreport/head/list',
  delete = '/online/graphreport/head/delete',
  deleteBatch = '/online/graphreport/head/deleteBatch',
  copy = '/online/graphreport/head/copy',
  exportXls = '/online/graphreport/head/exportXls',
  importXls = '/online/graphreport/head/importExcel',
  parseField = '/online/graphreport/head/parseField',
  paramsList = '/online/graphreport/params/listByHeadId',
  getChartsData = '/online/graphreport/api/getChartsData',
  getParamsInfo = '/online/graphreport/params/listByHeadId',
}

/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

// Xóa hàng loạt
export function doBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: Api.deleteBatch,
      params: { ids: idList.join(',') },
    },
    { joinParamsToUrl: true }
  );
}

export const queryParamsList = (headId: string) => defHttp.get({ url: Api.paramsList, params: { headId } });

// Truy vấn dữ liệu biểu đồ
export const getChartsData = (params) => defHttp.get({ url: Api.getChartsData, params: params });
export const getParamsInfo = (params) => defHttp.get({ url: Api.getParamsInfo, params: params });

export const parseField = (type, data, params = {}) => defHttp.post({ url: Api.parseField, params: { type, data, ...params } });

/**
 * Sao chép biểu đồ
 * @param id Biểu đồid
 */
export const doCopy = (id) => {
  // defHttp cái không phải của GET Yêu cầu sẽ đưa params Coi như thân yêu cầu (request body) data, backend @RequestParam từ query lấy tham số, vì vậy đưa id ghép vào URL trên
  return defHttp.post({ url: `${Api.copy}?id=${id}` });
};
