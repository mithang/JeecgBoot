import { defHttp } from '/@/utils/http/axios';

export enum Api {
  authField = '/online/cgform/api/authColumn',
  authButton = '/online/cgform/api/authButton',
  authData = '/online/cgform/api/authData',
  authPage = '/online/cgform/api/authPage',
  roleAuth = '/online/cgform/api/roleAuth',
  saveButton = '/online/cgform/api/roleButtonAuth',
  saveData = '/online/cgform/api/roleDataAuth',
  validData = '/online/cgform/api/validAuthData',
  saveField = '/online/cgform/api/roleColumnAuth',
  batchAuthField = '/online/cgform/api/authColumn/batch',
  batchAuthButton = '/online/cgform/api/authButton/batch',
}

// Quyền trường, truy vấn dữ liệu
export const authFieldLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authField}/${cgformId}`, params });
// Quyền trường, cập nhật trạng thái kích hoạt
export const authFieldUpdateStatus = (params) => defHttp.put({ url: Api.authField, params });
// Quyền trường, cập nhật trạng thái quyền
export const authFieldUpdateCheckbox = (params) => defHttp.post({ url: Api.authField, params });

// Quyền trường, cập nhật hàng loạt trạng thái kích hoạt
export const batchAuthFieldUpdateStatus = (params) => defHttp.put({ url: Api.batchAuthField, params });
// Quyền trường, cập nhật hàng loạt trạng thái quyền
export const batchAuthFieldUpdateCheckbox = (params) => defHttp.post({ url: Api.batchAuthField, params });


// Quyền nút, truy vấn dữ liệu
export const authButtonLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authButton}/${cgformId}`, params });
// Quyền nút, kích hoạt
export const authButtonEnable = (params) => defHttp.post({ url: Api.authButton, params });
// Quyền nút, vô hiệu hóa
export const authButtonDisable = (id: string, params?) => defHttp.put({ url: `${Api.authButton}/${id}`, params });
// Quyền nút, cập nhật hàng loạt trạng thái kích hoạt
export const batchAuthButtonUpdateStatus = (params) => defHttp.put({ url: Api.batchAuthButton, params });

// Quyền dữ liệu, truy vấn dữ liệu
export const authDataLoadData = (cgformId, params?) => defHttp.get({ url: `${Api.authData}/${cgformId}`, params });
// Quyền dữ liệu, cập nhật trạng thái kích hoạt
export const authDataUpdateStatus = (params) => defHttp.put({ url: Api.authData, params });
// Quyền dữ liệu, lưu hoặc sửa đổi
export const authDataSaveOrUpdate = (params, isUpdate: boolean) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.authData, params });
  } else {
    return defHttp.post({ url: Api.authData, params });
  }
};
// Quyền dữ liệu, xóa
export const authDataDelete = (id: string, params?) => defHttp.delete({ url: `${Api.authData}/${id}`, params });

export const authFieldLoadTree = (cgformId: string, authType: number, params?) => {
  let url = `${Api.authPage}/${cgformId}/${authType}`;
  return defHttp.get({ url, params });
};

export const authDataLoadTree = (cgformId: string, params?) => {
  let url = `${Api.validData}/${cgformId}`;
  return defHttp.get({ url, params });
};

export const authButtonLoadTree = (cgformId: string, authType: number, params?) => {
  let url = `${Api.authPage}/${cgformId}/${authType}`;
  return defHttp.get({ url, params });
};

export const loadRoleAuthChecked = (params) => defHttp.get({ url: Api.roleAuth, params });

export const saveAuthField = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveField}/${roleId}/${cgformId}`;
  return defHttp.post({ url, params });
};

export const saveAuthData = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveData}/${roleId}/${cgformId}`;
  return defHttp.post({ url, params });
};

export const saveAuthButton = (roleId: string, cgformId: string, params?) => {
  let url = `${Api.saveButton}/${roleId}/${cgformId}`;
  return defHttp.post({url, params}, {successMessageMode: 'none', isTransformResponse: false});
};
