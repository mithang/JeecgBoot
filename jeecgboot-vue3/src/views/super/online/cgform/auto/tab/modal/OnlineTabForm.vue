<template>
  <div :id="tableName + '_form'" class="onlineFormWrap" :class="[`formTemplate_${formTemplate}`]">
    <!-- update-begin--author:liaozhiyang---date:20240401---for：【QQYUN-8721】Trang chỉnh sửa không hiển thị in (đã cấu hình báo cáo Jimu) -->
    <!-- Nút in của báo cáo Jimu, chỉ khi đã cấu hình reportUrl mới hiển thị -->
    <!-- <div
      v-if="!!dbData.id && !!onlineExtConfigJson.reportPrintShow"
      style="text-align: right; position: absolute; top: 6px; right: 20px; z-index: 999"
    >
      <PrinterOutlined title="In" @click="onOpenReportPrint" style="font-size: 16px" />
    </div> -->
    <!-- update-end--author:liaozhiyang---date:20240401---for：【QQYUN-8721】Trang chỉnh sửa không hiển thị in (đã cấu hình báo cáo Jimu) -->

    <a-tabs class="tabTheme" v-model:activeKey="subActiveKey">
      <a-tab-pane tab="bảng chính" :key="'-1'">
        <BasicForm ref="onlineFormRef" @register="registerForm" />
      </a-tab-pane>
      <template v-if="hasSubTable">
        <a-tab-pane v-for="(sub, index) in subTabInfo" :tab="sub.describe" :key="index + ''" :forceRender="true">
          <div :style="{ 'overflow-y': 'auto', 'overflow-x': 'hidden', 'max-height': subFormHeight + 'px' }" v-if="sub.relationType == 1">
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
              @executeFillRule="handleSubTableDefaultValue(sub, $event)"
            >
              <template #toolbarAfterAdd="{ selectedRowIds }">
                <a-button-group class="sub-popup-button-group">
                  <!-- Nút thêm mới hiển thị dạng popup trong bảng con -->
                  <a-button
                      v-if="!onlineFormDisabled && getSubOpenAddBtnCfg.enabled && getBtnAuth('popup_add', sub.key)"
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
      </template>
    </a-tabs>
    <Loading :loading="loading" :absolute="false" />
    <!-- Trong biểu mẫu có thể thông quaslotNút gửi tùy chỉnh, dùng trong biểu mẫu quy trình -->
    <slot name="bottom"></slot>

    <!-- Popup dùng cho một biểu mẫu khác-Biểu mẫu có thể chỉnh sửa -->
    <online-pop-modal
      formTableType="3"
      :request="popModalRequest"
      :id="popTableName"
      @register="registerPopModal"
      @success="getPopFormData"
      topTip
      isVxeTableData
    />
  </div>
</template>

