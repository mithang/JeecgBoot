<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    :width="1200"
    :maskClosable="false"
    :defaultFullscreen="true"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
  >
    <!-- update-begin--author:liaozhiyang---date:20260824---for：【LHZP-984】onlineBáo cáo thêm chức năng định vị -->
    <template #insertFooter>
      <div class="positioning-area">
        <a-input v-model:value="positioning" placeholder="Tìm kiếm trường hoặc ghi chú" allowClear @pressEnter="handlePositioning" />
        <a-button type="primary" ghost @click="handlePositioning">Định vị</a-button>
      </div>
    </template>
    <!-- update-end--author:liaozhiyang---date:20260824---for：【LHZP-984】onlineBáo cáo thêm chức năng định vị -->
    <BasicForm @register="registerForm" ref="formRef">
      <template #analyseButton>
        <div style="flex: 1; text-align: left">
          <a-popover title="Hướng dẫn sử dụng" trigger="hover" style="margin: 0 10px 0 6px">
            <template #content>
              Bạn có thể nhập “” làm một tham số, tại đâyabclà tên của tham số. Ví dụ:<br />
              select * from table where id = ${abc}。<br />
              select * from table where id like concat('%',${abc},'%')。(mysqlTruy vấn mờ)<br />
              select * from table where id like '%'||${abc}||'%'。(oracleTruy vấn mờ)<br />
              select * from table where id like '%'+${abc}+'%'。(sqlserverTruy vấn mờ)<br />
              <span style="color: red">Chú thích: tham số chỉ hỗ trợ báo cáo động,popupTạm thời chưa hỗ trợ</span>
            </template>
            <a-icon type="question-circle" />
          </a-popover>
          <a-button style="margin-left: 10px" type="primary" @click="handleSQLAnalyze">SQLPhân tích (parse)</a-button>
        </div>
      </template>
    </BasicForm>
    <a-divider style="margin: 1px 0" class="cust-divider"></a-divider>
    <!-- Vùng biểu mẫu con -->
    <a-tabs v-model:activeKey="activeKey" animated @change="handleChangeTabs">
      <a-tab-pane tab="Chi tiết cấu hình báo cáo động" :key="refKeys[0]" :forceRender="true">
        <JVxeTable
          keep-source
          dragSort
          resizable
          ref="onlCgreportItem"
          :loading="onlCgreportItemTable.loading"
          :columns="onlCgreportItemTable.columns"
          :dataSource="onlCgreportItemTable.dataSource"
          :height="390"
          :rowNumber="true"
          :rowSelection="true"
          dragSortFixed="none"
          rowNumberFixed="none"
          rowSelectionFixed="none"
          :toolbar="true"
          :scrollX="{ enabled: true, gt: 0 }"
          :scrollY="{ enabled: true, gt: 10 }"
        />
      </a-tab-pane>

      <a-tab-pane tab="Tham số báo cáo" :key="refKeys[1]" :forceRender="true">
        <JVxeTable
          keep-source
          resizable
          dragSort
          ref="onlCgreportParam"
          :loading="onlCgreportParamTable.loading"
          :columns="onlCgreportParamTable.columns"
          :dataSource="onlCgreportParamTable.dataSource"
          :height="390"
          :rowNumber="true"
          :rowSelection="true"
          dragSortFixed="none"
          rowNumberFixed="none"
          rowSelectionFixed="none"
          :toolbar="true"
          :scrollX="{ enabled: true, gt: 0 }"
          :scrollY="{ enabled: true, gt: 10 }"
        />
      </a-tab-pane>
    </a-tabs>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive, nextTick } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods';
  import { formSchema, onlCgreportParamColumns, onlCgreportItemColumns } from '../cgreport.data';
  import { saveOrUpdate, onlCgreportParamList, onlCgreportItemList, analyzeSql } from '../cgreport.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage, createInfoModal } = useMessage();
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  // Hiện tại có đang tải hay không
  const confirmLoading = ref(true);
  const refKeys = ref(['onlCgreportItem', 'onlCgreportParam']);
  const activeKey = ref('onlCgreportItem');
  const positioning = ref('');
  const onlCgreportParam = ref();
  const onlCgreportItem = ref();
  const tableRefs = { onlCgreportItem, onlCgreportParam };
  // Khi bảng chỉnh sửa hàng chưa sẵn sàng, khoảng chờ mỗi lần thử lại (mili giây)
  const TABLE_READY_RETRY_INTERVAL = 50;
  // Số lần thử lại tối đa khi khởi tạo bảng chỉnh sửa hàng, tránh chờ vô hạn
  const TABLE_READY_MAX_RETRY_COUNT = 50;
  const onlCgreportParamTable = reactive({
    loading: false,
    dataSource: <any[]>[],
    columns: onlCgreportParamColumns,
  });
  const onlCgreportItemTable = reactive({
    loading: false,
    dataSource: <any[]>[],
    columns: onlCgreportItemColumns,
  });
  //Cấu hình biểu mẫu
  const [registerForm, { setProps, resetFields, setFieldsValue, validate, validateFields }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
    // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
    labelWidth: 100,
    wrapperCol: null
    // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
  });
  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await reset();
    setModalProps({ confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
      requestSubTableData(onlCgreportParamList, { headId: data?.record?.id }, onlCgreportParamTable);
      requestSubTableData(onlCgreportItemList, { headId: data?.record?.id }, onlCgreportItemTable);
    }
    // Khi ẩn phần dưới cùng, vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter });

    // Khi đã truyền aigc khi đó, chứng tỏ là thông qua ai Báo cáo đã tạo
    if (data?.aigc) {
      const hideWaitLoading = createMessage.loading('Đang tạo trường báo cáo, vui lòng chờ...', 0);
      try {
        await setFieldsValue({
          ...data.aigc,
        });
        // Chờ bảng chỉnh sửa hàng tải lười khởi tạo hoàn tất, tự động thực thiSQLPhân tích (parse)
        if (await waitForTableReady()) {
          handleSQLAnalyze();
        }
      } finally {
        hideWaitLoading();
      }
    }
  });
  //Cấu hình phương thức
  const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(
    requestAddOrEdit,
    classifyIntoFormData,
    tableRefs,
    activeKey,
    refKeys
  );

  //Thiết lập tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));

  async function reset() {
    await resetFields();
    activeKey.value = 'onlCgreportItem';
    positioning.value = '';
    onlCgreportParamTable.dataSource = [];
    onlCgreportItemTable.dataSource = [];
  }

  // update-begin--author:liaozhiyang---date:20260824---for：【LHZP-984】onlineBáo cáo thêm chức năng định vị
  /** Định vị trường báo cáo theo tên trường hoặc văn bản trường */
  async function handlePositioning() {
    const value = positioning.value.trim();
    if (!value) {
      createMessage.warning('Vui lòng nhập trường hoặc ghi chú');
      return;
    }
    const xTable = onlCgreportItem.value?.getXTable?.();
    const fullData = xTable?.getTableData?.().fullData || [];
    const matches = (item, exact = false) => {
      const fieldName = String(item.fieldName || '');
      const fieldTxt = String(item.fieldTxt || '');
      return exact ? fieldName === value || fieldTxt === value : fieldName.includes(value) || fieldTxt.includes(value);
    };
    const row = fullData.find((item) => matches(item, true)) || fullData.find((item) => matches(item));
    if (!row) {
      createMessage.warning('Không tìm thấy trường khớp');
      return;
    }
    await xTable.scrollToRow(row);
    const tableBody = xTable.getRefMaps?.().refTableBody?.value;
    const rowElement = tableBody?.$el?.querySelector(`[rowid="${xTable.getRowid(row)}"]`);
    if (rowElement) {
      rowElement.classList.add('customHighlight');
      setTimeout(() => rowElement.classList.remove('customHighlight'), 1000);
    }
  }
  // update-end--author:liaozhiyang---date:20260824---for：【LHZP-984】onlineBáo cáo thêm chức năng định vị

  /** Chờ instance bảng chỉnh sửa hàng tải lười khởi tạo hoàn tất */
  async function waitForTableReady() {
    for (let retryCount = 0; retryCount < TABLE_READY_MAX_RETRY_COUNT; retryCount++) {
      await nextTick();
      if (onlCgreportItem.value?.getTableData && onlCgreportParam.value?.getTableData) {
        return true;
      }
      console.log('【AITạo báo cáo】Đang chờ instance bảng chỉnh sửa hàng khởi tạo hoàn tất, đang thử lại...', retryCount + 1);
      await new Promise((resolve) => setTimeout(resolve, TABLE_READY_RETRY_INTERVAL));
    }
    console.warn('【AITạo báo cáo】Chờ instance bảng chỉnh sửa hàng khởi tạo hoàn tất bị quá thời gian, vui lòng thực thi thủ côngSQLPhân tích (parse)');
    createInfoModal({
      title: 'Trường báo cáo tạm thời chưa hiển thị',
      content: 'Hệ thống không thể tự động tải trường báo cáo, vui lòng nhấp phía trên “SQLnút Phân tích” để thử lại.',
      okText: 'Đã hiểu',
    });
    return false;
  }
  function classifyIntoFormData(allValues) {
    let main = Object.assign({}, allValues.formValue);
    return {
      ...main, // Mở rộng
      onlCgreportParamList: allValues.tablesValue[1].tableData,
      onlCgreportItemList: allValues.tablesValue[0].tableData,
    };
  }
  //Sự kiện gửi biểu mẫu
  async function requestAddOrEdit(values) {
    try {
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      let params = [],
        items = [],
        head = {};
      Object.keys(values).map((k) => {
        if (k == 'onlCgreportItemList') {
          items = values[k];
        } else if (k == 'onlCgreportParamList') {
          params = values[k];
        } else {
          head[k] = values[k];
        }
      });
      let obj = { head, params, items };
      console.log('Tham số yêu cầu lưu cấu hình báo cáo', obj);
      await saveOrUpdate(obj, isUpdate.value);
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  //Phân tích (parse)SQL
  function handleSQLAnalyze() {
    setModalProps({ confirmLoading: true });
    validateFields(['cgrSql', 'dbSource'])
      .then((values) => {
        let { cgrSql, dbSource } = values;
        let urlParam = 'sql=' + encodeURIComponent(cgrSql);
        if (dbSource) {
          urlParam += '&dbKey=' + dbSource;
        }
        console.log('urlParam----', urlParam);
        analyzeSql(urlParam).then((res) => {
          if (res) {
            createMessage.success('Phân tích thành công');
            let { fields, params } = res;
            let newFields = fields.filter((item) => item.fieldName != '__row_number__');
            // từvxeTablelấy dữ liệu hiện tại trong đó
            let itemDataSource = onlCgreportItem.value.getTableData()
            // Hợp nhất dữ liệu backend
            let newItemDataList = getTabData(itemDataSource, newFields || [], 'fieldName');
            // Dữ liệu sắp xếp
            newItemDataList = newItemDataList.sort((a, b) => a.orderNum - b.orderNum);
            onlCgreportItemTable.dataSource = newItemDataList;

            let paramDataSource = onlCgreportParam.value.getTableData()
            let newParamList = getTabData(paramDataSource, params || [], 'paramName');
            newParamList = newParamList.sort((a, b) => a.orderNum - b.orderNum);
            onlCgreportParamTable.dataSource = newParamList;
          }
        });
      })
      .catch(() => {
        console.log('Phân tích thất bại');
      })
      .finally(() => {
        setModalProps({ confirmLoading: false });
      });
  }

  /**
   * Lấytabcủatablecủadatasource
   * @param old_arr gốcdatasource
   * @param new_arr Truy vấn ra từ cơ sở dữ liệudatasource
   * @param columnName Cột trường
   */
  function getTabData(old_arr: any[], new_arr: any[], columnName: string): any[] {
    if (old_arr.length > 0) {
      let need: any[] = [],
        field_arr: any[] = [],
        max_order = 1;
      for (let t of new_arr) {
        for (let o of old_arr) {
          if (o[columnName] == t[columnName]) {
            need.push(o);
            field_arr.push(t[columnName]);
            if (o.orderNum > max_order) {
              max_order = o.orderNum;
            }
            break;
          }
        }
      }
      for (let t of new_arr) {
        if (field_arr.indexOf(t[columnName]) < 0) {
          t.orderNum = ++max_order;
          need.push(t);
        }
      }
      return need;
    } else {
      let max_order = 0;
      for (let t of new_arr) {
        if (!t.orderNum) {
          t.orderNum = ++max_order;
        }
      }
      return new_arr;
    }
  }

  // Thiết lập thủ côngtabledữ liệu của Gọi sau khi phân tích
  async function setDataSource(tableRef, data) {
    await nextTick();
    await tableRef.value!.getXTable().insert(data);
    await nextTick();
  }
</script>
<style lang='less' scoped>
  // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
  form {
    padding-right: 22px;
  }
  :deep(.ant-tabs-content-holder) {
    padding: 0 10px;
  }
  // update-end--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
  
  // ==================== Tối ưu thanh cuộn - Bắt đầu ====================
  // Mục đích: làm đẹp thanh cuộn của bảng, giảm nhiễu thị giác
  // Hiệu quả: thanh cuộn mảnh hơn, kiểu dáng đẹp hơn
  // Thời gian:2024Tối ưu năm
  :deep(.vxe-table--body-wrapper) {
    // Kiểu dáng thanh cuộn tùy chỉnh
    &::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: #f1f1f1;
      border-radius: 3px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #c1c1c1;
      border-radius: 3px;
      
      &:hover {
        background: #a8a8a8;
      }
    }
    
    &::-webkit-scrollbar-corner {
      background: #f1f1f1;
    }
  }
  
  // Tối ưu container của bảng, đảm bảo nội dung không tràn ra ngoài
  :deep(.vxe-table) {
    .vxe-body--row.customHighlight {
      background-color: var(--vxe-ui-table-row-hover-background-color);
    }
    .vxe-table--body-wrapper {
      overflow: auto;
    }
  }

  .positioning-area {
    width: 280px;
    display: flex;
    gap: 8px;
    float: left;
  }
  // ==================== Tối ưu thanh cuộn - Kết thúc ====================
</style>
