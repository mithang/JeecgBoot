package org.jeecg.modules.airag.llm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;

import java.util.Map;

/**
 * AIRag Kho tri thức
 *
 * @Author: jeecg-boot
 * @Date: 2025-02-18
 * @Version: V1.0
 */
public interface IAiragKnowledgeService extends IService<AiragKnowledge> {
    
    /**
     * Xây dựng công cụ của kho tri thức
     * 
     * @param memoryId
     * @return Map<String, Object>
     */
    Map<String, Object> getPluginMemory(String memoryId);
}
