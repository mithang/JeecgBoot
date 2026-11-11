<template>
  <MarkDown v-bind="bindProps" @change="onChange" @get="onGetVditor" />
</template>

<script lang="ts">
  import { computed, defineComponent, watch, nextTick } from 'vue';
  import { MarkDown } from '/@/components/Markdown';
  import { propTypes } from '/@/utils/propTypes';
  import { Form } from 'ant-design-vue';
  export default defineComponent({
    name: 'JMarkdownEditor',
    // Không binding thuộc tính của attrs lên thẻ html
    inheritAttrs: false,
    components: { MarkDown },
    props: {
      value: propTypes.string.def(''),
      disabled: propTypes.bool.def(false),
    },
    emits: ['change', 'update:value'],
    setup(props, { emit, attrs }) {
      // markdown Thể hiện component
      let mdRef: any = null;
      // vditor Thể hiện component
      let vditorRef: any = null;
      // Gộp props và attrs
      const bindProps = computed(() => Object.assign({}, props, attrs));
      const formItemContext = Form.useInjectFormItemContext();
      // Tương đương với onMounted
      function onGetVditor(instance) {
        mdRef = instance;
        vditorRef = mdRef.getVditor();

        // Lắng nghe trạng thái vô hiệu hóa, chuyển đổi trạng thái vô hiệu hóa của trình chỉnh sửa
        watch(
          () => props.disabled,
          (disabled) => (disabled ? vditorRef.disabled() : vditorRef.enable()),
          { immediate: true }
        );
      }

      // value change Sự kiện
      function onChange(value) {
        emit('change', value);
        emit('update:value', value);
        // Giải thích logic mã: 【QQYUN-9110] kiểm chứng vẫn không biến mất khi component có giá trị
        nextTick(() => {
          formItemContext?.onFieldChange();
        });
      }

      return {
        bindProps,

        onChange,
        onGetVditor,
      };
    },
  });
</script>

<style lang="less" scoped></style>
