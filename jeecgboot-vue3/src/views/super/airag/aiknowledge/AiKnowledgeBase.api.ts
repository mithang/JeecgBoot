import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

enum Api {
  //Quản lý kho tri thức
  list = '/airag/knowledge/list',
  save = '/airag/knowledge/add',
  delete = '/airag/knowledge/delete',
  queryById = '/airag/knowledge/queryById',
  edit = '/airag/knowledge/edit',
  rebuild = '/airag/knowledge/rebuild',
  //Tài liệu kho tri thức
  knowledgeDocList = '/airag/knowledge/doc/list',
  knowledgeEditDoc = '/airag/knowledge/doc/edit',
  knowledgeDeleteBatchDoc = '/airag/knowledge/doc/deleteBatch',
  knowledgeDeleteAllDoc = '/airag/knowledge/doc/deleteAll',
  knowledgeRebuildDoc = '/airag/knowledge/doc/rebuild',
  knowledgeEmbeddingHitTest = '/airag/knowledge/embedding/hitTest',
}

/**
 * Truy vấn kho tri thức
 * @param params
 */
export const list = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Truy vấn kho tri thức theo id
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Thêm mới kho tri thức
 * @param params
 */
export const saveKnowledge = (params) => {
  return defHttp.post({ url: Api.save, params });
};

/**
 * Chỉnh sửa kho tri thức
 *
 * @param params
 */
export const editKnowledge = (params) => {
  return defHttp.put({ url: Api.edit, params });
};

/**
 * Xóa kho tri thức
 */
export const deleteModel = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Có xóa tên là'+params.name+'kho tri thức?',
    okText: 'Xác nhận',
    cancelText: 'Hủy',
    onOk: () => {
      return defHttp.delete({ url: Api.delete, params }, { joinParamsToUrl: true }).then(() => {
        handleSuccess();
      });
    },
  });
};

/**
 * Truy vấn chi tiết kho tri thức
 * @param params
 */
export const knowledgeDocList = (params) => {
  return defHttp.get({ url: Api.knowledgeDocList, params }, { isTransformResponse: false });
};

/**
 * Vector hóa kho tri thức
 *
 * @param params
 */
export const rebuild = (params) => {
  return defHttp.put({ url: Api.rebuild, params,timeout: 2 * 60 * 1000 }, { joinParamsToUrl: true, isTransformResponse: false });
};

/**
 * Thêm mới kho tri thức
 * @param params
 */
export const knowledgeSaveDoc = (params) => {
  return defHttp.post({ url: Api.knowledgeEditDoc, params });
};

/**
 * Vector hóa tài liệu
 * @param params
 */
export const knowledgeRebuildDoc = (params, handleSuccess) => {
  return defHttp.put({ url: Api.knowledgeRebuildDoc, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt tài liệu
 *
 * @param params
 * @param handleSuccess
 */
export const knowledgeDeleteBatchDoc = (params, handleSuccess) => {
  return defHttp.delete({ url: Api.knowledgeDeleteBatchDoc, params }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Xóa hàng loạt tài liệu
 *
 * @param params
 * @param handleSuccess
 */
export const knowledgeDeleteAllDoc = (knowId: string, handleSuccess) => {
  return defHttp.delete({ url: Api.knowledgeDeleteAllDoc, params: {knowId} }, { joinParamsToUrl: true }).then(() => {
    handleSuccess();
  });
};

/**
 * Kiểm tra trúng
 * @param params
 */
export const knowledgeEmbeddingHitTest = (params) => {
  let url = Api.knowledgeEmbeddingHitTest + '/' + params.knowId;
  return defHttp.get({ url: url, params }, { isTransformResponse: false });
};
