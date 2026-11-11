import { Persistent, BasicKeys } from '/@/utils/cache/persistent';
import { CacheTypeEnum } from '/@/enums/cacheEnum';
import projectSetting from '/@/settings/projectSetting';
import { TOKEN_KEY, LOGIN_INFO_KEY, TENANT_ID } from '/@/enums/cacheEnum';

const { permissionCacheType } = projectSetting;
const isLocal = permissionCacheType === CacheTypeEnum.LOCAL;

/**
 * Lấytoken
 */
export function getToken() {
  return getAuthCache<string>(TOKEN_KEY);
}
/**
 * Lấy thông tin đăng nhập
 */
export function getLoginBackInfo() {
  return getAuthCache(LOGIN_INFO_KEY);
}
/**
 * Lấy tenantid
 */
export function getTenantId() {
  return getAuthCache<string>(TENANT_ID);
}

export function getAuthCache<T>(key: BasicKeys) {
  const fn = isLocal ? Persistent.getLocal : Persistent.getSession;
  return fn(key) as T;
}

export function setAuthCache(key: BasicKeys, value) {
  const fn = isLocal ? Persistent.setLocal : Persistent.setSession;
  return fn(key, value, true);
}

/**
 * Cài đặt độngkey
 * @param key
 * @param value
 */
export function setCacheByDynKey(key, value) {
  const fn = isLocal ? Persistent.setLocal : Persistent.setSession;
  return fn(key, value, true);
}

/**
 * Lấy độngkey
 * @param key
 */
export function getCacheByDynKey<T>(key) {
  const fn = isLocal ? Persistent.getLocal : Persistent.getSession;
  return fn(key) as T;
}

/**
 * Xóa độngkey
 * @param key
 */
export function removeCacheByDynKey<T>(key) {
  const fn = isLocal ? Persistent.removeLocal : Persistent.removeSession;
  return fn(key) as T;
}
/**
 * Xóa một thuộc tính trong cache
 * @param key
 * @update:Xóa một thuộc tính trong cache
 * @updateBy:lsq
 * @updateDate:2021-09-07
 */
export function removeAuthCache<T>(key: BasicKeys) {
  const fn = isLocal ? Persistent.removeLocal : Persistent.removeSession;
  return fn(key) as T;
}

export function clearAuthCache(immediate = true) {
  const fn = isLocal ? Persistent.clearLocal : Persistent.clearSession;
  return fn(immediate);
}
