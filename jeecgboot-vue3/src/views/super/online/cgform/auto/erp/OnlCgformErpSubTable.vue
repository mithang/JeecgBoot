<template>

  <!-- Màn hình khung xương (skeleton) khi đang tải -->
  <a-skeleton v-if="tableReloading" active />

  <!-- Điều kiện truy vấn -->
  <online-query-form
      v-if="mainTableSelectedRowRcord"
      v-show="!tableReloading"
      ref="onlineQueryFormOuter"
      :id="ID"
      :queryBtnCfg="getQueryButtonCfg"
      :resetBtnCfg="getResetButtonCfg"
      @search="queryWithCondition"
  />

  <!-- Danh sách -->
  <BasicTable
    v-if="!tableReloading"
    ref="onlineTable"
    rowKey="jeecg_row_key"
    :canResize="false"
    :bordered="true"
    :showIndexColumn="false"
    :loading="loading"
    :columns="columns"
    :dataSource="dataSource"
    :pagination="pagination"
    :rowSelection="rowSelection"
    :actionColumn="actionColumn"
    :showTableSetting="true"
    :clickToRowSelect="false"
    :scroll="tableScroll"
    @table-redo="reload"
    :class="{ 'j-table-force-nowrap': enableScrollBar }"
    :tableSetting="tableSetting"
    @change="handleChangeInTable"
    :minHeight="300"
  >
    <template #tableTitle>
      <a-button
          v-if="btnShow && buttonSwitch.add && cgBIBtnMap['add'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['add'].buttonIcon"
          @click="handleAddRcord"
      >
        <span>{{cgBIBtnMap['add'].buttonName}}</span>
      </a-button>
      <a-button
          v-if="btnShow && buttonSwitch.import && cgBIBtnMap['import'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['import'].buttonIcon"
          @click="handleImportExcel"
      >
        <span>{{cgBIBtnMap['import'].buttonName}}</span>
      </a-button>
      <a-button
          v-if="btnShow && buttonSwitch.export && cgBIBtnMap['export'].enabled"
          type="primary"
          :preIcon="cgBIBtnMap['export'].buttonIcon"
          :loading="exportLoading"
          @click="onExportExcelOverride"
      >
        <span>{{cgBIBtnMap['export'].buttonName}}</span>
      </a-button>

      <!-- Nút tùy chỉnh -->
      <template v-if="btnShow && cgTopButtonList && cgTopButtonList.length > 0" v-for="(item, index) in cgTopButtonList">
        <a-button
          v-if="item.optType == 'js'"
          :key="'cgbtn' + index"
          @click="cgButtonJsHandler(item.buttonCode)"
          type="primary"
          :preIcon="item.buttonIcon ? 'ant-design:' + item.buttonIcon : ''"
        >
          {{ item.buttonName }}
        </a-button>
        <a-button
          v-else-if="item.optType == 'action'"
          :key="'cgbtn' + index"
          @click="cgButtonActionHandler(item.buttonCode)"
          type="primary"
          :preIcon="item.buttonIcon ? 'ant-design:' + item.buttonIcon : ''"
        >
          {{ item.buttonName }}
        </a-button>
      </template>

      <a-button
          v-show="selectedKeys.length > 0"
          v-if="buttonSwitch.batch_delete && cgBIBtnMap['batch_delete'].enabled"
          :preIcon="cgBIBtnMap['batch_delete'].buttonIcon"
          @click="handleBatchDelete"
      >
        <span>{{cgBIBtnMap['batch_delete'].buttonName}}</span>
      </a-button>
    </template>

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

    <template #action="{ record }">
      <TableAction :actions="getActions(record)" :dropDownActions="getDropDownActions(record)" />
    </template>
  </BasicTable>

  <!-- Popup thêm mới, sửa đổi biểu mẫu -->
  <OnlineAutoModal
      @register="registerModal"
      :id="ID"
      :source="ERPSUBTABLE"
      :cgBIBtnMap="cgBIBtnMap"
      :buttonSwitch="buttonSwitch"
      :confirmBtnCfg="getFormConfirmButtonCfg"
      @success="reload"
      @formConfig="handleFormConfig"
  />

  <!-- Popup chi tiết -->
  <online-detail-modal :source="ERPSUBTABLE" :id="ID" @register="registerDetailModal" />

  <!-- Nhập (import) -->
  <JImportModal @register="registerImportModal" :url="importUrl()" @ok="reload" online></JImportModal>

  <!-- Chuyển hướng (nhảy trang)Hrefphương thức component động của -->
  <a-modal v-bind="hrefComponent.model" v-on="hrefComponent.on">
    <component :is="hrefComponent.is" v-bind="hrefComponent.params" />
  </a-modal>

  <!-- Popup tùy chỉnh -->
  <online-custom-modal @register="registerCustomModal" @success="reload" />

  <!-- Popup cấp chohrefdùng cho một biểu mẫu khác-Biểu mẫu chi tiết -->
  <online-detail-modal :id="hrefMainTableId" @register="registerOnlineHrefModal" :defaultFullscreen="false" />

  <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa-Trường của bản ghi liên kết có thể mở trên danh sáchmodalChỉnh sửa dữ liệu -->
  <online-pop-modal ref="onlinePopModalRef" :id="popTableId" @register="registerPopModal" @success="reload" request topTip></online-pop-modal>

  <!-- Xem sơ đồ quy trìnhmodal -->
  <BpmGraphicModal @register="registerBpmModal"></BpmGraphicModal>
