import type { UserInfo, LoginInfo } from '/#/store';
import type { ErrorMessageMode } from '/#/axios';
import { defineStore } from 'pinia';
import { store } from '/@/store';
import { RoleEnum } from '/@/enums/roleEnum';
import { PageEnum } from '/@/enums/pageEnum';
import { ROLES_KEY, TOKEN_KEY, USER_INFO_KEY, LOGIN_INFO_KEY, DB_DICT_DATA_KEY, DB_DICT_VERSION_KEY, TENANT_ID, OAUTH2_THIRD_LOGIN_TENANT_ID } from '/@/enums/cacheEnum';
import { getAuthCache, setAuthCache, removeAuthCache } from '/@/utils/auth';
import { GetUserInfoModel, LoginParams, ThirdLoginParams } from '/@/api/sys/model/userModel';
import { doLogout, getUserInfo, loginApi, phoneLoginApi, thirdLogin } from '/@/api/sys/user';
import { queryAllDictItems } from '/@/views/system/dict/dict.api';
import { useI18n } from '/@/hooks/web/useI18n';
import { useMessage } from '/@/hooks/web/useMessage';
import { router } from '/@/router';
import { usePermissionStore } from '/@/store/modules/permission';
import { RouteRecordRaw } from 'vue-router';
import { PAGE_NOT_FOUND_ROUTE } from '/@/router/routes/basic';
import { isArray } from '/@/utils/is';
import { useGlobSetting } from '/@/hooks/setting';
import { JDragConfigEnum } from '/@/enums/jeecgEnum';
import { getCasServiceUrl, useSso } from '/@/hooks/web/useSso';
import { isOAuth2AppEnv } from "/@/views/sys/login/useLogin";
import { getUrlParam } from "@/utils";
interface dictType {
  [key: string]: any;
}
interface UserState {
  userInfo: Nullable<UserInfo>;
  token?: string;
  roleList: RoleEnum[];
  dictItems?: dictType | null;
  sessionTimeout?: boolean;
  lastUpdateTime: number;
  tenantid?: string | number;
  shareTenantId?: Nullable<string | number>;
  loginInfo?: Nullable<LoginInfo>;
  // Công tắc mật khẩu mạnh bảo mật cấp 3
  enableStrongPwd?: boolean;
}

