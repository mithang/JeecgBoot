import type {Menu} from "@/router/types";
import { ref, watch, unref } from 'vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { useTitle as usePageTitle } from '@vueuse/core';
import { useGlobSetting } from '/@/hooks/setting';
import { useRouter } from 'vue-router';
import { useLocaleStore } from '/@/store/modules/locale';
import { REDIRECT_NAME } from '/@/router/constant';
import { getMenus } from '/@/router/menus';

/**
 * Listening to page changes and dynamically changing site titles
 */
export function useTitle() {
  const { title } = useGlobSetting();
  const { t } = useI18n();
  const { currentRoute } = useRouter();
  const localeStore = useLocaleStore();

  const pageTitle = usePageTitle();

  const menus = ref<Menu[] | null>(null)

  watch(
    [() => currentRoute.value.path, () => localeStore.getLocale],
    async () => {
      const route = unref(currentRoute);

      if (route.name === REDIRECT_NAME) {
        return;
      }
      // Giải thích logic mã: 【QQYUN-6938】onlineTên menu và trangtitleKhông nhất quán
      if (route.params && Object.keys(route.params).length) {
        if (!menus.value) {
          menus.value = await getMenus();
        }
        const getTitle = getMatchingRouterName(menus.value, route.fullPath);
        let tTitle = '';
        if (getTitle) {
          tTitle = t(getTitle);
        } else {
          tTitle = t(route?.meta?.title as string);
        }
        pageTitle.value = tTitle ? ` ${tTitle} - ${title} ` : `${title}`;
      } else {
        const tTitle = t(route?.meta?.title as string);
        pageTitle.value = tTitle ? ` ${tTitle} - ${title} ` : `${title}`;
      }
    },
    { immediate: true }
  );
}
/** 
 2023-11-09
 liaozhiyang
 Lấy tên trang thật của chế độ khớp route
*/
function getMatchingRouterName(menus, path) {
  for (let i = 0, len = menus.length; i < len; i++) {
    const item = menus[i];
    if (item.path === path && !item.redirect && !item.paramPath) {
      return item.meta?.title;
    } else if (item.children?.length) {
      const result = getMatchingRouterName(item.children, path);
      if (result) {
        return result;
      }
    }
  }
  return '';
}
