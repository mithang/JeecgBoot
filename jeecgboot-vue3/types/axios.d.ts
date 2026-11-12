export type ErrorMessageMode = 'none' | 'modal' | 'message' | undefined;
export type SuccessMessageMode = 'none' | 'success' | 'error' | undefined;

export interface RequestOptions {
  // Nối tham số request vào url
  joinParamsToUrl?: boolean;
  // Định dạng thời gian của tham số request
  formatDate?: boolean;
  // Có xử lý kết quả request không
  isTransformResponse?: boolean;
  // Có trả về response header gốc không; dùng thuộc tính này khi cần lấy response header
  isReturnNativeResponse?: boolean;
  // Mặc định thêm prefix vào url
  joinPrefix?: boolean;
  // Địa chỉ API, nếu để trống sẽ dùng giá trị mặc định
  apiUrl?: string;
  // Đường dẫn nối vào request
  urlPrefix?: string;
  // Kiểu nhắc thông báo lỗi
  errorMessageMode?: ErrorMessageMode;
  // Kiểu nhắc thông báo thành công
  successMessageMode?: SuccessMessageMode;
  // Có thêm dấu thời gian không
  joinTime?: boolean;
  ignoreCancelToken?: boolean;
  // Có gửi token trong header không
  withToken?: boolean;
}

export interface Result<T = any> {
  code: number;
  type: 'success' | 'error' | 'warning';
  message: string;
  result: T;
}

// Tham số tải tệp lên
export interface UploadFileParams {
  // Tham số khác
  data?: Recordable;
  // Tên trường của tham số tệp trong API
  name?: string;
  // Tệp
  file: File | Blob;
  // Tên tệp
  filename?: string;
  [key: string]: any;
}
// Tham số trả về của tệp
export interface UploadFileCallBack {
  // Phương thức callback khi thành công
  success?: any;
  // Có trả về response header không; dùng thuộc tính này khi cần lấy response header
  isReturnResponse?: boolean;
}
