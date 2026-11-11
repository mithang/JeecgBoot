<template>
  <JVxeTable
    ref="tableRef"
    row-class-name="online-config-page"
    rowNumber
    keyboardEdit
    :maxHeight="tableHeight.noToolbar"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :disabledRows="{ dbFieldName: ['id', 'has_child'] }"
    @valueChange="handleValueChange"
    v-bind="tableProps" >
    <!-- Phần định nghĩa slot bắt đầu -->
    <template #dbFieldNameSlot="props">
      <span class="field-name-cell">
        <span>{{ props.value }}</span>
        <a-tooltip v-if="isTitleFieldRow(props.row)" title="Đã thiết lập làm trường tiêu đề">
          <a-tag color="blue" class="title-field-tag">T</a-tag>
        </a-tooltip>
      </span>
    </template>
    <template #fieldConfig="props">
      <a-space v-if="showConfigButton(props)">
        <a-button type="primary" size="small" ghost @click="openConfig(props)">Cấu hình</a-button>
        <a-button size="small" @click="openJsonConfig(props)">JSON</a-button>
      </a-space>
    </template>
    <!--update-begin-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột-->
    <template #dictConfig="props">
      <div class="dict-config-cell">
        <a-input
          v-if="!props.row.dictTable"
          class="dict-config-input"
          :value="props.value"
          :placeholder="getDictInputPlaceholder(props.row)"
          allowClear
          @change="(e) => props.triggerChange(e.target.value)"
        />
        <a-tooltip v-else :title="getDictSummary(props.row)" placement="topLeft">
          <div class="dict-config-summary" @click="openDictConfig(props)">
            {{ getDictSummary(props.row) }}
          </div>
        </a-tooltip>
        <a-tooltip :title="props.row.dictTable ? 'Chỉnh sửa cấu hình từ điển' : 'Cấu hình nâng cao (từ điển bảng/Popup/Cây tùy chỉnh/liên động)'">
          <a-button class="dict-config-btn" size="small" type="text" @click="openDictConfig(props)">
            <Icon :icon="props.row.dictTable ? 'ant-design:edit-outlined' : 'mdi:book-cog-outline'" />
          </a-button>
        </a-tooltip>
      </div>
    </template>
    <!--update-end-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột-->
  </JVxeTable>
  
  <!-- Cấu hình popup bản ghi liên kết -->
  <link-table-config-modal @register="registerModal" @success="handleConfigData"/>
  
  <!-- Cấu hình popup của trường bảng khác -->
  <link-table-field-config-modal :isSubTableOneToMany="isSubTableOneToMany" @register="registerFieldModal" @success="handleFieldConfigData"/>

  <!-- Cấu hình popup tham số mở rộng -->
  <FieldExtendJsonModal @register="registerExtJsonModal" @success="handleExtJson"/>

  <!-- Tham số mở rộngJSONPopup chỉnh sửa trực tiếp -->
  <a-modal v-model:open="jsonModalVisible" title="Tham số mở rộng" okText="Xác nhận" cancelText="Hủy" :width="600" @ok="handleJsonSubmit">
    <div class="p-4">
      <a-textarea v-model:value="currentJsonStr" :rows="12" placeholder="Vui lòng nhập tham số mở rộngJSON" />
    </div>
  </a-modal>
  <!-- Popup cấu hình từ điển -->
  <DictConfigModal @register="registerDictConfigModal" @success="handleDictConfig" />
</template>

