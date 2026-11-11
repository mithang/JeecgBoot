<template>
  <BasicModal @register="registerModal" :title="title" :width="800" :maxHeight="650" v-bind="$attrs" @ok="onSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { formSchemas } from './template.data';
  import { saveOrUpdate } from './template.api';

  // Khai báo emits
  const emit = defineEmits(['success', 'register']);
  const title = ref<string>('');
  const isUpdate = ref<boolean>(false);
  // Đăng ký form
  // Giải thích logic mã: [VUEN-2807]Thêm chức năng xem cho mẫu tin nhắn------------
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema, setProps }] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
    baseRowStyle: {
      marginTop: '10px',
    },
    labelCol: {
      span: 5,
    },
    wrapperCol: {
      span: 17,
    },
  });
  // Đăng ký modal
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({confirmLoading: false,showCancelBtn:!!data?.showFooter,showOkBtn:!!data?.showFooter});
    isUpdate.value = unref(data.isUpdate);
    title.value = unref(data.title);
    await resetFields();
    await setFieldsValue({ ...data.record });
    // Khi ẩn phần chân thì vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter })
  });

  //Sự kiện gửi biểu mẫu
  async function onSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
