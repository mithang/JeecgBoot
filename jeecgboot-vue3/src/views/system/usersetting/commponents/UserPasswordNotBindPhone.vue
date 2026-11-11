<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Đổi mật khẩu" @ok="handleSubmit" destroyOnClose :width="400">
    <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
      <a-form-item name="oldPassword">
        <div class="black font-size-13">Mật khẩu cũ</div>
        <div class="pass-padding">
          <a-input-password placeholder="Vui lòng nhập mật khẩu cũ" v-model:value="formState.oldPassword" />
        </div>
      </a-form-item>
      <a-form-item name="password">
        <span class="black font-size-13">Mật khẩu mới</span>
        <div class="pass-padding">
          <a-input-password v-model:value="formState.password" placeholder="Mật khẩu mới" autocomplete="new-password" />
        </div>
        <span class="gray-9e font-size-13">8-20ký tự, cần chứa cả chữ cái và số</span>
      </a-form-item>
    </a-form>
  </BasicModal>
</template>
<script lang="ts" name="user-pass-word-modal" setup>
  import { ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Rule } from '@/components/Form';
  import { updatePasswordNotBindPhone } from '../UserSetting.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  const { createMessage } = useMessage();
  //Tên người dùng
  const username = ref<string>('');
  const formRef = ref();
  const formState = reactive({
    oldPassword: '',
    password: '',
  });
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    username.value = data.record.username;
    Object.assign(formState, { password: '', oldPassword: '' });
  });
  const userStore = useUserStore();
  const validatorRules: Record<string, Rule[]> = {
    oldPassword: [{ required: true, message: 'Vui lòng nhập mật khẩu cũ' }],
    password: [
      { required: true, validator: checkPassword },
      { pattern: /^(?=.*[0-9])(?=.*[a-zA-Z])(.{8,20})$/, message: '8-20ký tự, cần chứa cả chữ cái và số' },
    ],
  };

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await formRef.value.validateFields();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      values.username = unref(username);
      await updatePasswordNotBindPhone(values).then((res) => {
        if (res.success) {
          createMessage.info({
            content: 'Đổi mật khẩu thành công, vui lòng đăng nhập lại! Sau 3s sẽ tự động đăng xuất',
            duration: 3,
          });
          //3ssau đó quay lại trang đăng nhập
          setTimeout(() => {
            userStore.logout(true);
          }, 3000);
          //Đóng cửa sổ bật lên
          closeModal();
        } else {
          createMessage.warn(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Kiểm tra mật khẩu mới có trống hay không
   */
  function checkPassword(_rule: Rule, value: string) {
    if (value === '') {
      return Promise.reject('Vui lòng nhập mật khẩu mới');
    }
    return Promise.resolve();
  }
</script>
<style lang="less" scoped>
  .black {
    color: @text-color;
  }
  .font-size-13 {
    font-size: 13px;
    line-height: 15px;
  }
  .gray-9e {
    color: #9e9e9e;
  }
  .float-left {
    float: left;
  }
  .pass-padding {
    padding-top: 10px;
    padding-bottom: 10px;
  }
  .antd-modal-form {
    padding: 10px 24px 10px 24px;
  }
  :deep(.ant-form-item) {
    margin-bottom: 10px;
  }
</style>
