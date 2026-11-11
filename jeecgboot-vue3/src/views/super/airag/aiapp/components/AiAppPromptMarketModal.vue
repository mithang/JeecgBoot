<template>
  <BasicModal
    destroyOnClose
    @register="registerModal"
    :canFullscreen="false"
    width="1000px"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="Sử dụng"
    wrapClassName="ai-rag-generate-prompt-modal"
    :confirmLoading="loading"
  >
    <div class="prompt-market-content">
      <!-- Khu vực tìm kiếm -->
      <div class="search-section">
        <a-input-search
          v-model:value="searchText"
          placeholder="Tìm kiếm tên hoặc mô tả prompt"
          style="width: 300px"
          @search="handleSearch"
          @pressEnter="handleSearch"
        />
      </div>

      <!-- Danh sách prompt -->
      <div class="prompt-list-section">
        <a-spin :spinning="loading">
          <template v-if="promptList.length > 0">
            <a-row :gutter="[24, 24]">
              <a-col v-for="item in promptList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="8">
                <div class="prompt-card" @click="handleSelectPrompt(item)">
                  <a-card :class="['prompt-item-card', { selected: selectedPrompt?.id === item.id }]" :hoverable="true" size="small">
                    <template #title>
                      <div class="card-title">
                        <span class="title-text">{{ item.name }}</span>
                      </div>
                    </template>
                    <div class="card-content">
                      <p class="description" :title="item.description || item.desc">{{ item.description || item.desc }}</p>
                      <div class="card-footer" >
                        <span class="create-time">
                          {{ formatTime(item.createTime) }}
                        </span>
                      </div>
                    </div>
                  </a-card>
                </div>
              </a-col>
            </a-row>
          </template>

          <!-- Trạng thái trống -->
          <empty v-else description="Tạm thời chưa có dữ liệu prompt" class="empty-state" />
        </a-spin>
      </div>

      <!-- Khu vực phân trang -->
      <div class="pagination-section">
        <Pagination
          v-model:current="pagination.current"
          v-model:pageSize="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['10', '20', '30', '50']"
          :show-quick-jumper="true"
          @change="handlePageChange"
          @showSizeChange="handleSizeChange"
        />
      </div>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { ref, reactive } from 'vue';
  import { Empty } from 'ant-design-vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModalInner } from '@/components/Modal';
  import { list } from '@/views/super/airag/aiprompts/AiragPrompts.api';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { Pagination } from 'ant-design-vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  export default {
    name: 'AiAppPromptMarketModal',
    components: {
      BasicModal,
      Pagination,
      Empty,
    },
    emits: ['ok', 'register', 'select'],
    setup(props, { emit }) {
      const { createMessage } = useMessage();
      // Danh sách prompt
      const promptList = ref<any[]>([]);
      // Trạng thái tải
      const loading = ref<boolean>(false);
      // Tìm kiếm văn bản
      const searchText = ref<string>('');
      // Prompt đã chọn
      const selectedPrompt = ref<any>(null);

      // Cấu hình phân trang
      const pagination = reactive({
        current: 1,
        pageSize: 12,
        total: 0,
      });

      // Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async () => {
        loading.value = false;
        resetField();
        await getPromptList();
        setModalProps({
          title: 'Chọn prompt',
          height: 600,
          bodyStyle: { padding: '24px' },
        });
      });

      /**
       *
       */
      function resetField() {
        promptList.value = [];
        selectedPrompt.value = null;
        searchText.value = '';
        pagination.current = 1;
      }

      /**
       * Lấy danh sách prompt
       */
      async function getPromptList() {
        loading.value = true;
        try {
          const params = {
            pageNo: pagination.current,
            pageSize: pagination.pageSize,
            name: searchText.value ? `*${searchText.value}*` : '',
          };

          const res = await list(params);
          console.log('Lấy danh sách prompt thành công:', res);
          if (res?.records) {
            promptList.value = res?.records || [];
            pagination.total = res?.total || 0;
          } else {
            promptList.value = [];
            pagination.total = 0;
          }
        } catch (error) {
          console.error('Lấy danh sách prompt thất bại:', error);
          promptList.value = [];
          pagination.total = 0;
        } finally {
          loading.value = false;
        }
      }

      /**
       * Xử lý tìm kiếm
       */
      function handleSearch() {
        pagination.current = 1;
        getPromptList();
      }

      /**
       * Xử lý thay đổi số trang
       */
      function handlePageChange(page: number, pageSize: number) {
        pagination.current = page;
        pagination.pageSize = pageSize;
        getPromptList();
      }

      /**
       * Xử lý thay đổi kích thước trang
       */
      function handleSizeChange(current: number, size: number) {
        pagination.current = current;
        pagination.pageSize = size;
        getPromptList();
      }

      /**
       * Chọn prompt
       */
      function handleSelectPrompt(item: any) {
        selectedPrompt.value = item;
      }
      /**
       * Thời gian đã định dạng
       */
      function formatTime(time) {
        console.log('formatTime:', formatToDateTime(time));
        return formatToDateTime(time);
      }

      /**
       * Lưu
       */
      async function handleOk() {
        // update-begin--author:liaozhiyang---date:20260810---for：【LHZP-1518】Sửa prompt của popup chọn, không hiển thị lại được nội dung
        if (!selectedPrompt.value) {
          createMessage.warning('Vui lòng chọn prompt');
          return;
        }
        if (!String(selectedPrompt.value.content ?? '').trim()) {
          createMessage.warning('Dữ liệu prompt hiện tại chưa cấu hình nội dung prompt, vui lòng vào trang 「Prompt AI」 tìm dữ liệu tương ứng và hoàn tất cấu hình.');
          return;
        }
        // update-end--author:liaozhiyang---date:20260810---for：【LHZP-1518】Sửa prompt của popup chọn, không hiển thị lại được nội dung
        // select truyền đối tượng đầy đủ, để bên gọi lấy id theo nhu cầu/name/content
        emit('select', selectedPrompt.value);
        emit('ok', selectedPrompt.value.content);
        handleCancel();
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      return {
        registerModal,
        handleOk,
        handleCancel,
        promptList,
        loading,
        searchText,
        selectedPrompt,
        pagination,
        handleSearch,
        handlePageChange,
        handleSizeChange,
        handleSelectPrompt,
        formatTime,
      };
    },
  };
