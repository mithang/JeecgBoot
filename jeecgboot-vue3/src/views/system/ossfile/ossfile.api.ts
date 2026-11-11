import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/sys/oss/file/list',
  deleteFile = '/sys/oss/file/delete',
  ossUpload = '/sys/oss/file/upload',
  minioUpload = '/sys/upload/uploadMinio',
}

/**
 * ossTải lên
 * @param params
 */
export const getOssUrl = Api.ossUpload;
/**
 * minioTải lên
 * @param params
 */
export const getMinioUrl = Api.minioUpload;
/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa người dùng
 */
export const deleteFile = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteFile, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
