<template>
  <div class="writer-sidebar">
    <div class="mode-tabs">
      <div class="tab-item" :class="{ active: activeMode === 'compose' }" @click="activeModeClick('compose')"> Soạn thảo </div>
      <div class="tab-item" :class="{ active: activeMode === 'reply' }" @click="activeModeClick('reply')"> Trả lời </div>
    </div>

    <div class="form-content">
      <a-form layout="vertical">
        <template v-if="activeMode === 'reply'">
          <a-form-item>
            <template #label>
              <div style="display: flex; justify-content: space-between; width: 100%; align-items: center">
                <span>Văn bản gốc</span>
                <span class="example-link" @click="fillExample">Ví dụ</span>
              </div>
            </template>
            <a-textarea
              v-model:value="formData.originalContent"
              :auto-size="{ minRows: 4, maxRows: 6 }"
              :maxlength="500"
              show-count
              placeholder="Vui lòng nhập văn bản gốc"
            />
          </a-form-item>
          <a-form-item label="Nội dung trả lời">
            <a-textarea
              v-model:value="formData.prompt"
              :auto-size="{ minRows: 4, maxRows: 6 }"
              :maxlength="500"
              show-count
              placeholder="Vui lòng nhập nội dung trả lời"
            />
          </a-form-item>
        </template>
        <template v-else>
          <a-form-item>
            <template #label>
              <div style="display: flex; justify-content: space-between; width: 100%; align-items: center">
                <span>Nội dung viết</span>
                <span class="example-link" @click="fillComposeExample">Ví dụ</span>
              </div>
            </template>
            <a-textarea
              v-model:value="formData.prompt"
              :auto-size="{ minRows: 6, maxRows: 12 }"
              :maxlength="500"
              show-count
              placeholder="Vui lòng nhập nội dung viết"
            />
          </a-form-item>
        </template>

        <a-form-item label="Độ dài">
          <div class="tag-group">
            <span
              v-for="item in options.length"
              :key="item.value"
              class="custom-tag"
              :class="{ active: formData.length === item.value }"
              @click="formData.length = item.value"
            >
              {{ item.label }}
            </span>
          </div>
        </a-form-item>

        <a-form-item label="Định dạng">
          <div class="tag-group">
            <span
              v-for="item in options.format"
              :key="item.value"
              class="custom-tag"
              :class="{ active: formData.format === item.value }"
              @click="formData.format = item.value"
            >
              {{ item.label }}
            </span>
          </div>
        </a-form-item>

        <a-form-item label="Ngữ khí">
          <div class="tag-group">
            <span
              v-for="item in options.tone"
              :key="item.value"
              class="custom-tag"
              :class="{ active: formData.tone === item.value }"
              @click="formData.tone = item.value"
            >
              {{ item.label }}
            </span>
          </div>
        </a-form-item>

        <a-form-item label="Ngôn ngữ">
          <div class="tag-group">
            <span
              v-for="item in options.language"
              :key="item.value"
              class="custom-tag"
              :class="{ active: formData.language === item.value }"
              @click="formData.language = item.value"
            >
              {{ item.label }}
            </span>
          </div>
        </a-form-item>
      </a-form>
    </div>

    <div class="action-footer">
      <a-button @click="handleReset" style="margin-right: 10px">Đặt lại</a-button>
      <a-button type="primary" @click="handleGenerate">Tạo</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, reactive } from 'vue';
  import { useMessage } from '@/hooks/web/useMessage';

  const emit = defineEmits(['generate', 'reset', 'update:modelValue']);
  const { createMessage } = useMessage();
  const activeMode = ref('compose');

  const formData = reactive({
    prompt: '',
    originalContent: '',
    length: '自动',
    format: '自动',
    tone: '自动',
    language: '自动',
    activeMode: 'compose',
  });

  const options = {
    //Độ dài
    length: [
      { label: 'Tự động', value: '自动' },
      { label: 'Ngắn', value: '短' },
      { label: 'Trung bình', value: '中等' },
      { label: 'Dài', value: '长' },
    ],
    //Định dạng
    format: [
      { label: 'Tự động', value: '自动' },
      { label: 'Email', value: '电子邮件' },
      { label: 'Tin nhắn', value: '消息' },
      { label: 'Bình luận', value: '评论' },
      { label: 'Đoạn văn', value: '段落' },
      { label: 'Bài viết', value: '文章' },
      { label: 'Bài viết blog', value: '博客文章' },
      { label: 'Ý tưởng', value: '想法' },
      { label: 'Đề cương', value: '大纲' },
    ],
    //Ngữ khí
    tone: [
      { label: 'Tự động', value: '自动' },
      { label: 'Thân thiện', value: '友善' },
      { label: 'Tùy ý', value: '随意' },
      { label: 'Chuyên nghiệp', value: '专业' },
      { label: 'Hài hước', value: '诙谐' },
      { label: 'Thú vị', value: '有趣' },
      { label: 'Chính thức', value: '正式' },
    ],
    //Ngôn ngữ
    language: [
      { label: 'Tự động', value: '自动' },
      { label: 'Tiếng Trung', value: '中文' },
      { label: 'Tiếng Anh', value: '英文' },
      { label: 'Tiếng Hàn', value: '韩语' },
      { label: 'Tiếng Nhật', value: '日语' },
    ],
  };

  /**
   * Sự kiện chuyển đổi soạn thảo và trả lời
   * @param type
   */
  function activeModeClick(type) {
    activeMode.value = type;
    formData.originalContent = '';
    formData.prompt = '';
  }

  /**
   * Đặt lại
   */
  function handleReset() {
    formData.originalContent = '';
    formData.prompt = '';
    formData.length = '自动';
    formData.format = '自动';
    formData.tone = '自动';
    formData.language = '自动';
    formData.activeMode = 'compose';
    emit('reset');
  }

  /**
   * Tạo
   */
  function handleGenerate() {
    if (activeMode.value === 'compose' && !formData.prompt) {
      createMessage.warn('Vui lòng điền nội dung viết!');
      return;
    }
    if (activeMode.value === 'reply' && !formData.originalContent) {
      createMessage.warn('Vui lòng điền văn bản gốc!');
      return;
    }
    if (activeMode.value === 'reply' && !formData.prompt) {
      createMessage.warn('Vui lòng điền trả lời!');
      return;
    }
    formData.activeMode = activeMode.value;
    emit('generate', { ...formData, mode: activeMode.value });
  }

  /**
   * Ví dụ trả lời
   */
  function fillExample() {
    formData.originalContent = 'Thông báo về việc sắp xếp cuộc họp tiến độ dự án vào thứ Hai tuần sau.';
    formData.prompt = 'Tôi đã nhận được, thứ Hai tuần sau sẽ đến đúng giờ';
  }

  /**
   * Ví dụ sáng tác
   */
  function fillComposeExample() {
    formData.prompt = 'Giới thiệu kiến trúc tổng thể của Vue3';
    formData.originalContent = '';
  }
