<template>
  <JVxeTable
    ref="tableRef"
    rowNumber
    keyboardEdit
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    @valueChange="handleChange"
    v-bind="tableProps"
  />
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';

  export default defineComponent({
    name: 'QueryTable',
    emits: ['query'],
    setup(_, { emit }) {
      // Định nghĩa thông tin cột
      const columns = ref<JVxeColumn[]>([
        { title: 'Tên trường', key: 'dbFieldName', fixed: 'left', width: 130 },
        { title: 'Ghi chú trường', key: 'dbFieldTxt', fixed: 'left', width: 130 },
        {
          title: 'Loại control',
          key: 'queryShowType',
          width: 170,
          type: JVxeTypes.select,
          options: [
            { title: 'Ô văn bản', value: 'text' },
            { title: 'Ngày(yyyy-MM-dd)', value: 'date' },
            { title: 'Ngày (yyyy-MM-dd HH:mm:ss）', value: 'datetime' },
            { title: 'Thời gian (HH:mm:ss）', value: 'time' },
            // update-begin--author:liaozhiyang---date:20240531---for：【TV360X-415】Truy vấn cá nhân hóa hỗ trợ năm, tháng, tuần, quý
            // ảodate_year、date_month、date_week、date_quarterThực ra vẫn đi quadateComponent, để truy vấn
            { title: 'Ngày-Năm', value: 'date_year' },
            { title: 'Ngày-Tháng', value: 'date_month' },
            { title: 'Ngày-Tuần', value: 'date_week' },
            { title: 'Ngày-Quý', value: 'date_quarter' },
            // update-end--author:liaozhiyang---date:20240531---for：【TV360X-415】Truy vấn cá nhân hóa hỗ trợ năm, tháng, tuần, quý
            { title: 'Ô dropdown', value: 'list' },
            { title: 'Ô dropdown chọn nhiều', value: 'list_multi' },
            { title: 'Ô tìm kiếm dropdown', value: 'sel_search' },
            { title: 'Cây từ điển phân loại', value: 'cat_tree' },
            { title: 'PopupPopup', value: 'popup' },
            { title: 'Chọn phòng ban', value: 'sel_depart' },
            { title: 'Lựa chọn của người dùng', value: 'sel_user' },
            { title: 'Component tỉnh/thành/quận huyện', value: 'pca' },
            { title: 'Control cây tùy chỉnh', value: 'sel_tree' },
            // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-8】Bổ sung component công tắc cho loại control của cấu hình cá nhân hóa
            { title: 'Công tắc', value: 'switch' },
            // update-end--author:liaozhiyang---date:20240521---for：【TV360X-8】Bổ sung component công tắc cho loại control của cấu hình cá nhân hóa
            // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-415】Bổ sung loại control cho cấu hình cá nhân hóapopupComponent từ điển
            { title: 'Popuptừ điển', value: 'popup_dict' },
            // update-end--author:liaozhiyang---date:20240529---for：【TV360X-415】Bổ sung loại control cho cấu hình cá nhân hóapopupComponent từ điển
            // update-begin--author:liaozhiyang---date:20260810---for: 【Bảng đơn@Truy vấn tùy chỉnh】Khi tạo dữ liệu, hiển thị đúng bản ghi liên kết và loại control của trường bảng khác
            { title: 'bản ghi liên kết', value: 'link_table' },
            { title: 'trường bảng khác', value: 'link_table_field' },
            // update-end--author:liaozhiyang---date:20260810---for: 【Bảng đơn@Truy vấn tùy chỉnh】Khi tạo dữ liệu, hiển thị đúng bản ghi liên kết và loại control của trường bảng khác
          ],
          defaultValue: 'text',
          placeholder: 'Vui lòng chọn${title}',
          validateRules: [{ handler: validateQueryShowType }]
        },
        {
          title: 'từ điểnTable',
          key: 'queryDictTable',
          width: 130,
          type: JVxeTypes.textarea,
          defaultValue: '',
        },
        {
          title: 'từ điểnCode',
          key: 'queryDictField',
          width: 130,
          type: JVxeTypes.input,
          defaultValue: '',
        },
        {
          title: 'từ điểnText',
          key: 'queryDictText',
          width: 130,
          type: JVxeTypes.input,
          defaultValue: '',
        },
        {
          title: 'Giá trị mặc định',
          key: 'queryDefVal',
          width: 130,
          type: JVxeTypes.input,
          props: {
            allowClear: true,
          },
          defaultValue: '',
        },
        {
          title: 'Có kích hoạt hay không',
          key: 'queryConfigFlag',
          minWidth: 80,
          type: JVxeTypes.checkbox,
          customValue: ['1', '0'],
          defaultChecked: false,
          // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-816】Nếu là bản ghi liên kết hoặc truy vấn cá nhân hóa của trường cơ sở dữ liệu chưa được tích chọncheckboxVô hiệu hóa
          props: {
            isDisabledCell({ row, column }) {
              let { pageTable, dbTable, fkTable } = tables;
              // Lấy được pageTable(thuộc tính trang) dữ liệu trong đó
              const pageRowData = pageTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              if (['link_table'].includes(pageRowData?.fieldShowType)) {
                row.queryConfigFlag = '0';
                return true;
              }
              // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
              const dbRowData = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              if (dbRowData?.dbIsPersist == '0') {
                row.queryConfigFlag = '0';
                return true;
              }
              // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-199】Truy vấn tùy chỉnh vô hiệu hóa khóa ngoại
              // Lấy được ForeignKeyTable(khóa ngoại) dữ liệu trong đó
              const fkRowData = fkTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              if (fkRowData?.mainTable && fkRowData?.mainField) {
                return true;
              }
              // update-end--author:liaozhiyang---date:20240717---for：【TV360X-199】Truy vấn tùy chỉnh vô hiệu hóa khóa ngoại
              return false;
            },
          },
          // update-end--author:liaozhiyang---date:20240603---for：【TV360X-816】Nếu là bản ghi liên kết hoặc truy vấn cá nhân hóa của trường cơ sở dữ liệu chưa được tích chọncheckboxVô hiệu hóa
        },
      ]);
      const setup = useTableSync(columns);
      const { tables } = setup;

      function handleChange({ row, column, value }) {
        if (column.key === 'queryConfigFlag') {
          //update-begin-author:taoyan date:2022-5-23 for: VUEN-1084 【vue3】onlineVấn đề mới phát hiện khi kiểm thử biểu mẫu 3、truy vấn nâng cao, trường truy vấn danh sách có vấn đề, không hiển thị theo cấu hình truy vấn
          if (value === '1') {
            emit('query', row.id);
          }
          //update-end-author:taoyan date:2022-5-23 for: VUEN-1084 【vue3】onlineVấn đề mới phát hiện khi kiểm thử biểu mẫu 3、truy vấn nâng cao, trường truy vấn danh sách có vấn đề, không hiển thị theo cấu hình truy vấn
        }
      }

      /**
       *  2024-05-27
       *  liaozhiyang
       *  【TV360X-307】Ở trạng thái bật truy vấn, loại control bắt buộc chọn~
       */
      function validateQueryShowType({ cellValue, row }, callback) {
        // Ở trạng thái bắt đầu kích hoạt, loại control bắt buộc điền
        if (cellValue == null && row.queryConfigFlag == '1') {
          callback(false, 'Ở trạng thái bật truy vấn, loại control bắt buộc chọn~');
        }
        callback(true);
      }
      return { ...setup, columns, handleChange };
    },
  });
</script>
