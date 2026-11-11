<!--Trang thêm kho tri thức-->
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
            <a-form-item name="name" label="Tên kho tri thức">
              <JInput v-model:value="queryParam.name" placeholder="Vui lòng nhập tên kho tri thức" />
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
        <a-card class="add-knowledge-card" @click="handleAddKnowled">
          <div class="flex">
            <Icon icon="ant-design:plus-outlined" class="add-knowledge-card-icon" size="20"></Icon>
            <span class="add-knowledge-card-title">Tạo kho tri thức</span>
          </div>
        </a-card>
      </a-col>
      <a-col v-if="knowledgeList && knowledgeList.length>0" :xxl="4" :xl="6" :lg="6" :md="6" :sm="12" :xs="24" v-for="item in knowledgeList">
        <a-card class="knowledge-card pointer" @click="handleDocClick(item)">
          <div class="knowledge-header">
            <div class="flex">
              <img class="header-img" src="./icon/knowledge.png" />
              <div class="header-text">
                <span class="header-text-top header-name ellipsis" :title="item.name"> {{ item.name }} </span>
                <span class="header-text-top"> Người tạo:{{ item.createBy_dictText || item.createBy }} </span>
              </div>
            </div>
          </div>
          <div class="mt-10 text-desc">
            <span>{{ item.descr || 'Tạm thời chưa có mô tả' }}</span>
          </div>
          <div class="knowledge-footer flex" style="justify-content: space-between">
            <div style="width: calc(100% - 60px)" class="ellipsis">
              <Icon class="knowledge-footer-icon" icon="ant-design:deployment-unit-outlined" size="14"></Icon>
              <span :title="item.embedId_dictText">{{ item.embedId_dictText }}</span>
            </div>
            <div>
              <a-tag v-if="item.type == 'memory'" color="orange" class="tag-style">Kho bộ nhớ</a-tag>
              <a-tag v-else color="blue" class="tag-style">Kho tri thức</a-tag>
            </div>
          </div>
          <div class="knowledge-btn">
            <a-dropdown placement="bottomRight" :trigger="['click']" :getPopupContainer="(node) => node.parentNode">
              <div class="ant-dropdown-link pointer model-icon" @click.prevent.stop>
                <Icon icon="ant-design:ellipsis-outlined" size="16"></Icon>
              </div>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="vectorization" @click.prevent.stop="handleVectorization(item.id)">
                    <Icon icon="ant-design:retweet-outlined" size="16"></Icon>
                    Vector hóa
                  </a-menu-item>
                  <a-menu-item key="text" @click.prevent.stop="handleEditClick(item)">
                    <Icon class="pointer" icon="ant-design:edit-outlined" size="16"></Icon>
                    Chỉnh sửa
                  </a-menu-item>
                  <a-menu-item key="file" @click.prevent.stop="handleDelete(item)">
                    <Icon class="pointer" icon="ant-design:delete-outlined" size="16"></Icon>
                    Xóa
                  </a-menu-item>
                  <a-menu-item key="clear" @click.prevent.stop="onDeleteAllDoc(item)">
                    <Icon icon="ant-design:delete-outlined" size="16"></Icon>
                    Làm trống tài liệu
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
        </a-card>
      </a-col>
    </a-row>
    <Pagination
      v-if="knowledgeList.length > 0"
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
    <!--Popup thêm kho tri thức-->
    <KnowledgeBaseModal @register="registerModal" @success="reload"></KnowledgeBaseModal>
    <!-- Popup tài liệu kho tri thức -->
    <AiragKnowledgeDocListModal @register="docListRegister"></AiragKnowledgeDocListModal>
  </div>
</template>

