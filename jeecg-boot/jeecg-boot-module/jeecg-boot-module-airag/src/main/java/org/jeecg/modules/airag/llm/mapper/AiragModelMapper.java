package org.jeecg.modules.airag.llm.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.airag.llm.entity.AiragModel;

/**
 * @Description: AiRag Cấu hình mô hình
 * @Author: jeecg-boot
 * @Date:   2025-02-14
 * @Version: V1.0
 */
public interface AiragModelMapper extends BaseMapper<AiragModel> {

    /**
     * Truy vấn thông tin mô hình theo ID(Bỏ qua tenant)
     * for [QQYUN-12113]Sau khi chia sẻ, trò chuyện, ứng dụng, mô hình và kho tri thức không truy vấn theo tenant
     * @param id
     * @return
     * @author chenrui
     * @date 2025/4/21 15:24
     */
    @InterceptorIgnore(tenantLine = "true")
    AiragModel getByIdIgnoreTenant(String id);
}
