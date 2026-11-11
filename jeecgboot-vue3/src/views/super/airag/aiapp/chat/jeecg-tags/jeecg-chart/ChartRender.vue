<template>
  <div class="ai-chat-chart">
    <div v-if="isError" class="ai-chat-chart__error">{{ errorMessage }}</div>
    <div v-else-if="!resolvedType || !hasData" class="ai-chat-chart__error">
      <div v-if="loading"
           style="color: #999; padding: 12px 8px; border: 1px dashed #eee; margin: 8px 0; border-radius: 4px;">
        <span>Đang hiển thị biểu đồ...</span>
      </div>
      <span v-else>Định dạng hiển thị biểu đồ do mô hình trả về không đúng, vui lòng tối ưu prompt hoặc thử lại.</span>
    </div>
    <div v-else class="ai-chat-chart__body">
      <Tabs v-model:activeKey="activeTab" size="small" class="ai-chat-chart__tabs">
        <!-- Tab biểu đồ chính -->
        <TabPane :key="resolvedType" :tab="getChartLabel(resolvedType)">
          <ChartBody :chartType="resolvedType" />
        </TabPane>
        <!-- Tabs biểu đồ có thể thay thế -->
        <TabPane v-for="alt in validAltTypes" :key="alt" :tab="getChartLabel(alt)">
          <ChartBody :chartType="alt" />
        </TabPane>
        <!-- Tab dữ liệu -->
        <TabPane v-if="hasSql" key="__data__" tab="Dữ liệu">
          <div class="ai-chat-chart__data-toolbar">
            <a-button size="small" :loading="exportLoading" @click="handleExport">
              <template #icon><DownloadOutlined /></template>
              Xuất
            </a-button>
          </div>
          <BasicTable
            v-bind="tableBindings"
            :loading="tableLoading"
            size="small"
            class="ai-chat-chart__table"
          />
        </TabPane>
      </Tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ChartType } from './types';
import type { BasicColumn } from '/@/components/Table';
import { computed, defineComponent, h, ref, watch, watchEffect } from 'vue';
import { Tabs, TabPane, message } from 'ant-design-vue';
import { DownloadOutlined } from '@ant-design/icons-vue';
import { BasicTable, useTable } from '/@/components/Table';
import LineMulti from '/@/components/chart/LineMulti.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';
import Pie from '/@/components/chart/Pie.vue';
import Radar from '/@/components/chart/Radar.vue';
import Gauge from '/@/components/chart/Gauge.vue';
import BarAndLine from '/@/components/chart/BarAndLine.vue';
import SingleLine from '/@/components/chart/SingleLine.vue';
import { sqlPageExecute, sqlExportXls } from './ChartRender.api';

