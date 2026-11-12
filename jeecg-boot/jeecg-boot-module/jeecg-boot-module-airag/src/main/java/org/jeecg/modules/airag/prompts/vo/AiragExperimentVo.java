package org.jeecg.modules.airag.prompts.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Description: AiragExperimentVo
 * @Author: jeecg-boot
 * @Date:   2025-12-12
 * @Version: V1.0
 */
@Data
public class AiragExperimentVo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * Prompt Id
     */
    private String promptKey;
    /**
     * Nội dung nhập
     */
    private String extDataId;
    /**
     * Quan hệ ánh xạ
     */
    private Map<String,String> mappings;
}
