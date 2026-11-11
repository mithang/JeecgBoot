<template>
  <Form v-bind="getBindValue" :class="getFormClass" ref="formElRef" :model="formModel" @keypress.enter="handleEnterPress">
    <Row v-bind="getRow">
      <slot name="formHeader"></slot>
      <template v-for="schema in getSchema" :key="schema.field">
        <FormItem
          :tableAction="tableAction"
          :formActionType="formActionType"
          :schema="schema"
          :formProps="getProps"
          :allDefaultValues="defaultValueRef"
          :formModel="formModel"
          :formName="getBindValue.name"
          :source="getBindValue.source"
          :setFormModel="setFormModel"
          :validateFields="validateFields"
          :clearValidate="clearValidate"
          v-auth="schema.auth"
        >
          <template #[item]="data" v-for="item in Object.keys($slots)">
            <slot :name="item" v-bind="data || {}"></slot>
          </template>
        </FormItem>
      </template>

      <FormAction v-bind="getFormActionBindProps" @toggle-advanced="handleToggleAdvanced">
        <template #[item]="data" v-for="item in ['resetBefore', 'submitBefore', 'advanceBefore', 'advanceAfter']">
          <slot :name="item" v-bind="data || {}"></slot>
        </template>
      </FormAction>
      <slot name="formFooter"></slot>
    </Row>
  </Form>
