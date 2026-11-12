import { app, BrowserWindow, Menu } from 'electron';
import { isDev } from './env';
import { createMainWindow, createIndexWindow } from './utils/window';
import { getAppInfo } from './utils';
import './ipc';

// Ẩn toàn bộ menu
Menu.setApplicationMenu(null);

let mainWindow: BrowserWindow | null = null;

function main() {
  mainWindow = createMainWindow();
  // Giải thích logic code: 【JHHB-13】Thông báo tin nhắn của ứng dụng desktop
  mainWindow.on('focus', () => {
    // Xóa nhấp nháy thanh tác vụ
    if (process.platform === 'win32') {
      mainWindow!.flashFrame(false);
    }
  });
  return mainWindow;
}

// Môi trường không phải dev, chỉ cho phép một phiên bản chạy
if (!isDev) {
  // Đã lấy được khóa single-instance hay chưa
  const gotTheLock = app.requestSingleInstanceLock();

  if (gotTheLock) {
    app.on('second-instance', () => {
      // Mở một cửa sổ mới
      createIndexWindow();
    });
  } else {
    // Không lấy được khóa single-instance thì thoát ứng dụng
    app.exit(0);
  }
}

// Quản lý vòng đời
app.whenReady().then(() => {
  // Lấy thông tin ứng dụng
  const $appInfo = getAppInfo();
  if ($appInfo?.productName && $appInfo?.appId) {
    app.setName($appInfo.productName);
    app.setAppUserModelId($appInfo.appId);
  }

  main();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      main();
    }
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});
