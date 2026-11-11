<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Hủy tài khoản" @ok="handleSubmit" destroyOnClose :width="400">
    <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
      <div class="cancellation-tip">
        <p style="color: red;margin-bottom: 10px !important;">Sau khi hủy, tài khoản sẽ được giữ lại 10 ngày, nếu cần khôi phục vui lòng liên hệ quản trị viên qua QQ </p>
      </div>
      <a-form-item label="" name="phone" class="cancellation-tip">
        <div class="black font-size-13" style="margin-bottom: 6px">Cách xác thực</div>
        <div class="pass-padding">
          <a-input placeholder="Vui lòng nhập số điện thoại" v-model:value="formState.phone" />
        </div>
      </a-form-item>
      <a-form-item label="" name="smscode" class="cancellation-tip">
        <CountdownInput v-model:value="formState.smscode" placeholder="Vui lòng nhập mã xác thực 6 số" :sendCodeApi="sendCodeApi" />
      </a-form-item>
    </a-form>
  </BasicModal>
</template>
<script lang="ts" name="UserCancellationModal" setup>
  import { ref, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { Rule } from '/@/components/Form/index';
  import { userLogOff } from '../UserSetting.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  import { CountdownInput } from '/@/components/CountDown';
  import { defHttp } from '@/utils/http/axios';

  const { createMessage, createErrorModal } = useMessage();
  //Tên người dùng
  const username = ref<string>('');
  const formRef = ref();
  const formState = reactive({
    smscode: '',
    phone: '',
  });
  // Khai báoEmits
  const emit = defineEmits(['success', 'register']);
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    setModalProps({ confirmLoading: false });
    username.value = data.record.username;
    Object.assign(formState, { password: '', smscode: '', phone: '' });
  });
  const userStore = useUserStore();
  const validatorRules: Record<string, Rule[]> = {
    phone: [{ required: true, message: 'Vui lòng nhập số điện thoại' }],
    smscode: [{ required: true, message: 'Vui lòng nhập mã xác thực 6 số' }],
  };

  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await formRef.value.validateFields();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      values.username = unref(username);
      await userLogOff(values).then((res) => {
        if (res.success) {
          createMessage.info({
            content: 'Hủy tài khoản thành công!',
            duration: 1,
          });
          //Quay lại trang đăng nhập
          setTimeout(() => {
            userStore.logout(true);
          }, 1000);
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
   * Hàm chạy trước khi đếm ngược
   */
  function sendCodeApi() {
    return new Promise((resolve, reject) => {
      if(!formState.phone){
        createErrorModal({ title: 'Thông báo lỗi', content: 'Vui lòng nhập số điện thoại!' });
        reject();
        return;
      }
      let params = { phone: formState.phone, username: username.value };
      defHttp
        .post({ url: '/sys/user/sendLogOffPhoneSms', params }, { isTransformResponse: false })
        .then((res) => {
          if (res.success) {
            resolve(true);
          } else {
            createErrorModal({ title: 'Thông báo lỗi', content: res.message || 'Sự cố không xác định' });
            reject();
          }
        })
        .catch((res) => {
          createErrorModal({ title: 'Thông báo lỗi', content: res.message || 'Sự cố không xác định' });
          reject();
        });
    });
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
  .cancellation-tip {
    padding: 0 10px;
  }
</style>
