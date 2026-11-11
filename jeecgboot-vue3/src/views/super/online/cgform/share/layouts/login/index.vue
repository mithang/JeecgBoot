<template>
  <div>Đây là trang đăng nhập của bạn</div>
</template>

<script setup lang="ts">

import {useUserStore} from "@/store/modules/user";

const userStore = useUserStore();

// Lấy của route r

if (userStore.getToken) {

}

</script>

<style scoped lang="less">

</style>