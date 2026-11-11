import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/airag/extData/list',
  queryById = '/airag/extData/queryById',
  save='/airag/extData/add',
  edit='/airag/extData/edit',
  deleteOne = '/airag/extData/delete',
  deleteBatch = '/airag/extData/deleteBatch',
  importExcel = '/airag/extData/importExcel',
  exportXls = '/airag/extData/exportXls',
  debugEvaluator = '/airag/extData/evaluator/debug',

  queryTrackById = '/airag/extData/queryTrackById',
  getTrackList = '/airag/extData/getTrackList',
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
export const list = (params) =>
  defHttp.get({url: Api.list, params});
/**
 * Gọi giao diện quỹ đạo
 * @param params
 */
export const getTrackList = (params) =>
  defHttp.get({url: Api.getTrackList, params});
/**
 * Truy vấn dữ liệu theo id tập dữ liệu
 * @param params
 */
export const queryById = (params) =>
  defHttp.get({url: Api.queryById, params},{isTransformResponse: false});
/**
 * Truy vấn dữ liệu theo id tập dữ liệu
 * @param params
 */
export const queryTrackById = (params) =>
  defHttp.get({url: Api.queryTrackById, params},{isTransformResponse: false});

/**
 * Xóa đơn lẻ
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn hay không',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdate = (params, isUpdate,showSuccessMsg=true) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params},{successMessageMode:showSuccessMsg?'success':'none'});
}
/**
 * Gỡ lỗi
 * @param params
 */
export const debugEvaluator = (params) => {
  return defHttp.post({url: Api.debugEvaluator, params},{ isTransformResponse: false });
}
