import { contextBridge, ipcRenderer } from 'electron';
import { ElectronEnum } from '../../src/enums/jeecgEnum';

contextBridge.exposeInMainWorld(ElectronEnum.ELECTRON_API, {
  openInBrowser: (url: string) => ipcRenderer.send('open-in-browser', url),
  // Gửi thông báo tin nhắn
  sendNotification: (title: string, body: string, path: string) => {
    ipcRenderer.send('notify-with-path', { title, body, path });
  },
  // Gắn chuyển hướng route
  onNavigate: (cb: (path: string) => void) => {
    ipcRenderer.on('navigate-to', (_, path) => cb(path));
  },
  // Nhấp nháy thanh tác vụ
  sendNotifyFlash: () => ipcRenderer.send('notify-flash'),
  // Nhấp nháy khay hệ thống
  trayFlash: () => ipcRenderer.send('tray-flash'),
  // Dừng nhấp nháy khay hệ thống
  trayFlashStop: () => ipcRenderer.send('tray-flash-stop'),
});
