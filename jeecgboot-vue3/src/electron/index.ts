import type {App} from "vue";
import {router} from "@/router";
import {useGlobSetting} from "@/hooks/setting";
import { ElectronEnum } from '/@/enums/jeecgEnum'
const glob = useGlobSetting();

const _PRELOAD_UTILS = ElectronEnum.ELECTRON_API;

export const $electron = {
  // Hiện tại có phải là khôngElectronNền tảng
  isElectron: () => glob.isElectronPlatform,

  // Mở liên kết qua trình duyệt
  openInBrowser: bindUtils('openInBrowser') as (url: string) => void,

  resolveRoutePath,
}

function bindUtils(n: string) {
  const fn = window[_PRELOAD_UTILS]?.[n];
  if (typeof fn?.bind === 'function') {
    return fn.bind(null);
  }
  return () => console.warn(`Electron preload util ${n} is not a function`);
}

// Phân tích đường dẫn route
function resolveRoutePath(path: string) {
  return window.location.origin + window.location.pathname + router.resolve(path).href;
}

/**
 * Cấu hìnhElectron
 */
export function setupElectron(_: App) {
  // Không phảiElectronNền tảng không thực thi
  if (!$electron.isElectron()) {
    return;
  }
  hookWindowOpen();
  // Giải thích logic mã: 【JHHB-13]Thông báo tin nhắn ứng dụng desktop
  hookNavigate();
}
function hookNavigate() {
  // @ts-ignore
  window[ElectronEnum.ELECTRON_API].onNavigate((path) => {
    router.push({ path });
  });
}
function hookWindowOpen() {
  // Lưu tham chiếu phương thức gốc
  const originFunc = window.open;
  // Viết lạiwindow.openPhương thức
  window['open'] = function (url, windowName, windowFeatures) {
    url = typeof url === 'string' ? url.trim() : '';
    if (!url) {
      throw new Error('window.open: url is required');
    }
    // Kiểm tra có bắt đầu bằng khônghttphoặchttpsĐầu
    if (/^https?:\/\//.test(url)) {
      // Kiểm tra có phải địa chỉ local không
      if (url.startsWith(window.location.origin) || url.startsWith(window['_CONFIG']['domianURL'])) {
        // Mở trực tiếp
        return originFunc(url, windowName, windowFeatures);
      }
      // GọiElectronTiến hành mở bằng trình duyệt ngoài
      return $electron.openInBrowser(url) as any;
    }
    // Logic tùy chỉnh
    return originFunc(url, windowName, windowFeatures)
  }
}
