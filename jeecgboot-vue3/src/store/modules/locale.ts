import type { LocaleSetting, LocaleType } from '/#/config';

import { defineStore } from 'pinia';
import { store } from '/@/store';

import { LOCALE_KEY } from '/@/enums/cacheEnum';
import { createLocalStorage } from '/@/utils/cache';
import { localeSetting } from '/@/settings/localeSetting';

const ls = createLocalStorage();

const lsLocaleSetting = (ls.get(LOCALE_KEY) || localeSetting) as LocaleSetting;

interface LocaleState {
  localInfo: LocaleSetting;
  pathTitleMap: object;
  // Màu theme của myapps (trang chủ danh sách ứng dụng low-code)
  appIndexTheme: string
  // myapps - Địa chỉ route trước khi chuyển
  appMainPth: string
}

export const useLocaleStore = defineStore('app-locale', {
  state: (): LocaleState => ({
    localInfo: lsLocaleSetting,
    pathTitleMap: {},
    appIndexTheme: '',
    appMainPth: ''
  }),
  getters: {
    getShowPicker(): boolean {
      return !!this.localInfo?.showPicker;
    },
    getLocale(): LocaleType {
      return this.localInfo?.locale ?? 'zh_CN';
    },
    // Giải thích logic mã: VUEN-1144 online sau khi cấu hình thành menu, khi mở menu, tên hiển thị không hiển thị đúng tên menu
    getPathTitle: (state) => {
      return (path) => state.pathTitleMap[path];
    },
    getAppIndexTheme(): string {
      return this.appIndexTheme;
    },
    getAppMainPth(): string {
      return this.appMainPth;
    },
  },
  actions: {
    /**
     * Set up multilingual information and cache
     * @param info multilingual info
     */
    setLocaleInfo(info: Partial<LocaleSetting>) {
      this.localInfo = { ...this.localInfo, ...info };
      ls.set(LOCALE_KEY, this.localInfo);
    },
    /**
     * Initialize multilingual information and load the existing configuration from the local cache
     */
    initLocale() {
      this.setLocaleInfo({
        ...localeSetting,
        ...this.localInfo,
      });
    },
    // Giải thích logic mã: VUEN-1144 online sau khi cấu hình thành menu, khi mở menu, tên hiển thị không hiển thị đúng tên menu
    setPathTitle(path, title) {
      this.pathTitleMap[path] = title;
    },
    setAppIndexTheme(theme) {
      this.appIndexTheme = theme;
    },
    setAppMainPth(path) {
      this.appMainPth = path;
    },
  },
});

// Need to be used outside the setup
export function useLocaleStoreWithOut() {
  return useLocaleStore(store);
}
