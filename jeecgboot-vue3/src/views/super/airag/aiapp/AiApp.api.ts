import { defHttp } from '/@/utils/http/axios';
import { Modal } from 'ant-design-vue';

export enum Api {
  //Quản lý kho tri thức
  list = '/airag/app/list',
  save = '/airag/app/edit',
  copy = '/airag/app/copy',
  release = '/airag/app/release',
  delete = '/airag/app/delete',
  queryById = '/airag/app/queryById',
  queryBathById = '/airag/knowledge/query/batch/byId',
  queryKnowledgeById = '/airag/knowledge/queryById',
  queryFlowById = '/airag/flow/queryById',
  queryFlowByIds = '/airag/flow/list',
  promptGenerate = '/airag/app/prompt/generate',
  generateMemoryByAppId = '/airag/app/prompt/generateMemoryByAppId',
}

/**
 * Truy vấn ứng dụng
 * @param params
 */
export const appList = (params) => {
  return defHttp.get({ url: Api.list, params }, { isTransformResponse: false });
};

/**
 * Truy vấn kho tri thức
 * @param params
 */
export const queryKnowledgeBathById = (params) => {
  return defHttp.get({ url: Api.queryBathById, params }, { isTransformResponse: false });
};

/**
 * Truy vấn kho tri thức(Đơn lẻ)
 * @param params
 */
export const queryKnowledgeById = (params) => {
  return defHttp.get({ url: Api.queryKnowledgeById, params }, { isTransformResponse: false });
};

/**
 * Truy vấn ứng dụng theo id ứng dụng
 * @param params
 */
export const queryById = (params) => {
  return defHttp.get({ url: Api.queryById, params }, { isTransformResponse: false });
};

/**
 * Thêm mới ứng dụng
 * @param params
 */
export const saveApp = (params) => {
  return defHttp.put({ url: Api.save, params });
};

/**
 * Sao chép ứng dụng
 * @param params
 * @author scott
 * @since 2026-08-06 【LHZP-1512】AIsince 2026-08-06 Thêm giao diện sao chép phía backend cho ứng dụng AI
 */
export const copyApp = (params) => {
  return defHttp.post({ url: Api.copy, params }, { joinParamsToUrl: true });
};

// Phát hành ứng dụng
export function releaseApp(appId: string, release = false) {
  return defHttp.post({
    url: Api.release,
    params: {
      id: appId,
      release: release,
    }
  }, {joinParamsToUrl: true, isTransformResponse: false});
}

/**
 * Xóa ứng dụng
 * @param params
 * @param handleSuccess
 */
export const deleteApp = (params, handleSuccess) => {
  Modal.confirm({
    title: 'Xác nhận xóa',
    content: 'Có xóa tên là'+params.name+'ứng dụng?',
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
 * Truy vấn quy trình theo id ứng dụng
 * @param params
 */
export const queryFlowById = (params) => {
  return defHttp.get({ url: Api.queryFlowById, params }, { isTransformResponse: false });
};

/**
 * Truy vấn quy trình theo id ứng dụng
 * @param params
 */
export const queryFlowByIds = (params) => {
  return defHttp.get({ url: Api.queryFlowByIds, params }, { isTransformResponse: false });
};

/**
 * Sắp xếp ứng dụng
 * @param params
 */
export const promptGenerate = (params) => {
  return defHttp.post(
    {
      url: Api.promptGenerate+'?prompt='+ params.prompt,
      adapter: 'fetch',
      responseType: 'stream',
      timeout: 5 * 60 * 1000,
    },
    {
      isTransformResponse: false,
    }
  );
};

/**
 * Sắp xếp ứng dụng
 * @param params
 */
export const generateMemoryByAppId = (params) => {
  return defHttp.post(
    {
      url: Api.generateMemoryByAppId+'?variables='+ params.variables + '&memoryId='+ params.memoryId,
      adapter: 'fetch',
      responseType: 'stream',
      timeout: 60 * 60 * 1000,
    },
    {
      isTransformResponse: false,
    }
  );
};
