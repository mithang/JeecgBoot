<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="title"
    width="90%"
    destroyOnClose
    @ok="handleSubmit"
    :showFooter="showFooter"
  >
    <!-- Phần trên: Biểu mẫu thông tin cơ bản -->
    <BasicForm @register="registerForm" ref="formRef">
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
      <template #whiteListSlot="{ model, field }">
        <a-textarea
          v-model:value="model[field]"
          :rows="5"
          placeholder="Ví dụ:&#10;192.168.1.100&#10;10.0.0.0/8&#10;172.16.*.*"
          :disabled="formDisabled"
        />
        <!-- Xem trước nhãn -->
        <div v-if="model[field]" style="margin-top: 8px">
          <a-tag
            v-for="item in parseWhiteList(model[field])"
            :key="item"
            color="green"
            style="margin-bottom: 4px"
          >
            {{ item }}
          </a-tag>
        </div>
        <!-- Nút sắp xếp -->
        <div v-if="model[field] && !formDisabled" style="margin-top: 4px; text-align: right">
          <a-button size="small" @click="formatWhiteList(model, field)">Sắp xếp</a-button>
        </div>
      </template>
    </BasicForm>

    <!-- Phần dưới: Tabs -->
    <a-tabs v-model:activeKey="activeTab" style="margin-top: 16px">
      <a-tab-pane key="headers" tab="Header yêu cầu">
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
          asyncRemove
          @removed="onHeaderRemoved"
        />
      </a-tab-pane>

      <a-tab-pane key="params" tab="Tham số yêu cầu">
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
        />
      </a-tab-pane>

      <a-tab-pane key="body" tab="Body yêu cầu">
        <div style="margin-bottom: 8px" v-if="!formDisabled">
          <a-space>
            <a-button size="small" @click="toggleBodyExample">
              {{ showBodyExample ? 'Ẩn ví dụ' : 'Xem ví dụ' }}
            </a-button>
            <a-button size="small" type="primary" ghost @click="fillBodyExample" :disabled="formDisabled">Điền ví dụ</a-button>
            <a-button size="small" danger @click="clearBody" :disabled="formDisabled">Xóa sạch</a-button>
          </a-space>
        </div>
        <div
          v-if="showBodyExample"
          style="margin-bottom: 12px; padding: 12px; background: #f5f5f5; border-radius: 4px; border-left: 3px solid #1890ff"
        >
          <div style="margin-bottom: 8px; color: #666; font-size: 12px">
            <strong>Tham khảo ví dụ:</strong> Dưới đây là ví dụ JSON request body thường gặp, vui lòng điều chỉnh theo trường API thực tế rồi điền vào trình soạn thảo bên dưới.
          </div>
          <pre
            style="margin: 0; white-space: pre-wrap; word-break: break-all; font-family: 'Courier New', monospace; font-size: 12px; color: #333"
          >{{ bodyExample }}</pre>
        </div>
        <div style="border: 1px solid #d9d9d9; border-radius: 4px; min-height: 300px">
          <CodeEditor v-model:value="bodyContent" mode="application/json" :readonly="formDisabled" />
        </div>
      </a-tab-pane>

      <a-tab-pane key="response" tab="Cấu hình phản hồi" v-if="false">
        <div style="margin-bottom: 16px">
          <h4 style="margin-bottom: 8px">Ví dụ phản hồi</h4>
          <div style="border: 1px solid #d9d9d9; border-radius: 4px; min-height: 200px">
            <CodeEditor v-model:value="responseExample" mode="application/json" :readonly="formDisabled" />
          </div>
        </div>
        <div>
          <h4 style="margin-bottom: 8px">Giải thích trường phản hồi</h4>
          <JVxeTable
            keep-source
            ref="responseField"
            :loading="responseFieldTable.loading"
            :columns="responseFieldTable.columns"
            :dataSource="responseFieldTable.dataSource"
            :height="240"
            :disabled="formDisabled"
            :rowNumber="true"
            :rowSelection="true"
            :toolbar="true"
          />
        </div>
      </a-tab-pane>
    </a-tabs>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref, computed, unref, reactive } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { CodeEditor } from '/@/components/CodeEditor';
  import {
    formSchema,
    openApiHeaderJVxeColumns,
    openApiParamJVxeColumns,
    responseFieldJVxeColumns,
    requestMethodOptions,
    FIXED_HEADER_KEYS,
  } from '../OpenApi.data';
  import { saveOrUpdate, getGenPath } from '../OpenApi.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const emit = defineEmits(['register', 'success']);
  const $message = useMessage();
  // update-begin--author:liaozhiyang---date:20260804---for: [LHZP-1266] Header yêu cầu mặc định appKey/signature/timestamp: chặn kiểm tra khi xóa, bắt buộc bị vô hiệu hóa
  // Chặn kiểm tra khi xóa header yêu cầu mặc định appKey/signature/timestamp

  /** Chặn tham số mặc định khi xóa hàng loạt header yêu cầu */
  async function onHeaderRemoved(event) {
    const deleteRows = event?.deleteRows || [];
    const blockedRows = deleteRows.filter((row) => FIXED_HEADER_KEYS.includes(row.headerKey));
    const allowRows = deleteRows.filter((row) => !FIXED_HEADER_KEYS.includes(row.headerKey));

    if (blockedRows.length) {
      const names = blockedRows.map((row) => row.headerKey).join(', ');
      $message.createMessage.warning(`Header yêu cầu mặc định ${names} không được phép xóa`);
    }

    // Tất cả đều là header yêu cầu mặc định, hủy xóa trực tiếp
    if (!allowRows.length) {
      return;
    }

    // Chỉ xóa các hàng được phép xóa
    if (blockedRows.length) {
      await openApiHeader.value?.removeRows(allowRows);
      await openApiHeader.value?.clearSelection?.();
      return;
    }

    // Không có header yêu cầu mặc định, thực hiện xác nhận xóa bình thường
    if (typeof event.confirmRemove === 'function') {
      await event.confirmRemove();
    }
  }
  // update-end--author:liaozhiyang---date:20260804---for: [LHZP-1266] Header yêu cầu mặc định appKey/signature/timestamp: chặn kiểm tra khi xóa, bắt buộc bị vô hiệu hóa
  const isUpdate = ref(true);
  const formDisabled = ref(false);
  const showFooter = ref(true);
  const activeTab = ref('headers');
  const bodyContent = ref('');
  const responseExample = ref('');
  const showBodyExample = ref(false);

  // Mã ví dụ request body (để frontend tham khảo / điền một lần)
  const bodyExample = `{
  "name": "jeecg",
  "remember": true,
  "extra": {
    "deviceId": "abc123",
    "platform": "web"
  },
  "roles": ["jeecg", "user"]
}`;

  const openApiHeader = ref();
  const openApiParam = ref();
  const responseField = ref();

  const openApiHeaderTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: openApiHeaderJVxeColumns,
  });
  const openApiParamTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: openApiParamJVxeColumns,
  });
  const responseFieldTable = reactive({
    loading: false,
    dataSource: [] as any[],
    columns: responseFieldJVxeColumns,
  });

  const [registerForm, { setProps, resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 100,
    schemas: formSchema,
    showActionButtonGroup: false,
    baseColProps: { span: 12 },
  });

  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    showFooter.value = !!data?.showFooter;
    setDrawerProps({ confirmLoading: false, showFooter: showFooter.value });
    isUpdate.value = !!data?.isUpdate;
    formDisabled.value = !data?.showFooter;

    if (unref(isUpdate)) {
      await setFieldsValue({
        ...data.record,
      });
      openApiHeaderTable.dataSource = data.record.headersJson ? JSON.parse(data.record.headersJson) : [];
      openApiParamTable.dataSource = data.record.paramsJson ? JSON.parse(data.record.paramsJson) : [];
      bodyContent.value = data.record.requestBody || '';
      responseExample.value = data.record.responseExample || '';
      responseFieldTable.dataSource = data.record.responseFieldsJson ? JSON.parse(data.record.responseFieldsJson) : [];
    } else {
      const requestUrlObj = await getGenPath({});
      await setFieldsValue({
        requestUrl: requestUrlObj.result,
      });
      openApiHeaderTable.dataSource = [
        { headerKey: 'appKey', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
        { headerKey: 'signature', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
        { headerKey: 'timestamp', required: '1', paramType: 'string', defaultValue: '', example: '', note: '' },
      ];
    }
    setProps({ disabled: !data?.showFooter });
  });

  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : !unref(formDisabled) ? 'Chỉnh sửa' : 'Chi tiết'));

  /** Phân tích văn bản danh sách trắng thành mảng mục */
  function parseWhiteList(text: string): string[] {
    if (!text) return [];
    return text
      .split(/[,\n]/)
      .map((s) => s.trim())
      .filter(Boolean);
  }

  /** Sắp xếp whitelist: bỏ dòng trống, bỏ trùng, mỗi dòng một cái */
  function formatWhiteList(model: any, field: string) {
    const items = parseWhiteList(model[field]);
    const unique = [...new Set(items)];
    model[field] = unique.join('\n');
  }

  async function reset() {
    await resetFields();
    activeTab.value = 'headers';
    openApiHeaderTable.dataSource = [];
    openApiParamTable.dataSource = [];
    responseFieldTable.dataSource = [];
    bodyContent.value = '';
    responseExample.value = '';
    showBodyExample.value = false;
  }

  /** Bật/tắt hiển thị ví dụ request body */
  function toggleBodyExample() {
    showBodyExample.value = !showBodyExample.value;
  }

  /** Điền mã ví dụ vào trình chỉnh sửa request body */
  function fillBodyExample() {
    bodyContent.value = bodyExample;
  }

  /** Xóa sạch request body */
  function clearBody() {
    bodyContent.value = '';
  }

  async function handleSubmit() {
    try {
      const values = await validate();
      setDrawerProps({ confirmLoading: true });

      // Thu thập dữ liệu JVxeTable (getTableData trả về trực tiếp một mảng)
      const headerData = openApiHeader.value?.getTableData();
      const paramData = openApiParam.value?.getTableData();
      const responseFieldData = responseField.value?.getTableData();

      const headersJson = headerData?.length ? JSON.stringify(headerData) : null;
      // update-begin--author:liusq---date:20260805---for: [Tham số yêu cầu] Sau khi xóa tất cả dữ liệu rồi lưu, mở lại vẫn tồn tại
      // Mảng trống cần gửi rõ ràng, tránh null bị tham số yêu cầu serialize bỏ qua khiến backend giữ dữ liệu gốc
      const paramsJson = JSON.stringify(paramData || []);
      // update-end--author:liusq---date:20260805---for: [Tham số yêu cầu] Sau khi xóa tất cả dữ liệu rồi lưu, mở lại vẫn tồn tại
      const responseFieldsJson = responseFieldData?.length ? JSON.stringify(responseFieldData) : null;

      // Validate body JSON
      if (bodyContent.value) {
        try {
          if (typeof JSON.parse(bodyContent.value) != 'object') {
            $message.createMessage.error('Lỗi định dạng JSON, vui lòng kiểm tra dữ liệu đã nhập');
            return;
          }
        } catch (e) {
          $message.createMessage.error('Lỗi định dạng JSON, vui lòng kiểm tra dữ liệu đã nhập');
          return;
        }
      }

      // Validate response example JSON
      if (responseExample.value) {
        try {
          JSON.parse(responseExample.value);
        } catch (e) {
          $message.createMessage.error('Ví dụ phản hồi lỗi định dạng JSON, vui lòng kiểm tra dữ liệu đã nhập');
          return;
        }
      }

      const submitValues = {
        ...values,
        headersJson,
        paramsJson,
        requestBody: bodyContent.value || null,
        responseExample: responseExample.value || null,
        responseFieldsJson,
      };

      await saveOrUpdate(submitValues, isUpdate.value);
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-calendar-picker) {
    width: 100%;
  }
</style>
