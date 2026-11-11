<template>
  <a-result
      status="error"
      :title="title"
      :subTitle="subTitle"
  >
    <template #extra>
      <a-button type="primary" @click="onGoToHome">Quay lại trang chủ</a-button>
      <a-button @click="onReload">Làm mới trang</a-button>
      <a-button @click="onClose">Đóng</a-button>
    </template>
  </a-result>
</template>

<script setup lang="ts">
import {router} from "@/router";
import {PageEnum} from "@/enums/pageEnum";

defineProps({
  title: {
    type: String,
    default: 'Ối, trang đã xảy ra lỗi rồi'
  },
  subTitle: String,
})

function onGoToHome() {
  router.push(PageEnum.BASE_HOME);
}

function onReload() {
  location.reload()
}

function onClose() {
  window.close()
  setTimeout(() => alert('Trang hiện tại không thể đóng bằng nút, vui lòng đóng thủ công'), 1000)
}

</script>

<style scoped lang="less">

</style>