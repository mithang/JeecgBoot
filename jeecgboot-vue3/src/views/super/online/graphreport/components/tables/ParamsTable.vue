<template>
  <a-alert type="info" showIcon message="Hiện tại chỉ cóSQLChỉ chế độ này mới hỗ trợ tham số báo cáo!" />
  <br />
  <JVxeTable
    ref="tableRef"
    toolbar
    rowNumber
    rowSelection
    dragSort
    sortKey="orderNum"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :height="336"
  />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';

  export default defineComponent({
    name: 'FieldTable',
    props: {
      loading: Boolean,
      dataSource: Array,
    },
    setup() {
      const tableRef = ref<JVxeTableInstance>();
      const columns = ref<JVxeColumn[]>([
        {
          title: 'Tham số',
          key: 'paramName',
          width: 230,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
          validateRules: [{ required: true, message: '${title}không được để trống' }],
        },
        {
          title: 'Văn bản tham số',
          key: 'paramTxt',
          width: 230,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
          validateRules: [{ required: true, message: '${title}không được để trống' }],
        },
        {
          title: 'Giá trị mặc định',
          key: 'paramValue',
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
        },
      ]);
      return { tableRef, columns };
    },
  });
</script>

<style scoped></style>
