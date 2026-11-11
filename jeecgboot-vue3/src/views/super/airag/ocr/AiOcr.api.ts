import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

export enum Api {
  list = '/airag/ocr/list',
  add = '/airag/ocr/add',
  edit = '/airag/ocr/edit',
  deleteById = '/airag/ocr/deleteById',
  flowRun = '/airag/flow/run',
}

/**
 * Truy vấn danh sách ocr
 *
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params });
};

/**
 * Thêm Orc
 * @param params
 * @param handleSuccess
 */
export const addOcr = (params) => {
  return defHttp.post({ url: Api.add, params });
};

/**
 * Chỉnh sửa Orc
 * @param params
 * @param handleSuccess
 */
export const editOcr = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Xóa Orc theo id
 * @param params
 * @param handleSuccess
 */
export const deleteOcrById = (params) => {
  return defHttp.delete({ url: Api.deleteById, params });
};
