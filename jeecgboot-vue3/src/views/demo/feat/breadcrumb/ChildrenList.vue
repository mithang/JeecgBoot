<template>
  <PageWrapper title="Ví dụ breadcrumb theo cấp bậc" content="Breadcrumb của trang con sẽ được thêm vào sau cấp hiện tại">
    <router-link to="/feat/breadcrumb/children/childrenDetail"> Vào trang chi tiết cấp con </router-link>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { PageWrapper },
  });
</script>
