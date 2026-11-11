<template>
  <BasicModal
    title="OnlineỦy quyền quyền hạn"
    :width="900"
    :maskClosable="false"
    defaultFullscreen
    wrapClassName="onl-auth-setter-modal"
    :okButtonProps="{ style: { display: 'none' } }"
    cancelText="Đóng"
    @cancel="closeModal"
    @register="registerModal"
    @open-change="hanldeOpenChange"
  >
    <a-spin wrapperClassName="authsetting-container" :spinning="loading">
      <a-row v-if="contentShow">
        <a-col :span="12">
          <a-tabs v-model:activeKey="authMode" @change="onAuthModeChange">
            <a-tab-pane tab="Ủy quyền vai trò" key="role" forceRender>
              <LeftRole ref="roleRef" @select="onSelectRole" />
            </a-tab-pane>
            <a-tab-pane tab="Ủy quyền phòng ban" key="depart" forceRender>
              <LeftDepart ref="departRef" @select="onSelectDepart" />
            </a-tab-pane>
            <a-tab-pane tab="Ủy quyền nhân viên" key="user" forceRender>
              <LeftUser ref="userRef" @select="onSelectUser" />
            </a-tab-pane>
          </a-tabs>
        </a-col>
        <a-col :span="1"></a-col>
        <a-col :span="11">
          <a-tabs v-model:activeKey="activeKey" @change="onAuthTypeChange">
            <a-tab-pane tab="Quyền trường" key="field" forceRender>
              <AuthFieldTree class="authFieldTree" ref="fieldRef" :cgformId="cgformId" />
            </a-tab-pane>
            <template v-if="hasDataAuth">
              <a-tab-pane tab="Quyền nút" key="button" forceRender>
                <AuthButtonTree class="authButtonTree" ref="buttonRef" :cgformId="cgformId" />
              </a-tab-pane>
              <a-tab-pane tab="Quyền dữ liệu" key="data" forceRender>
                <AuthDataTree class="authDataTree" ref="dataRef" :cgformId="cgformId" />
              </a-tab-pane>
            </template>
          </a-tabs>
        </a-col>
      </a-row>
    </a-spin>
  </BasicModal>
</template>

