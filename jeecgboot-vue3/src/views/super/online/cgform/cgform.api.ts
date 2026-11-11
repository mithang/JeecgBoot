import { defHttp } from '/@/utils/http/axios';

export enum Api {
  list = '/online/cgform/head/list',
  delete = '/online/cgform/head/delete',
  deleteBatch = '/online/cgform/head/deleteBatch',
  databaseSync = '/online/cgform/api/doDbSynch',
  removeRecord = '/online/cgform/head/removeRecord',
  copyOnline = '/online/cgform/head/copyOnline',
  copyTable = '/online/cgform/head/copyOnlineTable',
  queryById = '/online/cgform/head/queryById',

  // CgformModalTrangAPI
  addAll = '/online/cgform/api/addAll',
  editAll = '/online/cgform/api/editAll',
  queryField = '/online/cgform/field/listByHeadId',
  queryIndex = '/online/cgform/index/listByHeadId',
  checkOnlyTable = '/online/cgform/api/checkOnlyTable',
  // Chỉ sửa cấu hình bảng, không sửa trường
  editHead = '/online/cgform/head/edit'
}

/**
 * API danh sách
 * @param params
 */
export const list = (params) => defHttp.get({ url: Api.list, params });

// update-begin---author:liusq---date:20261013---for:【LHZP-1967】Sau khi tạo thiết kế in, cấu hình mở rộng của biểu mẫu không mang theo địa chỉ báo cáo
/**
 * Căn cứ theoIDCấu hình biểu mẫu truy vấn (lấy mới nhất extConfigJson v.v.)
 * @param id
 */
export const queryHeadById = (id: string) => defHttp.get({ url: Api.queryById, params: { id } });
// update-end---author:liusq---date:20261013---for:【LHZP-1967】Sau khi tạo thiết kế in, cấu hình mở rộng của biểu mẫu không mang theo địa chỉ báo cáo

// Loại bỏ hàng loạt (việc loại bỏ chỉ xóa cấu hình biểu mẫu)
export const doBatchRemove = (idList: string[]) => doRemove(idList, 0);
export const doSingleRemove = (pid) => defHttp.delete({ url: Api.removeRecord, params: { id: pid } },
  { joinParamsToUrl: true });
// Xóa hàng loạt (việc xóa sẽ xóa bảng cơ sở dữ liệu tương ứng và cả bảng con)
export const doBatchDelete = (idList: string[]) => doRemove(idList, 1);
export const doSingleDelete = (pid) => defHttp.delete({ url: Api.delete, params: { id: pid } },
  { joinParamsToUrl: true });

// Thực thi thao tác xóa
function doRemove(idList: string[], flag: number) {
  return defHttp.delete(
    {
      url: Api.deleteBatch,
      params: {
        ids: idList.join(','),
        flag: flag,
      },
    },
    { joinParamsToUrl: true }
  );
}

// Đồng bộ cơ sở dữ liệu
export const doDatabaseSync = (id, method) =>
  defHttp.post({ url: `${Api.databaseSync}/${id}/${method}`, timeout: 12000, timeoutErrorMessage: 'Đồng bộ cơ sở dữ liệu quá thời gian, đã tự động làm mới' });

export const doCopyOnlineView = (id) => defHttp.post({ url: `${Api.copyOnline}?code=${id}` });

/**
 * Sao chép bảng
 * @param id của bảng cần sao chépid
 * @param tableName Tên bảng mới
 * @param params Tham số khác
 */
export const doCopyTable = (id, tableName, params?) => defHttp.get({ url: `${Api.copyTable}/${id}`, params: { tableName, ...params } });

// PopupformApi
export const formApi = {
  // Trường của bảng truy vấn
  doQueryField: (headId: string, params?) => defHttp.get({ url: Api.queryField, params: { headId, ...params } }),
  // Bảng truy vấnindexCấu hình
  doQueryIndexes: (headId: string, params?) => defHttp.get({ url: Api.queryIndex, params: { headId, ...params } }),
  // Thêm mới hoặc sửa đổi
  doSaveOrUpdate: (params, isUpdate) => {
    if (isUpdate) {
      return defHttp.put({ url: Api.editAll, params });
    } else {
      return defHttp.post({ url: Api.addAll, params });
    }
  },
  //Chỉ là sửa cấu hình bảng, không sửa trường
  editHead: (params)=>{
    return defHttp.put({ url: Api.editHead, params });
  }
};
