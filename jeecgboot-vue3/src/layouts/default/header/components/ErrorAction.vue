<template>
  <Tooltip :title="t('layout.header.tooltipErrorLog')" placement="bottom" :mouseEnterDelay="0.5">
    <!-- Phase 4 Bước 11 (G12): nút nhật ký lỗi tới được bằng bàn phím. -->
    <span
      tabindex="0"
      role="button"
      :aria-label="t('layout.header.tooltipErrorLog')"
      @click="handleToErrorList"
      @keydown.enter="handleToErrorList"
      @keydown.space.prevent="handleToErrorList"
    >
      <Badge :count="getCount" :offset="[0, 10]" :overflowCount="99">
        <Icon icon="ion:bug-outline" />
      </Badge>
    </span>
  </Tooltip>
</template>
<script lang="ts">
  import { defineComponent, computed } from 'vue';
  import { Tooltip, Badge } from 'ant-design-vue';
  import Icon from '/@/components/Icon';

  import { useI18n } from '/@/hooks/web/useI18n';
  import { useErrorLogStore } from '/@/store/modules/errorLog';
  import { PageEnum } from '/@/enums/pageEnum';

  import { useRouter } from 'vue-router';

  export default defineComponent({
    name: 'ErrorAction',
    components: { Icon, Tooltip, Badge },

    setup() {
      const { t } = useI18n();
      const { push } = useRouter();
      const errorLogStore = useErrorLogStore();

      const getCount = computed(() => errorLogStore.getErrorLogListCount);

      function handleToErrorList() {
        push(PageEnum.ERROR_LOG_PAGE).then(() => {
          errorLogStore.setErrorLogListCount(0);
        });
      }

      return {
        t,
        getCount,
        handleToErrorList,
      };
    },
  });
</script>