const props = defineProps({
  /**
   * Chuỗi cấu hình biểu đồ, ví dụ:
   * {"type":"bar","altTypes":["line","pie"],"data":[...],"sql":"SELECT ...","dbSource":"","columns":{"field1":"Tiêu đề 1"}}
   */
  data: {
    type: String,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
});

/** Ánh xạ tên tiếng Trung của loại biểu đồ */
const chartTypeLabels: Record<string, string> = {
  bar: 'Biểu đồ cột',
  line: 'Biểu đồ đường',
  pie: 'Biểu đồ tròn',
  radar: 'Biểu đồ radar',
  gauge: 'Bảng điều khiển',
  barline: 'Biểu đồ cột gấp',
  multibar: 'Biểu đồ cột nhiều cột',
  multiline: 'Biểu đồ đường nhiều dòng',
  area: 'Biểu đồ diện tích',
};

/** Lấy nhãn tiếng Trung của loại biểu đồ */
function getChartLabel(type: string): string {
  return chartTypeLabels[type] || type;
}

/** Tab hiện đang kích hoạt */
const activeTab = ref<string>('');
/** Trạng thái tải của bảng */
const tableLoading = ref(false);
/** Trạng thái tải của việc xuất */
const exportLoading = ref(false);
/** Dữ liệu bảng */
const tableData = ref<Recordable[]>([]);
/** Định nghĩa cột bảng */
const tableColumns = ref<BasicColumn[]>([]);
/** Tổng số dữ liệu */
const tableTotal = ref(0);
/** Đã tải dữ liệu lần đầu hay chưa */
const dataLoaded = ref(false);

/**
 * Văn bản gợi ý khi phân tích thất bại hoặc loại sai.
 */
const errorMessage = ref<string>('');
/**
 * Có tồn tại lỗi ngăn hiển thị hay không.
 */
const isError = ref<boolean>(false);

/**
 * Phân tích chuỗi thành đối tượng cấu hình, khi thất bại ghi lại thông tin lỗi.
 */
const parsedConfig = computed<Recordable>(() => {
  try {
    errorMessage.value = '';
    isError.value = false;
    return JSON.parse(props.data || '{}');
  } catch (error) {
    errorMessage.value = 'Lỗi phân tích dữ liệu biểu đồ, không thể hiển thị biểu đồ.';
    isError.value = true;
    return {};
  }
});

/**
 * Tập hợp các loại biểu đồ được hỗ trợ.
 */
const supportedTypes: ChartType[] = ['bar', 'line', 'pie', 'radar', 'gauge', 'barline', 'multibar', 'multiline', 'area'];

/**
 * Loại biểu đồ phân tích được, thống nhất chuyển thành chữ thường rồi kiểm chứng tính hợp lệ.
 */
const resolvedType = computed<ChartType>(() => {
  const rawType = String((parsedConfig.value as any).type || '').toLowerCase();
  const normalizedType = (rawType || '').replace(/[-_\s]/g, '');
  const typeAliasMap: Record<string, ChartType> = {
    bar: 'bar',
    line: 'line',
    pie: 'pie',
    radar: 'radar',
    gauge: 'gauge',
    barline: 'barline',
    barandline: 'barline',
    linebar: 'barline',
    multiline: 'multiline',
    multibar: 'multibar',
    area: 'area',
    arealine: 'area',
  };
  const mapped = typeAliasMap[normalizedType] || '';
  if (mapped && supportedTypes.includes(mapped)) {
    return mapped as ChartType;
  }
  return '';
});

/** Phân tích danh sách loại biểu đồ có thể thay thế (lọc bỏ loại không hợp lệ và trùng với loại chính) */
const validAltTypes = computed<ChartType[]>(() => {
  const altTypes = (parsedConfig.value as any).altTypes;
  if (!Array.isArray(altTypes)) {
    return [];
  }
  const primary = resolvedType.value;
  return altTypes
    .map((t: string) => String(t).toLowerCase() as ChartType)
    .filter((t: ChartType) => t !== primary && supportedTypes.includes(t));
});

/** Khởi tạo activeTab là loại biểu đồ chính */
watchEffect(() => {
  if (resolvedType.value && !activeTab.value) {
    activeTab.value = resolvedType.value;
  }
});

/**
 * Khi loại không được hỗ trợ, đưa ra gợi ý lỗi.
 */
watchEffect(() => {
  if (isError.value) {
    return;
  }
  if (resolvedType.value === '') {
    const typeValue = (parsedConfig.value as any).type;
    if (typeValue) {
      errorMessage.value = 'Hiện tại chỉ hỗ trợ các loại biểu đồ bar, line, pie, radar, gauge, barline, multibar, multiline, area.';
      isError.value = true;
    }
  } else {
    errorMessage.value = '';
    isError.value = false;
  }
});

/**
 * Danh sách dữ liệu gốc.
 */
const rawData = computed<unknown>(() => (parsedConfig.value as any).data);

/**
 * Phán đoán có tồn tại mảng dữ liệu khả dụng hay không.
 */
const hasData = computed<boolean>(() => {
  if (resolvedType.value === 'gauge') {
    return Boolean(rawData.value);
  }
  return Array.isArray(rawData.value) && rawData.value.length > 0;
});

/** Có chứa SQL có thể truy vấn hay không */
const hasSql = computed<boolean>(() => {
  const sql = (parsedConfig.value as any).sql;
  return typeof sql === 'string' && sql.trim().length > 0;
});

/** Trích xuất SQL từ cấu hình */
const chartSql = computed<string>(() => {
  return ((parsedConfig.value as any).sql || '').trim();
});

/** Trích xuất nguồn dữ liệu từ cấu hình */
const chartDbSource = computed<string>(() => {
  return ((parsedConfig.value as any).dbSource || '').trim();
});

/** Trích xuất ánh xạ tiêu đề cột từ cấu hình */
const chartColumns = computed<Recordable>(() => {
  return (parsedConfig.value as any).columns || {};
});

/**
 * Dùng component BasicTable
 */
const [registerTable, { setPagination }] = useTable({
  columns: tableColumns,
  dataSource: tableData,
  pagination: {
    pageSize: 10,
    current: 1,
    total: 0,
  },
  showIndexColumn: false,
  canResize: false,
  bordered: true,
  onChange: handleTableChange,
});

/** Thuộc tính ràng buộc của bảng */
const tableBindings = computed(() => ({
  onRegister: registerTable,
}));

/**
 * Tải dữ liệu bảng
 */
async function loadTableData(pageNo = 1, pageSize = 10) {
  if (!chartSql.value) {
    return;
  }
  tableLoading.value = true;
  try {
    const res = await sqlPageExecute({
      sql: chartSql.value,
      dbSource: chartDbSource.value,
      pageNo,
      pageSize,
    });
    if (res.success && res.result) {
      const { records = [], total = 0 } = res.result;
      tableData.value = records;
      tableTotal.value = total;
      // Tạo cột động theo dữ liệu trả về (dùng columns ánh xạ tiêu đề tiếng Trung)
      if (records.length > 0) {
        tableColumns.value = buildColumns(records[0]);
      }
      setPagination({
        current: pageNo,
        pageSize,
        total,
      });
    } else {
      tableData.value = [];
      tableTotal.value = 0;
    }
  } catch (e) {
    console.error('Tải dữ liệu gốc của biểu đồ thất bại', e);
    tableData.value = [];
    tableTotal.value = 0;
  } finally {
    tableLoading.value = false;
  }
}

/**
 * Tạo động định nghĩa cột BasicTable theo key của hàng dữ liệu, ưu tiên dùng tiêu đề tiếng Trung được ánh xạ trong columns
 */
function buildColumns(row: Recordable): BasicColumn[] {
  const columnMap = chartColumns.value;
  return Object.keys(row).map((key) => ({
    title: columnMap[key] || key,
    dataIndex: key,
    width: 150,
    ellipsis: true,
  }));
}

/**
 * Xử lý thay đổi phân trang bảng
 */
function handleTableChange(pagination: any) {
  loadTableData(pagination.current, pagination.pageSize);
}

/**
 * Xuất toàn bộ dữ liệu
 */
async function handleExport() {
  if (!chartSql.value) {
    return;
  }
  exportLoading.value = true;
  try {
    const response = await sqlExportXls({
      sql: chartSql.value,
      dbSource: chartDbSource.value,
      columns: chartColumns.value,
    });
    if (!response || !response.data) {
      message.warning('Xuất thất bại');
      return;
    }
    const data = response.data;
    // Kiểm tra có phải phản hồi lỗi hay không (định dạng JSON)
    if (data.type && data.type.indexOf('json') !== -1) {
      const text = await data.text();
      try {
        const json = JSON.parse(text);
        if (!json.success) {
          message.warning('Xuất thất bại:' + (json.message || 'Lỗi không rõ'));
          return;
        }
      } catch (_e) {
        // Không phải JSON, tiếp tục tải xuống
      }
    }
    const url = window.URL.createObjectURL(new Blob([data], { type: 'application/vnd.ms-excel' }));
    const link = document.createElement('a');
    link.style.display = 'none';
    link.href = url;
    link.setAttribute('download', 'Xuất dữ liệu.xls');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (e) {
    console.error('Xuất thất bại', e);
    message.error('Xuất thất bại');
  } finally {
    exportLoading.value = false;
  }
}

/**
 * Khi chuyển sang Tab dữ liệu thì tải lần đầu
 */
watch(activeTab, (val) => {
  if (val === '__data__' && !dataLoaded.value && hasSql.value) {
    dataLoaded.value = true;
    loadTableData();
  }
});

// ======================== Chuyển đổi dữ liệu biểu đồ ========================

/**
 * Chuẩn hóa dữ liệu gốc thành cấu trúc cần cho biểu đồ nhiều chuỗi.
 */
const multiSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => buildMultiSeriesItem(item));
});

