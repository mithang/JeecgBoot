import type { GlobConfig } from '/#/config';

import { getAppEnvConfig } from '/@/utils/env';

export const useGlobSetting = (): Readonly<GlobConfig> => {
  const {
    VITE_GLOB_APP_TITLE,
    VITE_GLOB_API_URL,
    VITE_GLOB_APP_SHORT_NAME,
    VITE_GLOB_API_URL_PREFIX,
    VITE_GLOB_APP_CAS_BASE_URL,
    VITE_GLOB_APP_OPEN_SSO,
    VITE_GLOB_APP_OPEN_QIANKUN,
    VITE_GLOB_DOMAIN_URL,
    VITE_GLOB_ONLINE_VIEW_URL,
    VITE_GLOB_RUN_PLATFORM,

    // 【JEECGLà micro app Qiankun][Kiểm tra hiện tại có đang chạy ở chế độ micro app Qiankun không]
    VITE_GLOB_QIANKUN_MICRO_APP_NAME,
    VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,
  } = getAppEnvConfig();

  // if (!/[a-zA-Z\_]*/.test(VITE_GLOB_APP_SHORT_NAME)) {
  //   warn(
  //     `VITE_GLOB_APP_SHORT_NAME Variables can only be characters/underscores, please modify in the environment variables and re-running.`
  //   );
  // }

  // Tiêu đề ngắn: thay thếshortNamegạch dưới của thành khoảng trắng
  const shortTitle = VITE_GLOB_APP_SHORT_NAME.replace(/_/g, " ");
  // Take global configuration
  const glob: Readonly<GlobConfig> = {
    title: VITE_GLOB_APP_TITLE,
    domainUrl: VITE_GLOB_DOMAIN_URL,
    apiUrl: VITE_GLOB_API_URL,
    shortName: VITE_GLOB_APP_SHORT_NAME,
    shortTitle: shortTitle,
    openSso: VITE_GLOB_APP_OPEN_SSO,
    openQianKun: VITE_GLOB_APP_OPEN_QIANKUN,
    casBaseUrl: VITE_GLOB_APP_CAS_BASE_URL,
    urlPrefix: VITE_GLOB_API_URL_PREFIX,
    uploadUrl: VITE_GLOB_DOMAIN_URL,
    viewUrl: VITE_GLOB_ONLINE_VIEW_URL,
    // true: Popup trang xử lý nhiệm vụ mới, false:Popup trang xử lý nhiệm vụ cũ
    useNewTaskModal: true,
    // Hiện tại có đang chạy trong không electron Nền tảng
    isElectronPlatform: VITE_GLOB_RUN_PLATFORM === 'electron',

    // 【JEECGLà micro app Qiankun] Có khởi động ở chế độ micro app Qiankun không
    isQiankunMicro: VITE_GLOB_QIANKUN_MICRO_APP_NAME != null && VITE_GLOB_QIANKUN_MICRO_APP_NAME !== '',
    // 【JEECGLà micro app Qiankun] Điểm vào micro app Qiankun
    qiankunMicroAppEntry: VITE_GLOB_QIANKUN_MICRO_APP_ENTRY,
  };

  // 【JEECGLà micro app Qiankun] Dưới micro app Qiankun, cần định nghĩa
  if (!window['_CONFIG']) {
    window['_CONFIG'] = {}
  }

  // Giải thích logic mã: 【QQYUN-10956]Đã cấu hình tiền tố tùy chỉnh, liên kết ngoài không mở được, cần xử lý tương thích
  let domainURL = VITE_GLOB_DOMAIN_URL;

  // Nếu không bắt đầu bằnghttp(s)bắt đầu bằng, cũng không bắt đầu bằng tên miền, thì nối tên miền hiện tại
  if (!/^http(s)?/.test(domainURL) && !/^(\/\/)?(.*\.)?.+\..+/.test(domainURL)) {
    if (!domainURL.startsWith('/')) {
      domainURL = '/' + domainURL;
    }
    domainURL = window.location.origin + domainURL;
  }

  // @ts-ignore
  window._CONFIG['domianURL'] = domainURL;

  return glob as Readonly<GlobConfig>;
};
