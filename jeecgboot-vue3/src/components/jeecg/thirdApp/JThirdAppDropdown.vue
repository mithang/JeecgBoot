<template>
  <a-dropdown v-if="syncToApp && syncToLocal">
    <a-button type="primary" preIcon="ant-design:sync-outlined">Đồng bộ{{ name }}</a-button>
    <template #overlay>
      <a-menu @click="handleMenuClick">
        <a-menu-item v-if="syncToApp" key="to-app">Đồng bộ đến{{ name }}</a-menu-item>
        <a-menu-item v-if="getSyncToLocal" key="to-local">Đồng bộ về máy local</a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
  <a-button v-else-if="syncToApp" type="primary" preIcon="ant-design:sync-outlined" @click="handleMenuClick({ key: 'to-app' })"
    >Đồng bộ{{ name }}</a-button
  >
  <a-button v-else type="primary" preIcon="ant-design:sync-outlined" @click="handleMenuClick({ key: 'to-local' })">Đồng bộ{{ name }}về máy local</a-button>
</template>

<script lang="ts" setup>
  /* JThirdAppButton là component con, không thể sử dụng độc lập */
  import { computed } from 'vue';

  const props = defineProps({
    type: String,
    name: String,
    syncToApp: Boolean,
    syncToLocal: Boolean,
  });
  // Khai báoEmits
  const emit = defineEmits(['to-app', 'to-local']);

  const getSyncToLocal = computed(() => {
    // Do API WeCom thay đổi, sẽ không còn hỗ trợ đồng bộ về local nữa
    if (props.type === 'wechatEnterprise') {
      return false;
    }
    return props.syncToLocal;
  });

  function handleMenuClick(event) {
    emit(event.key, { type: props.type });
  }
</script>

<style scoped></style>
