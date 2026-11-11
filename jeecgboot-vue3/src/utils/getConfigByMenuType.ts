import { MenuTypeEnum, MenuModeEnum } from '../enums/menuEnum';
import { APP_PRESET_COLOR_LIST, HEADER_PRESET_BG_COLOR_LIST, SIDE_BAR_BG_COLOR_LIST } from '../settings/designSetting';

/**
 * Lấy màu theme tương ứng theo loại menu và chế độ
 * @param menuType Loại menu
 */
export function getConfigByMenuType(menuType: MenuTypeEnum): {
  themeColor: string;
  headerBgColor: string;
  sideBgColor: string;
  split: boolean;
  mode: MenuModeEnum;
} {
  let themeColor;
  let headerBgColor;
  let sideBgColor;
  let split = false;
  let mode: MenuModeEnum = MenuModeEnum.INLINE;
  if (menuType === MenuTypeEnum.TOP_MENU) {
    // Điều hướng thanh trên
    themeColor = APP_PRESET_COLOR_LIST[1];
    headerBgColor = HEADER_PRESET_BG_COLOR_LIST[4];
    mode = MenuModeEnum.HORIZONTAL;
  } else if (menuType === MenuTypeEnum.MIX) {
    // Chế độ menu hỗn hợp phần trên
    themeColor = APP_PRESET_COLOR_LIST[2];
    headerBgColor = HEADER_PRESET_BG_COLOR_LIST[2];
    sideBgColor = SIDE_BAR_BG_COLOR_LIST[3];
    split = true;
  } else if (menuType === MenuTypeEnum.MIX_SIDEBAR) {
    // Chế độ điều hướng thu gọn bên hông
    themeColor = APP_PRESET_COLOR_LIST[1];
    headerBgColor = HEADER_PRESET_BG_COLOR_LIST[0];
    sideBgColor = SIDE_BAR_BG_COLOR_LIST[0];
  } else if (menuType === MenuTypeEnum.SIDEBAR) {
    // Điều hướng thanh bên
    themeColor = APP_PRESET_COLOR_LIST[1];
    headerBgColor = HEADER_PRESET_BG_COLOR_LIST[4];
    sideBgColor = SIDE_BAR_BG_COLOR_LIST[7];
  }
  return {
    themeColor,
    headerBgColor,
    sideBgColor,
    split,
    mode,
  };
}
