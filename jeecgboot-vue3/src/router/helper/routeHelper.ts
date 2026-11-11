import type { AppRouteModule, AppRouteRecordRaw } from '/@/router/types';
import type { Router, RouteRecordNormalized } from 'vue-router';

import { getParentLayout, LAYOUT, EXCEPTION_COMPONENT } from '/@/router/constant';
import { cloneDeep, omit } from 'lodash-es';
import { warn } from '/@/utils/log';
import { createRouter, createWebHashHistory } from 'vue-router';
import { getTenantId, getToken } from "/@/utils/auth";
import { URL_HASH_TAB, _eval } from '/@/utils';
// Import route của online lib
import { packageViews } from '/@/utils/monorepo/dynamicRouter';
import { loadPackageComponent } from '/@/utils/monorepo/registerPackages';
import { dynamicPages } from '/@/utils/dynamicPages';
import {useI18n} from "/@/hooks/web/useI18n";

export type LayoutMapKey = 'LAYOUT';
const IFRAME = () => import('/@/views/sys/iframe/FrameBlank.vue');
const LayoutContent = () => import('/@/layouts/default/content/index.vue');

const LayoutMap = new Map<string, () => Promise<typeof import('*.vue')>>();

LayoutMap.set('LAYOUT', LAYOUT);
LayoutMap.set('IFRAME', IFRAME);
// Micro frontend qiankun
LayoutMap.set('LayoutsContent', LayoutContent);

let dynamicViewsModules: Record<string, () => Promise<Recordable>>;

// Dynamic introduction
function asyncImportRoute(routes: AppRouteRecordRaw[] | undefined) {
  if (!dynamicViewsModules) {
    // update-begin--author:liaozhiyang---date:20260302---for:【QQYUN-14799】Import động trang sẽ tạo ra hai bản và import các tệp component bên dưới components
    dynamicViewsModules = dynamicPages as Record<string, () => Promise<Recordable>>;
    // update-end--author:liaozhiyang---date:20260302---for:【QQYUN-14799】Import động trang sẽ tạo ra hai bản và import các tệp component bên dưới components
    // Hợp nhất route của online lib
    dynamicViewsModules = Object.assign({}, dynamicViewsModules, packageViews);
  }
  if (!routes) return;
  routes.forEach((item) => {

    // 【jeecg-boot/issues/I5N2PN】Menu động bên trái xử lý quốc tế hóa như thế nào 2022-10-09
    // Menu hỗ trợ dịch i18n
    if (item?.meta?.title) {
      const { t } = useI18n();
      if(item.meta.title.includes('t(\'') && t){
        // Giải thích logic mã: 【QQYUN-6390】Thay eval thành new Function để giải quyết cảnh báo khi build
        item.meta.title = new Function('t', `return ${item.meta.title}`)(t);
      }
    }
    // @ts-ignore Thích ứng với route ẩn
    if (item?.hidden) {
      item.meta.hideMenu = true;
      // Có ẩn breadcrumb không
      item.meta.hideBreadcrumb = true;
    }
    // @ts-ignore Thêm cấu hình bỏ qua route
    if (item?.route == 0) {
      item.meta.ignoreRoute = true;
    }
    // @ts-ignore Thêm cấu hình route có được cache không
    item.meta.ignoreKeepAlive = !item?.meta.keepAlive;
    let token = getToken();
    let tenantId = getTenantId();
    // URL hỗ trợ biến placeholder {{ window.xxx }}
    // Giải thích logic mã: [VUEN-1638] tenantId của menu cần tạo động------------
    item.component = (item.component || '').replace(/{{([^}}]+)?}}/g, (s1, s2) => _eval(s2)).replace('${token}', token).replace('${tenantId}', tenantId);
    // Thích ứng iframe
    if (/^\/?http(s)?/.test(item.component as string)) {
      item.component = item.component.substring(1, item.component.length);
    }
    if (/^http(s)?/.test(item.component as string)) {
      if (item.meta?.internalOrExternal) {
        // @ts-ignore Mở bên ngoài
        item.path = item.component;
        // Giải thích logic mã: 【VUEN-656】Cấu hình URL ngoài không mở được, nguyên nhân là có ký tự #, cần thay thế
        item.path = item.path.replace('#', URL_HASH_TAB);
      } else {
        // @ts-ignore Mở nội bộ
        item.meta.frameSrc = item.component;
      }
      delete item.component;
    }
    if (!item.component && item.meta?.frameSrc) {
      item.component = 'IFRAME';
    }
    let { component, name } = item;
    const { children } = item;
    if (component) {
      const layoutFound = LayoutMap.get(component.toUpperCase());
      if (layoutFound) {
        item.component = layoutFound;
      } else {
        if (component.indexOf('dashboard/') > -1) {
          // Khi dữ liệu đánh dấu sys_permission trong component chưa nối index thì frontend cần nối
          if (component.indexOf('/index') < 0) {
            component = component + '/index';
          }
        }
        item.component = dynamicImport(dynamicViewsModules, component as string);
      }
    } else if (name) {
      item.component = getParentLayout();
    }
    children && asyncImportRoute(children);
  });
}

