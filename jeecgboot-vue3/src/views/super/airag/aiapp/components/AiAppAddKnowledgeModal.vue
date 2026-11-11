<template>
  <div>
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="600px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <div class="flex header">
        <a-input
          @pressEnter="loadKnowledgeData"
          class="header-search"
          size="small"
          v-model:value="searchText"
          placeholder="Vui lòng nhập tên kho tri thức, nhấn Enter để tìm kiếm"
        ></a-input>
      </div>
      <a-row :span="24">
        <a-col :span="12" v-for="item in appKnowledgeOption" @click="handleSelect(item)">
          <a-card :style="getCardStyle(item)" hoverable class="checkbox-card" :body-style="{ width: '100%' }">
            <div style="display: flex; width: 100%; justify-content: space-between">
              <div>
                <img class="checkbox-img" :src="knowledge" />
                <span class="checkbox-name">{{ item.name }}</span>
              </div>
              <a-checkbox v-if="multiple" v-model:checked="item.checked" @click.stop class="quantum-checker" @change="(e)=>handleChange(e,item)"> </a-checkbox>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <div v-if="knowledgeIds && knowledgeIds.length > 0" class="use-select">
        <template v-if="!multiple">
          Đã chọn <span class="ellipsis" style="max-width: 150px">{{knowledgeData.name}}</span>
        </template>
        <template v-else>
          Đã chọn {{ knowledgeIds.length }} Kho tri thức
        </template>
        <span style="margin-left: 8px; color: #3d79fb; cursor: pointer" @click="handleClearClick">Làm trống</span>
      </div>
      <Pagination
        v-if="appKnowledgeOption.length > 0"
        :current="pageNo"
        :page-size="pageSize"
        :page-size-options="pageSizeOptions"
        :total="total"
        :showQuickJumper="true"
        :showSizeChanger="true"
        @change="handlePageChange"
        class="list-footer"
        size="small"
      />
    </BasicModal>
  </div>
</template>

