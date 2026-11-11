<template>
  <div :class="prefixCls">
    <Badge :count="messageCount" :overflowCount="9" :offset="[-4, 18]" :numberStyle="numberStyle" @click="clickBadge('')">
      <BellOutlined />
    </Badge>

    <DynamicNotice ref="dynamicNoticeRef" v-bind="dynamicNoticeProps" />
    <DetailModal @register="registerDetail" />

    <sys-message-modal @register="registerMessageModal" @refresh="reloadCount" :messageCount="messageCount"></sys-message-modal>
    <!--  Popup đổi mật khẩu  -->
    <ChangePasswordModal @register="changePwdModal"></ChangePasswordModal>
  </div>
</template>
<script lang="ts">
  import { computed, defineComponent, ref, unref, reactive, onMounted, getCurrentInstance } from 'vue';
  import { Popover, Tabs, Badge } from 'ant-design-vue';
  import { BellOutlined } from '@ant-design/icons-vue';
  // import { tabListData } from './data';
  import { getUnreadMessageCount, editCementSend, clearAllUnReadMessage } from './notify.api';
  import NoticeList from './NoticeList.vue';
  import DetailModal from '/@/views/monitor/mynews/DetailModal.vue';
  import DynamicNotice from '/@/views/monitor/mynews/DynamicNotice.vue';
  import { useModal } from '/@/components/Modal';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useUserStore } from '/@/store/modules/user';
  import { connectWebSocket, onWebSocket } from '/@/hooks/web/useWebSocket';
  import { readAllMsg } from '/@/views/monitor/mynews/mynews.api';
  import { getToken } from '/@/utils/auth';
  import md5 from 'crypto-js/md5';
  import { useRouter } from 'vue-router';

  import SysMessageModal from '/@/views/system/message/components/SysMessageModal.vue'
  import ChangePasswordModal from './ChangePasswordModal.vue'
  import { ElectronEnum } from '/@/enums/jeecgEnum';
  import { defHttp } from "@/utils/http/axios";

  export default defineComponent({
    components: {
      Popover,
      BellOutlined,
      Tabs,
      TabPane: Tabs.TabPane,
      Badge,
      NoticeList,
      DetailModal,
      DynamicNotice,
      SysMessageModal,
      ChangePasswordModal,
    },
    setup() {
      const { prefixCls } = useDesign('header-notify');
      const instance: any = getCurrentInstance();
      const userStore = useUserStore();
      const glob = useGlobSetting();
      const dynamicNoticeProps = reactive({ path: '', formData: {} });
      const [registerDetail, detailModal] = useModal();
      const router = useRouter();
      // const listData = ref(tabListData);
      // const count = computed(() => {
      //   let count = 0;
      //   for (let i = 0; i < listData.value.length; i++) {
      //     count += listData.value[i].count;
      //   }
      //   return count;
      // });
      const chatRef = ref();

      const [registerMessageModal, { openModal: openMessageModal }] = useModal();
      const [registerBookModal, { openModal: openBookModal }] = useModal();
      const [changePwdModal, { openModal: openPwdModal }] = useModal();
      //Loại tin nhắn thông báo
      const noticeType = ref<string>('system');
      //Tin nhắn chưa đọc
      const unReadNum = ref<any>({});
      
      function clickBadge(value){
        // // Trước khi mở popup danh sách tin nhắn thì xóa badge
        // for (let i = 0; i < listData.value.length; i++) {
        //   listData.value[i].count = 0;
        // }
        // Giải thích logic mã: 【QQYUN-12162】 OA cải tạo dự án, hệ thống tách lại tin nhắn, hiện tại tin nhắn đều ở cùng nhau, cần tách theo phân loại ---
        openMessageModal(true, { noticeType: value })
      }

      const popoverVisible = ref<boolean>(false);
      onMounted(() => {
       initWebSocket();
      });

      const messageCount = ref<number>(0)
      function mapAnnouncement(item) {
        return {
          ...item,
          title: item.titile,
          description: item.msgAbstract,
          datetime: item.sendTime,
        };
      }

      // Lấy tin nhắn hệ thống
      async function loadData() {
        try {
          // let { anntMsgList, sysMsgList, anntMsgTotal, sysMsgTotal } = await listCementByUser({
          //   pageSize: 5,
          // });
          // listData.value[0].list = anntMsgList.map(mapAnnouncement);
          // listData.value[1].list = sysMsgList.map(mapAnnouncement);
          // listData.value[0].count = anntMsgTotal;
          // listData.value[1].count = sysMsgTotal;
          //let msgCount = anntMsgTotal+sysMsgTotal;
          let msgCount =  await getUnreadMessageCount();
          // Giải thích logic mã: 【QQYUN-12162】 OA cải tạo dự án, hệ thống tách lại tin nhắn, hiện tại tin nhắn đều ở cùng nhau, cần tách theo phân loại ---
          unReadNum.value = msgCount;
          messageCount.value = msgCount.count?msgCount.count:0;
          // Giải thích logic mã: 【JHHB-13】 Thông báo tin nhắn ứng dụng desktop
          if (glob.isElectronPlatform) {
            window[ElectronEnum.ELECTRON_API].sendNotifyFlash(messageCount.value);
            window[ElectronEnum.ELECTRON_API].trayFlash();
          }
        } catch (e) {
          console.warn('Thông báo tin nhắn hệ thống bất thường:', e);
        }
      }

      loadData();

      function onNoticeClick(record) {
        try {
          editCementSend(record.id);
          loadData();
        } catch (e) {
          console.error(e);
        }
        if (record.openType === 'component') {
          dynamicNoticeProps.path = record.openPage;
          dynamicNoticeProps.formData = { id: record.busId };
          instance.refs.dynamicNoticeRef?.detail(record.openPage);
        } else {
          detailModal.openModal(true, {
            record,
            isUpdate: true,
          });
        }
        popoverVisible.value = false;
      }

      // Khởi tạo WebSocket
      function initWebSocket() {
        let token = getToken();
        // Dùng token đăng nhập để tạo một định danh ngắn
        let wsClientId = md5(token);
        let userId = unref(userStore.getUserInfo).id + "_" + wsClientId;
        // WebSocket khác với giao thức dùng cho yêu cầu thông thường, ws tương đương với http, wss tương đương với https
        let url = glob.domainUrl?.replace('https://', 'wss://').replace('http://', 'ws://') + '/websocket/' + userId;
        connectWebSocket(url);
        onWebSocket(onWebSocketMessage);
      }

      function onWebSocketMessage(data) {
        if (data.cmd === 'topic' || data.cmd === 'user') {
          // Giải thích logic mã: VUEN-1674 [Nghiêm trọng bug] Thông báo hệ thống, tại sao phải làm mới góc trên bên phải mới nhắc
          if(data.noticeType){
            noticeType.value = data.noticeType;
          }
          // Backend lưu dữ liệu quá chậm, Frontend làm mới tin nhắn chậm
          setTimeout(()=>{
            // Giải thích logic mã: 【JHHB-13】 Thông báo tin nhắn ứng dụng desktop
            notification(data);
            loadData();
          }, 1000)
        }
      }
      // Thông báo ứng dụng desktop
      function notification(data) {
        if (glob.isElectronPlatform && (data.noticeType || data.cmd == 'email')) {
          // Quy trình, tệp, lịch trình, hệ thống, cuộc họp
          // flow, file, plan, system, meeting
          let title = '';
          let msgTxt = '';
          let path = '';
          if (data.noticeType === 'flow') {
            title = 'Quy trình';
            path = '/task/myHandleTaskInfo';
          } else if (data.noticeType === 'file') {
            title = 'Tệp';
            path = '/file';
          } else if (data.noticeType === 'plan') {
            title = 'Lịch trình';
            path = '/plan/view';
          } else if (data.noticeType === 'system') {
            title = 'Hệ thống';
            path = '/monitor/mynews';
          } else if (data.noticeType === 'meeting') {
            title = 'Cuộc họp';
            path = '/meeting';
          } else if (data.cmd === 'email') {
            title = 'Email';
            path = '/eoa/email?type=inbox';
          }
          msgTxt = data.msgTxt ?? 'Xem chi tiết';
          window[ElectronEnum.ELECTRON_API].sendNotification(`Có tin nhắn ${title} mới`, msgTxt, path);
        }
      }
      // Xóa sạch tin nhắn
      function onEmptyNotify() {
        popoverVisible.value = false;
        readAllMsg({}, loadData);
      }
      async function reloadCount(id){
        try {
          await editCementSend(id);
          await loadData();
        } catch (e) {
          console.error(e);
        }
      }

      /**
       * Lấy số tin nhắn chưa đọc
       */
      function getSystemUnreadNum() {
      }

      function clickAddressBook() {
        openBookModal(true,{})
      }

      /**
       * Xóa tất cả tin nhắn chưa đọc
       */
      function clearAllUnMessage() {
        clearAllUnReadMessage().then((res) =>{
          if(res.success){
            loadData();
          }
        })
      }

      // Kiểm tra có phải mật khẩu mặc định không
      verifyIzDefaultPwd();
      
      /**
       * Kiểm tra có phải mật khẩu mặc định không
       */
      function verifyIzDefaultPwd() {
        defHttp.get({ url: "/sys/user/verifyIzDefaultPwd" } ,{ isTransformResponse: false }).then((res) =>{
          if(res.success){
            if(res.message.indexOf('yes') != -1){
              openPwdModal(true,{
                oldPassword: res.message.split("_")[1]
              })
            }
          }
        })
      }

      return {
        prefixCls,
        // listData,
        // count,
        clickBadge,
        registerMessageModal,
        reloadCount,
        onNoticeClick,
        onEmptyNotify,
        numberStyle: {},
        popoverVisible,
        registerDetail,
        dynamicNoticeProps,
        chatRef,
        getSystemUnreadNum,
        clickAddressBook,
        registerBookModal,
        messageCount,
        clearAllUnMessage,
        changePwdModal,
      };
    },
  });
