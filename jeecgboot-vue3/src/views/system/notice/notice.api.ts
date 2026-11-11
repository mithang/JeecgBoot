import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/annountCement/list',
  save = '/sys/annountCement/add',
  edit = '/sys/annountCement/edit',
  delete = '/sys/annountCement/delete',
  queryById = '/sys/annountCement/queryById',
  deleteBatch = '/sys/annountCement/deleteBatch',
  exportXls = '/sys/annountCement/exportXls',
  importExcel = '/sys/annountCement/importExcel',
  releaseData = '/sys/annountCement/doReleaseData',
  reovkeData = '/sys/annountCement/doReovkeData',
  editIzTop = '/sys/annountCement/editIzTop',
  addVisitsNum = '/sys/annountCement/addVisitsNumber',
  tempList = '/sys/message/sysMessageTemplate/list',
}

/**
 * Xuất (export)url
 */
export const getExportUrl = Api.exportXls;
/**
 * Nhập (import)url
 */
export const getImportUrl = Api.importExcel;
/**
 * Truy vấn danh sách tin nhắn
 * @param params
 */
export const getList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Lưu hoặc cập nhật thông cáo
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Xóa thông cáo
 * @param params
 */
export const deleteNotice = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Chỉnh sửa ghim đầu
 * @param params
 */
export const editIzTop = (params, handleSuccess) => {
  return defHttp.post({ url: Api.editIzTop, data: params }).then(() => {
    handleSuccess();
  });
};

/**
 * Thông báo tin nhắn hàng loạt
 * @param params
 */
export const batchDeleteNotice = (params) => defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true });

/**
 * Phát hành
 * @param id
 */
export const doReleaseData = (params) => defHttp.get({ url: Api.releaseData, params });
/**
 * Thu hồi
 * @param id
 */
export const doReovkeData = (params) => defHttp.get({ url: Api.reovkeData, params });
/**
 * Thêm mới lượt truy cập
 * @param id
 */
export const addVisitsNum = (params) => defHttp.get({ url: Api.addVisitsNum, params }, { successMessageMode: 'none' });
/**
 * Truy vấn dữ liệu theo ID
 * @param id
 */
export const queryById = (params) => defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
/**
 * Truy vấn danh sách mẫu
 * @param params
 */
export const getTempList = (params) => {
  return defHttp.get({ url: Api.tempList, params: { ...params, useStatus: '1' } });
};
