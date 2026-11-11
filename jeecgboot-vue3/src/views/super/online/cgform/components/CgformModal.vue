<template>
  <BasicModal
    ref="modalRef"
    :title="title"
    :width="1200"
    :maskClosable="false"
    :defaultFullscreen="true"
    :confirmLoading="confirmLoading"
    v-bind="$attrs"
    wrapClassName="onlForm-config-modal"
    @cancel="onCancel"
    @register="registerModal"
  >
    <div
      ref="onlFormContentRef"
      class="onlForm-content"
      :data-input-method="inputMethod"
      @pointerdown.capture="inputMethod = 'mouse'"
      @keydown.capture="onInputKeyDown"
    >
      <a-spin ref="spinRef" wrapperClassName="pl-2 pr-2" :spinning="confirmLoading">
        <div ref="onlFormContentFormRef" class="onlForm-content-form">
          <CgformHeadForm
            ref="cgformHeadFormRef"
            @tableTypeChange="onTableTypeChange"
            @relationTypeChange="onRelationTypeChange"
            @isTreeChange="onIsTreeChange"
            @extConfigSaved="onExtConfigSaved"
          />
        </div>
        <a-spin :spinning="tableLoading || hideTabs">
          <a-tabs v-show="!hideTabs" v-model:activeKey="activeKey" @change="onTabsChange">
            <a-tab-pane tab="Thuộc tính cơ sở dữ liệu" key="dbTable" forceRender>
              <DBAttributeTable
                ref="dbTable"
                :isUpdate="isUpdate"
                :tableName="oldTableName"
                :actionButton="actionButton"
                :isTree="isTree"
                @added="onTableAdded"
                @removed="onTableRemoved"
                @dragged="onTableDragged"
                @inserted="onTableInserted"
                @syncDbType="onTableSyncDbType"
                @syncQueryMode="onTableSyncQueryMode"
                @syncDbIsPersist="onTableSyncDbIsPersist"
                @syncDbIsNull="onTableSyncDbIsNull"
              />
            </a-tab-pane>
            <a-tab-pane tab="Thuộc tính trang" key="pageTable" forceRender>
              <PageAttributeTable ref="pageTable" />
            </a-tab-pane>
            <a-tab-pane tab="Trường kiểm chứng" key="checkTable" forceRender>
              <CheckDictTable ref="checkTable" />
            </a-tab-pane>
            <a-tab-pane tab="Khóa ngoại" key="fkTable" forceRender>
              <ForeignKeyTable ref="fkTable" :actionButton="actionButton" />
            </a-tab-pane>
            <a-tab-pane tab="Chỉ mục (index)" key="idxTable" forceRender>
              <IndexTable ref="idxTable" :actionButton="actionButton" />
            </a-tab-pane>
            <a-tab-pane key="queryTable" forceRender>
              <template #tab>
                <span>
                  Cấu hình truy vấn cá nhân hóa
                  <a-tooltip>
                    <template #title>Cho phép tùy chỉnh, loại control của trường trong biểu mẫu truy vấn!</template>
                    <Icon class="query-help-icon" icon="bx:help-circle"></Icon>
                  </a-tooltip>
                </span>
              </template>
              <QueryTable ref="queryTable" @query="onTableQuery" />
            </a-tab-pane>
          </a-tabs>
        </a-spin>
      </a-spin>
    </div>

    <template #footer>
      <div class="footer-area">
        <div class="rightArea">
          <a-button @click="onCancel">Đóng</a-button>
          <a-button type="primary" :loading="confirmLoading" preIcon="ant-design:save" @click="onSubmit">Lưu</a-button>
        </div>
        <div class="leftArea">
          <div v-if="aiTestMode && !isUpdate">
            <a-select
              v-model:value="aiTestTable"
              placeholder="Vui lòng chọn mô hình dữ liệu để kiểm thử"
              :getPopupContainer="(n) => n?.parentElement"
              style="width: 300px; margin: 0 10px 0 0;text-align: left;"
            >
              <template v-for="(item, index) in aiTableList" :key="index">
                <a-select-option :value="item.name">{{ item.title + '（' + item.name + '）' }}</a-select-option>
              </template>
            </a-select>
            <a-button type="primary" ghost @click="initVirtualData">Tạo dữ liệu&gt;&gt;</a-button>
          </div>
          <div v-if="isUpdate" class="positioning-area">
            <a-input v-model:value="positioning" placeholder="Tìm kiếm trường hoặc ghi chú" allowClear @pressEnter="handlePositioning"></a-input>
            <a-button type="primary" ghost @click="handlePositioning">Định vị</a-button>
          </div>
        </div>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, computed, nextTick, provide, defineComponent, toRaw } from 'vue';
  import { useResizeObserver } from '@vueuse/core';
  import { Icon } from '/@/components/Icon';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import DBAttributeTable from './tables/DBAttributeTable.vue';
  import CgformHeadForm from './CgformHeadForm.vue';
  import PageAttributeTable from './tables/PageAttributeTable.vue';
  import CheckDictTable from './tables/CheckDictTable.vue';
  import ForeignKeyTable from './tables/ForeignKeyTable.vue';
  import IndexTable from './tables/IndexTable.vue';
  import QueryTable from './tables/QueryTable.vue';
  import { CgformModal } from '../types';
  import { useInitialData, VALIDATE_FAILED, useTreeNeedFields } from '../cgform.data';
  import { formApi } from '../cgform.api';
  import { simpleDebounce } from '/@/utils/common/compUtils';
  import { useOnlineTest } from '../hooks/aitest/useOnlineTest';
  import { buildUUID } from '/@/utils/uuid';
  import { sleep } from '/@/utils';

  export default defineComponent({
    name: 'CgformModal',
    components: {
      BasicModal,
      CgformHeadForm,
      DBAttributeTable,
      PageAttributeTable,
      CheckDictTable,
      ForeignKeyTable,
      IndexTable,
      QueryTable,
      Icon,
    },
    emits: ['success', 'register'],
    props: {
      actionButton: {
        type: Boolean,
        default: true,
        required: false,
      },
    },
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      const modalRef = ref();
      const spinRef = ref();
      // Có phải chế độ cập nhật hay không
      const isUpdate = ref(false);
      // Giá trị lưu trữ khi chỉnh sửa
      let model: Recordable = {};
      const title = computed(() => (isUpdate.value ? 'Chỉnh sửa' : 'Thêm mới'));
      // Hiện tại có đang tải hay không
      const confirmLoading = ref(true);
      // Vùng bảng đang tải
      const tableLoading = ref(false);
      // tabsTrang đang hoạt động hiện tại
      const activeKey = ref('dbTable');
      // Giải quyết vấn đề tốc độ mở popup chậm
      const hideTabs = ref(true);
      // Đánh dấu đã khởi tạo hay chưa (v-showỞ chế độ này component sẽ không bị hủy)
      let tabsInitialized = false;
      // table refs
      const tables: CgformModal.TablesRef = {
        dbTable: ref<CgformModal.DBAttributeTableType>(),
        pageTable: ref<CgformModal.PageAttributeTableType>(),
        checkTable: ref<CgformModal.CheckDictTableType>(),
        fkTable: ref<CgformModal.ForeignKeyTableType>(),
        idxTable: ref<CgformModal.IndexTableType>(),
        queryTable: ref<CgformModal.QueryTableType>(),
      };
      // Hiện tại có phải trạng thái toàn màn hình hay không
      const fullScreenRef = computed(() => modalRef.value?.fullScreenRef ?? false);
      provide('tables', tables);
      provide('fullScreenRef', fullScreenRef);
      const positioning = ref('');
      const onlFormContentRef = ref<HTMLElement>();

      //update-begin-author:liaozhiyang date:2026-05-12 for:【QQYUN-9789】Khi nhấp chuột thì bỏ đi vxe-table khung màu xanh của, điều hướng bằng bàn phím (Tab/Enter/phím mũi tên) thì hiển thị bình thường
      const inputMethod = ref<'mouse' | 'keyboard'>('mouse');
      const KEYBOARD_NAV_KEYS = new Set(['Tab', 'Enter', 'Escape', 'ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight']);
      function onInputKeyDown(e: KeyboardEvent) {
        if (KEYBOARD_NAV_KEYS.has(e.key)) {
          inputMethod.value = 'keyboard';
        }
      }
      //update-end-author:liaozhiyang date:2026-05-12 for：【QQYUN-9789】Khi nhấp chuột thì bỏ đi vxe-table khung màu xanh của, điều hướng bằng bàn phím (Tab/Enter/phím mũi tên) thì hiển thị bình thường
      const onlFormContentFormRef = ref<HTMLElement>();
      /** `.onlForm-content-form` Chiều cao hiện tại (theo bố cục/thay đổi như mở rộng/thu gọn) */
      const onlFormContentFormHeight = ref(0);
      const vxetableHeight = ref(0);
      provide('vxetableHeight', vxetableHeight);
      useResizeObserver(onlFormContentFormRef, (entries) => {
        const entry = entries[0];
        if (entry) {
          onlFormContentFormHeight.value = entry.contentRect.height;
          vxetableHeight.value = onlFormContentRef.value!.offsetHeight - onlFormContentFormHeight.value - 110;
        }
      });

      // Component con của biểu mẫu phần đầu ref
      const cgformHeadFormRef = ref<InstanceType<typeof CgformHeadForm>>();
      const isTree = ref<string>('N');
      // Proxy formAction cho ExtendConfigModal và useOnlineTest Sử dụng
      const formAction = {
        getFieldsValue: (fields?: string[]) => cgformHeadFormRef.value?.getFieldsValue(fields) ?? {},
        setFieldsValue: (values: Recordable) => cgformHeadFormRef.value?.setFieldsValue(values),
        validateFields: (_fields?: string[]) => Promise.resolve(),
        resetFields: () => cgformHeadFormRef.value?.resetFields(),
      };
      // Gán giá trị biểu mẫu
      const [registerModal, { closeModal }] = useModalInner((data) => {
        isUpdate.value = data?.isUpdate ?? false;
        if (isUpdate.value) {
          edit(data?.record);
        } else {
          add();
        }
      });
      // Tên bảng cũ
      const oldTableName = ref('');
      // Đồng bộ ngay lập tức tất cả table(bản chống rung debounce)
      const syncAllTableNowDebounce = simpleDebounce(() => syncAllTableNowPromise(), 150);
      // Dữ liệu tạm thờiID, không gửi lên backend
      let fieldTempIds: string[] = [];
      // Có hiển thị trường bảng phụ hay không
      let showSubTableStr = false;
      // Đã thêm trường biểu mẫu cây hay chưa
      let treeFieldAdded = false;
      // Trường biểu mẫu cây đã thêmID
      let treeFieldIds: string[] = [];
      let interval: any = null;

      // hook OnlineTest
      const { aiTestMode, aiTestTable, aiTableList, initVirtualData, tableJsonGetHelper, refreshCacheTableName } = useOnlineTest(
        {
          oldTableName,
          tables,
        },
        {
          initialAllShowItem,
          setAllTableData,
        },
        formAction
      );

      function add() {
        edit({});
      }

      async function edit(record) {
        confirmLoading.value = false;
        activeKey.value = 'dbTable';
        // Đặt lại biểu mẫu
        cgformHeadFormRef.value?.resetFields();
        model = Object.assign({}, record);
        initialAllShowItem(model);
        // Đặt lại nội dung định vị
        positioning.value = '';
        
        // thuận tiện cho việc ai test data Sao chép
        tableJsonGetHelper(model);

        cgformHeadFormRef.value?.initialExtConfigJson(model);
        cgformHeadFormRef.value?.setFieldsValue(model);
        oldTableName.value = model.tableName;
        // update-begin--author:liaozhiyang---date:20260330---for:【QQYUN-13610】Giải quyếtonlineVấn đề mở chậm
        // Giải quyết vấn đề tốc độ mở popup chậm (lần đầu hiển thị trì hoãn, các lần sau hiển thị trực tiếp)
        if (tabsInitialized) {
          hideTabs.value = false;
          // v-showỞ chế độ này component sẽ không bị hủy, cần xóa sạch dữ liệu cũ của mọi bảng trước
          clearAllTableData();
        } else {
          sleep(1, () => {
            hideTabs.value = false;
            tabsInitialized = true;
          });
        }
        // update-end--author:liaozhiyang---date:20260330---for:【QQYUN-13610】Giải quyếtonlineVấn đề mở chậm
        // updateChế độ, tải dữ liệu trong cơ sở dữ liệu
        if (isUpdate.value) {
          // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
          await getRefPromise(tables.dbTable);
          // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
          tables.dbTable.value?.setDataSource([]);
          await loadFields(model.id);
          // Tảiindexdữ liệu của, do mặc định không nhìn thấy nên có thể tải tách ra, nâng cao hiệu quả tải
          await loadIndexes(model.id);
          // Thiết lập chọn dropdown của bảng con
          getRefPromise(tables.pageTable).then(() => {
            tables.pageTable.value!.changePageType(model.tableType == 3, model.relationType);
          });
        } else {
          // Thêm dữ liệu khởi tạo
          let { initialData, tempIds } = useInitialData();
          await setAllTableData(initialData, true);
          fieldTempIds = tempIds;
        }
      }

      // Tải cấu hình trường của bảng
      async function loadFields(headId) {
        tableLoading.value = true;
        try {
          let fields = await formApi.doQueryField(headId);
          // thuận tiện cho việc ai test data Sao chép
          console.log("online fields:", fields)
          tableLoading.value = false;
          await setAllTableData(fields);
        } finally {
          tableLoading.value = false;
        }
      }

      // Tải cấu hình chỉ mục của bảng
      async function loadIndexes(headId) {
        let indexes = await formApi.doQueryIndexes(headId);
        // thuận tiện cho việc ai test data Sao chép
        console.log("online indexs:", indexes)
        // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
        await getRefPromise(tables.idxTable);
        // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
        tables.idxTable.value!.setDataSource(indexes);
      }

      function initialAllShowItem(model) {
        treeFieldAdded = model.isTree == 'Y';
        showSubTableStr = model.tableType === 2;
        // update-begin--author:liaozhiyang---date:20260713---for：【LHZP-68】Khi là bảng câyhas_childVô hiệu hóa, khi không phải bảng câyhas_childTrường không bị vô hiệu hóa
        isTree.value = model.isTree || 'N';
        // update-end--author:liaozhiyang---date:20260713---for：【LHZP-68】Khi là bảng câyhas_childVô hiệu hóa, khi không phải bảng câyhas_childTrường không bị vô hiệu hóa
      }
      // update-begin--author:liaozhiyang---date:20260330---for:【QQYUN-13610】Giải quyếtonlineVấn đề mở chậm
      // Xóa sạch dữ liệu của mọi bảng (v-showỞ chế độ này, gọi trước khi chuyển dữ liệu, tránh cái cũIDkhớp nối báo lỗi)
      function clearAllTableData() {
        const { dbTable, pageTable, checkTable, fkTable, idxTable, queryTable } = tables;
        dbTable.value?.setDataSource([]);
        pageTable.value?.setDataSource([]);
        checkTable.value?.setDataSource([]);
        fkTable.value?.setDataSource([]);
        idxTable.value?.setDataSource([]);
        queryTable.value?.setDataSource([]);
      }
      // update-end--author:liaozhiyang---date:20260330---for:【QQYUN-13610】Giải quyếtonlineVấn đề mở chậm

      // Thiết lập mọi thứ ngoài cấu hình chỉ mụcJVxeTabledữ liệu của
      async function setAllTableData(data: Recordable[], insert?) {
        const { dbTable, pageTable, checkTable, fkTable, queryTable } = tables;
        // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
        await getRefPromise(dbTable);
        // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
        dbTable.value!.setDataSource(data, insert);
        // Tải cái đầu tiên trướctabdữ liệu của, tải trì hoãn các kháctab, có thể làm hiệu quả thị giác của tốc độ mở tốt hơn
        // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
        setTimeout(async () => {
          await Promise.all([
            getRefPromise(pageTable),
            getRefPromise(checkTable),
            getRefPromise(fkTable),
            getRefPromise(queryTable),
          ]);
          // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable、vxetableTải theo nhu cầu
          pageTable.value!.setDataSource(data, insert);
          checkTable.value!.setDataSource(data, insert);
          fkTable.value!.setDataSource(data, insert);
          queryTable.value!.setDataSource(data, insert);
        }, 10);
      }

      /** ATabSự kiện chuyển đổi */
      function onTabsChange(activeKey) {
        // Khi đã chuyển tab, chỉ đồng bộ sửa đổi phần hiện đang nhìn thấytable
        if (['pageTable', 'checkTable', 'fkTable', 'idxTable', 'queryTable'].indexOf(activeKey) !== -1) {
          const dbTable = tables.dbTable;
          const table = tables[activeKey];
          dbTable.value!.tableRef!.resetScrollTop();
          // update-begin--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetableTối ưu
          clearInterval(interval);
          interval = setTimeout(() => {
            table.value.syncTable(dbTable);
          }, 200);
          // update-end--author:liaozhiyang---date:20260316---for:【QQYUN-13751】jVxetableTối ưu
        }
      }

      // Loại bảngtableTypetrườngchangesự kiện (do component con emit Kích hoạt,formModel đã cập nhật trong component con)
      function onTableTypeChange(value) {
        const relationType = cgformHeadFormRef.value?.formModel.relationType ?? 0;
        tables.pageTable.value!.changePageType(value == 3, relationType);
      }
      // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
      // Loại liên kếtrelationTypetrườngchangesự kiện
      function onRelationTypeChange(value) {
        const tableType = cgformHeadFormRef.value?.formModel.tableType ?? 1;
        tables.pageTable.value!.changePageType(tableType == 3, value);
      }
      // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác

      // Có phải cây hay khôngisTreetrườngchangesự kiện
      function onIsTreeChange(value) {
        isTree.value = value;
        value === 'Y' ? addTreeNeedField() : deleteTreeNeedField();
      }

      /** Đồng bộ chủ động ngay lập tức tất cảtable */
      function syncAllTableNow() {
        syncAllTableNowDebounce();
      }

      // Đồng bộ ngay lập tức tất cả table
      async function syncAllTableNowPromise() {
        let { dbTable, pageTable, checkTable, fkTable, queryTable } = tables;
        await pageTable.value!.syncTable(dbTable);
        await checkTable.value!.syncTable(dbTable);
        await fkTable.value!.syncTable(dbTable);
        await queryTable.value!.syncTable(dbTable);
      }

      // update-begin--author:liaozhiyang---date:20260414---for：【QQYUN-15128】Khi thêm trường mới, trường hệ thống luôn nằm ở dưới cùng
      const SYS_BUILT_IN_FIELDS = ['create_by', 'create_time', 'update_by', 'update_time', 'sys_org_code'];
      // update-end--author:liaozhiyang---date:20260414---for：【QQYUN-15128】Khi thêm trường mới, trường hệ thống luôn nằm ở dưới cùng

      // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-1858】onlineDữ liệu cũ của bảng, trường thêm mới, thêm ở phía dưới trường tùy chỉnh của người dùng
      /** Bố cục dữ liệu cũ: trường hệ thống ở phía trước, phía sau còn có trường tùy chỉnh của người dùng (khi thêm mới không nên chèn vào trước trường hệ thống, tránh làm tan rã các trường tùy chỉnh) */
      function hasCustomFieldsAfterSystemFields(fullData: Recordable[]) {
        const lastSysIndex = fullData.reduce(
          (max, row, index) => (SYS_BUILT_IN_FIELDS.includes(row.dbFieldName) ? Math.max(max, index) : max),
          -1,
        );
        if (lastSysIndex === -1) {
          return false;
        }
        const lastIndex = fullData.length - 1;
        for (let i = lastSysIndex + 1; i < lastIndex; i++) {
          const name = fullData[i]?.dbFieldName;
          if (name && name !== 'id' && !SYS_BUILT_IN_FIELDS.includes(name)) {
            return true;
          }
        }
        return false;
      }
      // update-end--author:liaozhiyang---date:20260807---for：【LHZP-1858】onlineDữ liệu cũ của bảng, trường thêm mới, thêm ở phía dưới trường tùy chỉnh của người dùng

      // update-begin--author:liaozhiyang---date:20260506---for:【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau
      let onTableAddedQueue: Promise<unknown> = Promise.resolve();
      // update-end--author:liaozhiyang---date:20260506---for:【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau

      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (bổ sung loại truy vấn mặc định cho hàng thuộc tính trang được chèn trực tiếp)
      /** Khi thêm hàng thuộc tính trang, chỉ khi chưa cấu hình rõ loại truy vấn String Áp dụng giá trị mặc định truy vấn mờ trên ô văn bản. */
      function withPageQueryModeDefault(row: Recordable) {
        const isTextControl = !row.fieldShowType || row.fieldShowType === 'text';
        if (row.dbType === 'string' && isTextControl && !row.queryMode) {
          return { ...row, queryMode: 'like' };
        }
        return row;
      }
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (bổ sung loại truy vấn mặc định cho hàng thuộc tính trang được chèn trực tiếp)

      /** Khi có thêm mới thì nên đồng bộ ngay lập tức */
      function onTableAdded() {
        // update-begin--author:liaozhiyang---date:20260506---for: 【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau
        onTableAddedQueue = onTableAddedQueue.then(async () => {
          // update-begin--author:liaozhiyang---date:20260414---for：【QQYUN-15128】Khi thêm hàng mới, trường hệ thống luôn ở dưới cùng (khi trường hệ thống bị xóa thì suy biến thành thêm vào thông thường)
          const dbJVxeRef = tables.dbTable.value?.tableRef;
          if (dbJVxeRef) {
            const fullData = dbJVxeRef.getXTable().internalData.tableFullData;
            const sysIndex = fullData.findIndex((row) => SYS_BUILT_IN_FIELDS.includes(row.dbFieldName));
            const lastIndex = fullData.length - 1;
            // Ở cuối là hàng thêm mới, và nằm sau trường hệ thống, đồng thời bản thân không phải trường hệ thống
            if (sysIndex !== -1 && lastIndex > sysIndex && !SYS_BUILT_IN_FIELDS.includes(fullData[lastIndex]?.dbFieldName)) {
              // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-1858】onlineDữ liệu cũ của bảng, trường thêm mới, thêm ở phía dưới trường tùy chỉnh của người dùng
              // Dữ liệu cũ: trường hệ thống ở trước, tùy chỉnh ở sau, trường thêm mới giữ ở cuối
              if (hasCustomFieldsAfterSystemFields(fullData)) {
                syncAllTableNow();
                return;
              }
              // update-end--author:liaozhiyang---date:20260807---for：【LHZP-1858】onlineDữ liệu cũ của bảng, trường thêm mới, thêm ở phía dưới trường tùy chỉnh của người dùng
              const newRowData = { ...fullData[lastIndex] };
              // tại dbTable chuyển hàng mới ở cuối tới trước trường hệ thống đầu tiên trong đó
              await dbJVxeRef.rowResort(lastIndex, sysIndex);
              // Đối với cái khác tables Chèn trực tiếp vào phía trước trường hệ thống, không đi qua syncAllTableNow logic bổ sung thêm của
              const { pageTable, checkTable, fkTable, queryTable } = tables;
              for (const t of [pageTable, checkTable, fkTable, queryTable]) {
                const jvxeRef = t.value?.tableRef;
                if (!jvxeRef) continue;
                const tFullData = jvxeRef.getXTable().internalData.tableFullData;
                const tSysIndex = tFullData.findIndex((row) => SYS_BUILT_IN_FIELDS.includes(row.dbFieldName));
                // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi thêm mới trước trường hệ thống, thiết lập loại truy vấn mặc định cho thuộc tính trang)
                const targetRowData = t === pageTable ? withPageQueryModeDefault(newRowData) : newRowData;
                // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi thêm mới trước trường hệ thống, thiết lập loại truy vấn mặc định cho thuộc tính trang)
                // nối tiếp (serial) await Bảng phụ insertRows/addRows, bảo đảm tác vụ hàng đợi lần sau đọc được trạng thái đã kết thúc hoàn toàn
                if (tSysIndex !== -1) {
                  await jvxeRef.insertRows(targetRowData, tSysIndex);
                } else {
                  await jvxeRef.addRows(targetRowData);
                }
              }
              return;
            }
          }
          // update-end--author:liaozhiyang---date:20260414---for：【QQYUN-15128】Khi thêm hàng mới, trường hệ thống luôn ở dưới cùng (khi trường hệ thống bị xóa thì suy biến thành thêm vào thông thường)
          syncAllTableNow();
        });
        // Dự phòng: bất thường của hàng đợi không chặn các lần gọi tiếp theo
        onTableAddedQueue = onTableAddedQueue.catch((e) => {
          console.error('[onTableAdded] queue error:', e);
        });
        // update-end--author:liaozhiyang---date:20260506---for: 【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau
      }

      /** Khi xóa cũng nên đồng bộ ngay lập tức */
      function onTableRemoved() {
        syncAllTableNow();
      }

      /** Khi sau khi kéo thì đồng bộ ngay lập tức */
      function onTableDragged(event) {
        let { oldIndex, newIndex } = event;
        syncAllOrderNumNow(oldIndex, newIndex);
      }

      /** Khi sau khi chèn thì đồng bộ ngay lập tức */
      async function onTableInserted(event) {
        let { insertIndex, row } = event;
        let { pageTable, checkTable, fkTable, queryTable } = tables;
        pageTable.value!.tableRef!.insertRows(withPageQueryModeDefault(row), insertIndex);
        checkTable.value!.tableRef!.insertRows(row, insertIndex);
        fkTable.value!.tableRef!.insertRows(row, insertIndex);
        queryTable.value!.tableRef!.insertRows(row, insertIndex);
      }

      /** Đồng bộ ngay lập tức thứ tự sắp xếp của mọi bảng */
      function syncAllOrderNumNow(oldIndex: number, newIndex: number) {
        let { pageTable, checkTable, fkTable, queryTable } = tables;
        pageTable.value!.tableRef!.rowResort(oldIndex, newIndex);
        checkTable.value!.tableRef!.rowResort(oldIndex, newIndex);
        fkTable.value!.tableRef!.rowResort(oldIndex, newIndex);
        queryTable.value!.tableRef!.rowResort(oldIndex, newIndex);
      }

      /** KhivalueĐồng bộ khi thay đổi date */
      function onTableSyncDbType(event) {
        tables.pageTable.value!.syncFieldShowType(event.row);
      }
      // update-begin--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi loại trường cơ sở dữ liệu thay đổi, đồng bộ loại truy vấn của trang)
      /** Khi loại trường cơ sở dữ liệu thay đổi, đồng bộ loại truy vấn mặc định của thuộc tính trang. */
      function onTableSyncQueryMode(event) {
        tables.pageTable.value!.syncQueryMode(event);
      }
      // update-end--author:liaozhiyang---date:20260824---for:【LHZP-160】Loại trường làStringvà control là ô văn bản thì mặc định truy vấn mờ (khi loại trường cơ sở dữ liệu thay đổi, đồng bộ loại truy vấn của trang)
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng
      /** KhidbIsPersist(đồng bộ cơ sở dữ liệu) valueĐồng bộ khi thay đổi Truy vấn bỏ tích chọn */
      function onTableSyncDbIsPersist(event) {
        tables.pageTable.value!.syncIsQuery(event.row);
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Trường không đồng bộ cơ sở dữ liệu thì bỏ dấu chọn truy vấn tương ứng
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng
      /** KhidbIsNull(không cho phép giá trị rỗng) valueĐồng bộ khi thay đổi Kiểm chứng mục bắt buộc điền */
      function onTableSyncDbIsNull(event) {
        tables.checkTable.value!.syncFieldMustInput(event.row);
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8485】Cơ sở dữ liệu không cho phép rỗng, kiểm chứng mặc định tích chọn tương ứng

      function onTableQuery(id) {
        tables.pageTable.value!.enableQuery(id);
      }

      /** Thêm trường cây */
      function addTreeNeedField() {
        if (!treeFieldAdded) {
          let { dbTable, pageTable, checkTable } = tables;
          let treeFields = useTreeNeedFields();
          treeFields = treeFields.filter((item: any) => {
            let nameList = dbTable.value!.tableRef!.getTableData().map((o) => o.dbFieldName);
            return !nameList.includes(item.dbFieldName);
          });
          treeFieldIds = [];
          treeFields.forEach((newData: any) => {
            let uuidTemp = buildUUID() + '__tempId';
            treeFieldIds.push(uuidTemp);
            newData.id = uuidTemp;
          });
          dbTable.value!.tableRef!.addRows(treeFields, { setActive: false });
          // update-begin--author:liaozhiyang---date:20260506---for: 【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau
          // pageTable.value!.tableRef!.addRows(treeFields, { setActive: false });
          // checkTable.value!.tableRef!.addRows(treeFields, { setActive: false });
          // update-end--author:liaozhiyang---date:20260506---for: 【issues/9593】Trong thuộc tính trang của bảng cây, thuộc tính của nút con và nút cha bị lẫn với nhau
          nextTick(() => syncAllTableNow());
          treeFieldAdded = true;
        }
        nextTick(() => {
          if (cgformHeadFormRef.value) {
            cgformHeadFormRef.value.formModel.treeIdField = 'has_child';
            cgformHeadFormRef.value.formModel.treeParentIdField = 'pid';
          }
        });
      }

      /** Xóa trường cây */
      function deleteTreeNeedField() {
        if (treeFieldIds && treeFieldIds.length > 0) {
          let { dbTable } = tables;
          dbTable.value!.tableDeleteLines(treeFieldIds);
          treeFieldIds = [];
          treeFieldAdded = false;
        }
      }

      // Kích hoạt mọi kiểm chứng biểu mẫu
      function validateAll() {
        let options = {};
        return new Promise((resolve, reject) => {
          // Xác thực biểu mẫu bảng chính
          cgformHeadFormRef.value!.validate().then(
            (values) => resolve({ values }),
            (errMsg) => {
              if (errMsg) $message.warning(errMsg);
              reject(VALIDATE_FAILED);
            }
          );
        })
          .then((result) => {
            Object.assign(options, result);
            return validateTableFields();
          })
          .then((allTableData) => {
            Object.assign(options, allTableData);
            let formData = classifyIntoFormData(options);
            return validateForeignKey(formData);
          })
          .catch((e) => {
            if (e === VALIDATE_FAILED || e?.code === VALIDATE_FAILED) {
              // Khi kiểm chứng biểu mẫu thất bại, thông báo đã ở validate() đã nhắc ở bên trong, tại đây không lặp lại
            } else {
              // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7503】Bảng phụ cấu hình nhiều khóa ngoại, lưu thất bại mà không có nhắc nhở
              e?.msg ? $message.warning(e.msg) : console.error(e);
              // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7503】Bảng phụ cấu hình nhiều khóa ngoại, lưu thất bại mà không có nhắc nhở
            }
            return Promise.reject(null);
          });
      }

      /** Xác thực đồng thời lấy dữ liệu của mọi bảng */
      function validateTableFields() {
        return new Promise(async (resolve, reject) => {
          let tableKeys = Object.keys(tables);
          let allTableData: any = {};
          for (let i = 0; i < tableKeys.length; i++) {
            let key = tableKeys[i];
            let table = tables[key];
            try {
              allTableData[key] = await table.value!.validateData(key);
            } catch (e: any) {
              if (e.code === VALIDATE_FAILED) {
                // Chưa thông qua thì chuyển tới tương ứngtabThẻ tab
                activeKey.value = e.activeKey;
              } else {
                console.error(e);
              }
              reject(e);
              return;
            }
          }
          resolve(allTableData);
        });
      }

      /** Sắp xếp và tích hợp dữ liệu của tất cả các bảng thành dạng mà backend nhận diện đượcformData */
      function classifyIntoFormData(options) {
        // Sắp xếp dữ liệu
        let formData = {
          head: {} as Recordable,
          fields: [] as any[],
          indexs: [] as any[],
          deleteFieldIds: [] as any[],
          deleteIndexIds: [] as any[],
        };
        formData.head = Object.assign(model, options.values);
        // update-begin--author:liaozhiyang---date:20260401---for：【QQYUN-14949】onlineTrong cấu hình hiển thị càng nhiều càng tốtvxetabletrường
        formData.head.formCategory = 'temp';
        formData.head.idType = 'UUID';
        // update-end--author:liaozhiyang---date:20260401---for：【QQYUN-14949】onlineTrong cấu hình hiển thị càng nhiều càng tốtvxetabletrường
        // Sắp xếp, sắp xếp lạionlineMở rộng biểu mẫuJSON
        const extConfigJson = { ...cgformHeadFormRef.value?.getExtConfigJson() };
        formData.head.isDesForm = extConfigJson.isDesForm;
        formData.head.desFormCode = extConfigJson.desFormCode;
        delete extConfigJson.isDesForm;
        delete extConfigJson.desFormCode;
        // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu:dbSource Lưu bền vững (persist) tới onl_cgform_head.db_source cột
        formData.head.dbSource = extConfigJson.enableMultiDataSource ? extConfigJson.dbSource || '' : '';
        delete extConfigJson.dbSource;
        delete extConfigJson.enableMultiDataSource;
        // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
        formData.head.extConfigJson = JSON.stringify(extConfigJson);
        // Sắp xếp, sắp xếp lại fields
        options.dbTable.tableData.forEach((item, index) => {
          // ID bằng dbTable của ID làm chuẩn
          let rowId = item.id;
          let fields = Object.assign({}, item);

          let pageTable = options.pageTable.tableData[index];
          fields = Object.assign(pageTable, fields);

          let checkTable = options.checkTable.tableData[index];
          fields = Object.assign(checkTable, fields);

          let fkTable = options.fkTable.tableData[index];
          fields = Object.assign(fkTable, fields);

          let queryTable = options.queryTable.tableData[index];
          fields = Object.assign(queryTable, fields);

          // Nếu dbTable Không có trả vềid, thì có nghĩa là dữ liệu thêm mới
          if (rowId == null || rowId === '') {
            delete fields.id;
          } else {
            fields.id = rowId;
          }
          // Bỏ phần tạm thờiID
          let tempIds = ([] as string[]).concat(fieldTempIds, treeFieldIds);
          if (tempIds.includes(fields.id)) {
            delete fields.id;
          }
          formData.fields.push(fields);
        });
        formData.deleteFieldIds = options.dbTable.deleteIds;
        // Sắp xếp, sắp xếp lại index
        formData.indexs = options.idxTable.tableData;
        formData.deleteIndexIds = options.idxTable.deleteIds;
        return formData;
      }

      /** Kiểm chứng cấu hình khóa ngoại Chỉ có thể cấu hình một cái */
      function validateForeignKey(formData) {
        // 1.Cấu hình hai cái Kiểm chứng
        // 2.Sau khi cấu hình một cái, lưu, rồi cấu hình cái mới Xóa cái cũ Kiểm chứng
        // 3.Sau khi cấu hình một cái, lưu,Sửa cái hiện tại thành cái mới Kiểm chứng
        return new Promise((resolve, reject) => {
          let fields = formData.fields;
          let saved = true;
          if (fields && fields.length > 0) {
            let hasForeignKey = 0;
            for (let i = 0; i < fields.length; i++) {
              if (fields[i].mainField || fields[i].mainTable) {
                hasForeignKey += 1;
              }
              if (hasForeignKey > 1) {
                saved = false;
                break;
              }
            }
          }
          if (saved) {
            resolve(formData);
          } else {
            reject({
              code: -1,
              msg: 'Khóa ngoại chỉ cho phép cấu hình một cái!',
              error: VALIDATE_FAILED,
            });
          }
        });
      }

      // Sự kiện gửi biểu mẫu
      function onSubmit() {
        confirmLoading.value = true;
        validateAll()
          .then(
            async (formData: any) => {
              // Trường bảng chuyển thành chữ thường
              if (formData.fields && formData.fields.length > 0) {
                for (let field of formData.fields) {
                  field.dbFieldName = field.dbFieldName.toLowerCase().trim();
                }
              }
              if (formData.head?.tableName) {
                formData.head.tableName = formData.head.tableName.toLowerCase().trim();
              }
              // Khởi tạo yêu cầu
              await formApi.doSaveOrUpdate(formData, isUpdate.value);
              refreshCacheTableName(oldTableName.value, formData.head['tableName']);
              emit('success');
              // Giải quyết vấn đề popup nhấp nháy một cái khi đóng, vì đồng thời tải nhiềuJVxeTableSự giật lag gây ra đã ảnh hưởng hiệu quả đóng popup
              sleep(1, () => onCancel());
            },
            (e) => {
              console.error(e);
            }
          )
          .finally(() => {
            confirmLoading.value = false;
          });
      }

      //update-begin-author:taoyan date:2022-8-15 for: VUEN-1891 onlineKhi chỉnh sửa biểu mẫu Đã sửa cấu hình mở rộng, có thể hay không Xác nhận là lưu ngay, không cần nhấp xác nhận của toàn bộ biểu mẫu nữa
      async function onExtConfigSaved(values) {
        if (isUpdate.value) {
          const params = {
            id: model.id,
            extConfigJson: JSON.stringify(toRaw(values)),
          };
          await formApi.editHead(params);
          emit('success');
        }
      }
      //update-end-author:taoyan date:2022-8-15 for: VUEN-1891 onlineKhi chỉnh sửa biểu mẫu Đã sửa cấu hình mở rộng, có thể hay không Xác nhận là lưu ngay, không cần nhấp xác nhận của toàn bộ biểu mẫu nữa

      function onCancel() {
        hideTabs.value = true;
        // Giải quyết vấn đề popup nhấp nháy một cái khi đóng, vì đồng thời tải nhiềuJVxeTableSự giật lag gây ra đã ảnh hưởng hiệu quả đóng popup
        sleep(1, () => closeModal());
      }
      /**
       * 2024-07-17
       * liaozhiyang
       * 【TV360X-829】Định vị nhanh tới hàng theo tên từ điển và ghi chú trường
       * */
      const handlePositioning = () => {
        const val = positioning.value.trim();
        if (val.length) {
          const jVxe_instance = tables[activeKey.value].value.tableRef;
          const vxe_instance = jVxe_instance.getXTable();
          const fullData = vxe_instance.getTableData().fullData;
          // Chính xác trước, mờ sau
          const preciseIndex = fullData.findIndex((item) => val === item.dbFieldName || val === item.dbFieldTxt);
          let index = -1;
          if (preciseIndex == -1) {
            // Mờ (fuzzy)
            const dimIndex = fullData.findIndex((item) => item.dbFieldName.includes(positioning.value) || item.dbFieldTxt.includes(positioning.value));
            index = dimIndex;
          } else {
            index = preciseIndex;
          }
          if(index != -1) {
            const row = fullData[index];
            vxe_instance.scrollToRow(row).then(() => {
              const { refTableBody } = vxe_instance.getRefMaps();
              const tableBody = refTableBody.value;
              const bodyElem = tableBody ? tableBody.$el : null;
              if (bodyElem) {
                const trElem = bodyElem.querySelector(`[rowid="${vxe_instance.getRowid(row)}"]`);
                if (trElem) {
                  trElem.classList.add('customHighlight');
                  setTimeout(() => {
                    trElem?.classList.remove('customHighlight');
                  }, 1e3);
                }
              }
              // update-begin--author:liaozhiyang---date:20260330---for：【QQYUN-15058】Giải quyết vấn đề định vị trường không nhìn thấy được trên màn hình nhỏ
              // Cuộn thanh cuộn dọc của popup xuống tận dưới cùng
              const spinEl = spinRef.value?.$el || spinRef.value;
              const scrollContainer = spinEl?.closest('.scrollbar__wrap');
              if (scrollContainer) {
                scrollContainer.scrollTop = scrollContainer.scrollHeight;
              }
              // update-end--author:liaozhiyang---date:20260330---for：【QQYUN-15058】Giải quyết vấn đề định vị trường không nhìn thấy được trên màn hình nhỏ
            });
          } else {
            $message.warning('Không tìm thấy trường khớp');
          }
        } else {
          $message.warning('Vui lòng nhập trường hoặc ghi chú');
        }
      };
      function getRefPromise(componentRef) {
        return new Promise((resolve) => {
          function next() {
            let ref = componentRef.value;
            if (ref?.tableRef?.getXTable) {
              resolve(ref);
            } else {
              requestAnimationFrame(next);
            }
          }
          next();
        });
      }
      return {
        ...tables,
        modalRef,
        onlFormContentRef,
        onlFormContentFormRef,
        onlFormContentFormHeight,
        spinRef,
        title,
        confirmLoading,
        tableLoading,
        activeKey,
        onCancel,
        formAction,
        hideTabs,
        onSubmit,
        onTabsChange,
        onTableAdded,
        onTableRemoved,
        onTableDragged,
        onTableInserted,
        onTableSyncDbType,
        onTableSyncQueryMode,
        onTableQuery,
        onExtConfigSaved,
        registerModal,
        // hook OnlineTest
        aiTestMode,
        aiTestTable,
        aiTableList,
        initVirtualData,
        onTableSyncDbIsPersist,
        onTableSyncDbIsNull,
        isUpdate,
        isTree,
        positioning,
        handlePositioning,
        oldTableName,
        onIsTreeChange,
        // Component con của biểu mẫu phần đầu
        cgformHeadFormRef,
        onTableTypeChange,
        onRelationTypeChange,
        inputMethod,
        onInputKeyDown,
      };
    },
  });
</script>

<style lang="less" scoped>
  .onlForm-content {
    height: 100%;
  }
  .query-help-icon {
    color: #a8b0bb;
  }
  //update-begin-author:liaozhiyang date:2026-05-12 for: 【QQYUN-9789】Khi nhấp chuột thì bỏ đi vxe-table khung màu xanh của, điều hướng bằng bàn phím (Tab/Enter/phím mũi tên) thì hiển thị bình thường
  .onlForm-content[data-input-method='mouse'] {
    :deep(.vxe-body--column.col--selected) {
      box-shadow: none;
    }
  }
  //update-end-author:liaozhiyang date:2026-05-12 for:【QQYUN-9789】Khi nhấp chuột thì bỏ đi vxe-table khung màu xanh của, điều hướng bằng bàn phím (Tab/Enter/phím mũi tên) thì hiển thị bình thường
  // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-829】Định vị nhanh tới hàng theo tên từ điển và ghi chú trường
  .footer-area {
    display: flex;
    justify-content: space-between;
    flex-direction: row-reverse;
    .leftArea {
      display: flex;
      > * {
        &:not(:first-child) {
          margin-left: 16px;
        }
      }
    }
  }

  .positioning-area {
    width: 280px;
    display: flex;
    > :first-child {
      margin-right: 8px;
    }
  }
  :deep(.vxe-table) {
    .vxe-body--row.customHighlight {
      background-color: var(--vxe-ui-table-row-hover-background-color);
    }
  }
  // update-begin--author:liaozhiyang---date:20240717---for：【TV360X-829】Định vị nhanh tới hàng theo tên từ điển và ghi chú trường
</style>
