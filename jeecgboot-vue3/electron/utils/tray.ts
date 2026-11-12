// tray = khay hệ thống
import path from 'path';
import { Tray, Menu, app, dialog, nativeImage, BrowserWindow, Notification, ipcMain } from 'electron';
import type { IpcMainInvokeEvent } from 'electron';
import {_PATHS} from '../paths';
import {$env, isDev} from '../env';

const TrayIcons = {
  // Giải thích logic code: 【JHHB-13】Thông báo tin nhắn của ứng dụng desktop
  normal: nativeImage.createFromPath(
    process.platform === 'win32'
      ? path.join(_PATHS.publicRoot, 'logo.png')
      : path.join(_PATHS.electronRoot, './icons/mac/tray-icon.png').replace(/[\\/]dist[\\/]/, '/')
  ),
  empty: nativeImage.createEmpty(),
};

// Tạo biểu tượng khay hệ thống
export function createTray(win: BrowserWindow) {
  const tray = new Tray(TrayIcons.normal);

  const TrayUtils = useTray(tray, win);

  tray.setToolTip($env.VITE_GLOB_APP_TITLE! + (isDev ? ' (Môi trường phát triển)' : ''));

  // Nhấn chuột trái vào biểu tượng khay để hiển thị cửa sổ chính
  tray.on('click', () => TrayUtils.showMainWindow());
  // Nhấn chuột phải vào biểu tượng khay để hiển thị menu khay
  tray.on('right-click', () => showTrayContextMenu());

  function showTrayContextMenu() {
    const trayContextMenu = getTrayMenus(win, TrayUtils);
    // Bật menu khay; không dùng phương thức setContextMenu vì cần cập nhật nội dung menu theo thời gian thực
    tray.popUpContextMenu(trayContextMenu);
  }
}

export function useTray(tray: Tray, win: BrowserWindow) {
  let isBlinking = false;
  let blinkTimer: NodeJS.Timeout | null = null;

  function showMainWindow() {
    win.show();
  }

  // Bắt đầu nhấp nháy
  function startBlink() {
    isBlinking = true;
    tray.setImage(TrayIcons.empty);
    blinkTimer = setTimeout(() => {
      tray.setImage(TrayIcons.normal);
      setTimeout(() => {
        if (isBlinking) {
          startBlink();
        }
      }, 500);
    }, 500);
  }

  // Kết thúc nhấp nháy
  function stopBlink() {
    isBlinking = false;
    if (blinkTimer) {
      clearTimeout(blinkTimer);
      blinkTimer = null;
    }
    tray.setImage(TrayIcons.normal);
  }
  ipcMain.on('tray-flash', (event: IpcMainInvokeEvent) => {
    // Chỉ nhấp nháy trên hệ thống Windows
    if (process.platform === 'win32') {
      startBlink();
    }
  });
  ipcMain.on('tray-flash-stop', (event: IpcMainInvokeEvent) => {
    // Chỉ dừng nhấp nháy trên hệ thống Windows
    if (process.platform === 'win32') {
      stopBlink();
    }
  });
  win.on('focus', () => {
    stopBlink();
  });
  // Gửi thông báo desktop
  function sendDesktopNotice() {
    // Kiểm tra có hỗ trợ thông báo desktop không
    if (!Notification.isSupported()) {
      // todo Trong quá trình phát triển thực tế không cần nhắc, trả về trực tiếp hoặc đổi sang cách nhắc khác
      dialog.showMessageBoxSync(win, {
        type: 'error',
        title: 'Lỗi',
        message: 'Hệ thống hiện tại không hỗ trợ thông báo desktop',
      });
      return;
    }
    const ins = new Notification({
      title: 'Tiêu đề thông báo',
      body: 'Dòng đầu tiên của nội dung thông báo\nDòng thứ hai của nội dung thông báo',
      // icon: TrayIcons.normal.resize({width: 32, height: 32}),
    });

    ins.on('click', () => {
      dialog.showMessageBoxSync(win, {
        type: 'info',
        title: 'Nhắc nhở',
        message: 'Đã nhấn vào thông báo',
      });
    });

    ins.show();
  }

  return {
    showMainWindow,

    startBlink,
    stopBlink,
    isBlinking: () => isBlinking,

    sendDesktopNotice,
  };
}

