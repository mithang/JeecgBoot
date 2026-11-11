<template>
  <div :id="printId">
    <a-card v-bind="chartCardProps">
      <template #extra>
        <div :ignore-print="true">
          <a-button v-if="showPrint" type="primary" ghost @click="onPrint">In</a-button>
          <a-button v-if="showDetail" type="primary" ghost @click="onGoToDetail">Chi tiết</a-button>
        </div>
      </template>
      <div>
        <a-radio-group buttonStyle="solid" v-model:value="activeKey" :ignore-print="true">
          <!-- Biểu tượng hiển thị sắp xếp -->
          <template v-for="(type, index) of chartTypes">
            <template v-if="type === 'line'">
              <a-radio-button v-if="hasLine" value="line" :key="index">Biểu đồ đường cong</a-radio-button>
            </template>
            <template v-if="type === 'bar'">
              <a-radio-button v-if="hasBar" value="bar" :key="index">Biểu đồ cột</a-radio-button>
            </template>
            <template v-if="type === 'pie'">
              <a-radio-button v-if="hasPie" value="pie" :key="index">Biểu đồ tròn</a-radio-button>
            </template>
          </template>
        </a-radio-group>
        <a-tabs v-model:activeKey="activeKey" size="small" :tabBarStyle="{ display: 'none' }">
          <a-tab-pane v-if="hasLine" tab="Biểu đồ đường cong" key="line">
            <LineMulti v-bind="lineProps" />
          </a-tab-pane>
          <a-tab-pane v-if="hasBar" tab="Biểu đồ cột" key="bar">
            <BarMulti v-bind="barProps" />
          </a-tab-pane>
          <a-tab-pane v-if="hasPie" tab="Biểu đồ tròn" key="pie">
            <Pie v-bind="pieProps" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-card>
    <a-card v-if="hasTable" v-bind="tableCardProps">
      <a-row>
        <a-col :ignore-print="true">
          <a-button v-bind="exportButtonProps" @click="onExportXls">{{ exportButtonProps.text }}</a-button>
          <a-switch v-bind="pageSwitchProps" v-model:checked="pageSwitch" />
        </a-col>
        <a-col>
          <a-table v-bind="tableProps" />
        </a-col>
      </a-row>
    </a-card>
  </div>
</template>

<script lang="ts">
  import { ChartRenderCommon, useChartRender } from '../../hooks/useChartRender';

  export default {
    name: 'ChartTabsRender',
    ...ChartRenderCommon,
    setup: useChartRender,
  };
</script>

<style lang="less" scoped></style>
