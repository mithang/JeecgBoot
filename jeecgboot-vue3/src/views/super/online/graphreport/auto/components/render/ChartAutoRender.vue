<template>
  <ChartTabsRender v-if="isTabs" v-bind="templateProps" />
  <ChartSingleRender v-else-if="isSingle" v-bind="templateProps" />
  <ChartDoubleRender v-else-if="isDouble" v-bind="templateProps" />
  <!--  <ChartCombinationRender v-else-if="isCombination" v-bind="templateProps"/>-->
</template>

<script lang="ts">
  import { ref, watch, computed, inject, defineComponent } from 'vue';
  import ErrorTip from '../ErrorTip.vue';
  // tabs Mẫu bố cục
  import ChartTabsRender from './ChartTabsRender.vue';
  // Mẫu bố cục một hàng
  import ChartSingleRender from './ChartSingleRender.vue';
  // Mẫu bố cục hai hàng
  import ChartDoubleRender from './ChartDoubleRender.vue';
  // Bố cục kết hợp
  // import ChartCombinationRender from './ChartCombinationRender.vue'

  export default defineComponent({
    name: 'ChartAutoRender',
    components: { ErrorTip, ChartTabsRender, ChartSingleRender, ChartDoubleRender },
    props: {
      // Dữ liệu biểu đồ
      chartsData: { type: Object, default: null },
      // Có phải chế độ kết hợp hay không
      isCombination: { type: Boolean, default: false },
    },
    setup(props) {
      const setErrorTip = inject('setErrorTip') as Fn;
      // Tiêu đề biểu đồ
      const chartTitle = ref(null);
      // Mẫu hiển thị
      const displayTemplate = ref('tab');
      // Nguồn dữ liệu
      const dataSource = ref<Recordable>({});
      const isTabs = computed(() => displayTemplate.value === 'tab');
      const isSingle = computed(() => displayTemplate.value === 'single');
      const isDouble = computed(() => displayTemplate.value === 'double');
      // const isCombination = computed(() => displayTemplate.value === 'combination')
      const templateProps = computed(() => {
        return {
          // Không hiển thị ở chế độ component title
          title: props.isCombination ? null : chartTitle.value,
          chartsData: props.chartsData,
          isCombination: props.isCombination,
          onError: (tip) => setErrorTip(tip),
        };
      });

      watch(
        () => props.chartsData,
        () => parseChartsData(props.chartsData),
        { immediate: true }
      );

      // Phân tích dữ liệu biểu đồ
      function parseChartsData(data) {
        if (data) {
          chartTitle.value = data.head.name;
          displayTemplate.value = data.head.displayTemplate;
          dataSource.value = data;
          if (!isTabs.value && !isDouble.value && !isSingle.value) {
            setErrorTip('Chế độ bố cục không nhận diện được');
          }
        }
      }

      return { isTabs, isSingle, isDouble, templateProps };
    },
  });
</script>

<style scoped></style>
