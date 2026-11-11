<template>
  <div>
    <a-empty v-if="disabled" description="Vui lòng chọn vai trò phía bên trái trước/Phòng ban/Người dùng" />
    <a-empty v-else-if="treeData.length === 0" description="Không có thông tin quyền" />
    <template v-else>
      <div class="onl-auth-tree-btns">
        <a-button @click="onRefresh" size="small" type="primary" preIcon="ant-design:redo" ghost>Làm mới</a-button>
        <a-button @click="onSave" size="small" type="primary" preIcon="ant-design:save" ghost>Lưu</a-button>
        <a-button @click="onSelectAll" size="small" type="primary" ghost><CheckOutlined />Chọn tất cả</a-button>
        <a-button @click="onClearSelected" size="small" type="primary" ghost><UndoOutlined />Đặt lại</a-button>
      </div>
      <a-tree
        checkable
        v-model:checkedKeys="checkedKeys"
        :expandedKeys="expandedKeys"
        :autoExpandParent="autoExpandParent"
        :treeData="treeData"
        @expand="onExpand"
      >
      </a-tree>
    </template>
  </div>
</template>

<script lang="ts">
  import { computed, defineComponent, ref, watch } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { authDataLoadTree, loadRoleAuthChecked, saveAuthData } from '../auth.api';
  import { CheckOutlined, UndoOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'AuthDataTree',
    components: { CheckOutlined, UndoOutlined },
    props: {
      cgformId: { type: String, required: true },
    },
    setup(props) {
      const { createMessage: $message } = useMessage();
      const roleId = ref('');
      const authType = ref(3);
      const autoExpandParent = ref(true);
      const expandedKeys = ref<string[]>([]);
      const checkedKeys = ref<string[]>([]);
      const treeData = ref<Recordable[]>([]);
      const authMode = ref('');
      const disabled = computed(() => !roleId.value);
      watch(() => props.cgformId, loadTree, { immediate: true });

      async function loadTree() {
        if (!props.cgformId) return;
        let result = (await authDataLoadTree(props.cgformId)) as Recordable[];
        treeData.value = result.map((item) => ({ key: item.id, title: item.ruleName }));
      }

      async function loadChecked($roleId, $authMode) {
        roleId.value = $roleId;
        authMode.value = $authMode;
        checkedKeys.value = [];
        await loadTree();
        let result = (await loadRoleAuthChecked({
          roleId: $roleId,
          cgformId: props.cgformId,
          type: authType.value,
          authMode: $authMode,
        })) as Recordable[];
        checkedKeys.value = result.map((item) => item.authId);
      }

      function clearChecked() {
        roleId.value = '';
        // QQYUN-4284 【onlineBiểu mẫu 】Quản lý quyền Sau khi bật nút, hiển thị trong ủy quyền vai trò, khi tắt, mở lại quyền vai trò vẫn hiển thị, cần làm mới trang mới không hiển thị
        loadTree();
      }

      function onRefresh() {
        loadTree();
        loadChecked(roleId.value, authMode.value);
      }

      async function onSave() {
        await saveAuthData(roleId.value, props.cgformId, {
          authId: JSON.stringify(checkedKeys.value),
          authMode: authMode.value,
        });
        $message.success('Lưu thành công');
      }

      function onExpand($expandedKeys) {
        expandedKeys.value = $expandedKeys;
        autoExpandParent.value = false;
      }

      function clear() {
        roleId.value = '';
        checkedKeys.value = [];
      }

      function onClearSelected() {
        checkedKeys.value = [];
      }

      function onSelectAll() {
        checkedKeys.value = treeData.value.map((item) => item.key);
      }

      return {
        loadChecked,
        clear,
        expandedKeys,
        autoExpandParent,
        checkedKeys,
        treeData,
        disabled,
        onSave,
        onExpand,
        onRefresh,
        clearChecked,
        onClearSelected,
        onSelectAll,
      };
    },
  });
</script>

<style scoped lang="less">
  .onl-auth-tree-btns {
    margin-left: 10px;
  }

  .onl-auth-tree-btns button {
    margin: 0 5px 0 2px;
  }
</style>
