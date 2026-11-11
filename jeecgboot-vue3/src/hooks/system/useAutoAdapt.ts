import { ref } from 'vue';
import { ScreenSizeEnum } from '/@/enums/sizeEnum';
import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
// Định nghĩa useAdapt Tham số phương thức
interface AdaptOptions {
  // xl>1200
  xl?: string | number;
  // xl>992
  lg?: string | number;
  // xl>768
  md?: string | number;
  // xl>576
  sm?: string | number;
  // xl>480
  xs?: string | number;
  //xl<480Giá trị mặc định
  mindef?: string | number;
  //Giá trị mặc định
  def?: string | number;
}
export function useAdapt(props?: AdaptOptions) {
  //Chiều rộng mặc định
  const width = ref<string | number>(props?.def || '600px');
  //Lấy chiều rộng
  useWindowSizeFn(calcWidth, 100, { immediate: true });
  //Tính chiều rộng
  function calcWidth() {
    let windowWidth = document.documentElement.clientWidth;
    switch (true) {
      case windowWidth > ScreenSizeEnum.XL:
        width.value = props?.xl || '600px';
        break;
      case windowWidth > ScreenSizeEnum.LG:
        width.value = props?.lg || '600px';
        break;
      case windowWidth > ScreenSizeEnum.MD:
        width.value = props?.md || '600px';
        break;
      case windowWidth > ScreenSizeEnum.SM:
        width.value = props?.sm || '500px';
        break;
      case windowWidth > ScreenSizeEnum.XS:
        width.value = props?.xs || '400px';
        break;
      default:
        width.value = props?.mindef || '300px';
        break;
    }
  }
  return { width, calcWidth };
}
