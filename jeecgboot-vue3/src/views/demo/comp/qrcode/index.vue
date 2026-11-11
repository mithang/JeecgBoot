<template>
  <PageWrapper title="Ví dụ sử dụng component mã QR">
    <div class="flex flex-wrap">
      <CollapseContainer title="Ví dụ cơ bản" :canExpan="true" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ render thành thẻ img" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" tag="img" />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ cấu hình kiểu" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          :options="{
            color: { dark: '#55D187' },
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ logo cục bộ" class="text-center mb-6 qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :logo="LogoImg" />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ logo trực tuyến" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          logo="http://jeecg.com/images/logo.png"
          :options="{
            color: { dark: '#55D187' },
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="logoVí dụ cấu hình" class="text-center mb-6 qrcode-demo-item">
        <QrCode
          :value="qrCodeUrl"
          :logo="{
            src: 'http://jeecg.com/images/logo.png',
            logoSize: 0.2,
            borderSize: 0.05,
            borderRadius: 50,
            bgColor: 'blue',
          }"
        />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ tải xuống" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" ref="qrRef" :logo="LogoImg" />
        <a-button class="mb-2" type="primary" @click="download"> Tải xuống </a-button>
        <div class="msg"> (Logo trực tuyến sẽ khiến hình ảnh bị cross-domain, nếu cần tải hình ảnh xuống thì tự giải quyết vấn đề cross-domain) </div>
      </CollapseContainer>

      <CollapseContainer title="Ví dụ cấu hình kích thước" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :width="300" />
      </CollapseContainer>

      <CollapseContainer title="Ví dụ vẽ mở rộng" class="text-center qrcode-demo-item">
        <QrCode :value="qrCodeUrl" :width="200" :options="{ margin: 5 }" ref="qrDiyRef" :logo="LogoImg" @done="onQrcodeDone" />
        <a-button class="mb-2" type="primary" @click="downloadDiy"> Tải xuống </a-button>
        <div class="msg"> Muốn vẽ mở rộng thì không thể đặt tag thànhimg </div>
      </CollapseContainer>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { QrCode, QrCodeActionType } from '/@/components/Qrcode/index';
  import LogoImg from '/@/assets/images/logo.png';
  import { CollapseContainer } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';

  const qrCodeUrl = 'https://www.vvbin.cn';
  export default defineComponent({
    components: { CollapseContainer, QrCode, PageWrapper },
    setup() {
      const qrRef = ref<Nullable<QrCodeActionType>>(null);
      const qrDiyRef = ref<Nullable<QrCodeActionType>>(null);
      function download() {
        const qrEl = unref(qrRef);
        if (!qrEl) return;
        qrEl.download('Tên tệp');
      }
      function downloadDiy() {
        const qrEl = unref(qrDiyRef);
        if (!qrEl) return;
        qrEl.download('Qrcode');
      }

      function onQrcodeDone({ ctx }: any) {
        if (ctx instanceof CanvasRenderingContext2D) {
          // Vẽ thêm
          ctx.fillStyle = 'black';
          ctx.font = '16px "微软雅黑"';
          ctx.textBaseline = 'bottom';
          ctx.textAlign = 'center';
          ctx.fillText('Bạn đẹp trai, bạn quét trước', 100, 195, 200);
        }
      }
      return {
        onQrcodeDone,
        qrCodeUrl,
        LogoImg,
        download,
        downloadDiy,
        qrRef,
        qrDiyRef,
      };
    },
  });
</script>
<style scoped>
  .qrcode-demo-item {
    width: 30%;
    margin-right: 1%;
  }
</style>
