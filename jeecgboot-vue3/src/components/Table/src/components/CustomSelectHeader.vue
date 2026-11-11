<!-- Cột chọn tùy chỉnh, phần triển khai tiêu đề bảng -->
<template>
  <template v-if="isRadio">
    <!-- radioKhông có chọn tất cả nên đặt một thẻ trống -->
    <span></span>
  </template>
  <template v-else>
    <template v-if="hideSelectAll">
      <span></span>
    </template>
    <a-checkbox :disabled="disabled" v-else :checked="checked" :indeterminate="isHalf" @update:checked="onChange" />
  </template>
</template>
<script setup lang="ts">
  import { computed } from 'vue';

  const props = defineProps({
    isRadio: {
      type: Boolean,
      required: true,
    },
    selectedLength: {
      type: Number,
      required: true,
    },
    // Số mục trang hiện tại
    pageSize: {
      type: Number,
      required: true,
    },
    hideSelectAll: {
      type: Boolean,
      default: false,
    },
    // Giải thích logic mã: 【QQYUN-6774] Giải quyết vấn đề sau khi vô hiệu hóa checkbox thì chọn tất cả vẫn có thể tích chọn
    disabled: {
      type: Boolean,
      required: true,
    },
  });
  const emit = defineEmits(['select-all']);

  // Có chọn tất cả hay không
  const checked = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength >= props.pageSize;
  });

  // Có chọn một nửa hay không
  const isHalf = computed(() => {
    if (props.isRadio) {
      return false;
    }
    return props.selectedLength > 0 && props.selectedLength < props.pageSize;
  });

  function onChange(checked: boolean) {
    emit('select-all', checked);
  }
</script>

<style scoped lang="scss"></style>
