<template>
  <div class="chatWrap">
    <div class="content">
      <div class="header-title" v-if="type === 'view' && headerTitle">
        <div class="title-content">
          <span>{{headerTitle}}</span>
          <a-button 
            v-if="hasExtraFlowInputs" 
            type="text" 
            class="edit-btn" 
            @click="handleEditSettings"
            title="Cài đặt tham số"
          >
            <Icon icon="ant-design:setting-outlined" :size="16" />
          </a-button>
        </div>
        <div class="header-actions">
          <div v-if="showAdvertising" class="header-advertisint">
            Chăm sóc khách hàng AI do
            <a style="color: #4183c4;margin-left: 2px;margin-right: 2px" href="https://jeecg.com/aigcIndex" target="_blank">
              JEECG AI
            </a>
            Cung cấp
          </div>
        </div>
      </div>
      <div class="main">
        <div id="scrollRef" ref="scrollRef" class="scrollArea">
          <template v-if="chatData.length>0">
            <div class="chatContentArea">
              <chatMessage
                v-for="(item, index) of chatData"
                :key="index"
                :date-time="item.dateTime || item.datetime"
                :text="item.content"
                :inversion="item.inversion || item.role"
                :error="item.error"
                :errorMsg="item.errorMsg"
                :currentToolTag="item.currentToolTag || currentToolTag"
                :thinkingToolTags="item.thinkingToolTags"
                :loading="item.loading"
                :appData="appData"
                :presetQuestion="item.presetQuestion"
                :images = "item.images"
                :files = "item.files"
                :retrievalText="item.retrievalText"
                :referenceKnowledge="item.referenceKnowledge"
                :eventType="item.eventType"
                :flowOutputSegments="item.flowOutputSegments"
                :showAvatar="item.showAvatar"
                :isLast="index === chatData.length -1"
                @send="handleOutQuestion"
              ></chatMessage>
            </div>
          </template>
        </div>
      </div>
      <div class="footer">
        <div class="topArea">
          <presetQuestion @out-question="handleOutQuestion" :quickCommandData="quickCommandData"></presetQuestion>
        </div>
        <div class="bottomArea">
          <a-popconfirm
            placement="topLeft"
            title="Xác nhận xóa phiên hiện tại?"
            ok-text="Xóa sạch"
            cancel-text="Hủy"
            :overlayStyle="{ 'z-index': 9999 }"
            @confirm="handleDelSession"
          >
          <a-button type="text" class="delBtn">
            <svg
              t="1706504908534"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="1584"
              width="18"
              height="18"
            >
              <path
                d="M816.872727 158.254545h-181.527272V139.636364c0-39.563636-30.254545-69.818182-69.818182-69.818182h-107.054546c-39.563636 0-69.818182 30.254545-69.818182 69.818182v18.618181H207.127273c-48.872727 0-90.763636 41.890909-90.763637 93.09091s41.890909 90.763636 90.763637 90.763636h609.745454c51.2 0 90.763636-41.890909 90.763637-90.763636 0-51.2-41.890909-93.090909-90.763637-93.09091zM435.2 139.636364c0-13.963636 9.309091-23.272727 23.272727-23.272728h107.054546c13.963636 0 23.272727 9.309091 23.272727 23.272728v18.618181h-153.6V139.636364z m381.672727 155.927272H207.127273c-25.6 0-44.218182-20.945455-44.218182-44.218181 0-25.6 20.945455-44.218182 44.218182-44.218182h609.745454c25.6 0 44.218182 20.945455 44.218182 44.218182 0 23.272727-20.945455 44.218182-44.218182 44.218181zM835.490909 407.272727h-121.018182c-13.963636 0-23.272727 9.309091-23.272727 23.272728s9.309091 23.272727 23.272727 23.272727h97.745455V837.818182c0 39.563636-30.254545 69.818182-69.818182 69.818182h-37.236364V602.763636c0-13.963636-9.309091-23.272727-23.272727-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364h-118.690909V602.763636c0-13.963636-9.309091-23.272727-23.272728-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364H372.363636V602.763636c0-13.963636-9.309091-23.272727-23.272727-23.272727s-23.272727 9.309091-23.272727 23.272727V907.636364h-34.909091c-39.563636 0-69.818182-30.254545-69.818182-69.818182V453.818182H558.545455c13.963636 0 23.272727-9.309091 23.272727-23.272727s-9.309091-23.272727-23.272727-23.272728H197.818182c-13.963636 0-23.272727 9.309091-23.272727 23.272728V837.818182c0 65.163636 51.2 116.363636 116.363636 116.363636h451.490909c65.163636 0 116.363636-51.2 116.363636-116.363636V430.545455c0-13.963636-11.636364-23.272727-23.272727-23.272728z"
                fill="currentColor"
                p-id="1585"
              />
            </svg>
          </a-button>
          </a-popconfirm>
          <a-button v-if="type === 'view'" type="text" class="contextBtn" :class="[usingContext && 'enabled']" @click="handleUsingContext">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              aria-hidden="true"
              role="img"
              class="iconify iconify--ri"
              width="20"
              height="20"
              viewBox="0 0 24 24"
            >
              <path
                fill="currentColor"
                d="M12 2c5.523 0 10 4.477 10 10s-4.477 10-10 10a9.956 9.956 0 0 1-4.708-1.175L2 22l1.176-5.29A9.956 9.956 0 0 1 2 12C2 6.477 6.477 2 12 2m0 2a8 8 0 0 0-8 8c0 1.335.326 2.618.94 3.766l.35.654l-.656 2.946l2.948-.654l.653.349A7.955 7.955 0 0 0 12 20a8 8 0 1 0 0-16m1 3v5h4v2h-6V7z"
              />
            </svg>
          </a-button>
          <div class="chat-textarea" :class="textareaActive?'textarea-active':''">
            <!--   begin Tương thích hiển thị tệp   -->
            <div class="textarea-top" v-if="(uploadUrlList && uploadUrlList.length>0) || (fileList && fileList.length>0)">
              <!-- Chỉ có hình ảnh -->
              <template v-if="(!fileList || fileList.length===0)">
                <div v-for="(item,index) in uploadUrlList" class="top-image" :key="index">
                  <img :src="getImage(item)" @click="handlePreview(item)"/>
                  <div class="upload-icon" @click="deleteImage(index)">
                    <Icon icon="ant-design:close-outlined" size="12px"></Icon>
                  </div>
                </div>
              </template>
              <!-- Có tệp -->
              <template v-else>
                <div class="file-card-container" style="display: flex; gap: 8px; flex-wrap: wrap;">
                  <!-- Hiển thị hình ảnh -->
                  <div v-for="(url, index) in uploadUrlList" :key="'img-'+index" class="file-card">
                    <div class="file-card-icon">
                      <img :src="getImage(url)" class="file-thumb" @click="handlePreview(url)"/>
                    </div>
                    <div class="file-card-info">
                      <div class="file-name" :title="fileInfoList[index]?.name || 'Hình ảnh'">{{ fileInfoList[index]?.name || 'Hình ảnh' }}</div>
                      <div class="file-size">{{ calculateFileSize(fileInfoList[index]?.size) }}</div>
                    </div>
                    <div class="file-card-close" @click="deleteImage(index)">
                      <Icon icon="ant-design:close-outlined" size="12px"/>
                    </div>
                  </div>
                  <!-- Hiển thị tệp -->
                  <template v-for="(item, index) in fileList" :key="'file-'+index">
                    <div class="file-card" v-if="item.status !== 'error'">
                      <div class="file-card-icon">
                        <Icon :icon="getFileIcon(item.name)" :color="getFileIconColor(item.name)" size="32" />
                      </div>
                      <div class="file-card-info">
                        <div class="file-name" :title="item.name">{{ item.name }}</div>
                        <div class="file-size">{{ calculateFileSize(item.size) }}</div>
                      </div>
                      <div class="file-card-close" @click="deleteFile(index)">
                        <Icon icon="ant-design:close-outlined" size="12px"/>
                      </div>
                    </div>
                  </template>
                </div>
              </template>
              <!--   end Tương thích hiển thị tệp   -->
            </div>
            <div class="textarea-bottom">
              <a-textarea
                  ref="inputRef"
                  v-model:value="prompt"
                  :autoSize="{ minRows: 1, maxRows: 6 }"
                  :placeholder="placeholder"
                  @press-enter="handleEnter"
                  @focus="textareaActive = true"
                  @blur="textareaActive = false"
                  autofocus
                  :readonly="loading"
                  style="border-color: #ffffff !important;box-shadow:none"
                  @paste="paste"
              >
              </a-textarea>
              <div class="textarea-action-bar">
                <div class="left-actions">
                <a-dropdown placement="topLeft" trigger="['click']" overlayClassName="chat-upload-dropdown">
                  <template #overlay>
                    <a-menu mode="vertical">
                      <a-menu-item key="img">
                        <a-upload
                            accept=".jpg,.jpeg,.png"
                            v-if="!loading"
                            name="file"
                            v-model:file-list="fileInfoList"
                            :showUploadList="false"
                            :headers="headers"
                            :beforeUpload="beforeUpload"
                            @change="handleChange"
                            :multiple="true"
                            :action="uploadUrl"
                            :max-count="3"
                        >
                          <div style="display: flex; align-items: center">
                            <Icon icon="ant-design:picture-outlined" style="margin-right:8px;color:#3d4353" />
                            Tải lên hình ảnh
                          </div>
                        </a-upload>
                      </a-menu-item>
                      <a-menu-item key="file">
                        <a-upload
                            accept=".txt, .pdf, .docx, .doc, .pptx, .ppt, .xlsx, .xls, .md"
                            :maxCount="3"
                            v-if="!loading"
                            name="file"
                            v-model:file-list="fileList"
                            :showUploadList="false"
                            :headers="headers"
                            :beforeUpload="beforeUploadFile"
                            @change="handleChangeFile"
                            :multiple="true"
                            :action="uploadUrl"
                        >
                          <div style="display: flex; align-items: center">
                            <Icon icon="ant-design:file-add-outlined" style="margin-right:8px;color:#3d4353" />
                            Tải lên tệp
                          </div>
                        </a-upload>
                      </a-menu-item>
                    </a-menu>
                  </template>
                  <a-button class="sendBtn" type="text">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M17.3977 3.9588C15.8361 2.39727 13.3037 2.39727 11.7422 3.9588L5.03365 10.6673C2.60612 13.0952 2.60612 17.0314 5.03365 19.4592C7.46144 21.887 11.3983 21.8875 13.8262 19.4599L20.5348 12.7514C20.8472 12.439 21.3534 12.439 21.6658 12.7514C21.9781 13.0638 21.9782 13.5701 21.6658 13.8825L14.9573 20.591C11.9046 23.6435 6.95518 23.6429 3.90255 20.5903C0.850191 17.5377 0.850191 12.5889 3.90255 9.53624L10.6111 2.82771C12.7975 0.641334 16.3424 0.641334 18.5288 2.82771C20.7149 5.01409 20.7151 8.55906 18.5288 10.7454L11.8699 17.4042C10.5369 18.7372 8.37542 18.7365 7.04241 17.4035C5.70963 16.0705 5.7095 13.9096 7.04241 12.5767L13.7012 5.91785C14.0136 5.60547 14.5199 5.60557 14.8323 5.91785C15.1447 6.23027 15.1447 6.73652 14.8323 7.04894L8.1735 13.7078C7.46543 14.4159 7.46556 15.5642 8.1735 16.2724C8.88167 16.9806 10.03 16.9806 10.7381 16.2724L17.397 9.61358C18.9584 8.05211 18.959 5.52035 17.3977 3.9588Z" fill="currentColor"></path></svg>
                  </a-button>
                </a-dropdown>
                  <a-divider type="vertical" v-if="showThink || showWebSearch || showDraw "/>
                  <a-tooltip v-if="showThink" :title="enableThink ? 'Tắt suy nghĩ sâu' : 'Bật suy nghĩ sâu'">
                    <a-button
                        class="sendBtn webSearchBtn"
                        type="text"
                        :class="{ 'enabled': enableThink }"
                        @click="toggleThink">
                      <svg style="margin-right: 6px" :style="enableThink ? { color: '#06f' } : { color: '#3d4353' }" width="16" height="16" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M7.06428 5.93342C7.6876 5.93342 8.19304 6.43904 8.19319 7.06233C8.19319 7.68573 7.68769 8.19123 7.06428 8.19123C6.44096 8.19113 5.93537 7.68567 5.93537 7.06233C5.93552 6.43911 6.44105 5.93353 7.06428 5.93342Z" fill="currentColor"></path><path fill-rule="evenodd" clip-rule="evenodd" d="M8.68147 0.963693C10.1168 0.447019 11.6266 0.374829 12.5633 1.31135C13.5 2.24805 13.4276 3.75776 12.911 5.19319C12.7126 5.74431 12.4385 6.31796 12.0965 6.89729C12.4969 7.54638 12.8141 8.19018 13.036 8.80647C13.5527 10.2419 13.625 11.7516 12.6883 12.6883C11.7516 13.625 10.2419 13.5527 8.80647 13.036C8.19019 12.8141 7.54638 12.4969 6.89729 12.0965C6.31794 12.4386 5.74432 12.7125 5.19319 12.911C3.75774 13.4276 2.24807 13.5 1.31135 12.5633C0.374829 11.6266 0.447019 10.1168 0.963693 8.68147C1.17182 8.10338 1.46318 7.50063 1.82893 6.8924C1.52179 6.35711 1.27232 5.82825 1.08869 5.31819C0.572038 3.88278 0.499683 2.37306 1.43635 1.43635C2.37304 0.499655 3.88277 0.572044 5.31819 1.08869C5.82825 1.27232 6.35712 1.5218 6.8924 1.82893C7.50063 1.46318 8.10338 1.17181 8.68147 0.963693ZM11.3572 8.01154C10.9083 8.62253 10.3901 9.22873 9.8094 9.8094C9.22874 10.3901 8.62252 10.9083 8.01154 11.3572C8.42567 11.5841 8.82867 11.7688 9.21272 11.9071C10.5455 12.3868 11.4246 12.2547 11.8397 11.8397C12.2547 11.4246 12.3869 10.5456 11.9071 9.21272C11.7688 8.82866 11.5841 8.42568 11.3572 8.01154ZM2.56526 8.02912C2.3734 8.39322 2.21492 8.74796 2.0926 9.08772C1.61288 10.4204 1.74509 11.2995 2.15998 11.7147C2.57502 12.1297 3.45412 12.2618 4.78694 11.7821C5.11053 11.6656 5.44783 11.5164 5.79377 11.3367C5.24897 10.9223 4.70919 10.4533 4.19026 9.9344C3.57575 9.31987 3.03166 8.67633 2.56526 8.02912ZM6.90705 3.2469C6.24062 3.70479 5.56457 4.26321 4.91389 4.91389C4.26322 5.56456 3.70479 6.24063 3.2469 6.90705C3.72671 7.63325 4.32774 8.37685 4.91389 8.96299C5.50003 9.54914 6.24362 10.1502 6.96983 10.6299C7.69601 10.1502 8.43961 9.54914 9.02575 8.96299C9.6119 8.37685 10.2129 7.63325 10.6927 6.90705C10.2129 6.18086 9.6119 5.43725 9.02575 4.8511C8.43961 4.26496 7.69601 3.66391 6.96983 3.18419C6.94896 3.205 6.92803 3.22593 6.90705 3.2469Z" fill="currentColor"></path></svg>
                      Suy nghĩ sâu
                    </a-button>
                  </a-tooltip>
                  <a-tooltip v-if="showWebSearch" :title="enableSearch ? 'Tắt tìm kiếm liên mạng' : 'Bật tìm kiếm liên mạng'">
                    <a-button
                        class="sendBtn webSearchBtn"
                        type="text"
                        :class="{ 'enabled': enableSearch }"
                        @click="toggleWebSearch">
                      <Icon size="16" icon="ant-design:global-outlined" :style="enableSearch ? { color: '#06f' } : { color: '#3d4353' }"></Icon>
                      Tìm kiếm liên mạng
                    </a-button>
                  </a-tooltip>
                  <a-tooltip v-if="showDraw" :title="enableDraw ? 'Tắt tạo hình tượng' : 'Bật tạo hình tượng'">
                    <a-button
                        class="sendBtn webSearchBtn"
                        type="text"
                        :class="{ 'enabled': enableDraw }"
                        @click="handleGenerateImage">
                      <svg style="margin-right: 6px" :style="enableDraw ? { color: '#06f' } : { color: '#3d4353' }" width="16" height="16" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg" class=""><path d="M12 2.3584C14.1681 2.35841 16.1541 2.52965 17.7266 2.72754C19.9228 3.00409 21.6336 4.66074 21.9365 6.85352C22.1348 8.28975 22.2998 10.0677 22.2998 12L22.293 12.7168C22.2586 14.3712 22.1101 15.8897 21.9365 17.1465L21.9043 17.3496C21.5268 19.4411 19.8545 21.0045 17.7266 21.2725L17.1182 21.3457C15.655 21.511 13.8972 21.6416 12 21.6416L11.1963 21.6338C9.60724 21.6034 8.13686 21.4874 6.88281 21.3457L6.27441 21.2725C4.14635 21.0046 2.47428 19.4411 2.09668 17.3496L2.06445 17.1465C1.89093 15.8897 1.74239 14.3712 1.70801 12.7168L1.7002 12C1.7002 10.3092 1.82669 8.737 1.99121 7.4082L2.06445 6.85352C2.35801 4.72923 3.9719 3.10743 6.06934 2.75684L6.27441 2.72754C7.84674 2.52969 9.83219 2.35841 12 2.3584ZM11.9775 13.3496C11.4613 13.3496 10.9378 13.4818 10.2207 13.8066C9.48747 14.1388 8.61112 14.6435 7.37793 15.3555L3.76367 17.4424C4.13152 18.6436 5.16153 19.5204 6.47363 19.6855C7.99607 19.8771 9.91342 20.042 12 20.042C14.0865 20.042 16.0039 19.8771 17.5264 19.6855C18.8303 19.5214 19.8566 18.6546 20.2305 17.4648L16.5771 15.3555C15.344 14.6435 14.4676 14.1388 13.7344 13.8066C13.0173 13.4818 12.4938 13.3496 11.9775 13.3496ZM12 3.95801C9.91342 3.95802 7.99607 4.12286 6.47363 4.31445C4.98011 4.50243 3.85117 5.61215 3.64941 7.07324C3.45876 8.45412 3.2998 10.1566 3.2998 12C3.2998 13.3468 3.38385 14.6183 3.50391 15.7441L6.57715 13.9707C7.78367 13.2741 8.73894 12.7218 9.56055 12.3496C10.3981 11.9702 11.1542 11.75 11.9775 11.75C12.8008 11.75 13.557 11.9702 14.3945 12.3496C15.2161 12.7218 16.1714 13.2741 17.3779 13.9707L20.4922 15.7686C20.6134 14.6367 20.7002 13.3565 20.7002 12C20.7002 10.1566 20.5422 8.4541 20.3516 7.07324C20.1498 5.61218 19.0198 4.50249 17.5264 4.31445C16.0039 4.12287 14.0865 3.95802 12 3.95801ZM7.73438 7.0625C8.76128 7.0625 9.59375 7.89497 9.59375 8.92188C9.59375 9.94878 8.76128 10.7812 7.73438 10.7812C6.70747 10.7812 5.875 9.94878 5.875 8.92188C5.875 7.89497 6.70747 7.0625 7.73438 7.0625Z" fill="currentColor"></path></svg>
                      <span style="font-size: 12px">Tạo hình tượng</span>
                    </a-button>
                  </a-tooltip>
                </div>
                <div class="right-actions">
                <a-button v-if="loading" type="primary" danger @click="handleStopChat" class="stopBtn">
                  <svg
                      t="1706148514627"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="5214"
                      width="18"
                      height="18"
                  >
                    <path
                        d="M512 967.111111c-250.311111 0-455.111111-204.8-455.111111-455.111111s204.8-455.111111 455.111111-455.111111 455.111111 204.8 455.111111 455.111111-204.8 455.111111-455.111111 455.111111z m0-56.888889c221.866667 0 398.222222-176.355556 398.222222-398.222222s-176.355556-398.222222-398.222222-398.222222-398.222222 176.355556-398.222222 398.222222 176.355556 398.222222 398.222222 398.222222z"
                        fill="currentColor"
                        p-id="5215"
                    />
                    <path d="M341.333333 341.333333h341.333334v341.333334H341.333333z" fill="currentColor" p-id="5216"/>
                  </svg>
                </a-button>
                <a-button
                    @click="
              () => {
                handleSubmit();
              }
            "
                    :disabled="!prompt"
                    class="sendBtn"
                    type="text"
                    v-if="!loading"
                >
                  <svg
                      t="1706147858151"
                      class="icon"
                      viewBox="0 0 1024 1024"
                      version="1.1"
                      xmlns="http://www.w3.org/2000/svg"
                      p-id="4237"
                      width="18"
                      height="18"
                  >
                    <path
                        d="M865.28 202.5472c-17.1008-15.2576-41.0624-19.6608-62.5664-11.5712L177.7664 427.1104c-23.2448 8.8064-38.5024 29.696-39.6288 54.5792-1.1264 24.8832 11.9808 47.104 34.4064 58.0608l97.5872 47.7184c4.5056 2.2528 8.0896 6.0416 9.9328 10.6496l65.4336 161.1776c7.7824 19.1488 24.4736 32.9728 44.7488 37.0688 20.2752 4.096 41.0624-2.1504 55.6032-16.7936l36.352-36.352c6.4512-6.4512 16.5888-7.8848 24.576-3.3792l156.5696 88.8832c9.4208 5.3248 19.8656 8.0896 30.3104 8.0896 8.192 0 16.4864-1.6384 24.2688-5.0176 17.8176-7.68 30.72-22.8352 35.4304-41.6768l130.7648-527.1552c5.5296-22.016-1.7408-45.2608-18.8416-60.416z m-20.8896 50.7904L713.5232 780.4928c-1.536 6.2464-5.8368 11.3664-11.776 13.9264s-12.5952 2.1504-18.2272-1.024L526.9504 704.512c-9.4208-5.3248-19.8656-7.9872-30.208-7.9872-15.9744 0-31.744 6.144-43.52 17.92l-36.352 36.352c-3.8912 3.8912-8.9088 5.9392-14.2336 6.0416l55.6032-152.1664c0.512-1.3312 1.2288-2.56 2.2528-3.6864l240.3328-246.1696c8.2944-8.4992-2.048-21.9136-12.3904-16.0768L301.6704 559.8208c-4.096-3.584-8.704-6.656-13.6192-9.1136L190.464 502.9888c-11.264-5.5296-11.5712-16.1792-11.4688-19.3536 0.1024-3.1744 1.536-13.824 13.2096-18.2272L817.152 229.2736c10.4448-3.9936 18.0224 1.3312 20.8896 3.8912 2.8672 2.4576 9.0112 9.3184 6.3488 20.1728z"
                        p-id="4238"
                        fill="currentColor"
                    />
                  </svg>
                </a-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Ref, watch } from 'vue';
  import { computed, ref, onUnmounted, onMounted, nextTick } from 'vue';
  import { useScroll } from './js/useScroll';
  import chatMessage from './chatMessage.vue';
  import presetQuestion from './presetQuestion.vue';
  import { DeleteOutlined } from '@ant-design/icons-vue';
  import { message, Tabs } from 'ant-design-vue';
  import './style/github-markdown.less';
  import './style/highlight.less';
  import './style/github-markdown.less';
  import dayjs from 'dayjs';
  import { defHttp } from '@/utils/http/axios';
  import { cloneDeep } from "lodash-es";
  import { calculateFileSize, getFileAccessHttpUrl, getFileIcon, getHeaders, getFileIconColor } from "@/utils/common/compUtils";
  import { createImgPreview } from "@/components/Preview";
  import { useAppInject } from "@/hooks/web/useAppInject";
  import { useGlobSetting } from "@/hooks/setting";
  import { Icon } from '/@/components/Icon';
  import { getChatErrorMessage } from './chatError';
  import { buildFlowOutputSegments } from '@jeecg/aiflow';

  message.config({
    prefixCls: 'ai-chat-message',
  });

  const props = defineProps(['uuid', 'prologue', 'formState', 'url', 'type','historyData','chatTitle','presetQuestion','quickCommandData','showAdvertising','hasExtraFlowInputs','conversationSettings','sessionType']);
  const emit = defineEmits(['save','reload-message-title','edit-settings']);
  //【LHZP-1351】AITrong quá trình AI tạo nội dung, khi người dùng cuộn lên thì không còn ép cuộn xuống đáy nữa
  const { scrollRef, scrollToBottom, scrollToBottomIfAuto } = useScroll();
  const prompt = ref<string>('');
  const loading = ref<boolean>(false);
  const inputRef = ref<Ref | null>(null);
  const headerTitle = ref<string>(props.chatTitle);

  //Dữ liệu trò chuyện
  const chatData = ref<any>([]);
  //Dữ liệu ứng dụng
  const appData = ref<any>({});
  const usingContext = ref<any>(true);
  const uuid = ref<string>(props.uuid);
  const topicId = ref<string>('');
  //id yêu cầu
  const requestId = ref<string>('');
  const { getIsMobile } = useAppInject();
  const conversationList = computed(() => chatData.value.filter((item) => item.inversion != 'user' && !!item.conversationOptions));
  const placeholder = computed(() => {
    if(getIsMobile.value){
      return 'Hãy nói gì đó đi... (Shift'
    } else {
      return 'Hãy nói gì đó đi... (Shift + Enter = Xuống dòng)';
    }
  });
  //token
  const headers = getHeaders();
  //Sự kiện nhấp vùng văn bản
  const textareaActive = ref<boolean>(false);

  const globSetting = useGlobSetting();
  const baseUploadUrl = globSetting.uploadUrl;
  // Tải lên ẩn danh cần mang theo appId + shareToken; trạng thái đã đăng nhập có thể không mang shareToken
  const uploadUrl = computed(() => {
    let url = `${baseUploadUrl}/airag/chat/upload`;
    const appId = appData.value?.id;
    if (!appId) {
      return url;
    }
    const params = new URLSearchParams();
    params.set('appId', appId);
    if (appData.value?.shareToken) {
      params.set('shareToken', appData.value.shareToken);
    }
    return `${url}?${params.toString()}`;
  });
  //Có phải kết nối lại sau khi mất kết nối hay không
  const isReConnect = ref<boolean>(false);
  //Có tồn tại quá trình suy nghĩ hay không
  const isThinking = ref<boolean>(false);
  //Có bật tìm kiếm mạng hay không
  const enableSearch = ref<boolean>(false);
  //Có hiển thị nút tìm kiếm mạng hay không (mặc định hiển thị)
  const showWebSearch = ref<boolean>(true);
  //Thông tin provider của mô hình
  const modelProvider = ref<string>('');
  //Có hiển thị suy nghĩ sâu hay không( Chỉ deepsee-reason hỗ trợ )
  const showThink = ref<boolean>(false);
  //Có bật suy nghĩ sâu hay không
  const enableThink = ref<boolean>(false);
  //Tên mô hình
  const modelName = ref<string>('');
  //Có bật vẽ hay không
  const enableDraw = ref<boolean>(false);
  //Có hiển thị thanh công cụ hay không
  const showDraw = ref<boolean>(false);
  //id của mô hình vẽ
  const drawModelId = ref<string>('');
  //Danh sách tệp khác
  const fileUrlList = ref<any>([]);
  //Danh sách tệp (dùng để hiển thị và quản lý)
  const fileList = ref<any>([]);

  // Công cụ hiện đang gọi
  const currentToolTag = ref<string>('');
  //update-begin---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Tên công cụ trong quá trình suy nghĩ được dịch thành tên quy trình và hiển thị lệnh gọi công cụ---
  // Tên hiển thị công cụ đã nhận diện trong vòng suy nghĩ hiện tại
  const thinkingToolDisplayNames = new Map<string, string>();
  // Vị trí tin nhắn suy nghĩ của vòng hiện tại
  const activeThinkingIndex = ref<number>(-1);
  //update-end---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Tên công cụ trong quá trình suy nghĩ được dịch thành tên quy trình và hiển thị lệnh gọi công cụ---

  function handleEnter(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleSubmit();
    }
  }
  function handleSubmit() {
    let userMessage = prompt.value;
    if (!userMessage || userMessage.trim() === '') return;
    prompt.value = '';
    onConversation(userMessage);
  }
  const handleOutQuestion = (userMessage) => {
    onConversation(userMessage);
  };
  async function onConversation(userMessage) {
    if(!props.type && props.type != 'view'){
      if(appData.value.type && appData.value.type == 'chatSimple' && !appData.value.modelId) {
        messageTip("Vui lòng chọn mô hình AI");
        return;
      }
      if(appData.value.type && appData.value.type == 'chatFLow' && !appData.value.flowId) {
        messageTip("Vui lòng chọn quy trình liên kết");
        return;
      }
      if(!appData.value.name) {
        messageTip("Vui lòng điền tên ứng dụng");
        return;
      }
    }
    
    // Kiểm tra có cần cài đặt tham số bổ sung hay không
    if (props.hasExtraFlowInputs) {
      // Kiểm tra đã cài đặt hay chưa
      if (!props.conversationSettings || Object.keys(props.conversationSettings).length === 0) {
        // Bật popup cài đặt, ngăn gửi
        message.warning('Vui lòng cài đặt tham số hội thoại trước');
        emit('edit-settings');
        return;
      }
    }
    
    if (loading.value) return;
    loading.value = true;

    addChat(uuid.value, {
      dateTime: new Date().toLocaleString(),
      content: userMessage,
      images:uploadUrlList.value?uploadUrlList.value:[],
      files: fileUrlList.value ? fileUrlList.value : [],
      inversion: 'user',
      error: false,
      conversationOptions: null,
      requestOptions: { prompt: userMessage, options: null },
      eventType: 'message',
    });
    scrollToBottom();

    let options: any = {};
    const lastContext = conversationList.value[conversationList.value.length - 1]?.conversationOptions;
    if (lastContext && usingContext.value) {
      options = { ...lastContext };
    }

    addChat(uuid.value, {
      dateTime: new Date().toLocaleString(),
      content: 'Đang suy nghĩ...',
      loading: true,
      inversion: 'ai',
      error: false,
      conversationOptions: null,
      requestOptions: { prompt: userMessage, options: { ...options } },
      referenceKnowledge: [],
      eventType: 'message',
    });

    scrollToBottom();

    //Gửi tin nhắn
    sendMessage(userMessage,options);
  }

  onUnmounted(() => {
    updateChatSome(uuid.value, chatData.value.length - 1, { loading: false });
  });

  const addChat = (uuid, data) => {
    chatData.value.push({ ...data });
  };
  const updateChat = async (uuid, index, data) => {
    let lastChatData = chatData.value[index];
    if(lastChatData.showAvatar){
      data.showAvatar = lastChatData.showAvatar;
    }
    chatData.value.splice(index, 1, data);
    await scrollToBottomIfAuto();
  };
  /**
   * Lời mở đầu ghim trên cùng
   * @param txt
   */
  const topChat = (txt) => {
    let data = {
      content: txt,
      key: 'prologue',
      loading: false,
      dateTime: dayjs().format('YYYY/MM/DD HH:mm:ss'),
      inversion: 'ai',
      presetQuestion: props.presetQuestion ? JSON.parse(props.presetQuestion) : "",
    };
    if (chatData.value && chatData.value.length > 0) {
      let key = chatData.value[0].key;
      if (key === 'prologue') {
        chatData.value[0] = { ...data };
        return;
      }
    }
    chatData.value.unshift({ ...data });
  };
  const updateChatSome = (uuid, index, data) => {
    chatData.value[index] = { ...chatData.value[index], ...data };
  };
  const updateChatFail = (uuid, data) => {
    const index = chatData.value.length - 1
    const oldChat = chatData.value[index];
    updateChat(uuid.value, chatData.value.length - 1, {
      ...oldChat,
      // dateTime: new Date().toLocaleString(),
      // content: data,
      inversion: 'ai',
      error: true,
      errorMsg: data,
      loading: true,
      conversationOptions: null,
      requestOptions: null,
      eventType: "message",
    });
    scrollToBottomIfAuto();
  };

  /**
   * Làm trống phiên
   * @param id
   */
  function handleDelSession (){
    try {
      //update-begin---author:wangshuai---date:2025-12-12---for:【QQYUN-14127】【AI】AICổng ứng dụng AI---
      let url = '/airag/chat/messages/clear/' + uuid.value;
      if(props.sessionType){
        url += "/" + props.sessionType;
      }
      defHttp.get({
        url: url,
      //update-end---author:wangshuai---date:2025-12-12---for:【QQYUN-14127】【AI】AICổng ứng dụng AI---
      },{ isTransformResponse: false }).then((res) => {
        if(res.success){
          chatData.value = [];
          topicId.value = "";
          if(props.prologue){
            topChat(props.prologue);
          }
        }
      })
    } catch {
      return console.log('Oops errors!');
    }
  };

  // Dừng phản hồi
  const handleStop = () => {
    console.log('ai trò chuyện：：：---dừng phản hồi, loading hiện tại:', loading.value, ', Ngăn xếp gọi:', new Error().stack?.split('\n').slice(1,4).join(' <- '));
    if (loading.value) {
      loading.value = false;
    }
    updateChatSome(uuid, chatData.value.length - 1, { loading: false });
  };

  handleStop();

  const knowList = ref<Recordable[]>([])

  /**
   * Dừng tin nhắn
   */
  function handleStopChat() {
    //update-begin---author:wangshuai---date:2025-06-03---for:【issues/8338】Ứng dụng AI trả lời trò chuyện, stop không hiệu lực, vẫn tiếp tục xuất ra câu trả lời---
    const currentRequestId = requestId.value
    if(currentRequestId){
      try{
        //Gọi giao diện backend dừng phản hồi
        defHttp.get({
          url: '/airag/chat/stop/' + currentRequestId,
        },{ isTransformResponse: false });
      } finally {
        handleStop();
        localStorage.removeItem('chat_requestId_' + uuid.value);
      }
      //update-end---author:wangshuai---date:2025-06-03---for:【issues/8338】Ứng dụng AI trả lời trò chuyện, stop không hiệu lực, vẫn tiếp tục xuất ra câu trả lời---
    } else {
      localStorage.removeItem('chat_requestId_' + uuid.value);
    }
  }

  /**
   * Đọc văn bản
   * @param message
   * @param options
   */
  async function sendMessage(message, options) {
    currentToolTag.value = '';
    thinkingToolDisplayNames.clear();
    activeThinkingIndex.value = -1;
    let param = {};
    if (!props.type && props.type != 'view') {
      param = {
        content: message,
        images: uploadUrlList.value?uploadUrlList.value:[],
        files: fileUrlList.value ? fileUrlList.value : [],
        topicId: topicId.value,
        app: appData.value,
        responseMode: 'streaming',
        // Thêm tham số cài đặt hội thoại (chế độ gỡ lỗi cũng cần)
        flowInputs: props.conversationSettings || {},
        // Thêm tham số tìm kiếm mạng
        enableSearch: enableSearch.value,
        // Thêm tham số suy nghĩ sâu
        enableThink: enableThink.value,
        // Thêm tham số vẽ
        enableDraw: enableDraw.value,
        drawModelId: enableDraw.value ? drawModelId.value : '',
        // Thêm loại tin nhắn portal Cổng thông tin
        sessionType: props.sessionType || ''
      };
    }else{
      param = {
        content: message,
        topicId: usingContext.value?topicId.value:'',
        images: uploadUrlList.value?uploadUrlList.value:[],
        files: fileUrlList.value ? fileUrlList.value : [],
        appId: appData.value.id,
        shareToken: appData.value.shareToken,
        responseMode: 'streaming',
        conversationId: uuid.value === "1002"?'':uuid.value,
        // Thêm tham số cài đặt hội thoại
        flowInputs: props.conversationSettings || {},
        // Thêm tham số tìm kiếm mạng
        enableSearch: enableSearch.value,
        // Thêm tham số suy nghĩ sâu
        enableThink: enableThink.value,
        // Thêm tham số vẽ
        enableDraw: enableDraw.value,
        drawModelId: enableDraw.value ? drawModelId.value : '',
        // Thêm loại tin nhắn portal Cổng thông tin
        sessionType: props.sessionType || ''
      };

      if(headerTitle.value == 'Tạo mới trò chuyện'){
        headerTitle.value = message.length>10?truncateString(message,10):message
      }

      emit("reload-message-title",message.length>10?truncateString(message,10):message)
    }

    uploadUrlList.value = [];
    fileInfoList.value = [];
    fileUrlList.value = [];
    fileList.value = [];
    knowList.value = [];
    options.message = message;
    const readableStream = await defHttp.post(
      {
        url: props.url,
        params: param,
        adapter: 'fetch',
        responseType: 'stream',
        timeout: 60 * 60 * 1000,
      },
      {
        isTransformResponse: false,
      }
    ).catch((e)=>{
      //update-begin---author:wangshuai---date:2025-04-28---for:【QQYUN-12297】【AI】Trò chuyện, thông báo sau khi quá thời gian chờ---
      if(e.code === 'ETIMEDOUT'){
        updateChatFail(uuid, "Hiện tại khá nhiều người dùng, đang xếp hàng, vui lòng thử lại sau giây lát!");
        handleStop();
        return;
      }else{
        updateChatFail(uuid, "Lỗi máy chủ, vui lòng thử lại sau!");
        handleStop();
        return;
      }
      console.error(e)
      //update-end---author:wangshuai---date:2025-04-28---for:【QQYUN-12297】【AI】Trò chuyện, thông báo sau khi quá thời gian chờ---
    });
    if (!readableStream) {
      return;
    }
    await renderChatByResult(readableStream,options);
  }
  // Có dùng ngữ cảnh hay không
  const handleUsingContext = () => {
    usingContext.value = !usingContext.value;
    if (usingContext.value) {
      message.success("Ở chế độ hiện tại, gửi tin nhắn sẽ mang theo lịch sử trò chuyện trước đó");
    } else {
      message.warning("Ở chế độ hiện tại, gửi tin nhắn sẽ không mang theo lịch sử trò chuyện trước đó");
    }
  };

  /**
   * Gợi ý
   * @param value
   */
  function messageTip(value) {
    message.warning(value);
  }

  /**
   * Phán đoán tin nhắn có phải bản ghi thực thi công cụ hay không, tránh để dữ liệu giao thức tầng dưới lẫn vào văn bản suy nghĩ.
   */
  function isToolExecPayload(value: unknown): boolean {
    if (typeof value !== 'string') {
      return false;
    }
    const text = value.trim();
    if (text.includes('<jeecg-tool-exec')) {
      return true;
    }
    if (!text.startsWith('{') || !text.endsWith('}')) {
      return false;
    }
    try {
      const record = JSON.parse(text);
      return !!record && typeof record === 'object' && !Array.isArray(record)
        && typeof record.name === 'string'
        && Object.prototype.hasOwnProperty.call(record, 'input')
        && Object.prototype.hasOwnProperty.call(record, 'output')
        && Object.prototype.hasOwnProperty.call(record, 'loading');
    } catch (error) {
      return false;
    }
  }

  /**
   * Hiển thị văn bản
   * @param item
   * @param conversationId
   * @param text
   * @param options
   */
  async function renderText(item,conversationId,text,options) {
    let returnText = "";
    if (item.event == 'MESSAGE' || item.event == 'THINKING' || item.event == 'THINKING_END') {
      let message = item.data?.message ?? "";
      // Kết quả công cụ đã có xử lý sự kiện chuyên dụng, trong lúc suy nghĩ không nên làm gián đoạn trạng thái suy nghĩ hiện tại như tin nhắn thường
      // update-begin--author:wangshuai---date:20260813---for：【LHZP-1601】【AIỨng dụng】Đôi khi suy nghĩ đã kết thúc vẫn suy nghĩ lại, cứ suy nghĩ nhiều vòng cho đến khi kết thúc
      if (isThinking.value && isToolExecPayload(message)) {
      // update-end--author:wangshuai---date:20260813---for：【LHZP-1601】【AIỨng dụng】Đôi khi suy nghĩ đã kết thúc vẫn suy nghĩ lại, cứ suy nghĩ nhiều vòng cho đến khi kết thúc
        return { returnText: text, conversationId };
      }
      let messageText = "";
      //update-begin---author:wangshuai---date:2025-04-24---for:Nên phán đoán trước xem có chứa card hay không---
      if(message && message.indexOf("::card::") !== -1){
        messageText = message;
      } else if(message && message.indexOf("::cardConfig::") !== -1) {
        messageText = message;
      } else {
        text = text + message;
        if (item.event == 'THINKING') {
          text = replaceThinkingToolNames(text);
        }
        messageText = text;
        returnText = text;
      }
      //update-end---author:wangshuai---date:2025-04-24---for:Nên phán đoán trước xem có chứa card hay không---
      // Lấy requestId từ tin nhắn
      if (item.requestId) {
        requestId.value = item.requestId;
      }
      if(item.event == 'THINKING'){
        isThinking.value = true;
      }
      if(item.event == 'MESSAGE' && isThinking.value){
        updateChatSome(uuid.value, chatData.value.length - 1, { loading: false, eventType: 'thinking_end' });
        text = item.data.message;
        returnText = item.data.message;
        //Gửi tin nhắn người dùng
        addChat(uuid.value, {
          dateTime: new Date().toLocaleString(),
          content: item.data.message,
          images:uploadUrlList.value?uploadUrlList.value:[],
          files: fileUrlList.value ? fileUrlList.value : [],
          inversion: 'ai',
          error: false,
          conversationOptions: null,
          requestOptions: { prompt: message, options: null },
          eventType: 'message',
          showAvatar: 'no'
        });
        isThinking.value = false;
        return { returnText, conversationId };
      }
      //Cập nhật thông tin trò chuyện
      const chatIndex = chatData.value.length - 1;
      const currentChat = chatData.value[chatIndex];
      updateChat(uuid.value, chatIndex, {
        dateTime: new Date().toLocaleString(),
        content: messageText,
        inversion: 'ai',
        error: false,
        loading: true,
        conversationOptions: { conversationId: conversationId, parentMessageId: topicId.value },
        requestOptions: { prompt: message, options: { ...options } },
        referenceKnowledge: knowList.value,
        eventType: item.event.toLowerCase(),
        //update-begin---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---
        currentToolTag: currentChat?.currentToolTag,
        thinkingToolTags: currentChat?.thinkingToolTags,
        //update-end---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---
      });
      if (item.event == 'THINKING') {
        activeThinkingIndex.value = chatIndex;
      }
    }
    if(item.event == 'INIT_REQUEST_ID'){
      if (item.requestId && props.url != "/airag/app/debug") {
        requestId.value = item.requestId;
        localStorage.setItem('chat_requestId_' + uuid.value, JSON.stringify({ requestId: item.requestId, message: options.message }));
      }
    }
    if (item.event == 'MESSAGE_END') {
      topicId.value = item.topicId;
      conversationId = item.conversationId;
      uuid.value = item.conversationId;
      localStorage.removeItem('chat_requestId_' + uuid.value);
      handleStop();
    }
    if (item.event == 'FLOW_FINISHED') {
      //update-begin---author:wangshuai---date:2025-03-07---for:【QQYUN-11457】Trò chuyện gọi quy trình, thực thi thất bại nhưng không có thông báo---
      if(item.data && !item.data.success){
        updateChatFail(uuid, item.data.message?item.data.message:'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!');
        localStorage.removeItem('chat_requestId_' + uuid.value);
        handleStop();
        return "";
      }
      //update-end---author:wangshuai---date:2025-03-07---for:【QQYUN-11457】Trò chuyện gọi quy trình, thực thi thất bại nhưng không có thông báo---
      // update-begin--author:liaozhiyang---date:20260831---for:【LHZP-2020】Thêm node tạo hình ảnh mới (sau khi quy trình kết thúc, hiển thị hình ảnh theo outputTypes và outputValues)
      const flowOutputSegments = buildFlowOutputSegments(item.data);
      if (flowOutputSegments.some((segment) => segment.type === 'image')) {
        updateChatSome(uuid.value, chatData.value.length - 1, {
          content: item.data?.outputs ?? '',
          flowOutputSegments,
          images: [],
          retrievalText: '',
          loading: false,
        });
        await scrollToBottomIfAuto();
      } else if (item.data?.type === 'Image') {
        const imageUrl = item.data.outputs;
        if (imageUrl) {
          updateChatSome(uuid.value, chatData.value.length - 1, {
            content: '',
            images: [imageUrl],
            retrievalText: '',
            loading: false,
          });
          await scrollToBottomIfAuto();
        }
      }
      // update-end--author:liaozhiyang---date:20260831---for:【LHZP-2020】Thêm node tạo hình ảnh mới (sau khi quy trình kết thúc, hiển thị hình ảnh theo outputTypes và outputValues)
      topicId.value = item.topicId;
      conversationId = item.conversationId;
      uuid.value = item.conversationId;
      requestId.value = item.requestId;
      localStorage.removeItem('chat_requestId_' + uuid.value);
      handleStop();
    }
    if (item.event == 'ERROR') {
      updateChatFail(uuid, item.data.message?item.data.message:'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!');
      localStorage.removeItem('chat_requestId_' + uuid.value);
      handleStop();
      return "";
    }

    //update-begin---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---
    // Gọi công cụ bắt đầu
    if (item.event == 'TOOL_EXEC_BEFORE') {
      const toolTag = item.data?.message ?? '';
      if (!updateThinkingTool(toolTag, false)) {
        currentToolTag.value = toolTag;
      }
    }

    // Gọi công cụ kết thúc
    if (item.event == 'TOOL_EXEC_DONE') {
      const toolTag = item.data?.message ?? '';
      if (!updateThinkingTool(toolTag, true)) {
        currentToolTag.value = '';
      }
    }
    //update-end---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Quá trình suy nghĩ không hiển thị lệnh gọi công cụ---

    //update-begin---author:wangshuai---date:2025-03-21---for:【QQYUN-11495】【AI】Hiển thị tiến trình suy nghĩ hiện tại theo thời gian thực---
    if(item.event === "NODE_STARTED"){   
      if(!item.data || item.data.type !== 'end'){
        let aiText = "";
        if(item.data.type === 'llm' || item.data.type === 'reply'){
          aiText = "Đang xây dựng nội dung phản hồi";
        }
        if(item.data.type === 'knowledge'){
          aiText = "Đang truy xuất sâu kho tri thức";
        }
        if(item.data.type === 'classifier'){
          aiText = "Đang phân loại";
        }
        if(item.data.type === 'code'){
          aiText = "Đang thực thi thao tác chạy mã";
        }
        if(item.data.type === 'subflow'){
          aiText = "Đang chạy quy trình con";
        }
        if(item.data.type === 'enhanceJava'){
          aiText = "Đang thực thi tăng cường java";
        }
        if(item.data.type === 'http'){
          aiText = "Đang gửi yêu cầu http";
        }
        if (item.data.type === 'imageGen') {
          aiText = "Đang tạo hình ảnh";
        }
        if(!text){
          //Cập nhật thông tin trò chuyện
          updateChat(uuid.value, chatData.value.length - 1, {
            dateTime: new Date().toLocaleString(),
            retrievalText: aiText,
            text:"",
            inversion: 'ai',
            error: false,
            loading: true,
            conversationOptions: null,
            requestOptions: { prompt: message, options: { ...options } },
            referenceKnowledge: knowList.value,
            eventType: 'message',
          });
        }
      }
    }
    //update-end---author:wangshuai---date:2025-03-21---for:【QQYUN-11495】【AI】Hiển thị tiến trình suy nghĩ hiện tại theo thời gian thực---
    else if (item.event === 'NODE_FINISHED') {
      if(!item.data || item.data.type !== 'end'){
        if(item.data.type === 'knowledge'){
          const id = item.data.id;
          const data = item.data.outputs[id + ".documents"]
          knowList.value = data
          //Cập nhật thông tin trò chuyện
          updateChatSome(uuid.value, chatData.value.length - 1, {referenceKnowledge: knowList.value})
        }
      }
    }
    if(!returnText && item.event !== 'NODE_FINISHED'){
      returnText = text;
    }
    return { returnText, conversationId };
  }

  //Tập hợp danh sách tệp tải lên
  const uploadUrlList = ref<any>([]);
  //Tập hợp tệp
  const fileInfoList = ref<any>([]);

  /**
   * Sự kiện callback tải lên tệp
   * @param info
   */
  function handleChange(info) {
    let { fileList, file } = info;
    fileInfoList.value = fileList;
    if (file.status === 'error' || (file.response && file.response.code == 500)) {
      message.error(file.response?.message || `${file.name} Tải lên thất bại, vui lòng xem nhật ký phía máy chủ`);
      return;
    }
    if (file.status === 'done') {
      uploadUrlList.value.push(file.response.message);
    }
  }

  /**
   * Lấy địa chỉ hình ảnh
   *
   * @param url
   */
  function getImage(url) {
    return getFileAccessHttpUrl(url);
  }

  /**
   * Sự kiện trước khi tải lên
   */
  function beforeUpload(file) {
    var fileType = file.type;
    if (fileType === 'image') {
      if (fileType.indexOf('image') < 0) {
        message.warning('Vui lòng tải lên hình ảnh');
        return false;
      }
    }
    if(uploadUrlList.value && uploadUrlList.value.length > 2){
      message.warning("Tối đa chỉ được tải lên ba hình!");
      return false;
    }
    return true;
  }

  /**
   * Xóa hình ảnh
   */
  function deleteImage(index) {
    uploadUrlList.value.splice(index,1);
    fileInfoList.value.splice(index,1);
  }

  /**
   * Xem trước hình ảnh
   * @param url
   */
  function handlePreview(url){
    const onImgLoad = ({ index, url, dom }) => {
      console.log(`Thứ${index + 1}hình ảnh đã tải, URL là:${url}`, dom);
    };
    let imageList = [getImage(url)];
    createImgPreview({ imageList: imageList, defaultWidth: 700, rememberState: true, onImgLoad });
  }

  /**
   * Cắt chuỗi
   * @param str
   * @param maxLength
   */
  function truncateString(str, maxLength) {
    if (str.length <= maxLength){
      return str;
    }
    let chineseCount = 0;
    let englishCount = 0;
    let digitCount = 0;
    let result = '';
    for (let i = 0; i < str.length; i++) {
      const char = str[i];
      if (/[\u4e00-\u9fa5]/.test(char)) { // Phán đoán có phải chữ Hán hay không
        chineseCount++;
      } else if (/[a-zA-Z]/.test(char)) { // Phán đoán có phải chữ cái tiếng Anh hay không
        englishCount++;
      } else if (/\d/.test(char)) { // Phán đoán có phải số hay không
        digitCount++;
      }
      if (chineseCount + englishCount / 2 + digitCount / 2 > maxLength) {
        break;
      }
      result += char;
    }

    return result;
  }

  /**
   * Sự kiện dán
   * @param event
   */
  function paste(event) {
    if(uploadUrlList.value && uploadUrlList.value.length > 2){
      message.warning("Tối đa chỉ được tải lên ba hình!");
      return;
    }
    const items = (event.clipboardData || window.clipboardData).items;
    if (!items || items.length === 0){
      //Giải thích trình duyệt không hỗ trợ sao chép hình ảnh
      message.error('Trình duyệt hiện tại không hỗ trợ mở hình ảnh cục bộ!');
      return;
    }
    let image = null;
    for (let i = 0; i < items.length; i++) {
      if (items[i].type.indexOf('image') !== -1) {
        image = items[i].getAsFile();
        handleUploadImage(image);
        break;
      }
    }
  }

  /**
   * Dán hình ảnh
   * @param image
   */
  async function handleUploadImage(image) {
    const isReturn = (fileInfo) => {
      try {
        if (fileInfo.code === 0) {
          let { message } = fileInfo;
          uploadUrlList.value.push(message);
          fileInfoList.value.push(image);
        } else if (fileInfo.code === 500 || fileInfo.code === 510) {
          message.error(fileInfo.message || `${image.name} Nhập thất bại`);
        }
      } catch (error) {
        console.log('Dữ liệu nhập vào bất thường', error);
        message.error(`${image.name} Nhập thất bại`);
      }
    };
    // Nhất quán với a-upload: tải lên ẩn danh mang theo appId + shareToken
    let uploadApi = '/airag/chat/upload';
    const appId = appData.value?.id;
    if (appId) {
      const params = new URLSearchParams();
      params.set('appId', appId);
      if (appData.value?.shareToken) {
        params.set('shareToken', appData.value.shareToken);
      }
      uploadApi = `${uploadApi}?${params.toString()}`;
    }
    await defHttp.uploadFile({ url: uploadApi }, { file: image }, { success: isReturn });
  }

  /**
   * Hiển thị kết quả trả về
   * @param readableStream
   * @param options
   */
  async function renderChatByResult(readableStream, options) {
    const reader = readableStream.getReader();
    const decoder = new TextDecoder('UTF-8');
    let conversationId = '';
    let buffer = '';
    let text = ''; // Chia tin nhắn theo giao thức SSE
    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      //update-begin---author:wangshuai---date:2025-03-12---for:【QQYUN-11555】Khi trò chuyện phải hiển thị tin nhắn dạng streaming---
      let result = decoder.decode(value, { stream: true });
      buffer += result;
      // Dùng theo giao thức SSE \n\n Chia sự kiện hoàn chỉnh, phần tử cuối cùng có thể không hoàn chỉnh, cần giữ lại trong buffer
      const parts = buffer.split('\n\n');
      buffer = parts.pop() || '';
      for (let part of parts) {
        if (!part || !part.trim()) {
          continue;
        }
        let content = part.startsWith('data:') ? part.replace('data:', '').trim() : part.trim();
        if (!content) {
          continue;
        }
        //update-begin---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Phát hành lên môi trường thật không cập nhật động theo thời gian thực được, nội dung không tải ra được, phải làm mới mới thấy toàn bộ câu trả lời---
        try {
          if(content.indexOf(":::card:::") !== -1){
            content = content.replace(/\s+/g, '');
          }
          let parse = JSON.parse(content);
          await renderText(parse, conversationId, text, options).then((res) => {
            text = res.returnText;
            conversationId = res.conversationId;
          });
        } catch (error) {
          console.log('Phân tích JSON thất bại, độ dài content:', content.length, ', error:', error);
        }
        //update-end---author:wangshuai---date:2025-03-13---for:【QQYUN-11572】Phát hành lên môi trường thật không cập nhật động theo thời gian thực được, nội dung không tải ra được, phải làm mới mới thấy toàn bộ câu trả lời---
      }
      //update-end---author:wangshuai---date:2025-03-12---for:【QQYUN-11555】Khi trò chuyện phải hiển thị tin nhắn dạng streaming---
    }
    //update-begin---author:scott ---date:20260721  for：【issues/9787】Tương thích phản hồi lỗi không phải SSE, tránh để văn bản lỗi đọng lại trong buffer-----------
    buffer += decoder.decode();
    if (buffer.trim()) {
      let content = buffer.startsWith('data:') ? buffer.replace('data:', '').trim() : buffer.trim();
      try {
        const payload = JSON.parse(content);
        const errorMessage = getChatErrorMessage(payload);
        if (errorMessage) {
          updateChatFail(uuid, errorMessage);
          localStorage.removeItem('chat_requestId_' + uuid.value);
          handleStop();
        } else {
          const result = await renderText(payload, conversationId, text, options);
          text = result.returnText;
          conversationId = result.conversationId;
        }
      } catch (error) {
        console.log('Phân tích JSON thất bại, độ dài content:', content.length, ', error:', error);
      }
    }
    //update-end---author:scott ---date:20260721  for：【issues/9787】Tương thích phản hồi lỗi không phải SSE, tránh để văn bản lỗi đọng lại trong buffer-----------
    //update-begin---author:wangshuai---date:2025-11-05---for: Nếu là kết nối lại sau khi mất kết nối và văn bản trống, cần loại bỏ hai phiên trò chuyện phía trước---
    if(!text && isReConnect && chatData.value.length >1){
      //Nếu là trường hợp kết nối lại sau khi mất kết nối, khi kết quả stream trống, loại bỏ tin nhắn AI giữ chỗ, tránh kết quả trống cũng thêm bản ghi trò chuyện
      const lastMsg = chatData.value[chatData.value.length - 1];
      if (lastMsg && lastMsg.inversion === 'ai' && lastMsg.content === 'Vui lòng chờ') {
        chatData.value.splice(chatData.value.length - 1, 1);
        chatData.value.splice(chatData.value.length - 1, 1);
      }
    //update-end---author:wangshuai---date:2025-11-05---for: Nếu là kết nối lại sau khi mất kết nối và văn bản trống, cần loại bỏ hai phiên trò chuyện phía trước---
      localStorage.removeItem('chat_requestId_' + uuid.value);
      loading.value = false;
    }
  }

  /**
   * ai kết nối lại
   */
  async function aiReConnection() {
    //Truy vấn requestId
    let chat = localStorage.getItem("chat_requestId_" + uuid.value);
    if(chat) {
      let array = JSON.parse(chat);
      let message = array.message;
      let requestId = array.requestId;
      const result = await defHttp.get({ url: '/airag/chat/receive/' + requestId ,
        adapter: 'fetch',
        responseType: 'stream',
        timeout: 60 * 60 * 1000
      }, { isTransformResponse: false }).catch(async (err)=>{
        loading.value = false;
        localStorage.removeItem('chat_requestId_' + uuid.value);
      });
      if(result && message){
        loading.value = true;
        isReConnect.value = true;
        //Gửi tin nhắn người dùng
        addChat(uuid.value, {
          dateTime: new Date().toLocaleString(),
          content: message,
          images:uploadUrlList.value?uploadUrlList.value:[],
          inversion: 'user',
          error: false,
          conversationOptions: null,
          requestOptions: { prompt: message, options: null },
          eventType: 'message',
        });
        let options: any = {};
        const lastContext = conversationList.value[conversationList.value.length - 1]?.conversationOptions;
        if (lastContext && usingContext.value) {
          options = { ...lastContext };
        }
        //Thêm tin nhắn ai
        addChat(uuid.value, {
          dateTime: new Date().toLocaleString(),
          content: 'Vui lòng chờ',
          loading: false,
          inversion: 'ai',
          error: false,
          conversationOptions: null,
          requestOptions: { prompt: message, options: { ...options } },
          referenceKnowledge: [],
          eventType: 'message',
        });
        options.message = message;
        scrollToBottom();
        //Đầu ra streaming
        await renderChatByResult(result,options);
      } else {
        loading.value = false;
        localStorage.removeItem('chat_requestId_' + uuid.value);
        isReConnect.value = false;
      }
    } else {
      isReConnect.value = false;
    }
  }

  //Lắng nghe lời mở đầu
  watch(
    () => props.prologue,
    (val) => {
      try {
        if (val) {
          topChat(val);
        }
      } catch (e) {}
    }
  );

  //Lắng nghe câu hỏi chế sẵn của lời mở đầu
  watch(
    () => props.presetQuestion,
    (val) => {
      topChat(props.prologue);
    }
  );

  //Lắng nghe thông tin ứng dụng
  watch(
    () => props.formState,
    (val) => {
      try {
        if (val) {
          appData.value = val;
          // Kiểm tra mô hình có hỗ trợ tìm kiếm mạng hay không
          checkModelProvider();
        }
      } catch (e) {}
    },
    { deep: true, immediate: true }
  );

  // Chỉnh sửa cài đặt hội thoại
  function handleEditSettings() {
    emit('edit-settings');
  }

  // Chuyển đổi tìm kiếm mạng
  function toggleWebSearch() {
    enableSearch.value = !enableSearch.value;
    if (enableSearch.value) {
      message.success("Đã bật tìm kiếm liên mạng");
    } else {
      message.info("Đã tắt tìm kiếm liên mạng");
    }
  }

  /**
   * Chuyển đổi suy nghĩ mạng
   */
  function toggleThink() {
    enableThink.value = !enableThink.value;
    if (enableThink.value) {
      message.success("Đã bật suy nghĩ sâu");
    } else {
      message.info("Đã tắt suy nghĩ sâu");
    }
  }

  // Kiểm tra mô hình có hỗ trợ tìm kiếm mạng hay không (lấy từ appData.metadata.modelInfo)
  function checkModelProvider() {
    if (appData.value && appData.value.metadata) {
      try {
        const metadata = typeof appData.value.metadata === 'string' 
          ? JSON.parse(appData.value.metadata) 
          : appData.value.metadata;

        //Có hiển thị công cụ vẽ hay không
        showDraw.value = metadata.izDraw === '1';
        // Năng lực vẽ chỉ kiểm soát nút có hiển thị hay không, khi chưa cấu hình rõ ràng thì không mặc định vào chế độ tạo hình ảnh
        enableDraw.value = metadata.defaultSelect === '1';
        
        drawModelId.value = metadata.drawModelId;

        if (metadata && metadata.modelInfo) {
          modelProvider.value = metadata.modelInfo.provider || '';
          modelName.value = metadata.modelInfo.modelName || '';
          showThink.value = modelName.value === 'deepseek-reasoner';
        } else {
          showThink.value = false;
        }
      } catch (e) {
        console.error('Phân tích thông tin mô hình thất bại', e);
        showThink.value = false;
        enableDraw.value = false;
      }
    } else {
      showThink.value = false;
      showDraw.value = false;
      enableDraw.value = false;
    }
  }

  /**
   * Tạo hình ảnh
   */
  function handleGenerateImage() {
    enableDraw.value = !enableDraw.value;
    if (enableDraw.value) {
      message.success("Đã bật tạo hình ảnh");
    } else {
      message.info("Đã tắt tạo hình ảnh");
    }
  }

  //================================================== begin 【QQYUN-14261】AIbegin Trợ lý AI, hỗ trợ năng lực đa phương thức- Tài liệu ====================================
  /**
   * Kiểm chứng trước khi tải lên tệp thông thường
   * 
   * @param file
   */
  function beforeUploadFile(file) {
    const fileName = file.name;
    const fileType = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    const allowFileTypes = ['txt', 'pdf', 'docx', 'doc', 'pptx', 'ppt', 'xlsx', 'xls', 'md'];
    if (allowFileTypes.indexOf(fileType) === -1) {
      message.warning('Không hỗ trợ tải lên loại tệp này, vui lòng tải lên tệp định dạng txt, pdf, docx, doc, pptx, ppt, xlsx, xls, md');
      return false;
    }
    return true;
  }

  /**
   * Tải lên tệp (không phải hình ảnh)
   * @param info
   */
  function handleChangeFile(info) {
    let { file, fileList: newFileList } = info;
    fileList.value = newFileList;
    if (file.status === 'error' || (file.response && file.response.code == 500)) {
      message.error(file.response?.message || `${file.name} Tải lên thất bại, vui lòng xem nhật ký phía máy chủ`);
      return;
    }
    fileUrlList.value = fileList.value
        .filter(item => item.status === 'done' && item.response)
        .map(item => item.response.message);
  }

  /**
   * Xóa tệp
   */
  function deleteFile(index) {
    fileList.value.splice(index, 1);
    fileUrlList.value = fileList.value
        .filter(item => item.status === 'done' && item.response)
        .map(item => item.response.message);
  }
  
  //================================================== end 【QQYUN-14261】AIend Trợ lý AI, hỗ trợ năng lực đa phương thức- Tài liệu ====================================

  //update-begin---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Tên công cụ trong quá trình suy nghĩ được dịch thành tên quy trình và hiển thị lệnh gọi công cụ---
  /**
   * Đọc tên công cụ và tên hiển thị từ nhãn công cụ.
   */
  function parseToolDisplayName(toolTag: string) {
    if (!toolTag || !toolTag.includes('<jeecg-tool-exec')) {
      return null;
    }
    try {
      const box = document.createElement('div');
      box.innerHTML = toolTag;
      const content = box.querySelector('jeecg-tool-exec > data')?.textContent?.trim();
      const record = content ? JSON.parse(content) : null;
      console.log(record);
      if (record?.name && record?.displayName) {
        return { name: record.name, displayName: record.displayName };
      }
    } catch (error) {
      return null;
    }
    return null;
  }

  /**
   * Ẩn tên kỹ thuật của công cụ trong văn bản suy nghĩ.
   */
  function replaceThinkingToolNames(content: string) {
    let displayContent = content;
    thinkingToolDisplayNames.forEach((displayName, name) => {
      displayContent = displayContent
          .split(`${name} "${displayName}"`).join(displayName)
          .split(`${name} “${displayName}”`).join(displayName)
          .split(name).join(displayName);
    });
    return displayContent;
  }

  /**
   * Tìm tin nhắn suy nghĩ gần nhất của vòng này.
   */
  function findThinkingMessageIndex() {
    const activeIndex = activeThinkingIndex.value;
    const activeMessage = chatData.value[activeIndex];
    if (activeMessage && (activeMessage.eventType === 'thinking' || activeMessage.eventType === 'thinking_end')) {
      return activeIndex;
    }
    return -1;
  }

  /**
   * Lưu trạng thái công cụ vào tin nhắn suy nghĩ hiện tại, tránh thẻ công cụ biến mất sau khi tạo câu trả lời.
   */
  function updateThinkingTool(toolTag: string, completed: boolean) {
    // Sự kiện công cụ cần gắn lên tin nhắn suy nghĩ tương ứng, không thể dùng trực tiếp tin nhắn trò chuyện cuối cùng
    const index = findThinkingMessageIndex();
    const thinkingMessage = chatData.value[index];
    if (!thinkingMessage) {
      return false;
    }
    // Trong lúc thực thi lưu nhãn công cụ hiện tại; sau khi thực thi xong làm trống trạng thái đang chạy
    const changes: Record<string, string> = {
      currentToolTag: completed ? '' : toolTag,
    };
    if (completed) {
      // Lưu nhãn công cụ đã hoàn thành, đảm bảo sau khi trả lời kết thúc bản ghi gọi công cụ vẫn hiển thị
      changes.thinkingToolTags = `${thinkingMessage.thinkingToolTags || ''}${toolTag}\n\n`;
    }
    // Lưu vào bộ nhớ đệm tên hiển thị do backend trả về, và thay flow_xxx trong văn bản suy nghĩ bằng tên quy trình
    const toolName = parseToolDisplayName(toolTag);
    if (toolName && typeof thinkingMessage.content === 'string') {
      thinkingToolDisplayNames.set(toolName.name, toolName.displayName);
      changes.content = replaceThinkingToolNames(thinkingMessage.content);
    }
    updateChatSome(uuid.value, index, changes);
    return true;
  }
  //update-end---author:wangshuai---date:20260827---for：【LHZP-1614】【AI】Tên công cụ trong quá trình suy nghĩ được dịch thành tên quy trình và hiển thị lệnh gọi công cụ---

  //Lắng nghe thông tin lịch sử
  watch(
    () => props.historyData,
    (val) => {
      try {
        //update-begin---author:wangshuai---date:2025-03-06---for:【QQYUN-11384】Mở ứng dụng bằng trình duyệt bị mất lời mở đầu---
        if (val && val.length > 0) {
          // update-begin--author:liaozhiyang---date:20260831---for:【LHZP-2020】Thêm node tạo hình ảnh mới (tin nhắn lịch sử khôi phục kết quả văn bản-hình ảnh dựa trên content, outputTypes và outputValues)
          chatData.value = cloneDeep(val).map((item) => {
            const flowOutputSegments = buildFlowOutputSegments(item);
            return flowOutputSegments.some((segment) => segment.type === 'image') ? { ...item, flowOutputSegments } : item;
          });
          // update-end--author:liaozhiyang---date:20260831---for:【LHZP-2020】Thêm node tạo hình ảnh mới (tin nhắn lịch sử khôi phục kết quả văn bản-hình ảnh dựa trên content, outputTypes và outputValues)
          if(chatData.value[0]){
            topicId.value = chatData.value[0].topicId
          }
        }else{
          chatData.value = [];
          headerTitle.value = props.chatTitle;
        }
        //update-begin---author:wangshuai---date:2025-11-18---for:【QQYUN-14049】【AI】Không có lời mở đầu thì không hiển thị câu hỏi đặt sẵn nữa---
        if((props.prologue || props.presetQuestion) && props.chatTitle){
        //update-end---author:wangshuai---date:2025-11-18---for:【QQYUN-14049】【AI】Không có lời mở đầu thì không hiển thị câu hỏi đặt sẵn nữa---
          topChat(props.prologue)
        }
        //ai trả lời kết nối lại
        aiReConnection();
      } catch (e) {
        console.log(e)
      }
      //update-end---author:wangshuai---date:2025-03-06---for:【QQYUN-11384】Mở ứng dụng bằng trình duyệt bị mất lời mở đầu---
    },
    { deep: true, immediate: true }
  );

  onMounted(() => {
    scrollToBottom();
    uploadUrlList.value = [];
    fileInfoList.value = [];
    fileUrlList.value = [];
    fileList.value = [];
    // Kiểm tra mô hình có hỗ trợ tìm kiếm mạng hay không
    checkModelProvider();
  });
