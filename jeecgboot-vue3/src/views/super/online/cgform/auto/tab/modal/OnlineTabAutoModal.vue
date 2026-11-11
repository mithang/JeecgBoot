<template>
  <BasicModal
    :title="title"
    @cancel="
      () => {
        onCloseEvent();
        restTabIndex();
      }
    "
    :enableComment="enableComment"
    :width="modalWidth"
    v-bind="$attrs"
    :maxHeight="600"
    @register="registerModal"
    wrapClassName="jeecg-online-modal"
    @ok="handleSubmit"
    @commentOpen="handleCommentOpen"
  >
    <!--update-begin--author:liaozhiyang---date:20230821---for：【QQYUN-6305】tabChủ đề một-nhiều-->
    <template v-if="themeTemplate === TAB" #title>
      <div class="titleArea">
        <div class="title">{{ title }}</div>
        <div class="right">
          <a-dropdown-button v-if="showDropdownBtn" trigger="click">
            {{ tabNav[+tabIndex + 1].tableTxt }}
            <template #overlay>
              <a-menu @click="handleMenuClick">
                <a-menu-item v-for="(item, index) in tabNav" :key="tabValue(index)">
                  {{ item.tableTxt }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown-button>
          <a-radio-group v-else v-model:value="tabIndex">
            <a-radio-button v-for="(item, index) in tabNav" :value="tabValue(index)" :key="item.tableName">{{ item.tableTxt }}</a-radio-button>
          </a-radio-group>
        </div>
      </div>
    </template>
    <!--update-end--author:liaozhiyang---date:20230821---for：【QQYUN-6305】tabChủ đề một-nhiều-->
    <template #footer>
      <a-row>
        <a-col :span='24 - commentSpan'>
          <a-button
            v-for="btn in cgButtonList"
            :key="btn.id"
            type="primary"
            @click="handleCgButtonClick(btn.optType, btn.buttonCode)"
            :preIcon="btn.buttonIcon ? 'ant-design:' + btn.buttonIcon : ''"
          >
            {{ btn.buttonName }}
          </a-button>

          <a-button
              v-if="!disableSubmit && confirmBtnCfg.enabled"
              key="submit"
              type="primary"
              :preIcon="confirmBtnCfg.buttonIcon"
              :loading="submitLoading"
              @click="handleSubmit"
          >
            <span>{{ confirmBtnCfg.buttonName }}</span>
          </a-button>
          <a-button
            key="back"
            @click="
              () => {
                handleCancel();
                restTabIndex();
              }
            "
            >Đóng</a-button
          >
        </a-col>
      </a-row>
    </template>
    <online-form
      ref="onlineFormCompRef"
      :id="id"
      :disabled="disableSubmit"
      :form-template="formTemplate"
      :isTree="isTreeForm"
      :pidField="pidFieldName"
      :themeTemplate="themeTemplate"
      :tabIndex="tabIndex"
      :cgBIBtnMap="cgBIBtnMap"
      :buttonSwitch="buttonSwitch"
      @rendered="renderSuccess"
      @success="handleSuccess"
      @toggleTab="handleToggleTab"
    >
    </online-form>

    <template #comment>
      <comment-panel ref="commentPanelRef" :tableId="tableId" :tableName="tableName" :dataId="formDataId" />
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, watch, ref, computed } from 'vue';
  import { BasicModal } from '/@/components/Modal';
  import OnlineForm from './OnlineTabForm.vue';
  import { useAutoModal } from '../../../hooks/auto/useAutoModal';
  import CommentPanel from '/@/components/jeecg/comment/CommentPanel.vue';
  import { TAB } from '../../../util/constant';
  import { useAppInject } from '/@/hooks/web/useAppInject';

  export default defineComponent({
    name: 'OnlineTabAutoModal',
    props: {
      id: {
        type: String,
        default: '',
      },
      cgBIBtnMap: Object,
      buttonSwitch: Object,
      confirmBtnCfg: {
        type: Object,
        default: () => {
          return {
            enabled: true,
            buttonName: 'Xác nhận',
            buttonIcon: '',
          }
        }
      },
    },
    components: {
      BasicModal,
      OnlineForm,
      CommentPanel,
    },
    emits: ['success', 'register', 'formConfig'],
    setup(props, { emit }) {
      console.log('Vào popup biểu mẫu 》》》》modal');
      const commentPanelRef = ref();
      const tabNav = ref<any>([]);
      const tabIndex = ref<any>('-1');
      const commentSpan = ref(0);
      const { getIsMobile } = useAppInject();

      function reloadComment() {
        if (commentPanelRef.value) commentPanelRef.value.reload();
      }

      const {
        title,
        modalWidth,
        registerModal,
        closeModal,
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
        tableName,
        tableId,
        formDataId,
        enableComment,
        onCloseEvent,
        themeTemplate,
      } = useAutoModal(false, { emit }, reloadComment);

      function handleSuccess(formData) {
        emit('success', formData);
        closeModal();
        // Gửi hoàn tất Kích hoạt sự kiện đóng
        onCloseEvent();
        restTabIndex();
      }

      // Lắng nghe (watch)idThay đổi Render lại biểu mẫu
      watch(() => props.id, renderFormItems, { immediate: true });
      async function renderFormItems() {
        formRendered.value = false;
        if (!props.id) {
          return;
        }
        console.log('Render lại biểu mẫu 》》》》modal');
        await handleFormConfig(props.id, {}, (result) => {
          const nav: any = [];
          const sub: any = [];
          const { head, schema } = result;
          const { properties } = schema;
          nav.push({ tableName: head.tableName, tableTxt: head.tableTxt });
          Object.entries(properties).forEach(([key, value]: any) => {
            if (value.view == 'tab') {
              sub.push({ tableName: key, tableTxt: value.describe, order: value.order });
            }
          });
          sub.sort((a, b) => a.order - b.order);
          tabNav.value = [...nav, ...sub];
        });
      }
      const tabValue = (index) => {
        return String(index - 1);
      };
      const restTabIndex = () => {
        // Khi đóng, đặt lại chỉ mục về vị trí đầu tiên (ngăn dữ liệu tiếp theo mở ra vẫn là cùng mộttab）
        setTimeout(() => {
          tabIndex.value = '-1';
        }, 500);
      };
      // Phía di động và lớn hơn2Khi có bản ghi dữ liệu thì hiển thị menu dropdown
      const showDropdownBtn = computed(() => {
        return getIsMobile.value && tabNav.value.length > 2;
      });
      const handleMenuClick = ({ key }) => {
        tabIndex.value = key;
      };
      const handleToggleTab = (key) => {
        tabIndex.value = key;
      };
      // update-begin--author:liaozhiyang---date:20240528---for：【TV360X-485】Sau khi bật bình luận, nút popup nằm ở bên phải, cách một khoảng bằng một bình luận
      const handleCommentOpen =  (visible, span) => { 
        console.log('Bình luận có mở rộng hay không:', visible);
        commentSpan.value = span;
      }
      // update-end--author:liaozhiyang---date:20240528---for：【TV360X-485】Sau khi bật bình luận, nút popup nằm ở bên phải, cách một khoảng bằng một bình luận
      const that = {
        title,
        onlineFormCompRef,
        renderSuccess,
        registerModal,
        handleSubmit,
        handleSuccess,
        handleCancel,
        modalWidth,
        formTemplate,
        disableSubmit,
        cgButtonList,
        handleCgButtonClick,
        isTreeForm,
        pidFieldName,
        submitLoading,
        tableName,
        tableId,
        formDataId,
        enableComment,
        commentPanelRef,
        onCloseEvent,
        themeTemplate,
        tabNav,
        tabValue,
        tabIndex,
        TAB,
        restTabIndex,
        handleMenuClick,
        showDropdownBtn,
        handleToggleTab,
        handleCommentOpen,
        commentSpan,
      };

      return that;
    },
  });
</script>

<style lang="less" scoped>
  .titleArea {
    display: flex;
    align-content: center;
    padding-right: 70px;
    .title {
      margin-right: 16px;
      line-height: 32px;
    }
    .right {
      overflow-x: auto;
      overflow-y: hidden;
      flex: 1;
      white-space: nowrap;
      .ant-radio-group {
        font-weight: normal;
        
      }
    }
  }
  .footerWrap {
    &.center {
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  html[data-theme='light'] {
    .right {
      .ant-radio-group {
        :deep(.ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked)) {
          color: #555;
        }
      }
    }
  }
</style>
