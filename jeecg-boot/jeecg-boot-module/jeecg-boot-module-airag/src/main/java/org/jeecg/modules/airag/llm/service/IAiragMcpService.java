package org.jeecg.modules.airag.llm.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.airag.llm.entity.AiragMcp;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: MCP
 * @Author: jeecg-boot
 * @Date:   2025-10-20
 * @Version: V1.0
 */
public interface IAiragMcpService extends IService<AiragMcp> {

    Result<String> edit(AiragMcp airagMcp);

    Result<?> sync(String id);


    Result<?> toggleStatus(String id, String action);

    /**
     * Lưu công cụ plugin (chỉ cập nhật trường tools)
     * for [QQYUN-12453]【AI】Hỗ trợ plugin
     * @param id Plugin ID
     * @param tools Chuỗi JSON danh sách công cụ
     * @return Kết quả thao tác
     * @author chenrui
     * @date 2025/10/30
     */
    Result<String> saveTools(String id, String tools);
}
