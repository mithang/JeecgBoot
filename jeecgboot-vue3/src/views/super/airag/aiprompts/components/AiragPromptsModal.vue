<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :maxHeight="500" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="AiragPromptsForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../AiragPrompts.data';
  import { saveOrUpdate } from '../AiragPrompts.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getDateByPicker } from '/@/utils';
  const { createMessage } = useMessage();
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const isDetail = ref(false);
  //Cấu hình biểu mẫu
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
    labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
    baseRowStyle: { padding: '0 20px' },
  });
  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !!data?.showFooter;
    if (unref(isUpdate)) {
      //Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
    // Khi ẩn phần đáy thì vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter });
  });
  //Chọn cá nhân hóa ngày tháng
  const fieldPickers = reactive({});
  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : !unref(isDetail) ? 'Chi tiết' : 'Chỉnh sửa'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      // Tiền xử lý dữ liệu ngày tháng
      changeDateValue(values);
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate.value);
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success',values);
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Xử lý giá trị ngày tháng
   * @param formData dữ liệu biểu mẫu
   */
  const changeDateValue = (formData) => {
    if (formData && fieldPickers) {
      for (let key in fieldPickers) {
        if (formData[key]) {
          formData[key] = getDateByPicker(formData[key], fieldPickers[key]);
        }
      }
    }
  };
</script>

<style lang="less" scoped>
  /** Kiểu ô nhập thời gian và số */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
