import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/airag/prompts/list',
  queryById = '/airag/prompts/queryById',
  save='/airag/prompts/add',
  edit='/airag/prompts/edit',
  deleteOne = '/airag/prompts/delete',
  deleteBatch = '/airag/prompts/deleteBatch',
  importExcel = '/airag/prompts/importExcel',
  exportXls = '/airag/prompts/exportXls',

  promptExperiment = '/airag/prompts/experiment',
  recycleBinList = '/airag/prompts/recycleBinList',
  revertRecycleBin = '/airag/prompts/revertRecycleBin',
  deleteRecycleBin = '/airag/prompts/deleteRecycleBin',
}
/**
 * api xuất
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * api nhập
 */
export const getImportUrl = Api.importExcel;
/**
 * Giao diện danh sách
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * Truy vấn chi tiết prompt theo ID
 * @param id ID prompt
 */
export const queryById = (id: string) =>
  defHttp.get({url: Api.queryById, params: {id}});

/**
 * Xóa đơn lẻ
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}
/**
 * Xóa hàng loạt
 * @param params
 */
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: 'Xác nhận xóa',
    content: 'Có xóa dữ liệu đã chọn hay không',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}
/**
 * Lưu hoặc cập nhật
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}
/**
 * Thử nghiệm
 * @param params
 */
export const promptExperiment = (params) => {
  return defHttp.post({url: Api.promptExperiment, params},{ isTransformResponse: false });
}
/**
 * Danh sách thùng rác
 */
export const getRecycleBinList = (params) => defHttp.get({ url: Api.recycleBinList, params });
/**
 * Khôi phục
 */
export const putRecycleBin = (params, handleSuccess) => {
  return defHttp.put({ url: Api.revertRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
/**
 * Xóa triệt để
 */
export const deleteRecycleBin = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.deleteRecycleBin, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};
