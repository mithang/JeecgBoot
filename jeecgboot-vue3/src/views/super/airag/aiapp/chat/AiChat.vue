<template>
  <div ref="chatContainerRef" class="chat-container" :style="chatContainerStyle">
    <div v-if="pageError" class="chat-error-state">
      <a-result status="warning" :title="pageError" sub-title="Vui lòng liên hệ người chia sẻ xác nhận trạng thái liên kết rồi thử lại" />
    </div>
    <template v-else-if="dataSource">
      <div v-if="isMultiSession" class="leftArea" :class="[expand ? 'expand' : 'shrink']">
        <div class="content">
          <slide :source="source" v-if="uuid" :dataSource="dataSource" @save="handleSave" :prologue="prologue" :appData="appData" @click="handleChatClick"></slide>
        </div>
        <div class="toggle-btn" @click="handleToggle">
          <span class="icon">
            <svg viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path
                d="M5.64645 3.14645C5.45118 3.34171 5.45118 3.65829 5.64645 3.85355L9.79289 8L5.64645 12.1464C5.45118 12.3417 5.45118 12.6583 5.64645 12.8536C5.84171 13.0488 6.15829 13.0488 6.35355 12.8536L10.8536 8.35355C11.0488 8.15829 11.0488 7.84171 10.8536 7.64645L6.35355 3.14645C6.15829 2.95118 5.84171 2.95118 5.64645 3.14645Z"
                fill="currentColor"
              ></path>
            </svg>
          </span>
        </div>
      </div>
      <div class="rightArea" :class="[expand ? 'expand' : 'shrink']">
        <chat
          url="/airag/chat/send"
          v-if="uuid && chatVisible"
          :uuid="uuid"
          :historyData="chatData"
          type="view"
          @save="handleSave"
          :formState="appData"
          :prologue="prologue"
          :presetQuestion="presetQuestion"
          @reload-message-title="reloadMessageTitle"
          :chatTitle="chatTitle"
          :quickCommandData="quickCommandData"
          :showAdvertising = "showAdvertising"
          :hasExtraFlowInputs="hasExtraFlowInputs"
          :conversationSettings="getCurrentSettings"
          @edit-settings="handleEditSettings"
          ref="chatRef"
        ></chat>
      </div>
      <!-- [issues/8545]Khi tạo mới ứng dụng AI chỉ có thể chọn quy trình AI không có tham số tùy chỉnh -->
      <ConversationSettingsModal
        ref="settingsModalRef"
        :flowInputs="flowInputs"
        :conversationId="uuid"
        :existingSettings="getCurrentSettings"
        :appId="appData?.id"
        :shareToken="appData?.shareToken"
        @ok="handleSettingsOk"
      />
    </template>
    <Loading :loading="loading" tip="Đang tải, vui lòng chờ"></Loading>
  </div>
</template>

