<template>
  <BasicModal
    destroyOnClose
    @register="registerModal"
    :canFullscreen="false"
    width="1400px"
    :title="isEdit ? 'Chỉnh sửa công cụ' : 'Thêm mới công cụ'"
    wrapClassName="plugin-tool-edit-modal"
  >
    <div class="modal">
      <div class="tool-edit-content">
        <!-- Thông tin cơ bản -->
        <div class="section">
          <h3 class="section-title">Thông tin cơ bản</h3>
          <a-form :model="toolForm" layout="vertical">
            <a-row :gutter="24">
              <a-col :span="12">
                <a-form-item label="Tên công cụ" required>
                  <a-input v-model:value="toolForm.name" placeholder="Vui lòng nhập tên công cụ" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="Phương thức yêu cầu" required>
                  <a-select v-model:value="toolForm.method" placeholder="Vui lòng chọn phương thức yêu cầu">
                    <a-select-option value="GET">GET</a-select-option>
                    <a-select-option value="POST">POST</a-select-option>
                    <a-select-option value="PUT">PUT</a-select-option>
                    <a-select-option value="DELETE">DELETE</a-select-option>
                    <a-select-option value="PATCH">PATCH</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="24">
                <a-form-item label="Đường dẫn truy cập" required>
                  <a-input v-model:value="toolForm.path" placeholder="Vui lòng nhập đường dẫn truy cập, ví dụ:/api/user/{userId}" @blur="normalizePath" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-row :gutter="24">
              <a-col :span="24">
                <a-form-item label="Mô tả" required>
                  <a-textarea v-model:value="toolForm.description" :rows="3" placeholder="Vui lòng nhập mô tả công cụ" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </div>

        <!-- Tham số yêu cầu -->
        <div class="section">
          <div class="section-header">
            <h3 class="section-title">Tham số yêu cầu</h3>
            <a-button type="primary" size="small" @click="handleAddRequestParam">Thêm tham số</a-button>
          </div>
          <a-table
            :dataSource="requestParams"
            :columns="requestParamsColumns"
            :pagination="false"
            bordered
            size="small"
            rowKey="tempId"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'name'">
                <a-input
                  v-model:value="record.name"
                  placeholder="Tên tham số (chữ cái, chữ số, gạch dưới)"
                  @blur="validateParamName(record)"
                />
              </template>
              <template v-else-if="column.key === 'description'">
                <a-input v-model:value="record.description" placeholder="Mô tả tham số" />
              </template>
              <template v-else-if="column.key === 'type'">
                <a-select v-model:value="record.type" style="width: 100%">
                  <a-select-option value="String">String</a-select-option>
                  <a-select-option value="Number">Number</a-select-option>
                  <a-select-option value="Integer">Integer</a-select-option>
                  <a-select-option value="Boolean">Boolean</a-select-option>
                </a-select>
              </template>
              <template v-else-if="column.key === 'location'">
                <a-select v-model:value="record.location" style="width: 100%" @change="onLocationChange(record)">
                  <a-select-option value="Body">Raw(json)</a-select-option>
                  <a-select-option value="Form-Data">Form-Data</a-select-option>
                  <a-select-option value="Query">Query</a-select-option>
                  <a-select-option value="Header">Header</a-select-option>
                  <a-select-option value="Path">Path</a-select-option>
                </a-select>
              </template>
              <template v-else-if="column.key === 'required'">
                <a-checkbox v-model:checked="record.required" />
              </template>
              <template v-else-if="column.key === 'defaultValue'">
                <a-input v-model:value="record.defaultValue" placeholder="Giá trị mặc định" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" danger size="small" @click="handleDeleteRequestParam(index)">Xóa</a-button>
              </template>
            </template>
          </a-table>
        </div>

        <!-- Tham số đầu ra -->
        <div class="section">
          <div class="section-header">
            <h3 class="section-title">Tham số đầu ra</h3>
            <a-button type="primary" size="small" @click="handleAddResponseParam">Thêm tham số</a-button>
          </div>
          <a-table
            :dataSource="responseParams"
            :columns="responseParamsColumns"
            :pagination="false"
            bordered
            size="small"
            rowKey="tempId"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'name'">
                <a-input
                  v-model:value="record.name"
                  placeholder="Tên tham số (hỗ trợ cú pháp dấu chấm và mảng, ví dụ data.name, data[].name)"
                  @blur="validateResponseParamName(record)"
                />
              </template>
              <template v-else-if="column.key === 'description'">
                <a-input v-model:value="record.description" placeholder="Mô tả tham số" />
              </template>
              <template v-else-if="column.key === 'type'">
                <a-select v-model:value="record.type" style="width: 100%" @change="onResponseTypeChange(record)">
                  <a-select-option value="String">String</a-select-option>
                  <a-select-option value="Number">Number</a-select-option>
                  <a-select-option value="Integer">Integer</a-select-option>
                  <a-select-option value="Boolean">Boolean</a-select-option>
                  <a-select-option value="Object">Object</a-select-option>
                  <a-select-option value="Array">Array</a-select-option>
                </a-select>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-button type="link" danger size="small" @click="handleDeleteResponseParam(index)">Xóa</a-button>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>
    <template #footer>
      <a-button @click="close">Đóng</a-button>
      <a-button type="primary" @click="handleSave" :loading="submitLoading">Lưu</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, reactive } from 'vue';
