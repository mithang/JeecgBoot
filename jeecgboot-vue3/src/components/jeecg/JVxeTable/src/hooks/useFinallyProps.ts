import { unref, computed, ref, watch, nextTick } from 'vue';
import { merge, debounce } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { useAttrs } from '/@/hooks/core/useAttrs';
import { useKeyboardEdit } from '../hooks/useKeyboardEdit';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';

export function useFinallyProps(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods) {
  const attrs = useAttrs();
  // vxe Cấu hình thao tác bàn phím
  const { keyboardEditConfig } = useKeyboardEdit(props);
  // vxe Cuối cùng editRules
  const vxeEditRules = computed(() => merge({}, props.editRules, data.innerEditRules));
  // vxe Cuối cùng events
  const vxeEvents = computed(() => {
    let listeners = { ...unref(attrs) };
    let events = {
      onScroll: methods.handleVxeScroll,
      onCellClick: methods.handleCellClick,
      onEditClosed: methods.handleEditClosed,
      onEditActived: methods.handleEditActived,
      onRadioChange: methods.handleVxeRadioChange,
      onCheckboxAll: methods.handleVxeCheckboxAll,
      onCheckboxChange: methods.handleVxeCheckboxChange,
      // Giải thích logic mã: 【QQYUN-8566] JVXETable không thể nhớ cài đặt cột
      onCustom: methods.handleCustom,
    };
    // Sự kiện do người dùng truyền vào sẽ được hợp nhất
    Object.keys(listeners).forEach((key) => {
      let listen = listeners[key];
      if (events.hasOwnProperty(key)) {
        if (isArray(listen)) {
          listen.push(events[key]);
        } else {
          listen = [events[key], listen];
        }
      }
      events[key] = listen;
    });
    return events;
  });

  // vxe Cuối cùng props
  const vxePropsMerge = computed(() => {
    // Giải thích logic mã: 【QQYUN-8785]id vị trí cột của biểu mẫu online chưa được giới hạn, kéo cột khác lên trên cột id, khi đồng bộ cơ sở dữ liệu sẽ báo lỗi
    let rowClass = {};
    if (props.dragSort) {
      rowClass = {
        rowClassName: (params) => {
          let { row } = params;
          const find = props.notAllowDrag?.find((item:any) => {
            const {key, value} = item;
            return row[key] == value;
          });
          // rowClassName do nghiệp vụ truyền vàorowClassName
          const popsRowClassName = props.rowClassName ?? '';
          let outClass = '';
          if(typeof popsRowClassName==='string'){
            popsRowClassName && (outClass = popsRowClassName);
          }else if(typeof popsRowClassName==='function'){
            outClass = popsRowClassName(params)
          }
          return find ? `not-allow-drag ${outClass}` : `allow-drag ${outClass}`;
        },
      };
    }
    return merge(
      {},
      data.defaultVxeProps,
      {
        showFooter: data.statistics.has,
      },
      unref(attrs),
      {
        ref: 'gridRef',
        size: props.size,
        loading: false,
        disabled: props.disabled,
        // columns: unref(data.vxeColumns),
        editRules: unref(vxeEditRules),
        height: props.height === 'auto' ? null : props.height,
        maxHeight: props.maxHeight,
        // Giải thích logic mã: 【QQYUN-5133] Nâng cấp chỉnh sửa dòng của JVxeTable
        scrollY: props.scrollY,
        scrollX: props.scrollX,
        border: props.bordered,
        footerMethod: methods.handleFooterMethod,
        // Cấu hình hàng mở rộng
        expandConfig: {
          toggleMethod: methods.handleExpandToggleMethod,
        },
        // Cấu hình có thể chỉnh sửa
        editConfig: {
          //activeMethod: methods.handleActiveMethod,
          beforeEditMethod: methods.handleActiveMethod,
        },
        radioConfig: {
          checkMethod: methods.handleCheckMethod,
        },
        checkboxConfig: {
          checkMethod: methods.handleCheckMethod,
        },
        ...rowClass
        // rowClassName:(params)=>{
        //   const { row } = params;
        //   return row.dbFieldName=='id'?"not-allow-drag":"allow-drag"
        // }
      },
      unref(vxeEvents),
      unref(keyboardEditConfig)
    );
  });

  const vxeColumnsRef = ref(data.vxeColumns!.value || [])
  const watchColumnsDebounce = debounce(async () => {
    vxeColumnsRef.value = data.vxeColumns!.value
  }, 50)
  watch(data.vxeColumns!, watchColumnsDebounce)

  const vxeProps = computed(() => {
    return {
      ...unref(vxePropsMerge),
      // 【issue/8695]Tách riêng columns để tránh vấn đề hiệu năng
      columns: unref(vxeColumnsRef),
    }
  });

  return {
    vxeProps,
    prefixCls: data.prefixCls,
  };
}
