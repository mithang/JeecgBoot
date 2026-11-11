<template>
  <PageWrapper title="TreeVí dụ thao tác hàm">
    <div class="flex">
      <BasicTree
        class="w-1/3"
        title="Nút thao tác bên phải/Biểu tượng tùy chỉnh"
        helpMessage="Thông tin trợ giúp"
        :treeData="treeData"
        :actionList="actionList"
        :renderIcon="createIcon"
      />
      <BasicTree class="w-1/3 mx-4" title="Menu chuột phải" :treeData="treeData" :beforeRightClick="getRightMenuList" />
      <BasicTree class="w-1/3" title="Sử dụng thanh công cụ" toolbar checkable search :treeData="treeData" :beforeRightClick="getRightMenuList" />
    </div>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { BasicTree, ActionItem, ContextMenuItem } from '/@/components/Tree/index';
  import { treeData } from './data';
  import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue';
  import { PageWrapper } from '/@/components/Page';

  export default defineComponent({
    components: { BasicTree, PageWrapper },
    setup() {
      function handlePlus(node: any) {
        console.log(node);
      }

      function getRightMenuList(node: any): ContextMenuItem[] {
        return [
          {
            label: 'Thêm mới',
            handler: () => {
              console.log('Đã nhấp Thêm mới', node);
            },
            icon: 'bi:plus',
          },
          {
            label: 'Xóa',
            handler: () => {
              console.log('Đã nhấp Xóa', node);
            },
            icon: 'bx:bxs-folder-open',
          },
        ];
      }
      const actionList: ActionItem[] = [
        {
          // show:()=>boolean;
          render: (node) => {
            return h(PlusOutlined, {
              class: 'ml-2',
              onClick: () => {
                handlePlus(node);
              },
            });
          },
        },
        {
          render: () => {
            return h(DeleteOutlined);
          },
        },
      ];

      function createIcon({ level }) {
        if (level === 1) {
          return 'ion:git-compare-outline';
        }
        if (level === 2) {
          return 'ion:home';
        }
        if (level === 3) {
          return 'ion:airplane';
        }
        return '';
      }
      return { treeData, actionList, getRightMenuList, createIcon };
    },
  });
</script>
