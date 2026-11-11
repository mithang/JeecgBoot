<!-- Gợi ý tiêu đề trường và tiền tố -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'month',
      label: 'Tháng hiện tại',
      component: 'Input',
      suffix: 'tháng',
    },
    {
      field: 'lateNumber',
      label: 'Số lần đi muộn',
      component: 'InputNumber',
      //Thông tin trợ giúp: Có thể trả về trực tiếpString(helpMessage:"Số lần đi muộn"), cũng có thể lấy giá trị biểu mẫu để điền động
      helpMessage: ({ values }) => {
        return 'Số lần đi muộn hiện tại' + values.lateNumber + ', Khấu trừ' + values.lateNumber * 50 + 'đồng';
      },
      defaultValue: 0,
    },
    {
      field: 'lateReason',
      label: 'Lý do đi muộn',
      component: 'Input',
      helpMessage: 'Lý do gì khiến đi làm muộn',
      //Thuộc tính nhắc nhở tùy chỉnh, cần dùng kết hợp với helpMessage
      helpComponentProps: {
        maxWidth: '200px',
        color: '#66CCFF',
      },
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

<style scoped></style>
