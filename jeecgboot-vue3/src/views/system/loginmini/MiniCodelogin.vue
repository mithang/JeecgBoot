<template>
  <div class="aui-content">
    <div class="aui-container">
      <div class="aui-form">
        <div class="aui-image">
          <div class="aui-image-text">
            <img :src="adTextImg" alt="" />
          </div>
        </div>
        <div class="aui-formBox aui-formEwm">
          <div class="aui-formWell">
            <form>
              <div class="aui-flex aui-form-nav investment_title" style="padding-bottom: 19px">
                <div class="aui-flex-box activeNav">{{t('sys.login.qrSignInFormTitle')}}</div>
              </div>
              <div class="aui-form-box">
                <div class="aui-account" style="padding: 30px 0">
                  <div class="aui-ewm">
                    <QrCode :value="qrCodeUrl" class="enter-x flex justify-center xl:justify-start" :width="280" />
                  </div>
                </div>
              </div>
              <div class="aui-formButton">
                <a class="aui-linek-code aui-link-register" @click="goBackHandleClick">{{t('sys.login.backSignIn')}}</a>
              </div>
            </form>
          </div>
          <div class="aui-flex aui-third-text">
            <div class="aui-flex-box aui-third-border">
              <span>{{ t('sys.login.otherSignIn') }}</span>
            </div>
          </div>
          <div class="aui-flex" :class="`${prefixCls}-sign-in-way`">
            <div class="aui-flex-box">
              <div class="aui-third-login">
                <a href="" title="github" @click="onThirdLogin('github')"><GithubFilled /></a>
              </div>
            </div>
            <div class="aui-flex-box">
              <div class="aui-third-login">
                <a href="" title="WeCom" @click="onThirdLogin('wechat_enterprise')"><icon-font class="item-icon" type="icon-qiyeweixin3" /></a>
              </div>
            </div>
            <div class="aui-flex-box">
              <div class="aui-third-login">
                <a href="" title="DingTalk" @click="onThirdLogin('dingtalk')"><DingtalkCircleFilled /></a>
              </div>
            </div>
            <div class="aui-flex-box">
              <div class="aui-third-login">
                <a href="" title="WeChat" @click="onThirdLogin('wechat_open')"><WechatFilled /></a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <!-- Các hộp thoại liên quan đến đăng nhập bên thứ ba -->
  <ThirdModal ref="thirdModalRef"></ThirdModal>
</template>

<script lang="ts" setup name="mini-code-login">
  import { ref, onUnmounted } from 'vue';
  import { getLoginQrcode, getQrcodeToken } from '/@/api/sys/user';
  import { useUserStore } from '/@/store/modules/user';
  import { QrCode } from '/@/components/Qrcode/index';
  import ThirdModal from '/@/views/sys/login/ThirdModal.vue';
  import logoImg from '/@/assets/loginmini/icon/jeecg_logo.png';
  import adTextImg from '/@/assets/loginmini/icon/jeecg_ad_text.png';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from "/@/hooks/web/useDesign";
  import { GithubFilled, WechatFilled, DingtalkCircleFilled } from '@ant-design/icons-vue';
  import { IconFont } from '/@/utils/iconfont2';

  const { prefixCls } = useDesign('minilogin');
  const { t } = useI18n();
  const qrCodeUrl = ref<string>('');
  let timer: IntervalHandle;
  const state = ref('0');
  const thirdModalRef = ref();
  const userStore = useUserStore();
  const emit = defineEmits(['go-back', 'success', 'register']);

  //Tải thông tin mã QR
  function loadQrCode() {
    state.value = '0';
    getLoginQrcode().then((res) => {
      qrCodeUrl.value = res.qrcodeId;
      if (res.qrcodeId) {
        openTimer(res.qrcodeId);
      }
    });
  }
  //Theo dõi trạng thái quét mã
  function watchQrcodeToken(qrcodeId) {
    getQrcodeToken({ qrcodeId: qrcodeId }).then((res) => {
      let token = res.token;
      if (token == '-2') {
        //Mã QR hết hạn, lấy lại
        loadQrCode();
        clearInterval(timer);
      }
      //Quét mã thành công
      if (res.success) {
        state.value = '2';
        clearInterval(timer);
        setTimeout(() => {
          userStore.qrCodeLogin(token);
        }, 500);
      }
    });
  }

  /** Bật bộ hẹn giờ */
  function openTimer(qrcodeId) {
    watchQrcodeToken(qrcodeId);
    closeTimer();
    timer = setInterval(() => {
      watchQrcodeToken(qrcodeId);
    }, 1500);
  }

  /** Tắt bộ hẹn giờ */
  function closeTimer() {
    if (timer) clearInterval(timer);
  }

  /**
   * Đăng nhập bên thứ ba
   * @param type
   */
  function onThirdLogin(type) {
    thirdModalRef.value.onThirdLogin(type);
  }

  /**
   * Khởi tạo biểu mẫu
   */
  function initFrom() {
    loadQrCode();
  }

  /**
   * Quay lại
   */
  function goBackHandleClick() {
    emit('go-back');
    closeTimer();
  }

  onUnmounted(() => {
    closeTimer();
  });

  defineExpose({
    initFrom,
  });
</script>
<style lang="less" scoped>
@import '/@/assets/loginmini/style/home.less';
@import '/@/assets/loginmini/style/base.less';
</style>
