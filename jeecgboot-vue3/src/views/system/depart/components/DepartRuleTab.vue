<template>
  <a-spin :spinning="loading">
    <template v-if="treeData.length > 0">
      <BasicTree
        ref="basicTree"
        class="depart-rule-tree"
        checkable
        :treeData="treeData"
        :checkedKeys="checkedKeys"
        :selectedKeys="selectedKeys"
        :expandedKeys="expandedKeys"
        :checkStrictly="true"
        style="height: 500px; overflow: auto"
        @check="onCheck"
        @expand="onExpand"
        @select="onSelect"
      >
        <template #title="{ slotTitle, ruleFlag }">
          <span>{{ slotTitle }}</span>
          <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red" />
        </template>
      </BasicTree>
    </template>
    <a-empty v-else description="Không có quyền phòng ban nào có thể cấu hình" />

    <div class="j-box-bottom-button offset-20" style="margin-top: 30px">
      <div class="j-box-bottom-button-float" :class="[`${prefixCls}`]">
        <a-dropdown :trigger="['click']" placement="top">
          <template #overlay>
            <a-menu>
              <a-menu-item key="3" @click="toggleCheckALL(true)">{{ t('component.tree.selectAll') }}</a-menu-item>
              <a-menu-item key="4" @click="toggleCheckALL(false)">{{ t('component.tree.unSelectAll') }}</a-menu-item>
              <a-menu-item key="5" @click="toggleExpandAll(true)">{{ t('component.tree.expandAll') }}</a-menu-item>
              <a-menu-item key="6" @click="toggleExpandAll(false)">{{ t('component.tree.unExpandAll') }}</a-menu-item>
              <a-menu-item key="7" @click="toggleRelationAll(false)">{{ t('component.tree.checkStrictly') }}</a-menu-item>
              <a-menu-item key="8" @click="toggleRelationAll(true)">{{ t('component.tree.checkUnStrictly') }}</a-menu-item>
            </a-menu>
          </template>
          <a-button style="float: left">
            Thao tác cây
            <Icon icon="ant-design:up-outlined" />
          </a-button>
        </a-dropdown>
        <a-button type="primary" preIcon="ant-design:save-filled" @click="onSubmit">Lưu</a-button>
      </div>
    </div>
  </a-spin>
  <DepartDataRuleDrawer @register="registerDataRuleDrawer" />
</template>

