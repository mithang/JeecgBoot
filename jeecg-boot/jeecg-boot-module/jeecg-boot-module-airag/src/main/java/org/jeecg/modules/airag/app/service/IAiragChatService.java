package org.jeecg.modules.airag.app.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.app.vo.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ai Trò chuyện
 *
 * @author chenrui
 * @date 2025/2/25 13:36
 */
public interface IAiragChatService {

    /**
     * Gửi tin nhắn
     *
     * @param chatSendParams
     * @return
     * @author chenrui
     * @date 2025/2/25 13:39
     */
    SseEmitter send(ChatSendParams chatSendParams);


    /**
     * Gỡ lỗi ứng dụng
     *
     * @param appDebugParams
     * @return
     * @author chenrui
     * @date 2025/2/28 10:49
     */
    SseEmitter debugApp(AppDebugParams appDebugParams);

    /**
     * Dừng phản hồi
     *
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/2/25 17:17
     */
    Result<?> stop(String requestId);

    /**
     * Lấy tất cả cuộc trò chuyện
     *
     * @param appId
     * @return
     * @author chenrui
     * @date 2025/2/26 14:48
     */
    Result<?> getConversations(String appId);

    /**
     * Lấy lịch sử trò chuyện của cuộc hội thoại
     *
     * @param conversationId
     * @param sessionType Loại
     * @return
     * @author chenrui
     * @date 2025/2/26 15:16
     */
    Result<?> getMessages(String conversationId, String sessionType);

    /**
     * Xóa phiên trò chuyện
     *
     * @param conversationId
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/3/3 16:55
     */
    Result<?> deleteConversation(String conversationId, String sessionType);

    /**
     * Cập nhật tiêu đề phiên
     * @param updateTitleParams
     * @return
     * @author chenrui
     * @date 2025/3/3 17:02
     */
    Result<?> updateConversationTitle(ChatConversation updateTitleParams);

    /**
     * Xóa sạch tin nhắn
     * @param conversationId
     * @param sessionType
     * @return
     * @author chenrui
     * @date 2025/3/3 19:49
     */
    Result<?> clearMessage(String conversationId, String sessionType);

    /**
     * Khởi tạo trò chuyện(Bỏ qua tenant)
     * [QQYUN-12113]Sau khi chia sẻ, trò chuyện, ứng dụng, mô hình và kho tri thức không truy vấn theo tenant
     * @param appId
     * @param shareToken Token chia sẻ (bắt buộc đối với truy cập ẩn danh)
     * @return
     * @author chenrui
     * @date 2025/4/21 14:17
     */
    Result<AiragAppShareInfoVO> initChat(String appId, String shareToken);

    /**
     * Xác thực truy cập chia sẻ ẩn danh (người dùng đã đăng nhập được cho qua trực tiếp).
     * Dùng cho các API ghi ẩn danh như upload, cùng send/init dùng chung một bộ quy tắc.
     *
     * @param appId Ứng dụng ID
     * @param shareToken Token chia sẻ
     * @author scott
     * @since 2026-07-21 【issues/9787】 Bổ sung xác thực token chia sẻ cho tải lên ẩn danh
     */
    void validateAnonymousShareAccess(String appId, String shareToken);

    /**
     * Tiếp tục nhận tin nhắn
     * @param requestId
     * @return
     * @author chenrui
     * @date 2025/8/11 17:39
     */
    SseEmitter receiveByRequestId(String requestId);

    /**
     * Lấy danh sách phiên theo loại
     * 
     * @param sessionType
     * @return
     */
    Result<?> getConversationsByType(String sessionType);

    /**
     * Tạo hình ảnh poster
     * @param aiDrawGenerateVo
     * @return
     */
    String genAiPoster(AiDrawGenerateVo aiDrawGenerateVo);

    //update-begin---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------
    /**
     * Tạo bất đồng bộ hình ảnh poster, trả về ngay lập tức taskId
     * @param aiDrawGenerateVo
     * @return taskId
     */
    String genAiPosterAsync(AiDrawGenerateVo aiDrawGenerateVo);

    /**
     * Truy vấn kết quả nhiệm vụ poster bất đồng bộ
     * @param taskId
     * @return Result, data là URL hình ảnh (thành công) hoặc status=pending/failed
     */
    Result<?> getAiPosterResult(String taskId);
    //update-end---author:wangshuai ---date:2026-04-15  for：【QQYUN-14568] Tạo áp phích AI chuyển sang bất đồng bộ, hỗ trợ lấy lại kết quả sau khi chuyển menu-----------

    /**
     * Tạo sáng tác AI
     *
     * @param chatSendParams
     * @return
     */
    SseEmitter genAiWriter(AiWriteGenerateVo chatSendParams);
}
