<template>
  <!-- Phase 4 Bước 11 (G12): nút thu/mở sider tới được bằng bàn phím (tabindex +
       role="button" + Enter/Space) và có nhãn aria theo trạng thái. -->
  <div
    tabindex="0"
    role="button"
    :aria-label="getCollapsed ? t('layout.header.tooltipExpandMenu') : t('layout.header.tooltipCollapseMenu')"
    @click.stop="toggleCollapsed"
    @keydown.enter.stop="toggleCollapsed"
    @keydown.space.prevent.stop="toggleCollapsed"
  >
    <DoubleRightOutlined v-if="getCollapsed" />
    <DoubleLeftOutlined v-else />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { DoubleRightOutlined, DoubleLeftOutlined } from '@ant-design/icons-vue';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { useI18n } from '/@/hooks/web/useI18n';

  export default defineComponent({
    name: 'SiderTrigger',
    components: { DoubleRightOutlined, DoubleLeftOutlined },
    setup() {
      const { getCollapsed, toggleCollapsed } = useMenuSetting();
      const { t } = useI18n();

      return { getCollapsed, toggleCollapsed, t };
    },
  });
</script>
