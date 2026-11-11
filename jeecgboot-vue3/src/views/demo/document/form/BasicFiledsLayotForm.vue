<!-- Bố cục tiêu đề và trường -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin: 20px auto"/>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      label: 'Họ tên',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'Tuổi',
      field: 'password',
      component: 'InputNumber',
    },
    {
      label: 'Ngày sinh',
      field: 'birthday',
      component: 'DatePicker',
    },
    {
      label: 'Ảnh đại diện',
      field: 'avatar',
      component: 'JImageUpload',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    showActionButtonGroup: false,
    actionColOptions: { span: 12 },
    //Kiểm soát tỷ lệ chiều rộng tiêu đề
    labelCol: {
      xs: 2,
      sm: 2,
      md: 2,
      lg: 9,
      xl: 3,
      xxl: 2,
    },
    //Kiểm soát tỷ lệ chiều rộng component
    wrapperCol: {
      xs: 15,
      sm: 14,
      md: 16,
      lg: 17,
      xl: 19,
      xxl: 20,
    },
  });

  /**
   * Giá trị value của nút gửi khi nhấp
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Dữ liệu nút gửi::::', values);
  }
</script>

<style scoped>
  /** Kiểu ô nhập thời gian và số */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }
</style>
