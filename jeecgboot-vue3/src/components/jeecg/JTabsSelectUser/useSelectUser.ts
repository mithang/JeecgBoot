import { defHttp } from '/@/utils/http/axios';
import { BasicColumn, FormSchema } from '/@/components/Table';

export enum Api {
  departList = '/sys/sysDepart/queryDepartTreeSync',
  userList = '/sys/user/list',
  departUserList = '/sys/user/queryUserByDepId',

  //Lấy thông tin cấp bậc của tất cả vị trí công tác
  queryALLRankRelation = '/sys/sysDepart/getALLRankRelation',
  //Tìm kiếm phòng ban theo từ khóa
  searchBy = '/sys/sysDepart/searchBy'
}

/**
 * Lấy danh sách cây phòng ban
 */
export const getDepartTreeData = (params?) => defHttp.get({ url: Api.departList, params });

/**
 * Lấy danh sách người dùng
 */
export const getUserList = (params?) => defHttp.get({ url: Api.userList, params: { ...params, status: '1' } });

/**
 * Lấy danh sách người dùng của phòng ban chỉ định
 */
export const getDepartUserList = (params?) => defHttp.get({ url: Api.departUserList, params });
/**
 * Lấy thông tin cấp bậc
 */
export const queryALLRankRelation = (params?) => defHttp.get({ url: Api.queryALLRankRelation, params,timeout: 2 * 60 * 1000 });

/**
 * Tìm kiếm phòng ban theo từ khóa
 */
export const searchByKeywords = (params) => defHttp.get({ url: Api.searchBy, params });
/**
 * Danh sách người dùng
 */
export const columns: BasicColumn[] = [
  // {
  //   title: 'Tài khoản người dùng',
  //   align: 'center',
  //   dataIndex: 'username',
  //   ellipsis: true,
  //   width: 130,
  // },
  {
    title: 'Họ tên người dùng',
    align: 'center',
    width: 150,
    dataIndex: 'realname',
    ellipsis: true,
  },
  {
    title: 'Phòng ban',
    align: 'center',
    width: 150,
    dataIndex: 'orgCodeTxt',
  },
];

/**
 * Danh sách người dùng đã chọn
 */
export const selectedUserColumns: BasicColumn[] = [
  {
    title: 'Họ tên người dùng',
    align: 'center',
    width: 150,
    dataIndex: 'realname',
    ellipsis: true,
  },
];

/**
 * Điều kiện tìm kiếm
 */
export const searchFormSchema: FormSchema[] = [
  {
    label: 'Họ tên người dùng',
    field: 'realname',
    component: 'Input',
    componentProps: {
      style: {
        width: '150px',
      },
    },
  },
  // {
  //   label: 'Tài khoản người dùng',
  //   field: 'username',
  //   component: 'JInput',
  //   componentProps: {
  //     style: {
  //       width: '150px',
  //     },
  //   },
  // },
];
