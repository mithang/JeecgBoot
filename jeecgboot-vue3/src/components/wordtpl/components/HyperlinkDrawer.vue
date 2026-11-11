<!--Siêu liên kết-->
<template>
  <BasicDrawer @register="registerBaseDrawer" title="Siêu liên kết" width="600" showFooter @ok="handleOk">
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script setup lang="ts">
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, FormSchema, useForm } from '@/components/Form';

  let emit = defineEmits(['register', 'ok']);

  const schemas: FormSchema[] = [
    {
      label: 'Tên liên kết',
      field: 'name',
      component: 'Input',
      rules: [{ required: true, message: 'Vui lòng nhập tên liên kết' }],
    },
    {
      label: 'Địa chỉ liên kết',
      field: 'url',
      component: 'Input',
      componentProps: {
        min: 0,
      },
      rules: [{ required: true, message: 'Vui lòng nhập địa chỉ liên kết' }],
    },
  ];

  const [registerForm, { setFieldsValue, resetFields, validate, clearValidate }] = useForm({
    schemas,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  //Đăng kýDrawer
  const [registerBaseDrawer, { closeDrawer }] = useDrawerInner((data) => {
    resetFields();
    setFieldsValue({
      ...data,
    });
    clearValidate();
  });

  /**
   * Sự kiện xác nhận
   */
  async function handleOk() {
    let values = await validate();
    emit('ok', values);
    closeDrawer();
  }
</script>

<style scoped lang="less">
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
