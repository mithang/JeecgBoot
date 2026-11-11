import type { Ref } from 'vue';
import { nextTick, ref, watch, onBeforeUnmount } from 'vue';

type ScrollElement = HTMLDivElement | null;

interface ScrollReturn {
  scrollRef: Ref<ScrollElement>;
  scrollToBottom: () => Promise<void>;
  scrollToTop: () => Promise<void>;
  scrollToBottomIfAtBottom: () => Promise<void>;
  scrollToBottomIfAuto: () => Promise<void>;
}

export function useScroll(): ScrollReturn {
  const scrollRef = ref<ScrollElement>(null);
  // update-begin--author:liaozhiyang---date:20261027---for：【LHZP-1351】AITrong quá trình AI tạo nội dung, khi người dùng cuộn lên thì không còn ép cuộn xuống đáy nữa
  const autoScroll = ref(true);
  // Khoảng cách đến đáy nhỏ hơn giá trị này thì coi là ở đáy
  const bottomThreshold = 30;
  // Có đang kéo thanh cuộn hay không
  let dragging = false;
  let lastScrollTop = 0;
  let touchStartY = 0;
  const getDistanceToBottom = (el: HTMLDivElement) => el.scrollHeight - el.scrollTop - el.clientHeight;
  // Chỉ tạm dừng khi nội dung có thể cuộn, tránh tạm dừng nhầm khi nội dung chưa tràn khiến sau đó không bám theo nữa
  const pauseAutoScroll = () => {
    const el = scrollRef.value;
    if (el && el.scrollHeight > el.clientHeight) autoScroll.value = false;
  };
  // Con lăn chuột/Cuộn lên bằng bàn di chuột: tạm dừng trực tiếp trước khi cuộn xảy ra (tránh để hoạt ảnh cuộn mượt bị đầu ra streaming làm gián đoạn)
  const handleWheel = (e: WheelEvent) => {
    if (e.deltaY < 0) pauseAutoScroll();
  };
  // Ngón tay trượt xuống trên di động (nội dung đi lên): tạm dừng
  const handleTouchStart = (e: TouchEvent) => {
    touchStartY = e.touches[0]?.clientY ?? 0;
  };
  const handleTouchMove = (e: TouchEvent) => {
    if ((e.touches[0]?.clientY ?? 0) > touchStartY) pauseAutoScroll();
  };
  // Trong lúc kéo thanh cuộn không tự động cuộn (khi nhấp thanh cuộn, target là chính container)
  const handlePointerDown = (e: PointerEvent) => {
    if (e.target === scrollRef.value) {
      dragging = true;
      window.addEventListener('pointerup', handlePointerUp, { once: true });
    }
  };
  const handlePointerUp = () => {
    dragging = false;
    const el = scrollRef.value;
    if (el) autoScroll.value = getDistanceToBottom(el) <= bottomThreshold;
  };
  const handleScroll = () => {
    const el = scrollRef.value;
    if (!el) return;
    const scrollTop = el.scrollTop;
    const distanceToBottom = getDistanceToBottom(el);
    if (scrollTop < lastScrollTop && distanceToBottom > bottomThreshold) {
      // Cuộn lên rời khỏi đáy (bàn phím, kéo thanh cuộn, v.v.)
      autoScroll.value = false;
    } else if (scrollTop >= lastScrollTop && distanceToBottom <= bottomThreshold && !dragging) {
      // Cuộn xuống trở lại đáy, khôi phục bám theo
      autoScroll.value = true;
    }
    lastScrollTop = scrollTop;
  };
  const listeners: [string, any][] = [
    ['scroll', handleScroll],
    ['wheel', handleWheel],
    ['touchstart', handleTouchStart],
    ['touchmove', handleTouchMove],
    ['pointerdown', handlePointerDown],
  ];
  watch(scrollRef, (el, oldEl) => {
    listeners.forEach(([type, fn]) => {
      oldEl?.removeEventListener(type, fn);
      el?.addEventListener(type, fn, { passive: true });
    });
  });
  onBeforeUnmount(() => {
    listeners.forEach(([type, fn]) => scrollRef.value?.removeEventListener(type, fn));
    window.removeEventListener('pointerup', handlePointerUp);
  });

  // Ép cuộn xuống đáy (ví dụ khi người dùng gửi tin nhắn), và khôi phục tự động bám theo
  const scrollToBottom = async () => {
    await nextTick();
    autoScroll.value = true;
    if (scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
  };

  // Chỉ cuộn xuống đáy ở trạng thái tự động bám theo (dùng cho quá trình đầu ra streaming của AI)
  const scrollToBottomIfAuto = async () => {
    await nextTick();
    if (autoScroll.value && !dragging && scrollRef.value) scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
  };
  // update-end--author:liaozhiyang---date:20261027---for：【LHZP-1351】AITrong quá trình AI tạo nội dung, khi người dùng cuộn lên thì không còn ép cuộn xuống đáy nữa

  const scrollToTop = async () => {
    await nextTick();
    if (scrollRef.value) scrollRef.value.scrollTop = 0;
  };

  const scrollToBottomIfAtBottom = async () => {
    await nextTick();
    if (scrollRef.value) {
      const threshold = 100; // Threshold, indicating the distance threshold to the bottom of the scroll bar.
      const distanceToBottom = scrollRef.value.scrollHeight - scrollRef.value.scrollTop - scrollRef.value.clientHeight;
      if (distanceToBottom <= threshold) scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
    }
  };

  return {
    scrollRef,
    scrollToBottom,
    scrollToTop,
    scrollToBottomIfAtBottom,
    scrollToBottomIfAuto,
  };
}
