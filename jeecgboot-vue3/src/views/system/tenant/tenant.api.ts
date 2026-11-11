import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';
import { getTenantId } from "/@/utils/auth";

enum Api {
  list = '/sys/tenant/list',
  save = '/sys/tenant/add',
  edit = '/sys/tenant/edit',
  get = '/sys/tenant/queryById',
  delete = '/sys/tenant/delete',
  deleteBatch = '/sys/tenant/deleteBatch',
  getCurrentUserTenants = '/sys/tenant/getCurrentUserTenant',
  invitationUserJoin = '/sys/tenant/invitationUserJoin',
  getTenantUserList = '/sys/tenant/getTenantUserList',
  leaveTenant = '/sys/tenant/leaveTenant',
  packList = '/sys/tenant/packList',
  addPackPermission = '/sys/tenant/addPackPermission',
  editPackPermission = '/sys/tenant/editPackPermission',
  // Quy tắc dữ liệu của menu gói dịch vụ
  packDataRule = '/sys/tenant/packDatarule',
  deleteTenantPack = '/sys/tenant/deleteTenantPack',
  recycleBinPageList = '/sys/tenant/recycleBinPageList',
  deleteLogicDeleted = '/sys/tenant/deleteLogicDeleted',
  revertTenantLogic = '/sys/tenant/revertTenantLogic',
  syncDefaultPack = '/sys/tenant/syncDefaultPack',
  //Quan hệ gói sản phẩm của người dùngapi
  queryTenantPackUserList = '/sys/tenant/queryTenantPackUserList',
  deleteTenantPackUser = '/sys/tenant/deleteTenantPackUser',
  addTenantPackUser = '/sys/tenant/addTenantPackUser',
  //Lấy danh sách đơn vị thuê của người dùng
  getTenantPageListByUserId = '/sys/tenant/getTenantPageListByUserId',
  
  //Thêm mới, chỉnh sửa đơn vị thuê của người dùng
  saveUser = '/sys/user/addTenantUser',
  editUser = '/sys/user/editTenantUser',
  //Dựa vào ID đơn vị thuê và người dùng để lấy danh sách gói sản phẩm của người dùng và gói sản phẩm thuộc người dùng hiện tạiid
  listPackByTenantUserId = '/sys/tenant/listPackByTenantUserId',
  // Lấy quyền menu
  queryPremTreeList = '/sys/role/queryTreeList',
}

/**
 * Truy vấn danh sách đơn vị thuê
 * @param params
 */
export const getTenantList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Lưu hoặc cập nhật đơn vị thuê
 * @param params
 */
export const saveOrUpdateTenant = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Truy vấn chi tiết đơn vị thuê
 * @param params
 */
export const getTenantById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Xóa đơn vị thuê
 * @param params
 */
export const deleteTenant = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt đơn vị thuê
 * @param params
 */
