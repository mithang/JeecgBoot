<template>
  <PageWrapper title="Ví dụ về đăng nhập hết hạn" content="Ví dụ hết hạn đăng nhập của người dùng, không chuyển hướng đến trang đăng nhập nữa, trực tiếp tạo trang phủ lên trang hiện tại, tiện giữ trạng thái người dùng trước khi hết hạn!">
    <a-card title="Vui lòng nhấp vào nút bên dưới để truy cập API kiểm tra" extra="API được truy cập sẽ trả về phản hồi Token hết hạn">
      <a-card-grid style="width: 50%; text-align: center">
        <a-button type="primary" @click="test1">HttpStatus == 401</a-button>
      </a-card-grid>
      <a-card-grid style="width: 50%; text-align: center">
        <span></span>
        <a-button class="ml-4" type="primary" @click="test2">Response.code == 401</a-button>
      </a-card-grid>
    </a-card>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { PageWrapper } from '/@/components/Page';
  import { useUserStore } from '/@/store/modules/user';

  import { sessionTimeoutApi, tokenExpiredApi } from '/@/api/demo/account';
  import { Card } from 'ant-design-vue';

  export default defineComponent({
    name: 'TestSessionTimeout',
    components: { ACardGrid: Card.Grid, ACard: Card, PageWrapper },
    setup() {
      const userStore = useUserStore();
      async function test1() {
        // Môi trường production của website ví dụ dùng dữ liệu mock, không thể trả về mã trạng thái Http,
        // Vì vậy trong môi trường production trực tiếp thay đổi trạng thái để đạt hiệu quả kiểm thử
        if (import.meta.env.PROD) {
          userStore.setToken(undefined);
          userStore.setSessionTimeout(true);
        } else {
          // API này sẽ trả về phản hồi có mã trạng thái 401
          await sessionTimeoutApi();
        }
      }

      async function test2() {
        // API này sẽ trả về dữ liệu json có code là 401, mã trạng thái Http là200
        try {
          await tokenExpiredApi();
        } catch (err) {
          console.log('Lỗi truy cập API:', (err as Error).message || 'Lỗi');
        }
      }

      return { test1, test2 };
    },
  });
</script>
