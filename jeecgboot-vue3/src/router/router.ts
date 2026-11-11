/*
 * Tệp lưu trữ instance của route, đừng tùy tiện thêm mã khác để tránh phát sinh vấn đề HMR hoặc các vấn đề khác
 */
import type {Router, RouterHistory} from 'vue-router';
import {createRouter as createVueRouter, createWebHistory, createWebHashHistory, RouterOptions} from 'vue-router';

export let router: Router = null as unknown as Router;

export function setRouter(r: Router) {
  router = r
}

let webHistory: Nullable<RouterHistory> = null;

/**
 * Tạo route
 * @param options Tham số
 * @param useHashHistory Có dùng hash route không, true là dùng, false là không dùng hash route
 */
export function createRouter(options: Partial<RouterOptions>, useHashHistory = false) {
  const createFn = useHashHistory ? createWebHashHistory : createWebHistory;
  webHistory = createFn(import.meta.env.VITE_PUBLIC_PATH);
  // app router
  let router = createVueRouter({
    history: webHistory,
    routes: [],
    ...options,
  });

  setRouter(router)

  return router
}

// Hủy route
export function destroyRouter() {
  setRouter(null as unknown as Router);
  if (webHistory) {
    webHistory.destroy();
  }
  webHistory = null
}
