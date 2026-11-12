package org.jeecg.modules.airag.video.vo;

import lombok.Data;

/**
 * AI Yêu cầu tạo video VO
 */
@Data
public class VideoGenerateVo {
    /**
     * Prompt mô tả video
     */
    private String prompt;

    /**
     * Phân loại cảnh (Demo chung/Marketing sản phẩm/Đào tạo giáo dục/Thiết kế sáng tạo)
     */
    private String category;

    /**
     * ID nhiệm vụ video (dùng để truyền vào khi thêm lồng tiếng)
     */
    private String taskId;

    /**
     * Kích thước video, ví dụ "1920x1080"、"720x480"
     */
    private String size;

    /**
     * Tốc độ khung hình video
     */
    private Integer fps;

    /**
     * Thời lượng video (giây)
     */
    private Integer duration;

    /**
     * Có phải hiệu ứng âm thanh do AI tổng hợp không
     */
    private Integer izAiAudio;
}
