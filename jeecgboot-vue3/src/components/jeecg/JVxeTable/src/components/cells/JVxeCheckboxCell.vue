<template>
  <div :class="boxClass" :style="boxStyle" title="">
    <a-checkbox :checked="innerValue" v-bind="cellProps" @change="handleChange" />
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { JVxeComponent } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';
  import { isArray, isBoolean } from '/@/utils/is';

  export default defineComponent({
    name: 'JVxeCheckboxCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, originColumn, scrolling, handleChangeCommon } = useJVxeComponent(props);
      // Có bật viền không
      const bordered = computed(() => !!props.renderOptions.bordered);
      // box Tên class
      const boxClass = computed(() => {
        return {
          'j-vxe-checkbox': true,
          'no-animation': scrolling.value,
        };
      });
      // box Kiểu dáng nội tuyến
      const boxStyle = computed(() => {
        const style = {};
        // Nếu có viền và chưa thiết lập thuộc tính align thì sẽ ép căn giữa
        if (bordered.value && !originColumn.value.align) {
          style['text-align'] = 'center';
        }
        return style;
      });

      // onChange Sự kiện
      function handleChange(event) {
        handleChangeCommon(event.target.checked);
      }

      return {
        cellProps,
        innerValue,
        boxClass,
        boxStyle,
        handleChange,
      };
    },

    // [Tăng cường component] Xem chú thích chi tiết:JVxeComponent.Enhanced
    enhanced: {
      switches: {
        visible: true,
      },
      getValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // Xử lý customValue
        if (isArray(originColumn.value.customValue)) {
          let customValue = getCustomValue(originColumn.value);
          if (isBoolean(value)) {
            return value ? customValue[0] : customValue[1];
          } else {
            return value;
          }
        } else {
          return value;
        }
      },
      setValue(value, ctx) {
        let { context } = ctx!;
        let { originColumn } = context;
        // Kiểm tra xem có đặt customValue (giá trị tùy chỉnh) hay không
        if (isArray(originColumn.value.customValue)) {
          let customValue = getCustomValue(originColumn.value);
          return neverNull(value).toString() === customValue[0].toString();
        } else {
          return !!value;
        }
      },
      createValue(_defaultValue, ctx) {
        let { context } = ctx!;
        let {
          column: { params: col },
        } = context;
        if (isArray(col.customValue)) {
          let customValue = getCustomValue(col);
          return col.defaultChecked ? customValue[0] : customValue[1];
        } else {
          return !!col.defaultChecked;
        }
      },
    } as JVxeComponent.EnhancedPartial,
  });

  function neverNull(value, def?) {
    return value == null ? neverNull(def, '') : value;
  }

  function getCustomValue(col) {
    let customTrue = neverNull(col.customValue[0], true);
    let customFalse = neverNull(col.customValue[1], false);
    return [customTrue, customFalse];
  }
</script>

<style lang="less">
  // Tắt animation, tránh lỗi khi gán giá trị động lúc cuộn
  .j-vxe-checkbox.no-animation {
    .ant-checkbox-inner,
    .ant-checkbox-inner::after {
      transition: none !important;
    }
  }
</style>
