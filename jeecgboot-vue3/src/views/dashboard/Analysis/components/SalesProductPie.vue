<template>
  <Card title="Tỷ lệ giao dịch thành công" :loading="loading">
    <div ref="chartRef" :style="{ width, height }"></div>
  </Card>
</template>
<script lang="ts" setup>
  import { Ref, ref, watch } from 'vue';
  import { Card } from 'ant-design-vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { BRAND_CHART_COLORS } from '../../dashboardHelpers';

  const props = defineProps({
    loading: Boolean,
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '300px',
    },
  });

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
  watch(
    () => props.loading,
    () => {
      if (props.loading) {
        return;
      }
      setOptions({
        tooltip: {
          trigger: 'item',
        },

        series: [
          {
            name: 'Nguồn truy cập',
            type: 'pie',
            radius: '80%',
            center: ['50%', '50%'],
            // Phase 4 Step 7 (G5): màu biểu đồ theo thương hiệu "Xanh Jeecg".
            color: BRAND_CHART_COLORS.slice(0, 4),
            data: [
              { value: 500, name: 'Sản phẩm điện tử' },
              { value: 310, name: 'Quần áo' },
              { value: 274, name: 'Mỹ phẩm' },
              { value: 400, name: 'Đồ gia dụng' },
            ].sort(function (a, b) {
              return a.value - b.value;
            }),
            roseType: 'radius',
            animationType: 'scale',
            animationEasing: 'exponentialInOut',
            animationDelay: function () {
              return Math.random() * 400;
            },
          },
        ],
      });
    },
    { immediate: true }
  );
</script>
