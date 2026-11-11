<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    keyboardEdit
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabled="!actionButton"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    v-bind="tableProps"
  />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';

  export default defineComponent({
    name: 'ForeignKeyTable',
    props: {
      actionButton: { type: Boolean, default: true },
    },
    setup() {
      // Định nghĩa thông tin cột
      const columns = ref<JVxeColumn[]>([
        { title: 'Tên trường', key: 'dbFieldName', width: 160 },
        { title: 'Ghi chú trường', key: 'dbFieldTxt', width: 160 },
        {
          title: 'Tên bảng chính',
          key: 'mainTable',
          width: 280,
          type: JVxeTypes.input,
          defaultValue: '',
        },
        {
          title: 'trường bảng chính',
          key: 'mainField',
          width: 280,
          type: JVxeTypes.input,
          defaultValue: '',
        },
      ]);
      const setup = useTableSync(columns);
      return { ...setup, columns };
    },
  });
</script>

<style scoped></style>
