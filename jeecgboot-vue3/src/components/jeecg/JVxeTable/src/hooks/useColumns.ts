import type { JVxeColumn, JVxeDataProps, JVxeTableProps } from '../types';
import { computed, nextTick, toRaw } from 'vue';
import { isArray, isEmpty, isPromise } from '/@/utils/is';
import { cloneDeep } from 'lodash-es';
import { JVxeTypePrefix, JVxeTypes } from '../types/JVxeTypes';
import { initDictOptions } from '/@/utils/dict';
import { pushIfNotExist } from '/@/utils/common/compUtils';
import { getEnhanced } from '../utils/enhancedUtils';
import { isRegistered } from '../utils/registerUtils';
import { JVxeComponent } from '../types/JVxeComponent';
import { useValidateRules } from './useValidateRules';
import { JVxeTableMethods } from '../types';

// handle Tham số phương thức
export interface HandleArgs {
  props: JVxeTableProps;
  slots: any;
  data: JVxeDataProps;
  methods: JVxeTableMethods;
  col?: JVxeColumn;
  columns: JVxeColumn[];
  renderOptions?: any;
  enhanced?: JVxeComponent.Enhanced;
}

export function useColumns(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, slots) {
  data.vxeColumns = computed(() => {
    // linkageConfigKhi thay đổi cũng cần thực thi
    const linkageConfig = toRaw(props.linkageConfig);
    if (linkageConfig) {
      // console.log(linkageConfig);
    }
    let columns: JVxeColumn[] = [];
    if (isArray(props.columns)) {
      // handle Tham số phương thức
      const args: HandleArgs = { props, slots, data, methods, columns };
      let seqColumn, selectionColumn, expandColumn, dragSortColumn;

      const handleColumn = (column: JVxeColumn, container: JVxeColumn[]) => {
        // Loại trừ cột chưa được cấp quyền 1 = Hiển thị/Ẩn; 2 = Vô hiệu hóa
        let auth = methods.getColAuth(column.key);
        if (auth?.type == '1' && !auth.isAuth) {
          return;
        } else if (auth?.type == '2' && !auth.isAuth) {
          column.disabled = true;
        }
        // type Không nhập, mặc định là normal
        if (column.type == null || isEmpty(column.type)) {
          column.type = JVxeTypes.normal;
        }
        let col: JVxeColumn = cloneDeep(column);
        // Xử lý cột ẩn
        if (col.type === JVxeTypes.hidden) {
          return handleInnerColumn(args, col, handleHiddenColumn);
        }
        // Xử lý cột con
        // Kiểm tra xem có phải là cột nhóm hay không, nếu hiện tại là cấp cha thì không cần xử lý render
        if (Array.isArray(col.children) && col.children.length > 0) {
          const children: JVxeColumn[] = [];
          col.children.forEach((child: JVxeColumn) => handleColumn(child, children));
          col.children = children;
          container.push(col);
          return;
        }
        // Component chưa được đăng ký, tự động đặt thành normal
        if (!isRegistered(col.type)) {
          col.type = JVxeTypes.normal;
        }
        args.enhanced = getEnhanced(col.type);
        args.col = col;
        args.renderOptions = {
          bordered: props.bordered,
          disabled: props.disabled,
          scrolling: data.scrolling,
          isDisabledRow: methods.isDisabledRow,
          listeners: {
            trigger: (name, event) => methods.trigger(name, event),
            valueChange: (event) => methods.trigger('valueChange', event),
            /** Sắp xếp lại hàng */
            rowResort: (event) => {
              methods.doSort(event.oldIndex, event.newIndex);
              methods.trigger('dragged', event);
            },
            /** Chèn một hàng bên dưới hàng hiện tại */
            rowInsertDown: (rowIndex) => methods.insertRows({}, rowIndex + 1),
          },
        };
        if (col.type === JVxeTypes.rowNumber) {
          seqColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowRadio || col.type === JVxeTypes.rowCheckbox) {
          selectionColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowExpand) {
          expandColumn = col;
          container.push(col);
        } else if (col.type === JVxeTypes.rowDragSort) {
          dragSortColumn = col;
          container.push(col);
        } else {
          col.params = column;
          args.columns = container;
          handlerCol(args);
        }
      }

      props.columns.forEach((column: JVxeColumn) => handleColumn(column, columns));

      handleInnerColumn(args, seqColumn, handleSeqColumn);
      handleInnerColumn(args, selectionColumn, handleSelectionColumn);
      handleInnerColumn(args, expandColumn, handleExpandColumn);
      handleInnerColumn(args, dragSortColumn, handleDragSortColumn, true);
      // update-begin--author:liaozhiyang---date:2024-05-30---for【TV360X-371] component không thể chỉnh sửa thiếu dấu * bắt buộc*dấu
      customComponentAddStar(columns);
    }
    return columns;
  });
}

