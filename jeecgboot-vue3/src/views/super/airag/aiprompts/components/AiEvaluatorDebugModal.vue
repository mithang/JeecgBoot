<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :canFullscreen="true"
    defaultFullscreen
    destroyOnClose
    title="Gỡ lỗi đánh giá"
    :showOkBtn="false"
    :showCancelBtn="false"
    wrapClassName="evaluator-debug-modal"
  >
    <!-- Thanh bước -->
    <a-steps :current="currentStep" class="steps-container">
      <a-step title="Chọn prompt">
        <template #icon>
          <Icon icon="ant-design:file-search-outlined" />
        </template>
      </a-step>
      <a-step title="Cấu hình gỡ lỗi">
        <template #icon>
          <Icon icon="ant-design:setting-outlined" />
        </template>
      </a-step>
    </a-steps>

    <!-- Bước một: chọn prompt và ánh xạ trường -->
    <div v-show="currentStep === 0" class="step-content step-one">
      <div class="form-section">
        <div class="form-item">
          <label class="form-label">Chọn prompt:</label>
          <a-select v-model:value="selectedPromptKey" placeholder="Vui lòng chọn prompt" class="prompt-select" @change="onPromptChange">
            <a-select-option v-for="prompt in promptOptions" :key="prompt.key" :value="prompt.key">
              <span class="prompt-option">
                <Icon icon="ant-design:file-text-outlined" />
                {{ prompt.name }}
              </span>
            </a-select-option>
          </a-select>
        </div>

        <div class="mapping-section" v-if="selectedPromptKey">
          <div class="section-header">
            <h3>
              <Icon icon="ant-design:link-outlined" />
              Quan hệ ánh xạ trường
            </h3>
          </div>
          <div class="mapping-list">
            <div class="mapping-item">
              <div class="field-label">
                <Icon icon="ant-design:tag-outlined" />
                Người dùng nhập[user_query]
              </div>
              <div class="equal-sign">=</div>
              <a-select allow-clear v-model:value="fieldMappings['user_query']" placeholder="Vui lòng chọn trường tập dữ liệu" class="field-select">
                <a-select-option v-for="column in datasetColumns" :key="column.key" :value="column.key">
                  <span class="column-option">
                    <span style="color:blue">Tập đánh giá</span>
                    <span> [{{ column.title }}]</span>
                    <span style="margin-left: 5px">
                      <a-tooltip :title="column.description"><Icon icon="ant-design:info-circle-outlined" size="14" /></a-tooltip>
                    </span>
                  </span>
                </a-select-option>
              </a-select>
            </div>
            <div class="mapping-item" v-for="(item, index) in promptParams" :key="index">
              <div class="field-label">
                <Icon icon="ant-design:tag-outlined" />
                Biến[{{ item.name }}]
              </div>
              <div class="equal-sign">=</div>
              <a-select allow-clear v-model:value="fieldMappings[item.name]" placeholder="Vui lòng chọn trường tập dữ liệu" class="field-select">
                <a-select-option v-for="column in datasetColumns" :key="column.key" :value="column.key">
                  <span class="column-option">
                    <span style="color:blue">Tập đánh giá</span>
                    <span> [{{ column.title }}]</span>
                    <span style="margin-left: 5px">
                      <a-tooltip :title="column.description"><Icon icon="ant-design:info-circle-outlined" size="14" /></a-tooltip>
                    </span>
                  </span>
                </a-select-option>
              </a-select>
            </div>
          </div>
        </div>
      </div>

      <div class="form-footer">
        <a-button @click="closeModal" class="footer-btn cancel-btn">
          <Icon icon="ant-design:close-circle-outlined" />
          Hủy
        </a-button>
        <a-button type="primary" @click="nextStep" :disabled="!selectedPromptKey || !fieldMappings['user_query']" class="footer-btn next-btn">
          <Icon icon="ant-design:right-circle-outlined" />
          Bước tiếp theo
        </a-button>
      </div>
    </div>

    <!-- Bước hai: cấu hình gỡ lỗi và hiển thị kết quả -->
    <div v-show="currentStep === 1" class="step-content step-two">
      <div class="form-section">
        <div class="mapping-section">
          <div class="section-header">
            <h3>
              <Icon icon="ant-design:link-outlined" />
              Ánh xạ trường
            </h3>
          </div>
          <div class="mapping-list">
            <div class="mapping-item" v-for="(item, index) in evaluatorFields" :key="index">
              <div class="field-label">
                <Icon icon="ant-design:tag-outlined" />
                Bộ đánh giá [{{ item.field }}]
              </div>
              <div class="equal-sign">=</div>
              <a-select allow-clear v-model:value="fieldMappings[item.field]" placeholder="Vui lòng chọn trường tập dữ liệu" class="field-select">
                <a-select-option v-for="column in evaluatorColumns" :key="column.name" :value="column.name">
                  <span class="column-option">
