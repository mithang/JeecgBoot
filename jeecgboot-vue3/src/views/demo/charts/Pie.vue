<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, Ref, onMounted } from 'vue';

  import { useECharts } from '/@/hooks/web/useECharts';

  export default defineComponent({
    props: {
      width: {
        type: String as PropType<string>,
        default: '100%',
      },
      height: {
        type: String as PropType<string>,
        default: 'calc(100vh - 78px)',
      },
    },
    setup() {
      const chartRef = ref<HTMLDivElement | null>(null);
      const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
      const dataAll = [389, 259, 262, 324, 232, 176, 196, 214, 133, 370];
      const yAxisData = ['Nguyên nhân1', 'Nguyên nhân2', 'Nguyên nhân3', 'Nguyên nhân4', 'Nguyên nhân5', 'Nguyên nhân6', 'Nguyên nhân7', 'Nguyên nhân8', 'Nguyên nhân9', 'Nguyên nhân10'];
      onMounted(() => {
        setOptions({
          backgroundColor: '#0f375f',
          title: [
            {
              text: 'Tỷ lệ khiếu nại theo từng kênh',
              left: '2%',
              top: '1%',
              textStyle: {
                color: '#fff',
                fontSize: 14,
              },
            },
            {
              text: 'Lý do khiếu nạiTOP10',
              left: '40%',
              top: '1%',
              textStyle: {
                color: '#fff',
                fontSize: 14,
              },
            },
            {
              text: 'Tỷ lệ khiếu nại theo từng cấp độ',
              left: '2%',
              top: '50%',
              textStyle: {
                color: '#fff',
                fontSize: 14,
              },
            },
          ],
          grid: [{ left: '50%', top: '7%', width: '45%', height: '90%' }],
          tooltip: {
            formatter: '{b} ({c})',
          },
          xAxis: [
            {
              gridIndex: 0,
              axisTick: { show: false },
              axisLabel: { show: false },
              splitLine: { show: false },
              axisLine: { show: false },
            },
          ],
          yAxis: [
            {
              gridIndex: 0,
              interval: 0,
              data: yAxisData.reverse(),
              axisTick: { show: false },
              axisLabel: { show: true },
              splitLine: { show: false },
              axisLine: { show: true, lineStyle: { color: '#6173a3' } },
            },
          ],
          series: [
            {
              name: 'Tỷ lệ khiếu nại theo từng kênh',
              type: 'pie',
              radius: '30%',
              center: ['22%', '25%'],
              data: [
                { value: 335, name: 'Điện thoại chăm sóc khách hàng' },
                { value: 310, name: 'Trang web chính thức của Audi' },
                { value: 234, name: 'Phơi bày trên truyền thông' },
                { value: 135, name: 'Tổng cục Kiểm tra chất lượng' },
                { value: 105, name: 'Khác' },
              ],
              labelLine: { show: false },
              label: {
                show: true,
                formatter: '{b} \n ({d}%)',
                color: '#B1B9D3',
              },
            },
            {
              name: 'Tỷ lệ khiếu nại theo từng cấp độ',
              type: 'pie',
              radius: '30%',
              center: ['22%', '75%'],
              labelLine: { show: false },
              data: [
                { value: 335, name: 'Acấp' },
                { value: 310, name: 'Bcấp' },
                { value: 234, name: 'Ccấp' },
                { value: 135, name: 'Dcấp' },
              ],
              label: {
                show: true,
                formatter: '{b} \n ({d}%)',
                color: '#B1B9D3',
              },
            },
            {
              name: 'Lý do khiếu nạiTOP10',
              type: 'bar',
              xAxisIndex: 0,
              yAxisIndex: 0,
              barWidth: '45%',
              itemStyle: { color: '#86c9f4' },
              label: { show: true, position: 'right', color: '#9EA7C4' },
              data: dataAll.sort(),
            },
          ],
        });
      });
      return { chartRef };
    },
  });
</script>
