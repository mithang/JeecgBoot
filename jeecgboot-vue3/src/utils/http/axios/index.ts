// axiosCấu hình  Có thể tự sửa theo dự án, chỉ cần sửa tệp này, các tệp khác không cần động vào
// The axios configuration can be changed according to the project, just change the file, other files can be left unchanged

import type { AxiosResponse } from 'axios';
import type { RequestOptions, Result } from '/#/axios';
import type { AxiosTransform, CreateAxiosOptions } from './axiosTransform';
import { VAxios } from './Axios';
import { checkStatus } from './checkStatus';
import { router } from '/@/router';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { RequestEnum, ResultEnum, ContentTypeEnum, ConfigEnum } from '/@/enums/httpEnum';
import { isString } from '/@/utils/is';
import { getToken, getTenantId } from '/@/utils/auth';
import { setObjToUrlParams, deepMerge } from '/@/utils';
import signMd5Utils from '/@/utils/encryption/signMd5Utils';
import { useErrorLogStoreWithOut } from '/@/store/modules/errorLog';
import { useI18n } from '/@/hooks/web/useI18n';
import { joinTimestamp, formatRequestDate } from './helper';
import { useUserStoreWithOut } from '/@/store/modules/user';
import { cloneDeep } from "lodash-es";
const globSetting = useGlobSetting();
const urlPrefix = globSetting.urlPrefix;
const { createMessage, createErrorModal } = useMessage();

/**
 * @description: Xử lý dữ liệu, tiện phân biệt nhiều cách xử lý
 */
