// noinspection JSUnusedGlobalSymbols

import { unref } from 'vue';
import { useWebSocket, WebSocketResult } from '@vueuse/core';
import { getToken } from '/@/utils/auth';

let result: WebSocketResult<any>;
const listeners = new Map();

/**
 * Bật WebSocket Liên kết, toàn cục chỉ cần thực thi một lần
 * @param url
 */
export function connectWebSocket(url: string) {
  // Giải thích logic mã: v2.4.6 của websocket Phía server, tồn tại vấn đề hiệu năng và bảo mật. #3278
  const token = (getToken() || '') as string;
  result = useWebSocket(url, {
    // Tự động kết nối lại (Gặp lỗi thì kết nối lại tối đa10lần)
    autoReconnect: {
      retries : 10,
      delay : 5000
    },
    // Kiểm tra heartbeat
    heartbeat: {
      message: "ping",
      interval: 55000
    },
    protocols: [token],
    // Giải thích logic mã: [issues/6662] Hệ thống demosocketTổng bị ngắt, đổi cách viết khác
    onConnected: function (ws) {
      console.log('[WebSocket] Kết nối thành công', ws);
    },
    onDisconnected: function (ws, event) {
      console.log('[WebSocket] Kết nối bị ngắt:', ws, event);
    },
    onError: function (ws, event) {
      console.log('[WebSocket] Kết nối gặp lỗi: ', ws, event);
    },
    onMessage: function (_ws, e) {
      console.debug('[WebSocket] -----Nhận tin nhắn-------', e.data);
      try {
        // Giải thích logic mã: 【issues/1161]FrontendwebsocketDo heartbeat khiến việc lắng nghe không có tác dụng---
        if (e.data === 'ping') {
          return;
        }
        const data = JSON.parse(e.data);
        for (const callback of listeners.keys()) {
          try {
            callback(data);
          } catch (err) {
            console.error(err);
          }
        }
      } catch (err) {
        console.error('[WebSocket] dataPhân tích thất bại:', err);
      }
    },
  });
  // if (result) {
  //   result.open = onOpen;
  //   result.close = onClose;

  //   const ws = unref(result.ws);
  //   if(ws!=null){
  //     ws.onerror = onError;
  //     ws.onmessage = onMessage;
  //     ws.onopen = onOpen;
  //     ws.onclose = onClose;
  //     
  //   }
  // }
}

function onOpen() {
  console.log('[WebSocket] Kết nối thành công');
}

function onClose(e) {
  console.log('[WebSocket] Kết nối bị ngắt:', e);
}

function onError(e) {
  console.log('[WebSocket] Kết nối gặp lỗi: ', e);
}

function onMessage(e) {
  console.debug('[WebSocket] -----Nhận tin nhắn-------', e.data);
  try {
    // Giải thích logic mã: 【issues/1161]FrontendwebsocketDo heartbeat khiến việc lắng nghe không có tác dụng---
    if(e==='ping'){
      return;
    }
    const data = JSON.parse(e.data);
    for (const callback of listeners.keys()) {
      try {
        callback(data);
      } catch (err) {
        console.error(err);
      }
    }
  } catch (err) {
    console.error('[WebSocket] dataPhân tích thất bại:', err);
  }
}


/**
 * Thêm WebSocket Lắng nghe tin nhắn
 * @param callback
 */
export function onWebSocket(callback: (data: object) => any) {
  if (!listeners.has(callback)) {
    if (typeof callback === 'function') {
      listeners.set(callback, null);
    } else {
      console.debug('[WebSocket] Thêm WebSocket Lắng nghe tin nhắn thất bại: tham số truyền vào không phải là một phương thức');
    }
  }
}

/**
 * Hủy WebSocket Lắng nghe tin nhắn
 *
 * @param callback
 */
export function offWebSocket(callback: (data: object) => any) {
  listeners.delete(callback);
}

export function useMyWebSocket() {
  return result;
}