</script>

<style scoped lang="less">
  .writer-sidebar {
    display: flex;
    flex-direction: column;
    height: 100%;
    position: relative;

    .mode-tabs {
      display: flex;
      background: #f1f3f6;
      border-radius: 8px;
      padding: 4px;
      margin-bottom: 16px;
      flex-shrink: 0;
      position: sticky;
      top: 0;
      z-index: 5;

      .tab-item {
        flex: 1;
        text-align: center;
        padding: 6px 0;
        font-size: 14px;
        color: #666;
        cursor: pointer;
        border-radius: 6px;
        transition: all 0.3s;

        &.active {
          background: #fff;
          color: #1677ff;
          font-weight: 500;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        }
      }
    }

    .form-content {
      flex: 1;
      overflow-y: auto;
      padding-right: 8px;

      &::-webkit-scrollbar {
        width: 4px;
      }
      &::-webkit-scrollbar-thumb {
        background: #ccc;
        border-radius: 2px;
      }
    }

    .action-footer {
      padding-top: 16px;
      display: flex;
      justify-content: center;
      border-top: 1px solid #f0f0f0;
      flex-shrink: 0;
      position: sticky;
      bottom: 0;
      background: #fff;
      z-index: 5;
    }

    .tag-group {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .custom-tag {
      padding: 2px 10px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
      font-size: 12px;
      color: #666;
      cursor: pointer;
      transition: all 0.3s;
      background: #fff;

      &:hover {
        color: #1677ff;
        border-color: #1677ff;
      }

      &.active {
        color: #1677ff;
        border-color: #1677ff;
        background: #e6f7ff;
      }
    }

    .example-link {
      color: #1677ff;
      cursor: pointer;
      font-size: 12px;
      float: right;
    }

    :deep(.ant-form-item-label) {
      display: flex;
      justify-content: space-between;
      width: 100%;

      > label {
        flex: 1;
      }

      .ant-form-item-extra {
        min-height: unset;
      }
    }

    // Custom fix to put extra in label line if possible, but Ant Design puts extra below.
    // So I used a slot in label or handled it via absolute positioning if needed.
    // But standard ant form label slot might be better.
    // Above I used #extra slot which goes below.
    // I put example-link in #extra slot of form-item in template.
    // Actually, I should probably put it in the label slot if I want it on the same line.
  }
</style>
