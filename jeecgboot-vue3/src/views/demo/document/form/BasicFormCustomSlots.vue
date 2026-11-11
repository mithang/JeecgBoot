<!-- Slot -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit">
      <!--  #nametương ứng với formSchemasslot(name)Slot    -->
      <template #name="{ model, field }">
        <JInput v-model:value="model[field]" />
      </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  //Import component tùy chỉnh CustomDemo
  import JInput from "/@/components/Form/src/jeecg/components/JInput.vue";

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'name',
      label: 'Họ tên',
      component: 'Input',
      slot:'name'
    },
    {
      field: 'phone',
      label: 'Thông tin liên hệ',
      component: 'Input',
    },
    {
      field: 'feedback',
      label: 'Phản hồi vấn đề',
      component: 'InputTextArea',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    showResetButton: false,
    labelWidth: '150px',
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    actionColOptions: { span: 17 },
  });

  /**
   * Thông tin gửi
   */
  function handleSubmit(values) {
    console.log("values::",values);
  }
</script>

<style scoped>
  .font-color {
    font-size: 13px;
    color: #a1a1a1;
    margin-bottom: 5px;
  }
</style>
