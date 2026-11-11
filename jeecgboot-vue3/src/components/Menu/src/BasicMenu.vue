<template>
  <Menu
    :selectedKeys="selectedKeys"
    :defaultSelectedKeys="defaultSelectedKeys"
    :mode="mode"
    :openKeys="getOpenKeys"
    :inlineIndent="inlineIndent"
    :theme="theme"
    @openChange="handleOpenChange"
    :class="getMenuClass"
    @click="handleMenuClick"
    :subMenuOpenDelay="0.2"
    v-bind="getInlineCollapseOptions"
  >
    <template v-for="item in items" :key="item.path">
      <BasicSubMenuItem :item="item" :theme="theme" :isHorizontal="isHorizontal" />
    </template>
  </Menu>
</template>
<script lang="ts">
  import type { MenuState } from './types';
  import { computed, defineComponent, unref, reactive, watch, toRefs, ref } from 'vue';
  import { Menu } from 'ant-design-vue';
  import BasicSubMenuItem from './components/BasicSubMenuItem.vue';
  import { MenuModeEnum, MenuTypeEnum } from '/@/enums/menuEnum';
  import { useOpenKeys } from './useOpenKeys';
  import { RouteLocationNormalizedLoaded, useRouter } from 'vue-router';
  import { isFunction, isUrl } from '/@/utils/is';
  import { basicProps } from './props';
  import { useMenuSetting } from '/@/hooks/setting/useMenuSetting';
  import { REDIRECT_NAME } from '/@/router/constant';
  import { useDesign } from '/@/hooks/web/useDesign';
  import { getCurrentParentPath } from '/@/router/menus';
  import { listenerRouteChange } from '/@/logics/mitt/routeChange';
  import { getAllParentPath } from '/@/router/helper/menuHelper';
  import { createBasicRootMenuContext } from './useBasicMenuContext';
  import { URL_HASH_TAB } from '/@/utils';
  import { getMenus } from '/@/router/menus';

  const MIX_EXTERNAL_WHITELIST = ['/myapps/index'];

  export default defineComponent({
    name: 'BasicMenu',
    components: {
      Menu,
      BasicSubMenuItem,
    },
    props: basicProps,
    emits: ['menuClick'],
    setup(props, { emit }) {
      const isClickGo = ref(false);
      const currentActiveMenu = ref('');

      const menuState = reactive<MenuState>({
        defaultSelectedKeys: [],
        openKeys: [],
        selectedKeys: [],
        collapsedOpenKeys: [],
      });
      // Giải thích logic mã: 【QQYUN-8691]Ở chế độ menu trên cùng, online không hiển thị tên menu mà hiển thị biểu mẫu trực tuyến auto mặc định
      createBasicRootMenuContext({ menuState: menuState });
      const { prefixCls } = useDesign('basic-menu');
      const { items, mode, accordion } = toRefs(props);

      const { getCollapsed, getTopMenuAlign, getSplit } = useMenuSetting();

      const { currentRoute, getRoutes } = useRouter();

      const { handleOpenChange, setOpenKeys, getOpenKeys } = useOpenKeys(menuState, items, mode as any, accordion);

      const getIsTopMenu = computed(() => {
        const { type, mode } = props;

        return (type === MenuTypeEnum.TOP_MENU && mode === MenuModeEnum.HORIZONTAL) || (props.isHorizontal && unref(getSplit));
      });

      const getMenuClass = computed(() => {
        const align = props.isHorizontal && unref(getSplit) ? 'start' : unref(getTopMenuAlign);
        return [
          prefixCls,
          `justify-${align}`,
          {
            [`${prefixCls}__second`]: !props.isHorizontal && unref(getSplit),
            [`${prefixCls}__sidebar-hor`]: unref(getIsTopMenu),
          },
        ];
      });

      const getInlineCollapseOptions = computed(() => {
        const isInline = props.mode === MenuModeEnum.INLINE;

        const inlineCollapseOptions: { inlineCollapsed?: boolean } = {};
        if (isInline) {
          inlineCollapseOptions.inlineCollapsed = props.mixSider ? false : unref(getCollapsed);
        }
        return inlineCollapseOptions;
      });

      listenerRouteChange((route) => {
        if (route.name === REDIRECT_NAME) return;
        handleMenuChange(route);
        currentActiveMenu.value = route.meta?.currentActiveMenu as string;

        if (unref(currentActiveMenu)) {
          menuState.selectedKeys = [unref(currentActiveMenu)];
          setOpenKeys(unref(currentActiveMenu));
        }
      });

      !props.mixSider &&
        watch(
          () => props.items,
          () => {
            handleMenuChange();
          }
        );

      // Giải thích logic mã: VUEN-1144 online Sau khi cấu hình thành menu, mở menu, tên hiển thị không hiển thị thành tên menu
      async function handleMenuClick({ item, key }: { item: any; key: string; keyPath: string[] }) {
        const { beforeClickFn } = props;
        // Giải thích logic mã: 【QQYUN-8773]Cấu hình URL bên ngoài không mở được ở chế độ menu trên cùng và trong tìm kiếm
        if (isUrl(key)) {
          key = key.replace(URL_HASH_TAB, '#');
          window.open(key);
          return;
        }
        // Giải thích logic mã: 【issues/7706]Điều hướng trên thanh trên cùng, route nội bộ cũng có thể hỗ trợ mở bằng tab trình duyệt mới
        const menus = await getMenus();
        const findItem = getMatchingPath(menus, key);
        if (findItem?.internalOrExternal == true && !findItem?.children?.length) {
          // Menu cấp một khi đặt mở bên ngoài, chỉ có hiệu lực khi không có menu con
          window.open(location.origin + key);
          return;
        }
        if (beforeClickFn && isFunction(beforeClickFn)) {
          const flag = await beforeClickFn(key);
          if (!flag) return;
        }
        // Giải thích logic mã: 【QQYUN-8773] điều hướng kết hợp phía trên(Menu kết hợp bên trái phía trên)Menu cấp một nếu không cấu hình redirect thì mặc định nhảy tới menu con đầu tiên
        if (props.type === MenuTypeEnum.MIX) {
          const menuItem = getMatchingPath(menus, key);
          if (handleMixMenuClick(menuItem, key)) {
            return;
          }
          emit('menuClick', key, item);
        } else {
          emit('menuClick', key, item);
        }
        // emit('menuClick', key, item);

        isClickGo.value = true;
        // const parentPath = await getCurrentParentPath(key);

        // menuState.openKeys = [parentPath];
        menuState.selectedKeys = [key];
      }

      async function handleMenuChange(route?: RouteLocationNormalizedLoaded) {
        if (unref(isClickGo)) {
          isClickGo.value = false;
          return;
        }
        const path = (route || unref(currentRoute)).meta?.currentActiveMenu || (route || unref(currentRoute)).path;
        setOpenKeys(path);
        if (unref(currentActiveMenu)) return;
        if (props.isHorizontal && unref(getSplit)) {
          const parentPath = await getCurrentParentPath(path);
          menuState.selectedKeys = [parentPath];
        } else {
          const parentPaths = await getAllParentPath(props.items, path);
          menuState.selectedKeys = parentPaths;
        }
      }
      /**
       * liaozhiyang
       * 2024-05-18
       * Lấy menu đầu tiên dưới menu được chỉ định(Bỏ qua route ẩn)
       */
      function getSubMenu(menus) {
        for (let i = 0, len = menus.length; i < len; i++) {
          const item = menus[i];
          if (item.path && !item.hideMenu && !item.children?.length) {
            return item;
          } else if (item.children?.length) {
            const result = getSubMenu(item.children);
            if (result) {
              return result;
            }
            // update-begin--author:liaozhiyang---date:20250401---for：【issues/9405] Sửa lỗi khi bấm menu cấp một ở chế độ điều hướng hỗn hợp phía trên,Nếu cấp cuối cùng là route ẩn thì hiển thị không đúng
            // Khi tất cả menu con đều là route ẩn, trả về cấp trên làm mục tiêu điều hướng
            if (item.path && !item.hideMenu) {
              return item;
            }
            // update-end--author:liaozhiyang---date:20250401---for：【issues/9405] Sửa lỗi khi bấm menu cấp một ở chế độ điều hướng hỗn hợp phía trên,Nếu cấp cuối cùng là route ẩn thì hiển thị không đúng
          }
        }
        return null;
      }

      /**
       * liaozhiyang
       * 2024-05-18
       * Lấy menu khớp với path
       */
      function getMatchingPath(menus, path) {
        for (let i = 0, len = menus.length; i < len; i++) {
          const item = menus[i];
          if (item.path === path) {
            return item;
          } else if (item.children?.length) {
            const result = getMatchingPath(item.children, path);
            if (result) {
              return result;
            }
          }
        }
        return null;
      }
      /**
       * 2025-01-14
       * liaozhiyang
       * Lấy mục chứa path khớp trong menu
       */
      const getMatchingMenu = (menus, path) => {
        for (let i = 0, len = menus.length; i < len; i++) {
          const item = menus[i];
          if (item.path === path && !item.redirect && !item.paramPath) {
            return item;
          } else if (item.children?.length) {
            const result = getMatchingMenu(item.children, path);
            if (result) {
              return result;
            }
          }
        }
        return '';
      };
      /*
      * 2025-11-12
      * liaozhiyang
      * Điều hướng kết hợp ở phía trên(Menu kết hợp bên trái phía trên)Xử lý sự kiện nhấn vào menu cấp một.
      * 1.Không có chuyển hướng thì mở mục đầu tiên của menu con
      * 2.Có phán đoán chuyển hướng xem router có tồn tại route đó hay không và route có component tương ứng hay không, nếu tồn tại thì mở ra, nếu không tồn tại thì mở mục con đầu tiên của submenu
      */
      function handleMixMenuClick(menuItem, key) {
        const hasChildren = menuItem && menuItem.children?.length;
        // Nếu không có menu con, trả về trực tiếp
        if (!hasChildren) {
          return false;
        }
        // Nếu tồn tại redirect thì xác định trong router có tồn tại route đó hay không và route có component tương ứng hay không
        if (menuItem.redirect) {
          const allRoutes = getRoutes();
          const findRedirectRoute = allRoutes.find((item) => item.path === menuItem.redirect);
          if (findRedirectRoute && findRedirectRoute.components) {
            return false;
          }
        }
        // Kiểm tra path đầu tiên của submenu có tồn tại hay không
        const subMenuItem = getSubMenu(menuItem.children);
        if (!subMenuItem?.path) {
          return false;
        }
        const path = subMenuItem.redirect ?? subMenuItem.path;
        let nextKey = path;
        if (isUrl(path)) {
          // window.open(path);
          // Key emit ra khi mở từ bên ngoài không được là url, nếu không menu bên trái sẽ không hiển thị
          nextKey = key;
        }
        // =====================================================================
        // TODO: Mã tạm thời - Cần xóa!!!
        // Đây là xử lý đặc biệt cho menu trang chủ của Qiaoqiao Cloud, về sau cần tái cấu trúc hoặc xóa
        // =====================================================================
        // Nếu mở bên ngoài và là menu trong danh sách trắng thì mở trực tiếp
        if (subMenuItem?.internalOrExternal == true && MIX_EXTERNAL_WHITELIST.includes(path)) {
          window.open(location.origin + path);
          return true;
        }
        // =====================================================================
        emit('menuClick', nextKey, { title: subMenuItem.title });
        return true;
      }

      return {
        handleMenuClick,
        getInlineCollapseOptions,
        getMenuClass,
        handleOpenChange,
        getOpenKeys,
        ...toRefs(menuState),
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';
</style>
