import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/test/jeecgDemo/list',
  save = '/test/jeecgDemo/add',
  edit = '/test/jeecgDemo/edit',
  get = '/test/jeecgDemo/queryById',
  delete = '/test/jeecgDemo/delete',
  deleteBatch = '/test/jeecgDemo/deleteBatch',
  exportXls = '/test/jeecgDemo/exportXls',
  importExcel = '/test/jeecgDemo/importExcel',
}
/**
 * Xuất (export)api
 */
export const getExportUrl = Api.exportXls;
/**
 * Nhập (import)api
 */
export const getImportUrl = Api.importExcel;
/**
 * Truy vấn danh sách ví dụ
 * @param params
 */
export const getDemoList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Lưu hoặc cập nhật ví dụ
 * @param params
 */
export const saveOrUpdateDemo = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Truy vấn chi tiết ví dụ
 * @param params
 */
export const getDemoById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Xóa ví dụ
 * @param params
 */
export const deleteDemo = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt ví dụ
 * @param params
 */
export const batchDeleteDemo = (params, handleSuccess) => {
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
