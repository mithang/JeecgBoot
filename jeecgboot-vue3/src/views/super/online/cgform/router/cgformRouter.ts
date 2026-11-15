import {router} from '/@/router';
import {LAYOUT} from '/@/router/constant';

export function registerCgformRouter() {
  router.addRoute({
    path: '/online-auto-cgform-router',
    name: 'onl-auto-cgform-router',
    component: LAYOUT,
    redirect: '/online/cgform',
    meta: {
      title: 'OnlCgformAuto',
      hideMenu: true,
      hideBreadcrumb: true,
    },
    children: [
      {
        path: '/online/cgformList/:id',
        name: 'OnlineAutoList',
        component: () => import('../auto/default/OnlineAutoList.vue'),
        meta: {title: 'Biểu mẫu trực tuyến', keepAlive: true},
      },
      {
        path: '/online/cgformTreeList/:id',
        name: 'DefaultOnlineList',
        component: () => import('../auto/tree/OnlineAutoTreeList.vue'),
        meta: {title: 'Danh sách biểu mẫu cây', keepAlive: true},
      },
      {
        path: '/online/cgformErpList/:id',
        name: 'CgformErpList',
        component: () => import('../auto/erp/OnlCgformErpList.vue'),
        meta: {title: 'Biểu mẫu ERP trực tuyến', keepAlive: true},
      },
      {
        path: '/online/cgformInnerTableList/:id',
        name: 'OnlCgformInnerTableList',
        component: () => import('../auto/innerTable/OnlCgformInnerTableList.vue'),
        meta: {title: 'Nhúng một-nhiều trực tuyến', keepAlive: true},
      },
      {
        path: '/online/cgformTabList/:id',
        name: 'OnlCgformTabList',
        component: () => import('../auto/tab/OnlCgformTabList.vue'),
        meta: {title: 'Biểu mẫu Tab trực tuyến', keepAlive: true},
      },
    ],
  })
}
