export type DynamicViewsRecord = Record<string, () => Promise<Recordable>>;

/** Trang động của module đã đăng ký */
export const packageViews: DynamicViewsRecord = {};

/**
 * Đăng ký trang route động
 * @param getViews Phương thức lấy tất cả trang dưới module này
 */
export function registerDynamicRouter(getViews: () => DynamicViewsRecord) {
  if (typeof getViews === 'function') {
    let dynamicViews = getViews();
    Object.keys(dynamicViews).forEach((key) => {
      // Xử lý trang độngkey, để route có thể nhận biết
      let newKey = key.replace('./src/views', '../../views');
      packageViews[newKey] = dynamicViews[key];
    });
  }
}
