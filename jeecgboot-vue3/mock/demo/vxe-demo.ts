import { MockMethod } from 'vite-plugin-mock';
import { resultSuccess, resultPageSuccess, baseUrl } from '../_util';

// Dữ liệu mẫu cho các demo JVxeTable (điều độ tàu kéo): Template1-5, ErpTemplate,
// JSBCDemo, PopupSubTable, SocketReload đều gọi /mock/vxe/getData và mong đợi
// { total, records }; lưu một dòng (PUT /mock/vxe/immediateSaveRow) và lưu toàn
// bộ (POST /mock/vxe/immediateSaveAll) chỉ cần trả về thành công.
// (Audit A5: trước đây không có handler nào cho các URL này nên các bảng demo
// luôn trống; FE gọi qua defHttp nên URL thực tế được thêm tiền tố /jeecgboot
// từ VITE_GLOB_API_URL, khớp với baseUrl '/jeecgboot/mock' tại đây.)
const vxeRows = (() => {
  const companies = ['Công ty TNHH Vận tải Biển Đông', 'Công ty CP Dịch vụ Hàng hải', 'Công ty TNHH Lai dắt Hải Phòng'];
  const ships = ['Hải Âu 01', 'Sao Biển 02', 'Bạch Đằng 03', 'Hồng Hà 04', 'Cửu Long 05', 'Trường Sa 06'];
  const ports = ['Cảng Hải Phòng', 'Cảng Cái Mép', 'Cảng Quy Nhơn', 'Cảng Đà Nẵng'];
  const rows: Recordable[] = [];
  for (let i = 0; i < 30; i++) {
    rows.push({
      id: `${i + 1}`,
      dd_num: `DD${String(260100 + i)}`,
      tug: ships[i % ships.length],
      power: `${1800 + (i % 5) * 400}`,
      nature: i % 2 === 0 ? 'Lai dắt' : 'Hộ tống',
      departure_time: `2026-11-${String((i % 28) + 1).padStart(2, '0')} 08:${String((i * 7) % 60).padStart(2, '0')}`,
      work_start_time: `2026-11-${String((i % 28) + 1).padStart(2, '0')} 09:00`,
      work_stop_time: `2026-11-${String((i % 28) + 1).padStart(2, '0')} 12:30`,
      company: companies[i % companies.length],
      ship_name: ships[i % ships.length],
      call: `CALL-${1000 + i}`,
      trend: i % 3 === 0 ? 'Tăng' : 'Ổn định',
      count: (i % 8) + 1,
      num: `NUM-${2000 + i}`,
      len: `${60 + (i % 40)}`,
      ton: `${1500 + (i % 10) * 250}`,
      payer: companies[(i + 1) % companies.length],
      port_area: ports[i % ports.length],
      type: i % 2 === 0 ? 'Nội địa' : 'Quốc tế',
      enabled: i % 2 === 0,
      '1-10': `${(i % 10) + 1}`,
      '11-20': `${11 + (i % 10)}`,
    });
  }
  return rows;
})();

export default [
  {
    url: `${baseUrl}/vxe/getData`,
    timeout: 200,
    method: 'get',
    response: ({ query }) => {
      const { pageNo = 1, pageSize = 10 } = query;
      return resultPageSuccess(Number(pageNo), Number(pageSize), vxeRows);
    },
  },
  {
    url: `${baseUrl}/vxe/immediateSaveRow`,
    timeout: 200,
    method: 'put',
    response: () => {
      return resultSuccess({});
    },
  },
  {
    url: `${baseUrl}/vxe/immediateSaveAll`,
    timeout: 200,
    method: 'post',
    response: () => {
      return resultSuccess({});
    },
  },
] as MockMethod[];
