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
      />
    </template>
  </div>
</template>

<script lang="ts">
  import { defineComponent, ref, watch, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { authButtonFixedList } from '../auth.data';
  import { authButtonLoadTree, loadRoleAuthChecked, saveAuthButton } from '../auth.api';
  import { CheckOutlined, UndoOutlined } from '@ant-design/icons-vue';

  export default defineComponent({
    name: 'AuthButtonTree',
    components: { CheckOutlined, UndoOutlined },
    props: {
      cgformId: { type: String, required: true },
    },
    setup(props) {
      const { createMessage: $message, createSuccessModal } = useMessage();
      const roleId = ref('');
      const authType = ref(2);
      const autoExpandParent = ref(true);
      const expandedKeys = ref<string[]>([]);
      const checkedKeys = ref<string[]>([]);

      const treeData = ref<Recordable[]>([]);
      const authMode = ref('');
      const disabled = computed(() => !roleId.value);
      watch(() => props.cgformId, loadTree, { immediate: true });

      async function loadTree() {
        if (!props.cgformId) return;
        let result = (await authButtonLoadTree(props.cgformId, authType.value)) as Recordable[];
        //1.Duyệt lần đầu tiên Căn cứ theocodeThiết lập tên nút mặc định
        result.forEach((item) => {
          for (const btn of authButtonFixedList) {
            if (item.code == btn.code) {
              if (!item['title']) {
                item['title'] = btn.title;
              }
              break;
            }
          }
        });
        //2.Ghép dữ liệu dạng cây
        let trees: Recordable[] = [];
        for (let item of result) {
          let title = getTreeNodeTitle(item);
          trees.push({ key: item.id, title });
        }
        treeData.value = trees;
      }

      function onRefresh() {
        loadTree();
        loadChecked(roleId.value, authMode.value);
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

      async function onSave() {
        try {
          const {success, message, result} = await saveAuthButton(roleId.value, props.cgformId, {
            authId: JSON.stringify(checkedKeys.value),
            authMode: authMode.value,
          });
          if (success) {
            if (Array.isArray(result?.disabledNames)) {
              createSuccessModal({
                title: 'Lưu thành công',
                content: `Do các nút sau chưa được kích hoạt nên quyền chưa có hiệu lực.<br>${result.disabledNames.join('<br>')}`,
              });
            } else {
              $message.success('Lưu thành công');
            }
          } else {
            $message.error(message);
          }
        } catch (e) {
          $message.error('Lưu xuất hiện bất thường');
          console.error(e);
        }
      }

      function getTreeNodeTitle(item) {
        let str = item.title + '-';
        if (item.code && item.code.includes('form_sub')) {
          // 【TV360X-2711】Khi code bằng form_sub Khi ở phần đầu, chứng tỏ là bảng phụ
          str += 'Biểu mẫu hiển thị (bảng phụ)'
        } else if (item.code === 'form_confirm') {
          str += 'Biểu mẫu hiển thị';
        } else if (item.page == 3) {
          str += 'Danh sách hiển thị';
        } else if (item.page == 5) {
          str += 'Biểu mẫu hiển thị';
        }
        return str;
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
