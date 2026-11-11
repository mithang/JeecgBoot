import { ref, reactive, provide, resolveComponent } from 'vue';
import { useDesign } from '/@/hooks/web/useDesign';
import { JVxeDataProps, JVxeRefs, JVxeTableProps } from '../types';
import { VxeGridInstance } from 'vxe-table';
import { randomString } from '/@/utils/common/compUtils';

export function useData(props: JVxeTableProps): JVxeDataProps {
  const { prefixCls } = useDesign('j-vxe-table');
  provide('prefixCls', prefixCls);
  return {
    prefixCls: prefixCls,
    caseId: `j-vxe-${randomString(8)}`,
    vxeDataSource: ref([]),
    scroll: reactive({ top: 0, left: 0 }),
    scrolling: ref(false),
    defaultVxeProps: reactive({
      // rowId: props.rowKey,
      rowConfig: {
        keyField: props.rowKey,
        // Tô sáng dòng được hover
        isHover: true,
      },

      // --- 【issues/209]tooltip đi kèm sẽ bị lệch vị trí, nên thay bằng title gốctitle ---
      // Ẩn khi tràn và hiển thị tooltiptooltip
      showOverflow: "title",
      // Tiêu đề bảng tràn sẽ ẩn và hiển thịtooltip
      showHeaderOverflow: "title",
      // --- 【issues/209]tooltip đi kèm sẽ bị lệch vị trí, nên thay bằng title gốctitle ---

      showFooterOverflow: true,
      // Cấu hình có thể chỉnh sửa
      editConfig: {
        trigger: 'click',
        mode: 'cell',
        //activeMethod: () => !props.disabled,
        beforeEditMethod: () => !props.disabled,
      },
      expandConfig: {
        iconClose: 'vxe-icon-arrow-right',
        iconOpen: 'vxe-icon-arrow-down',
        ...props.expandConfig,
      },
      // Cấu hình cuộn ảo, khi trục y có hơn xx bản ghi dữ liệu sẽ bật cuộn ảo
      scrollY: {
        gt: 30,
      },
      scrollX: {
        gt: 20,
        // Tạm thời tắt cuộn ảo trái/phải
        enabled: false,
      },
      radioConfig: {
        // Giữ trạng thái đã chọn
        reserve: true,
        highlight: true,
      },
      checkboxConfig: {
        // Giữ trạng thái đã chọn
        reserve: true,
        highlight: true,
      },
      // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
      // Cấm xóa trạng thái kiểm chứng khi nhấn bên ngoài bảng, tránh việc mỗi lần nhấn vùng trống đều kích hoạt reactData.validErrorMaps={} Gây ra việc render lại không cần thiết
      validConfig: {
        autoClear: false,
      },
      // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751] Tối ưu jVxetable
      keyboardConfig: {
        // Chức năng phím Delete
        isDel: false,
        // Escđóng chức năng chỉnh sửa
        isEsc: true,
        // Tab Chức năng phím
        isTab: true,
        // Nhấn phím bất kỳ để vào chế độ chỉnh sửa (trừ phím chức năng)
        isEdit: true,
        // Chức năng phím mũi tên
        isArrow: true,
        // Chức năng phím Enter
        isEnter: true,
        // Nếu tính năng được hỗ trợ, dùng cho column.type=checkbox|radio, bật chức năng dùng phím cách để chuyển trạng thái checkbox hoặc radio
        isChecked: true,
      },
    }),
    selectedRows: ref<any[]>([]),
    selectedRowIds: ref<string[]>([]),
    disabledRowIds: [],
    statistics: reactive({
      has: false,
      sum: [],
      average: [],
    }),
    authsMap: ref(null),
    innerEditRules: {},
    innerLinkageConfig: new Map<string, any>(),
    reloadEffectRowKeysMap: reactive({}),
  };
}

export function useRefs(): JVxeRefs {
  return {
    gridRef: ref<VxeGridInstance>(),
    subPopoverRef: ref<any>(),
    detailsModalRef: ref<any>(),
  };
}

export function useResolveComponent(...t: any[]): any {
  // @ts-ignore
  return resolveComponent(...t);
}
