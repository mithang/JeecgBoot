import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/category/rootList',
  save = '/sys/category/add',
  edit = '/sys/category/edit',
  deleteCategory = '/sys/category/delete',
  deleteBatch = '/sys/category/deleteBatch',
  importExcel = '/sys/category/importExcel',
  exportXls = '/sys/category/exportXls',
  loadTreeData = '/sys/category/loadTreeRoot',
  getChildList = '/sys/category/childList',
  getChildListBatch = '/sys/category/getChildListBatch',
}
/**
 * Xuất (export)api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * Nhập (import)api
 * @param params
 */
export const getImportUrl = Api.importExcel;
/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Xóa
 */
export const deleteCategory = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteCategory, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDeleteCategory = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Xóa dữ liệu đã chọn?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Truy vấn toàn bộ dữ liệu nút dạng cây
 * @param params
 */
export const loadTreeData = (params) => defHttp.get({ url: Api.loadTreeData, params });
/**
 * Truy vấn dữ liệu nút con
 * @param params
 */
export const getChildList = (params) => defHttp.get({ url: Api.getChildList, params });
/**
 * Truy vấn hàng loạt dữ liệu nút con
 * @param params
 */
export const getChildListBatch = (params) => defHttp.get({ url: Api.getChildListBatch, params }, { isTransformResponse: false });
