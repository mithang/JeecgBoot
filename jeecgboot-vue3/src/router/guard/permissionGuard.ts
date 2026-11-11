import type { Router, RouteRecordRaw } from 'vue-router';

import { usePermissionStoreWithOut } from '/@/store/modules/permission';

import { PageEnum } from '/@/enums/pageEnum';
import { useUserStoreWithOut } from '/@/store/modules/user';

import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';

import { RootRoute } from '/@/router/routes';

import {isOAuth2AppEnv, isOAuth2DingAppEnv} from '/@/views/sys/login/useLogin';
import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";
import { setAuthCache } from "/@/utils/auth";
import { PAGE_NOT_FOUND_NAME_404 } from '/@/router/constant';

const LOGIN_PATH = PageEnum.BASE_LOGIN;
// Route đăng nhập auth2
const OAUTH2_LOGIN_PAGE_PATH = PageEnum.OAUTH2_LOGIN_PAGE_PATH;

// Route chia sẻ miễn đăng nhập
const SYS_FILES_PATH = PageEnum.SYS_FILES_PATH;

// Địa chỉ chuyển hướng trong email, tương ứng với route này, mang theo token để miễn đăng nhập và đi thẳng đến trang xử lý
const TOKEN_LOGIN = PageEnum.TOKEN_LOGIN;

const ROOT_PATH = RootRoute.path;

// Giải thích logic mã: [VUEN-2472] Chia sẻ miễn đăng nhập------------
const whitePathList: PageEnum[] = [LOGIN_PATH, OAUTH2_LOGIN_PAGE_PATH,SYS_FILES_PATH, TOKEN_LOGIN ];

