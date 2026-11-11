<!-- Slot -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px">
    <!--  #phonetương ứng với formSchemasslot(phone)Slot    -->
    <template #phone="{ model, field }">
      <!-- Nếu là component cần binding hai chiều, model là đối tượng biểu mẫu hiện tại, field là tên trường hiện tại  -->
      <a-input v-model:value="model[field]" placeholder="Vui lòng nhập số điện thoại" />
      <span class="font-color">Vui lòng nhập số điện thoại của bạn để chúng tôi tiện liên hệ</span>
    </template>
    <template #feedback="{ model, field }">
      <JEditor v-model:value="model[field]" placeholder="Vui lòng nhập phản hồi vấn đề" />
      <span class="font-color">Mong bạn trình bày kèm hình ảnh và chữ, để chúng tôi hiểu vấn đề và phản hồi kịp thời</span>
    </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'name',
      label: 'Họ tên',
      component: 'Input',
    },
    {
      field: 'phone',
      label: 'Thông tin liên hệ',
      component: 'Input',
      slot: 'phone',
    },
    {
      field: 'feedback',
      label: 'Phản hồi vấn đề',
      component: 'InputTextArea',
      slot: 'feedback',
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
</script>

<style scoped>
  .font-color {
    font-size: 13px;
    color: #a1a1a1;
    margin-bottom: 5px;
  }
</style>
