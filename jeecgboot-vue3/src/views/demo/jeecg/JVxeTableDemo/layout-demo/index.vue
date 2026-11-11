<template>
  <a-card :bordered="false">
    <a-tabs>
      <a-tab-pane tab="ERPMẫu bố cục" key="erp">
        <erp-template />
      </a-tab-pane>
      <a-tab-pane tab="Mẫu bố cục1" key="1">
        <template1 />
      </a-tab-pane>
      <a-tab-pane tab="Mẫu bố cục2" key="2">
        <template2 />
      </a-tab-pane>
      <a-tab-pane tab="Mẫu bố cục3" key="3">
        <template3 />
      </a-tab-pane>
      <a-tab-pane tab="Mẫu bố cục4" key="4">
        <template4 />
      </a-tab-pane>
      <a-tab-pane tab="Mẫu bố cục5" key="5">
        <template5 />
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script lang="ts" setup>
  import Template1 from './Template1.vue';
  import Template2 from './Template2.vue';
  import Template3 from './Template3.vue';
  import Template4 from './Template4.vue';
  import Template5 from './Template5.vue';
  import ErpTemplate from './ErpTemplate.vue';
</script>

<style scoped></style>
