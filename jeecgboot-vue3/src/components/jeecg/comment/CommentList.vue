<template>
  <div :style="{ position: 'relative', height: allHeight + 'px' }">
    <a-list ref="listRef" class="jeecg-comment-list" header="" item-layout="horizontal" :data-source="dataList" :style="{ height: commentHeight + 'px' }">
      <template #renderItem="{ item }">
        <a-list-item style="padding-left: 10px; flex-direction: column" @click="handleClickItem">
          <a-comment>
            <template #avatar>
              <a-avatar class="tx" :src="getAvatar(item)" :alt="getAvatarText(item)">{{ getAvatarText(item) }}</a-avatar>
            </template>

            <template #author>
              <div class="comment-author">
                <span>{{ item.fromUserId_dictText }}</span>

                <template v-if="item.toUserId">
                  <span>Trả lời</span>
                  <span>{{ item.toUserId_dictText }}</span>
                  <Tooltip class="comment-last-content" @openChange="(v)=>visibleChange(v, item)">
                    <template #title>
                      <a-spin v-if="item.commentIdLoading" size="small" tip="Đang tải..." class="jeecg-comment-tooltip-spin" />
                      <div v-else v-html="getHtml(lineFeed(item.commentId_dictText))"></div>
                    </template>
                    <message-outlined />
                  </Tooltip>
                </template>
              </div>
            </template>

            <template #datetime>
              <div>
                <Tooltip :title="item.createTime">
                  <span>{{ getDateDiff(item) }}</span>
                </Tooltip>
              </div>
            </template>

            <template #actions>
              <span @click="showReply(item)">Trả lời</span>

              <Popconfirm title="Bạn có chắc chắn muốn xóa không?" @confirm="deleteComment(item)">
                <span>Xóa</span>
              </Popconfirm>
            </template>

            <template #content>
              <div class="content" v-html="getHtml(lineFeed(item.commentContent))" style="font-size: 15px">
              </div>

              <div v-if="item.fileList && item.fileList.length > 0">
                <!-- Tệp lịch sử -->
                <history-file-list :dataList="item.fileList" isComment></history-file-list>
              </div>
            </template>
          </a-comment>
          <div v-if="item.commentStatus" class="inner-comment">
            <my-comment inner @cancel="item.commentStatus = false" @comment="(content, fileList) => replyComment(item, content, fileList)" :inputFocus="focusStatus"></my-comment>
          </div>
        </a-list-item>
      </template>
    </a-list>

    <div class="comment-area">
      <a-comment style="margin: 0 10px">
        <template #avatar>
          <a-avatar class="tx" :src="getMyAvatar()" :alt="getMyname()">{{ getMyname() }}</a-avatar>
        </template>
        <template #content>
          <my-comment ref="bottomCommentRef" @comment="sendComment" :inputFocus="focusStatus"></my-comment>
        </template>
      </a-comment>
    </div>
  </div>
</template>

