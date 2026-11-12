package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.airag.llm.entity.AiragKnowledge;

/**
 * @Description: AIRag Kho tri thức
 * @Author: jeecg-boot
 * @Date:   2025-02-18
 * @Version: V1.0
 */
public interface AiragKnowledgeMapper extends BaseMapper<AiragKnowledge> {

    /**
     * Truy vấn thông tin kho tri thức theo ID(Bỏ qua tenant)
     * for [QQYUN-12113]Sau khi chia sẻ, trò chuyện, ứng dụng, mô hình và kho tri thức không truy vấn theo tenant
     * @param id
     * @return
     * @author chenrui
     * @date 2025/4/21 15:24
     */
    @InterceptorIgnore(tenantLine = "true")
    AiragKnowledge getByIdIgnoreTenant(String id);
}
