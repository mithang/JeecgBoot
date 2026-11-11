<template>
  <div ref="printRef" :id="tableName + '_form'" class="onlineFormWrap" :class="[`formTemplate_${formTemplate}`]">
    <!-- update-begin--author:liaozhiyang---date:20240401---for：【QQYUN-8721】Trang chỉnh sửa không hiển thị in (đã cấu hình báo cáo Jimu) -->
    <!-- Nút in của báo cáo Jimu, chỉ khi đã cấu hình reportUrl mới hiển thị -->
    <!-- <div v-if="!!dbData.id && !!onlineExtConfigJson.reportPrintShow" style="text-align: right;position: absolute;top: 6px;right: 20px;z-index: 999">
      <PrinterOutlined title="In" @click="onOpenReportPrint" style="font-size: 16px"/>
    </div> -->
     <!-- update-end--author:liaozhiyang---date:20240401---for：【QQYUN-8721】Trang chỉnh sửa không hiển thị in (đã cấu hình báo cáo Jimu) -->

    <BasicForm ref="onlineFormRef" @register="registerForm" :name="'online-form_' + tableName"/>

    <!-- bảng con -->
    <!-- update-begin--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con -->
    <a-tabs v-model:activeKey="subActiveKey" v-if="hasSubTable && (isShare || themeTemplate !== ERP)">
    <!-- update-end--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con -->
      <a-tab-pane v-for="(sub, index) in subTabInfo" :tab="sub.describe" :key="index + ''" :forceRender="true">
        <div ref="subFormWrapRef" :style="{ 'overflow-y': 'auto', 'overflow-x': 'hidden', 'max-height': subFormHeight + 'px' }" v-if="sub.relationType == 1">
          <!-- bảng con-một-một -->
          <online-sub-form
            :ref="refMap[sub.key]"
            :table="sub.key"
            :id="sub.id"
            :disabled="onlineFormDisabled"
            :form-template="sub.formTemplate"
            :main-id="getSubTableForeignKeyValue(sub.foreignKey)"
            :properties="sub.properties"
            :required-fields="sub.requiredFields"
            :is-update="isUpdate"
            :modal-class="modalClass"
            @formChange="(arg) => handleSubFormChange(arg, sub.key)"
          />
        </div>
        <div v-else>
          <!-- bảng con-một-nhiều -->
          <JVxeTable
            :ref="refMap[sub.key]"
            toolbar
            keep-source
            :row-number="rowNumber"
            row-selection
            :height="
              // 【VUEN-803】Cố định bảng con một-nhiều340chiều cao, sửa vấn đề component cột tùy chỉnh bị che khuất
              subTableHeight
            "
            :disabled="onlineFormDisabled"
            :columns="sub.columns"
            :dataSource="subDataSource[sub.key]"
            :addBtnCfg="getSubAddBtnCfg"
            :removeBtnCfg="getSubRemoveBtnCfg"
            @valueChange="(event) => handleValueChange(event, sub.key)"
            @removed="(event) => handleRemoved(event, sub.key)"
            :authPre="getSubTableAuthPre(sub.key)"
            @added="handleAdded(sub, $event)"
            @executeFillRule="handleSubTableDefaultValue(sub, $event)">

            <template #toolbarAfterAdd="{ selectedRowIds }">
              <a-button-group class="sub-popup-button-group">
                <!-- Nút thêm mới hiển thị dạng popup trong bảng con -->
                <a-button
                    v-if="!onlineFormDisabled && getSubOpenAddBtnCfg.enabled && getBtnAuth('add', sub.key)"
                    :preIcon="getSubOpenAddBtnCfg.buttonIcon"
                    @click="openSubFormModalForAdd(sub)"
                >
                  <span>{{getSubOpenAddBtnCfg.buttonName}}</span>
                </a-button>
                <!-- Nút chỉnh sửa hiển thị dạng popup trong bảng con -->
                <a-button
                    v-if="!onlineFormDisabled && getSubOpenEditBtnCfg.enabled && getBtnAuth('update', sub.key) && selectedRowIds.length === 1"
                    :preIcon="getSubOpenEditBtnCfg.buttonIcon"
                    @click="openSubFormModalForEdit(sub)"
                >
                  <span>{{getSubOpenEditBtnCfg.buttonName}}</span>
                </a-button>
              </a-button-group>
            </template>

          </JVxeTable>

        </div>
      </a-tab-pane>
    </a-tabs>
    <Loading :loading="loading" :absolute="false" />
    <!-- Trong biểu mẫu có thể thông quaslotNút gửi tùy chỉnh, dùng trong biểu mẫu quy trình -->
    <slot name="bottom"></slot>

    <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa -->
    <online-pop-modal formTableType="3" :request="popModalRequest" :id="popTableId" @register="registerPopModal" @success="getPopFormData" :taskId="taskId" :tableName="popTableName" topTip isVxeTableData></online-pop-modal>
  </div>
</template>

