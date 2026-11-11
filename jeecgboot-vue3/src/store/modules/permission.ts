import type { AppRouteRecordRaw, Menu } from '/@/router/types';

import { defineStore } from 'pinia';
import { store } from '/@/store';
import { useI18n } from '/@/hooks/web/useI18n';
import { useUserStore } from './user';
import { useAppStoreWithOut } from './app';
import { toRaw } from 'vue';
import { transformObjToRoute, flatMultiLevelRoutes, addSlashToRouteComponent } from '/@/router/helper/routeHelper';
import { transformRouteToMenu } from '/@/router/helper/menuHelper';

import projectSetting from '/@/settings/projectSetting';

import { PermissionModeEnum } from '/@/enums/appEnum';

import { asyncRoutes } from '/@/router/routes';
import { ERROR_LOG_ROUTE, PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { staticRoutesList } from '../../router/routes/staticRouter';

import { filter } from '/@/utils/helper/treeHelper';

import { getBackMenuAndPerms } from '/@/api/sys/menu';

import { useMessage } from '/@/hooks/web/useMessage';
import { PageEnum } from '/@/enums/pageEnum';

// Quyền hệ thống
interface AuthItem {
  // Mã quyền menu, ví dụ: “sys:schedule:list,sys:schedule:info”, tách bằng nhiều dấu phẩy
  action: string;
  // Chính sách quyền: 1 Hiển thị, 2 Vô hiệu hóa
  type: string | number;
  // Trạng thái quyền (0 Không hợp lệ, 1 Có hiệu lực)
  status: string | number;
  // Tên quyền
  describe?: string;
  isAuth?: boolean;
}

interface PermissionState {
  // Permission code list
  permCodeList: string[] | number[];
  // Whether the route has been dynamically added
  isDynamicAddedRoute: boolean;
  // To trigger a menu update
  lastBuildMenuTime: number;
  // Backstage menu list
  backMenuList: Menu[];
  frontMenuList: Menu[];
  // Quyền người dùng sở hữu
  authList: AuthItem[];
  // Cấu hình tất cả quyền
  allAuthList: AuthItem[];
  // Chế độ an toàn hệ thống
  sysSafeMode: boolean;
  // Quyền nút bảng con của online
  onlineSubTableAuthMap: object;
}
export const usePermissionStore = defineStore('app-permission', {
  state: (): PermissionState => ({
    permCodeList: [],
    // Whether the route has been dynamically added
    isDynamicAddedRoute: false,
    // To trigger a menu update
    lastBuildMenuTime: 0,
    // Backstage menu list
    backMenuList: [],
    // menu List
    frontMenuList: [],
    authList: [],
    allAuthList: [],
    sysSafeMode: false,
    onlineSubTableAuthMap: {},
  }),
  getters: {
    getPermCodeList(): string[] | number[] {
      return this.permCodeList;
    },
    getBackMenuList(): Menu[] {
      return this.backMenuList;
    },
    getFrontMenuList(): Menu[] {
      return this.frontMenuList;
    },
    getLastBuildMenuTime(): number {
      return this.lastBuildMenuTime;
    },
    getIsDynamicAddedRoute(): boolean {
      return this.isDynamicAddedRoute;
    },

    // Giải thích logic mã: VUEN-1162 Nút bảng con chưa được kiểm soát
    getOnlineSubTableAuth: (state) => {
      return (code) => state.onlineSubTableAuthMap[code];
    },
  },
  actions: {
    setPermCodeList(codeList: string[]) {
      this.permCodeList = codeList;
    },

    setBackMenuList(list: Menu[]) {
      this.backMenuList = list;
      list?.length > 0 && this.setLastBuildMenuTime();
    },

    setFrontMenuList(list: Menu[]) {
      this.frontMenuList = list;
    },

    setLastBuildMenuTime() {
      this.lastBuildMenuTime = new Date().getTime();
    },

    setDynamicAddedRoute(added: boolean) {
      this.isDynamicAddedRoute = added;
    },
    resetState(): void {
      this.isDynamicAddedRoute = false;
      this.permCodeList = [];
      this.backMenuList = [];
      this.lastBuildMenuTime = 0;
    },
    async changePermissionCode() {
      const systemPermission = await getBackMenuAndPerms();
      const codeList = systemPermission.codeList;
      this.setPermCodeList(codeList);
      this.setAuthData(systemPermission);
      // Giải thích logic mã: [issues/9838] Khi phiên bản từ điển thay đổi, backend làm mới cache từ điển, không chặn việc xây dựng menu
      useUserStore()
        .refreshDictItemsIfChanged(systemPermission.dictVersion)
        .catch((e) => console.warn('Làm mới cache từ điển thất bại', e));

      // Route menu
      const routeList = systemPermission.menu;
      return routeList;
    },
    async buildRoutesAction(): Promise<AppRouteRecordRaw[]> {
      const { t } = useI18n();
      const userStore = useUserStore();
      const appStore = useAppStoreWithOut();

      let routes: AppRouteRecordRaw[] = [];
      const roleList = toRaw(userStore.getRoleList) || [];
      const { permissionMode = projectSetting.permissionMode } = appStore.getProjectConfig;

      const routeFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { roles } = meta || {};
        if (!roles) return true;
        return roleList.some((role) => roles.includes(role));
      };

      const routeRemoveIgnoreFilter = (route: AppRouteRecordRaw) => {
        const { meta } = route;
        const { ignoreRoute } = meta || {};
        return !ignoreRoute;
      };

      /**
       * @description Theo path trang chủ đã cài đặt, sửa đánh dấu affix trong routes (trang chủ cố định)
       * */
      const patchHomeAffix = (routes: AppRouteRecordRaw[]) => {
        if (!routes || routes.length === 0) return;
        let homePath: string = userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
        function patcher(routes: AppRouteRecordRaw[], parentPath = '') {
          if (parentPath) parentPath = parentPath + '/';
          routes.forEach((route: AppRouteRecordRaw) => {
            const { path, children, redirect } = route;
            const currentPath = path.startsWith('/') ? path : parentPath + path;
            if (currentPath === homePath) {
              if (redirect) {
                homePath = route.redirect! as string;
              } else {
                route.meta = Object.assign({}, route.meta, { affix: true });
                throw new Error('end');
              }
            }
            children && children.length > 0 && patcher(children, currentPath);
          });
        }
        try {
          patcher(routes);
        } catch (e) {
          // Đã xử lý xong thì thoát vòng lặp
        }
        return;
      };

      switch (permissionMode) {
        case PermissionModeEnum.ROLE:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          //  Chuyển route nhiều cấp thành hai cấp
          routes = flatMultiLevelRoutes(routes);
          break;

        case PermissionModeEnum.ROUTE_MAPPING:
          routes = filter(asyncRoutes, routeFilter);
          routes = routes.filter(routeFilter);
          const menuList = transformRouteToMenu(routes, true);
          routes = filter(routes, routeRemoveIgnoreFilter);
          routes = routes.filter(routeRemoveIgnoreFilter);
          menuList.sort((a, b) => {
            return (a.meta?.orderNo || 0) - (b.meta?.orderNo || 0);
          });

          this.setFrontMenuList(menuList);
          // Chuyển route nhiều cấp thành hai cấp
          routes = flatMultiLevelRoutes(routes);
          break;

        // Xây dựng menu backend
        case PermissionModeEnum.BACK:
          const { createMessage, createWarningModal } = useMessage();
          console.log(" --- Xây dựng menu route backend --- ")
          // Gợi ý tải menu
          // createMessage.loading({
          //   content: t('sys.app.menuLoading'),
          //   duration: 1,
          // });

          // Lấy mã quyền từ backend,
          // Hàm này có thể chỉ cần thực thi một lần, và trong dự án thực tế có thể đặt nó vào thời điểm thích hợp
          let routeList: AppRouteRecordRaw[] = [];
          try {
            routeList = await this.changePermissionCode();
            //routeList = (await getMenuList()) as AppRouteRecordRaw[];
            // let hasIndex: boolean = false;
            // let hasIcon: boolean = false;
            // for (let menuItem of routeList) {
            //   // Điều kiện 1: Kiểm tra component có phải là layouts/default/index không
            //   if (!hasIndex) {
            //     hasIndex = menuItem.component === 'layouts/default/index';
            //   }
            //   // Điều kiện 2: Kiểm tra icon có mang dấu hai chấm không
            //   if (!hasIcon) {
            //     hasIcon = !!menuItem.meta?.icon?.includes(':');
            //   }
            //   // Thỏa mãn bất kỳ điều kiện nào cũng thoát vòng lặp ngay
            //   if (hasIcon || hasIndex) {
            //     break;
            //   }
            // }
            // // Cả hai điều kiện đều không thỏa, thì bật hộp gợi ý
            // if (!hasIcon && !hasIndex) {
            //   // Trì hoãn 1.5 giây rồi mới hiện gợi ý, nếu không hộp gợi ý sẽ không hiện ra
            //   setTimeout(
            //     () =>
            //       createWarningModal({
            //         title: 'Nhắc nhở phát hiện',
            //         content:
            //           'Bảng menu hiện tại là <b>phiên bản Vue2</b>, khiến việc tải menu bị bất thường!<br>Nhấn xác nhận để chuyển sang menu phiên bản Vue3!',
            //         onOk:function () {
            //           switchVue3Menu();
            //           location.reload();
            //         }
            //       }),
            //     100
            //   );
            // }
          } catch (error) {
            console.error(error);
          }
          // Xử lý thêm dấu gạch chéo trước địa chỉ component  author: lsq date:2021-09-08
          routeList = addSlashToRouteComponent(routeList);
          // Import động component
          routeList = transformObjToRoute(routeList);

          // Xây dựng menu route backend
          const backMenuList = transformRouteToMenu(routeList);
          this.setBackMenuList(backMenuList);

          // Xóa mục meta.ignoreRoute
          routeList = filter(routeList, routeRemoveIgnoreFilter);
          routeList = routeList.filter(routeRemoveIgnoreFilter);

          routeList = flatMultiLevelRoutes(routeList);
          // Giải thích logic mã: 【TV360X-522】Route của trợ lý AI được viết cứng ở frontend
          routes = [PAGE_NOT_FOUND_ROUTE, ...routeList, ...staticRoutesList];
          break;
      }

      routes.push(ERROR_LOG_ROUTE);
      patchHomeAffix(routes);
      return routes;
    },
    setAuthData(systemPermission) {
      this.authList = systemPermission.auth;
      this.allAuthList = systemPermission.allAuth;
      this.sysSafeMode = systemPermission.sysSafeMode;
    },
    setAuthList(authList: AuthItem[]) {
      this.authList = authList;
    },
    setAllAuthList(authList: AuthItem[]) {
      this.allAuthList = authList;
    },

    // Giải thích logic mã: VUEN-1162 Nút bảng con chưa được kiểm soát
    setOnlineSubTableAuth(code, hideBtnList) {
      this.onlineSubTableAuthMap[code] = hideBtnList;
    },
  },
});

// Cần dùng bên ngoài setup
export function usePermissionStoreWithOut() {
  return usePermissionStore(store);
}
