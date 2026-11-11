import { defHttp } from '/@/utils/http/axios';

enum Api {
  keysSize = '/sys/actuator/redis/keysSize',
  memoryInfo = '/sys/actuator/redis/memoryInfo',
  info = '/sys/actuator/redis/info',
  metricsHistory = '/sys/actuator/redis/metrics/history',
}

/**
 * Số lượng key
 */
export const getKeysSize = () => {
  return defHttp.get({ url: Api.keysSize }, { isTransformResponse: false });
};

/**
 * Thông tin bộ nhớ
 */
export const getMemoryInfo = () => {
  return defHttp.get({ url: Api.memoryInfo }, { isTransformResponse: false });
};

/**
 * Thông tin chi tiết
 */
export const getInfo = () => {
  return defHttp.get({ url: Api.info });
};

/**
 * Bản ghi giám sát lịch sử
 */
export const getMetricsHistory = () => {
  return defHttp.get({ url: Api.metricsHistory });
};

export const getRedisInfo = () => {
  return Promise.all([getKeysSize(), getMemoryInfo()]);
};