export function createPermissionGuard(router: Router) {
  const userStore = useUserStoreWithOut();
  const permissionStore = usePermissionStoreWithOut();

  // Số lần chuyển trang chủ tùy chỉnh
  let homePathJumpCount = 0;

  router.beforeEach(async (to, from) => {
    if (
      // 【#6861】Logic chuyển đến trang chủ tùy chỉnh, chỉ cần chuyển một lần
      homePathJumpCount < 1 &&
      from.path === ROOT_PATH &&
      to.path === PageEnum.BASE_HOME &&
      userStore.getUserInfo.homePath &&
      userStore.getUserInfo.homePath !== PageEnum.BASE_HOME
    ) {
      homePathJumpCount++;
      return userStore.getUserInfo.homePath;
    }

    const token = userStore.getToken;

    // Whitelist can be directly entered
    if (whitePathList.includes(to.path as PageEnum)) {
      if (to.path === LOGIN_PATH && token) {
        const isSessionTimeout = userStore.getSessionTimeout;
        
        // TODO: Cách viết mặc định của vben, tạm thời chưa biết mục đích, có vấn đề nên tạm thời comment lại trước
        //await userStore.afterLoginAction();
        
        try {
          if (!isSessionTimeout) {
            return (to.query?.redirect as string) || '/';
          }
        } catch {}
        // Giải thích logic mã: [issues/I5BG1I] vue3 không hỗ trợ đăng nhập auth2------------
      } else if (to.path === LOGIN_PATH && isOAuth2AppEnv() && !token) {
        // Đăng xuất sẽ vào logic này
        // Nếu trang truy cập là trang login và hiện tại là môi trường OAuth2 app, và token trống thì vào trang đăng nhập OAuth2
        // Giải thích logic mã: [QQYUN-3440] Tạo mới bảng cấu hình WeCom và DingTalk, cách ly theo chế độ tenant------------
        if(to.query.tenantId){
          setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID,to.query.tenantId)
        }
        return { path: OAUTH2_LOGIN_PAGE_PATH };
      }
      return true;
    }

    // token does not exist
    if (!token) {
      // You can access without permission. You need to set the routing meta.ignoreAuth to true
      if (to.meta.ignoreAuth) {
        return true;
      }

      // Giải thích logic mã: [issues/I5BG1I] vue3 chưa triển khai Auth2------------
      let path = LOGIN_PATH;
      if (whitePathList.includes(to.path as PageEnum)) {
        // Trong danh sách trắng miễn đăng nhập, nếu trang truy cập là trang login và hiện tại là môi trường OAuth2 app thì vào trang đăng nhập OAuth2
        if (to.path === LOGIN_PATH && isOAuth2AppEnv()) {
          return { path: OAUTH2_LOGIN_PAGE_PATH };
        } else {
          // Trong danh sách trắng miễn đăng nhập thì vào trực tiếp
          return true;
        }
      } else {
        //----------【Chỉ gọi khi lần đầu đăng nhập và là WeCom hoặc DingTalk】-----------------------------------------------
        // Chỉ gọi khi đăng nhập lần đầu và là WeCom hoặc DingTalk
        let href = window.location.href;
        // Kiểm tra hiện tại có phải trang auth2 không, có phải DingTalk/WeCom không, và có chứa tham số tenantId không
        if(isOAuth2AppEnv() && href.indexOf("/tenantId/")!= -1){
          let params = to.params;
          if(params && params.path && params.path.length>0){
            // Lấy trực tiếp ký tự cuối cùng của tham số
            setAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID,params.path[params.path.length-1])
          }
        }
        //---------【Chỉ gọi khi lần đầu đăng nhập và là WeCom hoặc DingTalk】------------------------------------------------
        // Nếu hiện tại đang ở môi trường OAuth2 APP thì chuyển đến trang đăng nhập OAuth2, nếu không thì chuyển đến trang đăng nhập
        path = isOAuth2AppEnv() ? OAUTH2_LOGIN_PAGE_PATH : LOGIN_PATH;
      }
      // redirect login page
      const redirectData: { path: string; replace: boolean; query?: Recordable<string> } = {
        // Giải thích logic mã: [issues/I5BG1I] vue3 chưa triển khai Auth2------------
        path: path,
        replace: true,
      };

      // Giải thích logic mã: 【QQYUN-4713】Logic điều chỉnh mã đăng nhập có vấn đề, cần theo dõi sau khi cải tạo--
      if (to.fullPath) {
        console.log("to.fullPath 1",to.fullPath)
        console.log("to.path 2",to.path)
        
        let getFullPath = to.fullPath;
        if(getFullPath=='/' || getFullPath=='/500' || getFullPath=='/400' || getFullPath=='/login?redirect=/' || getFullPath=='/login?redirect=/login?redirect=/'){
          return;
        }
        
        redirectData.query = {
          ...redirectData.query,
          // Giải thích logic mã: Sửa vấn đề sau khi đăng nhập thành công không chuyển hướng đúng
          redirect: to.fullPath,

        };
      }
      return redirectData;
    }

    //==============================【Chỉ gọi khi lần đầu đăng nhập và là WeCom hoặc DingTalk】==================
    // Kiểm tra có phải trang miễn đăng nhập không, nếu trang chứa /tenantId/ thì đi thẳng đến trang chủ
    if(isOAuth2AppEnv() && to.path.indexOf("/tenantId/") != -1){
      // Giải thích logic mã: 【TV360X-2958】Sau khi đăng nhập DingTalk rồi mở Qiaoqiao Cloud, đổi tài khoản khác đăng nhập rồi mở lại Qiaoqiao Cloud vẫn hiển thị ứng dụng của tài khoản cũ---
      if (isOAuth2DingAppEnv()) {
        return OAUTH2_LOGIN_PAGE_PATH;
      } else {
        return userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
      }
    }
    //==============================【Chỉ gọi khi lần đầu đăng nhập và là WeCom hoặc DingTalk】==================
    // Jump to the 404 page after processing the login
    if (from.path === LOGIN_PATH && to.name === PAGE_NOT_FOUND_NAME_404 && to.fullPath !== (userStore.getUserInfo.homePath || PageEnum.BASE_HOME)) {
      return userStore.getUserInfo.homePath || PageEnum.BASE_HOME;
    }

    // // get userinfo while last fetch time is empty
    // if (userStore.getLastUpdateTime === 0) {
    //   try {
    //     console.log("--LastUpdateTime---getUserInfoAction-----")
    //     await userStore.getUserInfoAction();
    //   } catch (err) {
    //     console.info(err);
    //     next();
    //   }
    // }
    // Giải thích logic mã: 【QQYUN-8572】Vấn đề chọn hàng trong bảng bị giật (do customRender trong từ điển gây ra)
    if (userStore.getLastUpdateTime === 0) {
      userStore.setAllDictItemsByLocal();
    }
    if (permissionStore.getIsDynamicAddedRoute) {
      return true;
    }

    // Xây dựng route menu phía backend
    const routes = await permissionStore.buildRoutesAction();
    routes.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw);
    });

    router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
    permissionStore.setDynamicAddedRoute(true);
    // Giải thích logic mã: 【issues/7500】vue-router 4.5.0, route name: PageNotFound trùng tên khiến không đăng nhập được
    if (to.name === PAGE_NOT_FOUND_NAME_404) {
      // Sau khi thêm động route, ở đây nên chuyển hướng đến fullPath, nếu không sẽ tải nội dung trang 404
      return { path: to.fullPath, replace: true, query: to.query };
    } else {
      const redirectPath = (from.query.redirect || to.path) as string;
      const redirect = decodeURIComponent(redirectPath);
      const nextData = to.path === redirect ? { ...to, replace: true } : { path: redirect };
      return nextData;
    }
  });
}
