<template>
  <PageWrapper title="Ví dụ sử dụng component drawer">
    <Alert message="Sử dụng useDrawer để thao tác ngăn kéo" show-icon />
    <a-button type="primary" class="my-4" @click="openDrawerLoading"> MởDrawer </a-button>

    <Alert message="Điều khiển hiển thị/ẩn đồng thời cả trong và ngoài" show-icon />
    <a-button type="primary" class="my-4" @click="openDrawer2(true)"> MởDrawer </a-button>
    <Alert message="Chiều cao tự thích ứng/Hiển thịfooter" show-icon />
    <a-button type="primary" class="my-4" @click="openDrawer3(true)"> MởDrawer </a-button>

    <Alert message="Tương tác dữ liệu trong và ngoài" show-icon />
    <a-button type="primary" class="my-4" @click="send"> Mở Drawer và truyền dữ liệu </a-button>
    <Alert message="Chế độ trang chi tiết" show-icon />
    <a-button type="primary" class="my-4" @click="openDrawer5(true)"> Mở chi tiếtDrawer </a-button>
    <Drawer1 @register="register1" />
    <Drawer2 @register="register2" />
    <Drawer3 @register="register3" />
    <Drawer4 @register="register4" />
    <Drawer5 @register="register5" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Alert } from 'ant-design-vue';
  import { useDrawer } from '/@/components/Drawer';
  import Drawer1 from './Drawer1.vue';
  import Drawer2 from './Drawer2.vue';
  import Drawer3 from './Drawer3.vue';
  import Drawer4 from './Drawer4.vue';
  import Drawer5 from './Drawer5.vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { Alert, PageWrapper, Drawer1, Drawer2, Drawer3, Drawer4, Drawer5 },
    setup() {
      const [register1, { openDrawer: openDrawer1, setDrawerProps }] = useDrawer();
      const [register2, { openDrawer: openDrawer2 }] = useDrawer();
      const [register3, { openDrawer: openDrawer3 }] = useDrawer();
      const [register4, { openDrawer: openDrawer4 }] = useDrawer();
      const [register5, { openDrawer: openDrawer5 }] = useDrawer();
      function send() {
        openDrawer4(true, {
          data: 'content',
          info: 'Info',
        });
      }
      function openDrawerLoading() {
        openDrawer1();
        setDrawerProps({ loading: true });
        setTimeout(() => {
          setDrawerProps({ loading: false });
        }, 2000);
      }
      return {
        register1,
        openDrawer1,
        register2,
        openDrawer2,
        register3,
        openDrawer3,
        register4,
        register5,
        openDrawer5,
        send,
        openDrawerLoading,
      };
    },
  });
</script>
