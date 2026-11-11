import { computed, nextTick, unref, watchEffect } from 'vue';
import { router } from '/@/router';
import { useRoute } from 'vue-router';
import { createLocalStorage } from '/@/utils/cache';
import { useTableContext } from './useTableContext';
import { useMessage } from '/@/hooks/web/useMessage';

/**
 * Cache cấu hình danh sách
 */
export function useColumnsCache(opt, setColumns, handleColumnFixed) {
  let isInit = false;
  const table = useTableContext();
  const $ls = createLocalStorage();
  const { createMessage: $message } = useMessage();
  const route = useRoute();
  // Cache cấu hình danh sáchkey
  const cacheKey = computed(() => {
    // Giải thích logic mã: 【QQYUN-8367]Việc lưu hiển thị cột cấu hình báo cáo online ảnh hưởng đến việc hiển thị/ẩn trường table của các trang khác (môi trường dev khi hot update sẽ có vấn đề này, môi trường production không có vấn đề)
    const path = route.path;
    let key = path.replace(/[\/\\]/g, '_');
    let cacheKey = table.getBindValues.value.tableSetting?.cacheKey;
    if (cacheKey) {
      key += ':' + cacheKey;
    }
    return 'columnCache:' + key;
  });

  watchEffect(() => {
    const columns = table.getColumns();
    if (columns.length) {
      init();
    }
  });

  async function init() {
    if (isInit) {
      return;
    }
    isInit = true;
    let columnCache = $ls.get(cacheKey.value);
    if (columnCache && columnCache.checkedList) {
      const { checkedList, sortedList, sortableOrder, checkIndex } = columnCache;
      await nextTick();
      // checkboxbộ nhớ đệm sắp xếp của
      opt.sortableOrder.value = sortableOrder;
      // checkboxbộ nhớ đệm đã chọn của
      opt.state.checkedList = checkedList;
      // tableColumnbộ nhớ đệm sắp xếp của
      opt.plainSortOptions.value.sort((prev, next) => {
        return sortedList.indexOf(prev.value) - sortedList.indexOf(next.value);
      });
      // Sắp xếp lạitableColumn
      checkedList.sort((prev, next) => sortedList.indexOf(prev) - sortedList.indexOf(next));
      // Có hiển thị cột số hàng không
      if (checkIndex) {
        table.setProps({ showIndexColumn: true });
      }
      setColumns(checkedList);
      // Đặt cột cố định
      setColumnFixed(columnCache);
    }
  }

  /** Đặt các cột đã cố định */
  async function setColumnFixed(columnCache) {
    const { fixedColumns } = columnCache;
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      let fixedCol = fixedColumns.find((fc) => fc.key === (column.key || column.dataIndex));
      if (fixedCol) {
        await nextTick();
        handleColumnFixed(column, fixedCol.fixed);
      }
    }
  }

  // Xác định trạng thái cố định của cột
  const fixedReg = /^(true|left|right)$/;

  /** Lấy các cột đã cố định */
  function getFixedColumns() {
    let fixedColumns: any[] = [];
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      if (fixedReg.test((column.fixed ?? '').toString())) {
        fixedColumns.push({
          key: column.key || column.dataIndex,
          fixed: column.fixed === true ? 'left' : column.fixed,
        });
      }
    }
    return fixedColumns;
  }

  /** Lưu cấu hình cột */
  function saveSetting() {
    const { checkedList } = opt.state;
    // Giải thích logic mã: 【TV360X-105] vấn đề cài đặt hiển thị cột[Sau khi đặt lại, thứ tự đã lưu vẫn là thứ tự lần trước]
    let sortedList = [];
    if (opt.restAfterOptions.value) {
      sortedList = opt.restAfterOptions.value.map((item) => item.value);
    } else {
      sortedList = unref(opt.plainSortOptions).map((item) => item.value);
    }
    $ls.set(cacheKey.value, {
      // Các cột đã lưu
      checkedList,
      // Cột sau khi sắp xếp
      sortedList,
      // Có hiển thị cột số hàng không
      checkIndex: unref(opt.checkIndex),
      // checkboxThứ tự gốc
      sortableOrder: unref(opt.sortableOrder),
      // Cột cố định
      fixedColumns: getFixedColumns(),
    });
    $message.success('Lưu thành công');
    // Sau khi lưu sẽ đóng trực tiếp
    opt.popoverVisible.value = false;
  }

  /** Đặt lại (xóa) cấu hình cột */
  async function resetSetting() {
    // Đặt lại cột cố định
    await resetFixedColumn();
    $ls.remove(cacheKey.value);
    $message.success('Đặt lại thành công');
  }

  async function resetFixedColumn() {
    const columns = opt.plainOptions.value;
    for (const column of columns) {
      column.fixed;
      if (fixedReg.test((column.fixed ?? '').toString())) {
        await nextTick();
        handleColumnFixed(column, null);
      }
    }
  }

  return {
    saveSetting,
    resetSetting,
    getCache: () => $ls.get(cacheKey.value),
  };
}
