<template>
  <div>
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="1000px" @ok="handleOk" @cancel="handleCancel" okText="Thay thế" wrapClassName='ai-rag-generate-prompt-modal'>
      <div class="prompt">
        <div class="prompt-left">
          <div class="prompt-left-title">Trình tạo prompt</div>
          <div class="prompt-left-desc">Trình tạo prompt dùng mô hình đã cấu hình để tối ưu prompt, nhằm đạt chất lượng cao hơn và cấu trúc tốt hơn. Vui lòng viết ra hướng dẫn rõ ràng chi tiết.</div>
          <a-divider></a-divider>
          <div class="prompt-left-try">
            <div class="prompt-left-try-title">Thử một lần</div>
          </div>
          <div class="instructions">
            <div class="instructions-content" v-for="item in instructionsList" @click="instructionsClick(item.value)">
              <Icon :icon="item.icon" size="14" color="#676f83"></Icon>
              <div class="instructions-name">{{ item.name }}</div>
            </div>
          </div>
          <div class="prompt-left-textarea">
            <div class="command">
              <span style="margin-right: 5px">Lệnh</span>
              <a-tooltip title="Thư viện prompt">
                <span @click="openPromptApps" style="color:#1890ff;cursor: pointer">
                  <Icon icon="ant-design:bulb-outlined" color="#1890ff"></Icon>Chọn kho từ
                </span>
              </a-tooltip>
            </div>
            <a-textarea v-model:value="prompt" :autoSize="{ minRows: 8, maxRows: 8 }"></a-textarea>
          </div>
          <a-button @click="generatedPrompt" class="prompt-left-btn" type="primary" :loading="loading">
            <span style="align-items: center; display: flex" v-if="!loading">
              <svg width="1em" height="1em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"
                ></path>
              </svg>
              <span style="margin-left: 4px">Tạo</span>
            </span>
          </a-button>
        </div>
        <div class="prompt-right">
          <div v-if="!loading && !content">
            <svg width="6em" height="6em" viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M18.9839 1.85931C19.1612 1.38023 19.8388 1.38023 20.0161 1.85931L20.5021 3.17278C20.5578 3.3234 20.6766 3.44216 20.8272 3.49789L22.1407 3.98392C22.6198 4.1612 22.6198 4.8388 22.1407 5.01608L20.8272 5.50211C20.6766 5.55784 20.5578 5.6766 20.5021 5.82722L20.0161 7.14069C19.8388 7.61977 19.1612 7.61977 18.9839 7.14069L18.4979 5.82722C18.4422 5.6766 18.3234 5.55784 18.1728 5.50211L16.8593 5.01608C16.3802 4.8388 16.3802 4.1612 16.8593 3.98392L18.1728 3.49789C18.3234 3.44216 18.4422 3.3234 18.4979 3.17278L18.9839 1.85931zM13.5482 4.07793C13.0164 2.64069 10.9836 2.64069 10.4518 4.07793L8.99368 8.01834C8.82648 8.47021 8.47021 8.82648 8.01834 8.99368L4.07793 10.4518C2.64069 10.9836 2.64069 13.0164 4.07793 13.5482L8.01834 15.0063C8.47021 15.1735 8.82648 15.5298 8.99368 15.9817L10.4518 19.9221C10.9836 21.3593 13.0164 21.3593 13.5482 19.9221L15.0063 15.9817C15.1735 15.5298 15.5298 15.1735 15.9817 15.0063L19.9221 13.5482C21.3593 13.0164 21.3593 10.9836 19.9221 10.4518L15.9817 8.99368C15.5298 8.82648 15.1735 8.47021 15.0063 8.01834L13.5482 4.07793zM5.01608 16.8593C4.8388 16.3802 4.1612 16.3802 3.98392 16.8593L3.49789 18.1728C3.44216 18.3234 3.3234 18.4422 3.17278 18.4979L1.85931 18.9839C1.38023 19.1612 1.38023 19.8388 1.85931 20.0161L3.17278 20.5021C3.3234 20.5578 3.44216 20.6766 3.49789 20.8272L3.98392 22.1407C4.1612 22.6198 4.8388 22.6198 5.01608 22.1407L5.50211 20.8272C5.55784 20.6766 5.6766 20.5578 5.82722 20.5021L7.14069 20.0161C7.61977 19.8388 7.61977 19.1612 7.14069 18.9839L5.82722 18.4979C5.6766 18.4422 5.55784 18.3234 5.50211 18.1728L5.01608 16.8593z"
              ></path>
            </svg>
            <div>Mô tả trường hợp sử dụng của bạn ở bên trái，</div>
            <div>Xem trước sắp xếp sẽ hiển thị tại đây.</div>
          </div>
          <div v-if="loading">
            <a-spin :spinning="loading" tip="Đang sắp xếp cho bạn trong ứng dụng…"></a-spin>
          </div>
          <div v-if="content">
            <a-textarea v-model:value="content" :autoSize="{ minRows: 18, maxRows: 18 }"></a-textarea>
          </div>
        </div>
      </div>
    </BasicModal>
  </div>
  <!-- Popup chọn prompt Ai   -->
  <AiAppPromptMarketModal @register="registerAiPromptSelectModal" @ok="handleAiAppPromptOk"></AiAppPromptMarketModal>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import {useModal, useModalInner} from '@/components/Modal';
  import { promptGenerate } from '@/views/super/airag/aiapp/AiApp.api';
  import AiAppPromptMarketModal from "@/views/super/airag/aiapp/components/AiAppPromptMarketModal.vue";

  export default {
    name: 'AiAppGeneratedPrompt',
    components: {
      AiAppPromptMarketModal,
      BasicModal,
    },
    emits: ['ok', 'register'],
    setup(props, { emit }) {
      //Prompt
      const prompt = ref<string>('');
      //Tải
      const loading = ref<boolean>(false);
      //Hiển thị văn bản
      const content = ref<string>('');
      //Prompt lệnh
      const instructionsList = ref<any>([
        { name: 'Trợ lý mã python', value: 'python', icon: 'ant-design:code-outlined' },
        { name: 'Trình dịch', value: 'translator', icon: 'ant-design:translation-outlined' },
        { name: 'Trợ lý cuộc họp', value: 'meeting', icon: 'ant-design:team-outlined' },
        { name: 'Trau chuốt bài viết', value: 'article', icon: 'ant-design:profile-outlined' },
        { name: 'Trình tạo sql', value: 'sql', icon: 'ant-design:console-sql-outlined' },
        { name: 'Chuyên gia lập kế hoạch du lịch', value: 'travel', icon: 'ant-design:car-outlined' },
        { name: 'Chuyên gia linux', value: 'linux', icon: 'ant-design:fund-projection-screen-outlined' },
        { name: 'Trình chắt lọc nội dung', value: 'content', icon: 'ant-design:read-outlined' },
      ]);
      //Lệnh
      const tip = ref<any>({
        python: 'Bạn là một chuyên gia python, có thể giúp người dùng viết và sửa lỗi mã.',
        translator: 'Một trình dịch có thể dịch nhiều ngôn ngữ sang tiếng Trung.',
        meeting: 'Chắt lọc tóm tắt nội dung cuộc họp, bao gồm chủ đề thảo luận, điểm chính quan trọng và việc cần làm.',
        article: 'Dùng kỹ thuật biên tập siêu đẳng để cải thiện bài viết của tôi.',
        sql: 'Theo mô tả của người dùng, tạo câu lệnh sql, cần hỗ trợ hướng dẫn người dùng cung cấp cấu trúc bảng',
        travel: 'Bạn là một chuyên viên lập kế hoạch du lịch, giỏi giúp người dùng lập kế hoạch chuyến đi một cách nhẹ nhàng',
        linux: 'Bạn là một chuyên gia linux, giỏi giải quyết các vấn đề liên quan đến linux.',
        content: 'Bạn là một bậc thầy về đọc hiểu, có thể đọc bài viết người dùng cung cấp, đồng thời chắt lọc nội dung chính để xuất ra cho người dùng.',
      });
      //Đăng ký modal prompt
      const [registerAiPromptSelectModal, { openModal: aiPromptSelectModalOpen }] = useModal();
      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        content.value = '';
        loading.value = false;
        prompt.value = '';
        // update-begin--author:liaozhiyang---date:20260804---for：Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
        setModalProps({ height: 500, okButtonProps: { disabled: true } });
        // update-end--author:liaozhiyang---date:20260804---for：Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
      });

      /**
       * Đặt nút thay thế có khả dụng hay không
       */
      function setReplaceEnabled(enabled: boolean) {
        setModalProps({ okButtonProps: { disabled: !enabled } });
      }

      /**
       * Lưu
       */
      async function handleOk() {
        emit('ok', content.value);
        handleCancel();
      }

      //update-begin---author:wangshuai---date:2025-04-01---for:【QQYUN-11796】【AI】Trình tạo prompt, đổi thành bất đồng bộ---
      /**
       * Tạo
       */
      async function generatedPrompt() {
        content.value = '';
        loading.value = true;
        // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
        // Khi bắt đầu tạo thì vô hiệu hóa「Thay thế」, sau khi dữ liệu streaming hoàn thành toàn bộ mới kích hoạt lại
        setReplaceEnabled(false);
        // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
        let readableStream = await promptGenerate({ prompt: encodeURIComponent(prompt.value) }).catch(() => {
            loading.value = false;
        });
        if (!readableStream) {
          return;
        }
        const reader = readableStream.getReader();
        const decoder = new TextDecoder('UTF-8');
        let buffer = '';
        while (true) {
          const { done, value } = await reader.read();
          if (done) {
            // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
            // Sau khi nhận xong toàn bộ dữ liệu streaming của giao diện mới kích hoạt「Thay thế」
            loading.value = false;
            if (content.value) {
              setReplaceEnabled(true);
            }
            // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
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
            content.value += parse.data.message;
            if(loading.value){
              loading.value = false;
            }
          }
          if (parse.event == 'MESSAGE_END') {
            loading.value = false;
          }
          if (parse.event == 'ERROR') {
            content.value = parse.data.message?parse.data.message:'Tạo thất bại, vui lòng thử lại sau!'
            loading.value = false;
            // update-begin--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
            // Khi tạo thất bại giữ「Thay thế」ở trạng thái vô hiệu hóa
            setReplaceEnabled(false);
            // update-end--author:liaozhiyang---date:20260804---for：【LHZP-1477】Khi mở popup thì vô hiệu hóa「Thay thế」, phải đợi tạo xong mới kích hoạt lại
          }
        } catch (error) {
          console.log('Error parsing update:', error);
        }
      }
      //update-end---author:wangshuai---date:2025-04-01---for:【QQYUN-11796】【AI】Trình tạo prompt, đổi thành bất đồng bộ---

      /**
       * Sự kiện nhấp lệnh
       */
      function instructionsClick(value) {
        prompt.value = tip.value[value];
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      /**
       * Mở popup thư viện prompt
       */
      function openPromptApps() {
        aiPromptSelectModalOpen(true,{});
      }
      /**
       * Callback prompt
       *
       * @param value
       */
      function handleAiAppPromptOk(value) {
        content.value = value;
      }
      return {
        registerModal,
        handleOk,
        handleCancel,
        prompt,
        generatedPrompt,
        instructionsList,
        loading,
        instructionsClick,
        content,
        openPromptApps,
        registerAiPromptSelectModal,
        handleAiAppPromptOk,
      };
    },
  };
