<template>
  <BasicForm ref="onlineFormRef" @register="registerForm" />
</template>

<script>
  import { useMessage } from '/@/hooks/web/useMessage';
  import { computed, defineComponent, ref, unref, watch, nextTick, toRaw } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { SUBMIT_FLOW_ID, SUBMIT_FLOW_KEY, VALIDATE_FAILED } from '../../types/onlineRender';
  import { defHttp } from '/@/utils/http/axios';
  import { pick } from 'lodash-es';
  import { useFormItems, getRefPromise } from '../../hooks/auto/useAutoForm';
  import { Loading } from '/@/components/Loading';
  import { loadFormFieldsDefVal } from '../../util/FieldDefVal';

  const urlObject = {
    optPre: '/online/cgform/api/form/',
    urlButtonAction: '/online/cgform/api/doButton',
  };
  const baseUrl = '/online/cgform/api/subform';
  export default {
    name: 'OnlineSubForm',
    components: {
      BasicForm,
      Loading,
    },
    props: {
      properties: {
        type: Object,
        required: true,
      },
      mainId: {
        type: String,
        default: '',
      },
      table: {
        type: String,
        default: '',
      },
      formTemplate: {
        type: Number,
        default: 1,
      },
      requiredFields: {
        type: Array,
        default: [],
      },
      isUpdate: {
        type: Boolean,
        default: false,
      },
      disabled: {
        type: Boolean,
        default: false,
      },
      id: {
        type: String,
        default: '',
      },
      // update-begin--author:liusq---date:20260731---for:【Ủy quyền phiếu công việcBPM】Bảng con một-một nhận dữ liệu do bảng chính truyền sang modalClass, nếu không dropdown/của component thuộc loại ngày popup sẽ gắn lên cùng tên ở tầng ngoài modal trên khiến việc nhấp không có phản hồi
      modalClass: {
        type: String,
        default: '',
      },
      // update-end--author:liusq---date:20260731---for:【Ủy quyền phiếu công việcBPM】Bảng con một-một nhận dữ liệu do bảng chính truyền sang modalClass, nếu không dropdown/của component thuộc loại ngày popup sẽ gắn lên cùng tên ở tầng ngoài modal trên khiến việc nhấp không có phản hồi
    },
    emits: ['formChange'],
    setup(props, { emit }) {
      console.log('VàoonlineTrang biểu mẫu bảng con》》》》' + props.table);

      // Biểu mẫuref
      const onlineFormRef = ref(null);
      // Biểu mẫu đã render xong hay chưa
      const formRendered = ref(false);
      const { createMessage: $message } = useMessage();
      const {
        formSchemas,
        defaultValueFields,
        changeDataIfArray2String,
        tableName,
        dbData,
        checkOnlyFieldValue,
        fieldDisplayStatus,
        createFormSchemas,
        baseColProps,
        labelCol,
        wrapperCol,
        labelWidth,
      } = useFormItems(props, onlineFormRef);
      //Cấu hình biểu mẫu
      const [registerForm, { setProps, validate, resetFields, setFieldsValue, getFieldsValue, updateSchema, scrollToField }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        baseColProps: baseColProps,
        // update-begin--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        labelWidth,
        // update-end--author:liaozhiyang---date:20240429---for：【QQYUN-7632】 labelLưới đổi thànhlabelwidthĐộ rộng cố định
        // update-begin--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
        labelCol,
        wrapperCol
        // update-end--author:liaozhiyang---date:20240105---for：【QQYUN-7499】Văn bản giàu kiểu nhiều cột、markdownThêm chức năng chiếm riêng một hàng
      });
      const getFormItem = () => {
        return new Promise((resolve, reject) => {
          defHttp.get({ url: `online/cgform/api/getFormItem/${props.id}` }, { isTransformResponse: false }).then((res) => {
            resolve(res.result);
          });
        });
      }
      let extConfigJson;
      watch(
        () => props.table,
        () => {
          tableName.value = props.table;
        },
        { immediate: true }
      );

      //Lắng nghe sự kiện thay đổi cấu hình
      watch(
        () => props.properties,
        async (valueObj) => {
          //Render lại biểu mẫu
          console.log('bảng chínhpropertiesThay đổi', props.properties);
          formRendered.value = false;
          addFormChangeEvent();
          // update-begin--author:liaozhiyang---date:20260318---for:【issues/9414】Thiết lập bảng con một-mộtlabelĐộ dài không có hiệu lực
          if (!extConfigJson) {
              try {
              const data = await getFormItem();
              extConfigJson = JSON.parse(data.head.extConfigJson);
              extConfigJson = {
                formLabelLength: extConfigJson.formLabelLength,
                formLabelLengthShow: extConfigJson.formLabelLengthShow,
              };
            } catch (error) {
              console.log(error);
            }
          }
          // update-end--author:liaozhiyang---date:20260318---for:【issues/9414】Thiết lập bảng con một-mộtlabelĐộ dài không có hiệu lực
          createFormSchemas(props.properties, props.requiredFields, checkOnlyFieldValue, extConfigJson);
          formRendered.value = true;
        },
        { deep: true, immediate: true }
      );

      //Lắng nghe dữ liệu bảng chínhID
      watch(
        () => props.mainId,
        (valueObj) => {
          //Tải lại dữ liệu bảng con
          console.log('bảng chínhIDThay đổi', props.mainId);
          // Độ trễ tại đây100mili giây là để chopropertiesViệc lắng nghe của thực thi trước
          setTimeout(() => {
            resetSubForm();
          }, 100);
        },
        { immediate: true }
      );

      watch(
        () => props.disabled,
        (val) => {
          setProps({ disabled: val });
        }
      );

      /**
       * Lắng nghe sự kiện thay đổi biểu mẫu
       */
      async function addFormChangeEvent() {
        let formRefObject = await getRefPromise(onlineFormRef);
        formRefObject.$formValueChange = (field, value, changeFormData) => {
          let emitArgument = { [field]: value };
          // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
          // bảng con một-một Bản ghi liên kết và trường bảng khác
          if(changeFormData){
            setFieldsValue(changeFormData);
          }
          // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441】onlineMột-nhiều cộng thêm bản ghi liên kết và trường bảng khác
          emit('formChange', emitArgument);
        };
      }

      /**
       * Logic giá trị mặc định của biểu mẫu hiện tại-Kích hoạt khi vào trang thêm mới
       */
      function handleDefaultValue() {
        if (unref(props.isUpdate) === false) {
          let fieldProperties = toRaw(defaultValueFields[tableName.value]);
          loadFormFieldsDefVal(fieldProperties, (values) => {
            setFieldsValue(values);
          });
        }
      }

      /**
       * Khi dữ liệu bảng chínhIDxảy ra thay đổi, bảng con lấy lại dữ liệu
       * @returns {Promise<void>}
       */
      async function resetSubForm() {
        //TODO Quy tắc điền giá trị
        // update-begin--author:sunjianlei --- date:20191111 --- for: Mỗi lần tải dữ liệu đều thực thi lại quy tắc điền giá trị một lượt -----------
        //this.$emit('executeFillRule', {form:this.form, target: this})
        // update-end--author:sunjianlei --- date:20191111 --- for: Mỗi lần tải dữ liệu đều thực thi lại quy tắc điền giá trị một lượt -----------
        await getRefPromise(formRendered);
        await resetFields();
        handleDefaultValue();
        const { table, mainId } = props;
        if (!table || !mainId) {
          return;
        }
        try {
          const values = await loadData(table, mainId);
          dbData.value = values;
          // VUEN-1033
          await setFieldsValue(values);
        } catch (error) {
          const message = error?.message || 'Tải dữ liệu bảng con thất bại';
          console.error('Tải dữ liệu bảng con thất bại:', error);
          $message.error(message);
        }
      }

      async function loadData(table, mainId) {
        let url = `${baseUrl}/${table}/${mainId}`;
        //console.log("loadData Dữ liệu bảng con url:", url);
        try {
          const res = await defHttp.get({ url }, { isTransformResponse: false });
          if (!res?.success) {
            throw new Error(res?.message || 'Tải dữ liệu bảng con thất bại');
          }
          return res.result;
        } finally {
          //resetFields()
          dbData.value = '';
        }
      }

      function getAll() {
        return new Promise((resolve, reject) => {
          validate()
            .then(() => {
              let formData = getFieldsValue();
              formData = changeDataIfArray2String(formData);
              resolve(formData);
            })
            .catch((e) => {
              if (e.errorFields) {
                e.scrollToField = () => e.errorFields[0] && scrollToField(e.errorFields[0].name, { behavior: 'smooth', block: 'center' });
              }
              reject(e);
            });
        });
      }

      //Lấy đối tượng sự kiện biểu mẫu Dùng khi lắng nghe thay đổi biểu mẫu
      function getFormEvent() {
        let row = getFieldsValue();
        if (!row.id) {
          row.id = 'sub-change-temp-id';
        }
        return {
          row,
          target: context,
        };
      }

      //Thiết lập giá trị của biểu mẫu
      function setValues(values) {
        setFieldsValue(values);
      }

      function executeFillRule() {
        let formData = getFieldsValue();
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        loadFormFieldsDefVal(fieldProperties, (values) => {
          setFieldsValue(values);
        }, formData);
      }

      // update-begin--author:liaozhiyang---date:20260714---for：【LHZP-249】Mở popup thêm mới lần thứ hai, giá trị mặc định của bảng con một-một bị mất
      async function resetAndApplyDefaultValue() {
        await resetFields();
        let fieldProperties = toRaw(defaultValueFields[tableName.value]);
        loadFormFieldsDefVal(fieldProperties, (values) => {
          setFieldsValue(values);
        });
      }
      // update-end--author:liaozhiyang---date:20260714---for：【LHZP-249】Mở popup thêm mới lần thứ hai, giá trị mặc định của bảng con một-một bị mất

      const context = {
        onlineFormRef,
        baseColProps,
        formSchemas,
        registerForm,
        setFieldsValue,
        getFieldsValue,
        getFormEvent,
        setValues,
        getAll,
        executeFillRule,
        resetAndApplyDefaultValue,
        sh: fieldDisplayStatus,
        resetFields,
        updateSchema,
      };
      return context;
    },
  };
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20240527---for：【TV360X-263】tabPhong cách một-một, component tải lên của bảng con có dữ liệu nhưng không render ra
  :deep(.ant-upload-list-item-container) {
    &.ant-motion-collapse {
      height: auto !important;
    }
  }
  // update-end--author:liaozhiyang---date:20240527---for：【TV360X-263】tabPhong cách một-một, component tải lên của bảng con có dữ liệu nhưng không render ra
</style>
