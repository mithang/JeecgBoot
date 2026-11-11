<!--Danh sách tài liệu kho tri thức-->
<template>
  <div class="knowledge">
    <!--Khu vực truy vấn-->
    <div class="jeecg-basic-table-form-container">
      <a-form
        ref="formRef"
        @keyup.enter.native="searchQuery"
        :model="queryParam"
        :label-col="labelCol"
        :wrapper-col="wrapperCol"
        style="background-color: #f7f8fc"
      >
        <a-row :gutter="24">
          <a-col :xl="7" :lg="7" :md="8" :sm="24">
            <a-form-item name="name" label="Tên ứng dụng">
              <JInput v-model:value="queryParam.name" placeholder="Vui lòng nhập tên ứng dụng" />
            </a-form-item>
          </a-col>
          <a-col :xl="7" :lg="7" :md="8" :sm="24">
            <a-form-item name="type" label="Loại ứng dụng">
              <j-dict-select-tag v-model:value="queryParam.type" dict-code="ai_app_type" placeholder="Vui lòng chọn loại ứng dụng" />
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left; overflow: hidden" class="table-page-search-submitButtons">
              <a-col :lg="6">
                <a-button type="primary" preIcon="ant-design:search-outlined" @click="searchQuery">Truy vấn</a-button>
                <a-button type="primary" preIcon="ant-design:reload-outlined" @click="searchReset" style="margin-left: 8px">Đặt lại</a-button>
              </a-col>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <a-row :span="24" class="knowledge-row">
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24">
        <a-card class="add-knowledge-card" @click="handleCreateApp">
          <div class="flex">
            <Icon icon="ant-design:plus-outlined" class="add-knowledge-card-icon" size="20"></Icon>
            <span class="add-knowledge-card-title">Tạo ứng dụng</span>
          </div>
        </a-card>
      </a-col>
      <a-col :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in knowledgeAppDataList">
        <a-card class="knowledge-card pointer" @click="handleEditClick(item)">
          <div class="flex" style="align-items: flex-start">
            <img class="header-img" :src="getImage(item.icon)" />
            <div class="header-text">
              <span class="header-text-top header-name ellipsis"> {{ item.name }} </span>
              <span class="header-text-top header-create ellipsis">
                <a-tag v-if="item.status === 'release'" color="green">Đã phát hành</a-tag>
                <a-tag v-if="item.status === 'disable'">Đã vô hiệu hóa</a-tag>
                <span>Người tạo:{{ item.createBy_dictText || item.createBy }}</span>
              </span>
            </div>
            <div class="header-tag">
              <a-tag color="#EBF1FF" style="margin-right: 0" v-if="item.type === 'chatSimple'">
                <span style="color: #3370ff">Tác tử thông minh</span>
              </a-tag>
              <a-tag color="#FDF6EC" style="margin-right: 0" v-if="item.type === 'chatFLow'">
                <span style="color: #e6a343">Sắp xếp nâng cao</span>
              </a-tag>
            </div>
          </div>
          <div class="card-description">
            <span>{{ item.descr || 'Tạm thời chưa có mô tả' }}</span>
          </div>
          <div class="card-footer">
            <a-tooltip title="Trình diễn">
              <div class="card-footer-icon" @click.prevent.stop="handleViewClick(item)">
                <Icon class="operation" icon="lucide:circle-play" size="16"></Icon>
              </div>
            </a-tooltip>
            <template v-if="item.status === 'release'">
              <a-tooltip title="Sao chép liên kết">
                <div class="card-footer-icon" @click.prevent.stop="handleCopyShareLink(item)">
                  <Icon class="operation" icon="lucide:link-2" size="16"></Icon>
                </div>
              </a-tooltip>
            </template>
            <template v-if="item.status !== 'release'">
              <a-tooltip title="Xóa">
                <div class="card-footer-icon delete-action" @click.prevent.stop="handleDeleteClick(item)">
                  <Icon icon="lucide:trash-2" class="operation" size="16"></Icon>
                </div>
              </a-tooltip>
            </template>
            <a-tooltip :title="item.status === 'release' ? 'Chia sẻ' : 'Thêm'">
              <a-dropdown class="card-footer-icon" placement="bottomLeft" :trigger="['click']">
                <div @click.prevent.stop>
                  <Icon
                    class="operation"
                    :icon="item.status === 'release' ? 'lucide:share-2' : 'lucide:ellipsis'"
                    size="16"
                  ></Icon>
                </div>
                <template #overlay>
                  <a-menu>
                    <template v-if="item.status === 'enable'">
                      <a-menu-item key="release" @click.prevent.stop="handleSendClick(item,'release')">
                        <Icon icon="lineicons:rocket-5" size="14"></Icon>
                        Phát hành
                      </a-menu-item>
                      <a-menu-divider/>
                    </template>
                    <template v-else-if="item.status === 'release'">
                      <a-menu-item key="copy-share" @click.prevent.stop="handleCopyShareLink(item)">
                        <Icon icon="ant-design:copy-outlined" size="14"></Icon>
                        Sao chép liên kết
                      </a-menu-item>
                      <a-menu-item key="un-release" @click.prevent.stop="handleSendClick(item,'un-release')">
                        <Icon icon="tabler:rocket-off" size="14"></Icon>
                        Hủy phát hành
                      </a-menu-item>
                      <a-menu-divider/>
                    </template>
                    <a-menu-item key="copy" @click.prevent.stop="handleCopyClick(item)">
                      <Icon icon="ant-design:copy-outlined" size="16"></Icon> Sao chép
                    </a-menu-item>
                    <a-menu-item v-if="item.status === 'release'" key="web" @click.prevent.stop="handleSendClick(item,'web')">
                      <Icon icon="ant-design:dribbble-outlined" size="16"></Icon>
                      Nhúng website
                    </a-menu-item>
                    <a-menu-item v-if="isShowMenu" key="menu" @click.prevent.stop="handleSendClick(item,'menu')">
                      <Icon icon="ant-design:menu-outlined" size="16"></Icon> Cấu hình menu
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-tooltip>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <Pagination
      v-if="knowledgeAppDataList.length > 0"
      :current="pageNo"
      :page-size="pageSize"
      :page-size-options="pageSizeOptions"
      :total="total"
      :showQuickJumper="true"
      :showSizeChanger="true"
      @change="handlePageChange"
      class="list-footer"
      size="small"
      :show-total="() => `Tổng cộng${total}Mục` "
    />
    <!-- Popup thêm mới Ai  -->
    <AiAppModal @register="registerModal" @success="handleSuccess"></AiAppModal>
    <!-- Popup cài đặt Ai -->
    <AiAppSettingModal @register="registerSettingModal" @success="reload"></AiAppSettingModal>
    <!-- Popup phát hành -->
    <AiAppSendModal @register="registerAiAppSendModal"/>
  </div>
