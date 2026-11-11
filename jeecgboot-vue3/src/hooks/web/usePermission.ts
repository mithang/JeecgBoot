import type { RouteRecordRaw } from 'vue-router';

import { useAppStore } from '/@/store/modules/app';
import { usePermissionStore } from '/@/store/modules/permission';
import { useUserStore } from '/@/store/modules/user';

import { useTabs } from './useTabs';

import { router, resetRouter } from '/@/router';
// import { RootRoute } from '/@/router/routes';

import projectSetting from '/@/settings/projectSetting';
import { PermissionModeEnum } from '/@/enums/appEnum';
import { RoleEnum } from '/@/enums/roleEnum';

import { intersection } from 'lodash-es';
import { isArray } from '/@/utils/is';
import { useMultipleTabStore } from '/@/store/modules/multipleTab';

// User permissions related operations
export function usePermission() {
  const userStore = useUserStore();
  const appStore = useAppStore();
  const permissionStore = usePermissionStore();
  //Tải động quyền biểu mẫu nút quy trình
  let formData: any = {};
  function initBpmFormData(_bpmFormData) {
    formData = _bpmFormData;
  }
  const { closeAll } = useTabs(router);

  //==================================Kiểm tra quyền quy trình làm việc-begin=========================================
  function hasBpmPermission(code, type) {
    // Vô hiệu hóa-type=2
    // Hiển thị-type=1
    let codeList: string[] = [];
    let permissionList = formData.permissionList;
    if (permissionList && permissionList.length > 0) {
      for (let item of permissionList) {
        if (item.type == type) {
          codeList.push(item.action);
        }
      }
    }
    return codeList.indexOf(code) >= 0;
  }
  //==================================Kiểm tra quyền quy trình làm việc-end=========================================

  /**
   * Change permission mode
   */
  async function togglePermissionMode() {
    appStore.setProjectConfig({
      permissionMode: projectSetting.permissionMode === PermissionModeEnum.BACK ? PermissionModeEnum.ROUTE_MAPPING : PermissionModeEnum.BACK,
    });
    location.reload();
  }

  /**
   * Reset and regain authority resource information
   * @param id
   */
  async function resume() {
    const tabStore = useMultipleTabStore();
    tabStore.clearCacheTabs();
    resetRouter();
    const routes = await permissionStore.buildRoutesAction();
    routes.forEach((route) => {
      router.addRoute(route as unknown as RouteRecordRaw);
    });
    permissionStore.setLastBuildMenuTime();
    closeAll();
  }

  /**
   * Xác nhận có tồn tại quyền không
   */
  function hasPermission(value?: RoleEnum | RoleEnum[] | string | string[], def = true): boolean {
    // Visible by default
    if (!value) {
      return def;
    }

    const permMode = projectSetting.permissionMode;

    if ([PermissionModeEnum.ROUTE_MAPPING, PermissionModeEnum.ROLE].includes(permMode)) {
      if (!isArray(value)) {
        return userStore.getRoleList?.includes(value as RoleEnum);
      }
      return (intersection(value, userStore.getRoleList) as RoleEnum[]).length > 0;
    }

    if (PermissionModeEnum.BACK === permMode) {
      const allCodeList = permissionStore.getPermCodeList as string[];
      if (!isArray(value) && allCodeList && allCodeList.length > 0) {
        //=============================Kiểm tra quyền quy trình làm việc-Hiển thị-begin==============================================
        if (formData) {
          let code = value as string;
          if (hasBpmPermission(code, '1') === true) {
            return true;
          }
        }
        //=============================Kiểm tra quyền quy trình làm việc-Hiển thị-end==============================================
        return allCodeList.includes(value);
      }
      return (intersection(value, allCodeList) as string[]).length > 0;
    }
    return true;
  }
  /**
   * Có vô hiệu hóa component không
   */
  function isDisabledAuth(value?: RoleEnum | RoleEnum[] | string | string[], def = true): boolean {
    //=============================Kiểm tra quyền quy trình làm việc-Vô hiệu hóa-begin==============================================
    if (formData) {
      let code = value as string;
      if (hasBpmPermission(code, '2') === true) {
        return true;
      }
      // Giải thích logic mã: VUEN-1342[Quy trình] Cách mã hóa Sau khi cấu hình quyền nút xong, chưa có hiệu lực
      if (isCodingButNoConfig(code) == true) {
        return false;
      }
    }
    //=============================Kiểm tra quyền quy trình làm việc-Vô hiệu hóa-end==============================================
    return !hasPermission(value);
  }

  /**
   * Change roles
   * @param roles
   */
  async function changeRole(roles: RoleEnum | RoleEnum[]): Promise<void> {
    if (projectSetting.permissionMode !== PermissionModeEnum.ROUTE_MAPPING) {
      throw new Error('Please switch PermissionModeEnum to ROUTE_MAPPING mode in the configuration to operate!');
    }

    if (!isArray(roles)) {
      roles = [roles];
    }
    userStore.setRoleList(roles);
    await resume();
  }

  /**
   * refresh menu data
   */
  async function refreshMenu() {
    resume();
  }

  /**
   * Kiểm tra có phải không Trường hợp trong mã đã viết logic nhưng chưa cấu hình quyền
   */
  function isCodingButNoConfig(code) {
    let all = permissionStore.allAuthList;
    if (all && all instanceof Array) {
      let temp = all.filter((item) => item.action == code);
      if (temp && temp.length > 0) {
        if (temp[0].status == '0') {
          return true;
        }
      } else {
        // Giải thích logic mã: 【TV360X-1604]Quyền vô hiệu hóa nút, nếu không tra được trong API thì cũng vô hiệu hóa
        return false;
      }
    }
    return false;
  }

  return { changeRole, hasPermission, togglePermissionMode, refreshMenu, isDisabledAuth, initBpmFormData };
}
