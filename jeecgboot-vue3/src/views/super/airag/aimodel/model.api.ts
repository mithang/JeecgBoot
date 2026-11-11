import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  list = '/airag/airagModel/list',
  save = '/airag/airagModel/add',
  testConn = '/airag/airagModel/test',
  delete = '/airag/airagModel/delete',
  queryById = '/airag/airagModel/queryById',
  edit = '/airag/airagModel/edit',
  copy = '/airag/airagModel/copy',
}

/**
 * Truy vấn mô hình AI
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Truy vấn mô hình AI theo id
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Thêm mới mô hình AI
 *
 * @param params
 */
export const saveModel = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * Chỉnh sửa mô hình AI
 *
 * @param params
 */
export const editModel = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Sao chép mô hình AI
 *
 * @param id
 * @author scott
 * @since 2026-08-06 LHZP-1552 Cấu hình mô hình AI thêm chức năng sao chép
 */
export const copyModel = (id) => {
  return defHttp.post({ url: `${Api.copy}/${id}` });
};

/**
 * Kiểm tra liên kết
 *
 * @param params
 */
export const testConn = (params) => {
  return defHttp.post({ url: Api.testConn, params, timeout: 2*60*1000 });
};

/**
 * Quyền xóa dữ liệu
 */
export const deleteModel = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Có xóa tên là' + params.name + 'mô hình?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};
