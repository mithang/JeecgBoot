import type { App } from 'vue';
// Import vxe-table
import 'xe-utils';
import VxeUIAll from 'vxe-pc-ui';
import VXETable /*Grid*/ from 'vxe-table';
import VXETablePluginAntd from 'vxe-table-plugin-antd';
import 'vxe-pc-ui/lib/style.css';
import 'vxe-table/lib/style.css';

import { getEventPath } from '/@/utils/common/compUtils';
import { registerAllComponent } from './utils/registerUtils';
import { getEnhanced } from './utils/enhancedUtils';
import type { JVxeTypes } from './types/JVxeTypes';
export interface RegisterJVxeTableOptions {
  /** Chỉ đăng ký các loại tích hợp sẵn được chỉ định; nếu không truyền thì đăng ký tất cả component tích hợp sẵn */
  builtinComponents?: JVxeTypes[];
}

export async function registerJVxeTable(app: App) {
  // VXETable Cấu hình toàn cục
  const VXETableSettings = {
    // z-index Giá trị bắt đầu
    zIndex: 1000,
    table: {},
  };

  // Thêm trình chặn sự kiện event.clearActived
  // Ví dụ sau khi click vào bảng nổi (panel) của một component nào đó, lúc này ô đang được kích hoạt không nên tự động đóng lại, trả về false có thể ngăn hành vi mặc định.
  VXETable.interceptor.add('event.clearActived', preventClosingPopUp);
  VXETable.interceptor.add('event.clearEdit', preventClosingPopUp);
  // Đăng ký plugin
  VXETable.use(VXETablePluginAntd);
  // Đăng ký component tùy chỉnh
  registerAllComponent();
  // Thực thi phương thức đăng ký
  app.use(VxeUIAll);
  app.use(VXETable, VXETableSettings);
}


/**
 * Ngăn đóng popup khi đang chỉnh sửa dòng
 * @param params
 */
function preventClosingPopUp(this: any, params) {
  // Lấy tăng cường component
  let col = params.column.params;
  // Giải thích logic mã: 【issues/8178] sử dụng gốcvxe-tableComponent ở chế độ chỉnh sửa bị mất focus sẽ báo lỗi
  if (col === undefined) {
    // Cho biết đang sử dụng thuần gốcvxe-table
    return;
  }
  let { $event } = params;
  const interceptor = getEnhanced(col.type).interceptor;
  // Thực thi tăng cường
  let flag = interceptor['event.clearActived']?.call(this, ...arguments);
  if (flag === false) {
    return false;
  }

  let path = getEventPath($event);
  for (let p of path) {
    let className: any = p.className || '';
    className = typeof className === 'string' ? className : className.toString();

    /* --- Xử lý đặc biệt các component sau, khi nhấn các nhãn sau sẽ không xóa trạng thái chỉnh sửa --- */

    // Thẻ được nhấp làJInputPop
    if (className.includes('j-input-pop')) {
      return false;
    }
    // Thẻ được nhấp là lớp bật lên của JPopup, chọn phòng ban, chọn người dùng
    if (
      className.includes('j-popup-modal') ||
      className.includes('j-depart-select-modal') ||
      className.includes('j-user-select-modal') ||
      className.includes('jeecg-online-pop-list-modal') ||
      className.includes('jeecg-online-pop-modal') ||
      className.includes('jeecg-all-table-pop-list-modal')
    ) {
      return false;
    }
    // Cái được nhấp là bộ chọn ngày
    if (className.includes('j-vxe-date-picker')) {
      return false;
    }
    // Thực thi tăng cường
    let flag = interceptor['event.clearActived.className']?.call(this, className, ...arguments);
    if (flag === false) {
      return false;
    }
  }
}
