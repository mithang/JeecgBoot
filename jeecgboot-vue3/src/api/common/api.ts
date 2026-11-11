import { defHttp } from '/@/utils/http/axios';
import { message } from 'ant-design-vue';
import { useGlobSetting } from '/@/hooks/setting';
const globSetting = useGlobSetting();
const baseUploadUrl = globSetting.uploadUrl;
enum Api {
  positionList = '/sys/position/list',
  userList = '/sys/user/list',
  roleList = '/sys/role/list',
  queryDepartTreeSync = '/sys/sysDepart/queryDepartTreeSync',
  queryTreeList = '/sys/sysDepart/queryTreeList',
  loadTreeData = '/sys/category/loadTreeData',
  loadDictItem = '/sys/category/loadDictItem',
  getDictItems = '/sys/dict/getDictItems/',
  getTableList = '/sys/user/queryUserComponentData',
  getCategoryData = '/sys/category/loadAllData',
  refreshDragCache = '/drag/page/refreshCache',
  refreshDefaultIndexCache = '/sys/sysRoleIndex/cleanDefaultIndexCache',
  //Lấy bất đồng bộ phòng ban và vị trí
  queryDepartAndPostTreeSync = '/sys/sysDepart/queryDepartAndPostTreeSync',
  //Truy vấn người dùng thuộc phòng ban/vị trí
  queryDepartPostUserPageList = '/sys/user/queryDepartPostUserPageList',
  //Truy vấn tất cả nút cha của phòng ban đã chọnID
  queryAllParentId = '/sys/sysDepart/queryAllParentId',
}

/**
 * Đường dẫn cha khi tải lên
 */
export const uploadUrl = `${baseUploadUrl}/sys/common/upload`;

/**
 * Danh sách chức vụ
 * @param params
 */
export const getPositionList = (params) => {
  return defHttp.get({ url: Api.positionList, params });
};

/**
 * Danh sách người dùng
 * @param params
 */
export const getUserList = (params) => {
  return defHttp.get({ url: Api.userList, params });
};

/**
 * Danh sách vai trò
 * @param params
 */
export const getRoleList = (params) => {
  return defHttp.get({ url: Api.roleList, params });
};

/**
 * Lấy bất đồng bộ danh sách cây phòng ban
 */
export const queryDepartTreeSync = (params?) => {
  return defHttp.get({ url: Api.queryDepartTreeSync, params });
};
/**
 * Lấy bất đồng bộ danh sách cây vị trí phòng ban
 */
export const queryDepartAndPostTreeSync = (params?) => {
  return defHttp.get({ url: Api.queryDepartAndPostTreeSync, params });
};

/**
 * Lấy danh sách cây phòng ban
 */
export const queryTreeList = (params?) => {
  return defHttp.get({ url: Api.queryTreeList, params });
};

/**
 * Control cây từ điển phân loại Tải nút
 */
export const loadTreeData = (params?) => {
  return defHttp.get({ url: Api.loadTreeData, params });
};

/**
 * Theo từ điểncodeTải từ điểntext
 */
export const loadDictItem = (params?) => {
  return defHttp.get({ url: Api.loadDictItem, params });
};

/**
 * Theo từ điểncodeTải từ điểntext
 */
export const getDictItems = (dictCode) => {
  return defHttp.get({ url: Api.getDictItems + dictCode }, { joinTime: false });
};
/**
 * Người dùng phòng banmodalTải danh sách chọnlist
 */
export const getTableList = (params) => {
  return defHttp.get({ url: Api.getTableList, params });
};

/**
 * Người dùng vị trí phòng banmodal[Truy vấn người dùng dưới vị trí phòng ban]
 */
export const queryDepartPostUserPageList = (params) => {
  return defHttp.get({ url: Api.queryDepartPostUserPageList, params });
};

/**
 * Truy vấn tất cả nút cha của phòng ban đã chọnID
 */
export const queryAllParentId = (params) => {
  return defHttp.get({ url: Api.queryAllParentId, params });
};

/**
 * Tải tất cả dữ liệu từ điển phân loại
 */
export const loadCategoryData = (params) => {
  return defHttp.get({ url: Api.getCategoryData, params });
};
/**
 * Tải lên tệp
 */
export const uploadFile = (params, success) => {
  return defHttp.uploadFile({ url: uploadUrl }, params, { success });
};
/**
 * Tải xuống tệp
 * @param url Đường dẫn tệp
 * @param fileName Tên tệp
 * @param parameter
 * @returns {*}
 */
export const downloadFile = (url, fileName?, parameter?) => {
  return getFileblob(url, parameter).then((data) => {
    if (!data || data.size === 0) {
      message.warning('Tải xuống tệp thất bại');
      return;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data]), fileName);
    } else {
      let url = window.URL.createObjectURL(new Blob([data]));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //Tải xuống xong thì xóa phần tử
      window.URL.revokeObjectURL(url); //Giải phóngblobĐối tượng
    }
  });
};

/**
 * Tải xuống tệp Dùng choexcelXuất
 * @param url
 * @param parameter
 * @returns {*}
 */
export const getFileblob = (url, parameter) => {
  return defHttp.get(
    {
      url: url,
      params: parameter,
      responseType: 'blob',
    },
    { isTransformResponse: false }
  );
};

/**
 * [Dùng cho chức năng bình luận] Upload tệp tùy chỉnh-Phương thức
 */
export const uploadMyFile = (url, data) => {
  return defHttp.uploadMyFile(url, data);
};
/**
 * Làm mới cache bảng điều khiển
 * @param params
 */
export const refreshDragCache = () => defHttp.get({ url: Api.refreshDragCache }, { isTransformResponse: false });
/**
 * Làm mới cache trang chủ mặc định
 * @param params
 */
export const refreshHomeCache = () => defHttp.get({ url: Api.refreshDefaultIndexCache }, { isTransformResponse: false });
