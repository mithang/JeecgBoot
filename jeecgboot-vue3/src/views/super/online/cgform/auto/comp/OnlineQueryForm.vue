<template>
  <div class="jeecg-basic-table-form-container online-query-form p-0" v-if="formSchemas && formSchemas.length > 0">
    <BasicForm ref="onlineQueryFormRef" @register="registerForm">
      <!-- Truy vấn theo phạm vi: ngày -->
      <template #groupDate="{ model, field, schema }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
        <!-- <a-date-picker
          :showTime="false"
          valueFormat="YYYY-MM-DD"
          placeholder="Ngày bắt đầu"
          v-model:value="model[field + '_begin']"
          style="width: calc(50% - 15px);min-width: 100px;"
          v-bind="schema.componentProps"
        ></a-date-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-date-picker
            :showTime="false"
            valueFormat="YYYY-MM-DD"
            placeholder="Ngày kết thúc"
            v-model:value="model[field + '_end']"
            style="width: calc(50% - 15px);min-width: 100px;"
            v-bind="schema.componentProps"
          ></a-date-picker>
        </a-form-item-rest> -->
        <a-range-picker :style="{ width: '100%' }" v-model:value="model[field]" v-bind="schema.componentProps" :placeholder="getGroupDatePlaceholder(schema.componentProps)" valueFormat="YYYY-MM-DD"/>
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
      </template>

      <!-- Truy vấn theo phạm vi: thời gian -->
      <template #groupDatetime="{ model, field }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
        <!-- <a-date-picker
          :showTime="true" 
          valueFormat="YYYY-MM-DD HH:mm:ss"
          placeholder="Thời gian bắt đầu"
          v-model:value="model[field + '_begin']"
          style="min-width: 100px;width: calc(50% - 15px);"
        ></a-date-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-date-picker
            :showTime="true"
            valueFormat="YYYY-MM-DD HH:mm:ss"
            placeholder="Thời gian kết thúc"
            v-model:value="model[field + '_end']"
            style="min-width: 100px;width: calc(50% - 15px);"
          ></a-date-picker>
        </a-form-item-rest> -->
        <a-range-picker :style="{ width: '100%' }" v-model:value="model[field]" :show-time="true" valueFormat="YYYY-MM-DD HH:mm:ss"/>
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
      </template>

      <!-- update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn -->
      <!-- Truy vấn theo phạm vi: thời gian -->
      <template #groupTime="{ model, field }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
        <!-- <a-time-picker
          placeholder="Thời gian bắt đầu"
          value-format="HH:mm:ss"
          v-model:value="model[field + '_begin']"
          style="min-width: 100px;width: calc(50% - 15px);"
        ></a-time-picker>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-time-picker
            placeholder="Thời gian kết thúc"
            value-format="HH:mm:ss"
            v-model:value="model[field + '_end']"
            style="min-width: 100px;width: calc(50% - 15px);"
          ></a-time-picker>
        </a-form-item-rest> -->
        <a-time-range-picker :style="{ width: '100%' }" v-model:value="model[field]" value-format="HH:mm:ss" />
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
      </template>
      <!-- update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9348】Bổ sungonlineChức năng truy vấn theo phạm vi thời gian của vùng truy vấn -->

      <!-- Truy vấn theo phạm vi: số -->
      <template #groupNumber="{ model, field, schema }">
        <!-- update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
        <!-- <a-input-number placeholder="Giá trị bắt đầu" v-model:value="model[field + '_begin']" style="width: calc(50% - 15px)"></a-input-number>
        <span class="group-query-string">~</span>
        <a-form-item-rest>
          <a-input-number placeholder="Giá trị kết thúc" v-model:value="model[field + '_end']" style="width: calc(50% - 15px)"></a-input-number>
        </a-form-item-rest> -->
        <JRangeNumber v-model:value="model[field]" v-bind="schema.componentProps" />
        <!-- update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường -->
      </template>

      <!-- Truy vấn/Nút đặt lại-->
      <template #formFooter>
        <a-col :md="6" :sm="8">
          <span
            class="table-page-search-submitButtons"
            :class="{ 'only-toggle': !queryBtnCfg.enabled && !resetBtnCfg.enabled }"
          >
            <a-button
                v-if="queryBtnCfg.enabled"
                type="primary"
                :preIcon="queryBtnCfg.buttonIcon"
                @click="doSearch"
            >
              <span>{{ queryBtnCfg.buttonName }}</span>
            </a-button>
            <a-button
                v-if="resetBtnCfg.enabled"
                type="primary"
                :preIcon="resetBtnCfg.buttonIcon"
                style="margin-left: 8px"
                @click="resetSearch"
            >
              <span>{{ resetBtnCfg.buttonName }}</span>
            </a-button>
            <a v-if="toggleButtonShow" @click="toggleSearchStatus = !toggleSearchStatus" style="margin-left: 8px">
              {{ toggleSearchStatus ? 'Thu gọn' : 'Mở rộng' }}
              <a-icon :type="toggleSearchStatus ? 'up' : 'down'" />
            </a>
          </span>
        </a-col>
      </template>
    </BasicForm>
  </div>
