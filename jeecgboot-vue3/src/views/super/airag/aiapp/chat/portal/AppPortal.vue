<!-- Cổng ứng dụng -->
<template>
  <div ref="portalRef" class="portal-container" :style="portalContainerStyle">
    <div class="leftArea" :class="[expand ? 'expand' : 'shrink']">
      <div class="content">
        <LeftPortalSession ref="leftPortalRef" @app-click="handleAppClick" @task-click="handleTaskClick"></LeftPortalSession>
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
        :key="uuid"
        url="/airag/chat/send"
        v-if="uuid"
        :uuid="uuid"
        :historyData="historyData"
        type="view"
        :formState="appData"
        :prologue="appData?.prologue"
        :presetQuestion="appData?.presetQuestion"
        @reload-message-title="reloadMessageTitle"
        :chatTitle="chatTitle"
        :conversationSettings="getCurrentSettings"
        @edit-settings="handleEditSettings"
        sessionType="portal"
        ref="chatRef"
      ></chat>
      <div v-if="showWelcome" class="emptyArea">
        <div class="welcome-text">{{ welcomeText }}</div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import LeftPortalSession from './LeftPortalSession.vue';
  import chat from '/@/views/super/airag/aiapp/chat/chat.vue';
  import { defHttp } from '@/utils/http/axios';
  import { useUserStore } from '@/store/modules/user';

  const portalContainerStyle = ref<any>(null);
  //ref của cổng ứng dụng
  const portalRef = ref();
  //Danh sách bên trái có mở rộng hay không
  const expand = ref<any>(true);
  //Sự kiện mở rộng/đóng danh sách bên trái
  const handleToggle = () => {
    expand.value = !expand.value;
  };
  //id ngẫu nhiên
  const uuid = ref<string>('');
  //Lịch sử
  const historyData = ref<any>();
  //Dữ liệu ứng dụng
  const appData = ref<any>();
  //Tiêu đề trò chuyện
  const chatTitle = ref<any>();
  //Cài đặt của phiên hiện tại
  const conversationSettings = ref<Record<string, Record<string, any>>>({});
  // Lấy cài đặt của phiên hiện tại
  const getCurrentSettings = computed(() => {
    return conversationSettings.value[uuid.value] || {};
  });
  //ref của popup cài đặt hội thoại
  const settingsModalRef = ref();
  //ref của phiên bên trái
  const leftPortalRef = ref();
  // Lời chào (lấy họ tên hoặc tên người dùng của người dùng đăng nhập hiện tại)
  const userStore = useUserStore();
  const welcomeName = computed(() => userStore.getUserInfo?.realname || userStore.getUserInfo?.username || '');
  const welcomeText = computed(() => (welcomeName.value ? `Xin chào，${welcomeName.value}。Bạn đã sẵn sàng bắt đầu chưa?` : 'Xin chào, bạn đã sẵn sàng bắt đầu chưa?'));
  const showWelcome = ref<boolean>(false);

  // Chỉ định độ dài và cơ số
  const getUuid = (len = 10, radix = 16) => {
    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    var uuid: any = [],
      i;
    radix = radix || chars.length;

    if (len) {
      for (i = 0; i < len; i++) uuid[i] = chars[0 | (Math.random() * radix)];
    } else {
      var r;
      uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
      uuid[14] = '4';
      for (i = 0; i < 36; i++) {
        if (!uuid[i]) {
          r = 0 | (Math.random() * 16);
          uuid[i] = chars[i == 19 ? (r & 0x3) | 0x8 : r];
        }
      }
    }
    return uuid.join('');
  };

  /**
   * Tải lại tiêu đề
   * @param title
   */
  function reloadMessageTitle(title) {
    showWelcome.value = false;
    leftPortalRef.value.addSession(title, uuid.value);
  }

  // Chỉnh sửa cài đặt hội thoại
  function handleEditSettings() {
    if (settingsModalRef.value) {
      settingsModalRef.value.open();
    }
  }

  /**
   * Sự kiện trả về khi nhấp dữ liệu ứng dụng
   *
   * @param value
   */
  function handleAppClick(value) {
    //Mỗi lần nhấp đều là một phiên mới
    uuid.value = getUuid(32);
    appData.value = value;
    chatTitle.value = appData.value.name;
    showWelcome.value = true;
  }

  /**
   * Sự kiện nhấp hội thoại
   *
   * @param id
   */
  function handleTaskClick(id, title) {
    showWelcome.value = false;
    uuid.value = id;
    chatTitle.value = title;
    //Truy vấn nội dung trò chuyện theo id đã chọn
    let params = { conversationId: id, sessionType: 'portal' };
    //Lấy lịch sử theo id
    defHttp.get({ url: '/airag/chat/messages', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        // Xử lý định dạng trả về mới (bao gồm messages và flowInputs)
        if (res.result && res.result.messages) {
          historyData.value = res.result.messages;
          if (res.result?.appData) {
            appData.value = res.result.appData;
          } else {
            appData.value = null;
          }
          // Tải cài đặt đã lưu
          if (res.result.flowInputs) {
            conversationSettings.value[id] = res.result.flowInputs;
          }
        } else if (Array.isArray(res.result)) {
          // Tương thích định dạng cũ
          historyData.value = res.result;
        } else {
          historyData.value = [];
        }
      } else {
        historyData.value = [];
      }
    });
  }

  // Khi uuid thay đổi, làm trống dữ liệu phiên của trò chuyện bên phải và khởi tạo cài đặt hiện tại, đảm bảo mở phiên hoàn toàn mới
  watch(
    () => uuid.value,
    (newVal) => {
      if (!newVal) return;
      // Làm trống tiêu đề và lịch sử, ngăn dùng lại dữ liệu của phiên trước
      historyData.value = [];
      // Khởi tạo container cài đặt phiên hiện tại
      conversationSettings.value[newVal] = conversationSettings.value[newVal] || {};
    }
  );

  watch(
    () => portalRef.value,
    () => {
      if (portalRef.value.offsetHeight) {
        portalRef.value = { height: `${portalRef.value.offsetHeight} px` };
      }
    }
  );
</script>

<style lang="less" scoped>
  @width: 260px;
  .portal-container {
    height: 100%;
    width: 100%;
    position: absolute;
    background: white;
    display: flex;
    overflow: hidden;
    z-index: 800;
    border: 1px solid #eeeeee;
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

  .emptyArea {
    position: absolute;
    top: 45%;
    left: 45%;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #d4d4d4;
  }
  .emptyArea .welcome-text {
    font-size: 32px;
    color: #3d4353;
    font-weight: 600;
  }
</style>