<script lang="ts" setup>
  import { watch, computed, inject, ref, nextTick } from 'vue';
  import { useDrawer } from '/@/components/Drawer';
  import { BasicTree } from '/@/components/Tree/index';
  import DepartDataRuleDrawer from './DepartDataRuleDrawer.vue';
  import { queryRoleTreeList, queryDepartPermission, saveDepartPermission } from '../depart.api';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { translateTitle } from '/@/utils/common/compUtils';
  import { DEPART_MANGE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  import { useI18n } from '/@/hooks/web/useI18n';

  const { prefixCls } = useDesign('j-depart-form-content');
  const props = defineProps({
    data: { type: Object, default: () => ({}) },
  });
  // ID phòng ban đang chọn, có thể để trống, nghĩa là chưa chọn phòng ban
  const departId = computed(() => props.data?.id);

  const basicTree = ref();
  const loading = ref<boolean>(false);
  //Thông tin tất cả các nút của cây
  const allTreeKeys = ref([]);
  const treeData = ref<any[]>([]);
  const expandedKeys = ref<Array<any>>([]);
  const selectedKeys = ref<Array<any>>([]);
  const checkedKeys = ref<Array<any>>([]);
  const lastCheckedKeys = ref<Array<any>>([]);
  const checkStrictly = ref(false);
  const { t } = useI18n();

  // Đăng ký ngăn kéo hộp thoại cấp quyền quy tắc dữ liệu
  const [registerDataRuleDrawer, dataRuleDrawer] = useDrawer();

  // onCreated
  loadData({
    success: (ids) => {
      // Giải thích logic mã: 【TV360X-1689】Đồng bộ phương pháp thay đổi vai trò hệ thống, bổ sung bộ nhớ đệm, liên kết phân cấp và các chức năng khác
      const localData = localStorage.getItem(DEPART_MANGE_AUTH_CONFIG_KEY);
      if (localData) {
        const obj = JSON.parse(localData);
        obj.level && toggleRelationAll(obj.level == 'relation' ? false : true);
        obj.expand && toggleExpandAll(obj.expand == 'openAll' ? true :false);
      } else {
        // expandedKeys.value = ids;
      }
    }
  });
  watch(departId, () => loadDepartPermission(), { immediate: true });

  async function loadData(options: any = {}) {
    try {
      loading.value = true;
      let { treeList, ids } = await queryRoleTreeList();
      // Giải thích logic mã: 【issues/1169】Trong chức năng Quản lý phòng ban, phần 【Quyền phòng ban】 chưa được dịch t('') Đa ngôn ngữ---
      treeData.value = translateTitle(treeList);
      // Giải thích logic mã: 【TV360X-1689】Đồng bộ phương pháp thay đổi vai trò hệ thống, bổ sung bộ nhớ đệm, liên kết phân cấp và các chức năng khác
      allTreeKeys.value = ids;
      options.success?.(ids);
    } finally {
      loading.value = false;
    }
  }

  async function loadDepartPermission() {
    if (departId.value) {
      try {
        loading.value = true;
        let keys = await queryDepartPermission({ departId: departId.value });
        checkedKeys.value = keys;
        lastCheckedKeys.value = [...keys];
      } finally {
        loading.value = false;
      }
    }
  }

  async function onSubmit() {
    try {
      loading.value = true;
      await saveDepartPermission({
        departId: departId.value,
        permissionIds: checkedKeys.value.join(','),
        lastpermissionIds: lastCheckedKeys.value.join(','),
      });
      await loadData();
      await loadDepartPermission();
    } finally {
      loading.value = false;
    }
  }

  /**
   * Nhấn để chọn
   * 2024-07-04
   * liaozhiyang
   */
  function onCheck(o, e) {
    // checkStrictly: true=>Phân cấp độc lập，false=>Liên kết theo cấp bậc.
    if (checkStrictly.value) {
      checkedKeys.value = o.checked ? o.checked : o;
    } else {
      const keys = getNodeAllKey(e.node, 'children', 'key');
      if (e.checked) {
        // Khi thao tác lặp lại có thể xuất hiện các key trùng lặp, cần dùng new Set để loại bỏ trùng lặp
        checkedKeys.value = [...new Set([...checkedKeys.value, ...keys])];
      } else {
        const result = removeMatchingItems(checkedKeys.value, keys);
        checkedKeys.value = result;
      }
    }
  }
  /**
   * 2024-07-04
   * liaozhiyang
   * Xóa các phần tử khớp trong mảng
   */
  function removeMatchingItems(arr1, arr2) {
    // Dùng bảng băm để ghi lại các phần tử trong arr2
    const hashTable = {};
    for (const item of arr2) {
      hashTable[item] = true;
    }
    // Dùng phương thức filter để duyệt mảng thứ nhất, lọc ra các phần tử không tồn tại trong bảng băm
    return arr1.filter((item) => !hashTable[item]);
  }
  /**
   * 2024-07-04
   * liaozhiyang
   * Lấy nút hiện tại và tất cả các nút cấp con cháu bên dướikey
   */
  function getNodeAllKey(node: any, children: any, key: string) {
    const result: any = [];
    result.push(node[key]);
    const recursion = (data) => {
      data.forEach((item: any) => {
        result.push(item[key]);
        if (item[children]?.length) {
          recursion(item[children]);
        }
      });
    };
    node[children]?.length && recursion(node[children]);
    return result;
  }

  // treeSự kiện mở rộng
  function onExpand($expandedKeys) {
    expandedKeys.value = $expandedKeys;
  }

  // treeSự kiện được chọn
  function onSelect($selectedKeys, { selectedNodes }) {
    if (selectedNodes[0]?.ruleFlag) {
      let functionId = $selectedKeys[0];
      dataRuleDrawer.openDrawer(true, { departId, functionId });
    }
    selectedKeys.value = [];
  }

  // Chuyển đổi liên kết cha-con
  async function toggleCheckStrictly(flag) {
    checkStrictly.value = flag;
    await nextTick();
    checkedKeys.value = basicTree.value.getCheckedKeys();
  }

  // Chuyển đổi mở rộng/thu gọn
  async function toggleExpandAll(flag) {
    // Giải thích logic mã: 【TV360X-1689】Đồng bộ phương pháp thay đổi vai trò hệ thống, bổ sung bộ nhớ đệm, liên kết phân cấp và các chức năng khác
    if (flag) {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    }
  }

  // Chuyển đổi chọn tất cả
  async function toggleCheckALL(flag) {
    // Giải thích logic mã: 【TV360X-1689】Đồng bộ phương pháp thay đổi vai trò hệ thống, bổ sung bộ nhớ đệm, liên kết phân cấp và các chức năng khác
    if (flag) {
      checkedKeys.value = allTreeKeys.value;
    } else {
      checkedKeys.value = [];
    }
  }

  // Chuyển đổi liên kết phân cấp(Độc lập)
  const toggleRelationAll = (flag) => {
    // Giải thích logic mã: 【TV360X-1689】Đồng bộ phương pháp thay đổi vai trò hệ thống, bổ sung bộ nhớ đệm, liên kết phân cấp và các chức năng khác
    checkStrictly.value = flag;
    if (flag) {
      saveLocalOperation('level', 'standAlone');
    } else {
      saveLocalOperation('level', 'relation');
    }
  };
  /**
   * 2024-07-04
   * liaozhiyang
   * Bộ nhớ đệm
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(DEPART_MANGE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(DEPART_MANGE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  // 【VUEN-188】Giải quyết vấn đề thanh cuộn không nhạy
  .depart-rule-tree :deep(.scrollbar__bar) {
    pointer-events: none;
  }
</style>