</template>

<script>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { watch, ref, reactive, toRaw, isProxy } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import FormSchemaFactory from './factory/FormSchemaFactory';
  import IFormSchema from './factory/IFormSchema';
  import { handleLinkDown, getFieldIndex, getRefPromise, LINK_DOWN } from '../../hooks/auto/useAutoForm';
  import { ONL_QUERY_LABEL_COL, ONL_QUERY_WRAPPER_COL, FORM_VIEW_TO_QUERY_VIEW } from '../../types/onlineRender';
  import { loadOneFieldDefVal } from '../../util/FieldDefVal';
  import { useExtendComponent } from '../../hooks/auto/useExtendComponent';
  import { LABELLENGTH } from '../../util/constant';
  import dayjs from 'dayjs';
  import JRangeNumber from '/@/components/Form/src/jeecg/components/JRangeNumber.vue'
  import { useDebounceFn } from "@vueuse/core";
  
  export default {
    name: 'OnlineQueryForm',
    components: {
      BasicForm,
      JRangeNumber,
    },
    props: {
      id: {
        type: String,
        default: '',
      },
      queryBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: 'Truy vấn',
            buttonIcon: 'ant-design:search',
          }
        }
      },
      resetBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: 'Đặt lại',
            buttonIcon: 'ant-design:reload',
          }
        }
      },
    },
    emits: ['search', 'loaded'],
    setup(props, { emit }) {
      // Lấy địa chỉ yêu cầu của điều kiện truy vấn
      const LOAD_URL = '/online/cgform/api/getQueryInfoVue3/';
      // Biểu mẫuref
      const onlineQueryFormRef = ref(null);
      // Cấu hình dùng khi render biểu mẫu
      const formSchemas = ref([]);
      // Lưới biểu mẫu VUEN-2493【Tối ưu】onlineĐiều kiện truy vấn mặc định quá rộng, tham khảoonlineBáo cáo
      const baseColProps = ref({ xs:24, sm: 24, md: 12, lg:6, xl:6 });
      // Chuyển đổi việc nút hiển thị/ẩn trường có hiển thị hay không
      const toggleButtonShow = ref(false);
      // Có hiển thị tất cả trường truy vấn hay không
      const toggleSearchStatus = ref(false);
      // Điều kiện truy vấn
      const queryParams = ref({});
      // Trường cần ẩn
      const hideList = ref([]);
      const { createMessage: $message } = useMessage();
      const { linkTableCard2Select } = useExtendComponent();
      const formLabelWidth = ref(80);
      /**
       *  Giá trị mặc định chia làm ba loại,param > cache > config
       *  1.Cấu hình biểu mẫu-config
       *  2.Bộ nhớ đệm route-cache
       *  3.Tham số thanh địa chỉ-param
       *  Khi biểu mẫuidxảy ra thay đổi,configThay đổi, trang danh sách truyền vàocache，param;Lắng nghe (watch)status changeSau đó đặt lại giá trị của biểu mẫu
       */
      const defaultValues = reactive({
        config: {},
        cache: {},
        param: {},
        status: false,
      });

      const debouncedCustomSetFieldsValue = useDebounceFn(customSetFieldsValue, 500);

      // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
      let resolveFirstQueryDecision = () => {};
      let firstQueryDecision = new Promise((r) => (resolveFirstQueryDecision = r));
      function resetFirstQueryDecision() {
        firstQueryDecision = new Promise((r) => (resolveFirstQueryDecision = r));
      }
      // Để component cha gọi: chờ đợi"Có giá trị truy vấn mặc định hay không"quyết định của
      function whenFirstQueryDecided() {
        return firstQueryDecision;
      }
      // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
      
      /**
       * Lắng nghe (watch)cacheFormValues
       */
      watch(
        () => defaultValues.status,
        async (val) => {
          console.log('-------------defaultValuesxảy ra thay đổi,Cần đặt lại biểu mẫu---------------');
          const { config, cache, param } = toRaw(defaultValues);
          let rawValues = Object.assign({}, config, cache, param);
          //update-begin---author:wangshuai---date:2025-10-11---for:【issues/8790】online Biểu mẫu trọng yếu bug, ảnh hưởng đã cấu hình truy vấn mọi biểu mẫu của---
          await debouncedCustomSetFieldsValue(rawValues);
          //update-end---author:wangshuai---date:2025-10-11---for:【issues/8790】online Biểu mẫu trọng yếu bug, ảnh hưởng đã cấu hình truy vấn mọi biểu mẫu của---
        },
        { immediate: true, deep: true }
      );

      /**
       * Thiết lập giá trị mặc định
       * @param values
       */
      async function initDefaultValues(cache, param) {
        defaultValues.cache = { ...cache };
        defaultValues.param = { ...param };
        defaultValues.status = !defaultValues.status;
      }

      const clearObj = (obj) => {
        Object.keys(obj).map((key) => {
          delete obj[key];
        });
      };

      // Lắng nghe (watch)
      watch(
        () => props.id,
        (val) => {
          if (val) {
            resetForm();
          } else {
            formSchemas.value = [];
            // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
            resolveFirstQueryDecision(false);
            // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
          }
        },
        { immediate: true }
      );

      /**
       * Lấy cấu hình biểu mẫu
       */
      async function initSchemas(formProperties, searchFieldOrderMap) {
        let arr = [];
        let configValue = {};
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        let keys = Object.keys(formProperties).sort(
          (a, b) => (searchFieldOrderMap.get(a) ?? 9999) - (searchFieldOrderMap.get(b) ?? 9999)
        );
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        let setLabelLength = -1;
        for (let key of keys) {
          const item = formProperties[key];
          
          //update-begin-author:taoyan date:2023-7-19 for:QQYUN-5783 Tham số từ điển dữ liệu của cấu hình, vấn đề nằm ở component chọn phòng ban của truy vấn kia, định nghĩa biểu mẫu không thể thay đổi cái truyền cho backendorg_code。Xem mã backend, cung cấp cho componentvaluecái được truyền vào cũng làid, chỗ này cần điều chỉnh.
          if(key === 'sys_org_code'){
            if(!item.fieldExtendJson){
              item.fieldExtendJson = '{"store":"orgCode"}'
            }
          }
          //update-end-author:taoyan date:2023-7-19 for:QQYUN-5783 Tham số từ điển dữ liệu của cấu hình, vấn đề nằm ở component chọn phòng ban của truy vấn kia, định nghĩa biểu mẫu không thể thay đổi cái truyền cho backendorg_code。Xem mã backend, cung cấp cho componentvaluecái được truyền vào cũng làid, chỗ này cần điều chỉnh.
          
          let view = item.view;
          // update-begin--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
          item.originView = item.view;
          // update-end--author:liaozhiyang---date:20240611---for：【TV360X-461】Loại trường làstring, control làtext, thì mặc định truy vấn mờ
          if (FORM_VIEW_TO_QUERY_VIEW[view]) {
            item.view = FORM_VIEW_TO_QUERY_VIEW[view];
          }
          await loadOneFieldDefVal(key, item, configValue);
          if (item.mode == 'group' && ('date' == view || 'datetime' == view || 'number' == view || 'time' == view )) {
            // Truy vấn theo phạm vi-Ngày, thời gian, giá trị số
            let temp = FormSchemaFactory.createSlotFormSchema(key, item);
            arr.push(temp);
          } else {
            if (item.view === LINK_DOWN) {
              let array = handleLinkDown(item, key);
              for (let linkDownItem of array) {
                let temp = FormSchemaFactory.createFormSchema(linkDownItem.key, linkDownItem);
                let tempIndex = getFieldIndex(arr, linkDownItem.key);
                if (tempIndex == -1) {
                  arr.push(temp);
                } else {
                  arr[tempIndex] = temp;
                }
              }
            } else {
              let tempIndex = getFieldIndex(arr, key);
              if (tempIndex == -1) {
                let temp = FormSchemaFactory.createFormSchema(key, item);
                arr.push(temp);
              }
            }
          }
          // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online labelHiển thị mặc định6cái
          let fieldExtendJson = item.fieldExtendJson;
          if (fieldExtendJson) {
            fieldExtendJson = JSON.parse(fieldExtendJson);
            if (fieldExtendJson.labelLength) {
              console.log(key, fieldExtendJson.labelLength);
              if (setLabelLength > -1) {
                // Lấy giá trị lớn nhất được thiết lập trong cấu hình, tất cảlabelĐộ dài của nên nhất quán, nếu không nhiều hàng sẽ không thẳng hàng
                setLabelLength = fieldExtendJson.labelLength > setLabelLength ? fieldExtendJson.labelLength : setLabelLength;
              } else {
                setLabelLength = fieldExtendJson.labelLength;
              }
            }
          }
          // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online labelHiển thị mặc định6cái
        }
        // update-begin--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online labelHiển thị mặc định6cái
        // Trong cấu hình chưa thiết lập,Đọc độ dài mặc định
        if (setLabelLength == -1) {
          setLabelLength = LABELLENGTH;
        } else {
          // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-98】labelChữ hiển thị phải giống vớilabelLengthCấu hình nhất quán
          arr.forEach(item=>{
            item.labelLength = setLabelLength;
          })
          // update-end--author:liaozhiyang---date:20240517---for：【TV360X-98】labelChữ hiển thị phải giống vớilabelLengthCấu hình nhất quán
        }
        // update-end--author:liaozhiyang---date:20231205---for：【QQYUN-7140】online labelHiển thị mặc định6cái
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        arr.sort(
          (a, b) => (searchFieldOrderMap.get(a.field) ?? 9999) - (searchFieldOrderMap.get(b.field) ?? 9999)
        );
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        let schemaArray = [];
        if (arr.length > 2) {
          toggleButtonShow.value = true;
        }
        let hideFieldName = [];
        for (let i = 0; i < arr.length; i++) {
          let item = arr[i];
          item.setFormRef(onlineQueryFormRef);
          item.noChange();
          item.asSearchForm();
          if (i > 1) {
            hideFieldName.push(item.field);
            item.isHidden();
          }
          //update-begin-author:taoyan date:2022-10-24 for: VUEN-2493【Tối ưu】onlineĐiều kiện truy vấn mặc định quá rộng, tham khảoonlineBáo cáo
          let tempSchema = item.getFormItemSchema();
          if(item.slot == 'groupDatetime'){
            // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
            //Nếu là kiểu thời gian Thiết lập lạicolprops (Nhỏ hơn3thì mới đặt lại, nếu không nhiều hàng sẽ khiến không căn đều nhau)
            arr.length <= 3 && (tempSchema['colProps'] = { xs:24, sm: 24, md: 12, lg:8, xl:8 })
            // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
          }
          // update-begin--author:liaozhiyang---date:20240522---for：【TV360X-250】Vùng truy vấn đưaSwitchComponent công tắc đổi thànhselectComponent
          if (tempSchema.component === 'JSwitch') {
            const componentProps = tempSchema.componentProps ?? {};
            tempSchema.componentProps = { ...componentProps, query: true };
          }
          // update-end--author:liaozhiyang---date:20240522---for：【TV360X-250】Vùng truy vấn đưaSwitchComponent công tắc đổi thànhselectComponent
          linkTableCard2Select(tempSchema);
          // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
          if (tempSchema.component === 'LinkTableSelect') {
            let componentProps = tempSchema.componentProps ?? {};
            tempSchema.componentProps = { ...componentProps, editBtnShow: false };
          }
          // update-end--author:liaozhiyang---date:20240530---for：【TV360X-389】Truy vấn thông thường của bản ghi liên kết bỏ nút chỉnh sửa
          // update-begin--author:liaozhiyang---date:20240614---for：【TV360X-1231】Một số component dropdown ở vùng truy vấn hiển thị không đầy đủ
          const compProps = tempSchema.componentProps ?? {};
          if (!compProps.getPopupContainer) {
            tempSchema.componentProps = { ...compProps, getPopupContainer: () => document.body };
          }
          // update-end--author:liaozhiyang---date:20240614---for：【TV360X-1231】Một số component dropdown ở vùng truy vấn hiển thị không đầy đủ
          // update-begin--author:liaozhiyang---date:20240725---for：【TV360X-1857】onlineTruy vấn thêm truy vấn mờ
           const fieldData = formProperties[tempSchema.field] ?? {};
          // 【TV360X-1966】Control trong thuộc tính trang đã cấu hình là ô văn bản và loại trường làstring, truy vấn cá nhân hóa đã cấu hình component người dùng, component người dùng không có hiệu lực
          if (fieldData.mode == 'like' && fieldData.view === 'text' && fieldData.originView === 'text') {
            tempSchema.component = 'JInput';
          }
          // update-end--author:liaozhiyang---date:20240725---for：【TV360X-1857】onlineTruy vấn thêm truy vấn mờ
          schemaArray.push(tempSchema);
          //update-end-author:taoyan date:2022-10-24 for: VUEN-2493【Tối ưu】onlineĐiều kiện truy vấn mặc định quá rộng, tham khảoonlineBáo cáo
        }
        hideList.value = hideFieldName;
        formSchemas.value = schemaArray;
        //Thiết lập giá trị mặc định của biểu mẫu
        defaultValues.config = { ...configValue };
        defaultValues.status = !defaultValues.status;
        // update-begin--author:liaozhiyang---date:20231204---for：【QQYUN-7140】online labelHiển thị mặc định6cái
        setTimeout(() => {
          // 14là văn tựsize，24là khoảng hở (gap)
          const w = setLabelLength * 14 + setLabelLength + 24;
          formLabelWidth.value = w;
        }, 0);
        // update-end--author:liaozhiyang---date:20231204---for：【QQYUN-7140】online labelHiển thị mặc định6cái
      }
      /**
       * 2024-05-31
       * liaozhiyang
       * 【TV360X-415】Truy vấn cá nhân hóa hỗ trợ năm, tháng, tuần, quý.
       * Phân tích đặc địnhview(Component)Giá trị của trường, đưaviewGiá trị trường làdate_year、date_month、date_week、date_quarter
       * đổi thànhdate, đồng thời lắp rápfieldExtendJson
       */
      const analysisComponent = (res) => {
        const properties = res.properties;
        if (properties) {
          Object.entries(properties).forEach(([key, value]) => {
            const data = value;
            if (['date_year', 'date_month', 'date_week', 'date_quarter'].includes(data.view)) {
              const fieldExtendJson = data.fieldExtendJson ? JSON.parse(data.fieldExtendJson) : {};
              fieldExtendJson.picker = data.view.split('_')[1];
              data.fieldExtendJson = JSON.stringify(fieldExtendJson);
              data.view = 'date';
            }
          });
        }
      };
      async function resetForm() {
        // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
        resetFirstQueryDecision();
        // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
        try {
          let json = await loadQueryInfo();
          // update-begin--author:liaozhiyang---date:20240531---for：【TV360X-415】Truy vấn cá nhân hóa hỗ trợ năm, tháng, tuần, quý
          analysisComponent(json);
          // update-end--author:liaozhiyang---date:20240318---for：【TV360X-415】Truy vấn cá nhân hóa hỗ trợ năm, tháng, tuần, quý
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-516】Truy vấn nâng cao lọc bỏ các component không hỗ trợ truy vấn
          // filterComponent(json);
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-516】Truy vấn nâng cao lọc bỏ các component không hỗ trợ truy vấn
          // Lấy mọi cấu hình trường Thông qua sự kiện, trả lại cho component truy vấn nâng cao
          let allFields = getAllFields(json);
          emit('loaded', json);
          // Lấy cấu hình trang biểu mẫu của điều kiện truy vấn
          let { formProperties, hasField, searchFieldOrderMap } = getQueryFormProperties(allFields, json);
          if (hasField == false) {
            formSchemas.value = [];
            // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
            resolveFirstQueryDecision(false);
            // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
            return;
          }
          // Lấy cấu hình biểu mẫuformSchemas
          await initSchemas(formProperties, searchFieldOrderMap);
          // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
          const { config, cache, param } = toRaw(defaultValues);
          const rawValues = Object.assign({}, config, cache, param);
          const values = transformGroupDefValus(rawValues);
          resolveFirstQueryDecision(Object.keys(values).length > 0);
          // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
        } catch (e) {
          console.error(e);
          // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
          resolveFirstQueryDecision(false);
          // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
        }
      }
      /**
       * 2024-05-24
       * liaozhiyang
       * Lọc bỏ các component không hỗ trợ truy vấn (hình ảnh, tệp, mật khẩu, bản ghi liên kết, liên động)
       */
      const filterComponent = (data) => {
        const { properties = {} } = data;
        Object.entries(properties).forEach(([field, value]) => {
          if (value.view === 'table') {
            filterComponent(value);
          }
          if (['image', 'password', 'file', 'link_table', 'link_down'].includes(value.view)) {
            delete properties[field];
          }
        });
      };
      /**
       * Thiết lập giá trị của biểu mẫu
       */
      async function customSetFieldsValue(rawValues) {
        await getRefPromise(onlineQueryFormRef);
        console.log('rawValues', rawValues);
        // update-begin--author:liaozhiyang---date:20240618---for：onlineTruy vấn theo phạm vi với giá trị mặc định của truy vấn thông thường không hoạt động
        const values = transformGroupDefValus(rawValues);
        await setFieldsValue(values);
        // update-end--author:liaozhiyang---date:20240618---for：onlineTruy vấn theo phạm vi với giá trị mặc định của truy vấn thông thường không hoạt động
        if (Object.keys(values).length > 0) {
          doSearch();
        }
      }

      /**
       * liaozhiyang
       * 20260807
       * 【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
       * Căn cứ theo searchFieldList Xây dựng ánh xạ sắp xếp trường (chỉ dùng cho biểu mẫu truy vấn, với cấu hình trường order không liên quan)
       */
      function buildSearchFieldOrderMap(searchFieldList, joinQuery, table) {
        const orderMap = new Map();
        searchFieldList.forEach((field, index) => {
          orderMap.set(field, index);
          if (joinQuery && field.indexOf('@') < 0) {
            orderMap.set(table + '@' + field, index);
          }
        });
        return orderMap;
      }
      /**
       * Chuyển hóa
       */
      function getQueryFormProperties(allFields, json) {
        const { searchFieldList, joinQuery, table } = json;
        let hasField = false;
        let formProperties = {};
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        const searchFieldOrderMap = buildSearchFieldOrderMap(searchFieldList, joinQuery, table);
        if (allFields && searchFieldList.length) {
          // Theo searchFieldList Xây dựng trường truy vấn theo thứ tự
          searchFieldList.forEach((field) => {
            const fieldConfig = allFields[field];
            if (!fieldConfig) {
              return;
            }
            const item = { ...fieldConfig };
            if (joinQuery == true) {
              //Phán đoán có phải truy vấn liên hợp hay không
              if (field.indexOf('@') < 0) {
                //Không có@Cho thấy là trường bảng chính, Nối ghép thủ công lên@
                formProperties[table + '@' + field] = item;
                hasField = true;
              } else {
                //Có@Cho thấy là trường bảng con, lấy trực tiếp
                formProperties[field] = item;
                hasField = true;
              }
            } else {
              // không phải truy vấn liên hợp Chỉ tra trường của bảng chính
              if (field.indexOf('@') < 0) {
                formProperties[field] = item;
                hasField = true;
              }
            }
          });
        }
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-487】Thứ tự của trường truy vấn theo thứ tự trong APIsearchFieldListThứ tự trong mảng chính là thứ tự sắp xếp của trường khi thiết kế
        return {
          formProperties,
          hasField,
          searchFieldOrderMap,
        };
      }

      /**
       * Lấy cấu hình biểu mẫu của điều kiện truy vấn
       * jsonCấu trúc:
       * trường bảng chính1：{Cấu hình1}
       * trường bảng chính2：{Cấu hình2}
       * tên bảng con@trường của bảng con1：{Cấu hình trường của bảng con1}
       * tên bảng con@trường của bảng con2：{Cấu hình trường của bảng con2}
       */
      function getAllFields(json) {
        // Lấy mọi cấu hình Trường truy vấn Có truy vấn liên hợp hay không
        const { properties, searchFieldList, joinQuery, table } = json;
        let allFields = {};
        let order = 1;
        let hasField = false;
        Object.keys(properties).map((field) => {
          let item = properties[field];
          if (item.view == 'table') {
            // trường của bảng con
            // Chỉ khi bật truy vấn liên hợp mới cần trường của bảng con làm điều kiện truy vấn
            let subProps = item['properties'];
            let subTableOrder = order * 100;
            Object.keys(subProps).map((subField) => {
              let subItem = subProps[subField];
              // Đảm bảo sắp xếp thống nhất
              subItem['order'] = subTableOrder + Number(subItem['order']);
              let subFieldKey = field + '@' + subField;
              allFields[subFieldKey] = subItem;
            });
            order++;
          } else {
            // trường bảng chính
            item['order'] = Number(item['order']);
            allFields[field] = item;
          }
        });
        return allFields;
      }

      /**
       * Định dạng dữ liệu truy vấn trả về, Cần đi quagetQueryFormPropertiesChuyển thành cấu hình biểu mẫu
       * jsonCấu trúc:
       *    table(Tên bảng)
       *    title(Mô tả bảng)
       *    properties(Trường bảng)
       *        field1
       *        field2
       *        fieldxxx
       *        sub-table1(tên bảng con1)
       *            title(Mô tả bảng con1)
       *            properties(trường của bảng con)
       *        sub-table2(tên bảng con2)
       *            title(Mô tả bảng con2)
       *            properties(trường của bảng con)
       */
      function loadQueryInfo() {
        let url = `${LOAD_URL}${props.id}`;
        return new Promise((resolve) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              // console.log("-onlineCấu hình lấy điều kiện truy vấn danh sách", res);
              if (res.success) {
                resolve(res.result);
              } else {
                resolve(false);
                $message.warning(res.message);
              }
            })
            .catch(() => {
              $message.warning('Lấy điều kiện truy vấn thất bại!');
              resolve(false);
            });
        });
      }
      
      //Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, updateSchema, getFieldsValue }] = useForm({
        name: 'online-query-form',
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        autoSubmitOnEnter: true,
        labelWidth: formLabelWidth,
        wrapperCol: null,
        submitFunc() {
          doSearch();
        },
        /* labelCol: ONL_QUERY_LABEL_COL,
        wrapperCol: ONL_QUERY_WRAPPER_COL*/
      });

      /**
       * Thực thi truy vấn
       */
      function doSearch() {
        let formValues = getFieldsValue();
         // update-begin--author:liaozhiyang---date:20240517---for：【TV360X-28】Năm, năm-tháng, tuần truy vấn cho kết quả không chính xác
        transformDateValus(formValues);
         // update-end--author:liaozhiyang---date:20240517---for：【TV360X-28】Năm, năm-tháng, tuần truy vấn cho kết quả không chính xác
        // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
        transformGroupValus(formValues);
        // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
        // Còn cần thêm tham số thanh địa chỉ vào
        let data = Object.assign({}, toRaw(defaultValues.param), changeDataIfArray2String(formValues));
        emit('search', data, true);
      }
      /**
       * 2024-06-18
       * liaozhiyang
       * onlineTruy vấn theo phạm vi với giá trị mặc định của truy vấn thông thường không hoạt động
       * */
      const transformGroupDefValus = (obj) => {
        const values = { ...obj };
        const groupSchemas = formSchemas.value.filter((item) => ['groupTime', 'groupDatetime', 'groupNumber', 'groupDate'].includes(item.slot));
        if (groupSchemas.length) {
          Object.keys(values).forEach((filed) => {
            let key;
            const findItem = groupSchemas.find((item) => {
              if (item.field === filed) {
                key = filed;
                return true;
              }
              return false;
            });
            if (findItem) {
              const value = values[key];
              if (typeof value === 'string') {
                const arr = value.split(',');
                values[key] = [...arr];
              }
            }
          });
        }
        return values;
      };

      /**
       * 2024-05-20
       * liaozhiyang
       * 【TV360X-213】ĐưagroupDatetime,groupTime,groupDate,groupNumberCác trường phạm vi v.v. phân tách thành hai trường
       */
      const transformGroupValus = (values) => {
        if (values) {
          const groupSchemas = formSchemas.value.filter((item) => ['groupTime', 'groupDatetime', 'groupDate', 'groupNumber'].includes(item.slot));
          if (groupSchemas.length) {
            Object.keys(values).forEach((filed) => {
              let key;
              const findItem = groupSchemas.find((item) => {
                if (item.field === filed) {
                  key = filed;
                  return true;
                }
                return false;
              });
              if (findItem) {
                const value = values[key];
                // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
                // Khi giá trị của component phạm vi là mảng, cũng cần tách thành begin/end
                if (Array.isArray(value)) {
                  values[`${key}_begin`] = value[0];
                  values[`${key}_end`] = value[1];
                  delete values[key];
                } else if (typeof value === 'string') {
                  const arr = value.split(',');
                  values[`${key}_begin`] = arr[0];
                  values[`${key}_end`] = arr[1];
                  delete values[key];
                }
                // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
              }
            });
          }
        }
      };
      /**
      * 2024-05-20
      * liaozhiyang
      * Đặt lại các thời gian như năm, năm-tháng, tuần về ngày đầu tiên của định dạng hiện tại, vì khi lưu cũng là ngày đầu tiên (【TV360X-180】Tương thích với phạm vi thời gian)
      */
      const transformDateValus = (values) => {
        const dateSchemas = formSchemas.value.filter((item) => item.componentProps?.picker && item.componentProps.picker != 'default');
        if (dateSchemas.length) {
          Object.keys(values).forEach((filed) => {
            let key;
            const findItem = dateSchemas.find((item) => {
              if (item.field === filed || `${item.field}_begin` === filed || `${item.field}_end` === filed) {
                key = filed;
                return true;
              }
              return false;
            });
            if (findItem) {
              const value = values[key];
              if (value) {
                // update-begin--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
                const auto = (value, key, isEnd) => {
                  const picker = findItem.componentProps.picker;
                  if (picker === 'year') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('year').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('year').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'month') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('month').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('month').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'week') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('week').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('week').format('YYYY-MM-DD');
                    }
                  } else if (picker === 'quarter') {
                    if (isEnd) {
                      values[key] = dayjs(value).endOf('quarter').format('YYYY-MM-DD');
                    } else {
                      values[key] = dayjs(value).startOf('quarter').format('YYYY-MM-DD');
                    }
                  }
                };
                if (findItem?.slot === 'groupDate') {
                  // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
                  // Tương thích trường hợp giá trị ngày trong phạm vi là mảng
                  const arr = Array.isArray(value) ? value : String(value).split(',');
                  // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
                  auto(arr[0], `${key}_begin`, false);
                  auto(arr[1], `${key}_end`, true);
                  delete values[key];
                } else {
                  auto(value, key, false);
                }
                // update-end--author:liaozhiyang---date:20240530---for：【TV360X-213】Thay đổi component ngày/số trong truy vấn thông thường
              }
            }
          });
        }
      }

      /**
       * Việc đặt lại là đưa Giá trị của control truy vấn Đặt lại về giá trị mặc định
       * 2024-05-23
       * liaozhiyang
       * 【TV360X-124】Cung cấpclearSearchPhương thức trở về trạng thái ban đầu
       */
      async function clearSearch() {
        await resetFields();
        const { config, param } = toRaw(defaultValues);
        let rawValues = Object.assign({}, config, param);
        if (Object.keys(rawValues).length > 0) {
          // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
          // Khi đặt lại truy vấn, ngày/Giá trị mặc định của các component phạm vi như ngày giờ cần chuyển thành mảng mới hiển thị lại đúng
          // và customSetFieldsValue Giữ nhất quán: giá trị mặc định của truy vấn theo phạm vi (ví dụ "2024-01-01,2024-12-31") cần chuyển đổi thành mảng
          const values = transformGroupDefValus(rawValues);
          await setFieldsValue(values);
          // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
        }
        return rawValues;
      }

      async function resetSearch() {
        const rawValues = await clearSearch();
        // update-begin--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
        // Sau khi đặt lại, tham số truy vấn giữ nhất quán với việc nhấp truy vấn (tách phạm vi ngày begin/end）
        const values = { ...rawValues };
        transformDateValus(values);
        transformGroupValus(values);
        emit('search', changeDataIfArray2String(values), false);
        // update-end--author:liaozhiyang---date:20260803---for：【LHZP-381】Khi nhấp đặt lại, giá trị mặc định của ngày và ngày giờ không được thiết lập
      }

      /**
       * Một số dữ liệu ở định dạng mảng Ép chuyển thành chuỗi
       */
      function changeDataIfArray2String(data) {
        Object.keys(data).map((k) => {
          if (data[k]) {
            if (data[k] instanceof Array) {
              data[k] = data[k].join(',');
            }
          }
        });
        return data;
      }

      watch(
        () => toggleSearchStatus.value,
        (status) => {
          let names = hideList.value;
          if (names && names.length > 0) {
            let arr = [];
            for (let name of names) {
              arr.push({
                field: name,
                show: status,
              });
            }
            updateSchema(arr);
          }
        },
        { immediate: false }
      );
      /**
       * 2024-05-30
       * liaozhiyang
       * 【TV360X-392】NgàyplaceholderChỉnh sửa
       * */
      const getGroupDatePlaceholder = (data) => {
        let result = ['Ngày bắt đầu', 'Ngày kết thúc'];
        console.log(data);
        if (data?.picker) {
          switch (data?.picker) {
            case 'year':
              result = ['Năm bắt đầu', 'Năm kết thúc'];
              break;
            case 'month':
              result = ['Tháng bắt đầu', 'Tháng kết thúc'];
              break;
            case 'week':
              result = ['Tuần bắt đầu', 'Tuần kết thúc'];
              break;
            case 'quarter':
              result = ['Quý bắt đầu', 'Quý kết thúc'];
              break;
            default:
              result = ['Ngày bắt đầu', 'Ngày kết thúc'];
          }
        }
        return result;
      };
      return {  
        onlineQueryFormRef,
        registerForm,
        initDefaultValues,
        toggleButtonShow,
        toggleSearchStatus,
        doSearch,
        resetSearch,
        queryParams,
        formSchemas,
        clearSearch,
        getGroupDatePlaceholder,
        // update-begin--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
        whenFirstQueryDecided,
        // update-end--author:liaozhiyang---date:20260715---for：【LHZP-219】Khắc phục: trường hợp truy vấn có giá trị mặc định sẽ gọi API hai lần, hãy chỉ gọi API truy vấn ở lần cuối cùng
      };
    },
  };
