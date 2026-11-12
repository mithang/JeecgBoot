import type {BrowserWindowConstructorOptions} from 'electron';
import {app, BrowserWindow, dialog} from 'electron';
import path from 'path';
import {_PATHS} from '../paths';
import {$env, isDev} from '../env';
import {createTray} from './tray';

// Lấy tùy chọn cửa sổ chung
export function getBrowserWindowOptions(options?: BrowserWindowConstructorOptions): BrowserWindowConstructorOptions {
  return {
    width: 1200,
    height: 800,
    webPreferences: {
      preload: path.join(_PATHS.preloadRoot, 'index.js'),
      nodeIntegration: false,
      contextIsolation: true,
    },
    // Biểu tượng ứng dụng
    icon: isDev ? _PATHS.appIcon : void 0,
    ...options,
  }
}

// Tạo cửa sổ
export function createBrowserWindow(options?: BrowserWindowConstructorOptions) {
  const win = new BrowserWindow(getBrowserWindowOptions(options));
  // Giải thích logic code: 【JHHB-13】Thông báo tin nhắn của ứng dụng desktop
  if (process.platform === 'darwin') { // Chỉ có hiệu lực trên macOS
    if (app.dock) {
      app.dock.setIcon(path.join(_PATHS.electronRoot, './icons/mac/dock.png').replace(/[\\/]dist[\\/]/, '/'));
    }
  }

  // Thiết lập bộ xử lý mở cửa sổ
  win.webContents.setWindowOpenHandler(() => {
    return {
      action: 'allow',
      // Ghi đè tùy chọn của cửa sổ mới, dùng để điều chỉnh kích thước mặc định và tải script preload, v.v.
      overrideBrowserWindowOptions: getBrowserWindowOptions(),
    }
  });

  // Kích hoạt khi beforeunload ngăn cửa sổ đóng
  win.webContents.on('will-prevent-unload', () => {
    const choice = dialog.showMessageBoxSync(win, {
      type: 'question',
      title: 'Bạn có chắc chắn muốn đóng không?',
      message: 'Hệ thống có thể sẽ không lưu các thay đổi bạn đã thực hiện.',
      buttons: ['Đóng', 'Hủy'],
      defaultId: 1,
      cancelId: 1,
      noLink: true,
    });
    // Người dùng đã chọn đóng, hủy cửa sổ trực tiếp
    if (choice === 0) {
      win.destroy();
    }
  });

  return win;
}

// Tạo cửa sổ chính và khay hệ thống
export function createMainWindow() {
  const win = createIndexWindow()

  // Thiết lập biểu tượng khay hệ thống
  createTray(win);

  // Khi cửa sổ chính thử đóng, mặc định không thoát ứng dụng trực tiếp mà ẩn xuống khay hệ thống
  win.on('close', (event) => {
    event.preventDefault();
    win.hide();
  });

  return win;
}

// Tạo cửa sổ index
export function createIndexWindow() {
  const win = createBrowserWindow({
    width: 1600,
    height: 1000,
    title: $env.VITE_GLOB_APP_TITLE!,
  });

  // Môi trường phát triển tải dịch vụ Vite, môi trường production tải tệp đã đóng gói
  if (isDev) {
    let serverUrl = $env.VITE_DEV_SERVER_URL! as string;
    // 【JHHB-936】Do xem trước wps không thể truy cập bằng localhost, nên thay localhost bằng 127.0.0.1
    serverUrl = serverUrl.replace('localhost', '127.0.0.1');
    win.loadURL(serverUrl)
    // Trong môi trường phát triển, tự động mở công cụ gỡ lỗi
    // win.webContents.openDevTools()
  } else {
    win.loadFile(path.join(_PATHS.publicRoot, 'index.html'));
  }

  return win;
}
