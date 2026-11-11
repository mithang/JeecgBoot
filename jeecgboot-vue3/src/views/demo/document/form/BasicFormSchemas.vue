<!-- Thao tác với cấu hình schemas của biểu mẫu -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <a-button @click="updateFormSchemas" class="mr-2"> Cập nhật thuộc tính trường </a-button>
    <a-button @click="resetFormSchemas" class="mr-2"> Đặt lại thuộc tính trường </a-button>
  </div>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
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
      componentProps: {
        disabled: true,
      },
    },
    {
      field: 'accessed',
      label: 'Ngày đến thăm',
      component: 'DatePicker',
      ifShow: false,
    },
    {
      field: 'phone',
      label: 'Số điện thoại của khách đến thăm',
      component: 'Input',
      required: true,
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   * updateSchema Cập nhật thuộc tính trường, hỗ trợ mọi cấu hình trong schemas
   * updateSchema([{ field: 'visitor', componentProps: { disabled: false },}, ... ]);
   * resetSchema Đặt lại thuộc tính trường, hỗ trợ tất cả cấu hình trong schemas
   * resetSchema([{ field: 'visitor',label: 'Khách đến thăm',component: 'Input',},... ]);
   */
  const [registerForm, { updateSchema, resetSchema }] = useForm({
    schemas: formSchemas,
    //Ẩn nút thao tác
    showActionButtonGroup: false,
    labelWidth: '150px',
    //Mặc định focus vào phần tử đầu tiên, chỉ hỗ trợinput
    autoFocusFirstItem: true,
  });

  /**
   * Xóa cấu hình biểu mẫu
   */
  async function resetFormSchemas() {
    await resetSchema([
      {
        //Trường bắt buộc
        field: 'visitor',
        label: 'Khách đến thăm',
        component: 'Input',
      },
    ]);
  }

  /**
   * Cập nhật cấu hình biểu mẫu
   */
  async function updateFormSchemas() {
    //Hỗ trợ cập nhật mọi cấu hình trong schemas
    await updateSchema([
      {
        //Trường bắt buộc
        field: 'visitor',
        componentProps: {
          disabled: false,
        },
      },
      {
        field: 'accessed',
        ifShow: true,
      },
    ]);
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
