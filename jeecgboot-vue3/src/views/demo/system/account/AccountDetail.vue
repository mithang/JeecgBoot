<template>
  <PageWrapper
    :title="`Người dùng` + userId + `tài liệu của`"
    content="Đây là trang chi tiết thông tin người dùng. Trang này chỉ dùng để minh họa cùng một route mở nhiều trang trong tab và hiển thị dữ liệu khác nhau"
    contentBackground
    @back="goBack"
  >
    <template #extra>
      <a-button type="primary" danger> Vô hiệu hóa tài khoản </a-button>
      <a-button type="primary"> Đổi mật khẩu </a-button>
    </template>
    <template #footer>
      <a-tabs default-active-key="detail" v-model:activeKey="currentKey">
        <a-tab-pane key="detail" tab="Thông tin người dùng" />
        <a-tab-pane key="logs" tab="Nhật ký thao tác" />
      </a-tabs>
    </template>
    <div class="pt-4 m-4 desc-wrap">
      <template v-if="currentKey == 'detail'">
        <div v-for="i in 10" :key="i">Đây là người dùng{{ userId }}Tài liệuTab</div>
      </template>
      <template v-if="currentKey == 'logs'">
        <div v-for="i in 10" :key="i">Đây là người dùng{{ userId }}Nhật ký thao tácTab</div>
      </template>
    </div>
  </PageWrapper>
</template>

<script>
  import { defineComponent, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';
  import { useGo } from '/@/hooks/web/usePage';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { Tabs } from 'ant-design-vue';
  export default defineComponent({
    name: 'AccountDetail',
    components: { PageWrapper, ATabs: Tabs, ATabPane: Tabs.TabPane },
    setup() {
      const route = useRoute();
      const go = useGo();
      // Tại đây có thể lấy được người dùngID
      const userId = ref(route.params?.id);
      const currentKey = ref('detail');
      const { setTitle } = useTabs();
      // TODO
      // Mã của trang này chỉ để demo, thực tế nên lấy thông tin liên quan của người dùng từ API thông qua userId

      // Đặt tiêu đề của Tab (sẽ không ảnh hưởng đến tiêu đề trang)
      setTitle('Chi tiết: Người dùng' + userId.value);

      // Thao tác khi nhấp vào liên kết quay lại ở phía trái trang
      function goBack() {
        // Hiệu quả của ví dụ này là khi nhấp quay lại sẽ luôn chuyển đến trang danh sách tài khoản, trong ứng dụng thực tế có thể quay lại trang trước
        go('/system/account');
      }
      return { userId, currentKey, goBack };
    },
  });
</script>

<style></style>
