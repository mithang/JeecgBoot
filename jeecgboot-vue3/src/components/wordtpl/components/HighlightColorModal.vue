<!-- Màu nổi bật -->
<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Đặt màu làm nổi bật" @ok="handleSubmit" :width="500">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script setup lang="ts">
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicForm, FormSchema, useForm } from '@/components/Form';
  const formSchema: FormSchema[] = [
    {
      label: 'Màu nổi bật',
      field: 'highlightColor',
      component: 'Select',
      required: true,
      componentProps: {
        options: [
          { value: '#ffffff00', label: 'Không màu' },
          { value: '#FFFF00', label: 'Màu vàng' },
          { value: '#00FF00', label: 'Màu xanh lá' },
          { value: '#00FFFF', label: 'Màu xanh lơ' },
          { value: '#FF00FF', label: 'Hồng' },
          { value: '#0000FF', label: 'Màu xanh dương' },
          { value: '#FF0000', label: 'Màu đỏ' },
          { value: '#000080', label: 'Xanh dương đậm' },
          { value: '#008080', label: 'Xanh cyan đậm' },
          { value: '#008000', label: 'Xanh lá đậm' },
          { value: '#800080', label: 'Màu tím' },
          { value: '#800000', label: 'Đỏ đậm' },
          { value: '#808000', label: 'Vàng đậm' },
          { value: '#808080', label: 'Xám đậm' },
          { value: '#C0C0C0', label: 'Xám nhạt' },
          { value: '#000000', label: 'Màu đen' },
        ],
        getPopupContainer: ()=> document.body
      },
    },
  ];
  // EmitsKhai báo
  const emit = defineEmits(['register', 'ok']);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    wrapperCol: { span: 24 },
    labelCol: { span: 24 },
  });

  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    //Gán giá trị biểu mẫu
    await setFieldsValue({
      ...data,
    });
  });

  //Sự kiện submit biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('ok', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style scoped lang="less"></style>
