package org.jeecg.modules.airag.voice.vo;

import lombok.Data;

/**
 * Yêu cầu chuyển văn bản thành giọng nói VO
 */
@Data
public class VoiceGenerateVo {
    /**
     * Văn bản cần tổng hợp (bắt buộc)
     */
    private String content;
    /**
     * Âm sắc, nếu không truyền sẽ dùng giá trị mặc định trong yml
     */
    private String voice;
    /**
     * Tốc độ phát, phạm vi 0.25~4.0
     */
    private Double speed;
    /**
     * Tăng âm lượng(dB)
     */
    private Double volume;
}
