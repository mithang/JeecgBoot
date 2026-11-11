<template>
  <div :id="formItemId" class="j-form-item-middleware">
    <slot></slot>
  </div>
</template>

<script setup>
  import { ref } from 'vue';
  // Giải thích logic mã: 【TV360X-1511] blur không có hiệu lực
  const formItemId = ref(null);
  const props = defineProps(['formName', 'fieldName', 'source']);
  if (props.formName && props.fieldName && props.source !== 'table-query') {
    formItemId.value = `${props.formName}_${props.fieldName}`;
  }
</script>

<style lang="less" scoped>
  // Giải thích logic mã: 【TV360X-1253] Vùng truy vấn sinh mã và component thêm mới không lấp đầy
  div > :deep(.ant-picker) {
    width: 100%;
  }
</style>
