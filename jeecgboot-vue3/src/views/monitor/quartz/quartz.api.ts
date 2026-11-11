import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/sys/quartzJob/list',
  save = '/sys/quartzJob/add',
  edit = '/sys/quartzJob/edit',
  get = '/sys/quartzJob/queryById',
  pause = '/sys/quartzJob/pause',
  resume = '/sys/quartzJob/resume',
  delete = '/sys/quartzJob/delete',
  exportXlsUrl = '/sys/quartzJob/exportXls',
  importExcelUrl = '/sys/quartzJob/importExcel',
  execute = '/sys/quartzJob/execute',
  deleteBatch = '/sys/quartzJob/deleteBatch',
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
 * Truy vấn danh sách nhiệm vụ
 * @param params
 */
export const getQuartzList = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Lưu hoặc cập nhật nhiệm vụ
 * @param params
 */
export const saveOrUpdateQuartz = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({ url: url, params });
};

/**
 * Truy vấn chi tiết nhiệm vụ
 * @param params
 */
export const getQuartzById = (params) => {
  return defHttp.get({ url: Api.get, params });
};

/**
 * Xóa nhiệm vụ
 * @param params
 */
export const deleteQuartz = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.delete, data: params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Khởi động
 * @param params
 */
export const resumeJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.resume, params }).then(() => {
    handleSuccess();
  });
};

/**
 * Tạm dừng
 * @param params
 */
export const pauseJob = (params, handleSuccess) => {
  return defHttp.get({ url: Api.pause, params }).then(() => {
    handleSuccess();
  });
};

/**
 * Thực thi ngay
 * @param params
 */
export const executeImmediately = (params, handleSuccess) => {
  return defHttp.get({ url: Api.execute, params }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt nhiệm vụ
 * @param params
 */
export const batchDeleteQuartz = (params, handleSuccess) => {
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
