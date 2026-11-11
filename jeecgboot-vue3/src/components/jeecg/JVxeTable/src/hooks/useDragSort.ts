import { onMounted, onUnmounted, nextTick, watch } from 'vue';
import { JVxeTableMethods, JVxeTableProps } from '/@/components/jeecg/JVxeTable/src/types';
import Sortable from 'sortablejs';
import { isEnabledVirtualYScroll } from '/@/components/jeecg/JVxeTable/utils';

export function useDragSort(props: JVxeTableProps, methods: JVxeTableMethods) {
  if (props.dragSort) {
    let sortable2: Sortable;
    let initTime: any;

    onMounted(() => {
      // Sau khi tải xong mới liên kết sự kiện kéo
      initTime = setTimeout(createSortable, 300);
    });

    onUnmounted(() => {
      clearTimeout(initTime);
      if (sortable2) {
        sortable2.destroy();
      }
    });
    // update-begin--author:liaozhiyang---date:20260415---for:【QQYUN-15134]Sửa lỗi jvxetable dùng fixed cố định thì không kéo thả được
    // Giải thích logic mã: Lắng nghe sự thay đổi của maxHeight (popup toàn màn hình↔Kích hoạt khi chuyển đổi thu nhỏ).
    // Giải pháp: khi maxHeight thay đổi thì hủy Sortable cũ và khởi tạo lại, để nó gắn vào tbody chính xác.
    watch(
      () => props.maxHeight,
      () => {
        if (sortable2) {
          sortable2.destroy();
          sortable2 = null as any;
        }
        clearTimeout(initTime);
        initTime = setTimeout(createSortable, 300);
      }
    );

    function createSortable() {
      let xTable = methods.getXTable();
      // Giải thích logic mã: Cột sắp xếp kéo-thả mặc định fixed:left, lúc này drag-btn Bên trong tbody của wrapper cột cố định.
      // Nếu dragSortFixed!='none', ưu tiên gắn tbody của wrapper cột cố định, đảm bảo Sortable có thể bắt được sự kiện kéo thả;
      // Nếu dragSortFixed='none'(Cột kéo thả không cố định), thì gắn với tbody chính.
      const domFixed =
        props.dragSortFixed !== 'none'
          ? xTable.$el.querySelector('.vxe-table--fixed-left-wrapper .vxe-table--body tbody')
          : null;
      const domMain = xTable.$el.querySelector('.vxe-table--body-inner-wrapper > .vxe-table--body tbody');
      const dom = domFixed || domMain;
      if (!dom) {
        console.warn('[JVxeTable] Khởi tạo sắp xếp kéo-thả thất bại, có thể làvxe-tableKhông tương thích phiên bản do nâng cấp.');
        return;
      }

      // Chỉ mục DOM của hàng đích đang di chuột trong quá trình kéo thả (được onMove cập nhật theo thời gian thực)
      let hoverIndex = -1;
      // Chỉ mục DOM của hàng bắt đầu kéo thả (dùng trong onMove để xác định có đang di chuột lên chính nó không)
      let dragStartIndex = -1;

      /**
       * Cho dòng thứ idx trong tất cả tbody (0-based) thêm hoặc loại bỏ CSS class.
       * Dùng cho liên chủ thể + Wrapper cột cố định đồng bộ trạng thái hiển thị, tránh chỉ sửa một phía tbody khiến kiểu dáng không nhất quán.
       */
      function setRowClass(idx: number, cls: string, add: boolean) {
        xTable.$el.querySelectorAll(`.vxe-table--body tbody tr:nth-child(${idx + 1})`).forEach((tr) => {
          (tr as HTMLElement).classList[add ? 'add' : 'remove'](cls);
        });
      }
      /** Xóa tất cả các dòng có class được chỉ định */
      function clearRowClass(cls: string) {
        xTable.$el.querySelectorAll(`.${cls}`).forEach((tr) => {
          (tr as HTMLElement).classList.remove(cls);
        });
      }

      sortable2 = Sortable.create(dom as HTMLElement, {
        handle: '.drag-btn',
        // Giải thích logic mã: 【QQYUN-8785]id vị trí cột của biểu mẫu online chưa được giới hạn, kéo cột khác lên trên cột id, khi đồng bộ cơ sở dữ liệu sẽ báo lỗi
        filter: '.not-allow-drag',
        draggable: '.allow-drag',
        direction: 'vertical',
        animation: 0,
        onStart(e) {
          // Khởi tạo chỉ mục hover ở vị trí bắt đầu, và thêm hiệu ứng vô hiệu hóa cho dòng đang được kéo lên
          hoverIndex = e.oldIndex!;
          dragStartIndex = e.oldIndex!;
          setRowClass(e.oldIndex!, 'j-vxe-drag-source', true);
        },
        onMove(e) {
          // Giải thích logic mã: trong quá trình kéo thả chỉ ghi lại chỉ mục DOM của mục tiêu đang hover, ngăn Sortable hoán đổi vị trí hàng theo thời gian thực.
          // Lợi ích: Tất cả tbody (phần thân + wrapper cột cố định) đều giữ nguyên thứ tự trước khi thả tay, không gây lệch thị giác;
          // Sau khi thả chuột sẽ thống nhất do vxe-table Dữ liệu điều khiển render lại, vấn đề kéo thả sắp xếp cột fixed được giải quyết triệt để.
          const idx = Array.from((e.from as HTMLElement).children).indexOf(e.related as HTMLElement);
          if (idx !== -1) {
            hoverIndex = idx;
          }
          // Cập nhật đường chỉ báo hàng đang hover: xóa trạng thái cũ trước, khi hover chính nó thì không hiển thị
          clearRowClass('j-vxe-drag-hover-top');
          clearRowClass('j-vxe-drag-hover-bottom');
          if (hoverIndex !== dragStartIndex) {
            // Kéo xuống (chèn bên dưới dòng đang di chuột)→ Đường dưới; kéo lên (chèn phía trên dòng đang hover)→ Đường kẻ trên cùng
            const cls = hoverIndex > dragStartIndex ? 'j-vxe-drag-hover-bottom' : 'j-vxe-drag-hover-top';
            setRowClass(hoverIndex, cls, true);
          }
          return false; // Ngăn Sortable di chuyển hàng DOM
        },
        onEnd(e: any) {
          // Kết thúc kéo-thả, xóa tất cả trạng thái hiển thị
          clearRowClass('j-vxe-drag-source');
          clearRowClass('j-vxe-drag-hover-top');
          clearRowClass('j-vxe-drag-hover-bottom');
          // -update-begin--author:liaozhiyang---date:20240619---for：【TV360X-585] Kéo trường thì cuộn ảo không hoạt động tốt
          const isRealEnabledVirtual = isEnabledVirtualYScroll(props, xTable);
          let newIndex: number;
          let oldIndex: number;

          if (isRealEnabledVirtual) {
            // Cuộn ảo: sau khi onMove trả về false, hàng DOM không di chuyển,e.item Chính là dòng bị kéo thả đó
            const dragNode = e.item as HTMLElement;
            const dragRowInfo = xTable.getRowNode(dragNode);
            if (!dragRowInfo) return;
            oldIndex = dragRowInfo.index;
            if (hoverIndex === e.oldIndex) return;

            // Lấy chỉ mục dữ liệu thực tế qua nút DOM vùng hiển thị tương ứng với hoverIndex
            const hoverNode = (e.from as HTMLElement).childNodes[hoverIndex] as HTMLElement;
            if (!hoverNode) return;
            const hoverRowInfo = xTable.getRowNode(hoverNode);
            if (!hoverRowInfo) return;
            newIndex = hoverRowInfo.index;
          } else {
            // Không phải cuộn ảo: hàng DOM chưa từng di chuyển, trực tiếp dùng hoverIndex làm chỉ mục đích, không cần khôi phục DOM
            oldIndex = e.oldIndex;
            newIndex = hoverIndex;
            if (oldIndex === newIndex) return;
          }
          // -update-end--author:liaozhiyang---date:20240619---for：【TV360X-585] Kéo trường thì cuộn ảo không hoạt động tốt

          nextTick(() => {
            methods.doSort(oldIndex, newIndex);
            methods.trigger('dragged', { oldIndex: oldIndex, newIndex: newIndex });
          });
        },
      });
    }
    // update-begin--author:liaozhiyang---date:20260415---for:【QQYUN-15134]Sửa lỗi jvxetable dùng fixed cố định thì không kéo thả được
  }
}
