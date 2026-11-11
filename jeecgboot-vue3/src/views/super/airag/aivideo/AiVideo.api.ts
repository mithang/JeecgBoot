// Cấu hình giao diện AI Video API
import { defHttp } from '@/utils/http/axios';

enum Api {
  submit = '/airag/video/submit',
  query = '/airag/video/query',
  listByUser = '/airag/video/listByUser',
  deleteRecord = '/airag/video/deleteVideoRecord',
}

/**
 * Gửi nhiệm vụ tạo video
 */
export const submitVideoTask = (params: any) => {
  return defHttp.post({ url: Api.submit, params }, { isTransformResponse: false });
};

/**
 * Truy vấn trạng thái nhiệm vụ tạo video
 */
export const queryVideoTask = (taskId: string) => {
  return defHttp.get({ url: `${Api.query}/${taskId}` }, { isTransformResponse: false });
};

/**
 * Truy vấn danh sách video theo id người dùng
 */
export const getVideoListByUser = (params: { userId: string }) => {
  return defHttp.get({ url: Api.listByUser, params }, { isTransformResponse: false });
};

/**
 * Xóa bản ghi video
 */
export const deleteVideoRecord = (params) => {
  return defHttp.delete({ url: Api.deleteRecord, params }, { isTransformResponse: false, joinParamsToUrl: true });
};
