package org.jeecg.config.vo;

import lombok.Data;

@Data
public class WeiXinPay {
    /**
     * Tài khoản công khai WeChat id
     */
    private String appId;
    /**
     * Mã thương nhân id
     */
    private String mchId;
    /**
     * Khóa bí mật mã thương nhân
     */
    private String apiKey;
    /**
     * Địa chỉ callback
     */
    private String notifyUrl;
    /**
     * Có bật xác thực thành viên hay không
     */
    private Boolean openVipLimit;
    /**
     * Đường dẫn chứng chỉ
     */
    private String certPath;
}
