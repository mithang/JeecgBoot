<template>
  <!-- Danh sách -->
  <BasicTable
    v-if="!tableReloading && (loading || columns.length)"
    ref="onlineTable"
    rowKey="jeecg_row_key"
    :canResize="true"
    :bordered="true"
    :showIndexColumn="false"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :pagination="false"
    :showActionColumn="false"
    :showTableSetting="false"
    :clickToRowSelect="false"
    :scroll="tableScroll"
    @table-redo="reload"
    :class="{ 'j-table-force-nowrap': enableScrollBar }"
    @change="handleChangeInTable"
  >
    <template #fileSlot="{ text, record, column }">
      <span v-if="!text" style="font-size: 12px; font-style: italic">Không có tệp</span>
      <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadRowFile(text, record, column, ID)">
        Tải xuống
      </a-button>
    </template>

    <template #imgSlot="{ text }">
      <span v-if="!text" style="font-size: 12px; font-style: italic">Không có hình ảnh</span>
      <img v-else :src="getImgView(text)" alt="Hình ảnh không tồn tại" class="online-cell-image" @click="viewOnlineCellImage(text)" />
    </template>

    <template #htmlSlot="{ text, column, record }">
      <!-- update-begin--author:liaozhiyang---date:20240517---for：【TV360X-129】Bổ sung cấu hình control văn bản giàu (rich text)hrefChuyển hướng (nhảy trang) -->
      <template v-if="column.fieldHref">
        <a v-html="sanitizeRichText(text)" @click="handleClickFieldHref(column.fieldHref, record)"></a>
      </template>
      <div v-else v-html="sanitizeRichText(text)"></div>
      <!-- update-end--author:liaozhiyang---date:20240517---for：【TV360X-129】Bổ sung cấu hình control văn bản giàu (rich text)hrefChuyển hướng (nhảy trang) -->
    </template>

    <template #pcaSlot="{ text, column }">
      <div :title="getPcaText(text, column)">{{ getPcaText(text, column) }}</div>
    </template>

    <template #dateSlot="{ text, column }">
      <span>{{ getFormatDate(text, column) }}</span>
    </template>
  </BasicTable>

  <!-- update-begin--author:liaozhiyang---date:20260810---for:Khi tất cả trường của bảng con bị ẩn, hiển thị trạng thái trống thân thiện -->
  <div v-else-if="!tableReloading" class="inner-sub-table-empty">
    <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" description="Tạm thời không có nội dung để hiển thị" />
  </div>
  <!-- update-end--author:liaozhiyang---date:20260810---for:Khi tất cả trường của bảng con bị ẩn, hiển thị trạng thái trống thân thiện -->

  <!-- Popup chi tiết -->
  <online-detail-modal :id="ID" @register="registerDetailModal" :source="INNER_TABLE"/>

  <!-- Nhập (import) -->
  <JImportModal @register="registerImportModal" :url="importUrl()" @ok="reload" online></JImportModal>

  <!-- Chuyển hướng (nhảy trang)Hrefphương thức component động của -->
  <a-modal v-bind="hrefComponent.model" v-on="hrefComponent.on">
    <component :is="hrefComponent.is" v-bind="hrefComponent.params" />
  </a-modal>

  <!-- Popup tùy chỉnh -->
  <online-custom-modal @register="registerCustomModal" @success="reload" />

  <!-- Popup cấp chohrefdùng cho một biểu mẫu khác-Biểu mẫu chi tiết -->
  <online-detail-modal :id="hrefMainTableId" @register="registerOnlineHrefModal" :defaultFullscreen="false" :source="INNER_TABLE" />

  <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa-Trường của bản ghi liên kết có thể mở trên danh sáchmodalChỉnh sửa dữ liệu -->
  <online-pop-modal ref="onlinePopModalRef" :id="popTableId" @register="registerPopModal" @success="reload" request topTip></online-pop-modal>

  <!-- Xem sơ đồ quy trìnhmodal -->
  <BpmGraphicModal @register="registerBpmModal"></BpmGraphicModal>
</template>

