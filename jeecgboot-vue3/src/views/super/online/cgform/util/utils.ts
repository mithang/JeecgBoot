import {ExtConfigDefaultJson} from "../cgform.data";

// Khởi tạo mở rộngJSON
export function parseExtConfigJson(record: Recordable) {
  // Phân tích mở rộngJSON
  let parseJSON = {};
  if (record.extConfigJson) {
    try {
      parseJSON = JSON.parse(record.extConfigJson);
    } catch (e) {
      console.error('onlineMở rộngJSONChuyển đổi thất bại:', e);
    }
  }
  // Lấy giá trị từ cơ sở dữ liệu, đồng thời hợp nhất
  return Object.assign({}, ExtConfigDefaultJson, parseJSON, {
    isDesForm: record.isDesForm || 'N',
    desFormCode: record.desFormCode || '',
  });
}
