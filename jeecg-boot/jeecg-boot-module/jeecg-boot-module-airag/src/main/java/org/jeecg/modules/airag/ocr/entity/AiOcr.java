package org.jeecg.modules.airag.ocr.entity;

import lombok.Data;

/**
* @Description: OCR Lớp thực thể nhận dạng
*
* @author: wangshuai
* @date: 2025/4/16 17:01
*/
@Data
public class AiOcr {

    /**
     * Mã
     */
    private String id;
    
    /**
     * Tiêu đề
     */
    private String title;
    
    /**
     * Prompt
     */
    private String prompt;
    
}
