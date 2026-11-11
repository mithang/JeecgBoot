<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="getTitle" @ok="handleSubmit" width="700px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { dataRuleFormSchema } from './menu.data';
  import { saveOrUpdateRule } from './menu.api';
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  const props = defineProps({ permissionId: String });
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: dataRuleFormSchema,
    showActionButtonGroup: false,
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });

  //Đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới quy tắc' : 'Chỉnh sửa quy tắc'));

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      const values = await validate();
      values.permissionId = props.permissionId;
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateRule(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
