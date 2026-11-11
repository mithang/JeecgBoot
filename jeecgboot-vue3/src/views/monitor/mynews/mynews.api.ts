import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/sysAnnouncementSend/getMyAnnouncementSend',
  editCementSend = '/sys/sysAnnouncementSend/editByAnntIdAndUserId',
  readAllMsg = '/sys/sysAnnouncementSend/readAll',
  syncNotic = '/sys/annountCement/syncNotic',
  getOne = '/sys/sysAnnouncementSend/getOne',
  delete = '/sys/sysAnnouncementSend/delete',
  deleteBatch = '/sys/sysAnnouncementSend/deleteBatch',
}

/**
 * Truy vấn danh sách tin nhắn
 * @param params
 */
export const getMyNewsList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Cập nhật trạng thái đọc tin nhắn hệ thống của người dùng
 * @param params
 */
export const editCementSend = (params) => {
  return defHttp.put({ url: Api.editCementSend, params });
};

/**
 * Đánh dấu tất cả đã đọc bằng một lần nhấn
 * @param params
 */
export const readAllMsg = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận thao tác',
    content: 'Có đánh dấu tất cả đã đọc không?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.put({ url: Api.readAllMsg, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * Đồng bộ tin nhắn
 * @param params
 */
export const syncNotic = (params) => {
  return defHttp.get({ url: Api.syncNotic, params });
};

/**
 * Lấy nội dung tin nhắn theo ID bản ghi gửi tin nhắn
 * @param sendId
 */
export const getOne = (sendId) => {
  return defHttp.get({ url: Api.getOne, params:{sendId} });
};

/**
 * Xóa dữ liệu đánh dấu đã đọc thông báo của người dùng
 * @param params
 * @param handleSuccess
 */
export const deleteAnnSend = (params, handleSuccess) =>{
  return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(()=>{
    handleSuccess();
  })
}

/**
 * Xóa hàng loạt dữ liệu đánh dấu đã đọc thông báo của người dùng
 * @param params
 * @param handleSuccess
 */
export const deleteBatchAnnSend = (params, handleSuccess) =>{
  Modal.confirm({
    iconType: 'warning',
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn không?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }).then(()=>{
        handleSuccess();
      })
    },
  });
}