<script setup name="OnlCgformInnerSubTable">
  import { ref, watch } from 'vue';
  import { Empty } from 'ant-design-vue';
  import { BasicTable } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OnlineCustomModal from '../default/OnlineCustomModal.vue';
  import OnlineDetailModal from '../default/OnlineDetailModal.vue';
  import JImportModal from '/@/components/Form/src/jeecg/components/JImportModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { useOnlineListPopEvent } from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from '../comp/OnlinePopModal.vue';
  import { INNER_TABLE } from '../../util/constant';
  import { ACTION_COLUMN_FLAG } from '/@/components/Table/src/const';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';

  /**
    1.Biểu mẫu con nhúng không có phân trang
    2.Biểu mẫu con nhúng không có vùng truy vấn
    3.Biểu mẫu con nhúng không có các nút như xuất, nhập
    4.Truy vấn luôn luôn là dữ liệu bảng chínhid(khóa ngoại không có tác dụng)
    5.Biểu mẫu con nhúng không có thêm mới và chỉnh sửa, không có cột thao tác
  */
  const props = defineProps(['subTableId', 'subTableName', 'mTableSelectedRcordId', 'subTableSuperQuery']);
  const { createMessage: $message } = useMessage();
  // Hàng mã này nên được chạy mỗi lần vào route mới, bất kể route đó có bị lưu bộ nhớ đệm hay không--
  const {
    ID,
    onlineTableContext,
    loading,
    reload,
    dataSource,
    handleSpecialConfig,
    getColumnList,
    handleChangeInTable,
    loadData,
    onlineExtConfigJson,
    registerCustomModal,
    tableReloading,
  } = useOnlineTableContext({ code: props.subTableId });
  onlineTableContext['isInnerSubTable'] = true;
  onlineTableContext['innerSubTableName'] = props.subTableName;
  onlineTableContext['innerSubTableId'] = ID.value;
  onlineTableContext['mTableSelectedRcordId'] = props.mTableSelectedRcordId;

  // Phán đoán NếuIDkhông tồn tại thì chấm dứt logic tiếp theo
  if (!ID.value) {
    $message.warning('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
  }
  // Xử lý phần tăng cường
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // Xử lý danh sáchbutton
  const {
    importUrl,
    registerImportModal,
    initButtonList,
    initButtonSwitch,
    registerDetailModal,
    registerBpmModal,
  } = useListButton(onlineTableContext, onlineExtConfigJson);

  
  // Xử lý BasicTable cấu hình của
  const {
    columns,
    enableScrollBar,
    tableScroll,
    downloadRowFile,
    getImgView,
    getPcaText,
    getFormatDate,
    handleColumnResult,
    hrefComponent,
    viewOnlineCellImage,
    hrefMainTableId,
    registerOnlineHrefModal,
    registerPopModal,
    openPopModal,
    onlinePopModalRef,
    popTableId,
    handleClickFieldHref,
  } = useTableColumns(onlineTableContext, onlineExtConfigJson);
  // Lắng nghe cấu hình biểu mẫuID và Điều kiện truy vấn nâng cao của bảng con
  // Lần đầu vào, kéo cấu hình cột + Dữ liệu, khi điều kiện lọc tiếp theo thay đổi chỉ làm lại loadData
  let subTableInited = false;
  watch(
    [ID, () => props.subTableSuperQuery],
    () => {
      console.log('watched id or subTableSuperQuery is change...');
      // Hợp nhất điều kiện truy vấn nâng cao của bảng con tách ra từ bảng chính vào bảng con context
      if (props.subTableSuperQuery) {
        onlineTableContext['superQuery'] = props.subTableSuperQuery;
      }
      if (!subTableInited) {
        subTableInited = true;
        initAutoList();
      } else {
        loadData();
      }
    },
    { immediate: true, deep: true }
  );
  /**Tải lạionlineCấu hình*/
  async function initAutoList() {
    loading.value = true;
    // 1.Thông tin cấu hình cột
    let columnResult = await getColumnList(INNER_TABLE);
    handleTableConfig(columnResult);
    // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9340】Dữ liệu bảng con nhúng đều được truy vấn ra hết
    if (columnResult.foreignKeys?.length) {
      onlineTableContext['innerSubTableFk'] = columnResult.foreignKeys[0].field;
    }
    // update-end--author:liaozhiyang---date:20240514---for：【QQYUN-9340】Dữ liệu bảng con nhúng đều được truy vấn ra hết
    // 2.Tải dữ liệu
    await loadData();
    loading.value = false;
    // 3.Thực thijsTăng cường setup
    onlineTableContext.execButtonEnhance('setup');
  }

  // Chuyển kết quả truy vấn thành tableCấu hình cần cho việc render
  function handleTableConfig(result) {
    // jsKhởi tạo phần tăng cường
    let EnhanceJS = initCgEnhanceJs(result.enhanceJs);
    onlineTableContext['EnhanceJS'] = EnhanceJS;
    // Thiết lập nút tùy chỉnh
    initButtonList(result.cgButtonList);
    // Thiết lập trạng thái hiển thị/ẩn của nút trang
    initButtonSwitch(result.hideColumns);
    // cấu hình cột
    handleColumnResult(result);
    // Cấu hình bảng
    handleSpecialConfig(result);
  }

  /**
   * listMở trang Popup biểu mẫu khác
   * @param params
   */
  function openOnlinePopModal(params) {
    console.log('openOnlinePopModal', params);
    popTableId.value = params.id;
    let data = {
      title: params.describe,
    };
    if (params.record && params.record.id) {
      data['record'] = params.record;
      data['isUpdate'] = true;
    }
    openPopModal(true, data);
  }
  //Liên kết sự kiện popup
  useOnlineListPopEvent(openOnlinePopModal);
</script>

<style lang="less">
  /** [Kiểu dáng chủ đề bảng một] Bảng ép cột không xuống dòng */
  .j-table-force-nowrap {
    td,
    th {
      white-space: nowrap;
    }

    .ant-table-selection-column {
      padding: 12px 22px !important;
    }

    /** Cột tự thích ứng, nhược điểm là sẽ khiến độ rộng cột mất hiệu lực */
    &.ant-table-wrapper .ant-table-content {
      overflow-x: auto;
    }
  }
  .online-cell-image {
    height: 25px !important;
    margin: 0 auto;
    max-width: 80px;
    font-size: 12px;
    font-style: italic;
    cursor: pointer;
  }
  // update-begin--author:liaozhiyang---date:20260810---for:Khi tất cả trường của bảng con bị ẩn, hiển thị trạng thái trống thân thiện
  .inner-sub-table-empty {
    display: flex;
    min-height: 100px;
    align-items: center;
    justify-content: center;
    border: 1px dashed #d9d9d9;
    border-radius: 2px;
    background: #fafafa;
  }
  // update-end--author:liaozhiyang---date:20260810---for:Khi tất cả trường của bảng con bị ẩn, hiển thị trạng thái trống thân thiện
</style>
