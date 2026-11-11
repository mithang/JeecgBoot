package org.jeecg.config.vo;

import lombok.Data;

/**
* @Description: Cấu hình API mở Gaode
*
* @author: wangshuai
* @date: 2025/7/17 20:32
*/
@Data
public class GaoDeApi {
    /**Ứng dụng key*/
    private String apiKey;
    /**Khóa bí mật ứng dụng*/
    private String secretKey;
}
