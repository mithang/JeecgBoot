<template>
  <PageWrapper title="TreeVí dụ thao tác hàm" contentBackground contentClass="p-4">
    <div class="mb-4">
      <a-button @click="expandAll(true)" class="mr-2"> Mở rộng tất cả </a-button>
      <a-button @click="expandAll(false)" class="mr-2"> Thu gọn tất cả </a-button>
      <a-button @click="checkAll(true)" class="mr-2"> Chọn tất cả </a-button>
      <a-button @click="checkAll(false)" class="mr-2"> Bỏ chọn tất cả </a-button>
      <a-button @click="handleLevel(2)" class="mr-2"> Hiển thị đến cấp 2 </a-button>
      <a-button @click="handleLevel(1)" class="mr-2"> Hiển thị đến cấp 1 </a-button>
    </div>
    <div class="mb-4">
      <a-button @click="handleSetCheckData" class="mr-2"> Đặt dữ liệu đã chọn </a-button>
      <a-button @click="handleGetCheckData" class="mr-2"> Lấy dữ liệu đã chọn </a-button>
      <a-button @click="handleSetSelectData" class="mr-2"> Đặt dữ liệu đã chọn </a-button>
      <a-button @click="handleGetSelectData" class="mr-2"> Lấy dữ liệu đã chọn </a-button>

      <a-button @click="handleSetExpandData" class="mr-2"> Đặt dữ liệu mở rộng </a-button>
      <a-button @click="handleGetExpandData" class="mr-2"> Lấy dữ liệu đã mở rộng </a-button>
    </div>
    <div class="mb-4">
      <a-button @click="appendNodeByKey(null)" class="mr-2"> Thêm nút gốc </a-button>
      <a-button @click="appendNodeByKey('2-2')" class="mr-2"> Thêm nút bên trong parent3 </a-button>
      <a-button @click="deleteNodeByKey('2-2')" class="mr-2"> Xóa nút parent3 </a-button>
      <a-button @click="updateNodeByKey('1-1')" class="mr-2"> Cập nhật nút parent2 </a-button>
    </div>
    <BasicTree :treeData="treeData" title="Thao tác hàm" ref="treeRef" :checkable="true" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, ref, unref } from 'vue';
  import { BasicTree, TreeActionType } from '/@/components/Tree/index';
  import { treeData } from './data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { BasicTree, PageWrapper },
    setup() {
      const treeRef = ref<Nullable<TreeActionType>>(null);
      const { createMessage } = useMessage();

      function getTree() {
        const tree = unref(treeRef);
        if (!tree) {
          throw new Error('tree is null!');
        }
        return tree;
      }

      function handleLevel(level: number) {
        getTree().filterByLevel(level);
      }

      function handleSetCheckData() {
        getTree().setCheckedKeys(['0-0']);
      }

      function handleGetCheckData() {
        const keys = getTree().getCheckedKeys();
        createMessage.success(JSON.stringify(keys));
      }

      function handleSetSelectData() {
        getTree().setSelectedKeys(['0-0']);
      }

      function handleGetSelectData() {
        const keys = getTree().getSelectedKeys();
        createMessage.success(JSON.stringify(keys));
      }

      function handleSetExpandData() {
        getTree().setExpandedKeys(['0-0']);
      }

      function handleGetExpandData() {
        const keys = getTree().getExpandedKeys();
        createMessage.success(JSON.stringify(keys));
      }

      function checkAll(checkAll: boolean) {
        getTree().checkAll(checkAll);
      }

      function expandAll(checkAll: boolean) {
        getTree().expandAll(checkAll);
      }

      function appendNodeByKey(parentKey: string | null = null) {
        getTree().insertNodeByKey({
          parentKey: parentKey,
          node: {
            title: 'Thêm mới nút',
            key: '2-2-2',
          },
          // Chèn về sau
          push: 'push',
          // Chèn lên trước
          // push:'unshift'
        });
      }

      function deleteNodeByKey(key: string) {
        getTree().deleteNodeByKey(key);
      }

      function updateNodeByKey(key: string) {
        getTree().updateNodeByKey(key, {
          title: 'parent2-new',
        });
      }

      return {
        treeData,
        treeRef,
        handleLevel,
        handleSetCheckData,
        handleGetCheckData,
        handleSetSelectData,
        handleGetSelectData,
        handleSetExpandData,
        handleGetExpandData,
        appendNodeByKey,
        deleteNodeByKey,
        updateNodeByKey,
        checkAll,
        expandAll,
      };
    },
  });
</script>
