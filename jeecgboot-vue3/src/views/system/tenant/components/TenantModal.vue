<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="700px">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../tenant.data';
  import { saveOrUpdateTenant, getTenantById } from '../tenant.api';
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    // labelWidth: 150,
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
      // Ở chế độ chỉnh sửa, vô hiệu hóa trường id
      updateSchema({ field: 'id', dynamicDisabled: true });
      //Lấy chi tiết
      data.record = await getTenantById({ id: data.record.id });
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    } else {
      updateSchema({ field: 'id', dynamicDisabled: false });
    }
  });
  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới đơn vị thuê' : 'Chỉnh sửa đơn vị thuê'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateTenant(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
