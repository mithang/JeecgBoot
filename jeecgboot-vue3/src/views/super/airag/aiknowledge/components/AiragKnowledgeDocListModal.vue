<!--Danh sách tài liệu kho tri thức-->
<template>
  <div class="p-2">
    <BasicModal
      wrapClassName="airag-knowledge-doc"
      destroyOnClose
      @register="registerModal"
      :canFullscreen="false"
      defaultFullscreen
      :title="title"
      :footer="null"
    >
      <a-layout style="height: 100%">
        <a-layout-sider :style="siderStyle">
          <a-menu v-model:selectedKeys="selectedKeys" mode="vertical" style="border: none" :items="menuItems" @click="handleMenuClick" />
        </a-layout-sider>
        <a-layout-content :style="contentStyle">
          <div v-if="selectedKey === 'document'">
            <div class="search-header" style="text-align: left;">
              <a-space align="center" wrap>
                <a-input
                    class="search-input"
                    v-model:value="searchText"
                    placeholder="Vui lòng nhập tên tài liệu, nhấn Enter để tìm kiếm"
                    @pressEnter="searchTextEnter"
                />
                <template v-if="selectedRows.length > 0">
                  <div>
                    <span>Đã vào chế độ chọn nhiều, hiện đang chọn</span>
                    <a style="margin: 0 4px;"> {{ selectedRows.length }} </a>
                    <span>tài liệu</span>
                  </div>
                  <div>
                    <a @click="onClearSelected">Làm trống lựa chọn</a>
                    <a-divider type="vertical"/>
                    <a @click="onDeleteBatch">Xóa hàng loạt</a>
                  </div>
                </template>
              </a-space>
            </div>
            <a-row :span="24" class="knowledge-row">
              <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-if="!type || type==='knowledge'">
                <a-card class="add-knowledge-card" :bodyStyle="cardBodyStyle">
                  <span style="line-height: 18px;font-weight: 500;color:#676f83;font-size: 12px">
                    Tạo tài liệu
                    <a-tooltip title="Tài liệu kho tri thức">
                      <a style="color: unset" href="https://help.jeecg.com/aigc/guide/knowledge#4-%E7%9F%A5%E8%AF%86%E5%BA%93%E6%96%87%E6%A1%A3" target="_blank">
                        <Icon style="position:relative;top:1px" icon="ant-design:question-circle-outlined" size="14"></Icon>
                      </a>
                    </a-tooltip>
                  </span>
                  <div class="add-knowledge-doc" @click="handleCreateText">
                    <Icon icon="ant-design:form-outlined" size="13"></Icon><span>Nhập thủ công</span>
                  </div>
                  <div class="add-knowledge-doc" @click="handleCreateUpload">
                    <Icon icon="ant-design:cloud-upload-outlined" size="13"></Icon><span>Tải lên tệp</span>
                  </div>
                  <div class="add-knowledge-doc" @click="handleCreateWeb">
                    <Icon icon="ant-design:global-outlined" size="13"></Icon><span>Nhập trang web</span>
                  </div>
                  <div class="add-knowledge-doc">
                    <a-upload
                        accept=".zip"
                        name="file"
                        :data="{ knowId: knowledgeId }"
                        :showUploadList="false"
                        :headers="headers"
                        :beforeUpload="beforeUpload"
                        :action="uploadUrl"
                        @change="handleUploadChange"
                        style="width: 100%;"
                    >
                        <div style="display: flex;width: 100%">
                          <Icon style="margin-left: 0;color:#676f83" icon="ant-design:project-outlined" size="13"></Icon>
                          <span style="color:#676f83;font-size: 12px">Tải lên kho tài liệu</span>
                        </div>
                    </a-upload>
                  </div>
                  <a-dropdown placement="bottomRight" :trigger="['click']">
                    <div class="ant-dropdown-link pointer operation" @click.prevent.stop>
                      <Icon icon="ant-design:ellipsis-outlined" size="16"></Icon>
                    </div>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item key="delete" @click="onDeleteAll">
                          <Icon icon="ant-design:delete-outlined" size="16"></Icon>
                          Làm trống tài liệu
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-card>
              </a-col>
              <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in knowledgeDocDataList">
                <a-card :class="['knowledge-card','pointer', {
                  checked: item.__checked,
                }]" @click="handleEdit(item)">
                  <div class="knowledge-checkbox">
                    <a-checkbox v-model:checked="item.__checked" @click.stop=""/>
                  </div>
                  <div class="knowledge-header">
                    <div class="header-text flex">
                      <Icon v-if="item.type==='text'" icon="ant-design:file-text-outlined" size="32" color="#00a7d0"></Icon>
                      <Icon v-if="item.type==='web'" icon="ant-design:global-outlined" size="32" color="#1890ff"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'pdf'" icon="ant-design:file-pdf-outlined" size="32" color="rgb(211, 47, 47)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'docx'" icon="ant-design:file-word-outlined" size="32" color="rgb(68, 138, 255)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'pptx'" icon="ant-design:file-ppt-outlined" size="32" color="rgb(245, 124, 0)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'xlsx'" icon="ant-design:file-excel-outlined" size="32" color="rgb(98, 187, 55)"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'txt'" icon="ant-design:file-text-outlined" size="32" color="#00a7d0"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === 'md'" icon="ant-design:file-markdown-outlined" size="32" color="#292929"></Icon>
                      <Icon v-if="item.type==='file' && getFileSuffix(item.metadata) === ''" icon="ant-design:file-unknown-outlined" size="32" color="#f5f5dc"></Icon>
                      <span class="ellipsis header-title">{{ item.title }}</span>
                    </div>
                  </div>
                  <div class="card-description">
                    <span>{{ item.content }}</span>
                  </div>
                  <div class="flex" style="justify-content: space-between">
                    <div class="card-text">
                      Trạng thái:
                      <div v-if="item.status==='complete'" class="card-text-status">
                        <Icon icon="ant-design:check-circle-outlined" size="16" color="#56D1A7"></Icon>
                        <span class="ml-2">Đã hoàn thành</span>
                      </div>
                      <div v-else-if="item.status==='building'" class="card-text-status">
                        <a-spin v-if="item.loading" :spinning="item.loading" :indicator="indicator"></a-spin>
                        <span class="ml-2">Đang xây dựng</span>
                      </div>
                      <div v-else-if="item.status==='draft'" class="card-text-status">
                        <img src="../icon/draft.png" style="width: 16px;height: 16px" />
                        <span class="ml-2">Bản nháp</span>
                      </div>
                      <a-tooltip v-else-if="item.status==='failed'" :title="getDocFailedReason(item)">
                        <div class="card-text-status">
                          <Icon icon="ant-design:close-circle-outlined" size="16" color="#FF4D4F"></Icon>
                          <span class="ml-2">Thất bại</span>
                        </div>
                      </a-tooltip>
                    </div>
                    <a-dropdown placement="bottomRight" :trigger="['click']">
                      <div class="ant-dropdown-link pointer operation" @click.prevent.stop>
                        <Icon icon="ant-design:ellipsis-outlined" size="16"></Icon>
                      </div>
                      <template #overlay>
                        <a-menu>
                          <a-menu-item key="vectorization" @click="handleVectorization(item.id)">
                            <Icon icon="ant-design:retweet-outlined" size="16"></Icon>
                            Vector hóa
                          </a-menu-item>
                          <a-menu-item key="edit" @click="handleEdit(item)">
                            <Icon icon="ant-design:edit-outlined" size="16"></Icon>
                            Chỉnh sửa
                          </a-menu-item>
                          <a-menu-item key="delete" @click="handleDelete(item.id)">
                            <Icon icon="ant-design:delete-outlined" size="16"></Icon>
                            Xóa
                          </a-menu-item>
                        </a-menu>
                      </template>
                    </a-dropdown>
                  </div>
                </a-card>
              </a-col>
            </a-row>
            <Pagination
              v-if="knowledgeDocDataList.length > 0"
              :current="pageNo"
              :page-size="pageSize"
              :page-size-options="pageSizeOptions"
              :total="total"
              :showQuickJumper="true"
              :showSizeChanger="true"
              @change="handlePageChange"
              class="list-footer"
              size="small"
              :show-total="() => `Tổng cộng${total}Mục` "
            />
          </div>

          <div v-if="selectedKey === 'hitTest'" style="padding: 16px">
            <a-spin :spinning="spinning">
              <div class="hit-test">
                <h4>Kiểm tra trúng</h4>
                <span>Gỡ lỗi tình huống khớp đoạn theo câu hỏi của người dùng, đảm bảo hiệu quả trả lời.</span>
              </div>
              <div class="content">
                <div class="content-title">
                  <Avatar v-if="hitShowSearchText" :size="35" :src="avatar" />
                  <span>{{ hitShowSearchText }}</span>
                </div>
                <div class="content-card">
                  <a-row :span="24" class="knowledge-row" v-if="hitTextList.length>0">
                    <a-col :xxl="6" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in hitTextList">
                      <a-card class="hit-card pointer" style="border-color: #ffffff" @click="hitTextDescClick(item)">
                        <div class="card-title">
                          <div style="display: flex;">
                            <Icon icon="ant-design:appstore-outlined" size="14"></Icon>
                            <span style="margin-left: 4px">Chunk-{{item.chunk}}</span>
                            <span style="margin-left: 10px">{{ item.content.length }} Ký tự</span>
                          </div>
                          <a-tag class="card-title-tag" color="#a9c8ff">
                            <span>{{ getTagTxt(item.score) }}</span>
                          </a-tag>
                        </div>
                        <div class="card-description">
                          {{ item.content }}
                        </div>
                        <div class="card-footer">
                          {{item.docName}}
                        </div>
                      </a-card>
                    </a-col>
                  </a-row>
                  <div v-else-if="notHit">
                    <a-empty :image-style="{ margin: '0 auto', height: '160px', verticalAlign: 'middle', borderStyle: 'none' }">
                      <template #description>
                        <div style="margin-top: 26px; font-size: 20px; color: #000; text-align: center !important">
                          Không có đoạn nào trúng
                        </div>
                      </template>
                    </a-empty>
                  </div>
                </div>
              </div>
              <div class="param">
                <span style="font-weight: bold; font-size: 16px">Cấu hình tham số</span>
                <ul>
                  <li>
                    <span>Số mục:</span>
                    <a-input-number :min="1" v-model:value="topNumber"></a-input-number>
                  </li>
                  <li>
                    <span>Ngưỡng Score:</span>
                    <a-input-number :min="0" :step="0.01" :max="1" v-model:value="similarity"></a-input-number>
                  </li>
                </ul>
              </div>
              <div class="hit-test-footer">
                <a-input v-model:value="hitText" size="large" placeholder="Vui lòng nhập" style="width: 100%" @pressEnter="hitTestClick">
                  <template #suffix>
                    <Icon icon="ant-design:send-outlined" style="transform: rotate(-33deg); cursor: pointer" size="22" @click="hitTestClick"></Icon>
                  </template>
                </a-input>
              </div>
            </a-spin>
          </div>
        </a-layout-content>
      </a-layout>
      <Loading tip="Đang tải lên, vui lòng chờ" :loading="uploadLoading"></Loading>
    </BasicModal>

    <!--  Nhập văn bản thủ công  -->
    <AiragKnowledgeDocTextModal @register="docTextRegister" @success="handleSuccess"></AiragKnowledgeDocTextModal>
    <!--  Chi tiết văn bản  -->
    <AiTextDescModal @register="docTextDescRegister"></AiTextDescModal>
  </div>
