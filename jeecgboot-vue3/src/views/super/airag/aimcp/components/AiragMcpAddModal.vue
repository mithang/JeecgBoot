<template>
  <BasicModal
    destroyOnClose
    @register="registerModal"
    :canFullscreen="false"
    width="640px"
    :title="isEdit ? 'Chỉnh sửa MCP' : 'Thêm mới MCP'"
    wrapClassName="airag-mcp-modal"
  >
    <div class="modal">
      <div class="mcp-content">
        <BasicForm @register="registerForm">
          <!-- Chọn loại category: chọn một -->
          <template #category="{ model, field }">
            <a-radio-group v-model:value="model[field]" @change="onCategoryChange" :disabled="isEdit">
              <a-radio value="mcp">MCP</a-radio>
              <a-radio value="plugin">Plugin</a-radio>
            </a-radio-group>
          </template>
          <!-- Chọn loại MCP -->
          <template #type="{ model, field }">
            <a-select v-model:value="model[field]" @change="onTypeChange" :disabled="isEdit">
              <a-select-option value="sse">SSE</a-select-option>
              <a-select-option value="stdio">STDIO</a-select-option>
              <a-select-option value="http">HTTP</a-select-option>
            </a-select>
          </template>
          <!-- endpoint chuyển đổi placeholder theo loại -->
          <template #endpoint="{ model, field }">
            <a-input
              v-model:value="model[field]"
              :placeholder="endpointPlaceholder"
              autocomplete="new-password"
            />
          </template>
          <!-- headers chuyển đổi tên nhãn và placeholder theo loại -->
          <template #headers>
            <!-- Giải quyết việc duyệt ném warning 70+ -->
            <a-form-item-rest>
              <div class="headers-table-wrapper">
                <a-button type="primary" size="small" @click="addHeaderRow" style="margin-bottom: 8px;">
                  Thêm
                </a-button>
                <div class="headers-table-container">
                  <a-table
                    :dataSource="headersData"
                    :columns="headersColumns"
                    :pagination="false"
                    size="small"
                    bordered
                  >
                    <template #bodyCell="{ column, record, index }">
                      <template v-if="column.key === 'key'">
                        <a-input v-model:value="record.key" placeholder="Vui lòng nhập khóa" size="small" />
                      </template>
                      <template v-if="column.key === 'value'">
                        <a-input v-model:value="record.value" placeholder="Vui lòng nhập giá trị" size="small" />
                      </template>
                      <template v-if="column.key === 'action'">
                        <a-button type="link" danger size="small" @click="deleteHeaderRow(index)">Xóa</a-button>
                      </template>
                    </template>
                  </a-table>
                </div>
              </div>
            </a-form-item-rest>
          </template>
          <!-- Cấu hình cách ủy quyền (chỉ loại plugin) -->
          <template #authType="{ model, field }">
            <a-select v-model:value="model[field]" @change="onAuthTypeChange">
              <a-select-option value="none">Không cần ủy quyền</a-select-option>
              <a-select-option value="token">Token / API Key</a-select-option>
            </a-select>
          </template>
          <!-- Tên tham số Token (chỉ loại plugin và khi chọn ủy quyền Token) -->
          <template #tokenParamName="{ model, field }">
            <a-input v-model:value="model[field]" placeholder="Vui lòng nhập tên tham số Token" />
          </template>
          <!-- Giá trị tham số Token (chỉ loại plugin và khi chọn ủy quyền Token) -->
          <template #tokenParamValue="{ model, field }">
            <a-input  autocomplete="new-password" v-model:value="model[field]" type="password" placeholder="Vui lòng nhập giá trị Token" />
          </template>
        </BasicForm>
      </div>
    </div>
    <template #footer>
      <a-button @click="close">Đóng</a-button>
      <a-button type="primary" @click="handleSubmit" :loading="submitLoading">Lưu</a-button>
      <a-button v-if="categoryValue === 'mcp'" type="primary" @click="handleSubmitAndSync" :loading="submitLoading">Lưu và đồng bộ</a-button>
    </template>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, computed, nextTick } from 'vue';
import { useModalInner } from '/@/components/Modal';
import BasicModal from '/@/components/Modal/src/BasicModal.vue';
import BasicForm from '/@/components/Form/src/BasicForm.vue';
import { useForm } from '/@/components/Form';
import { saveOrUpdate, saveAndSync } from '../AiragMcp.api';
import { useMessage } from '/@/hooks/web/useMessage';

