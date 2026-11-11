import { computed, nextTick, ref, unref, watch } from 'vue';
import { propTypes } from '/@/utils/propTypes';
import { useDesign } from '/@/hooks/web/useDesign';
import { getEnhanced, replaceProps } from '../utils/enhancedUtils';
import { vModel } from '/@/components/jeecg/JVxeTable/utils';
import { JVxeRenderType } from '../types/JVxeTypes';
import { isBoolean, isFunction, isObject, isPromise } from '/@/utils/is';
import { JVxeComponent } from '../types/JVxeComponent';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import { getAreaTextByCode } from "@/components/Form/src/utils/Area";

export function useJVxeCompProps() {
  return {
    // Loại component
    type: propTypes.string,
    // Loại render
    renderType: propTypes.string.def('default'),
    // Tham số render
    params: propTypes.object,
    // Render tùy chọn tùy chỉnh
    renderOptions: propTypes.object,
  };
}

export function useJVxeComponent(props: JVxeComponent.Props) {
  const value = computed(() => {
    // Giải thích logic mã: 【QQYUN-9125]Trường kiểu ngày của cơ sở dữ liệu oracle sẽ mặc định kèm theo giờ phút giây
    const val = props.params.row[props.params.column.property];
    if (props.type === 'date' && typeof val === 'string') {
      return val.split(' ').shift();
    } else {
      return val;
    }
  });
  const innerValue = ref(value.value);
  const row = computed(() => props.params.row);
  const rows = computed(() => props.params.data);
  const column = computed(() => props.params.column);
  // Column gốc do người dùng cấu hình column
  const originColumn = computed(() => column.value.params);
  const rowIndex = computed(() => props.params._rowIndex);
  const columnIndex = computed(() => props.params._columnIndex);
  // Độ dài dữ liệu bảng
  const fullDataLength = computed(() => props.params.$table.internalData.tableFullData.length);
  // Có đang cuộn không
  const scrolling = computed(() => !!props.renderOptions.scrolling);
  // Khi có formatter, ưu tiên sử dụngformatter
  const innerLabel = computed(() => {
    if(typeof column.value?.formatter === 'function'){
      return column.value.formatter({
        cellValue: innerValue.value,
        row: row.value,
        column: column.value,
      });
    }
    return innerValue.value
  });
  const cellProps = computed(() => {
    let renderOptions = props.renderOptions;
    let col = originColumn.value;

    let cellProps = {};

    // Placeholder nhập liệu
    cellProps['placeholder'] = replaceProps(col, col.placeholder);

    // Phân tích cú phápprops
    if (isObject(col.props)) {
      Object.keys(col.props).forEach((key) => {
        cellProps[key] = replaceProps(col, col.props[key]);
      });
    }

    // Kiểm tra có phải cột bị vô hiệu hóa không
    cellProps['disabled'] = isBoolean(col['disabled']) ? col['disabled'] : cellProps['disabled'];
    // Xác định có vô hiệu hóa hàng không
    if (renderOptions.isDisabledRow(row.value, rowIndex.value)) {
      cellProps['disabled'] = true;
    }
    // Giải thích logic mã: 【TV360X-291] Không tick đồng bộ cơ sở dữ liệu thì vô hiệu hóa chức năng sắp xếp
    if (col.props && col.props.isDisabledCell) {
      if (col.props.isDisabledCell({ row: row.value, rowIndex: rowIndex.value, column: col, columnIndex: columnIndex.value })) {
        cellProps['disabled'] = true;
      }
    }
    // Kiểm tra xem có vô hiệu hóa tất cả component không
    if (renderOptions.disabled === true) {
      cellProps['disabled'] = true;
      // Giải thích logic mã: 【TV360X-1068] Khi chỉnh sửa dòng bị vô hiệu hóa toàn bộ, nút tải lên không hiển thị
      cellProps['disabledTable'] = true;
    }
    // Giải thích logic mã: VUEN-1111 Bảng con một-nhiều: chọn phòng ban không nên liên kết cấp
    if (col.checkStrictly === true) {
      cellProps['checkStrictly'] = true;
    }

    // Giải thích logic mã: Component người dùng: cấu hình tham số mới điều khiển đơn chọn/đa chọn
    if (col.isRadioSelection === true) {
      cellProps['isRadioSelection'] = true;
    } else if (col.isRadioSelection === false) {
      cellProps['isRadioSelection'] = false;
    }

    return cellProps;
  });

  const listeners = computed(() => {
    let listeners = Object.assign({}, props.renderOptions.listeners || {});
    // Sự kiện change mặc định
    if (!listeners.change) {
      listeners.change = async (event) => {
        vModel(event.value, row, column);
        await nextTick();
        // Xử lý logic liên quan đến sự kiện change (ví dụ như kiểm chứng)
        props.params.$table.updateStatus(props.params);
      };
    }
    return listeners;
  });
  const context = {
    innerLabel,
    innerValue,
    row,
    rows,
    rowIndex,
    column,
    columnIndex,
    originColumn,
    fullDataLength,
    cellProps,
    scrolling,
    handleChangeCommon,
    handleBlurCommon,
  };
  const ctx = { props, context };

  // Lấy tăng cường component
  let enhanced = getEnhanced(props.type);

  watch(
    value,
    (newValue) => {
      // -update-begin--author:liaozhiyang---date:20241210---for：【issues/7497] Sau khi ẩn một cột, từ điển không được dịch, khôi phục lại thì bình thường
      // TODO Tạm sửa như vậy để giải quyết vấn đề, nguyên nhân gốc sẽ xem sau
      // enhanced = getEnhanced(props.type);
      // -update-end--author:liaozhiyang---date:20241210---for：【issues/7497] Sau khi ẩn một cột, từ điển không được dịch, sau khi khôi phục
      // Khắc phục vấn đề trong online khi nhấp chỉnh sửa nhiều lần cùng một bản ghi thì trường của bảng khác biến thành khoảng trắng
      if (props.type === 'input' && originColumn.value.flag === 'link-table-field' && (newValue === undefined || newValue === null)) {
        return;
      }
      // Xác minh định dạng giá trị
      let getValue = enhanced.getValue(newValue, ctx);
      if (newValue !== getValue) {
        // Định dạng giá trị không đúng, gán lại giá trị
        newValue = getValue;
        vModel(newValue, row, column);
      }
      innerValue.value = enhanced.setValue(newValue, ctx);
      // Giải thích logic mã: 【QQYUN-9205] Quan hệ một-nhiều(jVxetableComponentdate)Hỗ trợ năm, năm-tháng, năm-quý, năm-tuần
      if (props.type === 'date' && props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          innerValue.value = enhanced.translate.handler(newValue, ctx);
        }
        return;
      }

      // Giải thích logic mã: 【issues/7203] Vấn đề hiển thị lại Tỉnh/Thành/Quận huyện trong mã biểu mẫu một-nhiều được tạo tự động---
      if (props.type === 'pca' && props.renderType === JVxeRenderType.spaner) {
        innerValue.value = getAreaTextByCode(newValue);
        return;
      }

      // Kiểm tra có bật dịch không
      if (props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          let res = enhanced.translate.handler(newValue, ctx);
          // Dịch bất đồng bộ, có thể giải quyết vấn đề truy vấn từ điển chậm
          if (isPromise(res)) {
            res.then((v) => (innerValue.value = v));
          } else {
            innerValue.value = res;
          }
        }
      }
    },
    { immediate: true }
  );

  // update-begin--author:liaozhiyang---date:20250526---for：【QQYUN-15511]Sau khi options của bảng con một-nhiều được sửa bằng js, làm mới rồi chỉnh sửa thì giá trị hiển thị lại không phải làlabel
  // Lắng nghe thay đổi của options (giải quyết việc khi cập nhật động options cần dịch lại)
  watch(
    () => originColumn.value?.options,
    () => {
      if (props.renderType === JVxeRenderType.spaner && enhanced.translate.enabled === true) {
        if (isFunction(enhanced.translate.handler)) {
          const newValue = value.value;
          if (newValue === undefined || newValue === null) return;
          let res = enhanced.translate.handler(newValue, ctx);
          if (isPromise(res)) {
            res.then((v) => (innerValue.value = v));
          } else {
            innerValue.value = res;
          }
        }
      }
    },
    { deep: true }
  );
  // update-end--author:liaozhiyang---date:20250526---for：【QQYUN-15511]Sau khi options của bảng con một-nhiều được sửa bằng js, làm mới rồi chỉnh sửa thì giá trị hiển thị lại không phải làlabel

  /** Xử lý chung sự kiện change */
  function handleChangeCommon($value, force = false) {
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    // Giải thích logic mã: 【issues-5025] Sự kiện của JVueTable @valueChangeVấn đề kích hoạt lặp lại
    const execute = force ? true : newValue !== oldValue;
    if (execute) {
      trigger('change', { value: newValue });
      // Kích hoạt sự kiện valueChange
      parentTrigger('valueChange', {
        type: props.type,
        value: newValue,
        oldValue: oldValue,
        col: originColumn.value,
        rowIndex: rowIndex.value,
        columnIndex: columnIndex.value,
      });
    }
  }

  /** Xử lý chung sự kiện blur */
  function handleBlurCommon($value) {
    // Giải thích logic mã: 【issues/636] JVxeTable thêm sự kiện blur
    const newValue = enhanced.getValue($value, ctx);
    const oldValue = value.value;
    //trigger('blur', { value });
    // Kích hoạt sự kiện blur
    parentTrigger('blur', {
      type: props.type,
      value: newValue,
      oldValue: oldValue,
      col: originColumn.value,
      rowIndex: rowIndex.value,
      columnIndex: columnIndex.value,
    });
  }

  /**
   * Nếu sự kiện tồn tại thì kích hoạt
   * @param name Tên sự kiện
   * @param event Tham số sự kiện
   * @param args Các tham số đính kèm khác
   */
  function trigger(name, event?, args: any[] = []) {
    let listener = listeners.value[name];
    if (isFunction(listener)) {
      if (isObject(event)) {
        event = packageEvent(name, event);
      }
      listener(event, ...args);
    }
  }

  function parentTrigger(name, event, args: any[] = []) {
    args.unshift(packageEvent(name, event));
    trigger('trigger', name, args);
  }

  function packageEvent(name, event: any = {}) {
    event.row = row.value;
    event.column = column.value;
    // onlineTương thích tham số tăng cường
    event.column['key'] = column.value['property'];
    // event.cellTarget = this
    if (!event.type) {
      event.type = name;
    }
    if (!event.cellType) {
      event.cellType = props.type;
    }
    // Có kiểm chứng biểu mẫu không, mặc định làtrue
    if (isBoolean(event.validate)) {
      event.validate = true;
    }
    return event;
  }

  /**
   * Trình tạo tên class chống xung đột kiểu
   * @param scope
   */
  function useCellDesign(scope: string) {
    return useDesign(`vxe-cell-${scope}`);
  }

  return {
    ...context,
    enhanced,
    trigger,
    useCellDesign,
  };
}

/**
 * Lấy tăng cường mặc định của component
 */
export function useDefaultEnhanced(): JVxeComponent.EnhancedPartial {
  return {
    installOptions: {
      autofocus: '',
    },
    interceptor: {
      'event.clearActived': () => true,
      'event.clearActived.className': () => true,
    },
    switches: {
      editRender: true,
      visible: false,
    },
    aopEvents: {
      editActived() {},
      editClosed() {},
      activeMethod: () => true,
    },
    translate: {
      enabled: false,
      handler(value, ctx) {
        // Phương thức dịch mặc định
        if (ctx) {
          return filterDictText(unref(ctx.context.column).params.options, value);
        } else {
          return value;
        }
      },
    },
    getValue: (value) => value,
    setValue: (value) => value,
    createValue: (defaultValue) => defaultValue,
  } as JVxeComponent.Enhanced;
}
