/**
 * @description: Login interface parameters
 */
export interface LoginParams {
  username: string;
  password: string;
}

export interface ThirdLoginParams {
  token: string;
  thirdType: string;
}

export interface RoleInfo {
  roleName: string;
  value: string;
}

/**
 * @description: Login interface return value
 */
export interface LoginResultModel {
  userId: string | number;
  token: string;
  role: RoleInfo;
  userInfo?: any
}

/**
 * @description: Get user information return value
 */
export interface GetUserInfoModel {
  roles: RoleInfo[];
  // Người dùngid
  userId: string | number;
  // Tên người dùng
  username: string;
  // Tên thật
  realname: string;
  // Ảnh đại diện
  avatar: string;
  // Giới thiệu
  desc?: string;
  // Thông tin người dùng
  userInfo?: any;
  // Mục từ điển trong cache
  sysAllDictItems?: any;
  // Phiên bản từ điển
  dictVersion?: string;
  // Công tắc mật khẩu mạnh bảo mật cấp 3
  enableStrongPwd?: boolean;
}

/**
 * @description: Get user information return value
 */
export interface GetResultModel {
  code: number;
  message: string;
  result: object;
  success: Boolean;
}
