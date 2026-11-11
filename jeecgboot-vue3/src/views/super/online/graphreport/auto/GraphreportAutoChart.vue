<template>
  <div class="p-2">
    <a-spin :spinning="loading">
      <ErrorTip v-if="errorTip" :tip="errorTip" />
      <template v-else>
        <!--update-begin--author:liaozhiyang---date:20240316---for：【QQYUN-8426】Vùng truy vấn biểu đồ thích ứng với màu đen tối-->
        <Card v-if="showSearchField">
          <BasicForm @register="registerSearchForm" :model="searchFormModal" />
        </Card>
        <!--update-end--author:liaozhiyang---date:20240316---for：【QQYUN-8426】Vùng truy vấn biểu đồ thích ứng với màu đen tối-->
        <ErrorTip v-if="errors.resultIsEmpty" tip="Không truy vấn được bất kỳ dữ liệu nào" />
        <ChartAutoRender v-else :chartsData="chartsData" />
      </template>
    </a-spin>
  </div>
</template>

<script lang="ts">
  import {computed, defineComponent, nextTick, provide, reactive, ref, watch} from 'vue';
  import { router } from '/@/router';
  import ErrorTip from './components/ErrorTip.vue';
  import ChartAutoRender from './components/render/ChartAutoRender.vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { onMountedOrActivated } from '/@/hooks/core/onMountedOrActivated';
  import { getChartsData, getParamsInfo } from '../graphreport.api';
  import { useParseFormSchemas } from './hooks/useParseFormSchemas';
  import { isArray } from '/@/utils/is';
  import { formatToDate } from '/@/utils/dateUtil';
  import { Card } from "ant-design-vue"

  // noinspection JSUnusedGlobalSymbols
  export default defineComponent({
    name: 'GraphreportAutoChart',
    components: { BasicForm, ErrorTip, ChartAutoRender, Card },
    props: {},
    setup() {
      provide('setErrorTip', setErrorTip);
      const $route = router.currentRoute;
      const loading = ref(false);
      const code = ref<string>();
      const errorTip = ref<Nullable<string>>(null);
      // Căn cứ vào các lỗi runtime khác nhau, hiển thị thông tin nhắc nhở khác nhau
      const errors = reactive({ resultIsEmpty: false });
      const chartsData = ref();
      // Điều kiện truy vấn (mapCăn cứ theocodecách ly)
      const paramsMap = reactive({});
      // Điều kiện truy vấn tùy chỉnh của người dùng (mapCăn cứ theocodecách ly)
      const selfParamsMap = reactive({});
      // Điều kiện truy vấnform（mapCăn cứ theocodecách ly)
      const searchFormMap = reactive({});
      // Nhiều trangparamsBộ nhớ đệm
      const paramsRef = getPageComputed<string>(paramsMap, '');
      // Điều kiện truy vấn tùy chỉnh của người dùng
      const selfParamsRef = getPageComputed<Nullable<Recordable>>(selfParamsMap);
      // Điều kiện truy vấnform
      const searchFormModal = getPageComputed(searchFormMap, {});
      // Có hiển thị điều kiện truy vấn hay không
      const showSearchField = ref(false);
      // Cấu hình cột thích ứng
      const adaptiveColProps = { xs: 24, sm: 12, md: 12, lg: 8, xl: 8, xxl: 6 };
      const [registerSearchForm, searchForm] = useForm({
        schemas: [],
        // Chế độ gọn (compact)
        compact: true,
        // labelĐộ rộng mặc định
        labelWidth: 120,
        // Sau khi nhấn Enter, tự động gửi
        autoSubmitOnEnter: true,
        // Mặc định row Cấu hình
        rowProps: { gutter: 8 },
        // Mặc định col Cấu hình
        baseColProps: {
          ...adaptiveColProps,
        },
        labelCol: { xs: 24, sm: 8, md: 6, lg: 8, xl: 6, xxl: 6 },
        wrapperCol: {},
        // Có hiển thị hay không Mở rộng/Thu gọn Nút
        showAdvancedButton: true,
        // Vượt quá số cột chỉ định thì mặc định thu gọn
        autoAdvancedCol: 3,
        // Cấu hình nút thao tác
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
        resetFunc: onReset,
        submitFunc: onSubmit,
      });

      // 【VUEN-1554】Route bộ nhớ đệm đi quaActivated, cái chưa lưu bộ nhớ đệm thì đi quaMounted, đều chỉ đi qua một lần
      onMountedOrActivated(() => {
        code.value = $route.value.params.code as string;
      });

      // Đặt tham số làm mới vào đây để kích hoạt, là có thể làm mới cùng một giao diện
      watch(
        code,
        () => {
          selfParamsRef.value = null;
          loadChartsData();
        },
        { immediate: true }
      );

      function setErrorTip(tip: string | null) {
        errorTip.value = tip;
      }

      // Tải dữ liệu biểu đồ
      async function loadChartsData() {
        loading.value = true;
        if (!code.value) {
          loading.value = false;
          return false;
        }
        let params = { id: code.value, params: paramsRef.value };
        // Truy vấn tham số biểu đồ
        if (selfParamsRef.value == null) {
          selfParamsRef.value = {};
          try {
            let result = await getParamsInfo({ headId: params.id });
            if (result && result.length > 0) {
              for (let i of result) {
                selfParamsRef.value['self_' + i.paramName] = !$route.value.query[i.paramName] ? '' : $route.value.query[i.paramName];
              }
            }
          } catch {
            return;
          }
        }
        try {
          let result = await getChartsData({ ...params, ...selfParamsRef.value });
          let { head, data, items, dictOptions } = result;
          if (data && data.length === 0) {
            errors.resultIsEmpty = true;
          } else {
            errors.resultIsEmpty = false;
            chartsData.value = { head, data, items, dictOptions };
            parseChartsData();
          }
        } finally {
          loading.value = false;
        }
      }

      function getPageComputed<T = Recordable>(map, defVal?: T) {
        return computed<T>({
          get(): T {
            if (map[code.value!] == null) {
              map[code.value!] = defVal;
            }
            return map[code.value!];
          },
          set: (val: T) => (map[code.value!] = val),
        });
      }

      const { parseFormSchemas } = useParseFormSchemas(chartsData, showSearchField);

      // Phân tích dữ liệu biểu đồ
      async function parseChartsData() {
        let schemas = await parseFormSchemas();
        if (schemas && schemas.length>0) {
          nextTick(() => {
            searchForm.resetSchema(schemas);
          });
        }
      }

      async function onReset() {
        paramsRef.value = '';
        loadChartsData();
      }

      //Gửi biểu mẫu
      async function onSubmit() {
        const data = await searchForm.validate();
        let { items } = chartsData.value;
        let params: Recordable[] = [];
        for (let field of items) {
          if (field.searchFlag !== 'Y') continue;
          let values: any = null;
          if (!field.searchMode) field.searchMode = 'single';
          if (field.searchMode === 'single') {
            // Truy vấn điều kiện đơn
            values = data[field.fieldName];
          } else {
            // Truy vấn theo phạm vi
            let _begin = data[field.fieldName];
            let _end = data[`${field.fieldName}_end`];
            values = [_begin, _end];
          }
          // Định dạng hóa ngày
          if (field.fieldType === 'Date') {
            values = formatDate(values);
          }
          // Đóng gói thành params
          if (values != null && (values[0] != null || values[1] != null)) {
            params.push({
              value: values,
              fieldTxt: field.fieldTxt,
              fieldName: field.fieldName,
              fieldType: field.fieldType,
              searchMode: field.searchMode,
            });
          }
        }
        paramsRef.value = encodeURIComponent(JSON.stringify(params));
        loadChartsData();
      }

      // Định dạng hóa ngày
      function formatDate(values) {
        if (values) {
          if (isArray(values)) {
            if (values[0]) {
              values[0] = formatToDate(values[0]);
            }
            if (values[1]) {
              values[1] = formatToDate(values[1]);
            }
          } else {
            return formatToDate(values);
          }
        }
        return values;
      }

      return {
        loading,
        errors,
        errorTip,
        chartsData,
        showSearchField,
        searchFormModal,
        registerSearchForm,
      };
    },
  });
</script>

<style lang="less" scoped>
  .ant-card {
    margin-bottom: 20px;
    :deep(.ant-card-body) {
      padding: 10px 20px;
    }

    // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
    :deep(.ant-picker),
    :deep(.ant-calendar-picker),
    :deep(.ant-input-number) {
      width: 100%;
    }

    // Truy vấn theo phạm vi: sắp xếp ngang, trái phải mỗi bên chiếm một nửa
    :deep(.range-query) {
      .range-query-inner {
        display: flex;
        align-items: center;
        width: 100%;
        gap: 4px;

        .range-span {
          flex: none;
          width: 12px;
          text-align: center;
          line-height: 32px;
        }

        .ant-picker,
        .ant-calendar-picker,
        .ant-input,
        .ant-input-number {
          flex: 1;
          width: auto !important;
          min-width: 0;
        }
      }
    }
    // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1033】Ngày truy vấn biểu đồ/Thích ứng độ rộng chữ số
  }
</style>
