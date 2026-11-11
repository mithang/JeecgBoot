import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/dataSource/list',
  save = '/sys/dataSource/add',
  edit = '/sys/dataSource/edit',
  get = '/sys/dataSource/queryById',
  delete = '/sys/dataSource/delete',
  testConnection = '/online/cgreport/api/testConnection',
  deleteBatch = '/sys/dataSource/deleteBatch',
  exportXlsUrl = 'sys/dataSource/exportXls',
  importExcelUrl = 'sys/dataSource/importExcel',
}
/**
 * API xuất
 */
export const getExportUrl = Api.exportXlsUrl;
/**
 * API nhập
 */
export const getImportUrl = Api.importExcelUrl;

/**
 * Truy vấn danh sách nguồn dữ liệu
 * @param params
 */
export const getDataSourceList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Lưu hoặc cập nhật nguồn dữ liệu
 * @param params
 */
export const saveOrUpdateDataSource = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Truy vấn chi tiết nguồn dữ liệu
 * @param params
 */
export const getDataSourceById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Xóa nguồn dữ liệu
 * @param params
 */
export const deleteDataSource = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Kiểm tra kết nối
 * @param params
 */
export const testConnection = (params) => {
  return defHttp.post({ url: Api.testConnection, params });
};

/**
 * Xóa hàng loạt nguồn dữ liệu
 * @param params
 */
export const batchDeleteDataSource = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn không?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.deleteBatch, data: params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
