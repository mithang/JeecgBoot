package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Description: Tham số đầu vào gửi tin nhắn
 * @Author: chenrui
 * @Date: 2025/2/25 11:47
 */
@NoArgsConstructor
@Data
public class ChatSendParams {

    public ChatSendParams(String content, String conversationId, String topicId, String appId) {
        this.content = content;
        this.conversationId = conversationId;
        this.topicId = topicId;
        this.appId = appId;
    }

    /**
     * Nội dung trò chuyện do người dùng nhập
     */
    private String content;

    /**
     * Phiên hội thoại ID
     */
    private String conversationId;

    /**
     * ID chủ đề hội thoại (dùng để liên kết lịch sử)
     */
    private String topicId;

    /**
     * Ứng dụng id
     */
    private String appId;

    /**
     * Token chia sẻ (bắt buộc đối với truy cập ẩn danh)
     *
     * @author scott
     * @since 2026-07-21 【issues/9787】Gửi ẩn danh mang theo token chia sẻ
     */
    private String shareToken;

    /**
     * Danh sách hình ảnh
     */
    private List<String> images;

    /**
     * Danh sách tệp
     */
    private List<String> files;

    /**
     * Cấu hình tham số đầu vào bổ sung của quy trình làm việc
     * key: Tham số field, value: Giá trị tham số
     * for [issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh
     */
    private Map<String, Object> flowInputs;

    /**
     * Có bật tìm kiếm mạng hay không (chỉ mô hình Qwen hỗ trợ)
     */
    private Boolean enableSearch;

    /**
     * Có bật suy nghĩ sâu hay không
     */
    private Boolean enableThink;

    /**
     * Loại phiên: portal Cổng ứng dụng
     */
    private String sessionType;

    /**
     * Có bật tạo hình vẽ hay không
     */
    private Boolean enableDraw;

    /**
     * của mô hình vẽ tranh id
     */
    private String drawModelId;

    /**
     * Kích thước hình ảnh
     */
    private String imageSize;

    /**
     * Một hình ảnh
     */
    private String imageUrl;

    /**
     * Có lưu phiên không
     */
    private Boolean izSaveSession;

}