const transform: AxiosTransform = {
  /**
   * @description: Xử lý dữ liệu yêu cầu. Nếu dữ liệu không đúng định dạng mong đợi, có thể ném lỗi trực tiếp
   */
  transformRequestHook: (res: AxiosResponse<Result>, options: RequestOptions) => {
    const { t } = useI18n();
    const { isTransformResponse, isReturnNativeResponse } = options;
    // Có trả về header phản hồi gốc không Ví dụ: khi cần lấy header phản hồi thì dùng thuộc tính này
    if (isReturnNativeResponse) {
      return res;
    }
    // Không xử lý gì, trả về trực tiếp
    // Dùng cho mã trang có thể cần lấy trực tiếpcode, data, messageKhi các thông tin này thì bật
    if (!isTransformResponse) {
      return res.data;
    }
    // Khi lỗi thì trả về

    const { data } = res;
    if (!data) {
      // return '[HTTP] Request has no return value';
      throw new Error(t('sys.api.apiRequestFailed'));
    }
    //  Ở đây code, result, messagelà Trường thống nhất của backend, cần ở types.tsBên trong sửa thành định dạng trả về của API riêng của dự án
    const { code, result, message, success } = data;
    // Logic ở đây có thể sửa theo dự án
    const hasSuccess = data && Reflect.has(data, 'code') && (code === ResultEnum.SUCCESS || code === 200);
    if (hasSuccess) {
      if (success && message && options.successMessageMode === 'success') {
        //Gợi ý thành công thông tin
        createMessage.success(message);
      }
      return result;
    }

    // Ở đây căn cứ tình hình thực tế của dự án để xử lý khác nhaucodeThực hiện các thao tác khác nhau
    // Nếu không muốn ngắt yêu cầu hiện tại, vui lòngreturndữ liệu, nếu không thì ném ngoại lệ trực tiếp
    let timeoutMsg = '';
    switch (code) {
      case ResultEnum.TIMEOUT:
        timeoutMsg = t('sys.api.timeoutMessage');
        const userStore = useUserStoreWithOut();
        userStore.setToken(undefined);
        userStore.logout(true);
        break;
      default:
        if (message) {
          timeoutMsg = message;
        }
    }

    // errorMessageMode=‘modal’khi sẽ hiển thịmodalPopup lỗi, chứ không phải gợi ý tin nhắn, dùng cho một số lỗi quan trọng
    // errorMessageMode='none' Thông thường khi gọi, nêu rõ không muốn tự động bật gợi ý lỗi
    if (options.errorMessageMode === 'modal') {
      createErrorModal({ title: t('sys.api.errorTip'), content: timeoutMsg });
    } else if (options.errorMessageMode === 'message') {
      createMessage.error(timeoutMsg);
    }

    throw new Error(timeoutMsg || t('sys.api.apiRequestFailed'));
  },

  // Xử lý trước khi yêu cầuconfig
  beforeRequestHook: (config, options) => {
    const { apiUrl, joinPrefix, joinParamsToUrl, formatDate, joinTime = true, urlPrefix } = options;

    // httpYêu cầu bắt đầu bằngurl, không thêm tiền tố
    let isStartWithHttp = false;
    const requestUrl = config.url;
    if(requestUrl!=null && (requestUrl.startsWith("http:") || requestUrl.startsWith("https:"))){
      isStartWithHttp = true;
    }
    // Giải thích logic mã: 【QQYUN-9685]Xây dựng electron Ứng dụng desktop
    if (!isStartWithHttp && requestUrl != null) {
      // Doelectroncủaurllàfile://bắt đầu bằng, nên cần kiểm tra
      isStartWithHttp = requestUrl.startsWith('file://');
    }
    if (!isStartWithHttp && joinPrefix) {
      config.url = `${urlPrefix}${config.url}`;
    }

    if (!isStartWithHttp && apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`;
    }
    
    const params = config.params || {};
    const data = config.data || false;
    formatDate && data && !isString(data) && formatRequestDate(data);
    if (config.method?.toUpperCase() === RequestEnum.GET) {
      if (!isString(params)) {
        // Cho get Yêu cầu thêm tham số timestamp, tránh lấy dữ liệu từ cache.
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false));
      } else {
        // Tương thíchrestfulPhong cách
        config.url = config.url + params + `${joinTimestamp(joinTime, true)}`;
        config.params = undefined;
      }
    } else {
      if (!isString(params)) {
        formatDate && formatRequestDate(params);
        if (Reflect.has(config, 'data') && config.data && Object.keys(config.data).length > 0) {
          config.data = data;
          config.params = params;
        } else {
          // Không phảiGETYêu cầu nếu không cung cấpdata, thì sẽparamsCoi làdata
          config.data = params;
          config.params = undefined;
        }
        if (joinParamsToUrl) {
          config.url = setObjToUrlParams(config.url as string, Object.assign({}, config.params, config.data));
        }
      } else {
        // Tương thíchrestfulPhong cách
        config.url = config.url + params;
        config.params = undefined;
      }
    }

    // Giải thích logic mã: 【JEECGLà micro app Qiankun] Khi khởi động dưới dạng micro app Qiankun, nối đường dẫn yêu cầu
    if (globSetting.isQiankunMicro) {
      if (config.url && config.url.startsWith('/')) {
        config.url = globSetting.qiankunMicroAppEntry + config.url
      }
    }

    return config;
  },

  /**
   * @description: Xử lý request interceptor
   */
  requestInterceptors: (config: Recordable, options) => {
    // Xử lý trước khi yêu cầuconfig
    const token = getToken();
    let tenantId: string | number = getTenantId();
    
    // Thêm chữ ký và dấu thời gian vào API yêu cầu Header
    config.headers[ConfigEnum.TIMESTAMP] = signMd5Utils.getTimestamp();
    config.headers[ConfigEnum.Sign] = signMd5Utils.getSign(config.url, cloneDeep(config.params), cloneDeep(config.data));
    
    config.headers[ConfigEnum.VERSION] = 'v3';
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      // jwt token
      config.headers.Authorization = options.authenticationScheme ? `${options.authenticationScheme} ${token}` : token;
      config.headers[ConfigEnum.TOKEN] = token;
      
      // Thêm chữ ký và dấu thời gian vào API yêu cầu Header
      //config.headers[ConfigEnum.TIMESTAMP] = signMd5Utils.getTimestamp();
      //config.headers[ConfigEnum.Sign] = signMd5Utils.getSign(config.url, config.params);
      if (!tenantId) {
        tenantId = 0;
      }

      // Giải thích logic mã: 【QQYUN-5279]Sửa gợi ý khi tenant của ứng dụng được chia sẻ không khớp với tenant đang đăng nhập404vấn đề
      const userStore = useUserStoreWithOut();
      // Kiểm tra có tenant tạm thời khôngid
      if (userStore.hasShareTenantId && userStore.shareTenantId !== 0) {
        // Tenant tạm thờiidTồn tại, dùng tenant tạm thờiid
        tenantId = userStore.shareTenantId!;
      }

      config.headers[ConfigEnum.TENANT_ID] = tenantId;
      //--update-end--author:liusq---date:20211105---for:Đưa nhiều tenantid, thêm vào API request Header

      // ========================================================================================
      // Giải thích logic mã: Thêm ứng dụng low-codeID
      let routeParams = router.currentRoute.value.params;
      if (routeParams.appId) {
        config.headers[ConfigEnum.X_LOW_APP_ID] = routeParams.appId;
        // lowAppĐiều kiện lọc tùy chỉnh
        if (routeParams.lowAppFilter) {
          config.params = { ...config.params, ...JSON.parse(routeParams.lowAppFilter as string) };
          delete routeParams.lowAppFilter;
        }
      }
      // ========================================================================================

    }
    return config;
  },

  /**
   * @description: Xử lý interceptor phản hồi
   */
  responseInterceptors: (res: AxiosResponse<any>) => {
    return res;
  },

  /**
   * @description: Xử lý lỗi phản hồi
   */
  responseInterceptorsCatch: (error: any) => {
    const { t } = useI18n();
    const errorLogStore = useErrorLogStoreWithOut();
    errorLogStore.addAjaxErrorInfo(error);
    const { response, code, message, config } = error || {};
    const errorMessageMode = config?.requestOptions?.errorMessageMode || 'none';
    //scott 20211022 tokenThông tin gợi ý hết hiệu lực
    //const msg: string = response?.data?.error?.message ?? '';
    const msg: string = response?.data?.message ?? '';
    const err: string = error?.toString?.() ?? '';
    let errMessage = '';

    try {
      if (code === 'ECONNABORTED' && message.indexOf('timeout') !== -1) {
        errMessage = t('sys.api.apiTimeoutMessage');
      }
      if (err?.includes('Network Error')) {
        errMessage = t('sys.api.networkExceptionMsg');
      }

      if (errMessage) {
        if (errorMessageMode === 'modal') {
          createErrorModal({ title: t('sys.api.errorTip'), content: errMessage });
        } else if (errorMessageMode === 'message') {
          createMessage.error(errMessage);
        }
        return Promise.reject(error);
      }
    } catch (error) {
      throw new Error(error);
    }

    checkStatus(error?.response?.status, msg, errorMessageMode);
    return Promise.reject(error);
  },
};

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    deepMerge(
      {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // authentication schemes, e.g: Bearer
        // authenticationScheme: 'Bearer',
        authenticationScheme: '',
        //Cài đặt timeout API
        timeout: 10 * 1000,
        // Địa chỉ API cơ sở
        // baseURL: globSetting.apiUrl,
        headers: { 'Content-Type': ContentTypeEnum.JSON },
        // Nếu làform-dataĐịnh dạng
        // headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
        // Cách xử lý dữ liệu
        transform,
        // Mục cấu hình, các tùy chọn dưới đây đều có thể ghi đè trong request API độc lập
        requestOptions: {
          // Mặc định sẽprefix Thêm vàourl
          joinPrefix: true,
          // Có trả về header phản hồi gốc không Ví dụ: khi cần lấy header phản hồi thì dùng thuộc tính này
          isReturnNativeResponse: false,
          // Cần xử lý dữ liệu trả về
          isTransformResponse: true,
          // postKhi yêu cầu thêm tham số vàourl
          joinParamsToUrl: false,
          // Định dạng thời gian tham số gửi
          formatDate: true,
          // Loại gợi ý thông báo ngoại lệ
          errorMessageMode: 'message',
          // Loại gợi ý thông báo thành công
          successMessageMode: 'success',
          // Địa chỉ API
          apiUrl: globSetting.apiUrl,
          // Địa chỉ nối API
          urlPrefix: urlPrefix,
          //  Có thêm dấu thời gian không
          joinTime: true,
          // Bỏ qua yêu cầu trùng lặp
          ignoreCancelToken: true,
          // Có mang theo khôngtoken
          withToken: true,
        },
      },
      opt || {}
    )
  );
}
export const defHttp = createAxios();

// other api url
// export const otherHttp = createAxios({
//   requestOptions: {
//     apiUrl: 'xxx',
//   },
// });
