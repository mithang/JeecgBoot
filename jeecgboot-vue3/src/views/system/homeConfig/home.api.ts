import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/sysRoleIndex/list',
  save = '/sys/sysRoleIndex/add',
  edit = '/sys/sysRoleIndex/edit',
  deleteIndex = '/sys/sysRoleIndex/delete',
  deleteBatch = '/sys/sysRoleIndex/deleteBatch',
  queryIndexByCode = '/sys/sysRoleIndex/queryByCode',
}
/**
 * Danh sách vai trò hệ thống
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa vai trò
 */
export const deleteIndex = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteIndex, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt vai trò
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
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
 * Lưu hoặc cập nhật cấu hình trang chủ
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  const url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Truy vấn cấu hình trang chủ
 * @param params
 */
export const queryIndexByCode = (params) => defHttp.get({ url: Api.queryIndexByCode, params }, { isTransformResponse: false });
/**
 * Bật/Tắt cấu hình trang chủ
 * @param params
 */
export const changeStatus = (params, handleSuccess) => {
  return defHttp.put({ url: Api.edit, data: params }, { successMessageMode: 'none' }).then(() => {
    handleSuccess();
  });
};
