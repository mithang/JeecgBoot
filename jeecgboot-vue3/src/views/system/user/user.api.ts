import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
import { isObject } from '/@/utils/is';
enum Api {
  listNoCareTenant = '/sys/user/listAll',
  list = '/sys/user/list',
  save = '/sys/user/add',
  edit = '/sys/user/edit',
  getUserRole = '/sys/user/queryUserRole',
  duplicateCheck = '/sys/duplicate/check',
  deleteUser = '/sys/user/delete',
  deleteBatch = '/sys/user/deleteBatch',
  importExcel = '/sys/user/importExcel',
  exportXls = '/sys/user/exportXls',
  recycleBinList = '/sys/user/recycleBin',
  putRecycleBin = '/sys/user/putRecycleBin',
  deleteRecycleBin = '/sys/user/deleteRecycleBin',
  allRolesList = '/sys/role/queryall',
  allRolesListNoByTenant = '/sys/role/queryallNoByTenant',
  allTenantList = '/sys/tenant/queryList',
  allPostList = '/sys/position/list',
  userDepartList = '/sys/user/userDepartList',
  changePassword = '/sys/user/changePassword',
  frozenBatch = '/sys/user/frozenBatch',
  getUserAgent = '/sys/sysUserAgent/queryByUserName',
  userQuitAgent = '/sys/user/userQuitAgent',
  getQuitList = '/sys/user/getQuitList',
  putCancelQuit = '/sys/user/putCancelQuit',
  resetPassword = '/sys/user/resetPassword',
  updateUserTenantStatus='/sys/tenant/updateUserTenantStatus',
  getUserTenantPageList='/sys/tenant/getUserTenantPageList',
  getDepPostIdByDepId = '/sys/sysDepart/getDepPostIdByDepId',
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
 * API danh sách(Truy vấn người dùng, thông qua cách ly đơn vị thuê)
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * API danh sách(Truy vấn toàn bộ người dùng, không qua cách ly đơn vị thuê)
 * @param params
 */
export const listNoCareTenant = (params) => defHttp.get({ url: Api.listNoCareTenant, params });

/**
 * API vai trò người dùng
 * @param params
 */
export const getUserRoles = (params) => defHttp.get({ url: Api.getUserRole, params }, { errorMessageMode: 'none' });

/**
 * Xóa người dùng
 */
export const deleteUser = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteUser, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt người dùng
 * @param params
 */
export const batchDeleteUser = (params, handleSuccess) => {
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
 * Lưu hoặc cập nhật người dùng
 * @param params
 */
export const saveOrUpdateUser = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};
/**
 * Kiểm tra tính duy nhất
 * @param params
 */
export const duplicateCheck = (params) => defHttp.get({ url: Api.duplicateCheck, params }, { isTransformResponse: false });

/**
 * 20231215
 * liaozhiyang
 * Kiểm tra tính duy nhất ( trì hoãn 【chống rung】 )
 * @param params
 */
const timer = {};
export const duplicateCheckDelay = (params) => {
  return new Promise((resove, rejected) => {
    // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-1380】Khi dùng nhiều duplicateCheckDelay trong biểu mẫu, lúc gọi phương thức validate sẽ khiến promise bị treo, không lưu được
    let key;
    if (isObject(params)) {
      key = `${params.tableName}_${params.fieldName}`;
    } else {
      key = params;
    }
    clearTimeout(timer[key]);
    // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-1380】Khi dùng nhiều duplicateCheckDelay trong biểu mẫu, lúc gọi phương thức validate sẽ khiến promise bị treo, không lưu được
    timer[key] = setTimeout(() => {
      defHttp
        .get({ url: Api.duplicateCheck, params }, { isTransformResponse: false })
        .then((res: any) => {
          resove(res as any);
        })
        .catch((error) => {
          rejected(error);
        });
      delete timer[key];
    }, 500);
  });
};
/**
 * Lấy toàn bộ vai trò (cách ly đơn vị thuê)
 * @param params
 */
export const getAllRolesList = (params) => defHttp.get({ url: Api.allRolesList, params });
/**
 * Lấy toàn bộ vai trò (không cách ly đơn vị thuê)
 * @param params
 */
export const getAllRolesListNoByTenant = (params) => defHttp.get({ url: Api.allRolesListNoByTenant, params });
/**
 * Lấy tất cả đơn vị thuê
 */
export const getAllTenantList = (params) => defHttp.get({ url: Api.allTenantList, params });
/**
 * Lấy phòng ban phụ trách của người dùng chỉ định
 */
export const getUserDepartList = (params) => defHttp.get({ url: Api.userDepartList, params }, { successMessageMode: 'none' });
/**
 * Lấy tất cả chức danh
 */
export const getAllPostList = (params) => {
  return new Promise((resolve) => {
    defHttp.get({ url: Api.allPostList, params }).then((res) => {
      resolve(res.records);
    });
  });
};
/**
 * Danh sách thùng rác
 * @param params
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * Khôi phục từ thùng rác
 * @param params
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putRecycleBin, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa trong thùng rác
 * @param params
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Đổi mật khẩu
 * @param params
 */
export const changePassword = (params) => {
  return defHttp.put({ url: Api.changePassword, params });
};
/**
 * Đóng băng/Bỏ đóng băng
 * @param params
 */
export const frozenBatch = (params, handleSuccess) => {
  return defHttp.put({ url: Api.frozenBatch, params }).then(() => {
    handleSuccess();
  });
};
/**
 * Đặt lại mật khẩu
 * @param params
 */
export const resetPassword = (params, handleSuccess) => {
  return defHttp.put({ url: Api.resetPassword, params },{joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
};


/**
 * Danh sách người dùng nghỉ việc
 * @param params
 */
export const getQuitList = (params) => {
  return defHttp.get({ url: Api.getQuitList, params });
};

/**
 * Hủy nghỉ việc
 * @param params
 */
export const putCancelQuit = (params, handleSuccess) => {
  return defHttp.put({ url: Api.putCancelQuit, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Chờ phê duyệt: lấy dữ liệu danh sách
 */
export const getUserTenantPageList = (params) => {
  return defHttp.get({ url: Api.getUserTenantPageList, params });
};

/**
 * Cập nhật trạng thái đơn vị thuê
 * @param params
 */
export const updateUserTenantStatus = (params) => {
  return defHttp.put({ url: Api.updateUserTenantStatus, params }, { joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * Dựa vào ID phòng ban và ID vị trí phòng ban đã chọn để lấy các vị trí thuộc phòng banid
 * 
 * @param params
 */
export const getDepPostIdByDepId = (params) => {
  return defHttp.get({ url: Api.getDepPostIdByDepId, params },{ isTransformResponse: false });
};