<script>
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, defineComponent, ref, unref, watch, nextTick, toRaw, reactive, inject, provide, onMounted } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import {
    SUBMIT_FLOW_ID,
    SUBMIT_FLOW_KEY,
    VALIDATE_FAILED,
    ONL_FORM_TABLE_NAME,
    ENHANCEJS
  } from '../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick, omit, cloneDeep, debounce } from 'lodash-es';
  import { sleep } from '/@/utils';
  import { useFormItems, getRefPromise, useOnlineFormContext } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import OnlineSubForm from './OnlineSubForm.vue';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import {handleFillRuleWatchKeysMap, loadFormFieldsDefVal} from '../../util/FieldDefVal';
  import { getToken } from '/@/utils/auth';
  import { goJmReportViewPage } from '/@/utils'
  import { PrinterOutlined } from '@ant-design/icons-vue';
  import {useOnlineFormEvent, useOnlineEventContext} from '../../hooks/auto/useOnlinePopEvent';
  import OnlinePopModal from './OnlinePopModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useCustomHook, GET_FUN_BODY_REG } from "../../hooks/auto/useCustomHook";
  import { ERP } from "../../util/constant";
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { isArray } from '/@/utils/is';
  import dayjs from 'dayjs';
  import { usePermissionStore } from '/@/store/modules/permission';
  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  export default {
    name: 'OnlineForm',
    components: {
      BasicForm,
      Loading,
      OnlineSubForm,
      PrinterOutlined,
      OnlinePopModal
    },
    props: {
      id: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      isTree: {
        type: Boolean,
        default: false,
      },
      pidField: {
        type: String,
        default: '',
      },
      submitTip:{
        type: Boolean,
        default: true
      },
      izCopy:{
        type: Boolean,
        default: false
      },
      modalClass:{
        type: String,
        default: '',
      },
      themeTemplate: {
        type: String,
        default: '',
      },
      // update-begin--author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
      // erpPhong cách sẽ truyền tới mọi dữ liệu bảng con
      subTableSource: {
        default: () => ({}),
      },
      // update-end-author:liaozhiyang---date:20231128---for：【QQYUN-7260】erpKhi chỉnh sửa bảng chính, lưu các bản ghi của bảng con
      // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
      // Quy trình sẽ truyềntaskId
      taskId: {
        type: String,
      },
      // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
      cgBIBtnMap: Object,
      buttonSwitch: Object,
      // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
      isShare: {
        type: Boolean,
        default: false,
      },
      // update-end--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
    },
    emits: ['success', 'rendered', 'close','validate'],
    setup(props, { emit }) {
      console.log('onlineForm-setup》》');
      const { createMessage: $message } = useMessage();

      // In biểu mẫuref
      const printRef = ref(null);
      // Biểu mẫuref
      const onlineFormRef = ref(null);
      const single = ref(true);
      // Trạng thái tải
      const loading = ref(false);
      // Hiển thị nhắc nhở
      const showTip = ref(true);
      const tableType = ref(1);
      // Địa chỉ gửi chỉnh sửa của popup tùy chỉnh
      const customEditSubmitUrl = ref('');

      // Gửi biểu mẫu đồng thời gửi quy trình
      const submitFlowFlag = ref(false);
      const isUpdate = ref(false);
      const { getIsMobile } = useAppInject();
      const isSetFormLabelLength = ref(false);
      // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      // erpBảng con lấy khóa ngoại
      const foreignkey = inject("foreignkey", {value:{}});
      // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
      const rowNumber = ref(getIsMobile.value ? false : true);
      const subFormWrapRef = ref(null);
      let formItemData = null;
      /**
       * onlineCấu hình mở rộng biểu mẫu
       */
      const onlineExtConfigJson = reactive({
        reportPrintShow: 0,
        reportPrintUrl: '',
        joinQuery: 0,
        modelFullscreen: 0,
        modalMinWidth: '',
        commentStatus: 0
      });

      const {
        onlineFormContext,
        resetContext,

        getSubAddBtnCfg,
        getSubRemoveBtnCfg,
        getSubOpenAddBtnCfg,
        getSubOpenEditBtnCfg,
      } = useOnlineFormContext(props);

      const {
        formSchemas,
        defaultValueFields,
        changeDataIfArray2String,
        tableName,
        dbData,
        checkOnlyFieldValue,
        hasSubTable,
        subTabInfo,
        refMap,
        subDataSource,
        baseColProps,
        createFormSchemas,
        linkDownList,
        fieldDisplayStatus,
        labelCol,
        wrapperCol,
        labelWidth
      } = useFormItems(props, onlineFormRef);
      let { EnhanceJS, initCgEnhanceJs } = useEnhance(onlineFormContext, false);
      // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-8405】Vùng hiển thị phía di động hiển thị ba trường
      watch(
        [subTabInfo, getIsMobile],
        () => {
          if (getIsMobile.value && subTabInfo.value.length) {
            subTabInfo.value.forEach((item) => {
              if (item.relationType != 1) {
                item.columns.forEach((cItem) => {
                  cItem.width = 100;
                });
              }
            });
          }
        },
        { immediate: true }
      );
      // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-8405】Vùng hiển thị phía di động hiển thị ba trường
      // cái mớijsTăng cường
      const { executeJsEnhanced } = useCustomHook({}, onlineFormContext);

      //Cấu hình biểu mẫu
      const [registerForm, { setProps, validate, resetFields, clearValidate, setFieldsValue, updateSchema, getFieldsValue, scrollToField }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        // update-begin--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
        labelWidth,
        // update-end--author:liaozhiyang---date:20240329---for：【QQYUN-7872】onlineBiểu mẫulabelTối ưu khi dài
        // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        labelCol,
        wrapperCol
        // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
      });

      // Vô hiệu hóa biểu mẫu
      const onlineFormDisabled = ref(false);
      function handleFormDisabled() {
        let flag = props.disabled;
        onlineFormDisabled.value = flag;
        setProps({ disabled: flag });
      }

      /**
       * status: Có phải trang sửa đổi hay không
       * record: Dữ liệu hàng của trang danh sách
       * param： Danh sách dạng cây thêm nút con nút cấp cha được truyền vàoid
       * */
      async function show(status, record, param) {
        console.log('Thêm mới/chỉnh sửa đi vào biểu mẫu》》form', record);
        //update-begin-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống
        //Nếu là cây Cần thiết lập lạipidcủaformSchema Cho nó làm mới dữ liệu của control cây
        await updatePidFieldDict();
        //update-end-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống
        //Đặt lại địa chỉ gửi biểu mẫu của popup tùy chỉnh
        customEditSubmitUrl.value = '';
        await resetFields();
        // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9345】Nhấp chỉnh sửa rồi nhấp thêm mới, component sẽ trực tiếp kích hoạt kiểm chứng
        setTimeout(() => {
          clearValidate();
        }, 0);
        // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9345】Nhấp chỉnh sửa rồi nhấp thêm mới, component sẽ trực tiếp kích hoạt kiểm chứng
        dbData.value = '';
        let flag = unref(status);
        isUpdate.value = flag;
        // update-begin--author:liaozhiyang---date:20240301---for：【QQYUN-8401】Popup thêm mới của bảng con một-một xóa dữ liệu lần trước
        restSubForm();
        // update-end--author:liaozhiyang---date:20240301---for：【QQYUN-8401】Popup thêm mới của bảng con một-một xóa dữ liệu lần trước
        if (flag) {
          // Trang chỉnh sửa
          await edit(record);
        } else {
          // Trang thêm mới
          editSubVxeTableData();
        }
        nextTick(() => {
          if (!flag && param) {
            //Nếu là trang thêm mới và paramTruyền vào có giá trị Cần thiết lập biểu mẫu
            setFieldsValue(param);
            props.izCopy && setSubTableFieldsValue(param);
          }
          handleDefaultValue();
          // Tất cả thông tin đã tải xong Kích hoạtloadedsự kiện
          handleCgButtonClick('js', 'loaded');
          //Xử lý hiệu quả vô hiệu hóa của biểu mẫu
          handleFormDisabled();
          // update-begin--author:liaozhiyang---date:20240527---for【TV360X-114】Khi chỉnh sửa bảng con một-một, thanh cuộn quay về đầu trang
          if (subFormWrapRef.value?.length) {
            subFormWrapRef.value[0].scrollTop = 0;
          }
          // update-end--author:liaozhiyang---date:20240527---for【TV360X-114】Khi chỉnh sửa bảng con một-một, thanh cuộn quay về đầu trang
        });
      }
      //update-begin-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống
      /**
       * Làm mới trong biểu mẫu JTreeSelect Dữ liệu cây
       * - Bảng cây: làm mới trường nút cha, đồng thời xóa trống hiddenNodeKey
       * - Bảng thông thường: mỗi lần vào biểu mẫu (bao gồm lần thêm mới thứ hai/chỉnh sửa) tải lại control cây tùy chỉnh
       */
      async function updatePidFieldDict() {
        let arr = formSchemas.value;
        if (!arr || arr.length === 0) {
          console.log('Không lấy được thông tin cấu hình biểu mẫu, có thể là lần đầu mở trang thêm mới');
          return;
        }
        // update-begin--author:liaozhiyang---date:20260804---for：OnlineKhi vào biểu mẫu lần thứ hai JTreeSelect Tải lại dữ liệu cây
        const treeSelectSchemas = arr.filter((item) => item.component === 'JTreeSelect');
        if (treeSelectSchemas.length > 0) {
          const reload = new Date().getTime();
          const pidFieldName = props.isTree === true ? props.pidField : '';
          await updateSchema(
            treeSelectSchemas.map((item) => {
              const componentProps = { reload };
              // Trường nút cha của bảng cây: xóa trống hiddenNodeKey, khi chỉnh sửa mới lọc theo nút hiện tại
              if (pidFieldName && item.field === pidFieldName) {
                componentProps.hiddenNodeKey = '';
              }
              return {
                field: item.field,
                componentProps,
              };
            })
          );
        }
        // update-end--author:liaozhiyang---date:20260804---for：OnlineKhi vào biểu mẫu lần thứ hai JTreeSelect Tải lại dữ liệu cây
      }
      //update-end-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống

      const watchDefValParamMap = {
        keys: [],
        map: new Map(),
        calcFn: new Map()
      };

      /**
       * Logic giá trị mặc định của biểu mẫu hiện tại-Kích hoạt khi vào trang thêm mới
       */
      function handleDefaultValue() {
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        if (unref(isUpdate) === false) {
          loadFormFieldsDefVal(fieldProperties, (values) => {
            setFieldsValue(values);
          });
        }
        // Xử lý tham số động trong giá trị mặc định của quy tắc điền giá trị
        const watchKeyMap = handleFillRuleWatchKeysMap(fieldProperties)
        watchDefValParamMap.keys = [...watchKeyMap.keys()]
        watchDefValParamMap.map = watchKeyMap
        watchDefValParamMap.calcFn.clear()
      }

      /**
       * Logic giá trị mặc định của biểu mẫu hiện tại-Khi giá trị thay đổi thì tính toán lại
       */
      function recalcDefaultValue(field, value) {
        // Phán đoán trường đang thay đổi hiện tại có đang được theo dõi hay khôngkeystrong
        if (watchDefValParamMap.keys.includes(field)) {
          let fn = watchDefValParamMap.calcFn.get(field)
          if (typeof fn !== 'function') {
            // Chống rung (debounce)100ms
            fn = debounce(() => {
              let fieldProp = toRaw(defaultValueFields[tableName.value]);
              if (Array.isArray(fieldProp) && fieldProp.length > 0) {
                const fieldKeys = watchDefValParamMap.map.get(field)
                fieldProp = fieldProp.filter(item => {
                  return fieldKeys.includes(item.field)
                })
              } else {
                fieldProp = []
              }
              if (fieldProp.length > 0) {
                let formData = getFieldsValue();
                loadFormFieldsDefVal(fieldProp, (values) => setFieldsValue(values), formData);
              }
            }, 150)
            watchDefValParamMap.calcFn.set(field, fn)
          }
          fn(value)
        }
      }

      /**
       * Giá trị mặc định của bảng con-Khi bảng con thêm hàng mới thì kích hoạt logic này
       */
      function handleSubTableDefaultValue(sub, $event) {
        //console.log('Quy tắc điền giá trị', sub, $event)
        let subFieldProperties = toRaw(defaultValueFields[sub.key]);
        loadFormFieldsDefVal(subFieldProperties, (values) => {
          const { row, target } = $event;
          let v = [{ rowKey: row.id, values: { ...values } }];
          target.setValues(v);
        });
      }

      async function edit(record) {
        let formData = await getFormData(record.id);
        //update-begin-author:taoyan date:2022-5-31 for: VUEN-1194 【online】Sau khi điền xong trường, thiết lập quyền thành không hiển thị, khi chỉnh sửa dữ liệu lần nữa thì giá trị gốc bị thay bằng rỗng
        dbData.value = Object.assign({}, record, formData);
        //update-end-author:taoyan date:2022-5-31 for: VUEN-1194 【online】Sau khi điền xong trường, thiết lập quyền thành không hiển thị, khi chỉnh sửa dữ liệu lần nữa thì giá trị gốc bị thay bằng rỗng
        //Gán giá trị biểu mẫu
        let arr = realFormFieldNames.value;
        //update-begin-author:taoyan date:2022-7-11 for:  2、online Xem chi tiết vẫn nhắc bắt buộc điền
        let values = pick(formData, ...arr);
        if(props.disabled){
          //Trang chi tiết Loại bỏ dữ liệu trống
          Object.keys(values).map(k=>{
            if(!values[k] && values[k]!==0 && values[k]!=='0'){
              delete values[k]
            }
          })
        }
        await setFieldsValue(values);
        // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-87】Khi chỉnh sửa bảng cây, không được chọn chính nó và các nút con cháu làm nút cha
        treeTablefilterNode(record.id);
        // update-end--author:liaozhiyang---date:20240523---for：【TV360X-87】Khi chỉnh sửa bảng cây, không được chọn chính nó và các nút con cháu làm nút cha
        //update-end-author:taoyan date:2022-7-11 for:  2、online Xem chi tiết vẫn nhắc bắt buộc điền
        editSubVxeTableData(formData);
      }

      function editSubVxeTableData(record) {
        if (!record) {
          // Trang thêm mới cần xóa sạch dữ liệu bảng con
          record = {};
        }
        let keys = Object.keys(subDataSource.value);
        if (keys && keys.length > 0) {
          let obj = {};
          for (let key of keys) {
            obj[key] = record[key] || [];
          }
          subDataSource.value = obj;
        }
      }
      /**
       * 2024-05-23
       * liaozhiyang
       * 【TV360X-87】Khi chỉnh sửa bảng cây, không được chọn chính nó và các nút con cháu làm nút cha
       * @param id (Nút hiện tại)
       */
      function treeTablefilterNode(id) {
        if (props.isTree === true) {
          const { schema } = formItemData;
          const properties = schema.properties ?? {};
          const arr = Object.entries(properties);
          if (arr.length) {
            const selectTreeData = arr.find(([key, value]) => value.view === 'sel_tree' && value.pidComponent != null);
            if (selectTreeData) {
              const field = selectTreeData[0];
              const compData = formSchemas.value.find((item) => item.field == field);
              if (compData) {
                updateSchema({
                  field,
                  componentProps: {
                    hiddenNodeKey: id,
                  },
                });
              }
            }
          }
        }
      }
      /**
       * 2024-03-01
       * liaozhiyang
       * Đặt lại bảng con một-mộtformDữ liệu
       */
      function restSubForm(){
        subTabInfo.value?.forEach(item=>{
          if(item.relationType == 1){
            // update-begin--author:liaozhiyang---date:20240321---for：【QQYUN-8563】erpBiểu thức giá trị mặc định được cấu hình của phong cách Chưa hiển thị
            if (refMap[item.key].value) {
              // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-249】Mở popup thêm mới lần thứ hai, giá trị mặc định của bảng con một-một bị mất
              if (unref(isUpdate) === false && typeof refMap[item.key].value[0]?.resetAndApplyDefaultValue === 'function') {
                refMap[item.key].value[0].resetAndApplyDefaultValue();
              } else {
                refMap[item.key].value[0].resetFields();
              }
              // update-end--author:liaozhiyang---date:20260714---for：【LHZP-249】Mở popup thêm mới lần thứ hai, giá trị mặc định của bảng con một-một bị mất
            }
            // update-end--author:liaozhiyang---date:20240321---for：【QQYUN-5803】erpBiểu thức giá trị mặc định được cấu hình của phong cách Chưa hiển thị
          }
        });
      }
      /**
       * Thiết lập dữ liệu của bảng con
       */
      function setSubTableFieldsValue(param){
        //Xử lý dữ liệu bảng con một-một
        subTabInfo.value?.forEach(item=>{
          if(item.relationType == 1){
            if (refMap[item.key].value && param[item.key]) {
              const oneDefaultValue = param[item.key] && param[item.key].length>0?param[item.key][0] : {};
              setTimeout(()=>{
                refMap[item.key].value[0].setValues(omit(oneDefaultValue, ['id']));
              },20)
            }
          }
        });
        //Xử lý dữ liệu bảng con một-nhiều
        let keys = Object.keys(subDataSource.value);
        if (keys && keys.length > 0) {
          let obj = {};
          for (let key of keys) {
            let arr = [];
            if(param[key]){
              param[key].forEach(item=>{
                arr.push(omit(item, ['id']));
              });
            }
            obj[key] = arr;
          }
          subDataSource.value = obj;
        }
      }

      let realFormFieldNames = computed(() => {
        let arr = formSchemas.value;
        let names = [];
        for (let a of arr) {
          names.push(a.field);
        }
        return names;
      });

      function getFormData(dataId) {
        let url = `${urlObject.optPre}${props.id}/${dataId}`;
        return new Promise((resolve, reject) => {
          defHttp
            .get({ url }, { isTransformResponse: false })
            .then((res) => {
              if (res.success) {
                resolve(res.result);
              } else {
                reject();
                $message.warning(res.message);
              }
            })
            .catch(() => {
              reject();
            });
        });
      }

      // Render biểu mẫu
      async function createRootProperties(data) {
        tableType.value = data.head.tableType;
        tableName.value = data.head.tableName;
        single.value = data.head.tableType == 1;
        handleExtConfigJson(data.head.extConfigJson);

        createFormSchemas(data.schema.properties, data.schema.required, checkOnlyFieldValue, onlineExtConfigJson);
        EnhanceJS = initCgEnhanceJs(data.enhanceJs);
        emit('rendered', onlineExtConfigJson);

        //Lắng nghe sự kiện thay đổi biểu mẫu
        let formRefObject = await getRefPromise(onlineFormRef);
        formRefObject.$formValueChange = (field, value, changeFormData) => {
          onValuesChange(field, value);
          if(changeFormData){
            //Nếu tồn tại dữ liệu của control biểu mẫu khác, hãy trực tiếp thiết lập giá trị đó
            setFieldsValue(changeFormData)
          }
          onChangeEvent(field, value, changeFormData)
          recalcDefaultValue(field, value)
        };
        // QQYUN-5315【onlineBiểu mẫu 】onlineBiểu mẫu cũng cần hỗ trợhookTăng cường
        if(EnhanceJS && EnhanceJS["setup"]){
          executeEnhanceFormJsHook(EnhanceJS["setup"])
        }

        formItemData = data;
      }

      //  Kích hoạt biểu mẫuchangesự kiện khác với cái cũonlchange
      function onChangeEvent(field, value, changeFormData) {
        onlineFormContext.changEvent(field, value, changeFormData)
      }

      // Thêm biểu mẫuchangesự kiện
      function onlineFormValueChange(callback) {
        onlineFormContext.addObject2Context("changEvent", callback)
      }

      /**
       * Xử lý cấu hình mở rộng
       */
      function handleExtConfigJson(jsonStr) {
        //update-begin-author:taoyan date:2023-2-6 for: QQYUN-4082 Mặc định mởonlinebiểu mẫu, mặc định toàn màn hình
        let extConfigJson = { reportPrintShow: 0, reportPrintUrl: '', joinQuery: 0, modelFullscreen: 0, modalMinWidth: '', commentStatus: 0, formLabelLength: null };
        //update-end-author:taoyan date:2023-2-6 for: QQYUN-4082 Mặc định mởonlinebiểu mẫu, mặc định toàn màn hình
        if (jsonStr) {
          extConfigJson = JSON.parse(jsonStr);
          // update-begin--author:liaozhiyang---date:20240326---for：【QQYUN-8644】Hiệu ứng phía di động khi đóng cửa sổ trò chuyện
          if (getIsMobile.value) {
            extConfigJson.commentStatus = 0;
          }
          // update-begin--author:liaozhiyang---date:20240326---for：【QQYUN-8644】Hiệu ứng phía di động khi đóng cửa sổ trò chuyện
        }
        isSetFormLabelLength.value = !!extConfigJson.formLabelLength;
        Object.keys(extConfigJson).map((k) => {
          onlineExtConfigJson[k] = extConfigJson[k];
        });
      }

      // Gửi biểu mẫu và quy trình
      function submitFormAndFlow() {
        submitFlowFlag.value = true;
        handleSubmit();
      }

      function handleSubmit(tip = true) {
        showTip.value = tip;
        if (single.value === true) {
          handleSingleSubmit();
        } else {
          handleOne2ManySubmit();
        }
      }
      //Kiểm chứng mục bắt buộc điền
      async function handleValidate() {
        if (single.value === true) {
          try {
             await validate();
          }catch (e) {
            if (Array.isArray(e?.errorFields) && e.errorFields[0]) {
              emit('validate',{passed:false})
            }
          }
        } else {
          await validateAll()
        }
      }

      function handleOne2ManySubmit() {
        validateAll().then((formData) => {
          handleApplyRequest(formData);
        });
      }

      // Kích hoạt mọi kiểm chứng biểu mẫu
      function validateAll() {
        let temp = {};
        return new Promise((resolve, reject) => {
          // Xác thực biểu mẫu bảng chính
          validate().then(
            (values) => resolve(values),
            ({ errorFields, values }) => {
              reject({
                // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
                errorFields,
                values,
                // update-end--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
                code: VALIDATE_FAILED,
                key: tableName.value,
                // Cuộn tới trường chưa thông qua kiểm chứng
                scrollToField: () => errorFields[0] && scrollToField(errorFields[0].name, { behavior: 'smooth', block: 'center' }),
              });
            }
          );
        })
          .then((result) => {
            Object.assign(temp, changeDataIfArray2String(result));
            // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
            // erp Lối vào danh sách: trong popup không có bảng con, bảng con được duy trì riêng tại trang danh sách; lối vào chia sẻ isShare sẽ hiển thị bảng con Tab, bắt buộc phải kiểm chứng và gửi cùng nhau, nếu không bảng chính-con không liên kết được
            // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
            // Lối vào chia sẻ ERP Tạo mới bảng chính-con chưa liên kết
            if (props.themeTemplate === ERP && !props.isShare) {
              return Promise.resolve({});
            } else {
              return validateSubTableFields();
            }
            // update-end--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
            // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
          })
          .then((allTableData) => {
            Object.assign(temp, allTableData);
            return Promise.resolve(temp);
          })
          .catch((e) => {
            if (e === VALIDATE_FAILED || e?.code === VALIDATE_FAILED) {
              // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
              analysisErrorInfo(e.errorFields, e.values, single.value, e.key).then((val) => {
                if (!val) {
                  $message.warning('Kiểm chứng chưa thông qua');
                }
              });
              // update-end--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
              if (e.key) {
                changeTab(e.key);
                if (e.scrollToField) {
                  setTimeout(() => e.scrollToField(), 150)
                }
              }
              emit('validate',{passed:false})
            } else {
              console.error(e);
            }
            return Promise.reject(null);
          });
      }

      /**
       * Chuyển đổitabtới trang xuất hiện lỗi kiểm chứng
       * */
      function changeTab(key) {
        let arr = subTabInfo.value;
        for (let i = 0; i < arr.length; i++) {
          if (key == arr[i].key) {
            // update-begin-author:sunjianlei date:2022-7-7 for:【VUEN-1425】【vue3】Trong bảng con đối-nhiều, kiểm chứng không qua mà không nhắc
            let activeKey = i + '';
            if (subActiveKey.value === activeKey) {
              break;
            }
            subActiveKey.value = activeKey;
            // Nếu mục tiêu được chuyển đổitablà bảng con một-nhiều, thì cần kiểm chứng lại một lần
            if (arr[i].relationType === 0) {
              let instance = getSubTableInstance(key);
              sleep(300, () => instance?.validateTable());
            }
            // update-end-author:sunjianlei date:2022-7-7 for:【VUEN-1425】【vue3】Trong bảng con đối-nhiều, kiểm chứng không qua mà không nhắc
            break;
          }
        }
      }

      // Xác thực bảng con
      function validateSubTableFields() {
        return new Promise(async (resolve, reject) => {
          let subData = {};
          try {
            let arr = subTabInfo.value;
            for (let i = 0; i < arr.length; i++) {
              let key = arr[i].key;
              let instance = getSubTableInstance(key);
              if (arr[i].relationType == 1) {
                try {
                  let subFormData = await instance.getAll();
                  subData[key] = [];
                  subData[key].push(subFormData);
                } catch (e) {
                  return reject({code: VALIDATE_FAILED, key, ...e});
                }
              } else {
                let errMap = await instance.fullValidateTable();
                if (errMap) {
                  return reject({code: VALIDATE_FAILED, key});
                }
                subData[key] = instance.getTableData();
              }
            }
          } catch (e) {
            reject(e);
          }
          resolve(subData);
        });
      }

      /**
       * Gửi biểu mẫu-bảng đơn
       */
      async function handleSingleSubmit() {
        try {
          let values = await validate();
          values = Object.assign({}, dbData.value, values);
          values = changeDataIfArray2String(values);
          loading.value = true;
          handleApplyRequest(values);
        } catch (e) {
          if (Array.isArray(e?.errorFields) && e.errorFields[0]) {
            // Cuộn tới trường chưa thông qua kiểm chứng
            scrollToField(e.errorFields[0].name, { behavior: 'smooth', block: 'center' });
            // update-begin--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
            analysisErrorInfo(e.errorFields, e.values, single.value);
            // update-end--author:liaozhiyang---date:20240617---for：【TV360X-496】Dùng loại số, kiểm chứng số tiền, giá trị mặc định của control tính ra số thập phân khiến kiểm chứng không qua được, cần đưa ra nhắc nhở
            emit('validate',{passed:false})
          }
        } finally {
          loading.value = false;
          emit('close');
        }
      }
      /**
       * 2024-06-17
       * liaozhiyang
       * Phân tích thông tin lỗi xem có component nào làinput-number, lỗi kiểm chứng do giá trị mặc định có số thập phân gây ra
       */
      async function analysisErrorInfo(errorFields, values, single, curTableName = null) {
        let result = false;
        if (errorFields?.length) {
          const schema = formItemData.schema ?? {};
          const { properties = {} } = schema;
          const errorField = errorFields[0].name[0];
          let curField;
          if (single || curTableName === tableName.value) {
            // bảng đơn hoặc bảng chính
            curField = properties[errorField];
          } else {
            // một-mộtormột-nhiều(Một-nhiều có thể loại trừ, vì một-nhiều chính là hiển thị giá trị gốc)
            const subSchema = properties[curTableName];
            const { properties: subProperties = {} } = subSchema;
            curField = subProperties[errorField];
          }
          if (curField.type === 'number' && curField.view === 'number' && curField.defVal) {
            await loadFormFieldsDefVal(
              [
                {
                  field: errorField,
                  type: curField.type,
                  value: curField.defVal,
                  view: curField.view,
                },
              ],
              (res) => {
                if (res[errorField] === values[errorField]) {
                  $message.warning(`${curField.title}Giá trị mặc định của là:${values[errorField]}, dẫn đến kiểm chứng không thông qua, cần cấu hình đúng giá trị mặc định!`);
                  result = true;
                }
              }
            );
          }
        }
        return result;
      }

      //Trước khi gửi dữ liệu Trước tiên chạy qua phần tùy chỉnh một lượtJSKiểm chứng
      function handleApplyRequest(formData) {
        // 【issues/7190】【issues/9873】ERPBảng con trong danh sách là dữ liệu phân trang, do API bảng con duy trì độc lập, cấm gửi toàn bộ cùng với bảng chính
        customBeforeSubmit(context, formData)
          .then(() => {
            doApplyRequest(formData);
          })
          .catch((msg) => {
            $message.warning(msg);
          });
      }
      /**
       * 2024-04-30
       * liaozhiyang
       * Đưa ngày(Năm-tháng-ngày)Component đã chọn định dạng năm và năm-tháng, trước khi gửi dữ liệu, đặt lại về ngày đầu tiên của định dạng đó.
       */
      const changeDateValue = (formData) => {
        const { schema } = formItemData;
        const { properties } = schema;
        const recursion = (formData, properties) => {
          Object.entries(formData).forEach(([key, value]) => {
            const item = properties[key];
            if (item) {
              if (item.view === 'tab' && isArray(value)) {
                // bảng con(một-một)
                if (item.properties) {
                  value.forEach((vItem) => {
                    recursion(vItem, item.properties);
                  });
                }
                // Bảng con (một-nhiều)
                if (item.columns?.length) {
                  const findArr = cloneDeep(item.columns.filter((cItem) => cItem.type === 'date' && cItem.fieldExtendJson));
                  if (findArr.length) {
                    // Xử lý cấu trúc thành giống như một-một;
                    const buildProperties = {};
                    findArr.forEach((fItem) => {
                      buildProperties[fItem.key] = {
                        view: 'date',
                        fieldExtendJson: fItem.fieldExtendJson,
                      };
                    });
                    value.forEach((vItem) => {
                      recursion(vItem, buildProperties);
                    });
                  }
                };
              } else if (item.view === 'date' && typeof value === 'string' && value !== '') {
                // Component ngày
                let fieldExtendJson = item.fieldExtendJson;
                if (fieldExtendJson) {
                  fieldExtendJson = JSON.parse(fieldExtendJson);
                  if (fieldExtendJson.picker && fieldExtendJson.picker !== 'default') {
                    /**
                     * Cần đặt năm, năm-tháng thành ngày đầu tiên trong khoảng thời gian này ([Năm-quý]không cần xử lýantdCái được truyền về chính là ngày đầu tiên của quý đó,[Năm-tuần]cũng không xử lý)
                     * Ví dụ định dạng ngày là năm, thời gian truyền cho cơ sở dữ liệu phải là20240101
                     * Ví dụ định dạng ngày là năm-tháng (đã chọn202502) , thời gian truyền cho cơ sở dữ liệu phải là20250201
                     */
                    if (fieldExtendJson.picker === 'year') {
                      formData[key] = dayjs(value).set('month', 0).set('date', 1).format('YYYY-MM-DD');
                    } else if (fieldExtendJson.picker === 'month') {
                      formData[key] = dayjs(value).set('date', 1).format('YYYY-MM-DD');
                    } else if (fieldExtendJson.picker === 'week') {
                      // 【TV360X-28】Tuần cũng phải đặt lại về ngày đầu tiên, nếu không sẽ không truy vấn ra kết quả
                      formData[key] = dayjs(value).startOf('week').format('YYYY-MM-DD');
                    }
                  }
                }
              }
            }
          });
        };
        recursion(formData, properties);
      };
      /**
       * Khởi tạo yêu cầu
       * @param formData
       */
      function doApplyRequest(formData) {
        // Mảng không có phần tử thì trực tiếp đặt trống
        Object.keys(formData).map((key) => {
          if (Array.isArray(formData[key])) {
            if (formData[key].length == 0) {
              formData[key] = '';
            }
          }
        });
        // update-begin--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        changeDateValue(formData);
        // update-end--author:liaozhiyang---date:20240430---for：【issues/6094】online Ngày(Năm-tháng-ngày)Control bổ sung các định dạng như năm, năm-tháng, năm-tuần, năm-quý
        console.log('Gửi dữ liệu biểu mẫu》》》form:', formData);
        let customUrl = customEditSubmitUrl.value;
        let url = `${urlObject.optPre}${props.id}?tabletype=${tableType.value}`;
        if (customUrl) {
          url = `${customUrl}?tabletype=${tableType.value}`;
        }
        // Nếu cần gửi quy trình Cần thiết lập thêm một tham số
        if (submitFlowFlag.value === true) {
          formData[SUBMIT_FLOW_KEY] = 1;
        }
        // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
        // Nếu có khóa ngoại thì cần cộng thêm
        if (foreignkey.value.field && foreignkey.value.value) {
          formData[foreignkey.value.field] = foreignkey.value.value;
        }
        // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
        let method = isUpdate.value === true ? 'put' : 'post';
        defHttp
          .request({ url, method, params: formData }, { isTransformResponse: false })
          .then((res) => {
            //console.log('Gửi biểu mẫu hoàn tất', res)
            if (res.success) {
              if (res.result) {
                formData[SUBMIT_FLOW_ID] = res.result;
              }
              //Làm mới danh sách
              emit('success', formData);
              // Biểu mẫu gửi trong đơn yêu cầu công việc cũng đi theo logic này, lưu thành công không cần thông tin nhắc nhở
              if(props.submitTip === true && showTip.value === true){
                $message.success(res.message)
              }
            } else {
              $message.warning(res.message);
            }
          })
          .finally(() => {
            loading.value = false;
            emit('close');
          });
      }

      function triggleChangeValues(values, id, target) {
        if (id && target) {
          if (target.vxeProps) {
            //bảng con một-nhiều
            target.setValues([
              {
                rowKey: id,
                values: values,
              },
            ]);
          } else {
            //bảng con một-một
            target.setValues(values);
          }
        } else {
          //bảng chính
          setFieldsValue(values);
        }
      }
      function triggleChangeValue(field, value) {
        let obj = {};
        obj[field] = value;
        setFieldsValue(obj);
      }

      // bảng con một-nhiềuTabcủaKey, dùng để tự động chuyển hướng khi kiểm chứng chưa thông qua
      const subActiveKey = ref('0');
      // update-begin-author:liaozhiyang---date:20240313---for：【QQYUN-9034】onlinePopup: thiết lập chiều cao nội dung của bảng con một-một ở phía di động không hợp lý
      const subFormHeight = ref(getIsMobile.value ? 'auto' : 500);
      // update-end-author:liaozhiyang---date:20240313---for：【QQYUN-9034】onlinePopup: thiết lập chiều cao nội dung của bảng con một-một ở phía di động không hợp lý
      // Chiều cao bảng dữ liệu của bảng con
      // 【VUEN-803】Cố định bảng con một-nhiều340chiều cao, sửa vấn đề component cột tùy chỉnh bị che khuất
      const subTableHeight = ref(340);

      function getSubTableForeignKeyValue(key) {
        if (isUpdate.value === true) {
          let formData = dbData.value;
          return getValueIgnoreCase(formData, key);
        }
        return '';
      }

      /**
       * VUEN-1056 30、một-nhiều được tạo ra, khi chỉnh sửa thì dữ liệu bảng con không gắn lên được
       */
      function getValueIgnoreCase(data, key) {
        if (data) {
          let temp = data[key];
          if (!temp && temp !== 0) {
            temp = data[key.toLowerCase()];
            if (!temp && temp !== 0) {
              temp = data[key.toUpperCase()];
            }
          }
          return temp;
        }
        return '';
      }

      //Xử lý sự kiện thay đổi biểu mẫu của bảng con một-một
      function handleSubFormChange(valueObj, tableKey) {
        if (EnhanceJS && EnhanceJS[tableKey + '_onlChange']) {
          let tableChangeObj = EnhanceJS[tableKey + '_onlChange']();
          let columnKey = Object.keys(valueObj)[0];
          if (tableChangeObj[columnKey]) {
            let subRef = getSubTableInstance(tableKey)
            let formEvent = subRef.getFormEvent();
            let event = {
              column: { key: columnKey },
              value: valueObj[columnKey],
              ...formEvent,
            };
            tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
          }
        }
      }

      //Xử lý sự kiện thay đổi của bảng con một-nhiều
      function handleValueChange(event, tableKey) {
        if (EnhanceJS && EnhanceJS[tableKey + '_onlChange']) {
          let tableChangeObj = EnhanceJS[tableKey + '_onlChange'](onlineFormContext);
          //update-begin-author:taoyan date:2022-8-12 for: VUEN-1892【onlinePopup bảng con】có quan hệ liên kết chính-phụjskhi đó, popup bảng con đã sửa dữ liệu, trường bảng chính chưa sửa
          if(event.column === 'all'){
            let keys = Object.keys(tableChangeObj);
            if(keys.length>0){
              for(let key of keys){
                tableChangeObj[key].call(onlineFormContext, onlineFormContext, event);
              }
            }
          }else{
            let columnKey = event.column.key || event.col.key
            if (tableChangeObj[columnKey]) {
              //event.row.id Thêm điều kiện này là vì: popformThêm dữ liệu vàovxetablekhi không cần kích hoạt sự kiện thay đổi
              if(event.row && event.row.id){
                tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
              }
            }
          }
          //update-end-author:taoyan date:2022-8-12 for: VUEN-1892【onlinePopup bảng con】có quan hệ liên kết chính-phụjskhi đó, popup bảng con đã sửa dữ liệu, trường bảng chính chưa sửa
        }
      }

      /**
       * 2024-07-30
       * liaozhiyang
       * 【TV360X-1958】Khi xóa bảng con một-nhiềujsTăng cường tên bảng con_onlChange Sự kiện không được kích hoạt
       *  Xử lý sự kiện xóa của bảng con một-nhiều
       * */
      function handleRemoved(event, tableKey) {
        if (EnhanceJS && EnhanceJS[tableKey + '_onlChange']) {
          let tableChangeObj = EnhanceJS[tableKey + '_onlChange'](onlineFormContext);
          let keys = Object.keys(tableChangeObj);
          if (keys.length > 0) {
            for (let key of keys) {
              tableChangeObj[key]?.call(onlineFormContext, onlineFormContext, {...event, row: event.deleteRows});
            }
          }
        }
      }

      // Sự kiện được kích hoạt sau khi chỉnh sửa hàng và thêm mới hoàn tất
      function handleAdded(sub, event) {
        console.log('handleAdded', sub, event)
        //update-begin-author:taoyan date:2022-6-26 for: Cảnh báo console Tại đây gọi hàm trực tiếp không kích hoạt sự kiện nữa
       // event.target.emit('executeFillRule', event);
        if(!event.isModalData){
          handleSubTableDefaultValue(sub, event)
        }
        //update-end-author:taoyan date:2022-6-26 for: Cảnh báo console Tại đây gọi hàm trực tiếp không kích hoạt sự kiện nữa
      }

      function getSubTableAuthPre(table) {
        return 'online_' + table + ':';
      }

      //Lắng nghe sự kiện thay đổi biểu mẫu
      async function onValuesChange(columnKey, value) {
        //console.log('columnKey-value', `${columnKey}-${value}`)
        if (!EnhanceJS || !EnhanceJS['onlChange']) {
          return false;
        }
        if (!columnKey) {
          return false;
        }
        //let tableChangeObj = EnhanceJS["onlChange"].call(onlineFormContext);
        let tableChangeObj = EnhanceJS['onlChange']();
        if (tableChangeObj[columnKey]) {
          // update-begin--author:liaozhiyang---date:20250106---for：【issues/7642】jsTăng cườngonlchangesự kiệnevent.row.Trường lấy giá trị trước khi thay đổi
          setTimeout(async () => {
            let formData = await getFieldsValue();
            let event = {
              row: formData,
              column: { key: columnKey },
              value: value,
            };
            tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
          }, 0);
          // update-end--author:liaozhiyang---date:20250106---for：【issues/7642】jsTăng cườngonlchangesự kiệnevent.row.Trường lấy giá trị trước khi thay đổi
        }
      }

      /**
       * Thực thionlinecủa biểu mẫu hook
       * QQYUN-5315【onlineBiểu mẫu 】onlineBiểu mẫu cũng cần hỗ trợhookTăng cường
       */
      function executeEnhanceFormJsHook(funStr) {
        let str = funStr.toLocaleString();
        let arr = str.match(GET_FUN_BODY_REG);
        if (arr.length > 1) {
          let temp = arr[1];
          executeJsEnhanced(temp);
        }
      }

      // Nút tùy chỉnh Sự kiện kích hoạt phần tăng cường
      function handleCgButtonClick(optType, buttonCode) {
        if ('js' == optType) {
          let hookFunName = buttonCode+"_hook";
          if (EnhanceJS && EnhanceJS[buttonCode]) {
            EnhanceJS[buttonCode].call(onlineFormContext, onlineFormContext);
          }else if(EnhanceJS && EnhanceJS[hookFunName]){
            executeEnhanceFormJsHook(EnhanceJS[hookFunName])
          }
        } else if ('action' == optType) {
          let formData = dbData.value;
          let params = {
            formId: props.id,
            buttonCode: buttonCode,
            dataId: formData.id,
            uiFormData: Object.assign({}, formData),
          };
          //console.log("Tham số yêu cầu backend của nút tùy chỉnh:",params)
          defHttp
            .post(
              {
                url: `${urlObject.urlButtonAction}`,
                params,
              },
              { isTransformResponse: false }
            )
            .then((res) => {
              if (res.success) {
                $message.success('Xử lý hoàn tất!');
                emit('success', formData);
              } else {
                $message.warning('Xử lý thất bại!');
              }
            });
        }
      }

      // --------------------Tăng cường-------------------------

      /**
       * Xóa bỏ dữ liệu bảng con
       * @param tbname
       */
      function clearSubRows(tbname) {
        let instance = getSubTableInstance(tbname)
        let rows = [...instance.getNewDataWithId(), ...subDataSource.value[tbname]];
        if (!rows || rows.length == 0) {
          return false;
        }
        let ids = [];
        for (let i of rows) {
          ids.push(i.id);
        }
        instance.removeRowsById(ids);
      }

      /**
       * Thêm dữ liệu bảng con
       * @param tbname
       * @param rows có thể là mảng, cũng có thể là đối tượng
       * @returns {boolean}
       */
      function addSubRows(tbname, rows) {
        if (!rows) {
          return false;
        }
        let instance = getSubTableInstance(tbname)
        if (typeof rows == 'object') {
          instance.addRows(rows, true);
        } else {
          this.$message.error('Thêm dữ liệu bảng con,Tham số không nhận diện được!');
        }
      }

      /**
       * Xóa trước rồi thêm sau
       * @param tbname
       * @param rows
       */
      function clearThenAddRows(tbname, rows) {
        clearSubRows(tbname);
        addSubRows(tbname, rows);
      }

      /**
       * Sửa tùy chọn dropdown của ô dropdown
       * @param field
       * @param options
       */
      function changeOptions(field, options) {
        if (!options && options.length <= 0) {
          options = [];
        }
        options.map((item) => {
          if (!item.hasOwnProperty('label')) {
            item['label'] = item.text;
          }
        });
        updateSchema({
          field,
          componentProps: {
            options,
          },
        });
      }
      /**
       * 2024-03-21
       * liaozhiyang
       * jstăng cường, thay đổi tìm kiếm dropdownoptions
       */
      function changeRemoteOptions({ field, dict, label, type, subTableName }) {
        const _dict = dict
          .split(',')
          .map((item) => encodeURIComponent(item))
          .join(',');
        if (type == 'subTable') {
          // một-nhiều
          const findSubTableInfo = subTabInfo.value.find((sub) => sub.key === subTableName);
          if (findSubTableInfo) {
            const findIndex = findSubTableInfo.columns.findIndex((item) => item.key === field);
            if (findIndex !== -1) {
              defHttp.get({
                url: `/sys/dict/loadDict/${_dict}`,
                params: { keyword: '',pageSize:1000 },
              }) .then((res) => {
                const data = dict.split(',');
                const result = { customOptions:true ,dictTable: data[0], dictCode: data[2], dictText: data[1],options:res };
                label && (result.title = label);
                findSubTableInfo.columns[findIndex] = { ...findSubTableInfo.columns[findIndex], ...result };
                window.findSubTableInfo=findSubTableInfo;
              });
            }
          }
        } else if (type == 'subForm') {
          // một-một
          if (refMap[subTableName]?.value?.[0]) {
            const result = {
              field,
              componentProps: {
                dict: _dict,
              },
            };
            label && (result.label = label);
            refMap[subTableName].value[0].updateSchema(result);
          }
        } else {
          // bảng đơn
          const result = {
            field,
            componentProps: {
              dict: _dict,
            },
          };
          label && (result.label = label);
          updateSchema(result);
        }
      }
      // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
      /**
       * 2024-03-13
       * liaozhiyang
       * Thay đổi của một trường nào đó trong bảng con một-nhiềuoptionsTùy chọn (khi đó là để cung cấp chojsphần tăng cường sử dụng)
       */
      function changeSubTableOptions(subTableName, field, options) {
        const findSubTableInfo = subTabInfo.value.find((sub) => sub.key === subTableName);
        if (findSubTableInfo) {
          if (!options && options.length <= 0) {
            options = [];
          }
          options.map((item) => {
            if (!item.hasOwnProperty('label')) {
              item['label'] = item.text;
            }
          });
          const findIndex = findSubTableInfo.columns.findIndex((item) => item.key === field);
          if (findIndex !== -1) {
            findSubTableInfo.columns[findIndex] = { ...findSubTableInfo.columns[findIndex], ...{ options: options, dictCode: '' } };
          }
        }
      }

      /**
       * 2024-03-13
       * liaozhiyang
       * Thay đổi của một trường nào đó trong bảng con một-mộtoptionsTùy chọn (khi đó là để cung cấp chojsphần tăng cường sử dụng)
       */
      function changeSubFormbleOptions(subTableName, field, options) {
        if (refMap[subTableName]?.value?.[0]) {
          if (!options && options.length <= 0) {
            options = [];
          }
          options.map((item) => {
            if (!item.hasOwnProperty('label')) {
              item['label'] = item.text;
            }
          });
          refMap[subTableName].value[0].updateSchema({
            field,
            componentProps: {
              dictCode: '',
              options,
            },
          });
        }
      }
      // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
      /**
       * Sự kiện trước khi gửi biểu mẫu
       * @param that
       * @param formData
       * @returns {Promise<void>|*}
       */
      function customBeforeSubmit(that, formData) {
        if (EnhanceJS && EnhanceJS['beforeSubmit']) {
          return EnhanceJS['beforeSubmit'](that, formData);
        } else {
          return Promise.resolve();
        }
      }

      /**
       * Xử lý popup tùy chỉnh Việc hiển thị/ẩn của trường
       * @param show
       * @param hide
       */
      function handleCustomFormSh(show, hide) {
        let plain = toRaw(fieldDisplayStatus);
        // update-begin--author:liaozhiyang---date:20241101---for：【issues/7387】jsPhần tăng cường sử dụngopenCustomModalKhi vượt quá haishowHiển thị lỗi
        Object.keys(plain).map((k) => {
          if (k.endsWith('_load') || k.endsWith('_disabled')) {
            // Không xử lý (không phải trường);
          } else {
            fieldDisplayStatus[k] = true;
          }
        });
        // update-end--author:liaozhiyang---date:20241101---for：【issues/7387】jsPhần tăng cường sử dụngopenCustomModalKhi vượt quá haishowHiển thị lỗi
        if (show && show.length > 0) {
          Object.keys(plain).map((k) => {
            if (!k.endsWith('_load') && show.indexOf(k) < 0) {
              fieldDisplayStatus[k] = false;
            }
          });
        } else if (hide && hide.length > 0) {
          Object.keys(plain).map((k) => {
            if (hide.indexOf(k) >= 0) {
              fieldDisplayStatus[k] = false;
            }
          });
        }
      }

      async function handleCustomFormEdit(record, editSubmitUrl) {
        console.log('Mở popup tùy chỉnhonlineBiểu mẫu2》》form', record);
        customEditSubmitUrl.value = editSubmitUrl;
        await resetFields();
        dbData.value = '';
        isUpdate.value = true;
        // Chỉnh sửa dữ liệu
        await edit(record);
        await nextTick(() => {
          // Tất cả thông tin đã tải xong Kích hoạtloadedsự kiện
          handleCgButtonClick('js', 'loaded');
        });
      }

      /**
       * VUEN-1036
       * Lấy đối tượng instance của bảng con Có thể gọi trực tiếp phương thức của bảng con
       */
      function getSubTableInstance(tableName) {
        let instance = refMap[tableName].value;
        if (instance instanceof Array) {
          instance = instance[0];
        }
        if(!instance){
          $message.warning('bảng conrefKhông tìm thấy:'+tableName);
          return;
        }
        return instance;
      }

      //Chuyển tới trang báo cáo Jimu
      function onOpenReportPrint(){
        let url = onlineExtConfigJson.reportPrintUrl;
        let id = dbData.value.id;
        let token = getToken();
        goJmReportViewPage(url, id, token)
      }
      //Component in biểu mẫu
      function onCompPrint(){
        //In component
        setTimeout(() => {
          if (!printRef.value) return;

          const iframe = document.createElement('iframe');
          iframe.style.display = 'none';
          document.body.appendChild(iframe);

          const iframeDoc = iframe.contentDocument || iframe.contentWindow?.document;
          if (!iframeDoc) return;

          // Sao chép kiểu dáng và nội dung
          iframeDoc.open();
          iframeDoc.write(`
          <!DOCTYPE html>
          <html>
            <head>
              <title>In biểu mẫu</title>
              ${Array.from(document.querySelectorAll('style, link[rel="stylesheet"]'))
              .map((tag) => tag.outerHTML)
              .join('')}
            </head>
            <body>
              ${printRef.value.outerHTML}
            </body>
          </html>
        `);
          // Thêm kiểu dáng in
          const scrollHeight = printRef.value.scrollHeight;
          const style = iframeDoc.createElement('style');
          style.innerHTML = `
            body {
              margin: 0;
              padding: 15px;
              font-family: Arial, sans-serif;
            }
            img {
              max-width: 100%;
              height: auto;
            }
            @page {
              size: auto;
              margin: 15mm;
            }
            @media print {
              body {
                padding: 0;
                height: ${scrollHeight}px;
              }
            }
          `;
          iframeDoc.head.appendChild(style);
          iframeDoc.close();

          // Loại bỏ sau khi in iframe
          iframe.contentWindow?.addEventListener('afterprint', () => {
            document.body.removeChild(iframe);
          });

          // Kích hoạt in
          setTimeout(() => {
            iframe.contentWindow?.print();
          }, 200);
        }, 10);
      }

      //-----------------------------------thao tác dữ liệu trong popup của bảng con một-nhiều-begin-------------------------------------------------------------
      const [registerPopModal, { openModal: openPopModal }] = useModal();
      // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
      // BảngID
      const popTableId = ref('');
      // Tên bảng
      const popTableName = ref('');
      // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
      // Có khởi tạo yêu cầu hay không
      const popModalRequest = ref(true);

      // update-begin--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường
      // cha→Tiêm vào phía con: bảng chính context + enhanceJs(dùng getter Lấy giá trị mới nhất, vì EnhanceJS tại createRootProperties sau khi vào trong thì gán giá trị)+ Bảng con đang mở hiện tại key
      provide('parentOnlineForm', {
        context: onlineFormContext,
        getEnhanceJs: () => EnhanceJS,
        subTableKey: popTableName,
      });
      // update-end--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường

      /**
       * Thêm mới
       * @param sub
       */
      function openSubFormModalForAdd(sub){
        console.log('openSubFormModalForAdd', sub)
        // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        popTableId.value = sub.id;
        popTableName.value = sub.key;
        // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        popModalRequest.value = false;
        openPopModal(true,{isUpdate: false, tableType: '3'})
      }

      /**
       * Chỉnh sửa
       * @param sub
       */
      function openSubFormModalForEdit(sub){
        let ref = getSubTableInstance(sub.key)
        let arr = ref.getSelectedData();
        if(arr.length!=1){
          $message.warning('Vui lòng chọn một bản ghi dữ liệu');
          return;
        }
        // -update-begin--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        popTableId.value = sub.id;
        //update-begin-author:liusq---date:2024-08-28--for: [TV360X-2262]onlineBiểu mẫu kết nối quyền quy trình, thiết lập trường không thể chỉnh sửa, nhưng hai nút này có thể sửa đổi
        popTableName.value = sub.key;
        //update-end-author:liusq---date:2024-08-28--for: [TV360X-2262]onlineBiểu mẫu kết nối quyền quy trình, thiết lập trường không thể chỉnh sửa, nhưng hai nút này có thể sửa đổi
        // -update-end--author:liaozhiyang---date:20240613---for：【TV360X-1000】Quy trình một-nhiều đi qua API của quy trình
        popModalRequest.value = false;
        openPopModal(true,{isUpdate:true, record: arr[0]})
      }

      /**
       * Điền ngược dữ liệu vào mục hiện tạivxe table
       * @param data
       */
      function getPopFormData(data){
        const tableName = data[ONL_FORM_TABLE_NAME];
     //   console.log('getFormData', data)
        let record = omit(data, [ONL_FORM_TABLE_NAME]);
        if(record.id){
          let values = omit({...record}, 'id');
          let arr = [{ rowKey: record.id, values }];
          let instance = getSubTableInstance(tableName)
          instance.setValues(arr);
        }else{
          let instance = getSubTableInstance(tableName)
          instance.addRows(record, {isOnlineJS: false, setActive:false, emitChange: true, isModalData: true});
        }
      }
      //-----------------------------------thao tác dữ liệu trong popup của bảng con một-nhiều-end-------------------------------------------------------------

      //update-begin-author:taoyan date:2022-10-17 for: VUEN-2480【Nghiêm trọngbug】online vue3Vấn đề của kiểm thử 6、sau khi chỉnh sửa popup đã chọn, mở lại thì thấy không thể bỏ chọn
      function onCloseModal(){
        // update-begin--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
        // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
        // Lối vào chia sẻ ERP Tạo mới bảng chính-con chưa liên kết
        if (props.themeTemplate === ERP && !props.isShare) return;
        // update-end--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
        // update-end--author:liaozhiyang---date:20230818---for：【QQYUN-5803】onlinemột-nhiềuErpPhong cách
        let arr  = subTabInfo.value;
        if(arr && arr.length>0){
          for(let item of arr){
            if(item.relationType==1){
              //1Đối với1Bỏ qua
            }else{
              let inst = getSubTableInstance(item.key);
              if(inst){
                inst.clearSelection();
              }
            }
          }
        }
      }
      //update-end-author:taoyan date:2022-10-17 for: VUEN-2480【Nghiêm trọngbug】online vue3Vấn đề của kiểm thử 6、sau khi chỉnh sửa popup đã chọn, mở lại thì thấy không thể bỏ chọn

      /** cho js Lời gọi tăng cường: thực thi lại quy tắc điền giá trị của bảng chính  */
      function executeMainFillRule() {
        let formData = getFieldsValue();
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        loadFormFieldsDefVal(fieldProperties, (values) => {
          setFieldsValue(values);
        }, formData);
      }

      /** cho js Lời gọi tăng cường: thực thi lại quy tắc điền giá trị của bảng con  */
      function executeSubFillRule(subKey, $event){
        let subList = subTabInfo.value;
        if(subList && subList.length>0){
          let arr = subList.filter(sub=>sub.key === subKey);
          if(arr.length==0){
            console.error('Không tìm thấy bảng con khớp với nó', subKey)
            return;
          }
          if(arr[0].relationType == 1){
            //một-một
            let subInstance = getSubTableInstance(subKey);
            subInstance.executeFillRule();
          }else{
            // một-nhiều
            let subFieldProperties = toRaw(defaultValueFields[subKey]);
            let row = toRaw($event.row)
            loadFormFieldsDefVal(subFieldProperties,(values) => {
              const { row, target } = $event;
              let v = [{ rowKey: row.id, values: { ...values } }];
              target.setValues(v);
            }, row);
          }
        }


      }

      let context = {
        tableName,
        loading,
        subActiveKey,
        onlineFormRef,
        getFieldsValue,
        setFieldsValue,
        submitFlowFlag,
        subFormHeight,
        subTableHeight,
        refMap,
        triggleChangeValues,
        triggleChangeValue,
        sh: fieldDisplayStatus,
        clearSubRows,
        addSubRows,
        clearThenAddRows,
        changeOptions,
        isUpdate,
        getSubTableInstance,
        updateSchema,
        executeMainFillRule,
        executeSubFillRule,
        // update-begin--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
        changeSubTableOptions,
        changeSubFormbleOptions,
        // update-end--author:liaozhiyang---date:20240313---for：【QQYUN-8350】jsPhần tăng cường giới hạn bảng con theo bảng chínhoptions
        changeRemoteOptions,
        changEvent: ()=>{},
        onlineFormValueChange,
        // update-begin--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
        submitFormAndFlow,
        // update-end--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
      };
      resetContext(context);
      // -update-begin--author:liaozhiyang---date:20240614---for：【TV360X-1017】Các nút thêm, sửa của một-nhiều được kiểm soát theo quyền
      const getBtnAuth = (code, tableName) => {
        const prefix = getSubTableAuthPre(tableName);
        const permissionStore = usePermissionStore();
        let onlineButtonAuths = permissionStore.getOnlineSubTableAuth(prefix);
        if (onlineButtonAuths?.length) {
          const result = onlineButtonAuths.find((item) => item === code);
          return result ? false : true;
        }
        return true;
      };
      // -update-end--author:liaozhiyang---date:20240614---for：【TV360X-1017】Các nút thêm, sửa của một-nhiều được kiểm soát theo quyền

      return {
        //bảng chính
        tableName,
        onlineFormRef,
        registerForm,
        loading,

        //bảng con
        subActiveKey,
        hasSubTable,
        subTabInfo,
        refMap,

        //bảng con một-một
        subFormHeight,
        getSubTableForeignKeyValue,
        isUpdate,
        handleSubFormChange,

        //bảng con một-nhiều
        subTableHeight,
        onlineFormDisabled,
        subDataSource,
        getSubTableAuthPre,
        handleAdded,
        handleSubTableDefaultValue,
        handleValueChange,
        openSubFormModalForAdd,
        openSubFormModalForEdit,
        getBtnAuth,
        handleRemoved,
        //Lời gọi của component cha
        show,
        createRootProperties,
        handleSubmit,
        handleValidate,
        sh: fieldDisplayStatus,
        handleCgButtonClick,
        handleCustomFormSh,
        handleCustomFormEdit,
        //Chuyển hướng (nhảy trang)
        dbData,
        onOpenReportPrint,
        onlineExtConfigJson,
        onCompPrint,
        printRef,
        //thao tác dữ liệu trong popup của bảng con một-nhiều
        registerPopModal,
        popTableId,
        popTableName,
        getPopFormData,
        popModalRequest,
        onCloseModal,
        ERP,
        rowNumber,
        isSetFormLabelLength,
        subFormWrapRef,

        getSubAddBtnCfg,
        getSubRemoveBtnCfg,
        getSubOpenAddBtnCfg,
        getSubOpenEditBtnCfg,
      };
    },
  };
