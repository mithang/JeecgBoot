<template>
  <div ref="tableFooter">
    <Table
      v-if="summaryFunc || summaryData"
      :showHeader="false"
      :bordered="bordered"
      :pagination="false"
      :dataSource="getDataSource"
      :rowKey="(r) => r[rowKey]"
      :columns="getColumns"
      tableLayout="fixed"
      :scroll="scroll"
    />
  </div>
</template>
<script lang="ts">
  import type { PropType } from 'vue';
  import { defineComponent, unref, computed, toRaw, onMounted, onUnmounted, ref } from 'vue';
  import { Table } from 'ant-design-vue';
  import { cloneDeep } from 'lodash-es';
  import { isFunction } from '/@/utils/is';
  import type { BasicColumn } from '../types/table';
  import { INDEX_COLUMN_FLAG } from '../const';
  import { propTypes } from '/@/utils/propTypes';
  import { useTableContext } from '../hooks/useTableContext';

  const SUMMARY_ROW_KEY = '_row';
  const SUMMARY_INDEX_KEY = '_index';
  export default defineComponent({
    name: 'BasicTableFooter',
    components: { Table },
    props: {
      bordered: {
        type: Boolean,
        default: false,
      },
      summaryFunc: {
        type: Function as PropType<Fn>,
      },
      summaryData: {
        type: Array as PropType<Recordable[]>,
      },
      scroll: {
        type: Object as PropType<Recordable>,
      },
      rowKey: propTypes.string.def('key'),
      // Có cột mở rộng không
      hasExpandedRow: propTypes.bool,
    },
    setup(props) {
      const table = useTableContext();
      const getColumnsRef = table.getColumnsRef();
      const tableFooter = ref<any>(null);
      const getDataSource = computed((): Recordable[] => {
        const { summaryFunc, summaryData } = props;
        if (summaryData?.length) {
          summaryData.forEach((item, i) => (item[props.rowKey] = `${i}`));
          return summaryData;
        }
        if (!isFunction(summaryFunc)) {
          return [];
        }
        // Giải thích logic mã: 【QQYUN-8172] Sau khi chỉnh sửa ô có thể chỉnh sửa xong không cập nhật giá trị tổng
        let dataSource = cloneDeep(unref(table.getDataSource()));
        dataSource = summaryFunc(dataSource);
        dataSource.forEach((item, i) => {
          item[props.rowKey] = `${i}`;
        });
        return dataSource;
      });

      const getColumns = computed(() => {
        const dataSource = unref(getDataSource);
        // Giải thích logic mã: 【issues/8502]Sau khi cột quyền không hiển thị, thanh tổng cộng ở hàng cuối bảng vẫn hiển thị gây lệch
        const allColumns = unref(getColumnsRef);
        let columns: BasicColumn[] = cloneDeep(table.getColumns({ ignoreAuth: true, ignoreIfShow: true }));
        // Giải thích logic mã: 【issues/638]Tổng cộng bảng, khi cột tùy chỉnh ẩn hoặc hiển thị, thanh tổng cộng sẽ bị lệch
        columns = columns.filter((item) => !item.defaultHidden);
        const index = columns.findIndex((item) => item.flag === INDEX_COLUMN_FLAG);
        const hasRowSummary = dataSource.some((item) => Reflect.has(item, SUMMARY_ROW_KEY));
        const hasIndexSummary = dataSource.some((item) => Reflect.has(item, SUMMARY_INDEX_KEY));

        // Có cột số thứ tự không
        let hasIndexCol = false;
        // Có cột chọn không
        let hasSelection = table.getRowSelection() && hasRowSummary

        if (index !== -1) {
          if (hasIndexSummary) {
            hasIndexCol = true;
            columns[index].customRender = ({ record }) => record[SUMMARY_INDEX_KEY];
            columns[index].ellipsis = false;
          } else {
            Reflect.deleteProperty(columns[index], 'customRender');
          }
        }

        if (hasSelection) {
          // Giải thích logic mã: 【issues/776] hiển thị 100 dòng//trang, vấn đề hộp kiểm chỉ hiển thị được 3 cái(fixedCũng có thể đặttrue)
          const isFixed = columns.some((col) => col.fixed === 'left' || col.fixed === true);
          columns.unshift({
            width: 50,
            title: 'selection',
            key: 'selectionKey',
            align: 'center',
            ...(isFixed ? { fixed: 'left' } : {}),
            customRender: ({ record }) => hasIndexCol ? '' : record[SUMMARY_ROW_KEY],
          });
        }

        if (props.hasExpandedRow) {
          const isFixed = columns.some((col) => col.fixed === 'left');
          columns.unshift({
            width: 50,
            title: 'expandedRow',
            key: 'expandedRowKey',
            align: 'center',
            ...(isFixed ? { fixed: 'left' } : {}),
            customRender: () => '',
          });
        }
        return columns;
      });
      // Giải thích logic mã: 【issues/7773] Dòng tổng không cuộn theo thanh cuộn trái/phải
      let mainTableBody, footerTable;
      const handleSroll = () => {
        const scrollLeft = mainTableBody.scrollLeft;
        footerTable.scrollLeft = scrollLeft;
      };
      onMounted(() => {
        setTimeout(() => {
          mainTableBody = tableFooter.value?.parentNode?.parentNode?.querySelector('.ant-table-body');
          footerTable = tableFooter.value?.querySelector('.ant-table-body');
          mainTableBody?.addEventListener('scroll', handleSroll, false);
        }, 1e3);
      });
      onUnmounted(() => {
        mainTableBody?.addEventListener('scroll', handleSroll);
      });
      return { getColumns, getDataSource, tableFooter };
    },
  });
</script>
<style lang="less" scoped>
  // Giải thích logic mã: 【issues/776] hiển thị 100 dòng//trang, vấn đề hộp kiểm chỉ hiển thị được 3 cái(Ẩn thanh cuộn của tổng cộng)
  .ant-table-wrapper {
    :deep(.ant-table-body) {
      overflow-x: hidden !important;
    }
  }
</style>
