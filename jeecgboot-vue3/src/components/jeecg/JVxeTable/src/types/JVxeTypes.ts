/** Loại component */
export enum JVxeTypes {
  // Cột số hàng
  rowNumber = 'row-number',
  // Chọn cột
  rowCheckbox = 'row-checkbox',
  // Cột chọn đơn
  rowRadio = 'row-radio',
  // Cột mở rộng
  rowExpand = 'row-expand',
  // Sắp xếp lên/xuống
  rowDragSort = 'row-drag-sort',

  input = 'input',
  inputNumber = 'input-number',
  textarea = 'textarea',
  select = 'select',
  date = 'date',
  datetime = 'datetime',
  time = 'time',
  checkbox = 'checkbox',
  upload = 'upload',
  // Tìm kiếm trong dropdown
  selectSearch = 'select-search',
  // Danh sách thả xuống chọn nhiều
  selectMultiple = 'select-multiple',
  // Thanh tiến độ
  progress = 'progress',
  //Chọn phòng ban
  departSelect = 'depart-select',
  //Chọn người dùng
  userSelect = 'user-select',

  // Tags kéo thả (tạm thời chưa dùng)
  tags = 'tags', // TODO Chưa triển khai

  slot = 'slot',
  normal = 'normal',
  hidden = 'hidden',

  // Dưới đây là các component tùy chỉnh
  popup = 'popup',
  // update-begin--author:liaozhiyang---date:20260507---for：【QQYUN-7961]Bảng con vxetable hỗ trợ danh mục popup
  popupDict = 'popupDict',
  // update-end--author:liaozhiyang---date:20260507---for：【QQYUN-7961]Bảng con vxetable hỗ trợ danh mục popup
  selectDictSearch = 'selectDictSearch',
  radio = 'radio',
  image = 'image',
  file = 'file',
  // Tỉnh/Thành/Quận huyện
  pca = 'pca',
  // Bản ghi liên kết
  linkTable = 'link-table',
  // update-begin--author:liaozhiyang---date:20260413---for:【issues/7633] Bảng con online hỗ trợ cây danh mục phân loại, cây tùy chỉnh
  // Control cây tùy chỉnh
  treeSelect = 'sel-tree',
  // Cây từ điển phân loại
  catTreeSelect = 'cat-tree',
  // update-end--author:liaozhiyang---date:20260413---for:【issues/7633] Bảng con online hỗ trợ cây danh mục phân loại, cây tùy chỉnh
}

// Để tránh xung đột với kiểu có sẵn của vxe nên thêm một tiền tố
// Tiền tố được tự động thêm, dùng trực tiếp trong mã là được (JVxeTypes.input）
export const JVxeTypePrefix = 'j-';

/** VxeTable Loại render */
export enum JVxeRenderType {
  editer = 'editer',
  spaner = 'spaner',
  default = 'default',
}
