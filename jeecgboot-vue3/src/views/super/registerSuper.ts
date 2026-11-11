import type { App } from 'vue';

/**
 * Import động các component dưới super
 */
export async function registerSuper(app: App) {
  const modules = import.meta.glob('./**/register.ts');
  for (let [url, module] of Object.entries(modules)) {
    let { register } = await module();
    if (typeof register === 'function') {
      await register(app);
    } else {
      console.error(`${url} không export hàm register, không thể hoàn thành đăng ký!`);
    }
  }
}