import { useModalInner } from '/@/components/Modal';
import BasicModal from '/@/components/Modal/src/BasicModal.vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { queryById, saveTools } from '../AiragMcp.api';

const emit = defineEmits(['success', 'register']);
const { createMessage } = useMessage();
const submitLoading = ref(false);
const pluginId = ref<string | undefined>(undefined);
const isEdit = ref(false);
const toolIndex = ref<number>(-1); // Chỉ số của công cụ trong mảng khi chỉnh sửa

// Biểu mẫu công cụ
const toolForm = reactive({
  name: '',
  description: '',
  path: '',
  method: 'GET',
});

// Danh sách tham số yêu cầu
const requestParams = ref<any[]>([]);
// Danh sách tham số đầu ra
const responseParams = ref<any[]>([]);

// Cột bảng tham số yêu cầu
const requestParamsColumns = [
  { title: 'Tên tham số', key: 'name', width: 150 },
  { title: 'Mô tả tham số', key: 'description', width: 200 },
  { title: 'Loại tham số', key: 'type', width: 120 },
  { title: 'Cách truyền vào', key: 'location', width: 150 },
  { title: 'Có bắt buộc hay không', key: 'required', width: 100 },
  { title: 'Giá trị mặc định', key: 'defaultValue', width: 150 },
  { title: 'Thao tác', key: 'action', width: 100 },
];

// Cột bảng tham số đầu ra
const responseParamsColumns = [
  { title: 'Tên tham số', key: 'name', width: 200 },
  { title: 'Mô tả tham số', key: 'description', width: 200 },
  { title: 'Loại tham số', key: 'type', width: 150 },
  { title: 'Thao tác', key: 'action', width: 100 },
];

let tempIdCounter = 0;

const [registerModal, { closeModal }] = useModalInner(async (data) => {
  pluginId.value = data?.pluginId;
  isEdit.value = !!data?.tool;
  
  // Đặt lại biểu mẫu
  toolForm.name = '';
  toolForm.description = '';
  toolForm.path = '';
  toolForm.method = 'GET';
  requestParams.value = [];
  responseParams.value = [];
  toolIndex.value = -1;
  
  if (data?.tool) {
    // Chế độ chỉnh sửa: điền dữ liệu
    toolForm.name = data.tool.name || '';
    toolForm.description = data.tool.description || '';
    toolForm.path = data.tool.path || '';
    toolForm.method = data.tool.method || 'GET';
    
    requestParams.value = (data.tool.parameters || []).map((p: any) => ({
      ...p,
      tempId: `req_${tempIdCounter++}`,
      required: p.required !== undefined ? p.required : false,
    }));
    
    responseParams.value = (data.tool.responses || []).map((r: any) => ({
      ...r,
      tempId: `resp_${tempIdCounter++}`,
    }));
    
    // Tìm chỉ số của công cụ trong danh sách
    if (pluginId.value) {
      const res = await queryById(pluginId.value);
      const detail = res.result || res;
      if (detail.tools) {
        try {
          const tools = typeof detail.tools === 'string' ? JSON.parse(detail.tools) : detail.tools;
          toolIndex.value = tools.findIndex((t: any) => t.name === data.tool.name);
        } catch (e) {
          toolIndex.value = -1;
        }
      }
    }
  }
});

