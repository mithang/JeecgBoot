<template>
  <Footer :class="prefixCls" v-if="getShowLayoutFooter" ref="footerRef">
    <div :class="`${prefixCls}__links`">
      <a @click="openWindow(SITE_URL)">{{ t('layout.footer.onlinePreview') }}</a>

      <GithubFilled @click="openWindow(GITHUB_URL)" :class="`${prefixCls}__github`" />

      <a @click="openWindow(DOC_URL)">{{ t('layout.footer.onlineDocument') }}</a>
    </div>
    <div>Copyright &copy;{{ currentYear }} Sản phẩm của {{ COPYRIGHT_COMPANY }}</div>
  </Footer>
</template>

<script lang="ts">
  import { computed, defineComponent, unref, ref } from 'vue';
  import { Layout } from 'ant-design-vue';

  import { GithubFilled } from '@ant-design/icons-vue';

  import { DOC_URL, GITHUB_URL, SITE_URL, COPYRIGHT_COMPANY } from '/@/settings/siteSetting';
  import { openWindow } from '/@/utils';

  import { useI18n } from '/@/hooks/web/useI18n';
  import { useRootSetting } from '/@/hooks/setting/useRootSetting';
  import { useRouter } from 'vue-router';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLayoutHeight } from '../content/useContentViewHeight';
  import { ThemeEnum } from '/@/enums/appEnum';

  export default defineComponent({
    name: 'LayoutFooter',
    components: { Footer: Layout.Footer, GithubFilled },
    setup() {
      const { t } = useI18n();
      const { getShowFooter } = useRootSetting();
      const { currentRoute } = useRouter();
      const { prefixCls } = useDesign('layout-footer');

      const footerRef = ref<ComponentRef>(null);
      const { setFooterHeight } = useLayoutHeight();
      // Theme hiện tại
      const { getDarkMode } = useRootSetting();
      const isDark = computed(() => getDarkMode.value === ThemeEnum.DARK);

      const getShowLayoutFooter = computed(() => {
        if (unref(getShowFooter)) {
          const footerEl = unref(footerRef)?.$el;
          setFooterHeight(footerEl?.offsetHeight || 0);
        } else {
          setFooterHeight(0);
        }
        return unref(getShowFooter) && !unref(currentRoute).meta?.hiddenFooter;
      });
      // Phase 4 Bước 5: năm bản quyền tự động theo năm hiện tại.
      const currentYear = new Date().getFullYear();
      // Cài đặt màu khi di chuột vào
      const hoverColor = computed(() => {
        return unref(isDark) ? 'rgba(255, 255, 255, 1)' : 'rgba(0, 0, 0, 0.85)';
      });
      return {
        getShowLayoutFooter,
        prefixCls,
        t,
        DOC_URL,
        GITHUB_URL,
        SITE_URL,
        COPYRIGHT_COMPANY,
        currentYear,
        openWindow,
        footerRef,
        hoverColor,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-layout-footer';

  @normal-color: rgba(0, 0, 0, 0.45);
  // Giải thích logic mã: [issues/608] Ở chế độ dark, kiểu hover của chữ ở footer phía dưới khiến chữ biến mất
  @hover-color: v-bind(hoverColor);
  .@{prefix-cls} {
    color: @normal-color;
    text-align: center;
    // Phase 4 Bước 5: đường phân cách nhẹ để footer tách khỏi nội dung, tạo chiều sâu cho khung.
    padding: 16px 24px 12px;
    border-top: 1px solid @border-color-split;
    background-color: transparent;

    &__links {
      margin-bottom: 8px;

      a {
        color: @normal-color;

        &:hover {
          color: @hover-color;
        }
      }
    }

    &__github {
      margin: 0 30px;

      &:hover {
        color: @hover-color;
      }
    }
  }
</style>
