import { defHttp } from '/@/utils/http/axios';

enum Api {
  //Cấu hình đăng nhập bên thứ ba
  addThirdAppConfig = '/sys/thirdApp/addThirdAppConfig',
  editThirdAppConfig = '/sys/thirdApp/editThirdAppConfig',
  getThirdConfigByTenantId = '/sys/thirdApp/getThirdConfigByTenantId',
  syncDingTalkDepartUserToLocal = '/sys/thirdApp/sync/dingtalk/departAndUser/toLocal',
  getThirdUserByWechat = '/sys/thirdApp/getThirdUserByWechat',
  wechatEnterpriseToLocal = '/sys/thirdApp/sync/wechatEnterprise/departAndUser/toLocal',
  getThirdUserBindByWechat = '/sys/thirdApp/getThirdUserBindByWechat',
  deleteThirdAccount = '/sys/thirdApp/deleteThirdAccount',
  deleteThirdAppConfig = '/sys/thirdApp/deleteThirdAppConfig',
  // update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu
  syncFeishuDepartUserToLocal = '/sys/thirdApp/sync/feishu/departAndUser/toLocal',
  // update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu
}

/**
 * Lưu hoặc cập nhật cấu hình bên thứ ba
 */
export const saveOrUpdateThirdConfig = (params, isUpdate) => {
  let url = isUpdate ? Api.editThirdAppConfig : Api.addThirdAppConfig;
  return defHttp.post({ url: url, params }, { joinParamsToUrl: true });
};

/**
 * Lấy cấu hình bên thứ ba
 * @param params
 */
export const getThirdConfigByTenantId = (params) => {
  return defHttp.get({ url: Api.getThirdConfigByTenantId, params });
};

/**
 * Đồng bộ người dùng phòng ban DingTalk về cục bộ
 * @param params
 */
export const syncDingTalkDepartUserToLocal = () => {
  return defHttp.get({ url: Api.syncDingTalkDepartUserToLocal, timeout: 60000 }, { isTransformResponse: false });
};

/**
 * Lấy thông tin người dùng đã liên kết WeCom
 * @param params
 */
export const getThirdUserByWechat = () => {
  return defHttp.get({ url: Api.getThirdUserByWechat }, { isTransformResponse: false });
};

/**
 * Đồng bộ người dùng và phòng ban WeCom về cục bộ
 * @param params
 */
export const wechatEnterpriseToLocal = (params) => {
  return defHttp.get({ url: Api.wechatEnterpriseToLocal, params }, { isTransformResponse: false });
};

/**
 * Lấy người dùng liên kết WeCom
 * @param params
 */
export const getThirdUserBindByWechat = () => {
  return defHttp.get({ url: Api.getThirdUserBindByWechat }, { isTransformResponse: false });
};

/**
 * Dựa vào ID của bảng tài khoản bên thứ ba để gỡ liên kết tài khoản
 * @param params
 */
export const deleteThirdAccount = (params) => {
  return defHttp.delete({ url: Api.deleteThirdAccount, params }, { isTransformResponse:false, joinParamsToUrl: true });
};

/**
 * Dựa vào ID của bảng cấu hình để xóa cấu hình bên thứ ba
 * @param params
 * @param handleSuccess
 */
export const deleteThirdAppConfig = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteThirdAppConfig, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Đồng bộ phòng ban và người dùng Feishu về cục bộ (Feishu→cục bộ）
 */
export const syncFeishuDepartUserToLocal = () => {
  return defHttp.get({ url: Api.syncFeishuDepartUserToLocal, timeout: 120000 }, { isTransformResponse: false });
};
