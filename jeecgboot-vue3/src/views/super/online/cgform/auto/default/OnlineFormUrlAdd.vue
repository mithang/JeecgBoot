<template>
  <div :class="['p-2']" class="online-wrap">
    <Card :bordered="false">
      <spin :spinning="loading">
        <div :class="['wrap', `online-add-${ID}`]" ref="wrapRef">
          <OnlineForm v-if="false"></OnlineForm>
          <!-- Thêm mới(Chỉnh sửa)Popup -->
          <OnlineAutoModal
            v-if="token"
            :id="ID"
            :maskClosable="false"
            @register="registerModal"
            :getContainer="getContainer"
            @formConfig="handleFormConfig"
            @success="success"
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
  import OnlineAutoModal from './OnlineAutoModal.vue';
  import { useOnlineTableContext } from '../../hooks/auto/useOnlineTableContext';
  import { useListButton } from '../../hooks/auto/useListButton';
  import { useEnhance } from '../../hooks/auto/useEnhance';
  import { useRouter } from 'vue-router';
  import { useFormUrl } from '../../hooks/auto/useFormUrl';
  const { ID, onlineTableContext, onlineExtConfigJson, handleFormConfig } = useOnlineTableContext();
  const { createMessage: $message } = useMessage();
  const wrapRef = ref(null);
  const contentHeight = ref(400);
  const loading = ref(true);
  const router = useRouter();

  const { token } = useFormUrl();
  if (!unref(token)) {
    throw new Error('tokenkhông tồn tại~');
  }
  if (!ID.value) {
    $message.warning('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
    throw new Error('Lỗi địa chỉ, Cấu hìnhIDkhông tồn tại!');
  }
  // Xử lý phần tăng cường
  let { initCgEnhanceJs } = useEnhance(onlineTableContext);
  watch(
    () => wrapRef.value,
    (elem: HTMLElement) => {
      // Lấy chiều cao thực tế của trang
      contentHeight.value = elem.offsetHeight - 60;
    }
  );
  // Xử lý danh sáchbutton
  const { registerModal, handleAdd } = useListButton(onlineTableContext, onlineExtConfigJson);
  const getContainer = (node) => {
    return document.querySelector(`.online-add-${ID.value}`);
  };
  const success = () => {
    setTimeout(() => {
      handleAdd(false);
      setTimeout(() => {
        router.push({ path: '/online/formUrlSuccess' });
      }, 1e3);
    }, 0);
  };
  setTimeout(() => {
    handleAdd(false);
    loading.value = false;
  }, 1e3);
</script>

<style lang="less" scoped>
  .online-wrap {
    height: 100%;
    // position: relative;
    position: fixed;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    z-index: 1000;
    padding: 0;
    :deep(.ant-card) {
      padding: 0;
      height: 100%;
      border: none;
      .ant-card-body {
        height: 100%;
        padding: 40px 16px;
      }
      .ant-modal-footer {
        & > :last-child {
          display: none;
        }
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
      .ant-modal-header {
        display: none;
      }
      .ant-modal-close {
        display: none;
      }
    }
  }
</style>
