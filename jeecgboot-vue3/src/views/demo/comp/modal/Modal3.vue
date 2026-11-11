<template>
  <BasicModal v-bind="$attrs" title="Modal Title" :helpMessage="['Nhắc nhở1', 'Nhắc nhở2']" width="700px">
    <p class="h-20" v-for="index in 20" :key="index"> Tự thích ứng theo chiều cao màn hình </p>
  </BasicModal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  export default defineComponent({
    components: { BasicModal },
    setup() {
      return {};
    },
  });
</script>
