<!-- Thao tác giá trị biểu mẫu -->
<template>
  <div style="margin: 20px auto; text-align: center">
    <a-button @click="getFormValue" class="mr-2"> Lấy giá trị biểu mẫu </a-button>
    <a-button @click="clearFormValue" class="mr-2"> Xóa giá trị biểu mẫu </a-button>
    <a-button @click="updateFormValue" class="mr-2"> Cập nhật giá trị biểu mẫu </a-button>
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
   * getFieldsValue Lấy tất cả giá trị biểu mẫu
   * validate Giá trị biểu mẫu sau khi kiểm tra đạt,Hỗ trợ kiểm chứng một vài trường trong đó,validate(['visitor',...])
   * setFieldsValue Cập nhật giá trị biểu mẫu, ví dụ setFieldsValue({'visitor':'Lý Tứ',...})
   * resetFields Xóa tất cả giá trị biểu mẫu
   */
  const [registerForm, { getFieldsValue, setFieldsValue, resetFields, validate }] = useForm({
    schemas: formSchemas,
    //Ẩn nút thao tác
    showActionButtonGroup: false,
    labelWidth: '150px',
    //Mặc định focus vào phần tử đầu tiên, chỉ hỗ trợinput
    autoFocusFirstItem: true,
  });

  /**
   * Lấy giá trị biểu mẫu
   */
  async function getFormValue() {
    //Lấy tất cả giá trị
    let fieldsValue = await getFieldsValue();
    console.log('fieldsValue:::', fieldsValue);
    //Sau khi biểu mẫu kiểm chứng đạt, lấy giá trị của tất cả các trường
    fieldsValue = await validate();
    console.log('fieldsValue:::', fieldsValue);
    //Kiểm chứng biểu mẫu`visitorKhách đến thăm`Giá trị lấy được sau khi thông qua
    fieldsValue = await validate(['visitor']);
    console.log('fieldsValue:::', fieldsValue);
  }

  /**
   * Xóa giá trị biểu mẫu
   */
  async function clearFormValue() {
    await resetFields();
  }

  /**
   * Cập nhật giá trị biểu mẫu
   */
  async function updateFormValue() {
    console.log('Đã vào đây');
    await setFieldsValue({ visitor: 'Lý Tứ' });
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
