<template>
  <div :class="prefixCls" class="login-background-img">
    <AppLocalePicker class="absolute top-4 right-4 enter-x xl:text-gray-600" :showText="false"/>
    <AppDarkModeToggle class="absolute top-3 right-7 enter-x" />
    <div class="aui-logo" v-if="!getIsMobile">
      <div>
        <h3>
          <img :src="logoImg" alt="jeecg" />
        </h3>
      </div>
    </div>
    <div v-else class="aui-phone-logo">
      <img :src="logoImg" alt="jeecg" />
    </div>
    <div v-show="type === 'login'">
      <div class="aui-content">
        <div class="aui-container">
          <div class="aui-form">
            <div class="aui-image">
              <div class="aui-image-text">
                <img :src="adTextImg" />
              </div>
            </div>
            <div class="aui-formBox">
              <div class="aui-formWell">
                <div class="aui-flex aui-form-nav investment_title">
                  <div class="aui-flex-box" :class="activeIndex === 'accountLogin' ? 'activeNav on' : ''" @click="loginClick('accountLogin')"
                    >{{ t('sys.login.signInFormTitle') }}
                  </div>
                  <div class="aui-flex-box" :class="activeIndex === 'phoneLogin' ? 'activeNav on' : ''" @click="loginClick('phoneLogin')"
                    >{{ t('sys.login.mobileSignInFormTitle') }}
                  </div>
                </div>
                <div class="aui-form-box" style="height: 240px">
                  <a-form ref="loginRef" :model="formData" v-if="activeIndex === 'accountLogin'" @keyup.enter.native="loginHandleClick">
                    <div class="aui-account">
                      <div class="aui-inputClear">
                        <i class="icon icon-code"></i>
                        <a-form-item>
                          <a-input class="fix-auto-fill" :placeholder="t('sys.login.userName')" v-model:value="formData.username" />
                        </a-form-item>
                      </div>
                      <div class="aui-inputClear">
                        <i class="icon icon-password"></i>
                        <a-form-item>
                          <a-input class="fix-auto-fill" type="password" :placeholder="t('sys.login.password')" v-model:value="formData.password" />
                        </a-form-item>
                      </div>
                      <div class="aui-inputClear">
                        <i class="icon icon-code"></i>
                        <a-form-item>
                          <a-input class="fix-auto-fill" type="text" :placeholder="t('sys.login.inputCode')" v-model:value="formData.inputCode" />
                        </a-form-item>
                        <div class="aui-code">
                          <img v-if="randCodeData.requestCodeSuccess" :src="randCodeData.randCodeImage" @click="handleChangeCheckCode" />
                          <img v-else style="margin-top: 2px; max-width: initial" :src="codeImg" @click="handleChangeCheckCode" />
                        </div>
                      </div>
                      <div class="aui-inputClear" v-if="showDepart">
                        <i class="icon icon-depart"></i>
                        <div class="JLoginSelectDept">
                          <a-select allow-clear style="width: 100%" :bordered="false" v-model:value="formData.loginOrgCode" :placeholder="t('sys.login.loginOrgCode')">
                            <template #suffixIcon>
                              <Icon icon="ant-design:gold-outline" />
                            </template>
                            <template v-for="depart in departList" :key="depart.orgCode">
                              <a-select-option :value="depart.orgCode">{{ getShortDeptName(depart.label) }}</a-select-option>
                            </template>
                          </a-select>
                        </div>
                      </div>
                      <div class="aui-flex">
                        <div class="aui-flex-box">
                          <div class="aui-choice">
                            <a-checkbox v-model:checked="rememberMe">{{ t('sys.login.rememberMe') }}</a-checkbox>
                          </div>
                        </div>
                        <div class="aui-forget">
                          <a @click="forgetHandelClick"> {{ t('sys.login.forgetPassword') }}</a>
                        </div>
                      </div>
                    </div>
                  </a-form>
                  <a-form v-else ref="phoneFormRef" :model="phoneFormData" @keyup.enter.native="loginHandleClick">
                    <div class="aui-account phone">
                      <div class="aui-inputClear phoneClear">
                        <a-input class="fix-auto-fill" :placeholder="t('sys.login.mobile')" v-model:value="phoneFormData.mobile" />
                      </div>
                      <div class="aui-inputClear">
                        <a-input class="fix-auto-fill" :maxlength="6" :placeholder="t('sys.login.smsCode')" v-model:value="phoneFormData.smscode" />
                        <div v-if="showInterval" class="aui-code" @click="getLoginCode">
                          <a>{{ t('component.countdown.normalText') }}</a>
                        </div>
                        <div v-else class="aui-code">
                          <span class="aui-get-code code-shape">{{ t('component.countdown.sendText', [unref(timeRuning)]) }}</span>
                        </div>
                      </div>
                      <div class="aui-inputClear" v-if="showDepart">
                        <div class="JLoginSelectDept">
                          <a-select allow-clear style="width: 100%" :bordered="false" v-model:value="phoneFormData.loginOrgCode" :placeholder="t('sys.login.loginOrgCode')">
                            <template #suffixIcon>
                              <Icon icon="ant-design:gold-outline" />
                            </template>
                            <template v-for="depart in departList" :key="depart.orgCode">
                              <a-select-option :value="depart.orgCode">{{ getShortDeptName(depart.label) }}</a-select-option>
                            </template>
                          </a-select>
                        </div>
                      </div>
                    </div>
                  </a-form>
                </div>
                <div class="aui-formButton">
                  <div class="aui-flex">
                    <a-button :loading="loginLoading" class="aui-link-login" type="primary" @click="loginHandleClick">
                      {{ t('sys.login.loginButton') }}</a-button>
                  </div>
                  <div class="aui-flex">
                    <a class="aui-linek-code aui-flex-box" @click="codeHandleClick">{{ t('sys.login.qrSignInFormTitle') }}</a>
                  </div>
                  <div class="aui-flex">
                    <a class="aui-linek-code aui-flex-box" @click="registerHandleClick">{{ t('sys.login.registerButton') }}</a>
                  </div>
                </div>
              </div>
              <a-form @keyup.enter.native="loginHandleClick">
                <div class="aui-flex aui-third-text">
                  <div class="aui-flex-box aui-third-border">
                    <span>{{ t('sys.login.otherSignIn') }}</span>
                  </div>
                </div>
                <div class="aui-flex" :class="`${prefixCls}-sign-in-way`">
                  <div class="aui-flex-box">
                    <div class="aui-third-login">
                      <a title="github" @click="onThirdLogin('github')"><GithubFilled /></a>
                    </div>
                  </div>
                  <div class="aui-flex-box">
                    <div class="aui-third-login">
                      <a title="WeCom" @click="onThirdLogin('wechat_enterprise')"><icon-font class="item-icon" type="icon-qiyeweixin3" /></a>
                    </div>
                  </div>
                  <div class="aui-flex-box">
                    <div class="aui-third-login">
                      <a title="DingTalk" @click="onThirdLogin('dingtalk')"><DingtalkCircleFilled /></a>
                    </div>
                  </div>
                  <div class="aui-flex-box">
                    <div class="aui-third-login">
                      <a title="WeChat" @click="onThirdLogin('wechat_open')"><WechatFilled /></a>
                    </div>
                  </div>
                </div>
              </a-form>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="forgotLoaded" v-show="type === 'forgot'" :class="`${prefixCls}-form`">
      <MiniForgotpad ref="forgotRef" @go-back="goBack" @success="handleSuccess" />
    </div>
    <div v-if="registerLoaded" v-show="type === 'register'" :class="`${prefixCls}-form`">
      <MiniRegister ref="registerRef" @go-back="goBack" @success="handleSuccess" />
    </div>
    <div v-if="codeLoginLoaded" v-show="type === 'codeLogin'" :class="`${prefixCls}-form`">
      <MiniCodelogin ref="codeRef" @go-back="goBack" @success="handleSuccess" />
    </div>
    <!-- Các hộp thoại liên quan đến đăng nhập bên thứ ba -->
    <ThirdModal ref="thirdModalRef"></ThirdModal>

    <!-- Hộp thoại mã xác thực hình ảnh -->
    <CaptchaModal @register="captchaRegisterModal" @ok="getLoginCode" />
  </div>