/**
 * 2024-05-30
 * liaozhiyang
 * Component không thể chỉnh sửa: trường bắt buộc thêm dấu * thủ công qua title*dấu
 */
function customComponentAddStar(columns) {
  columns.forEach((column) => {
    const { params } = column;
    if (params) {
      const { validateRules, type } = params;
      if (
        validateRules?.length &&
        [
          JVxeTypes.checkbox,
          JVxeTypes.radio,
          JVxeTypes.upload,
          JVxeTypes.progress,
          JVxeTypes.departSelect,
          JVxeTypes.userSelect,
          JVxeTypes.image,
          JVxeTypes.file,
        ].includes(type)
      ) {
        if (validateRules.find((item) => item.required)) {
          // update-begin--author:liaozhiyang---date:20260807---for:【LHZP-703]Dấu bắt buộc của người dùng, phòng ban, hình ảnh, tệp, công tắc hiển thị không phải màu đỏ
          column.headerClassName = 'jvxe-col--required';
          // update-end--author:liaozhiyang---date:20260807---for:【LHZP-703]Dấu bắt buộc của người dùng, phòng ban, hình ảnh, tệp, công tắc hiển thị không phải màu đỏ
        }
      }
    }
  });
}

/** Xử lý cột tích hợp */
function handleInnerColumn(args: HandleArgs, col: JVxeColumn, handler: (args: HandleArgs) => void, assign?: boolean) {
  let renderOptions = col?.editRender || col?.cellRender;
  return handler({
    ...args,
    col: col,
    renderOptions: assign ? Object.assign({}, args.renderOptions, renderOptions) : renderOptions,
  });
}

/**
 * Xử lý cột ẩn
 */
function handleHiddenColumn({ col, columns }: HandleArgs) {
  col!.params = cloneDeep(col);
  delete col!.type;
  col!.field = col!.key;
  col!.visible = false;
  columns.push(col!);
}

/**
 * Xử lý cột số thứ tự hàng
 */
