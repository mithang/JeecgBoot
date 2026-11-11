<template>
  <div :class="['p-2', `online-list-${ID}`]">

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
      :clickToRowSelect="false"
      :scroll="tableScroll"
      @table-redo="reload"
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
          <span>{{cgBIBtnMap['add'].buttonName}}</span>
        </a-button>
        <a-button
            v-if="buttonSwitch.import && cgBIBtnMap['import'].enabled"
            type="primary"
            :preIcon="cgBIBtnMap['import'].buttonIcon"
            @click="onImportExcel"
        >
          <span>{{cgBIBtnMap['import'].buttonName}}</span>
        </a-button>
        <a-button
            v-if="buttonSwitch.export && cgBIBtnMap['export'].enabled"
            type="primary"
            :preIcon="cgBIBtnMap['export'].buttonIcon"
            :loading="exportLoading"
            @click="onExportExcelOverride"
        >
          <span>{{cgBIBtnMap['export'].buttonName}}</span>
        </a-button>
        <!-- update-begin--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử -->
        <a-tooltip
          v-if="!isConfigCurRoute && (tableType == 1 || tableType == 2) && buttonSwitch.aigc_mock_data && cgBIBtnMap['aigc_mock_data']?.enabled && dataSource.length == 0 && !hasQueryCondition && !loading"
          :open="false"
          placement="bottom"
        >
          <template #title>
            <span>Khi có dữ liệu, có điều kiện truy vấn hoặc sau khi tạo dữ liệu kiểm thử, nút này sẽ ẩn</span>
          </template>
          <a-button preIcon="mdi:robot-love-outline" :loading="testDataLoading" @click="handleAddTestData(currentTableName, reload)">
            {{ cgBIBtnMap['aigc_mock_data'].buttonName }}
          </a-button>
        </a-tooltip>
        <!-- update-end--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử -->
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
            @search="handleSuperQuery"
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

    <!-- Popup thêm mới, sửa đổi biểu mẫu -->
    <OnlineAutoModal
        @register="registerModal"
        :id="ID"
        :cgBIBtnMap="cgBIBtnMap"
        :buttonSwitch="buttonSwitch"
        :confirmBtnCfg="getFormConfirmButtonCfg"
        @success="reload"
        @formConfig="handleFormConfig"
    />

    <!-- Popup chi tiết -->
    <online-detail-modal :id="ID" @register="registerDetailModal"/>

    <!-- Nhập (import) -->
    <JImportModal @register="registerImportModal" :url="importUrl()" @ok="reload" online></JImportModal>

    <!-- Chuyển hướng (nhảy trang)Hrefphương thức component động của -->
    <a-modal v-bind="hrefComponent.model" v-on="hrefComponent.on">
      <component :is="hrefComponent.is" v-bind="hrefComponent.params" />
    </a-modal>

    <!-- Popup tùy chỉnh -->
    <online-custom-modal @register="registerCustomModal" @success="reload" />

    <!-- Popup cấp chohrefdùng cho một biểu mẫu khác-Biểu mẫu chi tiết -->
    <online-detail-modal :id="hrefMainTableId" @register="registerOnlineHrefModal" :defaultFullscreen="false"/>

    <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa-Trường của bản ghi liên kết có thể mở trên danh sáchmodalChỉnh sửa dữ liệu -->
    <online-pop-modal ref="onlinePopModalRef" :id="popTableId" @register="registerPopModal" @success="reload" request topTip></online-pop-modal>

    <!-- Xem sơ đồ quy trìnhmodal -->
    <BpmGraphicModal @register="registerBpmModal"></BpmGraphicModal>
    <!-- Trangloading[chủ yếu đểjsPhần tăng cường sử dụng] -->
    <Loading :loading="pageLoading" :absolute="true" />
  </div>
</template>

