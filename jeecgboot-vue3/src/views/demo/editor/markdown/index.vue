<template>
  <PageWrapper title="MarkDownVí dụ component">
    <div>
      <a-button @click="toggleTheme" class="mb-2" type="primary"> Chủ đề tối </a-button>
      <a-button @click="clearValue" class="mb-2" type="default"> Xóa nội dung </a-button>
      <MarkDown v-model:value="value" @change="handleChange" ref="markDownRef" placeholder="Đây là văn bản giữ chỗ" />
    </div>
    <div class="mt-2">
      <a-card title="Markdown Viewer Demo component">
        <MarkdownViewer :value="value" />
      </a-card>
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { MarkDown, MarkDownActionType, MarkdownViewer } from '/@/components/Markdown';
  import { PageWrapper } from '/@/components/Page';
  import { Card } from 'ant-design-vue';

  export default defineComponent({
    components: { MarkDown, PageWrapper, MarkdownViewer, ACard: Card },
    setup() {
      const markDownRef = ref<Nullable<MarkDownActionType>>(null);
      const valueRef = ref(`
# title

# content
`);

      function toggleTheme() {
        const markDown = unref(markDownRef);
        if (!markDown) return;
        const vditor = markDown.getVditor();
        vditor.setTheme('dark');
      }

      function handleChange(v: string) {
        valueRef.value = v;
      }

      function clearValue() {
        valueRef.value = '';
      }

      return {
        value: valueRef,
        toggleTheme,
        markDownRef,
        handleChange,
        clearValue,
      };
    },
  });
</script>
