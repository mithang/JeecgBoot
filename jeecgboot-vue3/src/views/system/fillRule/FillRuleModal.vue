<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" :width="800" wrapClassName="fill-rule-modal" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './fill.rule.data';
  import { saveFillRule, updateFillRule } from './fill.rule.api';
  import {useMessage} from "@/hooks/web/useMessage";

  const { createMessage: $message } = useMessage();

  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

  // Khai báoEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);

  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate, getFieldsValue }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
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

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let formValue = await validate();

      // Kiểm tra tham số có hợp lệ hay không
      let ruleParams = formValue.ruleParams;
      if (!!ruleParams) {
        ruleParams = JSON.parse(ruleParams);
        for (const key of Object.keys(ruleParams)) {
          // online Kiểm tra từ khóa dành riêng
          if (key === 'onl_watch') {
            $message.error('Tên tham số không thể làonl_watch');
            return
          }
        }
      }

      setModalProps({ confirmLoading: true });
      if (isUpdate.value) {
        let allFieldsValue = getFieldsValue();
        // Trang chỉnh sửa: nếu biểu mẫu không có hộp thả xuống cấp cha thì khi gửi, phương thức validate không trả về giá trị đó, cần đặt thủ công
        if (!formValue.parentId && allFieldsValue.parentId) {
          formValue.parentId = allFieldsValue.parentId;
        }
        await updateFillRule(formValue);
      } else {
        await saveFillRule(formValue);
      }
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less">
  .fill-rule-modal .dynamic-delete-button {
    position: static;
    display: inline-flex;
    align-items: center;
    width: auto !important;
    height: 32px;
    margin-left: 8px;
  }
</style>
