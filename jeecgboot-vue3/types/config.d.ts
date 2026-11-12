import { MenuTypeEnum, MenuModeEnum, TriggerEnum, MixSidebarTriggerEnum } from '/@/enums/menuEnum';
import {
  ContentEnum,
  PermissionModeEnum,
  ThemeEnum,
  RouterTransitionEnum,
  SettingButtonPositionEnum,
  SessionTimeoutProcessingEnum,
} from '/@/enums/appEnum';

import { CacheTypeEnum } from '/@/enums/cacheEnum';

export type LocaleType = 'zh_CN' | 'en' | 'ru' | 'ja' | 'ko';

export interface MenuSetting {
  bgColor: string;
  fixed: boolean;
  collapsed: boolean;
  canDrag: boolean;
  show: boolean;
  hidden: boolean;
  split: boolean;
  menuWidth: number;
  mode: MenuModeEnum;
  type: MenuTypeEnum;
  theme: ThemeEnum;
  // Giải thích logic code: 【QQYUN-8922】Điều chỉnh màu chữ của thanh điều hướng bên trái để phân biệt màu sắc và tối
  isThemeBright: boolean;
  topMenuAlign: 'start' | 'center' | 'end';
  trigger: TriggerEnum;
  accordion: boolean;
  closeMixSidebarOnChange: boolean;
  collapsedShowTitle: boolean;
  mixSideTrigger: MixSidebarTriggerEnum;
  mixSideFixed: boolean;
}

export interface MultiTabsSetting {
  cache: boolean;
  show: boolean;
  showQuick: boolean;
  canDrag: boolean;
  showRedo: boolean;
  showFold: boolean;
  theme: string;
}

export interface HeaderSetting {
  bgColor: string;
  fixed: boolean;
  show: boolean;
  theme: ThemeEnum;
  // Có hiển thị nút toàn màn hình không
  showFullScreen: boolean;
  // Có hiển thị nút khóa màn hình không
  useLockPage: boolean;
  // Có hiển thị liên kết tài liệu không
  showDoc: boolean;
  // Có hiển thị biểu tượng tin nhắn không
  showNotice: boolean;
  // Có hiển thị nút tìm kiếm không
  showSearch: boolean;
}

export interface LocaleSetting {
  // Có hiển thị nút chuyển đổi ngôn ngữ không
  showPicker: boolean;
  // Current language
  locale: LocaleType;
  // default language
  fallback: LocaleType;
  // available Locales
  availableLocales: LocaleType[];
}

export interface TransitionSetting {
  //  Whether to open the page switching animation
  enable: boolean;
  // Route basic switching animation
  basicTransition: RouterTransitionEnum;
  // Whether to open page switching loading
  openPageLoading: boolean;
  // Whether to open the top progress bar
  openNProgress: boolean;
}

export interface ProjectConfig {
  // Storage location of permission related information
  permissionCacheType: CacheTypeEnum;
  // Whether to show the configuration button
  showSettingButton: boolean;
  // Whether to show the theme switch button
  showDarkModeToggle: boolean;
  // Configure where the button is displayed
  settingButtonPosition: SettingButtonPositionEnum;
  // Permission mode
  permissionMode: PermissionModeEnum;
  // Session timeout processing
  sessionTimeoutProcessing: SessionTimeoutProcessingEnum;
  // Website gray mode, open for possible mourning dates
  grayMode: boolean;
  // Whether to turn on the color weak mode
  colorWeak: boolean;
  // Theme color
  themeColor: string;
  // Theme Mode
  themeMode: string;

