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
    BasicFunctionForm,
    BasicFormConAttribute,
    BasicFormFieldShow,
    BasicFormFieldTip,
    BasicFormRules,
    BasicFormDynamicsRules,
    BasicFormSlots,
    BasicFormCustomSlots,
    BasicFormRander,
    BasicFixedWidthForm,
    BasicFiledsLayotForm,
    BasicFormLayout,
    BasicFormBtn,
    BasicFormCompact,
    BasicFormCleanRule,
    BasicFormValue,
    BasicFormSchemas,
    BasicFormAdd,
    BasicFormFooter,
    BasicFormModal,
    BasicFormCustom,
    BasicFormSearch,
    BasicFormComponent,
    BasicFormCustomComponent,
  } from './index';
  export default defineComponent({
    name: 'document-table-demo',
    components: {
      BasicFunctionForm,
      BasicFormConAttribute,
      BasicFormFieldShow,
      BasicFormFieldTip,
      BasicFormRules,
      BasicFormDynamicsRules,
      BasicFormSlots,
      BasicFormCustomSlots,
      BasicFormRander,
      BasicFixedWidthForm,
      BasicFiledsLayotForm,
      BasicFormLayout,
      BasicFormBtn,
      BasicFormCompact,
      BasicFormCleanRule,
      BasicFormValue,
      BasicFormSchemas,
      BasicFormAdd,
      BasicFormFooter,
      BasicFormModal,
      BasicFormCustom,
      BasicFormSearch,
      BasicFormComponent,
      BasicFormCustomComponent,
    },
    setup() {
      //Mục đang chọnkey
      const activeKey = ref('BasicFunctionForm');
      //Tập hợp component
      const compList = ref([
        { key: 'BasicFunctionForm', label: 'Biểu mẫu cơ bản' },
        { key: 'BasicFormConAttribute', label: 'Thuộc tính control trường' },
        { key: 'BasicFormComponent', label: 'Ant Design VueControl có sẵn' },
        { key: 'BasicFormCustomComponent', label: 'JEECGControl đã đóng gói' },
        { key: 'BasicFormFieldShow', label: 'Hiển thị và ẩn trường' },
        { key: 'BasicFormFieldTip', label: 'Gợi ý tiêu đề trường' },
        { key: 'BasicFormRules', label: 'Kiểm tra biểu mẫu' },
        { key: 'BasicFormDynamicsRules', label: 'Kiểm chứng động tùy chỉnh' },
        { key: 'BasicFormSlots', label: 'Slot trường' },
        { key: 'BasicFormCustomSlots', label: 'Component tùy chỉnh(Slot)' },
        { key: 'BasicFormCustom', label: 'Component tùy chỉnh(component)' },
        { key: 'BasicFormRander', label: 'Kết xuất tùy chỉnh' },
        { key: 'BasicFixedWidthForm', label: 'Cố định chiều rộng label' },
        { key: 'BasicFiledsLayotForm', label: 'Bố cục tiêu đề và trường' },
        { key: 'BasicFormLayout', label: 'Bố cục biểu mẫu' },
        { key: 'BasicFormBtn', label: 'Ví dụ nút thao tác' },
        { key: 'BasicFormCompact', label: 'Biểu mẫu gọn' },
        { key: 'BasicFormCleanRule', label: 'Cấu hình kiểm tra biểu mẫu' },
        { key: 'BasicFormValue', label: 'Lấy giá trị value' },
        { key: 'BasicFormSchemas', label: 'Cập nhật cấu hình schemas của biểu mẫu' },
        { key: 'BasicFormAdd', label: 'Thêm/bớt biểu mẫu động' },
        { key: 'BasicFormFooter', label: 'Chân trang tùy chỉnh' },
        { key: 'BasicFormModal', label: 'Biểu mẫu lớp popup' },
        { key: 'BasicFormSearch', label: 'Khu vực tìm kiếm' },
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
