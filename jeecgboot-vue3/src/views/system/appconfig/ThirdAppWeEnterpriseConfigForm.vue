<template>
  <div class="base-collapse">
    <div class="header"> Tích hợp WeCom </div>
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px"> 1.Lấy thông tin tích hợp</div>
        </template>
        <div class="base-desc">Lấy thông tin tích hợp từ nền tảng WeCom là có thể bắt đầu tích hợp và đồng bộ danh bạ</div>
        <div style="margin-top: 5px">
          <a href="https://help.qiaoqiaoyun.com/expand/dingding.html" target="_blank">Cách lấy thông tin tích hợp?</a>
        </div>
      </a-collapse-panel>
    </a-collapse>
    <div>
      <a-collapse expand-icon-position="right" :bordered="false">
        <a-collapse-panel key="2">
          <template #header>
            <div style="width: 100%; justify-content: space-between; display: flex">
              <div style="font-size: 16px"> 2.Nhập thông tin tích hợp và gỡ liên kết</div>
            </div>
          </template>
          <div class="flex-flow">
            <div class="base-title">Agentld</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.agentId" readonly />
            </div>
          </div>
          <div class="flex-flow">
            <div class="base-title">AppKey</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.clientId" readonly />
            </div>
          </div>
          <div class="flex-flow">
            <div class="base-title">AppSecret</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.clientSecret" readonly />
            </div>
          </div>
          <div style="margin-top: 20px; width: 100%; text-align: right">
            <a-button @click="weEnterpriseEditClick">Chỉnh sửa</a-button>
            <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">Hủy liên kết</a-button>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div class="sync-padding">
        <div style="font-size: 16px; width: 100%"> 3.Đồng bộ dữ liệu</div>
        <div style="margin-top: 20px" class="base-desc">
          Đồng bộ từ WeCom vào Qiaoqiao Cloud
          <a style="margin-left: 10px" @click="seeBindWeChat">Xem người dùng WeCom đã liên kết</a>
          <div style="float: right">
            <a-button @loading="btnLoading" @click="thirdUserByWechat">Đồng bộ</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
  <ThirdAppBindWeEnterpriseModal @register="registerBindAppConfigModal" @success="handleBindSuccess" />
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref } from 'vue';
  import { getThirdConfigByTenantId, deleteThirdAppConfig } from './ThirdApp.api';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getTenantId } from '@/utils/auth';
  import ThirdAppBindWeEnterpriseModal from './ThirdAppBindWeEnterpriseModal.vue';
  import { Modal } from "ant-design-vue";

  export default defineComponent({
    name: 'ThirdAppWeEnterpriseConfigForm',
    components: {
      ThirdAppConfigModal,
      ThirdAppBindWeEnterpriseModal,
    },
    setup() {
      const btnLoading = ref<boolean>(false);
      //Dữ liệu cấu hình bên thứ ba
      const appConfigData = ref<any>({
        agentId: '',
        clientId: '',
        clientSecret: '',
      });
      //Cấu hình WeCom và DingTalkmodal
      const [registerAppConfigModal, { openModal }] = useModal();
      const [registerBindAppConfigModal, { openModal: openBindModal }] = useModal();
      const { createMessage } = useMessage();

      /**
       * Khởi tạo dữ liệu
       *
       * @param params
       */
      async function initThirdAppConfigData(params) {
        let values = await getThirdConfigByTenantId(params);
        if (values) {
          appConfigData.value = values;
        } else {
          appConfigData.value = "";
        }
      }

      /**
       * Chỉnh sửa WeCom
       */
      async function weEnterpriseEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'wechat_enterprise',
        });
      }

      /**
       * Lấy người dùng đã liên kết WeCom
       */
      async function thirdUserByWechat() {
        openBindModal(true, { izBind: false });
      }

      /**
       * Callback thành công
       */
      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'wechat_enterprise' });
      }

      /**
       * Giá trị trả về khi liên kết thành công
       *
       * @param options
       * @param item
       */
      function handleBindSuccess(options, item) {
        console.log("options:::",options)
        console.log("item:::",item)
        if (item.success) {
          if (options != null) {
            Modal.success(options);
          } else {
            createMessage.warning(item.message);
          }
        } else {
          if (options && options.title) {
            Modal.warning(options);
          } else {
            createMessage.warning({
              content: 'Đồng bộ thất bại, vui lòng kiểm tra thông tin tích hợp đã nhập có đúng không, đồng thời xác nhận đã bật cấu hình WeCom chưa!',
              duration: 5,
            });
          }
        }
      }
      
      /**
       * Xem WeCom đã liên kết
       */
      function seeBindWeChat() {
        openBindModal(true,{ izBind: true })
      }

      /**
       * Hủy liên kết
       */
      function cancelBindClick() {
        if(!appConfigData.value.id){
          createMessage.warning("Vui lòng liên kết ứng dụng WeCom trước!");
          return;
        }
        Modal.confirm({
          title: 'Hủy liên kết',
          content: 'Có muốn gỡ liên kết cấu hình ứng dụng WeCom của tổ chức hiện tại không?',
          okText: 'Xác nhận',
          cancelText: 'Hủy',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }
      
      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'wechat_enterprise' });
      });

      return {
        appConfigData,
        weEnterpriseEditClick,
        registerAppConfigModal,
        registerBindAppConfigModal,
        handleSuccess,
        btnLoading,
        thirdUserByWechat,
        handleBindSuccess,
        seeBindWeChat,
        cancelBindClick,
      };
    },
  });
</script>

<style lang="less" scoped>
  .header {
    align-items: center;
    box-sizing: border-box;
    display: flex;
    height: 50px;
    justify-content: space-between;
    font-weight: 700;
    font-size: 18px;
    color: @text-color;
  }

  .flex-flow {
    display: flex;
    min-height: 0;
  }

  .sync-padding {
    padding: 12px 0 16px;
    color: @text-color;
  }

  .base-collapse {
    margin-top: 20px;
    padding: 0 24px;
    font-size: 20px;

    .base-desc {
      font-size: 14px;
      color: @text-color;
    }

    .base-title {
      width: 100px;
      text-align: left;
      height: 50px;
      line-height: 50px;
    }

    .base-message {
      width: 100%;
      height: 50px;
      line-height: 50px;
    }

    :deep(.ant-collapse-header) {
      padding: 12px 0 16px;
    }

    :deep(.ant-collapse-content-box) {
      padding-left: 0;
    }
  }
  /*begin Tương thích chế độ tối*/
  //Ở chế độ tối, viền thẻ được đặt thànhnone
  [data-theme='dark'] .base-collapse .ant-collapse {
    border: none !important;
  }
  /*end Tương thích chế độ tối*/
  /*Kiểu dấu chấm hỏi của nút tài liệu*/
  .sync-text {
    margin-left: 2px;
    cursor: pointer;
    position: relative;
    top: 2px;
  }
</style>
