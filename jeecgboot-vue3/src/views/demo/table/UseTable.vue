<template>
  <div class="p-4">
    <div class="mb-4">
      <a-button class="mr-2" @click="reloadTable"> Khôi phục </a-button>
      <a-button class="mr-2" @click="changeLoading"> Bậtloading </a-button>
      <a-button class="mr-2" @click="changeColumns"> Thay đổiColumns </a-button>
      <a-button class="mr-2" @click="getColumn"> LấyColumns </a-button>
      <a-button class="mr-2" @click="getTableData"> Lấy dữ liệu bảng </a-button>
      <a-button class="mr-2" @click="getTableRawData"> Lấy dữ liệu gốc của API </a-button>
      <a-button class="mr-2" @click="setPaginationInfo"> Chuyển đến trang 2 </a-button>
    </div>
    <div class="mb-4">
      <a-button class="mr-2" @click="getSelectRowList"> Lấy hàng đã chọn </a-button>
      <a-button class="mr-2" @click="getSelectRowKeyList"> Lấy hàng đã chọnKey </a-button>
      <a-button class="mr-2" @click="setSelectedRowKeyList"> Đặt hàng đã chọn </a-button>
      <a-button class="mr-2" @click="clearSelect"> Xóa hàng đã chọn </a-button>
      <a-button class="mr-2" @click="getPagination"> Lấy thông tin phân trang </a-button>
    </div>
    <BasicTable @register="registerTable" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, ColumnChangeParam, useTable } from '/@/components/Table';
  import { getBasicColumns, getBasicShortColumns } from './tableData';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { demoListApi } from '/@/api/demo/table';
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const { createMessage } = useMessage();
      function onChange() {
        console.log('onChange', arguments);
      }
      const [
        registerTable,
        {
          setLoading,
          setColumns,
          getColumns,
          getDataSource,
          getRawDataSource,
          reload,
          getPaginationRef,
          setPagination,
          getSelectRows,
          getSelectRowKeys,
          setSelectedRowKeys,
          clearSelectedRowKeys,
        },
      ] = useTable({
        canResize: true,
        title: 'useTableVí dụ',
        titleHelpMessage: 'Dùng useTable để gọi phương thức trong bảng',
        api: demoListApi,
        columns: getBasicColumns(),
        defSort: {
          field: 'name',
          order: 'ascend',
        },
        rowKey: 'id',
        showTableSetting: true,
        onChange,
        rowSelection: {
          type: 'checkbox',
        },
        onColumnsChange: (data: ColumnChangeParam[]) => {
          console.log('ColumnsChanged', data);
        },
      });

      function changeLoading() {
        setLoading(true);
        setTimeout(() => {
          setLoading(false);
        }, 1000);
      }
      function changeColumns() {
        setColumns(getBasicShortColumns());
      }
      function reloadTable() {
        setColumns(getBasicColumns());

        reload({
          page: 1,
        });
      }
      function getColumn() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getColumns());
      }

      function getTableData() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getDataSource());
      }

      function getTableRawData() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getRawDataSource());
      }

      function getPagination() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getPaginationRef());
      }

      function setPaginationInfo() {
        setPagination({
          current: 2,
        });
        reload();
      }
      function getSelectRowList() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getSelectRows());
      }
      function getSelectRowKeyList() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getSelectRowKeys());
      }
      function setSelectedRowKeyList() {
        setSelectedRowKeys(['0', '1', '2']);
      }
      function clearSelect() {
        clearSelectedRowKeys();
      }

      return {
        registerTable,
        changeLoading,
        changeColumns,
        reloadTable,
        getColumn,
        getTableData,
        getTableRawData,
        getPagination,
        setPaginationInfo,
        getSelectRowList,
        getSelectRowKeyList,
        setSelectedRowKeyList,
        clearSelect,
        onChange,
      };
    },
  });
</script>
