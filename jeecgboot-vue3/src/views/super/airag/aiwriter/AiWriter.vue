<template>
  <div class="writer-wrap">
    <div class="sidebar">
      <AiWriterLeft @generate="onGenerate" />
    </div>
    <div class="preview" ref="previewRef">
      <div class="preview-header">
        <div class="preview-header-left">
          <span class="preview-title-text">{{ isEditing ? 'Chỉnh sửa' : 'Xem trước' }}</span>
          <a-select
            v-if="historyData && historyData.length"
            v-model:value="activeVersion"
            size="small"
            class="version-select"
            @change="handleVersionChange"
          >
            <a-select-option :value="CURRENT_VERSION_KEY">Nội dung hiện tại</a-select-option>
            <a-select-option v-for="item in historyData" :key="item.version" :value="item.version">
              {{ item.version }}
            </a-select-option>
          </a-select>
          <a-button
            v-if="historyData && historyData.length && activeVersion !== CURRENT_VERSION_KEY"
            type="link"
            size="small"
            class="preview-action-btn version-delete-btn"
            @click="handleDeleteVersion"
          >
            <DeleteOutlined />
            Xóa
          </a-button>
        </div>
        <div class="preview-actions">
          <a-button v-if="!generating" type="link" size="small" class="preview-action-btn custom-save-btn" @click="toggleEdit">
            <FormOutlined v-if="!isEditing" />
            <CheckOutlined v-else />
            {{ isEditing ? 'Hoàn thành' : 'Chỉnh sửa' }}
          </a-button>
          <a-button
            v-if="!generating"
            type="link"
            size="small"
            class="preview-action-btn custom-save-btn"
            :loading="polishLoading"
            @click="handlePolish"
          >
            <ThunderboltOutlined class="preview-actions-icon" style="position: relative; top: 1px" />
            Trau chuốt
          </a-button>
          <a-tooltip title="Lưu bản nháp">
            <a-button v-if="!generating" type="link" size="small" class="preview-action-btn custom-save-btn" :loading="saving" @click="handleSave">
              <SaveOutlined class="preview-actions-icon" />
              Lưu
            </a-button>
          </a-tooltip>
          <a-tooltip title="Sao chép nội dung">
            <a-button type="link" size="small" class="preview-action-btn custom-save-btn" @click="handleCopy">
              <CopyOutlined class="preview-actions-icon" />
              Sao chép
            </a-button>
          </a-tooltip>
        </div>
      </div>
      <div v-if="!isEditing" v-html="previewMd" class="markdown-container" @click="!generating" />
      <div v-else class="markdown-editor-container">
        <JMarkdownEditor v-model:value="writeText" height="100vh" :preview="{ mode: 'view', action: [] }" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, computed, nextTick, onMounted } from 'vue';
  import AiWriterLeft from './AiWriterLeft.vue';
  import { defHttp } from '@/utils/http/axios';
  import { CopyOutlined, ThunderboltOutlined, FormOutlined, CheckOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { copyTextToClipboard } from '/@/hooks/web/useCopyToClipboard';
  import { Modal } from 'ant-design-vue';
  import MarkdownIt from 'markdown-it';
  import mdKatex from '@traptitech/markdown-it-katex';
  import mila from 'markdown-it-link-attributes';
  import hljs from 'highlight.js';
  import JMarkdownEditor from '/@/components/Form/src/jeecg/components/JMarkdownEditor.vue';
  import '/@/views/super/airag/aiapp/chat/style/github-markdown.less';
  import '/@/views/super/airag/aiapp/chat/style/highlight.less';
  import '/@/views/super/airag/aiapp/chat/style/style.less';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';

  const { createMessage } = useMessage();
  const writeText = ref<string>('');
  const previewRef = ref<HTMLElement | null>(null);
  //loading của việc tạo
  const generating = ref<boolean>(false);
  //Có chỉnh sửa hay không
  const isEditing = ref<boolean>(false);
  //loading của việc trau chuốt
  const polishLoading = ref<boolean>(false);
  //Đang lưu loading
  const saving = ref<boolean>(false);
  //Nội dung bên trái
  const leftData = ref<any>();
  //Dữ liệu lịch sử
  const historyData = ref<any>([]);
  //Văn bản gợi ý ai
  const aiText = ref<string>('');
  //key của phiên bản kỳ này
  const CURRENT_VERSION_KEY = 'CURRENT';
  //Cuốn sổ hiện đang chọn
  const activeVersion = ref<string>(CURRENT_VERSION_KEY);
  //Nội dung gốc
  const originalContent = ref<string | null>(null);
  //Nội dung sau nút trả lời đầu tiên
  const afterNodeFinished = ref<boolean>(false);
  const mdi = new MarkdownIt({
    html: true,
    linkify: true,
    highlight(code, language) {
      const validLang = !!(language && hljs.getLanguage(language));
      if (validLang) {
        const lang = language ?? '';
        return highlightBlock(hljs.highlight(code, { language: lang }).value, lang);
      }
      return highlightBlock(hljs.highlightAuto(code).value, '');
    },
  });
  mdi.use(mila, { attrs: { target: '_blank', rel: 'noopener' } });
  mdi.use(mdKatex, { blockClass: 'katexmath-block rounded-md p-[10px]', errorColor: ' #cc0000' });

  //Trả về văn bản (trong lúc tạo thì thêm dấu chấm ở cuối)
  const previewMd = computed(() => {
    let content = writeText.value || aiText.value;
    if (generating.value) {
      content +=
        ' <span class="typing-dot"></span><span class="typing-dot typing-dot-delay-1"></span><span class="typing-dot typing-dot-delay-2"></span>';
    }
    return sanitizeRichText(mdi.render(content));
  });

  /**
   * Chỉnh sửa
   */
  function toggleEdit() {
    if (generating.value) {
      return;
    }
    isEditing.value = !isEditing.value;
  }

  /**
   * Sao chép
   */
  function handleCopy() {
    if (!writeText.value) {
      createMessage.warning('Tạm thời chưa có nội dung để sao chép');
      return;
    }
    const success = copyTextToClipboard(writeText.value);
    if (success) {
      createMessage.success('Sao chép thành công');
    } else {
      createMessage.error('Sao chép thất bại');
    }
  }

  function scrollToBottom() {
    const el = previewRef.value;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  }

  /**
   * Tạo
   *
   * @param data
   * @param type
   */
  async function onGenerate(data, type = '') {
    isEditing.value = false;
    data.responseMode = 'streaming';
    writeText.value = '';
    generating.value = true;
    activeVersion.value = CURRENT_VERSION_KEY;
    if (!type) {
      leftData.value = data;
    }

    try {
      let readableStream = await defHttp
        .post(
          {
            url: '/airag/chat/genAiWriter',
            params: { ...data },
            timeout: 5 * 60 * 1000,
            adapter: 'fetch',
            responseType: 'stream',
          },
          {
            isTransformResponse: false,
          }
        )
        .catch((res) => {
          createMessage.warn(res.message ? res.message : 'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!');
        });

      const reader = readableStream.getReader();
      const decoder = new TextDecoder('UTF-8');
      let buffer = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          break;
        }
        let chunk = decoder.decode(value, { stream: true });
        buffer += chunk;

        // Xử lý tin nhắn có thể bị cắt ngắn
        const lines = buffer.split('\n\n');
        // Giữ lại đoạn cuối cùng (có thể không hoàn chỉnh)
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.replace('data:', '').trim();
            if(!content){
              continue;
            }
            if(!content.endsWith('}')){
              buffer = buffer + line;
              continue;
            }
            buffer = "";
            renderText(content)
          } else {
            if(!line) {
              continue;
            }
            if(!line.endsWith('}')) {
              buffer = buffer + line;
              continue;
            }
            buffer = "";
            renderText(line)
          }
        }
      }
    } catch (e) {
      console.error('Generation error:', e);
      writeText.value += '\n\n[Tạo xảy ra lỗi]';
    } finally {
      // Nếu phía máy chủ kết thúc mà chưa kích hoạt MESSAGE_END, đóng trạng thái tạo ở mức dự phòng
      generating.value = false;
    }
  }

  /**
   * Hiển thị văn bản
   *
   * @param item
   */
  function renderText(item) {
    try {
      let parse = JSON.parse(item);
      if(parse.event == 'NODE_FINISHED'){
        afterNodeFinished.value = true;
        return;
      }
      if (parse.event == 'MESSAGE') {
        aiText.value = '';
        if (afterNodeFinished.value) {
          writeText.value = parse.data.message;
          afterNodeFinished.value = false;
        } else {
          writeText.value += parse.data.message;
        }
        generating.value = true;
        polishLoading.value = true;
        nextTick(scrollToBottom);
      }
      if (parse.event == 'MESSAGE_END') {
        generating.value = false;
        if (activeVersion.value === CURRENT_VERSION_KEY) {
          originalContent.value = writeText.value;
        }
        nextTick(scrollToBottom);
      }
      if (parse.event == 'ERROR') {
        writeText.value = parse.data.message ? parse.data.message : 'Tạo thất bại, vui lòng thử lại sau!';
        generating.value = false;
        polishLoading.value = false;
        nextTick(scrollToBottom);
      }
      
      //Bắt đầu thêm dấu chấm
      if (parse.event === 'NODE_STARTED') {
        if (!parse.data || parse.data.type !== 'end') {
          if (parse.data.type === 'llm' || parse.data.type === 'reply') {
            aiText.value = 'Đang xây dựng nội dung phản hồi';
          }
        }
      }
      //Nút kết thúc quy trình
      if (parse.event == 'FLOW_FINISHED') {
        if (parse.data && !parse.data.success) {
          writeText.value = parse.data.message ? parse.data.message : 'Tạo thất bại, vui lòng thử lại sau!';
        }
        generating.value = false;
        polishLoading.value = false;
      }
    } catch (error) {
      console.log('Error parsing update:', error);
    }
  }

  /**
   * Khối mã
   * @param str
   * @param lang
   */
  function highlightBlock(str: string, lang?: string) {
    return `<pre class="code-block-wrapper"><div class="code-block-header"><span class="code-block-header__lang">${lang}</span><span class="code-block-header__copy">Sao chép mã</span></div><code class="hljs code-block-body ${lang}">${str}</code></pre>`;
  }


  /**
   * Trau chuốt
   */
  async function handlePolish() {
    if (!writeText.value) {
      createMessage.warning('Tạm thời chưa có nội dung để trau chuốt');
      return;
    }
    if (generating.value || polishLoading.value) {
      return;
    }
    polishLoading.value = true;
    const data: any = {
      prompt: writeText.value,
      originalContent: '',
      length: leftData.value.length,
      format: leftData.value.format,
      tone: leftData.value.tone,
      language: leftData.value.language,
      activeMode: 'polish',
    };
    try {
      await onGenerate(data, 'polish');
    } finally {
      polishLoading.value = false;
    }
  }

  /**
   * Lưu
   */
  async function handleSave() {
    if (!writeText.value) {
      createMessage.warning('Tạm thời chưa có nội dung để lưu');
      return;
    }
    if (saving.value) {
      return;
    }
    try {
      saving.value = true;
      await defHttp.post({ url: '/airag/app/save/article/write', params: { content: writeText.value } });
      if (activeVersion.value === CURRENT_VERSION_KEY) {
        originalContent.value = writeText.value;
      }
      initHistoryData();
    } catch (e) {
      createMessage.error('Lưu thất bại, vui lòng thử lại sau');
    } finally {
      saving.value = false;
    }
  }

  /**
   * Khởi tạo thông tin lịch sử
   */
  function initHistoryData() {
    historyData.value = [];
    defHttp.get({ url: "/airag/app/list/article/write" }, { isTransformResponse: false }).then((res)=>{
      if(res.success){
        historyData.value = res.result;
      }
    })
  }

  /**
   * Xóa phiên bản
   */
  async function handleDeleteVersion() {
    if (activeVersion.value === CURRENT_VERSION_KEY) {
      return;
    }
    const target = historyData.value.find((item) => item.version === activeVersion.value);
    if (!target) {
      return;
    }
    Modal.confirm({
      title: 'Xóa phiên bản',
      content: 'Có xác nhận xóa phiên bản này không?',
      okText: 'Xác định',
      cancelText: 'Hủy',
      async onOk() {
        try {
          await defHttp.delete(
            { url: '/airag/app/delete/article/write', params: { version: target.version } },
            { joinParamsToUrl: true }
          );
          historyData.value = historyData.value.filter((item) => item.version !== target.version);
          activeVersion.value = CURRENT_VERSION_KEY;
          writeText.value = originalContent.value ?? '';
        } catch (e) {
          createMessage.error('Xóa thất bại, vui lòng thử lại sau');
        }
      },
    });
  }

  /**
   * Sự kiện chuyển đổi phiên bản
   * @param value
   */
  function handleVersionChange(value: string) {
    if (value === CURRENT_VERSION_KEY) {
      activeVersion.value = value;
      writeText.value = originalContent.value ?? '';
      return;
    }
    const target = historyData.value.find((item) => item.version === value);
    if (!target) {
      return;
    }
    activeVersion.value = value;
    writeText.value = target.content;
  }
  
  onMounted(() => {
    //Khi khởi tạo thì tải phiên bản lịch sử
    initHistoryData();
  });
