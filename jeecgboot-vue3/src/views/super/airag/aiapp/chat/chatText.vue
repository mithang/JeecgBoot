<template>
  <div v-if="parsedText != '' || error" class="textWrap" :class="[inversion === 'user' ? 'self' : (isOnlyImage ? 'chatgpt-image' : 'chatgpt')]" ref="textRef">
    <a-tooltip v-if="inversion !== 'user' && !isOnlyImage && !error && !loading && !hideCopyButton" :title="copied ? 'Đã sao chép' : 'Sao chép Markdown'">
      <a-button class="copyMarkdownBtn" type="text" size="small" :aria-label="copied ? 'Đã sao chép' : 'Sao chép Markdown'" @click="handleCopyMarkdown">
        <CheckOutlined v-if="copied" />
        <CopyOutlined v-else />
      </a-button>
    </a-tooltip>
    <div v-if="inversion != 'user'" :style="{ width: getIsMobile? screenWidth : 'auto' }">
      <div ref="markdownBodyRef" class="markdown-body" :class="{ 'markdown-body-generate': loading }" v-html="parsedText" />
      <template v-if="showRefKnow">
        <a-divider orientation="left">Trích dẫn</a-divider>
        <template v-for="(item, idx) in referenceKnowledge" :key="idx">
          <a-tooltip
            :title="item.content?.substring(0, 800)"
            overlayClassName="knowledge-reference-tooltip"
            :overlayInnerStyle="knowledgeTooltipStyle"
          >
            <a-tag style="min-width: 80px;background: #F7F8FA;padding-inline: 0 7px">
              <a-space style="min-height: 30px;padding-left: 4px;padding-right: 4px;background-color: #F0F1F6;color: #788194">
                <div>{{ 'chunk-' + item.chunk}}</div>
              </a-space>
              <a-space style="min-height: 30px;padding-left: 4px;">
                <img :src="knowledgePng" width="14" height="14" style="position: relative; top: -2px"/>
                <div style="max-width: 240px; overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">
                  {{ item.docName }}
                </div>
              </a-space>
            </a-tag>
          </a-tooltip>
        </template>
      </template>
      <div v-if="error" class="error-message">
        <p>{{ errorMsg }}</p>
      </div>
    </div>
    <div v-else class="msg user-message-text">{{ props.text }}</div>
  </div>
  <ImageViewer v-if="amplifyImage" :imageUrl="imageUrl" @hide="pictureHide"></ImageViewer>
  <!-- Hiển thị JeecgTag trong trò chuyện -->
  <template v-if="jeecgTagList.length">
    <template v-for="item of jeecgTagList" :key="item.key">
      <teleport :to="item.to">
        <Component :is="item.tag.component" :data="item.data" :loading="loading" />
      </teleport>
    </template>
  </template>
</template>

