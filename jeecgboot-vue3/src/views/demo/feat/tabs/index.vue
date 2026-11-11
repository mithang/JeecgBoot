<template>
  <PageWrapper title="Ví dụ thao tác tab">
    <CollapseContainer title="Nhập văn bản vào ô nhập bên dưới,Sau khi chuyển đi rồi quay lại, nội dung vẫn được lưu">
      <a-alert banner message="Thao tác này không ảnh hưởng đến tiêu đề trang, chỉ sửa tiêu đề Tab" />
      <div class="mt-2 flex flex-grow-0">
        <a-button class="mr-2" @click="setTabTitle" type="primary"> Đặt tiêu đề Tab </a-button>
        <a-input placeholder="Vui lòng nhập" v-model:value="title" class="mr-4 w-12" />
      </div>
    </CollapseContainer>

    <CollapseContainer class="mt-4" title="Thao tác tab">
      <a-button class="mr-2" @click="() => closeAll()"> Đóng tất cả </a-button>
      <a-button class="mr-2" @click="() => closeLeft()"> Đóng phía bên trái </a-button>
      <a-button class="mr-2" @click="() => closeRight()"> Đóng phía bên phải </a-button>
      <a-button class="mr-2" @click="() => closeOther()"> Đóng các tab khác </a-button>
      <a-button class="mr-2" @click="closeCurrent"> Đóng tab hiện tại </a-button>
      <a-button class="mr-2" @click="refreshPage"> Làm mới trang hiện tại </a-button>
    </CollapseContainer>

    <CollapseContainer class="mt-4" title="Tab tái sử dụng vượt giới hạn sẽ tự động đóng(Kịch bản sử dụng: Route động)">
      <a-button v-for="index in 6" :key="index" class="mr-2" @click="toDetail(index)"> Mở{{ index }}Trang chi tiết </a-button>
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { useTabs } from '/@/hooks/web/useTabs';
  import { PageWrapper } from '/@/components/Page';
  import { Input, Alert } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'TabsDemo',
    components: { CollapseContainer, PageWrapper, [Input.name]: Input, [Alert.name]: Alert },
    setup() {
      const go = useGo();
      const title = ref<string>('');
      const { closeAll, closeLeft, closeRight, closeOther, closeCurrent, refreshPage, setTitle } = useTabs();
      const { createMessage } = useMessage();
      function setTabTitle() {
        if (title.value) {
          setTitle(title.value);
        } else {
          createMessage.error('Vui lòng nhập tiêu đề Tab cần đặt!');
        }
      }

      function toDetail(index: number) {
        go(`/comp/basic/tabs/detail/${index}`);
      }
      return {
        closeAll,
        closeLeft,
        closeRight,
        closeOther,
        closeCurrent,
        toDetail,
        refreshPage,
        setTabTitle,
        title,
      };
    },
  });
</script>
