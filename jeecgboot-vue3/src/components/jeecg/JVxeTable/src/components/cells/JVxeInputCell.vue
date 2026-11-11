<template>
  <a-input ref="input" :value="innerValue" v-bind="cellProps" @blur="handleBlur" @change="handleChange" />
</template>

<script lang="ts">
  import { computed, defineComponent } from 'vue';
  import { isString } from '/@/utils/is';
  import { JVxeComponent, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useJVxeComponent, useJVxeCompProps } from '/@/components/jeecg/JVxeTable/hooks';

  const NumberRegExp = /^-?\d+\.?\d*$/;
  export default defineComponent({
    name: 'JVxeInputCell',
    props: useJVxeCompProps(),
    setup(props: JVxeComponent.Props) {
      const { innerValue, cellProps, handleChangeCommon, handleBlurCommon } = useJVxeComponent(props);

      // Có phải là ô nhập kiểu số không
      const isNumberType = props.type === JVxeTypes.inputNumber;

      /**
       * Tính thuộc tính giới hạn của kiểu số
       * Bao gồm cấu hình giá trị lớn nhất, giá trị nhỏ nhất và độ chính xác, dùng cho component nhập liệu và logic định dạng
       */
      const numberProps = computed<{
        max?: number,
        min?: number,
        precision?: number,
      }>(() => {
        const {max, min, precision} = cellProps.value as Recordable;
        const nProps: Recordable = {};
        // Giá trị lớn nhất
        if (typeof max === 'number') {
          nProps.max = max;
        }
        // Giá trị nhỏ nhất
        if (typeof min === 'number') {
          nProps.min = min;
        }
        // Độ chính xác số, giữ lại số chữ số thập phân
        if (typeof precision === 'number') {
          nProps.precision = precision;
        }
        return nProps;
      });

      /** Xử lý sự kiện change */
      function handleChange(event) {
        let { target } = event;
        let { value, selectionStart } = target;
        let change = true;
        if (isNumberType) {
          // Kiểm tra xem giá trị nhập vào có khớp biểu thức chính quy số hay không, không khớp thì khôi phục lại
          if (!NumberRegExp.test(value) && value !== '' && value !== '-') {
            change = false;
            value = innerValue.value;
            target.value = value || '';
            if (typeof selectionStart === 'number') {
              target.selectionStart = selectionStart - 1;
              target.selectionEnd = selectionStart - 1;
            }
          } else {
            // Ví dụ:41.1 -> 41.10, 100.1 -> 100.10 Không thực thi hàm handleChangeCommon.
            if (value.indexOf('.') != -1) {
              const result = value.split('.').pop();
              if (result && result.length >= 2 && result.substr(-1) === '0') {
                change = false;
                innerValue.value = value;
              }
            }
          }
        }
        // Kích hoạt sự kiện, lưu giá trị đã nhập
        if (change) {
          handleChangeCommon(value);
        }
      }

      /** Xử lý sự kiện blur mất focus */
      function handleBlur(event) {
        let { target } = event;
        // Kiểm tra xem giá trị nhập vào có khớp biểu thức chính quy số hay không, không khớp thì đặt thành rỗng
        if (isNumberType) {
          if (!NumberRegExp.test(target.value)) {
            target.value = '';
          } else {
            const parsedValue = Number.parseFloat(target.value);
            const clampedValue = clampNumber(parsedValue);
            target.value = applyPrecision(clampedValue);
          }
        }
        handleChangeCommon(target.value, true);
        handleBlurCommon(target.value);
      }

      /**
       * Giới hạn giá trị theo giá trị nhỏ nhất và lớn nhất
       * @param value Giá trị cần cắt
       */
      function clampNumber(value: number): number {
        let result = value;
        const { max, min } = numberProps.value;
        // Áp dụng giới hạn giá trị tối thiểu
        if (typeof min === 'number') {
          result = Math.max(min, result);
        }
        // Áp dụng giới hạn giá trị tối đa
        if (typeof max === 'number') {
          result = Math.min(max, result);
        }
        return result;
      }

      /**
       * Định dạng giá trị số theo độ chính xác đã cấu hình
       * @param value Giá trị cần định dạng
       */
      function applyPrecision(value: number): number {
        const { precision } = numberProps.value;
        if (typeof precision === 'number') {
          return Number(value.toFixed(precision));
        }
        return value;
      }

      return {
        innerValue,
        cellProps,
        isNumberType,
        handleChange,
        handleBlur,
      };
    },
    enhanced: {
      installOptions: {
        autofocus: '.ant-input',
      },
      getValue(value, ctx) {
        if (ctx?.props?.type === JVxeTypes.inputNumber && isString(value)) {
          if (NumberRegExp.test(value)) {
            // 【issues/I5IHN7] sửa lỗi không thể nhập dấu thập phânbug
            if (/\.0*$/.test(value)) {
              return value;
            }
            return Number.parseFloat(value);
          }
        }
        return value;
      },
    } as JVxeComponent.EnhancedPartial,
  });
</script>
