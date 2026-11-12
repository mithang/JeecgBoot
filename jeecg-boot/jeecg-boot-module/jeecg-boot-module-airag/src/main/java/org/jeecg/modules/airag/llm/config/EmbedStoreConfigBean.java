package org.jeecg.modules.airag.llm.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình kho lưu trữ vector
 *
 * @Author: chenrui
 * @Date: 2025/2/18 14:24
 */
@NoArgsConstructor
@Data
@Component
@ConfigurationProperties(prefix = EmbedStoreConfigBean.PREFIX)
public class EmbedStoreConfigBean {
    public static final String PREFIX = "jeecg.airag.embed-store";

    /**
     * host
     */
    private String host = "127.0.0.1";
    /**
     * Cổng
     */
    private int port = 5432;
    /**
     * Cơ sở dữ liệu
     */
    private String database = "postgres";
    /**
     * Tên người dùng
     */
    private String user = "postgres";
    /**
     * Mật khẩu
     */
    private String password = "postgres";

    /**
     * Bảng lưu trữ vector
     */
    private String table = "embeddings";

}
