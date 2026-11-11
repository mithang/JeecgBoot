/**
 * bởi vìonlineRender danh sách dùng chung mộtlist Vì vậy một số cấu hình thuộc tính riêng biệt Cần đặt riêng vàomaplưu trữ trong đó Tránh xung đột khi chuyển route
 */
interface SpecialConfig {
  // Trường sắp xếp
  sortField: string;
  // Cách sắp xếp
  sortType: 'asc' | 'desc';
  // Số trang hiện tại
  currentPage: number;
  // Số lượng mỗi trang hiện tại
  pageSize: number;
  // Tổng số trang
  total: number;
  // Hàng đã chọnkey
  selectedRowKeys: string[];
  // Điều kiện truy vấn
  queryParam: object;
  // hrefChuyển tớionlineTrang danh sách sẽ mang theo tham số
  acceptHrefParams: object;
  // Route có bị lưu bộ nhớ đệm hay không
  cache: boolean;
  // Mô tả bảng
  description: string;
  // Tên bảng
  currentTableName: string;
  // Có kích hoạt biểu mẫu của trình thiết kế biểu mẫu hay không
  isDesForm: boolean;
  // Mã biểu mẫu của trình thiết kế biểu mẫu
  desFormCode: string;
  isTree: boolean;
  hasChildrenField?: string;
}

interface Page {
  current?: number;
  pageSize?: number;
  pageSizeOptions?: string[];
  showTotal?: Function;
  showQuickJumper?: boolean;
  showSizeChanger?: boolean;
  total?: number;
}

interface CgFormButton {
  buttonCode?: string;
  buttonName?: string;
  buttonStyle?: string;
  optType?: 'js' | 'bus';
  exp?: string;
  buttonIcon?: string;
}

/***
 * Cấu hình mở rộng của trang biểu mẫu
 */
interface ExtendConfig {
  modalMinWidth: number;
}

/***
 * Kết quả phân tích cấu hình mở rộng của trường biểu mẫu
 */
interface FieldExtends {
  //Số lượng tải lên
  uploadnum?: number | string;

  //Giới hạn độ dài trưng bày của văn bản lớn ở trang danh sách
  showLength?: number | string;

  //popupCó hỗ trợ chọn nhiều hay không
  popupMulti?: boolean;

  //Tìm kiếm dropdown có hỗ trợ chọn nhiều hay không
  multiple?: boolean;

  //Component phòng ban, người dùng Tên trường dùng để lưu trữ
  store?: string;

  //Component phòng ban, người dùng Tên trường dùng để hiển thị
  text?: string;

  //Component phòng ban, người dùng Có chọn nhiều hay không
  multiSelect?: boolean;

  //Quy tắc sắp xếp truy vấn
  orderRule?: 'asc' | 'desc';
  
  // Phong cách hiển thị bản ghi liên kết card/select
  showType?: string;
  // Hình bìa của bản ghi liên kết
  imageField?: string;
  // labelĐộ dài
  labelLength?: number;
}

// onlineGửi biểu mẫu và cờ đánh dấu quy trình Mặc định chỉ gửi biểu mẫu
const SUBMIT_FLOW_KEY = 'jeecg_submit_form_and_flow';
// Sau khi gửi biểu mẫu thành công, trả lại của dữ liệu biểu mẫuid keyDùng để khi gửi quy trình, lấyid
const SUBMIT_FLOW_ID = 'flow_submit_id';
// Sau khi gửi biểu mẫu thành công tạiformDatathêm thuộc tính thiết lập tên bảng trong đó
const ONL_FORM_TABLE_NAME = 'online_form_table_name';
// Kiểm chứng thất bại
const VALIDATE_FAILED = 'validate-failed';

/*
 * Kiểu dáng của biểu mẫu truy vấn-label
 * */
const ONL_QUERY_LABEL_COL = { xs: { span: 24 }, sm: { span: 6 } };

/*
 * Kiểu dáng của biểu mẫu truy vấn-wrapper
 * */
const ONL_QUERY_WRAPPER_COL = { xs: { span: 24 }, sm: { span: 18 } };

/**setup*/
const SETUP = 'setup';

/**EnhanceJS*/
const ENHANCEJS = 'EnhanceJS';

/**
 * Chuyển loại biểu mẫu thành loại truy vấn
 * Sự khác biệt giữa component truy vấn thông thường và truy vấn nâng cao : truy vấn nâng cao không hỗ trợ component liên động
 */
const FORM_VIEW_TO_QUERY_VIEW = {
  password: 'text',
  file: 'text',
  image: 'text',
  textarea: 'text',
  umeditor: 'text',
  markdown: 'text',
  checkbox: 'list_multi',
  radio: 'list',
};

/**Component dropdownPopupContainerBộ chọn lớp (class selector)*/
const POP_CONTAINER = '.jeecg-online-modal .ant-modal-content';

/**onlineTiền tố quyền-Hiện chủ yếu dùng cho bảng conbutton*/
const ONL_AUTH_PRE = 'online_';

export {
  SpecialConfig,
  Page,
  CgFormButton,
  ExtendConfig,
  FieldExtends,
  SUBMIT_FLOW_KEY,
  SUBMIT_FLOW_ID,
  VALIDATE_FAILED,
  ONL_QUERY_LABEL_COL,
  ONL_QUERY_WRAPPER_COL,
  SETUP,
  ENHANCEJS,
  FORM_VIEW_TO_QUERY_VIEW,
  POP_CONTAINER,
  ONL_AUTH_PRE,
  ONL_FORM_TABLE_NAME
};
