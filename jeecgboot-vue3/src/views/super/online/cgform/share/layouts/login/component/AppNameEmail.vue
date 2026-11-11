<template>
  <div class="name-email-subject">
    <AppLoginHeader />
<!--    <div  class="pointer" style="display: flex;margin-top: 40px" @click="backStepClick">-->
<!--      <Icon icon="ant-design:arrow-left-outlined" style="color: #757575; margin-top: 2px" />-->
<!--      <span style="margin-left: 10px">Quay lại</span>-->
<!--    </div>-->
    <div class="flex-row align-items-center margin-top40">
      <div class="register-title"> Vui lòng điền biệt danh và email </div>
    </div>
    <div class="name-email-desc align-items-center">
      <span style="color: #9e9e9e">Vui lòng điền biệt danh và email, để mọi người tiện liên hệ với bạn</span>
    </div>
    <div class="name-email-content">
      <a-form ref="formRef" :model="formState" :rules="getFormRules">
        <div class="content-item" :class="getUserNameClass" @click="formStateInputClick('realname')">
          <a-form-item name="realname">
            <a-input ref="realNameRef" v-model:value="formState.realname" style="height: 40px" @blur="formStateInputBlur" />
            <div class="form-title" :class="activekey === 'username' ? 'active-title' : ''"> Biệt danh </div>
          </a-form-item>
        </div>
        <div class="content-item" :class="getEmailClass" @click="formStateInputClick('email')">
          <a-form-item name="email">
            <a-input ref="emailRef" v-model:value="formState.email" style="height: 40px" @blur="formStateInputBlur" />
            <div class="form-title" :class="activekey === 'email' ? 'active-title' : ''"> {{ t('sys.login.email') }} </div>
          </a-form-item>
        </div>
        <div class=" pointer" @click="finshClick">
          <a-button type="primary" class="next-step" :loading="finshLoading">Hoàn thành</a-button>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script lang="ts" setup name="app-name-email">
  import AppLoginHeader from './AppLoginHeader.vue';
  import { ref, reactive, computed, unref, toRaw } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { register } from '/@/api/sys/user';
  import { useMessage } from "/@/hooks/web/useMessage";

  const formState = reactive({
    realname: '',
    email: '',
  });
  const { t } = useI18n();
  const realNameRef = ref();
  const emailRef = ref();
  //Thêm email và họ tên thật
  const formRef = ref();

  const getEmailFormRule = computed(() => [{required:true,message:'Vui lòng điền email',trigger: 'change'},{ type: 'email',message:'Vui lòng điền email đúng'}]);
  const getUsernameFormRule = computed(() => [{required:true,message:'Vui lòng điền biệt danh',trigger: 'change'}]);
  const getFormRules = computed(() => {
    return {
      realname: unref(getUsernameFormRule),
      email: unref(getEmailFormRule),
    };
  });
  //Giá trị đã chọn
  const activekey = ref<string>('');
  //Lấy kiểu dáng của họ tên thật
  const getUserNameClass = computed(() => {
    return formState.realname ? 'current-active' : '' || unref(activekey) === 'realname' ? 'current-active' : '';
  });
  //Lấy kiểu dáng của email
  const getEmailClass = computed(() => {
    return formState.email != '' ? 'current-active' : '' || unref(activekey) === 'email' ? 'current-active' : '';
  });
  const registerData = ref<any>({});
  const emit = defineEmits(['login-account', 'bind-third-account']);
  const { notification, createErrorModal } = useMessage();
  const finshLoading = ref<boolean>(false);

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

  /**
   * Sự kiện nhấp của component biểu mẫu
   */
  function formStateInputClick(val) {
    activekey.value = val;
    if (val === 'realname') {
      realNameRef.value.focus();
    } else {
      emailRef.value.focus();
    }
  }

  /**
   * Component biểu mẫu mất tiêu điểm (focus)
   */
  function formStateInputBlur() {
    activekey.value = '';
  }

  /**
   * Liên kết email
   */
  async function finshClick() {
    formRef.value.validateFields().then(async (values) => {
      //Hiện tại có phải là đăng ký bên thứ ba liên kết số điện thoại hay không,Nếu đúng là vậy thì đi theo logic liên kết số điện thoại, nếu không thì đăng ký trực tiếp
      if (!unref(registerData).bindThirdAccount) {
        try {
          finshLoading.value = true;
          const resultInfo = await register(
            toRaw({
              username: unref(registerData).phone,
              email: values.email,
              realname: values.realname,
              password: unref(registerData).password,
              phone: unref(registerData).phone,
              smscode: unref(registerData).smscode,
            })
          );
          if (resultInfo && resultInfo.data.success) {
            //Đổi mật khẩu
            emit('login-account', { account: unref(registerData).phone, password: unref(registerData).password });
            notification.success({
              message: undefined,
              description: resultInfo.data.message || t('sys.api.registerMsg'),
              duration: 3,
            });
          } else {
            notification.warning({
              message: t('sys.api.errorTip'),
              description: resultInfo.data.message || t('sys.api.networkExceptionMsg'),
              duration: 3,
            });
          }
        } catch (error) {
          notification.error({
            message: t('sys.api.errorTip'),
            description: error.message || t('sys.api.networkExceptionMsg'),
            duration: 3,
          });
        } finally {
          finshLoading.value = false;
        }
      } else {
        emit("bind-third-account",{  
          username: unref(registerData).phone,
          email: values.email,
          realname: values.realname,
          password: unref(registerData).password,
          phone: unref(registerData).phone,
          smscode: unref(registerData).smscode
        })
      }
    });
  }

  /**
   * Thiết lập dữ liệu đăng ký
   * @param values
   */
  function setRegisterData(values) {
    registerData.value = values;
    console.log(unref(registerData));
  }
  
  defineExpose({
    setRegisterData
  })
</script>

<style lang="less" scoped>
  .name-email-box {
    display: block;
  }
  .name-email-subject {
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
  .name-email-desc {
    margin-top: 10px;
    font-size: 15px;
  }
  .name-email-content {
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
  .next-step {
    font-weight: 600;
    width: 100%;
    height: 40px;
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
    margin-top: 48px;
  }
  .to-login {
    font-size: 14px;
    color: #2196f3;
    display: block;
    margin: 20px auto 0;
    text-align: center;
  }
  @media screen and (max-width: 414px) {
    .name-email-box {
      padding: 46px 16px;
    }
    .name-email-subject {
      margin: 0;
      padding: 48px 24px 23px;
    }
  }
</style>
