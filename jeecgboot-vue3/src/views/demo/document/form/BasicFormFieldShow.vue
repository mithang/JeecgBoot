<!-- Hiển thị và ẩn trường -->
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
      field: 'id',
      label: 'Mã',
      component: 'Input',
      //Ẩn id, điều khiển bằng css, sẽ không xóa dom (hỗ trợ kiểu boolean true và false. Hỗ trợ phán đoán giá trị động, xem chi tiết tại ifShow)
      show: false,
    },
    {
      field: 'evaluate',
      label: 'Đánh giá tổng thể về công ty',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: 'Hài lòng', value: '0' },
          { label: 'Không hài lòng', value: '1' },
        ],
      },
      defaultValue: '0',
    },
    {
      field: 'describe',
      label: 'Giải thích lý do không hài lòng',
      component: 'InputTextArea',
      //ifShowNhất quán với thuộc tính show, dùng một trong hai là được, values đại diện cho giá trị của biểu mẫu hiện tại, js điều khiển, sẽ xóa dom
      ifShow: ({ values }) => {
        return values.evaluate == '1';
      },
    },
    {
      field: 'satisfiedLevel',
      label: 'Mức độ hài lòng',
      component: 'Slider',
      componentProps: {
        tipFormatter: (value) => {
          return value + '%';
        },
      },
      //Vô hiệu hóa động, values đại diện cho giá trị của biểu mẫu hiện tại, trả về true hoặcfalse
      dynamicDisabled: ({ values }) => {
        return values.evaluate == '1';
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
