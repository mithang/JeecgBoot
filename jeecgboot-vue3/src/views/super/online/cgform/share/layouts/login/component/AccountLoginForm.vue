<template>
  <a-form id="loginContentForm" ref="loginRef" :model="formState" :rules="getFormRules" @keyup.enter.native="accountLoginClick">
    <div class="content-item current-active" @click="formStateInputClick('account')">
      <a-form-item name="account">
        <a-input ref="accountRef" id="accountLogin" v-model:value="formState.account" style="height: 40px" @blur="formStateInputBlur" />
        <div class="form-title" :class="activekey === 'account' ? 'active-title' : ''">
          {{ t('sys.login.userName') }}
        </div>
      </a-form-item>
    </div>
    <div class="content-item current-active" @click="formStateInputClick('password')">
      <a-form-item name="password">
        <a-input id="pwdLogin" ref="" type="password" v-model:value="formState.password" style="height: 40px" @blur="formStateInputBlur"/>
        <div class="form-title" :class="activekey === 'password' ? 'active-title' : ''">
          {{ t('sys.login.password') }}
        </div>
      </a-form-item>
    </div>
    <div class="content-item current-active">
      <a-row :span="24">
        <a-col :span="15">
          <a-form-item name="inputCode" @click="formStateInputClick('inputCode')">
            <a-input ref="codeRef" v-model:value="formState.inputCode" style="height: 40px" @blur="formStateInputBlur" />
            <div class="form-title" :class="activekey === 'inputCode' ? 'active-title' : ''">
              {{ t('sys.login.inputCode') }}
            </div>
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <div class="code-image">
            <img
              class="pointer"
              v-if="randCodeData.requestCodeSuccess"
              style="margin-top: 2px; max-width: initial"
              :src="randCodeData.randCodeImage"
              @click="handleChangeCheckCode"
            />
            <img v-else style="margin-top: 2px; max-width: initial" :src="codeImage" @click="handleChangeCheckCode" />
          </div>
        </a-col>
      </a-row>
    </div>
    <div class="forget-pwd">
      <div style="float: left" class="pointer forget-login-pwd">
        <span style="color: #757575" @click="forgetPwdClick">{{ t('sys.login.forgetPassword') }}</span>
      </div>
      <div class="remember-password pointer" style="float: right">
        <a-checkbox v-model:checked="rememberMeCheck">{{ t('sys.login.rememberMe') }}</a-checkbox>
      </div>
    </div>
    <div>
      <a-button type="primary" @click="accountLoginClick" :loading="loginLoading" class="login-btn">{{ t('sys.login.loginButton') }}</a-button>
    </div>
    <div class="phone-login-btn pointer" @click="phoneLoginBtnClick">{{ t('sys.login.mobileSignInFormTitle') }}</div>
  </a-form>
</template>

