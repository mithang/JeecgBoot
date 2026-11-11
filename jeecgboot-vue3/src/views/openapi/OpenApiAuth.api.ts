import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/openapi/auth/list',
  save='/openapi/auth/add',
  edit='/openapi/auth/edit',
  apiList= '/openapi/list',
  genAKSK = '/openapi/auth/genAKSK',
  permissionList='/openapi/permission/getOpenApi',
  permissionAdd='/openapi/permission/add',
  deleteOne = '/openapi/auth/delete',
  deleteBatch = '/openapi/auth/deleteBatch',
  importExcel = '/openapi/auth/importExcel',
  exportXls = '/openapi/auth/exportXls',
}

/**
 * Lấy API
 * @param params
 */
export const apiList = Api.apiList;
/**
 * Thêm quyền
 * @param params
 */
export const permissionAdd = Api.permissionAdd;
/**
 * Tạo AK/SK
 * @param params
 */
export const genAKSK = Api.genAKSK;

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
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa một bản ghi
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * Xóa hàng loạt
 * @param params
 * @param handleSuccess
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
 * @param isUpdate
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params }, { isTransformResponse: false });
  }
  return defHttp.post({ url: Api.save, params }, { isTransformResponse: false });
}

/**
 * API danh sách toàn bộ quyền
 * @param params
 */
export const getApiList = (params) => defHttp.get({ url: Api.apiList, params }, { isTransformResponse: false });

/**
 * Lấy API của dự án đã ủy quyền
 * @param params
 */
export const getPermissionList = (params) => defHttp.get({ url: Api.permissionList, params });
/**
 * Phương thức lưu ủy quyền
 * @param params
 * @param isUpdate
 */
export const permissionAddFunction = (params) => {
  return defHttp.post({ url: Api.permissionAdd, params }, { isTransformResponse: false });
}
/**
 * Phương thức lưu ủy quyền
 * @param params
 * @param isUpdate
 */
export const getGenAKSK = (params) => {
  return defHttp.get({ url: Api.genAKSK, params });
}
