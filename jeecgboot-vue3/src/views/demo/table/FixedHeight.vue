<template>
  <div class="p-4">
    <BasicTable @register="registerTable">
      <template #customTitle>
        <span>
          Họ tên
          <BasicHelp class="ml-2" text="Họ tên" />
        </span>
      </template>
      <template #customAddress>
        Địa chỉ
        <FormOutlined class="ml-2" />
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getCustomHeaderColumns } from './tableData';
  import { FormOutlined } from '@ant-design/icons-vue';
  import { demoListApi } from '/@/api/demo/table';
  import { BasicHelp } from '/@/components/Basic';

  export default defineComponent({
    components: { BasicTable, FormOutlined, BasicHelp },
    setup() {
      const [registerTable] = useTable({
        title: 'Chiều cao cố định/Tùy chỉnh tiêu đề',
        api: demoListApi,
        columns: getCustomHeaderColumns(),
        canResize: false,
        scroll: { y: 100 },
      });

      return {
        registerTable,
      };
    },
  });
</script>