function dynamicImport(dynamicViewsModules: Record<string, () => Promise<Recordable>>, component: string) {
  const keys = Object.keys(dynamicViewsModules);
  const matchKeys = keys.filter((key) => {
    // update-begin--author:liaozhiyang---date:20260302---for:【QQYUN-14799】Import động trang sẽ tạo ra hai bản và import các tệp component bên dưới components
    // Tương thích hai loại tiền tố: dynamicPages của ../views và packageViews của ../../views
    const k = key.replace(/^(\.\.\/)+views/, '');
    // update-end--author:liaozhiyang---date:20260302---for:【QQYUN-14799】Import động trang sẽ tạo ra hai bản và import các tệp component bên dưới components
    const startFlag = component.startsWith('/');
    const endFlag = component.endsWith('.vue') || component.endsWith('.tsx');
    const startIndex = startFlag ? 0 : 1;
    const lastIndex = endFlag ? k.length : k.lastIndexOf('.');
    return k.substring(startIndex, lastIndex) === component;
  });
  if (matchKeys?.length === 1) {
    const matchKey = matchKeys[0];
    return dynamicViewsModules[matchKey];
  } else if (matchKeys?.length > 1) {
    warn(
      'Please do not create `.vue` and `.TSX` files with the same file name in the same hierarchical directory under the views folder. This will cause dynamic introduction failure'
    );
    return;
  }
  // Không tìm thấy online/aiflow ở local, thử tải theo nhu cầu từ gói lazy-load
  if (component.startsWith('/super/airag/aiflow/')) {
    return () => {
      return loadPackageComponent(component).then((factory) => (factory ? factory() : Promise.reject(`Không tìm thấy component ${component}`)));
    };
  }
}

// Turn background objects into routing objects
export function transformObjToRoute<T = AppRouteModule>(routeList: AppRouteModule[]): T[] {
  routeList.forEach((route) => {
    const component = route.component as string;
    if (component) {
      route.originComponent = component;
      if (component.toUpperCase() === 'LAYOUT') {
        route.component = LayoutMap.get(component.toUpperCase());
      } else {
        route.children = [cloneDeep(route)];
        route.component = LAYOUT;
        route.name = `${route.name}Parent`;
        route.path = '';
        const meta = route.meta || {};
        meta.single = true;
        meta.affix = false;
        route.meta = meta;
      }
    } else {
      warn('Vui lòng cấu hình route đúng: thuộc tính component của ' + route?.name);
    }
    route.children && asyncImportRoute(route.children);
  });
  return routeList as unknown as T[];
}

/**
 * Chuyển route nhiều cấp thành hai cấp
 */
export function flatMultiLevelRoutes(routeModules: AppRouteModule[]) {
  const modules: AppRouteModule[] = cloneDeep(routeModules);
  for (let index = 0; index < modules.length; index++) {
    const routeModule = modules[index];
    if (!isMultipleRoute(routeModule)) {
      continue;
    }
    promoteRouteLevel(routeModule);
  }
  return modules;
}

// Nâng cấp bậc của route
function promoteRouteLevel(routeModule: AppRouteModule) {
  // Use vue-router to splice menus
  let router: Router | null = createRouter({
    routes: [routeModule as unknown as RouteRecordNormalized],
    history: createWebHashHistory(),
  });

  const routes = router.getRoutes();
  addToChildren(routes, routeModule.children || [], routeModule);
  router = null;

  routeModule.children = routeModule.children?.map((item) => omit(item, 'children'));
}

// Add all sub-routes to the secondary route
function addToChildren(routes: RouteRecordNormalized[], children: AppRouteRecordRaw[], routeModule: AppRouteModule) {
  for (let index = 0; index < children.length; index++) {
    const child = children[index];
    const route = routes.find((item) => item.name === child.name);
    if (!route) {
      continue;
    }
    routeModule.children = routeModule.children || [];
    if (!routeModule.children.find((item) => item.name === route.name)) {
      routeModule.children?.push(route as unknown as AppRouteModule);
    }
    if (child.children?.length) {
      addToChildren(routes, child.children, routeModule);
    }
  }
}

// Determine whether the level exceeds 2 levels
function isMultipleRoute(routeModule: AppRouteModule) {
  if (!routeModule || !Reflect.has(routeModule, 'children') || !routeModule.children?.length) {
    return false;
  }

  const children = routeModule.children;

  let flag = false;
  for (let index = 0; index < children.length; index++) {
    const child = children[index];
    if (child.children?.length) {
      flag = true;
      break;
    }
  }
  return flag;
}
/**
 * Xử lý thêm dấu gạch chéo trước địa chỉ component
 * @updateBy:lsq
 * @updateDate:2021-09-08
 */
export function addSlashToRouteComponent(routeList: AppRouteRecordRaw[]) {
  routeList.forEach((route) => {
    let component = route.component as string;
    if (component) {
      const layoutFound = LayoutMap.get(component);
      if (!layoutFound) {
        route.component = component.startsWith('/') ? component : `/${component}`;
      }
    }
    route.children && addSlashToRouteComponent(route.children);
  });
  return routeList as unknown as T[];
}
