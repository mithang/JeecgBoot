<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Chi tiết nhật ký gọi" :width="800" :footer="null" :minHeight="600">
    <a-descriptions bordered :column="2" size="small" :labelStyle="{ whiteSpace: 'nowrap', width: '90px' }">
      <a-descriptions-item label="Thời gian gọi" :span="2">{{ record.callTime || '-' }}</a-descriptions-item>
      <a-descriptions-item label="Phương thức yêu cầu">
        <a-tag :color="getMethodColor(record.requestMethod)">{{ record.requestMethod || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="Mã trạng thái">
        <a-tag :color="getStatusColor(record.responseCode)">{{ record.responseCode || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item label="Đường dẫn yêu cầu" :span="2">{{ record.requestPath || '-' }}</a-descriptions-item>
      <a-descriptions-item label="AK người gọi">{{ record.callerAk || '-' }}</a-descriptions-item>
      <a-descriptions-item label="IP nguồn">{{ record.ip || '-' }}</a-descriptions-item>
      <a-descriptions-item label="Thời gian xử lý (ms)">{{ record.usedTime ?? '-' }}</a-descriptions-item>
      <a-descriptions-item label="Nguyên nhân thất bại">{{ record.errorMsg || '-' }}</a-descriptions-item>
      <a-descriptions-item label="Header yêu cầu" :span="2">
        <pre class="detail-pre">{{ formatJson(record.requestHeaders) }}</pre>
      </a-descriptions-item>
      <a-descriptions-item label="Tham số yêu cầu" :span="2">
        <pre class="detail-pre">{{ formatJson(record.requestParams) }}</pre>
      </a-descriptions-item>
      <a-descriptions-item v-if="record.responseBody" label="Nội dung phản hồi" :span="2">
        <pre class="detail-pre">{{ formatJson(record.responseBody) }}</pre>
      </a-descriptions-item>
    </a-descriptions>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';

  const record = ref<Record<string, any>>({});

  const [registerModal] = useModalInner((data) => {
    record.value = data.record || {};
  });

  function getStatusColor(code: number) {
    if (!code) return 'default';
    if (code >= 200 && code < 300) return 'success';
    if (code >= 400 && code < 500) return 'warning';
    if (code >= 500) return 'error';
    return 'default';
  }

  const methodColorMap: Record<string, string> = {
    GET: 'blue',
    POST: 'green',
    PUT: 'orange',
    DELETE: 'red',
    PATCH: 'purple',
  };
  function getMethodColor(method: string) {
    return methodColorMap[method] ?? 'default';
  }

  function formatJson(val: any) {
    if (!val) return '-';
    try {
      return JSON.stringify(JSON.parse(val), null, 2);
    } catch {
      return val;
    }
  }
</script>

<style scoped>
  .detail-pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
    font-size: 12px;
    max-height: 300px;
    overflow-y: auto;
    background: #f5f5f5;
    padding: 8px;
    border-radius: 4px;
  }
</style>
