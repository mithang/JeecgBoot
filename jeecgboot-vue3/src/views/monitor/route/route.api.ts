import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/gatewayRoute/list',
  deleteList = '/sys/gatewayRoute/deleteList',
  save = '/sys/gatewayRoute/add',
  edit = '/sys/gatewayRoute/updateAll',
  delete = '/sys/gatewayRoute/delete',

  copyRoute = '/sys/gatewayRoute/copyRoute',
  batchPutRecycleBin = '/sys/gatewayRoute/putRecycleBin',
  batchDeleteRecycleBin = '/sys/gatewayRoute/deleteRecycleBin',
}

/**
 * Truy vấn danh sách định tuyến
 * @param params
 */
export const getRouteList = (params) => {
  return defHttp.get({ url: Api.list, params });
};
/**
 * Truy vấn danh sách định tuyến đã xóa logic
 * @param params
 */
export const deleteRouteList = (params) => {
  return defHttp.get({ url: Api.deleteList, params });
};

/**
 * Lưu hoặc cập nhật định tuyến
 * @param params
 */
export const saveOrUpdateRoute = (params) => {
  return defHttp.post({ url: Api.edit, params });
};

/**
 * Xóa định tuyến
 * @param params
 */
export const deleteRoute = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Khôi phục thùng rác
 * @param params
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.batchPutRecycleBin, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa thùng rác
 * @param params
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: `${Api.batchDeleteRecycleBin}?ids=${params.ids}` }).then(() => {
    handleSuccess();
  });
};
/**
 * Sao chép
 */
export const copyRoute = (params, handleSuccess) => {
  return defHttp.get({ url: Api.copyRoute, params }).then(() => {
    handleSuccess();
  });
};
