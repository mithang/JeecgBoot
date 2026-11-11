<template>
  <a-card class="ai-prompts-info">
    <AiragExtDataList ref="processRef" v-if="activeKey==='evaluator'"></AiragExtDataList>
  </a-card>
</template>

<script lang="ts" setup>
/**
 * Bộ đánh giá evaluator
 * Tập đánh giá evaluationSet
 * Thử nghiệm experiment
 */
import { ref, onMounted } from 'vue';
import AiragExtDataList from './AiragExtDataList.vue'

/**
 * Bộ đánh giá evaluator
 * Tập đánh giá evaluationSet
 * Thử nghiệm experiment
 */
const activeKey = ref('evaluator');
/**
 * Chuyển đổi tab
 */
function handleChangePanel(key) {
  activeKey.value = key;
}

onMounted(()=>{
  activeKey.value = 'evaluator';
});
</script>

<style scoped lang="less">
.ai-prompts-info {
  margin: 10px;
  :deep(.ant-card-body) {
    padding: 12px 8px;
  }
  :deep(.ant-tabs-tabpane) {
    padding-left: 12px !important;
  }
}
</style>
