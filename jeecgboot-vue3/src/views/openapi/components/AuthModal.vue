<template>
  <!--  <j-modal :title="title" :width="width" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="Đóng">-->
  <div style="position: relative;">
    <a-modal
      v-model:open="authDrawerOpen"
      class="custom-class"
      root-class-name="root-class-name"
      :root-style="{ color: 'blue' }"
      :body-style="{ padding: '20px' }"
      style="color: red"
      :title="title"
      :width="600"
      @after-open-change="authDrawerOpenChange"
      @ok="handleOk"
    >
      <AuthForm ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false"></AuthForm>
    </a-modal>
  </div>
  <!--  </j-modal>-->
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import AuthForm from './AuthForm.vue';
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';

  const title = ref<string>('');
  const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  const authDrawerOpen = ref(false);
  const authDrawerOpenChange = (val: any) => {
    if(!val)
      registerForm.value.cleanData()
  };

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
   * Ủy quyền
   * @param record
   */
  function edit(record) {
    title.value = disableSubmit.value ? 'Chi tiết' : 'Ủy quyền';
    visible.value = true;
    authDrawerOpen.value = true;
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
    authDrawerOpen.value = false;
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