function validateParamName(record: any) {
  // Kiểm chứng tên tham số: chỉ cho phép chữ cái, chữ số, gạch dưới
  if (record.name && !/^[a-zA-Z0-9_]+$/.test(record.name)) {
    createMessage.warning('Tên tham số chỉ có thể chứa chữ cái, chữ số và gạch dưới');
    record.name = record.name.replace(/[^a-zA-Z0-9_]/g, '');
  }
}

function validateResponseParamName(record: any) {
  // Kiểm chứng tên tham số đầu ra: hỗ trợ chữ cái, chữ số, gạch dưới, dấu chấm, dấu ngoặc vuông
  // Chỉ loại Object và Array mới có thể dùng dấu chấm và dấu ngoặc vuông
  if (!record.name) return;
  
  const type = record.type;
  const name = record.name;
  
  if (type === 'Object' || type === 'Array') {
    // Loại Object và Array: hỗ trợ chữ cái, chữ số, gạch dưới, dấu chấm, dấu ngoặc vuông
    // Ví dụ: data.name, data[String], data[].name
    if (!/^[a-zA-Z0-9_.\[\]]+$/.test(name)) {
      createMessage.warning('Tên tham số chỉ có thể chứa chữ cái, chữ số, gạch dưới, dấu chấm và dấu ngoặc vuông');
      record.name = name.replace(/[^a-zA-Z0-9_.\[\]]/g, '');
    }
  } else {
    // Loại khác: chỉ cho phép chữ cái, chữ số, gạch dưới
    if (!/^[a-zA-Z0-9_]+$/.test(name)) {
      createMessage.warning('Không phải Object/Tên tham số loại Array chỉ có thể chứa chữ cái, chữ số và gạch dưới');
      record.name = name.replace(/[^a-zA-Z0-9_]/g, '');
    }
  }
}

function onResponseTypeChange(record: any) {
  // Khi loại thay đổi, nếu loại không phải Object hoặc Array, xóa dấu chấm và dấu ngoặc vuông
  if (record.type !== 'Object' && record.type !== 'Array') {
    if (record.name && /[.\[\]]/.test(record.name)) {
      createMessage.warning('Không phải Object/Loại Array không hỗ trợ dùng dấu chấm và dấu ngoặc vuông');
      record.name = record.name.replace(/[.\[\]]/g, '');
    }
  }
}

function onLocationChange(record: any) {
  // Body và Form-Data có thể tồn tại đồng thời, không cảnh báo nữa
  // Khi tồn tại đồng thời, backend mặc định dùng Body
}

function handleAddRequestParam() {
  requestParams.value.push({
    tempId: `req_${tempIdCounter++}`,
    name: '',
    description: '',
    type: 'String',
    location: 'Body',
    required: false,
    defaultValue: '',
  });
}

function handleDeleteRequestParam(index: number) {
  requestParams.value.splice(index, 1);
}

function handleAddResponseParam() {
  responseParams.value.push({
    tempId: `resp_${tempIdCounter++}`,
    name: '',
    description: '',
    type: 'String',
  });
}

function handleDeleteResponseParam(index: number) {
  responseParams.value.splice(index, 1);
}