const emit = defineEmits(['success', 'register']);
const { createMessage } = useMessage();
const isEdit = ref(false);
const submitLoading = ref(false);
const recordId = ref<string | undefined>(undefined);

// Loại category (plugin/mcp)
const categoryValue = ref('mcp');
// Loại MCP (sse/stdio)
const typeValue = ref('sse');
// Cách ủy quyền
const authType = ref('none'); // none: không cần ủy quyền, token: Token / API Key
// Cấu hình Token
const tokenParamName = ref('X-Access-Token');
const tokenParamValue = ref('');

const endpointPlaceholder = computed(() => {
  if (categoryValue.value === 'plugin') {
    return 'Vui lòng nhập BaseURL, ví dụ: https://api.example.com (tùy chọn, không điền thì dùng địa chỉ hệ thống hiện tại)';
  }
  return typeValue.value === 'sse' || typeValue.value === 'http' ? 'Vui lòng nhập URL điểm cuối máy chủ, ví dụ: https://example.com/stream' : 'Vui lòng nhập lệnh khởi động, ví dụ: ./start-mcp-service';
});

// Dữ liệu bảng
const headersData = ref<Array<{ key: string; value: string }>>([]);

// Cấu hình cột bảng
const headersColumns = [
  { title: 'Khóa', dataIndex: 'key', key: 'key', width: '40%' },
  { title: 'Giá trị', dataIndex: 'value', key: 'value', width: '45%' },
  { title: 'Thao tác', key: 'action', width: '15%' },
];

// Thêm dòng
function addHeaderRow() {
  headersData.value.push({ key: '', value: '' });
}

// Xóa dòng
function deleteHeaderRow(index: number) {
  // Nếu chỉ còn một dòng
  if (headersData.value.length <= 1) {
    const lastRow = headersData.value[0];
    // Phán đoán dòng cuối cùng có trống hay không
    if (!lastRow.key && !lastRow.value) {
      // Nếu khóa và giá trị đều trống, không xử lý
      return;
    } else {
      // Nếu không trống, làm trống khóa và giá trị của dòng này
      lastRow.key = '';
      lastRow.value = '';
      createMessage.success('Đã làm trống dữ liệu');
      return;
    }
  }
  headersData.value.splice(index, 1);
}

// Chuyển đổi dữ liệu bảng thành định dạng chuỗi JSON
function headersDataToString(): string {
  const filtered = headersData.value.filter(item => item.key && item.value);
  if (filtered.length === 0) {
    return '';
  }
  const obj = filtered.reduce((acc, item) => {
    acc[item.key] = item.value;
    return acc;
  }, {} as Record<string, string>);
  return JSON.stringify(obj);
}

// Chuyển đổi định dạng chuỗi JSON thành dữ liệu bảng
function stringToHeadersData(str: string | undefined) {
  if (!str) {
    // Mặc định thêm một dòng dữ liệu trống
    headersData.value = [{ key: '', value: '' }];
    return;
  }
  try {
    const obj = JSON.parse(str);
    const entries = Object.entries(obj);
    if (entries.length === 0) {
      // Nếu sau khi phân tích không có dữ liệu, mặc định thêm một dòng dữ liệu trống
      headersData.value = [{ key: '', value: '' }];
    } else {
      headersData.value = entries.map(([key, value]) => ({
        key,
        value: String(value)
      }));
    }
  } catch (e) {
    // Phân tích JSON thất bại, mặc định thêm một dòng dữ liệu trống
    console.error('Phân tích flowData.metadata thất bại', e);
    headersData.value = [{ key: '', value: '' }];
  }
}

function onAuthTypeChange(val: string) {
  authType.value = val;
  updateSchema([
    { field: 'tokenParamName', required: val === 'token', show: val === 'token' },
    { field: 'tokenParamValue', required: false, show: val === 'token' },
  ]);
}

