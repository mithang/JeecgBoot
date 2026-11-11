import { defHttp } from '/@/utils/http/axios';
// import { useMessage } from "/@/hooks/web/useMessage"; // Cần popup xác nhận rồi mới kích hoạt

enum Api {
  list = '/airag/airagMcp/list',
  save='/airag/airagMcp/save',
  deleteOne = '/airag/airagMcp/delete',
  importExcel = '/airag/airagMcp/importExcel',
  exportXls = '/airag/airagMcp/exportXls',
  sync = '/airag/airagMcp/sync',
  toggleStatus = '/airag/airagMcp/status',
  saveAndSync = '/airag/airagMcp/saveAndSync',
  queryById = '/airag/airagMcp/queryById',
  saveTools = '/airag/airagMcp/saveTools',
}

/**
 * api xuất
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * api nhập
 */
export const getImportUrl = Api.importExcel;

/**
 * Giao diện danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa đơn lẻ
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}


/**
 * Lưu hoặc cập nhật
 * @param params
 * @param isUpdate
 */
export const saveOrUpdate = (params) => {
  return defHttp.post({url: Api.save, data: params}, { isTransformResponse: false });
}

/**
 * Lưu và đồng bộ
 * @param params
 * @param isUpdate
 */
export const saveAndSync = (params) => {
  return defHttp.post({url: Api.saveAndSync, data: params}, { isTransformResponse: false });
}

/**
 * Giao diện đồng bộ
 * @param id
 */
export const syncMcp = (id) => defHttp.post({ url: Api.sync+"/"+id }, { isTransformResponse: false });

/**
 * Sửa đổi trạng thái
 * @param id
 */
export const toggleStatus = (id,status) => defHttp.post({ url: Api.toggleStatus+"/"+id + "/"+ status });

/**
 * Truy vấn chi tiết
 * @param id
 */
export const queryById = (id) => defHttp.get({ url: Api.queryById ,params: { id:id }}, { isTransformResponse: false });

/**
 * Truy vấn theo nhiều id
 * @param params
 */
export const queryByIds = (params) => defHttp.get({ url: Api.list, params }, { isTransformResponse: false });

/**
 * Lưu công cụ plugin (chỉ cập nhật trường tools)
 * @param id ID plugin
 * @param tools chuỗi JSON danh sách công cụ
 */
export const saveTools = (id, tools) => defHttp.post({ url: Api.saveTools, data: { id, tools } }, { isTransformResponse: false });
