import { getThemeColors, generateColors } from '../../../build/config/themeConfig';
import {
  replaceStyleVariables,
  loadDarkThemeCss,
  replaceCssColors,
  darkCssIsReady,
  linkID,
  styleTagId,
  appendCssToDom,
  getStyleDom,
} from '@rys-fe/vite-plugin-theme/es/client';
import { mixLighten, mixDarken, tinycolor } from '@rys-fe/vite-plugin-theme/es/colorUtils';
import { useAppStore } from '/@/store/modules/app';
import { defHttp } from '/@/utils/http/axios';

let cssText = '';
export async function changeTheme(color: string) {
  // Giải thích logic mã: 【QQYUN-6366]Nâng cấp lênantd4.x
  const appStore = useAppStore();
  appStore.setProjectConfig({ themeColor: color });
  const colors = generateColors({
    mixDarken,
    mixLighten,
    tinycolor,
    color,
  });
  // Giải thích logic mã: 【QQYUN-8570]Trong môi trường production, màu theme ở chế độ tối không có hiệu lực
  if (import.meta.env.PROD && appStore.getDarkMode === 'dark') {
    if (!darkCssIsReady && !cssText) {
      await loadDarkThemeCss();
    }
    const el: HTMLLinkElement = document.getElementById(linkID) as HTMLLinkElement;
    if (el?.href) {
      // cssText = await fetchCss(el.href) as string;
      !cssText && (cssText = await defHttp.get({ url: el.href }, { isTransformResponse: false }));
      const colorVariables = [...getThemeColors(color), ...colors];
      const processCss = await replaceCssColors(cssText, colorVariables);
      appendCssToDom(getStyleDom(styleTagId) as HTMLStyleElement, processCss);
    }
  } else {
    await replaceStyleVariables({
      colorVariables: [...getThemeColors(color), ...colors],
    });
    fixDark();
  }
}
// 【LOWCOD-2262]Sửa vấn đề chuyển skin ở chế độ tối không có hiệu lực
async function fixDark() {
  // Giải thích logic mã: 【QQYUN-8570]Trong môi trường production, màu theme ở chế độ tối không có hiệu lực
  const el = document.getElementById(styleTagId);
  if (el) {
    el.innerHTML = el.innerHTML.replace(/\\["']dark\\["']/g, `'dark'`);
  }
}