</template>

<script lang="tsx">
  import { onBeforeMount, computed, ref, unref, h } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { knowledgeDocList, knowledgeDeleteBatchDoc, knowledgeDeleteAllDoc, knowledgeRebuildDoc, knowledgeEmbeddingHitTest } from '../AiKnowledgeBase.api';
  import { doDeleteAllDoc } from '../AiKnowledgeBase.api.util';
  import { ActionItem, BasicTable, TableAction } from '@/components/Table';
  import { useListPage } from '@/hooks/system/useListPage';
  import AiragKnowledgeDocTextModal from './AiragKnowledgeDocTextModal.vue';
  import AiTextDescModal from './AiTextDescModal.vue';
  import { useMessage } from '@/hooks/web/useMessage';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import {Avatar, message, Modal, Pagination} from 'ant-design-vue';
  import { useUserStore } from '@/store/modules/user';
  import { getFileAccessHttpUrl, getHeaders } from '@/utils/common/compUtils';
  import defaultImg from '/@/assets/images/header.jpg';
  import Icon from "@/components/Icon";
  import { useGlobSetting } from '/@/hooks/setting';
  import Loading from '@/components/Loading/src/Loading.vue';

  export default {
    name: 'AiragKnowledgeDocListModal',
    components: {
      Icon,
      Pagination,
      Avatar,
      LoadingOutlined,
      TableAction,
      BasicTable,
      BasicModal,
      AiragKnowledgeDocTextModal,
      AiTextDescModal,
      Loading,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      //Tiêu đề
      const title = ref<string>('Chi tiết kho tri thức');

      //Lưu hoặc sửa đổi
      const knowledgeId = ref<string>('');

      //Giá trị chọn khi khởi tạo menu
      const selectedKeys = ref(['document']);
      //key được chọn khi nhấp menu, dùng để hiển thị và ẩn table
      const selectedKey = ref<string>('document');
      //Văn bản tìm kiếm có định hướng
      const hitText = ref<string>('');
      //Văn bản hiển thị có định hướng
      const hitShowSearchText = ref<string>('');
      //Hiệu ứng tải
      const spinning = ref<boolean>(false);
      //Điểm tối thiểu 0-1
      const similarity = ref<number>(0.65);
      //Số mục
      const topNumber = ref<number>(5);
      //Trả về tập kết quả có định hướng
      const hitTextList = ref<any>([]);
      //Ảnh đại diện người dùng
      const avatar = ref<any>('');
      const userStore = useUserStore();
      //Danh sách tài liệu
      const knowledgeDocDataList = ref<any>([]);
      //Số trang hiện tại
      const pageNo = ref<number>(1);
      //Số mục mỗi trang
      const pageSize = ref<number>(10);
      //Tổng số mục
      const total = ref<number>(0);
      //Số trang có thể chọn
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //Tham số truy vấn
      const searchText = ref<string>('');
      //Có không trúng hay không
      const notHit = ref<boolean>(false);
      //Nhiệm vụ định kỳ làm mới danh sách
      const timer = ref<any>(null);
      //token
      const headers = getHeaders();
      const globSetting = useGlobSetting();
      //Đường dẫn tải lên
      const uploadUrl = ref<string>(globSetting.domainUrl+"/airag/knowledge/doc/import/zip");
      //Đang tải lên
      const uploadLoading = ref<boolean>(false);

      //Mục menu
      const menuItems = ref<any>([
        {
          key: 'document',
          icon: '',
          label: 'Tài liệu',
          title: 'Tài liệu',
        },
        {
          key: 'hitTest',
          icon: '',
          label: 'Kiểm tra trúng',
          title: 'Kiểm tra trúng',
        },
      ]);

      // Dòng hiện đang chọn
      const selectedRows = computed(() => knowledgeDocDataList.value.filter(item => item.__checked))

      //Đăng ký modal
      const [docTextRegister, { openModal: docTextOpenModal }] = useModal();
      const [docTextDescRegister, { openModal: docTextDescOpenModal }] = useModal();
      const type = ref<string>('');
      // metadata chiến lược phân đoạn của kho tri thức
      const knowledgeMetadata = ref<string>('');
      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        knowledgeId.value = data.id;
        selectedKeys.value = ['document'];
        selectedKey.value = 'document';
        spinning.value = false;
        notHit.value = false;
        type.value = data.type;
        knowledgeMetadata.value = data.knowledgeMetadata || '';
        await reload();
        setModalProps({ confirmLoading: false });
      });

      const contentStyle = {
        textAlign: 'center',
        height: '100%',
        width: '80%',
        background: '#ffffff',
      };

      const siderStyle = {
        textAlign: 'center',
        width: '20%',
        background: '#ffffff',
        borderRight: '1px solid #cecece',
      };

      /**
       * Chỉ thị tải
       */
      const indicator =  h(LoadingOutlined, {
        style: {
          fontSize: '16px',
          marginRight: '2px'
        },
        spin: true,
      });

      const { createMessage, createConfirmSync } = useMessage();

      /**
       * Nhập văn bản thủ công
       */
      function handleCreateText() {
        //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
        docTextOpenModal(true, { knowledgeId: knowledgeId.value, type: "text", knowledgeMetadata: knowledgeMetadata.value, knowledgeType: type.value });
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
      }

      /**
       * Tải lên tệp
       */
      function handleCreateUpload() {
        //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
        docTextOpenModal(true, { knowledgeId: knowledgeId.value, type: "file", knowledgeMetadata: knowledgeMetadata.value, knowledgeType: type.value });
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
      }

      /**
       * Địa chỉ mạng web
       */
      function handleCreateWeb() {
        //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
        docTextOpenModal(true, { knowledgeId: knowledgeId.value, type: "web", knowledgeMetadata: knowledgeMetadata.value, knowledgeType: type.value });
        //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
      }

      /**
       * Chỉnh sửa
       */
      function handleEdit(record) {
        // Phán đoán nếu có dòng được chọn, thì đó là chế độ thao tác hàng loạt
        if (selectedRows.value.length > 0) {
          record.__checked = !record.__checked;
          return
        }


        if (record.type === 'text' || record.type === 'file' || record.type === 'web') {
          docTextOpenModal(true, {
            record,
            isUpdate: true,
            knowledgeMetadata: knowledgeMetadata.value,
            knowledgeType: type.value,
          });
        }
      }

      /**
       * Xóa
       * @param id
       */
      function handleDelete(id) {
        Modal.confirm({
          title: 'Gợi ý',
          content: 'Xác nhận muốn xóa tài liệu này?',
          okText: 'Xác nhận',
          cancelText: 'Hủy',
          onOk: () => {
            if(knowledgeDocDataList.value.length == 1 && pageNo.value > 1) {
              pageNo.value = pageNo.value - 1;
            }
            knowledgeDeleteBatchDoc({ ids: id }, reload);
          }
        })
      }

      function getDocFailedReason(doc) {
        let metadata = doc?.metadata;
        if (!metadata) {
          return 'Xây dựng thất bại, nguyên nhân không rõ';
        }
        try {
          metadata = JSON.parse(metadata);
          return metadata?.failedReason || 'Xây dựng thất bại, nguyên nhân không rõ';
        } catch (e) {
          console.log('getDocFailedReason', e);
          return 'Xây dựng thất bại, nguyên nhân không rõ';
        }
      }

      /**
       * Vector hóa
       *
       * @param id
       */
      async function handleVectorization(id) {
        await knowledgeRebuildDoc({ docIds: id }, handleSuccess);
      }

      /**
       * Callback thành công khi thêm mới và chỉnh sửa tài liệu
       */
      function handleSuccess() {
        clearInterval(timer.value);
        timer.value = null;
        reload();
        triggeringTimer();
      }

      /**
       * Kích hoạt nhiệm vụ định kỳ
       */
      function triggeringTimer() {
        timer.value = setInterval(() => {
          reload();
        },5000)
      }

      /**
       * Sự kiện nhấp menu
       * @param value
       */
      function handleMenuClick(value) {
        if (value.key === 'document') {
          setTimeout(() => {
            pageNo.value = 1;
            pageSize.value = 10;
            searchText.value = "";

            reload();
          });
        } else {
          hitTextList.value = [];
          hitShowSearchText.value = '';
          hitText.value = '';
          avatar.value = '';
          similarity.value = 0.65;
          topNumber.value = 5;
        }
        selectedKey.value = value.key;
      }

      /**
       * Kiểm tra trúng
       */
      function hitTestClick() {
        if (hitText.value) {
          spinning.value = true;
          knowledgeEmbeddingHitTest({
            queryText: hitText.value,
            knowId: knowledgeId.value,
            topNumber: topNumber.value,
            similarity: similarity.value,
          }).then((res) => {
            if (res.success) {
              if (res.result) {
                hitTextList.value = res.result;
              } else {
                hitTextList.value = [];
              }
            }
            hitShowSearchText.value = hitText.value;
            avatar.value = userStore.getUserInfo.avatar ? getFileAccessHttpUrl(userStore.getUserInfo.avatar) : defaultImg;
            hitText.value = '';
            notHit.value = hitTextList.value.length == 0;
            spinning.value = false;
          }).catch(()=>{
            spinning.value = false;
          });
        }
      }

      /**
       * Lấy văn bản
       * @param value
       */
      function getTagTxt(value) {
        return 'score' + ' ' + value.toFixed(2);
      }

      /**
       * Sự kiện nhấp thẻ kiểm tra trúng
       * @param values
       */
      function hitTextDescClick(values) {
        docTextDescOpenModal(true, { ...values });
      }

      /**
       * Tải bảng
       */
      async function reload() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          knowledgeId: knowledgeId.value,
          title: '*' + searchText.value + '*',
          column: 'createTime',
          order: 'desc'
        };
        await knowledgeDocList(params).then((res) => {
          if (res.success) {
            //update-begin---author:wangshuai---date:2025-03-21---for:【QQYUN-11636】Chức năng vector hóa đổi thành bất đồng bộ---
            if(res.result.records){
              let clearTimer = true;
              for (const item of res.result.records) {
                if(item.status && item.status === 'building' ){
                  clearTimer = false;
                  item.loading = true;
                }else{
                  item.loading = false;
                }
              }
              if(clearTimer){
                clearInterval(timer.value);
              }
            }
            //update-end---author:wangshuai---date:2025-03-21---for:【QQYUN-11636】Chức năng vector hóa đổi thành bất đồng bộ---
            knowledgeDocDataList.value = res.result.records.map((item)=>{
              item.__checked = false;
              return item;
            });
            total.value = res.result.total;
          } else {
            knowledgeDocDataList.value = [];
            total.value = 0;
          }
        });
      }

      /**
       * Sự kiện thay đổi phân trang
       * @param page
       * @param current
       */
      function handlePageChange(page, current) {
        pageNo.value = page;
        pageSize.value = current;
        reload();
      }

      /**
       * Lấy hậu tố tệp
       */
      function getFileSuffix(metadata) {
        if(metadata){
          let filePath = JSON.parse(metadata).filePath;
          const index = filePath.lastIndexOf('.');
          return index > 0 ? filePath.substring(index + 1).toLowerCase() : '';
        }
        return '';
      }

      /**
       * Sự kiện trước khi tải lên
       */
      function beforeUpload(file) {
        let fileType = file.type;
        if (fileType !== 'application/zip' && fileType !== 'application/x-zip-compressed') {
            createMessage.warning('Vui lòng tải lên tệp zip');
            return false;
        }
        uploadLoading.value = true;
        return true;
      }

      /**
       * Sự kiện callback tải lên tệp
       * @param info
       */
      function handleUploadChange(info) {
        let { file } = info;
        if (file.status === 'error' || (file.response && file.response.code == 500)) {
          createMessage.error(file.response?.message ||`${file.name} Tải lên thất bại, vui lòng xem nhật ký phía máy chủ`);
          uploadLoading.value = false;
          return;
        }
        if (file.status === 'done') {

          if(!file.response.success){
            createMessage.warning(file.response.message);
            uploadLoading.value = false;
            return;
          }
          uploadLoading.value = false;
          createMessage.success(file.response.message);
          handleSuccess();
        }
      }

      function onClearSelected() {
        knowledgeDocDataList.value.forEach(item => {
          item.__checked = false;
        });
      }

      // Làm trống tài liệu
      async function onDeleteAll() {
        pageNo.value = 1;
        doDeleteAllDoc(knowledgeId.value, reload);
      }

      // Xóa hàng loạt
      async function onDeleteBatch() {
        const flag = await createConfirmSync({ title: 'Xóa hàng loạt', content: `Xác nhận muốn xóa cái này ${selectedRows.value.length} dữ liệu?` });
        if (!flag) {
          return;
        }
        const ids = selectedRows.value.map(item => item.id)
        let number = knowledgeDocDataList.value.length - ids.length;
        if(number == 0 && pageNo.value > 1) {
          pageNo.value = pageNo.value - 1;
        }
        knowledgeDeleteBatchDoc({ ids }, reload);
      }

      /**
       * Nhấn Enter để tìm kiếm
       */
      function searchTextEnter(){
        pageNo.value = 1;
        reload();
      }

      onBeforeMount(()=>{
        clearInterval(timer.value);
        timer.value = null;
      })

      return {
        registerModal,
        title,
        docTextRegister,
        handleCreateText,
        beforeUpload,
        handleCreateUpload,
        handleCreateWeb,
        handleSuccess,
        contentStyle,
        siderStyle,
        selectedKeys,
        menuItems,
        handleMenuClick,
        selectedKey,
        hitTestClick,
        hitText,
        spinning,
        similarity,
        topNumber,
        hitShowSearchText,
        avatar,
        hitTextList,
        getTagTxt,
        docTextDescRegister,
        hitTextDescClick,
        knowledgeDocDataList,
        handleEdit,
        handleDelete,
        getDocFailedReason,
        handleVectorization,
        pageNo,
        pageSize,
        pageSizeOptions,
        total,
        handlePageChange,
        searchText,
        reload,
        cardBodyStyle:{ textAlign: 'left', width: '100%' },
        getFileSuffix,
        notHit,
        indicator,
        headers,
        uploadUrl,
        handleUploadChange,
        knowledgeId,
        uploadLoading,
        selectedRows,
        onClearSelected,
        onDeleteAll,
        onDeleteBatch,
        searchTextEnter,
        type,
      };
    },
  };