/**
 * Cấu trúc dữ liệu dùng chung giữa biểu đồ đường diện tích và biểu đồ đa đường, truyền trong suốt seriesType để kiểm soát loại đường.
 */
const areaSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      ...buildMultiSeriesItem(item),
      seriesType: item && item.seriesType ? String(item.seriesType) : 'line',
      areaStyle: {},
    };
  });
});

/**
 * Chuẩn hóa dữ liệu gốc thành cấu trúc cần cho biểu đồ tròn.
 */
const pieSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      name: resolveName(item),
      value: resolveNumber(item),
    };
  });
});

/**
 * Xây dựng mục dữ liệu chuẩn hóa cần cho biểu đồ nhiều chuỗi.
 */
function buildMultiSeriesItem(item: Recordable): Recordable {
  const seriesName = resolveSeriesName(item);
  const name = resolveName(item);
  const value = resolveNumber(item);
  return {type: seriesName, name, value};
}

/**
 * Phân tích tên series, ưu tiên dùng series, tiếp theo dùng type, cuối cùng quay về"Dữ liệu"。
 */
function resolveSeriesName(item: Recordable): string {
  if (item && item.series !== undefined) {
    return String(item.series);
  }
  if (item && item.type !== undefined) {
    return String(item.type);
  }
  return 'Dữ liệu';
}

