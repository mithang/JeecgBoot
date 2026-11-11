import { ref } from 'vue';
import { getMenus } from '/@/router/menus';

export const useHideHomeDesign = (currentRoute) => {
  let menus: any = [];
  // Có ẩn thiết kế cổng không
  const isHideHomeDesign = ref(true);
  const getHideHomeDesign = (isCurItem, path) => {
    if (/^\/portal-view\/[^/]+$/.test(path) && isCurItem) {
      if (['/portal-view/system', '/portal-view/template'].includes(path)) {
        // Cổng chính, cổng mẫu (cần kiểm tra có danh sách thiết kế hay không; nếu có thì hiển thị thiết kế cổng, nếu không thì ẩn thiết kế cổng)
        getIsHasPortalDesignList();
      } else if (['/portal-view/default'].includes(path)) {
        // Bản xem trước mở từ designer cần ẩn chế độ thiết kế
        isHideHomeDesign.value = true;
      } else {
        // Bàn làm việc cá nhân hoặc cổng thông thường đều có thể hiển thị thiết kế cổng
        isHideHomeDesign.value = false;
      }
    } else {
      // Trang không phải portal thì ẩn thiết kế portal
      isHideHomeDesign.value = true;
    }
  };
  const getMenusContainPath = async (ptah) => {
    if (!menus.length) {
      menus = await getMenus();
    }
    const result = getMatchingRouterName(menus, ptah);
    return !!result;
  };
  const getIsHasPortalDesignList = async () => {
    if (['/portal-view/system', '/portal-view/template'].includes(currentRoute.value.path)) {
      // Chỉ khi là cổng chính, cổng mẫu mới cần truy vấn xem trong menu có portalDesignList hay không
      getMenusContainPath('/super/eoa/portalapp/portalDesignList').then((result) => {
        isHideHomeDesign.value = !result;
      });
    }
  };
  getIsHasPortalDesignList();
  return {
    getHideHomeDesign,
    isHideHomeDesign,
  };
};

/*
 * 20250701
 * liaozhiyang
 * Khớp mục trong menu thông qua path
 * */
function getMatchingRouterName(menus, path) {
  for (let i = 0, len = menus.length; i < len; i++) {
    const item = menus[i];
    if (item.path === path && !item.redirect && !item.paramPath) {
      return item;
    } else if (item.children?.length) {
      const result = getMatchingRouterName(item.children, path);
      if (result) {
        return result;
      }
    }
  }
  return null;
}
