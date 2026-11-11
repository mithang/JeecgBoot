import { Ref, unref, watchEffect } from 'vue';
import { useTimeoutFn } from '/@/hooks/core/useTimeout';

export interface UseModalDragMoveContext {
  draggable: Ref<boolean>;
  destroyOnClose: Ref<boolean | undefined> | undefined;
  visible: Ref<boolean>;
}

export function useModalDragMove(context: UseModalDragMoveContext) {
  const getStyle = (dom: any, attr: any) => {
    return getComputedStyle(dom)[attr];
  };
  const drag = (wrap: any) => {
    if (!wrap) return;
    wrap.setAttribute('data-drag', unref(context.draggable));
    const dialogHeaderEl = wrap.querySelector('.ant-modal-header');
    const dragDom = wrap.querySelector('.ant-modal');

    if (!dialogHeaderEl || !dragDom || !unref(context.draggable)) return;

    dialogHeaderEl.style.cursor = 'move';

    dialogHeaderEl.onmousedown = (e: any) => {
      if (!e) return;
      // Nhấn chuột, tính khoảng cách từ phần tử hiện tại đến vùng hiển thị
      const disX = e.clientX;
      const disY = e.clientY;
      const screenWidth = document.body.clientWidth; // bodyChiều rộng hiện tại
      const screenHeight = document.documentElement.clientHeight; // Chiều cao vùng hiển thị(Đáng lẽ là chiều cao body, nhưng một số môi trường không lấy được)

      const dragDomWidth = dragDom.offsetWidth; // Chiều rộng hộp thoại
      const dragDomheight = dragDom.offsetHeight; // Chiều cao hộp thoại

      const minDragDomLeft = dragDom.offsetLeft;

      const maxDragDomLeft = screenWidth - dragDom.offsetLeft - dragDomWidth;
      const minDragDomTop = dragDom.offsetTop;
      let maxDragDomTop = screenHeight - dragDom.offsetTop - dragDomheight;
      // Giải thích logic mã: [issue/430]Trang popup xuất hiện tự động dính trên cùng, không thể di chuyển và hiển thị phần đầu---
      if(maxDragDomTop<0){
        maxDragDomTop = screenHeight - dragDom.offsetTop
      }
      // Giá trị lấy được có kèm px, dùng regex để khớp và thay thế
      const domLeft = getStyle(dragDom, 'left');
      const domTop = getStyle(dragDom, 'top');
      let styL = +domLeft;
      let styT = +domTop;

      // Lưu ý trong IE, giá trị lấy được lần đầu là giá trị mặc định của component50% Sau khi di chuyển sẽ gán thành pxpx
      if (domLeft.includes('%')) {
        styL = +document.body.clientWidth * (+domLeft.replace(/%/g, '') / 100);
        styT = +document.body.clientHeight * (+domTop.replace(/%/g, '') / 100);
      } else {
        styL = +domLeft.replace(/px/g, '');
        styT = +domTop.replace(/px/g, '');
      }

      document.onmousemove = function (e) {
        // Tính khoảng cách di chuyển thông qua ủy quyền sự kiện
        let left = e.clientX - disX;
        let top = e.clientY - disY;

        // Xử lý biên
        if (-left > minDragDomLeft) {
          left = -minDragDomLeft;
        } else if (left > maxDragDomLeft) {
          left = maxDragDomLeft;
        }

        if (-top > minDragDomTop) {
          top = -minDragDomTop;
        } else if (top > maxDragDomTop) {
          top = maxDragDomTop;
        }

        // Di chuyển phần tử hiện tại
        dragDom.style.cssText += `;left:${left + styL}px;top:${top + styT}px;`;
      };

      document.onmouseup = () => {
        document.onmousemove = null;
        document.onmouseup = null;
      };
    };
  };

  const handleDrag = () => {
    const dragWraps = document.querySelectorAll('.ant-modal-wrap');
    for (const wrap of Array.from(dragWraps)) {
      if (!wrap) continue;
      const display = getStyle(wrap, 'display');
      const draggable = wrap.getAttribute('data-drag');
      if (display !== 'none') {
        // Vị trí kéo thả
        if (draggable === null || unref(context.destroyOnClose)) {
          drag(wrap);
        }
      }
    }
  };

  watchEffect(() => {
    if (!unref(context.visible) || !unref(context.draggable)) {
      return;
    }
    useTimeoutFn(() => {
      handleDrag();
    }, 30);
  });
}
