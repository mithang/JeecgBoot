import { unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';

const { createConfirm } = useMessage();

enum Api {
  treeList = '/sys/sysDepart/queryMyDeptTreeList',
  queryIdTree = '/sys/sysDepart/queryIdTree',
  queryByIds = '/sys/sysDepart/queryByIds',
  searchBy = '/sys/sysDepart/searchBy',
}

// Người dùng phòng banAPI
enum DepartUserApi {
  list = '/sys/user/departUserList',
  link = '/sys/user/editSysDepartWithUser',
  unlink = '/sys/user/deleteUserInDepartBatch',
}

// Vai trò phòng banAPI
enum DepartRoleApi {
  list = '/sys/sysDepartRole/list',
  deleteBatch = '/sys/sysDepartRole/deleteBatch',
  save = '/sys/sysDepartRole/add',
  edit = '/sys/sysDepartRole/edit',
  queryTreeListForDeptRole = '/sys/sysDepartPermission/queryTreeListForDeptRole',
  queryDeptRolePermission = '/sys/sysDepartPermission/queryDeptRolePermission',
  saveDeptRolePermission = '/sys/sysDepartPermission/saveDeptRolePermission',
  dataRule = '/sys/sysDepartRole/datarule',
  getDeptRoleList = '/sys/sysDepartRole/getDeptRoleList',
  getDeptRoleByUserId = '/sys/sysDepartRole/getDeptRoleByUserId',
  saveDeptRoleUser = '/sys/sysDepartRole/deptRoleUserAdd',
}

/**
 * Lấy danh sách cây phòng ban
 */
export const queryMyDepartTreeList = (params?) => defHttp.get({ url: Api.treeList, params }, { isTransformResponse: false });

/**
 * Truy vấn dữ liệu, tải tên của tất cả phòng ban dưới dạng cấu trúc cây
 */
export const queryIdTree = (params?) => defHttp.get({ url: Api.queryIdTree, params });

/**
 * Dựa vào ID phòng ban để truy vấn thông tin phòng ban đầy đủ
 */
export const queryDepartByIds = (params) => defHttp.get({ url: Api.queryByIds, params });

/**
 * Tìm kiếm phòng ban theo từ khóa
 */
export const searchByKeywords = (params) => defHttp.get({ url: Api.searchBy, params });

/**
 * Truy vấn thông tin người dùng thuộc phòng ban
 */
export const departUserList = (params) => defHttp.get({ url: DepartUserApi.list, params });

/**
 * Thêm hàng loạt quan hệ liên kết giữa phòng ban và người dùng
 *
 * @param departId Phòng banID
 * @param userIdList Danh sách ID người dùng
 */
export const linkDepartUserBatch = (departId: string, userIdList: string[]) =>
  defHttp.post({ url: DepartUserApi.link, params: { depId: departId, userIdList } });

/**
 * Hủy hàng loạt quan hệ liên kết giữa phòng ban và người dùng
 */
export const unlinkDepartUserBatch = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartUserApi.unlink, params }, { joinParamsToUrl: true }));
    };
    if (confirm) {
      createConfirm({
        iconType: 'warning',
        title: 'Hủy bỏ liên kết',
        content: 'Bạn có chắc muốn hủy liên kết không?',
        onOk: () => doDelete(),
        onCancel: () => reject(),
      });
    } else {
      doDelete();
    }
  });
};

/**
 * Truy vấn thông tin vai trò phòng ban
 */
export const departRoleList = (params) => defHttp.get({ url: DepartRoleApi.list, params });

/**
 * Lưu hoặc cập nhật vai trò phòng ban
 */
export const saveOrUpdateDepartRole = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: DepartRoleApi.edit, params });
  } else {
    return defHttp.post({ url: DepartRoleApi.save, params });
  }
};

/**
 * Xóa hàng loạt vai trò phòng ban
 */
export const deleteBatchDepartRole = (params, confirm = false) => {
  return new Promise((resolve, reject) => {
    const doDelete = () => {
      resolve(defHttp.delete({ url: DepartRoleApi.deleteBatch, params }, { joinParamsToUrl: true }));
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
 * Chức năng cấp quyền vai trò người dùng, truy vấn cây quyền menu
 */
export const queryTreeListForDeptRole = (params) => defHttp.get({ url: DepartRoleApi.queryTreeListForDeptRole, params });
/**
 * Truy vấn cấp quyền vai trò
 */
export const queryDeptRolePermission = (params) => defHttp.get({ url: DepartRoleApi.queryDeptRolePermission, params });
/**
 * Lưu cấp quyền vai trò
 */
export const saveDeptRolePermission = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRolePermission, params });

/**
 *  Truy vấn danh sách quyền dữ liệu của vai trò phòng ban
 */
export const queryDepartRoleDataRule = (functionId, departId, roleId, params?) => {
  let url = `${DepartRoleApi.dataRule}/${unref(functionId)}/${unref(departId)}/${unref(roleId)}`;
  return defHttp.get({ url, params });
};
/**
 * Lưu quyền dữ liệu của vai trò phòng ban
 */
export const saveDepartRoleDataRule = (params) => defHttp.post({ url: DepartRoleApi.dataRule, params });
/**
 * Truy vấn cấp quyền người dùng của vai trò phòng ban
 */
export const queryDepartRoleUserList = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleList, params });
/**
 * Truy vấn cấp quyền người dùng của vai trò phòng ban theo userId
 */
export const queryDepartRoleByUserId = (params) => defHttp.get({ url: DepartRoleApi.getDeptRoleByUserId, params });
/**
 * Lưu cấp quyền người dùng của vai trò phòng ban
 */
export const saveDepartRoleUser = (params) => defHttp.post({ url: DepartRoleApi.saveDeptRoleUser, params });
