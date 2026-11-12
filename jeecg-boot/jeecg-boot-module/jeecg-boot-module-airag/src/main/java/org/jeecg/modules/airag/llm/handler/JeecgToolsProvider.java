package org.jeecg.modules.airag.llm.handler;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import lombok.Getter;

import java.util.Map;

/**
 * for [QQYUN-13565]【Trợ lý AI】Mở rộng công cụ tạo người dùng và truy vấn người dùng mới
 * @Description: jeecg llm Nhà cung cấp công cụ
 * @Author: chenrui
 * @Date: 2025/8/26 18:06
 */
public interface JeecgToolsProvider {

    /**
     * Lấy danh sách công cụ mặc định
     * @return
     * @author chenrui
     * @date 2025/8/27 09:49
     */
    public Map<ToolSpecification, ToolExecutor> getDefaultTools();

    /**
     * jeecgLlm Lớp công cụ
     * @author chenrui
     * @date 2025/8/27 09:49
     */
    @Getter
    class JeecgLlmTools{
        ToolSpecification toolSpecification;
        ToolExecutor toolExecutor;

        public JeecgLlmTools(ToolSpecification toolSpecification, ToolExecutor toolExecutor) {
            this.toolSpecification = toolSpecification;
            this.toolExecutor = toolExecutor;
        }


    }
}