</template>

<script lang="ts">
  import { ref, reactive, onMounted } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { LoadingOutlined } from '@ant-design/icons-vue';
  import { Avatar, Modal, Pagination } from 'ant-design-vue';
  import { getFileAccessHttpUrl } from '@/utils/common/compUtils';
  import defaultImg from './img/ailogo.png';
  import AiAppModal from './components/AiAppModal.vue';
  import AiAppSettingModal from './components/AiAppSettingModal.vue';
  import AiAppSendModal from './components/AiAppSendModal.vue';
  import Icon from '@/components/Icon';
  import { $electron } from "@/electron";
  import { appList, copyApp, deleteApp, releaseApp, queryById } from './AiApp.api';
  import { useMessage } from '@/hooks/web/useMessage';
  import { copyTextToClipboard } from '@/hooks/web/useCopyToClipboard';
  import JInput from '@/components/Form/src/jeecg/components/JInput.vue';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { useRouter } from "vue-router";

  export default {
    name: 'AiAppList',
    components: {
      JDictSelectTag,
      JInput,
      AiAppSendModal,
      Icon,
      Pagination,
      Avatar,
      LoadingOutlined,
      BasicModal,
      AiAppModal,
      AiAppSettingModal,
    },
    emits: ['success', 'register'],
    setup(props, { emit }) {
      /**
       * Tạo tập hợp của ứng dụng
       */
      const knowledgeAppDataList = ref<any>([]);
      //Số trang hiện tại
      const pageNo = ref<number>(1);
      //Số mục mỗi trang
      const pageSize = ref<number>(30);
      //Tổng số mục
      const total = ref<number>(0);
      //Số trang có thể chọn
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //Đăng ký modal
      const [registerModal, { openModal }] = useModal();
      const [registerSettingModal, { openModal: openAppModal }] = useModal();
      const [registerAiAppSendModal, { openModal: openAiAppSendModal }] = useModal();
      const { createMessage, createConfirmSync } = useMessage();
      //Tham số truy vấn
      const queryParam = reactive<any>({});
      //Độ rộng label của khu vực truy vấn
      const labelCol = reactive({
        xs: 24,
        sm: 4,
        xl: 6,
        xxl: 6,
      });
      //Độ rộng component khu vực truy vấn
      const wrapperCol = reactive({
        xs: 24,
        sm: 20,
      });
      //ref của biểu mẫu
      const formRef = ref();

      reload();

      /**
       * Tải dữ liệu
       */
      function reload() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          column: 'createTime',
          order: 'desc',
        };
        Object.assign(params, queryParam);
        appList(params).then((res) => {
          if (res.success) {
            knowledgeAppDataList.value = res.result.records;
            total.value = res.result.total;
          } else {
            knowledgeAppDataList.value = [];
            total.value = 0;
          }
        });
      }

      /**
       * Tạo ứng dụng
       */
      function handleCreateApp() {
        openModal(true, {});
      }

      /**
       * Sự kiện thay đổi phân trang
       * @param page
       * @param current
       */
      function handlePageChange(page, current) {
        pageNo.value = page;
        pageSize.value = current;
        reload();
      }

      /**
       * Thành công
       */
      function handleSuccess(id) {
        reload();
        //Mở popup chỉnh sửa
        openAppModal(true, {
          isUpdate: false,
          id: id,
        });
      }

      /**
       * Lấy hình ảnh
       * @param url
       */
      function getImage(url) {
        return url ? getFileAccessHttpUrl(url) : defaultImg;
      }

      /**
       * Chỉnh sửa
       * @param item
       */
      function handleEditClick(item) {
        console.log('item:::', item);
        openAppModal(true, {
          isUpdate: true,
          ...item,
        });
      }

      /**
       * Trình diễn
       */
      function handleViewClick(item: any) {
        let url = '/ai/app/chat/' + item.id;
        if (item.shareToken) {
          url += '?shareToken=' + item.shareToken;
        }

        // update-begin--author:sunjianlei---date:20250411---for：【QQYUN-9685】Xây dựng ứng dụng desktop electron
        if ($electron.isElectron()) {
          url = $electron.resolveRoutePath(url);
          window.open(url, '_blank', 'width=1200,height=800');
          return
        }
        // update-end----author:sunjianlei---date:20250411---for：【QQYUN-9685】Xây dựng ứng dụng desktop electron

        window.open(url, '_blank');
      }

      /**
       * Lấy liên kết quảng bá (chứa shareToken)
       */
      function getShareUrl(item: any) {
        const shareToken = item?.shareToken;
        // NOTE: giữ nguyên chuỗi tiếng Trung '发布成功'/'取消发布成功' — backend trả về đúng văn bản này làm shareToken khi lỗi, so sánh phải khớp văn bản backend (không dịch).
        if (!item?.id || !shareToken || shareToken === '发布成功' || shareToken === '取消发布成功') {
          return '';
        }
        return `${document.location.protocol}//${window.location.host}/ai/app/chat/${item.id}?shareToken=${shareToken}`;
      }

      /**
       * Làm mới token chia sẻ từ giao diện chi tiết
       */
      async function refreshShareToken(item: any) {
        const res: any = await queryById({ id: item.id });
        if (res && res.success && res.result?.shareToken) {
          item.shareToken = res.result.shareToken;
          return item.shareToken;
        }
        return '';
      }

      /**
       * Sao chép liên kết quảng bá
       */
      async function handleCopyShareLink(item: any) {
        let url = getShareUrl(item);
        if (!url && item.status === 'release') {
          await refreshShareToken(item);
          url = getShareUrl(item);
        }
        if (!url) {
          createMessage.warning('Liên kết chia sẻ không hợp lệ, vui lòng phát hành lại rồi sao chép');
          return;
        }
        if (copyTextToClipboard(url)) {
          createMessage.success('Liên kết đã sao chép');
        } else {
          createMessage.error('Sao chép thất bại');
        }
      }

      /**
       * Sao chép ứng dụng
       * @param item
       * @author scott
       * @since 2026-08-06 【LHZP-1512】AIsince 2026-08-06 Thêm chức năng sao chép cho ứng dụng AI
       */
      async function handleCopyClick(item: any) {
        await copyApp({ id: item.id });
        pageNo.value = 1;
        reload();
      }

      /**
       * Xóa
       */
      function handleDeleteClick(item) {
        if(knowledgeAppDataList.value.length == 1 && pageNo.value > 1) {
          pageNo.value = pageNo.value - 1;
        }
        deleteApp({ id: item.id, name: item.name }, reload);
      }

      /**
       * Sự kiện nhấp phát hành
       * @param item dữ liệu
       * @param type loại
       */
      function handleSendClick(item,type) {
        if (type === 'release' || type === 'un-release') {
          return onRelease(item);
        }

        openAiAppSendModal(true,{
          type: type,
          data: item
        })
      }

      async function onRelease(item) {
        const toRelease = item.status === 'enable';
        let flag = await createConfirmSync({
          title: toRelease ? 'Phát hành ứng dụng' : 'Hủy phát hành',
          content: toRelease
            ? 'Xác nhận phát hành? Sau khi phát hành sẽ tạo liên kết chia sẻ (khách truy cập không cần đăng nhập), và không cho phép sửa ứng dụng nữa.'
            : 'Xác nhận hủy phát hành? Sau khi hủy, liên kết chia sẻ sẽ mất hiệu lực.',
          okText: 'Xác định',
          cancelText: 'Hủy',
        });
        if (!flag) {
          return
        }
        doRelease(item, item.status === 'enable');
      }

      /**
       * Phát hành
       */
      async function doRelease(item, release: boolean) {
        const res: any = await releaseApp(item.id, release);
        if (res && res.success) {
          if (release) {
            item.status = 'release';
            // result phải là token 32 ký tự; nếu nhầm thành văn bản gợi ý thì tra lại chi tiết
            const token = res.result;
            if (token && token !== res.message && /^[a-fA-F0-9]{32}$/.test(token)) {
              item.shareToken = token;
            } else {
              await refreshShareToken(item);
            }
          } else {
            item.status = 'enable';
            item.shareToken = undefined;
          }
        }
      }

      /**
       * Đặt lại
       */
      function searchReset() {
        pageNo.value = 1;
        formRef.value.resetFields();
        queryParam.name = '';
        //Làm mới dữ liệu
        reload();
      }

      /**
       * Truy vấn
       */
      function searchQuery(){
        pageNo.value = 1; 
        //Làm mới dữ liệu
        reload();
      }

      const router = useRouter();
      //Có hiển thị tùy chọn cấu hình menu hay không
      const isShowMenu = ref<boolean>(false);
      onMounted((()=>{
        let fullPath = router.currentRoute.value.fullPath;
        console.log(fullPath)
        if(fullPath === '/myapps/ai/app'){
          isShowMenu.value = false;
        } else {
          isShowMenu.value = true;
        }
      }))

      return {
        handleCreateApp,
        knowledgeAppDataList,
        pageNo,
        pageSize,
        total,
        pageSizeOptions,
        handlePageChange,
        cardBodyStyle: { textAlign: 'left', width: '100%' },
        registerModal,
        handleSuccess,
        getImage,
        handleEditClick,
        handleViewClick,
        handleCopyShareLink,
        handleCopyClick,
        handleDeleteClick,
        registerSettingModal,
        reload,
        queryParam,
        labelCol,
        wrapperCol,
        handleSendClick,
        registerAiAppSendModal,
        searchReset,
        formRef,
        isShowMenu,
        searchQuery,
      };
    },
  };
