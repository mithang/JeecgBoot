<template>
  <div v-if="visible" class="welcome-banner enter-y">
    <div class="welcome-banner__content">
      <div class="welcome-banner__text">
        <h2 class="welcome-banner__title">{{ greeting }}, {{ realname }}!</h2>
        <p class="welcome-banner__date">{{ todayText }}</p>
        <p class="welcome-banner__subtitle"> Tổng quan hệ thống của bạn hôm nay — truy cập nhanh các khu vực hay dùng. </p>
      </div>
      <div class="welcome-banner__actions">
        <a-button v-for="link in visibleLinks" :key="link.path" ghost @click="go(link.path)">
          {{ link.title }}
        </a-button>
      </div>
    </div>
    <a-button type="text" class="welcome-banner__close" aria-label="Đóng banner chào mừng" @click="dismiss">
      <Icon icon="ant-design:close-outlined" :size="14" />
    </a-button>
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Icon } from '/@/components/Icon';
  import { useUserStore } from '/@/store/modules/user';
  import { usePermission } from '/@/hooks/web/usePermission';
  import { getTimeGreeting, formatVietnameseDate } from '../../dashboardHelpers';

  // Phase 4 Step 7 (G5): ô banner chào/thông báo trên trang chủ mặc định.
  // Chỉ là lớp trình bày: đóng banner không ảnh hưởng chức năng nào.
  // Banner đã đóng sẽ hiện lại vào ngày hôm sau.
  const STORAGE_KEY = 'jeecg-dashboard-banner-dismissed';

  const router = useRouter();
  const userStore = useUserStore();
  const { hasPermission } = usePermission();

  const userinfo = computed(() => userStore.getUserInfo);
  const realname = computed(() => userinfo.value?.realname || 'bạn');
  const greeting = computed(() => getTimeGreeting());
  const todayText = computed(() => formatVietnameseDate());

  function localDateKey(date = new Date()): string {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${date.getFullYear()}-${month}-${day}`;
  }

  function readDismissed(): boolean {
    try {
      return localStorage.getItem(STORAGE_KEY) === localDateKey();
    } catch (_e) {
      return false;
    }
  }

  const visible = ref(!readDismissed());

  function dismiss() {
    try {
      localStorage.setItem(STORAGE_KEY, localDateKey());
    } catch (_e) {
      // Không ghi được storage thì chỉ ẩn trong phiên hiện tại.
    }
    visible.value = false;
  }

  // Lối tắt tới các khu vực chính; mục cần quyền chỉ hiện khi người dùng có quyền đó.
  const links = [
    { title: 'Người dùng', path: '/system/user', auth: 'system:user:list' },
    { title: 'Biểu mẫu Online', path: '/online/cgform' },
    { title: 'Ứng dụng AI', path: '/super/airag/aiapp/AiAppList' },
  ];
  const visibleLinks = computed(() => links.filter((link) => !link.auth || hasPermission(link.auth)));

  function go(path: string) {
    router.push(path);
  }
</script>

<style lang="less" scoped>
  .welcome-banner {
    position: relative;
    border-radius: 8px;
    background: linear-gradient(120deg, #0b57d0 0%, #0a3d91 55%, #0b2447 100%);
    color: #fff;
    padding: 20px 24px;
    margin-bottom: 16px;
    overflow: hidden;

    &__content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 16px;
      flex-wrap: wrap;
      padding-right: 24px;
    }

    &__title {
      color: #fff;
      font-size: 20px;
      font-weight: 700;
      line-height: 1.4;
      margin: 0;
    }

    &__date {
      color: rgba(255, 255, 255, 0.85);
      margin: 4px 0 2px;
    }

    &__subtitle {
      color: rgba(255, 255, 255, 0.75);
      margin: 0;
    }

    &__actions {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    &__close {
      position: absolute;
      top: 8px;
      right: 8px;
      color: rgba(255, 255, 255, 0.75);

      &:hover {
        color: #fff;
      }
    }
  }

  @media (max-width: 768px) {
    .welcome-banner {
      padding: 16px;

      &__title {
        font-size: 18px;
      }

      &__content {
        padding-right: 0;
      }
    }
  }
</style>
