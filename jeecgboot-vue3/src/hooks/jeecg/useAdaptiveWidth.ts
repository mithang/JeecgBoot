/**
 * Constructor chiều rộng tự thích ứng
 *
 * @time 2022-4-8
 * @author sunjianlei
 */
import { ref } from 'vue';
import { useDebounceFn, tryOnUnmounted } from '@vueuse/core';
import { useEventListener } from '/@/hooks/event/useEventListener';

// key = jsToán tử+Số
const defWidthConfig: configType = {
  '<=565': '100%',
  '<=1366': '800px',
  '<=1600': '600px',
  '<=1920': '600px',
  '>1920': '500px',
};

type configType = Record<string, string | number>;

/**
 * Chiều rộng tự thích ứng
 *
 * @param widthConfig Cấu hình độ rộng, có thể tham khảo defWidthConfig Cấu hình
 * @param assign Có hợp nhất cấu hình mặc định không
 * @param debounce Số mili giây debounce
 */
export function useAdaptiveWidth(widthConfig = defWidthConfig, assign = true, debounce = 50) {
  const widthConfigAssign = assign ? Object.assign({}, defWidthConfig, widthConfig) : widthConfig;
  const configKeys = Object.keys(widthConfigAssign);

  const adaptiveWidth = ref<string | number>();

  /**
   * Tiến hành tính chiều rộng
   * @param innerWidth
   */
  function calcWidth(innerWidth) {
    let width;
    for (const key of configKeys) {
      try {
        // Thông quajsTính toán
        let flag = new Function(`return ${innerWidth} ${key}`)();
        if (flag) {
          width = widthConfigAssign[key];
          break;
        }
      } catch (e) {
        console.error(e);
      }
    }
    if (width) {
      adaptiveWidth.value = width;
    } else {
      console.warn('Không tìm thấy chiều rộng tự thích ứng khớp');
    }
  }

  // Tính toán ban đầu
  calcWidth(window.innerWidth);

  // Lắng nghe resize Sự kiện
  const { removeEvent } = useEventListener({
    el: window,
    name: 'resize',
    listener: useDebounceFn(() => calcWidth(window.innerWidth), debounce),
  });
  // Khi gỡ component thì hủy lắng nghe sự kiện
  tryOnUnmounted(() => removeEvent());

  return { adaptiveWidth };
}

/**
 * Drawer tự thích ứng chiều rộng
 */
export function useDrawerAdaptiveWidth() {
  return useAdaptiveWidth(
    {
      '<=620': '100%',
      '<=1600': 600,
      '<=1920': 650,
      '>1920': 700,
    },
    false
  );
}
