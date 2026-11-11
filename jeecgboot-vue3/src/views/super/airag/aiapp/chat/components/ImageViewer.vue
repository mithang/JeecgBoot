<!--Đóng gói phóng to image-->
<template>
  <div class="amplify-image">
    <div class="img-preview-content" @click="hideImageClick" @mousewheel="handlePicMousewheel">
      <img :src="imageUrl" ref="imageRef" />
    </div>
  </div>
</template>

<script setup lang="ts">
  //Địa chỉ hình ảnh
  import {onMounted, ref, unref} from 'vue';
  const props = defineProps(['imageUrl']);
  const emit = defineEmits(['register', 'hide']);
  //ref của hình ảnh
  const imageRef = ref();
  //Mức thu phóng
  const scale = ref<number>(1);

  /**
   * Ẩn hình ảnh
   */
  function hideImageClick() {
    scale.value = 1;
    emit('hide')
  }

  /**
   * Cuộn bằng con lăn chuột
   * @param event
   */
  function handlePicMousewheel(event) {
    event.preventDefault();
    // Phán đoán là phóng to hay thu nhỏ
    const delta = event.deltaY > 0 ? -1 : 1;
    const scaleStep = 0.1;
    // Cập nhật mức thu phóng
    scale.value = scale.value + delta * scaleStep
    imageRef.value.style.transform = `scale(${unref(scale)})`;
  }
</script>

<style scoped lang="less">
.amplify-image{
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.5);
  .img-preview-content{
    display: flex;
    width: 100%;
    height: 100%;
    color: #fff;
    justify-content: center;
    align-items: center;
    touch-action: none;
    -webkit-user-drag: none;
    img{
      transition: transform 0.3s;
      background-position: center center;
      background-repeat: no-repeat;
      -webkit-background-size: cover;
      -moz-background-size: cover;
      background-size: cover;
      max-height: 100%;
    }
  }
}
</style>
