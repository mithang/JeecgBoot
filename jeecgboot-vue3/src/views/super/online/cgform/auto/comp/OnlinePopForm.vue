<template>
  <div :id="tableName + '_form'" class="onlinePopFormWrap" :class="[`formTemplate_${formTemplate}`]">
    <BasicForm ref="onlineFormRef" @register="registerForm" />
  </div>
</template>

<script lang="ts">
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, ref, unref, nextTick, toRaw, reactive, inject } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SUBMIT_FLOW_KEY, VALIDATE_FAILED } from '../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick } from 'lodash-es';
  import { useFormItems, getRefPromise, useOnlineFormContext } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import OnlineSubForm from './OnlineSubForm.vue';
  import { loadFormFieldsDefVal } from '../../util/FieldDefVal';
  import { getToken } from '/@/utils/auth';
  import { goJmReportViewPage } from '/@/utils'
  import { PrinterOutlined, DiffOutlined, FormOutlined } from '@ant-design/icons-vue';
  import { useModal } from '/@/components/Modal';
  import { Method } from 'axios';
  import { isObject } from '/@/utils/is';

  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  export default {
    name: 'OnlinePopForm',
    components: {
      BasicForm,
      Loading,
      OnlineSubForm,
      PrinterOutlined,
      DiffOutlined,
      FormOutlined
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
      modalClass:{
        type: String,
        default: '',
      },
      //Có gửi yêu cầu hay không-tức là việc lưu của biểu mẫu/Yêu cầu chỉnh sửa,falsethì chỉ đưa dữ liệu biểu mẫu ra ngoài
      request: {
        type: Boolean,
        default: true
      },
      // Có phải làvxeTableDữ liệu biểu mẫu được mở bằng cách nhấp nút phía trên
      isVxeTableData: {
        type: Boolean,
        default: false
      }
    },
    emits: ['success', 'rendered', 'dataChange'],
    setup(props, { emit }) {
      console.log('onlineForm-setup》》');
      const { createMessage: $message } = useMessage();

      const [registerVxeFormModal, { openModal:openVxeFormModal }] = useModal();
      const vxeTableId = ref('');
      // Biểu mẫuref
      const onlineFormRef = ref(null);
      const single = ref(true);
      // Trạng thái tải
      const loading = ref(false);
      const tableType = ref(1);
      // Gửi biểu mẫu đồng thời gửi quy trình
      const submitFlowFlag = ref(false);
      const isUpdate = ref(false);

      /**
       * onlineCấu hình mở rộng biểu mẫu
       */
      const onlineExtConfigJson = reactive({
        reportPrintShow: 0,
        reportPrintUrl: '',
        joinQuery: 0,
        modelFullscreen: 0,
        modalMinWidth: '',
      });

      const { onlineFormContext, resetContext } = useOnlineFormContext();

      // update-begin--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường
      // Do cha OnlineForm Tiêm vào; trong tình huống sử dụng không phải popup (như popup riêng của bản ghi liên kết) là null, mọi logic thông suốt tự động no-op
      const parentOnlineForm: any = inject('parentOnlineForm', null);
      // update-end--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường

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

      //Cấu hình biểu mẫu
      const [registerForm, { setProps, validate, resetFields, setFieldsValue, updateSchema, getFieldsValue, scrollToField }] = useForm({
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
        console.log('onlinepopformThêm mới/chỉnh sửa đi vào biểu mẫu》》form', record);
        await resetFields();
        dbData.value = '';
        let flag = unref(status);
        isUpdate.value = flag;
        if (flag) {
          // Trang chỉnh sửa
          await edit(record);
        }
        await nextTick(() => {
          if (!flag && param) {
            //Nếu là trang thêm mới và paramTruyền vào có giá trị Cần thiết lập biểu mẫu
            setFieldsValue(param);
          }
          handleDefaultValue();
          // Tất cả thông tin đã tải xong Kích hoạtloadedsự kiện
          handleCgButtonClick('js', 'loaded');
          replayParentMainOnlChange();
          //Xử lý hiệu quả vô hiệu hóa của biểu mẫu
          handleFormDisabled();
        });
      }
     

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

      async function edit(record) {
        // Truy vấn cơ sở dữ liệu
        let formData:any = await getFormData(record.id);
        if(!formData || Object.keys(formData).length==0){
          //Không truy vấn ra dữ liệu
          formData = {...toRaw(record)}
        }
        dbData.value = Object.assign({}, formData);
        //Gán giá trị biểu mẫu
        let arr = realFormFieldNames.value;
        let values = pick(formData, ...arr);
        // Nếu làvxetableBiểu mẫu do nút phía trên mở ra, khi đó giá trị biểu mẫu lấy theorecordlàm chuẩn, chứ không phải dữ liệu truy vấn từ cơ sở dữ liệu,Nếu không, sau lần sửa đổi đầu tiên, mở biểu mẫu lần thứ hai, giá trị biểu mẫu khớp với cơ sở dữ liệu nhưng không khớp với trang hiện tại
        if(props.isVxeTableData === true){
          values = Object.assign({},values, record)
        }
        await setFieldsValue(values);
        // editSubVxeTableData(formData);
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
        let formRefObject:any = await getRefPromise(onlineFormRef);
        formRefObject.$formValueChange = (field, value, changeFormData) => {
          onValuesChange(field, value);
          if(changeFormData){
            //Nếu tồn tại dữ liệu của control biểu mẫu khác, hãy trực tiếp thiết lập giá trị đó
            setFieldsValue(changeFormData)
          }
        };
      }

      /**
       * Xử lý cấu hình mở rộng
       */
      function handleExtConfigJson(jsonStr) {
        let extConfigJson = { reportPrintShow: 0, reportPrintUrl: '', joinQuery: 0, modelFullscreen: 1, modalMinWidth: '', formLabelLength: null };
        if (jsonStr) {
          extConfigJson = JSON.parse(jsonStr);
        }
        Object.keys(extConfigJson).map((k) => {
          onlineExtConfigJson[k] = extConfigJson[k];
        });
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
        let arr = subTabInfo.value;
        for (let i = 0; i < arr.length; i++) {
          if (key == arr[i].key) {
            subActiveKey.value = i + '';
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
              let instance = refMap[key].value;
              // Cách viết tương thích:Nếu cái lấy được là kiểu mảng, thì lấy phần tử đầu tiên
              if (instance instanceof Array) {
                instance = instance[0];
              }
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
        } catch (error) {
          // update-begin--author:liaozhiyang---date:20240524---for：【TV360X-420】Khi mục không qua kiểm chứng của bản ghi liên kết nằm ngoài vùng hiển thị, nhấp lưu không có tác dụng gì
          if (isObject(error)) {
            const errorFields = error.errorFields;
            if (errorFields?.length && errorFields[0].errors) {
              $message.warning(errorFields[0].errors[0]);
              scrollToField(errorFields[0].name, { behavior: 'smooth', block: 'center' });
            }
          }
          console.log(error);
          // update-end--author:liaozhiyang---date:20240524---for：【TV360X-420】Khi mục không qua kiểm chứng của bản ghi liên kết nằm ngoài vùng hiển thị, nhấp lưu không có tác dụng gì
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


      function triggleChangeValues(values, id, target) {
        if (id && target) {
          if (target.setValues) {
            //bảng con một-một
            target.setValues(values);
          } else {
            //bảng con một-nhiều
            target.setValues([
              {
                rowKey: id,
                values: values,
              },
            ]);
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
      const subFormHeight = ref(300);
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
            let subRef = refMap[tableKey].value;
            if (subRef instanceof Array) {
              subRef = subRef[0];
            }
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
          if (tableChangeObj[event.column.key]) {
            tableChangeObj[event.column.key].call(onlineFormContext, onlineFormContext, event);
          }
        }
      }

      // Sự kiện được kích hoạt sau khi chỉnh sửa hàng và thêm mới hoàn tất
      function handleAdded(sub, event) {
        console.log('handleAdded', sub, event)
        //update-begin-author:taoyan date:2022-6-26 for: Cảnh báo console Tại đây gọi hàm trực tiếp không kích hoạt sự kiện nữa
       // event.target.emit('executeFillRule', event);
        //update-end-author:taoyan date:2022-6-26 for: Cảnh báo console Tại đây gọi hàm trực tiếp không kích hoạt sự kiện nữa
      }

      function getSubTableAuthPre(table) {
        return 'online_' + table + ':';
      }

      //Lắng nghe sự kiện thay đổi biểu mẫu
      async function onValuesChange(columnKey, value) {
        //console.log('columnKey-value', `${columnKey}-${value}`)
        // So sánh dữ liệu cũ và dữ liệu mới Nếu khác nhau Ném sự kiện thay đổi ra ngoài
        let oldFormData = dbData.value;
        if(oldFormData[columnKey]!=value){
          emit('dataChange', columnKey);
        }

        if (!columnKey) {
          return false;
        }

        // 1) Bản thân popup (bảng con head) của enhanceJs.onlChange —— Giữ nguyên hành vi vốn có
        if (EnhanceJS && EnhanceJS['onlChange']) {
          //let tableChangeObj = EnhanceJS["onlChange"].call(onlineFormContext);
          let tableChangeObj = EnhanceJS['onlChange']();
          if (tableChangeObj[columnKey]) {
            let formData = await getFieldsValue();
            let event = {
              row: formData,
              column: { key: columnKey },
              value: value,
            };
            tableChangeObj[columnKey].call(onlineFormContext, onlineFormContext, event);
          }
        }

        await triggerParentSubOnlChange(columnKey, value);
      }

      // update-begin--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường
      // Component cha dùng provide Tiêm vào (inject) onlineFormContext + enhanceJs + Bảng con hiện tại key, popup inject
      // sau đó làm hai việc:
      //   ① Thay đổi trường trong popup   → Gọi bảng chính 「tên bảng con_onlChange[trường]」(và VxeTable Chỉnh sửa trong hàng tương đương)
      //   ② Mở popup loaded → Phát lại bảng chính 「onlChange[Trường đã chọn]」(để"Phân loại bảng chính→Bảng con hiện tại options"Liên động có hiệu lực ngay lập tức)
      // cả hai đường dẫn đều dùng wrapParentCtxForPop Đóng gói this: viết"Trường của hàng hiện tại"của API Tự động rơi vào popup, tránh ô nhiễm bảng chính.
      function wrapParentCtxForPop(parentCtx, currentSubKey) {
        const popOverrides: Record<string, Function> = {
          triggleChangeValues: (values, rowId?, vxeTarget?) => {
            if (vxeTarget === undefined) return setFieldsValue(values);
            return parentCtx.triggleChangeValues?.(values, rowId, vxeTarget);
          },
          changeSubTableOptions: (tableName, field, options) => {
            if (tableName === currentSubKey) return changeOptions(field, options);
            return parentCtx.changeSubTableOptions?.(tableName, field, options);
          },
          changeRemoteOptions: (param) => {
            if (param?.subTableName !== currentSubKey) {
              return parentCtx.changeRemoteOptions?.(param);
            }
            // Tương đương với bảng cha"bảng đơn/nhánh bảng chính": dùng popup này updateSchema sửa dropdown từ xa dict
            const _dict = String(param.dict || '').split(',').map(encodeURIComponent).join(',');
            const schemaPatch: any = { field: param.field, componentProps: { dict: _dict } };
            if (param.label) schemaPatch.label = param.label;
            updateSchema(schemaPatch);
          },
        };

        return new Proxy(parentCtx, {
          get: (target, prop: string) =>
            prop in popOverrides ? popOverrides[prop] : Reflect.get(target, prop),
        });
      }
      /** Khi trường trong popup thay đổi, gọi của bảng chính「tên bảng con_onlChange[trường]」—— và VxeTable Chỉnh sửa trong hàng tương đương */
      async function triggerParentSubOnlChange(columnKey, value) {
        const subKey = unref(parentOnlineForm?.subTableKey);
        if (!subKey) return;

        const subOnlChange = parentOnlineForm.getEnhanceJs?.()?.[`${subKey}_onlChange`];
        if (typeof subOnlChange !== 'function') return;

        const wrappedCtx = wrapParentCtxForPop(parentOnlineForm.context, subKey);
        const handler = subOnlChange(wrappedCtx)?.[columnKey];
        if (typeof handler !== 'function') return;

        const event = {
          row: await getFieldsValue(),
          column: { key: columnKey },
          value,
          target: undefined,   // Đánh dấu chế độ popup: không VxeTable Thực thể (instance)
          type: undefined,
        };
        try {
          handler.call(wrappedCtx, wrappedCtx, event);
        } catch (e) {
          console.error(`[OnlinePopForm] Gọi bảng chính ${subKey}_onlChange.${columnKey} Thất bại:`, e);
        }
      }

      /**
       * Khi popup mở, phát lại một lần「của bảng chínhonlChange[Trường đã chọn của bảng chính]」。
       */
      function replayParentMainOnlChange() {
        const subKey = unref(parentOnlineForm?.subTableKey);
        if (!subKey) return;

        const mainOnlChange = parentOnlineForm.getEnhanceJs?.()?.['onlChange'];
        if (typeof mainOnlChange !== 'function') return;

        let handlers;
        try {
          handlers = mainOnlChange();
        } catch (e) {
          console.error('[OnlinePopForm] Phân tích bảng chính onlChange Thất bại:', e);
          return;
        }
        if (!handlers || typeof handlers !== 'object') return;

        const parentCtx = parentOnlineForm.context;
        const wrappedCtx = wrapParentCtxForPop(parentCtx, subKey);
        const mainValues = parentCtx?.getFieldsValue?.() || {};

        for (const [field, handler] of Object.entries(handlers)) {
          const val = mainValues[field];
          // Trường này của bảng chính không có giá trị thì không phát lại (tránh kích hoạt rỗng, đồng thời bỏ qua các trường chưa điền ở chế độ thêm mới)
          if (val === undefined || val === null || val === '') continue;
          if (typeof handler !== 'function') continue;

          const event = { row: mainValues, column: { key: field }, value: val, target: undefined, type: undefined };
          try {
            (handler as Function).call(wrappedCtx, wrappedCtx, event);
          } catch (e) {
            console.error(`[OnlinePopForm] Phát lại bảng chính onlChange.${field} Thất bại:`, e);
          }
        }
      }
      // update-end--author:liaozhiyang---date:20260519---for：【QQYUN-9773】onlineThêm mới trong popup nội bộ của một-nhiềujsTăng cường

      // Nút tùy chỉnh Sự kiện kích hoạt phần tăng cường
      function handleCgButtonClick(optType, buttonCode) {
        if ('js' == optType) {
          if (EnhanceJS && EnhanceJS[buttonCode]) {
            EnhanceJS[buttonCode].call(onlineFormContext, onlineFormContext);
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
        let instance = refMap[tbname].value;
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
        let instance = refMap[tbname].value;
        if (typeof rows == 'object') {
          instance.addRows(rows, true);
        } else {
          $message.error('Thêm dữ liệu bảng con,Tham số không nhận diện được!');
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

      async function handleCustomFormEdit(record) {
        console.log('Mở popup tùy chỉnhonlineBiểu mẫu 》》form', record);
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
        return instance;
      }
      
      //Chuyển tới trang báo cáo Jimu
      function onOpenReportPrint(){
        let url = onlineExtConfigJson.reportPrintUrl;
        let id = dbData.value.id;
        let token = getToken();
        goJmReportViewPage(url, id, token)
      }
      
      //----
      function openSubFormModalForAdd(sub){
        console.log(sub)
        vxeTableId.value = sub.id;
        openVxeFormModal(true, )
      }

      function openSubFormModalForEdit(sub){
        console.log(sub)
      }

      /**
       * Lưu dữ liệu
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
        console.log('GửipopDữ liệu biểu mẫu 》》》form:', formData);
        if(props.request == false){
          emit('success', formData);
        }else{
          let url = `${urlObject.optPre}${props.id}?tabletype=${tableType.value}`;

          console.log('GửipopBiểu mẫuurl》》》url:', url);
          // Nếu cần gửi quy trình Cần thiết lập thêm một tham số
          if (submitFlowFlag.value === true) {
            formData[SUBMIT_FLOW_KEY] = 1;
          }
          let method:Method = isUpdate.value === true ? 'put' : 'post';
          defHttp.request({ url, method, params: formData }, { isTransformResponse: false })
            .then((res) => {
              //console.log('Gửi biểu mẫu hoàn tất', res)
              if (res.success) {
                if (res.result) {
                  //formData[SUBMIT_FLOW_ID] = res.result;
                  if(!formData.id){
                    formData['id'] = res.result;
                  }
                }
                //Làm mới danh sách
                emit('success', formData);
                dbData.value = formData;
                isUpdate.value = true;
                $message.success('Thao tác thành công!')
                // Biểu mẫu gửi trong đơn yêu cầu công việc cũng đi theo logic này, lưu thành công không cần thông tin nhắc nhở
              } else {
                $message.warning(res.message);
              }
            })
            .finally(() => {
              loading.value = false;
            });
        }
      }


      /**
       * Khôi phục dữ liệu về dbdata
       */
      async function recoverFormData(){
        let record = dbData.value;
        let arr = realFormFieldNames.value;
        let values = pick(record, ...arr);
        if(record){
          await setFieldsValue(values);
        }else{
          let temp:any = {}
          for(let key of arr){
            temp[key] = ''
          }
          await setFieldsValue(temp);
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
      };
      resetContext(context);

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
        handleValueChange,
        openSubFormModalForAdd,
        openSubFormModalForEdit,
        registerVxeFormModal,
        vxeTableId,

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
        recoverFormData
      };
    },
  };
</script>

<style lang="less" scoped>
  .onlinePopFormWrap {
    // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
    padding: 0 1.5%;
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
    // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
  }
</style>
