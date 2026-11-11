<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" width="650px" destroyOnClose showFooter rootClassName="jeecg-role-auth-drawer">
    <template #title>
      Cấu hình quyền vai trò
      <a-dropdown>
        <a-button class="more-icon">
          Thao tác khác
          <Icon icon="ant-design:down-outlined" size="14px" style="position: relative;top: 1px;right: 5px"></Icon>
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
      <a-input-search
        v-model:value="searchValue"
        placeholder="Tìm kiếm theo tên"
        allowClear
        class="title-search"
        @search="onSearch"
        @change="onSearch"
      />
    </template>
    <div ref="treeWrapRef" class="perm-tree-wrap">
    <BasicTree
      ref="treeRef"
      checkable
      highlight
      expandOnSearch
      :height="treeHeight"
      :fieldNames="{ key: 'key', title: 'slotTitle' }"
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
    </div>
    <!--Nút ở góc dưới bên phải-->
    <template #footer>
      <!-- <PopConfirmButton title="Bạn có chắc muốn hủy bỏ chỉnh sửa không?" @confirm="closeDrawer" okText="Xác nhận" cancelText="Hủy"></PopConfirmButton> -->
      <a-button @click="closeDrawer">Hủy</a-button>
      <a-button @click="handleSubmit(false)" type="primary" :loading="loading" ghost style="margin-right: 0.8rem">Chỉ lưu</a-button>
      <a-button @click="handleSubmit(true)" type="primary" :loading="loading">Lưu và đóng</a-button>
    </template>
    <RoleDataRuleDrawer @register="registerDrawer1" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
  import { ref, computed, unref, onMounted, onUnmounted, nextTick } from 'vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTree, TreeItem } from '/@/components/Tree';
  import { PopConfirmButton } from '/@/components/Button';
  import RoleDataRuleDrawer from './RoleDataRuleDrawer.vue';
  import { queryTreeListForRole, queryRolePermission, saveRolePermission } from '../role.api';
  import { useI18n } from "/@/hooks/web/useI18n";
  import { ROLE_AUTH_CONFIG_KEY } from '/@/enums/cacheEnum';
  const emit = defineEmits(['register']);
  //Thông tin của cây
  const treeData = ref<TreeItem[]>([]);
  //Thông tin tất cả các nút của cây
  const allTreeKeys = ref([]);
  //Thông tin nút được chọn trên cây
  const checkedKeys = ref<any>([]);
  const defaultCheckedKeys = ref([]);
  //Thông tin nút được chọn của cây
  const selectedKeys = ref([]);
  const roleId = ref('');
  //Instance của cây
  const treeRef = ref(null);
  const loading = ref(false);

  // Giải thích logic mã: Cuộn ảo——Cấp cho antd Tree một height dạng số là bật, chỉ kết xuất các nút nhìn thấy được, giải quyết tình trạng giật lag khi kết xuất lượng dữ liệu lớn
  // Lớp bao bọc của vùng chứa cây, dùng để đo thực tế vị trí đỉnh thực của cây trong khung nhìn
  const treeWrapRef = ref<HTMLElement | null>(null);
  const treeHeight = ref(400);
  // Tính toán chính xác theo vị trí hình học thực: phần đỉnh của danh sách ảo antd → Khoảng cách tới phần đáy của nội dung thân ngăn kéo.
  // Các vị trí này đều không thay đổi theo treeHeight (do phần tử cố định ở phía trên/bên ngoài quyết định), nên không có vòng lặp phản hồi chết, chỉ cần đo một lần.
  function calcTreeHeight() {
    const el = unref(treeWrapRef);
    if (!el) {
      treeHeight.value = window.innerHeight - 220;
      return;
    }
    const bodyWrap = el.closest('.scrollbar__wrap') as HTMLElement | null; // Khung nhìn cuộn của thân ngăn kéo(Có padding 16px)
    const list = el.querySelector('.ant-tree-list') as HTMLElement | null; // antd Vùng chứa danh sách ảo
    if (bodyWrap && list) {
      const bottom = bodyWrap.getBoundingClientRect().bottom - 16; // Trừ đi padding dưới
      const listTop = list.getBoundingClientRect().top;
      treeHeight.value = Math.max(200, Math.floor(bottom - listTop - 4));
      return;
    }
    // dự phòng (fallback)
    const avail = bodyWrap?.clientHeight || (el.closest('.ant-drawer-body') as HTMLElement)?.clientHeight || window.innerHeight - 120;
    treeHeight.value = Math.max(200, Math.floor(avail - 90));
  }
  function handleWindowResize() {
    calcTreeHeight();
  }
  onMounted(() => {
    window.addEventListener('resize', handleWindowResize);
  });
  onUnmounted(() => {
    window.removeEventListener('resize', handleWindowResize);
  });

  //Từ khóa tìm kiếm trên thanh tiêu đề
  const searchValue = ref('');
  //Mở rộng phần đã thu gọnkey
  const expandedKeys = ref<any>([]);
  //Trạng thái chọn của nút cha và nút con có liên kết hay không: true = không liên kết, false = có liên kết
  const checkStrictly = ref<boolean>(false);
  const [registerDrawer1, { openDrawer: openDataRuleDrawer }] = useDrawer();
  const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
    await reset();
    setDrawerProps({ confirmLoading: false, loading: true });
    roleId.value = data.roleId;
    //Khởi tạo dữ liệu
    const roleResult = await queryTreeListForRole();
    // Giải thích logic mã: 【QQYUN-8355】Dịch menu trong cấu hình quyền của vai trò
    treeData.value = translateTitle(roleResult.treeList);
    allTreeKeys.value = roleResult.ids;
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    if (localData) {
      const obj = JSON.parse(localData);
      obj.level && treeMenuClick({ key: obj.level });
      obj.expand && treeMenuClick({ key: obj.expand });
    } else {
      expandedKeys.value = roleResult.ids;
    }
    //Khởi tạo dữ liệu menu của vai trò
    const permResult = await queryRolePermission({ roleId: unref(roleId) });
    checkedKeys.value = permResult;
    defaultCheckedKeys.value = permResult;
    setDrawerProps({ loading: false });
    // Sau khi ngăn kéo kết xuất xong, đo tính một lần chiều cao cuộn ảo (nguồn chiều cao là khung nhìn cố định của thân ngăn kéo, không cần tự động tính lại)
    nextTick(calcTreeHeight);
  });
  /**
  * 2024-02-28
  * liaozhiyang
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
   * 2024-04-26
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
   * 2024-04-26
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
   * 2024-04-26
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

  /**
   * Thanh tiêu đề tìm kiếm cây quyền theo tên
   */
  function onSearch() {
    getTree()?.setSearchValue(unref(searchValue));
  }
  /**
   * Chọn nút, mở ngăn kéo quyền dữ liệu
   */
  function onTreeNodeSelect(key) {
    if (key && key.length > 0) {
      selectedKeys.value = key;
    }
    openDataRuleDrawer(true, { functionId: unref(selectedKeys)[0], roleId: unref(roleId) });
  }
  /**
   * Đặt lại dữ liệu
   */
  function reset() {
    treeData.value = [];
    allTreeKeys.value = [];
    checkedKeys.value = [];
    defaultCheckedKeys.value = [];
    selectedKeys.value = [];
    roleId.value = '';
    searchValue.value = '';
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
      roleId: unref(roleId),
      permissionIds: unref(getTree().getCheckedKeys()).join(','),
      lastpermissionIds: unref(defaultCheckedKeys).join(','),
    };
    // Giải thích logic mã: issues/352 Lưu trùng lặp khi cấp quyền vai trò VUE
    if(loading.value===false){
      await doSave(params)
    }else{
      console.log('Vui lòng chờ lần thực thi trước hoàn tất!');
    }
    if(exit){
      // Nếu đóng
      closeDrawer();
    }else{
      // Chưa đóng, cần lấy lại dữ liệu đã chọn
      const permResult = await queryRolePermission({ roleId: unref(roleId) });
      defaultCheckedKeys.value = permResult;
    }
  }

  // VUELưu lặp lại việc cấp quyền vai trò #352
  async function doSave(params) {
    loading.value = true;
    try {
      await saveRolePermission(params);
    } catch (e) {
      loading.value = false;
    }
    setTimeout(()=>{
      loading.value = false;
    }, 500)
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
   * 2024-05-31
   * liaozhiyang
   * 【TV360X-590】Bộ nhớ đệm thao tác của hộp thoại cấp quyền vai trò
   * */
  const saveLocalOperation = (key, value) => {
    const localData = localStorage.getItem(ROLE_AUTH_CONFIG_KEY);
    const obj = localData ? JSON.parse(localData) : {};
    obj[key] = value;
    localStorage.setItem(ROLE_AUTH_CONFIG_KEY, JSON.stringify(obj))
  };
</script>

<style lang="less" scoped>
  /** Lớp bao bọc vùng chứa cây */
  .perm-tree-wrap {
    /** Để ScrollContainer có sẵn của BasicTree không tham gia cuộn/không giới hạn chiều cao/không hiển thị thanh cuộn, việc cuộn hoàn toàn giao cho danh sách ảo của antd,
        Tránh thanh cuộn trùng lặp, đồng thời tránh tình trạng treo do chuỗi chiều cao ảnh hưởng lẫn nhau */
    :deep(.scroll-container),
    :deep(.scrollbar__wrap),
    :deep(.scrollbar__view) {
      height: auto !important;
      max-height: none !important;
      overflow: visible !important;
    }
    :deep(.scrollbar__bar) {
      display: none !important;
    }
  }
  /** Nút thao tác cố định */
  .jeecg-basic-tree {
    position: absolute;
    width: 618px;
  }
  // Giải thích logic mã: Kiểu thả xuống của biểu tượng tiêu đề hộp thoại ngăn kéo------------
  .line {
    height: 1px;
    width: 100%;
    border-bottom: 1px solid #f0f0f0;
  }
  // Ô tìm kiếm trên thanh tiêu đề: căn phải, cố định ở hàng tiêu đề, không cuộn theo nội dung
  .title-search {
    float: right;
    width: 200px;
    margin-right: 12px;
  }
  .more-icon {
/*    font-size: 20px !important;
    color: black;
    display: inline-flex;*/
    float: right;
    margin-right: 2px;
    cursor: pointer;
  }
  :deep(.jeecg-tree-header) {
    border-bottom: none;
  }
</style>

<!-- Kiểu toàn cục không scoped: ngăn kéo được kết xuất vào body qua Teleport, bộ chọn scoped không thể áp dụng;
     Giới hạn bằng rootClassName duy nhất, chỉ ẩn mọi thanh cuộn tùy chỉnh trong ngăn kéo này, việc cuộn thống nhất giao cho danh sách ảo của antd, không ảnh hưởng lẫn nhau tới các hộp thoại khác -->
<style lang="less">
  .jeecg-role-auth-drawer .scrollbar__bar {
    display: none !important;
  }
  /** Lớp thân ngăn kéo cấm cuộn, ngăn chặn triệt để ở mức vật lý"Mượn cuộn của lớp ngoài"; việc cuộn chỉ do danh sách ảo của antd đảm nhiệm (chiều cao đã được tính chính xác, sẽ không bị cắt) */
  .jeecg-role-auth-drawer .ant-drawer-body > .scrollbar > .scrollbar__wrap {
    overflow: hidden !important;
  }
</style>
