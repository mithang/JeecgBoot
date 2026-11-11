// token key
export const TOKEN_KEY = 'TOKEN__';

export const LOCALE_KEY = 'LOCALE__';

// user info key
export const USER_INFO_KEY = 'USER__INFO__';

// role info key
export const ROLES_KEY = 'ROLES__KEY__';

// dict info key
export const DB_DICT_DATA_KEY = 'UI_CACHE_DB_DICT_DATA';
// dict version key (lưu thành cặp với dữ liệu từ điển, dùng để kiểm tra từ điển local có hết hạn không)
export const DB_DICT_VERSION_KEY = 'UI_CACHE_DB_DICT_VERSION';

// project config key
export const PROJ_CFG_KEY = 'PROJ__CFG__KEY__';

// lock info
export const LOCK_INFO_KEY = 'LOCK__INFO__KEY__';

export const MULTIPLE_TABS_KEY = 'MULTIPLE_TABS__KEY__';

export const APP_DARK_MODE_KEY_ = '__APP__DARK__MODE__';

// base global local key
export const APP_LOCAL_CACHE_KEY = 'COMMON__LOCAL__KEY__';

// base global session key
export const APP_SESSION_CACHE_KEY = 'COMMON__SESSION__KEY__';
// Tenant key
export const TENANT_ID = 'TENANT_ID';
// login info key
export const LOGIN_INFO_KEY = 'LOGIN__INFO__';

// Khóa UID trò chuyện
export const JEECG_CHAT_UID = 'JEECG_CHAT_UID';

// ID tenant miễn đăng nhập, tách khỏi hệ thống, tránh trùng lặp
export const OAUTH2_THIRD_LOGIN_TENANT_ID = 'THIRD_LOGIN_TENANT_ID';

// Định danh trợ lý AI (cần ghi lại khi thoát)
export const AIDE_FLAG = 'AIDE_FLAG';

// Định danh trợ lý AI (cần ghi lại khi thoát)
export const JEECG_CHAT_KEY = 'JEECG-CHAT-KEY';

// 【QQYUN-8925】Màu theme hệ thống (dùng khi tải trang)
export const APP__THEME__COLOR = '__APP__THEME__COLOR__';

// 
export const ROLE_AUTH_CONFIG_KEY = 'ROLE__AUTH__CONFIG__KEY__';
// Quyền vai trò phòng ban
export const DEPART_ROLE_AUTH_CONFIG_KEY = 'DEPART__ROLE__AUTH__CONFIG__KEY__';
// Quyền quản lý phòng ban
export const DEPART_MANGE_AUTH_CONFIG_KEY = 'DEPART__MANGE__AUTH__CONFIG__KEY__';

// Quyền quản lý gói sản phẩm
export const PACK_AUTH_CONFIG_KEY = 'PACK__AUTH__CONFIG__KEY__';

export enum CacheTypeEnum {
  SESSION,
  LOCAL,
}