<script lang="ts">
  import { reactive, ref } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { deleteModel, list, rebuild } from './AiKnowledgeBase.api';
  import { doDeleteAllDoc } from "./AiKnowledgeBase.api.util";
  import { Pagination } from 'ant-design-vue';
  import JInput from '@/components/Form/src/jeecg/components/JInput.vue';
  import KnowledgeBaseModal from './components/AiKnowledgeBaseModal.vue';
  import JSelectUser from '@/components/Form/src/jeecg/components/JSelectUser.vue';
  import JDictSelectTag from '@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import AiragKnowledgeDocListModal from './components/AiragKnowledgeDocListModal.vue';
  import Icon from '@/components/Icon';
  import { useMessage } from "@/hooks/web/useMessage";

  export default {
    name: 'KnowledgeBaseList',
    components: {
      Icon,
      AiragKnowledgeDocListModal,
      KnowledgeBaseModal,
      JDictSelectTag,
      JSelectUser,
      JInput,
      Pagination,
    },
    setup() {
      //Danh sách mô hình
      const knowledgeList = ref([]);

      //Đăng ký modal
      const [registerModal, { openModal }] = useModal();
      const [docListRegister, { openModal: openDocModal }] = useModal();

      //Số trang hiện tại
      const pageNo = ref<number>(1);
      //Số mục mỗi trang
      const pageSize = ref<number>(30);
      //Tổng số mục
      const total = ref<number>(0);
      //Số trang có thể chọn
      const pageSizeOptions = ref<any>(['10', '20', '30']);
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
      //ref của biểu mẫu khu vực truy vấn
      const formRef = ref();
      const { createMessage } = useMessage();

      //Khi khởi tạo trang thực thi truy vấn danh sách
      reload();

      /**
       * Thêm mới
       */
      async function handleAddKnowled() {
        openModal(true, {});
      }

      /**
       * Chỉnh sửa
       *
       * @param item
       */
      function handleEditClick(item) {
        console.log(item);
        openModal(true, {
          id: item.id,
          isUpdate: true,
        });
      }

      /**
       * Tải lại dữ liệu
       */
      function reload() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          column: 'createTime',
          order: 'desc'
        };
        Object.assign(params, queryParam);
      
        list(params).then((res) => {
          if (res.success) {
            knowledgeList.value = res.result.records;
            total.value = res.result.total;
          } else {
            knowledgeList.value = [];
            total.value = 0;
          }
        });
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
       * Xóa mô hình
       * @param item
       */
      async function handleDelete(item) {
        if(knowledgeList.value.length == 1 && pageNo.value > 1) {
          pageNo.value = pageNo.value - 1;
        }
        await deleteModel({ id: item.id, name: item.name }, reload);
      }

      /**
       * Làm trống tài liệu
       * @param item
       */
      async function onDeleteAllDoc(item) {
        pageNo.value = 1;
        return doDeleteAllDoc(item.id, reload);
      }

      /**
       * Truy vấn
       */
      function searchQuery() {
        pageNo.value = 1;
        reload();
      }

      /**
       * Đặt lại
       */
      function searchReset() {
        formRef.value.resetFields();
        queryParam.createBy = '';
        pageNo.value = 1;
        //Làm mới dữ liệu
        reload();
      }

      /**
       * Sự kiện nhấp cấu hình tham số
       *
       * @param id
       * @param type
       */
      //update-begin---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
      function handleDocClick(item) {
        openDocModal(true, { id: item.id, type: item.type, knowledgeMetadata: item.metadata });
      //update-end---wangshuai---date:20260414  for：【QQYUN-14932】Khi tạo kho tri thức, có thể tạo một chiến lược phân đoạn, tài liệu trong kho tri thức mặc định dùng chiến lược phân đoạn của kho tri thức------------
      }

      /**
       * Vector hóa kho tri thức
       * @param id
       */
      async function handleVectorization(id) {
        rebuild({ knowIds: id }).then((res) =>{
          if(res.success){
            createMessage.success("Thao tác thành công, bắt đầu xây dựng lại kho tri thức bất đồng bộ, vui lòng xem sau!");
            reload();
          }else{
            createMessage.warning("Vector hóa thất bại!");
          }
        }).catch(err=>{
          createMessage.warning("Vector hóa thất bại!");
        });
      }

      return {
        handleAddKnowled,
        handleEditClick,
        registerModal,
        knowledgeList,
        reload,
        pageNo,
        pageSize,
        pageSizeOptions,
        total,
        handlePageChange,
        handleDelete,
        onDeleteAllDoc,
        searchQuery,
        searchReset,
        queryParam,
        labelCol,
        wrapperCol,
        formRef,
        handleDocClick,
        docListRegister,
        handleVectorization,
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
      .knowledge-header {
        position: relative;
        font-size: 14px;
        height: 40px;
        .header-img {
          width: 34px;
          height: 34px;
          margin-right: 6px;
        }
        .header-text {
          width: calc(100% - 80px);
          overflow: hidden;
          position: relative;
          display: grid;
          .header-name {
            font-size: 14px !important;
            font-weight: bold;
            color: #354052 !important;
          }
          .header-text-top {
            height: 22px;
            font-size: 12px;
          }
        }
      }
    }

    .list-footer {
      flex-shrink: 0;
      margin-top: 12px;
      padding-top: 4px;
    }
  }

  .text-desc {
    width: 100%;
    font-weight: 400;
    display: inline-block;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    height: 40px;
    overflow: hidden;
    font-size: 12px;
    color: #676f83;
  }
  
  .knowledge-footer{
    font-size: 12px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-top: 16px;
    .knowledge-footer-icon{
      position: relative;
      top: 2px
    }
    span{
      margin-left: 2px;
    }
  }

  .flex {
    display: flex;
    align-items: center;
  }

  :deep(.ant-card .ant-card-body) {
    padding: 16px;
  }

  .mt-10 {
    margin-top: 10px;
  }

  .ml-4 {
    margin-left: 4px;
  }

  .knowledge-btn {
    position: absolute;
    right: 4px;
    top: 6px;
    height: auto;
    display: none;
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

  .add-knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
  }

  .knowledge-card {
    margin-right: 20px;
    margin-bottom: 20px;
    height: 152px;
    border-radius: 10px;
    background: #fcfcfd;
    border: 1px solid #f0f0f0;
    box-shadow: 0 2px 4px #e6e6e6;
    transition: all 0.3s ease;
  }
  .knowledge-card:hover {
    box-shadow: 0 6px 12px #d0d3d8;
    .knowledge-btn {
      display: block;
    }
  }
  .pointer {
    cursor: pointer;
  }
  .list-footer {
    text-align: right;
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
  
  .model-icon{
    background-color: unset;
    border: none;
    margin-right: 2px;
  }
  .model-icon:hover{
    color: #000000;
    background-color: #e9ecf2;
    border: none;
  }
  .ant-dropdown-link{
    font-size: 14px;
    height: 24px;
    padding: 0 7px;
    border-radius: 4px;
    align-content: center;
    text-align: center;
  }
  
  .ellipsis{
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .tag-style{
    margin-right: 5px;
    height: 20px;
    line-height: 18px;
  }
</style>
