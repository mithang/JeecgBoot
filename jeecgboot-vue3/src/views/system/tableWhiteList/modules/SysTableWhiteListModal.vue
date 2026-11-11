<template>
  <BasicModal
    @register="registerModal"
    :title="title"
    width="40%"
    v-bind="$attrs"
    @ok="handleSubmit"
  >
    <div class="content"> 
      <BasicForm  @register="registerForm"/>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import {computed, ref, unref} from 'vue';
import {BasicModal, useModalInner} from '/@/components/Modal';
import {BasicForm, useForm} from '/@/components/Form/index';
import {formSchema} from '../SysTableWhiteList.data';
import {saveOrUpdate} from '../SysTableWhiteList.api';
// EmitsKhai báo
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
//Cấu hình biểu mẫu
const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
  labelWidth: 120,
  wrapperCol: null,
  schemas: formSchema,
  showActionButtonGroup: false,
});
//Gán giá trị cho biểu mẫu
const [registerModal, {setModalProps, closeModal}] = useModalInner(async (data) => {
  //Đặt lại biểu mẫu
  await resetFields();
  setModalProps({
    confirmLoading: false,
    showCancelBtn: data?.showFooter,
    showOkBtn: data?.showFooter
  });
  isUpdate.value = !!data?.isUpdate;
  if (unref(isUpdate)) {
    //Gán giá trị cho biểu mẫu
    await setFieldsValue({
      ...data.record,
    });
  }
  setProps({ disabled: !data?.showFooter })
});
//Đặt tiêu đề
const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

//Sự kiện gửi biểu mẫu
async function handleSubmit(v) {
  try {
    let values = await validate();
    setModalProps({confirmLoading: true});
    //Gửi biểu mẫu
    await saveOrUpdate(values, isUpdate.value);
    //Đóng cửa sổ bật lên
    closeModal();
    //Làm mới danh sách
    emit('success', {isUpdate: isUpdate.value, values});
  } finally {
    setModalProps({confirmLoading: false});
  }
}
</script>

<style lang="less" scoped>
  .content {
    padding: 20px 8% 0 4%;
  }
</style>
