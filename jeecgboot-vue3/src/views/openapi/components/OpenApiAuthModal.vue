<template>
  <j-modal :title="title" :width="width" :maxHeight="200" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="Đóng">
    <OpenApiAuthForm ref="registerForm" @ok="submitCallback" :title="title" :formDisabled="disableSubmit" :formBpm="false"></OpenApiAuthForm>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import OpenApiAuthForm from './OpenApiAuthForm.vue'
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  
  const title = ref<string>('');
  const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  /**
   * Thêm mới
   */
  function add() {
    title.value = 'Thêm mới';
    visible.value = true;
    nextTick(() => {
      registerForm.value.add();
    });
  }
  
  /**
   * Chỉnh sửa
   * @param record
   */
  function edit(record) {
    title.value = disableSubmit.value ? 'Chi tiết' : 'Chỉnh sửa';
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record);
    });
  }
  
  /**
   * Sự kiện nhấp nút xác nhận
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * Sự kiện callback khi lưu form
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * Sự kiện callback nút hủy
   */
  function handleCancel() {
    visible.value = false;
  }

  defineExpose({
    add,
    edit,
    disableSubmit,
  });
</script>

<style lang="less">
  /** Kiểu ẩn — nút xác nhận của modal */
  .jee-hidden {
    display: none !important;
  }
</style>
<style lang="less" scoped></style>
