<template>
  <div class="p-4">
    <a-card :bordered="false" style="height: 100%">
      <a-tabs v-model:activeKey="activeKey" @change="tabChange">
        <a-tab-pane :key="item.key" :tab="item.label" v-for="item in compList" />
      </a-tabs>
      <component :is="currentComponent" />
    </a-card>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref, computed } from 'vue';
  import {
    AuthColumnDemo,
    BasicTableBorder,
    BasicTableDemo,
    BasicTableDemoAjax,
    CustomerCellDemo,
    EditCellTableDemo,
    EditRowTableDemo,
    ExpandTableDemo,
    ExportTableDemo,
    FixedHeaderColumn,
    InnerTableDemo,
    MergeHeaderDemo,
    MergeTableDemo,
      SelectTableDemo,
      TreeTableDemo,
      MultipleTableDemo,
    } from './index';
  export default defineComponent({
    name: 'document-table-demo',
    components: {
      AuthColumnDemo,
      BasicTableBorder,
      BasicTableDemo,
      BasicTableDemoAjax,
      CustomerCellDemo,
      EditCellTableDemo,
      EditRowTableDemo,
      ExpandTableDemo,
      ExportTableDemo,
      FixedHeaderColumn,
      InnerTableDemo,
      MergeHeaderDemo,
      MergeTableDemo,
        SelectTableDemo,
        TreeTableDemo,
        MultipleTableDemo,
      },
    setup() {
      //Mục đang chọnkey
      const activeKey = ref('BasicTableDemo');
      //Tập hợp component
      const compList = ref([
        { key: 'BasicTableDemo', label: 'Bảng tĩnh cơ bản' },
        { key: 'BasicTableDemoAjax', label: 'Bảng AJAX thông thường' },
        { key: 'BasicTableBorder', label: 'Bảng có viền' },
        { key: 'CustomerCellDemo', label: 'Nội dung cột tùy chỉnh' },
        { key: 'EditCellTableDemo', label: 'Ô có thể chỉnh sửa' },
        { key: 'EditRowTableDemo', label: 'Hàng có thể chỉnh sửa' },
        { key: 'ExpandTableDemo', label: 'Bảng có thể mở rộng' },
        { key: 'ExportTableDemo', label: 'Nhập / Xuất' },
        { key: 'FixedHeaderColumn', label: 'Ví dụ tiêu đề và cột cố định' },
        { key: 'InnerTableDemo', label: 'Bảng nhúng' },
        { key: 'MergeHeaderDemo', label: 'Ví dụ tiêu đề bảng nhóm' },
        { key: 'MergeTableDemo', label: 'Gộp hàng và cột' },
        { key: 'SelectTableDemo', label: 'Bảng có thể chọn' },
          { key: 'TreeTableDemo', label: 'Bảng cây' },
          { key: 'AuthColumnDemo', label: 'Cài đặt cột quyền hạn' },
          { key: 'MultipleTableDemo', label: 'Nhiều phiên bản bảng' },
        ]);
      //Component đang chọn
      const currentComponent = computed(() => {
        return activeKey.value;
      });

      //Dùng component để chuyển đổi độngtab
      function tabChange(key) {
        activeKey.value = key;
      }
      return {
        activeKey,
        currentComponent,
        tabChange,
        compList,
      };
    },
  });
</script>
