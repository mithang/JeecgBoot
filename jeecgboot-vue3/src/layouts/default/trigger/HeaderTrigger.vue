<template>
  <!-- Phase 4 Bước 11 (G12): nút thu/mở menu tới được bằng bàn phím (tabindex +
       role="button" + Enter/Space) và có nhãn aria theo trạng thái. -->
  <span
    :class="[prefixCls, theme]"
    tabindex="0"
    role="button"
    :aria-label="getCollapsed ? t('layout.header.tooltipExpandMenu') : t('layout.header.tooltipCollapseMenu')"
    @click="toggleCollapsed"
    @keydown.enter="toggleCollapsed"
    @keydown.space.prevent="toggleCollapsed"
  >
    <MenuUnfoldOutlined v-if="getCollapsed" /> <MenuFoldOutlined v-else />
  </span>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons-vue';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { propTypes } from '/@/utils/propTypes';

  export default defineComponent({
    name: 'HeaderTrigger',
    components: { MenuUnfoldOutlined, MenuFoldOutlined },
    props: {
      theme: propTypes.oneOf(['light', 'dark']),
    },
    setup() {
      const { getCollapsed, toggleCollapsed } = useMenuSetting();
      const { prefixCls } = useDesign('layout-header-trigger');
      const { t } = useI18n();
      return { getCollapsed, toggleCollapsed, prefixCls, t };
    },
  });
</script>
