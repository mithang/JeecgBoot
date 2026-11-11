<template>
  <Progress :class="clazz" :percent="innerValue" size="small" v-bind="cellProps" />
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { Progress } from 'ant-design-vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeCheckboxCell',
    components: { Progress },
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, scrolling } = useJVxeComponent(props);
      const clazz = computed(() => {
        return {
          'j-vxe-progress': true,
          'no-animation': scrolling.value,
        };
      });
      return { innerValue, cellProps, clazz };
    },
    // [Tăng cường component] Xem chú thích chi tiết tại::JVxeComponent.Enhanced
    enhanced: {
      switches: {
        editRender: false,
      },
      setValue(value) {
        try {
          if (typeof value !== 'number') {
            return Number.parseFloat(value);
          } else {
            return value;
          }
        } catch {
          return 0;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style scoped lang="less">
  // Tắt hiệu ứng động của thanh tiến trình, tránh xảy ra vấn đề khi gán giá trị động lúc cuộn
  .j-vxe-progress.no-animation {
    :deep(.ant-progress-bg) {
      transition: none !important;
    }
  }
</style>
