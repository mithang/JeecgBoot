import {store} from '/@/store';
import {defineStore} from 'pinia';
import {defHttp} from "@/utils/http/axios";

interface DefIndexState {
  // URL trang chủ
  url: string,
  // Component trang chủ
  component: string
}

export const useDefIndexStore = defineStore('defIndex', {
  state: (): DefIndexState => ({
    url: '',
    component: '',
  }),
  getters: {},
  actions: {
    /**
     * Truy vấn cấu hình trang chủ mặc định
     */
    async query() {
      const config = await defIndexApi.query();
      this.url = config.url;
      this.component = config.component;
    },
    /**
     * Cập nhật cấu hình trang chủ mặc định
     * @param url URL trang chủ
     * @param component Component trang chủ
     * @param isRoute Có phải route không
     */
    async update(url: string, component: string, isRoute: boolean) {
      await defIndexApi.update(url, component, isRoute);
      await this.query()
    },

    check(url: string) {
      return url === this.url;
    }
  }
});

// Need to be used outside the setup
export function useDefIndexStoreWithOut() {
  return useDefIndexStore(store);
}

/**
 * API cấu hình trang chủ mặc định
 */
export const defIndexApi = {
  /**
   * Truy vấn cấu hình trang chủ mặc định
   */
  async query() {
    const url = '/sys/sysRoleIndex/queryDefIndex'
    return await defHttp.get({url});
  },
  /**
   * Cập nhật cấu hình trang chủ mặc định
   * @param url URL trang chủ
   * @param component Component trang chủ
   * @param isRoute Có phải route không
   */
  async update(url: string, component: string, isRoute: boolean) {
    let apiUrl = '/sys/sysRoleIndex/updateDefIndex'
    apiUrl += '?url=' + url
    // Giải thích logic mã: Sửa truyền tham số API trang chủ mặc định, thêm encodeURIComponent, tránh trường hợp {{ window._CONFIG['domianURL'] }}/** không lưu được
    apiUrl += '&component=' + encodeURIComponent(component)
    apiUrl += '&isRoute=' + isRoute
    return await defHttp.put({url: apiUrl});
  },

}
