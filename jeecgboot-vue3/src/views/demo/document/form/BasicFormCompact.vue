<!-- Thao tác vô hiệu hóa biểu mẫu -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- Có thể đặt thuộc tính trong userForm thông qua setProps -->
    <!-- Kích thước biểu mẫu, mặc định là default   -->
    <a-button @click="setProps({ size: 'large' })" class="mr-2"> Đổi kích thước thành lớn nhất </a-button>
    <a-button @click="setProps({ size: 'default' })" class="mr-2"> Khôi phục kích thước </a-button>
    <a-button @click="setProps({ size: 'small' })" class="mr-2"> Đổi kích thước thành nhỏ nhất </a-button>
    <!--  disabledVô hiệu hóa biểu mẫu  -->
    <a-button @click="setProps({ disabled: true })" class="mr-2"> Vô hiệu hóa biểu mẫu </a-button>
    <a-button @click="setProps({ disabled: false })" class="mr-2"> Bỏ vô hiệu hóa </a-button>
    <!--  compact Có phải biểu mẫu gọn không  -->
    <a-button @click="setProps({ compact: true })" class="mr-2"> Biểu mẫu gọn </a-button>
    <a-button @click="setProps({ compact: false })" class="mr-2"> Khôi phục khoảng cách bình thường </a-button>
  </div>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Khách đến thăm',
      component: 'Input',
    },
    {
      field: 'accessed',
      label: 'Ngày đến thăm',
      component: 'DatePicker',
    },
    {
      field: 'phone',
      label: 'Số điện thoại của khách đến thăm',
      component: 'Input',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   * setPropsPhương thức có thể đặt động các thuộc tính trong useForm
   */
  const [registerForm, { setProps }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Ẩn nút thao tác
    showActionButtonGroup: false,
    //Mặc định focus vào phần tử đầu tiên, chỉ hỗ trợinput
    autoFocusFirstItem: true,
  });
</script>

<style scoped></style>
