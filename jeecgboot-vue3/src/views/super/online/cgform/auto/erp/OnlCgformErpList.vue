<template>
  <div ref="cgformErpListRef" class="p-2 cgformErpList">
    <div class="content">

      <!-- Màn hình khung xương (skeleton) khi đang tải -->
      <a-skeleton v-if="tableReloading" active />

      <!-- Điều kiện truy vấn -->
      <online-query-form
          v-show="!tableReloading"
          ref="onlineQueryFormOuter"
          :id="ID"
          :queryBtnCfg="getQueryButtonCfg"
          :resetBtnCfg="getResetButtonCfg"
          @search="queryWithCondition"
          @loaded="onQueryFormLoaded"
      />

      <!-- Danh sách -->
      <BasicTable
        v-if="!tableReloading"
        ref="onlineTable"
        rowKey="jeecg_row_key"
        :canResize="true"
        :bordered="true"
        :showIndexColumn="false"
        :loading="loading"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="pagination"
        :rowSelection="rowSelection"
        :actionColumn="actionColumn"
        :showTableSetting="true"
        :clickToRowSelect="true"
        :scroll="scroll"
        @table-redo="reload"
        :tableSetting="tableSetting"
        :class="{ 'j-table-force-nowrap': enableScrollBar }"
        @change="handleChangeInTable"
      >
        <template #tableTitle>
          <a-button
              v-if="buttonSwitch.add && cgBIBtnMap['add'].enabled"
              type="primary"
              :preIcon="cgBIBtnMap['add'].buttonIcon"
              @click="handleAdd"
          >
            <span>{{ cgBIBtnMap['add'].buttonName }}</span>
          </a-button>
          <a-button
              v-if="buttonSwitch.import && cgBIBtnMap['import'].enabled"
              type="primary"
              :preIcon="cgBIBtnMap['import'].buttonIcon"
              @click="onImportExcel"
          >
            <span>{{ cgBIBtnMap['import'].buttonName }}</span>
          </a-button>
          <a-button
              v-if="buttonSwitch.export && cgBIBtnMap['export'].enabled"
              type="primary"
              :preIcon="cgBIBtnMap['export'].buttonIcon"
              :loading="exportLoading"
              @click="onExportExcelOverride"
          >
            <span>{{ cgBIBtnMap['export'].buttonName }}</span>
          </a-button>

          <!-- Nút tùy chỉnh -->
          <template v-if="cgTopButtonList && cgTopButtonList.length > 0" v-for="(item, index) in cgTopButtonList">
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

          <online-super-query
            v-if="buttonSwitch.super_query && cgBIBtnMap['super_query'].enabled"
            ref="superQueryButtonRef"
            online
            :status="superQueryStatus"
            :queryBtnCfg="cgBIBtnMap['super_query']"
            @search="handleSuperSearch"
          />
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
      <!--Bảng dữ liệu của bảng contab-->
      <a-tabs v-if="subTableData?.length" animated v-model:activeKey="tabActiveKey" style="margin: 10px">
        <a-tab-pane v-for="(item, index) in subTableData" :tab="item.description" :key="index" forceRender>
          <OnlCgformErpSubTable :data="item" :mainTableSelectedRowRcord="selectedRowRcord" @getSource="getSource" />
        </a-tab-pane>
      </a-tabs>

      <!-- Popup thêm mới, sửa đổi biểu mẫu -->
      <OnlineAutoModal
          @register="registerModal"
          :id="ID"
          :subTableSource="erpAllSubTableSource"
          :cgBIBtnMap="cgBIBtnMap"
          :buttonSwitch="buttonSwitch"
          :confirmBtnCfg="getFormConfirmButtonCfg"
          @success="reload"
          @formConfig="handleFormConfig"
      />

      <!-- Popup chi tiết -->
      <online-detail-modal :id="ID" @register="registerDetailModal" />

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
      <!-- Trangloading[chủ yếu đểjsPhần tăng cường sử dụng] -->
      <Loading :loading="pageLoading" :absolute="true" />
    </div>
  </div>
