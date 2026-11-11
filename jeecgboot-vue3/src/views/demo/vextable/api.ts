import { defHttp } from '/@/utils/http/axios';

enum Api {
  list = '/test/jeecgOrderMain/list',
  delete = '/test/jeecgOrderMain/delete',
  orderCustomerList = '/test/jeecgOrderMain/queryOrderCustomerListByMainId',
  orderTicketList = '/test/jeecgOrderMain/queryOrderTicketListByMainId',
}

/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });
/**
 * Thông tin biểu mẫu con
 * @param params
 */
export const orderTicketList = (params) => defHttp.get({ url: Api.orderTicketList, params });
/**
 * Thông tin biểu mẫu con
 * @param params
 */
export const orderCustomerList = (params) => defHttp.get({ url: Api.orderCustomerList, params });
/**
 * Xóa người dùng
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
