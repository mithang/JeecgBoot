import {defHttp} from '/@/utils/http/axios';
import {Modal} from 'ant-design-vue';

enum Api {
  list = '/sys/tableWhiteList/list',
  save = '/sys/tableWhiteList/add',
  edit = '/sys/tableWhiteList/edit',
  deleteOne = '/sys/tableWhiteList/delete',
  deleteBatch = '/sys/tableWhiteList/deleteBatch',
  importExcel = '/sys/tableWhiteList/importExcel',
  exportXls = '/sys/tableWhiteList/exportXls',
}

/**
 * Xuất (export)api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * Nhập (import)api
 */
export const getImportUrl = Api.importExcel;
/**
 * API danh sách
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * Xóa một mục
 * @param params
 * @param handleSuccess
 */
export const deleteOne = (params, handleSuccess) => {
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
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Xóa dữ liệu đã chọn?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({
        url: Api.deleteBatch,
        data: params
      }, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * Lưu hoặc cập nhật
 * @param params
 * @param isUpdate Có phải dữ liệu cập nhật không
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
