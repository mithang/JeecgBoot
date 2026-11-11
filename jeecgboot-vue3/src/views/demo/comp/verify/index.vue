<template>
  <PageWrapper title="Ví dụ kiểm chứng kéo">
    <div class="flex justify-center p-4 items-center bg-gray-700">
      <BasicDragVerify ref="el1" @success="handleSuccess" />
      <a-button type="primary" class="ml-2" @click="handleBtnClick(el1)"> Khôi phục </a-button>
    </div>

    <div class="flex justify-center p-4 items-center bg-gray-700">
      <BasicDragVerify ref="el2" @success="handleSuccess" circle />
      <a-button type="primary" class="ml-2" @click="handleBtnClick(el2)"> Khôi phục </a-button>
    </div>

    <div class="flex justify-center p-4 items-center bg-gray-700">
      <BasicDragVerify
        ref="el3"
        @success="handleSuccess"
        text="Kéo để kiểm chứng"
        successText="Kiểm chứng thành công"
        :barStyle="{
          backgroundColor: '#018ffb',
        }"
      />
      <a-button type="primary" class="ml-2" @click="handleBtnClick(el3)"> Khôi phục </a-button>
    </div>

    <div class="flex justify-center p-4 items-center bg-gray-700">
      <BasicDragVerify ref="el4" @success="handleSuccess">
        <template #actionIcon="isPassing">
          <BugOutlined v-if="isPassing" />
          <RightOutlined v-else />
        </template>
      </BasicDragVerify>
      <a-button type="primary" class="ml-2" @click="handleBtnClick(el4)"> Khôi phục </a-button>
    </div>

    <div class="flex justify-center p-4 items-center bg-gray-700">
      <BasicDragVerify ref="el5" @success="handleSuccess">
        <template #text="isPassing">
          <div v-if="isPassing">
            <BugOutlined />
            Thành công
          </div>
          <div v-else>
            Kéo
            <RightOutlined />
          </div>
        </template>
      </BasicDragVerify>
      <a-button type="primary" class="ml-2" @click="handleBtnClick(el5)"> Khôi phục </a-button>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicDragVerify, DragVerifyActionType, PassingData } from '/@/components/Verify/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BugOutlined, RightOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { BasicDragVerify, BugOutlined, RightOutlined, PageWrapper },
    setup() {
      const { createMessage } = useMessage();
      const el1 = ref<Nullable<DragVerifyActionType>>(null);
      const el2 = ref<Nullable<DragVerifyActionType>>(null);
      const el3 = ref<Nullable<DragVerifyActionType>>(null);
      const el4 = ref<Nullable<DragVerifyActionType>>(null);
      const el5 = ref<Nullable<DragVerifyActionType>>(null);

      function handleSuccess(data: PassingData) {
        const { time } = data;
        createMessage.success(`Kiểm chứng thành công,Thời gian tốn${time}giây`);
      }

      function handleBtnClick(elRef: Nullable<DragVerifyActionType>) {
        if (!elRef) {
          return;
        }
        elRef.resume();
      }
      return {
        handleSuccess,
        el1,
        el2,
        el3,
        el4,
        el5,
        handleBtnClick,
      };
    },
  });
</script>
<style lang="less" scoped>
  .bg-gray-700 {
    background-color: #4a5568;
  }
</style>
