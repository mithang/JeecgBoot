<template>
  <PageWrapper
    contentFullHeight
    title="Component cơ bản"
    content=" Component cơ bản phụ thuộc vàoant-design-vue,Các component cơ bản đã có trong thư viện component,Trong dự án sẽ không trình diễn demo nữa (trừ các component được đóng gói lại lần hai)"
  >
    <a-row :gutter="[20, 20]">
      <a-col :xl="10" :lg="24">
        <a-card title="BasicButton Color">
          <div class="my-2">
            <h3>success</h3>
            <div class="py-2">
              <a-button color="success"> Thành công </a-button>
              <a-button color="success" class="ml-2" disabled> Vô hiệu hóa </a-button>
              <a-button color="success" class="ml-2" loading> loading </a-button>
              <a-button color="success" type="link" class="ml-2"> link </a-button>
              <a-button color="success" type="link" class="ml-2" loading> loading link </a-button>
              <a-button color="success" type="link" class="ml-2" disabled> disabled link </a-button>
            </div>
          </div>

          <div class="my-2">
            <h3>warning</h3>
            <a-button color="warning"> Cảnh báo </a-button>
            <a-button color="warning" class="ml-2" disabled> Vô hiệu hóa </a-button>
            <a-button color="warning" class="ml-2" loading> loading </a-button>
            <a-button color="warning" type="link" class="ml-2"> link </a-button>
            <a-button color="warning" type="link" class="ml-2" loading> loading link </a-button>
            <a-button color="warning" type="link" class="ml-2" disabled> disabled link </a-button>
          </div>

          <div class="my-2">
            <h3>error</h3>
            <a-button color="error"> Lỗi </a-button>
            <a-button color="error" class="ml-2" disabled> Vô hiệu hóa </a-button>
            <a-button color="error" class="ml-2" loading> loading </a-button>
            <a-button color="error" type="link" class="ml-2"> link </a-button>
            <a-button color="error" type="link" class="ml-2" loading> loading link </a-button>
            <a-button color="error" type="link" class="ml-2" disabled> disabled link </a-button>
          </div>

          <div class="my-2">
            <h3>ghost</h3>
            <a-button ghost color="success" class="ml-2"> Ghost thành công </a-button>
            <a-button ghost color="warning" class="ml-2"> Ghost cảnh báo </a-button>
            <a-button ghost color="error" class="ml-2"> Ghost lỗi </a-button>
            <a-button ghost type="dashed" color="warning" class="ml-2"> Ghost cảnh báodashed </a-button>
            <a-button ghost danger class="ml-2"> Ghost nguy hiểm </a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :xl="14" :lg="24">
        <a-card title="BasicButton Types">
          <div class="my-2">
            <h3>primary</h3>
            <a-button type="primary" preIcon="mdi:page-next-outline"> Nút chính </a-button>
            <a-button type="primary" class="ml-2" disabled> Vô hiệu hóa </a-button>
            <a-button type="primary" class="ml-2" danger preIcon="mdi:page-next-outline"> Nguy hiểm </a-button>
            <a-button type="primary" class="ml-2" loading> loading </a-button>
            <a-button type="link" class="ml-2"> link </a-button>
            <a-button type="link" class="ml-2" loading> loading link </a-button>
            <a-button type="link" class="ml-2" disabled> disabled link </a-button>
          </div>

          <div class="my-2">
            <h3>default</h3>
            <a-button type="default"> Mặc định </a-button>
            <a-button type="default" class="ml-2" disabled> Vô hiệu hóa </a-button>
            <a-button type="default" class="ml-2" danger> Nguy hiểm </a-button>
            <a-button type="default" class="ml-2" loading> loading </a-button>
            <a-button type="link" class="ml-2"> link </a-button>
            <a-button type="link" class="ml-2" loading> loading link </a-button>
            <a-button type="link" class="ml-2" disabled> disabled link </a-button>
          </div>

          <div class="my-2">
            <h3>dashed</h3>
            <a-button type="dashed"> dashed </a-button>
            <a-button type="dashed" class="ml-2" disabled> Vô hiệu hóa </a-button>
            <a-button type="dashed" class="ml-2" danger> Nguy hiểm </a-button>
            <a-button type="dashed" class="ml-2" loading> loading </a-button>
          </div>

          <div class="my-2">
            <h3>ghost Nút ghost thông thường thường dùng trên nền có màu</h3>
            <div class="bg-gray-400 py-2">
              <a-button ghost type="primary" class="ml-2"> Ghost chính </a-button>
              <a-button ghost type="default" class="ml-2"> Ghost mặc định </a-button>
              <a-button ghost type="dashed" class="ml-2"> Ghostdashed </a-button>
              <a-button ghost type="primary" class="ml-2" disabled> Vô hiệu hóa </a-button>
              <a-button ghost type="primary" class="ml-2" loading> loading </a-button>
            </div>
            <!-- antd Nút không thể dùng đồng thời ghost vàlink -->
            <!--      <a-button ghost type="link" class="ml-2"> link </a-button>-->
            <!--      <a-button ghost type="link" class="ml-2" loading> loading link </a-button>-->
            <!--      <a-button ghost type="link" class="ml-2" disabled> disabled link </a-button>-->
          </div>
        </a-card>
      </a-col>
    </a-row>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { Card, Row, Col } from 'ant-design-vue';
  export default defineComponent({
    components: { PageWrapper, ACard: Card, ARow: Row, ACol: Col },
  });
</script>
