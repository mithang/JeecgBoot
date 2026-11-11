<!-- Kiểm tra biểu mẫu -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Khách đến thăm',
      component: 'Input',
      //Tự động kích hoạt kiểm chứng, kiểu boolean
      required: true,
      //Khi kiểm chứng không cộng thêm tiêu đề
      rulesMessageJoinLabel: false,
    },
    {
      field: 'accessed',
      label: 'Ngày đến thăm',
      component: 'DatePicker',
      //Hỗ trợ lấy giá trị hiện tại để xác định kích hoạt, values đại diện cho giá trị của biểu mẫu hiện tại
      required: ({ values }) => {
        return !values.accessed;
      },
    },
    {
      field: 'phone',
      label: 'Số điện thoại của khách đến thăm',
      component: 'Input',
      //Hỗ trợ pattern biểu thức chính quy và thông báo gợi ý tùy chỉnh message
      rules: [{ required: false, message: 'Vui lòng nhập đúng số điện thoại', pattern: /^1[3456789]\d{9}$/ }],
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
   * Sự kiện gửi
   */
  function handleSubmit(values: any) {}
</script>

<style scoped></style>