</script>

<style scoped lang="less">
.prompt-market-content {
  min-height: 400px;
  padding: 16px;

  .search-section {
    margin-bottom: 24px;
    display: flex;
    justify-content: flex-start;

    .ant-input-search {
      max-width: 400px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      border-radius: 6px;
    }
  }

  .prompt-list-section {
    .prompt-card {
      transition:
          transform 0.2s ease,
          box-shadow 0.2s ease;

      &:hover {
        transform: translateY(-4px);
      }

      .prompt-item-card {
        border-radius: 8px;
        overflow: hidden;
        height: 150px; // Tăng chiều cao thẻ
        transition: all 0.3s ease;
        border: 1px solid #e8e8e8;

        .ant-card-head {
          border-bottom: 1px solid #f0f0f0;
          padding: 12px 16px;
        }

        .ant-card-body {
          padding: 12px 16px;
          height: calc(100% - 48px); // Điều chỉnh chiều cao khu vực nội dung
          display: flex;
          flex-direction: column;
        }

        &.selected {
          border-color: #1890ff;
          background-color: #e6f7ff; // Thêm màu nền khi chọn
          box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);

          .card-title .title-text {
            color: #1890ff;
          }
        }

        .card-title {
          display: flex;
          align-items: center;

          .title-text {
            font-weight: 600;
            font-size: 16px;
            color: #333;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }

        .card-content {
          flex: 1; // Dùng flex để khu vực nội dung tự thích ứng
          display: flex;
          flex-direction: column;
          justify-content: space-between; // Để nội dung và thông tin thời gian phân bố theo chiều dọc

          .description {
            color: #666;
            font-size: 13px;
            line-height: 1.5;
            margin: 8px 0;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            flex: 1; // Để khu vực mô tả tự thích ứng
          }

          .card-footer {
            margin-top: 8px; // Điều chỉnh khoảng cách
            padding-top: 8px;
            border-top: 1px solid #f5f5f5;
            display: flex;
            justify-content: flex-end;

            .create-time {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }
    }
  }

  .pagination-section {
    margin-top: 24px;
    padding-top: 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
  }

  .empty-state {
    margin-top: 40px;
  }
}

// Kiểu tổng thể của hộp modal
.ai-rag-generate-prompt-modal {
  .ant-modal-body {
    padding: 0 !important;
  }

  .ant-modal-header {
    border-radius: 8px 8px 0 0;
  }
}
</style>