<script setup lang="ts">
  import slide from './slide.vue';
  import chat from './chat.vue';
  import ConversationSettingsModal from './components/ConversationSettingsModal.vue';
  import { Spin, message } from 'ant-design-vue';
  import { ref, watch, nextTick, onUnmounted, onMounted, computed } from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { JEECG_CHAT_KEY } from '/@/enums/cacheEnum';
  import { defHttp } from '/@/utils/http/axios';
  import { useRouter } from 'vue-router';
  import { useAppInject } from "@/hooks/web/useAppInject";
  import Loading from '@/components/Loading/src/Loading.vue';
  import { getChatErrorMessage } from './chatError';

  const router = useRouter();
  const userId = useUserStore().getUserInfo?.id;
  const localKey = JEECG_CHAT_KEY + userId;
  let timer: any = null;
  let unwatch01: any = null;
  const dataSource = ref<any>({});
  const uuid = ref<string>('');
  const chatData = ref<any>([]);
  const expand = ref<any>(true);
  const chatVisible = ref(true);
  const chatContainerRef = ref<any>(null);
  const chatContainerStyle = ref({});
  //Thông tin trò chuyện bên trái
  const chatTitle = ref<string>('');
  //Tọa độ nhấp trò chuyện bên trái
  const chatActiveKey = ref<number>(0);
  //Lời mở đầu đặt sẵn
  const presetQuestion = ref<string>('');
  //Tải
  const loading = ref<any>(true);
  const pageError = ref<string>('');

  const handleToggle = () => {
    expand.value = !expand.value;
  };
  //id ứng dụng
  const appId = ref<string>('');
  //Dữ liệu ứng dụng
  const appData = ref<any>({});
  //Lời mở đầu
  const prologue = ref<string>('');
  //Lệnh tắt
  const quickCommandData = ref<any>([]);
  //Có hiển thị vị trí quảng cáo hay không
  const showAdvertising = ref<boolean>(false);
  //ref của popup cài đặt hội thoại
  const settingsModalRef = ref();
  //Danh sách tham số đầu vào quy trình làm việc
  const flowInputs = ref<any[]>([]);
  //Cài đặt của phiên hiện tại
  const conversationSettings = ref<Record<string, Record<string, any>>>({});

  const priming = () => {
    dataSource.value = {
      active: '1002',
      usingContext: true,
      history: [{ id: '1002', title: 'Tạo mới trò chuyện', isEdit: false, disabled: true }],
    };
    chatTitle.value = 'Tạo mới trò chuyện';
    chatActiveKey.value = 0;
  };

  const handleSave = () => {
    // Việc lưu sau khi xóa nhãn hoặc làm trống nội dung
    //save(dataSource.value);
    setTimeout(() => {
      // Xóa nhãn hoặc làm trống nội dung cũng kích hoạt lưu qua watch, lúc này không cần lưu qua watch thì phải xóa
      //clearTimeout(timer);
    }, 50);
  };

  /**
   * Kiểm tra có tham số đầu vào quy trình làm việc bổ sung hay không
   * for [issues/8545]Khi tạo mới ứng dụng AI chỉ có thể chọn quy trình AI không có tham số tùy chỉnh
    */
  const hasExtraFlowInputs = computed(() => {
    if (!appData.value || !appData.value.metadata) {
      return false;
    }
    try {
      const metadata = typeof appData.value.metadata === 'string' 
        ? JSON.parse(appData.value.metadata) 
        : appData.value.metadata;
      const flowInputsList = metadata.flowInputs || [];
      
      // Lọc bỏ tham số cố định
      const fixedParams = ['history', 'content', 'images'];
      const extraInputs = flowInputsList.filter((input: any) => !fixedParams.includes(input.field));
      
      return extraInputs.length > 0;
    } catch (e) {
      console.error('Phân tích metadata thất bại', e);
      return false;
    }
  });

  // Kiểm tra có tham số bổ sung bắt buộc hay không
  const hasRequiredFlowInputs = computed(() => {
    if (!appData.value || !appData.value.metadata) {
      return false;
    }
    try {
      const metadata = typeof appData.value.metadata === 'string' 
        ? JSON.parse(appData.value.metadata) 
        : appData.value.metadata;
      const flowInputsList = metadata.flowInputs || [];
      
      // Lọc bỏ tham số cố định, và phải là bắt buộc
      const fixedParams = ['history', 'content', 'images'];
      const requiredInputs = flowInputsList.filter((input: any) =>
        !fixedParams.includes(input.field) && isRequired(input.required)
      );
      
      return requiredInputs.length > 0;
    } catch (e) {
      console.error('Phân tích metadata thất bại', e);
      return false;
    }
  });

  /** Tương thích với dấu hiệu bắt buộc dạng boolean, number và string trong siêu dữ liệu quy trình. */
  function isRequired(required: unknown): boolean {
    return required === true || required === 1 || required === '1' || required === 'true';
  }

  // Lắng nghe thay đổi của appData, cập nhật flowInputs
  watch(
    () => appData.value,
    (val) => {
      if (!val || !val.metadata) {
        flowInputs.value = [];
        return;
      }
      try {
        const metadata = typeof val.metadata === 'string' 
          ? JSON.parse(val.metadata) 
          : val.metadata;
        flowInputs.value = metadata.flowInputs || [];
      } catch (e) {
        console.error('Phân tích metadata thất bại', e);
        flowInputs.value = [];
      }
    },
    { immediate: true, deep: true }
  );

  // Lấy cài đặt của phiên hiện tại
  const getCurrentSettings = computed(() => {
    return conversationSettings.value[uuid.value] || {};
  });

  // Chỉnh sửa cài đặt hội thoại
  function handleEditSettings() {
    if (settingsModalRef.value) {
      settingsModalRef.value.open();
    }
  }

  // Lưu cài đặt hội thoại
  function handleSettingsOk(data: Record<string, any>) {
    // Lưu vào trạng thái cục bộ (sẽ truyền cho backend khi gửi tin nhắn)
    conversationSettings.value[uuid.value] = data;
    message.success('Cài đặt hội thoại đã lưu');
    
    nextTick(() => {
      chatVisible.value = true;
    });
  }

  // Lắng nghe thay đổi của dataSource để thực thi thao tác
  const execute = () => {
    unwatch01 = watch(
      () => dataSource.value.active,
      (value) => {
        if (value) {
          if (value == '1002') {
            uuid.value = '1002';
            chatData.value = [];
            chatTitle.value = "Tạo mới trò chuyện";
            chatVisible.value = false;
            nextTick(() => {
              chatVisible.value = true;
              // Phiên mới và có tham số bắt buộc, bật popup cài đặt
              if (hasRequiredFlowInputs.value && !conversationSettings.value['1002']) {
                if (settingsModalRef.value) {
                  settingsModalRef.value.open();
                }
              }
            });
            return;
          }
          //update-begin---author:wangshuai---date:2025-03-14---for:【QQYUN-11421】Trò chuyện, sau khi xóa phiên trò chuyện, cuộc trò chuyện chuyển sang phiên mới nhưng tiêu đề trò chuyện không thay đổi---
          let values = dataSource.value.history.filter((item) => item.id === value);
          if(values && values.length>0){
            chatTitle.value = values[0]?.title
          }
          //update-end---author:wangshuai---date:2025-03-14---for:【QQYUN-11421】Trò chuyện, sau khi xóa phiên trò chuyện, cuộc trò chuyện chuyển sang phiên mới nhưng tiêu đề trò chuyện không thay đổi---
          //Truy vấn nội dung trò chuyện theo id đã chọn
          let params = { conversationId: value };
          uuid.value = value;
          defHttp.get({ url: '/airag/chat/messages', params }, { isTransformResponse: false }).then((res) => {
            if (res.success) {
              // Xử lý định dạng trả về mới (bao gồm messages và flowInputs)
              if (res.result && res.result.messages) {
                chatData.value = res.result.messages;
                // Tải cài đặt đã lưu
                if (res.result.flowInputs) {
                  conversationSettings.value[value] = res.result.flowInputs;
                }
              } else if (Array.isArray(res.result)) {
                // Tương thích định dạng cũ
                chatData.value = res.result;
              } else {
                chatData.value = [];
              }
            } else {
              chatData.value = [];
            }
            chatVisible.value = false;
            // Phiên mới và có tham số bắt buộc, bật popup cài đặt
            if (hasRequiredFlowInputs.value && !conversationSettings.value[value]) {
              if (settingsModalRef.value) {
                settingsModalRef.value.open();
              }
            }else{
              nextTick(() => {
                chatVisible.value = true;
              });
            }
          });
        }else{
          chatData.value = [];
          chatTitle.value = "";
        }
      },
      { immediate: true }
    );
  };

  //Có phải chế độ nhiều phiên hay không
  const isMultiSession = ref<boolean>(true);
  //Có phải điện thoại hay không
  const { getIsMobile } = useAppInject();
  //Nguồn
  const source = ref<string>('');
  
  /**
   * Khởi tạo thông tin trò chuyện
   * @param appId
   */
  function initChartData(appId = '') {
    defHttp
      .get(
        {
          url: '/airag/chat/conversations',
          params: { appId: appId },
        },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.success && res.result && res.result.length > 0) {
          dataSource.value.history = res.result;
          dataSource.value.active = res.result[0].id;
          chatTitle.value = res.result[0].title;
          chatActiveKey.value = 0;
        } else {
          priming();
        }
        !unwatch01 && execute();
      })
      .catch(() => {
        priming();
      }).finally(()=>{
        loading.value = false
    });
  }

  onMounted(() => {
    loading.value = true;
    let params: any = router.currentRoute.value.params;
    let query: any = router.currentRoute.value.query;
    source.value = query.source;
    if(query.source){
      showAdvertising.value = query.source === 'chatJs';
    }else{
      showAdvertising.value = false;
    }
    if (params.appId) {
      appId.value = params.appId;
      const shareToken = query.shareToken || '';
      getApplicationData(params.appId, shareToken);
      initChartData(params.appId);
    } else {
      initChartData();
      appData.value.metadata = { izDraw: '1', defaultSelect: '0' }  
      quickCommandData.value = [
          { name: 'Giới thiệu về JeecgBoot', descr: "Vui lòng giới thiệu về JeecgBoot" },
          { name: 'Ưu thế của JEECG?', descr: "JEECG có những ưu thế nào?" },
          { name: 'JEECG có thể làm gì?', descr: "JEECG có thể làm những việc gì?" },
          { name: 'Tạo một người dùng hệ thống', descr: "Vui lòng tạo một người dùng hệ thống, thông tin cụ thể như sau:\n" +
              "Tài khoản zhangsan2, mật khẩu Abc123456, họ tên Zhang San, email zhangsan@example.com, số điện thoại 13800138000, và cấp quyền vai trò jeecg." },];
    }
  });

  onUnmounted(() => {
    chatData.value = [];
    chatTitle.value = "";
    prologue.value = ""
    presetQuestion.value = "";
    quickCommandData.value = [];
  })
  
  /**
   * Lấy id ứng dụng
   *
   * @param appId
   * @param shareToken token chia sẻ
   */
  async function getApplicationData(appId, shareToken = '') {
    pageError.value = '';
    await defHttp
      .get(
        {
          url: '/airag/chat/init',
          params: { id: appId, shareToken: shareToken || undefined },
        },
        { isTransformResponse: false }
      )
      .then((res) => {
        if (res.success) {
          appData.value = res.result;
          if (res.result && res.result.prologue) {
            prologue.value = res.result.prologue;
          }  
          if (res.result && res.result.quickCommand) {
            quickCommandData.value = JSON.parse(res.result.quickCommand);
          } 
          if (res.result && res.result.presetQuestion) {
            presetQuestion.value = res.result.presetQuestion;
          }
          if (res.result && res.result.metadata) {
            let metadata = JSON.parse(res.result.metadata);
            //Phán đoán có phải chế độ điện thoại hay không
            if(!getIsMobile.value){
              //Có phải chế độ nhiều phiên hay không
              if((metadata.multiSession && metadata.multiSession === '1') || !metadata.multiSession) {
                isMultiSession.value = true;
              } else {
                isMultiSession.value = false;
                expand.value = false;
              }
            }
          }
          if(getIsMobile.value){
            isMultiSession.value = false;
            expand.value = false;
          }
        } else {
          pageError.value = getChatErrorMessage(res) || 'Liên kết chia sẻ không hợp lệ hoặc đã hủy phát hành';
          appData.value = {};
        }
      })
      .catch((error) => {
        pageError.value = error?.response?.data?.message || error?.message || 'Tải ứng dụng thất bại, vui lòng thử lại sau';
        appData.value = {};
      });
  }

  /**
   * Sự kiện nhấp danh sách tin nhắn bên trái
   * @param title
   * @param index
   */
  function handleChatClick(title, index) {
    chatTitle.value = title;
    chatActiveKey.value = index;
  }

  /**
   * Tải lại tin nhắn tiêu đề
   * @param text
   */
  function reloadMessageTitle(text) {
    let title = dataSource.value.history[chatActiveKey.value].title;
    if(title === 'Tạo mới trò chuyện'){
      dataSource.value.history[chatActiveKey.value].title = text;
      dataSource.value.history[chatActiveKey.value]['disabled'] = false;
    }

  }
  
  /**
   * Khởi tạo trò chuyện: dùng cho nhấp icon
   */
  function initChat(value) {
    appId.value = value;
    getApplicationData(value);
    initChartData(value);
  }
  
  defineExpose({
    initChat
  })

  onUnmounted(() => {
    unwatch01 && unwatch01();
  });

  watch(
    () => chatContainerRef.value,
    () => {
      if(chatContainerRef.value.offsetHeight){
        chatContainerStyle.value = { height: `${chatContainerRef.value.offsetHeight} px` };
      }
    }
  );
