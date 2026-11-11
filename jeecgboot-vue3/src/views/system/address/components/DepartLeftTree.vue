<template>
  <a-card :bordered="false" style="height: 100%;" :body-style="{ background: backgroundColor }" >
    <a-spin :spinning="loading">
      <a-input-search v-if="showSearch" placeholder="Tìm kiếm theo tên phòng ban…" style="margin-bottom: 10px" @search="onSearch" allowClear />
      <!--Cây tổ chức-->
      <template v-if="treeData.length > 0">
        <a-tree
          v-if="!treeReloading"
          :style="{ background: backgroundColor }"
          showLine
          :clickRowToExpand="false"
          :treeData="treeData"
          :selectedKeys="selectedKeys"
          :load-data="loadChildrenTreeData"
          v-model:expandedKeys="expandedKeys"
          @select="onSelect"
          style="overflow-y: auto;height: calc(100vh - 330px);"
        >
          <template #title="{ orgCategory, title, departNameAbbr }">
            <TreeIcon :orgCategory="orgCategory" :title="getTitle(title,departNameAbbr)"></TreeIcon>
          </template>
        </a-tree>
      </template>
      <a-empty v-else description="Không có dữ liệu" />
    </a-spin>
  </a-card>
</template>

<script lang="ts" setup>
  import { inject, nextTick, ref, unref } from 'vue';
  import { queryDepartTreeSync } from '../address.api';
  import { searchByKeywords } from '/@/views/system/departUser/depart.user.api';
  import { Popconfirm } from 'ant-design-vue';
  import TreeIcon from "@/components/Form/src/jeecg/components/TreeIcon/TreeIcon.vue";

  const prefixCls = inject('prefixCls');
  // Định nghĩaprops
  const props = defineProps({
    // Có hiển thị ô tìm kiếm không
    showSearch: {
      type: Boolean,
      default: true,
    },
    // Màu nền
    backgroundColor: {
      type: String,
      default: 'inherit',
    },
  });
  const emit = defineEmits(['select', 'rootTreeData']);

  const loading = ref<boolean>(false);
  // Dữ liệu danh sách cây phòng ban
  const treeData = ref<any[]>([]);
  // Mục đang mở rộng
  const expandedKeys = ref<any[]>([]);
  // Mục đang được chọn
  const selectedKeys = ref<any[]>([]);
  // Tải lại component cây
  const treeReloading = ref<boolean>(false);
  // Phòng ban đang chọn
  const currentDepart = ref<any>(null);
  // Từ khóa tìm kiếm
  const searchKeyword = ref('');

  // Tải thông tin phòng ban cấp cao nhất
  async function loadRootTreeData() {
    try {
      loading.value = true;
      treeData.value = [];
      const result = await queryDepartTreeSync();
      if (Array.isArray(result)) {
        treeData.value = result;
      }
      if (expandedKeys.value.length === 0) {
        autoExpandParentNode();
      }
    } finally {
      loading.value = false;
    }
  }

  loadRootTreeData();

  // Tải thông tin phòng ban cấp con
  async function loadChildrenTreeData(treeNode) {
    try {
      const result = await queryDepartTreeSync({
        pid: treeNode.dataRef.id,
      });
      if (result.length == 0) {
        treeNode.dataRef.isLeaf = true;
      } else {
        treeNode.dataRef.children = result;
        if (expandedKeys.value.length > 0) {
          // Kiểm tra trong các mục con lấy được có mục đang mở rộng hay không
          let subKeys: any[] = [];
          for (let key of expandedKeys.value) {
            if (result.findIndex((item) => item.id === key) !== -1) {
              subKeys.push(key);
            }
          }
          if (subKeys.length > 0) {
            expandedKeys.value = [...expandedKeys.value];
          }
        }
      }
      treeData.value = [...treeData.value];
    } catch (e) {
      console.error(e);
    }
    return Promise.resolve();
  }

  // Tự động mở rộng nút cha, chỉ mở rộng một cấp
  function autoExpandParentNode() {
    let item = treeData.value[0];
    if (item) {
      if (!item.isLeaf) {
        expandedKeys.value = [item.key];
      }
      reloadTree();
    }
  }

  // Tải lại component cây, ngăn việc không thể mở rộng dữ liệu mặc định
  async function reloadTree() {
    await nextTick();
    treeReloading.value = true;
    await nextTick();
    treeReloading.value = false;
  }

  /**
   * Đặt hàng đang được chọn
   */
  function setSelectedKey(key: string, data?: object) {
    selectedKeys.value = [key];
    if (data) {
      currentDepart.value = data;
      emit('select', data);
    }
  }

  // Sự kiện tìm kiếm
  async function onSearch(value: string) {
    if (value) {
      try {
        loading.value = true;
        treeData.value = [];
        let result = await searchByKeywords({ keyWord: value, orgCategory: '1,2,4' });
        if (Array.isArray(result)) {
          treeData.value = result;
        }
        autoExpandParentNode();
      } finally {
        loading.value = false;
      }
    } else {
      loadRootTreeData();
    }
    searchKeyword.value = value;
  }

  // Sự kiện lựa chọn trên cây
  function onSelect(selKeys, event) {
    if (selKeys.length > 0 && selectedKeys.value[0] !== selKeys[0]) {
      setSelectedKey(selKeys[0], event.selectedNodes[0]);
    } else {
      // Làm vậy có thể ngăn người dùng hủy lựa chọn
      setSelectedKey(selectedKeys.value[0]);
    }
  }

  /**
   * Lấy tiêu đề
   * @param title Tên phòng ban
   * @param departNameAbbr Tên viết tắt
   */
  function getTitle(title, departNameAbbr) {
    if (departNameAbbr){
      return departNameAbbr;
    }
    return title;
  }

  defineExpose({
    loadRootTreeData,
  });
</script>
