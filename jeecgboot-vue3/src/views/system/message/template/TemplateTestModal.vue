<template>
  <BasicModal @register="registerModal" title="Gửi kiểm tra" :width="800" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { sendTestFormSchemas } from './template.data';
  import { sendMessageTest } from './template.api';

  // Khai báo emits
  const emit = defineEmits(['register']);
  // Đăng ký form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: sendTestFormSchemas,
    showActionButtonGroup: false,
  });
  // Đăng ký modal
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    await setFieldsValue({ ...unref(data.record) });
  });

  //Sự kiện gửi biểu mẫu
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Gửi biểu mẫu
      await sendMessageTest(values);
      //Đóng cửa sổ bật lên
      closeModal();
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
