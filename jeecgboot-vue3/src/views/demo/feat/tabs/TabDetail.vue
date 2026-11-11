<template>
  <PageWrapper title="TabTrang chi tiết">
    <div>{{ index }} - Nội dung trang chi tiết ở đây</div>
  </PageWrapper>
</template>

<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { useRoute } from 'vue-router';

  export default defineComponent({
    name: 'TabDetail',
    components: { PageWrapper },
    setup() {
      const route = useRoute();
      const index = route.params?.id ?? -1;
      const { setTitle } = useTabs();

      // Thiết lập định danh
      setTitle(`No.${index} - Thông tin chi tiết`);
      return {
        index,
      };
    },
  });
</script>
