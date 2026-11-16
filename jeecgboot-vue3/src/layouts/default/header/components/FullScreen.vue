<template>
  <Tooltip :title="getTitle" placement="bottom" :mouseEnterDelay="0.5">
    <!-- Phase 4 Bước 11 (G12): nút toàn màn hình tới được bằng bàn phím. -->
    <span
      tabindex="0"
      role="button"
      :aria-label="getTitle"
      @click="toggle"
      @keydown.enter="toggle"
      @keydown.space.prevent="toggle"
    >
      <FullscreenOutlined v-if="!isFullscreen" />
      <FullscreenExitOutlined v-else />
    </span>
  </Tooltip>
</template>
<script lang="ts">
  import { defineComponent, computed, unref } from 'vue';
  import { Tooltip } from 'ant-design-vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { useFullscreen } from '@vueuse/core';

  import { FullscreenExitOutlined, FullscreenOutlined } from '@ant-design/icons-vue';
  export default defineComponent({
    name: 'FullScreen',
    components: { FullscreenExitOutlined, FullscreenOutlined, Tooltip },

    setup() {
      const { t } = useI18n();
      const { toggle, isFullscreen } = useFullscreen();

      const getTitle = computed(() => {
        return unref(isFullscreen) ? t('layout.header.tooltipExitFull') : t('layout.header.tooltipEntryFull');
      });

      return {
        getTitle,
        isFullscreen,
        toggle,
      };
    },
  });
</script>
