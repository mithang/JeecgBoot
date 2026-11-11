<!-- Cách dùng cơ bản -->
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
      //Tên tiêu đề
      label: 'Tên người dùng(phía sau sẽ ẩn theo độ dài được định nghĩa bởi labelLength)',
      //Trường
      field: 'username',
      //Component
      component: 'Input',
      //Chiều rộng tiêu đề,Hỗ trợ số và chuỗi
      labelWidth: 150,
      //Độ dài tiêu đề, vượt quá số ký tự sẽ bị ẩn
      labelLength: 3,
    },
    {
      label: 'Mật khẩu',
      field: 'password',
      //Tên tiêu đề phụ (nằm sau tiêu đề chính)
      subLabel: '(Gồm chữ số và chữ cái)',
      component: 'InputPassword',
      labelWidth: '150px',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   * useForm là cốt lõi của toàn bộ framework, dùng để render biểu mẫu, bên trong đóng gói rất nhiều phương thức công khai;
   * Hỗ trợ (schemas: Render cột biểu mẫu, autoSubmitOnEnter: nhấn Enter để gửi,submitButtonOptions: các phương thức như tùy chỉnh văn bản và icon của nút);
   * Nền tảng thông qua việc đóng gói này đã đơn giản hóa mã, hỗ trợ mở rộng tùy chỉnh;
   */
  const [registerForm] = useForm({
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    //Nhấn Enter để gửi
    autoSubmitOnEnter: true,
    //Không hiển thị nút đặt lại
    showResetButton: false,
    //Tùy chỉnh văn bản và biểu tượng của nút gửi
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    //Tỷ lệ cột truy vấn: 24 đại diện cho một dòng, phạm vi giá trị 0-24
    actionColOptions: { span: 17 },
  });

  /**
   * Giá trị value của nút gửi khi nhấp
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Dữ liệu nút gửi::::', values);
  }
</script>

<style scoped></style>
