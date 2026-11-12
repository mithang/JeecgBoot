package org.jeecg.modules.airag.llm.dto;

import lombok.Data;

/**
 * Lưu công cụ plugin DTO
 * fro [QQYUN-12453]【AI】Hỗ trợ plugin
 * @author chenrui
 * @date 2025/10/30
 */
@Data
public class SaveToolsDTO {
    /**
     * Plugin ID
     */
    private String id;

    /**
     * Chuỗi JSON danh sách công cụ
     */
    private String tools;
}

