<template>
  <a-popover
    trigger="contextmenu"
    v-model:open="visible"
    :overlayClassName="`${prefixCls}-popover`"
    :getPopupContainer="getPopupContainer"
    :placement="position"
  >
    <template #title>
      <span :class="title ? 'title' : 'emptyTitle'">{{ title }}</span>
      <span style="float: right" title="Đóng">
        <Icon icon="ant-design:close-outlined" @click="visible = false" />
      </span>
    </template>
    <template #content>
      <a-textarea ref="textareaRef" :value="innerValue" :disabled="disabled" :style="textareaStyle" v-bind="attrs" @input="onInputChange" @blur="onInputBlur" />
    </template>
    <a-input :class="`${prefixCls}-input`" :value="innerValue" :disabled="disabled" v-bind="attrs" @change="onInputChange" @blur="onInputBlur">
      <template #suffix>
        <Icon icon="ant-design:fullscreen-outlined" @click.stop="onShowPopup" />
      </template>
    </a-input>
  </a-popover>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref, watch } from 'vue';
  import Icon from '/@/components/Icon/src/Icon.vue';
  import { useAttrs } from '/@/hooks/core/useAttrs';
  import { propTypes } from '/@/utils/propTypes';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-input-popup');
  const props = defineProps({
    // v-model:value
    value: propTypes.string.def(''),
    title: propTypes.string.def(''),
    // Vị trí hiển thị hộp bật lên
    position: propTypes.string.def('right'),
    width: propTypes.number.def(300),
    height: propTypes.number.def(150),
    disabled: propTypes.bool.def(false),
    // Phần tử mà hộp thoại gắn vàoID
    popContainer: propTypes.oneOfType([propTypes.string, propTypes.func]).def(''),
  });
  const attrs = useAttrs();
  const emit = defineEmits(['change', 'update:value', 'blur']);

  const visible = ref<boolean>(false);
  const innerValue = ref<string>('');
  // textarea refĐối tượng
  const textareaRef = ref();
  // textarea Kiểu
  const textareaStyle = computed(() => ({
    height: `${props.height}px`,
    width: `${props.width}px`,
  }));

  watch(
    () => props.value,
    (value) => {
      if (value && value.length > 0) {
        innerValue.value = value;
      }
    },
    { immediate: true }
  );

  function onInputChange(event) {
    innerValue.value = event.target.value;
    emitValue(innerValue.value);
  }

  async function onShowPopup() {
    visible.value = true;
    await nextTick();
    textareaRef.value?.focus();
  }

  // Lấy phần tử mà popup gắn vào
  function getPopupContainer(node) {
    if (!props.popContainer) {
      return node?.parentNode;
    } else if (typeof props.popContainer === 'function') {
      return props.popContainer(node);
    } else {
      return document.getElementById(props.popContainer);
    }
  }

  function emitValue(value) {
    emit('change', value);
    emit('update:value', value);
  }

  const onInputBlur = (event) => {
    emit('blur', event);
  }
</script>

<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-j-input-popup';

  .@{prefix-cls} {
    &-popover {
      // Giải thích logic mã: 【TV360X-144]Khi title không có trong component văn bản nhiều dòng của jVxetable thì bỏ đường kẻ thừa
      .ant-popover-title:has(.emptyTitle) {
        border-bottom: none;
      }
    }

    &-input {
      .app-iconify {
        cursor: pointer;
        color: #666666;
        transition: color 0.3s;

        &:hover {
          color: black;
        }
      }
    }
  }
</style>
