import type { DropMenu } from '../components/Dropdown';
import type { LocaleSetting, LocaleType } from '/#/config';

export const LOCALE: { [key: string]: LocaleType } = {
  ZH_CN: 'zh_CN',
  EN_US: 'en',
};

export const localeSetting: LocaleSetting = {
  // Có hiển thị bộ chọn ngôn ngữ không
  showPicker: true,
  // Ngôn ngữ hiện tại
  locale: LOCALE.ZH_CN,
  // Ngôn ngữ mặc định
  fallback: LOCALE.ZH_CN,
  // Ngôn ngữ cho phép
  availableLocales: [LOCALE.ZH_CN, LOCALE.EN_US],
};

// Danh sách ngôn ngữ
export const localeList: DropMenu[] = [
  {
    text: 'Tiếng Trung giản thể',
    event: LOCALE.ZH_CN,
  },
  {
    text: 'English',
    event: LOCALE.EN_US,
  },
];
