<template>
  <a-list item-layout="horizontal" :data-source="messageList" :locale="locale">
    <template #loadMore>
      <div
        v-if="messageList && messageList.length > 0 && !loadEndStatus && !loadingMoreStatus"
        :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }"
      >
        <a-button @click="onLoadMore">Tải thêm</a-button>
      </div>
      <div
        v-if="messageList && messageList.length > 0 && loadEndStatus"
        :style="{ textAlign: 'center', marginTop: '12px', height: '32px', lineHeight: '32px' }"
      >
        Không còn thêm nữa
      </div>
    </template>

    <template #renderItem="{ item }">
      <a-list-item :style="{ background: item?.izTop && item.izTop == 1 ? '#f7f7f7' : 'auto' }">
        <template #actions>
          <span>{{formatData(item.sendTime)}}</span>
          <a-rate class="antd-rate" :value="item.starFlag=='1'?1:0" :count="1" @click="clickStar(item)" style="cursor: pointer" disabled />
        </template>

        <a-list-item-meta>
          <template #description>
            <div v-if="isFormComment(item)" style="background: #f7f7f7;color: #555;padding: 2px 5px;white-space:nowrap;overflow: hidden">
              <div v-html="getHtml(item.msgContent)"></div>
            </div>
            <div>
              {{item.createTime}}
            </div>
          </template>

          <template #title>
            <div style="position: relative">
           <!--   <span style="display: inline-block; position: absolute; left: -16px">
                <exclamation-outlined v-if="noRead(item)" title="Tin nhắn chưa đọc" style="color: red" />
              </span>-->

              <span>{{ getMsgCategory(item) }}</span>
              <span v-if="item.busType == 'bpm' || item.busType == 'bpm_cc' || item.busType == 'msg_node' || item.busType == 'bpm_msg_node'" class="bpm-cuiban-content" v-html="item.msgContent">
              </span>
<!--              <a-tooltip v-else>-->
<!--                <template #title>-->
<!--                  <div v-html="item.msgContent"></div>-->
<!--                </template>-->
<!--                {{ item.titile }}-->
<!--              </a-tooltip>-->
              <span v-else>{{ item.titile }}</span>

              <a @click="showMessageDetail(item)" style="margin-left: 16px">{{ getHrefText(item) }}</a>
            </div>
          </template>
          <template #avatar>
            <template v-if="item.busType=='email'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><mail-outlined style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><mail-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="item.busType=='bpm_task'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><interaction-outlined style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><interaction-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="item.busType=='bpm'">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><alert-outlined style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><alert-outlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="['eoa_co_remind', 'eoa_co_notify'].includes(item.busType)">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><GatewayOutlined style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><GatewayOutlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else-if="['eoa_sup_remind', 'eoa_sup_notify'].includes(item.busType)">
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><AlertOutlined style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><AlertOutlined style="font-size: 16px"/></a-avatar>
            </template>

            <template v-else>
              <a-badge dot v-if="noRead(item)" class="msg-no-read">
                <a-avatar style="background: #79919d"><bell-filled style="font-size: 16px" title="Tin nhắn chưa đọc"/></a-avatar>
              </a-badge>
              <a-avatar v-else style="background: #79919d"><bell-filled style="font-size: 16px" /></a-avatar>
            </template>
          </template>
        </a-list-item-meta>
      </a-list-item>
    </template>
  </a-list>

  <keep-alive>
    <component v-if="currentModal" v-bind="bindParams" :key="currentModal" :is="currentModal" @register="modalRegCache[currentModal].register" />
  </keep-alive>
</template>

