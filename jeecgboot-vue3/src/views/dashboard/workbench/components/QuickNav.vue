<template>
  <Card title="Điều hướng nhanh" v-bind="$attrs">
    <template v-for="item in visibleNavItems" :key="item.title">
      <CardGrid class="quick-nav-grid" @click="goPage(item.path)">
        <span class="flex flex-col items-center">
          <Icon :icon="item.icon" :color="item.color" size="20" />
          <span class="text-md mt-2">{{ item.title }}</span>
        </span>
      </CardGrid>
    </template>
  </Card>
</template>
<script lang="ts" setup>
  import { computed } from 'vue';
  import { useRouter } from 'vue-router';
  import { Card } from 'ant-design-vue';
  import { navItems } from './data';
  import { Icon } from '/@/components/Icon';
  import { usePermission } from '/@/hooks/web/usePermission';

  const CardGrid = Card.Grid;
  const router = useRouter();
  const { hasPermission } = usePermission();

  // Phase 4 Step 7 (G5): lối tắt theo vai trò — ẩn ô mà người dùng không có quyền.
  const visibleNavItems = computed(() => navItems.filter((item) => !item.auth || hasPermission(item.auth)));

  function goPage(path: string) {
    router.push(path);
  }
</script>

<style lang="less" scoped>
  .quick-nav-grid {
    cursor: pointer;
  }
</style>
