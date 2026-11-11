<template>
  <BasicModal :bodyStyle="{ padding: '20px' }" v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" width="80%" @ok="handleSubmit">
    <a-row :gutter="24">
      <a-col :span="10">
        <BasicForm @register="registerForm" ref="formRef" name="OpenApiForm">
          <template #requestUrlSlot="{ model, field }">
            <a-input-group compact style="display: flex">
              <a-input v-model:value="model[field]" disabled style="flex: 1; min-width: 0" />
              <a-select
                v-model:value="model['requestMethod']"
                style="width: 130px; flex-shrink: 0"
                :options="requestMethodOptions"
                :disabled="formDisabled"
                placeholder="Phương thức yêu cầu"
              />
            </a-input-group>
          </template>
        </BasicForm>
      </a-col>
      <a-col :span="14">
        <a-row :gutter="24">
          <a-col :span="24" style="margin-top: -0.6em">
            <JVxeTable
              keep-source
              ref="openApiHeader"
              :loading="openApiHeaderTable.loading"
              :columns="openApiHeaderTable.columns"
              :dataSource="openApiHeaderTable.dataSource"
              :height="240"
              :disabled="formDisabled"
              :rowNumber="true"
              :rowSelection="true"
              :toolbar="true"
              size="mini"
            />
          </a-col>
          <a-col :span="24">
            <JVxeTable
              keep-source
              ref="openApiParam"
              :loading="openApiParamTable.loading"
              :columns="openApiParamTable.columns"
              :dataSource="openApiParamTable.dataSource"
              :height="240"
              :disabled="formDisabled"
              :rowNumber="true"
              :rowSelection="true"
              :toolbar="true"
              size="mini"
            />
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { useJvxeMethod } from '/@/hooks/system/useJvxeMethods.ts';
  import { formSchema, openApiHeaderJVxeColumns, openApiParamJVxeColumns, requestMethodOptions } from '../OpenApi.data';
  import { saveOrUpdate, queryOpenApiHeader, queryOpenApiParam, getGenPath } from '../OpenApi.api';
  import { VALIDATE_FAILED } from '/@/utils/common/vxeUtils';
  import { useMessage } from "@/hooks/web/useMessage";

  // Khai báo Emits
  const $message = useMessage();
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const formDisabled = ref(false);
  const refKeys = ref(['openApiHeader', 'openApiParam']);
  const activeKey = ref('openApiHeader');
  const openApiHeader = ref();
  const openApiParam = ref();
  const tableRefs = { openApiHeader, openApiParam };
  const openApiHeaderTable = reactive({
    loading: false,
    dataSource: [],
    columns: openApiHeaderJVxeColumns,
  });
  const openApiParamTable = reactive({
    loading: false,
    dataSource: [],
    columns: openApiParamJVxeColumns,
  });
  // Cấu hình biểu mẫu
  const [registerForm, { setProps, resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 24 },
    wrapperCol: { span: 24 },
  });
  // Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Đặt lại biểu mẫu
    await reset();
    setModalProps({ confirmLoading: false, showCancelBtn: data?.showFooter, showOkBtn: data?.showFooter });
    isUpdate.value = !!data?.isUpdate;
    formDisabled.value = !data?.showFooter;
    if (unref(isUpdate)) {
      // Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
      // Yêu cầu API backend để lấy dữ liệu
      //  requestSubTableData(queryOpenApiHeader, {id:data?.record?.id}, openApiHeaderTable)
      //  requestSubTableData(queryOpenApiParam, {id:data?.record?.id}, openApiParamTable)
      openApiHeaderTable.dataSource = !!data.record.headersJson?JSON.parse(data.record.headersJson):[];
      openApiParamTable.dataSource = !!data.record.paramsJson?JSON.parse(data.record.paramsJson):[];
    } else {
      //  /openapi/genpath
      const requestUrlObj = await getGenPath({});
      await setFieldsValue({
        requestUrl: requestUrlObj.result

      });
    }
    // Khi ẩn phần dưới thì vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter });
  });
  // Cấu hình phương thức
  const [handleChangeTabs, handleSubmit, requestSubTableData, formRef] = useJvxeMethod(
    requestAddOrEdit,
    classifyIntoFormData,
    tableRefs,
    activeKey,
    refKeys
  );

  // Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : !unref(formDisabled) ? 'Chỉnh sửa' : 'Chi tiết'));

  async function reset() {
    await resetFields();
    activeKey.value = 'openApiHeader';
    openApiHeaderTable.dataSource = [];
    openApiParamTable.dataSource = [];
  }
  function classifyIntoFormData(allValues) {
    let main = Object.assign({}, allValues.formValue);
    return {
      ...main, // Mở rộng
      headersJson: allValues.tablesValue[0].tableData,
      paramsJson: allValues.tablesValue[1].tableData,
    };
  }
  // Sự kiện gửi biểu mẫu
  async function requestAddOrEdit(values) {
    let headersJson = !!values.headersJson?JSON.stringify(values.headersJson):null;
    let paramsJson = !!values.headersJson?JSON.stringify(values.paramsJson):null;
    try {
      if (!!values.requestBody){
        try {
          if (typeof JSON.parse(values.requestBody)!='object'){
            $message.createMessage.error("Lỗi định dạng JSON, vui lòng kiểm tra dữ liệu đã nhập");
            return;
          }
        } catch (e) {
          $message.createMessage.error("Lỗi định dạng JSON, vui lòng kiểm tra dữ liệu đã nhập");
          return;
        }
      }
      setModalProps({ confirmLoading: true });
      values.headersJson = headersJson
      values.paramsJson = paramsJson
      console.log("values*****model>>>>", values)
      // Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate.value);
      // Đóng popup
      closeModal();
      // Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  /** Kiểu ô nhập thời gian và số */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
