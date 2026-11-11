<template>
  <a-radio-group :class="clazz" :value="innerValue" v-bind="cellProps" @change="(e) => handleChangeCommon(e.target.value)">
    <a-radio v-for="item of originColumn.options" :key="item.value" :value="item.value" @click="(e) => handleRadioClick(item, e)"
      >{{ item.text || item.label || item.title || item.value }}
    </a-radio>
  </a-radio-group>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  export default defineComponent({
    name: 'JVxeRadioCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, handleChangeCommon } = useJVxeComponent(props);
      const scrolling = computed(() => !!props.renderOptions.scrolling);
      const clazz = computed(() => {
        return {
          'j-vxe-radio': true,
          'no-animation': scrolling.value,
        };
      });

      function handleRadioClick(item) {
        if (originColumn.value.allowClear === true) {
          // Hủy lựa chọn
          if (item.value === innerValue.value) {
            handleChangeCommon(null);
          }
        }
      }

      return {
        clazz,
        innerValue,
        originColumn,
        cellProps,
        handleRadioClick,
        handleChangeCommon,
      };
    },
    // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
    enhanced: {
      switches: { visible: true },
    } as JVxeComponent.EnhancedPartial,
  });
</script>

<style lang="less">
  // Tắt animation, tránh lỗi khi gán giá trị động lúc cuộn
  .j-vxe-radio.no-animation {
    .ant-radio-inner,
    .ant-radio-inner::after {
      transition: none !important;
    }
  }
</style>
