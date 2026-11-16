// Phase 4 Step 7 (G5) — helper dùng chung cho Dashboard & Workbench.
// Bảng màu biểu đồ theo thương hiệu "Xanh Jeecg" (Step 3), thay cho palette
// mặc định của ECharts (#5ab1ef/#b6a2de/#67e0e3/#2ec7c9) và các màu random
// rải rác ở dashboard. Chỉ đổi trình bày, không đổi dữ liệu hay API.

/** Bảng màu biểu đồ thương hiệu: xanh chính, xanh sáng, xanh nhạt, cam, xanh lá, vàng. */
export const BRAND_CHART_COLORS = ['#0B57D0', '#2E6FE8', '#5B8DEF', '#F4502A', '#34A853', '#F5A623'];

/** Xanh lá phụ cho chuỗi thứ hai (vd: lượt truy cập) — đi cùng xanh chính. */
export const BRAND_CHART_GREEN = '#34A853';

/** Cam phụ cho chuỗi tương phản (vd: chuỗi thứ hai của biểu đồ đa chuỗi). */
export const BRAND_CHART_ORANGE = '#F4502A';

/** Lời chào theo giờ trong ngày (giờ địa phương của trình duyệt). */
export function getTimeGreeting(date = new Date()): string {
  const hour = date.getHours();
  if (hour >= 5 && hour < 11) return 'Chào buổi sáng';
  if (hour >= 11 && hour < 13) return 'Chào buổi trưa';
  if (hour >= 13 && hour < 18) return 'Chào buổi chiều';
  return 'Chào buổi tối';
}

/** Ngày hiện tại dạng tiếng Việt, vd: "Thứ Hai, ngày 16 tháng 11 năm 2026". */
export function formatVietnameseDate(date = new Date()): string {
  const weekday = new Intl.DateTimeFormat('vi-VN', { weekday: 'long' }).format(date);
  const day = date.getDate();
  const month = date.getMonth() + 1;
  const year = date.getFullYear();
  const weekdayText = weekday.charAt(0).toUpperCase() + weekday.slice(1);
  return `${weekdayText}, ngày ${day} tháng ${month} năm ${year}`;
}
