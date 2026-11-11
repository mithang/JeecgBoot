<template>
  <BasicModal :title="title" :width="modalWidth" :maxHeight="600" :enableComment="enableComment" :defaultFullscreen="false" v-bind="$attrs" @register="registerModal" wrapClassName="jeecg-online-detail-modal" @commentOpen="handleCommentOpen">
    <template #footer>
      <a-row>
        <a-col :span="24 - commentSpan">
          <a-button v-if="cancelBtnCfg.enabled" key="back" @click="handleCancel">
            <span>{{ cancelBtnCfg.buttonName }}</span>
          </a-button>
          <slot name="footerBtn"></slot>
        </a-col>
      </a-row>
    </template>
    <online-form-detail ref="onlineFormCompRef" :id="id" :form-template="formTemplate" :show-sub="showSub" :themeTemplate="isShare && themeTemplate === 'erp' ? 'normal' : themeTemplate" :isShare="isShare" @rendered="renderSuccess" />

    <template #comment>
      <comment-panel ref="commentPanelRef" :tableId="tableId" :tableName="tableName" :dataId="formDataId"></comment-panel>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  import OnlineFormDetail from '../comp/OnlineFormDetail.vue';
  import { useAutoModal } from '../../hooks/auto/useAutoModal';
  import CommentPanel from '/@/components/jeecg/comment/CommentPanel.vue'
  import { ERPSUBTABLE, INNER_TABLE } from '../../util/constant';

  export default defineComponent({
    name: 'OnlineDetailModal',
    props: {
      id: {
        type: String,
        required: false,
        default: '',
      },
      // Để phân biệt nguồn gốc, khi mã hóa chủ yếu làerpBảng con có xử lý đặc biệt
      source: {
        type: String,
        default: '',
      },
      cancelBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: 'Đóng',
            buttonIcon: '',
          }
        }
      },
      // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
      isShare: {
        type: Boolean,
        default: false,
      },
      // update-end--author:liaozhiyang---date:20260807---for：【LHZP-166】Khắc phụcerpLiên kết ngoài của phong cách không có bảng con
    },
    components: {
      BasicModal,
      OnlineFormDetail,
      CommentPanel
    },
    emits: ['success', 'register','formConfig'],
    setup(props, {emit}) {
      console.log('Vào popup biểu mẫu 》》》》modal');

      const commentPanelRef = ref();
      const commentSpan = ref(0);
      function reloadComment(){
        if(commentPanelRef.value)
          commentPanelRef.value.reload();
      }
      // update-begin--author:liaozhiyang---date:20260807---for：【LHZP-7】Sau khi liên kết ngoài bật bình luận, nút chỉnh sửa dữ liệu bị lộn xộn
      const handleCommentOpen = (visible, span) => {
        commentSpan.value = span;
      };
      // update-end--author:liaozhiyang---date:20260807---for：【LHZP-7】Sau khi liên kết ngoài bật bình luận, nút chỉnh sửa dữ liệu bị lộn xộn
      
      const {
        title,
        modalWidth,
        registerModal,
        cgButtonList,
        handleCgButtonClick,
        disableSubmit,
        handleSubmit,
        submitLoading,
        handleCancel,
        handleFormConfig,
        onlineFormCompRef,
        formTemplate,
        isTreeForm,
        pidFieldName,
        renderSuccess,
        formRendered,
        showSub,
        tableName,
        tableId,
        formDataId,
        enableComment,
        themeTemplate,
      } = useAutoModal(false, { emit }, reloadComment);

      // Lắng nghe (watch)idThay đổi Render lại biểu mẫu
      watch(() => props.id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!props.id) {
          return;
        }
        console.log('Render lại biểu mẫu 》》》》modal');
        // update-begin--author:liaozhiyang---date:20240426---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xem dữ liệu thêm mới và chi tiết của bảng phụ thuộc
        let params: any = {};
        //update-begin---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
        if (props.source === ERPSUBTABLE || props.source === INNER_TABLE) {
        //update-end---author:wangshuai---date:2025-10-21---for:【issues/8933】Danh sách chủ đề bảng con nhúng (một-nhiều) nhấp+Số mở rộng chi tiết nhắc nhở: không có quyền truy cập(Thao tác)---
          params.tabletype = 3;
        }
        await handleFormConfig(props.id, params);
        // update-end--author:liaozhiyang---date:20240426---for：【issues/6124】Khi người dùng không có【OnlinePhát triển biểu mẫu】Khi có quyền của trang, người dùng không có quyền xem dữ liệu thêm mới và chi tiết của bảng phụ thuộc
      }

      const that = {
        title,
        onlineFormCompRef,
        renderSuccess,
        registerModal,
        handleSubmit,
        handleCancel,
        modalWidth,
        formTemplate,
        disableSubmit,
        cgButtonList,
        handleCgButtonClick,
        isTreeForm,
        pidFieldName,
        submitLoading,
        showSub,
        tableName,
        tableId,
        formDataId,
        enableComment,
        commentPanelRef,
        themeTemplate,
        handleCommentOpen,
        commentSpan,
      };

      return that;
    },
  });
</script>

<style scoped></style>
