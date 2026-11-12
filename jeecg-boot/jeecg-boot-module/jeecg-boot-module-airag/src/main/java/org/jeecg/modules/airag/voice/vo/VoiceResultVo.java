package org.jeecg.modules.airag.voice.vo;

import lombok.Data;

/**
 * Phản hồi chuyển văn bản thành giọng nói VO
 */
@Data
public class VoiceResultVo {
    /**
     * Đường dẫn tương đối của tệp âm thanh đã tạo
     */
    private String voiceUrl;
    /**
     * Tên tệp
     */
    private String fileName;
}
