import {qiankunWindow} from 'vite-plugin-qiankun/dist/helper'

/**
 * 【JEECGlàm sub app Qiankun][Phán đoán hiện tại có đang chạy ở chế độ sub app Qiankun không]
 */
export function checkIsQiankunMicro(): boolean {
  return !!qiankunWindow.__POWERED_BY_QIANKUN__;
}

export function getGlobal() {
  return (checkIsQiankunMicro() ? qiankunWindow : window) as Window
}