</template>
<script lang="ts" setup name="login-mini">
  import { getCaptcha, getCodeInfo } from '/@/api/sys/user';
  import { computed, defineAsyncComponent, onMounted, reactive, ref, toRaw, unref, watch } from 'vue';
  import codeImg from '/@/assets/images/checkcode.png';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { SmsEnum } from '/@/views/sys/login/useLogin';
  import ThirdModal from '/@/views/sys/login/ThirdModal.vue';
  const MiniForgotpad = defineAsyncComponent(() => import('./MiniForgotpad.vue'));
  const MiniRegister = defineAsyncComponent(() => import('./MiniRegister.vue'));
  const MiniCodelogin = defineAsyncComponent(() => import('./MiniCodelogin.vue'));
  import logoImg from '/@/assets/loginmini/icon/jeecg_logo.png';
  import adTextImg from '/@/assets/loginmini/icon/jeecg_ad_text.png';
  import { AppLocalePicker, AppDarkModeToggle } from '/@/components/Application';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { createLocalStorage } from '/@/utils/cache';
  import { useDesign } from "/@/hooks/web/useDesign";
  import { useAppInject } from "/@/hooks/web/useAppInject";
  import { GithubFilled, WechatFilled, DingtalkCircleFilled } from '@ant-design/icons-vue';
  import '/@/utils/iconfont2';
  import CaptchaModal from '@/components/jeecg/captcha/CaptchaModal.vue';
  import { useModal } from "@/components/Modal";
  import { ExceptionEnum } from "@/enums/exceptionEnum";
  import { encryptAESCBC } from '/@/utils/cipher';
  import { defHttp } from "@/utils/http/axios";
  import { IconFont } from '/@/utils/iconfont2';

  const { prefixCls } = useDesign('mini-login');
  const { notification, createMessage } = useMessage();
  const userStore = useUserStore();
  const { t } = useI18n();
  const $ls = createLocalStorage();
  const localeStore = useLocaleStore();
  const showLocale = localeStore.getShowPicker;
  const randCodeData = reactive<any>({
    randCodeImage: '',
    requestCodeSuccess: false,
    checkKey: null,
  });
  // Ghi nhớ tên người dùng
  const rememberMe = ref<boolean>(false);
  const REMEMBER_USERNAME_KEY = 'LOGIN_REMEMBER_USERNAME';
  //Đăng nhập bằng số điện thoại hay đăng nhập bằng tài khoản
  const activeIndex = ref<string>('accountLogin');
  const type = ref<string>('login');
  //Trường của biểu mẫu đăng nhập bằng tài khoản
  const formData = reactive<any>({
    inputCode: '',
    username: 'admin',
    password: '123456',
    loginOrgCode: '',
  });
  //Trường của biểu mẫu đăng nhập bằng điện thoại
  const phoneFormData = reactive<any>({
    mobile: '',
    smscode: '',
    loginOrgCode: '',
  });
  const loginRef = ref();
  //Hộp thoại đăng nhập bên thứ ba
  const thirdModalRef = ref();
  //Đăng nhập bằng quét mã
  const codeRef = ref();
  //Có hiển thị lấy mã xác thực không
  const showInterval = ref<boolean>(true);
  //60s
  const timeRuning = ref<number>(60);
  //Bộ hẹn giờ
  const timer = ref<any>(null);
  //Quên mật khẩu
  const forgotRef = ref();
  //Đăng ký
  const registerRef = ref();
  const loginLoading = ref<boolean>(false);
  // Cờ kiểm soát tải lười của ba component dưới đây, chỉ gắn kết khi nhấn vào
  const forgotLoaded = ref<boolean>(false);
  const registerLoaded = ref<boolean>(false);
  const codeLoginLoaded = ref<boolean>(false);
  const { getIsMobile } = useAppInject();
  const [captchaRegisterModal, { openModal: openCaptchaModal }] = useModal();
  defineProps({
    sessionTimeout: {
      type: Boolean,
    },
  });
 //**********************Logic truy vấn phòng banbegin**********************************************
  //Phòng ban của người dùng
  const departList = ref([]);
  //Hiển thị phòng ban
  const showDepart = computed(()=>{
    return departList.value.length > 1
  })
  //Lấy tên viết tắt phòng ban
  const getShortDeptName = computed(()=>{
    return (deptName) => {
      if (!deptName) return '';
      if (deptName.length > 18) {
        return '...' + deptName.substring(deptName.length-18, deptName.length) ;
      }
      return deptName;
    };
  })
  //Theo dõi thay đổi của mã xác thực và ô nhập
  watch(
      () => [formData.inputCode, phoneFormData.smscode],
      () => {
        if ((formData.inputCode && formData.inputCode.length == 4)
            || (phoneFormData.smscode && phoneFormData.smscode.length == 6)) {
            checkAccount()
        }
      },
  );
  /**
   * Theo dõi thay đổi tài khoản, xóa thông tin phòng ban
   */
  watch(
      () => [formData.username,phoneFormData.mobile,activeIndex.value],
      () => {
        formData.loginOrgCode = null;
        phoneFormData.loginOrgCode = null;
        departList.value = [];
        if ((formData.inputCode && formData.inputCode.length == 4)
            || (phoneFormData.smscode && phoneFormData.smscode.length == 6)) {
          checkAccount()
        }
      }
  );

  //Khởi tạo dữ liệu
  let deptTimer;
  function checkAccount() {
    deptTimer && clearTimeout(deptTimer);
    deptTimer = setTimeout(async () => {
      let loginType = activeIndex.value === 'accountLogin' ? 'account' : 'phone';
      // Trích xuất điều kiện xác thực
      const isValidAccount = loginType === 'account' && formData.username && formData.password;
      const isValidPhone = loginType == 'phone' && phoneFormData.mobile && phoneFormData.smscode;
      let finalFormData = loginType == 'phone' ? {...phoneFormData} : {...formData};
      if (!isValidAccount && !isValidPhone) {
        return;
      }
      //Trước khi truy vấn thông tin phòng ban, ưu tiên kiểm tra tài khoản
      if (departList.value && departList.value.length == 0) {
        let params = {...finalFormData, loginType: activeIndex.value === 'accountLogin' ? 'account' : 'phone'};
        if (loginType == 'account') {
          params['password'] = encryptAESCBC(formData.password);
          params['checkKey'] = randCodeData.checkKey;
        }
        const res = await defHttp.post({
          url: '/sys/loginGetUserDeparts',
          params: {...params}
        }, {isTransformResponse: false});
        if (res.success && res.result) {
          let {departs,currentOrgCode} = res.result;
          // Kiểm tra phòng ban hiện tại có nằm trong danh sách phòng ban trực thuộc hay không
          if (departs && departs.length > 0) {
            // Giải thích logic mã: JHHB-790 Khi phòng ban của người dùng thay đổi sẽ xuất hiện tình huống này (vì cài đặt ở đây trước đây chỉ chuyển đổi phòng ban, đã lọc bỏ thông tin công ty và vị trí)
            const hasCurrentDepart = departs.some(item => item.orgCode == currentOrgCode);
            formData.loginOrgCode = hasCurrentDepart?currentOrgCode:null;
            phoneFormData.loginOrgCode = hasCurrentDepart?currentOrgCode:null;
            departList.value = departs.map((item) => {
              return {
                label: item.departName,
                value: item.orgCode,
                orgCode: item.orgCode,
                departName: item.departName,
              };
            });
          }
        } else {
          //createMessage.warn(res.message);
        }
      }
    },500)
  }
 //**********************Logic truy vấn phòng banend*************************************************
  /**
   * Lấy mã xác thực
   */
  function handleChangeCheckCode() {
    formData.inputCode = '';
    // Giải thích logic mã: [QQYUN-10775]Mã xác thực có thể tái sử dụng #7674------------
    randCodeData.checkKey = new Date().getTime() + Math.random().toString(36).slice(-4); // 1629428467008;
    getCodeInfo(randCodeData.checkKey).then((res) => {
      randCodeData.randCodeImage = res;
      randCodeData.requestCodeSuccess = true;
    });
  }

  /**
   * Chuyển đổi cách đăng nhập
   */
  function loginClick(type) {
    activeIndex.value = type;
  }

  /**
   * Đăng nhập bằng tài khoản hoặc điện thoại
   */
  async function loginHandleClick() {
    if (unref(activeIndex) === 'accountLogin') {
      accountLogin();
    } else {
      //Đăng nhập bằng số điện thoại
      phoneLogin();
    }
  }

  async function accountLogin() {
    if (!formData.username) {
      createMessage.warn(t('sys.login.accountPlaceholder'));
      return;
    }
    if (!formData.password) {
      createMessage.warn(t('sys.login.passwordPlaceholder'));
      return;
    }
    try {
      loginLoading.value = true;

      // Mật khẩu được truyền bằng mã hóa AES
      const encryptedPassword = encryptAESCBC(formData.password);
      const { userInfo } = await userStore.login(
        toRaw({
          password: encryptedPassword,
          username: formData.username,
          loginOrgCode: formData.loginOrgCode,
          captcha: formData.inputCode,
          checkKey: randCodeData.checkKey,
          mode: 'none', //Không dùng thông báo lỗi mặc định
        })
      );
      if (userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${userInfo.realname}`,
          duration: 3,
        });
        // Sau khi đăng nhập thành công, xử lý ghi nhớ tên người dùng
        if (rememberMe.value && formData.username) {
          $ls.set(REMEMBER_USERNAME_KEY, formData.username)
        } else {
          $ls.remove(REMEMBER_USERNAME_KEY)
        }
      }
    } catch (error) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error.message || t('sys.login.networkExceptionMsg'),
        duration: 3,
      });
      handleChangeCheckCode();
    } finally {
      loginLoading.value = false;
    }
  }

  /**
   * Đăng nhập bằng số điện thoại
   */
  async function phoneLogin() {
    if (!phoneFormData.mobile) {
      createMessage.warn(t('sys.login.mobilePlaceholder'));
      return;
    }
    if (!phoneFormData.smscode) {
      createMessage.warn(t('sys.login.smsPlaceholder'));
      return;
    }
    try {
      loginLoading.value = true;
      const { userInfo }: any = await userStore.phoneLogin({
        mobile: phoneFormData.mobile,
        captcha: phoneFormData.smscode,
        loginOrgCode: phoneFormData.loginOrgCode,
        mode: 'none', //Không dùng thông báo lỗi mặc định
      });
      if (userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${userInfo.realname}`,
          duration: 3,
        });
      }
    } catch (error) {
      notification.error({
        message: t('sys.api.errorTip'),
        description: error.message || t('sys.login.networkExceptionMsg'),
        duration: 3,
      });
    } finally {
      loginLoading.value = false;
    }
  }

  /**
   * Lấy mã xác thực điện thoại
   */
  async function getLoginCode() {
    if (!phoneFormData.mobile) {
      createMessage.warn(t('sys.login.mobilePlaceholder'));
      return;
    }
    // Giải thích logic mã: 【issues/8567】Nghiêm trọng: đổi mật khẩu tồn tại lỗ hổng vượt quyền ngang: đăng nhập phải dùng mẫu đăng nhập, không được dùng mẫu quên mật khẩu---
    const result = await getCaptcha({ mobile: phoneFormData.mobile, smsmode: SmsEnum.LOGIN }).catch((res) =>{
      if(res.code === ExceptionEnum.PHONE_SMS_FAIL_CODE){
        openCaptchaModal(true, {});
      }
    });
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
   * Đăng nhập bên thứ ba
   * @param type
   */
  function onThirdLogin(type) {
    thirdModalRef.value.onThirdLogin(type);
  }

  /**
   * Quên mật khẩu
   */
  function forgetHandelClick() {
    forgotLoaded.value = true;
    type.value = 'forgot';
    setTimeout(() => {
      forgotRef.value?.initForm();
    }, 300);
  }

  /**
   * Quay lại trang đăng nhập
   */
  function goBack() {
    activeIndex.value = 'accountLogin';
    type.value = 'login';
  }

  /**
   * Sự kiện callback quên mật khẩu/đăng ký tài khoản
   * @param value
   */
  function handleSuccess(value) {
    Object.assign(formData, value);
    Object.assign(phoneFormData, { mobile: "", smscode: "" });
    type.value = 'login';
    activeIndex.value = 'accountLogin';
    handleChangeCheckCode();
  }

  /**
   * Đăng ký
   */
  function registerHandleClick() {
    registerLoaded.value = true;
    type.value = 'register';
    setTimeout(() => {
      registerRef.value?.initForm();
    }, 300);
  }

  /**
   * Đăng ký
   */
  function codeHandleClick() {
    codeLoginLoaded.value = true;
    type.value = 'codeLogin';
    setTimeout(() => {
      codeRef.value?.initFrom();
    }, 300);
  }

  onMounted(() => {
    //Tải mã xác thực
    handleChangeCheckCode();
    // Khôi phục tên người dùng đã ghi nhớ
    const saved = $ls.get(REMEMBER_USERNAME_KEY);
    if (saved) {
      formData.username = saved;
      rememberMe.value = true;
    }
  });
</script>

<style lang="less" scoped>
  @import '/@/assets/loginmini/style/home.less';
  @import '/@/assets/loginmini/style/base.less';

  :deep(.ant-input:focus) {
    box-shadow: none;
  }
  .aui-get-code {
    float: right;
    position: relative;
    z-index: 3;
    background: #ffffff;
    color: #1573e9;
    border-radius: 100px;
    padding: 5px 16px;
    margin: 7px;
    border: 1px solid #1573e9;
    top: 12px;
  }

  .aui-get-code:hover {
    color: #1573e9;
  }

  .code-shape {
    border-color: #dadada !important;
    color: #aaa !important;
  }

  :deep(.jeecg-dark-switch){
    position:absolute;
    margin-right: 10px;
  }
  .aui-link-login{
    height: 42px;
    padding: 10px 15px;
    font-size: 14px;
    border-radius: 8px;
    margin-top: 15px;
    margin-bottom: 8px;
    flex: 1;
    color: #fff;
  }
  .aui-phone-logo{
    position: absolute;
    margin-left: 10px;
    width: 60px;
    top:2px;
    z-index: 4;
  }
  .top-3{
    top: 0.45rem;
  }
  .JLoginSelectDept {
    margin:5px auto;
    :deep(.ant-select-selection-placeholder) {
      font-size: 14px;
      color: #9a9a9a;
    }
  }
</style>

<style lang="less">
@prefix-cls: ~'@{namespace}-mini-login';
@dark-bg: #293146;

html[data-theme='dark'] {
  .@{prefix-cls} {
    background-color: @dark-bg !important;
    background-image: none;

    &::before {
      background-image: url(/@/assets/svg/login-bg-dark.svg);
    }
    .aui-inputClear{
      background-color: #232a3b !important;
    }
    .ant-input,
    .ant-input-password {
      background-color: #232a3b !important;
    }

    .ant-btn:not(.ant-btn-link):not(.ant-btn-primary) {
      border: 1px solid #4a5569 !important;
    }

    &-form {
      background: @dark-bg !important;
    }

    .app-iconify {
      color: #fff !important;
    }
    .aui-inputClear input,.aui-input-line input,.aui-choice{
      color: #c9d1d9 !important;
    }

    .aui-formBox{
      background-color: @dark-bg !important;
    }
    .aui-third-text span{
      background-color: @dark-bg !important;
    }
    .aui-form-nav .aui-flex-box{
      color: #c9d1d9 !important;
    }

    .aui-formButton .aui-linek-code{
      background:  @dark-bg !important;
      color: white !important;
    }
    .aui-code-line{
      border-left: none !important;
    }
    .ant-checkbox-inner,.aui-success h3{
      border-color: #c9d1d9;
    }
    // Giải thích logic mã: 【QQYUN-6363】Đoạn mã kiểu này có vấn đề, không nằm ở bên trong, khiến biểu thức gặp vấn đề------------
    &-sign-in-way {
      .anticon {
        font-size: 22px !important;
        color: #888 !important;
        cursor: pointer !important;

        &:hover {
          color: @primary-color !important;
        }
      }
    }
  }

  input.fix-auto-fill,
  .fix-auto-fill input {
    -webkit-text-fill-color: #c9d1d9 !important;
    box-shadow: inherit !important;
  }

  .ant-divider-inner-text {
    font-size: 12px !important;
    color: @text-color-secondary !important;
  }
  .aui-third-login a{
    background: transparent;
  }
}
</style>