<script lang="ts">
  import { ref, unref } from 'vue';
  import BasicModal from '@/components/Modal/src/BasicModal.vue';
  import { useModal, useModalInner } from '@/components/Modal';
  import { Pagination } from 'ant-design-vue';
  import { list } from '@/views/super/airag/aiknowledge/AiKnowledgeBase.api';
  import knowledge from '/@/views/super/airag/aiknowledge/icon/knowledge.png';
  import { cloneDeep } from 'lodash-es';

  export default {
    name: 'AiAppAddKnowledgeModal',
    components: {
      Pagination,
      BasicModal,
    },
    emits: ['success', 'register'],
    props: {
      multiple:{ type: Boolean, default: true },
      type: { type: String, default: 'knowledge' }
    },
    setup(props, { emit }) {
      const title = ref<string>('Thêm kho tri thức liên kết');

      //Kho tri thức app
      const appKnowledgeOption = ref<any>([]);
      //Loại ứng dụng
      const knowledgeIds = ref<any>([]);
      //Dữ liệu ứng dụng
      const knowledgeData = ref<any>([]);
      //Số trang hiện tại
      const pageNo = ref<number>(1);
      //Số mục mỗi trang
      const pageSize = ref<number>(10);
      //Tổng số mục
      const total = ref<number>(0);
      //Tìm kiếm văn bản
      const searchText = ref<string>('');
      //Số trang có thể chọn
      const pageSizeOptions = ref<any>(['10', '20', '30']);
      //Đăng ký modal
      const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
        //update-begin---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        if (props.multiple) {
          knowledgeIds.value = data.knowledgeIds ? cloneDeep(data.knowledgeIds.split(',')) : [];
          knowledgeData.value = data.knowledgeDataList ? cloneDeep(data.knowledgeDataList) : [];
        } else {
          knowledgeIds.value = data.knowledgeIds ? cloneDeep(data.knowledgeIds) : '';
          knowledgeData.value = data.knowledgeData ? cloneDeep(data.knowledgeData) : {};
        }
        //update-end---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        setModalProps({ minHeight: 500, bodyStyle: { padding: '10px' } });
        loadKnowledgeData();
      });

      /**
       * Lưu
       */
      async function handleOk() {
        console.log("Giá trị kho tri thức xác nhận chọn",knowledgeData.value);
        //update-begin---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        if (props.multiple) {
          emit('success', knowledgeIds.value, knowledgeData.value);
        } else {
          emit('success', knowledgeIds.value, knowledgeData.value);
        }
        //update-end---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        handleCancel();
      }

      /**
       * Hủy
       */
      function handleCancel() {
        closeModal();
      }

      //Sự kiện chọn hộp kiểm
      function handleSelect(item){
        //update-begin---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        if(!props.multiple) {
          if (knowledgeIds.value === item.id) {
            knowledgeIds.value = "";
            knowledgeData.value = null;
            return;
          }
          knowledgeIds.value = item.id;
          knowledgeData.value = item;
        } else {
          let id = item.id;
          const target = appKnowledgeOption.value.find((item) => item.id === id);
          if (target) {
            target.checked = !target.checked;
          }
          //Lưu id của kho tri thức đã chọn
          if (!knowledgeIds.value || knowledgeIds.value.length == 0) {
            knowledgeIds.value.push(id);
            knowledgeData.value.push(item);
            console.log("Giá trị của hộp kiểm chọn hoặc bỏ chọn kho tri thức",knowledgeData.value);
            return;
          }
          let findIndex = knowledgeIds.value.findIndex((item) => item === id);
          if (findIndex === -1) {
            knowledgeIds.value.push(id);
            knowledgeData.value.push(item);
          } else {
            knowledgeIds.value.splice(findIndex, 1);
            knowledgeData.value.splice(findIndex, 1);
          }
          console.log("Giá trị của hộp kiểm chọn hoặc bỏ chọn kho tri thức",knowledgeData.value);
        }
        //update-end---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
      }

      /**
       * Tải kho tri thức
       */
      function loadKnowledgeData() {
        let params = {
          pageNo: pageNo.value,
          pageSize: pageSize.value,
          name: searchText.value?`*${searchText.value}*`:searchText.value,
          type: props.type,
          column: 'createTime',
          order: 'desc',
        };
        list(params).then((res) => {
          if (res.success) {
            if (props.multiple && knowledgeIds.value.length > 0) {
              for (const item of res.result.records) {
                if (knowledgeIds.value.includes(item.id)) {
                  item.checked = true;
                }
              }
              appKnowledgeOption.value = res.result.records;
            } else {
              appKnowledgeOption.value = res.result.records;
            }
            total.value = res.result.total;
          } else {
            appKnowledgeOption.value = [];
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
        loadKnowledgeData();
      }

      /**
       * Làm trống trạng thái chọn
       */
      function handleClearClick() {
        //update-begin---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
        if (!props.multiple) {
          knowledgeIds.value = "";
          knowledgeData.value = null;
        } else {
          knowledgeIds.value = [];
          knowledgeData.value = [];
          appKnowledgeOption.value.forEach((item) => {
            item.checked = false;
          });
        }
        //update-end---author:wangshuai---date:2025-12-25---for:Chọn kho tri thức hỗ trợ chọn một và chọn nhiều---
      }

      /**
       * Sự kiện chọn hộp kiểm
       *
       * @param e
       * @param item
       */
      function handleChange(e, item) {
        if (e.target.checked) {
          knowledgeIds.value.push(item.id);
          knowledgeData.value.push(item);
        } else {
          let findIndex = knowledgeIds.value.findIndex((val) => val === item.id);
          if (findIndex != -1) {
            knowledgeIds.value.splice(findIndex, 1);
            knowledgeData.value.splice(findIndex, 1);
          }
        }
      }

      /**
       * Lấy kiểu thẻ
       * 
       * @param item
       */
      function getCardStyle(item) {
        if (props.multiple) {
          return item.checked ? { border: '1px solid #3370ff' } : {};
        }
        return item.id === knowledgeIds.value ? { border: '1px solid #3370ff' } : {};
      }

      return {
        registerModal,
        title,
        handleOk,
        handleCancel,
        appKnowledgeOption,
        knowledgeIds,
        handleSelect,
        pageNo,
        pageSize,
        pageSizeOptions,
        total,
        handlePageChange,
        knowledge,
        searchText,
        loadKnowledgeData,
        handleClearClick,
        handleChange,
        getCardStyle,
        knowledgeData,
      };
    },
  };
</script>

<style scoped lang="less">
  .ellipsis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .header {
    color: #646a73;
    width: 100%;
    justify-content: space-between;
    margin-bottom: 10px;
    .header-search {
      width: 200px;
    }
  }
  .pointer {
    cursor: pointer;
  }
  .type-title {
    color: #1d2025;
    margin-bottom: 4px;
  }
  .type-desc {
    color: #8f959e;
    font-weight: 400;
  }
  .list-footer {
    position: absolute;
    bottom: 0;
    right: 10px;
  }
  .checkbox-card {
    margin-bottom: 10px;
    margin-right: 10px;
  }
  .checkbox-img {
    width: 30px;
    height: 30px;
  }
  .checkbox-name {
    margin-left: 4px;
  }
  .use-select {
    color: #646a73;
    position: absolute;
    bottom: 0;
    left: 20px;
  }
</style>
