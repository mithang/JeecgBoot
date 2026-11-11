/* JVxeTable Quyền chỉnh sửa hàng */
import { usePermissionStoreWithOut } from '/@/store/modules/permission';

/**
 * JVxe Dành riêng cho JVxe, lấy quyền hạn
 * @param prefix
 */
export function getJVxeAuths(prefix) {
  const permissionStore = usePermissionStoreWithOut();
  prefix = getPrefix(prefix);
  let { authList, allAuthList } = permissionStore;
  let authsMap = new Map<string, typeof allAuthList[0]>();
  if (!prefix || prefix.length == 0) {
    return authsMap;
  }
  // Lấy ra tất cả quyền mà vxe sử dụng
  for (let auth of allAuthList) {
    if (auth.status == '1' && (auth.action || '').startsWith(prefix)) {
      authsMap.set(auth.action, { ...auth, isAuth: false });
    }
  }
  // Đặt đã được ủy quyền hay chưa
  for (let auth of authList) {
    let getAuth = authsMap.get(auth.action);
    if (getAuth != null) {
      getAuth.isAuth = true;
    }
  }
  // Giải thích logic mã: VUEN-1162 Nút bảng con chưa được kiểm soát
  let onlineButtonAuths = permissionStore.getOnlineSubTableAuth(prefix);
  if (onlineButtonAuths && onlineButtonAuths.length > 0) {
    for (let auth of onlineButtonAuths) {
      authsMap.set(prefix + 'btn:' + auth, { action: auth, type: 1, status: 1, isAuth: false });
    }
  }
  return authsMap;
}

/**
 * Lấy tiền tố
 * @param prefix
 */
export function getPrefix(prefix: string) {
  if (prefix && !prefix.endsWith(':')) {
    return prefix + ':';
  }
  return prefix;
}
