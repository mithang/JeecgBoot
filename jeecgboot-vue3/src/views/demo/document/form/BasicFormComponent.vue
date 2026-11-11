<!-- Thao tác giá trị biểu mẫu -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { schemas } from './example.data';

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm, { getFieldsValue, setFieldsValue, resetFields, validate }] = useForm({
    schemas: schemas,
    labelWidth: '150px',
    //Ẩn nút thao tác
    showActionButtonGroup: false,
    //Mặc định focus vào phần tử đầu tiên, chỉ hỗ trợinput
    autoFocusFirstItem: true,
  });
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
