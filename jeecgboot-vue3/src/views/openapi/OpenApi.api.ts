import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/openapi/list',
  save='/openapi/add',
  edit='/openapi/edit',
  deleteOne = '/openapi/delete',
  deleteBatch = '/openapi/deleteBatch',
  genPath = '/openapi/genPath',
  importExcel = '/openapi/importExcel',
  exportXls = '/openapi/exportXls',
  openApiHeaderList = '/openapi/list',
  openApiParamList = '/openapi/list',
  openApiJson = '/openapi/json',
}

/**
 * API truy vấn biểu mẫu con
 * @param params
 */
export const genPath = Api.genPath
/**
 * JSON tài liệu Swagger
 * @param params
 */
export const openApiJson = Api.openApiJson
/**
 * Xuất API
 * @param params
 */
export const getExportUrl = Api.exportXls;

/**
 * Nhập API
 */
export const getImportUrl = Api.importExcel;
/**
 * API truy vấn biểu mẫu con
 * @param params
 */
export const queryOpenApiHeader = Api.openApiHeaderList
/**
 * API truy vấn biểu mẫu con
 * @param params
 */
export const queryOpenApiParam = Api.openApiParamList

/**
 * API danh sách
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * Xóa một bản ghi
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
    content: 'Có xóa dữ liệu đã chọn không?',
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
export const saveOrUpdate = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({url: Api.edit, params});
  } else {
    return defHttp.post({url: Api.save, params});
  }
}
/**
 * Lấy địa chỉ API
 * @param params
 */
export const getGenPath = (params) =>
  defHttp.get({url: Api.genPath, params},{isTransformResponse:false});
/**
 * API danh sách bảng con
 * @param params
 */
export const openApiHeaderList = (params) =>
  defHttp.get({url: Api.openApiHeaderList, params},{isTransformResponse:false});
/**
 * API danh sách bảng con
 * @param params
 */
export const openApiParamList = (params) =>
  defHttp.get({url: Api.openApiParamList, params},{isTransformResponse:false});
/**
 * JSON tài liệu Swagger
 * @param params
 */
export const getOpenApiJson = (params) =>
  defHttp.get({url: Api.openApiJson, params},{isTransformResponse:false});
