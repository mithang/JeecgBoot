<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm" :disabled="isDisabled" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './demo.data';
  import { saveOrUpdateDemo, getDemoById } from './demo.api';
  // Khai báoEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Tham số tiếp nhận tùy chỉnh
  const props = defineProps({
    //Có vô hiệu hóa trang không
    isDisabled: {
      type: Boolean,
      default: false,
    },
  });

  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    //labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false, showOkBtn: !props.isDisabled});
    isUpdate.value = !!data?.isUpdate;
    if(data.createBy){
      await setFieldsValue({createBy: data.createBy})
    }
    if(data.createTime){
      await setFieldsValue({createTime: data.createTime})
    }
    if (unref(isUpdate)) {
      //Lấy chi tiết
      data.record = await getDemoById({ id: data.record.id });
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateDemo(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