<script lang="ts">
  import LeftRole from './setter/LeftRole.vue';
  import LeftDepart from './setter/LeftDepart.vue';
  import LeftUser from './setter/LeftUser.vue';
  import AuthFieldTree from './setter/AuthFieldTree.vue';
  import AuthButtonTree from './setter/AuthButtonTree.vue';
  import AuthDataTree from './setter/AuthDataTree.vue';

  import { ref, computed, nextTick, defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';

  export default defineComponent({
    name: 'AuthSetterModal',
    components: {
      BasicModal,
      LeftRole,
      LeftDepart,
      LeftUser,
      AuthFieldTree,
      AuthButtonTree,
      AuthDataTree,
    },
    props: {
      // 1bảng đơn 2bảng chính 3Bảng phụ
      tableType: { type: Number, default: 1 },
    },
    setup(props) {
      const cgformId = ref('');
      const loading = ref(false);
      const activeKey = ref('field');
      const authMode = ref('role');
      const refs = {
        fieldRef: ref(),
        buttonRef: ref(),
        dataRef: ref(),
        roleRef: ref(),
        userRef: ref(),
        departRef: ref(),
      };
      const activeRole = ref('');
      const hasDataAuth = computed(() => props.tableType == 1 || props.tableType == 2);
      const contentShow = ref(true);
      // Gán giá trị biểu mẫu
      const [registerModal, { closeModal }] = useModalInner((data) => {
        activeKey.value = 'field';
        // update-begin--author:liaozhiyang---date:20240520---for：【TV360X-147】Thiết lập quyền đặt lại về vai tròtab
        authMode.value = 'role';
        // update-end--author:liaozhiyang---date:20240520---for：【TV360X-147】Thiết lập quyền đặt lại về vai tròtab
        // QQYUN-4285【onlineBiểu mẫu 】Quyền trường Sau khi tích chọn không lưu, đóng rồi mở lại Vẫn là trạng thái đã chọn
        cgformId.value = data.cgformId;
        reset();
      });

      function getActiveRef<T = any>(key = activeKey.value): T {
        return refs[key + 'Ref']?.value;
      }

      async function reset() {
        await nextTick();
        clearLeftCurrentTabSelect();
        getActiveRef().clear();
      }

      // Sự kiện chọn vai trò
      function onSelectRole(roleId) {
        activeRole.value = roleId;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // Sự kiện chọn phòng ban
      function onSelectDepart(departid) {
        activeRole.value = departid;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // Sự kiện chọn người dùng
      function onSelectUser(userId) {
        activeRole.value = userId;
        onAuthTypeChange(activeKey.value);
        clearLeftOtherTabSelect();
      }

      // Xóa lựa chọn phía bên trái
      function clearLeftOtherTabSelect() {
        if (authMode.value == 'role') {
          refs.departRef.value.clearSelected();
          refs.userRef.value.clearSelected();
        } else if (authMode.value == 'depart') {
          refs.roleRef.value.clearSelected();
          refs.userRef.value.clearSelected();
        } else if (authMode.value == 'user') {
          refs.departRef.value.clearSelected();
          refs.roleRef.value.clearSelected();
        }
      }

      function clearLeftCurrentTabSelect() {
        if (authMode.value == 'role') {
          refs.roleRef.value.clearSelected();
        } else if (authMode.value == 'depart') {
          refs.departRef.value.clearSelected();
        } else if (authMode.value == 'user') {
          refs.userRef.value.clearSelected();
        }
        getActiveRef().clearChecked();
        // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7543】Khi phía bên trái chưa chọn vai trò, phòng ban, nhân viên, phía bên phải hiển thị trống
        activeRole.value = '';
        // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7543】Khi phía bên trái chưa chọn vai trò, phòng ban, nhân viên, phía bên phải hiển thị trống
      }

      // Chuyển đổi loại ủy quyền ở phía bên phải sự kiện
      async function onAuthTypeChange(key) {
        // Chuyển đổi Phía bên phảitab Nếu thông tin vai trò đang chọn hiện tại cần được tảitabThông tin được chọn bên trong
        await nextTick();
        if (activeRole.value) {
          getActiveRef(key).loadChecked(activeRole.value, authMode.value);
        }
      }

      // Chuyển đổi cách thức ủy quyền ở phía bên trái sự kiện
      function onAuthModeChange() {
        clearLeftCurrentTabSelect();
      }
      // update-begin--author:liaozhiyang---date:20240523---for：【TV360X-239】Trang ủy quyền quyền hạn: phân trang vai trò khi mở lại popup không khởi tạo về trang đầu tiên
      const hanldeOpenChange = (open: boolean) => {
        contentShow.value = open;
      };
      // update-end--author:liaozhiyang---date:20240523---for：【TV360X-239】Trang ủy quyền quyền hạn: phân trang vai trò khi mở lại popup không khởi tạo về trang đầu tiên
      return {
        ...refs,
        cgformId,
        loading,
        activeKey,
        hasDataAuth,
        authMode,
        onAuthModeChange,
        onAuthTypeChange,
        closeModal,
        onSelectRole,
        onSelectDepart,
        onSelectUser,
        registerModal,
        hanldeOpenChange,
        contentShow,
      };
    },
  });
</script>

<style lang="less" scoped>
  // update-begin--author:liaozhiyang---date:20231226---for：【QQYUN-7540】Popup quyền vai trò, khi nội dung phía bên phải dài thì cuộn cục bộ
  .authsetting-container {
    height: 100%;
    :deep(.ant-spin-container) {
      height: 100%;
    }
    .ant-row,
    .ant-col {
      height: 100%;
    }
    .ant-tabs {
      height: 100%;
      :deep(.ant-tabs-content) {
        height: 100%;
      }
      .ant-tabs-tabpane {
        height: 100%;
        .authFieldTree, .authButtonTree, .authDataTree {
          height: 100%;
          display: flex;
          flex-direction: column;
          :deep(.ant-tree) {
            flex: 1;
            min-height: 0;
            overflow: auto;
          }
        }
      }
    }
  }
  // update-end--author:liaozhiyang---date:20231226---for：【QQYUN-7540】Popup quyền vai trò, khi nội dung phía bên phải dài thì cuộn cục bộ
</style>

<style lang="less">
  .onl-auth-setter-modal:not(.fullscreen-modal) {
    .onl-auth-user-query-item {
      flex: 0 0 50%;
      max-width: 50%;
    }

    .onl-auth-user-query-actions {
      flex: 0 0 100%;
      max-width: 100%;
    }

    .onl-auth-tree-btns {
      display: grid;
      grid-template-columns: repeat(4, max-content);
      gap: 8px;
      margin-left: 10px;

      button {
        margin: 0;
      }
    }
  }
</style>
