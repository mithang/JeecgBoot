<!-- Thao tác vô hiệu hóa biểu mẫu -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <!-- all Kích hoạt hoặc xóa tất cả xác thực, visitor chỉ kích hoạt hoặc xóa xác thực của nhân viên đến thăm -->
    <a-button @click="triggerFormRule('all')" class="mr-2"> Kích hoạt kiểm tra biểu mẫu </a-button>
    <a-button @click="cancelFormRule('all')" class="mr-2"> Xóa xác thực biểu mẫu </a-button>
    <a-button @click="triggerFormRule('visitor')" class="mr-2"> Chỉ kiểm chứng nhân viên đến thăm </a-button>
    <a-button @click="cancelFormRule('visitor')" class="mr-2"> Chỉ xóa xác thực của khách đến thăm </a-button>
  </div>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" />
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
      required: true,
    },
    {
      field: 'accessed',
      label: 'Ngày đến thăm',
      component: 'DatePicker',
      required: true,
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
   * clearValidate Xóa tất cả kiểm chứng, hỗ trợ hủy kiểm chứng một vài trường như clearValidate(['visitor',...])
   * validate Kiểm chứng tất cả,Hỗ trợ kiểm chứng một vài trường trong đó,validate(['visitor',...])
   * validateFields Chỉ hỗ trợ kiểm chứng một vài trường trong số đó, ví dụvalidateFields(['visitor',...])
   */
  const [registerForm, { clearValidate, validateFields, validate }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Ẩn nút thao tác
    showActionButtonGroup: false,
    //Mặc định focus vào phần tử đầu tiên, chỉ hỗ trợinput
    autoFocusFirstItem: true,
  });

  /**
   * Kích hoạt kiểm tra biểu mẫu
   * @param type all Tất cả kiểm chứng: visitor chỉ kiểm chứng người đến thăm
   */
  async function triggerFormRule(type) {
    if (type == 'all') {
      //Kích hoạt tất cả xác thực
      await validate();
    } else {
      //Kích hoạt kiểm chứng nhân viên đến thăm
      //visitor Trường field tương ứng trong formSchemas của người đến thăm
      await validateFields(['visitor']);
    }
  }

  /**
   * Kích hoạt kiểm tra biểu mẫu
   * @param type all Tất cả kiểm chứng: visitor chỉ kiểm chứng người đến thăm
   */
  async function cancelFormRule(type) {
    if (type == 'all') {
      //Hủy tất cả kiểm chứng
      await clearValidate();
    } else {
      //Chỉ hủy kiểm chứng của khách đến thăm
      //visitor Trường field tương ứng trong formSchemas của người đến thăm
      await clearValidate(['visitor']);
    }
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
