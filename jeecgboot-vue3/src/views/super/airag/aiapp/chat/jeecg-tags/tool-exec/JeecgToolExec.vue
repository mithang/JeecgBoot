<template>
  <div class="tool-exec-wrapper">
    <div v-if="!toolRecord" class="tool-exec-empty">Tạm thời chưa có kết quả gọi công cụ</div>
    <div v-else-if="toolRecord.loading" class="tool-exec-loading">
      <LoadingOutlined spin/>
      <span style="margin-left: 8px;">Đang chạy {{ titleText }}</span>
    </div>
    <div v-else class="tool-exec-list">
      <div class="tool-exec-card">
        <div class="card-header" @click="toggleCard()">
          <div class="header-left">
            <span class="status-icon" :class="`status-${resolvedStatus}`"></span>
            <div class="title-block">
              <div class="title-text">
                <span class="status-text">{{ statusLabel }}</span>
                <span>{{ titleText }}</span>
              </div>
              <div class="subtitle-text" v-if="toolRecord.subtitle">
                <span>{{ toolRecord.subtitle }}</span>
              </div>
            </div>
          </div>
          <div class="header-right">
            <!-- <span class="collapse-text">{{ expanded ? 'Thu gọn' : 'Mở rộng' }}</span>-->
            <span class="collapse-icon" :class="{ opened: expanded }">
              <DownOutlined/>
            </span>
          </div>
        </div>
        <div v-if="expanded" class="card-body">
          <div class="section">
            <div class="section-title">Đầu vào</div>
            <pre class="section-content">{{ formattedInput }}</pre>
          </div>
          <div class="section">
            <div class="section-title">Đầu ra</div>
            <pre class="section-content">{{ formattedOutput }}</pre>
          </div>
          <div v-if="toolRecord.errorMessage" class="section">
            <div class="section-title">Thông tin lỗi</div>
            <pre class="section-content">{{ formattedError }}</pre>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { DownOutlined, LoadingOutlined } from '@ant-design/icons-vue';

/**
 * Loại bản ghi kết quả gọi công cụ
 */
type ToolExecStatus = 'success' | 'running' | 'error';

/**
 * Bản ghi hiển thị gọi công cụ
 */
interface ToolExecRecord {
  id: string;
  name: string;
  displayName?: string;
  mcpName?: string;
  input: string;
  output: string;
  loading: boolean;
  hasError: boolean;

  subtitle?: string;
  errorMessage?: string;
}

const props = defineProps({
  data: {
    type: String,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

/**
 * Phân tích props.data thành một bản ghi đơn
 */
const parsedRecord = computed<ToolExecRecord | null>(() => {
  try {
    const value = JSON.parse(props.data);
    if (value && typeof value === 'object' && !Array.isArray(value)) {
      return value as ToolExecRecord;
    }
    return null;
  } catch (error) {
    return null;
  }
});

/**
 * Bản ghi đơn sau khi chuẩn hóa, bổ sung đủ các trường thường dùng
 */
const toolRecord = computed<ToolExecRecord | null>(() => {
  const item = parsedRecord.value;
  if (!item) {
    return null;
  }
  return {
    ...item,
    status: status,
    toolName: item.name,
    input: item.input ?? (item as any).request ?? (item as any).payload ?? (item as any).args,
    output: item.output ?? (item as any).response ?? (item as any).result,
    errorMessage: '',
  } as ToolExecRecord;
});

/**
 * Trạng thái mở rộng của thẻ đơn
 */
const expanded = ref<boolean>(false);

/**
 * Ánh xạ văn bản trạng thái
 */
const statusLabelMap: Record<ToolExecStatus, string> = {
  success: 'Đã chạy',
  running: 'Đang thực thi',
  error: 'Thực thi thất bại',
};

/**
 * Trạng thái đã phân tích
 */
const resolvedStatus = computed<ToolExecStatus>(() => {
  const record = toolRecord.value;
  if (record) {
    if (record.loading === true) {
      return 'running';
    }
    if (record.hasError === true) {
      return 'error';
    }
  }
  return 'success';
});

/**
 * Nội dung nhãn trạng thái
 */
const statusLabel = computed<string>(() => {
  const current = resolvedStatus.value;
  return statusLabelMap[current];
});

/**
 * Tiêu đề thẻ
 */
const titleText = computed<string>(() => {
  if (toolRecord.value) {
    const parts: string[] = [];
    const primaryName = toolRecord.value.displayName || toolRecord.value.name;
    if (primaryName) {
      parts.push(primaryName as string);
    }
    if (toolRecord.value.mcpName) {
      parts.push(toolRecord.value.mcpName as string);
    }
    if (parts.length > 0) {
      return parts.join(' - ');
    }
  }
  return 'Gọi công cụ';
});

/**
 * Định dạng đầu vào, đầu ra, thông tin lỗi
 */
const formattedInput = computed<string>(() => {
  return formatData(toolRecord.value ? toolRecord.value.input : '');
});

const formattedOutput = computed<string>(() => {
  return formatData(toolRecord.value ? toolRecord.value.output : '');
});

const formattedError = computed<string>(() => {
  return formatData(toolRecord.value ? toolRecord.value.errorMessage : '');
});

/**
 * Chuyển trạng thái mở rộng của thẻ
 */
function toggleCard(): void {
  const next = !expanded.value;
  expanded.value = next;
}

/**
 * Tuần tự hóa dữ liệu đầu vào hoặc đầu ra, đảm bảo tiền định dạng dễ đọc
 * @param value giá trị đầu vào hoặc đầu ra
 */
function formatData(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed.length === 0) {
      return '';
    }
    try {
      const parsed = JSON.parse(trimmed);
      return JSON.stringify(parsed, null, 2);
    } catch (error) {
      return value;
    }
  }
  try {
    return JSON.stringify(value, null, 2);
  } catch (error) {
    return String(value);
  }
}

</script>

<style scoped lang="less">
.tool-exec-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  max-width: 520px;
}

.tool-exec-loading,
.tool-exec-empty {
  padding: 12px 16px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  background-color: #fafafa;
}

.tool-exec-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tool-exec-card {
  border: 1px solid #e5e6eb;
  border-radius: 10px;
  background-color: #fff;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.card-header:hover {
  background-color: #f7f8fa;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-icon {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid #c8c9cc;
  background-color: #fff;
}

.status-success {
  border-color: #52c41a;
  background-color: #f6ffed;
}

.status-running {
  border-color: #1677ff;
  background-color: #e6f4ff;
}

.status-error {
  border-color: #ff4d4f;
  background-color: #fff1f0;
}

.title-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title-text {
  font-weight: 600;

  .status-text {
    margin-right: 6px;
    font-weight: normal;
  }
}

.subtitle-text {
  font-size: 12px;
  color: #666;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #555;
  font-size: 13px;
}

.status-text {
  font-weight: 500;
}

.collapse-text {
  font-size: 12px;
}

.collapse-icon {
  display: inline-flex;
  align-items: center;
  transition: transform 0.2s ease;

  &.opened {
    transform: rotate(180deg);
  }
}

.card-body {
  padding: 0 14px 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-title {
  font-weight: 600;
  font-size: 13px;
}

.section-content {
  margin: 0;
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #f8f8f8;
  border: 1px solid #e5e6eb;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 340px;
  overflow: auto;
}

.section-content:empty {
  display: none;
}

@media (max-width: 600px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .header-right {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
