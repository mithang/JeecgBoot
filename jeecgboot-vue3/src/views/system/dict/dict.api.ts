import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
enum Api {
  list = '/sys/dict/list',
  save = '/sys/dict/add',
  edit = '/sys/dict/edit',
  duplicateCheck = '/sys/duplicate/check',
  deleteDict = '/sys/dict/delete',
  deleteBatch = '/sys/dict/deleteBatch',
  importExcel = '/sys/dict/importExcel',
  exportXls = '/sys/dict/exportXls',
  recycleBinList = '/sys/dict/deleteList',
  putRecycleBin = '/sys/dict/back',
  batchPutRecycleBin = '/sys/dict/putRecycleBin',
  batchDeleteRecycleBin = '/sys/dict/deleteRecycleBin',
  deleteRecycleBin = '/sys/dict/deletePhysic',
  itemList = '/sys/dictItem/list',
  deleteItem = '/sys/dictItem/delete',
  itemSave = '/sys/dictItem/add',
  itemEdit = '/sys/dictItem/edit',
  dictItemCheck = '/sys/dictItem/dictItemCheck',
  refreshCache = '/sys/dict/refleshCache',
  queryAllDictItems = '/sys/dict/queryAllDictItems',
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
 * API danh sách từ điển
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Xóa từ điển
 */
export const deleteDict = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteDict, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt từ điển
 * @param params
 */
export const batchDeleteDict = (params, handleSuccess) => {
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
 * Lưu hoặc cập nhật từ điển
 * @param params
 */
export const saveOrUpdateDict = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Kiểm tra tính duy nhất
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });
/**
 * Danh sách thùng rác từ điển
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });

/**
 * Khôi phục hàng loạt từ thùng rác
 * @param params
 */
export const batchPutRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.batchPutRecycleBin, params}).then(() => {
    handleSuccess();
  });
};
/**
 * Khôi phục từ thùng rác
 * @param params
 */
export const putRecycleBin = (id, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt trong thùng rác
 * @param params
 */
export const batchDeleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.batchDeleteRecycleBin}?ids=${params.ids}`}).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa trong thùng rác
 * @param params
 */
export const deleteRecycleBin = (id, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin + `/${id}` }).then(() => {
    handleSuccess();
  });
};
/**
 * Danh sách cấu hình từ điển
 * @param params
 */
export const itemList = (params) => defHttp.get({ url: Api.itemList, params });
/**
 * Xóa cấu hình từ điển
 * @param params
 */
export const deleteItem = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteItem, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Lưu hoặc cập nhật cấu hình từ điển
 * @param params
 */
export const saveOrUpdateDictItem = (params, isUpdate) => {
  let url = isUpdate ? Api.itemEdit : Api.itemSave;
  return defHttp.post({ url: url, params });
};
/**
 * Kiểm tra giá trị dữ liệu từ điển
 * @param params
 */
export const dictItemCheck = (params) => defHttp.get({ url: Api.dictItemCheck, params }, { isTransformResponse: false });
/**
 * Làm mới từ điển
 * @param params
 */
export const refreshCache = () => defHttp.get({ url: Api.refreshCache }, { isTransformResponse: false });
/**
 * Lấy mọi mục từ điển
 * @param params
 */
export const queryAllDictItems = () => defHttp.get({ url: Api.queryAllDictItems }, { isTransformResponse: false });
