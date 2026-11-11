import type { BasicColumn } from '/@/components/Table';
import type { Ref, ComputedRef } from 'vue';
import type { BasicTableProps, PaginationProps, TableRowSelection } from '/@/components/Table';
import { computed, nextTick, onUnmounted, ref, toRaw, unref, watch, watchEffect } from 'vue';
import { omit, isEqual } from 'lodash-es';
import { throttle } from 'lodash-es';
import { Checkbox, Radio } from 'ant-design-vue';
import { isFunction } from '/@/utils/is';
import { findNodeAll } from '/@/utils/helper/treeHelper';
import { ROW_KEY } from '/@/components/Table/src/const';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useMessage } from '/@/hooks/web/useMessage';
import { ModalFunc } from 'ant-design-vue/lib/modal/Modal';

// Cột chọn tùy chỉnhkey
export const CUS_SEL_COLUMN_KEY = 'j-custom-selected-column';

/**
 * Cột chọn tùy chỉnh
 */
export function useCustomSelection(
  propsRef: ComputedRef<BasicTableProps>,
  emit: EmitType,
  wrapRef: Ref<null | HTMLDivElement>,
  getPaginationRef: ComputedRef<boolean | PaginationProps>,
  tableData: Ref<Recordable[]>,
  childrenColumnName: ComputedRef<string>
) {
  const { createConfirm } = useMessage();
  // Phần tử body của bảng
  const bodyEl = ref<HTMLDivElement>();
  // bodyChiều cao phần tử
  const bodyHeight = ref<number>(0);
  // Chiều cao tr của bảng
  const rowHeight = ref<number>(0);
  // body Chiều cao cuộn
  const scrollTop = ref(0);
  // đã chọnkey
  const selectedKeys = ref<string[]>([]);
  // Hàng đã chọn
  const selectedRows = ref<Recordable[]>([]);
  // Các hàng đã thay đổi
  let changeRows: Recordable[] = [];
  let allSelected: boolean = false;

  let timer;

  // Làm phẳng dữ liệu, dữ liệu children cũng sẽ được đặt cùng nhau
  const flattedData = computed(() => {
    // Giải thích logic mã: 【QQYUN-6774] Giải quyết vấn đề sau khi vô hiệu hóa checkbox thì chọn tất cả vẫn có thể tích chọn
    const data = flattenData(tableData.value, childrenColumnName.value);
    const rowSelection = propsRef.value.rowSelection;
    if (rowSelection?.type === 'checkbox' && rowSelection.getCheckboxProps) {
      for (let i = 0, len = data.length; i < len; i++) {
        const record = data[i];
        const result = rowSelection.getCheckboxProps(record);
        if (result.disabled) {
          data.splice(i, 1);
          i--;
          len--;
        }
      }
    }
    return data;
  });

  const getRowSelectionRef = computed((): TableRowSelection | null => {
    const { rowSelection } = unref(propsRef);
    if (!rowSelection) {
      return null;
    }

    return {
      preserveSelectedRowKeys: true,
      // selectedRowKeys: unref(selectedKeys),
      // onChange: (selectedRowKeys: string[]) => {
      //   setSelectedRowKeys(selectedRowKeys);
      // },
      ...omit(rowSelection, ['onChange', 'selectedRowKeys']),
    };
  });

  // Có phải chọn đơn không
  const isRadio = computed(() => {
    return getRowSelectionRef.value?.type === 'radio';
  });

  const getAutoCreateKey = computed(() => {
    return unref(propsRef).autoCreateKey && !unref(propsRef).rowKey;
  });

  // Trường key của cột
  const getRowKey = computed(() => {
    const { rowKey } = unref(propsRef);
    return unref(getAutoCreateKey) ? ROW_KEY : rowKey;
  });
  // Lấy dữ liệu trường key của hàng
  const getRecordKey = (record) => {
    if (!getRowKey.value) {
      return record[ROW_KEY];
    } else if (isFunction(getRowKey.value)) {
      return getRowKey.value(record);
    } else {
      return record[getRowKey.value];
    }
  };

  // Cấu hình phân trang
  const getPagination = computed<PaginationProps>(() => {
    return typeof getPaginationRef.value === 'boolean' ? {} : getPaginationRef.value;
  });
  // Số lượng mục của trang hiện tại
  const currentPageSize = computed(() => {
    const { pageSize = 10, total = flattedData.value.length } = getPagination.value;
    return pageSize > total ? total : pageSize;
  });

  // Tiêu đề danh sách lựa chọnprops
  const selectHeaderProps = computed(() => {
    return {
      onSelectAll,
      isRadio: isRadio.value,
      selectedLength: flattedData.value.filter((data) => selectedKeys.value.includes(getRecordKey(data))).length,
      // 【TV360X-53] Khi trống sẽ báo lỗi, tăng cường kiểm tra
      pageSize: flattedData.value?.length ?? 0,
      // 【QQYUN-6774] Giải quyết vấn đề sau khi vô hiệu hóa checkbox thì chọn tất cả vẫn có thể tích chọn
      disabled: flattedData.value.length == 0,
      hideSelectAll: unref(propsRef)?.rowSelection?.hideSelectAll,
    };
  });

  // Lắng nghe dữ liệu được truyền vàoselectedRowKeys
  // Giải thích logic mã: 【QQYUN-8390] Component nhân sự phòng ban bấm đặt lại chưa xóa (selectedRowKeys.value=[], watch không lắng nghe được thì thêm deep)
  watch(
    () => unref(propsRef)?.rowSelection?.selectedRowKeys,
    (val: string[]) => {
      // Khắc phục selectedRowKeys bị mất hiệu lực khi dùng ref tại nơi gọi trong trang
      const value = unref(val);
      if (Array.isArray(value) && !sameArray(value, selectedKeys.value)) {
        // Trì hoãn là để đợiwatch selectedRows
        setTimeout(() => {
          setSelectedRowKeys(value);
        }, 0);
      }
    },
    {
      immediate: true,
      deep: true
    }
  );
  // Khi chỉnh sửa, selectedRows có thể được điền lại
  watch(
    () => unref(propsRef)?.rowSelection?.selectedRows,
    (val: string[]) => {
      const value: any = unref(val);
      if (Array.isArray(value) && !sameArray(value, selectedRows.value)) {
        selectedRows.value = value;
      }
    },
    {
      immediate: true,
      deep: true,
    }
  );
  /**
  * 2024-03-06
  * liaozhiyang
  * Kiểm tra có phải cùng một mảng không (Chỉ khi địa chỉ tham chiếu, độ dài và thông tin vị trí phần tử giống nhau mới là cùng một mảng. Các phần tử của mảng chỉ là chuỗi)
  */
  function sameArray(a, b) {
    if (a === b) {
      if (a.length === b.length) {
        return a.toString() === b.toString();
      } else {
        return false;
      }
    } else {
      // Giải thích logic mã: 【QQYUN-9123]popupdict mở popup, mở chương trình chạy
      if (isEqual(a, b)) {
        return true;
      }
      return false;
    }
  }

  // Khi bất kỳ giá trị nào thay đổi, kích hoạt kiểm tra đồng bộ
  watch([selectedKeys, selectedRows], () => {
    nextTick(() => {
      syncSelectedRows();
    });
  });

  // Lắng nghe sự kiện thanh cuộn
  const onScrollTopChange = throttle((e) => (scrollTop.value = e?.target?.scrollTop), 150);

  let bodyResizeObserver: Nullable<ResizeObserver> = null;
  // Lấy chiều cao hàng đầu tiên
  watchEffect(() => {
    // Cách viết này là để lắng nghe sự thay đổi của size
    propsRef.value.size && void 0;
    if (bodyEl.value) {
      // Lắng nghe thay đổi chiều cao của div
      bodyResizeObserver = new ResizeObserver((entries) => {
        for (let entry of entries) {
          if (entry.target === bodyEl.value && entry.contentRect) {
            const { height } = entry.contentRect;
            bodyHeight.value = Math.ceil(height);
          }
        }
        updateRowHeight();
      });
      bodyResizeObserver.observe(bodyEl.value);
    }
    rowHeight.value = 50;
  });

  onMountedOrActivated(async () => {
    bodyEl.value = await getTableBody(wrapRef.value!);
    bodyEl.value.addEventListener('scroll', onScrollTopChange);
  });
  onUnmounted(() => {
    if (bodyEl.value) {
      bodyEl.value?.removeEventListener('scroll', onScrollTopChange);
    }
    if (bodyResizeObserver != null) {
      bodyResizeObserver.disconnect();
    }
  });

  // Cập nhật chiều cao hàng đầu tiên
  function updateRowHeight() {
    const el = bodyEl.value?.querySelector('tbody.ant-table-tbody tr.ant-table-row') as HTMLDivElement;
    if (el) {
      // Giải thích logic mã: 【issues/7442]basicTable khi chuyển từ mặc định sang rộng rãi/thu gọn thì ô chọn nhiều hiển thị bất thường
      nextTick(() => rowHeight.value = el.offsetHeight);
    }
  }

  // Chọn tất cả
  function onSelectAll(checked: boolean, flag = 'currentPage') {
    // Giải thích logic mã: 【issues/5577]Khi component BasicTable chọn tất cả và hủy chọn tất cả thì không kích hoạt sự kiện onSelectAll
    if (unref(propsRef)?.rowSelection?.onSelectAll) {
      allSelected = checked;
      changeRows = getInvertRows(selectedRows.value, checked, flag);
    }
    // Bỏ chọn tất cả
    if (!checked) {
      // Giải thích logic mã: 【issues/6958]Hủy không kích hoạt sự kiện onSelectAll, sau khi chọn trên nhiều trang thì changeRows rỗng
      if (flag === 'allPage') {
        selectedKeys.value = [];
        selectedRows.value = [];
      } else {
        flattedData.value.forEach((item) => {
          updateSelected(item, false);
        });
      }
      emitChange('all');
      return;
    }
    let modal: Nullable<ReturnType<ModalFunc>> = null;
    // Chọn tất cả
    const checkAll = () => {
      if (modal != null) {
        modal.update({
          content: 'Đang chọn tất cả theo từng đợt, vui lòng chờ…',
          cancelButtonProps: { disabled: true },
        });
      }
      let showCount = 0;
      // Số lượng chọn tối thiểu
      let minSelect = 100;
      const hidden: Recordable[] = [];
      flattedData.value.forEach((item, index, array) => {
        if (array.length > 120) {
          if (showCount <= minSelect && recordIsShow(index, Math.max((minSelect - 10) / 2, 3))) {
            showCount++;
            updateSelected(item, checked);
          } else {
            hidden.push(item);
          }
        } else {
          updateSelected(item, checked);
        }
      });
      if (hidden.length > 0) {
        return batchesSelectAll(hidden, checked, minSelect);
      } else {
        emitChange('all');
      }
    };

    // Khi lượng dữ liệu lớn hơn 120 dòng, chọn tất cả sẽ khiến trang bị lag, cần thực hiện chọn tất cả chậm
    if (flattedData.value.length > 120) {
      modal = createConfirm({
        title: 'Chọn tất cả',
        content: 'Lượng dữ liệu hiện tại khá lớn, chọn tất cả có thể khiến trang bị lag, bạn có chắc chắn muốn thực hiện thao tác này không?',
        iconType: 'warning',
        onOk: () => checkAll(),
      });
    } else {
      checkAll();
    }
  }

  // Chọn tất cả theo lô
  function batchesSelectAll(hidden: Recordable[], checked: boolean, minSelect: number) {
    return new Promise<void>((resolve) => {
      (function call() {
        // Cứ mỗi nửa giây, chọn 100 bản ghi dữ liệu
        setTimeout(() => {
          const list = hidden.splice(0, minSelect);
          if (list.length > 0) {
            list.forEach((item) => {
              updateSelected(item, checked);
            });
            call();
          } else {
            setTimeout(() => {
              emitChange('all');
              // Giải thích logic mã: 【QQYUN-5687] Chọn hàng loạt, sau khi nhắc thành công lại xuất hiện thêm một nhắc nhở
              setTimeout(() =>resolve(), 0);
            }, 500);
          }
        }, 300);
      })();
    });
  }

  // Chọn một mục
  function onSelect(record, checked) {
    onSelectChild(record, checked);
    updateSelected(record, checked);
    onSelectParent(record, checked);
    // Giải thích logic mã: 【issues/8690]rowSelection của BasicTable thêm mới phương thức onSelect
    propsRef.value.rowSelection?.onSelect?.(toRaw(record), checked, toRaw(selectedRows.value));
    emitChange();
  }

  function updateSelected(record, checked) {
    const recordKey = getRecordKey(record);
    if (isRadio.value) {
      selectedKeys.value = [recordKey];
      selectedRows.value = [record];
      return;
    }
    const index = selectedKeys.value.findIndex((key) => key === recordKey);
    if (checked) {
      if (index === -1) {
        selectedKeys.value.push(recordKey);
        selectedRows.value.push(record);
      }
    } else {
      if (index !== -1) {
        selectedKeys.value.splice(index, 1);
        selectedRows.value.splice(index, 1);
      }
    }
    // Giải thích logic mã: 【issues/7200]basicTable sau khi chọn không có kiểu đã chọn
    clearTimeout(timer);
    timer = setTimeout(() => {
      selectedKeys.value = [...selectedKeys.value];
    }, 0);
  }

  // Gọi sự kiện onChange do người dùng tự định nghĩa
  function emitChange(mode = 'single') {
    const { rowSelection } = unref(propsRef);
    if (rowSelection) {
      const { onChange } = rowSelection;
      if (onChange && isFunction(onChange)) {
        setTimeout(() => {
          onChange(selectedKeys.value, selectedRows.value);
        }, 0);
      }
    }
    emit('selection-change', {
      keys: getSelectRowKeys(),
      rows: getSelectRows(),
    });
    // Giải thích logic mã: 【issues/5577]Khi component BasicTable chọn tất cả và hủy chọn tất cả thì không kích hoạt sự kiện onSelectAll
    if (mode == 'all') {
      const rowSelection = unref(propsRef)?.rowSelection;
      if (rowSelection?.onSelectAll) {
        rowSelection.onSelectAll(allSelected, toRaw(getSelectRows()), toRaw(changeRows));
      }
    }
  }
  /**
   * Khi liên kết theo cấp bậc, chọn dữ liệu cấp dưới
   * @param record
   * @param checked
   */
  function onSelectChild(record, checked) {
    if (unref(propsRef)?.isTreeTable && unref(propsRef)?.rowSelection?.checkStrictly === false && !isRadio.value) {
      if (record[childrenColumnName.value] && record[childrenColumnName.value].length > 0) {
        record[childrenColumnName.value].forEach((children) => {
          updateSelected(children, checked);
          if (children[childrenColumnName.value] && children[childrenColumnName.value].length > 0) {
            onSelectChild(children, checked);
          }
        });
      }
    }
  }
  /**
   * 2024-09-24
   * liaozhiyang
   * Khi liên kết theo cấp bậc, chọn dữ liệu cấp trên
   * 【issues/7217]Đặt checkStrictly cho bảng cây của BasicTable không có hiệu lực
   * */
  function onSelectParent(record, checked) {
    if (unref(propsRef)?.isTreeTable && unref(propsRef)?.rowSelection?.checkStrictly === false && !isRadio.value) {
      let condition = true,
        currentRecord = record;
      while (condition) {
        const parentRecord: any = findParent(tableData.value, currentRecord, childrenColumnName.value);
        if (parentRecord) {
          const childrenRecordKeys: any = [];
          parentRecord[childrenColumnName.value].forEach((item) => {
            childrenRecordKeys.push(getRecordKey(item));
          });
          if (checked === true) {
            const isSubSet = childrenRecordKeys.every((item) => selectedKeys.value.includes(item));
            isSubSet && updateSelected(parentRecord, checked);
          } else if (checked === false) {
            updateSelected(parentRecord, checked);
          }
          if (tableData.value.find((item) => getRecordKey(item) === getRecordKey(parentRecord))) {
            // Kết thúc vòng lặp
            condition = false;
          } else {
            currentRecord = parentRecord;
          }
        } else {
          // Kết thúc vòng lặp
          condition = false;
        }
      }
    }
    function findParent(tree, record, children = 'children') {
      let parent = null;
      function search(nodes) {
        for (let node of nodes) {
          if (node[children]?.some((child) => getRecordKey(child) === getRecordKey(record))) {
            parent = node;
            return true;
          }
          if (node[children] && search(node[children])) {
            return true;
          }
        }
        return false;
      }
      search(tree);
      return parent;
    }
  }
  // Dùng để kiểm tra có phải cột chọn tùy chỉnh hay không
  function isCustomSelection(column: BasicColumn) {
    return column.key === CUS_SEL_COLUMN_KEY;
  }

  /**
   * Kiểm tra dòng hiện tại có hiển thị không, dùng cho cuộn ảo
   * @param index Chỉ số hàng
   * @param threshold Ngưỡng trước sau, mặc định hiển thị 3 dòng trước và sau vùng hiển thị
   */
  function recordIsShow(index: number, threshold = 3) {
    // Chỉ khi lượng dữ liệu lớn hơn 50 bản ghi mới thực hiện cuộn ảo
    const isVirtual = flattedData.value.length > 50;
    if (isVirtual) {
      // Dựa vào scrollTop, bodyHeight, rowHeight để tính xem hàng hiện tại có hiển thị hay không (ngưỡng 3 hàng trước và sau)
      // flag1 = Kiểm tra dòng hiện tại có nằm trên vùng hiển thị 3 dòng hay không
      const flag1 = scrollTop.value - rowHeight.value * threshold < index * rowHeight.value;
      // flag2 = Kiểm tra dòng hiện tại có nằm dưới vùng hiển thị 3 dòng hay không
      const flag2 = index * rowHeight.value < scrollTop.value + bodyHeight.value + rowHeight.value * threshold;
      // Chỉ hiển thị dòng hiện tại khi tất cả điều kiện đều thỏa mãn
      return flag1 && flag2;
    }
    return true;
  }

  // Kết xuất tùy chỉnhBody
  function bodyCustomRender(params) {
    const { index } = params;
    // Giải thích logic mã: 【issues/776] hiển thị 100 dòng//trang, vấn đề hộp kiểm chỉ hiển thị được 3 cái
    if (propsRef.value.canResize && !recordIsShow(index)) {
      return '';
    }
    if (isRadio.value) {
      return renderRadioComponent(params);
    } else {
      return renderCheckboxComponent(params);
    }
  }

  /**
   * Render component checkbox
   */
  function renderCheckboxComponent({ record }) {
    const recordKey = getRecordKey(record);
    // Lấy tùy chỉnh của người dùngcheckboxProps
    const checkboxProps = ((getCheckboxProps) => {
      if (typeof getCheckboxProps === 'function') {
        try {
          return getCheckboxProps(record) ?? {};
        } catch (error) {
          console.error(error);
        }
      }
      return {};
    })(propsRef.value.rowSelection?.getCheckboxProps);
    return (
      <Checkbox
        {...checkboxProps}
        key={'j-select__' + recordKey}
        checked={selectedKeys.value.includes(recordKey)}
        onUpdate:checked={(checked) => onSelect(record, checked)}
        // Giải thích logic mã: 【QQYUN-8694] BasicTable đang được dùngclickToRowSelect=truedưới,selection-change Sự kiện sẽ kích hoạt nhiều lần
        onClick={(e) => e.stopPropagation()}
      />
    );
  }

  /**
   * Render component radio
   */
  function renderRadioComponent({ record }) {
    const recordKey = getRecordKey(record);
    // Lấy tùy chỉnh của người dùngradioProps
    const checkboxProps = (() => {
      const rowSelection = propsRef.value.rowSelection;
      if (rowSelection?.getCheckboxProps) {
        return rowSelection.getCheckboxProps(record);
      }
      return {};
    })();
    return (
      <Radio
        {...checkboxProps}
        key={'j-select__' + recordKey}
        checked={selectedKeys.value.includes(recordKey)}
        onUpdate:checked={(checked) => onSelect(record, checked)}
        // Giải thích logic mã: 【QQYUN-8694] BasicTable đang được dùngclickToRowSelect=truedưới,selection-change Sự kiện sẽ kích hoạt nhiều lần
        onClick={(e) => e.stopPropagation()}
      />
    );
  }

  // Tạo cột chọn
  function handleCustomSelectColumn(columns: BasicColumn[]) {
    // Giải thích logic mã: 【issues/757]Cấu hình cố định cột chọn của bảng JPopup không có hiệu lực
    const rowSelection = propsRef.value.rowSelection;
    if (!rowSelection) {
      return;
    }
    const isFixedLeft = rowSelection.fixed || columns.some((item) => item.fixed === 'left');
    // Giải thích logic mã: 【issues/757]Cấu hình cố định cột chọn của bảng JPopup không có hiệu lực
    columns.unshift({
      title: 'Chọn cột',
      flag: 'CHECKBOX',
      key: CUS_SEL_COLUMN_KEY,
      width: 50,
      minWidth: 50,
      maxWidth: 50,
      align: 'center',
      ...(isFixedLeft ? { fixed: 'left' } : {}),
      customRender: bodyCustomRender,
    });
  }

  // Xóa tất cả lựa chọn
  function clearSelectedRowKeys() {
    onSelectAll(false, 'allPage');
  }

  // Đồng bộ qua selectedKeys selectedRows
  function syncSelectedRows() {
    if (selectedKeys.value.length !== selectedRows.value.length) {
      // Trì hoãn là để đợiwatch selectedRows
      setTimeout(() => {
        setSelectedRowKeys(selectedKeys.value);
      }, 0);
    }
  }

  // Đặt key đã chọnkey
  function setSelectedRowKeys(rowKeys: string[]) {
    const isSomeRowKeys = selectedKeys.value === rowKeys;
    selectedKeys.value = rowKeys;
    const allSelectedRows = findNodeAll(
      toRaw(unref(flattedData)).concat(toRaw(unref(selectedRows))),
      (item) => rowKeys.includes(getRecordKey(item)),
      {
        children: propsRef.value.childrenColumnName ?? 'children',
      }
    );
    const trueSelectedRows: any[] = [];
    rowKeys.forEach((key: string) => {
      const found = allSelectedRows.find((item) => getRecordKey(item) === key);
      found && trueSelectedRows.push(found);
    });
    // Giải thích logic mã: 【issues/828] khắc phục sự cố treo
    if (!(isSomeRowKeys && equal(selectedRows.value, trueSelectedRows))) {
      selectedRows.value = trueSelectedRows;
      emitChange();
    }
  }
  /**
   *2023-11-03
   *Liêu Chí Dương
   *Kiểm traselectedRows.valuevà trueSelectedRows có bằng nhau không, để tránh vòng lặp vô hạn
   */
  function equal(oldVal, newVal) {
    let oldKeys = [],
      newKeys = [];
    if (oldVal.length === newVal.length) {
      oldKeys = oldVal.map((item) => getRecordKey(item));
      newKeys = newVal.map((item) => getRecordKey(item));
      for (let i = 0, len = oldKeys.length; i < len; i++) {
        const findItem = newKeys.find((item) => item === oldKeys[i]);
        if (!findItem) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  /**
   *2024-08-08
   *Liêu Chí Dương
   *Theo chọn tất cả hoặc bỏ chọn ngược lại(hoặc sử dụngclearSelectedRowKeys()Phương thức)Trả về dữ liệu cần thay đổi lần này trong dữ liệu nguồn
   */
  function getInvertRows(selectedRows: any, checked: boolean, flag): any {
    if (flag == 'currentPage') {
      const curPageRows = findNodeAll(toRaw(unref(flattedData)), () => true, {
        children: propsRef.value.childrenColumnName ?? 'children',
      });
      const selectedkeys = selectedRows.map((item) => getRecordKey(item));
      const result: any = [];
      curPageRows.forEach((item) => {
        const curRowkey = getRecordKey(item);
        const index = selectedkeys.findIndex((item) => item === curRowkey);
        if (index == -1) {
          checked && result.push(toRaw(item));
        } else {
          !checked && result.push(toRaw(item));
        }
      });
      return result;
    } else {
      return toRaw(selectedRows);
    }
  }
  function getSelectRows<T = Recordable>() {
    return unref(selectedRows) as T[];
  }

  function getSelectRowKeys() {
    return unref(selectedKeys);
  }

  function getRowSelection() {
    return unref(getRowSelectionRef)!;
  }

  function deleteSelectRowByKey(key: string) {
    const index = selectedKeys.value.findIndex((item) => item === key);
    if (index !== -1) {
      selectedKeys.value.splice(index, 1);
      selectedRows.value.splice(index, 1);
    }
  }

  // 【QQYUN-5837] tính toán động expandIconColumnIndex
  const getExpandIconColumnIndex = computed(() => {
    const { expandIconColumnIndex } = unref(propsRef);
    // Nếu chưa đặt cột chọn thì giữ nguyên
    if (getRowSelectionRef.value == null) {
      return expandIconColumnIndex;
    }
    // Đã đặt cột chọn, và chưa truyền tham số index thì trả về 1
    if (expandIconColumnIndex == null) {
      return 1;
    }
    return expandIconColumnIndex;
  });

  return {
    getRowSelection,
    getRowSelectionRef,
    getSelectRows,
    getSelectRowKeys,
    setSelectedRowKeys,
    deleteSelectRowByKey,
    selectHeaderProps,
    isCustomSelection,
    handleCustomSelectColumn,
    clearSelectedRowKeys,
    getExpandIconColumnIndex,
  };
}

function getTableBody(wrap: HTMLDivElement) {
  return new Promise<HTMLDivElement>((resolve) => {
    (function fn() {
      const bodyEl = wrap.querySelector('.ant-table-wrapper .ant-table-body') as HTMLDivElement;
      if (bodyEl) {
        resolve(bodyEl);
      } else {
        setTimeout(fn, 100);
      }
    })();
  });
}

function flattenData<RecordType>(data: RecordType[] | undefined, childrenColumnName: string): RecordType[] {
  let list: RecordType[] = [];
  (data || []).forEach((record) => {
    list.push(record);

    if (record && typeof record === 'object' && childrenColumnName in record) {
      list = [...list, ...flattenData<RecordType>((record as any)[childrenColumnName], childrenColumnName)];
    }
  });

  return list;
}

