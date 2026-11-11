<template>
  <PageWrapper title="Ví dụ breadcrumb cùng cấp" content="Breadcrumb của trang con sẽ ghi đè lên cấp hiện tại">
    <router-link to="/feat/breadcrumb/flatDetail"> Vào trang chi tiết cùng cấp </router-link>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { PageWrapper },
  });
</script>
