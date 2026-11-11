<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Cấu hình trang chủ" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { roleIndexFormSchema } from '../role.data';
  import { saveOrUpdateRoleIndex, queryIndexByCode } from '../role.api';
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: roleIndexFormSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    setFieldsValue({ roleCode: data.roleCode });
    let res = await queryIndexByCode({ roleCode: data.roleCode });
    isUpdate.value = !!res.result?.id;
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...res.result,
      });
    }
  });

  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateRoleIndex(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success', { isUpdate: isUpdate.value, values });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped></style>
