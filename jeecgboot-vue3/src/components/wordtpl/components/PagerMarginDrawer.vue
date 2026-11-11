<!--Trang lề-->
<template>
  <BasicDrawer @register="registerBaseDrawer" title="Thiết lập lề" width="600" showFooter @ok="handleOk">
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script setup lang="ts">
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, FormSchema, useForm } from '@/components/Form';

  let emit = defineEmits(['register','ok']);
  
  const schemas: FormSchema[] = [
    {
      label:'Lề trên',
      field: 'marginTop',
      component: 'InputNumber',
      componentProps: {
        min: 0,
      },
      rules: [{ required: true, message: 'Vui lòng nhập lề trên' }],
    },
    {
      label:'Lề dưới',
      field: 'marginBottom',
      component: 'InputNumber',
      componentProps: {
        min: 0,
      },
      rules: [{ required: true, message: 'Vui lòng nhập lề dưới' }],
    },
    {
      label:'Lề trái',
      field: 'marginLeft',
      component: 'InputNumber',
      componentProps: {
        min: 0,
      },
      rules: [{ required: true, message: 'Vui lòng nhập lề trái' }],
    },
    {
      label:'Lề phải',
      field: 'marginRight',
      component: 'InputNumber',
      componentProps: {
        min: 0,
      },
      rules: [{ required: true, message: 'Vui lòng nhập lề phải' }],
    },
  ];

  const [registerForm, { setFieldsValue, resetFields, validate }] = useForm({
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
  :deep(.ant-input-number){
    width: 100%;
  }
</style>