<script lang="ts" setup name="account-login-form">
  //Lấy kiểu dáng của số điện thoại
  import {computed, effect, nextTick, onMounted, reactive, ref, toRaw, unref, watch} from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { useFormRules, useFormValid } from '/@/views/sys/login/useLogin';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getCodeInfo } from '/@/api/sys/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import codeImage from '/@/assets/images/checkcode.png';
  import { RuleObject } from 'ant-design-vue/lib/form/interface';

  const accountRef = ref();
  const codeRef = ref();
  const passwordRef = ref();
  const { t } = useI18n();
  //Giá trị đã chọn
  const activekey = ref<string>('');

  //Đang tải đăng nhập
  const loginLoading = ref<boolean>(false);
  //Biểu mẫu đăng nhập
  const loginRef = ref();
  const randCodeData = reactive({
    randCodeImage: '',
    requestCodeSuccess: false,
    checkKey: -1,
  });
  //Biểu mẫu số điện thoại
  const formState = reactive<any>({
    account: '',
    password: '',
    inputCode: '',
  });
  //Quy tắc đăng nhập
  const { getFormRules } = useFormRules(formState);
  const { validForm } = useFormValid(loginRef);
  const userStore = useUserStore();
  //Ghi nhớ tôi
  const rememberMeCheck = ref<boolean>(false);
  const getUsernameClass = computed(() => {
    return formState.account != '' ? 'current-active' : '' || unref(activekey) === 'account' ? 'current-active' : '';
  });
  //Lấy kiểu dáng của mật khẩu
  const getPwdClass = computed(() => {
    return formState.password != '' ? 'current-active' : '' || unref(activekey) === 'password' ? 'current-active' : '';
  });
  //Lấy kiểu dáng của mã xác thực
  const getInputCodeClass = computed(() => {
    return formState.inputCode != '' ? 'current-active' : '' || unref(activekey) === 'inputCode' ? 'current-active' : '';
  });
  const { notification, createErrorModal } = useMessage();
  const emit = defineEmits(['login', 'forget-pwd', 'login-success']);

  /**
   * Sự kiện nhấp của component biểu mẫu
   */
  function formStateInputClick(val) {
    activekey.value = val;
  }

  /**
   * Component biểu mẫu mất tiêu điểm (focus)
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * Sự kiện nhấp đăng nhập
   */
  async function accountLoginClick() {
    let values = await validForm();
    if (!values) {
      return;
    }
    try {
      loginLoading.value = true;
      const { userInfo } = await userStore.login(
        toRaw({
          password: values.password,
          username: values.account,
          captcha: values.inputCode,
          checkKey: randCodeData.checkKey,
          mode: 'none', //không dùng nhắc lỗi mặc định
          goHome: false, //không chuyển đến trang chủ
        })
      );
      if (userInfo) {
        emit('login-success', userInfo.realname);
      }
    } catch (error) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error.message || t('sys.api.networkExceptionMsg'),
        duration: 3,
      });
      handleChangeCheckCode();
    } finally {
      loginLoading.value = false;
    }
  }

  /**
   * Lấy mã xác thực
   */
  function handleChangeCheckCode() {
    formState.inputCode = '';
    //TODO Tương thíchmockvà API, tạm thời xử lý như vậy
    //update-begin---author:chenrui ---date:2025/1/7  for：[QQYUN-10775]Mã xác thực có thể dùng lại #7674------------
    randCodeData.checkKey = new Date().getTime() + Math.random().toString(36).slice(-4); // 1629428467008;
    //update-end---author:chenrui ---date:2025/1/7  for：[QQYUN-10775]Mã xác thực có thể dùng lại #7674------------
    getCodeInfo(randCodeData.checkKey).then((res) => {
      randCodeData.randCodeImage = res;
      randCodeData.requestCodeSuccess = true;
    });
  }

  /**
   * Đăng nhập bằng số điện thoại
   */
  function phoneLoginBtnClick() {
    emit('login', 'phoneLogin');
  }

  /**
   * Quên mật khẩu
   */
  function forgetPwdClick() {
    emit('forget-pwd');
  }

  /**
   * Thiết lập dữ liệu tài khoản
   * @param values
   */
  function setAccountData(values) {
    Object.assign(formState, values);
    handleChangeCheckCode();
  }

  onMounted(() => {
    handleChangeCheckCode();
  });

  defineExpose({
    setAccountData,
  });
</script>

<style lang="less" scoped>
  .content-item {
    position: relative;
    margin-top: 24px;
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
  .code-image {
    margin-left: 15px;
  }
  .forget-login-pwd:hover {
    span {
      color: #1182dd !important;
    }
  }

  .forget-pwd {
    font-size: 14px;
    margin-top: 20px;
  }

  .remember-password {
    color: #757575;
    line-height: 18px;
    cursor: pointer;
  }
  .pointer {
    cursor: pointer;
  }

  .login-btn {
    width: 100%;
    height: 40px;
    display: block;
    background: #2296f3;
    border-radius: 4px;
    font-size: 14px;
    color: #fff;
    margin-top: 65px;
    text-align: center;
  }
  .phone-login-btn {
    width: 100%;
    height: 40px;
    line-height: 40px;
    display: block;
    background: #ffffff;
    border-radius: 4px;
    font-size: 14px;
    color: #333333;
    margin-top: 20px;
    text-align: center;
    border: 1px solid #d9d9d9;
  }
</style>