<script>
  /**
   * Danh sách bình luận
   */
  import { defineComponent, ref, onMounted, watch, watchEffect ,inject, nextTick } from 'vue';
  import { propTypes } from '/@/utils/propTypes';
  // import dayjs from 'dayjs';
  // import relativeTime from 'dayjs/plugin/relativeTime';
  // import customParseFormat from 'dayjs/plugin/customParseFormat';
  // dayjs.locale('zh');
  // dayjs.extend(relativeTime);
  // dayjs.extend(customParseFormat);
  
  import { MessageOutlined } from '@ant-design/icons-vue';
  import { Comment, Spin, Tooltip } from 'ant-design-vue';
  import { useUserStore } from '/@/store/modules/user';
  import MyComment from './MyComment.vue';
  import { list, saveOne, deleteOne, useCommentWithFile, useEmojiHtml, queryById, getGloablEmojiIndex } from './useComment';
  import { useMessage } from '/@/hooks/web/useMessage';
  import HistoryFileList from './HistoryFileList.vue';
  import { Popconfirm } from 'ant-design-vue';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';

  export default defineComponent({
    name: 'CommentList',
    components: {
      MessageOutlined,
      AComment: Comment,
      Tooltip,
      MyComment,
      Popconfirm,
      HistoryFileList,
    },
    props: {
      tableId: propTypes.string.def(''),
      tableName: propTypes.string.def(''),
      dataId: propTypes.string.def(''),
      datetime:  propTypes.number.def(1),
      // Chiều cao khác cần trừ đi
      otherHeight: propTypes.number.def(0),
    },
    setup(props) {
      const { createMessage } = useMessage();
      const dataList = ref([]);
      const userStore = useUserStore();
      const dayjs = inject('$dayjs')
      const listRef = ref(null);
      /**
       * Lấy tên người dùng hiện tại
       */
      function getMyname() {
        if (userStore.getUserInfo.realname) {
          return userStore.getUserInfo.realname.substr(0, 2);
        }
        return '';
      }
      
      function getMyAvatar(){
        return getFileAccessHttpUrl(userStore.getUserInfo.avatar);
      }
      
      // Lấy ảnh đại diện
      function getAvatar(item) {
        if (item.fromUserAvatar) {
          return getFileAccessHttpUrl(item.fromUserAvatar)
        }
        return '';
      }

      // Chưa lấy được avatar thì lấy hai ký tự đầu của tên người dùng
      function getAvatarText(item){
        if (item.fromUserId_dictText) {
          return item.fromUserId_dictText.substr(0, 2);
        }
        return 'Không xác định';
      }

      function getAuthor(item) {
        if (item.toUser) {
          return item.fromUserId_dictText + ' Trả lời ' + item.fromUserId_dictText;
        } else {
          return item.fromUserId_dictText;
        }
      }

      function getDateDiff(item) {
        if (item.createTime) {
          const temp = dayjs(item.createTime, 'YYYY-MM-DD hh:mm:ss');
          return temp.fromNow();
        }
        return '';
      }
      const commentHeight = ref(300);
      const allHeight = ref(300);
      onMounted(() => {
        let otherHeight = props.otherHeight || 0;
        commentHeight.value = window.innerHeight - 57 - 46 - 70 - 160 - otherHeight;
        allHeight.value = window.innerHeight - 57 - 46 - 53 -20 - otherHeight;
      });

      /**
       * Tải dữ liệu
       * @returns {Promise<void>}
       */
      async function loadData() {
        const params = {
          tableName: props.tableName,
          tableDataId: props.dataId,
          column: 'createTime',
          order: 'desc',
        };
        const data = await list(params);
        if (!data || !data.records || data.records.length == 0) {
          dataList.value = [];
        } else {
          let array = data.records;
          console.log(123, array);
          dataList.value = array;
          // Number.MAX_SAFE_INTEGER Firefox không tương thích nên đổi thành 10e4
          nextTick(() => {
            listRef.value && listRef.value.$el && (listRef.value.$el.scrollTop = 10e5);
          });
        }
      }

      const { saveCommentAndFiles } = useCommentWithFile(props);
      // Trả lời
      async function replyComment(item, content, fileList) {
        console.log(content, item);
        let obj = {
          fromUserId: userStore.getUserInfo.id,
          toUserId: item.fromUserId,
          commentId: item.id,
          commentContent: content
        }
        await saveCommentAndFiles(obj, fileList)
        await loadData();
      }
      
      //Bình luận
      async function sendComment(content, fileList) {
        let obj = {
          fromUserId: userStore.getUserInfo.id,
          commentContent: content
        }
        await saveCommentAndFiles(obj, fileList)
        await loadData();
        focusStatus.value = false;
        setTimeout(()=>{
          focusStatus.value = true;
        },100)
      }

      //Xóa
      async function deleteComment(item) {
        const params = { id: item.id };
        await deleteOne(params);
        await loadData();
      }

      /**
       * Kích hoạt khi mở trả lời
       * @type {Ref<UnwrapRef<boolean>>}
       */
      const focusStatus = ref(false);
      function showReply(item) {
        let arr = dataList.value;
        for (let temp of arr) {
          temp.commentStatus = false;
        }
        item.commentStatus = true;
        focusStatus.value = false;
        focusStatus.value = true;
      }

      // Biểu mẫu thay đổi -Tải lại danh sách bình luận
      watchEffect(() => {
        if(props.datetime){
          if (props.tableName && props.dataId) {
            loadData();
          }
        }
      });

      //const storageEmojiIndex = inject('$globalEmojiIndex')
      const storageEmojiIndex = getGloablEmojiIndex()
      const { getHtml } = useEmojiHtml(storageEmojiIndex);
      const bottomCommentRef = ref()
      function handleClickItem(){
        bottomCommentRef.value.changeActive()
      }


      /**
       * Truy vấn thông tin bình luận theo id
       */
      async function visibleChange(v, item){
        if(v==true){
          if(!item.commentId_dictText){
            item.commentIdLoading = true;
            const data = await queryById(item.commentId);
            if(data.success == true){
              item.commentId_dictText = data.result.commentContent
            }else{
              console.error(data.message)
              item.commentId_dictText='Bình luận này đã bị xóa';
            }
            item.commentIdLoading = false;
          }
        }
      }
      // Giải thích logic mã: 【TV360X-932] bình luận thêm xuống dòng
      const lineFeed = (content) => {
        if (!content) return '';
        return content.replace(/\n/g, '<br>');
      };

      return {
        dataList,
        getAvatar,
        getAvatarText,
        getAuthor,
        getDateDiff,
        commentHeight,
        allHeight,
        replyComment,
        sendComment,
        getMyname,
        getMyAvatar,

        focusStatus,
        showReply,
        deleteComment,
        getHtml,
        handleClickItem,
        bottomCommentRef,
        visibleChange,
        listRef,
        lineFeed,
      };
    },
  });
</script>

<style lang="less" scoped>
  .jeecg-comment-list {
    overflow: auto;
    /* border-bottom: 1px solid #eee;*/
    .inner-comment {
      width: 100%;
      padding: 0 10px;
    }
    .ant-comment {
      width: 100%;
    }
    :deep(.ant-comment-avatar) {
      cursor: default;
    }
  }
  .comment-author {
    span {
      margin: 3px;
    }
    .comment-last-content {
      margin-left: 5px;
      &:hover{
        color: @primary-color;
      }
    }
  }
  .ant-list-items{
    .ant-list-item:last-child{
      margin-bottom: 46px;
    }
  }
  .tx{
    margin-top: 4px;
  }
  // Giải thích logic mã: 【QQYUN-8639] thích ứng chủ đề tối
  .comment-area {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    border-top: 1px solid #eee;
    background-color: #fff;
  }
  html[data-theme='dark'] {
    .comment-area {
      border-color: rgba(253, 253, 253, 0.12);
      background-color: #1f1f1f;
    }
    .content {
      color:rgba(255, 255, 255, 0.85);
    }
  }
</style>

<style lang="less">
  .jeecg-comment-tooltip-spin {
    .ant-spin-dot-item {
      background-color: rgba(255, 255, 255, 0.85);
    }
    .ant-spin-text {
      color: rgba(255, 255, 255, 0.85);
    }
  }
</style>
