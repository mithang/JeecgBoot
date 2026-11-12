package org.jeecg.modules.airag.app.vo;

import lombok.Data;

/**
* @Description: ai Lớp thực thể tạo nội dung viết
*
* @author: wangshuai
* @date: 2026/1/12 15:59
*/
@Data
public class AiWriteGenerateVo {

    /**
     * Loại viết
     */
    private String activeMode;

    /**
     * Gợi ý nội dung viết
     */
    private String prompt;

    /**
     * Văn bản gốc
     */
    private String originalContent;

    /**
     * Độ dài
     */
    private String length;

    /**
     * Định dạng
     */
    private String format;

    /**
     * Giọng điệu
     */
    private String tone;

    /**
     * Ngôn ngữ
     */
    private String language;
}
