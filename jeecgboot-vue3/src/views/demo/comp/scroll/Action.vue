<template>
  <PageWrapper title="Ví dụ hàm component cuộn" content="Dựa trênel-scrollbar">
    <div class="my-4">
      <a-button @click="scrollTo(100)" class="mr-2"> Cuộn đến vị trí 100px </a-button>
      <a-button @click="scrollTo(800)" class="mr-2"> Cuộn đến vị trí 800px </a-button>
      <a-button @click="scrollTo(0)" class="mr-2"> Cuộn lên đầu </a-button>
      <a-button @click="scrollBottom()" class="mr-2"> Cuộn xuống cuối </a-button>
    </div>
    <div class="scroll-wrap">
      <ScrollContainer class="mt-4" ref="scrollRef">
        <ul class="p-3">
          <template v-for="index in 100" :key="index">
            <li class="p-2" :style="{ border: '1px solid #eee' }">
              {{ index }}
            </li>
          </template>
        </ul>
      </ScrollContainer>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { ScrollContainer, ScrollActionType } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { ScrollContainer, PageWrapper },
    setup() {
      const scrollRef = ref<Nullable<ScrollActionType>>(null);
      const getScroll = () => {
        const scroll = unref(scrollRef);
        if (!scroll) {
          throw new Error('scroll is Null');
        }
        return scroll;
      };

      function scrollTo(top: number) {
        getScroll().scrollTo(top);
      }
      function scrollBottom() {
        getScroll().scrollBottom();
      }
      return {
        scrollTo,
        scrollRef,
        scrollBottom,
      };
    },
  });
</script>
<style lang="less" scoped>
  .scroll-wrap {
    width: 50%;
    height: 300px;
    background-color: @component-background;
  }
</style>
