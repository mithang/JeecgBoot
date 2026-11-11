import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";
import { Modal } from 'ant-design-vue';
const { createConfirm } = useMessage();

enum Api {
  list = '/sys/ugroup/list',
  save='/sys/ugroup/add',
  edit='/sys/ugroup/edit',
  deleteOne = '/sys/ugroup/delete',
  deleteBatch = '/sys/ugroup/deleteBatch',
  importExcel = '/sys/ugroup/importExcel',
  exportXls = '/sys/ugroup/exportXls',

  userList = '/sys/user/groupUserList',
  deleteUserGroup = '/sys/user/deleteGroupUser',
  batchDeleteUserGroup = '/sys/user/deleteUserGroupBatch',
  addUserGroup = '/sys/user/addSysUserGroup',
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
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: 'Xác nhận xóa',
    content: 'Xóa dữ liệu đã chọn?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdate = (params, isUpdate,showTip = true) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params},{successMessageMode:showTip?'success':'none'});
}
/**
 * API danh sách vai trò
 * @param params
 */
export const userList = (params) => defHttp.get({ url: Api.userList, params });
/**
 * Xóa người dùng của vai trò
 */
export const deleteUserGroup = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUserGroup, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt người dùng của vai trò
 * @param params
 */
export const batchDeleteUserGroup = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Xác nhận hủy liên kết người dùng đã chọn?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.batchDeleteUserGroup, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Thêm người dùng hiện có
 */
export const addUserGroup = (params, handleSuccess) => {
  return defHttp.post({ url: Api.addUserGroup, params }).then(() => {
    handleSuccess();
  });
};
