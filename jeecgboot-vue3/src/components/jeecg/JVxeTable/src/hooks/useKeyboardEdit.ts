/*
 * JVxeTable Thao tác bàn phím
 */
import type { VxeTablePropTypes } from 'vxe-table';
import type { JVxeTableProps } from '../types';
import { computed } from 'vue';

/**
 * JVxeTable Thao tác bàn phím
 *
 * @param props
 */
export function useKeyboardEdit(props: JVxeTableProps) {
  // Có bật thao tác bàn phím không
  const enabledKeyboard = computed(() => props.keyboardEdit ?? false);
  // Viết lại keyboardConfig
  const keyboardConfig: VxeTablePropTypes.KeyboardConfig = {
    editMethod({ row, column, $table }) {
      // Viết lại chế độ ghi đè mặc định thành chế độ thêm vào
      $table.setActiveCell(row, column);
      return true;
    },
  };
  // Cấu hình thao tác bàn phím
  const keyboardEditConfig = computed(() => {
    return {
      mouseConfig: {
        selected: enabledKeyboard.value,
      },
      keyboardConfig,
    };
  });

  return {
    keyboardEditConfig,
  };
}
