package org.jeecg.config.tencent;

import lombok.Data;

/**
* @Description: Cấu hình SMS Tencent
*
* @author: wangshuai
* @date: 2025/10/30 18:22
*/
@Data
public class JeecgTencent {

    /**
     * Tên miền truy cập
     */
    private String endpoint;
    
    /**
     * api Khóa bí mật id
     */
    private String secretId;
    
    /**
     * api Khóa bí mật key
     */
    private String secretKey;
    
    /**
     * Ứng dụng id
     */
    private String sdkAppId;

    /**
     * Thông tin khu vực
     */
    private String region;
}
