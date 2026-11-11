<template>
  <div ref="chartRef" :style="{ height, width }"></div>
</template>
<script lang="ts">
  import { defineComponent, PropType, ref, Ref, reactive, watchEffect } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { cloneDeep } from 'lodash-es';
  export default defineComponent({
    name: 'BarMulti',
    props: {
      chartData: {
        type: Array,
        default: () => [],
        required: true,
      },
      option: {
        type: Object,
        default: () => ({}),
      },
      type: {
        type: String as PropType<string>,
        default: 'bar',
      },
      width: {
        type: String as PropType<string>,
        default: '100%',
      },
      height: {
        type: String as PropType<string>,
        default: 'calc(100vh - 78px)',
      },
      // Giải thích logic mã: 【QQYUN-8762] Mặc định trang chủ và điều chỉnh màu echars
      seriesColor: {
        type: Array,
        default: () => [],
      },
    },
    emits: ['click'],
    setup(props, { emit }) {
      const chartRef = ref<HTMLDivElement | null>(null);
      const { setOptions, getInstance } = useECharts(chartRef as Ref<HTMLDivElement>);
      const option = reactive({
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
            label: {
              show: true,
              backgroundColor: '#333',
            },
          },
        },
        legend: {
          top: 30,
        },
        grid: {
          top: 60,
        },
        xAxis: {
          type: 'category',
          data: [],
        },
        yAxis: {
          type: 'value',
        },
        series: [],
      });

      watchEffect(() => {
        props.chartData && initCharts();
      });

      function initCharts() {
        if (props.option) {
          Object.assign(option, cloneDeep(props.option));
        }
        //Loại chú giải
        let typeArr = Array.from(new Set(props.chartData.map((item) => item.type)));
        //Dữ liệu trục
        let xAxisData = Array.from(new Set(props.chartData.map((item) => item.name)));
        let seriesData = [];
        typeArr.forEach((type) => {
          let obj: any = { name: type, type: props.type };
          // Giải thích logic mã: [issues/613] LineMulti Khi dữ liệu không thẳng hàng, tính toán trục hoành bị sai
          let data = [];
          xAxisData.forEach((x) => {
            let dataArr = props.chartData.filter((item) => type === item.type && item.name == x);
            if (dataArr && dataArr.length > 0) {
              data.push(dataArr[0].value);
            } else {
              data.push(null);
            }
          });
          //dataDữ liệu
          obj['data'] = data;
          // Giải thích logic mã: 【QQYUN-8762] Mặc định trang chủ và điều chỉnh màu echars
          if (props.seriesColor?.length) {
            const findItem = props.seriesColor.find((item: any) => item.type === type);
            if (findItem?.color) {
              obj['color'] = findItem.color;
            }
          }
          seriesData.push(obj);
        });
        option.series = seriesData;
        option.xAxis.data = xAxisData;
        setOptions(option);
        getInstance()?.off('click', onClick);
        getInstance()?.on('click', onClick);
      }

      function onClick(params) {
        emit('click', params);
      }

      return { chartRef };
    },
  });
</script>
