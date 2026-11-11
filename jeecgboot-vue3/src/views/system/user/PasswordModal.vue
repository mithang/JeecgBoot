<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Đổi mật khẩu" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" name="PassWordModal" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formPasswordSchema } from './user.data';
  import { changePassword } from './user.api';
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formPasswordSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    //Gán giá trị cho biểu mẫu
    await setFieldsValue({ ...data });
  });
  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await changePassword(values);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
