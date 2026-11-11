/**
 * Loại component JInput
 */
export enum JInputTypeEnum {
  // Khớp mờ
  JINPUT_QUERY_LIKE = 'like',
  // Khác (không bằng)
  JINPUT_QUERY_NE = 'ne',
  // Lớn hơn hoặc bằng
  JINPUT_QUERY_GE = 'ge',
  // Nhỏ hơn hoặc bằng
  JINPUT_QUERY_LE = 'le',
}

/**
 * Định nghĩa hằng số cần cho trình thiết kế panel
 */
export enum JDragConfigEnum {
  //baseURL
  DRAG_BASE_URL = 'drag-base-url',
  // Tiền tố cache kéo thả
  DRAG_CACHE_PREFIX = 'drag-cache:',
}
// Enum electron
export enum ElectronEnum {
  ELECTRON_API = '_ELECTRON_PRELOAD_UTILS_',
}
