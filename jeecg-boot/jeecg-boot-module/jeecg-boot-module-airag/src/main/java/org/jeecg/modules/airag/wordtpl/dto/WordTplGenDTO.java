package org.jeecg.modules.airag.wordtpl.dto;

import lombok.Data;

import java.util.Map;

/**
 * word Tham số đầu vào tạo mẫu
 * @author chenrui
 * @date 2025/7/10 14:38
 */
@Data
public class WordTplGenDTO {

    /**
     * Mẫu id
     */
    String templateId;


    /**
     * Mẫu code
     */
    String templateCode;

    /**
     * Dữ liệu
     */
    Map<String,Object> data;

}
