<template>
  <BasicModal
    @register="register"
    :getContainer="getContainer"
    :canFullscreen="false"
    destroyOnClose
    title="Chọn nhân viên đồng ký"
    :width="600"
    wrapClassName="j-filtered-user-select-modal"
  >
    <!-- Mô tả nút -->
    <a-select 
      value="Ký duyệt chung theo vai trò hệ thống, vui lòng lọc người tham gia" 
      disabled 
      style="width: 100%" 
      class="node-label-select"
    >
      <a-select-option value="Ký duyệt chung theo vai trò hệ thống, vui lòng lọc người tham gia">
        Ký duyệt chung theo vai trò hệ thống, vui lòng lọc người tham gia
      </a-select-option>
    </a-select>

    <div class="modal-content">
      <!-- Hộp tìm kiếm -->
      <div :class="['search-wrapper', { 'search-expanded': searchInputStatus }]">
        <span v-show="!searchInputStatus" class="search-icon" @click="showSearchInput">
          <SearchOutlined />
        </span>
        <div v-show="searchInputStatus" class="search-input">
          <a-input 
            v-model:value="searchText" 
            placeholder="Vui lòng nhập tên người dùng, nhấn Enter để tìm kiếm"
            @pressEnter="onSearchUser"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
            <template #suffix>
              <CloseOutlined title="Thoát tìm kiếm" @click="clearSearch" />
            </template>
          </a-input>
        </div>
      </div>

      <!-- Danh sách người dùng -->
      <div class="user-list-wrapper">
        <user-list 
          :multi="multi" 
          :dataList="userDataList" 
          :selectedIdList="selectedIdList" 
          @selected="onSelectUser" 
          @unSelect="unSelectUser" 
        />
      </div>

      <!-- Nhãn người dùng đã chọn -->
      <div v-if="selectedUserList.length > 0" class="selected-users">
        <SelectedUserItem 
          v-for="item in selectedUserList" 
          :key="item.id" 
          :info="item" 
          @unSelect="unSelectUser" 
        />
      </div>
    </div>

    <template #footer>
      <div class="modal-footer">
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="pageNo"
            size="small"
            :total="totalRecord"
            :pageSize="PAGE_SIZE"
            show-quick-jumper
            @change="onPageChange"
          />
        </div>
        <a-button 
          type="primary" 
          :disabled="!hasSelectedUser" 
          @click="handleOk"
        >
          Xác nhận gửi
        </a-button>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, computed, ref, toRaw, PropType } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SearchOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import { Pagination } from 'ant-design-vue';
  import { defHttp } from '/@/utils/http/axios';
  import UserList from './UserList.vue';
  import SelectedUserItem from './SelectedUserItem.vue';

  // API dữ liệu người dùng
  interface UserInfo {
    id: string;
    username: string;
    realname: string;
    [key: string]: any;
  }

  // Interface tham số mở popup
  interface ModalData {
    usernames?: string[];
    list?: UserInfo[];
  }

  // Định nghĩa hằng số
  const PAGE_SIZE = 10;
  const API_URL = '/sys/user/selectUserList';

  export default defineComponent({
    name: 'FilteredUserSelectModal',
    components: {
      BasicModal,
      SearchOutlined,
      CloseOutlined,
      SelectedUserItem,
      UserList,
      APagination: Pagination,
    },
    props: {
      // Có chọn nhiều hay không
      multi: {
        type: Boolean,
        default: true,
      },
      // Hàm container
      getContainer: {
        type: Function as PropType<() => HTMLElement>,
        default: undefined,
      },
      // Danh sách tài khoản người dùng được truyền vào (dùng để lọc)
      usernames: {
        type: Array as PropType<string[]>,
        default: () => [],
      },
    },
    emits: ['selected', 'register'],
    setup(props, { emit }) {
      // ==================== Định nghĩa trạng thái ====================
      const selectedUserList = ref<UserInfo[]>([]);
      const actualUsernames = ref<string[]>([]);
      const searchInputStatus = ref(false);
      const searchText = ref('');
      const pageNo = ref(1);
      const totalRecord = ref(0);
      const userDataList = ref<UserInfo[]>([]);

      // ==================== Thuộc tính tính toán ====================
      const selectedIdList = computed(() => 
        selectedUserList.value.map((item) => item.id)
      );

      const hasSelectedUser = computed(() => 
        selectedUserList.value.length > 0
      );

      // ==================== Sự kiện popup ====================
      const [register] = useModalInner((data: ModalData) => {
        initUsernames(data);
        initSelectedUsers(data);
        resetSearchState();
        loadUserList();
      });

      /**
       * Khởi tạo danh sách tên người dùng
       */
      function initUsernames(data: ModalData) {
        if (data?.usernames?.length) {
          actualUsernames.value = [...data.usernames];
        } else if (props.usernames?.length) {
          actualUsernames.value = [...props.usernames];
        } else {
          actualUsernames.value = [];
        }
      }

      /**
       * Khởi tạo người dùng đã chọn
       */
      function initSelectedUsers(data: ModalData) {
        selectedUserList.value = data?.list?.length ? [...data.list] : [];
      }

      /**
       * Đặt lại trạng thái tìm kiếm
       */
      function resetSearchState() {
        searchText.value = '';
        searchInputStatus.value = false;
        pageNo.value = 1;
      }

      // ==================== Thao tác người dùng ====================
      /**
       * Xác nhận lựa chọn
       */
      function handleOk() {
        if (!hasSelectedUser.value) return;
        emit('selected', toRaw(selectedUserList.value));
      }

      /**
       * Người dùng đã chọn
       */
      function onSelectUser(info: UserInfo) {
        if (!props.multi) {
          selectedUserList.value = [{ ...info }];
          return;
        }
        
        if (!selectedIdList.value.includes(info.id)) {
          selectedUserList.value.push({ ...info });
        }
      }

      /**
       * Hủy chọn người dùng
       */
      function unSelectUser(id: string) {
        const index = selectedUserList.value.findIndex((item) => item.id === id);
        if (index >= 0) {
          selectedUserList.value.splice(index, 1);
        }
      }

      // ==================== Chức năng tìm kiếm ====================
      /**
       * Hiển thị ô tìm kiếm
       */
      function showSearchInput(e?: Event) {
        e?.preventDefault();
        e?.stopPropagation();
        searchInputStatus.value = true;
      }

      /**
       * Thực hiện tìm kiếm
       */
      function onSearchUser() {
        pageNo.value = 1;
        loadUserList();
      }

      /**
       * Xóa tìm kiếm
       */
      function clearSearch(e?: Event) {
        e?.preventDefault();
        e?.stopPropagation();
        searchText.value = '';
        searchInputStatus.value = false;
        pageNo.value = 1;
        loadUserList();
      }

      // ==================== Chức năng phân trang ====================
      /**
       * Xử lý chuyển trang
       */
      function onPageChange() {
        loadUserList();
      }

      // ==================== Tải dữ liệu ====================
      /**
       * Tải danh sách người dùng
       */
      async function loadUserList() {
        if (!actualUsernames.value?.length) {
          userDataList.value = [];
          totalRecord.value = 0;
          return;
        }

        try {
          const params: Record<string, any> = {
            pageNo: pageNo.value,
            pageSize: PAGE_SIZE,
            includeUsernameList: actualUsernames.value.join(','),
          };
          
          if (searchText.value.trim()) {
            params.keyword = searchText.value.trim();
          }

          const { success, result } = await defHttp.get(
            { url: API_URL, params }, 
            { isTransformResponse: false }
          );
          
          if (success && result) {
            userDataList.value = result.records || [];
            totalRecord.value = result.total || 0;
          } else {
            resetUserData();
          }
        } catch (error) {
          console.error('Tải danh sách người dùng thất bại:', error);
          resetUserData();
        }
      }

      /**
       * Đặt lại dữ liệu người dùng
       */
      function resetUserData() {
        userDataList.value = [];
        totalRecord.value = 0;
      }

      // ==================== Giá trị trả về ====================
      return {
        PAGE_SIZE,
        register,
        handleOk,
        searchText,
        searchInputStatus,
        showSearchInput,
        onSearchUser,
        clearSearch,
        pageNo,
        totalRecord,
        onPageChange,
        userDataList,
        selectedUserList,
        selectedIdList,
        hasSelectedUser,
        onSelectUser,
        unSelectUser,
      };
    },
  });
