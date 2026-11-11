import { defHttp } from '/@/utils/http/axios';

enum Api {
  saveTenantJoinUser = '/sys/tenant/saveTenantJoinUser',
  joinTenantByHouseNumber = '/sys/tenant/joinTenantByHouseNumber',
}

/**
 * Lưu đơn vị thuê (tenant)
 * @param params
 */
export const saveTenantJoinUser = (params) => {
  return defHttp.post({ url: Api.saveTenantJoinUser, params }, { isTransformResponse: false });
};

/**
 * Tham gia đơn vị thuê
 * @param params
 */
export const joinTenantByHouseNumber = (params) => {
  return defHttp.post({ url: Api.joinTenantByHouseNumber, params }, { isTransformResponse: false });
};
