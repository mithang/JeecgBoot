import { defHttp } from '@/utils/http/axios';

enum Api {
  submit = '/airag/video/submit',
  query = '/airag/video/query',
  prompts = '/airag/video/prompts',
}

/**
 * Gửi nhiệm vụ tạo video
 */
export const submitVideoTask = (params: { prompt: string; category?: string }) => defHttp.post({ url: Api.submit, params });

/**
 * Truy vấn trạng thái nhiệm vụ tạo video
 */
export const queryVideoTask = (taskId: string) => defHttp.get({ url: `${Api.query}/${taskId}` });

/**
 * Lấy prompt đặt sẵn
 */
export const getPresetPrompts = () => defHttp.get({ url: Api.prompts });
