<template>
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
    :height="398"
  >
    <template #toolbarSuffix>
      <a-dropdown>
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="fieldNameConvertCase('Upper', 'Chữ hoa')">
              <a-icon type="sort-descending" />
              <span>Chuyển đổi thành chữ hoa</span>
            </a-menu-item>
            <a-menu-item key="2" @click="fieldNameConvertCase('Lower', 'Chữ thường')">
              <a-icon type="sort-ascending" />
              <span>Chuyển đổi thành chữ thường</span>
            </a-menu-item>
          </a-menu>
        </template>
        <a-button preIcon="ant-design:down">Chuyển đổi tên trường</a-button>
      </a-dropdown>
    </template>
  </JVxeTable>
</template>

<script lang="ts">
  import { ref, defineComponent, nextTick, watch } from 'vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'ParamsTable',
    props: {
      loading: Boolean,
      dataSource: Array,
      disableSearch: Boolean,
    },
    setup(props) {
      const tableRef = ref<JVxeTableInstance>();
      const columns = ref<JVxeColumn[]>([
        {
          title: 'tên trường',
          key: 'fieldName',
          width: 180,
          type: JVxeTypes.input,
          defaultValue: '',
          validateRules: [{ required: true, message: '${title}không được để trống' }],
          placeholder: 'Vui lòng nhập${title}',
        },
        {
          title: 'Văn bản trường',
          key: 'fieldTxt',
          width: 180,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
        },
        {
          title: 'Loại trường',
          key: 'fieldType',
          width: 200,
          type: JVxeTypes.select,
          options: [
            { title: 'Loại giá trị số', value: 'Integer' },
            { title: 'Loại ngày', value: 'Date' },
            { title: 'Loại ký tự', value: 'String' },
            { title: 'Số nguyên dài (long)', value: 'Long' },
          ],
          defaultValue: '',
          placeholder: 'Vui lòng chọn${title}',
        },
        {
          title: 'Tính tổng cộng',
          key: 'isTotal',
          width: 80,
          type: JVxeTypes.checkbox,
          defaultChecked: false,
          customValue: ['Y', 'N'],
        },
        {
          title: 'Có truy vấn hay không',
          key: 'searchFlag',
          width: 80,
          type: JVxeTypes.checkbox,
          disabled: props.disableSearch,
          defaultChecked: false,
          customValue: ['Y', 'N'],
        },
        {
          title: 'Chế độ truy vấn',
          key: 'searchMode',
          width: 180,
          type: JVxeTypes.select,
          options: [
            { title: 'Truy vấn điều kiện đơn', value: 'single' },
            { title: 'Truy vấn theo phạm vi', value: 'group' },
          ],
          defaultValue: '',
          placeholder: 'Vui lòng chọn${title}',
        },
        {
          title: 'từ điểnCode',
          key: 'dictCode',
          width: 200,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng chọn${title}',
        },
        {
          title: 'Có hiển thị hay không',
          key: 'isShow',
          type: JVxeTypes.checkbox,
          defaultChecked: true,
          customValue: ['Y', 'N'],
        },
      ]);
      const { createConfirm: $confirm } = useMessage();

      // update-begin--author:liaozhiyang---date:20260824---for：【LHZP-1055】onlineBiểu đồJSONCách thức vô hiệu hóa truy vấn
      /** JSON Dữ liệu không hỗ trợ điều kiện truy vấn, hủy tất cả tùy chọn có truy vấn hay không */
      async function clearSearchFlags() {
        await nextTick();
        const values = tableRef.value?.getTableData() || [];
        tableRef.value?.setValues(
          values.map((item) => ({
            rowKey: item.id,
            values: { searchFlag: 'N' },
          }))
        );
      }

      watch(
        () => props.disableSearch,
        (disabled) => {
          const searchColumn = columns.value.find((column) => column.key === 'searchFlag');
          if (searchColumn) {
            searchColumn.disabled = disabled;
          }
          if (disabled) {
            clearSearchFlags();
          }
        },
        { immediate: true }
      );

      watch(
        () => props.dataSource,
        () => {
          if (props.disableSearch) {
            clearSearchFlags();
          }
        }
      );
      // update-end--author:liaozhiyang---date:20260824---for：【LHZP-1055】onlineBiểu đồJSONCách thức vô hiệu hóa truy vấn

      /** Chuyển đổi chữ hoa/thường của tên trường */
      function fieldNameConvertCase(convertCase, name) {
        $confirm({
          iconType: 'info',
          title: 'Chuyển đổi tên trường',
          content: `Xác nhận muốn chuyển đổi tất cả tên trường thành${name}không?`,
          onOk() {
            let fn = `to${convertCase}Case`;
            let values = tableRef.value!.getTableData();
            let newValues = values.map((item) => {
              return {
                rowKey: item.id,
                values: {
                  fieldName: item['fieldName'][fn](),
                },
              };
            });
            tableRef.value!.setValues(newValues);
          },
        });
      }

      return { tableRef, columns, fieldNameConvertCase };
    },
  });
</script>

<style scoped></style>