const MenuIcon = {
  exit: nativeImage
    .createFromDataURL(
      'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAA7EAAAOxAGVKw4bAAACJ0lEQVR4nH1TzWvUQBRP7fpxsWqVXsSLiAevRWhhN28msRJo981kay4WRBCF/QdEFJpbaUHw4kFBQTwUKX4gKh48KPiBBcGLJ1F0uzPZ7ibWXf0DIjObielS+mDIm/fxm9/85sWyBixN06E0CIaV3wB2XhC8puOWNZSG4Y7B+k2mi7Kl9l2n9rHnzvbWJoLRYn7r5jTViQjwzM8ynlC+AFyVgN2NU8G+Rnn6QETx3FfP223A/jeHfWqCsAUJ7Hlryh9Te0nYqiDsz9rE6VHVIABvNwEf/ADYk4OsZPeVFbwiCHtcZBVR9k4CJhJmDuUxwEVJ8H4fINOkC9Vjbeq/UTR1IgPturX3f93Z35+B7ddxgJL6dih/skF9zE9KCJ//5bDLpii1+npIuzolKTubC5gBxzarJo6vWWjrUP+etFlF+ds9lRFOXalN+NPEmxvRDS3KH34v8+PFIgNmTh0EahH+InGCwzoQEbYcuTMnlR8aYbaxGHFvRNiznssP6sA65UsxrdU1+hYnFhlpAGAkdvzlPLFu88mY8pcrVjCsxcqGapC2eYW249/tUH4xS4QaVQLeigi/YWJqPl4DlNRSrAwzSaoXIspeWUYrI9qXINglgT1qAt5JPG+kkNN5BSAJuyoJfhAVdmST4PlPBFASNs6rIgnspqC8HlF+SQAuRQTfKpYiEy6fwuIdP42P71T+t0l/TBKcE8AXm4DXBfB6w50+apgUhf4HZ5j+Z5+zNTAAAAAASUVORK5CYII='
    )
    .resize({
      width: 16,
      height: 16,
    }),
};

// Thiết lập menu khay hệ thống
function getTrayMenus(win: BrowserWindow, TrayUtils: ReturnType<typeof useTray>) {
  const {startBlink, stopBlink, sendDesktopNotice} = TrayUtils;
  const isBlinking = TrayUtils.isBlinking();

  return Menu.buildFromTemplate([
    ...(isDev
      ? [
        {
          label: 'Công cụ phát triển',
          submenu: [
            {
              label: 'Các menu dưới đây chỉ hiển thị trong môi trường phát triển',
              sublabel: 'Hiện đang ở môi trường phát triển',
              enabled: false,
            },
            {type: 'separator'},
            {
              label: 'Bật/tắt DevTools',
              click: () => win.webContents.toggleDevTools(),
            },
            {
              label: `Biểu tượng khay hệ thống ${isBlinking ? 'dừng' : 'bắt đầu'} nhấp nháy`,
              sublabel: 'Mô phỏng nhắc tin nhắn mới',
              click: () => (isBlinking ? stopBlink() : startBlink()),
            },
            {
              label: 'Ví dụ gửi thông báo desktop',
              click: () => sendDesktopNotice(),
            },
          ],
        },
        {type: 'separator'},
      ]
      : ([] as any)),
    {
      label: 'Hiển thị cửa sổ chính',
      // Biểu tượng tệp
      icon: TrayIcons.normal.resize({width: 16, height: 16}),
      click: () => win.show(),
    },
    {type: 'separator'},
    {
      label: 'Thoát',
      // Biểu tượng base64
      icon: MenuIcon.exit,
      click: () => {
        // Bật hộp thoại hỏi có xác nhận thoát không
        const choice = dialog.showMessageBoxSync(win, {
          type: 'question',
          title: 'Nhắc nhở',
          message: 'Bạn có chắc chắn muốn thoát ứng dụng không?',
          buttons: ['Thoát', 'Hủy'],
          defaultId: 1,
          cancelId: 1,
          noLink: true,
        });
        // Người dùng đã chọn thoát, exit trực tiếp
        if (choice === 0) {
          // global.isQuitting = true;
          app.exit(0);
        }
      },
    },
  ]);
}
