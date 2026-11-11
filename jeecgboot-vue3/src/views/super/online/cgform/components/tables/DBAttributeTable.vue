<template>
  <JVxeTable
    class="dBAttributeTable"
    ref="tableRef"
    rowNumber
    rowSelection
    dragSort
    :notAllowDrag="[{'key':'dbFieldName','value':'id'}]"
    keyboardEdit
    sortKey="orderNum"
    addButtonSettings
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :toolbar="actionButton"
    :insertRow="actionButton"
    :maxHeight="tableHeight.normal"
    :disabledRows="{ dbFieldName: handleDisabledDbFieldName }"
    v-bind="tableProps"
    @added="handleAdded"
    @removed="handleRemoved"
    @dragged="handleDragged"
    @inserted="handleInserted"
    @valueChange="handleValueChange"
  >
    <template #toolbarSuffix>
      <a-button type="primary" preIcon="ant-design:robot-outlined" @click="openAiModal">AIĐề xuất trường</a-button>
      <!-- update-begin--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (hiển thị nút tắt ở cuối thanh công cụ theo trạng thái tồn tại của trường) -->
      <a-tooltip title="Thêm trường trạng thái quy trình">
        <a-button v-if="actionButton && !hasBpmStatusField" type="primary" preIcon="ant-design:branches-outlined" @click="addBpmStatusField">Trường trạng thái quy trình</a-button>
      </a-tooltip>
      <!-- update-end--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (hiển thị nút tắt ở cuối thanh công cụ theo trạng thái tồn tại của trường) -->
    </template>
  </JVxeTable>
  <AiModal
    :tableName="tableName"
    :isUpdate="isUpdate"
    @register="registerAiModal"
    v-if="aiModalShow"
    @close="() => (aiModalShow = false)"
    :DBtableRef="tableRef"
    @generate="handleGenerate"
  />
</template>

