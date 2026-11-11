import type { ProjectConfig } from '../../types/config';
import { MenuTypeEnum, MenuModeEnum, TriggerEnum, MixSidebarTriggerEnum } from '../enums/menuEnum';
import { CacheTypeEnum } from '../enums/cacheEnum';
import {
  ContentEnum,
  PermissionModeEnum,
  ThemeEnum,
  RouterTransitionEnum,
  SettingButtonPositionEnum,
  SessionTimeoutProcessingEnum,
  TabsThemeEnum,
} from '../enums/appEnum';
import { darkMode } from '../settings/designSetting';
import { getConfigByMenuType } from '../utils/getConfigByMenuType';
// Sửa thuộc tính này để chuyển nhanh giữa bốn theme hệ thống mặc định
const menuType = MenuTypeEnum.SIDEBAR;

// update-begin--author:liaozhiyang---date:20251201---for【QQYUN-14176】Chỉ cần sửa một cấu hình là có thể chuyển đổi bốn theme mặc định, không cần sửa thêm màu sắc, v.v.
const { themeColor, headerBgColor, sideBgColor, split, mode } = getConfigByMenuType(menuType);
// update-end--author:liaozhiyang---date:20251201---for【QQYUN-14176】Chỉ cần sửa một cấu hình là có thể chuyển đổi bốn theme mặc định, không cần sửa thêm màu sắc, v.v.
// ! Sau khi thay đổi cần xóa cache trình duyệt
const setting: ProjectConfig = {
  // Có hiển thị SettingButton không
  showSettingButton: true,

  // Có hiển thị nút chuyển theme không
  showDarkModeToggle: true,

  // Vị trí nút Cài đặt
  // SettingButtonPositionEnum.AUTO: Tự động chọn
  // SettingButtonPositionEnum.HEADER: Ở đầu trang
  // SettingButtonPositionEnum.FIXED: Cố định ở bên phải
  settingButtonPosition: SettingButtonPositionEnum.AUTO,

  // Chế độ quyền, mặc định là chế độ quyền vai trò frontend
  // ROUTE_MAPPING: Chế độ frontend (menu do route tạo, mặc định)
  // ROLE: Chế độ frontend (route menu tách riêng)
  // BACK: Chế độ backend
  permissionMode: PermissionModeEnum.BACK,

  // Vị trí lưu cache quyền. Mặc định lưu tại localStorage
  permissionCacheType: CacheTypeEnum.LOCAL,

  // Phương án xử lý hết phiên
  // SessionTimeoutProcessingEnum.ROUTE_JUMP: Route chuyển đến trang đăng nhập
  // SessionTimeoutProcessingEnum.PAGE_COVERAGE: Tạo popup đăng nhập, phủ trang hiện tại
  sessionTimeoutProcessing: SessionTimeoutProcessingEnum.ROUTE_JUMP,
  // Màu chủ đề dự án - Xác định màu theme động theo chế độ thanh điều hướng
  themeColor: themeColor,
  // Chế độ chủ đề dự án
  themeMode: darkMode,

  // Chế độ xám của trang web, dùng để bật vào các ngày tưởng niệm
  grayMode: false,

  // Chế độ mù màu
  colorWeak: false,

  // Có ẩn menu, phần trên và hiển thị nhiều tab không, dùng khi có thể nhúng vào hệ thống khác
  fullContent: false,

  // Độ rộng nội dung theme
  contentMode: ContentEnum.FULL,

  // Có hiển thị logo không
  showLogo: true,

  // Có hiển thị thông tin copyright ở dưới cùng không
  showFooter: false,

  // Có hiển thị biểu tượng AI không
  aiIconShow: false,

  // Cấu hình đầu trang
  headerSetting: {
    // Màu nền
    bgColor: headerBgColor,
    // Cố định đầu trang
    fixed: true,
    // Có hiển thị đầu trang không
    show: true,
    // Theme
    theme: ThemeEnum.LIGHT,
    // Bật chức năng khóa màn hình
    useLockPage: false,
    // Hiển thị nút toàn màn hình
    showFullScreen: false,
    // Hiển thị nút trang web chính thức
    showDoc: false,
    // Hiển thị nút trung tâm tin nhắn
    showNotice: true,
    // Hiển thị nút tìm kiếm menu
    showSearch: true,
  },

  // Cấu hình menu
  menuSetting: {
    // Màu nền
    bgColor: sideBgColor,
    // Có cố định menu bên trái không
    fixed: true,
    // Thu gọn menu
    collapsed: false,
    // Khi thu gọn menu có hiển thị tên menu không
    collapsedShowTitle: false,
    // Có thể kéo thả không
    // Only limited to the opening of the left menu, the mouse has a drag bar on the right side of the menu
    canDrag: false,
    // Whether to show no dom
    show: true,
    // Whether to show dom
    hidden: false,
    // Chiều rộng menu
    menuWidth: 210,
    // Chế độ menu
    mode,
    // Loại menu
    type: menuType,
    // Chủ đề menu
    theme: ThemeEnum.LIGHT,
    // Điều chỉnh màu chữ của thanh điều hướng bên trái để phân biệt chế độ màu sắc và chế độ tối (không tương ứng với cấu hình)
    isThemeBright: false,
    // Tách menu
    split,
    // Bố cục menu phần trên
    topMenuAlign: 'center',
    // Vị trí của trigger thu gọn
    trigger: TriggerEnum.HEADER,
    // Chế độ accordion, chỉ hiển thị một menu
    accordion: true,
    // Khi chuyển route thì đóng menu mở rộng của menu hỗn hợp bên trái
    closeMixSidebarOnChange: false,
    // Cách kích hoạt chuyển module của menu hỗn hợp bên trái: 'click' | 'hover'
    mixSideTrigger: MixSidebarTriggerEnum.CLICK,
    // Có cố định menu hỗn hợp bên trái không
    mixSideFixed: false,
  },

  // Nhiều tab
  multiTabsSetting: {
    // Sau khi làm mới có giữ các tab đã mở không
    cache: false,
    // Bật
    show: true,
    // Có thể kéo thả không
    canDrag: true,
    // Bật thao tác nhanh
    showQuick: true,
    // Có hiển thị nút làm mới không
    showRedo: true,
    // Có hiển thị nút thu gọn không
    showFold: true,
    // Kiểu tab
    theme: TabsThemeEnum.CARD,
  },

  // Cấu hình animation
  transitionSetting: {
    //  Có bật animation chuyển đổi không
    // The disabled state will also disable pageLoading
    enable: true,

    // Tên animation chuyển đổi route cơ bản
    basicTransition: RouterTransitionEnum.FADE_SIDE,

    // Có bật loading khi chuyển trang không
    // Only open when enable=true
    openPageLoading: true,

    // Có mở thanh tiến trình phía trên khi chuyển trang không
    openNProgress: true,
  },

  // Có bật cache KeepAlive không. Khi phát triển tốt nhất nên tắt, nếu không mỗi lần đều cần xóa cache
  openKeepAlive: true,

  // Thời gian tự động khóa màn hình, bằng 0 là không khóa màn hình. Đơn vị: phút, mặc định 1 giờ
  lockTime: 0,

  // Hiển thị breadcrumb
  showBreadCrumb: false,

  // Hiển thị biểu tượng breadcrumb
  showBreadCrumbIcon: true,

  // Có dùng bắt lỗi toàn cục không
  useErrorHandle: false,

  // Có bật quay lại đầu trang không
  useOpenBackTop: true,

  // Có thể nhúng trang iframe không
  canEmbedIFramePage: true,

  // Khi chuyển giao diện có xóa các message và notify chưa đóng không
  closeMessageOnSwitch: true,

  // Khi chuyển giao diện có hủy các yêu cầu http đã gửi nhưng chưa nhận phản hồi không.
  // Nếu bật, muốn ghi đè cho API riêng lẻ thì có thể cài đặt tại API riêng lẻ đó
  removeAllHttpPending: false,
};

export default setting;
