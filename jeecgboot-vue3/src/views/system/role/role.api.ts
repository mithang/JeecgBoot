import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/role/list',
  listByTenant = '/sys/role/listByTenant',
  save = '/sys/role/add',
  edit = '/sys/role/edit',
  deleteRole = '/sys/role/delete',
  deleteBatch = '/sys/role/deleteBatch',
  exportXls = '/sys/role/exportXls',
  importExcel = '/sys/role/importExcel',
  isRoleExist = '/sys/role/checkRoleCode',
  queryTreeListForRole = '/sys/role/queryTreeList',
  queryRolePermission = '/sys/permission/queryRolePermission',
  saveRolePermission = '/sys/permission/saveRolePermission',
  queryDataRule = '/sys/role/datarule',
  getParentDesignList = '/act/process/extActDesignFlowData/getDesFormFlows',
  getRoleDegisnList = '/joa/designform/designFormCommuse/getRoleDegisnList',
  saveRoleDesign = '/joa/designform/designFormCommuse/sysRoleDesignAdd',
  userList = '/sys/user/userRoleList',
  deleteUserRole = '/sys/user/deleteUserRole',
  batchDeleteUserRole = '/sys/user/deleteUserRoleBatch',
  addUserRole = '/sys/user/addSysUserRole',
  saveRoleIndex = '/sys/sysRoleIndex/add',
  editRoleIndex = '/sys/sysRoleIndex/edit',
  queryIndexByCode = '/sys/sysRoleIndex/queryByCode',
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
 * Danh sách vai trò hệ thống
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Danh sách vai trò đơn vị thuê
 * @param params
 */
export const listByTenant = (params) => defHttp.get({ url: Api.listByTenant, params });

/**
 * Xóa vai trò
 */
export const deleteRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt vai trò
 * @param params
 */
export const batchDeleteRole = (params, handleSuccess) => {
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
 * Lưu hoặc cập nhật vai trò
 * @param params
 */
export const saveOrUpdateRole = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Kiểm tra mã
 * @param params
 */
// Giải thích logic mã: 【QQYUN-7415】Bổ sung chống rung (debounce) cho việc biểu mẫu gọi API để kiểm tra
let timer;
export const isRoleExist = (params) => {
  return new Promise((resolve, rejected) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      defHttp
        .get({ url: Api.isRoleExist, params }, { isTransformResponse: false })
        .then((res) => {
          resolve(res);
        })
        .catch((error) => {
          rejected(error);
        });
    }, 500);
  });
};
/**
 * Truy vấn thông tin cây theo vai trò
 */
export const queryTreeListForRole = () => defHttp.get({ url: Api.queryTreeListForRole });
/**
 * Truy vấn quyền vai trò
 */
export const queryRolePermission = (params) => defHttp.get({ url: Api.queryRolePermission, params });
/**
 * Lưu quyền vai trò
 */
export const saveRolePermission = (params) => defHttp.post({ url: Api.saveRolePermission, params });
/**
 * Truy vấn quy tắc dữ liệu của vai trò
 */
export const queryDataRule = (params) =>
  defHttp.get({ url: `${Api.queryDataRule}/${params.functionId}/${params.roleId}` }, { isTransformResponse: false });
/**
 * Lưu quy tắc dữ liệu của vai trò
 */
export const saveDataRule = (params) => defHttp.post({ url: Api.queryDataRule, params });
/**
 * Lấy dữ liệu biểu mẫu
 * @return List<Map>
 */
export const getParentDesignList = () => defHttp.get({ url: Api.getParentDesignList });
/**
 * Lấy dữ liệu biểu mẫu vai trò
 * @return List<Map>
 */
export const getRoleDegisnList = (params) => defHttp.get({ url: Api.getRoleDegisnList, params });
/**
 * Gửi thông tin phiếu công việc của vai trò
 */
export const saveRoleDesign = (params) => defHttp.post({ url: Api.saveRoleDesign, params });
/**
 * API danh sách vai trò
 * @param params
 */
export const userList = (params) => defHttp.get({ url: Api.userList, params });
/**
 * Xóa người dùng của vai trò
 */
export const deleteUserRole = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt người dùng của vai trò
 * @param params
 */
export const batchDeleteUserRole = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Xóa dữ liệu đã chọn?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.batchDeleteUserRole, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Thêm người dùng hiện có
 */
export const addUserRole = (params, handleSuccess) => {
  return defHttp.post({ url: Api.addUserRole, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Lưu hoặc cập nhật
 * @param params
 * @param isUpdate Có phải dữ liệu cập nhật không
 */
export const saveOrUpdateRoleIndex = (params, isUpdate) => {
  let url = isUpdate ? Api.editRoleIndex : Api.saveRoleIndex;
  return defHttp.post({ url: url, params });
};
/**
 * Truy vấn cấu hình trang chủ theo code
 * @param params
 */
export const queryIndexByCode = (params) => defHttp.get({ url: Api.queryIndexByCode, params }, { isTransformResponse: false });
