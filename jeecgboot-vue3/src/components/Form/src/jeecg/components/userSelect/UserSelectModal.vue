<template>
  <BasicModal
    @register="register"
    :getContainer="getContainer"
    :canFullscreen="false"
    destroyOnClose
    title="Chọn người dùng"
    :width="800"
    wrapClassName="j-user-select-modal2"
  >
    <!-- Hộp dropdown phòng ban -->
    <a-select v-model:value="selectedDepart" style="width: 100%" class="depart-select" @change="onDepartChange">
      <a-select-option v-for="item in departOptions" :value="item.value">{{ item.label }}</a-select-option>
    </a-select>

    <div style="position: relative; min-height: 350px">
      <!-- Ô tìm kiếm người dùng -->
      <div :class="searchInputStatus ? 'my-search all-width' : 'my-search'">
        <span :class="searchInputStatus ? 'hidden' : ''" style="margin-left: 10px"
          ><SearchOutlined style="color: #c0c0c0" @click="showSearchInput"
        /></span>
        <div style="width: 100%" :class="searchInputStatus ? '' : 'hidden'">
          <a-input v-model:value="searchText" @pressEnter="onSearchUser" style="width: 100%" placeholder="Vui lòng nhập tên người dùng, nhấn Enter để tìm kiếm">
            <template #prefix>
              <SearchOutlined style="color: #c0c0c0" />
            </template>
            <template #suffix>
              <CloseOutlined title="Thoát tìm kiếm" @click="clearSearch" />
            </template>
          </a-input>
        </div>
      </div>

      <!-- tabs -->
      <div class="my-tabs">
        <a-tabs v-model:activeKey="myActiveKey" :centered="true" @change="onChangeTab">
          <!-- Tất cả người dùng -->
          <a-tab-pane key="1" tab="Tất cả" forceRender>
            <user-list :multi="multi" :excludeUserIdList="excludeUserIdList" :dataList="userDataList" :selectedIdList="selectedIdList" depart @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>

          <!-- Người dùng phòng ban -->
          <a-tab-pane key="2" tab="Theo phòng ban" forceRender>
            <depart-user-list
              :multi="multi"
              :searchText="searchText"
              :selectedIdList="selectedIdList"
              :excludeUserIdList="excludeUserIdList"
              @loaded="initDepartOptions"
              @selected="onSelectUser"
              @unSelect="unSelectUser"
            />
          </a-tab-pane>

          <!-- Người dùng vai trò -->
          <a-tab-pane key="3" tab="Theo vai trò" forceRender>
            <role-user-list :multi="multi" :excludeUserIdList="excludeUserIdList" :searchText="searchText" :selectedIdList="selectedIdList" @selected="onSelectUser" @unSelect="unSelectUser" />
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- Người dùng đã chọn -->
      <div class="selected-users" style="width: 100%; overflow-x: hidden">
        <SelectedUserItem v-for="item in selectedUserList" :info="item" @unSelect="unSelectUser" />
      </div>
    </div>

    <template #footer>
      <div style="display: flex; justify-content: space-between; width: 100%">
        <div class="select-user-page-info">
          <a-pagination
            v-if="myActiveKey == '1'"
            v-model:current="pageNo"
            size="small"
            :total="totalRecord"
            show-quick-jumper
            @change="onPageChange"
          />
        </div>
        <a-button type="primary" @click="handleOk">Xác nhận</a-button>
      </div>
    </template>
  </BasicModal>
</template>

