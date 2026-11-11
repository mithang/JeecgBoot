<template>
  <PageWrapper title="Menu có tham số (route)" content="Hỗ trợ tham số nhiều cấp">
    Tham số hiện tại:{{ params }}
    <br />
    Nhập tham số để chuyển route:
    <Input v-model:value="value" placeholder="Nên dùng ký tự chuẩn của url, sau khi nhập bấm để chuyển đổi" />
    <a-button type="primary" @click="handleClickGo">Chuyển đổi route</a-button>
    <br />
    Sau khi chuyển route
    <ul>
      <li>Có thể làm mới trang để kiểm tra tình trạng tham số route có bình thường không.</li>
      <li>Có thể mở rộng menu con trong menu bên trái, nhấp để kiểm tra tham số có được mang theo bình thường không.</li>
    </ul>
  </PageWrapper>
</template>
<script lang="ts">
  import { Input } from 'ant-design-vue';
  import { computed, defineComponent, ref, unref } from 'vue';
  import { useRouter } from 'vue-router';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    name: 'TestMenu',
    components: { PageWrapper, Input },
    setup() {
      const { currentRoute, replace } = useRouter();
      const value = ref<string>('');

      const handleClickGo = () => {
        const { name } = unref(currentRoute);
        replace({ name: name!, params: { id: unref(value) } });
      };
      return {
        value,
        handleClickGo,
        params: computed(() => {
          return unref(currentRoute).params;
        }),
      };
    },
  });
</script>
