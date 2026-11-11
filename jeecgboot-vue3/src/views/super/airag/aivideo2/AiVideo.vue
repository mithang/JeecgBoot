<template>
  <div class="content-wrapper">
    <!-- Panel cấu hình bên trái -->
    <div class="config-panel">
      <div class="config-tabs">
        <a-tabs v-model:activeKey="activeCategory" :tabBarStyle="{ margin: 0 }">
          <a-tab-pane v-for="cat in categoryList" :key="cat" :tab="cat" />
        </a-tabs>
      </div>

      <!-- Prompt đặt sẵn -->
      <div class="preset-prompts">
        <div class="preset-label">Prompt tắt</div>
        <div class="preset-list">
          <a-button
            v-for="(prompt, index) in currentPrompts"
            :key="index"
            class="preset-btn"
            size="small"
            @click="applyPrompt(prompt)"
          >
            {{ prompt.length > 20 ? prompt.substring(0, 20) + '...' : prompt }}
          </a-button>
        </div>
      </div>

      <div class="form-container">
        <BasicForm @register="registerForm" />
      </div>

      <div class="action-container">
        <a-button type="primary" size="large" block @click="handleGenerate" :loading="generating" :disabled="generating">
          <Icon icon="ant-design:video-camera-outlined" />
          {{ generating ? 'Đang tạo...' : 'Bắt đầu tạo' }}
        </a-button>
      </div>
    </div>

    <!-- Panel xem trước bên phải -->
    <div class="preview-panel">
      <div class="panel-title">Kết quả tạo</div>
      <div class="preview-content">
        <!-- Trạng thái trống -->
        <div v-if="!videoUrl && !generating" class="empty-state">
          <Icon icon="ant-design:video-camera-outlined" size="64" color="#ccc" />
          <p>Nhập mô tả video ở bên trái, nhấp bắt đầu tạo</p>
        </div>

        <!-- Đang tạo -->
        <div v-if="generating" class="loading-state">
          <a-spin size="large" />
          <div class="loading-text">
            <p>Đang tạo video, vui lòng kiên nhẫn chờ đợi...</p>
            <p class="elapsed-time">Đã chờ {{ elapsedTimeText }}</p>
            <p class="status-text">{{ statusText }}</p>
          </div>
        </div>

        <!-- Tạo hoàn thành -->
        <div v-if="videoUrl && !generating" class="result-video-wrapper">
          <video :src="videoUrl" controls class="result-video" />
          <div class="video-actions">
            <a-button type="primary" @click="handleDownload">
              <Icon icon="ant-design:download-outlined" />
              Tải xuống video
            </a-button>
            <a-button @click="handleReset">
              <Icon icon="ant-design:redo-outlined" />
              Tạo lại
            </a-button>
          </div>
        </div>

        <!-- Tạo thất bại -->
        <div v-if="errorMessage && !generating" class="error-state">
          <Icon icon="ant-design:close-circle-outlined" size="64" color="#ff4d4f" />
          <p class="error-text">{{ errorMessage }}</p>
          <a-button type="primary" @click="handleReset">Thử lại</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { ref, computed, onUnmounted } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Icon } from '@/components/Icon';
  import { submitVideoTask, queryVideoTask, getPresetPrompts } from './AiVideo.api';
  import { videoFormSchema, categoryList, fallbackPrompts } from './AiVideo.data';

  const { createMessage } = useMessage();

  const activeCategory = ref('通用演示');
  const generating = ref(false);
  const videoUrl = ref('');
  const errorMessage = ref('');
  const elapsedSeconds = ref(0);
  const statusText = ref('Nhiệm vụ đã gửi, đang xếp hàng...');

  let pollTimer: ReturnType<typeof setInterval> | null = null;
  let elapsedTimer: ReturnType<typeof setInterval> | null = null;

  // Prompt đặt sẵn (ưu tiên tải từ backend)
  const promptsMap = ref<Record<string, string[]>>({ ...fallbackPrompts });

  // Tải prompt đặt sẵn của backend
  getPresetPrompts()
    .then((data) => {
      if (data && Object.keys(data).length > 0) {
        promptsMap.value = data;
      }
    })
    .catch(() => {
      // Dùng prompt dự phòng
    });

  const currentPrompts = computed(() => {
    return promptsMap.value[activeCategory.value] || [];
  });

  const elapsedTimeText = computed(() => {
    const minutes = Math.floor(elapsedSeconds.value / 60);
    const seconds = elapsedSeconds.value % 60;
    if (minutes > 0) {
      return `${minutes} phút ${seconds} giây`;
    }
    return `${seconds} giây`;
  });

  const [registerForm, { validate, setFieldsValue }] = useForm({
    schemas: videoFormSchema,
    labelWidth: 100,
    actionColOptions: { span: 24 },
    showActionButtonGroup: false,
  });

  function applyPrompt(prompt: string) {
    setFieldsValue({ prompt });
  }

  async function handleGenerate() {
    try {
      const values = await validate();
      if (!values.prompt || !values.prompt.trim()) {
        createMessage.warning('Vui lòng nhập mô tả video');
        return;
      }

      // Đặt lại trạng thái
      generating.value = true;
      videoUrl.value = '';
      errorMessage.value = '';
      elapsedSeconds.value = 0;
      statusText.value = 'Nhiệm vụ đã gửi, đang xếp hàng...';

      // Khởi động bộ hẹn giờ
      elapsedTimer = setInterval(() => {
        elapsedSeconds.value++;
      }, 1000);

      // Gửi nhiệm vụ
      const submitResult = await submitVideoTask({
        prompt: values.prompt.trim(),
        category: activeCategory.value,
      });

      if (!submitResult || !submitResult.taskId) {
        throw new Error(submitResult?.message || 'Gửi nhiệm vụ thất bại');
      }

      statusText.value = 'Đang tạo video...';

      // Bắt đầu thăm dò
      pollTimer = setInterval(async () => {
        try {
          const queryResult = await queryVideoTask(submitResult.taskId);
          if (queryResult.status === 'SUCCESS') {
            clearTimers();
            generating.value = false;
            videoUrl.value = queryResult.videoUrl;
            createMessage.success('Tạo video thành công!');
          } else if (queryResult.status === 'FAIL') {
            clearTimers();
            generating.value = false;
            errorMessage.value = queryResult.message || 'Tạo video thất bại';
          }
          // Trạng thái PROCESSING tiếp tục thăm dò
        } catch (e: any) {
          clearTimers();
          generating.value = false;
          errorMessage.value = 'Truy vấn trạng thái nhiệm vụ thất bại: ' + (e.message || 'Lỗi không rõ');
        }
      }, 5000);
    } catch (error: any) {
      clearTimers();
      generating.value = false;
      if (error?.errorFields) {
        // Xác thực biểu mẫu thất bại, không hiển thị lỗi bổ sung
        return;
      }
      errorMessage.value = error.message || 'Gửi nhiệm vụ thất bại';
    }
  }

  function clearTimers() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
    if (elapsedTimer) {
      clearInterval(elapsedTimer);
      elapsedTimer = null;
    }
  }

  function handleReset() {
    videoUrl.value = '';
    errorMessage.value = '';
    elapsedSeconds.value = 0;
  }

  function handleDownload() {
    if (!videoUrl.value) return;
    const a = document.createElement('a');
    a.href = videoUrl.value;
    a.download = `ai-video-${Date.now()}.mp4`;
    a.target = '_blank';
    a.click();
  }

  onUnmounted(() => {
    clearTimers();
  });
