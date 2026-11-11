<!-- Xác thực biểu mẫu động -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" @submit="handleSubmit" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { duplicateCheck } from '/@/views/system/user/user.api';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'visitor',
      label: 'Khách đến thăm',
      component: 'Input',
      //Tự động kích hoạt kiểm chứng, kiểu boolean
      required: true,
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
      //Regex tùy chỉnh động,values: Tất cả giá trị của biểu mẫu hiện tại
      dynamicRules: ({ values }) => {
        //Cầnreturn
        return [
          {
            //Mặc định bật kiểm tra biểu mẫu
            required: true,
            // value Giá trị số điện thoại hiện đang nhập
            validator: (_, value) => {
              //Cần return một đối tượng Promise
              return new Promise((resolve, reject) => {
                if (!value) {
                  reject('Vui lòng nhập số điện thoại!');
                }
                //Kiểm tra số điện thoại có đúng không
                let reg = /^1[3456789]\d{9}$/;
                if (!reg.test(value)) {
                  reject('Vui lòng nhập đúng số điện thoại!');
                }
                resolve();
              });
            },
          },
        ];
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

  /**
   * Sự kiện gửi
   */
  function handleSubmit(values: any) {}
</script>

<style scoped></style>
