<template>
  <div class="p-4">
    <BasicTable @register="register">
      <template #toolbar>
        <a-button type="primary" @click="expandAll">Mở rộng tất cả</a-button>
        <a-button type="primary" @click="collapseAll">Thu gọn tất cả</a-button>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable } from '/@/components/Table';
  import { getBasicColumns, getTreeTableData } from './tableData';

  export default defineComponent({
    components: { BasicTable },
    setup() {
      const [register, { expandAll, collapseAll }] = useTable({
        title: 'Bảng cây',
        isTreeTable: true,
        rowSelection: {
          type: 'checkbox',
          getCheckboxProps(record: Recordable) {
            // Demo: Ô chọn của dòng đầu tiên (id bằng 0) bị vô hiệu hóa
            if (record.id === '0') {
              return { disabled: true };
            } else {
              return { disabled: false };
            }
          },
        },
        titleHelpMessage: 'Component dạng cây không thể tồn tại cùng lúc với cột số thứ tự',
        columns: getBasicColumns(),
        dataSource: getTreeTableData(),
        rowKey: 'id',
      });
      return { register, expandAll, collapseAll };
    },
  });
</script>
