import { ComputedRef, isRef, nextTick, Ref, ref, unref, watch } from 'vue';
import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
import { useLayoutHeight } from '/@/layouts/default/content/useContentViewHeight';
import { getViewportOffset } from '/@/utils/domUtils';
import { isNumber, isString } from '/@/utils/is';

export interface CompensationHeight {
  // Sử dụng layout Footer Chiều cao làm điều kiện phán đoán chiều cao bù
  useLayoutFooter: boolean;
  // refs HTMLElement
  elements?: Ref[];
}

type Upward = number | string | null | undefined;

/**
 * Tính động chiều cao nội dung, theo điểm neodomTọa độ dưới cùng đến tọa độ dưới cùng của màn hình, theo truyền vàodomchiều cao,padding, marginGiá trị bằng nhau thì tính động
 * Cuối cùng lấy chiều cao nội dung phù hợp
 *
 * @param flag Dùng để bật định danh responsive cho tính toán
 * @param anchorRef Component neo Ref<ElRef | ComponentRef>
 * @param subtractHeightRefs Danh sách component có chiều cao cần trừ Ref<ElRef | ComponentRef>
 * @param substractSpaceRefs Không gian trống cần trừ(margins/paddings)danh sách component Ref<ElRef | ComponentRef>
 * @param offsetHeightRef Tính chiều cao responsive bù trừ, khi tính chiều cao sẽ trừ trực tiếp giá trị này
 * @param upwardSpace Đệ quy lên trừ đi không gian trống Cấp bậc hoặc Cho đến khi chỉ địnhclasscho đến Giá trị số là2Đại diện cho việc đệ quy lên hai lần|Giá trị số làant-layoutBiểu thị đệ quy lên trên cho đến khi gặp.ant-layoutcho đến
 * @returns Chiều cao responsive
 */
export function useContentHeight(
  flag: ComputedRef<Boolean>,
  anchorRef: Ref,
  subtractHeightRefs: Ref[],
  substractSpaceRefs: Ref[],
  upwardSpace: Ref<Upward> | ComputedRef<Upward> | Upward = 0,
  offsetHeightRef: Ref<number> = ref(0)
) {
  const contentHeight: Ref<Nullable<number>> = ref(null);
  const { footerHeightRef: layoutFooterHeightRef } = useLayoutHeight();
  let compensationHeight: CompensationHeight = {
    useLayoutFooter: true,
  };

  const setCompensation = (params: CompensationHeight) => {
    compensationHeight = params;
  };

  function redoHeight() {
    nextTick(() => {
      calcContentHeight();
    });
  }

  function calcSubtractSpace(element: Element | null | undefined, direction: 'all' | 'top' | 'bottom' = 'all'): number {
    function numberPx(px: string) {
      return Number(px.replace(/[^\d]/g, ''));
    }
    let subtractHeight = 0;
    const ZERO_PX = '0px';
    if (element) {
      const cssStyle = getComputedStyle(element);
      const marginTop = numberPx(cssStyle?.marginTop ?? ZERO_PX);
      const marginBottom = numberPx(cssStyle?.marginBottom ?? ZERO_PX);
      const paddingTop = numberPx(cssStyle?.paddingTop ?? ZERO_PX);
      const paddingBottom = numberPx(cssStyle?.paddingBottom ?? ZERO_PX);
      if (direction === 'all') {
        subtractHeight += marginTop;
        subtractHeight += marginBottom;
        subtractHeight += paddingTop;
        subtractHeight += paddingBottom;
      } else if (direction === 'top') {
        subtractHeight += marginTop;
        subtractHeight += paddingTop;
      } else {
        subtractHeight += marginBottom;
        subtractHeight += paddingBottom;
      }
    }
    return subtractHeight;
  }

  function getEl(element: any): Nullable<HTMLDivElement> {
    if (element == null) {
      return null;
    }
    return (element instanceof HTMLDivElement ? element : element.$el) as HTMLDivElement;
  }

  async function calcContentHeight() {
    if (!flag.value) {
      return;
    }
    // Add a delay to get the correct height
    await nextTick();

    const anchorEl = getEl(unref(anchorRef));
    if (!anchorEl) {
      return;
    }
    const { bottomIncludeBody } = getViewportOffset(anchorEl);

    // substract elements height
    let substractHeight = 0;
    subtractHeightRefs.forEach((item) => {
      substractHeight += getEl(unref(item))?.offsetHeight ?? 0;
    });

    // subtract margins / paddings
    let substractSpaceHeight = calcSubtractSpace(anchorEl) ?? 0;
    substractSpaceRefs.forEach((item) => {
      substractSpaceHeight += calcSubtractSpace(getEl(unref(item)));
    });

    // upwardSpace
    let upwardSpaceHeight = 0;
    function upward(element: Element | null, upwardLvlOrClass: number | string | null | undefined) {
      if (element && upwardLvlOrClass) {
        const parent = element.parentElement;
        if (parent) {
          if (isString(upwardLvlOrClass)) {
            if (!parent.classList.contains(upwardLvlOrClass)) {
              upwardSpaceHeight += calcSubtractSpace(parent, 'bottom');
              upward(parent, upwardLvlOrClass);
            } else {
              upwardSpaceHeight += calcSubtractSpace(parent, 'bottom');
            }
          } else if (isNumber(upwardLvlOrClass)) {
            if (upwardLvlOrClass > 0) {
              upwardSpaceHeight += calcSubtractSpace(parent, 'bottom');
              upward(parent, --upwardLvlOrClass);
            }
          }
        }
      }
    }
    if (isRef(upwardSpace)) {
      upward(anchorEl, unref(upwardSpace));
    } else {
      upward(anchorEl, upwardSpace);
    }

    let height =
      bottomIncludeBody - unref(layoutFooterHeightRef) - unref(offsetHeightRef) - substractHeight - substractSpaceHeight - upwardSpaceHeight;

    // compensation height
    const calcCompensationHeight = () => {
      compensationHeight.elements?.forEach((item) => {
        height += getEl(unref(item))?.offsetHeight ?? 0;
      });
    };
    if (compensationHeight.useLayoutFooter && unref(layoutFooterHeightRef) > 0) {
      calcCompensationHeight();
    } else {
      calcCompensationHeight();
    }

    contentHeight.value = height;
  }

  onMountedOrActivated(() => {
    nextTick(() => {
      calcContentHeight();
    });
  });
  useWindowSizeFn(
    () => {
      calcContentHeight();
    },
    50,
    { immediate: true }
  );
  watch(
    () => [layoutFooterHeightRef.value],
    () => {
      calcContentHeight();
    },
    {
      flush: 'post',
      immediate: true,
    }
  );

  return { redoHeight, setCompensation, contentHeight };
}