async function handleSave() {
  try {
    submitLoading.value = true;
    
    // Kiểm chứng trường cơ bản
    if (!toolForm.name || !toolForm.description || !toolForm.path || !toolForm.method) {
      createMessage.warning('Vui lòng điền đầy đủ thông tin cơ bản của công cụ');
      return;
    }

    // Kiểm chứng biểu thức chính quy name ^[a-zA-Z0-9_-]+$
    if (!/^[a-zA-Z0-9_-]+$/.test(toolForm.name)) {
      createMessage.warning('Tên công cụ chỉ có thể chứa chữ cái, chữ số, gạch dưới và gạch nối');
      return;
    }

    // Body và Form-Data có thể tồn tại đồng thời, backend mặc định dùng Body
    
    if (!pluginId.value) {
      createMessage.error('ID plugin không tồn tại');
      return;
    }
    
    // Tải dữ liệu plugin
    const res = await queryById(pluginId.value);
    const detail = res.result || res;
    
    // Phân tích danh sách công cụ hiện có
    let tools: any[] = [];
    if (detail.tools) {
      try {
        const parsedTools = typeof detail.tools === 'string' ? JSON.parse(detail.tools) : detail.tools;
        // Đảm bảo là mảng
        if (Array.isArray(parsedTools)) {
          tools = [...parsedTools]; // Sao chép mảng, tránh vấn đề tham chiếu
        } else {
          tools = [];
        }
      } catch (e) {
        console.error('Phân tích danh sách công cụ thất bại:', e);
        tools = [];
      }
    }
    
    // Đảm bảo tools là mảng
    if (!Array.isArray(tools)) {
      tools = [];
    }
    
    // Xây dựng dữ liệu công cụ hiện tại (loại bỏ tempId)
    const parameters = requestParams.value.map(p => {
      const { tempId: _tempId, ...param } = p;
      return param;
    });
    const responses = responseParams.value.map(r => {
      const { tempId: _tempId, ...resp } = r;
      return resp;
    });
    
    const toolData = {
      name: toolForm.name,
      description: toolForm.description,
      path: toolForm.path,
      method: toolForm.method,
      enabled: true, // Mặc định kích hoạt
      parameters,
      responses,
    };
    
    // Xử lý công cụ theo trạng thái chỉnh sửa
    if (isEdit.value && toolIndex.value >= 0 && toolIndex.value < tools.length) {
      // Chế độ chỉnh sửa: cập nhật công cụ hiện có
      tools[toolIndex.value] = toolData;
    } else {
      // Chế độ thêm mới: thêm công cụ mới
      // Kiểm tra tên công cụ đã tồn tại hay chưa
      const nameExists = tools.some((t: any) => t.name === toolForm.name);
      if (nameExists) {
        createMessage.error('Tên công cụ đã tồn tại, vui lòng dùng tên khác');
        return;
      }
      tools.push(toolData);
    }
    
    // Xây dựng chuỗi JSON danh sách công cụ
    const toolsJson = JSON.stringify(tools);
    
    // Gọi giao diện lưu công cụ
    const saveRes = await saveTools(pluginId.value, toolsJson);
    if (saveRes.success) {
      createMessage.success('Lưu thành công');
      emit('success');
      closeModal();
    } else {
      createMessage.error(saveRes.message || 'Lưu thất bại');
    }
  } finally {
    submitLoading.value = false;
  }
}

function close() {
  closeModal();
}

// Chuẩn hóa đường dẫn: đảm bảo bắt đầu bằng/Mở đầu
function normalizePath() {
  if (toolForm.path && !toolForm.path.startsWith('/')) {
    toolForm.path = '/' + toolForm.path;
  }
}
</script>

<style scoped lang="less">
.modal {
  padding: 16px;
  .tool-edit-content {
    .section {
      margin-bottom: 24px;
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
      }
      .section-title {
        margin: 0;
        font-size: 16px;
        font-weight: bold;
      }
      :deep(.ant-table) {
        .ant-table-thead > tr > th {
          background: #fafafa;
          padding: 8px;
        }
        .ant-table-tbody > tr > td {
          padding: 8px;
        }
      }
    }
  }
}
</style>

<style lang="less">
.plugin-tool-edit-modal {
  .jeecg-basic-modal-close > span { margin-left: 0 !important; }
  .ant-modal {
    max-height: 90vh;
    .ant-modal-body {
      max-height: calc(90vh - 110px);
      overflow-y: auto;
    }
  }
}
</style>

