# Đóng gói ứng dụng desktop Electron

- 1. Cài đặt dependency rất chậm, mất khoảng 10 phút
- 2. Tài liệu đóng gói ứng dụng desktop electron
  https://help.jeecg.com/ui/setup/electron-build
- 3. Tạm thời comment tính năng electron
 Chú thích mã: build/vite/plugin/electron.ts
 Sửa build/vite/plugin/index.ts, tìm `electron plugin` rồi chú thích mã logic liên quan
 Sửa package.json để xóa các dependency liên quan

```yaml
{
  "main": "dist/electron/main.js",
  "scripts": {
    "electron:dev": "cross-env VITE_GLOB_RUN_PLATFORM=electron npm run dev",
    "electron:build-all": "npm run electron:build-web && npm run electron:build-app",
    "electron:build-web": "cross-env VITE_GLOB_RUN_PLATFORM=electron NODE_ENV=production NODE_OPTIONS=--max-old-space-size=8192 vite build --mode prod_electron && cross-env VITE_GLOB_RUN_PLATFORM=electron esno ./build/script/postBuild.ts && esno ./build/script/copyChat.ts",
    "electron:build-app": "esno ./electron/script/buildBefore.ts && electron-builder && esno ./electron/script/buildAfter.ts",
    "electron:install": "cross-env ELECTRON_MIRROR=https://npmmirror.com/mirrors/electron/ node node_modules/electron/install.js",
  },
  "devDependencies": {
    "electron": "35.1.4",
    "electron-builder": "^26.0.12",
    "vite-plugin-electron": "^0.29.0",
  },
}
```

> Lưu ý: Sau khi chạy `pnpm install`, nếu cài đặt Electron thất bại, có thể thử chạy `npm run electron:install` để cài đặt

# Ví dụ về thông báo desktop Electron và vị trí mã

1. Vị trí mã: electron/utils/tray.ts
2. Gọi hàm gửi thông báo hệ thống: sendDesktopNotice
3. Gọi hàm bắt đầu nhấp nháy biểu tượng khay hệ thống: startBlink
4. Gọi hàm dừng nhấp nháy biểu tượng khay hệ thống: stopBlink
