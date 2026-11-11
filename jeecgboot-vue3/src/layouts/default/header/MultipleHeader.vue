<template>
  <div :style="getPlaceholderDomStyle" v-if="getIsShowPlaceholderDom"></div>
  <div :style="getWrapStyle" :class="getClass">
    <LayoutHeader v-if="getShowHeader" />
    <MultipleTabs v-if="getShowTabs" />
  </div>
</template>
<script lang="ts">
  import { defineComponent, unref, computed, CSSProperties } from 'vue';

  import LayoutHeader from './index.vue';
  import MultipleTabs from '../tabs/index.vue';

  import { useAppStore } from "@/store/modules/app";
  import { useGlobSetting } from "/@/hooks/setting";
  import { useHeaderSetting } from '/@/hooks/setting/useHeaderSetting';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useFullContent } from '/@/hooks/web/useFullContent';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useLayoutHeight } from '../content/useContentViewHeight';
  import { TabsThemeEnum } from '/@/enums/appEnum';
  import { MenuTypeEnum } from '/@/enums/menuEnum';

  // Giải thích logic mã: Tăng chiều cao khu vực header của trang web
  const HEADER_HEIGHT = 60;

  // updateBy:sunjianlei---updateDate:2021-09-03---Sửa kiểu thanh chuyển tab: thay đổi chiều cao
  const TABS_HEIGHT = 32;
  const TABS_HEIGHT_CARD = 50;
  const TABS_HEIGHT_SMOOTH = 50;

  export default defineComponent({
    name: 'LayoutMultipleHeader',
    components: { LayoutHeader, MultipleTabs },
    setup() {
      const { setHeaderHeight } = useLayoutHeight();
      const { prefixCls } = useDesign('layout-multiple-header');

      const appStore = useAppStore()
      const glob = useGlobSetting()

      const { getCalcContentWidth, getSplit, getMenuType } = useMenuSetting();
      const { getIsMobile } = useAppInject();
      const { getFixed, getShowInsetHeaderRef, getShowFullHeaderRef, getHeaderTheme } = useHeaderSetting();

      const { getFullContent } = useFullContent();

      const { getShowMultipleTab, getTabsTheme } = useMultipleTabSetting();

      const getShowHeader = computed(() => {
        // Kiểm soát có hiển thị đầu trang không
        if (appStore.getLayoutHideHeader) {
          return false;
        }
        return unref(getShowInsetHeaderRef);
      })

      const getShowTabs = computed(() => {
        // Kiểm soát có hiển thị chuyển đổi nhiều tab không
        if (appStore.getLayoutHideMultiTabs) {
          return false;
        }
        return unref(getShowMultipleTab) && !unref(getFullContent);
      });

      const getIsShowPlaceholderDom = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      const getWrapStyle = computed((): CSSProperties => {
        const style: CSSProperties = {};
        if (unref(getFixed) && !glob.isQiankunMicro) {
          style.width = unref(getIsMobile) ? '100%' : unref(getCalcContentWidth);
        }
        if (unref(getShowFullHeaderRef)) {
          style.top = `${HEADER_HEIGHT}px`;
        }
        return style;
      });

      const getIsFixed = computed(() => {
        return unref(getFixed) || unref(getShowFullHeaderRef);
      });

      // updateBy:sunjianlei---updateDate:2021-09-08---Tính động chiều cao tab theo theme khác nhau
      const getTabsThemeHeight = computed(() => {
        let tabsTheme = unref(getTabsTheme);
        if (tabsTheme === TabsThemeEnum.CARD) {
          return TABS_HEIGHT_CARD;
        } else if (tabsTheme === TabsThemeEnum.SMOOTH) {
          return TABS_HEIGHT_SMOOTH;
        } else {
          return TABS_HEIGHT;
        }
      });

      const getPlaceholderDomStyle = computed((): CSSProperties => {
        let height = 0;
        // Giải thích logic mã: Khi chuyển theme sang chế độ hỗn hợp phía trên, nội dung đầu trang không hiển thị, bị che khuất
        if ((unref(getShowFullHeaderRef) || !unref(getSplit)) && unref(getShowHeader) && !unref(getFullContent) || unref(getMenuType) == MenuTypeEnum.MIX) {
          height += HEADER_HEIGHT;
        }
        if (unref(getShowTabs) && !unref(getFullContent)) {
          height += unref(getTabsThemeHeight);
        }
        setHeaderHeight(height);
        return {
          height: `${height}px`,
        };
      });

      const getClass = computed(() => {
        return [prefixCls, `${prefixCls}--${unref(getHeaderTheme)}`, {
          [`${prefixCls}--fixed`]: unref(getIsFixed),
          // 
          [`${prefixCls}--qiankun-micro`]: glob.isQiankunMicro,
        }];
      });

      return {
        glob,
        getClass,
        prefixCls,
        getPlaceholderDomStyle,
        getIsFixed,
        getWrapStyle,
        getIsShowPlaceholderDom,
        getShowTabs,
        getShowHeader,
      };
    },
  });
</script>
<style lang="less" scoped>
  @prefix-cls: ~'@{namespace}-layout-multiple-header';

  .@{prefix-cls} {
    transition: width 0.2s;
    flex: 0 0 auto;
    // Giải thích logic mã: 【issues/8709】Kiểu LayoutContent thừa ra 1px
    // &--dark {
    //   margin-left: -1px;
    // }

    &--fixed {
      position: fixed;
      top: 0;
      z-index: @multiple-tab-fixed-z-index;
      width: 100%;
    }

    // 
    &--qiankun-micro {
      position: absolute;

      :deep(.@{namespace}-layout-header--qiankun-micro) {
        width: 100%;
      }
    }

  }
</style>
