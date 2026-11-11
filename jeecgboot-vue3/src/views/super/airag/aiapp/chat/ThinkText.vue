<template>
  <div v-if="text != ''" class="textWrap" :class="[inversion === 'user' ? 'self' : 'chatgpt']" ref="textRef">
    <div :style="{ width: getIsMobile ? screenWidth : 'auto' }">
      <div class="markdown-body" :class="{ 'markdown-body-generate': loading }" :style="{ color: error ? '#FF4444 !important' : '' }" v-html="text" />
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, onUnmounted, onUpdated, ref } from 'vue';
  import MarkdownIt from 'markdown-it';
  import './style/github-markdown.less';
  import './style/highlight.less';
  import './style/style.less';
  import { useAppInject } from '@/hooks/web/useAppInject';
  import hljs from 'highlight.js';
  import mila from 'markdown-it-link-attributes';
  import mdKatex from '@traptitech/markdown-it-katex';
  import { useGlobSetting } from '@/hooks/setting';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';

  /**
   * Độ rộng màn hình
   */
  const screenWidth = ref<string>();
  const { getIsMobile } = useAppInject();

  const props = defineProps(['dateTime', 'text', 'inversion', 'error', 'loading', 'referenceKnowledge']);
  const textRef = ref();
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

  const text = computed(() => {
    let value = props.text ?? '';
    if (props.inversion != 'user') {
      value = replaceImageWith(value);
      value = replaceDomainUrl(value);
      return sanitizeRichText(mdi.render(value));
    }
    return sanitizeRichText(value.replace(/\n/g, '<br>'));
  });

  // Có hiển thị trích dẫn kho tri thức hay không
  const showRefKnow = computed(() => {
    const { loading, referenceKnowledge } = props;
    if (loading) {
      return false;
    }
    return Array.isArray(referenceKnowledge) && referenceKnowledge.length > 0;
  });

  //Thay thế độ rộng hình ảnh
  const replaceImageWith = (markdownContent) => {
    // Hỗ trợ cách viết đặt width cho hình ảnh ![](/static/jimuImages/screenshot_1617252560523.png =100)
    const regex = /!\[([^\]]*)\]\(([^)]+)=([0-9]+)\)/g;
    return markdownContent.replace(regex, (match, alt, src, width) => {
      let reg = /#\s*{\s*domainURL\s*}/g;
      src = src.replace(reg, domainUrl);
      return `<div><img src='${src}' alt='${alt}' width='${width}' /></div>`;
    });
  };
  const { domainUrl } = useGlobSetting();
  //Thay thế domainURL
  const replaceDomainUrl = (markdownContent) => {
    const regex = /!\[([^\]]*)\]\(.*?#\s*{\s*domainURL\s*}.*?\)/g;
    return markdownContent.replace(regex, (match) => {
      let reg = /#\s*{\s*domainURL\s*}/g;
      return match.replace(reg, domainUrl);
    });
  };

  //Có phóng to hình ảnh hay không
  const amplifyImage = ref<boolean>(false);
  //Địa chỉ hình ảnh
  const imageUrl = ref<string>('');

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
        });
      });
    }
  }

  /**
   * Sự kiện nhấp loại bỏ hình ảnh
   */
  function removeImageClickEvent() {
    if (textRef.value) {
      const image = textRef.value.querySelectorAll('img');
      image.forEach((img) => {
        img.removeEventListener('click', () => {});
      });
    }
  }

  /**
   * Ẩn hình ảnh
   */
  function pictureHide() {
    amplifyImage.value = false;
    imageUrl.value = '';
  }

  /**
   * Đặt độ rộng tổng thể của markdown body
   */
  function setMarkdownBodyWidth() {
    //Máy tính bảng
    console.log('window.innerWidth::', window.innerWidth);
    if (window.innerWidth > 600 && window.innerWidth < 1024) {
      screenWidth.value = window.innerWidth - 120 + 'px';
    } else if (window.innerWidth < 600) {
      //Điện thoại
      screenWidth.value = window.innerWidth - 60 + 'px';
    }
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
    border-radius: 0.375rem;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    padding-left: 0.75rem;
    padding-right: 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
    border-left: 1px solid #e1e5ea;
  }

  .error {
    background: linear-gradient(135deg, #ff4444, #ff914d) !important;
    border-radius: 0.375rem;
    padding-top: 0.5rem;
    padding-bottom: 0.5rem;
    padding-left: 0.75rem;
    padding-right: 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;
  }

  .self {
    // background-color: #d2f9d1;
    background-color: @primary-color;
    color: #fff;
    overflow-wrap: break-word;
    line-height: 1.625;
    min-width: 20px;
  }

  .chatgpt {
    font-size: 0.875rem;
    line-height: 1.25rem;
  }

  // Kiểu của phần đã dừng ở phía dưới
  :deep(.markdown-body) {
    color: #333;
    font-family:
      -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial,
      sans-serif;
    line-height: 1.6;
    padding: 14px;

    // Kiểu đoạn văn
    p {
      color: #333;
      line-height: 1.6;
      margin-bottom: 1em;
      margin-top: 0;
    }

    // Kiểu danh sách
    ul,
    ol {
      margin-left: 1.5em;
      margin-bottom: 1em;
      padding-left: 0;

      li {
        color: #333;
        line-height: 1.6;
        margin-bottom: 0.5em;
        padding-left: 0.5em;
      }
    }

    // Từ khóa/Kiểu tô sáng mã
    code {
      background-color: #f0f0f0;
      color: #333;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 0.9em;
    }

    // Mã nội dòng
    p code,
    li code,
    td code {
      background-color: #f0f0f0;
      color: #333;
      padding: 2px 6px;
      border-radius: 3px;
    }

    // Khối mã giữ kiểu vốn có
    pre {
      code {
        background-color: transparent;
        padding: 0;
      }
    }

    // Văn bản nhấn mạnh
    strong,
    b {
      color: #333;
      font-weight: 600;
    }

    // Kiểu liên kết
    a {
      color: #4183c4;
      text-decoration: none;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  @media (max-width: 1024px) {
    //Kiểu trên điện thoại và máy tính bảng
    .textWrap {
      margin-left: -40px;
      margin-top: 10px;
    }
  }
</style>