</script>

<style lang="less" scoped>
  .onlineFormWrap {
    // update-begin--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
    padding: 20px 1.5% 0;
    // update-begin--author:liaozhiyang---date:20240506---for：【QQYUN-9229】Điều chỉnh khoảng cách
    &.formTemplate_1 {
      > form {
        padding-left: 5%;
        padding-right: 5%;
      }
    }
    &.formTemplate_2 {
      > form {
        padding-left: 1%;
        padding-right: 1%;
      }
    }
    // update-end--author:liaozhiyang---date:20240506---for：【QQYUN-9229】Điều chỉnh khoảng cách
    :deep(.ant-form) {
      > .ant-row {
        > .ant-col {
          padding: 0 6px;
        }
      }
    }
    // update-end--author:liaozhiyang---date:20230105---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
    :deep(.vxe-buttons--wrapper) {
      > div {
        display: flex;
      }
    }
    .sub-popup-button-group {
      margin-right: 8px;

      :deep(.ant-btn) {
        margin-right: 0;
      }
    }
    // update-begin--author:liaozhiyang---date:20240517---for：【QQYUN-9353】markdownSau khi toàn màn hình, nút đóng và nút toàn màn hình của popup nằm ởmarkdownphía trên
    :deep(.vditor--fullscreen) {
      z-index: 1011 !important;
    }
    // update-end--author:liaozhiyang---date:20240517---for：【QQYUN-9353】markdownSau khi toàn màn hình, nút đóng và nút toàn màn hình của popup nằm ởmarkdownphía trên
  }
</style>
