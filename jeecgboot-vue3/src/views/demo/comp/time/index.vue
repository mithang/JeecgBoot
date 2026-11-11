<template>
  <PageWrapper title="Ví dụ về component thời gian">
    <CollapseContainer title="Ví dụ cơ bản">
      <Time :value="time1" />
      <br />
      <Time :value="time2" />
    </CollapseContainer>

    <CollapseContainer title="Cập nhật định kỳ" class="my-4">
      <Time :value="now" :step="1" />
      <br />
      <Time :value="now" :step="5" />
    </CollapseContainer>

    <CollapseContainer title="Cập nhật định kỳ">
      <Time :value="now" mode="date" />
      <br />
      <Time :value="now" mode="datetime" />
      <br />
      <Time :value="now" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive, toRefs } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { Time } from '/@/components/Time';
  import { CollapseContainer } from '/@/components/Container/index';

  export default defineComponent({
    components: { PageWrapper, Time, CollapseContainer },
    setup() {
      const now = new Date().getTime();
      const state = reactive({
        time1: now - 60 * 3 * 1000,
        time2: now - 86400 * 3 * 1000,
      });
      return {
        ...toRefs(state),
        now,
      };
    },
  });
</script>
