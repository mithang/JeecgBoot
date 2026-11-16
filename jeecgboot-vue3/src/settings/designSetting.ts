import { ThemeEnum } from '../enums/appEnum';

export const prefixCls = 'jeecg';

export const darkMode = ThemeEnum.LIGHT;

// Phase 4 Step 3 (G2) — Bảng màu thương hiệu "Xanh Jeecg" (xanh dương đậm, tin cậy kiểu SaaS Việt).
// Quyết định tự chốt theo UI_UX_REFACTOR_PLAN.md mục 10 Bước 3 (tông xanh dương đậm thay cho xanh teal):
//   primary #0B57D0 | dark #0A3D91 (header) | light #2E6FE8 | tint #5B8DEF | navy #0B2447 (sider)
//   nền nội dung #F0F4F8 (xem src/design/color.less), radius 8px (xem src/App.vue token).
// Thứ tự các mục trong 3 danh sách preset được GIỮ NGUYÊN vì getConfigByMenuType.ts
// và updateBackground.ts tham chiếu theo chỉ số (index 1 = theme mặc định, 4 = header mặc định,
// 7 = sider mặc định); chỉ thay GIÁ TRỊ ở các vị trí đó bằng biến thể thương hiệu.
// Người dùng vẫn đổi được màu trong ngăn Cài đặt — chỉ mặc định đổi sang thương hiệu.

// app theme preset color
export const APP_PRESET_COLOR_LIST: string[] = [
  '#0A3D91',
  '#0B57D0',
  '#2E6FE8',
  '#5B8DEF',
  '#009688',
  '#536dfe',
  '#13c2c2',
  '#52c41a',
  '#faad14',
  '#ee4f12',
  '#9c27b0',
];

// header preset color
export const HEADER_PRESET_BG_COLOR_LIST: string[] = [
  '#ffffff',
  '#151515',
  '#0B57D0',
  '#5172DC',
  '#0A3D91',
  '#13c2c2',
  '#e74c3c',
  '#52c41a',
  '#394664',
  '#faad14',
  '#383f45',
];

// sider preset color
export const SIDE_BAR_BG_COLOR_LIST: string[] = [
  '#001529',
  // '#212121',
  '#009688',
  '#273352',
  '#ffffff',
  '#191b24',
  // '#191a23',
  '#037bd5',
  '#304156',
  '#0B2447',
  '#28333E',
  // '#344058',
  '#e74c3c',
  '#383f45',
];

// sider logo line preset color [logo����ɫ]
export const SIDER_LOGO_BG_COLOR_LIST: string[] = [
  'linear-gradient(180deg, #0B2447, #0A3D91)',
  // 'linear-gradient(180deg, #000000, #282828)',
  'linear-gradient(180deg, #078d80, #029184)',
  'linear-gradient(180deg, #1c253e, #2b385c)',
  'linear-gradient(180deg, #ffffff, #ffffff)',
  'linear-gradient(180deg, #000000, #242735)',
  // 'linear-gradient(180deg, #000000, #1d1f2a)',
  'linear-gradient(180deg, #1d77bb, #188efa)',
  'linear-gradient(180deg, #304156, #32455d)',
  'linear-gradient(180deg, #0B2447, #0B57D0)',
  'linear-gradient(180deg, #000000, #2b3743)',
  // 'linear-gradient(180deg, #344058, #37455c)',
  'linear-gradient(180deg, #e83723, #e52611)',
  'linear-gradient(180deg, #383f45, #3b434b)',
];
