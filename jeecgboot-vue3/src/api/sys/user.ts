import { defHttp } from '/@/utils/http/axios';
import { LoginParams, LoginResultModel, GetUserInfoModel } from './model/userModel';

import { ErrorMessageMode } from '/#/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { setAuthCache } from '/@/utils/auth';
import { TOKEN_KEY } from '/@/enums/cacheEnum';
import { router } from '/@/router';
import { PageEnum } from '/@/enums/pageEnum';
import { ExceptionEnum } from "@/enums/exceptionEnum";

const { createErrorModal } = useMessage();
enum Api {
  Login = '/sys/login',
  phoneLogin = '/sys/phoneLogin',
  Logout = '/sys/logout',
  GetUserInfo = '/sys/user/getUserInfo',
  // Lấy quyền hệ thống
  // 1, Truy vấn nút người dùng sở hữu/Quyền truy cập biểu mẫu
  // 2, Tất cả quyền hạn
  // 3, Chế độ an toàn hệ thống
  GetPermCode = '/sys/permission/getPermCode',
  //API mới lấy mã xác thực hình ảnh
  getInputCode = '/sys/randomImage',
  //API lấy mã xác thực SMS
  getCaptcha = '/sys/sms',
  //API đăng ký
  registerApi = '/sys/user/register',
  //API kiểm tra người dùng
  checkOnlyUser = '/sys/user/checkOnlyUser',
  //SSOKiểm tra đăng nhập
  validateCasLogin = '/sys/cas/client/validateLogin',
  //Kiểm tra số điện thoại
  phoneVerify = '/sys/user/phoneVerification',
  //Đổi mật khẩu
  passwordChange = '/sys/user/passwordChange',
  //Đăng nhập bên thứ ba
  thirdLogin = '/sys/thirdLogin/getLoginUser',
  //Đăng nhập bên thứ ba
  getThirdCaptcha = '/sys/thirdSms',
  //Lấy thông tin mã QR
  getLoginQrcode = '/sys/getLoginQrcode',
  //Giám sát trạng thái quét mã QR
  getQrcodeToken = '/sys/getQrcodeToken',
}

/**
 * @description: user login api
 */
export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.Login,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: user phoneLogin api
 */
export function phoneLoginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<LoginResultModel>(
    {
      url: Api.phoneLogin,
      params,
    },
    {
      errorMessageMode: mode,
    }
  );
}

/**
 * @description: getUserInfo
 */
export function getUserInfo() {
  return defHttp.get<GetUserInfoModel>({ url: Api.GetUserInfo }, {}).catch((e) => {
    // TokenHết hạn không có hiệu lực, chuyển thẳng trang đăng nhập
    if (e && (e.message.includes('timeout') || e.message.includes('401'))) {
      //Khi API không thông thì chuyển đến giao diện đăng nhập
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

export function getPermCode() {
  return defHttp.get({ url: Api.GetPermCode });
}

export function doLogout() {
  return defHttp.get({ url: Api.Logout });
}

export function getCodeInfo(currdatetime) {
  let url = Api.getInputCode + `/${currdatetime}`;
  return defHttp.get({ url: url });
}
/**
 * @description: Lấy mã xác thực SMS
 */
export function getCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        // Giải thích logic mã: 【QQYUN-9005】Cùng mộtIP, 1Vượt quá số phút5tin nhắn SMS, thì gợi ý cần mã xác thực---
        if(res.code != ExceptionEnum.PHONE_SMS_FAIL_CODE){
          createErrorModal({ title: 'Gợi ý lỗi', content: res.message || 'Vấn đề không xác định' });
          reject();
        }
        reject(res);
      }
    }).catch((res)=>{
      createErrorModal({ title: 'Gợi ý lỗi', content: res.message || 'Vấn đề không xác định' });
      reject();
    });
  });
}

/**
 * @description: API đăng ký
 */
export function register(params) {
  return defHttp.post({ url: Api.registerApi, params }, { isReturnNativeResponse: true });
}

/**
 *Kiểm tra người dùng có tồn tại không
 * @param params
 */
export const checkOnlyUser = (params) => defHttp.get({ url: Api.checkOnlyUser, params }, { isTransformResponse: false });
/**
 *Kiểm tra số điện thoại
 * @param params
 */
export const phoneVerify = (params) => defHttp.post({ url: Api.phoneVerify, params }, { isTransformResponse: false });
/**
 *Sửa mật khẩu
 * @param params
 */
export const passwordChange = (params) => defHttp.get({ url: Api.passwordChange, params }, { isTransformResponse: false });
/**
 * @description: Đăng nhập bên thứ ba
 */
export function thirdLogin(params, mode: ErrorMessageMode = 'modal') {
  //==========begin Đăng nhập bên thứ ba/auth2Đăng nhập cần truyền tenantid===========
  let tenantId = "0";
  if(!params.tenantId){
    tenantId = params.tenantId;
  }
  //==========end Đăng nhập bên thứ ba/auth2Đăng nhập cần truyền tenantid===========
  return defHttp.get<LoginResultModel>(
    {
      url: `${Api.thirdLogin}/${params.token}/${params.thirdType}/${tenantId}`,
    },
    {
      errorMessageMode: mode,
    }
  );
}
/**
 * @description: Lấy mã xác thực SMS bên thứ ba
 */
export function setThirdCaptcha(params) {
  return new Promise((resolve, reject) => {
    defHttp.post({ url: Api.getThirdCaptcha, params }, { isTransformResponse: false }).then((res) => {
      console.log(res);
      if (res.success) {
        resolve(true);
      } else {
        createErrorModal({ title: 'Gợi ý lỗi', content: res.message || 'Vấn đề không xác định' });
        reject();
      }
    });
  });
}

/**
 * Lấy thông tin mã QR đăng nhập
 */
export function getLoginQrcode() {
  let url = Api.getLoginQrcode;
  return defHttp.get({ url: url });
}

/**
 * Giám sát trạng thái quét mã
 */
export function getQrcodeToken(params) {
  let url = Api.getQrcodeToken;
  return defHttp.get({ url: url, params });
}

/**
 * SSOKiểm tra đăng nhập
 */
export async function validateCasLogin(params) {
  let url = Api.validateCasLogin;
  return defHttp.get({ url: url, params });
}
