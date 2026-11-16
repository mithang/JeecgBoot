<template>
  <div class="wrap">
    <div class="content">
      <AiChat></AiChat>
    </div>
  </div>
</template>

<script setup>
  import AiChat from '/@/views/super/airag/aiapp/chat/AiChat.vue';
  
</script>

<style lang="less" scoped>
  .wrap {
    height: 100%;
    width: 100%;
    .content {
      background: #fff;
      width: 100%;
      height: 100%;
    }
  }

  // Phase 4 Bước 11 (G12): dark mode — nền khung chat không còn trắng chói.
  html[data-theme='dark'] {
    .wrap .content {
      background: #141414;
    }
  }
</style>
