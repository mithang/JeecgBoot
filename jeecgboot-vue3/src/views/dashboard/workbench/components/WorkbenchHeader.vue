<template>
  <div class="lg:flex">
    <Avatar :src="userinfo.avatar || headerImg" :size="72" class="!mx-auto !block" />
    <div class="md:ml-6 flex flex-col justify-center md:mt-0 mt-2">
      <h1 class="md:text-lg text-md">{{ greeting }}, {{ userinfo.realname }}!</h1>
      <span class="text-secondary">{{ todayText }}</span>
    </div>
    <div class="flex flex-1 justify-end md:mt-0 mt-4">
      <div class="flex flex-col justify-center text-right">
        <span class="text-secondary"> Việc cần làm </span>
        <span class="text-2xl">2/10</span>
      </div>

      <div class="flex flex-col justify-center text-right md:mx-16 mx-12">
        <span class="text-secondary"> Dự án </span>
        <span class="text-2xl">8</span>
      </div>
      <div class="flex flex-col justify-center text-right md:mr-10 mr-4">
        <span class="text-secondary"> Nhóm </span>
        <span class="text-2xl">300</span>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/user';
  import headerImg from '/@/assets/images/header.jpg';
  import { getTimeGreeting, formatVietnameseDate } from '../../dashboardHelpers';

  const userStore = useUserStore();
  const userinfo = computed(() => userStore.getUserInfo);
  // Phase 4 Step 7 (G5): lời chào theo giờ thật + ngày thật, thay cho câu chào
  // "buổi sáng" và thời tiết giả cố định trước đây.
  const greeting = computed(() => getTimeGreeting());
  const todayText = computed(() => formatVietnameseDate());
</script>