export const useUserStore = defineStore('app-user', {
  state: (): UserState => ({
    // Thông tin người dùng
    userInfo: null,
    // token
    token: undefined,
    // Danh sách vai trò
    roleList: [],
    // Từ điển
    dictItems: null,
    // Thời gian hết hạn của session
    sessionTimeout: false,
    // Last fetch time
    lastUpdateTime: 0,
    // ID tenant
    tenantid: '',
    // ID tenant được chia sẻ
    // Dùng cho trường hợp tenant của trang được chia sẻ không khớp với tenant đăng nhập của người dùng hiện tại
    shareTenantId: null,
    // Thông tin trả về khi đăng nhập
    loginInfo: null,
    // Công tắc mật khẩu mạnh bảo mật cấp 3
    enableStrongPwd: false,
  }),
  getters: {
    getUserInfo(): UserInfo {
      if(this.userInfo == null){
        this.userInfo = getAuthCache<UserInfo>(USER_INFO_KEY)!=null ? getAuthCache<UserInfo>(USER_INFO_KEY) : null;
      }
      return this.userInfo || getAuthCache<UserInfo>(USER_INFO_KEY) || {};
    },
    getLoginInfo(): LoginInfo {
      return this.loginInfo || getAuthCache<LoginInfo>(LOGIN_INFO_KEY) || {};
    },
    getToken(): string {
      return this.token || getAuthCache<string>(TOKEN_KEY);
    },
    getAllDictItems(): [] {
      return this.dictItems || getAuthCache(DB_DICT_DATA_KEY);
    },
    getRoleList(): RoleEnum[] {
      return this.roleList.length > 0 ? this.roleList : getAuthCache<RoleEnum[]>(ROLES_KEY);
    },
    getSessionTimeout(): boolean {
      return !!this.sessionTimeout;
    },
    getLastUpdateTime(): number {
      return this.lastUpdateTime;
    },
    getTenant(): string | number {
      return this.tenantid || getAuthCache<string | number>(TENANT_ID);
    },
    // Có ID tenant được chia sẻ không
    hasShareTenantId(): boolean {
      return this.shareTenantId != null && this.shareTenantId !== '';
    },
    // Công tắc mật khẩu mạnh bảo mật cấp 3
    getEnableStrongPwd(): boolean {
      return this.enableStrongPwd ?? false;
    },
  },
  actions: {
    setToken(info: string | undefined) {
      this.token = info ? info : ''; // for null or undefined value
      setAuthCache(TOKEN_KEY, info);
    },
    setRoleList(roleList: RoleEnum[]) {
      this.roleList = roleList;
      setAuthCache(ROLES_KEY, roleList);
    },
    setUserInfo(info: UserInfo | null) {
      this.userInfo = info;
      this.lastUpdateTime = new Date().getTime();
      setAuthCache(USER_INFO_KEY, info);
    },
    setLoginInfo(info: LoginInfo | null) {
      this.loginInfo = info;
      setAuthCache(LOGIN_INFO_KEY, info);
    },
    /**
     * Cài đặt cache từ điển đầy đủ
     * @param dictItems Dữ liệu từ điển
     * @param dictVersion Phiên bản từ điển, lưu theo cặp với dữ liệu từ điển; không truyền thì xóa, lần làm mới trang sau sẽ kéo lại từ điển một lần
     */
    setAllDictItems(dictItems, dictVersion?: string | null) {
      this.dictItems = dictItems;
      setAuthCache(DB_DICT_DATA_KEY, dictItems);
      setAuthCache(DB_DICT_VERSION_KEY, dictVersion ?? null);
    },
    /**
     * Khi phiên bản từ điển backend không khớp với local, sẽ kéo lại toàn bộ từ điển từ backend
     * @param dictVersion Phiên bản từ điển backend trả về
     * @author scott
     * @since 2026-10-13 [issues/9838] Sau khi quản trị viên sửa từ điển, người dùng khác làm mới trang sẽ đồng bộ được từ điển mới nhất
     */
    async refreshDictItemsIfChanged(dictVersion?: string) {
      if (!dictVersion || dictVersion === getAuthCache<string>(DB_DICT_VERSION_KEY)) {
        return;
      }
      const res = await queryAllDictItems();
      if (res?.success && res.result) {
        this.setAllDictItems(res.result, dictVersion);
      }
    },
    setAllDictItemsByLocal() {
      // Giải thích logic mã: [QQYUN-8572] Vấn đề chọn hàng trong bảng bị giật (do customRender trong từ điển gây ra)
      if (!this.dictItems) {
        const allDictItems = getAuthCache(DB_DICT_DATA_KEY);
        if (allDictItems) {
          this.dictItems = allDictItems;
        }
      }
    },
    setTenant(id) {
      this.tenantid = id;
      setAuthCache(TENANT_ID, id);
    },
    setShareTenantId(id: NonNullable<typeof this.shareTenantId>) {
      this.shareTenantId = id;
    },
    setSessionTimeout(flag: boolean) {
      this.sessionTimeout = flag;
    },
    resetState() {
      this.userInfo = null;
      this.dictItems = null;
      this.token = '';
      this.roleList = [];
      this.sessionTimeout = false;
    },
    /**
     * Sự kiện đăng nhập
     */
    async login(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await loginApi(loginParams, mode);
        const { token, userInfo } = data;
        // save token
        this.setToken(token);
        this.setTenant(userInfo.loginTenantId);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Sự kiện đăng nhập bằng quét mã
     */
    async qrCodeLogin(token): Promise<GetUserInfoModel | null> {
      try {
        // save token
        this.setToken(token);
        return this.afterLoginAction(true, {});
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Xử lý hoàn thành đăng nhập
     * @param goHome
     */
    async afterLoginAction(goHome?: boolean, data?: any): Promise<any | null> {
      if (!this.getToken) return null;
      // Lấy thông tin người dùng
      const userInfo = await this.getUserInfoAction();
      const sessionTimeout = this.sessionTimeout;
      if (sessionTimeout) {
        this.setSessionTimeout(false);
      } else {
        // // Xây dựng route menu backend
        // const permissionStore = usePermissionStore();
        // if (!permissionStore.isDynamicAddedRoute) {
        //   const routes = await permissionStore.buildRoutesAction();
        //   routes.forEach((route) => {
        //     router.addRoute(route as unknown as RouteRecordRaw);
        //   });
        //   router.addRoute(PAGE_NOT_FOUND_ROUTE as unknown as RouteRecordRaw);
        //   permissionStore.setDynamicAddedRoute(true);
        // }
        
        await this.setLoginInfo({ ...data, isLogin: true });
        // Giải thích logic mã: Sau khi đăng nhập thành công, cache tiền tố API của module kéo thả
        localStorage.setItem(JDragConfigEnum.DRAG_BASE_URL, useGlobSetting().domainUrl);

        // Giải thích logic mã: Sửa vấn đề sau khi đăng nhập thành công không chuyển hướng đúng
        let redirect = router.currentRoute.value?.query?.redirect as string;
        // Kiểm tra có địa chỉ redirect hay không
        // Giải thích logic mã: [QQYUN-5195] Sau khi đăng nhập, làm mới trang trực tiếp khiến không vào được trang tạo tổ chức------------
        if (redirect && goHome) {
          // router.options.history.base có thể thay thế publicPath trước đây
          // Mở trang hiện tại
          window.open(`${router.options.history.base}${redirect}`, '_self');
          return data;
        }

        // Giải thích logic mã: [issues/1102] Sau khi cài đặt đăng nhập một lần (SSO), vào trang thì báo 404 ở trang chủ và cũng không vẽ sidebar #1102---
        let ticket = getUrlParam('ticket');
        if(ticket){
          //update-begin---wangshuai---date:20260810  for: [issues/9797] Bật kiểm tra CAS, địa chỉ quay lại sai------------
          // Sau khi kiểm tra CAS thành công, làm mới địa chỉ nghiệp vụ hiện tại để tránh deep link bị trang chủ người dùng ghi đè (issues/9797)
          goHome && window.location.replace(getCasServiceUrl());
          //update-end---author:wangshuai ---date:20260810  for: [issues/9797] Bật kiểm tra CAS, địa chỉ quay lại sai------------  
        }else{
          goHome && (await router.replace((userInfo && userInfo.homePath) || PageEnum.BASE_HOME));
        }
      }
      return data;
    },
    /**
     * Đăng nhập bằng số điện thoại
     * @param params
     */
    async phoneLogin(
      params: LoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<GetUserInfoModel | null> {
      try {
        const { goHome = true, mode, ...loginParams } = params;
        const data = await phoneLoginApi(loginParams, mode);
        // Giải thích logic mã: [issues/7488] Đăng nhập bằng số điện thoại, không lấy được ID tenant trong header yêu cầu---
        const { token , userInfo } = data;
        this.setTenant(userInfo!.loginTenantId);
        // save token
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Lấy thông tin người dùng
     */
    async getUserInfoAction(): Promise<UserInfo | null> {
      if (!this.getToken) {
        return null;
      }
      const { userInfo, sysAllDictItems, dictVersion, enableStrongPwd } = await getUserInfo();
      if (userInfo) {
        const { roles = [] } = userInfo;
        if (isArray(roles)) {
          const roleList = roles.map((item) => item.value) as RoleEnum[];
          this.setRoleList(roleList);
        } else {
          userInfo.roles = [];
          this.setRoleList([]);
        }
        this.setUserInfo(userInfo);
      }
      /**
       * Thêm thông tin từ điển vào cache
       * @updateBy:lsq
       * @updateDate:2021-09-08
       */
      if (sysAllDictItems) {
        this.setAllDictItems(sysAllDictItems, dictVersion);
      }
      this.enableStrongPwd = enableStrongPwd ?? false;
      return userInfo;
    },
    /**
     * Đăng xuất
     */
    async logout(goLogin = false) {
      if (this.getToken) {
        try {
          await doLogout();
        } catch {
          console.log('Đăng xuất token thất bại');
        }
      }

      // let username:any = this.userInfo && this.userInfo.username;
      // if(username){
      //   removeAuthCache(username)
      // }

      this.setToken('');
      setAuthCache(TOKEN_KEY, null);
      this.setSessionTimeout(false);
      this.setUserInfo(null);
      this.setLoginInfo(null);
      this.setTenant(null);
      // Giải thích logic mã: [TV360X-23] Sau khi đăng xuất sẽ nhắc “Token hết hiệu lực, vui lòng đăng nhập lại”
      setTimeout(() => {
        this.setAllDictItems(null);
      }, 1e3);
      // Giải thích logic mã: Sau khi đăng xuất xóa tiền tố API của module kéo thả
      localStorage.removeItem(JDragConfigEnum.DRAG_BASE_URL);

      // Nếu bật đăng nhập một lần (SSO), thì chuyển đến trung tâm đăng nhập thống nhất SSO
      const openSso = useGlobSetting().openSso;
      if (openSso == 'true') {
        await useSso().ssoLoginOut();
      }
      // ID ứng dụng cần dùng khi đăng xuất
      if(isOAuth2AppEnv()){
        let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
        removeAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID);
        goLogin && await router.push({ name:"Login",query:{ tenantId:tenantId }})
      }else{
        // Giải thích logic mã: Sửa vấn đề sau khi đăng nhập thành công không chuyển hướng đúng
        goLogin && (await router.push({
          path: PageEnum.BASE_LOGIN,
          query: {
            // Truyền route hiện tại, sau khi đăng nhập thành công sẽ chuyển đến route hiện tại
            redirect: router.currentRoute.value.fullPath,
          }
        }));

      }
    },
    /**
     * Sự kiện đăng nhập
     */
    async ThirdLogin(
      params: ThirdLoginParams & {
        goHome?: boolean;
        mode?: ErrorMessageMode;
      }
    ): Promise<any | null> {
      try {
        const { goHome = true, mode, ...ThirdLoginParams } = params;
        const data = await thirdLogin(ThirdLoginParams, mode);
        // Giải thích logic mã: [issues/6652] Bật cách ly dữ liệu tenant, sau khi tích hợp DingTalk, tenant mặc định khi đăng nhập thành 0---
        const { token, userInfo } = data;
        this.setTenant(userInfo?.loginTenantId);
        // save token
        this.setToken(token);
        return this.afterLoginAction(goHome, data);
      } catch (error) {
        return Promise.reject(error);
      }
    },
    /**
     * Hỏi trước khi thoát
     */
    confirmLoginOut() {
      const { createConfirm } = useMessage();
      const { t } = useI18n();
      createConfirm({
        iconType: 'warning',
        title: t('sys.app.logoutTip'),
        content: t('sys.app.logoutMessage'),
        onOk: async () => {
          await this.logout(true);
        },
      });
    },
  },
});

// Need to be used outside the setup
export function useUserStoreWithOut() {
  return useUserStore(store);
}
