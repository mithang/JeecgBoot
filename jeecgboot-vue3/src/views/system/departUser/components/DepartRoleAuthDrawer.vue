<template>
  <BasicDrawer
    :width="650"
    :loading="loading"
    showFooter
    okText="Lưu và đóng"
    @ok="onSubmit(true)"
    @close="onClose"
    @register="registerDrawer"
  >
    <template #title>
      Cấu hình quyền vai trò phòng ban
      <a-dropdown>
        <Icon icon="ant-design:more-outlined" class="more-icon" />
        <template #overlay>
          <a-menu @click="treeMenuClick">
            <a-menu-item key="checkAll">{{ t('component.tree.selectAll') }}</a-menu-item>
            <a-menu-item key="cancelCheck">{{ t('component.tree.unSelectAll') }}</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="openAll">{{ t('component.tree.expandAll') }}</a-menu-item>
            <a-menu-item key="closeAll">{{ t('component.tree.unExpandAll') }}</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="relation">{{ t('component.tree.checkStrictly') }}</a-menu-item>
            <a-menu-item key="standAlone">{{ t('component.tree.checkUnStrictly') }}</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>
    <div>
      <a-spin :spinning="loading">
        <template v-if="treeData.length > 0">
          <BasicTree
            title="Các quyền phòng ban đang sở hữu"
            checkable
            :treeData="treeData"
            :checkedKeys="checkedKeys"
            :selectedKeys="selectedKeys"
            :expandedKeys="expandedKeys"
            :checkStrictly="true"
            :clickRowToExpand="false"
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
      </a-spin>
    </div>

    <template #centerFooter>
      <a-button type="primary" :loading="loading" ghost @click="onSubmit(false)">Chỉ lưu</a-button>
    </template>
  </BasicDrawer>
  <DepartRoleDataRuleDrawer @register="registerDataRuleDrawer" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';

  import { BasicTree } from '/@/components/Tree/index';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useI18n } from '/@/hooks/web/useI18n';
  import DepartRoleDataRuleDrawer from './DepartRoleDataRuleDrawer.vue';
  import { queryTreeListForDeptRole, queryDeptRolePermission, saveDeptRolePermission } from '../depart.user.api';
  import { translateTitle } from "@/utils/common/compUtils";
  import { DEPART_ROLE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';

  defineEmits(['register']);
  const { createMessage } = useMessage();
  const loading = ref(false);
  const departId = ref('');
  const roleId = ref('');
  const treeData = ref<Array<any>>([]);
  const checkedKeys = ref<Array<any>>([]);
  const lastCheckedKeys = ref<Array<any>>([]);
  const expandedKeys = ref<Array<any>>([]);
  const selectedKeys = ref<Array<any>>([]);
  const allTreeKeys = ref<Array<any>>([]);
  //Trạng thái chọn của nút cha và nút con có liên kết hay không: true = không liên kết, false = có liên kết
  const checkStrictly = ref(false);
  const { t } = useI18n();

  // Đăng ký thành phần ngăn kéo
  const [registerDrawer, { closeDrawer }] = useDrawerInner((data) => {
    roleId.value = data.record.id;
    departId.value = data.record.departId;
    loadData({
      success: (ids) => {
        // Giải thích logic mã: 【TV360X-1619】Đồng bộ phương pháp thay đổi vai trò hệ thống kèm bộ nhớ đệm, liên kết phân cấp mặc định sửa lại thành liên kết phân cấp gốcbug
        const localData = localStorage.getItem(DEPART_ROLE_AUTH_CONFIG_KEY);
        if (localData) {
          const obj = JSON.parse(localData);
          obj.level && treeMenuClick({ key: obj.level });
          obj.expand && treeMenuClick({ key: obj.expand });
        } else {
          // expandedKeys.value = ids;
        }
      },
    });
  });
  // Đăng ký ngăn kéo hộp thoại cấp quyền quy tắc dữ liệu
  const [registerDataRuleDrawer, dataRuleDrawer] = useDrawer();

  async function loadData(options: any = {}) {
    try {
      loading.value = true;
      // Chức năng cấp quyền vai trò người dùng, truy vấn cây quyền menu
      const { ids, treeList } = await queryTreeListForDeptRole({ departId: departId.value });
      if (ids.length > 0) {
        allTreeKeys.value = ids;
        // Giải thích logic mã: 【TV360X-1619】Đồng bộ phương pháp thay đổi vai trò hệ thống kèm bộ nhớ đệm, liên kết phân cấp mặc định sửa lại thành liên kết phân cấp gốcbug
        options.success?.(ids);
        // Giải thích logic mã: 【issues/1169】Trong chức năng Phòng ban của tôi, phần 【Quyền phòng ban】 chưa được dịch t('') Đa ngôn ngữ---
        treeData.value = translateTitle(treeList);
        // Truy vấn cấp quyền vai trò
        checkedKeys.value = await queryDeptRolePermission({ roleId: roleId.value });
        lastCheckedKeys.value = [checkedKeys.value];
      } else {
        reset();
      }
    } finally {
      loading.value = false;
    }
  }

  // Đặt lại trang
  function reset() {
    treeData.value = [];
    expandedKeys.value = [];
    checkedKeys.value = [];
    lastCheckedKeys.value = [];
    loading.value = false;
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
      dataRuleDrawer.openDrawer(true, { roleId, departId, functionId });
    }
    selectedKeys.value = [];
  }

  function doClose() {
    reset();
    closeDrawer();
  }

  function onClose() {
    reset();
  }

  async function onSubmit(exit) {
    try {
      loading.value = true;
      let params = {
        roleId: roleId.value,
        permissionIds: checkedKeys.value.join(','),
        lastpermissionIds: lastCheckedKeys.value.join(','),
      };
      await saveDeptRolePermission(params);
      if (exit) {
        doClose();
      }
    } finally {
      loading.value = false;
      if (!exit) {
        loadData();
      }
    }
  }

  /**
   * Chọn menu dạng cây
   * @param key
   */
  function treeMenuClick({ key }) {
    if (key === 'checkAll') {
      checkedKeys.value = allTreeKeys.value;
    } else if (key === 'cancelCheck') {
      checkedKeys.value = [];
    } else if (key === 'openAll') {
      expandedKeys.value = allTreeKeys.value;
      saveLocalOperation('expand', 'openAll');
    } else if (key === 'closeAll') {
      expandedKeys.value = [];
      saveLocalOperation('expand', 'closeAll');
    } else if (key === 'relation') {
      checkStrictly.value = false;
      saveLocalOperation('level', 'relation');
    } else {
      checkStrictly.value = true;
      saveLocalOperation('level', 'standAlone');
    }
  }
  /**
   * 2024-07-04
   * liaozhiyang
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(DEPART_ROLE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(DEPART_ROLE_AUTH_CONFIG_KEY, JSON.stringify(obj));
  };
</script>
<style lang="less" scoped>
  /** Nút thao tác cố định */
  .jeecg-basic-tree {
    position: absolute;
    width: 618px;
  }
  .line {
    height: 1px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
  }
  .more-icon {
    font-size: 20px !important;
    color: black;
    display: inline-flex;
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.jeecg-tree-header) {
    border-bottom: none;
  }
</style>
