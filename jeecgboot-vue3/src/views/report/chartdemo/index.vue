<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" animated @change="tabChange">
        <a-tab-pane key="1" tab="Biểu đồ cột">
          <a-row>
            <a-col :span="24">
              <Bar :chartData="barDataSource" height="50vh" :option="{ title: { text: 'Xếp hạng doanh số bán hàng', left: 'center' } }"></Bar>
            </a-col>
            <!-- <a-col :span="7" style="margin-left:50px" >
                            Mục cấu hình:
                            <textarea rows="18" style="width: 500px">{{ barDataSource }}</textarea>
                        </a-col>-->
          </a-row>
        </a-tab-pane>
        <a-tab-pane key="2" tab="Biểu đồ cột nhiều cột" force-render>
          <BarMulti :chartData="barMultiData" :option="multiBarOption" height="50vh"></BarMulti>
        </a-tab-pane>
        <a-tab-pane key="3" tab="Biểu đồ cột mini" style="display: flex; justify-content: center">
          <Bar :chartData="barDataSource" width="30%" height="50vh"></Bar>
        </a-tab-pane>
        <a-tab-pane key="4" tab="Biểu đồ diện tích">
          <SingleLine :chartData="barDataSource" height="50vh" :option="{ title: { text: 'Xếp hạng doanh số bán hàng', left: 'center' } }"></SingleLine>
        </a-tab-pane>
        <a-tab-pane key="5" tab="Biểu đồ diện tích mini" style="display: flex; justify-content: center">
          <SingleLine :chartData="barDataSource" width="30%" height="50vh"></SingleLine>
        </a-tab-pane>
        <a-tab-pane key="6" tab="Biểu đồ đường nhiều dòng">
          <LineMulti :chartData="barMultiData" height="50vh" :option="multiBarOption" type="line"></LineMulti>
        </a-tab-pane>
        <a-tab-pane key="7" tab="Biểu đồ tròn">
          <pie :chartData="pieData" height="40vh" :option="{ title: { text: 'Biểu đồ tròn cơ sở', left: 'center' } }" />
        </a-tab-pane>
        <a-tab-pane key="8" tab="Biểu đồ radar">
          <Radar :chartData="radarData" height="50vh"></Radar>
        </a-tab-pane>
        <a-tab-pane key="9" tab="Bảng điều khiển">
          <Gauge :chartData="{ name: 'Tỷ lệ chuyên cần', value: 70 }" height="50vh"></Gauge>
        </a-tab-pane>
        <a-tab-pane key="10" tab="Biểu đồ cột – đường">
          <BarAndLine :chartData="barLineData" :customColor="barLineColors" height="50vh"></BarAndLine>
        </a-tab-pane>
        <a-tab-pane key="11" tab="Danh sách xếp hạng">
          <RankList title="Bảng xếp hạng bán hàng cửa hàng" :list="rankList" style="width: 600px; margin: 0 auto"></RankList>
        </a-tab-pane>
        <a-tab-pane key="13" tab="Xu hướng">
          <trend title="Trend" term="Trend: " :percentage="30" />
        </a-tab-pane>
        <!-- Tạm thời không có -->
        <!-- <a-tab-pane key="14" tab="Thanh tiến độ">
                    <Bar :option="{xAxis:{show:false},yAxis:{show:false}}" :chartData="chartData" width="100px" height="50px"></Bar>
                </a-tab-pane>-->
        <!--<a-tab-pane key="15" tab="Biểu đồ sóng nước"></a-tab-pane>-->
      </a-tabs>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { getData } from './chartdemo.data.ts';
  import Bar from '/@/components/chart/Bar.vue';
  import BarMulti from '/@/components/chart/BarMulti.vue';
  import SingleLine from '/@/components/chart/SingleLine.vue';
  import LineMulti from '/@/components/chart/LineMulti.vue';
  import Pie from '/@/components/chart/Pie.vue';
  import Radar from '/@/components/chart/Radar.vue';
  import Gauge from '/@/components/chart/Gauge.vue';
  import RankList from '/@/components/chart/RankList.vue';
  import Trend from '/@/components/chart/Trend.vue';
  import BarAndLine from '/@/components/chart/BarAndLine.vue';

  const activeKey = ref('1');
  const { barDataSource, barMultiData, pieData, barLineData, radarData,barLineColors } = getData;
  const multiBarOption = {
    title: { text: 'Biểu đồ cột nhiều cột', left: 'center' },
  };
  const rankList = loadData('name', 'total', 2000, 100, 'Triều Dương, Bắc Kinh ', ' cửa hàng');
  // Chuyển đổi tab
  function tabChange(key) {
    console.log('Key đang chuyển:', key);
  }
  function loadData(x, y, max, min, before = '', after = 'tháng') {
    let data = [];
    for (let i = 0; i < 12; i += 1) {
      data.push({
        [x]: `${before}${i + 1}${after}`,
        [y]: Math.floor(Math.random() * max) + min,
      });
    }
    return data;
  }
</script>
