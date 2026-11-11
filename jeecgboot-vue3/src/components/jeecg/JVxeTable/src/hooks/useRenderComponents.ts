import { h } from 'vue';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';
import JVxeSubPopover from '../components/JVxeSubPopover.vue';
import JVxeDetailsModal from '../components/JVxeDetailsModal.vue';
import { useToolbar } from '/@/components/jeecg/JVxeTable/src/hooks/useToolbar';
import { usePagination } from '/@/components/jeecg/JVxeTable/src/hooks/usePagination';

export function useRenderComponents(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, slots) {
  // Render toolbar
  const { renderToolbar } = useToolbar(props, data, methods, slots);
  // Kết xuất phân trang
  const { renderPagination } = usePagination(props, methods);

  // Render slot toolbarAfter
  function renderToolbarAfterSlot() {
    if (slots['toolbarAfter']) {
      return slots['toolbarAfter']();
    }
    return null;
  }

  // Render bảng con bật lên khi nhấn
  function renderSubPopover() {
    if (props.clickRowShowSubForm && slots.subForm) {
      return h(
        JVxeSubPopover,
        {
          ref: 'subPopoverRef',
        },
        {
          subForm: slots.subForm,
        }
      );
    }
    return null;
  }

  // Render thông tin chi tiết bật lên khi nhấn
  function renderDetailsModal() {
    if (props.clickRowShowMainForm && slots.mainForm) {
      return h(
        JVxeDetailsModal,
        {
          ref: 'detailsModalRef',
          trigger: methods.trigger,
        },
        {
          mainForm: slots.mainForm,
        }
      );
    }
  }

  return {
    renderToolbar,
    renderPagination,
    renderSubPopover,
    renderDetailsModal,
    renderToolbarAfterSlot,
  };
}
