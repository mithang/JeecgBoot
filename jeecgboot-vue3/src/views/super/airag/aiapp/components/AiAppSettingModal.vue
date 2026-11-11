<template>
<div>
    <BasicModal wrapClassName="ai-app-edit-modal" destroyOnClose @register="registerModal" :canFullscreen="false" defaultFullscreen width="800px" :footer="null" @visible-change="visibleChange">
      <template #title>
        <div style="display: flex;width: 100%;justify-content: space-between;align-items: center;">
          <div style="display: flex">
            <img :src="getImage()" class="header-img"/>
            <div class="header-name">{{formState.name}}</div>
            <a-tooltip v-if="!isRelease" title="Chỉnh sửa">
              <Icon icon="ant-design:edit-outlined" style="margin-left: 4px;cursor: pointer" color="#354052" size="20" @click="handleEdit"></Icon>
            </a-tooltip>
          </div>
          <div>
            Sắp xếp ứng dụng
            <a-tooltip title="Tài liệu ứng dụng AI">
              <a style="color: unset" href="https://help.jeecg.com/aigc/guide/app" target="_blank">
                <Icon style="position:relative;left:2px;top:1px" icon="ant-design:question-circle-outlined"></Icon>
              </a>
            </a-tooltip>
          </div>
          <div style="display: flex">
            <a-button v-if="!isRelease" @click="handleOk" style="margin-right: 30px" type="primary">Lưu</a-button>
          </div>
        </div>
      </template>
      <div style="height: 100%; width: 100%">
        <a-row :span="24">
          <a-col :span="10">
            <div class="orchestration">Sắp xếp</div>
          </a-col>
          <a-col :span="14">
            <div class="view">Xem trước</div>
          </a-col>
        </a-row>
        <a-row :span="24">
          <a-col :span="10" class="setting-left">
            <a-form class="antd-modal-form" ref="formRef" :model="formState" :rules="validatorRules">
              <a-row>
                <a-col :span="24" v-if="formState.type==='chatFLow'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.flowId">
                      <template #label>
                        <div style="display: flex;justify-content: space-between;width: 100%;">
                          <span>Quy trình liên kết</span>
                          <span v-if="!isRelease" @click="handleAddFlowClick('chatFLow')" class="knowledge-txt">
                             <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                          </span>
                        </div>
                      </template>
                      <a-card v-if="flowData" hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                        <div style="display: flex; width: 100%; justify-content: space-between;">
                          <div style="width: 100%;display: flex;">
                            <img :src="getFlowImage(flowData.icon)" class="flow-icon"/>
                            <div style="display: grid;margin-left: 5px;align-items: center;width: calc(100% - 20px)">
                              <span class="flow-name ellipsis align-items: center;">{{ flowData.name }}</span>
                              <div class="flex text-status" v-if="flowData.metadata && flowData.metadata.length>0">
                                <span class="tag-input">Đầu vào</span>
                                <div v-for="(metaItem, index) in flowData.metadata">
                                  <a-tag color="#f2f3f8" class="tags-meadata">
                                    <span v-if="index<5" class="tag-text">{{ metaItem.field }}</span>
                                  </a-tag>
                                </div>
                              </div>
                            </div>
                          </div>
                          <Icon v-if="!isRelease" @click="handleDeleteFlow('chatFLow')" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                        </div>
                      </a-card>
                      <div v-else class="data-empty-text">
                        Quy trình làm việc hỗ trợ bằng cách trực quan hóa, kết hợp các chức năng như mô hình ngôn ngữ lớn, tập lệnh, tăng cường, từ đó hiện thực hóa việc sắp xếp quy trình nghiệp vụ phức tạp, ổn định, ví dụ lập kế hoạch du lịch, phân tích báo cáo.
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'">
                  <div class="prompt-back ">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.prompt" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <span>Prompt</span>
                          <div style="align-items: center;display:flex;justify-content: center" v-if="!isRelease">
                            <a-button size="middle" ghost>
                              <span style="align-items: center;display:flex"  @click="openPromptApps">
                                <Icon icon="ant-design:database-outlined"></Icon>Thư viện prompt
                              </span>
                            </a-button>
                            <a-button  size="middle" ghost>
                              <span style="align-items: center;display:flex"  @click="generatedPrompt">
                                <svg width="1em" height="1em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"/></svg>
                                <span style="margin-left: 4px">Tạo</span>
                              </span>
                            </a-button>
                          </div>
                        </div>
                      </template>
                      <a-textarea :disabled="isRelease" :rows="8" v-model:value="formState.prompt" placeholder="Vui lòng nhập prompt"/>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.prologue" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title">Lời mở đầu</div>
                      </template>
                      <div class="prologue-chunk-edit">
                        <j-markdown-editor :height="166" v-model:value="formState.prologue" :disabled="isRelease" @change="prologueTextAreaBlur" :preview="{ mode: 'view', action: [] }"></j-markdown-editor>
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.presetQuestion" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <div class="item-title">Câu hỏi đặt sẵn</div>
                          <a-tooltip v-if="!isRelease" title="Thêm câu hỏi đặt sẵn">
                            <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 16px;cursor: pointer" @click="presetQuestionAddClick"></Icon>
                          </a-tooltip>
                        </div>
                      </template>
                      <div style="padding: 0 10px" v-if="presetQuestionList.length>0">
                        <draggable :disabled="disabledDrag" item-key="key" v-model="presetQuestionList" @end="presetQuestionEnd">
                          <template #item="{ element:item }">
                            <div style="display: flex;width: 100%;margin-top: 10px">
                              <Icon v-if="!isRelease" icon="ant-design:holder-outlined" size="20"></Icon>
                              <a-input :disabled="isRelease" placeholder="Nhập câu hỏi đặt sẵn" v-model:value="item.descr" style="margin-left: 10px;" @blur="onBlur(item)" @focus="onFocus(item)" @change="questionChange"></a-input>
                              <Icon v-if="!isRelease" style="cursor: pointer;margin-left: 10px" icon="ant-design:delete-outlined" @click="deleteQuestionClick(item.key)"></Icon>
                            </div>
                          </template>
                        </draggable>
                      </div>
                      <div v-else class="data-empty-text">
                        Câu hỏi đặt sẵn là hướng dẫn ban đầu của cuộc trò chuyện mới, người dùng có thể nhanh chóng bắt đầu cuộc trò chuyện đặt sẵn
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk-edit">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.presetQuestion" style="margin-bottom:0;">
                      <template #label>
                        <div class="prompt-title-padding item-title space-between">
                          <div class="item-title">Lệnh tắt</div>
                          <a-tooltip v-if="!isRelease" title="Thêm lệnh tắt">
                            <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 16px;cursor: pointer" @click="quickCommandAddClick"></Icon>
                          </a-tooltip>
                        </div>
                      </template>
                      <div style="padding: 0 10px" v-if="quickCommandList.length>0">
                        <draggable item-key="key" v-model="quickCommandList" @end="quickCommandEnd">
                          <template #item="{ element:item }">
                            <div class="quick-command">
                              <div style="display: flex;align-items: center">
                                <Icon v-if="item.icon" :icon="item.icon" size="20"></Icon>
                                <svg v-else width="14px" height="14px" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"/></svg>
                                <div style="max-width: 400px;margin-left: 4px" class="ellipsis">{{item.name}}</div>
                              </div>
                              <div v-if="!isRelease" style="align-items: center" class="quick-command-icon">
                                <a-tooltip title="Chỉnh sửa">
                                  <Icon style="cursor: pointer;margin-left: 10px" icon="ant-design:edit-outlined" @click="editCommandClick(item)"></Icon>
                                </a-tooltip>
                                <a-tooltip title="Xóa">
                                  <Icon style="cursor: pointer;margin-left: 10px" icon="ant-design:delete-outlined" @click="deleteCommandClick(item.key)"></Icon>
                                </a-tooltip>
                              </div>
                            </div>
                          </template>
                        </draggable>
                      </div>
                      <div v-else class="data-empty-text">
                        Lệnh tắt là nút phía trên ô nhập hội thoại, sau khi cấu hình xong, người dùng có thể nhanh chóng bắt đầu cuộc trò chuyện đặt sẵn
                      </div>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.modelId">
                      <template #label>
                       <div style="display: flex;justify-content: space-between;width: 100%;margin-right: 2px">
                         <div class="item-title">Mô hình AI</div>
                         <div v-if="!isRelease" @click="handleParamSettingClick('model')" class="knowledge-txt">
                            <Icon icon="ant-design:setting-outlined" size="13" style="margin-right: 2px"></Icon>Cấu hình tham số
                         </div>
                       </div>
                      </template>
                      <JDictSelectTag
                          v-model:value="formState.modelId"
                          :disabled="isRelease"
                          placeholder="Vui lòng chọn mô hình AI"
                          dict-code="airag_model where model_type = 'LLM' and activate_flag = 1,name,id"
                          style="width: 100%;"
                          @change="handleModelIdChange"
                      ></JDictSelectTag>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item
                        class="knowledgeId"
                        style="width: 100%"
                        :labelCol="labelCol"
                        :wrapperCol="wrapperCol"
                        v-bind="validateInfos.knowledgeIds"
                    >
                      <template #label>
                        <div style="display: flex; justify-content: space-between; width: 100%;margin-left: 2px;">
                          <div class="item-title">Kho tri thức</div>
                          <div v-if="!isRelease">
                            <span @click="handleParamSettingClick('knowledge')" class="knowledge-txt">
                              <Icon icon="ant-design:setting-outlined" size="13" style="margin-right: 2px"></Icon>Cấu hình tham số
                            </span>
                            <span @click="handleAddKnowledgeIdClick('knowledge')" class="knowledge-txt">
                              <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                            </span>
                          </div>
                        </div>
                      </template>
                      <a-row :span="24">
                        <a-col :span="12" v-for="item in knowledgeDataList" v-if="knowledgeDataList && knowledgeDataList.length>0">
                          <a-card hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                            <div style="display: flex; width: 100%; justify-content: space-between">
                              <div>
                                <img class="knowledge-img" :src="knowledge" />
                                <span class="knowledge-name" style="color: #e03e2d;text-decoration: line-through" v-if="item.isDelete">{{ item.name }}</span>
                                <span class="knowledge-name" v-else>{{ item.name }}</span>
                              </div>
                              <Icon v-if="!isRelease" @click="handleDeleteKnowledge(item.id,'knowledge')" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                            </div>
                          </a-card>
                        </a-col>
                        <div v-else class="data-empty-text">
                          Sau khi thêm vào kho tri thức, khi người dùng gửi tin nhắn, tác tử có thể trích dẫn nội dung trong tri thức văn bản để trả lời câu hỏi của người dùng.
                        </div>
                      </a-row>
                    </a-form-item>
                  </div>
                </a-col>
                <!-- Liên kết nhiều quy trình làm việc -->
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol">
                      <template #label>
                        <div style="display: flex;justify-content: space-between;width: 100%;">
                          <span>Quy trình liên kết</span>
                          <span v-if="!isRelease" @click="handleAddFlowClick('chatSimple')" class="knowledge-txt">
                             <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                          </span>
                        </div>
                      </template>
                      <a-row :span="24">
                        <a-col :span="12" v-for="flowData in flowDataList" v-if="flowDataList && flowDataList.length>0">
                          <a-card hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                            <div style="display: flex; width: 100%; justify-content: space-between;">
                              <div style="width: 100%;display: flex;">
                                <img :src="getFlowImage(flowData.icon)" class="flow-icon"/>
                                <div style="display: grid;margin-left: 5px;align-items: center;width: calc(100% - 20px)">
                                  <span class="flow-name" style="color: #e03e2d;text-decoration: line-through" v-if="flowData.type">{{ flowData.name }}</span>
                                  <span v-else class="flow-name ellipsis align-items: center;">{{ flowData.name }}</span>
                                  <div class="flex text-status" v-if="flowData.metadata && flowData.metadata.length>0">
                                    <span class="tag-input">Đầu vào</span>
                                    <div v-for="(metaItem, index) in flowData.metadata">
                                      <a-tag color="#f2f3f8" class="tags-meadata">
                                        <span v-if="index<5" class="tag-text">{{ metaItem.field }}</span>
                                      </a-tag>
                                    </div>
                                  </div>
                                </div>
                              </div>
                              <Icon v-if="!isRelease" @click="handleDeleteFlow('chatSimple',flowData.id)" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                            </div>
                          </a-card>
                        </a-col>
                        <div v-else class="data-empty-text">
                          Quy trình làm việc hỗ trợ bằng cách trực quan hóa, kết hợp các chức năng như mô hình ngôn ngữ lớn, tập lệnh, tăng cường, từ đó hiện thực hóa việc sắp xếp quy trình nghiệp vụ phức tạp, ổn định, ví dụ lập kế hoạch du lịch, phân tích báo cáo.
                        </div>
                      </a-row>
               
                    </a-form-item>
                  </div>
                </a-col>
                <!-- Khối liên kết plugin -->
                <a-col :span="24" v-if="formState.type==='chatSimple'" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol">
                      <template #label>
                        <div style="display: flex; justify-content: space-between; width: 100%;margin-left: 2px;">
                          <div class="item-title">Liên kết MCP&Plugin</div>
                          <div v-if="!isRelease">
                            <span @click="handleAddMcpClick" class="knowledge-txt">
                              <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                            </span>
                          </div>
                        </div>
                      </template>
                      <a-row :span="24">
                        <a-col :span="12" v-for="item in pluginDataList" :key="item.id" v-if="pluginDataList && pluginDataList.length>0">
                          <a-card hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                            <div style="display: flex; width: 100%; justify-content: space-between">
                              <div>
                                <img class="knowledge-img" src="/@/views/super/airag/aimcp/imgs/mcpLogo.png" />
                                <span class="knowledge-name" :class="{ disabled: item.status === 'disable' || item.isDelete }" :title="item.isDelete ? 'Đã xóa' : item.status === 'disable' ? 'Đã vô hiệu hóa' : item.name">{{ item.name }}</span>
                                <span v-if="item.status === 'disable'" class="status-disabled">Vô hiệu hóa</span>
                                <span v-else-if="item.isDelete" class="status-disabled">Đã xóa</span>
                              </div>
                              <Icon v-if="!isRelease" @click="handleDeleteMcp(item.id)" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                            </div>
                          </a-card>
                        </a-col>
                        <div v-else class="data-empty-text">
                          <div style="margin-bottom: 8px;">Sau khi thêm plugin, tác tử có thể gọi năng lực công cụ bên ngoài để làm phong phú cuộc trò chuyện.</div>
                          <div class="mcp-warning-tip">
                            <Icon icon="ant-design:exclamation-circle-outlined" style="margin-right: 4px;" />
                            <span>Lưu ý: một số mô hình lớn tạm thời không hỗ trợ chức năng gọi công cụ, vui lòng xác nhận tính tương thích của mô hình đã chọn</span>
                          </div>
                        </div>
                      </a-row>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10" v-if="formState.type==='chatSimple'">
                  <a-collapse v-model:activeKey="memoryActiveKey" :bordered="false" style="background-color: transparent">
                    <a-collapse-panel key="1">
                      <template #header>
                        <div style="font-weight: 600;color: rgba(32,41,69,0.62);font-size: 14px;justify-content: space-between;display: flex; width: 100%">
                          <span>Bộ nhớ</span>
                          <a-switch @click.prevent.stop="" :disabled="isRelease" v-model:checked="izOpenMemoryChecked" checked-children="Mở" un-checked-children="Đóng" @change="handleMemoryChange"></a-switch>
                        </div>
                      </template>
                      <div v-if="izOpenMemoryChecked">
                        <div class="prologue-chunk">
                          <div style="display: flex; justify-content: space-between; width: 100%;margin-left: 2px;">
                            <div class="item-title">Biến</div>
                            <div v-if="!isRelease">
                              <span @click="handleAddVariable" class="knowledge-txt">
                                <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                              </span>
                            </div>
                          </div>
                          <div v-if="formState.variables">
                            <div style="display: flex; flex-wrap: wrap; gap: 5px; padding-top: 8px;margin-bottom: 8px">
                              <a-tag v-for="(item, index) in variablesList"
                                     :key="index"
                                     color="#2e2e3814"
                                     :class="{ 'variable-tag-editable': !isRelease }"
                                     @click="!isRelease && handleAddVariable()"
                                     style="color: #6b6b75;border-radius: 4px; border: none; padding: 0 5px;">
                                {{ item.name }}
                              </a-tag>
                            </div>
                          </div>
                          <div v-else class="data-empty-text">
                            Dùng để lưu thông tin cá nhân của người dùng, để tác tử ghi nhớ đặc điểm của người dùng, giúp trả lời cá nhân hóa hơn.
                          </div>
                        </div>
                        <div class="prologue-chunk" style="margin-top: 10px;">
                          <div style="display: flex; justify-content: space-between; width: 100%;margin-left: 2px;">
                            <div class="item-title">Bộ nhớ dài hạn</div>
                            <div v-if="!isRelease">
                              <span @click="handleAddKnowledgeIdClick('memory')" class="knowledge-txt">
                                <Icon icon="ant-design:plus-outlined" size="13" style="margin-right: 2px"></Icon>Thêm
                              </span>
                            </div>
                          </div>
                          <div v-if="memoryData" class="prologue-chunk">
                            <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.memoryId">
                              <a-card hoverable class="knowledge-card" :body-style="{ width: '100%' }">
                                <div style="display: flex; width: 100%; justify-content: space-between">
                                  <div>
                                    <img class="knowledge-img" :src="knowledge" />
                                    <span class="knowledge-name" style="color: #e03e2d;text-decoration: line-through" v-if="memoryData.isDelete">{{ memoryData.name }}</span>
                                    <span class="knowledge-name" v-else>{{ memoryData.name }}</span>
                                  </div>
                                  <Icon v-if="!isRelease" @click="handleDeleteKnowledge(memoryData.id,'memgory')" icon="ant-design:close-outlined" size="20" class="knowledge-icon"></Icon>
                                </div>
                              </a-card>
                            </a-form-item>
                          </div>
                          <div v-else class="data-empty-text">
                            Sau khi bật có thể tóm tắt nội dung cuộc trò chuyện, và dùng để phản hồi tin nhắn của người dùng tốt hơn.
                          </div>
                        </div>
                        <div class="prologue-chunk" style="margin-top: 20px;">
                          <div class="prompt-title-padding item-title space-between">
                            <span>Prompt bộ nhớ và biến</span>
                            <div style="align-items: center;display:flex;justify-content: center" v-if="!isRelease">
                              <a-button size="middle" ghost>
                                 <span style="align-items: center;display:flex" @click="generateVariablePrompt">
                                   <svg width="1em" height="1em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"/></svg>
                                   <span style="margin-left: 4px">Tạo</span>
                                 </span>
                              </a-button>
                            </div>
                          </div>
                          <a-spin :spinning="memoryLoading" tip="Đang sắp xếp cho bạn trong ứng dụng…">
                            <a-textarea :disabled="memoryLoading || !canGenerateMemoryPrompt || isRelease" :rows="6" v-model:value="formState.memoryPrompt" placeholder="Nhấp nút tạo để tạo prompt bộ nhớ và biến"/>
                          </a-spin>
                        </div>
                      </div>
                    </a-collapse-panel>
                  </a-collapse>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk">
                    <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.msgNum">
                      <template #label>
                        <div style="margin-left: 2px">Lịch sử trò chuyện</div>
                      </template>
                      <a-input-number :disabled="isRelease" v-model:value="formState.msgNum"></a-input-number>
                    </a-form-item>
                  </div>
                </a-col>
                <a-col :span="24" class="mt-10">
                  <div class="prologue-chunk">
                    <div style="margin-left: 2px">Cài đặt cá nhân hóa</div>
                    <a-row>
                      <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.multiSession">
                        <div style="display: flex;margin-top: 10px">
                          <div style="margin-left: 2px">Chế độ nhiều phiên:</div>
                          <a-switch :disabled="isRelease" v-model:checked="multiSessionChecked" checked-children="Mở" un-checked-children="Đóng" @change="handleMultiSessionChange"></a-switch>
                        </div>
                      </a-form-item>
                    </a-row>
                    <a-row>
                      <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol">
                        <div style="display: flex;margin-top: 10px">
                          <div style="margin-left: 2px">Bật khả năng vẽ:</div>
                          <a-switch :disabled="isRelease" v-model:checked="izDrawChecked" checked-children="Mở" un-checked-children="Đóng" @change="handleDrawChange"></a-switch>
                        </div>
                      </a-form-item>
                    </a-row>
                    <a-row>
                      <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol">
                        <div style="display: flex;margin-top: 10px">
                          <div style="margin-left: 2px">Hiển thị quá trình gọi công cụ:</div>
                          <a-switch
                            v-model:checked="showToolProcessChecked"
                            :disabled="isRelease"
                            checked-children="Mở"
                            un-checked-children="Đóng"
                            @change="handleShowToolProcessChange"
                          />
                        </div>
                      </a-form-item>
                    </a-row>
                    <a-row v-if="izDrawChecked" class="mt-10">
                      <a-col :span="24">
                        <a-form-item :labelCol="labelCol" :wrapperCol="wrapperCol" v-bind="validateInfos.drawModelId">
                          <span style="margin-left: 2px; margin-bottom: 5px">Mô hình vẽ:</span>
                          <JDictSelectTag
                              v-model:value="formState.drawModelId"
                              :disabled="isRelease"
                              placeholder="Vui lòng chọn mô hình phiên"
                              dict-code="airag_model where model_type = 'IMAGE' and activate_flag = 1,name,id"
                              @change="handleDrawModelChange"
                          ></JDictSelectTag>
                        </a-form-item>
                      </a-col>
                    </a-row>
                  </div>
                </a-col>
              </a-row>
            </a-form>
          </a-col>
          <a-col :span="14" class="setting-right">
            <chat 
              :uuid="uuid" 
              :prologue="prologue" 
              :appId="appId" 
              :formState="formState" 
              url="/airag/app/debug" 
              :presetQuestion="presetQuestion" 
              :quickCommandData="quickCommandList"
              :hasExtraFlowInputs="hasExtraFlowInputs"
              :conversationSettings="conversationSettings"
              @edit-settings="handleEditSettings"
            ></chat>
          </a-col>
        </a-row>
      </div>
    </BasicModal>

    <!--  Popup chọn kho tri thức Ai   -->
    <AiAppAddKnowledgeModal :multiple="knowledgeMultiple" :type="knowledgeMultiple?'knowledge':'memory'" @register="registerKnowledgeModal" @success="handleSuccess"></AiAppAddKnowledgeModal>
    <!--  Popup chọn plugin   -->
    <AiAppAddMcpModal @register="registerMcpModal" @success="handleMcpSuccess"></AiAppAddMcpModal>
    <!--  Popup thêm quy trình Ai  -->
    <AiAppAddFlowModal @register="registerFlowModal" @success="handleAddFlowSuccess" :multiple="multiple"></AiAppAddFlowModal>
    <!-- Popup cấu hình Ai   -->
    <AiAppParamsSettingModal @register="registerParamsSettingModal" @ok="handleParamsSettingOk"></AiAppParamsSettingModal>
    <!--  Popup thêm mới/chỉnh sửa ứng dụng Ai  -->
    <AiAppModal @register="registerAiAppModal" @success="handelEditSuccess"></AiAppModal>
    <!-- Trình tạo Ai   -->
    <AiAppGeneratedPromptModal @register="registerAiAppPromptModal" @ok="handleAiAppPromptOk"></AiAppGeneratedPromptModal>
    <!-- Popup chọn prompt Ai   -->
    <AiAppPromptMarketModal @register="registerAiPromptSelectModal" @ok="handleAiAppPromptOk"></AiAppPromptMarketModal>
    <!--  Lệnh tắt Ai  -->
    <AiAppQuickCommandModal @register="registerAiAppCommandModal" @ok="handleAiAppCommandOk" @update-ok="handleAiAppCommandUpdateOk"></AiAppQuickCommandModal>
    <!-- Popup cài đặt hội thoại -->
    <ConversationSettingsModal
      ref="settingsModalRef"
      :flowInputs="flowInputs"
      conversationId="debug"
      :existingSettings="conversationSettings"
      @ok="handleSettingsOk"
    />
    <!--  Biến người dùng  -->
    <AiUserVariablesModal @register="registerVariablesModal" @ok="handleVariablesOk"></AiUserVariablesModal>
  </div>