<script>
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, ref, unref, watch, nextTick, toRaw, reactive, inject } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SUBMIT_FLOW_ID, SUBMIT_FLOW_KEY, VALIDATE_FAILED, ONL_FORM_TABLE_NAME } from '../../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick, omit } from 'lodash-es';
  import { sleep } from '/@/utils';
  import { useFormItems, getRefPromise, useOnlineFormContext } from '../../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { useEnhance } from '../../../hooks/auto/useEnhance';
  import OnlineSubForm from '../../comp/OnlineSubForm.vue';
  import { loadFormFieldsDefVal } from '../../../util/FieldDefVal';
  import { getToken } from '/@/utils/auth';
  import { goJmReportViewPage } from '/@/utils';
  import { PrinterOutlined } from '@ant-design/icons-vue';
  import OnlinePopModal from '../../comp/OnlinePopModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useCustomHook, GET_FUN_BODY_REG } from '../../../hooks/auto/useCustomHook';
  import { useAppInject } from '/@/hooks/web/useAppInject';
  import { usePermissionStore } from '/@/store/modules/permission';

  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  export default {
    name: 'OnlineTabForm',
    components: {
      BasicForm,
      Loading,
      OnlineSubForm,
      PrinterOutlined,
      OnlinePopModal,
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
      submitTip: {
        type: Boolean,
        default: true,
      },
      modalClass: {
        type: String,
        default: '',
      },
      themeTemplate: {
        type: String,
        default: '',
      },
      tabIndex: {
        type: String,
        default: '',
      },
      cgBIBtnMap: Object,
      buttonSwitch: Object,
    },
    emits: ['success', 'rendered', 'toggleTab'],
    setup(props, { emit }) {
      console.log('onlineForm-setup》》');
      const { createMessage: $message } = useMessage();

      // Biểu mẫuref
      const onlineFormRef = ref(null);
      const single = ref(true);
      // Trạng thái tải
      const loading = ref(false);
      const tableType = ref(1);
      // Địa chỉ gửi chỉnh sửa của popup tùy chỉnh
      const customEditSubmitUrl = ref('');

      // Gửi biểu mẫu đồng thời gửi quy trình
      const submitFlowFlag = ref(false);
      const isUpdate = ref(false);
      const { getIsMobile } = useAppInject();
      const rowNumber = ref(getIsMobile.value ? false : true);

      /**
       * onlineCấu hình mở rộng biểu mẫu
       */
      const onlineExtConfigJson = reactive({
        reportPrintShow: 0,
        reportPrintUrl: '',
        joinQuery: 0,
        modelFullscreen: 0,
        modalMinWidth: '',
        commentStatus: 0,
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
        fieldDisplayStatus,
        labelCol,
        wrapperCol,
        labelWidth
      } = useFormItems(props, onlineFormRef);
      let { EnhanceJS, initCgEnhanceJs } = useEnhance(onlineFormContext, false);

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
          }
          handleDefaultValue();
          // Tất cả thông tin đã tải xong Kích hoạtloadedsự kiện
          handleCgButtonClick('js', 'loaded');
          //Xử lý hiệu quả vô hiệu hóa của biểu mẫu
          handleFormDisabled();
        });
      }
      //update-begin-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống
      /**
       * Nếu là cây Cần thiết lập lạipidcủaformSchema--dictThông tin Cho nó làm mới dữ liệu của control cây
       */
      async function updatePidFieldDict() {
        if (props.isTree === true) {
          let pidFieldName = props.pidField;
          let arr = formSchemas.value;
          if (arr && arr.length > 0) {
            //Trước tiên lấy đượcpidcủa componentPropsCấu hình
            let temp = arr.filter((item) => item.field === pidFieldName);
            if (temp.length > 0) {
              await updateSchema({
                field: pidFieldName,
                componentProps: {
                  reload: new Date().getTime(),
                  // update-begin--author:liaozhiyang---date:20240529---for：【TV360X-87】Khi chỉnh sửa bảng cây, không được chọn chính nó và các nút con cháu làm nút cha
                  hiddenNodeKey: '',
                  // update-end--author:liaozhiyang---date:20240529---for：【TV360X-87】Khi chỉnh sửa bảng cây, không được chọn chính nó và các nút con cháu làm nút cha
                },
              });
            }
          } else {
            console.log('Không lấy được thông tin cấu hình biểu mẫu, có thể là lần đầu mở trang thêm mới');
          }
        }
      }
      //update-end-author:taoyan date:2022-5-17 for: VUEN-1056 15、nghiêm trọng——onlineBiểu mẫu cây, khi thêm mới, nút cha đang trống

      /**
       * Logic giá trị mặc định của biểu mẫu hiện tại-Kích hoạt khi vào trang thêm mới
       */
      function handleDefaultValue() {
        if (unref(isUpdate) === false) {
          let fieldProperties = toRaw(defaultValueFields[tableName.value]);
          loadFormFieldsDefVal(fieldProperties, (values) => {
            setFieldsValue(values);
          });
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
        if (props.disabled) {
          //Trang chi tiết Loại bỏ dữ liệu trống
          Object.keys(values).map((k) => {
            if (!values[k] && values[k] !== 0 && values[k] !== '0') {
              delete values[k];
            }
          });
        }
        await setFieldsValue(values);
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
          if (changeFormData) {
            //Nếu tồn tại dữ liệu của control biểu mẫu khác, hãy trực tiếp thiết lập giá trị đó
            setFieldsValue(changeFormData);
          }
          onChangeEvent(field, value, changeFormData);
        };
        // QQYUN-5315【onlineBiểu mẫu 】onlineBiểu mẫu cũng cần hỗ trợhookTăng cường
        if (EnhanceJS && EnhanceJS['setup']) {
          executeEnhanceFormJsHook(EnhanceJS['setup']);
        }
      }

      //  Kích hoạt biểu mẫuchangesự kiện khác với cái cũonlchange
      function onChangeEvent(field, value, changeFormData) {
        onlineFormContext.changEvent(field, value, changeFormData);
      }

      // Thêm biểu mẫuchangesự kiện
      function onlineFormValueChange(callback) {
        onlineFormContext.addObject2Context('changEvent', callback);
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
        }
        Object.keys(extConfigJson).map((k) => {
          onlineExtConfigJson[k] = extConfigJson[k];
        });
      }

      // Gửi biểu mẫu và quy trình
      function submitFormAndFlow() {
        submitFlowFlag.value = true;
        handleSubmit();
      }

      function handleSubmit() {
        if (single.value === true) {
          handleSingleSubmit();
        } else {
          handleOne2ManySubmit();
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
            ({ errorFields }) => {
              reject({
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
            return validateSubTableFields();
          })
          .then((allTableData) => {
            Object.assign(temp, allTableData);
            return Promise.resolve(temp);
          })
          .catch((e) => {
            if (e === VALIDATE_FAILED || e?.code === VALIDATE_FAILED) {
              $message.warning('Kiểm chứng chưa thông qua');
              if (e.key) {
                changeTab(e.key);
                if (e.scrollToField) {
                  setTimeout(() => e.scrollToField(), 150)
                }
              }
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
        // update-begin-author:liaozhiyang---date:20240523---for：【TV360X-126】Bảng chính - bảng con-tabPhong cách, khi tạo mới (chỉnh sửa), kiểm chứng bảng con chưa thông qua, nội dung đã chuyển sang bảng con nhưngtabChưa chuyển đổi
        if (key === tableName.value) {
          emit('toggleTab', '-1');
          return;
        }
        // update-end-author:liaozhiyang---date:20240523---for：【TV360X-126】Bảng chính - bảng con-tabPhong cách, khi tạo mới (chỉnh sửa), kiểm chứng bảng con chưa thông qua, nội dung đã chuyển sang bảng con nhưngtabChưa chuyển đổi
        let arr = subTabInfo.value;
        for (let i = 0; i < arr.length; i++) {
          if (key == arr[i].key) {
            // update-begin-author:sunjianlei date:2022-7-7 for:【VUEN-1425】【vue3】Trong bảng con đối-nhiều, kiểm chứng không qua mà không nhắc
            let activeKey = i + '';
            if (subActiveKey.value === activeKey) {
              break;
            }
            // update-begin-author:liaozhiyang---date:20240523---for：【TV360X-126】Bảng chính - bảng con-tabPhong cách, khi tạo mới (chỉnh sửa), kiểm chứng bảng con chưa thông qua, nội dung đã chuyển sang bảng con nhưngtabChưa chuyển đổi
            // subActiveKey.value = activeKey;
            emit('toggleTab', activeKey);
            // update-end-author:liaozhiyang---date:20240523---for：【TV360X-126】Bảng chính - bảng con-tabPhong cách, khi tạo mới (chỉnh sửa), kiểm chứng bảng con chưa thông qua, nội dung đã chuyển sang bảng con nhưngtabChưa chuyển đổi
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
          }
        } finally {
          loading.value = false;
        }
      }

      //Trước khi gửi dữ liệu Trước tiên chạy qua phần tùy chỉnh một lượtJSKiểm chứng
      function handleApplyRequest(formData) {
        customBeforeSubmit(context, formData)
          .then(() => {
            doApplyRequest(formData);
          })
          .catch((msg) => {
            $message.warning(msg);
          });
      }

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
              if (props.submitTip === true) {
                $message.success(res.message);
              }
            } else {
              $message.warning(res.message);
            }
          })
          .finally(() => {
            loading.value = false;
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
      const subFormHeight = ref('auto');
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
      watch(
        () => props.tabIndex,
        (value) => {
          subActiveKey.value = value;
        },
        {
          immediate: true,
        }
      );
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
            let subRef = getSubTableInstance(tableKey);
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
          if (event.column === 'all') {
            let keys = Object.keys(tableChangeObj);
            if (keys.length > 0) {
              for (let key of keys) {
                tableChangeObj[key].call(onlineFormContext, onlineFormContext, event);
              }
            }
          } else {
            let columnKey = event.column.key || event.col.key;
            if (tableChangeObj[columnKey]) {
              //event.row.id Thêm điều kiện này là vì: popformThêm dữ liệu vàovxetablekhi không cần kích hoạt sự kiện thay đổi
              if (event.row && event.row.id) {
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
        console.log('handleAdded', sub, event);
        //update-begin-author:taoyan date:2022-6-26 for: Cảnh báo console Tại đây gọi hàm trực tiếp không kích hoạt sự kiện nữa
        // event.target.emit('executeFillRule', event);
        if (!event.isModalData) {
          handleSubTableDefaultValue(sub, event);
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
          let hookFunName = buttonCode + '_hook';
          if (EnhanceJS && EnhanceJS[buttonCode]) {
            EnhanceJS[buttonCode].call(onlineFormContext, onlineFormContext);
          } else if (EnhanceJS && EnhanceJS[hookFunName]) {
            executeEnhanceFormJsHook(EnhanceJS[hookFunName]);
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
        let instance = getSubTableInstance(tbname);
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
        let instance = getSubTableInstance(tbname);
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
        if (!instance) {
          $message.warning('bảng conrefKhông tìm thấy:' + tableName);
          return;
        }
        return instance;
      }

      //Chuyển tới trang báo cáo Jimu
      function onOpenReportPrint() {
        let url = onlineExtConfigJson.reportPrintUrl;
        let id = dbData.value.id;
        let token = getToken();
        goJmReportViewPage(url, id, token);
      }

      //-----------------------------------thao tác dữ liệu trong popup của bảng con một-nhiều-begin-------------------------------------------------------------
      const [registerPopModal, { openModal: openPopModal }] = useModal();
      //Tên bảng/BảngIDĐều có thể
      const popTableName = ref('');
      // Có khởi tạo yêu cầu hay không
      const popModalRequest = ref(true);

      /**
       * Thêm mới
       * @param sub
       */
      function openSubFormModalForAdd(sub) {
        console.log('openSubFormModalForAdd', sub);
        popTableName.value = sub.id;
        popModalRequest.value = false;
        openPopModal(true, { isUpdate: false, tableType: '3' });
      }

      /**
       * Chỉnh sửa
       * @param sub
       */
      function openSubFormModalForEdit(sub) {
        let ref = getSubTableInstance(sub.key);
        let arr = ref.getSelectedData();
        if (arr.length != 1) {
          $message.warning('Vui lòng chọn một bản ghi dữ liệu');
          return;
        }
        popTableName.value = sub.id;
        popModalRequest.value = false;
        openPopModal(true, { isUpdate: true, record: arr[0] });
      }

      /**
       * Điền ngược dữ liệu vào mục hiện tạivxe table
       * @param data
       */
      function getPopFormData(data) {
        const tableName = data[ONL_FORM_TABLE_NAME];
        //   console.log('getFormData', data)
        let record = omit(data, [ONL_FORM_TABLE_NAME]);
        if (record.id) {
          let values = omit({ ...record }, 'id');
          let arr = [{ rowKey: record.id, values }];
          let instance = getSubTableInstance(tableName);
          instance.setValues(arr);
        } else {
          let instance = getSubTableInstance(tableName);
          instance.addRows(record, { isOnlineJS: false, setActive: false, emitChange: true, isModalData: true });
        }
      }
      //-----------------------------------thao tác dữ liệu trong popup của bảng con một-nhiều-end-------------------------------------------------------------

      //update-begin-author:taoyan date:2022-10-17 for: VUEN-2480【Nghiêm trọngbug】online vue3Vấn đề của kiểm thử 6、sau khi chỉnh sửa popup đã chọn, mở lại thì thấy không thể bỏ chọn
      function onCloseModal() {
        let arr = subTabInfo.value;
        if (arr && arr.length > 0) {
          for (let item of arr) {
            if (item.relationType == 1) {
              //1Đối với1Bỏ qua
            } else {
              let inst = getSubTableInstance(item.key);
              if (inst) {
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
        loadFormFieldsDefVal(
          fieldProperties,
          (values) => {
            setFieldsValue(values);
          },
          formData
        );
      }

      /** cho js Lời gọi tăng cường: thực thi lại quy tắc điền giá trị của bảng con  */
      function executeSubFillRule(subKey, $event) {
        let subList = subTabInfo.value;
        if (subList && subList.length > 0) {
          let arr = subList.filter((sub) => sub.key === subKey);
          if (arr.length == 0) {
            console.error('Không tìm thấy bảng con khớp với nó', subKey);
            return;
          }
          if (arr[0].relationType == 1) {
            //một-một
            let subInstance = getSubTableInstance(subKey);
            subInstance.executeFillRule();
          } else {
            // một-nhiều
            let subFieldProperties = toRaw(defaultValueFields[subKey]);
            let row = toRaw($event.row);
            loadFormFieldsDefVal(
              subFieldProperties,
              (values) => {
                const { row, target } = $event;
                let v = [{ rowKey: row.id, values: { ...values } }];
                target.setValues(v);
              },
              row
            );
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
        changEvent: () => {},
        onlineFormValueChange,
        // update-begin--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
        submitFormAndFlow,
        // update-end--author:liaozhiyang---date:20240705---for：【TV360X-1754】jsTăng cường-Gửi biểu mẫu đồng thời khởi tạo quy trình
      };
      resetContext(context);

      // update-begin--author:liusq---date:20260831---for: 【Kiểm soát quyền】TabPhong cách một-nhiều, nút popup của bảng con hiển thị theo ủy quyền
      const getBtnAuth = (code, tableName) => {
        const prefix = getSubTableAuthPre(tableName);
        const permissionStore = usePermissionStore();
        const onlineButtonAuths = permissionStore.getOnlineSubTableAuth(prefix);
        return !onlineButtonAuths?.includes(code);
      };
      // update-end--author:liusq---date:20260831---for: 【Kiểm soát quyền】TabPhong cách một-nhiều, nút popup của bảng con hiển thị theo ủy quyền

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
        handleRemoved,
        //Lời gọi của component cha
        show,
        createRootProperties,
        handleSubmit,
        sh: fieldDisplayStatus,
        handleCgButtonClick,
        handleCustomFormSh,
        handleCustomFormEdit,
        //Chuyển hướng (nhảy trang)
        dbData,
        onOpenReportPrint,
        onlineExtConfigJson,
        //thao tác dữ liệu trong popup của bảng con một-nhiều
        registerPopModal,
        popTableName,
        getPopFormData,
        popModalRequest,
        onCloseModal,
        rowNumber,

        getSubAddBtnCfg,
        getSubRemoveBtnCfg,
        getSubOpenAddBtnCfg,
        getSubOpenEditBtnCfg,
        getBtnAuth,
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
  .tabTheme {
    :deep(.ant-tabs-nav) {
      display: none;
    }
  }
</style>
