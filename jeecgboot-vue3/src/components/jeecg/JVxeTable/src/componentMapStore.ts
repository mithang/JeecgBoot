import type { JVxeVueComponent } from './types';
import { JVxeTypes } from './types/JVxeTypes';

/** Chỉ lưu componentMap và clearComponent, dùng cho các tình huống qiankun unmount, không tham chiếu bất kỳ component Cell nào */
let componentMap = new Map<JVxeTypes | string, JVxeVueComponent>();
const JVxeComponents = 'JVxeComponents__';
if (import.meta.env.DEV && componentMap.size === 0 && window[JVxeComponents] && window[JVxeComponents].size > 0) {
  componentMap = window[JVxeComponents];
}

export { componentMap };

/**
 * Xóa các component đã đăng ký (gọi khi ứng dụng con Qiankun unmount, chỉ tham chiếu file này để tránh tải tất cả Cell)
 */
export function clearComponent() {
  componentMap.clear();
  // Giải thích logic mã: 【issues/860] Mã một-nhiều được tạo, sau khi cập nhật nóng nhấn thêm mới bị kẹt[Tạm thời xử lý trước]
  import.meta.env.DEV && (window[JVxeComponents] = componentMap);
}
