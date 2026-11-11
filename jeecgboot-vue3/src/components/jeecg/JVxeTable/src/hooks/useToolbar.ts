import { h } from 'vue';
import JVxeToolbar from '../components/JVxeToolbar.vue';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';

export function useToolbar(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, $slots) {
  /** Kết xuất thanh công cụ */
  function renderToolbar() {
    if (props.toolbar) {
      return h(
        JVxeToolbar,
        {
          size: props.size,
          disabled: props.disabled,
          toolbarConfig: props.toolbarConfig,
          disabledRows: props.disabledRows,
          hasBtnAuth: methods.hasBtnAuth,
          selectedRowIds: data.selectedRowIds.value,
          custom: props.custom,
          addBtnCfg: props.addBtnCfg,
          removeBtnCfg: props.removeBtnCfg,
          // Sự kiện thêm mới
          onAdd: () => {
            // Giải thích logic mã: 【TV360X-212] Thêm trường mới online là xuất hiện nhắc kiểm chứng
            setTimeout(() => {
              methods.addRows();
            }, 0);
          },
          // Sự kiện lưu
          onSave: () => methods.trigger('save'),
          onRemove() {
            const $table = methods.getXTable();
            // Kích hoạt sự kiện xóa
            if (data.selectedRows.value.length > 0) {
              const deleteOldRows = methods.filterNewRows(data.selectedRows.value);
              const removeEvent: any = { deleteRows: data.selectedRows.value, $table };
              const insertRecords = $table.getInsertRecords();
              if (props.asyncRemove && deleteOldRows.length) {
                data.selectedRows.value.forEach((item) => {
                  // Xóa dữ liệu mới thêmid
                  if (insertRecords.includes(item)) {
                    delete item.id;
                  }
                });
                // Xác nhận xóa, chỉ khi gọi phương thức này mới thực sự xóa
                removeEvent.confirmRemove = () => methods.removeSelection();
              } else {
                if (props.asyncRemove) {
                  // asyncRemoveKhi dữ liệu bị xóa chỉ là dữ liệu mới thêm, tránh gọi confirmRemove gây lỗi
                  removeEvent.confirmRemove = () => {};
                }
                methods.removeSelection();
              }
              methods.trigger('removed', removeEvent);
            } else {
              methods.removeSelection();
            }
          },
          // Xóa sự kiện chọn
          onClearSelection: () => methods.clearSelection(),
          onRegister: ({ xToolbarRef }) => methods.getXTable().connect(xToolbarRef.value),
        },
        {
          toolbarPrefix: $slots.toolbarPrefix,
          toolbarAfterAdd: $slots.toolbarAfterAdd,
          toolbarSuffix: $slots.toolbarSuffix,
        }
      );
    }
    return null;
  }

  return { renderToolbar };
}
