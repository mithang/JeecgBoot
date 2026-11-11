<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Cấu hình trang chủ" @ok="handleSubmit" :width="600">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../home.data';
  import { saveOrUpdate } from '../home.api';
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(false);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      const record = {...data.values}
      //Gán giá trị cho biểu mẫu
      if (record.relationType == 'USER') {
        record.userCode = record.roleCode;
      }
      //Gán giá trị cho biểu mẫu
      if (record.relationType == 'DEFAULT') {
        record.roleCode = '';
      }
      await setFieldsValue({
        ...record,
      });
    }
  });

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      if(values.relationType == 'USER'){
        values.roleCode = values.userCode;
      }
      if(values.relationType == 'DEFAULT'){
        values.roleCode = 'DEF_INDEX_ALL';
      }
      await saveOrUpdate(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped></style>
