<!-- Nút thao tác -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- Có thể đặt thuộc tính trong userForm thông qua setProps -->
    <!--  showActionButtonGroup Hiển thị hoặc ẩn nút tìm kiếm, đặt lại  -->
    <a-button @click="setProps({ showActionButtonGroup: false })" class="mr-2"> Ẩn nút thao tác </a-button>
    <a-button @click="setProps({ showActionButtonGroup: true })" class="mr-2"> Hiển thị nút thao tác </a-button>
    <!--  showActionButtonGroup Hiển thị hoặc ẩn nút đặt lại  -->
    <a-button @click="setProps({ showResetButton: false })" class="mr-2"> Ẩn nút đặt lại </a-button>
    <a-button @click="setProps({ showResetButton: true })" class="mr-2"> Hiển thị nút đặt lại </a-button>
    <!--  showActionButtonGroup Hiển thị hoặc ẩn nút tìm kiếm  -->
    <a-button @click="setProps({ showSubmitButton: false })" class="mr-2"> Ẩn nút tìm kiếm </a-button>
    <a-button @click="setProps({ showSubmitButton: true })" class="mr-2"> Hiển thị nút tìm kiếm </a-button>
  </div>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 50px; margin-left: 50px" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';

  /**
   * BasicFormĐăng ký liên kết;
   * setPropsPhương thức có thể đặt động các thuộc tính trong useForm
   */
  const [registerForm, { setProps }] = useForm({
    //Văn bản và biểu tượng của nút tìm kiếm tùy chỉnh
    submitButtonOptions: { text: 'Truy vấn', preIcon: '' },
    //Văn bản và biểu tượng của nút đặt lại tùy chỉnh
    resetButtonOptions: { text: 'Đặt lại', preIcon: '' },
    //Vị trí của nút thao tác
    actionColOptions: { span: 17 },
    //Sự kiện tùy chỉnh của nút gửi
    submitFunc: customSubmitFunc,
    //Thời gian tùy chỉnh của nút đặt lại
    resetFunc: customSubmitFunc,
    //Hiển thị nút thao tác
    showActionButtonGroup: true,
  });

  /**
   * Sự kiện nhấp nút tìm kiếm
   */
  async function customSubmitFunc() {
    console.log('Sự kiện nhấn nút tìm kiếm, xử lý logic của nút tìm kiếm tại đây');
  }

  /**
   * Sự kiện nhấp nút đặt lại
   */
  async function customResetFunc() {
    console.log('Sự kiện nhấn nút đặt lại, xử lý logic của nút đặt lại tại đây');
  }

  /**
   * Giá trị value của nút gửi khi nhấp
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Dữ liệu nút gửi::::', values);
  }
</script>

<style scoped></style>
