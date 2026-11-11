<template>
  <div class="m-5 result-error">
    <Result status="error" title="Gửi thất bại" sub-title="Vui lòng kiểm tra và sửa các thông tin sau rồi gửi lại.">
      <template #extra>
        <a-button key="console" type="primary"> Quay lại chỉnh sửa </a-button>
      </template>
    </Result>
    <div class="result-error__content">
      <div class="result-error__content-title"> Nội dung bạn đã gửi có các lỗi sau: </div>
      <div class="mb-4">
        <CloseCircleOutlined class="mr-2 result-error__content-icon" />
        Tài khoản của bạn đã bị đóng băng
        <a class="ml-4">Bỏ đóng băng ngay ></a>
      </div>
      <div class="mb-4">
        <CloseCircleOutlined class="mr-2 result-error__content-icon" />
        Tài khoản của bạn chưa đủ điều kiện đăng ký
        <a class="ml-4">Bỏ đóng băng ngay ></a>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result } from 'ant-design-vue';
  import { CloseCircleOutlined } from '@ant-design/icons-vue';
  export default defineComponent({
    components: {
      Result,
      CloseCircleOutlined,
    },
  });
</script>
<style lang="less" scoped>
  .result-error {
    padding: 48px 32px;
    background-color: @component-background;

    &__content {
      padding: 24px 40px;
      background-color: @background-color-light;

      &-title {
        margin-bottom: 16px;
        font-size: 16px;
        font-weight: 500;
      }

      &-icon {
        color: #ff4d4f;
      }
    }
  }
</style>
