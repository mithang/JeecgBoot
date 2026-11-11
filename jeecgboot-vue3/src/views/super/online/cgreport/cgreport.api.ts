import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
const { createConfirm } = useMessage();

enum Api {
  list = '/online/cgreport/head/list',
  save = '/online/cgreport/head/add',
  edit = '/online/cgreport/head/editAll',
  deleteOne = '/online/cgreport/head/delete',
  deleteBatch = '/online/cgreport/head/deleteBatch',
  copy = '/online/cgreport/head/copy',
  onlCgreportParamList = '/online/cgreport/param/listByHeadId',
  onlCgreportItemList = '/online/cgreport/item/listByHeadId',
  getDataSourceList = '/sys/dataSource/options',
  getParamsInfo = '/online/cgreport/api/getParamsInfo/',
  analyzeSql = '/online/cgreport/head/parseSql',
}

/**
 * Truy vấn dữ liệu bảng con
 * @param params
 */
export const onlCgreportParamList = Api.onlCgreportParamList;
/**
 * Truy vấn dữ liệu bảng con
 * @param params
 */
export const onlCgreportItemList = Api.onlCgreportItemList;
/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

/**
 * Xóa đơn lẻ
 */
export const deleteOne = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteOne, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn hay không',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    iconType: 'warning',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * Lấy địa chỉ tham số
 * @param params
 */
export const getReportParam = (id) => {
  return defHttp.get({ url: Api.getParamsInfo + id });
};

/**
 * Lấy danh sách nguồn dữ liệu
 */
export const getDataSourceList = () => {
  return defHttp.get({ url: Api.getDataSourceList });
};

/**
 * Sao chép báo cáo
 * @param id Báo cáoid
 */
export const doCopy = (id) => {
  // Lưu ý:defHttp cái không phải của GET Yêu cầu sẽ đưa params Coi như thân yêu cầu (request body) data, backend @RequestParam từ query lấy tham số,
  // Vì vậy, ở đây đưa id Nối trực tiếp vào URL phía trên (và cgform của doCopyOnlineView giữ nhất quán)
  return defHttp.post({ url: `${Api.copy}?id=${id}` });
};

/**
 * Phân tích (parse)sql
 * @param params
 */
export const analyzeSql = (params) => {
  return defHttp.get({
    url: Api.analyzeSql + '?' + params,
  });
};
