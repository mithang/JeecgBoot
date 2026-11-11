<template>
  <div>
    <a-empty v-if="disabled" description="Vui lòng chọn vai trò phía bên trái trước/Phòng ban/Người dùng" />
    <a-empty v-else-if="treeData.length === 0" description="Không có thông tin quyền" />
    <template v-else>
      <div class="onl-auth-tree-btns">
        <a-button @click="onRefresh" size="small" type="primary" preIcon="ant-design:redo" ghost>Làm mới</a-button>
        <a-button @click="onExpandAll" size="small" type="primary" ghost><DownCircleOutlined />Mở rộng</a-button>
        <a-button @click="onCloseAll" size="small" type="primary" ghost><UpCircleOutlined />Gập lại</a-button>
        <a-button @click="onSave" size="small" type="primary" preIcon="ant-design:save" ghost>Lưu</a-button>

        <!-- update-begin-author:taoyan date:2022-5-25 for: VUEN-1102 Ủy quyền trường Chọn tất cả không có -->
        <a-button @click="onSelectAll" size="small" type="primary" ghost><CheckOutlined />Chọn tất cả</a-button>
        <a-button @click="onClearSelected" size="small" type="primary" ghost><UndoOutlined />Đặt lại</a-button>
        <!-- update-end-author:taoyan date:2022-5-25 for: VUEN-1102 Ủy quyền trường Chọn tất cả không có -->
      </div>
      <a-tree
        checkable
        v-model:checkedKeys="checkedKeys"
        :expandedKeys="expandedKeys"
        :autoExpandParent="autoExpandParent"
        :treeData="treeData"
        @expand="onExpand"
      />
    </template>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, watch, computed, unref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { authFieldLoadTree, loadRoleAuthChecked, saveAuthField } from '../auth.api';
  import { DownCircleOutlined, HomeOutlined, UpCircleOutlined, CheckOutlined, UndoOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'AuthFieldTree',
    components: {
      DownCircleOutlined,
      HomeOutlined,
      UpCircleOutlined,
      UndoOutlined,
      CheckOutlined,
    },
    props: {
      cgformId: { type: String, required: true },
    },
    setup(props) {
      const { createMessage: $message } = useMessage();
      const roleId = ref('');
      const authType = ref(1);
      const autoExpandParent = ref(true);
      const expandedKeys = ref<string[]>([]);
      const checkedKeys = ref<string[]>([]);
      const allCode = ref<string[]>([]);
      const treeData = ref<Recordable[]>([]);
      const authMode = ref('');
      const disabled = computed(() => !roleId.value);
      watch(() => props.cgformId, loadTree, { immediate: true });

      async function loadTree() {
        if (!props.cgformId) return;
        let result = (await authFieldLoadTree(props.cgformId, authType.value)) as Recordable[];
        // 1. Căn cứ theocodeTìm nút cấp một
        let trees: Recordable[] = [];
        let codes: string[] = [];
        result.forEach((item) => {
          if (!codes.includes(item.code)) {
            codes.push(item.code);
            trees.push({ key: item.code, title: item.title });
          }
        });
        // 2.Duyệt hai lần, nối ghép dữ liệu dạng cây
        for (let node of trees) {
          let children: Recordable[] = [];
          for (let item of result) {
            if (node.key === item.code) {
              let temp = getTreeNodeTitle(item);
              children.push({ key: item.id, title: temp });
            }
          }
          node.children = children;
        }
        treeData.value = trees;
        expandedKeys.value = [...codes];
        allCode.value = codes;
      }

      function getTreeNodeTitle(item) {
        let str = '';
        if (item.page == 3) {
          str += 'Danh sách';
        } else if (item.page == 5) {
          str += 'Biểu mẫu';
        }
        if (item.control == 3) {
          str += 'Có thể chỉnh sửa';
        } else if (item.control == 5) {
          str += 'Hiển thị (có thể thấy)';
        }
        return str;
      }

      // Cho bên ngoài gọi, tải quyền đang chọn hiện tại
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
        let ids = checkedKeys.value.filter((i) => allCode.value.indexOf(i) < 0);
        await saveAuthField(roleId.value, props.cgformId, {
          authId: JSON.stringify(ids),
          authMode: authMode.value,
        });
        $message.success('Lưu thành công');
      }

      function onExpandAll() {
        expandedKeys.value = [...allCode.value];
      }

      function onCloseAll() {
        expandedKeys.value = [];
      }

      function onExpand($expandedKeys) {
        expandedKeys.value = $expandedKeys;
        autoExpandParent.value = false;
      }

      function clear() {
        roleId.value = '';
        checkedKeys.value = [];
      }

      // update-begin-author:taoyan date:2022-5-25 for: VUEN-1102 Ủy quyền trường Chọn tất cả không có
      // Hủy lựa chọn--Đặt lại
      function onClearSelected() {
        checkedKeys.value = [];
      }
      // Chọn tất cả
      function onSelectAll() {
        const selectFun = function (arr) {
          for (let node of arr) {
            checkedKeys.value.push(node.key);
            if (node.children && node.children.length > 0) {
              selectFun.call(null, node.children);
            }
          }
        };
        checkedKeys.value = [];
        selectFun.call(null, unref(treeData));
      }
      // update-end-author:taoyan date:2022-5-25 for: VUEN-1102 Ủy quyền trường Chọn tất cả không có

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
        clearChecked,
        onCloseAll,
        onExpandAll,
        onRefresh,
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
