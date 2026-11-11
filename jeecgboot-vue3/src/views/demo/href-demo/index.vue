<template>
  <div class="href-demo-page">
    <a-card title="Href Ví dụ nhận tham số" :bordered="false">
      <template #extra>
        <a-tag color="blue">Online Chuyển hướng Href của biểu mẫu</a-tag>
      </template>

      <a-divider orientation="left">Tham số route (route.query)</a-divider>
      <a-descriptions bordered :column="1" size="middle">
        <a-descriptions-item v-for="(value, key) in queryParams" :key="key" :label="key">
          <a-tag v-if="value" color="green">{{ value }}</a-tag>
          <a-tag v-else color="default">(Giá trị trống)</a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-empty v-if="Object.keys(queryParams).length === 0" description="Chưa nhận được tham số nào" />

      <a-divider orientation="left">Thông tin đầy đủ</a-divider>
      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="Đường dẫn đầy đủ">{{ route.fullPath }}</a-descriptions-item>
        <a-descriptions-item label="Tên route">{{ route.name || '(Không có)' }}</a-descriptions-item>
        <a-descriptions-item label="Nguồn tham số">{{ Object.keys(props).filter(k => props[k]).length > 0 ? 'Component nội bộ (props)' : 'Chuyển route (query)' }}</a-descriptions-item>
        <a-descriptions-item label="Số lượng tham số">{{ Object.keys(queryParams).length }}</a-descriptions-item>
      </a-descriptions>

      <a-divider />
      <a-space>
        <a-button @click="goBack">Quay lại trang trước</a-button>
        <a-button type="primary" ghost @click="copyInfo">Sao chép thông tin tham số</a-button>
      </a-space>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { computed, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';

  const props = defineProps({
    name: { type: String, default: '' },
    code: { type: String, default: '' },
  });

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();

  // Gộp props và tham số query
  const queryParams = computed(() => {
    const params: Record<string, string> = {};
    // Tham số query của route
    Object.entries(route.query).forEach(([k, v]) => {
      params[k] = String(v ?? '');
    });
    // Ghi đè props của component nội bộ (key trùng tên thì dùng giá trị props)
    Object.entries(props).forEach(([k, v]) => {
      if (v) params[k] = String(v);
    });
    return params;
  });

  // Lắng nghe thay đổi tham số
  watch(
    () => route.query,
    (newQuery) => {
      console.log('===== Online Href $route props =====');
      console.log('query params:', newQuery);
      Object.entries(newQuery).forEach(([key, value]) => {
        console.log(`$route ${key}: ${value}`);
      });
    },
    { immediate: true, deep: true }
  );

  function goBack() {
    router.back();
  }

  function copyInfo() {
    const info = Object.entries(queryParams.value)
      .map(([k, v]) => `${k}=${v}`)
      .join('&');
    navigator.clipboard.writeText(info).then(() => {
      createMessage.success('Tham số đã được sao chép vào clipboard');
    });
  }
</script>

<style lang="less" scoped>
  .href-demo-page {
    padding: 24px;
    max-width: 800px;
    margin: 0 auto;
  }
</style>
