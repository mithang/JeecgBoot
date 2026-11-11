<!-- Thuộc tính control -->
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
      label: 'Tên nhân viên',
      field: 'name',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      defaultValue: 'Trương Tam',
    },
    {
      label: 'Giới tính',
      field: 'sex',
      component: 'Select',
      //Điền thuộc tính của component
      componentProps: {
        options: [
          { label: 'Nam', value: 1 },
          { label: 'Nữ', value: 2 },
          { label: 'Không xác định', value: 3 },
        ],
      },
      //Giá trị mặc định
      defaultValue: 3,
    },
    {
      label: 'Tuổi',
      field: 'age',
      component: 'Input',
    },
    {
      label: 'Thời gian vào làm',
      subLabel: '( Không bắt buộc )',
      field: 'entryTime',
      component: 'TimePicker',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    labelWidth: '150px',
    showResetButton: false,
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    actionColOptions: { span: 17 },
  });
</script>

<style scoped></style>