<script setup lang="ts">
  import type { JeecgTag } from './jeecg-tags/types';
  import { computed, nextTick, onMounted, onUnmounted, onUpdated, ref, watch } from 'vue';
  import { throttle } from 'lodash-es';
  import md5 from 'crypto-js/md5';
  import MarkdownIt from 'markdown-it';
  import mdKatex from '@traptitech/markdown-it-katex';
  import mila from 'markdown-it-link-attributes';
  import hljs from 'highlight.js';
  import './style/github-markdown.less';
  import './style/highlight.less';
  import './style/style.less';
  import ImageViewer from '@/views/super/airag/aiapp/chat/components/ImageViewer.vue';
  import { useAppInject } from "@/hooks/web/useAppInject";
  import { useGlobSetting } from "@/hooks/setting";
  import { mdPluginJeecgTag, JEECG_TAG_CLASS, jeecgTagMap } from './jeecg-tags'
  import knowledgePng from '../../aiknowledge/icon/knowledge.png'
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  import { CheckOutlined, CopyOutlined } from '@ant-design/icons-vue';

  /**
   * Độ rộng màn hình
   */
  const screenWidth = ref<string>();
  const { domainUrl } = useGlobSetting();
  const { getIsMobile } = useAppInject();

  //update-begin---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---
  const props = defineProps(['dateTime', 'text', 'inversion', 'error', 'errorMsg', 'currentToolTag', 'loading', 'referenceKnowledge', 'isLast', 'hideCopyButton']);
  //update-end---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---
  const textRef = ref();
  const markdownBodyRef = ref<HTMLDivElement>();
  const copied = ref(false);
  const knowledgeTooltipStyle = { maxHeight: '240px', overflowY: 'auto', scrollbarGutter: 'stable' };
  let copyResetTimer: ReturnType<typeof setTimeout> | undefined;

  //Địa chỉ hình ảnh
  const imageUrl = ref<string>('');
  //Có phóng to hình ảnh hay không
  const amplifyImage = ref<boolean>(false);

  const parsedText = ref<string>('');

  // Danh sách jeecgTag phân tích được
  const jeecgTagList = ref<{
    key: string;
    to: HTMLDivElement;
    tag: JeecgTag;
    data: string;
  }[]>([]);

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

  // Plugin mdi tùy chỉnh
  mdi.use(mdPluginJeecgTag);
  // Plugin mdi chính thức
  mdi.use(mila, { attrs: { target: '_blank', rel: 'noopener' } });
  mdi.use(mdKatex, { blockClass: 'katexmath-block rounded-md p-[10px]', errorColor: ' #cc0000' });

  /**
   * Xử lý văn bản trò chuyện và cập nhật tiết lưu trong một khoảng thời gian nhất định
   */
  const updateTextContent = throttle(() => {
    let value = props.text ?? '';
    if (props.inversion !== 'user') {
      // Trước tiên thay thế trình giữ chỗ độ rộng hình ảnh và tên miền rồi mới hiển thị markdown
      value = replaceImageWith(value);
      value = replaceDomainUrl(value);
      parsedText.value = sanitizeRichText(mdi.render(value));
      // Phân tích nhãn jeecgTag
      parseJeecgTag();
      return;
    }
    parsedText.value = value;
  }, 100);

  // Có hiển thị trích dẫn kho tri thức hay không
  const showRefKnow = computed(() => {
    const {loading, referenceKnowledge} = props
    if (loading) {
      return false;
    }
    return Array.isArray(referenceKnowledge) && referenceKnowledge.length > 0;
  })

  // Phán đoán có chỉ có hình ảnh hay không
  const isOnlyImage = computed(() => {
    if (showRefKnow.value){
      return false;
    }
    
    const content = props.text || '';
    if (!content){
      return false;
    }
    
    //Khớp![Hình ảnh 1](url1) 
    const imageRegex = /!\[.*?\]\(.*?\)/g;
    if (!imageRegex.test(content)) {
      return false;
    }
    
    //Sau khi thay thế có tồn tại văn bản hay không
    const remaining = content.replace(imageRegex, '').trim();
    return remaining.length === 0;
  });

  // Lắng nghe thay đổi văn bản, kích hoạt cập nhật giao diện
  watch(() => props.text, () => updateTextContent(), {immediate: true});

  // Lắng nghe thay đổi của công cụ đang gọi, hiển thị thêm nội dung
  watch(() => props.currentToolTag, () => {
    const {isLast, inversion, currentToolTag, loading} = props;
    if (isLast && inversion != 'user' && currentToolTag && loading) {
      parsedText.value += sanitizeRichText(mdi.render(currentToolTag));
      // Phân tích nhãn jeecgTag
      parseJeecgTag();
    }
  }, {immediate: true});

  //Thay thế độ rộng hình ảnh
  function replaceImageWith(markdownContent) {
    //update-begin---author:wangshuai---date:2026-01-08---for: Tương thích trả về nhiều hình ảnh, điều chỉnh độ rộng mặc định của ảnh trong tập hình ảnh---
    // 1. Nhiều hình ảnh xuất ra liên tiếp ưu tiên dùng lưới chín ô, bao gồm ảnh minh họa bài viết có width
    const regexCustomWidth = /!\[([^\]]*)\]\(([^)]+)\s=([0-9]+)\)/g;
    const regexStandard = /!\[([^\]]*)\]\(([^)]+)\)/g;
    const regexContinuousImages = /(?:!\[[^\]]*\]\([^)]+\)[ \t]*(?:\r?\n[ \t]*)*){2,}/g;
    const renderImage = (alt: string, src: string, className: string) => {
      const reg = /#\s*{\s*domainURL\s*}/g;
      const imageSrc = src.replace(/\s=[0-9]+$/, '').replace(reg, domainUrl);
      return `<div class="${className}"><img src='${imageSrc}' alt='${alt}' /></div>`;
    };

    markdownContent = markdownContent.replace(regexContinuousImages, (imageGroup) => {
      return imageGroup.replace(regexStandard, (_match, alt, src) => renderImage(alt, src, 'chat-image-grid-item'));
    });

    // 2. Hỗ trợ cách viết đặt width cho hình ảnh ![](/static/jimuImages/screenshot_1617252560523.png =100)
    // Phải có dấu cách, tránh khớp vào trong tham số url=
    markdownContent = markdownContent.replace(regexCustomWidth, (_match, alt, src, width) => {
      const reg = /#\s*{\s*domainURL\s*}/g;
      const imageSrc = src.replace(reg, domainUrl);
      return `<div class="chat-image-custom"><img src='${imageSrc}' alt='${alt}' width='${width}' /></div>`;
    });

    // 3. Hình ảnh thường không liên tiếp trong bài viết giữ hiển thị ảnh đơn
    markdownContent = markdownContent.replace(regexStandard, (_match, alt, src) => renderImage(alt, src, 'chat-image-single'));
    return markdownContent;
    //update-end---author:wangshuai---date:2026-01-08---for: Tương thích trả về nhiều hình ảnh, điều chỉnh độ rộng mặc định của ảnh trong tập hình ảnh---
  };

  //Thay thế domainURL
  function replaceDomainUrl(markdownContent) {
    const regex = /!\[([^\]]*)\]\(.*?#\s*{\s*domainURL\s*}.*?\)/g;
    return markdownContent.replace(regex, (match) => {
      let reg = /#\s*{\s*domainURL\s*}/g;
      return match.replace(reg,domainUrl);
    })
  }

  function highlightBlock(str: string, lang?: string) {
    return `<pre class="code-block-wrapper"><div class="code-block-header"><span class="code-block-header__lang">${lang}</span><span class="code-block-header__copy">Sao chép mã</span></div><code class="hljs code-block-body ${lang}">${str}</code></pre>`;
  }
  function addCopyEvents() {
    if (textRef.value) {
      const copyBtn = textRef.value.querySelectorAll('.code-block-header__copy');
      copyBtn.forEach((btn) => {
        btn.addEventListener('click', () => {
          const code = btn.parentElement?.nextElementSibling?.textContent;
          if (code) {
            copyToClip(code).then(() => {
              btn.textContent = 'Sao chép thành công';
              setTimeout(() => {
                btn.textContent = 'Sao chép mã';
              }, 1e3);
            });
          }
        });
      });
    }
  }

  function removeCopyEvents() {
    if (textRef.value) {
      const copyBtn = textRef.value.querySelectorAll('.code-block-header__copy');
      copyBtn.forEach((btn) => {
        btn.removeEventListener('click', () => {});
      });
    }
  }


  /**
   * Sự kiện nhấp thêm hình ảnh
   */
  function addImageClickEvent() {
    if (textRef.value) {
      const image = textRef.value.querySelectorAll('img');
      image.forEach((img) => {
        img.addEventListener('click', () => {
          imageUrl.value = img.src;
          amplifyImage.value = true;
        })
      });
    }
  }


  /**
   * Sự kiện nhấp loại bỏ hình ảnh
   */
  function removeImageClickEvent(){
    if (textRef.value) {
      const image = textRef.value.querySelectorAll('img');
      image.forEach((img) => {
        img.removeEventListener('click', () => { })
      });
    }
  }

  /**
   * Ẩn hình ảnh
   */
  function pictureHide(){
    amplifyImage.value = false;
    imageUrl.value = ""
  }


  /**
   * Đặt độ rộng tổng thể của markdown body
   */
  function setMarkdownBodyWidth() {
    //Máy tính bảng
    console.log("window.innerWidth::",window.innerWidth)
    if(window.innerWidth>600 && window.innerWidth<1024){
      screenWidth.value = window.innerWidth - 120 + 'px';
    }else if(window.innerWidth < 600){
      //Điện thoại
      screenWidth.value = window.innerWidth - 60 + 'px';
    }
  }

  // Phân tích nhãn JeecgTag
  async function parseJeecgTag() {
    await nextTick();
    if (!markdownBodyRef.value) {
      return;
    }

    jeecgTagList.value = [];
    const elements = markdownBodyRef.value.querySelectorAll('.' + JEECG_TAG_CLASS);
    elements.forEach((el) => {
      const tagName = el.nodeName.toLowerCase();
      const tag = jeecgTagMap.get(tagName);
      if (!tag) {
        console.warn(`Nhãn jeecg chưa nhận diện:`, tagName, el);
        return;
      }
      const renderEl = el.querySelector('render') as HTMLDivElement;
      if (!renderEl) {
        if (props.loading) {
          // Đang hiển thị
          el.innerHTML = `<div style="color: #888; margin-top: 12px;">Đang hiển thị biểu đồ, vui lòng chờ...</div>`;
          return;
        }
        el.innerHTML = `<div style="color: red;">Định dạng hiển thị biểu đồ do mô hình trả về không đúng, vui lòng tối ưu prompt hoặc thử lại.</div>`;
        return;
      }

      const dataEl = el.querySelector('data');
      const dataStr = dataEl?.textContent || '';
      renderJeecgTag(tag, dataStr, renderEl);
    });
  }

  /**
   * Gửi hiển thị nhãn jeecg
   */
  function renderJeecgTag(tag: JeecgTag, data: string, renderEl: HTMLDivElement) {
    jeecgTagList.value.push({
      key: md5(tag.name + '_' + data).toString(),
      to: renderEl,
      tag: tag,
      data: data,
    });
  }

  /**
   * Sao chép nội dung Markdown gốc của câu trả lời cuối cùng
   */
  async function handleCopyMarkdown() {
    const markdown = props.text ?? '';
    if (!markdown) {
      return;
    }
    await copyToClip(markdown);
    copied.value = true;
    clearTimeout(copyResetTimer);
    copyResetTimer = setTimeout(() => {
      copied.value = false;
    }, 1500);
  }

  onMounted(() => {
    addCopyEvents();
    addImageClickEvent();
    setMarkdownBodyWidth();
    window.addEventListener('resize', setMarkdownBodyWidth);
  });

  onUpdated(() => {
    addCopyEvents();
    addImageClickEvent();
  });

  onUnmounted(() => {
    removeCopyEvents();
    removeImageClickEvent();
    window.removeEventListener('resize', setMarkdownBodyWidth);
    clearTimeout(copyResetTimer);
  });

  function copyToClip(text: string) {
    return new Promise((resolve, reject) => {
      try {
        const input: HTMLTextAreaElement = document.createElement('textarea');
        input.setAttribute('readonly', 'readonly');
        input.value = text;
        document.body.appendChild(input);
        input.select();
        if (document.execCommand('copy')) document.execCommand('copy');
        document.body.removeChild(input);
        resolve(text);
      } catch (error) {
        reject(error);
      }
    });
  }