  // The main interface is displayed in full screen, the menu is not displayed, and the top
  fullContent: boolean;
  // content width
  contentMode: ContentEnum;
  // Whether to display the logo
  showLogo: boolean;
  // Whether to show the global footer
  showFooter: boolean;
  // menuType: MenuTypeEnum;
  headerSetting: HeaderSetting;
  // menuSetting
  menuSetting: MenuSetting;
  // Multi-tab settings
  multiTabsSetting: MultiTabsSetting;
  // Animation configuration
  transitionSetting: TransitionSetting;
  // pageLayout whether to enable keep-alive
  openKeepAlive: boolean;
  // Lock screen time
  lockTime: number;
  // Show breadcrumbs
  showBreadCrumb: boolean;
  // Show breadcrumb icon
  showBreadCrumbIcon: boolean;
  // Use error-handler-plugin
  useErrorHandle: boolean;
  // Whether to open back to top
  useOpenBackTop: boolean;
  // Is it possible to embed iframe pages
  canEmbedIFramePage: boolean;
  // Whether to delete unclosed messages and notify when switching the interface
  closeMessageOnSwitch: boolean;
  // Whether to cancel the http request that has been sent but not responded when switching the interface.
  removeAllHttpPending: boolean;
  aiIconShow: boolean;
}

export interface GlobConfig {
  // Site title
  title: string;
  // Service interface url
  apiUrl: string;
  domainUrl: string;
  // Upload url (đã bỏ)
  uploadUrl?: string;
  openSso?: string;
  openQianKun?: string;
  casBaseUrl?: string;
  // onlineview url
  viewUrl?: string;
  //  Service interface url prefix
  urlPrefix?: string;
  // Project abbreviation
  shortName: string;
  // Tiêu đề ngắn
  shortTitle: string;
  // Dùng popup nhiệm vụ mới
  useNewTaskModal: boolean;
  // Hiện có đang chạy trên nền tảng electron không
  isElectronPlatform: boolean;

  // 【JEECG là ứng dụng con qiankun】Có khởi động ở chế độ ứng dụng con qiankun không
  isQiankunMicro: boolean;
  // 【JEECG là ứng dụng con qiankun】Điểm vào của ứng dụng con qiankun
  qiankunMicroAppEntry?: string;
}
export interface GlobEnvConfig {
  // Site title
  VITE_GLOB_APP_TITLE: string;
  // Service interface url
  VITE_GLOB_API_URL: string;
  VITE_USE_MOCK: string;
  // Service interface url prefix
  VITE_GLOB_API_URL_PREFIX?: string;
  // Project abbreviation
  VITE_GLOB_APP_SHORT_NAME: string;
  // Có bật đăng nhập một lần (SSO) không
  VITE_GLOB_APP_OPEN_SSO: string;
  // Có bật chế độ micro-app không
  VITE_GLOB_APP_OPEN_QIANKUN: string;
  // Địa chỉ server đăng nhập một lần
  VITE_GLOB_APP_CAS_BASE_URL: string;
  VITE_GLOB_DOMAIN_URL: string;
  // Upload url
  VITE_GLOB_UPLOAD_URL?: string;
  // view url
  VITE_GLOB_ONLINE_VIEW_URL?: string;
  // Ẩn toàn cục những layout nào, nhiều layout ngăn cách bằng dấu phẩy
  VITE_GLOB_HIDE_LAYOUT_TYPES?: string;

  // 【JEECG là ứng dụng con qiankun】Sau khi điền sẽ khởi động dưới dạng ứng dụng con qiankun; AppName khi ứng dụng chính đăng ký phải giữ nhất quán
  VITE_GLOB_QIANKUN_MICRO_APP_NAME?: string;
  // 【JEECG là ứng dụng con qiankun】Bắt buộc điền khi khởi động dưới dạng ứng dụng con qiankun; phải nhất quán với entry mà ứng dụng chính qiankun điền khi đăng ký ứng dụng con
  VITE_GLOB_QIANKUN_MICRO_APP_ENTRY?: string;
  // Phiên bản chỉnh sửa tài liệu trực tuyến. Thuộc tính tùy chọn: wps, onlyoffice
  VITE_GLOB_ONLINE_DOCUMENT_VERSION?: string;
  // Hiện đang chạy trên nền tảng nào
  VITE_GLOB_RUN_PLATFORM?: 'web' | 'electron';
}
