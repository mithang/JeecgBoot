<template>
  <div class="base-collapse">
    <div class="header"> Tích hợp DingTalk </div>
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px"> 1.Lấy thông tin tích hợp</div>
        </template>
        <div class="base-desc">Lấy thông tin tích hợp từ nền tảng mở DingTalk là có thể bắt đầu tích hợp và đồng bộ danh bạ</div>
        <div style="margin-top: 5px">
          <a href='https://help.qiaoqiaoyun.com/expand/dingding.html' target='_blank'>Cách lấy thông tin tích hợp?</a>
        </div>
      </a-collapse-panel>
    </a-collapse>
    <div class="sync-padding">
      <a-collapse expand-icon-position="right" :bordered="false">
        <a-collapse-panel key="2">
          <template #header>
            <div style="width: 100%; justify-content: space-between; display: flex">
              <div style="font-size: 16px"> 2.Nhập thông tin tích hợp và gỡ liên kết</div>
            </div>
          </template>
          <div class="base-desc">Sau khi hoàn thành bước 1, điền Agentld, AppKey, AppSecret vào là có thể tích hợp ứng dụng và đồng bộ danh bạ</div>
          <div class="flex-flow">
            <div class="base-title">CorpId</div>
            <div class="base-message">
              <a-input-password v-model:value="appConfigData.corpId" readonly />
            </div>
          </div>
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
            <a-button @click="dingEditClick">Chỉnh sửa</a-button>
            <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">Hủy liên kết</a-button>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <div class="sync-padding">
        <div style="font-size: 16px; width: 100%"> 3.Đồng bộ dữ liệu</div>
        <div style="margin-top: 20px" class="base-desc">
          Đồng bộ từ DingTalk về cục bộ
          <ul style='list-style-type: disc;margin-left: 20px;'>
            <li>Đồng bộ phòng ban về cục bộ</li>
            <li>
              Đồng bộ người dùng thuộc phòng ban về cục bộ
              <a-tooltip title='Đồng bộ tài liệu người dùng và phòng ban'>
                <a-icon @click='handleIconClick' type="question-circle" class="sync-text"/>
              </a-tooltip>
            </li>
          </ul>
          <div style="float: right">
            <a-button :loading="btnLoading" @click="syncDingTalk">{{ !btnLoading ? 'Đồng bộ' : 'Đang đồng bộ' }}</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
</template>

<script lang="ts">
  import { defineComponent, h, inject, onMounted, reactive, ref, watch } from 'vue';
  import { getThirdConfigByTenantId, syncDingTalkDepartUserToLocal, deleteThirdAppConfig } from './ThirdApp.api';
  import { useModal } from '/@/components/Modal';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { Modal } from 'ant-design-vue';
  import { getTenantId } from '/@/utils/auth';
  import { useMessage } from '/@/hooks/web/useMessage';

  export default defineComponent({
    name: 'OrganDingConfigForm',
    components: {
      ThirdAppConfigModal,
    },
    setup() {
      const { createMessage } = useMessage();
      //Chọn bảng thu gọnkey
      const collapseActiveKey = ref<string>('');
      //Sự kiện tải nút
      const btnLoading = ref<boolean>(false);
      //Dữ liệu cấu hình bên thứ ba
      const appConfigData = ref<any>({
        agentId: undefined,
        clientId: '',
        clientSecret: '',
      });

      //Cấu hình WeCom và DingTalkmodal
      const [registerAppConfigModal, { openModal }] = useModal();

      /**
       * Chỉnh sửa DingTalk
       */
      async function dingEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'dingtalk',
        });
      }

      /**
       * Khởi tạo dữ liệu bên thứ ba
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
       * Callback thành công
       */
      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'dingtalk' });
      }

      /**
       * Đồng bộ DingTalk
       */
      async function syncDingTalk() {
        btnLoading.value = true;
        await syncDingTalkDepartUserToLocal()
          .then((res) => {
            let options = {};
            if (res.result) {
              options = {
                width: 600,
                title: res.message,
                content: () => {
                  let nodes;
                  let successInfo = [`Thông tin thành công như sau:`, renderTextarea(h, res.result.successInfo.map((v, i) => `${i + 1}. ${v}`).join('\n'))];
                  if (res.success) {
                    nodes = [...successInfo, h('br'), `Không có thông tin thất bại!`];
                  } else {
                    nodes = [
                      `Thông tin thất bại như sau:`,
                      renderTextarea(h, res.result.failInfo.map((v, i) => `${i + 1}. ${v}`).join('\n')),
                      h('br'),
                      ...successInfo,
                    ];
                  }
                  return nodes;
                },
              };
            }
            if (res.success) {
              if (options != null) {
                Modal.success(options);
              } else {
                createMessage.warning(res.message);
              }
            } else {
              if (options && options.title) {
                Modal.warning(options)
              } else {
                createMessage.warning({
                  content: res.message || "Đồng bộ thất bại, vui lòng kiểm tra thông tin tích hợp đã nhập có đúng không, đồng thời xác nhận đã bật cấu hình DingTalk chưa!",
                  duration: 5
                });
              }
            }
          })
          .finally(() => {
            btnLoading.value = false;
          });
      }

      /**
       * Kết xuất văn bản
       * @param h
       * @param value
       */
      function renderTextarea(h, value) {
        return h(
          'div',
          {
            id: 'box',
            style: {
              minHeight: '100px',
              border: '1px solid #d9d9d9',
              fontSize: '14px',
              maxHeight: '250px',
              whiteSpace: 'pre',
              overflow: 'auto',
              padding: '10px',
            },
          },
          value
        );
      }

      /**
       * Tài liệu đồng bộ DingTalk
       */
      function handleIconClick(){
        window.open("https://help.qiaoqiaoyun.com/expand/dingdingsyn.html","_target")
      }

      /**
       * Hủy liên kết
       */
      function cancelBindClick() {
        if(!appConfigData.value.id){
          createMessage.warning("Vui lòng liên kết ứng dụng DingTalk trước!");
          return;
        }
        Modal.confirm({
          title: 'Hủy liên kết',
          content: 'Có muốn gỡ liên kết cấu hình ứng dụng DingTalk của tổ chức hiện tại không?',
          okText: 'Xác nhận',
          cancelText: 'Hủy',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }
      
      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'dingtalk' });
      });

      return {
        appConfigData,
        collapseActiveKey,
        registerAppConfigModal,
        dingEditClick,
        handleSuccess,
        syncDingTalk,
        btnLoading,
        handleIconClick,
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
  [data-theme='dark'] .base-collapse .ant-collapse{
    border: none !important;
  }
  /*end Tương thích chế độ tối*/
  /*Kiểu dấu chấm hỏi của nút tài liệu*/
  .sync-text{
    margin-left: 2px;
    cursor: pointer;
    position: relative;
    top: 2px
  }
 :deep(.ant-collapse-borderless >.ant-collapse-item:last-child) {border-bottom-width:1px;}
</style>