function onCategoryChange(val) {
  categoryValue.value = val.target.value;
  if (categoryValue.value === 'plugin') {
    // Loại plugin: hiển thị BaseURL, header yêu cầu, cách ủy quyền, ẩn lựa chọn loại MCP
    updateSchema([
      { field: 'category', label: 'Loại' },
      { field: 'endpoint', label: 'BaseURL', required: false },
      { field: 'type', show: false },
      { field: 'headers', label: 'Header yêu cầu', show: true },
      { field: 'authType', label: 'Cách ủy quyền', required: true, show: true },
      { field: 'tokenParamName', label: 'Tên tham số Token', required: authType.value === 'token', show: authType.value === 'token' },
      { field: 'tokenParamValue', label: 'Giá trị tham số Token', required: false , show: authType.value === 'token' },
    ]);
    // Đặt giá trị mặc định của loại plugin
    setFieldsValue({
      type: 'api',
      authType: authType.value || 'none',
      tokenParamName: tokenParamName.value || 'X-Access-Token',
    });
  } else {
    // Loại MCP: hiển thị trường vốn có
    updateSchema([
      { field: 'category', label: 'Loại' },
      { field: 'endpoint', label: typeValue.value === 'stdio' ? 'Lệnh' : 'URL', required: true },
      { field: 'type', label: 'Loại MCP', show: true },
      { field: 'headers', label: 'Header yêu cầu', show:() =>typeValue.value !=='stdio'},
      { field: 'authType', show: false },
      { field: 'tokenParamName', show: false },
      { field: 'tokenParamValue', show: false },
    ]);
    // Đặt giá trị mặc định của loại MCP
    setFieldsValue({
      type: typeValue.value || 'sse',
      authType: undefined,
      tokenParamName: undefined,
      tokenParamValue: undefined,
    });
  }
}


// Cấu hình biểu mẫu
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  showActionButtonGroup: false,
  layout: 'vertical',
  baseColProps: { span: 24 },
  schemas: [
    { field: 'name', component: 'Input', label: 'Tên', required: true, componentProps: { placeholder: 'Vui lòng nhập tên' } },
    { field: 'icon', label: 'Biểu tượng', component: 'JImageUpload' },
    { field: 'category', component: 'RadioGroup', label: 'Loại', required: true, slot: 'category', defaultValue: 'mcp' },
    { field: 'type', component: 'Select', label: 'Loại MCP', required: true, slot: 'type', defaultValue: 'sse' },
    { field: 'endpoint', component: 'Input', label: 'URL', required: true, slot: 'endpoint' },
    { field: 'headers', component: 'InputTextArea', label: 'Header yêu cầu', slot: 'headers', show: false },
    { field: 'authType', component: 'Select', label: 'Cách ủy quyền', required: true, slot: 'authType', defaultValue: 'none', show: false },
    { field: 'tokenParamName', component: 'Input', label: 'Tên tham số Token', required: false, slot: 'tokenParamName', defaultValue: 'X-Access-Token', show: false },
    { field: 'tokenParamValue', component: 'Input', label: 'Giá trị tham số Token', required: false, slot: 'tokenParamValue', show: false },
    { field: 'descr', component: 'InputTextArea', label: 'Mô tả', componentProps: { rows: 3, placeholder: 'Vui lòng nhập mô tả' } },
  ]
});

