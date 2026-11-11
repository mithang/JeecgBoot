<template>
  <div class="p-4">
    <BasicTable
      title="Ví dụ cơ bản"
      titleHelpMessage="Nhắc nhở thân thiện"
      :columns="columns"
      :dataSource="data"
      :canResize="canResize"
      :loading="loading"
      :striped="striped"
      :bordered="border"
      showTableSetting
      :pagination="pagination"
      @columns-change="handleColumnChange"
    >
      <template #toolbar>
        <a-button type="primary" @click="toggleCanResize">
          {{ !canResize ? 'Chiều cao tự thích ứng' : 'Hủy tự thích ứng' }}
        </a-button>
        <a-button type="primary" @click="toggleBorder">
          {{ !border ? 'Hiển thị viền' : 'Ẩn viền' }}
        </a-button>
        <a-button type="primary" @click="toggleLoading"> Bậtloading </a-button>
        <a-button type="primary" @click="toggleStriped">
          {{ !striped ? 'Hiển thị vân ngựa vằn' : 'Ẩn vân ngựa vằn' }}
        </a-button>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicTable, ColumnChangeParam } from '/@/components/Table';
  import { getBasicColumns, getBasicData } from './tableData';

  export default defineComponent({
    components: { BasicTable },
    setup() {
      const canResize = ref(false);
      const loading = ref(false);
      const striped = ref(true);
      const border = ref(true);
      const pagination = ref<any>(false);
      function toggleCanResize() {
        canResize.value = !canResize.value;
      }
      function toggleStriped() {
        striped.value = !striped.value;
      }
      function toggleLoading() {
        loading.value = true;
        setTimeout(() => {
          loading.value = false;
          pagination.value = { pageSize: 20 };
        }, 3000);
      }
      function toggleBorder() {
        border.value = !border.value;
      }

      function handleColumnChange(data: ColumnChangeParam[]) {
        console.log('ColumnChanged', data);
      }

      return {
        columns: getBasicColumns(),
        data: getBasicData(),
        canResize,
        loading,
        striped,
        border,
        toggleStriped,
        toggleCanResize,
        toggleLoading,
        toggleBorder,
        pagination,
        handleColumnChange,
      };
    },
  });
</script>
