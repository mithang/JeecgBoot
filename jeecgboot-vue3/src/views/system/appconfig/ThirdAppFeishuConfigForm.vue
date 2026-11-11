<template>
  <div class="base-collapse">
    <div class="header">Tích hợp Feishu</div>

    <!-- Bước 1: Lấy thông tin tích hợp -->
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="1">
        <template #header>
          <div style="font-size: 16px">1. Lấy thông tin tích hợp</div>
        </template>
        <div class="base-desc"> Đăng nhập nền tảng mở Feishu để tạo ứng dụng tự xây dựng cho doanh nghiệp, sau khi lấy App ID và App Secret là có thể bắt đầu tích hợp </div>
        <div style="margin-top: 5px">
          <a href="https://open.feishu.cn/app" target="_blank">Đi tới nền tảng mở Feishu để tạo ứng dụng</a>
        </div>
        <div style="margin-top: 8px" class="base-desc">
          <ul style="list-style-type: disc; margin-left: 20px">
            <li>Vào chi tiết ứng dụng → Thông tin xác thực và thông tin cơ bản, lấy App ID và App Secret</li>
          </ul>
        </div>
      </a-collapse-panel>
    </a-collapse>

    <!-- Bước 2: Nhập thông tin tích hợp -->
    <a-collapse expand-icon-position="right" :bordered="false">
      <a-collapse-panel key="2">
        <template #header>
          <div style="width: 100%; justify-content: space-between; display: flex">
            <div style="font-size: 16px">2. Nhập thông tin tích hợp và gỡ liên kết</div>
          </div>
        </template>
        <div class="base-desc">Sau khi điền App ID và App Secret, có thể bật đăng nhập quét mã OAuth2 của Feishu</div>
        <div class="flex-flow">
          <div class="base-title">App ID</div>
          <div class="base-message">
            <a-input-password v-model:value="appConfigData.clientId" readonly />
          </div>
        </div>
        <div class="flex-flow">
          <div class="base-title">App Secret</div>
          <div class="base-message">
            <a-input-password v-model:value="appConfigData.clientSecret" readonly />
          </div>
        </div>
        <div style="margin-top: 20px; width: 100%; text-align: right">
          <a-button @click="feishuEditClick">Chỉnh sửa</a-button>
          <a-button v-if="appConfigData.id" @click="cancelBindClick" danger style="margin-left: 10px">Hủy liên kết</a-button>
        </div>
      </a-collapse-panel>
    </a-collapse>

    <!-- Bước 3: Đồng bộ dữ liệu -->
    <div class="sync-padding">
      <div style="font-size: 16px; width: 100%">3. Đồng bộ dữ liệu</div>
      <div style="margin-top: 20px" class="base-desc">
        Đồng bộ từ Feishu vào hệ thống này (Feishu→cục bộ）
        <ul style="list-style-type: disc; margin-left: 20px">
          <li>Đồng bộ cấu trúc phòng ban trong danh bạ Feishu về cục bộ</li>
          <li>
            Đồng bộ người dùng thuộc phòng ban về cục bộ, đồng thời tự động tạo tài khoản hệ thống
            <a-tooltip title="Cần bật các quyền contact:department:read và contact:user.base:readonly trong quản lý quyền của ứng dụng Feishu, đồng thời phát hành ứng dụng">
              <QuestionCircleOutlined class="sync-tip-icon" />
            </a-tooltip>
          </li>
          <li>Người dùng và phòng ban đã tồn tại sẽ bị bỏ qua, không tạo trùng lặp</li>
        </ul>
        <div style="float: right; padding-bottom: 20px">
          <a-button :loading="btnLoading" :disabled="!appConfigData.id" @click="syncFeishu">
            {{ btnLoading ? 'Đang đồng bộ...' : 'Đồng bộ danh bạ' }}
          </a-button>
        </div>
      </div>
    </div>
  </div>

  <ThirdAppConfigModal @register="registerAppConfigModal" @success="handleSuccess" />
</template>

<script lang="ts">
  //update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu
  import { defineComponent, h, onMounted, ref } from 'vue';
  import { deleteThirdAppConfig, getThirdConfigByTenantId, syncFeishuDepartUserToLocal } from './ThirdApp.api';
  import { useModal } from '/@/components/Modal';
  import ThirdAppConfigModal from './ThirdAppConfigModal.vue';
  import { Modal } from 'ant-design-vue';
  import { getTenantId } from '/@/utils/auth';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { QuestionCircleOutlined } from '@ant-design/icons-vue';
  import { useGlobSetting } from '@/hooks/setting';

  export default defineComponent({
    name: 'ThirdAppFeishuConfigForm',
    components: {
      ThirdAppConfigModal,
      QuestionCircleOutlined,
    },
    setup() {
      const { createMessage } = useMessage();
      const globSetting = useGlobSetting();
      const btnLoading = ref<boolean>(false);
      const appConfigData = ref<any>({
        clientId: '',
        clientSecret: '',
      });
      const baseApiUrl = globSetting.domainUrl;
      const [registerAppConfigModal, { openModal }] = useModal();

      async function feishuEditClick() {
        let tenantId = getTenantId();
        openModal(true, {
          tenantId: tenantId,
          thirdType: 'feishu',
        });
      }

      async function initThirdAppConfigData(params) {
        let values = await getThirdConfigByTenantId(params);
        appConfigData.value = values || { clientId: '', clientSecret: '' };
      }

      function handleSuccess() {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'feishu' });
      }

      /**
       * Đồng bộ danh bạ Feishu (Feishu→cục bộ）
       */
      async function syncFeishu() {
        if (!appConfigData.value.id) {
          createMessage.warning('Vui lòng cấu hình thông tin ứng dụng Feishu trước');
          return;
        }
        btnLoading.value = true;
        await syncFeishuDepartUserToLocal()
          .then((res) => {
            const options: any = {};
            if (res.result) {
              options.width = 600;
              options.title = res.message;
              options.content = () => {
                const nodes: any[] = [];
                const successList = res.result.successInfo || [];
                const failList = res.result.failInfo || [];
                if (failList.length > 0) {
                  nodes.push('Thông tin thất bại như sau:');
                  nodes.push(renderTextarea(h, failList.map((v, i) => `${i + 1}. ${v}`).join('\n')));
                  nodes.push(h('br'));
                }
                if (successList.length > 0) {
                  nodes.push('Thông tin thành công như sau:');
                  nodes.push(renderTextarea(h, successList.map((v, i) => `${i + 1}. ${v}`).join('\n')));
                } else {
                  nodes.push('Không có thông tin thành công');
                }
                return nodes;
              };
            }
            if (res.success) {
              options.title ? Modal.success(options) : createMessage.success(res.message || 'Đồng bộ thành công');
            } else {
              options.title ? Modal.warning(options) : createMessage.warning(res.message || 'Đồng bộ hoàn thành (bao gồm các mục thất bại)');
            }
          })
          .catch(() => {
            createMessage.error('Yêu cầu đồng bộ thất bại, vui lòng kiểm tra cấu hình ứng dụng Feishu và mạng');
          })
          .finally(() => {
            btnLoading.value = false;
          });
      }

      function renderTextarea(h, value) {
        return h(
          'div',
          {
            style: {
              minHeight: '100px',
              border: '1px solid #d9d9d9',
              fontSize: '14px',
              maxHeight: '250px',
              whiteSpace: 'pre',
              overflow: 'auto',
              padding: '10px',
              borderRadius: '4px',
            },
          },
          value
        );
      }

      function cancelBindClick() {
        if (!appConfigData.value.id) {
          createMessage.warning('Vui lòng liên kết ứng dụng Feishu trước!');
          return;
        }
        Modal.confirm({
          title: 'Hủy liên kết',
          content: 'Có muốn gỡ liên kết cấu hình ứng dụng Feishu của đơn vị thuê hiện tại không?',
          okText: 'Xác nhận',
          cancelText: 'Hủy',
          onOk: () => {
            deleteThirdAppConfig({ id: appConfigData.value.id }, handleSuccess);
          },
        });
      }

      onMounted(() => {
        let tenantId = getTenantId();
        initThirdAppConfigData({ tenantId: tenantId, thirdType: 'feishu' });
      });

      return {
        appConfigData,
        baseApiUrl,
        btnLoading,
        registerAppConfigModal,
        feishuEditClick,
        handleSuccess,
        syncFeishu,
        cancelBindClick,
      };
    },
  });
  //update-end---author:jeecg ---date:2026-05-13  for：【QQYUN-12767】Tích hợp Feishu
</script>

<style lang="less" scoped>
  .header {
    align-items: center;
    box-sizing: border-box;
    display: flex;
    height: 50px;
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

  .sync-tip-icon {
    margin-left: 4px;
    cursor: pointer;
    position: relative;
    top: 2px;
    color: #8c8c8c;
  }

  .base-collapse {
    margin-top: 20px;
    padding: 0 24px;
    font-size: 20px;

    .base-desc {
      color: #a1a1a1;
      font-size: 14px;

      code {
        color: #1677ff;
        background: #f0f5ff;
        padding: 0 4px;
        border-radius: 2px;
        font-size: 12px;
      }
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
  [data-theme='dark'] .base-collapse .ant-collapse {
    border: none !important;
  }
  /*end Tương thích chế độ tối*/
  :deep(.ant-collapse-borderless > .ant-collapse-item:last-child) {
    border-bottom-width: 1px;
  }
</style>
