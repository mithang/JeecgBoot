import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

export enum Api {
  queryDepartTreeSync = '/sys/sysDepart/queryDepartTreeSync',
  searchByDepartManage = '/sys/sysDepart/searchByDepartManage',
  queryAllParentId = '/sys/sysDepart/queryAllParentId',
  save = '/sys/sysDepart/add',
  edit = '/sys/sysDepart/edit',
  delete = '/sys/sysDepart/delete',
  deleteBatch = '/sys/sysDepart/deleteBatch',
  exportXlsUrl = '/sys/sysDepart/exportXls',
  importExcelUrl = '/sys/sysDepart/importExcel',

  roleQueryTreeList = '/sys/role/queryTreeList',
  queryDepartPermission = '/sys/permission/queryDepartPermission',
  saveDepartPermission = '/sys/permission/saveDepartPermission',

  dataRule = '/sys/sysDepartPermission/datarule',

  getCurrentUserDeparts = '/sys/user/getCurrentUserDeparts',
  selectDepart = '/sys/selectDepart',
  getUpdateDepartInfo = '/sys/user/getUpdateDepartInfo',
  doUpdateDepartInfo = '/sys/user/doUpdateDepartInfo',
  changeDepartChargePerson = '/sys/user/changeDepartChargePerson',
  //Dựa vào ID phòng ban để lấy thông tin vị trí
  getPositionByDepartId = '/sys/sysDepart/getPositionByDepartId',
  //Dựa vào ID phòng ban để lấy quan hệ cấp trên/cấp dưới của vị trí
  getRankRelation = '/sys/sysDepart/getRankRelation',
  //Lấy phòng ban và vị trí không đồng bộ
  queryDepartAndPostTreeSync = '/sys/sysDepart/queryDepartAndPostTreeSync',
  //Lấy thành viên thuộc phòng ban và vị trí
  queryDepartPostByOrgCode = '/sys/user/queryDepartPostByOrgCode',
  //Cập nhật vị trí sau khi kéo thả phòng ban
  updateChangeDepart = '/sys/sysDepart/updateChangeDepart',
  //Lấy phòng ban phụ trách
  getDepartmentHead = '/sys/sysDepart/getDepartmentHead',
  //Loại bỏ người dùng khỏi phòng ban/vị trí
  removeDepartUser = '/sys/user/removeDepartUser',
}

/**
 * Lấy danh sách cây phòng ban
 */
export const queryDepartTreeSync = (params?) => defHttp.get({ url: Api.queryDepartTreeSync, params });

/**
 * Tìm kiếm chuyên dụng của trang Quản lý phòng ban, kết quả tìm kiếm hỗ trợ tiếp tục mở rộng cấp dưới
 */
export const searchByDepartManage = (params) => defHttp.get({ url: Api.searchByDepartManage, params });

/**
 * Lấy nút phòng ban và mọi cấp trên của nó
 */
export const queryAllParentId = (params) => defHttp.get({ url: Api.queryAllParentId, params });

/**
 * Lấy danh sách cây phòng ban và vị trí
 */
export const queryDepartAndPostTreeSync = (params?) => defHttp.get({ url: Api.queryDepartAndPostTreeSync, params });

/**
 * Lưu hoặc cập nhật vai trò phòng ban
 */
export const saveOrUpdateDepart = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * Xóa hàng loạt vai trò phòng ban
 */
export const deleteBatchDepart = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: Api.deleteBatch, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'Xóa',
        content: 'Bạn có chắc chắn muốn xóa không?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * Lấy danh sách cây quyền
 */
export const queryRoleTreeList = (params?) => defHttp.get({ url: Api.roleQueryTreeList, params });
/**
 * Truy vấn quyền phòng ban
 */
export const queryDepartPermission = (params?) => defHttp.get({ url: Api.queryDepartPermission, params });
/**
 * Lưu quyền phòng ban
 */
export const saveDepartPermission = (params) => defHttp.post({ url: Api.saveDepartPermission, params });

/**
 *  Truy vấn danh sách quyền dữ liệu của phòng ban
 */
export const queryDepartDataRule = (functionId, departId, params?) => {
  let url = `${Api.dataRule}/${unref(functionId)}/${unref(departId)}`;
  return defHttp.get({ url, params });
};
/**
 * Lưu quyền dữ liệu của phòng ban
 */
export const saveDepartDataRule = (params) => defHttp.post({ url: Api.dataRule, params });
/**
 * Lấy thông tin phòng ban của người dùng đăng nhập
 */
export const getUserDeparts = (params?) => defHttp.get({ url: Api.getCurrentUserDeparts, params });
/**
 * Chuyển đổi lựa chọn phòng ban
 */
export const selectDepart = (params?) => defHttp.put({ url: Api.selectDepart, params });

/**
 * Trước khi chỉnh sửa phòng ban, lấy thông tin liên quan của phòng ban
 * @param id
 */
export const getUpdateDepartInfo = (id) => defHttp.get({ url: Api.getUpdateDepartInfo, params: {id} });

/**
 * Chỉnh sửa phòng ban
 * @param params
 */
export const doUpdateDepartInfo = (params) => defHttp.put({ url: Api.doUpdateDepartInfo, params });

/**
 * Xóa phòng ban
 * @param id
 */
export const deleteDepart = (id) => defHttp.delete({ url: Api.delete, params:{ id } }, { joinParamsToUrl: true });

/**
 * Đặt người phụ trách / Hủy người phụ trách
 * @param params
 */
export const changeDepartChargePerson = (params) => defHttp.put({ url: Api.changeDepartChargePerson, params });

/**
 * Dựa vào ID phòng ban để lấy thông tin vị trí
 */
export const getPositionByDepartId = (params) => defHttp.get({ url: Api.getPositionByDepartId, params }, { isTransformResponse: false });

/**
 * Dựa vào ID phòng ban để lấy quan hệ cấp trên/cấp dưới của vị trí
 * @param params
 */
export const getRankRelation = (params) => defHttp.get({ url: Api.getRankRelation, params,timeout: 2 * 60 * 1000 }, { isTransformResponse: false });

/**
 * Dựa vào mã phòng ban hoặc vị trí để lấy thành viên danh bạ
 * 
 * @param params
 */
export const queryDepartPostByOrgCode = (params) => defHttp.get({ url: Api.queryDepartPostByOrgCode, params });

/**
 * Cập nhật vị trí sau khi kéo thả phòng ban
 * 
 * @param params
 */
export const updateChangeDepart = (params) => defHttp.put({ url: Api.updateChangeDepart, params },{ isTransformResponse: false });

/**
 * Lấy phòng ban phụ trách
 * 
 * @param params
 */
export const getDepartmentHead = (params) => defHttp.get({ url: Api.getDepartmentHead, params });

/**
 * Loại bỏ người dùng khỏi phòng ban/vị trí (chỉ xóa quan hệ liên kết, không xóa người dùng)
 *
 * @param params depId：ID phòng ban hoặc vị trí, userIds: ID người dùng, nhiều giá trị phân tách bằng dấu phẩy
 */
export const removeDepartUser = (params) => defHttp.delete({ url: Api.removeDepartUser, params }, { joinParamsToUrl: true });