</template>

<script setup>
  import { ref, computed, provide } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OnlineAutoModal from '../default/OnlineAutoModal.vue';
  import OnlineCustomModal from '../default/OnlineCustomModal.vue';
  import OnlineDetailModal from '../default/OnlineDetailModal.vue';
  import { watch } from 'vue';
  import JImportModal from '/@/components/Form/src/jeecg/components/JImportModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useTableColumns } from '../../hooks/auto/useTableColumns';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { getRefPromise } from '../../hooks/auto/useAutoForm';
  import OnlineQueryForm from '../comp/OnlineQueryForm.vue';
  import { useOnlineListPopEvent } from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from '../comp/OnlinePopModal.vue';
  import { ERP, ERPSUBTABLE } from '../../util/constant';
  import { cloneDeep } from 'lodash-es';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  const props = defineProps(['data', 'mainTableSelectedRowRcord']);
  const emit = defineEmits(['getSource']);
  const btnShow = ref(false);
  // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1004】erpSau khi thiết lập cột phong cách, làm mới lại, nội dung trưng bày không đúng
  const tableSetting = {
    cacheKey: `online_erp_subTable_${props.data.currentTableName}`,
  };
  // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1004】erpSau khi thiết lập cột phong cách, làm mới lại, nội dung trưng bày không đúng
  const { createMessage: $message } = useMessage();
  // Hàng mã này nên được chạy mỗi lần vào route mới, bất kể route đó có bị lưu bộ nhớ đệm hay không--
  const {
    ID,
    onlineTableContext,
    onlineQueryFormOuter,
    loading,
    reload,
    dataSource,
    pagination,
    handleSpecialConfig,
    getColumnList,
    handleChangeInTable,
    loadData,
    onlineExtConfigJson,
    handleFormConfig,
    registerCustomModal,
    tableReloading,
  } = useOnlineTableContext({ code: props.data.code, themeTemplate: ERP });
  onlineTableContext['isErpSubTable'] = true;

  // Xử lý phần tăng cường
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // Xử lý danh sáchbutton
  const {
    buttonSwitch,
    cgLinkButtonList,
    cgBIBtnMap,
    getQueryButtonCfg,
    getResetButtonCfg,
    getFormConfirmButtonCfg,
    cgTopButtonList,
    importUrl,
    registerModal,
    handleAdd,
    handleEdit,
    handleBatchDelete,
    registerImportModal,
    onImportExcel,
    onExportExcel,
    cgButtonJsHandler,
    cgButtonActionHandler,
    cgButtonLinkHandler,
    handleSubmitFlow,
    getDropDownActions,
    getActions,
    initButtonList,
    initButtonSwitch,
    registerDetailModal,
    registerBpmModal,
  } = useListButton(onlineTableContext, onlineExtConfigJson);

  const exportLoading = ref(false);

  // Viết lại phương thức xuất, ngăn nhấp thường xuyên
  async function onExportExcelOverride() {
    try {
      exportLoading.value = true;
      await onExportExcel();
    } finally {
      // Ngăn nhấp thường xuyên, trì hoãn1.5sĐóngloading
      setTimeout(() => (exportLoading.value = false), 1500);
    }
  }

  // Xử lý BasicTable cấu hình của
  const {
    columns,
    actionColumn,
    selectedKeys,
    rowSelection,
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

  initAutoList(cloneDeep(props.data));

  const foreignkeyRef = ref(null);
  const foreignKeys = props.data.foreignKeys;
  let $key;
  if (foreignKeys?.length) {
    // Khóa ngoại chỉ có một cái, nhưng được đưa dưới dạng mảng, chỉ cần lấy cái đầu tiên.
    const item = foreignKeys[0];
    const field = item.field;
    $key = item.key;
    onlineTableContext['foreignKeyField'] = field;
  } else {
    onlineTableContext['foreignKeyField'] = null;
    onlineTableContext['foreignKeyValue'] = null;
  }
  provide('foreignkey', foreignkeyRef);
  watch(
    () => props.mainTableSelectedRowRcord,
    (newVal) => {
      // Chuyển đổi trạng thái đặt lại
      // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-188】erpPhân trang bảng con của phong cách đổi thành không hiển thị phân trang, trang web báo lỗi không phản hồi
      if (pagination.value) pagination.value.current = 1;
      // update-end--author:liaozhiyang---date:20260713---for：【LHZP-188】erpPhân trang bảng con của phong cách đổi thành không hiển thị phân trang, trang web báo lỗi không phản hồi
      selectedKeys.value = [];
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-124】erpPhong cách, khi chuyển dữ liệu bảng chính, điều kiện truy vấn của bảng con chưa được xóa trống
      onlineQueryFormOuter.value?.clearSearch();
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-124】erpPhong cách, khi chuyển dữ liệu bảng chính, điều kiện truy vấn của bảng con chưa được xóa trống
      // update-begin--author:liusq---date:20260113---for：【LHZP-118】Liên động bảng chính-con (xử lý dự phòng): chỉ hàng được chọn hợp lệ(id !== undefined)mới đi theo nhánh tải
      if (newVal && newVal.id !== undefined) {
        if (onlineTableContext['foreignKeyField']) {
          const value = newVal[$key];
          onlineTableContext['foreignKeyValue'] = value;
          foreignkeyRef.value = { field: onlineTableContext['foreignKeyField'], value };
        }
        btnShow.value = true;
        loading.value = true;
        loadData().finally(() => {
          loading.value = false;
        });
      } else {
        btnShow.value = false;
        // Xóa trống tường minh foreignKeyValue, tránh loadData Trúng giá trị cũ vẫn đi truy vấn bảng con
        onlineTableContext['foreignKeyValue'] = null;
        dataSource.value = [];
      }
      // update-end--author:liusq---date:20260113---for：【LHZP-118】Liên động bảng chính-con (xử lý dự phòng): chỉ hàng được chọn hợp lệ(id !== undefined)mới đi theo nhánh tải
    },
    {
      immediate: true,
    }
  );
  // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
  watch(
    () => dataSource.value,
    () => {
      emit('getSource', props.data.currentTableName, dataSource.value);
    },
    { immediate: true }
  );
  // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
  /**Tải lạionlineCấu hình*/
  async function initAutoList(data) {
    // 1.Thông tin cấu hình cột
    handleTableConfig(data);
    loading.value = false;
    // 2.Tải dữ liệu
    // await loadData();
    // loading.value = false;
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
   * Control truy vấn sự kiện-Thực thi truy vấn
   * @param data
   */
  function queryWithCondition(data) {
    onlineTableContext['queryParam'] = data;
    reload({mode:'search'});
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
  const handleAddRcord = () => {
    if(props.data.relationType ==1 && dataSource.value.length) {
      $message.warning('Bảng một-một chỉ được thêm một bản ghi dữ liệu');
    } else {
      handleAdd();
    }

  }

  // Sự kiện nhập - Kiểm chứng quan hệ một-một (làm ở frontend UX chặn lại, ràng buộc thực sự do backend đảm trách)
  const handleImportExcel = () => {
    if (props.data.relationType == 1 && dataSource.value.length) {
      $message.warning(`Bảng một-một chỉ được nhập một bản ghi dữ liệu!`);
    } else {
      onImportExcel();
    }
  }
</script>

