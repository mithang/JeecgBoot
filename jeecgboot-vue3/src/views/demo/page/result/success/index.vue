<template>
  <div class="m-5 result-success">
    <Result
      status="success"
      title="Gửi thành công"
      sub-title="Trang kết quả gửi dùng để phản hồi kết quả xử lý của một loạt nhiệm vụ thao tác. Nếu chỉ là thao tác đơn giản, dùng thông báo toàn cục Message để phản hồi là đủ. Vùng văn bản này có thể hiển thị phần giải thích bổ sung đơn giản, nếu có nhu cầu hiển thị dạng &quot;chứng từ&quot; tương tự, vùng màu xám bên dưới có thể trình bày nội dung phức tạp hơn."
    >
      <template #extra>
        <a-button key="console" type="primary"> Quay lại danh sách </a-button>
        <a-button key="buy"> Xem dự án </a-button>
        <a-button key="buy"> In </a-button>
      </template>
    </Result>
    <div class="result-success__content">
      <Descriptions title="Tên dự án">
        <DescriptionItem label="Dự án ID"> 111222 </DescriptionItem>
        <DescriptionItem label="Người phụ trách"> Jeecg </DescriptionItem>
        <DescriptionItem label="Thời gian có hiệu lực"> 2016-12-12 ~ 2017-12-12 </DescriptionItem>
      </Descriptions>
      <Steps :current="1" progress-dot size="small">
        <Step title="Tạo dự án">
          <template #description> <div>Jeecg</div> <p>2016-12-12 12:32</p> </template>
        </Step>
        <Step title="Sơ duyệt của phòng ban">
          <template #description>
            <p>Chad</p>
          </template>
        </Step>
        <Step title="Kiểm tra lại tài chính" />
        <Step title="Hoàn thành" />
      </Steps>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result, Steps, Descriptions } from 'ant-design-vue';
  export default defineComponent({
    components: {
      Result,
      Steps,
      Step: Steps.Step,
      Descriptions,
      DescriptionItem: Descriptions.Item,
    },
  });
</script>
<style lang="less" scoped>
  .result-success {
    padding: 48px 32px;
    background-color: @component-background;

    &__content {
      padding: 24px 40px;
      background-color: @background-color-light;
    }
  }
</style>