<script lang="ts">
  import { ref, defineComponent } from 'vue';
  import { JVxeTypes, JVxeColumn } from '/@/components/jeecg/JVxeTable/types';
  import { useTableSync } from '../../hooks/useTableSync';
  import LinkTableConfigModal from '../../extend/linkTable/LinkTableConfigModal.vue'
  import LinkTableFieldConfigModal from '../../extend/linkTable/LinkTableFieldConfigModal.vue'
  import FieldExtendJsonModal from '../../extend/FieldExtendJsonModal.vue'
  import DictConfigModal from '../../extend/DictConfigModal.vue'
  import { Icon } from '/@/components/Icon';
  import { useModal } from '/@/components/Modal';

  const commonPageOptions = [
    { title: 'Ô văn bản', value: 'text', group: 'Nhập liệu cơ bản' },
    { title: 'Ô dropdown', value: 'list', group: 'Loại lựa chọn' },
    { title: 'Ô dropdown chọn nhiều', value: 'list_multi', group: 'Loại lựa chọn' },
    { title: 'Ô tìm kiếm dropdown', value: 'sel_search', group: 'Loại lựa chọn' },
    { title: 'Ô chọn đơn (radio)', value: 'radio', group: 'Loại lựa chọn' },
    { title: 'Ô chọn nhiều (checkbox)', value: 'checkbox', group: 'Loại lựa chọn' },
    { title: 'Công tắc', value: 'switch', group: 'Loại lựa chọn' },
    { title: 'Ngày(Năm-tháng-ngày)', value: 'date', group: 'Ngày giờ' },
    { title: 'Ngày(Năm-tháng-ngày giờ-phút-giây)', value: 'datetime', group: 'Ngày giờ' },
    { title: 'Thời gian(giờ-phút-giây)', value: 'time', group: 'Ngày giờ' },
    { title: 'Tệp', value: 'file', group: 'Tệp/Hình ảnh' },
    { title: 'Hình ảnh', value: 'image', group: 'Tệp/Hình ảnh' },
    { title: 'PopupPopup', value: 'popup', group: 'Popup/Popup' },
    // update-begin--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
    { title: 'Popuptừ điển', value: 'popup_dict', group: 'Popup/Popup' },
    // update-end--author:liaozhiyang---date:20240130---for：【QQYUN-7961】popupDicttừ điển
    { title: 'Cây từ điển phân loại', value: 'cat_tree', group: 'Dạng cây/Liên động/Khu vực' },
    { title: 'Control cây tùy chỉnh', value: 'sel_tree', group: 'Dạng cây/Liên động/Khu vực' },
    { title: 'Component tỉnh/thành/quận huyện', value: 'pca', group: 'Dạng cây/Liên động/Khu vực' },
    { title: 'Component liên động', value: 'link_down', group: 'Dạng cây/Liên động/Khu vực' },
    { title: 'Lựa chọn của người dùng', value: 'sel_user', group: 'Nhân viên/Phòng ban' },
    { title: 'Chọn phòng ban', value: 'sel_depart', group: 'Nhân viên/Phòng ban' },
    { title: 'bản ghi liên kết', value: 'link_table', group: 'bản ghi liên kết' },
    { title: 'trường bảng khác', value: 'link_table_field', group: 'bản ghi liên kết' },
    { title: 'Mật khẩu', value: 'password', group: 'Ít dùng' },
    { title: 'Văn bản nhiều dòng', value: 'textarea', group: 'Ít dùng' },
    { title: 'Văn bản giàu (rich text)', value: 'umeditor', group: 'Ít dùng' },
    { title: 'MarkDown', value: 'markdown', group: 'Ít dùng' },
  ];
  const subTablePageOptions = [
    { title: 'Ô văn bản', value: 'text', group: 'Nhập liệu cơ bản' },
    { title: 'Ô dropdown', value: 'list', group: 'Loại lựa chọn' },
    { title: 'Ô dropdown chọn nhiều', value: 'list_multi', group: 'Loại lựa chọn' },
    { title: 'Ô tìm kiếm dropdown', value: 'sel_search', group: 'Loại lựa chọn' },
    { title: 'Ô chọn đơn (radio)', value: 'radio', group: 'Loại lựa chọn' },
    { title: 'Công tắc', value: 'switch', group: 'Loại lựa chọn' },
    { title: 'Ngày(Năm-tháng-ngày)', value: 'date', group: 'Ngày giờ' },
    { title: 'Ngày(Năm-tháng-ngày giờ-phút-giây)', value: 'datetime', group: 'Ngày giờ' },
    { title: 'Thời gian(giờ-phút-giây)', value: 'time', group: 'Ngày giờ' },
    { title: 'Tệp', value: 'file', group: 'Tệp/Hình ảnh' },
    { title: 'Hình ảnh', value: 'image', group: 'Tệp/Hình ảnh' },
    { title: 'PopupPopup', value: 'popup', group: 'Popup/Popup' },
    // update-begin--author:liaozhiyang---date:20260507---for：【issues/7617】vxetableBảng con hỗ trợpopuptừ điển
    { title: 'Popuptừ điển', value: 'popup_dict', group: 'Popup/Popup' },
    // update-end--author:liaozhiyang---date:20260507---for：【issues/7617】vxetableBảng con hỗ trợpopuptừ điển
    { title: 'Cây từ điển phân loại', value: 'cat_tree', group: 'Dạng cây/Khu vực' },
    { title: 'Control cây tùy chỉnh', value: 'sel_tree', group: 'Dạng cây/Khu vực' },
    { title: 'Component tỉnh/thành/quận huyện', value: 'pca', group: 'Dạng cây/Khu vực' },
    { title: 'Lựa chọn của người dùng', value: 'sel_user', group: 'Nhân viên/Phòng ban' },
    { title: 'Chọn phòng ban', value: 'sel_depart', group: 'Nhân viên/Phòng ban' },
    { title: 'bản ghi liên kết', value: 'link_table', group: 'bản ghi liên kết' },
    { title: 'trường bảng khác', value: 'link_table_field', group: 'bản ghi liên kết' },
    { title: 'Văn bản nhiều dòng', value: 'textarea', group: 'Ít dùng' },
  ];

  export default defineComponent({
    name: 'PageAttributeTable',
    components: {
      LinkTableConfigModal,
      LinkTableFieldConfigModal,
      FieldExtendJsonModal,
      DictConfigModal,
      Icon,
    },
    setup() {
      const columns = ref<JVxeColumn[]>([
        { title: 'Tên trường', key: 'dbFieldName', width: 120, fixed: 'left', type: JVxeTypes.slot, slotName: 'dbFieldNameSlot' },
        { title: 'Ghi chú trường', key: 'dbFieldTxt', width: 120, fixed: 'left' },
        {
          title: 'Hiển thị biểu mẫu',
          key: 'isShowForm',
          width: 68,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: true,
        },
        {
          title: 'Hiển thị cột',
          key: 'isShowList',
          width: 55,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: true,
        },
        {
          title: 'Sắp xếp',
          key: 'sortFlag',
          width: 60,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
          // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-291】Chưa tích chọn đồng bộ cơ sở dữ liệu thì vô hiệu hóa chức năng sắp xếp
          props: {
            isDisabledCell({ row, column }) {
              let { dbTable } = tables;
              // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
              const dbRowData = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              return dbRowData?.dbIsPersist == '0' ? true : false;
            },
          },
          // update-end--author:liaozhiyang---date:20240528---for：【TV360X-291】Chưa tích chọn đồng bộ cơ sở dữ liệu thì vô hiệu hóa chức năng sắp xếp
        },
        {
          title: 'Chỉ đọc',
          key: 'isReadOnly',
          width: 60,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
        },
        {
          title: 'Loại control',
          key: 'fieldShowType',
          width: 170,
          type: JVxeTypes.select,
          options: commonPageOptions,
          allowSearch: true,
          defaultValue: 'text',
          placeholder: 'Vui lòng chọn${title}',
          validateRules: [{ required: true, message: 'Vui lòng chọn${title}' }, { handler: validateFieldShowType }],
        },
        {
          title: 'Giá trị mặc định của control',
          key: 'fieldDefaultValue',
          width: 140,
          type: JVxeTypes.textarea,
          props: {
            allowClear: true,
          },
          defaultValue: '',
        },
        {
          title: 'Độ dài',
          key: 'fieldLength',
          width: 90,
          titleHelp: { message: 'Độ dài của control này chỉ có hiệu lực với độ rộng trường cột của bảng con!'},
          type: JVxeTypes.inputNumber,
          defaultValue: 120,
          placeholder: 'Vui lòng nhập độ dài control',
        },
        {
          title: 'Truy vấn',
          key: 'isQuery',
          width: 60,
          type: JVxeTypes.checkbox,
          align: 'center',
          customValue: ['1', '0'],
          defaultChecked: false,
          // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-423】Chưa tích chọn đồng bộ cơ sở dữ liệu thì vô hiệu hóa truy vấn
          props: {
            isDisabledCell({ row, column }) {
              let { dbTable } = tables;
              // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
              const dbRowData = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
              if (dbRowData?.dbType === 'Blob') {
                return true;
              }
              // update-end--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
              // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-489】Trường bảng khác tắt truy vấn
              if (row.fieldShowType === 'link_table_field') {
                return true;
              }
              // update-end--author:liaozhiyang---date:20260714---for：【LHZP-489】Trường bảng khác tắt truy vấn
              return dbRowData?.dbIsPersist == '0' ? true : false;
            },
          },
          // update-end--author:liaozhiyang---date:20240528---for：【TV360X-423】Chưa tích chọn đồng bộ cơ sở dữ liệu thì vô hiệu hóa truy vấn
        },
        {
          title: 'Loại truy vấn',
          key: 'queryMode',
          width: 120,
          type: JVxeTypes.select,
          options: [
            { title: 'Truy vấn thông thường', value: 'single' },
            { title: 'Truy vấn mờ', value: 'like' },
            { title: 'Truy vấn theo phạm vi', value: 'group' },
          ],
          defaultValue: 'single',
          placeholder: 'Vui lòng chọn${title}',
          validateRules: [{ handler: validateQueryMode }],
          props: {
            isDisabledCell({ row, column }) {
              // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-222】Truy vấn theo phạm vi chỉ hỗ trợ loại số và loại ngày
              // let { dbTable } = tables;
              // // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
              // const dbRowData = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
              // if (['double', 'int', 'BigDecimal', 'Date', 'Datetime'].includes(dbRowData?.dbType)) {
              //   return false;
              // }
              // return true;
              // update-end--author:liaozhiyang---date:20240717---for：【TV360X-222】Truy vấn theo phạm vi chỉ hỗ trợ loại số và loại ngày
            },
          },
        },
        // update-begin-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột
        {
          title: 'Mã từ điển',
          key: 'dictField',
          width: 210,
          type: JVxeTypes.slot,
          slotName: 'dictConfig',
          allowInput: true,
          defaultValue: '',
          validateRules: [{ handler: validateDictFieldOrDictText }],
        },
        // Cột ẩn:dictTable và dictText vẫn được lưu trữ dưới dạng dữ liệu tầng dưới, ghi vào thông qua popup hoặc logic đồng bộ
        {
          title: 'từ điểnTable',
          key: 'dictTable',
          type: JVxeTypes.hidden,
          defaultValue: '',
        },
        {
          title: 'từ điểnText',
          key: 'dictText',
          type: JVxeTypes.hidden,
          defaultValue: '',
        },
        // update-end-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột
        {
          title: 'Tham số mở rộng',
          key: 'fieldExtendJson',
          type: JVxeTypes.hidden,
          defaultValue: '',
        },
        {
          title: 'Thêm cấu hình',
          minWidth: 150,
          key: 'fieldConfig',
          type: JVxeTypes.slot,
          slotName: 'fieldConfig',
        },
      ]);
      const setup = useTableSync(columns);
      const { tableRef, tables } = setup;

      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi thêm hàng thuộc tính trang, thiết lập loại truy vấn mặc định)
      /** Khi đồng bộ trường của cơ sở dữ liệu, chỉ áp dụng cho trường mới thêm String Ô văn bản thiết lập giá trị mặc định truy vấn mờ. */
      async function syncTable(dbTable) {
        const existingIds = new Set(tableRef.value!.getTableData().map((row) => row.id));
        await setup.syncTable(dbTable);
        const values = tableRef.value!
          .getTableData()
          .filter((row) => !existingIds.has(row.id) && row.dbType === 'string' && row.fieldShowType === 'text')
          .map((row) => ({ rowKey: row.id, values: { queryMode: 'like' } }));
        if (values.length > 0) {
          tableRef.value!.setValues(values);
        }
      }
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi thêm hàng thuộc tính trang, thiết lập loại truy vấn mặc định)

      function validateFieldShowType({ cellValue, row }, callback) {
        let { dbTable } = tables;
        // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
        let dbType = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0].dbType;
        //Khi loại control là thời gian, loại cơ sở dữ liệu phải làStringLoại
        if (cellValue === 'time' && dbType !== 'string') {
          callback(false, 'Khi loại control là thời gian,Loại trường trong thuộc tính cơ sở dữ liệu phải làString！');
        }
        // Khi loại control là ngày, loại cơ sở dữ liệu phải làDateLoại
        else if (cellValue === 'date' && dbType !== 'Date' && dbType !== 'Datetime') {
          callback(false, 'Khi loại control là ngày, loại trường trong thuộc tính cơ sở dữ liệu phải làDatehoặcDatetime！');
        }
        // Khi loại control là yyyy-MM-dd HH:mm:ss khi đó, loại cơ sở dữ liệu phải làDatetimeLoại
        else if (cellValue === 'datetime' && dbType !== 'Datetime') {
          callback(false, 'Khi loại control làdatetimekhi đó, loại trường trong thuộc tính cơ sở dữ liệu phải làDatetime！');
        } else {
          callback(true);
        }
      }

      /** Đồng bộ loại control */
      function syncFieldShowType(row) {
        // update-begin--author:liaozhiyang---date:20240801---for：【TV360X-1963】Khi giá trị cũ của loại trường là loại thời gian, chọn lại chỉ cần không phải loại thời gian thì loại control sẽ đặt lại về ô văn bản, tất cả đều đặt lại thành ô nhập liệu
        let showType = 'text';
        if (row.dbType === 'Datetime') {
          showType = 'datetime';
        } else if (row.dbType === 'Date') {
          showType = 'date';
        }
        // update-end--author:liaozhiyang---date:20240801---for：【TV360X-1963】Khi giá trị cũ của loại trường là loại thời gian, chọn lại chỉ cần không phải loại thời gian thì loại control sẽ đặt lại về ô văn bản, tất cả đều đặt lại thành ô nhập liệu
        // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
        // BlobLoại không hỗ trợ truy vấn, bỏ tích chọn truy vấn và sắp xếp
        const values: Recordable = { fieldShowType: showType };
        if (row.dbType === 'Blob') {
          values.isQuery = '0';
          values.sortFlag = '0';
        }
        tableRef.value!.setValues([
          {
            rowKey: row.id,
            values,
          },
        ]);
        // update-end--author:liaozhiyang---date:20260713---for：【LHZP-516】blobTrường loại không cho tích chọn truy vấn
      }
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng
      function syncIsQuery(row) {
        if (row.dbIsPersist === '0') {
          tableRef.value!.setValues([
            {
              rowKey: row.id,
              values: { isQuery: '0', sortFlag: '0' },
            },
          ]);
        }
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng

      const isSubTableOneToMany = ref(false);
      const isSubTable = ref(false);
      function changePageType(flag, relationType = 0) {
        isSubTableOneToMany.value = flag && relationType == 0;
        isSubTable.value = !!flag;
        for (let col of columns.value) {
          if (col.key == 'fieldShowType') {
            // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-580】Loại control của bảng con một-một hỗ trợ giống loại control của bảng chính
            col.options = !flag ? commonPageOptions : relationType == 1 ? commonPageOptions : subTablePageOptions;
            // update-end--author:liaozhiyang---date:20260715---for：【LHZP-580】Loại control của bảng con một-một hỗ trợ giống loại control của bảng chính
            break;
          }
        }
      }

      function enableQuery(id) {
        tableRef.value!.setValues([
          {
            rowKey: id,
            values: { isQuery: '1' },
          },
        ]);
      }

      //update-begin-author:taoyan date:2022-8-11 for: VUEN-1940【online】Bản ghi liên kết và trường bảng khác, cấu hình thông minh hóa
      //Đăng kýmodal
      const [registerModal, { openModal }] = useModal();
      const [registerFieldModal, { openModal: openFieldModal }] = useModal();
      const [registerExtJsonModal, { openModal: openExtJsonModal }] = useModal();
      const [registerDictConfigModal, { openModal: openDictConfigModal }] = useModal();
      //Có hiển thị nút cấu hình cá nhân hóa hay không
      function showConfigButton(props){
        if(props.row.dbFieldName=='id'){
          return false
        }else{
          return true;
        }
      }

      /**
       * Căn cứ vào loại khác nhau, mở cấu hình khác nhau
       */
      function openConfig(props){
        if(props.row.fieldShowType.indexOf('link_table')>=0){
          showFieldConfig(props);
        }else{
          showExtJsonConfig(props);
        }
      }
      
      //Nhấp nút kích hoạt popup
      function showFieldConfig(props){
        let { row } = props;
        // update-begin--author:liaozhiyang---date:20260413---for:【QQYUN-9776】Từ điển đặt trong thuộc tính trangtabtrong
        if(props.row.fieldShowType == 'link_table'){
          let record = getLinkTableModalData(row);
          openModal(true, {
            record,
            fieldName: row.dbFieldName,
            isSubTableOneToMany: isSubTableOneToMany.value,
          })
        }else if(props.row.fieldShowType == 'link_table_field'){
          let data = getLinkTableFieldData(row);
          openFieldModal(true, data)
        }
        // update-end--author:liaozhiyang---date:20260413---for:【QQYUN-9776】Từ điển đặt trong thuộc tính trangtabtrong
      }
      
      // Lấy thông tin cấu hình của trường bảng khác
      function getLinkTableFieldData(row){
        let tableData = tableRef.value!.getTableData();
        let pageTableData = tableData.filter(item=>item.fieldShowType=='link_table');
        // Lấy tên bảng của bản ghi liên kết và thông tin trường liên quan
        let tableAndFieldsMap = {}
        if(pageTableData && pageTableData.length>0){
          for(let pageTableItem of pageTableData){
            tableAndFieldsMap[pageTableItem.dbFieldName] = {
              title: pageTableItem.dbFieldTxt,
              table: pageTableItem.dictTable,
              fields: pageTableItem.dictText
            }
          }
        }
        const { dictTable, dictText, id, dbFieldTxt} = row;
        let record = {
          rowKey: id,
          dbFieldTxt,
          dictText,
          dictTable,
        };
        return {
          record,
          tableAndFieldsMap
        }
      }
      
      //Lấy dữ liệu cấu hình của bản ghi liên kết
      function getLinkTableModalData(row){
        const { id, dbFieldTxt, fieldExtendJson, dictTable, dictText} = row;
        //dbFieldTxt  dictTable titleField otherFields multiSelect showType
        let result = {
          rowKey: id,
          dbFieldTxt,
          dictTable
        };
        if(dictText){
          let arr = dictText.split(',');
          result['titleField'] = arr[0];
          if(arr.length>1){
            result['otherFields'] = dictText.substring(dictText.indexOf(',')+1)
          }
        }else{
          result['titleField'] = ''
          result['otherFields'] = ''
        }
        if(fieldExtendJson){
          try{
            let json = JSON.parse(fieldExtendJson);
            if(json.multiSelect){
              result['multiSelect'] = json.multiSelect
            }else{
              result['multiSelect'] = false
            }
            // update-begin--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
            if (json.isListReadOnly) {
              result['isListReadOnly'] = json.isListReadOnly;
            } else {
              result['isListReadOnly'] = false;
            }
            // update-end--author:liaozhiyang---date:20250318---for：【issues/7930】Trong danh sách bảng, hỗ trợ cấu hình bản ghi liên kết có chỉ đọc hay không
            if(json.showType){
              result['showType'] = json.showType
            }else{
              result['showType'] = 'card'
            }
            if(json.imageField){
              result['imageField'] = json.imageField
            }else{
              result['imageField'] = ''
            }
          }catch (e) {
            console.error('Phân tích tham số mở rộng xảy ra lỗi', fieldExtendJson)
          }
        }
        return result;
      }
      
      // Cấu hình hoàn tất, điền dữ liệu trở lạivxe table
      function handleConfigData(record){
        //dbFieldTxt  dictTable titleField otherFields multiSelect showType
        //1.Thiết lập cấu hình mở rộng và thông tin từ điển
        const { multiSelect, showType, imageField, isListReadOnly } = record;
        let extJson = {showType, multiSelect, imageField, isListReadOnly};
        // update-begin--author:liaozhiyang---date:20260413---for:【QQYUN-9776】Từ điển đặt trong thuộc tính trangtabtrong
        let dictText = record.titleField;
        if(record.otherFields){
          dictText+=','+record.otherFields;
        }
        const {dictTable, dictField} = record;
        let values = [{ rowKey: record.rowKey, values: { fieldExtendJson: JSON.stringify(extJson), dbFieldTxt: record.dbFieldTxt, dictTable, dictField, dictText } }];
        // update-end--author:liaozhiyang---date:20260413---for:【QQYUN-9776】Từ điển đặt trong thuộc tính trangtabtrong
        tableRef.value!.setValues(values);
        
        let { dbTable } = tables;
        //2.Thiết lập mô tả trường
        if(dbTable){
          let dbTableValues = [{ rowKey: record.rowKey, values: { dbFieldTxt: record.dbFieldTxt } }];
          dbTable.value!.tableRef!.setValues(dbTableValues)
        }
      }

      // Cấu hình hoàn tất, điền dữ liệu trở lạivxe table
      function handleFieldConfigData(record){
        const {dbFieldTxt, dictTable, dictText, rowKey } = record;
        //1.Thiết lập mô tả trường và thông tin từ điển
        let values = [{ rowKey, values: { dbFieldTxt, dictTable, dictText } }];
        tableRef.value!.setValues(values);

        let { dbTable } = tables;
        //2.Thiết lập mô tả trường, hủy trạng thái tích chọn đồng bộ cơ sở dữ liệu
        if(dbTable){
          let tableValues = [{ rowKey, values: { dbFieldTxt, dbIsPersist:'0' } }];
          dbTable.value!.tableRef!.setValues(tableValues)
        }
      }

      /**
       * Popup cấu hình của tham số mở rộng
       * @param props
       */
      function showExtJsonConfig(props) {
        let jsonStr = props.row.fieldExtendJson || '';
        let id = props.rowId;
        let fieldShowType = props.row.fieldShowType || '';
        let sortFlag = props.row.sortFlag || '0';
        const dbType = props.row.dbType;
        openExtJsonModal(true, {
          jsonStr,
          fieldShowType,
          sortFlag,
          id,
          dbType,
          isSubTable: isSubTable.value,
        });
      }

      /**
       * của tham số mở rộng Callback của popup
       * @param props
       */
      function handleExtJson(data, rowKey){
        let values;
        if(data && Object.keys(data).length>0){
          //update-begin---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
          // Nếu đã thiết lập trường tiêu đề, xóa của các hàng khác isTitleField
          if (data.isTitleField === 1) {
            const allRows = tableRef.value!.getTableData();
            const clearValues = allRows
              .filter((row) => row.id !== rowKey && row.fieldExtendJson)
              .reduce((acc: any[], row) => {
                try {
                  const json = JSON.parse(row.fieldExtendJson);
                  if (json.isTitleField === 1) {
                    delete json.isTitleField;
                    acc.push({ rowKey: row.id, values: { fieldExtendJson: Object.keys(json).length > 0 ? JSON.stringify(json) : '' } });
                  }
                } catch (e) {}
                return acc;
              }, []);
            if (clearValues.length > 0) {
              tableRef.value!.setValues(clearValues);
            }
          }
          //update-end---author:liusq ---date:20260519  for：【QQYUN-13645】onlineBiểu mẫu cho phép thiết lập một trường nào đó là trường tiêu đề
          values = [{ rowKey, values: { fieldExtendJson: JSON.stringify(data) } }];
        }else{
          values = [{ rowKey, values: { fieldExtendJson: '' } }];
        }
        tableRef.value!.setValues(values);
      }

      // Phán đoán một hàng nào đó có phải là trường tiêu đề hay không
      function isTitleFieldRow(row) {
        if (!row?.fieldExtendJson) return false;
        try {
          const json = JSON.parse(row.fieldExtendJson);
          return json.isTitleField === 1;
        } catch (e) {
          return false;
        }
      }

      // Tham số mở rộngJSONChỉnh sửa trực tiếp
      const jsonModalVisible = ref(false);
      const currentJsonStr = ref('');
      const currentJsonRowKey = ref('');

      function openJsonConfig(props) {
        currentJsonStr.value = props.row.fieldExtendJson || '';
        currentJsonRowKey.value = props.rowId;
        jsonModalVisible.value = true;
      }

      function handleJsonSubmit() {
        let values = [{ rowKey: currentJsonRowKey.value, values: { fieldExtendJson: currentJsonStr.value } }];
        tableRef.value!.setValues(values);
        jsonModalVisible.value = false;
      }
      
      const handleValueChange = (event) => {
        console.log(event);
        // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-489】Trường bảng khác tắt truy vấn
        const { col, value, row } = event;
        if (col?.key === 'fieldShowType' && value === 'link_table_field') {
          tableRef.value!.setValues([
            {
              rowKey: row.id,
              values: { isQuery: '0', sortFlag: '0' },
            },
          ]);
        }
        // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi loại control thay đổi, đồng bộ loại truy vấn mặc định)
        if (col?.key === 'fieldShowType') {
          syncQueryMode({ row });
        }
        // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi loại control thay đổi, đồng bộ loại truy vấn mặc định)
        // update-end--author:liaozhiyang---date:20260714---for【LHZP-489】Trường bảng khác tắt truy vấn
      }
      /**
       * 2024-07-25
       * liaozhiyang
       * 【TV360X-1857】onlineTruy vấn thêm truy vấn mờ
       * */
      function validateQueryMode({ cellValue, row }, callback) {
        let { dbTable } = tables;
        // Lấy được dbTable(thuộc tính cơ sở dữ liệu) dữ liệu trong đó
        const dbType = dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0].dbType;
        if (cellValue === 'group') {
          // 【TV360X-1967】Truy vấn theo phạm vi chỉ hỗ trợ loại thời gian và loại số, đồng thời phải hỗ trợ loại control là thời gian(HH:mm:ss)
          if (['double', 'int', 'long', 'BigDecimal', 'Date', 'Datetime'].includes(dbType) || row.fieldShowType === 'time') {
            callback(true);
          } else {
            callback(false, 'Truy vấn theo phạm vi, chỉ hỗ trợ loại trường trong thuộc tính cơ sở dữ liệu là:Integer、Long、Double、BigDecimalvàDate、Datetime hoặc Loại control là thời gian(HH:mm:ss)');
          }
        } else if (cellValue === 'like') {
          if (['string'].includes(dbType) && row.fieldShowType === 'text') {
            callback(true);
          } else {
            callback(false, 'Truy vấn mờ, chỉ hỗ trợ loại control là ô văn bản và loại trường trong thuộc tính cơ sở dữ liệu làString');
          }
        } else {
          callback(true);
        }
      }
      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (thiết lập truy vấn mờ hoặc truy vấn thông thường theo loại trường và loại control)
      const syncQueryMode = ({ row }) => {
        const curTableRow = tableRef.value!.getTableData({ rowIds: [row.id] })[0];
        if (!curTableRow) {
          return;
        }
        const dbRow = tables.dbTable.value!.tableRef!.getTableData({ rowIds: [row.id] })[0];
        const queryMode = dbRow?.dbType === 'string' && curTableRow.fieldShowType === 'text' ? 'like' : 'single';
        if ((queryMode === 'like' && curTableRow.queryMode !== 'like') || (queryMode === 'single' && curTableRow.queryMode === 'like')) {
          tableRef.value!.setValues([{ rowKey: row.id, values: { queryMode } }]);
        }
      };
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (thiết lập truy vấn mờ hoặc truy vấn thông thường theo loại trường và loại control)

      /**
       * 2024-04-17
       * liaozhiyang
       * 【QQYUN-8972】popupTừ điển của component từ điểncodevà từ điểntextChỉ cho phép nhập một trường
       * */
      function validateDictFieldOrDictText({ cellValue, row }, callback) {
        if (row?.fieldShowType === 'popup_dict') {
          if (cellValue.indexOf(',') == -1) {
            callback(true);
          } else {
            callback(false, 'popupComponent từ điển chỉ cho phép điền một trường');
          }
        } else {
          callback(true);
        }
      }

      // update-begin-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột
      /** Tóm tắt cấu hình từ điển: từ điển bảng/Popup Hiển thị khi đã cấu hình table.code → text */
      function getDictSummary(row) {
        const table = row?.dictTable || '';
        const code = row?.dictField || '';
        const text = row?.dictText || '';
        if (!table) return '';
        const left = code ? `${table}.${code}` : table;
        return text ? `${left} → ${text}` : left;
      }

      /** Ô nhập liệu nội tuyến placeholder：popup_dict khi nhấn mạnh trường đơn */
      function getDictInputPlaceholder(row) {
        if (row?.fieldShowType === 'popup_dict') {
          return 'Nhấp vào phía bên phải để cấu hình từ điển bảng';
        }
        return 'Mã từ điển';
      }

      /** Mở popup cấu hình từ điển */
      function openDictConfig(props) {
        const { row, rowId } = props;
        openDictConfigModal(true, {
          rowKey: rowId,
          fieldShowType: row.fieldShowType,
          dictTable: row.dictTable || '',
          dictField: row.dictField || '',
          dictText: row.dictText || '',
        });
      }

      /** Popup điền ngược ba trường */
      function handleDictConfig(record) {
        const { rowKey, dictTable, dictField, dictText } = record;
        tableRef.value!.setValues([{ rowKey, values: { dictTable, dictField, dictText } }]);
      }
      // update-end-author:liaozhiyang---date:2026-05-20---for:【QQYUN-15464】Từ điểnTable/Code/Text Tổng hợp thành một cột

      return { ...setup, syncTable, columns, enableQuery, syncFieldShowType, changePageType,
        showConfigButton, showFieldConfig,
        registerExtJsonModal, handleExtJson, openConfig,
        registerModal, handleConfigData,
        registerFieldModal, handleFieldConfigData, syncIsQuery, handleValueChange, syncQueryMode, isSubTableOneToMany,
        jsonModalVisible, currentJsonStr, openJsonConfig, handleJsonSubmit, isSubTable,
        registerDictConfigModal, openDictConfig, handleDictConfig, getDictSummary, getDictInputPlaceholder,
        isTitleFieldRow,
      };
      //update-end-author:taoyan date:2022-8-11 for: VUEN-1940【online】Bản ghi liên kết và trường bảng khác, cấu hình thông minh hóa
      
    },
  });
