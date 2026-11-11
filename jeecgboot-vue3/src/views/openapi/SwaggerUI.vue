<template>
  <div ref="swaggerUiRef" style="height: 100%;" @click="handleCopyClick"></div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
// Thử import trực tiếp SwaggerUI mà không dùng * as
import SwaggerUI from 'swagger-ui-dist/swagger-ui-bundle'; // Đảm bảo sử dụng phiên bản ESM
import 'swagger-ui-dist/swagger-ui.css';
import { useMessage } from '/@/hooks/web/useMessage';

import { getOpenApiJson } from './OpenApi.api';

const { createMessage } = useMessage();
const swaggerUiRef = ref<HTMLElement | null>(null);
const API_DOMAIN = import.meta.env.VITE_GLOB_DOMAIN_URL
onMounted(async () => {
  try {
    const response = await getOpenApiJson();
    const openApiJson = response;
    if (swaggerUiRef.value) {
      SwaggerUI({
        domNode: swaggerUiRef.value,
        spec: openApiJson,
      });
    }
  } catch (error) {
    console.error('Failed to fetch OpenAPI JSON:', error);
  }
});

/**
 * Nút sao chép tích hợp sẵn của Swagger UI (đường dẫn API, Curl, giá trị mẫu, v.v.) không có phản hồi nào sau khi sao chép, nên bổ sung thống nhất một gợi ý tại đây.
 * Cấu trúc nút là .copy-to-clipboard > svg/button, chỉ khi nhấp trúng phần tử con này mới thực sự sao chép, nhấp vào viền xám của nút thì không.
 */
function handleCopyClick(e: MouseEvent) {
  if ((e.target as Element).closest('.copy-to-clipboard > *')) {
    createMessage.success('Đã sao chép');
  }
}
</script>

<style scoped>
/* Đảm bảo container có chiều cao */
.swagger-ui-container {
  height: 100%;
}
</style>
