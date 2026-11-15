import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess, baseUrl } from '../_util';

// Dữ liệu mẫu cho demo bảng cây không đồng bộ (AsyncTreeTable): trang gọi
// /mock/api/asynTreeList với params { id } — id '0' lấy các nút gốc, mở rộng
// một nút sẽ gọi lại với id của nút đó để lấy các nút con.
// (Audit A5: trước đây không có handler nào cho URL này nên bảng cây luôn trống;
// FE gọi qua defHttp nên URL thực tế được thêm tiền tố /jeecgboot từ
// VITE_GLOB_API_URL, khớp với baseUrl '/jeecgboot/mock' tại đây.)
const treeData: Record<string, Recordable[]> = {
  '0': [
    { id: '1', name: 'Trang chủ', component: 'demo/feat/index', orderNum: 1, hasChildren: true },
    { id: '2', name: 'Quản lý hệ thống', component: 'demo/system/index', orderNum: 2, hasChildren: true },
    { id: '3', name: 'Ví dụ biểu mẫu', component: 'demo/form/index', orderNum: 3, hasChildren: false },
  ],
  '1': [
    { id: '11', name: 'Bảng điều khiển', component: 'demo/dashboard/index', orderNum: 1, hasChildren: false },
    { id: '12', name: 'Trang công việc', component: 'demo/workbench/index', orderNum: 2, hasChildren: false },
  ],
  '2': [
    { id: '21', name: 'Quản lý người dùng', component: 'system/user/index', orderNum: 1, hasChildren: false },
    { id: '22', name: 'Quản lý vai trò', component: 'system/role/index', orderNum: 2, hasChildren: false },
  ],
};

export default [
  {
    url: `${baseUrl}/api/asynTreeList`,
    timeout: 200,
    method: 'get',
    response: ({ query }) => {
      return resultSuccess(treeData[query?.id] ?? []);
    },
  },
] as MockMethod[];
