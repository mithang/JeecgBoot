import type {RouteRecordRaw} from 'vue-router';
import {PageEnum} from "@/enums/pageEnum";

// Tên route của liên kết ngoài (thêm mới)
export const SHARE_ADD_ROUTER_NAME = 'online-cgform-@formId-share-add'

// Tên route của liên kết ngoài (chỉnh sửa)
export const SHARE_UPDATE_ROUTER_NAME = 'online-cgform-@formId-share-u-@dataId'

// Tên route của liên kết ngoài (chi tiết)
export const SHARE_DETAIL_ROUTER_NAME = 'online-cgform-@formId-share-d-@dataId'

export const SHARE_ROUTE: RouteRecordRaw = {
  path: "/online/cgform/share",
  name: "online-cgform-share",
  component: () => import("../layouts/default/index.vue"),
  meta: {
    title: 'OnlineTrang bên ngoài biểu mẫu',
    ignoreAuth: true,
  },
  children: [
    {
      path: ':id/add',
      name: SHARE_ADD_ROUTER_NAME,
      component: () => import("../components/add/ShareAddView.vue"),
      meta: {
        title: 'OnlineTrang thêm mới bên ngoài biểu mẫu',
      },
    },
    {
      path: ':id/u/:dataId',
      name: SHARE_UPDATE_ROUTER_NAME,
      component: () => import("../components/edit/ShareEditView.vue"),
      meta: {
        title: 'OnlineTrang chỉnh sửa bên ngoài biểu mẫu',
      },
    },
    {
      path: ':id/d/:dataId',
      name: SHARE_DETAIL_ROUTER_NAME,
      component: () => import("../components/edit/ShareEditView.vue"),
      meta: {
        title: 'OnlineTrang chi tiết bên ngoài biểu mẫu',
      },
    },
  ]
}


// Tên route của liên kết ngoài (chi tiết)
export const SHARE_LOGIN__ROUTER_NAME = 'online-cgform-share-login'

export const SHARE_LOGIN_ROUTE: RouteRecordRaw = {
  path: "/online/cgform/share/login",
  name: SHARE_LOGIN__ROUTER_NAME,
  // component: () => import("../layouts/login/index.vue"),
  component: () => import("../layouts/login/AppLogin.vue"),
  meta: {
    title: 'Đăng nhập · OnlineBiểu mẫu',
    ignoreAuth: true,
  },
}

//
// OnlineTrang liên kết ngoài của biểu mẫu
const ONLINE_CGFORM_SHARE = '/online/cgform/share';

export async function routerBeforeEach(to: any, _from: any) {
  // Nếu là route đăng nhập
  if (to.path === PageEnum.BASE_LOGIN) {
    // Lấy redirect
    let redirect = ((redirect: string) => {
      if (!redirect) {
        return '';
      }
      // Phán đoán redirect Có phải là onlineBiểu mẫu Trang bên ngoài
      if (redirect.startsWith(ONLINE_CGFORM_SHARE)) {
        return redirect;
      }
      redirect = decodeURIComponent(redirect)
      if (redirect.startsWith(ONLINE_CGFORM_SHARE)) {
        return redirect;
      }
      return '';
    })(to.query.redirect as string)
    // Nếu đúng là vậy thì chuyển tới onlineBiểu mẫu Trang đăng nhập chuyên dụng ở bên ngoài
    if (redirect) {
      redirect = redirect.split('?')[0];
      return {
        name: SHARE_LOGIN__ROUTER_NAME,
        query: {
          redirect: encodeURIComponent(redirect),
        }
      };
    }
  }
}