const [registerModal, { closeModal }] = useModalInner(async (data) => {
  await resetFields();
  submitLoading.value = false;
  recordId.value = data?.id;
  if (data && Object.keys(data).length > 0) {
    // Phân biệt thêm mới/Chỉnh sửa
    if (data.id) {
      isEdit.value = true;
    } else {
      isEdit.value = false;
    }
    // Lấy category, mặc định là mcp
    const category = data.category || 'mcp';
    categoryValue.value = category;
    const t = data.type || 'sse';
    typeValue.value = t;
    
    // Phân tích cấu hình ủy quyền (phân tích từ headers hoặc metadata)
    let parsedAuthType = 'none';
    let parsedTokenParamName = 'X-Access-Token';
    let parsedTokenParamValue = '';
    
    if (category === 'plugin') {
      // Thử phân tích cấu hình ủy quyền từ metadata
      if (data.metadata) {
        try {
          const metadata = typeof data.metadata === 'string' ? JSON.parse(data.metadata) : data.metadata;
          parsedAuthType = metadata.authType || 'none';
          parsedTokenParamName = metadata.tokenParamName || 'X-Access-Token';
          parsedTokenParamValue = metadata.tokenParamValue || '';
        } catch (e) {
          // Phân tích thất bại, dùng giá trị mặc định
        }
      }
      
      // Trích xuất token từ headers (nếu tồn tại)
      let headersObj: any = {};
      if (data.headers) {
        try {
          headersObj = typeof data.headers === 'string' ? JSON.parse(data.headers) : data.headers;
          // Nếu trong metadata có cấu hình token, thử trích xuất giá trị tương ứng từ headers
          if (parsedAuthType === 'token' && parsedTokenParamName && headersObj[parsedTokenParamName]) {
            parsedTokenParamValue = headersObj[parsedTokenParamName];
            // Loại bỏ token khỏi headers, tránh hiển thị trong bảng
            delete headersObj[parsedTokenParamName];
          }
        } catch (e) {
          // Phân tích thất bại
        }
      }
      
      authType.value = parsedAuthType;
      tokenParamName.value = parsedTokenParamName;
      tokenParamValue.value = parsedTokenParamValue;
      
      updateSchema([
        { field: 'category', label: 'Loại', componentProps: { disabled: true } },
        { field: 'endpoint', label: 'BaseURL', required: false },
        { field: 'type', show: false },
        { field: 'headers', label: 'Header yêu cầu', show: true },
        { field: 'authType', label: 'Cách ủy quyền', required: true, show: true },
        { field: 'tokenParamName', label: 'Tên tham số Token', required: parsedAuthType === 'token', show: parsedAuthType === 'token' },
        { field: 'tokenParamValue', label: 'Giá trị tham số Token', required: false, show: parsedAuthType === 'token' },
      ]);
      // Chuyển đổi headers đã xử lý (đã loại bỏ token) thành dữ liệu bảng
      stringToHeadersData(Object.keys(headersObj).length > 0 ? JSON.stringify(headersObj) : '');
      
      // Cần đặt giá trị ở tick tiếp theo, đảm bảo updateSchema hoàn thành
      nextTick(() => {
        setFieldsValue({
          icon: data.icon,
          name: data.name,
          descr: data.descr,
          category: category,
          type: t,
          endpoint: data.endpoint,
          authType: parsedAuthType,
          tokenParamName: parsedTokenParamName,
          tokenParamValue: parsedTokenParamValue,
        });
      });
    } else {
      updateSchema([
        { field: 'category', label: 'Loại', componentProps: { disabled: true } },
        { field: 'endpoint', label: t === 'stdio' ? 'Lệnh' : 'URL', required: true },
        { field: 'type', label: 'Loại MCP', show: true },
        { field: 'headers', label: 'Header yêu cầu', show:() => t !== 'stdio' },
        { field: 'authType', show: false },
        { field: 'tokenParamName', show: false },
        { field: 'tokenParamValue', show: false },
      ]);
      // Chuyển đổi chuỗi headers thành dữ liệu bảng
      stringToHeadersData(data.headers);
      
      // Cần đặt giá trị ở tick tiếp theo, đảm bảo updateSchema hoàn thành
      nextTick(() => {
        setFieldsValue({
          icon: data.icon,
          name: data.name,
          descr: data.descr,
          category: category,
          type: t,
          endpoint: data.endpoint,
        });
      });
    }
  } else {
    isEdit.value = false;
    categoryValue.value = 'mcp';
    typeValue.value = 'sse';
    authType.value = 'none';
    tokenParamName.value = 'X-Access-Token';
    tokenParamValue.value = '';
    // Mặc định thêm một dòng dữ liệu trống
    headersData.value = [{ key: '', value: '' }];
    updateSchema([
      { field: 'category', label: 'Loại' },
      { field: 'endpoint', label: 'URL' },
      { field: 'headers', label: 'Header yêu cầu', show: true },
      { field: 'authType', show: false },
      { field: 'tokenParamName', show: false },
      { field: 'tokenParamValue', show: false },
    ]);
    // Đặt giá trị chọn mặc định
    setFieldsValue({ category: 'mcp', type: 'sse', authType: 'none', tokenParamName: 'X-Access-Token', tokenParamValue: '' });
  }
});

/**
 * Sự kiện thay đổi loại
 * @param val
 */
function onTypeChange(val: string){
  typeValue.value = val;
  updateSchema([
    { field: 'endpoint', label: val === 'stdio' ? 'Lệnh' : 'URL' },
    { field: 'headers', label: 'Header yêu cầu', show:() => val !== 'stdio' },
  ]);
}

