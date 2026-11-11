import { defHttp } from "/@/utils/http/axios";

enum Api {
  userEdit='/sys/user/login/setting/userEdit',
  getUserData='/sys/user/login/setting/getUserData',
  queryNameByCodes='/sys/position/queryByCodes',
  updateMobile='/sys/user/updateMobile',
  updateUserPassword='/sys/user/passwordChange',
  getTenantListByUserId = '/sys/tenant/getTenantListByUserId',
  cancelApplyTenant = '/sys/tenant/cancelApplyTenant',
  exitUserTenant = '/sys/tenant/exitUserTenant',
  changeOwenUserTenant = '/sys/tenant/changeOwenUserTenant',
  getThirdAccountByUserId = '/sys/thirdApp/getThirdAccountByUserId',
  bindThirdAppAccount = '/sys/thirdApp/bindThirdAppAccount',
  deleteThirdAccount = '/sys/thirdApp/deleteThirdAccount',
  agreeOrRefuseJoinTenant = '/sys/tenant/agreeOrRefuseJoinTenant',
  //Thay đổi số điện thoại
  changePhone = '/sys/user/changePhone',
  //Hủy tài khoản người dùng
  userLogOff = '/sys/user/userLogOff',
  //Địa chỉ yêu cầu đổi mật khẩu dùng khi chưa liên kết số điện thoại
  updatePasswordNotBindPhone = '/sys/user/updatePasswordNotBindPhone',
}

/**
 * Chỉnh sửa người dùng
 * @param params
 */
export const userEdit = (params) => {
  return defHttp.post({ url: Api.userEdit, params },{ isTransformResponse:false });
}

/**
 * Lấy thông tin người dùng
 * @param params
 */
export const getUserData = () => {
  return defHttp.get({ url: Api.getUserData },{ isTransformResponse:false });
}

/**
 * Lấy thông tin nhiều chức danh
 * @param params
 */
export const queryNameByCodes = (params) => {
  return defHttp.get({ url: Api.queryNameByCodes, params },{isTransformResponse:false});
}

/**
 * Đổi số điện thoại
 * @param params
 */
export const updateMobile = (params) => {
  return defHttp.put({ url: Api.updateMobile, params },{isTransformResponse:false});
}

/**
 * Đổi mật khẩu
 * @param params
 */
export const updateUserPassword = (params) => {
  return defHttp.get({ url: Api.updateUserPassword, params },{isTransformResponse:false});
}

/**
 * Đổi mật khẩu
 * @param params
 */
export const updatePasswordNotBindPhone = (params) => {
  return defHttp.put({ url: Api.updatePasswordNotBindPhone, params },{ isTransformResponse:false, joinParamsToUrl: true });
}

/**
 * Lấy danh sách đơn vị thuê thông qua ID người dùng
 * @param params
 */
export const getTenantListByUserId = (params) => {
  return defHttp.get({ url: Api.getTenantListByUserId, params }, { isTransformResponse: false });
};

/**
 * Hủy đơn đăng ký
 * @param params
 */
export const cancelApplyTenant = (params) => {
  return defHttp.put({ url: Api.cancelApplyTenant, data: params }, { joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * Người dùng thoát khỏi đơn vị thuê
 * @param params
 */
export const exitUserTenant = (params)=>{
  return defHttp.delete({ url: Api.exitUserTenant, params },{ isTransformResponse: false, joinParamsToUrl: true });
}

/**
 * Thay đổi chủ sở hữu đơn vị thuê
 * @param params
 */
export const changeOwenUserTenant = (params)=>{
  return defHttp.post({ url: Api.changeOwenUserTenant, params },{ isTransformResponse: false, joinParamsToUrl: true });
}

/**
 * Lấy thông tin bên thứ ba của tài khoản thông qua loại bên thứ ba
 * @param params
 */
export const getThirdAccountByUserId = (params) => {
  return defHttp.get({ url: Api.getThirdAccountByUserId, params }, { isTransformResponse: false });
};

/**
 * Liên kết tài khoản dựa theo UUID của bên thứ ba
 * @param params
 */
export const bindThirdAppAccount = (params) => {
  return defHttp.post({ url: Api.bindThirdAppAccount, params }, { isTransformResponse: false, joinParamsToUrl: true });
};

/**
 * Liên kết tài khoản dựa theo UUID của bên thứ ba
 * @param params
 */
export const deleteThirdAccount = (params) => {
  return defHttp.delete({ url: Api.deleteThirdAccount, params }, { isTransformResponse:false, joinParamsToUrl: true });
};

/**
 * Đồng ý và từ chối tham gia đơn vị thuê
 * @param params
 */
export const agreeOrRefuseJoinTenant = (params) => {
  return defHttp.put({ url: Api.agreeOrRefuseJoinTenant, params },{ joinParamsToUrl: true });
};

/**
 * Thay đổi số điện thoại
 * @param params
 */
export const changePhone = (params) => {
  return defHttp.put({ url: Api.changePhone, params },{ joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * Hủy tài khoản người dùng
 * @param params
 */
export const userLogOff = (params) => {
  return defHttp.put({ url: Api.userLogOff, params },{ isTransformResponse:false });
}
