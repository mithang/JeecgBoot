import { defHttp } from '/@/utils/http/axios';

enum Api {
  listCementByUser = '/sys/annountCement/listByUser',
  getUnreadMessageCount = '/sys/annountCement/getUnreadMessageCount',
  editCementSend = '/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  clearAllUnReadMessage = '/sys/annountCement/clearAllUnReadMessage',
}

/**
 * Lấy danh sách tin nhắn thông báo hệ thống
 * @param params
 */
export const listCementByUser = (params?) => defHttp.get({ url: Api.listCementByUser, params });

/**
 * Lấy số tin nhắn chưa đọc trong gần hai tháng của người dùng
 * @param params
 */
export const getUnreadMessageCount = (params?) => defHttp.get({ url: Api.getUnreadMessageCount, params });

export const editCementSend = (anntId, params?) => defHttp.put({ url: Api.editCementSend, params: { anntId, ...params } });

/**
 * Xóa sạch tất cả tin nhắn chưa đọc
 */
export const clearAllUnReadMessage = () => defHttp.post({ url: Api.clearAllUnReadMessage },{ isTransformResponse: false });
