<template>
  <PageWrapper title="Tab có tham số" content="Hỗ trợ cache nhiều tab có tham số">
    Current Param : {{ params }}
    <br />
    Keep Alive
    <Input />
  </PageWrapper>
</template>
<script lang="ts">
  import { computed, defineComponent, unref } from 'vue';
  import { useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { Input } from 'ant-design-vue';

  export default defineComponent({
    name: 'TestTab',
    components: { PageWrapper, Input },
    setup() {
      const { currentRoute } = useRouter();
      return {
        params: computed(() => {
          return unref(currentRoute).params;
        }),
      };
    },
  });
</script>