</script>

<style scoped lang="less">
  .prompt {
    width: 100%;
    display: flex;
  }
  .prompt-left {
    width: 50%;
    padding: 20px;
    border-right: 1px solid #10182814;
    .prompt-left-title {
      background: linear-gradient(92deg, #2250f2 -29.55%, #0ebcf3 75.22%);
      background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 28px;
      font-weight: 700;
      font-size: 18px;
    }
    .prompt-left-desc {
      color: #676f83;
      font-weight: 400;
      font-size: 13px;
      margin-top: 4px;
    }
    .prompt-left-try {
      display: flex;
      align-items: center;
      .prompt-left-try-title {
        color: #676f83;
        line-height: 18px;
        text-transform: uppercase;
        font-weight: 600;
        font-size: 12px;
        margin-right: 10px;
      }
    }
    .prompt-left-textarea {
      margin-top: 25px;
      .command {
        display: flex;
        align-items: center;
        color: #101828;
        line-height: 15px;
        font-weight: 500;
        font-size: 12px;
        margin-bottom: 15px;
      }
    }
    .prompt-left-btn {
      width: 80px;
      margin-top: 10px;
      float: right;
    }
  }
  .prompt-right {
    padding: 20px;
    width: 50%;
    text-align: center;
    align-content: center;
    svg {
      color: #676f83;
    }
  }
  .instructions {
    display: flex;
    flex-wrap: wrap;
    .instructions-content {
      padding-left: 5px;
      padding-right: 5px;
      border-radius: 5px;
      align-items: center;
      cursor: pointer;
      height: 20px;
      display: flex;
      margin-top: 8px;
      margin-left: 5px;
    }
    .instructions-name {
      color: #354052;
      font-size: 13px;
      font-weight: 500;
      line-height: 2px;
      margin-left: 4px;
    }
  }
  :deep(.ant-divider-horizontal) {
    margin: 12px 0;
  }
</style>
<style lang="less">
  .ai-rag-generate-prompt-modal {
    .jeecg-modal-content > .scroll-container {
      padding: 0;

      & > .scrollbar__wrap {
        overflow: hidden;
        margin-bottom: 0 !important;
      }
    }
  }
</style>
