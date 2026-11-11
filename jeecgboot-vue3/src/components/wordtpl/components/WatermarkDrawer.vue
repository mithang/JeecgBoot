<!--Trang watermark-->
<template>
  <BasicDrawer @register="registerBaseDrawer" title="Thiết lập watermark" width="600" showFooter @ok="handleOk">
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script setup lang="ts">
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, FormSchema, useForm } from '@/components/Form';

  let emit = defineEmits(['register', 'ok']);

  const schemas: FormSchema[] = [
    {
      label: 'Nội dung',
      field: 'data',
      component: 'Input',
      rules: [{ required: true, message: 'Vui lòng điền nội dung' }],
    },
    {
      label: 'Màu sắc',
      field: 'color',
      component: 'Input',
      componentProps: {
        type: 'color',
      },
      rules: [{ required: true, message: 'Vui lòng chọn màu' }],
      defaultValue: '#AEB5C0',
    },
    {
      label: 'Cỡ chữ',
      field: 'size',
      component: 'InputNumber',
      componentProps: {
        min: 10,
      },
      defaultValue: 50,
      rules: [{ required: true, message: 'Vui lòng nhập cỡ chữ' }],
    },
    {
      label: 'Độ trong suốt',
      field: 'opacity',
      component: 'Slider',
      componentProps: {
        min: 0.1,
        max: 1,
        step: 0.1,
      },
      defaultValue: 0.3,
      rules: [{ required: true, message: 'Vui lòng nhập độ trong suốt' }],
    },
    {
      label: 'Lặp lại',
      field: 'repeat',
      component: 'Select',
      componentProps: {
        options: [
          { label: 'Không lặp lại', value: '0' },
          { label: 'Lặp lại', value: '1' },
        ],
      },
      defaultValue: '0',
    },
    {
      label: 'Khoảng cách ngang',
      field: 'horizontalGap',
      component: 'InputNumber',
      defaultValue: 10,
    },
    {
      label: 'Khoảng cách dọc',
      field: 'verticalGap',
      component: 'InputNumber',
      defaultValue: 10,
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
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