</template>

<script setup name="CgformErpList">
  import { ref, nextTick, computed, watchEffect } from 'vue';
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
  import OnlineSuperQuery from '../comp/superquery/SuperQuery.vue';
  import { useOnlineListPopEvent } from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from '../comp/OnlinePopModal.vue';
  import OnlCgformErpSubTable from './OnlCgformErpSubTable.vue';
  import { ERP } from "../../util/constant";
  import { cloneDeep } from 'lodash-es';
  import { Loading } from '/@/components/Loading';
  import { isObject } from '/@/utils/is';
  import { useDebounceFn } from '@vueuse/core';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';
  const subTableData = ref([]);
  const tabActiveKey = ref(0);
  const selectedRowRcord = ref(null);
  const tableSetting = ref({});
  const { createMessage: $message } = useMessage();
  const cgformErpListRef = ref(null);
  const onlineTable = ref(null);
  // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
  const erpAllSubTableSource = ref({});
  const getSource = (tableName, data) => {
    erpAllSubTableSource.value[tableName] = data;
  };
  // update-end--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
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
    superQueryButtonRef,
    superQueryStatus,
    handleSuperQuery,
    onlineExtConfigJson,
    handleFormConfig,
    registerCustomModal,
    tableReloading,
    pageLoading,
  } = useOnlineTableContext({ themeTemplate: ERP });

  const scroll = computed(() => {
    const reuslt = { y: 300 };
    if (isObject(tableScroll.value)) {
      return { ...tableScroll.value, ...reuslt };
    }
    return reuslt;
  });

  // Lắng nghe thay đổi nguồn dữ liệu
  // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-593】Nhấp phân trang trang trước, mục đã chọn không hiển thị nữa
  const handleDataSourceChange = useDebounceFn((value) => {
    // update-begin--author:liusq---date:20260713---for：【LHZP-118】ERPKhi kết quả truy vấn bảng chính-con trống, bảng con còn sót lại dữ liệu
    // Khi bảng chính không có dữ liệu, bắt buộc xóa hàng đã chọn, kích hoạt bảng con watch Đi qua else Nhánh xóa sạch bảng con
    if (!Array.isArray(value) || value.length === 0) {
      if (selectedKeys.value.length > 0) {
        selectedKeys.value = [];
      }
      if (selectedRowRcord.value !== null) {
        selectedRowRcord.value = null;
      }
      return;
    }
    // update-end--author:liusq---date:20260713---for：【LHZP-118】ERPKhi kết quả truy vấn bảng chính-con trống, bảng con còn sót lại dữ liệu
    if (selectedKeys.value.length > 0) {
      // 【TV360X-2701】Nếu dữ liệu đã chọn không tồn tại trong dữ liệu của trang hiện tại thì xóa lựa chọn
      // selectedKeys.value = selectedKeys.value.filter((key) => value.some((item) => item['jeecg_row_key'] === key));
    }
    // update-begin--author:liaozhiyang---date:20250722---for：【issues/8575】erpMặc định chọn cái đầu tiên và khi chưa chọn bảng chính thì bảng con không truy vấn
    if (pagination.value.current == 1 && selectedKeys.value.length === 0) {
        const tableTbodyElem = document.querySelector('.ant-table-wrapper .ant-table-tbody');
        tableTbodyElem?.querySelector('.ant-table-row')?.click();
    }
    // update-end--author:liaozhiyang---date:20250722---for：【issues/8575】erpMặc định chọn cái đầu tiên và khi chưa chọn bảng chính thì bảng con không truy vấn
  }, 100);
  watch(dataSource, (value) => handleDataSourceChange(value));
  // update-end--author:liaozhiyang---date:20260715---for：【LHZP-593】Nhấp phân trang trang trước, mục đã chọn không hiển thị nữa

  // Phán đoán NếuIDkhông tồn tại thì chấm dứt logic tiếp theo
  if (!ID.value) {
    $message.warning('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
    // update-end--author:liaozhiyang---date:20230825---for：【QQYUN-6326】Một phần mã không tìm thấy tham chiếu
    throw new Error('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
    // update-end--author:liaozhiyang---date:20230825---for：【QQYUN-6326】Một phần mã không tìm thấy tham chiếu

  }
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
  } = useListButton(onlineTableContext, onlineExtConfigJson, {
    singleDelCallback: (id) => {
      // Cái bị xóa đúng là dữ liệu đang được chọn, cần xóa lựa chọnkey
      if (Array.isArray(onlineTableContext['selectedRowKeys']) && onlineTableContext['selectedRowKeys'].includes(id)) {
        onlineTableContext['clearSelectedRow']();
      }
    },
    editClickCallback: (id, e) => {
      // Dữ liệu hiện tại đã được chọn thì phải ngăn nổi bọt (bubbling), tránh đảo chọn
      if (Array.isArray(onlineTableContext['selectedRowKeys']) && onlineTableContext['selectedRowKeys'].includes(id)) {
        e.stopPropagation();
      }
    },
  });
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

  watch(
    selectedKeys,
    (value) => {
      if (selectedKeys.value?.length) {
        selectedRowRcord.value = dataSource.value.find((item) => item['id'] === selectedKeys.value[0]);
      }else{
        selectedRowRcord.value = null;
      }
    },
    {
      immediate: true,
    }
  );

  // Lắng nghe cấu hình biểu mẫuID
  watch(
    ID,
    () => {
      console.log('watched id is change...');
      initAutoList();
    },
    { immediate: true }
  );

  /**Tải lạionlineCấu hình*/
  async function initAutoList() {
    loading.value = true;
    // 1.Thông tin cấu hình cột
    let columnResult = await getColumnList(ERP);
    handleTableConfig(columnResult.main);
    subTableData.value = columnResult.subList;
    // 2.Tải dữ liệu
    // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
    let hasDefaultQuery = false;
    try {
      const queryForm = await getRefPromise(onlineQueryFormOuter);
      if (queryForm && typeof queryForm.whenFirstQueryDecided === 'function') {
        hasDefaultQuery = await queryForm.whenFirstQueryDecided();
      }
    } catch (e) {
      hasDefaultQuery = false;
    }
    if (!hasDefaultQuery) {
      await loadData();
    }
    // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
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
    handleColumnResult(result, 'radio');
    // Cấu hình bảng
    handleSpecialConfig(result);
    // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-1004】erpSau khi thiết lập cột phong cách, làm mới lại, nội dung trưng bày không đúng
    tableSetting.value = {
      cacheKey: `online_erp_mainTable_${result.currentTableName}`,
    };
    // update-end--author:liaozhiyang---date:20240611---for：【TV360X-1004】erpSau khi thiết lập cột phong cách, làm mới lại, nội dung trưng bày không đúng
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
   * Sự kiện component truy vấn tải hoàn tất, lấy thông tin trường cần cho truy vấn nâng cao
   */
  async function onQueryFormLoaded(json = {}) {
    console.log('onQueryFormLoaded', json);
    await getRefPromise(superQueryButtonRef);
    // update-begin--author:liaozhiyang---date:20230904---for：【QQYUN-6425】erpPhong cách Truy vấn nâng cao Khi chưa truy vấn ra dữ liệu Dữ liệu bảng con chưa được xóa sạch
    const cJson = cloneDeep(json),
      { properties = {} } = cJson;
    Object.entries(properties).forEach(([key, item]) => {
      if (item.view == 'table') {
        delete properties[key];
      }
    });
    superQueryButtonRef.value.init(cJson);
    // update-end--author:liaozhiyang---date:20230904---for：【QQYUN-6425】erpPhong cách Truy vấn nâng cao Khi chưa truy vấn ra dữ liệu Dữ liệu bảng con chưa được xóa sạch
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
  console.log('111111111-------onlineTableContext====111111111111111111', onlineTableContext);

  const handleSuperSearch = (params, matchType) => {
    handleSuperQuery(params, matchType);
    selectedKeys.value = [];
  };
  // Giải quyếterpbảng chính5Khi số mục lớn, trên laptop sẽ xuất hiện vấn đề thanh cuộn
  watchEffect(() => {
    const size = onlineTable?.value?.getBindValuesRef().value?.size
    setTimeout(() => {
      const tableTbodyElem = cgformErpListRef.value.querySelector('.ant-table-wrapper .ant-table-body');
      if (tableTbodyElem) {
        tableTbodyElem.style.height = 'auto';
        if (dataSource.value.length > 0) {
          if (tableTbodyElem.offsetHeight < 300) {
            tableTbodyElem.style.height = `${tableTbodyElem.offsetHeight + 1}px`;
            console.log(size);
          }
        }
      }
    }, 0);
  }, {
    flush: 'post'
  });
  // 1Đã đưa vàoloadsh   console.log(that.simpleDateFormat(new Date().getTime(),'yyyy-MM-dd'));
  // 2. valuevấn đề của
  // 3. Sau khi vị trí của biến thay đổi, cần Viết lại (override)api
  // 1Khi thêm nút Chừa ra đối tượng kiểu dáng sau đójsThiết lập đối tượng kiểu dáng trong phần tăng cường
  // 2Thiết lập trực tiếpcssChuỗi ký tự Sau đó thông quajs document về phíaheadThêm vào bên trongcssĐoạn (fragment) Có hiệu lực toàn cục
</script>

<style lang="less">
 // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8493】Chỉnh sửa chế độ tốionlineBiểu mẫuErpvà trang chỉnh sửa hiển thị không đúng
  html[data-theme='light'] {
    .cgformErpList {
      height: 100%;
      .content {
        background-color: #fff;
        height: 100%;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8493】Chỉnh sửa chế độ tốionlineBiểu mẫuErpvà trang chỉnh sửa hiển thị không đúng
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
</style>
<style lang="less" scoped>
  // Để giải quyết việc trên laptop tính ra số thực dấu phẩy động gây ra5mục cũng xuất hiện thanh cuộn
  :deep(.ant-table-body) {
    line-height: 22px;
  }

  // update-begin--author:liaozhiyang---date:20260831---for：【LHZP-1073】erpcủa trường đầu tiên trong mỗi hàng của vùng truy vấn label Hủy độ rộng cố định
  @media (min-width: 992px) {
    :deep(.online-query-form .ant-row > .ant-col:nth-child(4n + 1) .ant-form-item-label) {
      flex: none;
      width: auto !important;
    }

    :deep(.online-query-form .ant-row > .ant-col:nth-child(4n + 1) .ant-form-item-control) {
      flex: 1;
      min-width: 0;
    }
  }

  @media (min-width: 768px) and (max-width: 991px) {
    :deep(.online-query-form .ant-row > .ant-col:nth-child(2n + 1) .ant-form-item-label) {
      flex: none;
      width: auto !important;
    }

    :deep(.online-query-form .ant-row > .ant-col:nth-child(2n + 1) .ant-form-item-control) {
      flex: 1;
      min-width: 0;
    }
  }

  @media (max-width: 767px) {
    :deep(.online-query-form .ant-form-item-label) {
      flex: none;
      width: auto !important;
    }

    :deep(.online-query-form .ant-form-item-control) {
      flex: 1;
      min-width: 0;
    }
  }
  // update-end--author:liaozhiyang---date:20260831---for：【LHZP-1073】erpcủa trường đầu tiên trong mỗi hàng của vùng truy vấn label Hủy độ rộng cố định
</style>