async function handleSubmit(){
  try {
    submitLoading.value = true;
    const values = await validate();
    if(recordId.value){
      values.id = recordId.value;
    }
    
    if (values.category === 'plugin') {
      // Loại plugin: kiểm chứng cấu hình ủy quyền
      if (values.authType === 'token') {
        if (!values.tokenParamName) {
          createMessage.error('Cách ủy quyền Token cần điền tên tham số Token và giá trị tham số');
          return;
        }
      }
      
      // Loại plugin: xử lý headers và cấu hình ủy quyền
      // Hợp nhất headers và cấu hình token
      let headersObj = {};
      const headersStr = headersDataToString();
      if (headersStr) {
        try {
          headersObj = JSON.parse(headersStr);
        } catch (e) {
          headersObj = {};
        }
      }
      
      // Nếu đã chọn ủy quyền Token, thêm token vào headers
      if (values.authType === 'token' && values.tokenParamName && values.tokenParamValue) {
        headersObj[values.tokenParamName] = values.tokenParamValue;
      }
      
      values.headers = Object.keys(headersObj).length > 0 ? JSON.stringify(headersObj) : '';
      
      // Lưu cấu hình ủy quyền vào metadata
      const metadata: any = {};
      if (values.metadata) {
        try {
          Object.assign(metadata, typeof values.metadata === 'string' ? JSON.parse(values.metadata) : values.metadata);
        } catch (e) {
          // Phân tích thất bại, dùng đối tượng trống
        }
      }
      metadata.authType = values.authType || 'none';
      if (values.authType === 'token') {
        metadata.tokenParamName = values.tokenParamName || 'X-Access-Token';
        metadata.tokenParamValue = values.tokenParamValue || '';
      }
      values.metadata = JSON.stringify(metadata);
      
      values.type = 'api';
    } else {
      // Loại MCP: chuyển đổi dữ liệu bảng thành chuỗi
      values.headers = headersDataToString();
      // Làm trống các trường liên quan đến ủy quyền
      delete values.authType;
      delete values.tokenParamName;
      delete values.tokenParamValue;
    }
    
    const res = await saveOrUpdate(values);
    if(res.success){
      createMessage.success(res.message || 'Lưu thành công');
      emit('success');
      closeModal();
    }else{
      createMessage.error(res.message || 'Lưu thất bại');
    }
  }finally{
    submitLoading.value = false;
  }
}

async function handleSubmitAndSync(){
  try {
    submitLoading.value = true;
    const values = await validate();
    if(recordId.value){
      values.id = recordId.value;
    }
    // Chuyển đổi dữ liệu bảng thành chuỗi
    values.headers = headersDataToString();
    const res = await saveAndSync(values);
    if(res.success){
      createMessage.success(res.message || 'Lưu và đồng bộ thành công');
      emit('success');
      closeModal();
    }else{
      createMessage.error(res.message || 'Lưu và đồng bộ thất bại');
    }
  }finally{
    submitLoading.value = false;
  }
}

function close(){
  closeModal();
}
</script>
<style scoped lang="less">
.modal {
  padding: 5px 16px 8px 16px;
  .header {
    padding: 0 0 12px 0;
    display: flex;
    justify-content: space-between;
    .header-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
  .mcp-content {
    :deep(.ant-form-item) { margin-bottom: 8px; }
    :deep(.ant-input),
    :deep(.ant-input-number),
    :deep(.ant-select),
    :deep(.ant-select-selector),
    :deep(.ant-textarea),
    :deep(textarea.ant-input) { width: 100% !important; }
    :deep(.ant-select-selector){ padding: 0 8px; }
    
    .headers-table-wrapper {
      .headers-table-container {
        max-height: 200px;
        overflow-y: auto;
        border: 1px solid #d9d9d9;
        border-radius: 2px;
        
        :deep(.ant-table) {
          .ant-table-thead > tr > th {
            background: #fafafa;
            padding: 8px;
          }
          .ant-table-tbody > tr > td {
            padding: 4px 8px;
          }
        }
      }
    }
  }
}
</style>
<style lang="less">
.airag-mcp-modal {
  .jeecg-basic-modal-close > span { margin-left: 0 !important; }
  .ant-modal {
    max-height: 85vh;
    .ant-modal-body {
      max-height: calc(85vh - 110px);
      overflow-y: auto;
    }
  }
}
</style>
