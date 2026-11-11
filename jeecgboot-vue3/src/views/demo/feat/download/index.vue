<template>
  <PageWrapper title="Ví dụ tải xuống tệp">
    <a-alert message="Tải xuống theo luồng tệp của API phía máy chủ" />
    <a-button type="primary" class="my-4" @click="handleDownByData"> Tải xuống luồng tệp </a-button>

    <a-alert message="Tải xuống tệp theo địa chỉ tệp" />
    <a-button type="primary" class="my-4" @click="handleDownloadByUrl"> Tải xuống theo địa chỉ tệp </a-button>

    <a-alert message="base64Tải xuống dạng stream" />
    <a-button type="primary" class="my-4" @click="handleDownloadByBase64"> base64Tải xuống dạng stream </a-button>

    <a-alert message="Tải xuống hình ảnh qua Url,Nếu có vấn đề cross-domain, cần xử lý cross-domain hình ảnh" />
    <a-button type="primary" class="my-4" @click="handleDownloadByOnlineUrl"> Tải xuống hình ảnh qua Url </a-button>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { downloadByUrl, downloadByData, downloadByBase64, downloadByOnlineUrl } from '/@/utils/file/download';
  import imgBase64 from './imgBase64';
  import { PageWrapper } from '/@/components/Page';
  import { Alert } from 'ant-design-vue';

  export default defineComponent({
    components: { PageWrapper, [Alert.name]: Alert },
    setup() {
      function handleDownByData() {
        downloadByData('text content', 'testName.txt');
      }
      function handleDownloadByUrl() {
        downloadByUrl({
          url: 'https://codeload.github.com/anncwb/vue-Jeecg-admin-doc/zip/master',
          target: '_self',
        });

        downloadByUrl({
          url: 'https://vebn.oss-cn-beijing.aliyuncs.com/Jeecg/logo.png',
          target: '_self',
        });
      }

      function handleDownloadByBase64() {
        downloadByBase64(imgBase64, 'logo.png');
      }

      function handleDownloadByOnlineUrl() {
        downloadByOnlineUrl(
          'https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/5944817f47b8408e9f1442ece49d68ca~tplv-k3u1fbpfcp-watermark.image',
          'logo.png'
        );
      }
      return {
        handleDownloadByUrl,
        handleDownByData,
        handleDownloadByBase64,
        handleDownloadByOnlineUrl,
      };
    },
  });
</script>
