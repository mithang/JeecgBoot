<template>
  <div :class="['p-2']" class="online-wrap">
    <Card :bordered="false">
      <spin :spinning="loading">
        <div :class="['wrap', `online-detail-${ID}`]" ref="wrapRef">
          <OnlineForm v-if="false"></OnlineForm>
          <!-- Popup chi tiết -->
          <OnlineDetailModal
            v-if="token"
            :id="ID"
            :maskClosable="false"
            @register="registerDetailModal"
            @success="success"
            :getContainer="getContainer"
            :height="contentHeight"
          />
        </div>
      </spin>
    </Card>
  </div>
</template>

<script setup lang="ts">
  import { ref, watch, unref } from 'vue';
  import { Card, Spin } from 'ant-design-vue';
  // OnlineForm Cái này bắt buộc phải tham chiếu,setupChế độ phải ởtemplatesử dụng ở bên trong. Nếu khôngurlVào sẽ báo lỗi
  import OnlineForm from '../comp/OnlineForm.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useRoute } from 'vue-router';
  import OnlineDetailModal from './OnlineDetailModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { useFormUrl } from '../../hooks/auto/useFormUrl';
  const route = useRoute();
  const { ID, onlineTableContext, onlineExtConfigJson } = useOnlineTableContext();
  const { createMessage: $message } = useMessage();
  const wrapRef = ref(null);
  const contentHeight = ref(400);
  const loading = ref(true);

  const { token } = useFormUrl();
  if (!unref(token)) {
    throw new Error('tokenkhông tồn tại~');
  }
  if (!ID.value || !route.params.dataId) {
    $message.warning('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
    throw new Error('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
  }
  watch(
    () => wrapRef.value,
    (elem: HTMLElement) => {
      // Lấy chiều cao thực tế của trang
      contentHeight.value = elem.offsetHeight;
    }
  );
  // Xử lý phần tăng cường
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  // Xử lý danh sáchbutton
  const { registerDetailModal, openDetailModal } = useListButton(onlineTableContext, onlineExtConfigJson);
  const getContainer = (node) => {
    return document.querySelector(`.online-detail-${ID.value}`);
  };
  setTimeout(() => {
    openDetailModal(true, {
      isUpdate: true,
      disableSubmit: true,
      record: {
        id: route.params.dataId,
      },
    });
    loading.value = false;
  }, 1e3);
</script>

<style lang="less" scoped>
  .online-wrap {
    height: 100%;
    position: relative;
    :deep(.ant-card) {
      padding: 0;
      height: 100%;
      border: none;
      .ant-card-body {
        height: 100%;
        padding: 5px;
      }
    }
    :deep(.ant-spin-nested-loading) {
      height: 100%;
      width: 100%;
      .ant-spin-container {
        height: 100%;
      }
    }
  }
  .wrap {
    height: 100%;
    > div {
      height: 100%;
    }
    :deep(.ant-modal-root) {
      height: 100%;
      .ant-modal-mask {
        height: 0;
      }
      .ant-modal-wrap {
        position: static;
        height: 100%;
      }
      .ant-modal {
        position: static;
        height: 100%;
        width: 100% !important;
        height: 100% !important;
        transform: scale(1) !important;
        opacity: 1;
        animation-name: none !important;
      }
      .ant-modal-content {
        position: static;
        box-shadow: none;
        .scroll-container {
          padding: 0;
          .scrollbar__view > div {
            overflow: auto;
          }
        }
      }
      .ant-modal-header,
      .ant-modal-footer {
        display: none;
      }
      .ant-modal-close {
        display: none;
      }
    }
  }
</style>
