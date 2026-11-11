<template>
  <Menu
    v-bind="getBindValues"
    :activeName="activeName"
    :openNames="getOpenKeys"
    :class="`${prefixCls} ${isThemeBright ? 'bright' : ''}`"
    :activeSubMenuNames="activeSubMenuNames"
    @select="handleSelect"
  >
    <template v-for="item in items" :key="item.path">
      <SimpleSubMenu :isThemeBright="isThemeBright" :item="item" :parent="true" :collapsedShowTitle="collapsedShowTitle" :collapse="collapse" />
    </template>
  </Menu>
</template>
<script lang="ts">
  import type { MenuState } from './types';
  import type { Menu as MenuType } from '/@/router/types';
  import type { RouteLocationNormalizedLoaded } from 'vue-router';
  import { defineComponent, computed, ref, unref, reactive, toRefs, watch } from 'vue';
  import { useDesign } from '/@/hooks/web/useDesign';
  import Menu from './components/Menu.vue';
  import SimpleSubMenu from './SimpleSubMenu.vue';
  import { listenerRouteChange } from '/@/logics/mitt/routeChange';
  import { propTypes } from '/@/utils/propTypes';
  import { REDIRECT_NAME } from '/@/router/constant';
  import { useRouter } from 'vue-router';
  import { isFunction, isUrl } from '/@/utils/is';
  import { openWindow } from '/@/utils';

  import { useOpenKeys } from './useOpenKeys';
  import { URL_HASH_TAB } from '/@/utils';
  import { useAppStore } from '/@/store/modules/app';

  export default defineComponent({
    name: 'SimpleMenu',
    components: {
      Menu,
      SimpleSubMenu,
    },
    inheritAttrs: false,
    props: {
      items: {
        type: Array as PropType<MenuType[]>,
        default: () => [],
      },
      collapse: propTypes.bool,
      mixSider: propTypes.bool,
      theme: propTypes.string,
      accordion: propTypes.bool.def(true),
      collapsedShowTitle: propTypes.bool,
      beforeClickFn: {
        type: Function as PropType<(key: string) => Promise<boolean>>,
      },
      isSplitMenu: propTypes.bool,
    },
    emits: ['menuClick'],
    setup(props, { attrs, emit }) {
      const currentActiveMenu = ref('');
      const isClickGo = ref(false);
      const appStore = useAppStore();
      const isThemeBright = ref(false);

      const menuState = reactive<MenuState>({
        activeName: '',
        openNames: [],
        activeSubMenuNames: [],
      });

      const { currentRoute } = useRouter();
      const { prefixCls } = useDesign('simple-menu');
      const { items, accordion, mixSider, collapse } = toRefs(props);

      const { setOpenKeys, getOpenKeys } = useOpenKeys(menuState, items, accordion, mixSider, collapse);

      const getBindValues = computed(() => ({ ...attrs, ...props }));

      watch(
        () => props.collapse,
        (collapse) => {
          if (collapse) {
            menuState.openNames = [];
          } else {
            setOpenKeys(currentRoute.value.path);
          }
        },
        { immediate: true }
      );

      watch(
        () => props.items,
        () => {
          if (!props.isSplitMenu) {
            return;
          }
          setOpenKeys(currentRoute.value.path);
        },
        { flush: 'post' }
      );
      // Giải thích logic mã: 【QQYUN-8922]Điều chỉnh màu chữ thanh điều hướng bên trái để phân biệt chế độ màu và chế độ tối
      watch(
        () => appStore.getProjectConfig.menuSetting,
        (menuSetting) => {
          isThemeBright.value = !!menuSetting?.isThemeBright;
        },
        { immediate: true, deep: true }
      );
      listenerRouteChange((route) => {
        if (route.name === REDIRECT_NAME) return;

        currentActiveMenu.value = route.meta?.currentActiveMenu as string;
        handleMenuChange(route);

        if (unref(currentActiveMenu)) {
          menuState.activeName = unref(currentActiveMenu);
          setOpenKeys(unref(currentActiveMenu));
        }
      });

      async function handleMenuChange(route?: RouteLocationNormalizedLoaded) {
        if (unref(isClickGo)) {
          isClickGo.value = false;
          return;
        }
        const path = (route || unref(currentRoute)).path;

        menuState.activeName = path;

        setOpenKeys(path);
      }

      async function handleSelect(key: string) {
        if (isUrl(key)) {
          // Giải thích logic mã: 【VUEN-656]Cấu hình URL ngoài không mở được, nguyên nhân là có#số, cần thay thế
          let url = key.replace(URL_HASH_TAB, '#');
          window.open(url)
          //openWindow(url);
          // Giải thích logic mã: 【VUEN-656]Cấu hình URL ngoài không mở được, nguyên nhân là có#số, cần thay thế
          return;
        }
        // Giải thích logic mã: 【QQYUN-6366] Route nội bộ cũng có thể hỗ trợ mở bằng tab trình duyệt mới
        const findItem = getMatchingMenu(props.items, key);
        if (findItem?.internalOrExternal == true) {
          window.open(location.origin + key);
          return;
        }

        const { beforeClickFn } = props;
        if (beforeClickFn && isFunction(beforeClickFn)) {
          const flag = await beforeClickFn(key);
          if (!flag) return;
        }

        emit('menuClick', key);

        isClickGo.value = true;
        setOpenKeys(key);
        menuState.activeName = key;
      }

      /**
       * 2024-02-27
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
      }

      return {
        prefixCls,
        getBindValues,
        handleSelect,
        getOpenKeys,
        ...toRefs(menuState),
        isThemeBright,
      };
    },
  });
</script>
<style lang="less">
  @import './index.less';
</style>
