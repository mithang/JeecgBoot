<!--Trang liên kết WeCom của hộp thoại-->
<template>
  <BasicModal @register="registerModal" :width="800" :title="title" destroyOnClose>
    <a-spin :spinning="loading">
      <div class="we-bind">
        <a-row :span="24" class="we-title-background">
          <a-col :span="12" class="border-right">
            <span>Người dùng tổ chức</span>
          </a-col>
          <a-col :span="12" class="padding-left">
            <span>Người dùng WeCom</span>
          </a-col>
        </a-row>
        <a-row :span="24">
          <template v-for="(item, index) in bindData.jwUserDepartVos">
            <a-col :span="12" class="border-right padding-left border-bottom">
              <div class="we-account">
                <a-avatar v-if="item.avatar" :src="getFileAccessHttpUrl(item.avatar)" :size="28"></a-avatar>
                <a-avatar v-else :size="28">
                  {{ item.realName.length > 2 ? item.realName.substr(0, 2) : item.realName }}
                </a-avatar>
                <a-input style="margin-left: 20px" :value="item.realName" readonly />
              </div>
            </a-col>
            <a-col :span="12" class="padding-left border-bottom">
              <div class="we-account">
                <span v-if="item.wechatUserId || izBind" class="we-remove"
                  >{{ item.wechatRealName }} <span style="margin-right: 20px" @click="handleRemoveClick(index, item)">Loại bỏ</span></span
                >
                <a-select
                  v-else
                  v-model:value="item.wechatUserId"
                  :options="userList"
                  :fieldNames="{ label: 'wechatRealName', value: 'wechatUserId' }"
                  style="width: 200px"
                  showSearch
                  @select="(val, option) => handleSelect(val, option, index)"
                />
              </div>
            </a-col>
          </template>
        </a-row>
      </div>
    </a-spin>
    <template #footer>
      <a-button v-if="!izBind" type="primary" @click="handleSubmit">Đồng bộ</a-button>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, h, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getThirdUserByWechat, wechatEnterpriseToLocal, getThirdUserBindByWechat, deleteThirdAccount } from './ThirdApp.api';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { useMessage } from '@/hooks/web/useMessage';
  import { Modal } from 'ant-design-vue';
  import { useUserStore } from '@/store/modules/user';

  export default defineComponent({
    name: 'ThirdAppBindWeEnterpriseModal',
    components: { BasicModal },
    setup(props, { emit }) {
      const title = ref<string>('Liên kết WeCom');
      //Dữ liệu liên kết của WeCom
      const bindData = ref<any>({});
      const loading = ref<boolean>(false);
      const btnLoading = ref<boolean>(false);
      const { createMessage } = useMessage();
      const userList = ref<any>([]);
      //Hiển thị thông tin văn bản đồng bộ
      const syncText = ref<string>('');
      //Dữ liệu đã liên kết hay chưa thì hiển thị danh sách khác nhau
      const izBind = ref<boolean>(false);
      const userStore = useUserStore();
      //Gán giá trị cho biểu mẫu
      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        loading.value = true;
        console.log('izBind：：', izBind);
        if (!data.izBind) {
          await getUnboundData();
        } else {
          await getBoundData();
        }
        izBind.value = data.izBind;
      });

      /**
       * Dữ liệu chưa liên kết
       */
      async function getUnboundData() {
        await getThirdUserByWechat().then((res) => {
          if (res.success) {
            let userLists = res.result.userList;
            bindData.value = res.result;
            userList.value = res.result.userList;
            /*   if (userLists && userLists.length > 0) {
            syncText.value = "";
          } else {
            syncText.value = "Người dùng WeCom đều đã đồng bộ";
          }*/
            loading.value = false;
          } else {
            createMessage.warning(res.message);
            loading.value = false;
          }
        });
      }

      /**
       * Dữ liệu đã liên kết
       */
      async function getBoundData() {
        await getThirdUserBindByWechat().then((res) => {
          if (res.success) {
            bindData.value.jwUserDepartVos = res.result;
            loading.value = false;
          } else {
            createMessage.warn(res.message);
            loading.value = false;
          }
        });
      }

      /**
       * Sự kiện nhấp vào cấu hình bên thứ ba
       */
      async function handleSubmit() {
        btnLoading.value = true;
        let userList = bindData.value.userList;
        //Đóng gói lại dữ liệu, chỉ giữ ID người dùng và ID WeCom là đủ,Còn cần truyền người dùng chưa liên kết cho hậu trường
        let params: any = [];
        //Truy vấn người dùng WeCom mà người dùng đã liên kết
        for (const item of bindData.value.jwUserDepartVos) {
          if (item.wechatUserId) {
            userList = userList.filter((a) => a.wechatUserId != item.wechatUserId);
            params.push({
              wechatUserId: item.wechatUserId,
              wechatDepartId: item.wechatDepartId,
              wechatRealName: item.wechatRealName,
              userId: item.userId,
            });
          }
        }
        let text: string = '';
        //Truy vấn đơn vị thuê chưa được liên kết
        if (userList && userList.length > 0) {
          for (const item of userList) {
            params.push({ wechatUserId: item.wechatUserId, wechatDepartId: item.wechatDepartId, wechatRealName: item.wechatRealName });
          }
          text = 'Phát hiện người dùng WeCom chưa liên kết ' + userList.length + ' , nền tảng sẽ cho người dùng này ' + userList.length + ' người dùng tạo tài khoản mới';
        }

        Modal.confirm({
          title: 'Xác nhận đồng bộ',
          content: text,
          okText: 'Xác nhận',
          onOk: () => {
            let json = JSON.stringify(params);
            console.log('json：：', json);
            wechatEnterpriseToLocal({ jwUserDepartJson: json })
              .then((res) => {
                let options = {};
                if (res.success) {
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
                  closeModal();
                  emit('success', options, res);
                }
              })
              .finally(() => {
                btnLoading.value = false;
              });
          },
        });
      }

      /**
       * Sự kiện chọn hộp thả xuống
       */
      function handleSelect(val, option, index) {
        bindData.value.jwUserDepartVos[index].wechatUserId = option.wechatUserId;
        bindData.value.jwUserDepartVos[index].wechatRealName = option.wechatRealName;
        bindData.value.jwUserDepartVos[index].wechatDepartId = option.wechatDepartId;
        userList.value = userList.value.filter((item) => item.wechatUserId != option.wechatUserId);
      }

      /**
       * Sự kiện loại bỏ
       * @param index
       * @param item
       */
      function handleRemoveClick(index, item) {
        if (!izBind.value) {
          userList.value.push({
            wechatUserId: item.wechatUserId,
            wechatRealName: item.wechatRealName,
            wechatDepartId: item.wechatDepartId,
          });
          bindData.value.jwUserDepartVos[index].wechatUserId = '';
          bindData.value.jwUserDepartVos[index].wechatRealName = '';
          bindData.value.jwUserDepartVos[index].wechatDepartId = '';
        } else {
          Modal.confirm({
            title: 'Xác nhận hủy liên kết không',
            okText: 'Xác nhận',
            onOk: async () => {
              await deleteThirdAccount({ id: item.thirdId, sysUserId: userStore.getUserInfo.id }).then((res) => {
                if (res.success) {
                  createMessage.success('Hủy liên kết thành công!');
                  getBoundData();
                } else {
                  createMessage.warning(res.message);
                }
              });
            },
          });
        }
      }

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

      return {
        title,
        registerModal,
        handleSubmit,
        bindData,
        getFileAccessHttpUrl,
        loading,
        userList,
        handleSelect,
        handleRemoveClick,
        btnLoading,
        izBind,
      };
    },
  });
</script>

<style lang="less" scoped>
  .we-bind {
    max-height: calc(100vh - 260px);
    overflow-y: auto;
    border: 1px @border-color-base solid;
    border-bottom: none;
    .we-title-background {
      background: @component-background;
      height: 40px;
      line-height: 40px;
      padding: 0 10px;
    }
    .we-account {
      display: flex;
      height: 40px;
      line-height: 40px;
      align-items: center;
    }

    :deep(.ant-input) {
      border: none;
      padding: 0;
      box-shadow: none;
    }

    .we-remove {
      display: flex;
      justify-content: space-between;
      width: 100%;
      cursor: pointer;
    }
    .border-right {
      border-right: 1px @border-color-base solid;
    }
    .border-bottom {
      border-bottom: 1px @border-color-base solid;
    }
    .padding-left {
      padding-left: 10px;
    }
  }
</style>
