<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" name="AiOcrModal" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { schemas } from '../AiOcr.data';
  import {addOcr, editOcr} from "../AiOcr.api";
  const title = ref<string>('Thêm mới');
  const isUpdate = ref<boolean>();
  // Khai báo Emits
  const emit = defineEmits(['success', 'register']);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: schemas,
    showActionButtonGroup: false,
    layout: 'vertical',
    wrapperCol: { span: 24 },
  });

  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: true, bodyStyle:{ padding:'24px'} });
    isUpdate.value = !!data?.isUpdate;
    title.value = !unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    if(unref(isUpdate)){
      //Gán giá trị biểu mẫu
      await setFieldsValue({ ...data.record });
    }
  });

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      if(unref(isUpdate)){
        await editOcr(values);
      } else{
        await addOcr(values);
      }
      setModalProps({ confirmLoading: true });
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
:deep(.ant-modal-body){
  padding: 24px !important;
}
</style>
