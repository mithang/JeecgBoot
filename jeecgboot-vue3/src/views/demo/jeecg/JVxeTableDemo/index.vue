<template>
  <PageWrapper>
    <a-card :bordered="false">
      <template #title>
        <span> 
          JVXETableLà component bảng hiệu năng cao được phát triển chuyên cho dữ liệu lớn và các thao tác phức tạp theo phong cách ERP, tầng đáy dùngvxe-tablecomponent, có thể khắc phục hoàn toàn vấn đề hiệu năng chưa đủ của bảng mặc định antd. 
          <a href="https://help.jeecg.com/component/JVxeTable.html">APITài liệu</a>
        </span>
      </template>
    
      <a-tabs defaultActiveKey="1">
        <a-tab-pane tab="Ví dụ cơ bản" key="1">
          <JVxeDemo1 />
        </a-tab-pane>
        <a-tab-pane tab="Ví dụ nâng cao" key="2">
          <JVxeDemo2 />
        </a-tab-pane>
        <a-tab-pane tab="Ví dụ sắp xếp" key="3">
          <JVxeDemo3 />
        </a-tab-pane>
        <a-tab-pane tab="Ví dụ liên động" key="4">
          <JVxeDemo4 />
        </a-tab-pane>
        <a-tab-pane tab="Thao tác bàn phím" key="5">
          <JVxeDemo5 />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageWrapper>
</template>

<script lang="ts" setup>
  // noinspection ES6UnusedImports
  import { PageWrapper } from '/@/components/Page';
  import JVxeDemo1 from './JVxeDemo1.vue';
  import JVxeDemo2 from './JVxeDemo2.vue';
  import JVxeDemo3 from './JVxeDemo3.vue';
  import JVxeDemo4 from './JVxeDemo4.vue';
  import JVxeDemo5 from './JVxeDemo5.vue';
</script>
