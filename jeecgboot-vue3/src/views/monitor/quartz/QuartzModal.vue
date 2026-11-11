<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <div class="quartz-modal">
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './quartz.data';
  import { saveOrUpdateQuartz, getQuartzById } from './quartz.api';
  import { isJsonObjectString } from '/@/utils/is';
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  // Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    // Giải thích logic mã: 【issues/790】Vấn đề ô nhập văn bản trong popup không được căn giữa
    labelWidth: 100,
  });
  // Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      // Lấy chi tiết
      //data.record = await getQuartzById({id: data.record.id});
      try {
        data.record.paramterType = isJsonObjectString(data?.record?.parameter) ? 'json' : 'string';
      } catch (e) {
        console.log(e);
      }
      // Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  // Cài đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới nhiệm vụ' : 'Chỉnh sửa nhiệm vụ'));
  // Sự kiện submit biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      // Gửi biểu mẫu
      await saveOrUpdateQuartz(values, isUpdate.value);
      // Đóng popup
      closeModal();
      // Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
<style lang="less" scoped>
  .quartz-modal :deep(.dynamic-delete-button) {
    position: static;
    align-self: center;
    margin-bottom: 5px;
    margin-left: 5px;
  }
</style>