export const batchDeleteTenant = (params, handleSuccess) => {
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
 * Lấy thông tin phòng ban của người dùng đăng nhập
 */
export const getUserTenants = (params?) => defHttp.get({ url: Api.getCurrentUserTenants, params });

/**
 * Mời người dùng tham gia đơn vị thuê
 * @param params
 */
export const invitationUserJoin = (params) => defHttp.put({ url: Api.invitationUserJoin, params }, { joinParamsToUrl: true });

/**
 * Lấy dữ liệu thông qua ID đơn vị thuê
 * @param params
 */
export const getTenantUserList = (params) => {
  return defHttp.get({ url: Api.getTenantUserList, params });
};

/**
 * Người dùng rời khỏi đơn vị thuê
 * @param params
 */
export const leaveTenant = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xin nghỉ việc',
    content: 'Có mời người dùng này rời khỏi đơn vị thuê hiện tại không',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.put({ url: Api.leaveTenant, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * Lấy danh sách gói sản phẩm
 * @param params
 */
export const packList = (params) => {
  return defHttp.get({ url: Api.packList, params });
};

/**
 * Thêm menu
 * @param params
 */
export const addPackPermission = (params) => {
  return defHttp.post({ url: Api.addPackPermission, params });
};

/**
 * Thêm menu
 * @param params
 */
export const editPackPermission = (params) => {
  return defHttp.put({ url: Api.editPackPermission, params });
};

/**
 * Truy vấn quy tắc dữ liệu của menu gói dịch vụ
 * @param params functionId ID menu, packId: gói dịch vụid
 */
export const queryPackDataRule = (params) =>
  defHttp.get({ url: `${Api.packDataRule}/${params.functionId}/${params.packId}` }, { isTransformResponse: false });

/**
 * Lưu quy tắc dữ liệu của menu gói dịch vụ
 * @param params permissionId ID menu, packId: ID gói dịch vụ, dataRuleIds: quy tắc dữ liệuid
 */
export const savePackDataRule = (params) => defHttp.post({ url: Api.packDataRule, params });

/**
 * Xóa menu
 * @param params
 */
export const deleteTenantPack = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteTenantPack, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Khởi tạo gói dịch vụ
 * @param params
 * @param handleSuccess
 */
export const syncDefaultTenantPack = (params, handleSuccess) => {
  return defHttp.post({ url: Api.syncDefaultPack, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Lấy danh sách thùng rác của đơn vị thuê
 * @param params
 */
export const recycleBinPageList = (params) => {
  return defHttp.get({ url: Api.recycleBinPageList, params });
};

/**
 * Xóa vĩnh viễn đơn vị thuê
 * @param params
 */
export const deleteLogicDeleted = (params,handleSuccess) => {
  return defHttp.delete({ url: Api.deleteLogicDeleted, params },{ joinParamsToUrl: true }).then(() => {
    handleSuccess();
  }).catch(()=>{
    handleSuccess();
  });
};

/**
 * Khôi phục đơn vị thuê
 * @param params
 */
export const revertTenantLogic = (params,handleSuccess) => {
  return defHttp.put({ url: Api.revertTenantLogic, params },{ joinParamsToUrl: true }).then(() => {
    handleSuccess();
  })
};

/**
 * Lấy người dùng thuộc gói sản phẩm của đơn vị thuê
 * @param params
 */
export const queryTenantPackUserList = (params) => {
  return defHttp.get({ url: Api.queryTenantPackUserList, params });
};

/**
 * Loại bỏ dữ liệu quan hệ giữa người dùng và gói sản phẩm
 * @param params
 */
export const deleteTenantPackUser = (params)=>{
  return defHttp.put({ url: Api.deleteTenantPackUser, params });
}

/**
 * Thêm dữ liệu quan hệ giữa người dùng và gói sản phẩm
 * @param params
 */
export const addTenantPackUser = (params)=>{
  return defHttp.post({ url: Api.addTenantPackUser, params });
}

/**
 * Truy vấn danh sách đơn vị thuê của người dùng
 * @param params
 */
export const getTenantPageListByUserId = (params) => {
  return defHttp.get({ url: Api.getTenantPageListByUserId, params });
};


/**
 * Lấy tên đơn vị thuê đang đăng nhập
 */
export async function getLoginTenantName() {
  let tenantId = getTenantId();
  if(tenantId){
    let result = await getTenantById({ id:tenantId });
    if(result){
      return result.name;
    }
  }
  return "Trống";
}

/**
 * Lưu hoặc cập nhật người dùng
 * @param params
 */
export const saveOrUpdateTenantUser = (params, isUpdate) => {
  let url = isUpdate ? Api.editUser : Api.saveUser;
  return defHttp.post({ url: url, params },{ joinParamsToUrl: true });
};
/**
 * Dựa vào ID đơn vị thuê và người dùng để lấy danh sách gói sản phẩm của người dùng và gói sản phẩm thuộc người dùng hiện tạiid
 * 
 * @param params
 */
export const listPackByTenantUserId = (params) => {
  return defHttp.get({ url: Api.listPackByTenantUserId, params });
}

/**
 * Lấy cây menu
 */
export const queryPremTreeList = () =>{
  return defHttp.get({ url: Api.queryPremTreeList });
}
