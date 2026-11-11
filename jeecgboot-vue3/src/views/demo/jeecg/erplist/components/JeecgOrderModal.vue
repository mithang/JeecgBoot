<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="700">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../erplist.data';
  import { saveOrUpdate } from '../erplist.api';
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Cài đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

  //Sự kiện submit biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate.value);
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
