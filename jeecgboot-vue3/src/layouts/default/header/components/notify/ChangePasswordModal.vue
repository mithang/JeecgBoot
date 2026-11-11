<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="getTitle"
    :width="500"
    @ok="handleSubmit"
    :keyboard="false"
    :maskClosable="false"
    :closable="false"
    :bodyStyle="{ padding: '10px 20px 20px 20px' }"
    :ok-button-props="{ loading: confirmLoading }"
    :cancel-button-props="{ style: { display: 'none' } }"
    destroyOnClose
  >
    <div>
      <span class="pwd-topbar">Mật khẩu hiện tại của bạn trùng với mật khẩu hệ thống, vui lòng đổi mật khẩu!</span>
      <BasicForm @register="registerForm" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { rules, createPasswordValidator } from '@/utils/helper/validator';
  import { defHttp } from '@/utils/http/axios';
  import { useUserStore } from '@/store/modules/user';

  const emit = defineEmits(['success', 'register']);

  const { t } = useI18n();
  const { createMessage } = useMessage();
  const confirmLoading = ref(false);
  const oldPassword = ref('');
  const userStore = useUserStore();

  // Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: [
      {
        label: t('layout.changePassword.newPassword'),
        field: 'password',
        component: 'StrengthMeter',
        componentProps: {
          placeholder: t('layout.changePassword.pleaseEnterNewPassword'),
        },
        rules: [
          {
            required: true,
            message: t('layout.changePassword.pleaseEnterNewPassword'),
          },
          //update-begin---author:wangshuai ---date:2026-06-29  for: 【QQYUN-16619】 Chuyển đổi động quy tắc kiểm tra theo công tắc bảo mật cấp 3-----------
          {
            validator: createPasswordValidator(oldPassword.value),
          },
          //update-end---author:wangshuai ---date:2026-06-29  for: 【QQYUN-16619】 Chuyển đổi động quy tắc kiểm tra theo công tắc bảo mật cấp 3-----------
        ],
      },
      {
        label: t('layout.changePassword.confirmNewPassword'),
        field: 'confirmpassword',
        component: 'InputPassword',
        dynamicRules: ({ values }) => rules.confirmPassword(values, true),
      },
    ],
    showActionButtonGroup: false,
    actionColOptions: {
      span: 24,
    },
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    resetFields();
    setModalProps({ confirmLoading: false });
    oldPassword.value = decodeURIComponent(data.oldPassword)
  });

  const getTitle = computed(() => 'Đổi mật khẩu');
  const { createMessage: $message } = useMessage();

  
  
  // Xử lý gửi
  async function handleSubmit() {
    try {
      const values = await validate();
      setModalProps({ confirmLoading: true });
      // Gửi biểu mẫu
      let params = Object.assign({ username: userStore.getUserInfo.username, oldpassword: oldPassword.value }, values);
      defHttp.put({ url: '/sys/user/updatePassword', params }, { isTransformResponse: false }).then((res) => {
        if (res.success) {
            $message.info({
              content:'Đổi mật khẩu thành công, vui lòng đăng nhập lại! 2s sau sẽ tự động đăng xuất',
              duration: 2
            })
            // 3s sau quay lại trang đăng nhập
            setTimeout(()=>{
              userStore.logout(true);
            },2000)
            // Đóng popup
            closeModal();
          // Đóng popup
          closeModal();
        } else {
          $message.warning(res.message);
        }
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-form-item) {
    margin-bottom: 20px;
  }
  .pwd-topbar{
    width: 400px;
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background: linear-gradient(90deg, #fff7e6, #fff1b8);
    border: 1px solid #ffd591;
    border-left: 0;
    border-right: 0;
    color: #ad6800;
    position: relative;
    margin-bottom: 10px;
    left: 36px;
  }
</style>
