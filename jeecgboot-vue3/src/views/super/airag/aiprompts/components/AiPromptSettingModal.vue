<template>
  <div class="p-2">
    <BasicModal
      wrapClassName="ai-prompt-edit-modal"
      destroyOnClose
      @register="registerModal"
      :canFullscreen="false"
      defaultFullscreen
      width="800px"
      :footer="null"
      @visible-change="visibleChange"
      @cancel="handleCancel"
    >
      <template #title>
        <div style="display: flex; width: 100%; justify-content: space-between; align-items: center">
          <div style="display: flex">
            <img :src="getImage()" class="header-img" />
            <div class="header-name">{{ formState.name }}</div>
            <a-tooltip title="Chỉnh sửa">
              <Icon icon="ant-design:edit-outlined" style="margin-left: 4px; cursor: pointer" color="#354052" size="20" @click="handleEdit"></Icon>
            </a-tooltip>
          </div>
          <div>
            Sắp xếp prompt
            <a-tooltip title="Tài liệu ứng dụng AI">
              <a style="color: unset" href="https://help.jeecg.com/aigc/guide/app" target="_blank">
                <Icon style="position: relative; left: 2px; top: 1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </div>
          <div style="display: flex">
            <a-button @click="handleOk" style="margin-right: 30px" type="primary">Lưu</a-button>
          </div>
        </div>
      </template>
      <div style="height: 100%; width: 100%">
        <a-row :span="24">
          <a-col :span="10">
            <div class="orchestration">Prompt</div>
          </a-col>
          <a-col :span="14">
            <div class="view">Xem trước</div>
          </a-col>
        </a-row>
        <a-row :span="24">
          <a-col :span="10" class="setting-left">
            <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
              <a-row>
                <a-col :span="24">
                  <div class="prompt-back">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.content" style="margin-bottom: 0">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <span>Prompt</span>
                          <a-button size="middle" ghost>
                            <span style="align-items: center; display: flex" @click="generatedPrompt">
                              <svg width="1em" height="1em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                                <path
                                  d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"
                                />
                              </svg>
                              <span style="margin-left: 4px">Tạo</span>
                            </span>
                          </a-button>
                        </div>
                      </template>
                      <a-textarea :rows="8" v-model:value="formState.content" placeholder="Vui lòng nhập prompt" @change="handleContentChange" />
                    </a-form-item>
                  </div>
                </a-col>

                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.modelId">
                      <template #label>
                        <div style="display: flex; justify-content: space-between; width: 100%; margin-right: 2px">
                          <div class="item-title">Mô hình AI</div>
                          <div @click="handleParamSettingClick('model')" class="knowledge-txt">
                            <Icon icon="ant-design:setting-outlined" size="13" style="margin-right: 2px"></Icon>Cấu hình tham số
                          </div>
                        </div>
                      </template>
                      <JDictSelectTag
                        v-model:value="formState.modelId"
                        placeholder="Vui lòng chọn mô hình AI"
                        dict-code="airag_model where model_type = 'LLM' and activate_flag = 1,name,id"
                        style="width: 100%"
                        @change="handleModelIdChange"
                      ></JDictSelectTag>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10" v-if="metadata.promptVariables && metadata.promptVariables.length > 0">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol">
                      <template #label>
                        <div style="display: flex; justify-content: space-between; width: 100%; margin-right: 2px">
                          <div class="item-title">Biến Prompt</div>
                        </div>
                      </template>
                      <!-- Container danh sách biến -->
                      <div class="prompt-variable-container">
                        <!-- Trạng thái trống khi không có biến -->
                        <div v-if="!metadata.promptVariables || metadata.promptVariables.length === 0" class="empty-variables">
                          <Icon icon="ant-design:inbox-outlined" style="font-size: 32px; color: #d9d9d9" />
                          <p>Tạm thời chưa có biến</p>
                          <span class="empty-hint">Vui lòng sử dụng định dạng {{ tên biến }} trong prompt để thêm biến</span>
                        </div>

                        <!-- Danh sách biến -->
                        <div v-else class="prompt-variables-list">
                          <div
                            class="prompt-variable-item"
                            v-for="(item, index) in metadata.promptVariables"
                            :key="index"
                            :class="{ 'variable-focused': focusedVariable === index }"
                          >
                            <!-- Nhãn tên biến -->
                            <div class="variable-tag">
                              <Icon icon="ant-design:tag-outlined" />
                              <span class="variable-name">{{ item.name }}</span>
                            </div>

                            <!-- Ô nhập giá trị biến -->
                            <div class="variable-input-wrapper">
                              <!-- Ô nhập tệp đính kèm -->
                              <JImageUpload :maxCount="1" v-if="item.type === 'FILE'" v-model:value="item.value"></JImageUpload>
                              <!-- Ô nhập văn bản -->
                              <a-input
                                  v-else
                                  v-model:value="item.value"
                                  placeholder="Vui lòng nhập giá trị biến"
                                  @focus="focusedVariable = index"
                                  @blur="focusedVariable = null"
                                  class="variable-input"
                              >
                                <template #suffix>
                                  <Icon v-if="item.value" icon="ant-design:check-circle-outlined" style="color: #52c41a" />
                                </template>
                              </a-input>
                            </div>
                           
                            <!-- Hộp chọn loại -->
                            <div class="variable-input-wrapper">
                              <a-select
                                  v-model:value="item.type"
                                  placeholder="Vui lòng chọn loại"
                                  :options="[
                                    { value: 'TEXT', label: 'Văn bản' },
                                    { value: 'FILE', label: 'Tệp đính kèm' }
                                  ]"
                                  class="variable-input"
                              >
                              </a-select>
                            </div>

                            <!-- Nút thao tác biến -->
                            <div class="variable-actions">
                              <a-tooltip title="Làm trống giá trị">
                                <a-button type="text" size="small" @click="item.value = ''" class="action-btn">
                                  <Icon icon="ant-design:clear-outlined" />
                                </a-button>
                              </a-tooltip>
                            </div>
                          </div>
                        </div>
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
              </a-row>
            </a-form>
          </a-col>
          <a-col :span="14" class="setting-right">
            <chat :uuid="uuid" :formState="chatData" url="/airag/app/debug"></chat>
          </a-col>
        </a-row>
      </div>
    </BasicModal>

    <!-- Popup cấu hình Ai   -->
    <AiAppParamsSettingModal @register="registerParamsSettingModal" @ok="handleParamsSettingOk"></AiAppParamsSettingModal>
    <!-- Trình tạo Ai   -->
    <AiAppGeneratedPromptModal @register="registerAiAppPromptModal" @ok="handleAiAppPromptOk"></AiAppGeneratedPromptModal>
    <!-- Popup prompt Ai   -->
    <AiragPromptsModal @register="registerPromptModal" @success="handleSuccess"></AiragPromptsModal>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive, computed, watch } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { Form } from 'ant-design-vue';
  import { defHttp } from '@/utils/http/axios';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import AiAppParamsSettingModal from '@/views/super/airag/aiapp/components/AiAppParamsSettingModal.vue';
  import AiAppGeneratedPromptModal from '@/views/super/airag/aiapp/components/AiAppGeneratedPromptModal.vue';
  import AiragPromptsModal from './AiragPromptsModal.vue';
  import chat from '@/views/super/airag/aiapp/chat/chat.vue';
  import defaultImg from '@/views/super/airag/aiapp/img/ailogo.png';
  import { getFileAccessHttpUrl, randomString } from '@/utils/common/compUtils';
  import { cloneDeep } from 'lodash-es';
  import { saveOrUpdate } from '@/views/super/airag/aiprompts/AiragPrompts.api';
  import { JImageUpload } from "@/components/Form";

  //Lưu hoặc sửa đổi
  const isUpdate = ref<boolean>(false);
  //uuid
  const uuid = ref(randomString(16));
  //Dữ liệu biểu mẫu form
  const formState = reactive<any>({
    id: '',
    name: '',
    promptKey: '',
    content: '',
    description: '',
    modelId: '',
    modelParam: '',
  });
  //Xác thực biểu mẫu
  const validatorRules = ref<any>({
    content: [{ required: true, message: 'Vui lòng nhập prompt!' }],
    modelId: [{ required: true, message: 'Vui lòng chọn mô hình AI!' }],
  });
  //Đăng ký form
  const useForm = Form.useForm;
  const { resetFields, validate, validateInfos } = useForm(formState, validatorRules, { immediate: false });
  const labelCol = ref<any>({ span: 24 });
  const wrapperCol = ref<any>({ span: 24 });
  //Cấu hình tham số
  const metadata = ref<any>({});
  //Định nghĩa sự kiện emit
  const emit = defineEmits(['success', 'register']);
  // Thêm chỉ số biến được tập trung
  const focusedVariable = ref<number | null>(null);
  // Prompt
  const prompt = ref<any>();

  //Đăng ký modal
  const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
    isUpdate.value = !!data?.isUpdate;
    clearFormState();
    resetFields();
    if (isUpdate.value) {
      setFormState(data.record);
    }
    setModalProps({ bodyStyle: { padding: '10px' } });
  });

  //Đăng ký modal
  const [registerParamsSettingModal, { openModal: paramsSettingOpen }] = useModal();
  const [registerAiAppPromptModal, { openModal: aiAppPromptModalOpen }] = useModal();
  const [registerPromptModal, { openModal: promptModalOpen }] = useModal();

  const chatData = computed(() => {
    return {
      id: formState.id,
      name: formState.name,
      prompt: prompt.value,
      modelId: formState.modelId,
      metadata: metadata.value?.modelInfo ? JSON.stringify(metadata.value.modelInfo) : '',
    };
  });

  watch(
    () => metadata.value,
    () => {
      changePrompt();
    },
    {
      deep: true,
    }
  );
  /**
   * Thay đổi prompt
   */
  function changePrompt() {
    prompt.value = formState.content;
    if (metadata.value && metadata.value['promptVariables']) {
      metadata.value['promptVariables'].forEach((variable) => {
        if (variable.type === 'FILE') {
          variable.value = getFileAccessHttpUrl(variable.value);
        }
        prompt.value = prompt.value.replace(new RegExp(`{{${variable.name}}}`, 'g'), variable.value);
      });
    }
  }

  /**
   * Sự kiện thay đổi nội dung prompt
   */
  function handleContentChange() {
    if (formState.content) {
      console.log("formState.content",formState.content)
      let variables = extractVariables(formState.content);
      console.log("variables",variables)
      if(variables.length > 0){
        const promptVariables = Array.isArray(metadata.value['promptVariables']) ? metadata.value['promptVariables'] : [];
        metadata.value['promptVariables'] = variables.map((variable) => ({
          name: variable,
          value: promptVariables.find((item) => item.name === variable)?.value || '',
          type: promptVariables.find((item) => item.name === variable)?.type || 'TEXT'
        }));
      }else{
        metadata.value['promptVariables'] = '';
      }
    } else {
      metadata.value['promptVariables'] = '';
    }
  }

  /**
   * Trích xuất tên biến từ prompt
   * @param str
   */
  function extractVariables(str) {
    // Khớp {{ Và }} nội dung ở giữa, khớp không tham lam
    const regex = /\{\{(.*?)\}\}/g;
    const matches = new Set(); // Dùng Set tự động loại trùng
    let match;

    while ((match = regex.exec(str)) !== null) {
      matches.add(match[1].trim()); // Loại bỏ dấu cách ở đầu và cuối có thể có và thêm vào Set
    }

    return Array.from(matches);;
  }

  //Chỉnh sửa
  function handleEdit() {
    promptModalOpen(true, {
      isUpdate: true,
      showFooter: true,
      record: {
        id: formState.id,
        name: formState.name,
        promptKey: formState.promptKey,
        description: formState.description,
      },
    });
  }
  /**
   * Lưu
   */
  async function handleOk() {
    try {
      let values = await validate();
      values.modelParam = JSON.stringify(cloneDeep(metadata.value));
      values = Object.assign(formState, values);
      //Gửi biểu mẫu
      await saveOrUpdate(values, isUpdate.value);
      setModalProps({ confirmLoading: true });
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Hủy
   */
  function handleCancel() {
    closeModal();
  }

  /**
   * Đóng popup kích hoạt làm mới danh sách
   *
   * @param value
   */
  function visibleChange(value) {
    if (!value) {
      emit('success');
    }
  }
  /**
   * Callback thành công
   */
  function handleSuccess(data) {
    setFormState(data);
    emit('success');
  }

  /**
   * Sự kiện nhấp cấu hình tham số
   * @param value
   */
  function handleParamSettingClick(value) {
    paramsSettingOpen(true, { type: value, metadata: metadata.value });
  }

  /**
   * Sự kiện callback xác nhận cấu hình tham số
   *
   * @param value
   */
  function handleParamsSettingOk(value) {
    Object.assign(metadata.value, value);
  }

  /**
   * Lấy biểu tượng
   */
  function getImage() {
    return formState.icon ? getFileAccessHttpUrl(formState.icon) : defaultImg;
  }

  /**
   * Làm trống tham số
   */
  function clearFormState() {
    formState.id = '';
    formState.name = '';
    formState.content = '';
    formState.description = '';
    formState.modelId = '';
    formState.modelParam = '';
    metadata.value = {};
    prompt.value = '';
  }

  /**
   * Đặt thuộc tính form
   * @param data
   */
  function setFormState(data: any) {
    //Gán giá trị
    Object.assign(formState, data);

    // Nếu đã có modelId, truy vấn thông tin mô hình và cập nhật vào metadata
    if (data.modelId) {
      metadata.value = data.modelParam ? JSON.parse(data.modelParam) : {};
    }
    console.log('Đặt thuộc tính form là formState', formState);
  }

  //============= begin Prompt ================================
  /**
   * Tạo prompt
   */
  function generatedPrompt() {
    aiAppPromptModalOpen(true, {});
  }

  /**
   * Callback prompt
   *
   * @param value
   */
  function handleAiAppPromptOk(value) {
    console.log('handleAiAppPromptOk value', value);
    formState.content = value;
  }
  //============= end Prompt ================================

  /**
   * Xử lý thay đổi ID mô hình
   * Truy vấn thông tin mô hình và cập nhật vào metadata, để component chat sử dụng
   */
  async function handleModelIdChange(modelId: string) {
    if (!modelId) {
      // Nếu làm trống mô hình, xóa thông tin mô hình
      if (metadata.value.modelInfo) {
        delete metadata.value.modelInfo;
      }
      return;
    }
    try {
      //update-begin---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy được LLM API Key——tình huống không chỉnh sửa đổi sang dùng /detail, tránh phụ thuộc vào quyền airag:model:queryById và giảm bề mặt lộ credential-----------
      const res = await defHttp.get(
        {
          url: '/airag/airagModel/detail',
          params: { id: modelId },
        },
        { isTransformResponse: false }
      );
      //update-end---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy được LLM API Key——tình huống không chỉnh sửa đổi sang dùng /detail, tránh phụ thuộc vào quyền airag:model:queryById và giảm bề mặt lộ credential-----------
      if (res.success && res.result) {
        const model = res.result;
        // Thêm thông tin mô hình vào metadata
        if (!metadata.value) {
          metadata.value = {};
        }
        metadata.value['modelInfo'] = {
          provider: model.provider || '',
          modelType: model.modelType || '',
          modelName: model.modelName || '',
        };
      }
    } catch (e) {
      console.error('Lấy thông tin mô hình thất bại', e);
    }
  }
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .orchestration,
  .view {
    color: #0a3069;
    font-weight: bold;
    text-align: center;
    font-size: 18px;
    width: 100%;
    padding-bottom: 10px;
  }
  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }

  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }

  .setting-left {
    padding: 20px;
    overflow-y: auto;
    height: (100vh - 15px);
  }

  .setting-right {
    overflow-y: auto;
    height: (100vh - 15px);
    border-left: 1px solid #dee0e3;
  }

  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-form-item .ant-form-item-label > label) {
    width: 100%;
  }

  .knowledge-img {
    width: 30px;
    height: 30px;
  }

  .flow-name {
    font-size: 14px;
    font-weight: bold;
    color: #354052;
    width: calc(100% - 20px);
    overflow: hidden;
    align-content: center;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: grid;
  }
  .knowledge-name {
    margin-left: 4px;
  }

  .knowledge-card {
    margin-bottom: 10px;
    margin-right: 10px;
  }

  .knowledge-icon {
    display: none !important;
    position: relative;
    top: 6px;
  }

  .knowledge-card:hover {
    .knowledge-icon {
      display: block !important;
    }
  }
  .header-img {
    width: 35px;
    height: 35px;
    border-radius: 10px;
  }
  .flex {
    display: flex;
  }
  .header-name {
    color: #354052;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    max-width: 300px;
    margin-left: 10px;
    align-content: center;
  }
  .prompt-back {
    background-color: #eef4ff;
    border-radius: 12px;
    padding: 2px;
    border: 1px solid #77b2f8;
    box-sizing: border-box;
    margin-left: 5px;
    textarea {
      min-height: 250px;
      max-height: 400px;
      border: none !important;
    }
  }
  .prompt-title-padding {
    margin-left: 14px;
    height: 50px;
    align-content: center;
  }
  .prologue-chunk {
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 10px 2px 10px;
    box-sizing: border-box;
  }

  .prologue-chunk-edit {
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 0 2px 0;
    box-sizing: border-box;
  }
  .mt-10 {
    margin-top: 10px;
  }
  :deep(.ant-form-item-label) {
    padding: 0 !important;
  }

  :deep(.ant-form-item-required) {
    margin-left: 4px !important;
  }
  .knowledge-txt {
    color: #354052;
    cursor: pointer;
    margin-right: 10px;
    font-size: 12px;
  }
  .item-title {
    color: #111928;
    font-weight: 400;
  }
  :deep(.ant-form-item) {
    margin-bottom: 5px;
  }
  :deep(.vditor) {
    border: none;
  }
  :deep(.vditor-sv) {
    font-size: 14px;
  }
  :deep(.vditor-sv:focus) {
    background-color: #ffffff;
  }
  .space-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    button {
      padding: 0 6px;
      height: 25px;
      color: #155aef !important;
      margin-right: 10px;
      border: none;
    }
  }
  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
  .quick-command {
    display: flex;
    width: 100%;
    margin-top: 10px;
    justify-content: space-between;
    background-color: #ffffff;
    padding: 4px 8px 4px;
    align-items: center;
    align-content: center;
    align-self: center;
    border-radius: 8px;
    height: 40px;
    .quick-command-icon {
      display: none;
    }
  }
  .quick-command:hover {
    background-color: #eff0f8;
    .quick-command-icon {
      display: flex;
    }
  }
  .data-empty-text {
    color: #757c8f;
    margin-left: 10px;
  }
  .mcp-warning-tip {
    display: flex;
    align-items: center;
    color: #fa8c16;
    font-size: 12px;
    line-height: 18px;
    font-weight: 500;
  }
  .flow-icon {
    width: 34px;
    height: 34px;
    border-radius: 10px;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .text-status {
    font-size: 12px;
    color: #676f83;
  }
  .tag-text {
    display: flow;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    height: 20px;
    font-size: 12px;
    color: #3a3f4f;
  }
  .tag-input {
    align-self: center;
    color: #737c97;
    font-size: 12px;
    font-style: normal;
    font-weight: 500;
    line-height: 16px;
    margin-right: 6px;
    text-align: right;
    white-space: nowrap;
  }
  .tags-meadata {
    padding-inline: 2px;
    border-radius: 4px;
    display: flex;
    font-weight: 500;
    max-width: 100%;
  }
  .text-desc {
    width: 100%;
    font-weight: 400;
    display: inline-block;
    text-overflow: ellipsis;
    overflow: hidden;
    text-wrap: nowrap;
    font-size: 12px;
    color: #676f83;
  }
  // Kiểu thông tin biến
  .variable-info {
    display: flex;
    align-items: center;
    color: #676f83;
    font-size: 12px;
    font-weight: 500;
  }

  // Kiểu container biến
  .prompt-variable-container {
    margin-top: 12px;
    min-height: 80px;
  }

  // Kiểu trạng thái trống
  .empty-variables {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 30px 20px;
    background: #fafafa;
    border-radius: 8px;
    border: 1px dashed #d9d9d9;

    p {
      margin: 12px 0 8px;
      font-size: 16px;
      color: #676f83;
    }

    .empty-hint {
      font-size: 13px;
      color: #8c8c8c;
    }
  }

  // Kiểu danh sách biến
  .prompt-variables-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  // Kiểu mục biến
  .prompt-variable-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: white;
    border-radius: 8px;
    border: 1px solid #e8e8e8;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    overflow: hidden;

    // Hiệu ứng di chuột
    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      border-color: #1890ff;
      transform: translateY(-2px);
    }

    // Hiệu ứng tập trung
    &.variable-focused {
      border-color: #1890ff;
      box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
    }

    // Thanh trang trí bên trái
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: #1890ff;
      opacity: 0;
      transition: opacity 0.3s;
    }

    &:hover::before,
    &.variable-focused::before {
      opacity: 1;
    }
  }

  // Kiểu nhãn biến
  .variable-tag {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 120px;
    padding: 6px 12px;
    background: #f0f8ff;
    border-radius: 6px;
    border: 1px solid #b7e3ff;
    margin-right: 16px;

    .variable-name {
      font-weight: 600;
      color: #1890ff;
      font-size: 14px;
    }
  }

  // Bộ bọc ô nhập biến
  .variable-input-wrapper {
    flex: 1;
    margin-right: 12px;

    .variable-input {
      border-radius: 6px;
      transition: all 0.3s;

      &:hover {
        border-color: #40a9ff;
      }

      &:focus,
      &:focus-within {
        border-color: #1890ff;
        box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
      }
    }
  }

  // Nút thao tác biến
  .variable-actions {
    display: flex;
    gap: 4px;
    opacity: 0.7;
    transition: opacity 0.3s;

    .action-btn {
      width: 28px;
      height: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 4px;
      color: #676f83;
      transition: all 0.2s;

      &:hover {
        background: #f0f8ff;
        color: #1890ff;
        transform: scale(1.05);
      }
    }
  }

  // Khi di chuột qua mục biến thì hiển thị nút thao tác
  .prompt-variable-item:hover .variable-actions {
    opacity: 1;
  }

  // Thiết kế responsive
  @media (max-width: 768px) {
    .prompt-variable-item {
      flex-direction: column;
      align-items: flex-start;
      padding: 12px;

      .variable-tag {
        margin-right: 0;
        margin-bottom: 10px;
        width: 100%;
      }

      .variable-input-wrapper {
        width: 100%;
        margin-right: 0;
        margin-bottom: 10px;
      }

      .variable-actions {
        width: 100%;
        justify-content: flex-end;
        opacity: 1;
      }
    }
  }
</style>
<style lang="less">
  .ai-prompt-edit-modal {
    .ant-modal .ant-modal-header {
      padding: 13px 32px !important;
    }
    .jeecg-basic-modal-close > span {
      margin-left: 0;
    }
  }
</style>
