<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" :width="500" :minHeight="20" :maxHeight="100">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, inject } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { ticketFormSchema } from '../erplist.data';
  import { saveOrUpdateTicket } from '../erplist.api';
  //Nhận bảng chínhid
  const orderId = inject('orderId');
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: ticketFormSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Cài đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

  //Sự kiện submit biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      if (unref(orderId)) {
        values.orderId = unref(orderId);
      }
      //Gửi biểu mẫu
      await saveOrUpdateTicket(values, isUpdate.value);
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