</template>
<script lang="ts">
  import type { FormActionType, FormProps, FormSchema } from './types/form';
  import type { AdvanceState } from './types/hooks';
  import type { Ref } from 'vue';

  import { defineComponent, reactive, ref, computed, unref, onMounted, watch, nextTick } from 'vue';
  import { Form, Row } from 'ant-design-vue';
  import FormItem from './components/FormItem.vue';
  import FormAction from './components/FormAction.vue';

  import { dateItemType } from './helper';
  import { dateUtil } from '/@/utils/dateUtil';

  // import { cloneDeep } from 'lodash-es';
  import { deepMerge } from '/@/utils';

  import { useFormValues } from './hooks/useFormValues';
  import useAdvanced from './hooks/useAdvanced';
  import { useFormEvents } from './hooks/useFormEvents';
  import { createFormContext } from './hooks/useFormContext';
  import { useAutoFocus } from './hooks/useAutoFocus';
  import { useModalContext } from '/@/components/Modal';

  import { basicProps } from './props';
  import componentSetting from '/@/settings/componentSetting';

  import { useDesign } from '/@/hooks/web/useDesign';
  import dayjs from 'dayjs';
  import { useDebounceFn } from '@vueuse/core';
  import { isFunction, isObject } from '/@/utils/is';

  export default defineComponent({
    name: 'BasicForm',
    components: { FormItem, Form, Row, FormAction },
    props: basicProps,
    emits: ['advanced-change', 'reset', 'submit', 'register'],
    setup(props, { emit, attrs }) {
      const formModel = reactive<Recordable>({});
      const modalFn = useModalContext();

      const advanceState = reactive<AdvanceState>({
        // Mặc định là trạng thái thu gọn
        isAdvanced: false,
        hideAdvanceBtn: true,
        isLoad: false,
        actionSpan: 6,
      });

      const defaultValueRef = ref<Recordable>({});
      const isInitedDefaultRef = ref(false);
      const propsRef = ref<Partial<FormProps>>({});
      const schemaRef = ref<Nullable<FormSchema[]>>(null);
      const formElRef = ref<Nullable<FormActionType>>(null);

      const { prefixCls } = useDesign('basic-form');

      // Get the basic configuration of the form
      const getProps = computed((): FormProps => {
        let mergeProps = { ...props, ...unref(propsRef) } as FormProps;
        // Giải thích logic mã: Nếu người dùng đã đặt labelWidth thì làm cho labelCol vô hiệu, giải quyết vấn đề labelWidth đặt không có hiệu lực
        if (mergeProps.labelWidth) {
          mergeProps.labelCol = undefined;
        }
        // Giải thích logic mã: 【QQYUN-6566] BasicForm hỗ trợ hiển thị một dòng(inline)
        if (mergeProps.layout === 'inline') {
          if (mergeProps.labelCol === componentSetting.form.labelCol) {
            mergeProps.labelCol = undefined;
          }
          if (mergeProps.wrapperCol === componentSetting.form.wrapperCol) {
            mergeProps.wrapperCol = undefined;
          }
        }
        return mergeProps;
      });

      const getFormClass = computed(() => {
        return [
          prefixCls,
          {
            [`${prefixCls}--compact`]: unref(getProps).compact,
            'jeecg-form-detail-effect': unref(getProps).disabled
          },
        ];
      });

      // Get uniform row style and Row configuration for the entire form
      const getRow = computed((): Recordable => {
        const { baseRowStyle = {}, rowProps } = unref(getProps);
        return {
          style: baseRowStyle,
          ...rowProps,
        };
      });

      const getBindValue = computed(() => {
        const bindValue = { ...attrs, ...props, ...unref(getProps) } as Recordable;
        // Giải thích logic mã: 【issues/8484]Nhấp vào label của popup thêm mới trong từ điển phân loại sẽ kích hoạt vùng tìm kiếminput
        if (bindValue.name === undefined && bindValue.source === 'table-query') {
          bindValue.name = 'top-query-form';
        }
        return bindValue;
      });

      const getSchema = computed((): FormSchema[] => {
        const schemas: FormSchema[] = unref(schemaRef) || (unref(getProps).schemas as any);
        for (const schema of schemas) {
          const { defaultValue, component, componentProps } = schema;
          // handle date type
          if (defaultValue && dateItemType.includes(component)) {
            // Giải thích logic mã: 【issues/435] Control ngày sinh mã gán giá trị mặc định bị lỗi------------
            let valueFormat:string = "";
            // Giải thích logic mã: 【issues/8683]Khi componentProps của component DatePicker dùng dạng hàm thì giá trị khởi tạo lấy không đúng
            if(isObject(componentProps)) {
              valueFormat = componentProps?.valueFormat;
            } else if (isFunction(componentProps)) {
              try {
                // @ts-ignore
                valueFormat = componentProps({schema, tableAction: props.tableAction, formModel})?.valueFormat;
              } catch (error) {
              }
            }
            // update-end--author:liaozhiyang---date:20250818---for【issues/8683]Khi componentProps của component DatePicker dùng dạng hàm thì giá trị khởi tạo lấy không đúng
            if(!valueFormat){
              console.warn("Chưa cấu hìnhvalueFormat,Có thể gây lỗi định dạng!");
            }
            if (!Array.isArray(defaultValue)) {
              // Giải thích logic mã: [issues/215]Ô tìm kiếm ở trang danh sách (ô chọn ngày) đặt thời gian khởi tạo, vừa vào trang thì backend báo lỗi chuyển đổi kiểu ngày------------
              if(valueFormat){
                // schema.defaultValue = dateUtil(defaultValue).format(valueFormat);
                // Giải thích logic mã: 【TV360X-346 ] Component thời gian điền giá trị mặc định có vấn đề
                schema.defaultValue = dateUtil(defaultValue, valueFormat).format(valueFormat);
              }else{
                schema.defaultValue = dateUtil(defaultValue);
              }
            } else {
              const def: dayjs.Dayjs[] = [];
              defaultValue.forEach((item) => {
                // Giải thích logic mã: [issues/215]Ô tìm kiếm ở trang danh sách (ô chọn ngày) đặt thời gian khởi tạo, vừa vào trang thì backend báo lỗi chuyển đổi kiểu ngày------------
                if(valueFormat){
                  // Giải thích logic mã: 【TV360X-346 ] Component thời gian điền giá trị mặc định có vấn đề
                  def.push(dateUtil(item, valueFormat).format(valueFormat));
                }else{
                  def.push(dateUtil(item));
                }
              });
              // Giải thích logic mã: 【issues/1114]rangepicker và các control thời gian khác báo lỗi (vue3.4Phiên bản trở lên có vấn đề)
              def.forEach((item, index) => {
                defaultValue[index] = item;
              });
            }
          }
        }
        if (unref(getProps).showAdvancedButton) {
          return schemas.filter((schema) => schema.component !== 'Divider') as FormSchema[];
        } else {
          return schemas as FormSchema[];
        }
      });

      const { handleToggleAdvanced } = useAdvanced({
        advanceState,
        emit,
        getProps,
        getSchema,
        formModel,
        defaultValueRef,
      });

      const { handleFormValues, initDefault } = useFormValues({
        getProps,
        defaultValueRef,
        getSchema,
        formModel,
      });

      useAutoFocus({
        getSchema,
        getProps,
        isInitedDefault: isInitedDefaultRef,
        formElRef: formElRef as Ref<FormActionType>,
      });

      const {
        handleSubmit,
        setFieldsValue,
        clearValidate,
        validate,
        validateFields,
        getFieldsValue,
        updateSchema,
        resetSchema,
        getSchemaByField,
        appendSchemaByField,
        removeSchemaByFiled,
        resetFields,
        scrollToField,
      } = useFormEvents({
        emit,
        getProps,
        formModel,
        getSchema,
        defaultValueRef,
        formElRef: formElRef as Ref<FormActionType>,
        schemaRef: schemaRef as Ref<FormSchema[]>,
        handleFormValues,
      });

      createFormContext({
        resetAction: resetFields,
        submitAction: handleSubmit,
      });

      watch(
        () => unref(getProps).model,
        () => {
          const { model } = unref(getProps);
          if (!model) return;
          setFieldsValue(model);
        },
        {
          immediate: true,
        }
      );

      watch(
        () => unref(getProps).schemas,
        (schemas) => {
          resetSchema(schemas ?? []);
        }
      );

      watch(
        () => getSchema.value,
        (schema) => {
          nextTick(() => {
            //  Solve the problem of modal adaptive height calculation when the form is placed in the modal
            modalFn?.redoModalHeight?.();
          });
          if (unref(isInitedDefaultRef)) {
            return;
          }
          if (schema?.length) {
            initDefault();
            isInitedDefaultRef.value = true;
          }
        }
      );

      async function setProps(formProps: Partial<FormProps>): Promise<void> {
        propsRef.value = deepMerge(unref(propsRef) || {}, formProps);
      }

      // Giải thích logic mã: QQYUN-3121 [Tối ưu] Vấn đề về chế độ xem biểu mẫu#scottKiểm thử 8, tính năng này chưa được triển khai
      const onFormSubmitWhenChange = useDebounceFn(handleSubmit, 300);
      function setFormModel(key: string, value: any) {
        formModel[key] = value;
        // const { validateTrigger } = unref(getBindValue);
        // if (!validateTrigger || validateTrigger === 'change') {
        //   validateFields([key]).catch((_) => {});
        // }
        if(props.autoSearch === true){
          onFormSubmitWhenChange();
        }
      }

      function handleEnterPress(e: KeyboardEvent) {
        const { autoSubmitOnEnter } = unref(getProps);
        if (!autoSubmitOnEnter) return;
        if (e.key === 'Enter' && e.target && e.target instanceof HTMLElement) {
          const target: HTMLElement = e.target as HTMLElement;
          if (target && target.tagName && target.tagName.toUpperCase() == 'INPUT') {
            handleSubmit();
          }
        }
      }

      /**
       * Lấy componentProps, xử lý trường hợp có thể là hàm
       * @param schema
       */
      function getSchemaComponentProps(schema: FormSchema) {
        if (typeof schema.componentProps === 'function') {
          return schema.componentProps({
            schema,
            tableAction: props.tableAction,
            formActionType,
            formModel,
          })
        }
        return schema.componentProps
      }

      const formActionType: Partial<FormActionType> = {
        getFieldsValue,
        setFieldsValue,
        resetFields,
        updateSchema,
        resetSchema,
        setProps,
        getProps,
        getSchemaByField,
        removeSchemaByFiled,
        appendSchemaByField,
        clearValidate,
        validateFields,
        validate,
        submit: handleSubmit,
        scrollToField: scrollToField,
        getSchemaComponentProps,
      };

      onMounted(() => {
        initDefault();
        emit('register', formActionType);
      });

      return {
        getBindValue,
        handleToggleAdvanced,
        handleEnterPress,
        formModel,
        defaultValueRef,
        advanceState,
        getRow,
        getProps,
        formElRef,
        getSchema,
        formActionType: formActionType as any,
        setFormModel,
        getFormClass,
        getFormActionBindProps: computed((): Recordable => ({ ...getProps.value, ...advanceState })),
        ...formActionType,
      };
    },
  });
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-basic-form';

  .@{prefix-cls} {
    .ant-form-item {
      &-label label::after {
        margin: 0 6px 0 2px;
      }

      &-with-help {
        margin-bottom: 0;
      }
      //&:not(.ant-form-item-with-help) {
      //  margin-bottom: 24px;
      //}
      // Giải thích logic mã: 【TV360X-1420] nhấp nháy khi kiểm chứng
      &-has-error {
        margin-bottom: 24px;
      }

      // Kiểu middleware của component biểu mẫu
      .j-form-item-middleware {
        flex: 1;
        width: 100%
      }

      &.suffix-item {
        .ant-form-item-children {
          display: flex;
        }

        .ant-form-item-control {
          margin-top: 4px;
        }

        // 【QQYUN-12876]Khi suffix dạng thu gọn, chiều rộng của middleware component biểu mẫu không chiếm đầy
        &.suffix-compact .j-form-item-middleware {
          flex: unset;
          width: auto;
        }

        .suffix {
          display: inline-flex;
          padding-left: 6px;
          margin-top: 1px;
          line-height: 1;
          align-items: center;
        }
      }
    }
    /*[Làm đẹp biểu mẫu] Cỡ chữ của form giảm một cỡ*/
/*    .ant-form-item-label > label{
      font-size: 13px;
    }
    .ant-form-item .ant-select {
      font-size: 13px;
    }
    .ant-select-item-option-selected {
      font-size: 13px;
    }
    .ant-select-item-option-content {
      font-size: 13px;
    }
    .ant-input {
      font-size: 13px;
    }*/
    /*[Làm đẹp biểu mẫu] Cỡ chữ của form giảm một cỡ*/
    
    .ant-form-explain {
      font-size: 14px;
    }

    &--compact {
      .ant-form-item {
        margin-bottom: 8px !important;
      }
    }
    // Giải thích logic mã: 【QQYUN-6566] BasicForm hỗ trợ hiển thị một dòng(inline)
    &.ant-form-inline {
      & > .ant-row {
        .ant-col { width:auto !important; }
      }
    }
  }
</style>
