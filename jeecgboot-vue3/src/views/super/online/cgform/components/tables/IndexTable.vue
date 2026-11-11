<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    rowSelection
    dragSort
    keyboardEdit
    sortKey="orderNum"
    :maxHeight="tableHeight.normal"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :toolbar="actionButton"
    :insertRow="actionButton"
    v-bind="tableProps"
  />
</template>

<script lang="ts">
  import { ref, defineComponent, Ref } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { CgformModal } from '../../types';
  import { useTableSync } from '../../hooks/useTableSync';

  export default defineComponent({
    name: 'IndexTable',
    components: {},
    props: {
      actionButton: {
        type: Boolean,
        default: true,
        required: false,
      },
    },
    setup() {
      const columns = ref<JVxeColumn[]>([
        {
          title: 'Tên chỉ mục',
          key: 'indexName',
          width: 330,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
          validateRules: [
            { required: true, message: '${title}không được để trống' },
            // update-begin--author:liaozhiyang---date:20240807---for：【TV360X-1974】onlineTên chỉ mục thêm kiểm chứng
            {
              pattern: /^[a-zA-Z][a-zA-Z0-9_-]*$/,
              message: 'Quy tắc đặt tên: chỉ được cấu thành từ chữ cái, chữ số, dấu gạch dưới; phải bắt đầu bằng chữ cái',
            },
            // update-end--author:liaozhiyang---date:20240807---for：【TV360X-1974】onlineTên chỉ mục thêm kiểm chứng
          ],
        },
        {
          title: 'Ô chỉ mục',
          key: 'indexField',
          width: 330,
          type: JVxeTypes.selectMultiple,
          options: [],
          defaultValue: '',
          placeholder: 'Vui lòng chọn${title}',
          validateRules: [{ required: true, message: 'Vui lòng chọn${title}' }],
        },
        {
          title: 'Loại chỉ mục',
          key: 'indexType',
          width: 330,
          type: JVxeTypes.select,
          options: [
            { title: 'normal', value: 'normal' },
            { title: 'unique', value: 'unique' },
          ],
          defaultValue: 'normal',
          placeholder: 'Vui lòng chọn${title}',
          validateRules: [{ required: true, message: 'Vui lòng chọn${title}' }],
        },
      ]);

      const setup = useTableSync(columns);
      const { tableRef, loading, dataSource, tableHeight, tableProps, setDataSource, validateData } = setup;

      /** Đồng bộ danh sách */
      function syncTable(dbTable: Ref<CgformModal.DBAttributeTableType>) {
        let options: any[] = [];
        let data = dbTable.value!.tableRef!.getTableData();
        data.forEach((value) => {
          if (value.dbFieldName) {
            options.push({
              title: value.dbFieldTxt,
              value: value.dbFieldName,
            });
          }
        });
        columns.value[1].options = options;
      }

      return { tableRef, loading, dataSource, columns, tableHeight, tableProps, syncTable, setDataSource, validateData };
    },
  });
</script>

<style scoped></style>