</script>

<style scoped lang="less">
  @width: 260px;
  .chat-container {
    height: 100%;
    width: 100%;
    position: absolute;
    background: white;
    display: flex;
    overflow: hidden;
    z-index: 800;
    border: 1px solid #eeeeee;
    :deep(.ant-spin) {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }

  .chat-error-state {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .leftArea {
    width: @width;
    transition: 0.3s left;
    position: absolute;
    left: 0;
    height: 100%;

    .content {
      width: 100%;
      height: 100%;
      overflow: hidden;
    }

    &.shrink {
      left: -@width;

      .toggle-btn {
        .icon {
          transform: rotate(0deg);
        }
      }
    }

    .toggle-btn {
      transition:
        color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        right 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        left 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1),
        background-color 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      width: 24px;
      height: 24px;
      position: absolute;
      top: 50%;
      right: 0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      color: rgb(51, 54, 57);
      border: 1px solid rgb(239, 239, 245);
      background-color: #fff;
      box-shadow: 0 2px 4px 0px #e7e9ef;
      transform: translateX(50%) translateY(-50%);
      z-index: 1;
    }

    .icon {
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      transform: rotate(180deg);
      font-size: 18px;
      height: 18px;

      svg {
        height: 1em;
        width: 1em;
        vertical-align: top;
      }
    }
  }

  .rightArea {
    margin-left: @width;
    transition: 0.3s margin-left;

    &.shrink {
      margin-left: 0;
    }

    flex: 1;
    min-width: 0;
  }
</style>