/**
 * Phân tích tên trục ngang, hỗ trợ trường x và name.
 */
function resolveName(item: Recordable): string {
  if (item && item.x !== undefined) {
    return String(item.x);
  }
  if (item && item.name !== undefined) {
    return String(item.name);
  }
  return '';
}

/**
 * Phân tích trường số, hỗ trợ y và value, khi không phải số thì quay về 0.
 */
function resolveNumber(item: Recordable): number {
  const rawValue = item ? item.y ?? item.value : null;
  const num = Number(rawValue);
  if (Number.isFinite(num)) {
    return num;
  }
  return 0;
}

/**
 * Phân tích loại chuỗi, biểu đồ cột gấp cần phân biệt bar / line。
 */
function resolveSeriesType(item: Recordable): string {
  if (item && item.seriesType !== undefined) {
    return String(item.seriesType);
  }
  return 'bar';
}

/**
 * Dữ liệu dashboard, cho phép đầu vào là mảng hoặc đối tượng, đảm bảo trả về name, value.
 */
const gaugeData = computed(() => {
  const dataSource = rawData.value as any;
  if (Array.isArray(dataSource) && dataSource.length > 0) {
    const item = dataSource[0];
    return { name: resolveName(item) || 'Bảng điều khiển', value: resolveNumber(item) };
  }
  if (dataSource && typeof dataSource === 'object') {
    return { name: resolveName(dataSource) || 'Bảng điều khiển', value: resolveNumber(dataSource as Recordable) };
  }
  return { name: 'Bảng điều khiển', value: 0 };
});

/**
 * Dữ liệu biểu đồ radar, hỗ trợ trường max định nghĩa giới hạn trên của chỉ số.
 */
const radarSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      type: resolveSeriesName(item),
      name: resolveName(item),
      value: resolveNumber(item),
      max: item && item.max !== undefined ? Number(item.max) : undefined,
    };
  });
});

/**
 * Dữ liệu biểu đồ cột gấp, kèm seriesType dùng để phân biệt cột/tuyến.
 */
const barLineSeriesData = computed<Recordable[]>(() => {
  if (!Array.isArray(rawData.value)) {
    return [];
  }
  return rawData.value.map((item: Recordable) => {
    return {
      ...buildMultiSeriesItem(item),
      seriesType: resolveSeriesType(item),
    };
  });
});

// ======================== Lấy props biểu đồ theo loại ========================

/**
 * Theo loại biểu đồ trả về thuộc tính hiển thị tương ứng
 */
function getChartPropsForType(type: ChartType): Recordable {
  switch (type) {
    case 'line':
      return { type: 'line', height: '360px', width: '100%', chartData: multiSeriesData.value };
    case 'bar':
      return { height: '360px', width: '100%', chartData: multiSeriesData.value };
    case 'pie':
      return { height: '360px', width: '100%', chartData: pieSeriesData.value };
    case 'multibar':
      return { height: '360px', width: '100%', chartData: multiSeriesData.value, option: parsedConfig.value.option || {} };
    case 'multiline':
      return { type: 'line', height: '360px', width: '100%', chartData: multiSeriesData.value, option: parsedConfig.value.option || {} };
    case 'barline':
      return { height: '360px', width: '100%', chartData: barLineSeriesData.value, customColor: (parsedConfig.value as any).colors || [], option: parsedConfig.value.option || {} };
    case 'area':
      return { type: 'line', height: '360px', width: '100%', chartData: areaSeriesData.value, option: { ...(parsedConfig.value.option || {}), areaStyle: {} } };
    case 'radar':
      return { height: '420px', width: '100%', chartData: radarSeriesData.value, option: parsedConfig.value.option || {} };
    case 'gauge':
      return { height: '360px', width: '100%', chartData: gaugeData.value, option: parsedConfig.value.option || {}, seriesColor: (parsedConfig.value as any).seriesColor || undefined };
    default:
      return {};
  }
}

/**
 * Theo loại biểu đồ trả về component tương ứng
 */
function getChartComponentForType(type: ChartType) {
  switch (type) {
    case 'line':
      return LineMulti;
    case 'bar':
      return BarMulti;
    case 'pie':
      return Pie;
    case 'multibar':
      return BarMulti;
    case 'multiline':
      return LineMulti;
    case 'barline':
      return BarAndLine;
    case 'area':
      return SingleLine;
    case 'radar':
      return Radar;
    case 'gauge':
      return Gauge;
    default:
      return null;
  }
}

/**
 * Component hiển thị biểu đồ nội bộ, hiển thị biểu đồ tương ứng một cách động theo chartType
 */
const ChartBody = defineComponent({
  props: {
    chartType: {
      type: String as () => ChartType,
      required: true,
    },
  },
  setup(bodyProps) {
    return () => {
      const comp = getChartComponentForType(bodyProps.chartType as ChartType);
      if (!comp) {
        return null;
      }
      const chartProps = getChartPropsForType(bodyProps.chartType as ChartType);
      return h(comp, chartProps);
    };
  },
});
</script>

<style scoped lang="less">

.ai-chat-chart {
  width: 100%;
  min-width: 420px;
  padding: 12px 0;
}

.ai-chat-chart__body {
  width: 100%;
}

.ai-chat-chart__error {
  color: #ff4d4f;
  font-size: 14px;
  line-height: 20px;
}

.ai-chat-chart__tabs {
  :deep(.ant-tabs-nav) {
    margin-bottom: 8px;
  }
}

.ai-chat-chart__data-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.ai-chat-chart__table {
  :deep(.ant-table-wrapper) {
    padding: 0;
  }
}

</style>
