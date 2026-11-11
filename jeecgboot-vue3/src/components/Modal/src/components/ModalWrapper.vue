<template>
  <ScrollContainer ref="wrapperRef">
    <div ref="spinRef" :style="spinStyle" v-loading="loading" :loading-tip="loadingTip">
      <slot></slot>
    </div>
  </ScrollContainer>
</template>
<script lang="ts">
  import type { CSSProperties } from 'vue';
  import { defineComponent, computed, ref, watchEffect, unref, watch, onMounted, nextTick, onUnmounted } from 'vue';
  import { useWindowSizeFn } from '/@/hooks/event/useWindowSizeFn';
  import { ScrollContainer } from '/@/components/Container';
  import { createModalContext } from '../hooks/useModalContext';
  import { useMutationObserver } from '@vueuse/core';

  const props = {
    loading: { type: Boolean },
    useWrapper: { type: Boolean, default: true },
    modalHeaderHeight: { type: Number, default: 57 },
    modalFooterHeight: { type: Number, default: 74 },
    minHeight: { type: Number, default: null },
    maxHeight: { type: Number, default: null },
    height: { type: Number },
    footerOffset: { type: Number, default: 0 },
    visible: { type: Boolean },
    fullScreen: { type: Boolean },
    loadingTip: { type: String },
  };

  export default defineComponent({
    name: 'ModalWrapper',
    components: { ScrollContainer },
    inheritAttrs: false,
    props,
    emits: ['height-change', 'ext-height'],
    setup(props, { emit }) {
      const wrapperRef = ref<ComponentRef>(null);
      const spinRef = ref<ElRef>(null);
      const realHeightRef = ref(0);
      const minRealHeightRef = ref(0);
      const availableHeightRef = ref(0);

      let realHeight = 0;

      let stopElResizeFn: Fn = () => {};

      useWindowSizeFn(setModalHeight.bind(null, false));

      // Giải thích logic mã: 【TV360X-145]Sau khi khôi phục popup về toàn màn hình, đóng rồi mở lại cửa sổ bị thu nhỏ
      let observer,
        recordCount: any = {};
      watch(
        () => props.visible,
        () => {
          if (props.visible && !observer && !(props.maxHeight || props.height)) {
            recordCount = {};
            observer = useMutationObserver(
              spinRef,
              () => {
                setModalHeight({
                  source: 'muob',
                  callBack: (height) => {
                    const count = recordCount[height];
                    if (count) {
                      recordCount[height] = ++recordCount[height];
                      if (count > 10) {
                        observer.stop();
                        recordCount = {};
                        observer = null;
                      }
                    } else {
                      recordCount = {};
                      recordCount[height] = 1;
                    }
                  },
                });
              },
              {
                attributes: true,
                subtree: true,
              }
            );
          } else {
            if (observer) {
              observer.stop();
              observer = null;
            }
          }
        },
        { immediate: true }
      );

      createModalContext({
        redoModalHeight: setModalHeight,
      });

      const spinStyle = computed((): CSSProperties => {
        // Giải thích logic mã: 【QQYUN-7147] Cài đặt chiều cao của Model không có hiệu lực
        if (props.fullScreen) {
          return {
            height: `${unref(realHeightRef)}px`,
          };
        } else {
          const defaultMiniHeight = 200;
          if (props.height != undefined) {
            let height: number = props.height;
            if (props.minHeight === null) {
              return {
                height: `${height}px`,
              };
            } else {
              return {
                height: `${props.minHeight > height ? props.minHeight : height}px`,
              };
            }
          } else {
            return {
              minHeight: `${props.minHeight === null ? defaultMiniHeight : props.minHeight}px`,
              // Giải thích logic mã: 【QQYUN-7641]Component basicModal thêm thuộc tính MaxHeight
              maxHeight: `${props.maxHeight ? Math.min(props.maxHeight, unref(availableHeightRef) || props.maxHeight) : unref(realHeightRef)}px`,
            };
          }
        }
      });

      watchEffect(() => {
        props.useWrapper && setModalHeight();
      });

      watch(
        () => props.fullScreen,
        (v) => {
          setModalHeight();
          if (!v) {
            realHeightRef.value = minRealHeightRef.value;
          } else {
            minRealHeightRef.value = realHeightRef.value;
          }
        }
      );

      onMounted(() => {
        const { modalHeaderHeight, modalFooterHeight } = props;
        emit('ext-height', modalHeaderHeight + modalFooterHeight);
      });

      onUnmounted(() => {
        stopElResizeFn && stopElResizeFn();
      });

      async function scrollTop() {
        nextTick(() => {
          const wrapperRefDom = unref(wrapperRef);
          if (!wrapperRefDom) return;
          (wrapperRefDom as any)?.scrollTo?.(0);
        });
      }

      async function setModalHeight(option?) {
        console.log("---------Giám sát hiệu năng--------setModalHeight----------")
        const options = option || {};
        const source = options.source;
        const callBack = options.callBack;
        // Khắc phục việc listener vẫn tồn tại khi popup đóng,Khiến lần mở popup tiếp theo không có chiều cao
        // Thêm cái này,thì bắt buộc phải truyền của cấp cha khi sử dụngvisible
        if (!props.visible) return;
        const wrapperRefDom = unref(wrapperRef);
        if (!wrapperRefDom) return;
        // Giải thích logic mã: 【QQYUN-8573]Component BasicModal khi không ở chế độ toàn màn hình thì việc lấy chiều cao tối đa bị bất thường, bất kể chiều cao nội dung có vượt quá chiều cao màn hình hay không, đều bằng chiều cao nội dung
        const bodyDom = wrapperRefDom.$el.parentElement?.parentElement?.parentElement;
        if (!bodyDom) return;
        // bodyDom.style.padding = '0';
        await nextTick();

        try {
          const modalDom = bodyDom.closest('.ant-modal');
          if (!modalDom) return;
          //update-begin---author:scott ---date:20260814  for:Sửa vấn đề chiều cao nội dung popup tự thích ứng và nút thao tác ở dưới cùng bị che khuất-----------
          const spinEl = unref(spinRef);
          if (!spinEl) return;

          // Dùng vị trí bố cục CSS để tính chiều cao khả dụng, tránh việc animation mở gây đọc vị trí không ổn định
          const modalTop = Number.parseFloat(getComputedStyle(modalDom).top) || 0;
          const modalExtHeight = (modalDom as HTMLElement).offsetHeight - spinEl.clientHeight;
          let maxHeight = window.innerHeight - modalTop + (props.footerOffset! || 0) - modalExtHeight;
          //update-end---author:scott ---date:20260814  for:Sửa vấn đề chiều cao nội dung popup tự thích ứng và nút thao tác ở dưới cùng bị che khuất-----------

          // Khi quá gần phía trên sẽ xuất hiện thanh cuộn
          if (modalTop < 40) {
            maxHeight -= 26;
          }
          availableHeightRef.value = maxHeight;
          await nextTick();
          await nextTick();
          // if (!realHeight) {
          realHeight = spinEl.scrollHeight;
          // }

          if (props.fullScreen) {
            realHeightRef.value = window.innerHeight - props.modalFooterHeight - props.modalHeaderHeight - 28;
          } else {
            realHeightRef.value = props.height ? props.height : realHeight > maxHeight ? maxHeight : realHeight;
          }
          // Giải thích logic mã: 【QQYUN-9035]basicModal không đặt maxHeight hoặc height sẽ luôn thực thi setModalHeight, cần hủy kịp thờiMutationObserver
          if (source == 'muob') {
            callBack(realHeightRef.value);
          }
          
          emit('height-change', unref(realHeightRef));
        } catch (error) {
          console.log(error);
        }
      }

      return { wrapperRef, spinRef, spinStyle, scrollTop, setModalHeight };
    },
  });
</script>
