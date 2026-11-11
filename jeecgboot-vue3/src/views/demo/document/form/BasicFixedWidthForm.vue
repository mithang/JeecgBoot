<!-- Cố định chiều rộng tiêu đề label -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 20px" />
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
    showResetButton: false,
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    actionColOptions: { span: 17 },
    //Dùng labelWidth để điều khiển độ rộng tiêu đề
    labelWidth: '150px',
    //Sử dụng thuộc tính kiểu của labelCol để kiểm soát độ rộng tiêu đề
    labelCol: { style: { width: '150px' } },
    //Cách căn chỉnh tiêu đề (left:Căn trái, right: căn phải), mặc định căn phải
    labelAlign: 'right',
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