</script>

<style scoped lang="less">
  .pointer {
    cursor: pointer;
  }

  .hit-test {
    box-sizing: border-box;
    flex-wrap: wrap;
    text-align: left;
    display: flex;
    margin-bottom: 10px;

    h4 {
      font-weight: bold;
      font-size: 16px;
      align-self: center;
      margin-bottom: 0;
      color: #1f2329;
    }

    span {
      margin-left: 10px;
      color: #8f959e;
      font-weight: 400;
      align-self: center;
      margin-top: 2px;
    }
  }

  .hit-test-footer {
    margin-top: 10px;
    display: flex;
  }
  .param {
    text-align: left;
    margin-top: 10px;
    ul {
      margin-top: 10px;
      display: flex;
      li {
        align-items: center;
        margin-right: 10px;
        display: flex;
      }
    }
    border-bottom: 1px solid #cec6c6;
  }
  .content {
    height: calc(100vh - 300px);
    padding: 8px;
    border-radius: 10px;
    background-color: #f9fbfd;
    overflow-y: auto;
    .content-title {
      font-size: 16px;
      font-weight: 600;
      text-align: left;
      margin-left: 10px;
      display: flex;
      span {
        margin-left: 4px;
        font-size: 20px;
        align-self: center;
      }
    }
    .content-card {
      margin-top: 20px;
      margin-left: 10px;
      .hit-card {
        height: 160px;
        margin-bottom: 10px;
        margin-right: 10px;
        border-radius: 10px;
        background: #fcfcfd;
        border: 1px solid #f0f0f0;
        box-shadow: 0 2px 4px #e6e6e6;
        transition: all 0.3s ease;
        .card-title {
          justify-content: space-between;
          color: #887a8b;
          font-size: 14px;
          display: flex;
        }
      }
    }
  }
  .hit-card:hover {
    box-shadow: 0 6px 12px #d0d3d8 !important;
  }
  .pointer {
    cursor: pointer;
  }

  .card-description {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 4;
    height: 6em;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.5;
    margin-top: 16px;
    text-align: left;
    font-size: 12px;
    color: #676F83;
  }

  .card-title-tag {
    color: #477dee;
  }

  .knowledge-row {
    padding: 16px;
    overflow-y: auto;
  }

  .add-knowledge-card {
    border-radius: 10px;
    margin-bottom: 20px;
    display: inline-flex;
    font-size: 16px;
    height: 196px;
    width: calc(100% - 20px);
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    .add-knowledge-card-icon {
      padding: 8px;
      margin-right: 12px;
    }
  }

  .knowledge-card {
    border-radius: 10px;
    margin-right: 20px;
    margin-bottom: 20px;
    height: 196px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;

    .knowledge-checkbox {
      position: absolute;
      top: 8px;
      right: 8px;
      z-index: 1;
      align-items: center;
      justify-content: center;
      display: none;
    }

    &:hover, &.checked {
      .knowledge-checkbox {
        display: flex;
      }
    }

    &.checked {
      border: 1px solid @primary-color;
    }

    .knowledge-header {
      position: relative;
      font-size: 14px;
      height: 20px;
      text-align: left;
      .header-img {
        width: 40px;
        height: 40px;
        margin-right: 12px;
      }
      .header-title{
        font-weight: bold;
        color: #354052;
        margin-left: 4px;
        align-self: center;
      }

      .header-text {
        overflow: hidden;
        position: relative;
        display: flex;
        font-size: 16px;
      }
    }
  }

  .add-knowledge-card,.knowledge-card{
    transition: box-shadow 0.3s ease;
  }

  .add-knowledge-card:hover,.knowledge-card:hover{
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .ellipsis {
    text-overflow: ellipsis;
    overflow: hidden;
    text-wrap: nowrap;
    width: calc(100% - 30px);
  }

  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }

  .card-text{
    font-size: 12px;
    display: flex;
    margin-top: 10px;
    align-items: center;
  }

  .search-header {
    margin-top: 10px;
    margin-left: 26px;

    .search-input {
      width: 240px;
      display: block;
    }
  }

  .operation{
    border: none;
    margin-top: 10px;
    align-items: end;
    display: none !important;
    bottom: 8px;
    right: 4px;
    position: absolute;
  }

  .add-knowledge-card:hover, .knowledge-card:hover{
    .operation{
      display: block !important;
    }
  }

  .add-knowledge-doc{
    margin-top: 6px;
    color:#6F6F83;
    font-size: 13px;
    width: 100%;
    cursor: pointer;
    display: flex;
    span{
      margin-left: 4px;
      line-height: 28px;
    }
  }
  .add-knowledge-doc:hover{
    background: #c8ceda33;
  }
  .operation{
    background-color: unset;
    border: none;
    margin-right: 2px;
  }
  .operation:hover{
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }
  .ant-dropdown-link{
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    align-content: center;
    text-align: center;
  }
  .card-footer{
    margin-top: 4px;
    font-weight: 400;
    color: #1f2329;
    text-align: left;
    font-size: 12px;
  }
  .card-text-status{
    display: flex;
    align-items: center;
  }
  .ml-2{
    margin-left: 2px;
  }
  .add-knowledge-doc {
    :deep(.ant-upload) {
      width: 100%;
    }
  }
</style>
<style lang="less">
  .airag-knowledge-doc .scroll-container {
    padding: 0 !important;
  }
</style>