<script lang="ts">
  import { ref, getCurrentInstance, defineComponent, computed } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';
  import AiModal from './components/aiModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';

  const MySQLKeywords = [
    'ADD',
    'ALL',
    'ALTER',
    'ANALYZE',
    'AND',
    'AS',
    'ASC',
    'ASENSITIVE',
    'BEFORE',
    'BETWEEN',
    'BIGINT',
    'BINARY',
    'BLOB',
    'BOTH',
    'BY',
    'CALL',
    'CASCADE',
    'CASE',
    'CHANGE',
    'CHAR',
    'CHARACTER',
    'CHECK',
    'COLLATE',
    'COLUMN',
    'CONDITION',
    'CONNECTION',
    'CONSTRAINT',
    'CONTINUE',
    'CONVERT',
    'CREATE',
    'CROSS',
    'CURRENT_DATE',
    'CURRENT_TIME',
    'CURRENT_TIMESTAMP',
    'CURRENT_USER',
    'CURSOR',
    'DATABASE',
    'DATABASES',
    'DAY_HOUR',
    'DAY_MICROSECOND',
    'DAY_MINUTE',
    'DAY_SECOND',
    'DEC',
    'DECIMAL',
    'DECLARE',
    'DEFAULT',
    'DELAYED',
    'DELETE',
    'DESC',
    'DESCRIBE',
    'DETERMINISTIC',
    'DISTINCT',
    'DISTINCTROW',
    'DIV',
    'DOUBLE',
    'DROP',
    'DUAL',
    'EACH',
    'ELSE',
    'ELSEIF',
    'ENCLOSED',
    'ESCAPED',
    'EXISTS',
    'EXIT',
    'EXPLAIN',
    'FALSE',
    'FETCH',
    'FLOAT',
    'FLOAT4',
    'FLOAT8',
    'FOR',
    'FORCE',
    'FOREIGN',
    'FROM',
    'FULLTEXT',
    'GOTO',
    'GRANT',
    'GROUP',
    'HAVING',
    'HIGH_PRIORITY',
    'HOUR_MICROSECOND',
    'HOUR_MINUTE',
    'HOUR_SECOND',
    'IF',
    'IGNORE',
    'IN',
    'INDEX',
    'INFILE',
    'INNER',
    'INOUT',
    'INSENSITIVE',
    'INSERT',
    'INT',
    'INT1',
    'INT2',
    'INT3',
    'INT4',
    'INT8',
    'INTEGER',
    'INTERVAL',
    'INTO',
    'IS',
    'ITERATE',
    'JOIN',
    'KEY',
    'KEYS',
    'KILL',
    'LABEL',
    'LEADING',
    'LEAVE',
    'LEFT',
    'LIKE',
    'LIMIT',
    'LINEAR',
    'LINES',
    'LOAD',
    'LOCALTIME',
    'LOCALTIMESTAMP',
    'LOCK',
    'LONG',
    'LONGBLOB',
    'LONGTEXT',
    'LOOP',
    'LOW_PRIORITY',
    'MATCH',
    'MEDIUMBLOB',
    'MEDIUMINT',
    'MEDIUMTEXT',
    'MIDDLEINT',
    'MINUTE_MICROSECOND',
    'MINUTE_SECOND',
    'MOD',
    'MODIFIES',
    'NATURAL',
    'NOT',
    'NO_WRITE_TO_BINLOG',
    'NULL',
    'NUMERIC',
    'ON',
    'OPTIMIZE',
    'OPTION',
    'OPTIONALLY',
    'OR',
    'ORDER',
    'OUT',
    'OUTER',
    'OUTFILE',
    'PRECISION',
    'PRIMARY',
    'PROCEDURE',
    'PURGE',
    'RAID0',
    'RANGE',
    'READ',
    'READS',
    'REAL',
    'REFERENCES',
    'REGEXP',
    'RELEASE',
    'RENAME',
    'REPEAT',
    'REPLACE',
    'REQUIRE',
    'RESTRICT',
    'RETURN',
    'REVOKE',
    'RIGHT',
    'RLIKE',
    'SCHEMA',
    'SCHEMAS',
    'SECOND_MICROSECOND',
    'SELECT',
    'SENSITIVE',
    'SEPARATOR',
    'SET',
    'SHOW',
    'SMALLINT',
    'SPATIAL',
    'SPECIFIC',
    'SQL',
    'SQLEXCEPTION',
    'SQLSTATE',
    'SQLWARNING',
    'SQL_BIG_RESULT',
    'SQL_CALC_FOUND_ROWS',
    'SQL_SMALL_RESULT',
    'SSL',
    'STARTING',
    'STRAIGHT_JOIN',
    'TABLE',
    'TERMINATED',
    'THEN',
    'TINYBLOB',
    'TINYINT',
    'TINYTEXT',
    'TO',
    'TRAILING',
    'TRIGGER',
    'TRUE',
    'UNDO',
    'UNION',
    'UNIQUE',
    'UNLOCK',
    'UNSIGNED',
    'UPDATE',
    'USAGE',
    'USE',
    'USING',
    'UTC_DATE',
    'UTC_TIME',
    'UTC_TIMESTAMP',
    'VALUES',
    'VARBINARY',
    'VARCHAR',
    'VARCHARACTER',
    'VARYING',
    'WHEN',
    'WHERE',
    'WHILE',
    'WITH',
    'WRITE',
    'X509',
    'XOR',
    'YEAR_MONTH',
    'ZEROFILL',
  ];

  export default defineComponent({
    name: 'DBAttributeTable',
    props: {
      actionButton: { type: Boolean, default: true },
      tableName: { type: String, default: '' },
      isUpdate: { type: Boolean, default: false },
      isTree: { type: String, default: 'N' },
    },
    components: {
      AiModal,
    },
    emits: ['added', 'removed', 'inserted', 'dragged', 'syncDbType', 'syncQueryMode', 'syncDbIsPersist', 'syncDbIsNull'],
    setup(props, { emit }) {
      const instance = getCurrentInstance();
      const { createMessage: $message } = useMessage();
      const addBatching = ref(false);
      const columns = ref<JVxeColumn[]>([
        {
          title: 'Tên trường',
          key: 'dbFieldName',
          width: 140,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
          fixed: 'left',
          validateRules: [
            { required: true, message: '${title}không được để trống' },
            {
              pattern: /^[a-zA-Z]{1}(?!_)[a-zA-Z0-9_\\$]+$/,
              message: 'Quy tắc đặt tên: chỉ được gồm chữ cái, chữ số, dấu gạch dưới,$ký tự hợp thành; phải bắt đầu bằng chữ cái; không được bắt đầu bằng một chữ cái đơn lẻ cộng dấu gạch dưới',
            },
            { unique: true, message: '${title}không được trùng lặp' },
            {
              handler({ cellValue }, callback) {
                /* Phán đoán có phải từ khóa (keyword) hay không */
                if (MySQLKeywords.includes(cellValue.toUpperCase())) {
                  callback(false, cellValue + 'là từ khóa (keyword), không thể dùng làm tên trường!');
                } else {
                  callback(true);
                }
              },
            },
            { handler: validateExistIndex },
            // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
            {
              handler({ cellValue }, callback) {
                if (cellValue.length > 32) {
                  callback(false, 'Tên trường dài nhất32ký tự');
                } else {
                  callback(true);
                }
              },
            },
            // update-end--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
          ],
          disabled: !props.actionButton,
        },
        {
          title: 'Ghi chú trường',
          key: 'dbFieldTxt',
          width: 140,
          type: JVxeTypes.input,
          defaultValue: '',
          placeholder: 'Vui lòng nhập${title}',
          fixed: 'left',
          validateRules: [
            { required: true, message: '${title}không được để trống' },
            // update-begin--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
            {
              handler({ cellValue }, callback) {
                if (cellValue.length > 200) {
                  callback(false, 'Tên trường dài nhất200ký tự');
                } else {
                  callback(true);
                }
              },
            },
            // update-end--author:liaozhiyang---date:20240603---for：【TV360X-631】Kiểm chứng độ dài của tên bảng, tên trường, mô tả bảng, ghi chú trường
          ],
        },
        {
          title: 'Độ dài trường',
          key: 'dbLength',
          width: 120,
          type: JVxeTypes.inputNumber,
          defaultValue: 32,
          placeholder: 'Vui lòng nhập${title}',
          validateRules: [{ required: true, message: '${title}không được để trống' }],
          disabled: !props.actionButton,
        },
        {
          title: 'Dấu thập phân',
          key: 'dbPointLength',
          width: 100,
          type: JVxeTypes.inputNumber,
          defaultValue: 0,
          placeholder: 'Vui lòng nhập${title}',
          validateRules: [{ required: true, message: '${title}không được để trống' }],
          disabled: !props.actionButton,
        },
        {
          title: 'Giá trị mặc định',
          key: 'dbDefaultVal',
          width: 140,
          type: JVxeTypes.input,
          defaultValue: '',
          disabled: !props.actionButton,
        },
        {
          title: 'Loại trường',
          key: 'dbType',
          width: 140,
          type: JVxeTypes.select,
          options: [
            { title: 'String', value: 'string' },
            { title: 'Integer', value: 'int' },
            { title: 'Double', value: 'double' },
            { title: 'Long', value: 'long' },
            { title: 'Date', value: 'Date' },
            { title: 'Datetime', value: 'Datetime' },
            { title: 'BigDecimal', value: 'BigDecimal' },
            { title: 'Text', value: 'Text' },
            { title: 'LongText', value: 'LongText' },
            { title: 'Blob', value: 'Blob' },
          ],
          defaultValue: 'string',
          placeholder: 'Vui lòng chọn${title}',
          disabled: !props.actionButton,
          validateRules: [{ required: true, message: 'Vui lòng chọn${title}' }, { handler: validateFieldDbType }],
        },
        {
          title: 'Khóa chính',
          key: 'dbIsKey',
          width: 80,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
          disabled: !props.actionButton,
        },
        {
          title: 'Cho phép giá trị rỗng',
          key: 'dbIsNull',
          width: 80,
          type: JVxeTypes.checkbox,
          customValue: ['1', '0'],
          defaultChecked: true,
          disabled: !props.actionButton,
        },
        {
          title: 'Đồng bộ cơ sở dữ liệu',
          key: 'dbIsPersist',
          minWidth: 80,
          type: JVxeTypes.checkbox,
          customValue: ['1', '0'],
          defaultChecked: true,
          disabled: !props.actionButton,
        },
        { title: 'orderNum', key: 'orderNum', type: JVxeTypes.hidden },
      ]);
      let removeIds = [];

      const setup = useTableSync(columns);
      const { tableRef, loading, dataSource, tableHeight, tableProps, setDataSource, validateData } = setup;
      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (theo dõi thêm/xóa trong bảng và điều khiển ẩn/hiện nút tắt)
      const tableDataVersion = ref(0);
      const hasBpmStatusField = computed(() => {
        tableDataVersion.value;
        const tableData = tableRef.value?.getTableData() ?? dataSource.value;
        return tableData.some((row) => row.dbFieldName === 'bpm_status');
      });
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (theo dõi thêm/xóa trong bảng và điều khiển ẩn/hiện nút tắt)
      // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
      const aiModalShow = ref(false);
      const [registerAiModal, { openModal }] = useModal();
      const handleGenerate = (data) => {
        tableRef.value!.addRows(data);
      };
      // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng

      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (dùng lại giá trị mặc định thêm mới thông thường và cấu hình thuộc tính trang trạng thái quy trình)
      /** Thêm trường trạng thái quy trình theo cấu hình mặc định của hàng thêm mới thông thường, chỉ ghi đè các thuộc tính mà trường quy trình yêu cầu. */
      function addBpmStatusField() {
        if (hasBpmStatusField.value) {
          $message.warning('Trường trạng thái quy trình đã tồn tại');
          return;
        }
        tableRef.value!.addRows({
          dbFieldName: 'bpm_status',
          dbFieldTxt: 'Trạng thái quy trình',
          isShowList: '1',
          fieldShowType: 'list',
          dictField: 'bpm_status',
          isShowForm: '0',
          sortFlag: '0',
        });
      }
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-1951】Thêm trường trạng thái quy trình (dùng lại giá trị mặc định thêm mới thông thường và cấu hình thuộc tính trang trạng thái quy trình)

      function handleAdded() {
        tableDataVersion.value++;
        emit('added', instance);
      }

      function handleRemoved(event) {
        tableDataVersion.value++;
        removeIds = removeIds.concat(event.deleteRows.map((r) => r.id));
        emit('removed', { ...event, removeIds, target: instance });
      }

      function handleDragged(event) {
        emit('dragged', {
          oldIndex: event.oldIndex,
          newIndex: event.newIndex,
          target: instance,
        });
      }

      function handleInserted(event) {
        emit('inserted', { ...event, target: instance });
      }

      function getRemoveIds() {
        return removeIds;
      }

      function handleValueChange(event) {
        let { type, row, col, value, target, oldValue } = event;
        if (type === JVxeTypes.select && col.key === 'dbType') {
          // Khi loại trường đổi thànhDatekhi kích hoạt sự kiện đồng bộ, loại control đồng bộ là bộ chọn ngày
          if (value === 'Date' || value === 'Datetime') {
            emit('syncDbType', { row, value, target: instance });
          }

          // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7530】Loại trường từDatevàDateTimeđổi thànhstring, control cũng biến thành ô văn bản
          if ((value !== 'Date' || value !== 'Datetime') && (oldValue == 'Date' || oldValue == 'Datetime')) {
            emit('syncDbType', { row, value, target: instance });
          }
          // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7530】Loại trường từDatevàDateTimeđổi thànhstring, control cũng biến thành ô văn bản
          // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
          // BlobLoại không hỗ trợ truy vấn, kích hoạt đồng bộ bỏ tích chọn truy vấn
          if (value === 'Blob') {
            emit('syncDbType', { row, value, target: instance });
          }
          // update-end--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
          // Loại trườngBlob、Date、Text Đổi độ dài trường thành0
          if (value === 'Blob' || value === 'Text' || value === 'Date') {
            target.setValues([{ rowKey: row.id, values: { dbLength: '0' } }]);
          } else if (value === 'string') {
            // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-56】Sửa loại trường, đổi thànhstringkhi đó cấp độ dài mặc định
            target.setValues([{ rowKey: row.id, values: { dbLength: '32' } }]);
            // update-end--author:liaozhiyang---date:20240517---for：【TV360X-56】Sửa loại trường, đổi thànhstringkhi đó cấp độ dài mặc định
          } else if (value === 'int' || value === 'double' || value === 'BigDecimal') {
            target.setValues([{ rowKey: row.id, values: { dbLength: '10' } }]);
          //update-begin---author:wangshuai---date:2025-10-24---for:【QQYUN-13901】Bổ sungLongLoại---
          } else if (value === 'long') {
            target.setValues([{ rowKey: row.id, values: { dbLength: '19' } }]);
          //update-end---author:wangshuai---date:2025-10-24---for:【QQYUN-13901】Bổ sungLongLoại---
          } else if (row['dbLength'] === '0') {
            target.setValues([{ rowKey: row.id, values: { dbLength: '32' } }]);
          }
          // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (sau khi loại trường thay đổi, thông báo thuộc tính trang cập nhật loại truy vấn)
          emit('syncQueryMode', { oldValue, row });
          // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (sau khi loại trường thay đổi, thông báo thuộc tính trang cập nhật loại truy vấn)
        } else if (col.key === 'dbIsPersist') {
          // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng
          emit('syncDbIsPersist', { row, value, target: instance });
          // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng
        } else if (col.key === 'dbIsNull') {
          // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng
          emit('syncDbIsNull', { row, value, target: instance });
          // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng
        }
      }

      // dbFieldNameTrường bị vô hiệu hóa
      function handleDisabledDbFieldName(value, _row, rowIndex) {
        // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-368】onlineView chèn một hàng bị vô hiệu hóa
        if (value === 'has_child' && props.isTree === 'Y') {
          return true;
        }
        // update-end--author:liaozhiyang---date:20260713---for：【LHZP-368】onlineView chèn một hàng bị vô hiệu hóa
        if (value === 'id') {
          // -update-begin--author:liaozhiyang---date:20240614---for：【TV360X-357】Kéo hàng khác trong thuộc tính cơ sở dữ liệu tớiidkhi ở trên hàngidđã có thể chỉnh sửa
          const { tables } = setup;
          const dataSource = tables.dbTable.value!.tableRef.getTableData() ?? [];
          const findIndex = dataSource.findIndex((item) => item.dbFieldName === 'id');
          const idIndex = findIndex === -1 ? 0 : findIndex;
          // -update-end--author:liaozhiyang---date:20240614---for：【TV360X-357】Kéo hàng khác trong thuộc tính cơ sở dữ liệu tớiidkhi ở trên hàngidđã có thể chỉnh sửa
          if (idIndex === rowIndex) {
            return true;
          }
        }
        return false;
      }

      /** Thêm một hàng, đồng thời phản hồi cho component cha */
      function tableAddLine(newLine) {
        tableRef.value!.pushRows(newLine);
        // Đang thêm dữ liệu hàng loạt, không phản hồi riêng lẻ
        if (!addBatching.value) {
          emit('added', instance);
        }
      }

      function tableDeleteLines(ids: string[]) {
        return tableRef.value!.removeRowsById(ids);
      }

      /** Bắt đầu thêm dữ liệu hàng loạt, dữ liệu thêm trong khoảng thời gian này sẽ không phản hồi cho component cha để đồng bộ */
      function addBatchBegin() {
        addBatching.value = true;
        loading.value = true;
      }

      /** Kết thúc thêm dữ liệu hàng loạt, sẽ phản hồi ngay cho component cha để đồng bộ dữ liệu */
      function addBatchEnd() {
        addBatching.value = false;
        loading.value = false;
        emit('added', instance);
      }

      //update-begin-author:taoyan date:2022-5-26 for: issues/I56ATQ OnlineChức năng mã, khi đồng bộ cơ sở dữ liệu liên kho thì báo lỗi (kho đơn lẻ bình thường):org.jeecg.modules.online.cgform.c.a:492
      function validateExistIndex({ cellValue, row }, callback) {
        const { tables } = setup;
        if (tables) {
          let dataSource = tables.dbTable.value!.tableRef.dataSource;
          //Lấy tabledữ liệu gốc của
          let temp = dataSource.filter((item) => item.id === row.id);
          if (!temp || temp.length <= 0) {
            callback(true);
          }
          let dbFieldName = temp[0]['dbFieldName'];
          if (dbFieldName == cellValue) {
            callback(true);
          }
          //Lấy dữ liệu chỉ mục
          let arr = tables.idxTable.value!.tableRef.getTableData();
          for (let item of arr) {
            let indexField = item.indexField;
            //Trường chỉ mục có thể là nhiều trường
            let indexFieldArray = indexField.split(',');
            if (indexFieldArray.indexOf(dbFieldName) >= 0) {
              callback(false, 'Trường hiện tại tồn tại cấu hình chỉ mục, vui lòng xóa chỉ mục trước rồi sửa trường');
            }
          }
        }
        callback(true);
      }
      //update-end-author:taoyan date:2022-5-26 for: issues/I56ATQ OnlineChức năng mã, khi đồng bộ cơ sở dữ liệu liên kho thì báo lỗi (kho đơn lẻ bình thường):org.jeecg.modules.online.cgform.c.a:492
      // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-278】Đã thiết lập số thập phân thì không thể thiết lậpintegerLoại
      function validateFieldDbType({ cellValue, row }, callback) {
        if (row.dbType == 'int' && row.dbPointLength > 0) {
          callback(false, 'Đã thiết lập dấu thập phân thì không thể thiết lậpintegerLoại');
        } else if (row.dbType == 'long' && row.dbPointLength > 0 ) {
          // update-begin--author:liaozhiyang---date:20240522---for：【LHZP-280】Đã thiết lập số thập phân thì không thể thiết lậpLongLoại
          callback(false, 'Đã thiết lập dấu thập phân thì không thể thiết lậpLongLoại');
          // update-end--author:liaozhiyang---date:20240522---for：【LHZP-280】Đã thiết lập số thập phân thì không thể thiết lậpLongLoại
        }
        callback(true);
      }
      // update-end--author:liaozhiyang---date:20240522---for：【TV360X-278】Đã thiết lập số thập phân thì không thể thiết lậpintegerLoại
      // update-begin--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
      const openAiModal = () => {
        aiModalShow.value = true;
        setTimeout(() => {
          openModal(true);
        }, 0);
      };
      // update-end--author:liaozhiyang---date:20250407---for：【QQYUN-11801】aiTrường tạo bảng
      return {
        tableRef,
        loading,
        columns,
        dataSource,
        setDataSource,
        addBatchBegin,
        addBatchEnd,
        tableAddLine,
        tableHeight,
        tableProps,
        tableDeleteLines,
        handleAdded,
        handleRemoved,
        handleDragged,
        handleInserted,
        handleValueChange,
        handleDisabledDbFieldName,
        validateData,
        getRemoveIds,
        validateExistIndex,
        openAiModal,
        registerAiModal,
        aiModalShow,
        handleGenerate,
        addBpmStatusField,
        hasBpmStatusField,
      };
    },
  });
</script>
<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-212】Điều chỉnh kiểu dáng kiểm chứng tên trường
  .dBAttributeTable {
    :deep(table) {
      tr {
        td:nth-child(4) {
          .vxe-cell--valid-error-msg {
            white-space: nowrap;
          }
        }
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240521---for：【TV360X-212】Điều chỉnh kiểu dáng kiểm chứng tên trường
</style>
