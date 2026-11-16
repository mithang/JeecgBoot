<template>
  <div :class="prefixCls" class="relative w-full h-full px-4">
    <AppLocalePicker class="absolute text-white top-4 right-4 enter-x xl:text-gray-600" :showText="false" v-if="!sessionTimeout && showLocale" />
    <AppDarkModeToggle class="absolute top-3 right-7 enter-x" v-if="!sessionTimeout" />
    <div class="-enter-x xl:hidden login-brand-strip">
      <AppLogo :alwaysShowTitle="true" />
      <p class="mt-1 text-xs text-white/85">{{ t('sys.login.signInTagline') }}</p>
    </div>

    <div class="container relative h-full py-2 mx-auto sm:px-10">
      <div class="flex h-full">
        <div class="hidden min-h-full pl-4 mr-4 xl:flex xl:flex-col xl:w-6/12">
          <AppLogo class="-enter-x" />
          <div class="my-auto">
            <img :alt="title" src="../../../assets/svg/login-brand-banner.svg" class="w-10/12 -enter-x" />
            <div class="mt-8 font-medium text-white -enter-x">
              <span class="inline-block mt-4 text-3xl"> {{ t('sys.login.signInTitle') }}</span>
            </div>
            <div class="mt-4 font-normal text-white text-md dark:text-gray-500 -enter-x">
              {{ t('sys.login.signInDesc') }}
            </div>
            <ul class="mt-6 space-y-3 -enter-x">
              <li v-for="key in loginFeatures" :key="key" class="flex items-start gap-2 text-sm text-white/90">
                <CheckCircleFilled class="mt-0.5 shrink-0 text-base" style="color: #9db9f5" />
                <span>{{ t(key) }}</span>
              </li>
            </ul>
          </div>
        </div>
        <div class="flex w-full h-full py-5 xl:h-auto xl:py-0 xl:my-0 xl:w-6/12">
          <div
            :class="`${prefixCls}-form`"
            class="relative w-full px-5 py-8 mx-auto my-auto rounded-md shadow-md xl:ml-16 xl:bg-transparent sm:px-8 xl:p-4 xl:shadow-none sm:w-3/4 lg:w-2/4 xl:w-auto enter-x"
          >
            <LoginForm />
            <ForgetPasswordForm />
            <RegisterForm />
            <MobileForm />
            <QrCodeForm />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed } from 'vue';
  import { CheckCircleFilled } from '@ant-design/icons-vue';
  import { AppLogo } from '/@/components/Application';
  import { AppLocalePicker, AppDarkModeToggle } from '/@/components/Application';
  import LoginForm from './LoginForm.vue';
  import ForgetPasswordForm from './ForgetPasswordForm.vue';
  import RegisterForm from './RegisterForm.vue';
  import MobileForm from './MobileForm.vue';
  import QrCodeForm from './QrCodeForm.vue';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { useLoginState, LoginStateEnum } from './useLogin';
  defineProps({
    sessionTimeout: {
      type: Boolean,
    },
  });

  const globSetting = useGlobSetting();
  const { prefixCls } = useDesign('login');
  const { t } = useI18n();
  const localeStore = useLocaleStore();
  const showLocale = localeStore.getShowPicker;
  const title = computed(() => globSetting?.title ?? '');
  const { handleBackLogin } = useLoginState();
  handleBackLogin();
  // 3 điểm nổi bật của nền tảng hiển thị ở panel thương hiệu (Phase 4 Bước 6)
  const loginFeatures = [
    'sys.login.signInFeature1',
    'sys.login.signInFeature2',
    'sys.login.signInFeature3',
  ];
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-login';
  @logo-prefix-cls: ~'@{namespace}-app-logo';
  @countdown-prefix-cls: ~'@{namespace}-countdown-input';
  @dark-bg: #293146;

  html[data-theme='dark'] {
    .@{prefix-cls} {
      background-color: @dark-bg;

      &::before {
        background-image: url(/@/assets/svg/login-bg-dark.svg);
      }

      .ant-input,
      .ant-input-password {
        background-color: #232a3b;
      }

      .ant-btn:not(.ant-btn-link):not(.ant-btn-primary) {
        border: 1px solid #4a5569;
      }

      &-form {
        background: transparent !important;
      }

      .app-iconify {
        color: #fff;
      }
    }

    input.fix-auto-fill,
    .fix-auto-fill input {
      -webkit-text-fill-color: #c9d1d9 !important;
      box-shadow: inherit !important;
    }
  }

  .@{prefix-cls} {
    min-height: 100%;
    overflow: hidden;
    @media (max-width: @screen-xl) {
      background-color: #293146;

      .@{prefix-cls}-form {
        background-color: #fff;
      }
    }

    // Dải banner thương hiệu trên mobile (Phase 4 Bước 6): logo + tagline
    // trên nền gradient thương hiệu, thay cho logo trần trước đây.
    .login-brand-strip {
      margin-top: 12px;
      padding: 12px 16px 10px;
      background: linear-gradient(120deg, #0a3d91 0%, #0b57d0 55%, #2e6fe8 100%);
      border-radius: 12px;

      .@{logo-prefix-cls} {
        position: static;
        height: auto;

        &__title {
          font-size: 18px;
          color: #fff;
        }

        img {
          width: 36px;
        }
      }
    }

    &::before {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      margin-left: -48%;
      background-image: url(/@/assets/svg/login-bg.svg);
      background-position: 100%;
      background-repeat: no-repeat;
      background-size: auto 100%;
      content: '';
      @media (max-width: @screen-xl) {
        display: none;
      }
    }

    .@{logo-prefix-cls} {
      position: absolute;
      top: 12px;
      height: 30px;

      &__title {
        font-size: 16px;
        color: #fff;
      }

      img {
        width: 32px;
      }
    }

    .container {
      .@{logo-prefix-cls} {
        display: flex;
        width: 60%;
        height: 80px;

        &__title {
          font-size: 24px;
          color: #fff;
        }

        img {
          width: 48px;
        }
      }
    }

    &-sign-in-way {
      .anticon {
        font-size: 22px;
        color: #888;
        cursor: pointer;

        &:hover {
          color: @primary-color;
        }
      }
    }

    input:not([type='checkbox']) {
      min-width: 360px;

      @media (max-width: @screen-xl) {
        min-width: 320px;
      }

      @media (max-width: @screen-lg) {
        min-width: 260px;
      }

      @media (max-width: @screen-md) {
        min-width: 240px;
      }

      @media (max-width: @screen-sm) {
        min-width: 160px;
      }
    }

    .@{countdown-prefix-cls} input {
      min-width: unset;
    }

    .ant-divider-inner-text {
      font-size: 12px;
      color: @text-color-secondary;
    }
  }
</style>