<script lang="ts">
  import { ref } from 'vue'
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useMessage } from '/@/hooks/web/useMessage';
  import OnlineAutoModal from './OnlineAutoModal.vue';
  import OnlineCustomModal from './OnlineCustomModal.vue';
  import OnlineDetailModal from './OnlineDetailModal.vue'
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
  import { NORMAL } from "../../util/constant";
  import { Loading } from '/@/components/Loading';
  import { sanitizeRichText } from '/@/utils/htmlSanitizer';

  export default {
    name: 'OnlineAutoList',
    components: {
      BasicTable,
      TableAction,
      OnlineAutoModal,
      JImportModal,
      OnlineQueryForm,
      OnlineSuperQuery,
      OnlineCustomModal,
      OnlineDetailModal,
      OnlinePopModal,
      Loading,
    },
    setup() {
      const { createMessage: $message } = useMessage();
      const currentTableName = ref('');
      const tableType = ref('');
      // Hàng mã này nên được chạy mỗi lần vào route mới, bất kể route đó có bị lưu bộ nhớ đệm hay không--
      const {
        ID,
        onlineTableContext,
        onlineQueryFormOuter,
        loading,
        reload,
        dataSource,
        hasQueryCondition,
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
        isConfigCurRoute,
        pageLoading,
      } = useOnlineTableContext();

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
        handleAddTestData,
        testDataLoading,
        testDataBtnShow,
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
        registerBpmModal
      } = useListButton(onlineTableContext, onlineExtConfigJson);

      const exportLoading = ref(false)

      // Viết lại phương thức xuất, ngăn nhấp thường xuyên
      async function onExportExcelOverride() {
        try {
          exportLoading.value = true
          await onExportExcel()
        } finally {
          // Ngăn nhấp thường xuyên, trì hoãn1.5sĐóngloading
          setTimeout(() => exportLoading.value = false, 1500)
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
        let columnResult: any = await getColumnList(NORMAL);
        handleTableConfig(columnResult);
        // update-begin--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử
        currentTableName.value = columnResult.currentTableName;
        tableType.value = columnResult.tableType;
        // update-end--author:liaozhiyang---date:20250403---for：【QQYUN-11801】Tạo dữ liệu kiểm thử
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
       * Sự kiện component truy vấn tải hoàn tất, lấy thông tin trường cần cho truy vấn nâng cao
       */
      async function onQueryFormLoaded(json) {
        console.log('onQueryFormLoaded', json)
        await getRefPromise(superQueryButtonRef)
        superQueryButtonRef.value.init(json);
      }

      /**
       * listMở trang Popup biểu mẫu khác
       * @param params
       */
      function openOnlinePopModal(params){
        console.log('openOnlinePopModal', params)
        popTableId.value = params.id;
        let data = {
          title: params.describe
        }
        if(params.record && params.record.id){
          data['record'] = params.record
          data['isUpdate'] = true;
        }
        openPopModal(true, data);
      }
      //Liên kết sự kiện popup
      useOnlineListPopEvent(openOnlinePopModal);
      
      const that = {
        sanitizeRichText,
        ID,
        // Vùng truy vấn
        onlineQueryFormOuter,
        queryWithCondition,
        onQueryFormLoaded,
        reload,

        //Truy vấn nâng cao
        superQueryButtonRef,
        superQueryStatus,
        handleSuperQuery,

        // tableKhu vực
        loading,
        columns,
        dataSource,
        hasQueryCondition,
        pagination,
        actionColumn,
        rowSelection,
        selectedKeys,
        tableScroll,
        enableScrollBar,
        handleChangeInTable,

        //Nút
        buttonSwitch,
        handleAdd,
        handleEdit,
        onImportExcel,
        onExportExcel,
        exportLoading,
        onExportExcelOverride,
        cgBIBtnMap,
        getQueryButtonCfg,
        getResetButtonCfg,
        getFormConfirmButtonCfg,
        cgTopButtonList,
        cgLinkButtonList,
        cgButtonJsHandler,
        cgButtonActionHandler,
        cgButtonLinkHandler,
        handleBatchDelete,
        handleAddTestData,
        testDataLoading,
        testDataBtnShow,
        currentTableName,
        loadData,
        tableType,
        isConfigCurRoute,

        // table-slot
        downloadRowFile,
        getImgView,
        getPcaText,
        getFormatDate,

        // Cột thao tác
        getActions,
        getDropDownActions,

        // Popup
        registerModal,
        registerCustomModal,
        registerImportModal,
        registerDetailModal,
        importUrl,
        handleFormConfig,
        onlinePopModalRef,

        //Khác
        tableReloading,
        handleSubmitFlow,
        hrefComponent,
        viewOnlineCellImage,
        hrefMainTableId,
        onlineExtConfigJson,
        registerOnlineHrefModal,
        registerPopModal,
        popTableId,
        registerBpmModal,
        handleClickFieldHref,
        pageLoading,
      };
      return that;
    },

    // 1Đã đưa vàoloadsh   console.log(that.simpleDateFormat(new Date().getTime(),'yyyy-MM-dd'));
    // 2. valuevấn đề của
    // 3. Sau khi vị trí của biến thay đổi, cần Viết lại (override)api
    // 1Khi thêm nút Chừa ra đối tượng kiểu dáng sau đójsThiết lập đối tượng kiểu dáng trong phần tăng cường
    // 2Thiết lập trực tiếpcssChuỗi ký tự Sau đó thông quajs document về phíaheadThêm vào bên trongcssĐoạn (fragment) Có hiệu lực toàn cục
  };
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
</style>