</script>

<style lang="less" scoped>
  :deep(.online-config-page td:last-child.col--selected){
    box-shadow:none !important;
  }
  .field-name-cell {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    width: 100%;
  }
  .title-field-tag {
    font-size: 10px;
    line-height: 14px;
    padding: 0 4px;
    margin: 0;
    flex-shrink: 0;
  }
  //【QQYUN-8972】onlineẨn cái có thể chỉnh sửa phía trước độ dài controlicon
  :deep(.vxe-header--row) {
    .vxe-header--column:nth-child(9) {
      .vxe-cell--edit-icon {display:none;}
    }
  }
  .dict-config-cell {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 4px;
    .dict-config-input {
      flex: 1;
      min-width: 0;
    }
    // update-begin--author:liaozhiyang---date:20260831---for:【LHZP-157】Mã từ điển mặc định không xuất hiện đường viền
    > :deep(.dict-config-input.ant-input-affix-wrapper) {
      border-color: transparent;
      background-color: transparent;
      box-shadow: none;
      transition-property: border-color, box-shadow;
      &:hover {
        border-color: transparent;
        background-color: transparent;
      }
      .ant-input,
      .ant-input:hover {
        background-color: transparent;
      }
      &:focus-within {
        border-color: @primary-color;
        background-color: @component-background;
        box-shadow: 0 0 0 2px fade(@primary-color, 20%);
        .ant-input {
          background-color: @component-background;
        }
        .ant-input-clear-icon {
          visibility: visible;
        }
      }
      .ant-input-clear-icon {
        visibility: hidden;
      }
    }
    // update-end--author:liaozhiyang---date:20260831---for:【LHZP-157】Mã từ điển mặc định không xuất hiện đường viền
    .dict-config-summary {
      flex: 1;
      min-width: 0;
      padding: 0 8px;
      height: 28px;
      line-height: 28px;
      border: 1px dashed var(--ant-color-border, #d9d9d9);
      border-radius: 4px;
      background-color: var(--ant-color-fill-quaternary, #fafafa);
      color: var(--ant-color-text, rgba(0, 0, 0, 0.85));
      cursor: pointer;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      &:hover {
        border-color: var(--ant-color-primary, #1677ff);
        color: var(--ant-color-primary, #1677ff);
      }
    }
    .dict-config-btn {
      flex: 0 0 auto;
      padding: 0 6px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: rgba(0, 0, 0, 0.45);
      .app-iconify {
        color: inherit;
      }
      &:hover {
        color: @primary-color;
      }
    }
  }
</style>
