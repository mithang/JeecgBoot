import { defHttp } from '/@/utils/http/axios';

enum Api {
  queryDiskInfo = '/sys/actuator/redis/queryDiskInfo',
}

/**
 * Thông tin chi tiết
 */
export const queryDiskInfo = () => {
  return defHttp.get({ url: Api.queryDiskInfo }, { successMessageMode: 'none' });
};
