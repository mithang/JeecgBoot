<template>
  <div class="step3">
    <a-result status="success" title="Thao tác thành công" sub-title="Dự kiến nhận được trong hai giờ">
      <template #extra>
        <a-button type="primary" @click="redo"> Chuyển thêm một khoản </a-button>
        <a-button> Xem hóa đơn </a-button>
      </template>
    </a-result>
    <div class="desc-wrap">
      <a-descriptions :column="1" class="mt-5">
        <a-descriptions-item label="Tài khoản thanh toán"> ant-design@alipay.com </a-descriptions-item>
        <a-descriptions-item label="Tài khoản nhận tiền"> test@example.com </a-descriptions-item>
        <a-descriptions-item label="Tên người nhận tiền"> Jeecg </a-descriptions-item>
        <a-descriptions-item label="Số tiền chuyển khoản"> 500đồng </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Result, Descriptions } from 'ant-design-vue';
  export default defineComponent({
    components: {
      [Result.name]: Result,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
    },
    emits: ['redo'],
    setup(_, { emit }) {
      return {
        redo: () => {
          emit('redo');
        },
      };
    },
  });
</script>
<style lang="less" scoped>
  .step3 {
    width: 600px;
    margin: 0 auto;
  }

  .desc-wrap {
    padding: 24px 40px;
    margin-top: 24px;
    background-color: @background-color-light;
  }
</style>