</script>

<style lang="less" scoped>
  .j-filtered-user-select-modal {
    // Nhãn giải thích nút
    .node-label-select {
      margin-bottom: 10px;
      
      :deep(.ant-select-selector) {
        color: #fff !important;
        background-color: #409eff !important;
        border-radius: 5px !important;
        cursor: not-allowed !important;
      }
      
      :deep(.ant-select-selection-item),
      :deep(.ant-select-arrow) {
        color: #fff !important;
      }
    }

    // Vùng nội dung popup
    .modal-content {
      position: relative;
      min-height: 350px;
    }

    // Vùng chứa ô tìm kiếm
    .search-wrapper {
      position: absolute;
      top: 14px;
      z-index: 1;
      
      &.search-expanded {
        width: 100%;
      }

      .search-icon {
        margin-left: 10px;
        color: #c0c0c0;
        cursor: pointer;
        transition: color 0.3s;
        
        &:hover {
          color: #0a8fe9;
        }
      }

      .search-input {
        width: 100%;
        
        :deep(.anticon) {
          color: #c0c0c0;
          cursor: pointer;
          transition: color 0.3s;
          
          &:hover {
            color: #0a8fe9;
          }
        }
      }
    }

    // Danh sách người dùng
    .user-list-wrapper {
      padding-top: 50px;
    }

    // Nhãn người dùng đã chọn
    .selected-users {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding-top: 15px;
      width: 100%;
      overflow-x: hidden;
    }

    // Container dưới cùng
    .modal-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
      gap: 16px;
      
      .pagination-wrapper {
        flex: 1;
        min-width: 0;
        
        :deep(.ant-pagination) {
          display: flex;
          justify-content: flex-start;
        }
      }
      
      .ant-btn {
        flex-shrink: 0;
      }
    }

    // Tối ưu hóa vùng chứa cuộn
    :deep(.scroll-container) {
      padding-bottom: 0 !important;
    }
  }
</style>
