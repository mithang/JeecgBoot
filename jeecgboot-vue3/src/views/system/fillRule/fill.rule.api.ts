import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/fillRule/list',
  test = '/sys/fillRule/testFillRule',
  save = '/sys/fillRule/add',
  edit = '/sys/fillRule/edit',
  delete = '/sys/fillRule/delete',
  deleteBatch = '/sys/fillRule/deleteBatch',
  exportXls = '/sys/fillRule/exportXls',
  importExcel = '/sys/fillRule/importExcel',
}

/**
 * Xuất địa chỉ
 */
export const exportUrl = Api.exportXls;
/**
 * Nhập địa chỉ
 */
export const importUrl = Api.importExcel;

/**
 * Truy vấn danh sách
 * @param params
 */
export const getFillRuleList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Xóa
 * @param params
 * @param handleSuccess
 */
export const deleteFillRule = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDeleteFillRule = (params, handleSuccess) => {
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
 * Kiểm tra chức năng quy tắc
 * @param params
 */
export const handleTest = (params) => {
  return defHttp.get({ url: Api.test, params }, { isTransformResponse: false });
};

/**
 * Lưu
 * @param params
 */
export const saveFillRule = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * Cập nhật
 * @param params
 */
export const updateFillRule = (params) => {
  return defHttp.put({ url: Api.edit, params });
};
