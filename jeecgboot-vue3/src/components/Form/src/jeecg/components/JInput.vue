<template>
  <a-input v-bind="getBindValue" v-model:value="showText" @input="backValue"></a-input>
</template>

<script lang="ts">
  import { defineComponent, PropType, ref, watchEffect, unref, watch, computed } from 'vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { JInputTypeEnum } from '/@/enums/jeecgEnum.ts';
  import { omit } from 'lodash-es';

  export default defineComponent({
    name: 'JInput',
    inheritAttrs: false,
    props: {
      value: propTypes.string.def(''),
      type: propTypes.string.def(JInputTypeEnum.JINPUT_QUERY_LIKE),
      placeholder: propTypes.string.def(''),
      trim: propTypes.bool.def(false),
      class: propTypes.string,
      style: propTypes.object,
    },
    emits: ['change', 'update:value'],
    setup(props, { emit }) {
      const attrs = useAttrs();
      //Giá trị biểu mẫu
      const showText = ref('');
      //Thuộc tính liên kết
      const getBindValue = computed(() => {
        return omit(Object.assign({}, unref(props), unref(attrs)), ['value']);
      });
      //Lắng nghe thay đổi loại
      watch(
        () => props.type,
        (val) => {
          val && backValue({ target: { value: unref(showText) } });
        }
      );
      //Lắng nghe thay đổi của value
      watch(
        () => props.value,
        () => {
          initVal();
        },
        { immediate: true }
      );

      /**
       * Khởi tạo giá trị số
       */
      function initVal() {
        if (!props.value) {
          showText.value = '';
        } else {
          let text = props.value;
          switch (props.type) {
            case JInputTypeEnum.JINPUT_QUERY_LIKE:
              //Sửa lỗi giá trị tham số route truyền vào ô jinput bị cắt mất một ký tự ở đầu và cuối #1336
              if (text.indexOf('*') != -1) {
                text = text.substring(1, text.length - 1);
              }
              break;
            case JInputTypeEnum.JINPUT_QUERY_NE:
              text = text.substring(1);
              break;
            case JInputTypeEnum.JINPUT_QUERY_GE:
              text = text.substring(2);
              break;
            case JInputTypeEnum.JINPUT_QUERY_LE:
              text = text.substring(2);
              break;
            default:
          }
          showText.value = text;
        }
      }

      /**
       * Giá trị trả về
       */
      function backValue(e) {
        let text = e?.target?.value ?? '';
        if (text && !!props.trim) {
          text = text.trim();
        }
        switch (props.type) {
          case JInputTypeEnum.JINPUT_QUERY_LIKE:
            // Nếu nhập bắt đầu bằng ! Bắt đầu bằng, nghĩa là dùng truy vấn NE (không bằng), không ghép nữa * đi qua LIKE
            if (!text.startsWith('!')) {
              text = '*' + text + '*';
            }
            break;
          case JInputTypeEnum.JINPUT_QUERY_NE:
            text = '!' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_GE:
            text = '>=' + text;
            break;
          case JInputTypeEnum.JINPUT_QUERY_LE:
            text = '<=' + text;
            break;
          default:
        }
        emit('change', text);
        emit('update:value', text);
      }

      return { showText, attrs, getBindValue, backValue };
    },
  });
</script>

<style scoped></style>