</script>

<style scoped lang="less">
  .group-query-string {
    width: 20px;
    display: inline-block;
    text-align: center;
  }
  // Lề của điều kiện truy vấn phải căn chỉnh với danh sách, vì vậy lề của điều kiện truy vấn phải đặt thành0
  .jeecg-basic-table-form-container.p-0 {
    padding: 0;
  }
  // update-begin--author:liaozhiyang---date:20240514---for：【QQYUN-9241】formKhoảng cách trên dưới của biểu mẫu lớn hơn một chút
  .jeecg-basic-table-form-container {
    :deep(.ant-form-item) {
      &:not(.ant-form-item-with-help) {
        margin-bottom: 16px;
      }
    }
  }
  // update-end--author:liaozhiyang---date:20240514---for：【QQYUN-9241】formKhoảng cách trên dưới của biểu mẫu lớn hơn một chút
  .online-query-form {
    overflow-x: hidden;
    :deep(.ant-form) {
      max-height: 40vh;
      overflow-y: auto;
      overflow-x: hidden;
    }
  }
  // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-326】Khi không có nút đặt lại tìm kiếm, chữ mở rộng căn giữa theo chiều dọc
  .table-page-search-submitButtons {
    display: inline-flex;
    align-items: center;
    min-height: 32px;
    margin-left: 10px;
    overflow: hidden;
    // chỉ có mở rộng/Khi thu gọn, căn chỉnh theo chiều dọc với control truy vấn cùng hàng
    &.only-toggle {
      line-height: 32px;
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for：【LHZP-326】Khi không có nút đặt lại tìm kiếm, chữ mở rộng căn giữa theo chiều dọc
</style>
