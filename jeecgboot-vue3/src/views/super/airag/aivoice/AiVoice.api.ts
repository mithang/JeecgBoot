import { defHttp } from '@/utils/http/axios';

enum Api {
  generate = '/airag/voice/generate',
  generateAsync = '/airag/voice/generateAsync',
  queryTask = '/airag/voice/queryTask',
  listByUser = '/airag/voice/listByUser',
  deleteVoiceRecord = '/airag/voice/deleteVoiceRecord',
}

/**
 * Gửi nhiệm vụ tạo giọng nói (đồng bộ, giữ tương thích)
 */
export const submitVoiceTask = (params) => defHttp.post({ url: Api.generate, params }, { isTransformResponse: false });

/**
 * Gửi bất đồng bộ nhiệm vụ tạo giọng nói, lập tức trả về taskId
 */
export const generateVoiceAsync = (params) => defHttp.post({ url: Api.generateAsync, params }, { isTransformResponse: false });

/**
 * Truy vấn kết quả nhiệm vụ giọng nói bất đồng bộ
 */
export const queryVoiceTask = (taskId: string) => defHttp.get({ url: `${Api.queryTask}/${taskId}` }, { isTransformResponse: false });

/**
 * Truy vấn danh sách giọng nói theo id người dùng
 */
export const getVoiceListByUser = (params: { userId: string }) => defHttp.get({ url: Api.listByUser, params },{ isTransformResponse: false });

/**
 * Xóa bản ghi giọng nói
 */
export const deleteVoiceRecord = (params) => defHttp.delete({ url: Api.deleteVoiceRecord, params }, { isTransformResponse: false, joinParamsToUrl: true });