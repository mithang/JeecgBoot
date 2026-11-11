import { ref, watch, computed, reactive, ExtractPropTypes } from 'vue';
import { router } from '/@/router';
import { cloneDeep } from 'lodash-es';
import { propTypes } from '/@/utils/propTypes';
import { printJS } from '/@/hooks/web/usePrintJS';
import { downloadByData } from '/@/utils/file/download';
import { filterDictText } from '/@/utils/dict/JDictSelectUtil';
import Bar from '/@/components/chart/Bar.vue';
import Pie from '/@/components/chart/Pie.vue';
import BarMulti from '/@/components/chart/BarMulti.vue';
import LineMulti from '/@/components/chart/LineMulti.vue';
import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { isFunction } from '/@/utils/is';
import { formatToDate } from '/@/utils/dateUtil';

export const ChartRenderProps = {
  // Tiêu đề biểu đồ
  title: propTypes.string,
  // Dữ liệu biểu đồ
  chartsData: propTypes.object,
  // Có đang chạy ở chế độ component hay không
  asComponent: propTypes.bool.def(false),
};
type PropsType = ExtractPropTypes<typeof ChartRenderProps>;

export const ChartRenderEmits = ['error'];
export const ChartRenderComponents = {
  LineMulti,
  BarMulti,
  Pie,
  Bar,
};

export const ChartRenderCommon = {
  components: ChartRenderComponents,
  props: ChartRenderProps,
  emits: ChartRenderEmits,
};

const errorText = {
  jsonFormattingFailed: 'JSONĐịnh dạng chuỗi ký tự thất bại',
};

