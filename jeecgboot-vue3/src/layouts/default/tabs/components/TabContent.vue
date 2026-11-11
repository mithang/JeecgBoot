<template>
  <Dropdown :dropMenuList="getDropMenuList" :trigger="getTrigger" @menuEvent="handleMenuEvent" :overlayClassName="prefixCls">
    <div :class="`${prefixCls}__info`" @contextmenu="handleContext" v-if="getIsTabs">
      <!-- updateBy: sunjianlei --- updateDate: 2021-09-03 --- Sửa kiểu thanh chuyển đổi tab: thêm biểu tượng tiền tố -->
      <!-- <span v-if="showPrefixIcon" :class="`${prefixCls}__prefix-icon`" @click="handleContext">
        <Icon :icon="prefixIconType" :size="14" />
      </span> -->
      <span class="ml-1">{{ getTitle }}</span>
    </div>
    <span :class="`${prefixCls}__extra-quick`" v-else @click="handleContext">
      <Icon icon="ion:chevron-down" />
    </span>
  </Dropdown>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import type { RouteLocationNormalized } from 'vue-router';

  import { defineComponent, computed, unref } from 'vue';
  import { Dropdown } from '/@/components/Dropdown/index';
  import { Icon } from '/@/components/Icon';

  import { TabContentProps } from '../types';

  import { TabsThemeEnum } from '/@/enums/appEnum';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useTabDropdown } from '../useTabDropdown';
  import { useMultipleTabSetting } from '/@/hooks/setting/useMultipleTabSetting';
  import { useLocaleStore } from '/@/store/modules/locale';
  import { PageEnum } from '/@/enums/pageEnum';

  export default defineComponent({
    name: 'TabContent',
    components: { Dropdown, Icon },
    props: {
      tabItem: {
        type: Object as PropType<RouteLocationNormalized>,
        default: null,
      },
      isExtra: Boolean,
    },
    setup(props) {
      const { prefixCls } = useDesign('multiple-tabs-content');
      const { t } = useI18n();

      // Giải thích logic mã: VUEN-1144 Sau khi cấu hình online thành menu, mở menu, tên hiển thị không hiển thị thành tên menu
      const localeStore = useLocaleStore();
      const getTitle = computed(() => {
        const { tabItem: { meta, fullPath } = {} } = props;
        let title = localeStore.getPathTitle(fullPath);
        if (title) {
          return title;
        }
        return meta && t(meta.title as string);
      });

      const getIsTabs = computed(() => !props.isExtra);

      // updateBy: sunjianlei --- updateDate: 2021-09-03 --- Sửa kiểu thanh chuyển đổi tab: loại biểu tượng tiền tố
      const prefixIconType = computed(() => {
        if (props.tabItem.meta.icon) {
          return props.tabItem.meta.icon;
        } else if (props.tabItem.path === PageEnum.BASE_HOME) {
          // Khi là trang chủ thì trả về biểu tượng home. TODO: Ở đây có thể cần kiểm tra động đường dẫn trang chủ
          return 'ant-design:home-outlined';
        } else {
          return 'ant-design:code';
        }
      });

      const getTrigger = computed((): ('contextmenu' | 'click' | 'hover')[] => (unref(getIsTabs) ? ['contextmenu'] : ['click']));

      const { getDropMenuList, handleMenuEvent, handleContextMenu } = useTabDropdown(props as TabContentProps, getIsTabs);

      function handleContext(e) {
        props.tabItem && handleContextMenu(props.tabItem)(e);
      }

      const { getTabsTheme } = useMultipleTabSetting();
      // Có hiển thị biểu tượng không
      const showPrefixIcon = computed(() => unref(getTabsTheme) === TabsThemeEnum.SMOOTH);

      return {
        prefixCls,
        getDropMenuList,
        handleMenuEvent,
        handleContext,
        getTrigger,
        getIsTabs,
        getTitle,
        prefixIconType,
        showPrefixIcon,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-multiple-tabs-content';
  .@{prefix-cls} {
    .ant-dropdown-menu-item {
      .ant-dropdown-menu-title-content {
        .anticon {
          font-size: 14px !important;
        }
        span:not(.anticon) {
          margin-left: 6px;
        }
      }
    }
  }
</style>
