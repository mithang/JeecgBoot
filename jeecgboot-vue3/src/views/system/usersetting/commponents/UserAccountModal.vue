<template>
  <BasicModal v-bind="$attrs" @register="registerModal" width="500px"  :title="title" @ok="handleSubmit" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../UserSetting.data';
import { ref, unref, defineEmits } from 'vue';
import { userEdit } from "../UserSetting.api";
import { useUserStore } from "/@/store/modules/user";
import { useMessage } from "/@/hooks/web/useMessage";

const userStore = useUserStore();
const { createMessage } = useMessage();
const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
  schemas: formSchema,
  showActionButtonGroup: false,
});
const userDetail = ref<any>({});
const isUpdate = ref<boolean>(false);
const title = ref<string>('');
const emit = defineEmits(['register','success']);
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  await resetFields();
  setModalProps({ confirmLoading: false });
  title.value = 'Chỉnh sửa hồ sơ cá nhân';
  if(data.record.post){
    data.record.post = data.record.post.split(",")
  }
  if(data.record.relTenantIds){
    data.record.relTenantIds = data.record.relTenantIds.split(",")
  }
  userDetail.value = data.record;
  // Giải thích logic mã: [QQYUN-3976]Thông tin cá nhân: khi chỉnh sửa, vừa đăng ký chưa đặt ngày sinh thì hiển thị ngày sinh có vấn đề------------
  if(data.record.birthday === 'Chưa điền'){
    data.record.birthday = undefined;
  }
  await setFieldsValue({ ...data.record });
});

/**
 * Sửa và thêm mới dữ liệu
 */
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    //Gửi biểu mẫu
    await userEdit(values).then((res) =>{
      if(res.success){
        createMessage.success(res.message)
      }else{
        createMessage.warn(res.message)
      }
    });
    //Cập nhật bộ nhớ đệm
    Object.assign(userDetail.value,values)
    userStore.setUserInfo(unref(userDetail));
    emit('success')
    //Đóng cửa sổ bật lên
    closeModal();
  } finally {
    setModalProps({ confirmLoading: false });
  }
}
</script>
