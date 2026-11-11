<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" width="650px" destroyOnClose showFooter>
    <template #title>
      Cấu hình quyền hạn
      <a-dropdown>
        <a-button class="more-icon">
          Thao tác khác
          <Icon icon="ant-design:down-outlined" size="14px" style="position: relative; top: 1px; right: 5px"></Icon>
        </a-button>
        <template #overlay>
          <a-menu @click="treeMenuClick">
            <a-menu-item key="checkAll">Chọn tất cả</a-menu-item>
            <a-menu-item key="cancelCheck">Hủy lựa chọn</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="openAll">Mở rộng tất cả</a-menu-item>
            <a-menu-item key="closeAll">Thu gọn tất cả</a-menu-item>
            <div class="line"></div>
            <a-menu-item key="relation">Liên kết theo cấp bậc</a-menu-item>
            <a-menu-item key="standAlone">Phân cấp độc lập</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>
    </template>
    <BasicTree
      ref="treeRef"
      checkable
      :treeData="treeData"
      :checkedKeys="checkedKeys"
      :expandedKeys="expandedKeys"
      :selectedKeys="selectedKeys"
      :clickRowToExpand="false"
      :checkStrictly="true"
      title="Các quyền đang sở hữu"
      @check="onCheck"
      @select="onTreeNodeSelect"
    >
      <template #title="{ slotTitle, ruleFlag }">
        {{ slotTitle }}
        <Icon v-if="ruleFlag" icon="ant-design:align-left-outlined" style="margin-left: 5px; color: red"></Icon>
      </template>
    </BasicTree>
    <!--Nút ở góc dưới bên phải-->
    <template #footer>
      <!-- <PopConfirmButton title="Bạn có chắc muốn hủy bỏ chỉnh sửa không?" @confirm="closeDrawer" okText="Xác nhận" cancelText="Hủy"></PopConfirmButton> -->
      <a-button @click="closeDrawer">Hủy</a-button>
      <a-button @click="handleSubmit(false)" type="primary" :loading="loading" ghost style="margin-right: 0.8rem">Chỉ lưu</a-button>
      <a-button @click="handleSubmit(true)" type="primary" :loading="loading">Lưu và đóng</a-button>
    </template>
    <PackDataRuleDrawer @register="registerDataRuleDrawer" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { queryPremTreeList, editPackPermission } from '../tenant.api';
  import PackDataRuleDrawer from './PackDataRuleDrawer.vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import { PACK_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  const emit = defineEmits(['register','success']);
  //Thông tin của cây
  const treeData = ref<TreeItem[]>([]);
  //Thông tin tất cả các nút của cây
  const allTreeKeys = ref([]);
  //Thông tin nút được chọn trên cây
  const checkedKeys = ref<any>([]);
  //Thông tin nút được chọn của cây
  const selectedKeys = ref([]);
  const packId = ref('');
  //Instance của cây
  const treeRef = ref(null);
  const loading = ref(false);

  //Mở rộng phần đã thu gọnkey
  const expandedKeys = ref<any>([]);
  //Trạng thái chọn của nút cha và nút con có liên kết hay không: true = không liên kết, false = có liên kết
  const checkStrictly = ref<boolean>(false);
  // Ngăn kéo quy tắc dữ liệu
  const [registerDataRuleDrawer, { openDrawer: openDataRuleDrawer }] = useDrawer();
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false, loading: true });
    packId.value = data.packId;
    //Khởi tạo dữ liệu
    const roleResult = await queryPremTreeList();
    treeData.value = translateTitle(roleResult.treeList);
    allTreeKeys.value = roleResult.ids;
    const localData = localStorage.getItem(PACK_AUTH_CONFIG_KEY);
    if (localData) {
      const obj = JSON.parse(localData);
      obj.level && treeMenuClick({ key: obj.level });
      obj.expand && treeMenuClick({ key: obj.expand });
    } else {
      expandedKeys.value = roleResult.ids;
    }

    //Khởi tạo dữ liệu menu của vai trò
    if(data.permissionIds){
      checkedKeys.value = data.permissionIds.split(",");
    }

    setDrawerProps({ loading: false });
  });
  /**
   * Dịch tên menu
   */
  function translateTitle(data) {
    if (data?.length) {
      data.forEach((item) => {
        if (item.slotTitle) {
          const { t } = useI18n();
          if (item.slotTitle.includes("t('") && t) {
            item.slotTitle = new Function('t', `return ${item.slotTitle}`)(t);
          }
        }
        if (item.children?.length) {
          translateTitle(item.children);
        }
      });
    }
    return data;
  }
  /**
   * Nhấn để chọn
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
  
  /**
   * Chọn nút, mở ngăn kéo quyền dữ liệu
   */
  function onTreeNodeSelect(key) {
    if (key && key.length > 0) {
      selectedKeys.value = key;
    }
    openDataRuleDrawer(true, { functionId: unref(selectedKeys)[0], packId: unref(packId) });
  }

  /**
   * Đặt lại dữ liệu
   */
  function reset() {
    treeData.value = [];
    allTreeKeys.value = [];
    checkedKeys.value = [];
    selectedKeys.value = [];
    packId.value = '';
  }
  /**
   * Lấy instance của tree
   */
  function getTree() {
    const tree = unref(treeRef);
    if (!tree) {
      throw new Error('tree is null!');
    }
    return tree;
  }
  /**
   * Gửi
   */
  async function handleSubmit(exit) {
    let params = {
      id: unref(packId),
      permissionIds: unref(getTree()?.getCheckedKeys()).join(','),
    };
    if (loading.value === false) {
      await doSave(params);
    } else {
      console.log('Vui lòng chờ lần thực thi trước hoàn tất!');
    }
    if (exit) {
      // Nếu đóng
      closeDrawer();
    }
  }

  // VUELưu lặp lại việc cấp quyền vai trò #352
  async function doSave(params) {
    loading.value = true;
    try {
      await editPackPermission(params);
      emit("success")
    } catch (e) {
      loading.value = false;
    }
    setTimeout(() => {
      loading.value = false;
    }, 500);
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
   * Bộ nhớ đệm thao tác của hộp thoại cấp quyền vai trò
   */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(PACK_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(PACK_AUTH_CONFIG_KEY, JSON.stringify(obj));
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
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.jeecg-tree-header) {
    border-bottom: none;
  }
</style>