export function useChartRender(props: PropsType, { emit }) {
  const {
    createMessage: $message,
    createConfirm: $confirm,
    createInfoModal: $info,
    createErrorModal: $error,
    createSuccessModal: $success,
    createWarningModal: $warning,
  } = useMessage();
  const headId = ref(null);
  // Chiều cao của biểu đồ
  const height = ref('400px');
  // Biểu đồ đang hiển thị hiện tại
  const activeKey = ref('bar');
  // Loại biểu đồ
  const chartTypes = ref<string[]>([]);
  // Có bật phân trang hay không
  const pageSwitch = ref(true);
  // InID
  const printId = computed(() => `print-content-${headId.value}`);
  // Cấu hình tham số biểu đồ đường cong
  const lineParams = reactive({
    chartData: [] as Recordable[],
  });
  // Cấu hình tham số biểu đồ cột
  const barParams = reactive({
    chartData: [] as Recordable[],
  });
  // Cấu hình tham số biểu đồ tròn
  const pieParams = reactive({
    chartData: [] as Recordable[],
  });
  // Cấu hình tham số biểu đồ gập/cột
  const barLineParams = reactive({
    dataSource: [] as Recordable[],
  });
  // Cấu hình tham số bảng
  const tableParams = reactive({
    // Dữ liệu cột cố định
    fixedColumns: [
      {
        title: '#',
        key: 'rowIndex',
        width: 60,
        align: 'center',
        customRender: function ({ record, index }) {
          if (record.isTotal === true) {
            return 'Tổng cộng';
          } else {
            return parseInt(index) + 1;
          }
        },
      },
    ],
    columns: [] as Recordable[],
    dataSource: [] as Recordable[],
  });
  // Người dùngJSNơi lưu tạm sự kiện của phần tăng cường (thông quaheadIdcách ly)
  const extendJsHandlerIsolation = reactive<Recordable>({});
  // của biểu đồ hiện tạiJSTăng cường
  const extendJsHandler = computed<Nullable<Recordable>>({
    get() {
      if (headId.value == null) {
        return null;
      } else {
        return extendJsHandlerIsolation[headId.value];
      }
    },
    set(obj) {
      if (headId.value != null) {
        extendJsHandlerIsolation[headId.value] = obj;
      }
    },
  });
  // Có bao gồm biểu đồ đường cong hay không
  const hasLine = computed(() => chartTypes.value.includes('line'));
  // Có bao gồm biểu đồ cột hay không
  const hasBar = computed(() => chartTypes.value.includes('bar'));
  // Có bao gồm biểu đồ tròn (pie) hay không
  const hasPie = computed(() => chartTypes.value.includes('pie'));
  // Có bao gồm bảng dữ liệu hay không
  const hasTable = ref(false);
  // Tham số biểu đồ đường cong
  const lineProps = computed(() => {
    return {
      type: 'line',
      height: height.value,
      chartData: lineParams.chartData,
      onClick(params) {
        // console.debug('lineProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // Tham số biểu đồ cột
  const barProps = computed(() => {
    return {
      height: height.value,
      chartData: barParams.chartData,
      onClick(params) {
        // console.debug('barProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // Tham số biểu đồ tròn
  const pieProps = computed(() => {
    return {
      height: height.value,
      chartData: pieParams.chartData,
      onClick(params) {
        // console.debug('pieProps-click: ', arguments)
        emitExtendJsEvent(params);
      },
    };
  });
  // Tham số biểu đồ gập/cột
  const barLineProps = computed(() => {
    return {
      height: height.value,
      dataSource: barLineParams.dataSource,
      onClick(_event, _chart) {
        console.debug('barLineProps-click: ', arguments);
      },
    };
  });
  // Vùng biểu đồ ACard Thuộc tính cố định của nhãn
  const chartCardProps = computed(() => {
    return {
      title: props.title,
      headStyle: { paddingLeft: '20px' },
      bodyStyle: { padding: '10px' },
      bordered: !props.asComponent,
    };
  });
  // Vùng bảng dữ liệu ACard Thuộc tính cố định của nhãn
  const tableCardProps = computed(() => {
    return {
      title: 'Chi tiết dữ liệu',
      headStyle: { paddingLeft: '20px' },
      bodyStyle: { padding: '0' },
      style: { marginTop: '20px' },
      bordered: !props.asComponent,
    };
  });
  // Thuộc tính cố định của nút xuất
  const exportButtonProps = computed(() => {
    return {
      type: 'primary',
      preIcon: 'ant-design:download',
      text: 'Xuất (export)',
      style: { margin: '12px' },
    };
  });
  /** Thuộc tính cố định của công tắc phân trang */
  const pageSwitchProps = computed(() => {
    return {
      checkedChildren: 'Phân trang',
      unCheckedChildren: 'Phân trang',
      style: {
        position: 'absolute',
        top: '17px',
        right: '12px',
      },
    };
  });
  // Thuộc tính cố định của bảng dữ liệu
  const tableProps = computed(() => {
    // update-begin--author:liaozhiyang---date:20260720---for：【LHZP-1041】Khi có nhiều trường, nội dung bảng bị dồn ép vào một chỗ
    // Tính độ rộng cuộn ngang theo tổng độ rộng cột, khi trường quá nhiều, bảng có thể cuộn ngang thay vì nén xuống dòng
    let scrollX = 0;
    for (const col of tableParams.columns) {
      const w = parseInt(col.width);
      scrollX += isNaN(w) ? 150 : w;
    }
    // update-end--author:liaozhiyang---date:20260720---for：【LHZP-1041】Khi có nhiều trường, nội dung bảng bị dồn ép vào một chỗ
    return {
      size: 'middle',
      rowKey: 'id',
      // bordered: true,
      pagination: pageSwitch.value ? { pageSize: 10 } : false,
      columns: tableParams.columns,
      dataSource: tableParams.dataSource,
      scroll: { x: scrollX },
      style: { borderTop: '1px solid #e8e8e8' },
    };
  });
  // Có hiển thị nút in hay không
  const showPrint = computed(() => !props.asComponent);
  // Có hiển thị nút chi tiết hay không
  const showDetail = computed(() => props.asComponent);

  watch(
    () => props.chartsData,
    (data) => parseChartsData(data),
    { immediate: true }
  );

  /** Thực thiJSMở rộng */
  function executeExtendJs(headId, jsCode) {
    if (!jsCode || !headId) {
      return;
    }
    let onClick = { line: null, bar: null, pie: null };
    // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
    // Thực thiJSTăng cường
    new Function('onClick', 'headId', `${jsCode}`)(onClick, headId);
    // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6390】evalthay thế thànhnew Function, giải quyếtbuildCảnh báo
    if (extendJsHandler.value == null) {
      extendJsHandler.value = { click: onClick };
    } else {
      extendJsHandler.value.click = onClick;
    }
  }

  // click của sự kiện this Trỏ tới
  const onClickThis = {
    $router: router,
    $http: defHttp,
    $message,
    $confirm,
    $info,
    $error,
    $success,
    $warning,
  };

  /** Kích hoạtJSSự kiện được định nghĩa trong phần tăng cường */
  function emitExtendJsEvent(params) {
    if (extendJsHandler.value != null) {
      let clickType = params.seriesType;
      let fn: Fn = extendJsHandler.value.click[clickType];
      if (isFunction(fn)) {
        normalizeExtendJsEventParams(params);
        fn.call(onClickThis, params);
      }
    }
  }

  /** Khi nhấp vào trường từ điển,JSPhần tăng cường sử dụng giá trị gốc, văn bản hiển thị được truyền qua displayName Lấy */
  function normalizeExtendJsEventParams(params) {
    let rawName = params.data?.rawName;
    if (rawName === undefined && (params.seriesType === 'line' || params.seriesType === 'bar')) {
      const chartData = params.seriesType === 'line' ? lineParams.chartData : barParams.chartData;
      rawName = chartData.find((item) => item.type === params.seriesName && item.name === params.name)?.rawName;
    }
    if (rawName !== undefined) {
      params.displayName = params.name;
      params.rawName = rawName;
      params.name = rawName;
    }
  }

  /** Phân tích (parse) chartData */
  function parseChartsData(chartsData) {
    if (chartsData == null) return null;
    let { head, data, items, dictOptions } = chartsData;
    if (head == null) return;
    let { id, xaxisField, yaxisField, dataType, cgrSql, graphType, extendJs } = head;
    headId.value = id;
    executeExtendJs(id, extendJs);
    try {
      data = dataType === 'sql' || dataType === 'api' ? data : JSON.parse(cgrSql);
    } catch {
      emit('error', errorText.jsonFormattingFailed);
      return;
    }
    let dictList = dictOptions[xaxisField];
    let graphTypes = graphType.split(',');
    activeKey.value = graphTypes[0];
    if (activeKey.value == 'table') {
      activeKey.value = graphTypes[1];
    }
    chartTypes.value = graphTypes;
    let yaxisFields: string[] = yaxisField.split(',');
    let fieldMap = new Map<string, Recordable>();
    items.forEach((item) => fieldMap.set(item.fieldName, item));
    // Phán đoán có định nghĩa bảng dữ liệu hay không, nếu đã định nghĩa thì xóa mục đó, không tham gia render động, chỉ hiển thị ở dưới cùng
    let index = graphTypes.indexOf('table');
    hasTable.value = index !== -1;
    if (hasTable.value) {
      graphTypes.splice(index, 1);
    }
    let parseOption = { graphTypes, data, items, fieldMap, xaxisField, yaxisFields, dictList, dictOptions };
    parseLineData(parseOption);
    parseBarData(parseOption);
    parsePicData(parseOption);
    parseTableData(parseOption);
  }

  type ParseDataOption = {
    graphTypes: string[];
    data: Recordable[];
    items: Recordable[];
    fieldMap: Map<string, Recordable>;
    xaxisField: string;
    yaxisFields: string[];
    dictList;
    dictOptions;
  };

  // Xây dựng dữ liệu dùng chung theo cấu hình của người dùng
  function parseCommonData(option: ParseDataOption) {
    let { data, fieldMap, xaxisField, yaxisFields, dictList } = option;
    let chartData: Recordable[] = [];
    for (let yField of yaxisFields) {
      for (let item of data) {
        const rawName = item[xaxisField];
        let name = rawName;
        // Phán đoán có từ điển hay không
        if (dictList) {
          name = filterDictText(dictList, name);
        }
        chartData.push({
          name: name,
          rawName,
          value: item[yField],
          type: fieldMap.get(yField)?.fieldTxt || yField,
        });
      }
    }
    return chartData;
  }

  // Xây dựng theo cấu hình của người dùng lineChartData
  function parseLineData(option: ParseDataOption) {
    let { graphTypes } = option;
    if (graphTypes.includes('line')) {
      lineParams.chartData = parseCommonData(option);
    }
  }

  // Xây dựng theo cấu hình của người dùng barChartData
  function parseBarData(option: ParseDataOption) {
    let { graphTypes } = option;
    if (graphTypes.includes('bar')) {
      barParams.chartData = parseCommonData(option);
    }
  }

  // Xây dựng theo cấu hình của người dùng pieChartData
  function parsePicData(option: ParseDataOption) {
    let { graphTypes, data, xaxisField, yaxisFields, dictList } = option;
    let yField = yaxisFields[0];
    if (graphTypes.includes('pie')) {
      let chartData: Recordable[] = [];
      for (let item of data) {
        const rawName = item[xaxisField];
        let name = rawName;
        // Phán đoán có từ điển hay không
        if (dictList) {
          name = filterDictText(dictList, name);
        }
        chartData.push({
          name: name,
          rawName,
          value: item[yField],
        });
      }
      pieParams.chartData = chartData;
    }
  }

  // Xây dựng theo cấu hình của người dùng tableData
  function parseTableData(option: ParseDataOption) {
    let { data, items, xaxisField, yaxisFields, dictList, dictOptions } = option;
    if (hasTable.value) {
      tableParams.dataSource = data.map((item, index) => {
        item.id = index;
        let pieData = {
          item: item[xaxisField],
          count: item[yaxisFields[0]],
        };
        // Phán đoán có từ điển hay không
        if (dictList) {
          pieData.item = filterDictText(dictList, pieData.item);
        }
        return item;
      });
      // Xây dựng theo cấu hình của người dùng tableColumns
      let tableColumns: Recordable[] = cloneDeep(tableParams.fixedColumns);
      let isTotals: string[] = [];
      items.forEach((item) => {
        if (item.isShow === 'Y') {
          let column: Recordable = {
            align: 'center',
            width: 150,
            ellipsis: true,
            title: item.fieldTxt,
            dataIndex: item.fieldName,
          };
          if (item.dictCode) {
            column.customRender = ({ text }) => filterDictText(dictOptions[item.fieldName], text);
          } else if (item.fieldType === 'Date') {
            column.customRender = ({ text, record }) => (record.isTotal || !text ? text : formatToDate(text));
          }
          tableColumns.push(column);
          // Phán đoán có tính tổng số hay không
          if (item.isTotal === 'Y') isTotals.push(item.fieldName);
        }
      });
      tableParams.columns = tableColumns;
      // Nếu có giá trị cần cho việc tính toán thì tiến hành tính toán
      if (isTotals.length > 0) {
        let totalRow = { id: tableParams.dataSource.length, isTotal: true };
        isTotals.forEach((column) => {
          let count = 0;
          tableParams.dataSource.forEach((row) => {
            // update-begin--author:liaozhiyang---date:20260805---for：【LHZP-1056】Chuỗi rỗng cần được lọc bỏ
            if (row[column]=='' || row[column]==null || isNaN(parseFloat(row[column]))) return;
            // update-end--author:liaozhiyang---date:20260805---for：【LHZP-1056】Chuỗi rỗng cần được lọc bỏ
            count += parseFloat(row[column]);
          });
          totalRow[column] = isNaN(count) ? 'Bao gồm nội dung không phải chữ số' : count.toFixed(2);
        });
        tableParams.dataSource.push(totalRow);
      }
    }
  }

  // Xuất (export)Excel
  function onExportXls() {
    let fileName = props.title;
    defHttp
      .get(
        {
          url: '/online/graphreport/api/exportXlsById',
          params: {
            id: headId.value,
            name: fileName,
          },
          responseType: 'blob',
        },
        { isTransformResponse: false }
      )
      .then((data) => {
        if (!data || data.size == 0) {
          $message.warning('Xuất thất bại!');
          return;
        }
        downloadByData(data, fileName + '.xls');
      });
  }

  // In
  function onPrint() {
    printJS({
      type: 'html',
      printable: '#' + printId.value,
    });
  }

  /** Chuyển tới trang chi tiết */
  function onGoToDetail() {
    goToInfo(props.chartsData);
  }

  function goToInfo(data) {
    let url = `/online/graphreport/chart/${data.head.id}`;
    router.push({ path: url });
  }

  return {
    headId,
    printId,
    height,
    activeKey,
    chartTypes,
    pageSwitch,
    showPrint,
    showDetail,
    hasLine,
    hasBar,
    hasPie,
    hasTable,
    lineProps,
    barProps,
    pieProps,
    tableProps,
    barLineProps,
    chartCardProps,
    tableCardProps,
    exportButtonProps,
    pageSwitchProps,
    extendJsHandlerIsolation,
    onPrint,
    onGoToDetail,
    onExportXls,
  };
}
