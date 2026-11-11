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
    <BasicTable
      :canResize="false"
      title="RefTableVí dụ"
      titleHelpMessage="Dùng Ref để gọi phương thức trong bảng"
      ref="tableRef"
      :api="api"
      :columns="columns"
      rowKey="id"
      :rowSelection="{ type: 'checkbox' }"
    />
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicTable, TableActionType } from '/@/components/Table';
  import { getBasicColumns, getBasicShortColumns } from './tableData';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { demoListApi } from '/@/api/demo/table';
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const tableRef = ref<Nullable<TableActionType>>(null);
      const { createMessage } = useMessage();

      function getTableAction() {
        const tableAction = unref(tableRef);
        if (!tableAction) {
          throw new Error('tableAction is null');
        }
        return tableAction;
      }
      function changeLoading() {
        getTableAction().setLoading(true);
        setTimeout(() => {
          getTableAction().setLoading(false);
        }, 1000);
      }
      function changeColumns() {
        getTableAction().setColumns(getBasicShortColumns());
      }
      function reloadTable() {
        getTableAction().setColumns(getBasicColumns());

        getTableAction().reload({
          page: 1,
        });
      }
      function getColumn() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getColumns());
      }

      function getTableData() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getDataSource());
      }
      function getTableRawData() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getRawDataSource());
      }

      function getPagination() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getPaginationRef());
      }

      function setPaginationInfo() {
        getTableAction().setPagination({
          current: 2,
        });
        getTableAction().reload();
      }
      function getSelectRowList() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getSelectRows());
      }
      function getSelectRowKeyList() {
        createMessage.info('Vui lòng xem trong console!');
        console.log(getTableAction().getSelectRowKeys());
      }
      function setSelectedRowKeyList() {
        getTableAction().setSelectedRowKeys(['0', '1', '2']);
      }
      function clearSelect() {
        getTableAction().clearSelectedRowKeys();
      }

      return {
        tableRef,
        api: demoListApi,
        columns: getBasicColumns(),
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
      };
    },
  });
</script>