</template>

<script lang="ts">
  import { ref, reactive, nextTick, computed, watch } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { Form, TimePicker, Collapse, CollapsePanel } from 'ant-design-vue';
  import { initDictOptions } from '@/utils/dict';
  import { queryKnowledgeBathById, saveApp, queryById, queryFlowById, queryFlowByIds, queryKnowledgeById, generateMemoryByAppId } from '../AiApp.api';
  import { queryByIds as queryMcpByIds } from '../../aimcp/AiragMcp.api';
  import { defHttp } from '@/utils/http/axios';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import AiAppAddKnowledgeModal from './AiAppAddKnowledgeModal.vue';
  import AiAppAddMcpModal from './AiAppAddMcpModal.vue';
  import AiAppParamsSettingModal from './AiAppParamsSettingModal.vue';
  import AiAppGeneratedPromptModal from './AiAppGeneratedPromptModal.vue';
  import AiAppPromptMarketModal from './AiAppPromptMarketModal.vue';
  import AiAppQuickCommandModal from './AiAppQuickCommandModal.vue';
  import AiAppAddFlowModal from './AiAppAddFlowModal.vue';
  import AiAppModal from './AiAppModal.vue';
  import chat from '../chat/chat.vue';
  import ConversationSettingsModal from '../chat/components/ConversationSettingsModal.vue';
  import knowledge from '/@/views/super/airag/aiknowledge/icon/knowledge.png';
  import { cloneDeep } from 'lodash-es';
  import JImageUpload from '@/components/Form/src/jeecg/components/JImageUpload.vue';
  import defaultImg from '../img/ailogo.png';
  import { getFileAccessHttpUrl, randomString } from "@/utils/common/compUtils";
  import JSearchSelect from "@/components/Form/src/jeecg/components/JSearchSelect.vue";
  import JMarkdownEditor from "@/components/Form/src/jeecg/components/JMarkdownEditor.vue";
  import AiAppJson from './AiApp.json'
  import draggable from 'vuedraggable';
  import { useMessage } from "@/hooks/web/useMessage";
  import defaultFlowImg from "@/assets/images/ai/aiflow.png";
  import AiUserVariablesModal from "./AiUserVariablesModal.vue";
  
  export default {
    name: 'AiAppSettingModal',
    components: {
      draggable,
      JMarkdownEditor,
      JSearchSelect,
      JImageUpload,
      JDictSelectTag,
      BasicModal,
      AiAppAddKnowledgeModal,
      AiAppAddMcpModal,
      AiAppParamsSettingModal,
      AiAppAddFlowModal,
      AiAppModal,
      chat,
      ConversationSettingsModal,
      AiAppGeneratedPromptModal,
      AiAppQuickCommandModal,
      AiAppPromptMarketModal,
      AiUserVariablesModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      const title = ref<string>('Cài đặt');

      //Lưu hoặc sửa đổi
      const isUpdate = ref<boolean>(false);
      //uuid
      const uuid = ref(randomString(16));
      //Loại app
      const appTypeOption = ref<any>([]);
      //Loại ứng dụng
      const type = ref<string>('chatSimple');
      //Dữ liệu biểu mẫu form
      const formState = reactive<any>({
        name: '',
        descr: '',
        msgNum: 10,
        prompt: '',
        prologue: null,
        knowledgeIds: '',
        id: '',
        type: '',
        modelId: '',
        icon: '',
        presetQuestion:'',
        memoryId: '',
        variables: '',
        izOpenMemory: 1,
        memoryPrompt: '',
        drawModelId: '',
      });

      //Xác thực biểu mẫu
      const validatorRules = ref<any>({
        name: [{ required: true, message: 'Vui lòng nhập tên ứng dụng!' }],
        modelId: [{ required: true, message: 'Vui lòng chọn mô hình AI!' }],
        flowId:[{ required: true, message: 'Vui lòng chọn quy trình AI!' }],
        drawModelId: [{  required: true, message: 'Vui lòng chọn mô hình vẽ!' }]
      });
      //Đăng ký form
      const useForm = Form.useForm;
      const { resetFields, validate, validateInfos } = useForm(formState, validatorRules, { immediate: false });
      const labelCol = ref<any>({ span: 24 });
      const wrapperCol = ref<any>({ span: 24 });
      //id của kho tri thức liên kết
      const knowledgeIds = ref<any>('');
      //Tập hợp kho tri thức
      const knowledgeDataList = ref<any>([]);
      //Dữ liệu của kho bộ nhớ
      const memoryData = ref<any>(null);
      const knowledgeMultiple = ref<boolean>(true);
      // Tập hợp id plugin (chỉ lưu id)
      const pluginIds = ref<any>([]);
      // Tập hợp đối tượng plugin (bao gồm name, v.v.)
      const pluginDataList = ref<any>([]);
      // Cấu trúc plugin (khi lưu ghi vào formState.plugins)
      const plugins = ref<any>([]);
      //Dữ liệu của lời mở đầu
      const prologue = ref<any>('');
      //id ứng dụng
      const appId = ref<any>('');
      //Cấu hình tham số
      const metadata = ref<any>({});
      const presetQuestion = ref<string>('');
      //Tập hợp câu hỏi đặt sẵn
      const presetQuestionList = ref<any>([{ key:1, sort: 1, descr: '' }]);
      //Tập hợp lệnh tắt
      const quickCommandList = ref<any>([]);
      //Lệnh tắt
      const quickCommand = ref<any>('');
      const { createMessage } = useMessage();
      //Trạng thái chọn của chế độ nhiều phiên
      const multiSessionChecked = ref<boolean>(true);
      //Bật khả năng phiên
      const izDrawChecked = ref<boolean>(false);
      //Hiển thị quá trình gọi công cụ
      const showToolProcessChecked = ref<boolean>(true);
      // Đã phát hành hay chưa
      const isRelease = ref<boolean>(false);
      //ref của popup cài đặt hội thoại
      const settingsModalRef = ref();
      //Danh sách tham số đầu vào quy trình làm việc
      const flowInputs = ref<any[]>([]);
      //Cài đặt hội thoại (dùng cho chế độ gỡ lỗi)
      const conversationSettings = ref<Record<string, any>>({});
      //Tập hợp dữ liệu quy trình
      const flowDataList = ref<any>([]);
      //Nhiều id quy trình
      const flowIds = ref<string>('');
      //Có chọn nhiều hay không
      const multiple = ref<boolean>(false);
      //Dữ liệu biến
      const variablesList = ref<any>([]);
      //Có bật bộ nhớ hay không
      const izOpenMemoryChecked = ref<any>(false);
      //Panel gấp bộ nhớ
      const memoryActiveKey = ref<any>();
      //Prompt bộ nhớ có tải hay không
      const memoryLoading = ref<any>(false);
      
      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        appId.value = data.id;
        isUpdate.value = !!data?.isUpdate;
        isRelease.value = data?.status === 'release';
        clearParam();
        if (isUpdate.value) {
          setTimeout(() => {
            setFormState(data);
          }, 300);
        } else {
          //Sau khi thêm mới thành công cần có id
          queryById({ id: data.id }).then((res) => {
            if (res.success) {
              resetFields();
              //Gán giá trị
              Object.assign(formState, res.result);
              formState.prompt = cloneDeep(AiAppJson.prompt);
              formState.prologue = cloneDeep(AiAppJson.prologue);
              formState.presetQuestion = JSON.stringify(cloneDeep(AiAppJson.presetQuestion));
              formState.msgNum = 10;
              prologue.value = cloneDeep(AiAppJson.prologue);
              presetQuestion.value = formState.presetQuestion;
              presetQuestionList.value = cloneDeep(AiAppJson.presetQuestion);
              addRules(res.result.type)
            }
          });
        }
        setModalProps({ bodyStyle: { padding: '10px' } });
      });

      //Đăng ký modal
      const [registerKnowledgeModal, { openModal }] = useModal();
      const [registerMcpModal, { openModal: openMcpModal }] = useModal();
      const [registerFlowModal, { openModal: registerFlowOpen }] = useModal();
      const [registerParamsSettingModal, { openModal: paramsSettingOpen }] = useModal();
      const [registerAiAppModal, { openModal: aiAppModalOpen }] = useModal();
      const [registerAiAppPromptModal, { openModal: aiAppPromptModalOpen }] = useModal();
      const [registerAiAppCommandModal, { openModal: aiAppCommandModalOpen }] = useModal();
      const [registerAiPromptSelectModal, { openModal: aiPromptSelectModalOpen }] = useModal();
      const [registerVariablesModal, { openModal: aiVariablesModalOpen }] = useModal();

      /**
       * Lưu
       */
      async function handleOk() {
        try {
          let values = await validate();
          setModalProps({ confirmLoading: true });
          formState.knowledgeIds = knowledgeIds.value;
          if(flowIds.value){
            formState.flowId = flowIds.value;
          }
          await saveApp(formState);
          emit('success')
        } finally {
          setModalProps({ confirmLoading: false });
        }
      }

      //Khởi tạo loại ứng dụng AI
      initAppTypeOption();

      function initAppTypeOption() {
        initDictOptions('ai_app_type').then((data) => {
          if (data && data.length > 0) {
            for (const datum of data) {
              if (datum.value === 'chatSimple') {
                datum['desc'] = 'Phù hợp cho người mới tạo trợ lý nhỏ';
              } else if (datum.value === 'chatFLow') {
                datum['desc'] = 'Quy trình làm việc phù hợp cho người dùng nâng cao tùy chỉnh trợ lý nhỏ';
              }
            }
          }
          appTypeOption.value = data;
        });
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      /**
       * Sự kiện nhấp loại ứng dụng
       */
      function handleTypeClick(val) {
        type.value = val;
      }

      /**
       * Thêm kho tri thức liên kết
       * 
       * @param type
       */
      function handleAddKnowledgeIdClick(type) {
        //update-begin---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        knowledgeMultiple.value = type === 'knowledge'
        if(type === 'knowledge'){
          openModal(true, {
            knowledgeIds: knowledgeIds.value,
            knowledgeDataList: knowledgeDataList.value,
          });
        } else {
          openModal(true, {
            knowledgeIds: formState.memoryId,
            knowledgeData: memoryData.value,
          });
        }
        //update-end---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
      }

      /**
       * Thêm MCP liên kết
       */
      function handleAddMcpClick(){
        openMcpModal(true,{
          pluginIds: pluginIds.value,
          pluginDataList: pluginDataList.value
        })
      }

      /**
       * Sự kiện callback chọn
       * @param knowledgeId
       * @param knowledgeData
       */
      function handleSuccess(knowledgeId, knowledgeData) {
        //update-begin---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        if(knowledgeMultiple.value){
          knowledgeIds.value = cloneDeep(knowledgeId.join(','));
          knowledgeDataList.value = cloneDeep(knowledgeData);
          formState.knowledgeIds = knowledgeIds.value;
        } else {
          formState.memoryId = knowledgeId;
          memoryData.value = knowledgeData;
        }
        //update-end---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
      }

      /**
       * Callback chọn MCP thành công
       * @param ids
       * @param dataList
       * @param pluginStruct
       */
      function handleMcpSuccess(ids, dataList, pluginStruct){
        pluginIds.value = cloneDeep(ids);
        pluginDataList.value = cloneDeep(dataList);
        plugins.value = cloneDeep(pluginStruct);
        formState.plugins = JSON.stringify(plugins.value);
      }

      /**
       * Xóa kho tri thức
       */
      function handleDeleteKnowledge(id, type) {
        //update-begin---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        //update-begin---author:wangshuai---date:2026-01-14---for:【QQYUN-14562】【aiỨng dụng】Sau khi thêm bộ nhớ, dấu × không bỏ được---
        if(type === 'knowledge'){
        //update-end---author:wangshuai---date:2026-01-14---for:【QQYUN-14562】【aiỨng dụng】Sau khi thêm bộ nhớ, dấu × không bỏ được---
          let array = knowledgeIds.value.split(',');
          let findIndex = array.findIndex((item) => item === id);
          if (findIndex != -1) {
            array.splice(findIndex, 1);
            knowledgeIds.value = array ? array.join(',') : '';
            knowledgeDataList.value.splice(findIndex, 1);
            formState.knowledgeIds = knowledgeIds.value;
          }
        } else {
          formState.memoryId = "";
          memoryData.value = null;
        //update-end---author:wangshuai---date:2025-12-29---for:【QQYUN-14265】【AI】Hỗ trợ bộ nhớ---
        }
      }

      /**
       * Xóa MCP
       */
      function handleDeleteMcp(id){
        const findIndex = pluginIds.value.findIndex((item:any)=> item === id);
        if(findIndex>-1){
          pluginIds.value.splice(findIndex,1);
          pluginDataList.value.splice(findIndex,1);
          plugins.value = pluginDataList.value.map((it:any)=> ({ pluginId: it.id, pluginName: it.name, category: 'mcp'}));
          formState.plugins = JSON.stringify(plugins.value);
        }
      }

      /**
       * Truy vấn nội dung kho tri thức theo id kho tri thức
       * @param ids
       */
      function getKnowledgeDataList(ids) {
        queryKnowledgeBathById({ ids: ids }).then((res) => {
          //update-begin---author:wangshuai---date:2025-04-24---for:【QQYUN-12133】【AI】Sau khi kho tri thức liên kết với ứng dụng bị xóa, vào lại ứng dụng không thấy kho tri thức đã xóa và không thể dọn sạch kho tri thức.---
          if (res.success && res.result) {
            let result = res.result;
            let idArray = ids.split(",");
            let arr:any = [];
            for (const id of idArray) {
              let filter = result.filter((item) => item.id === id);
              if(filter && filter.length > 0) {
                arr.push({ id: id, name: filter[0].name});
              } else {
                arr.push({ name: 'Kho tri thức này đã bị xóa', id: id,isDelete: true })
              }
            }
            knowledgeDataList.value = arr;
            knowledgeIds.value = ids;
          } else {
            let arr:any = [];
            for (const id of ids) {
              arr.push({ name: 'Kho tri thức này đã bị xóa', id: id, isDelete: true })
            }
            knowledgeDataList.value = arr;
            knowledgeIds.value = ids;
          }
          //update-end---author:wangshuai---date:2025-04-24---for:【QQYUN-12133】【AI】Sau khi kho tri thức liên kết với ứng dụng bị xóa, vào lại ứng dụng không thấy kho tri thức đã xóa và không thể dọn sạch kho tri thức.---
        });
      }

      //update-begin---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---
      /**
       * Truy vấn nội dung plugin theo id plugin
       * @param ids
       */
      function getPluginDataList(ids) {
        queryMcpByIds({ id: ids, pageNo: 1, pageSize: 999 }).then((res) => {
          if (res.success && res.result) {
            let records = res.result.records || [];
            let idArray = ids.split(",");
            let arr = [];
            for (const id of idArray) {
              let item = records.find((r) => r.id === id);
              // Tìm lại tên gốc từ plugins đã lưu
              let original = plugins.value.find((p:any) => p.pluginId === id);
              if (item) {
                arr.push({ id: id, name: item.name || original?.pluginName || id, status: item.status });
              } else {
                arr.push({ id: id, name: original?.pluginName || id, isDelete: true });
              }
            }
            pluginDataList.value = arr;
          } else {
            let arr = [];
            let idArray = ids.split(",");
            for (const id of idArray) {
              let original = plugins.value.find((p:any) => p.pluginId === id);
              arr.push({ id: id, name: original?.pluginName || id, isDelete: true });
            }
            pluginDataList.value = arr;
          }
        });
      }
      //update-end---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---

      /**
       * Sự kiện thay đổi giá trị lời mở đầu
       */
      function prologueTextAreaBlur(value) {
        prologue.value = value;
      }

      /**
       * Đóng popup kích hoạt làm mới danh sách
       *
       * @param value
       */
      function visibleChange(value) {
        if(!value){
          emit('success');
        }
      }

      /**
       * Thêm kiểm nghiệm
       *
       * @param type
       */
      function addRules(type){
        if(type === 'chatSimple') {
          validatorRules.value = {
            name: [{ required: true, message: 'Vui lòng nhập tên ứng dụng!' }],
            modelId: [{ required: true, message: 'Vui lòng chọn mô hình AI!' }],
          }
        }else if(type === 'chatFLow') {
          validatorRules.value = {
            name: [{ required: true, message: 'Vui lòng nhập tên ứng dụng!' }],
            flowId:[{ required: true, message: 'Vui lòng chọn quy trình AI!' }]
          }
        }
      }

      /**
       * Sự kiện nhấp cấu hình tham số
       * @param value
       */
      function handleParamSettingClick(value) {
        paramsSettingOpen(true,{ type: value, metadata : metadata.value })
      }

      /**
       * Sự kiện callback xác nhận cấu hình tham số
       *
       * @param value
       */
      function handleParamsSettingOk(value){
        Object.assign(metadata.value,value)
        if(value) {
          formState.metadata = JSON.stringify(metadata.value);
        }
      }

      //id quy trình
      const flowId = ref<string>('');
      //Dữ liệu quy trình
      const flowData = ref<any>(null);

      /**
       * Truy vấn quy trình theo id quy trình
       */
      function getFlowDataById(id) {
        queryFlowById({ id: id}).then((res) =>{
          if(res.success){
            flowData.value = res.result;
            flowId.value = res.result.id;
            if(res.result.metadata){
              let metadata = JSON.parse(res.result.metadata);
              if(metadata.inputs){
                flowData.value.metadata = metadata.inputs;
              }
            }
          }
        })
      }

      /**
       * Thêm quy trình
       * @param type có chọn nhiều hay không
       */
      function handleAddFlowClick(type) {
        //update-begin---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
        multiple.value = type === 'chatSimple';
        if(type === 'chatSimple'){
          registerFlowOpen(true,{ flowId: flowIds.value, flowData: flowDataList.value })
        } else {
          registerFlowOpen(true,{ flowId: flowId.value, flowData: flowData.value })
        }
        //update-end---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
      }

      /**
       * Sự kiện xác nhận thêm quy trình
       * @param values
       */
      function handleAddFlowSuccess(values) {
        //update-begin---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
        if(multiple.value){
          flowIds.value = values.flowId?.join(",");
          formState.flowId = flowIds.value;
          flowDataList.value = values.flowData;
        } else {
          flowId.value = values.flowId;
          formState.flowId = values.flowId;
          flowData.value = values.flowData;
        }
        //update-end---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
      }

      /**
       * Xóa quy trình
       */
      function handleDeleteFlow(type, id = '') {
        //update-begin---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
        if(type === 'chatFLow'){
          flowId.value = "";
          formState.flowId = "";
          flowData.value = null;
        } else {
          let array = flowIds.value.split(',');
          let findIndex = array.findIndex((item) => item === id);
          if (findIndex != -1) {
            array.splice(findIndex, 1);
            flowIds.value = array ? array.join(',') : '';
            formState.flowId = flowIds.value;
            flowDataList.value.splice(findIndex, 1);
          }
        }
        //update-end---author:wangshuai---date:2025-12-24---for:【QQYUN-14267】Khi tạo ứng dụng, chọn quy trình làm việc, để ứng dụng có thể gọi quy trình như gọi MCP: phân biệt chọn một và chọn nhiều---
      }

      /**
       * Lấy biểu tượng
       */
      function getImage() {
        return formState.icon ? getFileAccessHttpUrl(formState.icon): defaultImg;
      }

      /**
       * Lấy biểu tượng quy trình
       */
      function getFlowImage(icon) {
        return icon ? getFileAccessHttpUrl(icon) : defaultFlowImg;
      }

      /**
       * Popup chỉnh sửa ứng dụng
       */
      function handleEdit() {
        aiAppModalOpen(true,{
          isUpdate: true,
          record: formState
        })
      }

      /**
       * Sự kiện callback chỉnh sửa ứng dụng
       *
       * @param values
       */
      function handelEditSuccess(values) {
        formState.icon = values.icon ? values.icon:'';
        formState.name = values.name ? values.name:'';
      }

      //=========== beginCâu hỏi đặt sẵn ===========================

      // Trạng thái chỉnh sửa không cho phép kéo
      const disabledDrag = computed(()=>{
        let list = presetQuestionList.value;
        if(list && list.length>0){
          let arr = list.filter(item=>item.update==true)
          if(arr.length>0){
            return true
          }
        }
        return false;
      });

      /**
       * Kéo thả câu hỏi đặt sẵn
       */
      function presetQuestionEnd(){
        presetQuestion.value = JSON.stringify(presetQuestionList.value);
        formState.presetQuestion = presetQuestion.value;
      }

      /**
       * Thêm câu hỏi đặt sẵn
       *
       * @param e
       */
      function presetQuestionAddClick(e){
        let find = presetQuestionList.value.find((item)=> item.descr == '');
        if(find){
          return;
        }
        const length = presetQuestionList.value.length;
        presetQuestionList.value.push({key: length + 1, sort: length + 1, descr: ''})
      }

      /**
       * Xóa câu hỏi đặt sẵn
       *
       * @param key
       */
      function deleteQuestionClick(key){
        presetQuestionList.value = presetQuestionList.value.filter((item) => item.key !== key);
        presetQuestion.value = JSON.stringify(presetQuestionList.value);
        formState.presetQuestion = presetQuestion.value;
      }

      /**
       * Sự kiện tập trung của input
       * @param item
       */
      function onFocus(item){
        item.update = true;
      }

      /**
       * Sự kiện mất tiêu điểm của input
       * @param item
       */
      function onBlur(item){
        item.update = false;
      }

      /**
       * Sự kiện thay đổi giá trị câu hỏi đặt sẵn
       *
       */
      function questionChange() {
        if(presetQuestionList.value && presetQuestionList.value.length>0){
          presetQuestion.value = JSON.stringify(presetQuestionList.value);
          formState.presetQuestion = presetQuestion.value;
        }else{
          presetQuestion.value = "";
          formState.presetQuestion = "";
        }
      }

      //=========== endCâu hỏi đặt sẵn ===========================

      /**
       * Truy vấn quy trình theo nhiều id quy trình
       */
      function getFlowDataByIds(ids) {
        queryFlowByIds({ id: ids, pageNo:1, pageSize: 100 }).then((res) =>{
          if(res.success && res.result){
            let records = res.result.records || [];
            let idArray = ids.split(",");
            let arr:any = [];
            let idsList:any = [];

            for (const id of idArray) {
              let item = records.find(r => r.id === id);
              if (item) {
                if(item.metadata){
                  try {
                    let metadata = JSON.parse(item.metadata);
                    if(metadata.inputs){
                      item.metadata = metadata.inputs;
                    }
                  } catch (e) {
                    console.error("metadata parse error", e);
                  }
                }
                arr.push(item);
              } else {
                arr.push({ name: 'Quy trình này đã bị xóa', id: id, type: 'delete' });
              }
              idsList.push(id);
            }
            flowDataList.value = arr;
            flowIds.value = idsList.join(",");
          } else {
            let arr:any = [];
            let idArray = ids.split(",");
            for (const id of idArray) {
              arr.push({ name: 'Quy trình này đã bị xóa', id: id, type: 'delete'})
            }
            flowDataList.value = arr;
            flowIds.value = ids;
          }
        })
      }

      /**
       * Làm trống tham số
       */
      function clearParam() {
        knowledgeIds.value = '';
        knowledgeDataList.value = [];
        memoryData.value = null;
        pluginIds.value = [];
        pluginDataList.value = [];
        plugins.value = [];
        prologue.value = '';
        flowId.value = '';
        flowData.value = null;
        flowDataList.value = [];
        flowIds.value = '';
        presetQuestion.value = '';
        presetQuestionList.value = [{ key:1, sort: 1, descr: '' }];
        quickCommandList.value = [];
        quickCommand.value = '';
        multiSessionChecked.value = true;
        izDrawChecked.value = false;
        variablesList.value = [];
        izOpenMemoryChecked.value = false;
        memoryLoading.value = false;
        memoryActiveKey.value = [];
      }

      /**
       * Đặt thuộc tính form
       * @param data
       */
      function setFormState(data: any) {
        resetFields();
        addRules(data.type)
        if (data.prologue) {
          prologue.value = data.prologue ? data.prologue : '';
        }
        data.msgNum = data.msgNum ? data.msgNum : 10;
        if(data.metadata){
          metadata.value = JSON.parse(data.metadata);
          if(metadata.value?.multiSession){
            multiSessionChecked.value = metadata.value.multiSession === '1';
          }else{
            multiSessionChecked.value = true;
          }
          if(metadata.value?.izDraw){
            izDrawChecked.value = metadata.value.izDraw === '1';
          }else{
            izDrawChecked.value = false;
          }
          if(metadata.value?.showToolProcess != null){
            showToolProcessChecked.value = metadata.value.showToolProcess === '1';
          }else{
            showToolProcessChecked.value = true;
          }
          if(metadata.value?.drawModelId){
            formState.drawModelId = metadata.value.drawModelId;
          }
        }
        if(data.presetQuestion){
          presetQuestion.value = data.presetQuestion;
          presetQuestionList.value = JSON.parse(data.presetQuestion);
        }
        if(data.quickCommand){
          //update-begin---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiỨng dụng AI, lệnh tắt, sau khi sửa và lưu, mở lại vẫn là nội dung cũ---
          let parse = JSON.parse(data.quickCommand);
          for (let i = 0; i < parse.length; i++) {
            parse[i].key = (i+1).toString();
          }
          quickCommandList.value = parse;
          //update-end---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiỨng dụng AI, lệnh tắt, sau khi sửa và lưu, mở lại vẫn là nội dung cũ---
        }
        if(data.plugins){
          try {
            const parsePlugins = JSON.parse(data.plugins);
            pluginIds.value = parsePlugins.map((p:any)=> p.pluginId);
            pluginDataList.value = parsePlugins.map((p:any)=> ({ id: p.pluginId, name: p.pluginName }));
            plugins.value = parsePlugins;
            //update-begin---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---
            // Truy vấn trạng thái plugin theo id plugin
            const pluginIdStr = pluginIds.value.join(',');
            if(pluginIdStr){
              getPluginDataList(pluginIdStr);
            }
            //update-end---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---
          } catch (e) {
            pluginIds.value = [];
            pluginDataList.value = [];
            plugins.value = [];
          }
        }
        //Gán giá trị
        Object.assign(formState, data);
        //Truy vấn nội dung kho tri thức theo id kho tri thức
        if (data.type === 'chatSimple' && data.knowledgeIds) {
          getKnowledgeDataList(data.knowledgeIds);
        }
        //Truy vấn thông tin quy trình theo id kho tri thức
        if (data.type === 'chatFLow' && data.flowId) {
          getFlowDataById(data.flowId);
        }
        //Truy vấn thông tin quy trình theo id của quy trình
        if (data.type === 'chatSimple' && data.flowId) {
          getFlowDataByIds(data.flowId);
        }
        // Nếu đã có modelId, truy vấn thông tin mô hình và cập nhật vào metadata
        if (data.type === 'chatSimple' && data.modelId) {
          handleModelIdChange(data.modelId);
        }
        // Tồn tại id của kho bộ nhớ, truy vấn kho bộ nhớ và hiển thị
        if(data.type === 'chatSimple' && data.memoryId){
          getMemoryDataById(data.memoryId);
        }
        //Biến
        if(formState.variables){
          variablesList.value = JSON.parse(formState.variables);
        }
        //Có bật bộ nhớ hay không
        if(formState.izOpenMemory){
          console.log("formState.izOpenMemory:::",formState.izOpenMemory)
          izOpenMemoryChecked.value = formState.izOpenMemory === 1;
        } else {
          izOpenMemoryChecked.value = false;
        }
        if(izOpenMemoryChecked.value){
          memoryActiveKey.value = [1];
        }
      }

      //============= begin Prompt ================================
      /**
       * Tạo prompt
       */
      function generatedPrompt() {
        aiAppPromptModalOpen(true,{})
      }

      /**
       * Callback prompt
       *
       * @param value
       */
      function handleAiAppPromptOk(value) {
        formState.prompt = value;
      }
      //============= end Prompt ================================

      //=============== begin Lệnh tắt ============================
      function quickCommandEnd() {
        quickCommand.value = JSON.stringify(quickCommandList.value);
        formState.quickCommand = quickCommand.value;
      }

      /**
       * Sự kiện nhấp thêm mới lệnh tắt
       */
      function quickCommandAddClick(){
        if(quickCommandList.value && quickCommandList.value.length > 4){
          createMessage.warning("Tối đa chỉ được thêm 5 cái!");
          return;
        }
        aiAppCommandModalOpen(true,{})
      }

      /**
       * Sự kiện nhấp chỉnh sửa lệnh tắt
       * @param item
       */
      function editCommandClick(item){
        aiAppCommandModalOpen(true,{
          isUpdate: true,
          record: item
        })
      }

      /**
       * Sự kiện callback thêm lệnh tắt
       * @param value
       */
      function handleAiAppCommandOk(value){
        //update-begin---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiỨng dụng AI, lệnh tắt, sau khi sửa và lưu, mở lại vẫn là nội dung cũ---
        value.key = (quickCommandList.value.length + 1).toString();
        //update-end---author:wangshuai---date:2025-04-08---for:【QQYUN-11939】aiỨng dụng AI, lệnh tắt, sau khi sửa và lưu, mở lại vẫn là nội dung cũ---
        quickCommandList.value.unshift({...value });
        quickCommand.value = JSON.stringify(quickCommandList.value);
        formState.quickCommand = quickCommand.value;
      }

      /**
       * Sự kiện callback cập nhật lệnh tắt
       * @param value
       */
      function handleAiAppCommandUpdateOk(value) {
        let findIndex = quickCommandList.value.findIndex(item => item.key === value.key);
        if(findIndex>-1){
          quickCommandList.value[findIndex] = value;
          quickCommand.value = JSON.stringify(quickCommandList.value);
          formState.quickCommand = quickCommand.value;
        }
      }

      /**
       * Xóa lệnh tắt
       * @param value
       */
      function deleteCommandClick(value) {
        let findIndex = quickCommandList.value.findIndex(item => item.key === value);
        if(findIndex>-1){
          quickCommandList.value.splice(findIndex, 1);
          quickCommand.value = JSON.stringify(quickCommandList.value);
          formState.quickCommand = quickCommand.value;
        }
      }
      //=============== end Lệnh tắt ============================

      /**
       * Callback khi hộp kiểm được chọn
       */
      function handleMultiSessionChange(checked){
        if(checked){
          metadata.value.multiSession = "1";
        }else{
          metadata.value.multiSession = "0";
        }
        formState.metadata = JSON.stringify(metadata.value);
      }

      // Kiểm tra có tham số đầu vào quy trình làm việc bổ sung hay không
      const hasExtraFlowInputs = computed(() => {
        if (!flowData.value || !flowData.value.metadata) {
          return false;
        }
        try {
          const flowInputsList = flowData.value.metadata || [];
          
          // Lọc bỏ tham số cố định
          const fixedParams = ['history', 'content', 'images'];
          const extraInputs = flowInputsList.filter((input: any) => !fixedParams.includes(input.field));
          
          return extraInputs.length > 0;
        } catch (e) {
          console.error('Phân tích flowData.metadata thất bại', e);
          return false;
        }
      });

      // Lắng nghe thay đổi của flowData, cập nhật flowInputs
      watch(
        () => flowData.value,
        (val) => {
          if (!val || !val.metadata) {
            flowInputs.value = [];
            return;
          }
          try {
            const flowInputsList = val.metadata || [];
            flowInputs.value = flowInputsList;
          } catch (e) {
            console.error('Phân tích flowData.metadata thất bại', e);
            flowInputs.value = [];
          }
        },
        { immediate: true, deep: true }
      );

      /**
       * Mở popup cài đặt hội thoại
       */
      function handleEditSettings() {
        if (settingsModalRef.value) {
          settingsModalRef.value.open();
        }
      }

      /**
       * Callback xác nhận cài đặt hội thoại
       */
      function handleSettingsOk(settings: Record<string, any>) {
        conversationSettings.value = settings;
        console.log('Cài đặt hội thoại chế độ gỡ lỗi đã cập nhật:', settings);
      }

      /**
       * Xử lý thay đổi ID mô hình
       * Truy vấn thông tin mô hình và cập nhật vào metadata, để component chat sử dụng
       */
      async function handleModelIdChange(modelId: string) {
        if (!modelId) {
          // Nếu làm trống mô hình, xóa thông tin mô hình
          if (metadata.value.modelInfo) {
            delete metadata.value.modelInfo;
            formState.metadata = JSON.stringify(metadata.value);
          }
          return;
        }
        try {
          //update-begin---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy được LLM API Key——tình huống không chỉnh sửa đổi sang dùng /detail, tránh phụ thuộc vào quyền airag:model:queryById và giảm bề mặt lộ credential-----------
          const res = await defHttp.get({
            url: '/airag/airagModel/detail',
            params: { id: modelId }
          }, { isTransformResponse: false });
          //update-end---author:scott ---date:20260506  for：【issues/9600】Người dùng quyền thấp có thể lấy được LLM API Key——tình huống không chỉnh sửa đổi sang dùng /detail, tránh phụ thuộc vào quyền airag:model:queryById và giảm bề mặt lộ credential-----------
          if (res.success && res.result) {
            const model = res.result;
            // Thêm thông tin mô hình vào metadata
            if (!metadata.value) {
              metadata.value = {};
            }
            metadata.value.modelInfo = {
              provider: model.provider || '',
              modelType: model.modelType || '',
              modelName: model.modelName || ''
            };
            formState.metadata = JSON.stringify(metadata.value);
          }
        } catch (e) {
          console.error('Lấy thông tin mô hình thất bại', e);
        }
      }
      
      function openPromptApps() {
        aiPromptSelectModalOpen(true,{});
      }
      /**
       * Lấy dữ liệu của kho bộ nhớ
       */
      function getMemoryDataById(id) {
        queryKnowledgeById({id: id}).then((res) =>{
          if(res.success && res.result){
            memoryData.value = res.result;
          } else {
            memoryData.value = { name: 'Kho tri thức này đã bị xóa', id: id, isDelete: true };
          }
        })
      }
      //================================================ begin Chức năng prompt biến và bộ nhớ =========================================================
      /**
       * Thêm biến
       */
      function handleAddVariable() {
        aiVariablesModalOpen(true,{
          variables: formState.variables
        })
      }

      /**
       * Sự kiện trả về của biến
       * 
       * @param values
       */
      function handleVariablesOk(values) {
        if(values){
          variablesList.value = values;
          formState.variables = JSON.stringify(values);
        }else{
          formState.variables = "";
          variablesList.value = [];
        }
      }

      /**
       * Giá trị trả về có bật chế độ phiên hay không
       * @param checked
       */
      function handleMemoryChange(checked) {
        if(checked){
          formState.izOpenMemory = 1;
        } else {
          formState.izOpenMemory = 0;
        }
      }

      /**
       * Prompt bộ nhớ và biến
       */
      // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-918】Khi không có biến và không có bộ nhớ dài hạn thì vô hiệu hóa ô văn bản, kiểm tra cảnh báo trước khi tạo
      const canGenerateMemoryPrompt = computed(() => {
        return variablesList.value?.length > 0 || !!formState.memoryId;
      });
      async function generateVariablePrompt() {
        if (!canGenerateMemoryPrompt.value) {
          createMessage.warning('Vui lòng thêm biến hoặc bộ nhớ dài hạn trước rồi mới tạo');
          return;
        }
        // update-end--author:liaozhiyang---date:20260804---for：【LHZP-918】Khi không có biến và không có bộ nhớ dài hạn thì vô hiệu hóa ô văn bản, kiểm tra cảnh báo trước khi tạo
        formState.memoryPrompt = '';
        memoryLoading.value = true;
        let readableStream = await generateMemoryByAppId({ variables: formState.variables, memoryId: formState.memoryId }).catch(() => {
          memoryLoading.value = false;
        });
        const reader = readableStream.getReader();
        const decoder = new TextDecoder('UTF-8');
        let buffer = '';
        while (true) {
          const { done, value } = await reader.read();
          if (done) {
            break;
          }
          let result = decoder.decode(value, { stream: true });
          const lines = result.split('\n\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              const content = line.replace('data:', '').trim();
              if(!content){
                continue;
              }
              if(!content.endsWith('}')){
                buffer = buffer + line;
                continue;
              }
              buffer = "";
              renderText(content)
            } else {
              if(!line) {
                continue;
              }
              if(!line.endsWith('}')) {
                buffer = buffer + line;
                continue;
              }
              buffer = "";
              renderText(line)
            }
          }
        }
      }

      /**
       * Hiển thị văn bản
       *
       * @param item
       */
      function renderText(item) {
        try {
          let parse = JSON.parse(item);
          if (parse.event == 'MESSAGE') {
            formState.memoryPrompt += parse.data.message;
            if(memoryLoading.value){
              memoryLoading.value = false;
            }
          }
          if (parse.event == 'MESSAGE_END') {
            memoryLoading.value = false;
          }
          if (parse.event == 'ERROR') {
            formState.memoryPrompt = parse.data.message ? parse.data.message: 'Tạo thất bại, vui lòng thử lại sau!'
            memoryLoading.value = false;
          }
        } catch (error) {
          console.log('Error parsing update:', error);
        }
      }
      //================================================ end Chức năng prompt biến và bộ nhớ =========================================================

      //================================================ begin Bắt đầu vẽ =========================================================
      /**
       * Callback bật khả năng phiên
       */
      function handleDrawChange(checked){
        if(checked){
          metadata.value.izDraw = "1";
        }else{
          metadata.value.izDraw = "0";
        }
        formState.metadata = JSON.stringify(metadata.value);
      }

      /**
       * Callback thay đổi mô hình phiên
       */
      function handleDrawModelChange(val){
        metadata.value.drawModelId = val;
        formState.metadata = JSON.stringify(metadata.value);
      }
      //================================================ end Bắt đầu vẽ ========================================================

      //================================================ begin Hiển thị quá trình gọi công cụ =========================================================
      /**
       * Callback công tắc hiển thị quá trình gọi công cụ
       */
      function handleShowToolProcessChange(checked){
        if(checked){
          metadata.value.showToolProcess = "1";
        }else{
          metadata.value.showToolProcess = "0";
        }
        formState.metadata = JSON.stringify(metadata.value);
      }
      //================================================ end Hiển thị quá trình gọi công cụ ========================================================

      return {
        registerModal,
        title,
        isRelease,
        handleOk,
        handleCancel,
        appTypeOption,
        type,
        handleTypeClick,
        formState,
        validatorRules,
        labelCol,
        wrapperCol,
        validateInfos,
        handleAddKnowledgeIdClick,
        handleAddMcpClick,
        registerKnowledgeModal,
        registerMcpModal,
        knowledgeDataList,
        pluginDataList,
        plugins,
        knowledge,
        handleSuccess,
        handleMcpSuccess,
        handleDeleteKnowledge,
        handleDeleteMcp,
        uuid,
        prologueTextAreaBlur,
        prologue,
        appId,
        visibleChange,
        handleParamSettingClick,
        registerParamsSettingModal,
        handleParamsSettingOk,
        registerFlowModal,
        handleAddFlowSuccess,
        handleAddFlowClick,
        flowData,
        handleDeleteFlow,
        getImage,
        handleEdit,
        registerAiAppModal,
        handelEditSuccess,
        presetQuestionEnd,
        presetQuestionList,
        presetQuestionAddClick,
        deleteQuestionClick,
        onBlur,
        onFocus,
        disabledDrag,
        questionChange,
        presetQuestion,
        generatedPrompt,
        registerAiAppPromptModal,
        handleAiAppPromptOk,
        quickCommandList,
        quickCommandEnd,
        registerAiAppCommandModal,
        quickCommandAddClick,
        handleAiAppCommandOk,
        editCommandClick,
        handleAiAppCommandUpdateOk,
        deleteCommandClick,
        quickCommand,
        getFlowImage,
        metadata,
        multiSessionChecked,
        handleMultiSessionChange,
        pluginIds,
        settingsModalRef,
        flowInputs,
        conversationSettings,
        hasExtraFlowInputs,
        handleEditSettings,
        handleSettingsOk,
        handleModelIdChange,
        flowDataList,
        multiple,
        memoryData,
        knowledgeMultiple,
        registerAiPromptSelectModal,
        openPromptApps,
        handleAddVariable,
        handleVariablesOk,
        registerVariablesModal,
        variablesList,
        generateVariablePrompt,
        canGenerateMemoryPrompt,
        izOpenMemoryChecked,
        memoryLoading,
        handleMemoryChange,
        memoryActiveKey,
        izDrawChecked,
        handleDrawChange,
        handleDrawModelChange,
        showToolProcessChecked,
        handleShowToolProcessChange,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .orchestration,.view{
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

  .flow-name{
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
/**update-begin---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---*/
  .knowledge-name.disabled {
    color: #999;
    text-decoration: line-through;
  }
  .status-disabled {
    margin-left: 6px;
    color: #ff4d4f;
    font-size: 12px;
    border: 1px solid #ff4d4f;
    border-radius: 10px;
    padding: 0 6px;
    height: 16px;
    line-height: 14px;
    display: inline-block;
  }
  /**update-end---author:liaozhiyang---date:2026-07-27---for:【LHZP-1499】Plugin đã bị xóa và đã bị vô hiệu hóa cần được đánh dấu---*/

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
  .header-img{
    width: 35px;
    height: 35px;
    border-radius: 10px;
  }
  .flex{
    display: flex;
  }
  .header-name{
    color:#354052;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    max-width: 300px;
    margin-left: 10px;
    align-content: center;
  }
  .prompt-back{
    background-color: #eef4ff;
    border-radius: 12px;
    padding: 2px;
    border: 1px solid #77B2F8;
    box-sizing: border-box;
    margin-left: 5px;
    textarea{
      min-height: 250px;
      max-height: 400px;
      border: none !important;
    }
  }
  .prompt-title-padding{
    margin-left: 14px;
    height: 50px;
    align-content: center;
  }
  .prologue-chunk{
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 10px 2px 10px;
    box-sizing: border-box;
  }

  .prologue-chunk-edit{
    background-color: #f2f4f7;
    border-radius: 12px;
    padding: 2px 0 2px 0;
    box-sizing: border-box;
  }
  .mt-10{
    margin-top: 10px;
  }
  :deep(.ant-form-item-label){
    padding: 0 !important;
  }

  :deep(.ant-form-item-required){
    margin-left: 4px !important;
  }
  .knowledge-txt{
    color: #354052;
    cursor: pointer;
    margin-right: 10px;
    font-size: 12px
  }
  .item-title{
    color: #111928;
    font-weight: 400;
  }
  :deep(.ant-form-item){
    margin-bottom: 5px;
  }
  :deep(.vditor){
    border: none;
  }
  :deep(.vditor--fullscreen){
    z-index: 1100 !important;
  }
  :deep(.vditor-sv){
    font-size: 14px;
  }
  :deep(.vditor-sv:focus){
    background-color: #ffffff;
  }
  .space-between{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    button{
      padding: 0 6px;
      height: 25px;
      color: #155AEF !important;
      margin-right: 10px;
      border: none;
    }
  }
  .ellipsis{
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
  .quick-command{
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
    .quick-command-icon{
      display: none;
    }
  }
  .quick-command:hover{
    background-color: #EFF0F8;
    .quick-command-icon{
      display: flex;
    }
  }
  .data-empty-text{
    color: #757c8f;
    margin-left: 10px;
  }
  .variable-tag-editable{
    cursor: pointer;
  }
  .mcp-warning-tip {
    display: flex;
    align-items: center;
    color: #fa8c16;
    font-size: 12px;
    line-height: 18px;
    font-weight: 500;
  }
  .flow-icon{
    width: 34px;
    height: 34px;
    border-radius: 10px;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .text-status{
    font-size: 12px;
    color: #676F83;
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
  .tag-input{
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
  .tags-meadata{
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
    color: #676F83;
  }
</style>
<style lang="less">
  .ai-app-edit-modal{
    .ant-modal .ant-modal-header{
      padding: 13px 32px !important;
    }
    .jeecg-basic-modal-close > span{
      margin-left: 0;
    }
  }
</style>
