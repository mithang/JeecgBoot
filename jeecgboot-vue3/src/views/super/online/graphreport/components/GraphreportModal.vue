<template>
  <BasicModal
    :title="title"
    :width="1200"
    :maskClosable="false"
    :confirmLoading="confirmLoading"
    defaultFullscreen
    v-bind="$attrs"
    @cancel="onCancel"
    @register="registerModal"
  >
    <a-spin :spinning="confirmLoading">
      <BasicForm @register="registerForm">
        <template #SQLAnalyzeButton>
          <div style="flex: 1; text-align: right">
            <a-button v-if="dataType === 'sql'" type="primary" @click="onSQLAnalyze">SQLPhân tích (parse)</a-button>
            <a-button v-if="dataType === 'json'" type="primary" @click="onJSONAnalyze">JSONPhân tích (parse)</a-button>
            <a-button v-if="dataType === 'api'" type="primary" @click="onAPIAnalyze">APIPhân tích (parse)</a-button>
          </div>
        </template>
      </BasicForm>
      <!-- Vùng biểu mẫu con -->
      <a-tabs animated v-model:activeKey="activeKey">
        <a-tab-pane tab="Trường danh sách" key="field" forceRender>
          <FieldTable ref="fieldTableRef" :dataSource="fieldTable.dataSource" :disableSearch="dataType === 'json'" />
        </a-tab-pane>
        <a-tab-pane tab="Tham số biểu đồ" key="params" forceRender>
          <ParamsTable ref="paramsTableRef" :dataSource="paramsTable.dataSource" />
        </a-tab-pane>
      </a-tabs>
    </a-spin>

    <template #footer>
      <a-button @click="onCancel">Đóng</a-button>
      <a-button type="primary" :loading="confirmLoading" preIcon="ant-design:save" @click="onSubmit">Lưu</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive, computed, nextTick, defineComponent } from 'vue';

  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useFormSchemas } from '../graphreport.data';
  import FieldTable from './tables/FieldTable.vue';
  import ParamsTable from './tables/ParamsTable.vue';
  import { defHttp } from '/@/utils/http/axios';
  import { isArray } from '/@/utils/is';
  import { parseField } from '/@/views/super/online/graphreport/graphreport.api';

  enum Api {
    add = '/online/graphreport/head/add',
    edit = '/online/graphreport/head/edit',
    sql = '/online/cgreport/head/parseSql',
  }

  enum FieldApi {
    list = '/online/graphreport/head/queryOnlGraphreportItemByMainId',
  }

  enum ParamsApi {
    list = '/online/graphreport/params/listByHeadId',
  }

  export default defineComponent({
    name: 'GraphreportModal',
    components: { ParamsTable, FieldTable, BasicModal, BasicForm },
    emits: ['register', 'success'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();
      // Có phải chế độ cập nhật hay không
      const isUpdate = ref(false);
      const fieldTableRef = ref<InstanceType<typeof FieldTable>>();
      const paramsTableRef = ref<InstanceType<typeof ParamsTable>>();
      // Giá trị lưu trữ khi chỉnh sửa
      let model: Recordable = {};
      const title = computed(() => (isUpdate.value ? 'Chỉnh sửa' : 'Thêm mới'));
      // tabsTrang đang hoạt động hiện tại
      const activeKey = ref('field');
      // Hiện tại có đang tải hay không
      const confirmLoading = ref(false);
      // Loại nguồn dữ liệu
      const dbSourceOptions = ref<any[]>([]);

      const fieldTable = reactive({
        dataSource: [],
      });
      const paramsTable = reactive({
        dataSource: [],
      });

      //  Popup đăng ký
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        isUpdate.value = data?.isUpdate ?? false;
        if (isUpdate.value) {
          await edit(data?.record);
        } else {
          await add();
        }

        // Khi đã truyền aigc khi đó, chứng tỏ là thông qua ai Báo cáo đã tạo
        if (data?.aigc) {
          console.log('top - data.aigc :', data.aigc)
          await setFieldsValue({
            ...data.aigc,
          });
          // Chờ đợi 0.5s sau đó, tự động thực thiSQLPhân tích (parse)
          setTimeout(() => onSQLAnalyze())
        }
      });
      const { formSchemas, dataType, isCombination } = useFormSchemas(props, {
        dbSourceOptions,
      });
      // Biểu mẫu đăng ký
      const [registerForm, { resetFields, clearValidate, setFieldsValue, validate, validateFields }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      async function add() {
        // Xóa sạch dữ liệu
        fieldTable.dataSource = [];
        paramsTable.dataSource = [];
        await nextTick();
        // Mặc định thêm một hàng
        fieldTableRef.value?.tableRef?.addRows({}, { setActive: false });
        edit({});
      }

      async function edit(record) {
        confirmLoading.value = false;
        activeKey.value = 'field';
        // Đặt lại biểu mẫu
        await resetFields();
        model = Object.assign({}, record);
        model.dbSource = model.dbSource == null ? '' : model.dbSource;
        // Nếu là chế độ kết hợp, thì chia chuỗi thành mảng
        if (isCombination.value === 'combination' && typeof model.graphType === 'string') {
          model.graphType = model.graphType.split(',');
        }
        if (typeof model.yaxisField === 'string') {
          model.yaxisField = model.yaxisField.split(',');
        }
        // Gán giá trị cho biểu mẫu
        await setFieldsValue(model);
        await clearValidate();
        // Xử lý dữ liệu
        dataType.value = model.dataType ?? 'sql';
        // isCombination.value = model.isCombination ?? 'combination'
        // Tải dữ liệu bảng con
        if (model.id) {
          requestSubData(model.id);
        }
      }

      /** Truy vấn dữ liệu bảng con */
      function requestSubData(headId) {
        confirmLoading.value = true;
        requestTabData(FieldApi.list, { id: headId }, fieldTable).finally(() => {
          confirmLoading.value = false;
        });
        requestTabData(ParamsApi.list, { headId }, paramsTable);
      }

      /** Truy vấn một cái nào đótabdữ liệu của */
      function requestTabData(url, params, tab) {
        return defHttp.get({ url, params }).then((result) => {
          tab.dataSource = result || [];
        });
      }

      /** SQL Phân tích (parse) */
      async function onSQLAnalyze() {
        confirmLoading.value = true;
        try {
          let { cgrSql, dbSource } = await validate(['cgrSql', 'dbSource']);
          let result = await defHttp.get({ url: Api.sql, params: { sql: cgrSql, dbKey: dbSource || '' } });
          $message.success('Phân tích thành công');
          let newData = (result.fields || []).map((item) => {
            item.isShow = item.isShow === 1 ? 'Y' : 'N';
            return item;
          });
          let oldData = fieldTableRef.value?.tableRef?.getTableData();
          fieldTable.dataSource = getTableData(oldData, newData, 'fieldName');
          // Phân tích tham số
          paramsTable.dataSource = getTableData(paramsTable.dataSource, result.params || [], 'paramName');
        } finally {
          confirmLoading.value = false;
        }
      }

      /** JSON Phân tích (parse) */
      async function onJSONAnalyze() {
        await doBackAnalyze('JSON');
      }

      /** API Phân tích (parse) */
      async function onAPIAnalyze() {
        await doBackAnalyze('API');
      }

      /**
       * Backend phân tích trường
       *
       * @param type Phân tích loại
       */
      async function doBackAnalyze(type: string) {
        let { cgrSql } = await validateFields(['cgrSql']);
        let result = await parseField(type, cgrSql);
        let fields = Array.isArray(result) ? result: result.fields;
        let newData = (fields || []).map((item) => {
          item.isShow = item.isShow === 1 ? 'Y' : 'N';
          return item;
        });
        let oldData = fieldTableRef.value?.tableRef?.getTableData();
        fieldTable.dataSource = getTableData(oldData, newData, 'fieldName');
        // Phân tích tham số
        if (Array.isArray(result?.params)) {
          paramsTable.dataSource = getTableData(paramsTable.dataSource, result.params, 'paramName');
        }
      }

      /** 【Viết lại】Sự kiện nhấp nút xác nhận */
      async function onSubmit() {
        try {
          // Kiểm chứng bảng chính
          let mainValues = await validate();
          // Bảng trường kiểm chứng
          let errMap = await fieldTableRef.value?.tableRef?.validateTable();
          if (errMap) {
            activeKey.value = 'field';
            return;
          }
          // Bảng tham số kiểm chứng
          errMap = await paramsTableRef.value?.tableRef?.validateTable();
          if (errMap) {
            activeKey.value = 'params';
            return;
          }
          // Sắp xếp dữ liệu
          let formData = classifyIntoFormData(mainValues);
          // Khởi tạo yêu cầu
          confirmLoading.value = true;
          await request(formData);
          emit('success');
          closeModal();
        } finally {
          confirmLoading.value = false;
        }
      }

      /** Khởi tạo yêu cầu, tự động phán đoán là thực thi thao tác thêm mới hay thao tác sửa đổi */
      function request(params) {
        if (isUpdate.value) {
          return defHttp.put({ url: Api.edit, params });
        } else {
          return defHttp.post({ url: Api.add, params });
        }
      }

      /** Sắp xếp thànhformData */
      function classifyIntoFormData(mainValues) {
        let main = Object.assign(model, mainValues);
        // update-begin--author:liaozhiyang---date:20260824---for：【LHZP-1055】onlineBiểu đồJSONCách thức vô hiệu hóa truy vấn
        const fieldData = fieldTableRef.value?.tableRef?.getTableData() || [];
        if (main.dataType === 'json') {
          fieldData.forEach((item) => (item.searchFlag = 'N'));
        }
        // update-end--author:liaozhiyang---date:20260824---for：【LHZP-1055】onlineBiểu đồJSONCách thức vô hiệu hóa truy vấn
        if (isArray(main.graphType)) {
          main.graphType = main.graphType.join(',');
        }
        if (isArray(main.yaxisField)) {
          main.yaxisField = main.yaxisField.join(',');
        }
        return {
          ...main,
          onlGraphreportItemList: fieldData,
          paramsList: paramsTableRef.value?.tableRef?.getTableData(),
        };
      }

      function onCancel() {
        closeModal();
      }

      // Truy vấn loại nguồn dữ liệu
      async function queryDataSourceOptions() {
        let url = '/sys/dataSource/options';
        try {
          let result = await defHttp.get({ url });
          dbSourceOptions.value = result || [];
          dbSourceOptions.value.unshift({ label: 'Vui lòng chọn', value: '' });
        } catch {
          dbSourceOptions.value = [{ label: 'Tải thất bại', value: undefined }];
        }
      }

      queryDataSourceOptions();

      return {
        fieldTableRef,
        paramsTableRef,
        title,
        confirmLoading,
        dataType,
        activeKey,
        fieldTable,
        paramsTable,
        onSubmit,
        onCancel,
        onSQLAnalyze,
        onAPIAnalyze,
        onJSONAnalyze,
        registerModal,
        registerForm,
      };
    },
  });

  /**
   * Lấytabcủatablecủadatasource
   * @param oldData gốcdatasource
   * @param newData Truy vấn ra từ cơ sở dữ liệudatasource
   * @param columnName Cột trường, dùng để so sánh dữ liệu có giống nhau hay không
   */
  function getTableData(oldData, newData, columnName) {
    newData.forEach((newItem) => {
      for (let oldItem of oldData) {
        if (oldItem[columnName] === newItem[columnName]) {
          Object.assign(newItem, oldItem);
          break;
        }
      }
    });
    return newData;
  }
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
  form {
    padding-right: 3.5%;
  }
  :deep(.ant-tabs-content-holder) {
    padding: 0 10px;
  }
  // update-begin--author:liaozhiyang---date:20240509---for：【QQYUN-9230】Điều chỉnh kiểu dáng popup biểu đồ báo cáo
</style>
