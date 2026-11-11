<template>
  <div> </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { isOAuth2AppEnv, sysOAuth2Callback, sysOAuth2Login } from '/@/views/sys/login/useLogin';
  import { useRouter } from 'vue-router';
  import { PageEnum } from '/@/enums/pageEnum';
  import { router } from '/@/router';
  import { useUserStore } from '/@/store/modules/user';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { getAuthCache, getTenantId, getToken } from "/@/utils/auth";
  import { requestAuthCode } from 'dingtalk-jsapi';
  import { defHttp } from '/@/utils/http/axios';
  import { OAUTH2_THIRD_LOGIN_TENANT_ID } from "/@/enums/cacheEnum";

  const isOAuth = ref<boolean>(isOAuth2AppEnv());
  const env = ref<any>({ thirdApp: false, wxWork: false, dingtalk: false, feishu: false });
  const { currentRoute } = useRouter();
  const route = currentRoute.value;
  if (!isOAuth2AppEnv()) {
    router.replace({ path: PageEnum.BASE_LOGIN, query: route.query });
  }

  if (isOAuth.value) {
    checkEnv();
  }

  /**
   * Kiểm tra môi trường hiện tại
   */
  function checkEnv() {
    // Kiểm tra tại thời điểm đó có phải môi trường WeCom hay không
    if (/wxwork/i.test(navigator.userAgent)) {
      env.value.thirdApp = true;
      env.value.wxWork = true;
    }
    // Kiểm tra tại thời điểm đó có phải môi trường DingTalk hay không
    if (/dingtalk/i.test(navigator.userAgent)) {
      env.value.thirdApp = true;
      env.value.dingtalk = true;
    }
    // Kiểm tra hiện có phải môi trường Feishu hay không
    if (/lark|feishu/i.test(navigator.userAgent)) {
      env.value.thirdApp = true;
      env.value.feishu = true;
    }
    doOAuth2Login();
  }

  /**
   * Thực hiện thao tác đăng nhập OAuth2
   */
  function doOAuth2Login() {
    if (env.value.thirdApp) {
      // Kiểm tra có mang theo Token hay không, nếu có nghĩa là đăng nhập thành công
      if (route.query.oauth2LoginToken) {
        let token = route.query.oauth2LoginToken;
        //Thực hiện thao tác đăng nhập
        thirdLogin({ token, thirdType: route.query.thirdType,tenantId: getTenantId });
      } else if (env.value.wxWork) {
        sysOAuth2Login('wechat_enterprise');
      } else if (env.value.dingtalk) {
        //Đăng nhập DingTalk phiên bản mới
        dingdingLogin();
      } else if (env.value.feishu) {
        sysOAuth2Login('feishu');
      }
    }
  }

  /**
   * Đăng nhập bên thứ ba
   * @param params
   */
  function thirdLogin(params) {
    const userStore = useUserStore();
    const { notification } = useMessage();
    const { t } = useI18n();
    userStore.ThirdLogin(params).then((res) => {
      if (res && res.userInfo) {
        notification.success({
          message: t('sys.login.loginSuccessTitle'),
          description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
          duration: 3,
        });
      } else {
        notification.error({
          message: t('sys.login.errorTip'),
          description: ((res.response || {}).data || {}).message || res.message || t('sys.login.networkExceptionMsg'),
          duration: 4,
        });
      }
    });
  }

  /**
   * Đăng nhập DingTalk
   */
  function dingdingLogin() {
    //Trước tiên lấy ID doanh nghiệp của DingTalk, nếu chưa cấu hình thì vẫn theo logic cũ; theo logic cũ cần kiểm tra token có tồn tại hay không, nếu có token thì đi thẳng tới trang chủ
    let tenantId = getAuthCache(OAUTH2_THIRD_LOGIN_TENANT_ID) || 0;
    let url = `/sys/thirdLogin/get/corpId/clientId?tenantId=${tenantId}`;
    // Giải thích logic mã: Không dùng getAction trong online, phải dùngdefHttp---
    defHttp.get({ url:url },{ isTransformResponse: false }).then((res) => {
        if (res.success) {
          if(res.result && res.result.corpId && res.result.clientId){
            requestAuthCode({ corpId: res.result.corpId, clientId: res.result.clientId }).then((res) => {
              let { code } = res;
              sysOAuth2Callback(code);
            });
          }else{
            toOldAuthLogin();
          }
        } else {
          toOldAuthLogin();
        }
      }).catch((err) => {
        toOldAuthLogin();
      });
  }
  
  /**
   * Đăng nhập DingTalk phiên bản cũ
   */
  function toOldAuthLogin() {
    let token = getToken();
    if (token) {
      router.replace({ path: PageEnum.BASE_HOME });
    } else {
      sysOAuth2Login('dingtalk');
    }
  }
</script>