<script>

  import { FilterOutlined, CloseOutlined, BellFilled, ExclamationOutlined, MailOutlined,InteractionOutlined, AlertOutlined, GatewayOutlined } from '@ant-design/icons-vue';
  import { useSysMessage, useMessageHref } from './useSysMessage';
  import {getGloablEmojiIndex, useEmojiHtml} from "/@/components/jeecg/comment/useComment";
  import { ref, h, watch } from "vue";
  import dayjs from 'dayjs';

  export default {
    name: 'SysMessageList',
    components: {
      FilterOutlined,
      CloseOutlined,
      BellFilled,
      ExclamationOutlined,
      MailOutlined,
      InteractionOutlined,
      AlertOutlined,
      GatewayOutlined,
    },
    props:{
      star: {
        type:Boolean,
        default: false
      },
      isLowApp:{
        type:Boolean,
        default: false
      },
      messageCount: {
        type: Number,
        default: 0
      },
      cancelStarAfterDel: {
        type: Boolean,
        default: false,
      },
    },
    emits:['close', 'detail', 'clear', 'close-modal'],
    setup(props, {emit}){
      const { messageList,loadEndStatus,loadingMoreStatus,onLoadMore,noRead, getMsgCategory, getHrefText, searchParams, reset, loadData, updateStarMessage } = useSysMessage(setLocaleText);

      //Tin nhắn hệ thống
      const messageCount = ref(0);

      function reload(params){
        let { fromUser, rangeDateKey, rangeDate, noticeType } = params;
        searchParams.fromUser = fromUser||'';
        searchParams.rangeDateKey = rangeDateKey||'';
        searchParams.rangeDate = rangeDate||[];
        searchParams.noticeType = noticeType || '';
        //listKhi danh sách trống thì gán giá trị khởi tạo
        locale.value = { locale: { emptyText: `<a-empty />` }};
        if(props.star===true){
          searchParams.starFlag = '1'
        }else{
          searchParams.starFlag = ''
        }
        reset();
        loadData();
      }

      function clickStar(item){
        console.log(item)
        updateStarMessage(item);
        // Giải thích logic mã: 【TV360X-349】Thông báo - Sau khi bỏ gắn sao trong danh sách tab tin nhắn gắn sao, tin nhắn đó sẽ bị loại khỏi danh sách gắn sao
        if (item.starFlag == '1' && props.cancelStarAfterDel) {
          const findIndex = messageList.value.findIndex((item) => item.id === item.id);
          if (findIndex !== -1) {
            messageList.value.splice(findIndex, 1);
            return;
          }
        }
        if(item.starFlag=='1'){
          item.starFlag = '0'
        }else{
          item.starFlag = '1'
        }
      }

      // Giải thích logic mã: QQYUN-4744【Thông báo hệ thống】6、Thông báo hệ thống@sau đó, đối phương không nhìn ra đó là biểu mẫu nào@, không có siêu liên kết
      const { goPage, currentModal, modalRegCache, bindParams, isFormComment } = useMessageHref(emit, props);
      //const emojiIndex = inject('$globalEmojiIndex')
      const emojiIndex = getGloablEmojiIndex()
      const { getHtml } = useEmojiHtml(emojiIndex);

      function showMessageDetail(record){
        record.readFlag = '1'
        goPage(record);
        emit('close', record.id)
        // Giải thích logic mã: 【TV360X-791】Nhận thông báo qua email, nhấn trả lời thì cần đóng danh sách thông báo---
        if(record.busType==='email'){
          emit('close-modal')
        // Giải thích logic mã: 【JHHB-224】【Tin nhắn của tôi】Sau khi nhấn đi xử lý thì cần đóng hộp thoại---
        } else if(['bpm', 'bpm_task', 'tenant_invite'].includes(record.busType)){
          //Kiểm tra có phải đang xem chi tiết hay không
          if (record.msgAbstract) {
            try {
              const json = JSON.parse(record.msgAbstract);
              //Xem chi tiết, không phải đi xử lý
              if (json.taskDetail) {
                return;
              }
            } catch (e) {
              console.error('getHrefText:msgAbstractTham số không ở định dạng JSON', record.msgAbstract);
            }
          }
          emit('close-modal');
        } else if (['eoa_co_notify', 'eoa_co_remind', 'eoa_sup_notify', 'eoa_sup_remind'].includes(record.busType)) {
          emit('close-modal');
        }
      }

      //Nội dung hiển thị khi danh sách list trả về dữ liệu trống
      const locale = ref({});

      /**
       * Định dạng ngày
       */
      function formatData(data) {
        return data?dayjs(data).format('YYYY-MM-DD HH:mm'): '';
      }
      /**
       * Sự kiện nhấp vào chưa đọc
       */
      function noReadClick() {
        emit('clear')
      }

      /**
       * Đặt văn bản nhắc khi list trống
       *
       */
      function setLocaleText() {
        // Giải thích logic mã: 【QQYUN-9105】Mục chưa đọc có vấn đề---
        let rangeDateKey = searchParams.rangeDateKey;
        let value = messageCount.value;
        if (value > 0 && !props.star && rangeDateKey && rangeDateKey === '7day') {
          locale.value = {
            emptyText: h(
                'span',
                {
                  style: {'color': 'rgb(255,154,0)', 'cursor': 'pointer', 'text-align': 'left', 'display': 'block'},
                  onClick: () => {
                    noReadClick();
                  },
                }, `Còn tin nhắn chưa đọc(${value > 99 ? '99+' : value})`)
          }
        } else {
          locale.value = { locale: {emptyText: `<a-empty />` }};
        }
      }

      //Theo dõi số lượng thông tin
      watch(() => props.messageCount, (value) => {
        messageCount.value = value;
      }, { immediate: true })

      return {
        messageList,
        loadEndStatus,
        loadingMoreStatus,
        onLoadMore,
        noRead,
        getMsgCategory,
        getHrefText,
        reload,
        clickStar,
        showMessageDetail,
        isFormComment,
        getHtml,
        modalRegCache,
        currentModal,
        bindParams,
        locale,
        formatData,
      };
    },
  };
</script>

<style scoped lang="less">
   .msg-no-read{
     :deep(.ant-badge-dot){
      top: 5px;
      right: 3px;
    }
  }
   :deep(.bpm-cuiban-content) p{
    display: inherit;
     margin-bottom: 0;
     margin-top: 0;
   }

   /** QQYUN-5688 【Kiểu】Di chuột lên sao không phải hình bàn tay */
   :deep(.ant-rate-disabled){
     .ant-rate-star{
       cursor: pointer !important;
     }
   }
   .antd-rate{
     :deep(.ant-rate-star-first),
     :deep(.ant-rate-star-second) {
       color: #dddddd;
     }
     :deep(.ant-rate-star-full .ant-rate-star-second){
       color: #fadb14 !important;
     }
   }
</style>
