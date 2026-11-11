<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :maxHeight="500" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="SysUgroupForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from '../ugroup.data';
  import { saveOrUpdate } from '../ugroup.api';
  // EmitsKhai báo
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
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    isDetail.value = !!data?.showFooter;
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
    // Khi ẩn phần chân thì vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter });
  });
  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : !unref(isDetail) ? 'Chi tiết' : 'Chỉnh sửa'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
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
</script>

<style lang="less" scoped>
  /** Kiểu của ô nhập thời gian và số */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
