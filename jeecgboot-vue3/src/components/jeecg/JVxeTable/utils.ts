import type { Ref, ComponentInternalInstance } from 'vue';
import { unref, isRef } from 'vue';
import { isFunction } from '/@/utils/is';

type dispatchEventOptions = {
  // JVxeTable của props
  props;
  // Đối tượng sự kiện event được kích hoạt
  $event;
  // Hàng, cột
  row?;
  column?;
  // JVxeTableInstance vue3 của JVxeTable
  instance?: ComponentInternalInstance | any;
  // cần tìmclassName
  className: string;
  // Viết lại phương thức xử lý sau khi tìm thấy dom
  handler?: Fn;
  // Có thực thi trực tiếp phương thức click thay vì mô phỏng sự kiện click hay không
  isClick?: boolean;
};

/** Mô phỏng kích hoạt sự kiện */
export function dispatchEvent(options: dispatchEventOptions) {
  const { props, $event, row, column, instance, className, handler, isClick } = options;
  if ((!$event || !$event.path) && !instance) {
    return;
  }
  // alwaysEdit không mô phỏng kích hoạt sự kiện, nếu không sẽ kích hoạt hai lần
  if (props && props.alwaysEdit) {
    return;
  }
  let getCell = () => {
    let paths: HTMLElement[] = [...($event?.path ?? [])];
    // Lấy đối tượng cell DOM thông qua instance
    if (row && column) {
      let selector = `table.vxe-table--body tbody tr[rowid='${row.id}'] td[colid='${column.id}']`;
      let cellDom = instance!.vnode?.el?.querySelector(selector);
      // -update-begin--author:liaozhiyang---date:20230830---for：【QQYUN-6390]Khắc phục cảnh báo khi thêm trường mới trong online (tương thích)
      if (!cellDom) {
        cellDom = instance!.$el?.querySelector(selector);
      }
      // -update-begin--author:liaozhiyang---date:20230830---for：【QQYUN-6390]Khắc phục cảnh báo khi thêm trường mới trong online (tương thích)
      if (cellDom) {
        paths.unshift(cellDom);
      }
    }
    for (const el of paths) {
      if (el.classList?.contains('vxe-body--column')) {
        return el;
      }
    }
    return null;
  };
  let cell = getCell();
  if (cell) {
    window.setTimeout(() => {
      let getElement = () => {
        let classList = className.split(' ');
        if (classList.length > 0) {
          const getClassName = (cls: string) => {
            if (cls.startsWith('.')) {
              return cls.substring(1, cls.length);
            }
            return cls;
          };
          let get = (target, className, idx = 0) => {
            let elements = target.getElementsByClassName(getClassName(className));
            if (elements && elements.length > 0) {
              return elements[idx];
            }
            return null;
          };
          let element: HTMLElement = get(cell, classList[0]);
          for (let i = 1; i < classList.length; i++) {
            if (!element) {
              break;
            }
            element = get(element, classList[i]);
          }
          return element;
        }
        return null;
      };
      let element = getElement();
      if (element) {
        if (isFunction(handler)) {
          handler(element);
        } else {
          // Mô phỏng kích hoạt sự kiện nhấp
          if (isClick) {
            element.click();
          } else {
            element.dispatchEvent($event);
          }
        }
      }
    }, 10);
  } else {
    console.warn('[JVxeTable]dispatchEvent lấy cell thất bại');
  }
}

/** Liên kết dữ liệu VxeTable */
export function vModel(value, row, column: Ref<any> | string) {
  // @ts-ignore
  let property = isRef(column) ? column.value.property : column;
  unref(row)[property] = value;
}

/**
 * liaozhiyang
 * 2024-06-20
 * Xác định việc chỉnh sửa hàng hiện tại có dùng cuộn ảo hay không (không phải cứ bật là dùng, còn phải thỏa mãn số lượng dữ liệu lớn hơn giá trị gt)
 */
export function isEnabledVirtualYScroll(props, xTable): boolean {
  let isRealEnabledVirtual = false;
  const isEnabledVScroll = props?.scrollY?.enabled;
  // 100Là giá trị mặc định của tầng dưới
  const gtYNum = props?.scrollY?.gt || 100;
  if (isEnabledVScroll) {
    const tableFullData = xTable.internalData.tableFullData;
    if (gtYNum === 0) {
      isRealEnabledVirtual = true;
    } else {
      if (tableFullData.length > gtYNum) {
        isRealEnabledVirtual = true;
      }
    }
  }
  return isRealEnabledVirtual;
}