</script>

<style scoped lang="less">
  .writer-wrap {
    display: flex;
    height: 100%;
    padding: 16px;
    gap: 16px;
    background: #fff;
  }

  .sidebar {
    width: 340px;
    flex-shrink: 0;
    border: 1px solid #eef0f5;
    border-radius: 12px;
    padding: 16px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
    background: #fff;
    overflow: visible;
    position: sticky;
    top: 16px;
  }
  /*begin  Kiểu phần đầu */
  .preview {
    flex: 1;
    border: 1px solid #eef0f5;
    border-radius: 12px;
    padding: 16px 20px;
    overflow: auto;
    background: #fafbff;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
  }

  .preview-header {
    font-size: 14px;
    color: #344767;
    margin-bottom: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eef0f5;
    padding-bottom: 10px;
  }
  .preview-header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .preview-title-text {
    font-weight: 500;
    letter-spacing: 0.5px;
  }

  .preview-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .version-select {
    min-width: 120px;
  }
  
  .preview-action-btn {
    padding: 0 14px;
    font-size: 12px;
    border-radius: 4px;
    height: 26px;
    line-height: 24px;
  }

  .custom-green-btn {
    background-color: @primary-color;
    border-color: @primary-color;
    color: #ffffff;

    &:hover,
    &:focus {
      background-color: @primary-color;
      border-color: @primary-color;
      color: #ffffff;
    }
  }

  .preview-actions-icon {
    font-size: 12px;
    color: white;
  }

  .custom-save-btn, .version-delete-btn {
    background-color: #ffffff;
    border-color: @primary-color;
    color: @primary-color;

    &:hover,
    &:focus {
      background-color: fade(@primary-color, 10%);
      border-color: @primary-color;
      color: @primary-color;
    }

    .preview-actions-icon {
      color: @primary-color;
    }
  }
  /*end  Kiểu phần đầu */

  /*begin Kiểu của trình soạn thảo*/
  .markdown-container {
    min-height: 300px;
    padding: 8px 4px 16px 4px;
    border-radius: 8px;
    background-color: #fff;
    cursor: text;
    :deep(img) {
      width: 40% !important;
      max-width: 280px;
      height: auto;
      border-radius: 6px;
      display: block;
    }
  }

  .markdown-editor-container {
    min-height: 300px;
  }

  :deep(.typing-dot) {
    display: inline-block;
    width: 6px;
    height: 6px;
    margin-left: 3px;
    border-radius: 50%;
    background-color: #999;
    vertical-align: baseline;
    animation: typing-dot 1.2s infinite ease-in-out;
  }

  :deep(.typing-dot-delay-1) {
    animation-delay: 0.2s;
  }

  :deep(.typing-dot-delay-2) {
    animation-delay: 0.4s;
  }

  @keyframes typing-dot {
    0%,
    80%,
    100% {
      transform: scale(0.6);
      opacity: 0.6;
    }
    40% {
      transform: scale(1);
      opacity: 1;
    }
  }
  /*end Kiểu của trình soạn thảo*/
</style>