</script>
<style lang="less">
  //noinspection LessUnresolvedVariable
  @prefix-cls: ~'@{namespace}-header-notify';

  .@{prefix-cls} {
  /*  padding-top: 2px;*/

    &__overlay {
      max-width: 340px;

      .ant-popover-inner-content {
        padding: 0;
      }

      .ant-tabs-nav {
        margin-bottom: 12px;
      }

      .ant-list-item {
        padding: 12px 24px;
        transition: background-color 300ms;

      }

      .bottom-buttons {
        text-align: center;
        border-top: 1px solid #f0f0f0;
        height: 42px;

        .ant-btn {
          border: 0;
          height: 100%;

          &:first-child {
            border-right: 1px solid #f0f0f0;
          }
        }
      }
    }

    .ant-tabs-content {
      width: 300px;
    }

    .ant-badge {
      font-size: 18px;

      .ant-badge-count {
        @badget-size: 16px;
        width: @badget-size;
        height: @badget-size;
        min-width: @badget-size;
        line-height: @badget-size;
        padding: 0;

        .ant-scroll-number-only > p.ant-scroll-number-only-unit {
          font-size: 14px;
          height: @badget-size;
        }
      }

      .ant-badge-multiple-words {
        padding: 0 0 0 2px;
        font-size: 12px;
      }

      svg {
        width: 0.9em;
      }
    }
  }

  // Tương thích chế độ tối
  [data-theme='dark'] .@{prefix-cls} {
    &__overlay {
      .ant-list-item {
        &:hover {
          background-color: #111b26;
        }
      }

      .bottom-buttons {
        border-top: 1px solid #303030;

        .ant-btn {
          &:first-child {
            border-right: 1px solid #303030;
          }
        }
      }
    }
  }

</style>
