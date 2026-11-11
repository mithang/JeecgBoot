package org.jeecg.config;

import org.jeecg.config.vo.GaoDeApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * Cấu hình tài khoản Gaode
 */
@Lazy(false)
@Configuration("jeecgGaodeBaseConfig")
@ConfigurationProperties(prefix = "jeecg.jmreport")
public class JeecgGaodeBaseConfig {

    /**
     * Cấu hình API mở Gaode
     */
    private GaoDeApi gaoDeApi;

    public GaoDeApi getGaoDeApi() {
        return gaoDeApi;
    }

    public void setGaoDeApi(GaoDeApi gaoDeApi) {
        this.gaoDeApi = gaoDeApi;
    }
    
}
