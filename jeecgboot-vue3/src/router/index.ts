import type { RouteRecordRaw } from 'vue-router';
import type { App } from 'vue';

import { $electron } from "@/electron";
import { basicRoutes } from './routes';
import {createRouter as createVueRouter, destroyRouter, router} from './router'

// Danh sách trắng nên chứa các route tĩnh cơ bản
const WHITE_NAME_LIST: string[] = [];
// Route Online được đăng ký khi ứng dụng khởi động; khi đăng xuất, các route quyền cần được giữ lại khi đặt lại, nếu không sau khi đăng nhập lại sẽ gặp lỗi 404.
const ONLINE_ROUTE_NAMES = [
  'onl-auto-cgform-router',
  'OnlineAutoList',
  'DefaultOnlineList',
  'CgformErpList',
  'OnlCgformInnerTableList',
  'OnlCgformTabList',
  'onl-auto-cgreport-router',
  'OnlCgReportList',
  'onl-auto-graphreport-router',
  'GraphreportAutoChart',
  'onl-auto-desform-router',
  'AutoDesformDataList'
];
const getRouteNames = (array: any[]) =>
  array.forEach((item) => {
    WHITE_NAME_LIST.push(item.name);
    getRouteNames(item.children || []);
  });
getRouteNames(basicRoutes);

/**
 * Tạo instance route
 */
export function createRouter() {
  let router = createVueRouter({
      routes: basicRoutes as unknown as RouteRecordRaw[],
      strict: true,
      scrollBehavior: () => ({left: 0, top: 0}),
    },
    // Nếu là môi trường Electron thì dùng hash route
    $electron.isElectron(),
  )

  // TODO 【QQYUN-4517】【Trình thiết kế biểu mẫu】Ghi lại kiểm tra route guard chia sẻ
  router.beforeEach(async (to, from) => {
    //console.group('【QQYUN-4517】beforeEach');
    //console.warn('from', from);
    //console.warn('to', to);
    //console.groupEnd();
  });
}

// reset router
export function resetRouter() {
  router.getRoutes().forEach((route) => {
    const { name } = route;
    if (name && !WHITE_NAME_LIST.includes(name as string) && !ONLINE_ROUTE_NAMES.includes(name as string)) {
      router.hasRoute(name) && router.removeRoute(name);
    }
  });
}

// config router
export function setupRouter(app: App<Element>) {
  app.use(router);
}

export {
  router,
  destroyRouter,
}
