<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" width="550px" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../dict.data';
  import { saveOrUpdateDict } from '../dict.api';
  // Khai báoEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const rowId = ref('');
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      rowId.value = data.record.id;
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới từ điển' : 'Chỉnh sửa từ điển'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateDict(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success', { isUpdate: unref(isUpdate), values: { ...values, id: rowId.value } });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
