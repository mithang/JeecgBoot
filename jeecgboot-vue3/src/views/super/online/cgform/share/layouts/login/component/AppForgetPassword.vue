<template>
  <div class="forget-pwd-box">
    <div class="forget-pwd-subject">
      <AppLoginHeader />
      <div class="flex-row align-items-center margin-top40">
        <div class="register-title">
          {{ t('sys.login.forgetFormTitle') }}
        </div>
      </div>
      <div class="register-content">
        <a-form ref="phoneUpdateRef" :model="formState" :rules="getFormRules" v-show="activeUpdateKey === 'vailPhone'">
          <div class="content-item" :class="getPhoneClass" @click="formStateInputClick('phone')">
            <a-form-item name="phone">
              <a-input ref="phoneRef" v-model:value="formState.phone" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'phone' ? 'active-title' : ''">
                {{ t('sys.login.mobile') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getSmsCodeClass">
            <a-form-item name="smscode" @click="formStateInputClick('smscode')">
              <a-input ref="smscodeRef" :maxLength="6" v-model:value="formState.smscode" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'smscode' ? 'active-title' : ''">
                {{ t('sys.login.smsCode') }}
              </div>
            </a-form-item>
            <a-input
              type="button"
              v-if="showInterval"
              class="aui-code-line pointer"
              :bordered="false"
              @click="getLoginCode"
              v-model:value="loginCode"
            />
            <a-input type="button" v-else class="aui-code-line disabled-btn" :bordered="false" v-model:value="loginDisabledCode" />
          </div>
          <div class="forget-btn pointer" @click="forgetPwdClick">
            <span>{{ t('sys.login.nextStep') }}</span>
          </div>
          <div class="line"></div>
        </a-form>
        <a-form ref="pwdUpdateRef" :model="accountData" :rules="getFormPwdRules" v-show="activeUpdateKey === 'vailPwd'">
          <div class="content-item" :class="getPhoneClass">
            <a-form-item name="username">
              <a-input ref="phoneRef" v-model:value="accountData.username" style="height: 40px" disabled />
              <div class="active-form-title">
                {{ t('sys.login.userName') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getPasswordClass" @click="formStateInputClick('forgetPassword')">
            <a-form-item name="forgetPassword">
              <a-input ref="pwdRef" type="password" v-model:value="accountData.forgetPassword" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'password' ? 'active-title' : ''">
                {{ t('sys.login.password') }}
              </div>
            </a-form-item>
          </div>
          <div class="content-item" :class="getConfirmPasswordClass" @click="formStateInputClick('confirmPassword')">
            <a-form-item name="confirmPassword">
              <a-input ref="conPwdRef" type="password" v-model:value="accountData.confirmPassword" style="height: 40px" @blur="formStateInputBlur" />
              <div class="form-title" :class="activekey === 'password' ? 'active-title' : ''">
                {{ t('sys.login.confirmPassword') }}
              </div>
            </a-form-item>
          </div>
          <div class="forget-btn pointer" @click="updatePwdClick">
            <span>Hoàn thành</span>
          </div>
          <div class="line"></div>
        </a-form>
        <span class="to-login pointer" @click="toLoginClick">{{ t('sys.exception.backLogin') }}</span>
      </div>
    </div>
  </div>
  <!-- Popup mã xác thực hình ảnh -->
  <CaptchaModal @register="captchaRegisterModal" @ok="getLoginCode" />
</template>

<script lang="ts" setup name="app-forget-password">
  import AppLoginHeader from './AppLoginHeader.vue';
  import { computed, reactive, ref, toRaw, unref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Rule } from '/@/components/Form';
  import { getCaptcha, passwordChange, phoneVerify } from '/@/api/sys/user';
  import { SmsEnum } from '/@/views/sys/login/useLogin';
  import { RuleObject } from 'ant-design-vue/lib/form/interface';

  import CaptchaModal from '@/components/jeecg/captcha/CaptchaModal.vue';
  import { useModal } from "@/components/Modal";
  import { ExceptionEnum } from "@/enums/exceptionEnum";
  const [captchaRegisterModal, { openModal: openCaptchaModal }] = useModal();
  
  const { createMessage } = useMessage();
  const { t } = useI18n();
  //Biểu mẫu số điện thoại
  const formState = reactive<any>({
    phone: '',
    smscode: '',
    forgetPassword: '',
    policy: true,
  });
  //input ref
  const phoneRef = ref();
  const pwdRef = ref();
  const smscodeRef = ref();
  const pwdUpdateRef = ref();
  const conPwdRef = ref();
  //Sửa giá trị mật khẩu
  const activeUpdateKey = ref<string>('vailPhone');
  //Giá trị đã chọn
  const activekey = ref<string>('');

  const getMobileFormRule = computed(() => createRule(t('sys.login.mobilePlaceholder')));
  const getSmsFormRule = computed(() => createRule(t('sys.login.smsPlaceholder')));
  const getPasswordFormRule = computed(() => createRule(t('sys.login.passwordPlaceholder')));
  //Quy tắc quên mật khẩu
  const getFormRules = computed(() => {
    return {
      phone: unref(getMobileFormRule),
      smscode: unref(getSmsFormRule),
    };
  });

  const getFormPwdRules = computed(() => {
    return {
      forgetPassword: unref(getPasswordFormRule),
      confirmPassword: [{ validator: validateConfirmPassword(unref(accountData).forgetPassword), trigger: 'change' }],
    };
  });

  const { notification, createErrorModal } = useMessage();
  const loginCode = computed(() => t('component.countdown.normalText'));
  const loginDisabledCode = computed(() => t('component.countdown.sendText', [unref(timeRuning)]));

  /**
   * Xác thực mật khẩu lần hai
   * @param password
   */
  const validateConfirmPassword = (password: string) => {
    return async (_: RuleObject, value: string) => {
      if (!value) {
        return Promise.reject(t('sys.login.passwordPlaceholder'));
      }
      if (value !== password) {
        return Promise.reject(t('sys.login.diffPwd'));
      }
      return Promise.resolve();
    };
  };

  /**
   * Tạo quy tắc
   * @param message
   */
  function createRule(message: string) {
    return [
      {
        required: true,
        message,
        trigger: 'change',
      },
    ];
  }

  //Lấy kiểu dáng của số điện thoại
  const getPhoneClass = computed(() => {
    return formState.phone != '' ? 'current-active' : '' || unref(activekey) === 'phone' ? 'current-active' : '';
  });

  //Lấy kiểu dáng của số điện thoại
  const getSmsCodeClass = computed(() => {
    return formState.smscode != '' ? 'current-active' : '' || unref(activekey) === 'smscode' ? 'current-active' : '';
  });

  //Lấy kiểu dáng của mật khẩu
  const getPasswordClass = computed(() => {
    return unref(accountData).forgetPassword != '' ? 'current-active' : '' || unref(activekey) === 'forgetPassword' ? 'current-active' : '';
  });
  //Lấy kiểu dáng của mật khẩu xác nhận
  const getConfirmPasswordClass = computed(() => {
    return unref(accountData).confirmPassword != '' ? 'current-active' : '' || unref(activekey) === 'confirmPassword' ? 'current-active' : '';
  });

  const emit = defineEmits(['return-login', 'login-account']);
  //Có hiển thị lấy mã xác thực hay không
  const showInterval = ref<boolean>(true);
  //60s
  const timeRuning = ref<number>(60);
  //Bộ hẹn giờ
  const timer = ref<any>(null);
  //Xác thực biểu mẫu số điện thoại
  const phoneUpdateRef = ref();
  //Dữ liệu người dùng
  const accountData = ref<any>([]);

  /**
   * Sự kiện nhấp của component biểu mẫu
   */
  function formStateInputClick(val) {
    activekey.value = val;
    if (val === 'phone') {
      phoneRef.value.focus();
    } else if (val === 'smscode') {
      smscodeRef.value.focus();
    } else if (val === 'confirmPassword') {
      conPwdRef.value.focus();
    } else {
      pwdRef.value.focus();
    }
  }

  /**
   * Component biểu mẫu mất tiêu điểm (focus)
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * Lấy mã xác thực
   */
  async function getLoginCode() {
    if (!formState.phone) {
      createMessage.warn(t('sys.login.mobilePlaceholder'));
      return;
    }
    //update-begin---author:wangshuai ---date:20230620  for：【QQYUN-5590】Quên mật khẩu không hoạt động------------
    //update-begin---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】Cùng mộtIP，1phút vượt quá5lần tin nhắn SMS, thì nhắc cần mã xác thực---
    const result = await getCaptcha({ mobile: formState.phone, smsmode: SmsEnum.FORGET_PASSWORD }).catch((res) =>{
      if(res.code === ExceptionEnum.PHONE_SMS_FAIL_CODE){
        openCaptchaModal(true, {});
      }
    });
    //update-end---author:wangshuai---date:2024-04-18---for:【QQYUN-9005】Cùng mộtIP，1phút vượt quá5lần tin nhắn SMS, thì nhắc cần mã xác thực---
    //update-end---author:wangshuai ---date:20230620  for：【QQYUN-5590】Quên mật khẩu không hoạt động------------
    if (result) {
      const TIME_COUNT = 60;
      if (!unref(timer)) {
        timeRuning.value = TIME_COUNT;
        showInterval.value = false;
        timer.value = setInterval(() => {
          if (unref(timeRuning) > 0 && unref(timeRuning) <= TIME_COUNT) {
            timeRuning.value = timeRuning.value - 1;
          } else {
            showInterval.value = true;
            clearInterval(unref(timer));
            timer.value = null;
          }
        }, 1000);
      }
    }
  }

  /**
   * Quên mật khẩu
   */
  function forgetPwdClick() {
    phoneUpdateRef.value.validateFields().then(async (values) => {
      const resultInfo = await phoneVerify(
        toRaw({
          phone: values.phone,
          smscode: values.smscode,
        })
      );
      if (resultInfo.success) {
        console.log(resultInfo);
        let accountInfo = {
          username: resultInfo.result.username,
          phone: values.phone,
          smscode: resultInfo.result.smscode,
        };
        accountData.value = accountInfo;
        activeUpdateKey.value = 'vailPwd';
      } else {
        notification.error({
          message: t('sys.api.errorTip'),
          description: resultInfo.message || t('sys.api.networkExceptionMsg'),
          duration: 3,
        });
      }
    });
  }

  /**
   * Hoàn thành
   */
  async function updatePwdClick() {
    pwdUpdateRef.value.validateFields().then(async (values) => {
      const resultInfo = await passwordChange(
        toRaw({
          username: values.username,
          password: values.forgetPassword,
          smscode: unref(accountData).smscode,
          phone: unref(accountData).phone,
        })
      );
      if (resultInfo.success) {
        //Đổi mật khẩu
        emit('login-account', { account: values.username, password: values.forgetPassword });
        phoneUpdateRef.value.resetFields();
        accountData.value = {};
        activeUpdateKey.value = 'vailPhone';
      } else {
        //Nhắc nhở lỗi
        createErrorModal({
          title: t('sys.api.errorTip'),
          content: resultInfo.message || t('sys.api.networkExceptionMsg'),
        });
      }
    });
  }

  /**
   * Quay lại đăng nhập
   */
  function toLoginClick() {
    emit('return-login');
  }
</script>

<style lang="less" scoped>
  .forget-pwd-box {
    display: block;
  }

  .forget-pwd-subject {
    max-width: 420px;
    padding: 48px 48px 23px;
    box-sizing: border-box;
    background: #ffffff;
    border-radius: 4px;
    margin: 80px auto 15px;
    position: relative;
  }

  .flex-row {
    display: flex;
    min-width: 0;
  }

  .align-items-center {
    align-items: center;
  }

  .margin-top40 {
    margin-top: 40px;
  }

  .register-title {
    text-align: left;
    font-size: 20px;
    color: #333333;
  }

  .register-content {
    margin-top: 5px;
  }

  .content-item {
    position: relative;
    margin-top: 30px;
    background: #ffffff;
    height: 40px;
    box-sizing: border-box;
  }

  .form-title {
    position: absolute;
    left: 12px;
    top: 13px;
    color: #9e9e9e;
    font-size: 14px;
    z-index: 0;
    line-height: 1;
    transition: all 0.3s;
  }

  .current-active {
    .form-title {
      font-size: 12px;
      color: #757575;
      top: -9px;
      z-index: 1;
      background: #fff;
      padding: 0 4px;
    }

    .active-title {
      color: #2196f3;
    }
  }

  .aui-code-line {
    background-color: #42a5f5;
    display: inline-block;
    width: 110px;
    height: 32px;
    border-radius: 4px;
    position: absolute;
    right: 4px;
    top: 4px;
    text-align: center;
    font-size: 13px;
    color: #fff;
  }

  .register-rule {
    margin-top: 13px;
    font-size: 14px;
    padding: 0;
    line-height: 1.5;
    color: #757575;
  }

  .forget-btn {
    font-weight: 600;
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: block;
    background: #2296f3;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    margin-top: 32px;
    text-align: center;
  }

  .pointer {
    cursor: pointer;
  }

  .line {
    width: 100%;
    height: 0;
    border-top: 1px solid #f0f0f0;
    opacity: 1;
    display: block;
    margin-top: 84px;
  }

  .to-login {
    font-size: 14px;
    color: #2196f3;
    display: block;
    margin: 20px auto 0;
    text-align: center;
  }

  .active-form-title {
    position: absolute;
    left: 12px;
    top: -9px;
    color: #757575;
    font-size: 12px;
    z-index: 0;
    line-height: 1;
    background: #fff;
  }

  .disabled-btn {
    background-color: #ccc;
  }

  @media screen and (max-width: 414px) {
    .login-box {
      padding: 46px 16px;
    }

    .login-subject {
      margin: 0;
      padding: 48px 24px 23px;
    }
  }
</style>
