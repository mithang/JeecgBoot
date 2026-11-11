/**
 * @description: Bốn chủ đề menu mặc định
 */
export enum MenuTypeEnum {
  // Kiểu điều hướng menu bên trái
  SIDEBAR = 'sidebar',
  // Kiểu điều hướng thanh trên
  MIX = 'mix',
 // Kiểu điều hướng thu gọn bên hông
  MIX_SIDEBAR = 'mix-sidebar',
  // Kiểu điều hướng hỗn hợp phía trên
  TOP_MENU = 'top-menu',
}

// Vị trí trigger thu gọn
export enum TriggerEnum {
  // Không hiển thị
  NONE = 'NONE',
  // Dưới cùng của menu
  FOOTER = 'FOOTER',
  // Đầu trang
  HEADER = 'HEADER',
}

export type Mode = 'vertical' | 'vertical-right' | 'horizontal' | 'inline';

// menu mode
export enum MenuModeEnum {
  VERTICAL = 'vertical',
  HORIZONTAL = 'horizontal',
  VERTICAL_RIGHT = 'vertical-right',
  INLINE = 'inline',
}

export enum MenuSplitTyeEnum {
  NONE,
  TOP,
  LEFT,
}

export enum TopMenuAlignEnum {
  CENTER = 'center',
  START = 'start',
  END = 'end',
}

export enum MixSidebarTriggerEnum {
  HOVER = 'hover',
  CLICK = 'click',
}