<script lang="ts">
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { SearchOutlined, CloseOutlined } from '@ant-design/icons-vue';
  import UserList from './UserList.vue';
  import SelectedUserItem from './SelectedUserItem.vue';
  import DepartUserList from './UserListAndDepart.vue';
  import RoleUserList from './UserListAndRole.vue';
  import { Pagination } from 'ant-design-vue';
  const APagination = Pagination;
  import { defHttp } from '/@/utils/http/axios';

  import {computed, ref, toRaw, unref} from 'vue';
  import { useUserStore } from '/@/store/modules/user';
  import { mySelfData } from './useUserSelect'
  
  export default {
    name: 'UserSelectModal',
    components: {
      BasicModal,
      SearchOutlined,
      CloseOutlined,
      SelectedUserItem,
      UserList,
      DepartUserList,
      RoleUserList,
      APagination,
    },
    props: {
      multi: {
        type: Boolean,
        default: false,
      },
      getContainer: {
        type: Function,
        default: null,
      },
      //Có loại trừ chính tôi không
      izExcludeMy: {
        type: Boolean,
        default: false,
      },
      //Có làm điều kiện trong tìm kiếm nâng cao hay không, có thể chọn biểu thức người dùng hiện tại
      inSuperQuery:{
        type: Boolean,
        default: false,
      }
    },
    emits: ['selected', 'register'],
    setup(props, { emit }) {
      const myActiveKey = ref('1');
      const selectedUserList = ref<any[]>([]);
      const userStore = useUserStore();
      const selectedIdList = computed(() => {
        let arr = selectedUserList.value;
        if (!arr || arr.length == 0) {
          return [];
        } else {
          return arr.map((k) => k.id);
        }
      });
      // QQYUN-4152[Ứng dụng] Người dùng đã tồn tại, khi thêm vẫn có thể chọn trùng lặp
      const excludeUserIdList = ref<any[]>([]);

      // Sự kiện popup
      const [register] = useModalInner((data) => {
        let list = data.list;
        if (list && list.length > 0) {
          selectedUserList.value = [...list];
        } else {
          selectedUserList.value = [];
        }
        if(data.excludeUserIdList){
          excludeUserIdList.value = data.excludeUserIdList;
        }else{
          excludeUserIdList.value = [];
        }
        //Nếu loại trừ chính mình, trực tiếpexcludeUserIdList.pushChỉ cần loại trừ
        if (props.izExcludeMy) {
          excludeUserIdList.value.push(userStore.getUserInfo.id);
        }
        resetFilterState();
        //Tải danh sách người dùng
        loadUserList();
      });

      // Sự kiện xác nhận
      function handleOk() {
        let arr = toRaw(selectedUserList.value);
        emit('selected', arr);
      }

      /*--------------Hộp thả xuống phòng ban, dùng để lọc người dùng---------------*/
      const selectedDepart = ref('');
      const departOptions = ref<any[]>([]);
      function initDepartOptions(options) {
        departOptions.value = [{ value: '', label: 'Tất cả người dùng' }, ...options];
        selectedDepart.value = '';
      }
      function onDepartChange() {
        pageNo.value = 1;
        loadUserList();
      }
      /*--------------Hộp thả xuống phòng ban, dùng để lọc người dùng---------------*/

      /*--------------Trang đầu tiên - Ô tìm kiếm---------------*/
      const searchInputStatus = ref(false);
      const searchText = ref('');

      /**
       * Xóa dữ liệu
       */
      function resetFilterState() {
        selectedDepart.value = '';
        searchText.value = '';
        searchInputStatus.value = false;
        pageNo.value = 1;
      }
      
      function showSearchInput(e) {
        e && prevent(e);
        searchInputStatus.value = true;
      }

      // Sự kiện Enter, kích hoạt tìm kiếm
      function onSearchUser() {
        pageNo.value = 1;
        loadUserList();
      }

      // Xóa lọc theo tên
      function clearSearch(e) {
        e && prevent(e);
        pageNo.value = 1;
        searchText.value = '';
        searchInputStatus.value = false;
        loadUserList();
      }
      /*--------------Trang đầu tiên - Ô tìm kiếm---------------*/

      /*--------------Tải dữ liệu---------------*/
      const pageNo = ref(1);
      const totalRecord = ref(0);
      const userDataList = ref<any[]>([]);
      async function onPageChange() {
        console.log('onPageChange', pageNo.value);
        await loadUserList();
      }
      async function loadUserList() {
        const url = '/sys/user/selectUserList';
        let params = {
          pageNo: pageNo.value,
          pageSize: 10,
        };
        if (searchText.value) {
          params['keyword'] = searchText.value;
        }
        if (selectedDepart.value) {
          params['departId'] = selectedDepart.value;
        }

        // Giải thích logic mã: 【QQYUN-8239]Vai trò người dùng, khi thêm người dùng trả về dữ liệu 2 trang, thực tế chỉ hiển thị một trang---
        if(unref(excludeUserIdList) && unref(excludeUserIdList).length>0){
          params['excludeUserIdList'] = excludeUserIdList.value.join(",");
        }
        
        const data = await defHttp.get({ url, params }, { isTransformResponse: false });
        if (data.success) {
          let { records, total } = data.result;
          totalRecord.value = total;
          initCurrentUserData(records);
          userDataList.value = records;
        } else {
          console.error(data.message);
        }
        console.log('loadUserList', data);
      }
      
      // Thêm một tùy chọn Người dùng hiện tại vào danh sách người dùng
      function initCurrentUserData(records) {
        if(pageNo.value==1 && props.inSuperQuery === true){
          records.unshift({...mySelfData})
        }
      }
      /*--------------Tải dữ liệu---------------*/

      /*--------------Đã chọn/Bỏ chọn---------------*/
      function onSelectUser(info) {
        if (props.multi === true) {
          let arr = selectedUserList.value;
          let idList = selectedIdList.value;
          if (idList.indexOf(info.id) < 0) {
            arr.push({ ...info });
            selectedUserList.value = arr;
          }
        } else {
          selectedUserList.value = [{ ...info }];
        }
      }
      function unSelectUser(id) {
        let arr = selectedUserList.value;
        let index = -1;
        for (let i = 0; i < arr.length; i++) {
          if (arr[i].id === id) {
            index = i;
            break;
          }
        }
        if (index >= 0) {
          arr.splice(index, 1);
          selectedUserList.value = arr;
        }
      }
      /*--------------Đã chọn/Bỏ chọn---------------*/

      function onChangeTab(tab) {
        myActiveKey.value = tab;
      }

      function prevent(e) {
        e.preventDefault();
        e.stopPropagation();
      }

      //Tải dữ liệu trang đầu tiên
      loadUserList();

      return {
        selectedDepart,
        departOptions,
        initDepartOptions,
        onDepartChange,

        register,
        handleOk,

        searchText,
        searchInputStatus,
        showSearchInput,
        onSearchUser,
        clearSearch,

        myActiveKey,
        onChangeTab,

        pageNo,
        totalRecord,
        onPageChange,
        userDataList,
        selectedUserList,
        selectedIdList,
        onSelectUser,
        unSelectUser,
        excludeUserIdList
      };
    },
  };
</script>

<style lang="less">
  .j-user-select-modal2 {
    .depart-select {
      .ant-select-selector {
        color: #fff !important;
        background-color: #409eff !important;
        border-radius: 5px !important;
      }
      .ant-select-selection-item,
      .ant-select-arrow {
        color: #fff !important;
      }
    }
    .my-search {
      position: absolute;
      top: 14px;
      z-index: 1;
      &.all-width {
        width: 100%;
      }

      .anticon {
        cursor: pointer;
        &:hover {
          color: #0a8fe9 !important;
        }
      }
      .hidden {
        display: none;
      }
    }

    .my-tabs {
    }

    .selected-users {
      display: flex;
      flex-wrap: wrap;
      flex-direction: row;
      padding-top: 15px;
    }

    .scroll-container {
      padding-bottom: 0 !important;
    }
  }
</style>
