import type { AppRouteRecordRaw, AppRouteModule } from '/@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '/@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '/@/enums/pageEnum';
import { t } from '/@/hooks/web/useI18n';
import { LAYOUT } from '/@/router/constant';

const modules = import.meta.glob('./modules/**/*.ts', { eager: true });

const routeModuleList: AppRouteModule[] = [];

// Thêm vào tập hợp route
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  // Đăng nhập backend phiên bản mới, nếu muốn dùng đăng nhập phiên bản cũ thì bỏ comment là được
  // component: () => import('/@/views/sys/login/Login.vue'),
  component: () => import('/@/views/system/loginmini/MiniLogin.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

// Giải thích logic mã: Route trang đăng nhập auth2------------
export const Oauth2LoginRoute: AppRouteRecordRaw = {
  path: '/oauth2-app/login',
  name: 'oauth2-app-login',
  // Miễn đăng nhập DingTalk phiên bản mới, nếu muốn dùng phiên bản cũ thì bỏ comment là được
  // component: () => import('/@/views/sys/login/OAuth2Login.vue'),
  component: () => import('/@/views/system/loginmini/OAuth2Login.vue'),
  meta: {
    title: t('routes.oauth2.login'),
  },
};

/**
 * 【Đăng nhập im lặng trực tiếp qua token】Trang đăng nhập xử lý quy trình, chuyển tiếp
 */
export const TokenLoginRoute: AppRouteRecordRaw = {
  path: '/tokenLogin',
  name: 'TokenLoginRoute',
  component: () => import('/@/views/sys/login/TokenLoginPage.vue'),
  meta: {
    title: 'Trang đăng nhập mang token',
    ignoreAuth: true,
  },
};
// Giải thích logic mã: 【QQYUN-7967】Truy cập route thêm mới, chỉnh sửa
export const formUrlDetail = {
  path: '/online/formUrlDetail/:id/:dataId',
  name: 'formUrlDetail',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlDetail.vue'),
  meta: {
    title: 'Chi tiết biểu mẫu kê khai ngoài',
    ignoreAuth: true,
  },
};

export const formUrlAdd = {
  path: '/online/formUrlAdd/:id',
  name: 'formUrlAdd',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlAdd.vue'),
  meta: {
    title: 'Thêm mới biểu mẫu kê khai ngoài',
    ignoreAuth: true,
  },
};

export const formUrlEdit = {
  path: '/online/formUrlEdit/:id/:dataId',
  name: 'formUrlEdit',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlEdit.vue'),
  meta: {
    title: 'Chỉnh sửa biểu mẫu kê khai ngoài',
    ignoreAuth: true,
  },
};

export const formUrlSuccess = {
  path: '/online/formUrlSuccess',
  name: 'formUrlSuccess',
  component: () => import('/@/views/super/online/cgform/auto/default/OnlineFormUrlSuccess.vue'),
  meta: {
    title: 'Biểu mẫu kê khai ngoài thành công',
    ignoreAuth: true,
  },
};


export const onlinePreview = {
  path: '/onlinePreview',
  name: 'online-preview',
  meta: {
    title: 'Xem trước tệp WPS',
    ignoreAuth: false,
  },
  component: () => import('/@/components/onlinePreview/WpsFileView.vue'),
}



// Basic routing without permission
export const basicRoutes = [LoginRoute, RootRoute, ...mainOutRoutes, REDIRECT_ROUTE, PAGE_NOT_FOUND_ROUTE, TokenLoginRoute, Oauth2LoginRoute, formUrlDetail, formUrlAdd, formUrlEdit, formUrlSuccess, onlinePreview];
