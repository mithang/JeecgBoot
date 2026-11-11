import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/online/cgform/button/list/',
  delete = '/online/cgform/button/delete',
  deleteBatch = '/online/cgform/button/deleteBatch',
  save = '/online/cgform/button/add',
  edit = '/online/cgform/button/edit',

  builtInList = '/online/cgform/button/builtInList/',
}

export const list = (code: string, params) => defHttp.get({ url: Api.list + code, params });

// Thực thi thao tác xóa
export function doBatchDelete(idList: string[]) {
  return defHttp.delete(
    {
      url: Api.deleteBatch,
      params: {
        ids: idList.join(','),
      },
    },
    { joinParamsToUrl: true }
  );
}

/**
 * Lưu hoặc cập nhật
 */
export const saveOrUpdate = (params, isUpdate: boolean) => {
  if (isUpdate) {
    return defHttp.put({ url: Api.edit, params });
  } else {
    return defHttp.post({ url: Api.save, params });
  }
};

/**
 * Tải danh sách nút tích hợp sẵn
 */
export const builtInList = (code: string, params) => defHttp.get({url: Api.builtInList + code, params});
