/**
 * Trích xuất văn bản lỗi nghiệp vụ trong phản hồi của giao diện trò chuyện AI.
 *
 * @param payload param payload phản hồi Result thông thường hoặc sự kiện SSE
 * @return văn bản lỗi; phản hồi không phải lỗi thì trả về chuỗi trống
 * @author scott
 * @since 2026-07-21 【issues/9787】Gợi ý lỗi liên kết chia sẻ không hợp lệ
 */
export function getChatErrorMessage(payload: any): string {
  if (!payload) {
    return '';
  }
  if (payload.success === false) {
    return payload.message || 'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!';
  }
  if (payload.event === 'ERROR') {
    return payload.data?.message || 'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!';
  }
  if (payload.event === 'FLOW_FINISHED' && payload.data?.success === false) {
    return payload.data?.message || 'Yêu cầu xảy ra lỗi, vui lòng thử lại sau!';
  }
  return '';
}