<!--                    <a-divider style="margin:4px 0;" v-if="column.label" />-->
                    <span :style="{color:column.label ? 'green' : 'blue'}"> [{{ column.label || 'Tập đánh giá' }}] </span>
                    <span>{{ column.name }}</span>
                    <span style="margin-left: 5px">
                      <a-tooltip :title="column.description">
                        <Icon icon="ant-design:info-circle-outlined" size="14"/>
                      </a-tooltip>
                    </span>
                  </span>
                </a-select-option>
              </a-select>
            </div>
          </div>
        </div>

        <div class="debug-result" v-if="debugResult.length > 0 || confirmLoading">
          <div class="section-header">
            <h3>
              <Icon icon="ant-design:bar-chart-outlined" />
              Kết quả gỡ lỗi
            </h3>
          </div>
          <div class="debug-result-container">
            <!-- Hiển thị có điều kiện, khi có kết quả gỡ lỗi thì hiển thị bảng -->
            <div class="result-table-container" v-if="debugResult.length > 0">
              <!-- Thêm container bên ngoài và tiêu đề -->
              <div class="table-header">
                <div class="table-title" v-if="!confirmLoading">Kết quả gỡ lỗi</div>
                <div class="table-title"  v-if="confirmLoading"><span>Thử nghiệm đang khởi tạo, vui lòng nhấp sau<a href="javascript:void(0)" @click="handleReload">Làm mới</a></span></div>
                <div class="table-subtitle">So sánh đầu ra thực tế với đầu ra tham khảo</div>
              </div>

              <!-- Khu vực bảng -->
              <a-table
                :columns="resultColumns"
                :dataSource="debugResult"
                :pagination="false"
                :scroll="{ x: 'max-content' }"
                class="result-table"
                :rowClassName="setRowClassName"
              >
                <!-- Hiển thị ô tùy chỉnh -->
                <template #bodyCell="{ column, record, text }">
                  <!-- Kiểu đặc biệt cho cột trạng thái -->
                  <template v-if="column.dataIndex === 'status'">
                    <span :class="['status-badge', getStatusClass(record)]">
                      {{ text }}
                    </span>
                  </template>
                </template>
              </a-table>
            </div>

            <!-- Khi không có dữ liệu hiển thị đang tải hoặc gợi ý -->
            <div v-else class="no-data-container">
              <div class="loading-text" v-if="confirmLoading">
                <loading-outlined style="font-size: 24px; margin-right: 8px" @click="handleReload" />
                <span>Thử nghiệm đang khởi tạo, vui lòng nhấp sau<a href="javascript:void(0)" @click="handleReload">Làm mới</a></span>
              </div>
              <div class="empty-text" v-else> Tạm thời chưa có kết quả gỡ lỗi </div>
            </div>
          </div>
        </div>
      </div>

      <div class="form-footer">
        <a-button @click="prevStep" class="footer-btn prev-btn">
          <Icon icon="ant-design:left-circle-outlined" />
          Bước trước
        </a-button>
        <a-button v-if="debugResult.length == 0"  :disabled="canConfirmDebug" :loading="confirmLoading" type="primary" @click="confirmDebug" class="footer-btn confirm-btn">
          <Icon v-if="!confirmLoading" icon="ant-design:play-circle-outlined" />
          {{ confirmLoading ? 'Đang gỡ lỗi...' : 'Xác nhận cấu hình gỡ lỗi' }}
        </a-button>
        <a-button v-if="debugResult.length > 0" @click="closeModal" class="footer-btn close-btn">
          <Icon icon="ant-design:check-circle-outlined" />
          Hoàn thành
        </a-button>
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, reactive,computed  } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import Icon from '/@/components/Icon/src/Icon.vue';
  import { list, promptExperiment } from '../AiragPrompts.api';
  import { useMessage } from '@/hooks/web/useMessage';
  import { queryTrackById } from '@/views/super/airag/aiprompts/AiragExtData.api';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  // Xác nhận trạng thái tải cấu hình gỡ lỗi
  const confirmLoading = ref(false);
  // Thông tin gợi ý
  const { createMessage } = useMessage();
  //Dữ liệu cốt lõi
  const record = ref<any>({});
  // Kiểm soát bước
  const currentStep = ref(0);
  // Chọn prompt để gỡ lỗi
  const selectedPromptKey = ref<string>('');
  const selectedPrompt = ref<any>({});
  // Cấu hình trường của bộ đánh giá
  const evaluatorFields = ref<any>([]);
  // Cấu hình cột thả xuống của bộ đánh giá
  const evaluatorColumns = ref<any>([]);
  //Ánh xạ trường
  const fieldMappings = reactive<Record<string, string>>({});
  //Cấu hình số trang
  const page = ref({
    pageNo: 1,
    pageSize: 10,
  });
  // Dữ liệu tùy chọn prompt
  const promptOptions = ref<any[]>([]);
  // Cột tập dữ liệu
  const datasetColumns = ref<any[]>([]);
  // Tham số prompt
  const promptParams = ref<any[]>([]);
  // Kết quả gỡ lỗi
  const debugResult = ref<any[]>([]);
  // Cột bảng kết quả
  const resultColumns = ref<any[]>([
    { title: 'Câu hỏi', dataIndex: 'userQuery', key: 'userQuery',fixed: 'left' },
    { title: 'Prompt xuất đáp án', dataIndex: 'promptAnswer', key: 'promptAnswer',fixed: 'left' },
    { title: 'Chấm điểm', dataIndex: 'answerScore', key: 'answerScore',fixed: 'left' },
  ]);

  // Xác nhận cấu hình gỡ lỗi
  const canConfirmDebug = computed(() => {
    let canConfirm = true;
    evaluatorFields.value.forEach((field) => {
      if(!fieldMappings[field.name]){
        canConfirm = false;
      }
    });
    return canConfirm;
  });
  // Gán giá trị biểu mẫu
  const [registerModal, { closeModal }] = useModalInner(async (data) => {
    //Dữ liệu gỡ lỗi
    record.value = data.record;
    //Đặt lại dữ liệu
    resetForm();
    //Truy vấn prompt
    await getPromptList();
    //Truy vấn cấu hình cột trắc nghiệm
    getDatasetColumns();
    //Truy vấn cấu hình trường của bộ đánh giá
    getEvaluatorFields();
  });

  /**
   * Lấy trường cột của tập dữ liệu
   */
  function getDatasetColumns() {
    let datasetValue = record.value?.datasetValue;
    if (datasetValue) {
      let columns = JSON.parse(datasetValue).columns;
      columns.forEach((item) => {
        if(item.name !== 'action') {
          datasetColumns.value.push({ ...item, key: item.name, title: item.name });
          evaluatorColumns.value.push({ ...item });
          if(!resultColumns.value.some(a=> a.dataIndex === item.name)){
            resultColumns.value.push({ title: item.name, dataIndex: item.name, key: item.name });
          }
        }
      });
    } else {
      createMessage.warning('Chưa cấu hình thông tin tập đánh giá!');
    }
  }
  /**
   * Lấy trường cột của tập dữ liệu
   */
  function getEvaluatorFields() {
    let dataValue = record.value?.dataValue;
    if (dataValue) {
      evaluatorFields.value = dataValue.match(/{{\s*([^}\s]+)\s*}}/g).map((match) => ({ field: match.replace(/{{\s*|\s*}}/g, '') }));
    } 
    //Cấu hình cột
    evaluatorColumns.value.push({ name: 'actual_output', label: 'Đối tượng đánh giá', description: 'Đầu ra thực tế', dateType: 'String' });
  }

  /**
   * Lấy prompt
   */
  async function getPromptList() {
    const res = await list({ ...page });
    if (res?.records) {
      res?.records.forEach((item) => {
        promptOptions.value.push({ name: item.name, key: item.id, value: item.id, ...item });
      });
    }
  }
  // Đặt lại biểu mẫu
  function resetForm() {
    currentStep.value = 0;
    selectedPromptKey.value = '';
    promptOptions.value = [];
    datasetColumns.value = [];
    promptParams.value = [];
    evaluatorColumns.value = [];
    Object.keys(fieldMappings).forEach((key) => {
      delete fieldMappings[key];
    });
    debugResult.value = [];
  }

  // Sự kiện thay đổi prompt
  function onPromptChange(value: string) {
    //Thông tin prompt đã chọn
    selectedPrompt.value = promptOptions.value.find((item) => item.value == value);
    // Làm trống ánh xạ trước đó
    Object.keys(fieldMappings).forEach((key) => {
      delete fieldMappings[key];
    });
    let modelParam = selectedPrompt.value?.modelParam;
    if(modelParam){
      modelParam = typeof modelParam === 'string' ? JSON.parse(modelParam) : modelParam;
      if(modelParam?.promptVariables && modelParam?.promptVariables.length > 0){
        modelParam.promptVariables.forEach((item) => {
          promptParams.value.push({ name: item.name, label: item.name, description: item.description, dateType: 'String' });
        });
      }
    }
  }

  // Bước tiếp theo
  function nextStep() {
    if (selectedPromptKey.value) {
      currentStep.value = 1;
    }
  }

  // Bước trước
  function prevStep() {
    confirmLoading.value = false;
    debugResult.value = [];
    currentStep.value = 0;
  }

  // Xác nhận cấu hình gỡ lỗi
  async function confirmDebug() {
    // Mô phỏng quá trình gỡ lỗi
    let params = {
      mappings: fieldMappings,
      promptKey: selectedPromptKey.value,
      extDataId: record.value.id,
    };
    console.log('Bắt đầu gỡ lỗi...', params);
    confirmLoading.value = true;
    let res = await promptExperiment(params);
    console.log('Kết thúc gỡ lỗi res...', res);
    emit('success');
  }

  // Làm mới tập đánh giá
  async function handleReload() {
    debugResult.value = []
    let res = await queryTrackById({ id: record.value.id });
    console.log('Làm mới tập đánh giá res...', res);
    if (!res.success) {
      createMessage.error(res.message);
      return;
    } else {
      if (res.result && res.result.length > 0) {
        console.log('Làm mới tập đánh giá res.result...', res.result);
        confirmLoading.value = false;
        // 1. Trước tiên tìm ra giá trị version lớn nhất
        const maxVersion = Math.max(...res.result.map(item => item.version));

        // 2. Lọc ra tất cả các mục có version lớn nhất
        const maxVersionItems = res.result.filter(item => item.version === maxVersion);
       
        console.log('Làm mới tập đánh giá maxVersionItems...', maxVersionItems);
        if (maxVersionItems.length > 0) {
          maxVersionItems.forEach((item) => {
            debugResult.value.push(JSON.parse(item.dataValue));
          });
        }
      }else{
        createMessage.warning('Đang xử lý dữ liệu, vui lòng làm mới sau!');
      }
    }
  }

  // Lớp kiểu trạng thái — trạng thái do LLM trả về, chấp nhận cả giá trị tiếng Trung (gốc) và tiếng Việt (sau khi dịch prompt đánh giá)
  const getStatusClass = (record) => {
    if (record.status === '成功' || record.status === 'Thành công') return 'status-success';
    if (record.status === '失败' || record.status === 'Thất bại') return 'status-failed';
    if (record.status === '警告' || record.status === 'Cảnh báo') return 'status-warning';
    return '';
  };

  // Kiểu dòng
  const setRowClassName = (record, index) => {
    return index % 2 === 0 ? 'even-row' : 'odd-row';
  };