</script>

<style lang="less" scoped>
  .chatWrap {
    width: 100%;
    height: 100%;
    padding: 20px;
    .content {
      height: 100%;
      width: 100%;
      background: #fff;
      display: flex;
      flex-direction: column;
    }
  }
  .main {
    flex: 1;
    min-height: 0;
    .scrollArea {
      overflow-x: hidden;
      overflow-y: auto;
      height: 100%;

      // Trả lời dài sẽ nén thanh trượt theo tỷ lệ nội dung, chỗ này chỉ tăng khả năng hiển thị của thanh cuộn khu vực trò chuyện, tránh ảnh hưởng kiểu toàn cục.
      &::-webkit-scrollbar {
        width: 10px;
      }

      &::-webkit-scrollbar-thumb {
        min-height: 48px;
        border-radius: 5px;
        background-color: rgba(144, 147, 153, 0.55);
      }

      &::-webkit-scrollbar-thumb:hover {
        background-color: rgba(96, 98, 102, 0.75);
      }
    }
    .chatContentArea {
      padding: 10px;
    }
  }
  .emptyArea {
    display: flex;
    justify-content: center;
    align-items: center;
    color: #d4d4d4;
  }
  .stopArea {
    display: flex;
    justify-content: center;
    padding: 10px 0;
  }
  .footer {
    display: flex;
    flex-direction: column;
    padding: 6px 16px;
    .topArea {
      padding-left: 6%;
      margin-bottom: 6px;
    }
    .bottomArea {
      display: flex;
      align-items: center;

      .ant-input {
        padding-left: 12px;
      }
      .ant-input,
      .ant-btn {
        height: 36px;
      }
      textarea.ant-input {
        padding-top: 6px;
        padding-bottom: 6px;
      }
      .contextBtn,
      .delBtn {
        padding: 0;
        width: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
      .delBtn {
        margin-right: 8px;
      }
      .contextBtn {
        color: #a8071a;
        &.enabled {
          color: @primary-color;
        }
        font-size: 18px;
      }
      .sendBtn {
        font-size: 12px;
        width: 100%;
        display: flex;
        padding: 4px 6px;
        align-items: center;
        &.enabled {
          color: #0a66ff !important;
        }
      }
      .webSearchBtn {
        border-radius: 8px;
        padding: 4px 8px;
        height: 30px;
        background-color: transparent;
        border: 1px solid transparent;
        color: #3d4353;
        transition: all .2s ease;
        :deep(.anticon){
          margin-right: 6px;
          color: #3d4353;
        }
        :deep(.anticon + span),
        :deep(span + .anticon) {
          margin-inline-start: 0;
        }
        &:hover{
          border-color: #d2d7e5;
          background-color: #f7f9fc;
        }
        &.enabled {
          background-color: rgba(10,102,255,0.08);
          border-color: #0a66ff;
          color: #0a66ff;
          box-shadow: none;
          font-weight: 500;
          :deep(.anticon) {
            color: #0a66ff !important;
          }
        }
      }
      .stopBtn {
        width: 32px;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 8px;
      }
    }
  }
  :deep(.chatgpt .markdown-body) {
    background-color: #f4f6f8;
  }
  :deep(.ant-message) {
    top: 50% !important;
  }
  .header-title{
    color: #101828;
    font-size: 16px;
    font-weight: 400;
    padding-bottom: 8px;
    margin-left: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 30px;
    
    .title-content{
      display: flex;
      align-items: center;
      gap: 4px;
      overflow: hidden;
      
      > span{
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    
    .header-actions{
      display: flex;
      align-items: center;
      gap: 8px;
      flex-shrink: 0;
    }
    
    .edit-btn{
      padding: 2px 4px;
      color: #999;
      flex-shrink: 0;
      height: 24px;
      
      &:hover{
        color: @primary-color;
      }
      
      :deep(.anticon){
        font-size: 16px;
      }
    }
    
    .header-advertisint{
      display:flex;
      margin-right: 20px;
      font-size: 12px;
    }
  }
  .chat-textarea{
    display: flex;
    align-items: center;
    width: 100%;
    border-radius: 15px;
    border-style: solid;
    border-width: 1px;
    flex-direction: column;
    transition: width 0.3s;
    border-color: #d2d7e5;
    .textarea-top{
      border-bottom: 1px solid #f0f0f5;
      padding: 12px 28px;
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      .top-image{
        display: flex;
        img{
          border-radius: 8px;
          cursor: pointer;
          height: 60px;
          position: relative;
          width: 60px;
        }
      }
      /*begin Kiểu của tệp*/
      .file-card {
        display: flex;
        align-items: center;
        background: #f4f6f8;
        border-radius: 8px;
        padding: 8px 12px;
        width: 200px;
        position: relative;
        
        .file-card-icon {
          width: 32px;
          height: 32px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 8px;
          .file-thumb {
            width: 32px;
            height: 32px;
            border-radius: 4px;
            object-fit: cover;
          }
        }
        
        .file-card-info {
          flex: 1;
          overflow: hidden;
          .file-name {
            font-size: 14px;
            color: #333;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          .file-size {
            font-size: 12px;
            color: #999;
          }
        }
        
        .file-card-close {
          position: absolute;
          top: -6px;
          right: -6px;
          width: 16px;
          height: 16px;
          background: #ccc;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          color: #fff;
          font-size: 10px;
          opacity: 0;
          transition: opacity 0.2s;
          
          &:hover {
            background: #ff4d4f;
          }
        }
        
        &:hover .file-card-close {
          opacity: 1;
        }
        /*end Kiểu của tệp*/
      }
    }
    .textarea-bottom{
      display: flex;
      flex-direction: column;
      flex: 1 1;
      min-height: 48px;
      position: relative;
      padding: 2px 4px;
      width: 100%;
      /*begin Kiểu phần đáy*/
      .textarea-action-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        margin-top: 8px;

        .left-actions {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .right-actions {
          display: flex;
          align-items: center;
          gap: 4px;
        }
        .sendBtn {
          width: auto;
          padding: 4px 6px;
          height: 30px;
        }
      }
      /*end Kiểu phần đáy*/
    }
  }
  .chat-textarea:hover{
    border-color: #9dc1fb;
  }
  .textarea-active{
    border-color: #98bdfa !important;
  }
  :deep(.ant-divider-vertical){
    margin: 0 2px;
  }
  .upload-icon{
    cursor: pointer;
    position: absolute;
    background-color: #1D1C23;
    color: white;
    border-radius: 50%;
    padding: 4px;
    display: none;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 4px #e6e6e6;
    margin-left: 44px;
    margin-top: -4px;
  }
  .top-image:hover{
    .upload-icon{
      display: flex;
    }
  }
  
  @media (max-width: 600px) {
    //Kiểu trên điện thoại, máy tính bảng không cần điều chỉnh
    .footer{
      padding: 0;
      .bottomArea{
        .delBtn{
          margin-right: 0;
        }
      }
    }
    .chatWrap{
      padding: 10px 10px 10px 0;
    }
    .main .chatContentArea{
      padding: 10px 0 0 10px;
    }
  }
</style>
<style lang="less">
 .ai-chat-message{
   z-index: 9999 !important;
 }

 .chat-upload-dropdown .ant-dropdown-menu{
   border-radius: 10px;
   padding: 6px 4px;
 }
 .chat-upload-dropdown .ant-dropdown-menu-item:hover{
   background-color: #f0f6ff;
 }
</style>