</script>

<style lang="less" scoped>
  .content-wrapper {
    flex: 1;
    display: flex;
    gap: 16px;
    overflow: hidden;
    height: 100%;
  }

  .config-panel {
    width: 550px;
    min-width: 350px;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    padding: 20px;
    overflow: hidden;

    .config-tabs {
      margin-bottom: 16px;

      :deep(.ant-tabs-nav::before) {
        border-bottom: none;
      }

      :deep(.ant-tabs-tab) {
        padding: 8px 0;
        margin: 0 24px 0 0;
        font-size: 15px;

        &.ant-tabs-tab-active .ant-tabs-tab-btn {
          color: #1890ff;
          font-weight: 500;
        }
      }

      :deep(.ant-tabs-ink-bar) {
        background: #1890ff;
      }
    }

    .preset-prompts {
      margin-bottom: 16px;

      .preset-label {
        font-size: 13px;
        color: #8c8c8c;
        margin-bottom: 8px;
      }

      .preset-list {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;

        .preset-btn {
          color: #595959;
          border-color: #d9d9d9;
          font-size: 12px;

          &:hover {
            color: #1890ff;
            border-color: #1890ff;
          }
        }
      }
    }

    .form-container {
      flex: 1;
      overflow-y: auto;
    }

    .action-container {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #f0f0f0;
    }
  }

  .preview-panel {
    flex: 1;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    padding: 20px;
    overflow: hidden;

    .preview-content {
      flex: 1;
      background: #f7f8fc;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
    }
  }

  .panel-title {
    font-size: 16px;
    font-weight: 600;
    color: #1f2329;
    margin-bottom: 20px;
    padding-left: 8px;
    border-left: 4px solid #1890ff;
    line-height: 1;
  }

  .empty-state {
    text-align: center;
    color: #8f959e;

    p {
      margin-top: 16px;
    }
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .loading-text {
      text-align: center;
      color: #595959;

      p {
        margin: 4px 0;
      }

      .elapsed-time {
        font-size: 20px;
        font-weight: 600;
        color: #1890ff;
      }

      .status-text {
        font-size: 13px;
        color: #8c8c8c;
      }
    }
  }

  .result-video-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 16px;

    .result-video {
      flex: 1;
      width: 100%;
      max-height: calc(100% - 60px);
      border-radius: 8px;
      background: #000;
      object-fit: contain;
    }

    .video-actions {
      margin-top: 16px;
      display: flex;
      gap: 12px;
    }
  }

  .error-state {
    text-align: center;

    .error-text {
      margin: 16px 0;
      color: #ff4d4f;
      font-size: 14px;
    }
  }
</style>