</script>

<style lang="less" scoped>
  @primary-color: #1890ff;
  @success-color: #52c41a;
  @warning-color: #faad14;
  @error-color: #ff4d4f;
  @border-color: #e8e8e8;
  @background-color: #f5f7fa;
  @text-color: #262626;
  @text-secondary: #8c8c8c;

  .steps-container {
    margin: 15px 0;
    padding: 30px;
    border-radius: 8px;
    background: linear-gradient(90deg, #f0f8ff, #e6f7ff);
    overflow: auto;
    max-height: calc(100vh - 200px);
    :deep(.ant-steps-item-icon) {
      background: white;
      border-color: @primary-color;
      width: 35px;
      .ant-steps-icon {
        color: @primary-color;
      }
    }

    :deep(.ant-steps-item-finish) {
      .ant-steps-item-icon {
        background: @primary-color;
        width: 35px;
        .ant-steps-icon {
          color: white;
        }
      }

      .ant-steps-item-title {
        color: @primary-color;
      }
    }

    :deep(.ant-steps-item-title) {
      font-weight: 500;
    }
  }

  .step-content {
    padding: 20px;
    border-radius: 8px;
    background-color: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);

    &.step-one {
      .prompt-select {
        width: 100%;
        font-size: 14px;
      }
    }

    &.step-two {
      .mapping-summary {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
        gap: 16px;
        margin-top: 16px;
      }

      .summary-item {
        display: flex;
        padding: 12px 16px;
        background-color: @background-color;
        border-radius: 6px;
        border: 1px solid @border-color;

        .field-key {
          font-weight: 500;
          color: @text-color;
          margin-right: 12px;
          min-width: 80px;

          &::after {
            content: ':';
          }
        }

        .field-value {
          color: @text-secondary;
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .section-header {
    h3 {
      margin: 0 0 20px 0;
      font-size: 18px;
      font-weight: 600;
      color: @text-color;
      display: flex;
      align-items: center;
      gap: 8px;

      svg {
        color: @primary-color;
      }
    }
  }

  .form-section {
    margin-bottom: 24px;
  }

  .form-item {
    margin-bottom: 24px;

    .form-label {
      display: block;
      margin-bottom: 10px;
      font-weight: 500;
      font-size: 15px;
      color: @text-color;
    }

    .prompt-option {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  .mapping-section {
    margin-top: 30px;
    padding: 20px;
    background-color: @background-color;
    border-radius: 8px;
    border: 1px solid @border-color;

    .mapping-list {
      .mapping-item {
        display: flex;
        align-items: center;
        margin-bottom: 16px;
        padding: 16px;
        background: white;
        border-radius: 6px;
        border: 1px solid @border-color;
        transition: all 0.3s;

        &:hover {
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          border-color: @primary-color;
        }

        .field-label {
          display: flex;
          align-items: center;
          gap: 8px;
          font-weight: 500;
          color: @text-color;
          min-width: 140px;
          padding-right: 16px;
        }

        .equal-sign {
          font-weight: bold;
          color: @text-secondary;
          margin: 0 12px;
        }

        .field-select {
          flex: 1;

          .column-option {
            display: flex;
            align-items: center;
            gap: 8px;
          }
        }
      }
    }
  }

  .debug-result {
    margin-top: 36px;
    padding: 20px;
    background-color: @background-color;
    border-radius: 8px;
    border: 1px solid @border-color;

    .result-table-container {
      margin-top: 16px;
      border-radius: 6px;
      overflow: hidden;
      border: 1px solid @border-color;

      :deep(.result-table) {
        .ant-table-thead > tr > th {
          background-color: #fafafa;
          font-weight: 600;
          color: @text-color;
        }

        .ant-table-tbody > tr:hover {
          background-color: #f0f8ff;
        }
      }
    }
  }

  .form-footer {
    display: flex;
    justify-content: flex-end;
    gap: 16px;
    padding-top: 24px;
    margin-top: 24px;
    border-top: 1px solid @border-color;

    .footer-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 20px;
      font-size: 14px;
      border-radius: 6px;
      transition: all 0.3s;

      &.cancel-btn {
        &:hover {
          color: @error-color;
          border-color: @error-color;
        }
      }

      &.next-btn,
      &.confirm-btn {
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(24, 144, 255, 0.2);
        }
      }

      &.prev-btn {
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }
      }

      &.close-btn {
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 8px rgba(82, 196, 26, 0.2);
        }
      }
    }
  }

  // Thiết kế responsive
  @media (max-width: 768px) {
    .step-content {
      padding: 16px;
    }

    .mapping-section {
      padding: 16px;

      .mapping-list {
        .mapping-item {
          flex-direction: column;
          align-items: flex-start;

          .field-label {
            margin-bottom: 10px;
            padding-right: 0;
          }

          .equal-sign {
            display: none;
          }

          .field-select {
            width: 100%;
            margin-top: 8px;
          }
        }
      }
    }

    .step-two .mapping-summary {
      grid-template-columns: 1fr;
    }

    .form-footer {
      flex-direction: column;

      .footer-btn {
        width: 100%;
        justify-content: center;
      }
    }
  }

  .debug-result-container {
    margin: 16px 0;
  }

  .table-header {
    padding: 12px 16px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-bottom: none;
    border-radius: 4px 4px 0 0;
  }

  .table-title {
    font-size: 16px;
    font-weight: 600;
    color: #1890ff;
  }

  .table-subtitle {
    font-size: 12px;
    color: #999;
    margin-top: 4px;
  }

  .result-table {
    border: 1px solid #e8e8e8;
  }

  :deep(.ant-table-thead > tr > th) {
    background-color: #f5f5f5;
    font-weight: 600;
    color: #333;
    border-bottom: 2px solid #1890ff;
  }

  :deep(.ant-table-tbody > tr.even-row) {
    background-color: #fafafa;
  }

  :deep(.ant-table-tbody > tr.odd-row) {
    background-color: #fff;
  }

  :deep(.ant-table-tbody > tr:hover) {
    background-color: #e6f7ff !important;
  }

  .status-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 500;
  }

  .status-success {
    background-color: #f6ffed;
    color: #52c41a;
    border: 1px solid #b7eb8f;
  }

  .status-failed {
    background-color: #fff2f0;
    color: #ff4d4f;
    border: 1px solid #ffccc7;
  }

  .status-warning {
    background-color: #fffbe6;
    color: #faad14;
    border: 1px solid #ffe58f;
  }

  .latency-text {
    font-weight: 500;
  }

  .latency-low {
    color: #52c41a;
  }

  .latency-medium {
    color: #faad14;
  }

  .latency-high {
    color: #ff4d4f;
  }

  .input-cell,
  .output-cell {
    padding: 4px 0;
  }

  .cell-label {
    font-size: 11px;
    color: #999;
    margin-bottom: 2px;
  }

  .cell-value {
    font-size: 13px;
    color: #333;
    word-break: break-word;
  }

  .output-cell.reference .cell-value {
    color: #1890ff;
  }

  .output-cell.actual .cell-value {
    color: #52c41a;
  }

  .table-footer {
    display: flex;
    justify-content: flex-start;
    gap: 24px;
    padding: 12px 16px;
    background: #fafafa;
    border: 1px solid #e8e8e8;
    border-top: none;
    border-radius: 0 0 4px 4px;
  }

  .footer-item {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .footer-label {
    font-size: 12px;
    color: #666;
  }

  .footer-value {
    font-size: 14px;
    font-weight: 500;
    color: #1890ff;
  }

  .no-data-container {
    padding: 40px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 4px;
    background-color: #fafafa;
  }

  .loading-text {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #666;
  }

  .empty-text {
    color: #999;
  }
</style>