</script>
<style lang="less" scoped>
  .textWrap {
    box-sizing: border-box;
    min-width: 0;
    max-width: 100%;
    border-radius: 0.375rem;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    padding-left: 0.75rem;
    padding-right: 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
    position: relative;
  }

  .copyMarkdownBtn {
    position: absolute;
    top: 8px;
    right: 8px;
    z-index: 1;
    width: 30px;
    height: 30px;
    padding: 0;
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.82);
    color: #8b95a5;
    opacity: 0.72;
    transition: color 0.2s ease, background-color 0.2s ease, opacity 0.2s ease;

    &:hover,
    &:focus-visible {
      background: #fff;
      color: @primary-color;
      opacity: 1;
    }
  }

  .error {
    background: linear-gradient(135deg, #FF4444, #FF914D) !important;
    border-radius: 0.375rem;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    padding-left: 0.75rem;
    padding-right: 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
  }

  .error-message {
    color: #FF4444 !important
  }

  .self {
    // background-color: #d2f9d1;
    background-color: @primary-color;
    color: #fff;
    overflow-wrap: break-word;
    line-height: 1.625;
    min-width: 20px;
  }
  .user-message-text {
    white-space: pre-wrap;
  }
  .chatgpt {
    background-color: #f4f6f8;

    font-size: 0.875rem;
    line-height: 1.25rem;

    :deep(.markdown-body) {
      box-sizing: border-box;
      max-width: 100%;
      overflow-wrap: anywhere;
      padding-right: 48px;
    }
  }
  .chatgpt-image {
    .markdown-body{
      background-color: transparent !important;
    }
  }
  @media (max-width: 1024px) {
    //Kiểu trên điện thoại và máy tính bảng
    .textWrap{
      margin-left: -40px;
      margin-top: 10px;
    }
  }

  // Kiểu của hình ảnh được tạo
  :deep(.chat-image-grid-item) {
    display: inline-block;
    width: 24%;
    padding: 4px;
    box-sizing: border-box;
    vertical-align: top;
    img {
      width: 100%;
      height: auto;
      border-radius: 4px;
      cursor: pointer;
      object-fit: cover;
      aspect-ratio: 1/1;
    }
  }
  :deep(.chat-image-single) {
    img {
      max-width: 50%;
      border-radius: 4px;
      cursor: pointer;
    }
  }

  .markdown-body {
    :deep(.jeecg-tag) {
      display: block;
      margin: 8px 0;

      data {
        display: none;
      }

      render {
        display: block;
        width: 100%;
        height: auto;
        min-width: 300px;
      }
    }
  }

  :global(.knowledge-reference-tooltip .ant-tooltip-inner) {
    scrollbar-color: #aeb7c5 #4a4a4a;
    scrollbar-width: auto;
  }

  :global(.knowledge-reference-tooltip .ant-tooltip-inner::-webkit-scrollbar) {
    width: 10px;
  }

  :global(.knowledge-reference-tooltip .ant-tooltip-inner::-webkit-scrollbar-track) {
    background: #4a4a4a;
    border-radius: 5px;
  }

  :global(.knowledge-reference-tooltip .ant-tooltip-inner::-webkit-scrollbar-thumb) {
    background: #aeb7c5;
    border: 2px solid #4a4a4a;
    border-radius: 5px;
  }

  :global(.knowledge-reference-tooltip .ant-tooltip-inner::-webkit-scrollbar-thumb:hover) {
    background: #d7dce3;
  }

</style>
