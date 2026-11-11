<template>
  <PageWrapper contentBackground contentClass="flex" dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <a-button type="primary" @click="handleReloadCurrent"> Làm mới trang hiện tại </a-button>
        <a-button type="primary" @click="handleReload"> Làm mới và quay về trang đầu tiên </a-button>
      </template>
    </BasicTable>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getBasicColumns } from './tableData';
  import { PageWrapper } from '/@/components/Page';

  import { demoListApi } from '/@/api/demo/table';
  export default defineComponent({
    components: { BasicTable, PageWrapper },
    setup() {
      const [registerTable, { reload }] = useTable({
        title: 'Ví dụ tải từ xa',
        api: demoListApi,
        columns: getBasicColumns(),
        pagination: { pageSize: 10 },
      });
      function handleReloadCurrent() {
        reload();
      }

      function handleReload() {
        reload({
          page: 1,
        });
      }
      return {
        registerTable,
        handleReloadCurrent,
        handleReload,
      };
    },
  });
</script>
