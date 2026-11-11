import { defHttp } from '/@/utils/http/axios';
import { getMenuListResultModel } from './model/menuModel';
import { useUserStoreWithOut } from '@/store/modules/user';
import { setAuthCache } from '@/utils/auth';
import { TOKEN_KEY } from '@/enums/cacheEnum';
import { router } from '@/router';
import { PageEnum } from '@/enums/pageEnum';

enum Api {
  GetMenuList = '/sys/permission/getUserPermissionByToken',
  // 【QQYUN-8487】
  // SwitchVue3Menu = '/sys/switchVue3Menu',
}

/**
 * @description: Get user menu based on id
 */

export const getMenuList = () => {
  return new Promise((resolve) => {
    //Để tương thíchmockvà dữ liệu API
    defHttp.get<getMenuListResultModel>({ url: Api.GetMenuList }).then((res) => {
      if (Array.isArray(res)) {
        resolve(res);
      } else {
        resolve(res['menu']);
      }
    });
  });
};

/**
 * @description: Lấy quyền menu backend và quyền nút
 */
export function getBackMenuAndPerms() {
  return defHttp.get({ url: Api.GetMenuList }).catch((e) => {
    console.log('API getBackMenuAndPerms Thông tin lỗi ngoại lệ:', e);
    // TokenHết hạn không có hiệu lực, chuyển thẳng trang đăng nhập 2025-09-08 scott
    if (e && (e.message.includes('timeout') || e.message.includes('401') || e.message.includes('500'))) {
      const userStore = useUserStoreWithOut();
      userStore.setToken('');
      setAuthCache(TOKEN_KEY, null);
      router.push({
        path: PageEnum.BASE_LOGIN,
        query: {
          // Truyền route hiện tại, sau khi đăng nhập thành công sẽ chuyển đến route hiện tại
          redirect: router.currentRoute.value.fullPath,
        }
      });
    }
  });
}

/**
 * Chuyển thànhvue3Menu
 */
// export const switchVue3Menu = () => {
//   return new Promise((resolve) => {
//     defHttp.get({ url: Api.SwitchVue3Menu });
//   });
// };
