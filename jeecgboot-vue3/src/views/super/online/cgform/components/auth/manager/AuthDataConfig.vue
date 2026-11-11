<template>
  <div>
    <BasicTable @register="registerTable" :loading="loading">
      <template #tableTitle>
        <a-button v-if="showAdd" @click="onAdd" type="primary" preIcon="ant-design:plus">Thêm mới</a-button>
      </template>

      <template #switch="{ record }">
        <a-switch size="small" :checked="record.status === 1" @click="() => onUpdateStatus(record)" />
      </template>

      <template #description="{ record }">
        <div class="rule-description">
          <template v-if="record.ruleOperator === USE_SQL_RULES">
            <span class="rule-operator rule-operator-sql">Tùy chỉnh SQL</span>
          </template>
          <template v-else>
            <span class="rule-field">{{ getRuleFieldLabel(record.ruleColumn) }}</span>
            <span class="rule-operator">{{ getRuleOperatorLabel(record.ruleOperator) }}</span>
          </template>
          <a-tooltip v-if="hasRuleValue(record)" :title="getRuleValueText(record)">
            <span class="rule-value" :class="{ 'rule-value-system': isSysVarValue(record) }">{{ getRuleValueText(record, true) }}</span>
          </a-tooltip>
        </div>
      </template>

      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!-- Biểu mẫu con -->
    <BasicModal v-bind="formModalProps" @open-change="handleOpenChange">
      <a-spin :spinning="formModalProps.confirmLoading">
        <BasicForm @register="registerForm" />
      </a-spin>
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { watch, computed, defineComponent, ref, reactive, nextTick } from 'vue';
  import { BasicTable, TableAction, ActionItem, useTable } from '/@/components/Table';
  import { BasicModal, useModal } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { ajaxGetDictItems, getDictItemsByCode } from '/@/utils/dict';
  import { defHttp } from '/@/utils/http/axios';
  import { authButtonLoadData, authDataDelete, authDataLoadData, authDataSaveOrUpdate, authDataUpdateStatus } from '../auth.api';
  import {
    authDataColumns,
    useAuthDataFormSchemas,
    USE_SQL_RULES,
    getRuleValueWidget,
    getRuleValueDictCode,
    getRuleValueDisplayText,
    isFuzzyRuleOperator,
    getPopupDictConfig,
    getSysVarLabel,
    getUserDepartExtend,
    DEFAULT_RULE_VALUE_WIDGET,
    wrapRuleValueWidget,
    normalizeDateRuleValue,
  } from '../auth.data';

  export default defineComponent({
    name: 'AuthDataConfig',
    components: { BasicTable, TableAction, BasicModal, BasicForm },
    props: {
      cgformId: { type: String, required: true },
      authFields: { type: Array, required: true },
      tableType: { type: Number, default: 1 },
    },
    setup(props) {
      const loading = ref(false);
      const mainThemeTemplate = ref('');
      const showAdd = computed(() => props.tableType !== 3 || ['erp', 'innerTable'].includes(mainThemeTemplate.value));
      const [registerTable, { reload, setLoading }] = useTable({
        api: (params) => authDataLoadData(props.cgformId, params),
        rowKey: 'id',
        bordered: true,
        columns: authDataColumns,
        showIndexColumn: false,
        // Cột thao tác
        actionColumn: {
          width: 120,
          title: 'Thao tác',
          fixed: false,
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
      });
      watch(loading, (l) => setLoading(l));
      const [registerModal, { openModal, closeModal }] = useModal();
      const formModalProps = reactive({
        title: '',
        width: 800,
        confirmLoading: false,
        onOk: onSubmit,
        onCancel: closeModal,
        onRegister: registerModal,
      });
      let isUpdate = false;
      let formRecord = {};
      let isManualEnter = false;
      const dictLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const loadingDictCodes = new Set<string>();
      const popupLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const popupConfigIdCache = new Map<string, string>();
      const loadingPopupConfigIds = new Map<string, Promise<string>>();
      const loadingPopupQueries = new Set<string>();
      const userLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const departLabelMaps = reactive<Record<string, Record<string, string>>>({});
      const loadingUserQueries = new Set<string>();
      const loadingDepartQueries = new Set<string>();
      const { formSchemas } = useAuthDataFormSchemas(props, {
        onRuleOperatorChange,
        onRuleColumnChange,
        onRuleNameChange,
      });
      // Cấu hình biểu mẫu
      const [registerForm, { validate, resetFields, setFieldsValue, getFieldsValue, clearValidate, updateSchema }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      watch(
        () => props.cgformId,
        () => {
          reload().catch(() => null);
          if (props.tableType === 3) {
            mainThemeTemplate.value = '';
            authButtonLoadData(props.cgformId, { pageNo: 1, pageSize: 1 })
              .then((result) => (mainThemeTemplate.value = result.mainThemeTemplate || ''))
              .catch(() => (mainThemeTemplate.value = ''));
          }
        },
        { immediate: true }
      );

      /** Tải văn bản hiển thị của từ điển thông thường, khi thất bại thì ghi bộ nhớ đệm rỗng và tiếp tục hiển thị giá trị gốc. */
      async function loadRuleValueDict(field) {
        const dictCode = getRuleValueDictCode(field);
        if (!dictCode || dictLabelMaps[dictCode] || loadingDictCodes.has(dictCode)) return;
        loadingDictCodes.add(dictCode);
        try {
          const cachedOptions = getDictItemsByCode(dictCode);
          const options: any = cachedOptions || (await ajaxGetDictItems(dictCode, undefined, { errorMessageMode: 'none' }));
          dictLabelMaps[dictCode] = (options || []).reduce((result, item) => {
            result[String(item.value)] = item.text || item.label || String(item.value);
            return result;
          }, {});
        } catch (_error) {
          dictLabelMaps[dictCode] = {};
        } finally {
          loadingDictCodes.delete(dictCode);
        }
      }

      /**
       * Lấy Popup Cấu hình nội bộ tương ứng của báo cáo ID。
       * Promise cũng tham gia vào bộ nhớ đệm, đảm bảo cùng một báo cáo khi render đồng thời nhiều quy tắc chỉ yêu cầu cấu hình cột một lần.
       */
      async function getPopupConfigId(code: string) {
        if (popupConfigIdCache.has(code)) return popupConfigIdCache.get(code) || '';
        const loadingRequest = loadingPopupConfigIds.get(code);
        if (loadingRequest) return loadingRequest;
        const request = defHttp
          .get({ url: `/online/cgreport/api/getRpColumns/${code}` }, { isTransformResponse: false, successMessageMode: 'none' })
          .then((res) => {
            const configId = res.success && res.result?.cgRpConfigId ? String(res.result.cgRpConfigId) : '';
            if (configId) popupConfigIdCache.set(code, configId);
            return configId;
          })
          .catch(() => '')
          .finally(() => loadingPopupConfigIds.delete(code));
        loadingPopupConfigIds.set(code, request);
        return request;
      }

      /** Truy vấn theo giá trị quy tắc hiện tại Popup báo cáo, đồng thời ánh xạ trường lưu giá trị thành trường hiển thị rồi hợp nhất vào bộ nhớ đệm phản ứng. */
      async function loadRuleValuePopupDict(field, values: string[]) {
        const config = getPopupDictConfig(field);
        if (!config || !values.length) return;
        const cacheKey = config.dictCode;
        const queryKey = `${cacheKey}:${values.slice().sort().join(',')}`;
        if (loadingPopupQueries.has(queryKey)) return;
        loadingPopupQueries.add(queryKey);
        try {
          const configId = await getPopupConfigId(config.code);
          if (!configId) return;
          const res = await defHttp.get(
            {
              url: `/online/cgreport/api/getData/${configId}`,
              params: { [`force_${config.valueField}`]: values.join(',') },
            },
            { isTransformResponse: false, successMessageMode: 'none' }
          );
          const labelMap = popupLabelMaps[cacheKey] || {};
          (res?.result?.records || []).forEach((item) => {
            labelMap[String(item[config.valueField])] = item[config.labelField] ?? String(item[config.valueField]);
          });
          popupLabelMaps[cacheKey] = { ...labelMap };
        } catch (_error) {
          // Khi truy vấn thất bại, giữ nguyên hiển thị mã gốc, không chặn danh sách quyền dữ liệu
        } finally {
          loadingPopupQueries.delete(queryKey);
        }
      }

      /**
       * Phân tích danh sách giá trị quy tắc (xử lý thống nhất chuỗi/mảng, loại bỏ phần tử trống).
       */
      function normalizeRuleValues(value): string[] {
        const raw = Array.isArray(value) ? value.join(',') : String(value ?? '');
        return raw
          .split(',')
          .map((item) => String(item).trim())
          .filter(Boolean);
      }

      /** Truy vấn hàng loạt tên hiển thị theo trường lưu trữ của control chọn người dùng, khi thất bại giữ nguyên giá trị gốc. */
      async function loadRuleValueUser(field, values: string[]) {
        const extend = getUserDepartExtend(field);
        const storeField = extend.store || 'username';
        const textField = extend.text || 'realname';
        const cacheKey = `${storeField}_${textField}`;
        const labelMap = userLabelMaps[cacheKey] || {};
        const missingValues = values.filter((value) => !labelMap[value]);
        if (!missingValues.length) return;
        const queryKey = `${cacheKey}:${missingValues.slice().sort().join(',')}`;
        if (loadingUserQueries.has(queryKey)) return;
        loadingUserQueries.add(queryKey);
        try {
          await Promise.all(
            missingValues.map(async (value) => {
              const params: any = { pageNo: 1, pageSize: 1 };
              params[storeField] = value;
              try {
                const res = await defHttp.get(
                  { url: '/sys/user/queryUserComponentData', params },
                  { isTransformResponse: false, successMessageMode: 'none' }
                );
                const record = res?.result?.records?.[0];
                labelMap[value] = record ? String(record[textField] ?? value) : value;
              } catch (_error) {
                labelMap[value] = value;
              }
            })
          );
          userLabelMaps[cacheKey] = { ...labelMap };
        } catch (_error) {
          // Khi truy vấn thất bại, giữ nguyên hiển thị mã gốc, không chặn danh sách quyền dữ liệu
        } finally {
          loadingUserQueries.delete(queryKey);
        }
      }

      /** Truy vấn hàng loạt tên hiển thị theo trường lưu trữ của control chọn phòng ban, khi thất bại giữ nguyên giá trị gốc. */
      async function loadRuleValueDepart(field, values: string[]) {
        const extend = getUserDepartExtend(field);
        const storeField = extend.store || 'id';
        const textField = extend.text || 'departName';
        const cacheKey = `${storeField}_${textField}`;
        const labelMap = departLabelMaps[cacheKey] || {};
        const missingValues = values.filter((value) => !labelMap[value]);
        if (!missingValues.length) return;
        const queryKey = `${cacheKey}:${missingValues.slice().sort().join(',')}`;
        if (loadingDepartQueries.has(queryKey)) return;
        loadingDepartQueries.add(queryKey);
        try {
          // Backend chỉ hỗ trợ theo id hoặc orgCode Truy vấn hàng loạt
          const primaryKey = storeField === 'orgCode' ? 'orgCode' : 'id';
          const res = await defHttp.get(
            {
              url: '/sys/sysDepart/queryDepartTreeSync',
              params: { ids: missingValues.join(','), primaryKey },
            },
            { isTransformResponse: false, successMessageMode: 'none' }
          );
          const labelMapCopy = { ...labelMap };
          (res?.result || []).forEach((item) => {
            const value = item[storeField];
            if (value != null) {
              labelMapCopy[String(value)] = String(item[textField] ?? item.title ?? value);
            }
          });
          // Giá trị chưa truy vấn được cũng ghi vào bộ nhớ đệm, tránh yêu cầu lặp lại
          missingValues.forEach((value) => {
            if (labelMapCopy[value] === undefined) {
              labelMapCopy[value] = value;
            }
          });
          departLabelMaps[cacheKey] = labelMapCopy;
        } catch (_error) {
          // Khi truy vấn thất bại, giữ nguyên hiển thị mã gốc, không chặn danh sách quyền dữ liệu
        } finally {
          loadingDepartQueries.delete(queryKey);
        }
      }

      async function openFormModal(data) {
        isUpdate = data.isUpdate ?? false;
        formModalProps.title = data.title;
        openModal();
        await nextTick();
        await resetFields();
        formRecord = Object.assign({}, data.record);
        await setFieldsValue(formRecord);
        // -update-begin--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
        // Mỗi lần mở đều khởi tạo lại theo quy tắc hiện tại, tránh dùng lại component giá trị quy tắc còn sót từ popup lần trước
        await applyRuleValueWidget(formRecord['ruleColumn'], formRecord['ruleOperator']);
        // -update-end--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
      }

      function onAdd() {
        openFormModal({ title: 'Thêm mới' });
      }

      function onEdit(record) {
        openFormModal({ title: 'Chỉnh sửa', record, isUpdate: true });
      }

      function onDelete(id) {
        loading.value = true;
        authDataDelete(id)
          .then(reload)
          .finally(() => (loading.value = false));
      }

      async function onSubmit() {
        try {
          formModalProps.confirmLoading = true;
          let formData = await validate();
          formData = Object.assign({}, formRecord, formData);
          // Giá trị quy tắc của component chọn nhiều là mảng, thống nhất chuyển thành chuỗi phân tách bằng dấu phẩy để lưu trữ (backend sẽ tách theo dấu phẩy rồi ghép in điều kiện)
          if (Array.isArray(formData.ruleValue)) {
            formData.ruleValue = formData.ruleValue.join(',');
          }
          //update-begin--author:wangshuai---date:20260723---for：【LHZP-1267】Năm/Giá trị quy tắc năm-tháng lưu thành năm hiện tại/Ngày đầu tiên của tháng hiện tại
          const ruleField: any = props.authFields.find((item: any) => item.value === formData.ruleColumn);
          formData.ruleValue = normalizeDateRuleValue(ruleField, formData.ruleValue);
          //update-end--author:wangshuai---date:20260723---for：【LHZP-1267】Năm/Giá trị quy tắc năm-tháng lưu thành năm hiện tại/Ngày đầu tiên của tháng hiện tại
          delete formData.createTime;
          delete formData.createBy;
          delete formData.updateTime;
          delete formData.updateBy;
          if (formData.ruleOperator == USE_SQL_RULES) {
            formData.ruleColumn = '';
          }
          formData.cgformId = props.cgformId;
          await authDataSaveOrUpdate(formData, isUpdate);
          reload();
          closeModal();
        } finally {
          formModalProps.confirmLoading = false;
        }
      }

      function onUpdateStatus(record) {
        loading.value = true;
        let status = Math.abs(record.status - 1);
        authDataUpdateStatus({ ...record, status })
          .then(() => {
            record.status = status;
          })
          .finally(() => {
            loading.value = false;
          });
      }

      async function onRuleOperatorChange(val) {
        if (val == USE_SQL_RULES) {
          await setFieldsValue({
            ruleColumn: '',
            ruleValue: '',
          });
          await applyRuleValueWidget('', val);
          await clearValidate(['ruleValue']);
        } else {
          // -update-begin--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
          await applyRuleValueWidget(getFieldsValue().ruleColumn, val);
          // -update-end--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
        }
      }
      // -update-begin--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
      // Căn cứ theo"Trường quy tắc"Loại control tương ứng và"Quy tắc điều kiện", chuyển đổi"Giá trị quy tắc"component nhập liệu của (trường loại từ điển dùng chọn dropdown, các trường khác giữ ô nhập gốc;IN khi có điều kiện thì hỗ trợ chọn nhiều)
      async function applyRuleValueWidget(ruleColumn, ruleOperator?) {
        const operator = ruleOperator ?? getFieldsValue().ruleOperator;
        if (operator === USE_SQL_RULES) {
          await updateSchema({
            field: 'ruleValue',
            component: 'InputTextArea',
            componentProps: { customComponent: null },
          });
          return;
        }
        const findItem: any = props.authFields.find((item: any) => item.value === ruleColumn);
        const innerWidget = getRuleValueWidget(findItem, operator);
        const widget = innerWidget ? wrapRuleValueWidget(innerWidget) : DEFAULT_RULE_VALUE_WIDGET;
        // updateSchema sẽ hợp nhất sâu componentProps, trước tiên xóa trống rõ ràng control nội bộ cũ, tránh còn sót lại sau khi chuyển trường dictCode、picker cấu hình v.v.
        await updateSchema({
          field: 'ruleValue',
          component: 'JAuthRuleValue',
          componentProps: { customComponent: null },
        });
        await updateSchema({
          field: 'ruleValue',
          component: widget.component,
          componentProps: widget.componentProps,
        });
        adaptRuleValueType(widget, operator === 'IN');
      }
      // Sau khi chuyển đổi component giá trị quy tắc, hiệu chỉnh loại giá trị:IN Component từ điển chọn nhiều cần giá trị dạng mảng; người dùng chọn đơn/Component phòng ban chỉ giữ một giá trị đơn lẻ, tránh hiển thị lại bất thường
      function adaptRuleValueType(widget, isIn) {
        const value = getFieldsValue().ruleValue;
        // Lấy ra JAuthRuleValue Component thực tế lồng nhau ở bên trong
        const customComponent = widget.componentProps?.customComponent;
        if (isIn) {
          // IN Chọn nhiều: component từ điển chọn nhiều cần giá trị dạng mảng; người dùng/Phòng ban/Component văn bản vốn tương thích với chuỗi phân tách bằng dấu phẩy, không cần xử lý
          if (customComponent?.component === 'JDictSelectTag' && typeof value === 'string') {
            setFieldsValue({ ruleValue: value ? value.split(',') : [] });
          }
          return;
        }
        if (Array.isArray(value)) {
          setFieldsValue({ ruleValue: value[0] ?? '' });
        } else if (
          typeof value === 'string' &&
          value.indexOf(',') > -1 &&
          (customComponent?.component === 'JSelectUser' || customComponent?.component === 'JSelectDept')
        ) {
          setFieldsValue({ ruleValue: value.split(',')[0] });
        }
      }
      // -update-end--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
      // -update-begin--author:liaozhiyang---date:20240607---for：【TV360X-536】Tối ưu cấu hình quyền dữ liệu và bổ sungJInputSelectComponent
      async function onRuleColumnChange(val) {
        const values = getFieldsValue();
        if (!values.ruleName || (values.ruleName && !isManualEnter)) {
          const findItem: any = props.authFields.find((item: any) => item.value === val);
          const text = findItem ? findItem.text : val;
          await setFieldsValue({
            ruleName: text,
          });
        }
        // -update-begin--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
        // Khi chuyển đổi trường quy tắc, control giá trị quy tắc cũng chuyển theo, loại giá trị cũ có thể không khớp, cần xóa trống
        await setFieldsValue({ ruleValue: '' });
        await applyRuleValueWidget(val);
        // -update-end--author:scott---date:20260722---for: 【Quyền dữ liệu】Giá trị quy tắc tự động chuyển dropdown theo loại control của trường quy tắc/Chọn từ điển
      }
      function onRuleNameChange(e) {
        if (e.target.value.length) {
          isManualEnter = true;
        } else {
          isManualEnter = false;
        }
      }
      function handleOpenChange(visible) {
        if (visible) {
          isManualEnter = false;
        }
      }
      // -update-end--author:liaozhiyang---date:20240607---for：【TV360X-536】Tối ưu cấu hình quyền dữ liệu và bổ sungJInputSelectComponent
      const ruleOperatorLabels = {
        '=': 'Bằng',
        '!=': 'Không bằng',
        '>': 'Lớn hơn',
        '>=': 'Lớn hơn hoặc bằng',
        '<': 'Nhỏ hơn',
        '<=': 'Nhỏ hơn hoặc bằng',
        LIKE: 'Mờ (fuzzy)',
        RIGHT_LIKE: 'Bắt đầu bằng…',
        LEFT_LIKE: 'Kết thúc bằng…',
        IN: 'Trong…',
        EMPTY: 'rỗng',
        NOT_EMPTY: 'không rỗng',
      };
      const noValueOperators = ['EMPTY', 'NOT_EMPTY'];

      function getRuleFieldLabel(ruleColumn) {
        return props.authFields.find((item: any) => item.value === ruleColumn)?.text || ruleColumn || 'Trường chưa chỉ định';
      }

      function getRuleOperatorLabel(ruleOperator) {
        return ruleOperatorLabels[ruleOperator] || ruleOperator;
      }

      function getRuleValueText(record, ellipsisValue = false) {
        const field: any = props.authFields.find((item: any) => item.value === record.ruleColumn);
        const sysVarLabel = getSysVarLabel(record.ruleValue);
        if (sysVarLabel) return sysVarLabel;
        if (isFuzzyRuleOperator(record.ruleOperator)) {
          return Array.isArray(record.ruleValue) ? record.ruleValue.join('、') : String(record.ruleValue ?? '');
        }
        if (field?.view === 'popup_dict') {
          const config = getPopupDictConfig(field);
          const values = Array.isArray(record.ruleValue) ? record.ruleValue : String(record.ruleValue ?? '').split(',');
          const normalizedValues = values.map((value) => String(value).trim()).filter(Boolean);
          if (!config) return normalizedValues.join('、');
          const labelMap = popupLabelMaps[config.dictCode] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValuePopupDict(field, missingValues);
          return normalizedValues.map((value) => labelMap[value] || value).join('、');
        }
        // Lựa chọn của người dùng: truy vấn tên hiển thị theo trường lưu trữ trong cấu hình mở rộng của trường, định dạng là"Tên hiển thị (giá trị thực)", khi thiếu tên thì chỉ hiển thị giá trị thật
        if (field?.view === 'sel_user') {
          const extend = getUserDepartExtend(field);
          const storeField = extend.store || 'username';
          const textField = extend.text || 'realname';
          const cacheKey = `${storeField}_${textField}`;
          const normalizedValues = normalizeRuleValues(record.ruleValue);
          const labelMap = userLabelMaps[cacheKey] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValueUser(field, missingValues);
          return normalizedValues
            .map((value) => {
              const label = labelMap[value];
              const displayValue = ellipsisValue ? ellipsisText(value, 5) : value;
              return label ? `${label}(${displayValue})` : displayValue;
            })
            .join('、');
        }
        // Chọn phòng ban: truy vấn tên hiển thị theo trường lưu trữ trong cấu hình mở rộng của trường, định dạng là"Tên hiển thị (giá trị thực)", khi thiếu tên thì chỉ hiển thị giá trị thật
        if (field?.view === 'sel_depart') {
          const extend = getUserDepartExtend(field);
          const storeField = extend.store || 'id';
          const textField = extend.text || 'departName';
          const cacheKey = `${storeField}_${textField}`;
          const normalizedValues = normalizeRuleValues(record.ruleValue);
          const labelMap = departLabelMaps[cacheKey] || {};
          const missingValues = normalizedValues.filter((value) => !labelMap[value]);
          if (missingValues.length) loadRuleValueDepart(field, missingValues);
          return normalizedValues
            .map((value) => {
              const label = labelMap[value];
              const displayValue = ellipsisValue ? ellipsisText(value, 5) : value;
              return label ? `${label}(${displayValue})` : displayValue;
            })
            .join('、');
        }
        const dictCode = getRuleValueDictCode(field);
        if (!dictCode) {
          return getRuleValueDisplayText(field, record.ruleValue, record.ruleOperator);
        }
        const labelMap = dictLabelMaps[dictCode];
        const values = Array.isArray(record.ruleValue) ? record.ruleValue : String(record.ruleValue ?? '').split(',');
        if (!labelMap) {
          loadRuleValueDict(field);
          return values.join('、');
        }
        return values.map((value) => labelMap[String(value).trim()] || value).join('、');
      }

      /** Cắt ngắn văn bản: vượt quá maxLen ký tự thì hiển thị phần đầu maxLen ký tự + ... */
      function ellipsisText(text: string | number, maxLen = 5) {
        const str = text == null ? '' : String(text);
        if (str.length <= maxLen) return str;
        return str.substring(0, maxLen) + '...';
      }

      function isSysVarValue(record) {
        return !!getSysVarLabel(record.ruleValue);
      }

      function hasRuleValue(record) {
        return (
          !noValueOperators.includes(record.ruleOperator) && record.ruleValue !== null && record.ruleValue !== undefined && record.ruleValue !== ''
        );
      }

      /**
       * Thanh thao tác
       */
      function getTableAction(record) {
        return [
          {
            label: 'Chỉnh sửa',
            onClick: () => onEdit(record),
          },
        ];
      }

      /**
       * Thanh thao tác dropdown
       */
      function getDropDownAction(record): ActionItem[] {
        return [
          {
            label: 'Xóa',
            popConfirm: {
              title: 'Xác nhận xóa chứ?',
              placement: 'left',
              confirm: () => onDelete(record.id),
            },
          },
        ];
      }

      return {
        loading,
        showAdd,
        formModalProps,
        onAdd,
        onUpdateStatus,
        getTableAction,
        getDropDownAction,
        registerTable,
        registerModal,
        registerForm,
        handleOpenChange,
        USE_SQL_RULES,
        getRuleFieldLabel,
        getRuleOperatorLabel,
        getRuleValueText,
        isSysVarValue,
        hasRuleValue,
      };
    },
  });
</script>

<style lang="less" scoped>
  .rule-description {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
    padding: 0 8px;
    white-space: nowrap;
  }

  .rule-field {
    flex: 0 0 auto;
    color: @text-color;
    font-weight: 500;
  }

  .rule-operator {
    flex: 0 0 auto;
    padding: 1px 8px;
    border-radius: 10px;
    background: fade(@primary-color, 10%);
    color: @primary-color;
    font-size: 12px;
    line-height: 20px;
  }

  .rule-operator-sql {
    background: fade(@warning-color, 12%);
    color: @warning-color;
  }

  .rule-value {
    display: block;
    min-width: 0;
    overflow: hidden;
    color: @text-color-secondary;
    text-overflow: ellipsis;
  }

  .rule-value-system {
    flex: 0 1 auto;
    padding: 1px 8px;
    border: 1px solid fade(@primary-color, 18%);
    border-radius: 4px;
    background: fade(@primary-color, 6%);
    color: @primary-color;
    font-size: 13px;
    line-height: 22px;

    &::before {
      margin-right: 6px;
      color: fade(@primary-color, 65%);
      font-family: monospace;
      content: '{ }';
    }
  }
</style>
