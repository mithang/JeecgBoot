<template>
  <a-spin :spinning="loading">
    <template v-if="treeData && treeData.length > 0">
      <div style="margin-top: 10px;margin-bottom: 10px;display: flex">
        <a-button preIcon="ant-design:down-outlined" @click="expandAll(true)" type="primary">Mở rộng tất cả</a-button>
        <a-button preIcon="ant-design:up-outlined" @click="expandAll(false)" type="primary" style="margin-left: 10px">Thu gọn tất cả</a-button>
      </div>
      <BasicTree
        :expandedKeys="expandedKeys"
        :fieldNames="{ children: 'children', title: 'title', key: 'value' }"
        ref="basicTree"
        :treeData="treeData"
        :checkStrictly="true"
        style="height: 500px; overflow: auto"
      ></BasicTree>
    </template>
    <a-empty v-else description="Không có tin nhắn vị trí" />
  </a-spin>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { BasicTree } from '/@/components/Tree/index';
  import { getRankRelation } from '../depart.api';

  const props = defineProps({
    data: { type: Object, default: () => ({}) },
  });
  // ID phòng ban đang chọn, có thể để trống, nghĩa là chưa chọn phòng ban
  const departId = computed(() => props.data?.id);

  const basicTree = ref();
  const loading = ref<boolean>(false);
  //Thông tin tất cả các nút của cây
  const treeData = ref<any[]>([]);
  //đã được chọnkey
  const expandedKeys = ref<any[]>([]);
  //Tất cả các phòng banid
  const departIds = ref<any[]>([]);

  watch(departId, (val) => loadData(val), { immediate: true });

  async function loadData(val) {
    try {
      loading.value = true;
      await getRankRelation({ departId: val }).then((res) => {
        if (res.success) {
          treeData.value = res.result;
          departIds.value = getParentDepartmentIds(res.result);
        }
      });
    } finally {
      loading.value = false;
    }
  }

  /**
   * Thu gọn tất cả
   *
   * @param expandAll
   */
  async function expandAll(expandAll) {
    if (!expandAll) {
      expandedKeys.value = [];
    } else {
      expandedKeys.value = departIds.value;
    }
  }

  /**
   * Lấy các phòng ban có cấp conid
   * @param departments
   */
  function getParentDepartmentIds(departments) {
    const ids: any = [];
    departments.forEach((dept) => {
      // Kiểm tra có mảng children hay không và không trống
      if (dept.children && Array.isArray(dept.children) && dept.children.length > 0) {
        ids.push(dept.id);
        // Kiểm tra đệ quy phòng ban con có mục con hay không
        ids.push(...getParentDepartmentIds(dept.children));
      }
    });
    return ids;
  }
</script>

<style lang="less" scoped>
  .depart-rule-tree :deep(.scrollbar__bar) {
    pointer-events: none;
  }
</style>
