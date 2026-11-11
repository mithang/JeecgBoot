package org.jeecg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
* @Description: Mẫu SMS
*
* @author: wangshuai
* @date: 2024/11/5 Buổi chiều 3:44
*/
@Data
@Component("jeecgSmsTemplateConfig")
@ConfigurationProperties(prefix = "jeecg.oss.sms-template")
public class JeecgSmsTemplateConfig {

    /**
     * Chữ ký SMS
     */
    private String signature;


    /**
     * Mẫu SMS code
     *
     * @return
     */
    private Map<String,String> templateCode;
    
}
