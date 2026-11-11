import { defHttp } from '/@/utils/http/axios';
import { cloneObject } from '/@/utils/index';

export const backEndUrl = {
  // Lấy ứng dụng bên thứ ba đã bậtApp
  getEnabledType: '/sys/thirdApp/getEnabledType',
  // WeCom
  wechatEnterprise: {
    user: '/sys/thirdApp/sync/wechatEnterprise/user',
    depart: '/sys/thirdApp/sync/wechatEnterprise/depart',
  },
  // DingTalk
  dingtalk: {
    user: '/sys/thirdApp/sync/dingtalk/user',
    depart: '/sys/thirdApp/sync/dingtalk/depart',
  },
};
// Đã kích hoạt những App bên thứ ba nào (cache tại đây)
let enabledTypes = null;

// Lấy ứng dụng bên thứ ba đã bậtApp
export const getEnabledTypes = async () => {
  // Lấy cache
  if (enabledTypes != null) {
    return cloneObject(enabledTypes);
  } else {
    let { success, result } = await defHttp.get({ url: backEndUrl.getEnabledType }, { isTransformResponse: false });
    if (success) {
      // Lưu cache tại đây
      enabledTypes = cloneObject(result);
      return result;
    } else {
      console.warn('getEnabledTypeTìm kiếm thất bại:');
    }
  }
  return {};
};