function handleSeqColumn({ props, col, columns }: HandleArgs) {
  // Kiểm tra xem có bật cột số hàng không
  if (props.rowNumber) {
    let column = {
      type: 'seq',
      title: '#',
      width: 60,
      // 【QQYUN-8405】
      fixed: props.rowNumberFixed,
      align: 'center',
    };
    // Giải thích logic mã: 【QQYUN-8405]vxetable hỗ trợ cố định số thứ tự hay không (cần cho mobile)
    if (props.rowNumberFixed === 'none') {
      delete column.fixed;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * Xử lý cột có thể chọn
 */
function handleSelectionColumn({ props, data, col, columns }: HandleArgs) {
  // Kiểm tra xem có bật hàng có thể chọn không
  // -update-begin--author:liaozhiyang---date:20240617---for：【TV360X-1002] trang chi tiết không hiển thị chỉnh sửa hàngcheckbox
  if (props.rowSelection && props.disabled == false) {
    // -update-end--author:liaozhiyang---date:20240617---for：【TV360X-1002] trang chi tiết không hiển thị chỉnh sửa hàngcheckbox
    let width = 45;
    if (data.statistics.has && !props.rowExpand && !props.dragSort) {
      width = 60;
    }
    let column: any = {
      type: props.rowSelectionType,
      width: width,
      fixed: 'left',
      align: 'center',
    };
    // Giải thích logic mã: 【issues/1162]Khi cột JVxeTable quá dài (xuất hiện thanh cuộn ngang) thì không thể kéo thả sắp xếp
    if (props.rowSelectionFixed === 'none') {
      delete column.fixed;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/**
 * Xử lý hàng có thể mở rộng
 */
function handleExpandColumn({ props, data, col, columns }: HandleArgs) {
  // Có thể mở rộng hàng không
  if (props.rowExpand) {
    let width = 40;
    if (data.statistics.has && !props.dragSort) {
      width = 60;
    }
    let column = {
      type: 'expand',
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      slots: { content: 'expandContent' },
    };
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column as any);
    }
  }
}

/** Xử lý cột có thể sắp xếp */
function handleDragSortColumn({ props, data, col, columns, renderOptions }: HandleArgs) {
  // Có thể kéo thả để sắp xếp không
  if (props.dragSort) {
    let width = 40;
    if (data.statistics.has) {
      width = 60;
    }
    let column: any = {
      title: '',
      width: width,
      fixed: 'left',
      align: 'center',
      // Giải thích logic mã: 【QQYUN-8785]id vị trí cột của biểu mẫu online chưa được giới hạn, kéo cột khác lên trên cột id, khi đồng bộ cơ sở dữ liệu sẽ báo lỗi
      params: {
        insertRow: props.insertRow,
        notAllowDrag: props.notAllowDrag,
        ...col?.params,
      },
    };
    // Giải thích logic mã: 【issues/1162]Khi cột JVxeTable quá dài (xuất hiện thanh cuộn ngang) thì không thể kéo thả sắp xếp
    if (props.dragSortFixed === 'none') {
      delete column.fixed;
    }
    let cellRender = {
      name: JVxeTypePrefix + JVxeTypes.rowDragSort,
      sortKey: props.sortKey,
    };
    if (renderOptions) {
      column.cellRender = Object.assign(renderOptions, cellRender);
    } else {
      column.cellRender = cellRender;
    }
    if (col) {
      Object.assign(col, column);
    } else {
      columns.unshift(column);
    }
  }
}

/** Xử lý cột component tùy chỉnh */
function handlerCol(args: HandleArgs) {
  const { props, col, columns, enhanced } = args;
  if (!col) return;
  let { type } = col;
  col.field = col.key;
  delete col.type;
  let renderName = 'cellRender';
  // Tùy chọn render
  let $renderOptions: any = { name: JVxeTypePrefix + type };
  if (enhanced?.switches.editRender) {
    if (!(enhanced.switches.visible || props.alwaysEdit)) {
      renderName = 'editRender';
    }
    // $renderOptions.type = (enhanced.switches.visible || props.alwaysEdit) ? 'visible' : 'default'
  }
  col[renderName] = $renderOptions;
  // Giải thích logic mã: 【QQYUN-5806]Mã tăng cường js thay đổi options tìm kiếm của dropdown (thêm customOptions là true thì không đọc danh mục, đi theo options của chính nó)
  !col.params.customOptions && handleDict(args);
  handleRules(args);
  handleStatistics(args);
  handleSlots(args);
  handleLinkage(args);
  handleReloadEffect(args);

  if (col.editRender) {
    Object.assign(col.editRender, args.renderOptions);
  }
  if (col.cellRender) {
    Object.assign(col.cellRender, args.renderOptions);
  }

  // slot Cột loại slot: hiển thị biểu tượng chỉnh sửa ở tiêu đề bảng thông qua titlePrefix (không được dùng editRender, nếu không cellRender sẽ bị bỏ qua khi ở trạng thái không chỉnh sửa)
  if (col.params.type === JVxeTypes.slot && col.cellRender && !col.editRender) {
    col.titlePrefix = { icon: 'vxe-table-icon-edit' };
  }

  columns.push(col);
}

/**
 * Xử lý từ điển
 */
async function handleDict({ col, methods }: HandleArgs) {
  if (col && col.params.dictCode) {
    /** Tải từ điển dữ liệu và gộp vào options */
    try {
      // Tìm kiếm danh mục
      if (!isPromise(col.params.optionsPromise)) {
        col.params.optionsPromise = new Promise(async (resolve) => {
          // Giải thích logic mã: VUEN-1180 [Sinh mã] Bảng con không hỗ trợ kèm điều kiện?
          let dictCodeString = col.params.dictCode;
          if (dictCodeString) {
            dictCodeString = encodeURI(dictCodeString);
          }
          const dictOptions: any = await initDictOptions(dictCodeString);
          let options = col.params.options ?? [];
          dictOptions.forEach((dict) => {
            // Lọc dữ liệu trùng lặp
            if (options.findIndex((o) => o.value === dict.value) === -1) {
              options.push(dict);
            }
          });
          resolve(options);
        });
      }
      col.params.options = await col.params.optionsPromise;
      await nextTick();
      await methods.getXTable().updateData();
    } catch (e) {
      console.group(`[JVxeTable] Tìm kiếm danh mục "${col.params.dictCode}"  đã xảy ra ngoại lệ!`);
      console.warn(e);
      console.groupEnd();
    }
  }
}

/**
 * Xử lý kiểm chứng
 */
function handleRules(args: HandleArgs) {
  if (isArray(args.col?.validateRules)) {
    useValidateRules(args);
  }
}

/**
 * Xử lý cột thống kê
 */
function handleStatistics({ col, data }: HandleArgs) {
  // sum = Tổng,average = Giá trị trung bình
  if (col && isArray(col.statistics)) {
    data.statistics.has = true;
    col.statistics.forEach((item) => {
      if (!isEmpty(item)) {
        let arr = data.statistics[(item as string).toLowerCase()];
        if (isArray(arr)) {
          pushIfNotExist(arr, col.key);
        }
      }
    });
  }
}

/**
 * Xử lý slot
 */
function handleSlots({ slots, col, renderOptions }: HandleArgs) {
  // slot Xử lý đặc biệt cho component
  if (col && col.params.type === JVxeTypes.slot) {
    if (!isEmpty(col.slotName) && slots.hasOwnProperty(col.slotName)) {
      renderOptions.slot = slots[col.slotName];
    }
  }
}

/** Xử lý cột liên động */
function handleLinkage({ data, col, renderOptions, methods }: HandleArgs) {
  // Xử lý cột liên động, cột liên động chỉ tác dụng lên component select
  if (col && col.params.type === JVxeTypes.select && data.innerLinkageConfig != null) {
    // Kiểm tra xem cột hiện tại có phải là cột liên động không
    if (data.innerLinkageConfig.has(col.key)) {
      renderOptions.linkage = {
        config: data.innerLinkageConfig.get(col.key),
        getLinkageOptionsAsync: methods.getLinkageOptionsAsync,
        getLinkageOptionsSibling: methods.getLinkageOptionsSibling,
        handleLinkageSelectChange: methods.handleLinkageSelectChange,
      };
    }
  }
}

function handleReloadEffect({ props, data, renderOptions }: HandleArgs) {
  renderOptions.reloadEffect = {
    enabled: props.reloadEffect,
    getMap() {
      return data.reloadEffectRowKeysMap;
    },
    isEffect(rowId) {
      return data.reloadEffectRowKeysMap[rowId] === true;
    },
    removeEffect(rowId) {
      return (data.reloadEffectRowKeysMap[rowId] = false);
    },
  };
}