</script>

<style scoped lang="less">
  .knowledge {
    height: calc(100vh - 115px);
    background: #f7f8fc;
    padding: 24px;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .jeecg-basic-table-form-container {
      flex-shrink: 0;
    }

    .knowledge-row {
      flex: 1;
      min-height: 0;
      margin-top: 20px;
      overflow-y: auto;
      align-content: flex-start;
      align-items: flex-start;
    }

    .list-footer {
      flex-shrink: 0;
      margin-top: 12px;
      padding-top: 4px;
    }
  }

  .add-knowledge-card {
    margin-bottom: 20px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    border-radius: 10px;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    cursor: pointer;
    height: 152px;
    width: calc(100% - 20px);
    .add-knowledge-card-icon {
      padding: 8px;
      color: #1f2329;
      background-color: #f5f6f7;
      margin-right: 12px;
    }
    .add-knowledge-card-title {
      font-size: 16px;
      color:#1f2329;
      font-weight: 400;
      align-self: center;
    }
  }

  .knowledge-card {
    border-radius: 10px;
    margin-right: 20px;
    margin-bottom: 20px;
    height: 152px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
    .header-img {
      width: 25px;
      height: 25px;
      border-radius: 0.5rem;
    }
    .header-text {
      margin-left: 5px;
      position: relative;
      font-size: 14px;
      display: grid;
      flex: 1;
      min-width: 0;
      .header-name {
        font-weight: bold;
        color: #354052;
      }
      .header-create {
        font-size: 12px;
        color: #646a73;
      }
    }
    .header-tag {
      flex-shrink: 0;
      margin-left: 4px;
    }
  }

  .add-knowledge-card,
  .knowledge-card {
    transition: box-shadow 0.3s ease;
  }

  .add-knowledge-card:hover,
  .knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .add-knowledge-doc {
    margin-top: 6px;
    color: #6f6f83;
    font-size: 13px;
    width: 100%;
    cursor: pointer;
    display: flex;
    span {
      margin-left: 4px;
      line-height: 28px;
    }
  }
  .add-knowledge-doc:hover {
    background: #c8ceda33;
  }
  .card-description {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    height: 4.5em;
    overflow: hidden;
    text-overflow: ellipsis;
    line-height: 1.5;
    margin-top: 10px;
    text-align: left;
    font-size: 12px;
    color: #676f83;
  }
  .card-footer {
    position: absolute;
    bottom: 8px;
    left: 0;
    min-height: 30px;
    padding: 0 16px;
    width: 100%;
    align-items: center;
    display: flex;
    gap: 14px;
  }

  .card-footer-icon {
    font-size: 14px;
    height: 28px;
    padding: 0;
    color: #475467;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    float: left;
    width: 28px;
  }

  .card-footer-icon:hover {
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }

  .delete-action {
    color: #475467;
  }

  .delete-action:hover {
    color: #ff4d4f;
    background-color: #fff1f0;
  }

  .operation {
    display: block;
  }
  .list-footer {
    text-align: right;
  }
  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }
  .ellipsis{
    overflow: hidden;
    text-wrap: nowrap;
    text-overflow: ellipsis;
  }
  .jeecg-basic-table-form-container {
    padding: 0;
    :deep(.ant-form) {
      background-color: transparent;
    }
    .table-page-search-submitButtons {
      display: block;
      margin-bottom: 24px;
      white-space: nowrap;
    }
  }
</style>
<style lang="less">
  .airag-knowledge-doc .scroll-container {
    padding: 0 !important;
  }
</style>
