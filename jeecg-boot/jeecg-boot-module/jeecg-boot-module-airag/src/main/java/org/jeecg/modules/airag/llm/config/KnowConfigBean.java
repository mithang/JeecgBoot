package org.jeecg.modules.airag.llm.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình kho tri thức
 *
 * @Author: chenrui
 * @Date: 2025-04-01 14:19
 */
@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = KnowConfigBean.PREFIX)
public class KnowConfigBean {
    public static final String PREFIX = "jeecg.airag.know";

    /**
     * Bật phân tích MinerU
     */
    private boolean enableMinerU = false;

    /**
     * conda môi trường(Mặc định không sử dụng conda)
     */
    private String condaEnv = null;

}
