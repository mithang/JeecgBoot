<template>
  <div class="comment-tabs-warp" v-if="showStatus">
    <a-tabs v-if="show" @change="handleChange" :animated="false">
      <a-tab-pane v-if="showComment" tab="Bình luận" key="comment" class="comment-list-tab">
        <comment-list :tableId="tableId" :tableName="tableName" :dataId="dataId" :datetime="datetime1" :otherHeight="otherHeight"></comment-list>
      </a-tab-pane>
      <a-tab-pane v-if="showFiles" tab="Tệp" key="file">
        <comment-files :tableId="tableId" :tableName="tableName" :dataId="dataId" :datetime="datetime2"></comment-files>
      </a-tab-pane>
      <a-tab-pane v-if="showDataLog" tab="Nhật ký" key="log">
        <data-log-list :tableName="tableName" :dataId="dataId" :datetime="datetime3"></data-log-list>
      </a-tab-pane>
    </a-tabs>
  </div>
  <a-empty v-else description="Trang thêm mới không hỗ trợ bình luận" />
</template>

<script>
  /**
   * Khu vực bình luận
   */
  import { propTypes } from '/@/utils/propTypes';
  import { computed, ref, nextTick } from 'vue';
  import CommentList from './CommentList.vue';
  import CommentFiles from './CommentFiles.vue';
  import DataLogList from './DataLogList.vue';

  export default {
    name: 'CommentPanel',
    components: {
      CommentList,
      CommentFiles,
      DataLogList,
    },
    props: {
      tableId: propTypes.string.def(''),
      tableName: propTypes.string.def(''),
      dataId: propTypes.string.def(''),
      // Hiển thị bình luận
      showComment: propTypes.bool.def(true),
      // Hiển thị tệp
      showFiles: propTypes.bool.def(true),
      // Hiển thị nhật ký
      showDataLog: propTypes.bool.def(true),
      // Chiều cao khác cần trừ đi
      otherHeight: propTypes.number.def(0),
    },
    setup(props) {
      const showStatus = computed(() => {
        if (props.dataId && props.tableName) {
          return true;
        }
        return false;
      });

      const datetime1 = ref(1);
      const datetime2 = ref(1);
      const datetime3 = ref(1);
      const show = ref(true);
      function handleChange(e) {
        let temp = new Date().getTime();
        if (e == 'comment') {
          datetime1.value = temp;
        } else if (e == 'file') {
          datetime2.value = temp;
        } else {
          datetime3.value = temp;
        }
      }

      // VUEN-1978Bản ghi liên kết online và trường bảng khác có vấn đề 20 Sau khi sửa xong dữ liệu, mở lại mà không chuyển tab thì nhật ký sửa đổi không thay đổi
      function reload() {
        let temp = new Date().getTime();
        datetime1.value = temp;
        datetime2.value = temp;
        datetime3.value = temp;
        // Mở lại sẽ đặt lại trạng thái bên trong component
        show.value = false;
        nextTick(() => {
          show.value = true;
        });
      }

      return {
        showStatus,
        handleChange,
        datetime1,
        datetime2,
        datetime3,
        reload,
        show,
      };
    },
  };
</script>

<style lang="less" scoped>
  .comment-tabs-warp {
    height: 100%;
    overflow: visible;
    > .ant-tabs {
      overflow: visible;
    }
  }
  //antd3Sau khi nâng cấp, điều chỉnh kiểu thảo luận ở phía phải biểu mẫu
  :deep(.ant-tabs-top  .ant-tabs-nav, .ant-tabs-bottom  .ant-tabs-nav, .ant-tabs-top  div  .ant-tabs-nav, .ant-tabs-bottom  div  .ant-tabs-nav) {
    margin: 0 16px 0;
  }
</style>
