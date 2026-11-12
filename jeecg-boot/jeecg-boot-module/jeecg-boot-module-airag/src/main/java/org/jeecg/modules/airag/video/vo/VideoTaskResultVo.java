package org.jeecg.modules.airag.video.vo;

import lombok.Data;

/**
 * AI Kết quả nhiệm vụ tạo video VO
 */
@Data
public class VideoTaskResultVo {
    /**
     * Nhiệm vụ ID
     */
    private String taskId;

    /**
     * Trạng thái nhiệm vụ: PROCESSING, SUCCESS, FAIL
     */
    private String status;

    /**
     * URL tải video (có giá trị khi SUCCESS)
     */
    private String videoUrl;

    /**
     * URL ảnh bìa video (nếu có)
     */
    private String coverUrl;

    /**
     * Video có lồng tiếng URL
     */
    private String voiceoverVideoUrl;

    /**
     * Nội dung lời dẫn
     */
    private String narration;

    /**
     * Thông tin lỗi (có giá trị khi FAIL)
     */
    private String message;
}
