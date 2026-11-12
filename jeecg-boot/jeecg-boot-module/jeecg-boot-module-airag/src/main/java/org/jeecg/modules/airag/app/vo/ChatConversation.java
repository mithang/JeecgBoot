package org.jeecg.modules.airag.app.vo;

import lombok.Data;
import org.jeecg.modules.airag.app.entity.AiragApp;
import org.jeecg.modules.airag.common.vo.MessageHistory;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: Phiên trò chuyện
 * @Author: chenrui
 * @Date: 2025/2/25 14:56
 */
@Data
public class ChatConversation {

    /**
     * Phiên id
     */
    private String id;

    /**
     * Tiêu đề phiên
     */
    private String title;

    /**
     * Bản ghi tin nhắn
     */
    private List<MessageHistory> messages;

    /**
     * app
     */
    private AiragApp app;

    /**
     * Thời gian tạo
     */
    private Date createTime;

    /**
     * Cấu hình tham số đầu vào quy trình (cài đặt tham số bổ sung của quy trình làm việc)
     * key: Tham số field, value: Giá trị tham số
     * for [issues/8545]Khi tạo ứng dụng AI mới, chỉ có thể chọn quy trình AI không có tham số tùy chỉnh
     */
    private Map<String, Object> flowInputs;

    /**
     * portal Cổng ứng dụng
     */
    private String sessionType;

    /**
     * Có lưu phiên không
     */
    private Boolean izSaveSession;
}
