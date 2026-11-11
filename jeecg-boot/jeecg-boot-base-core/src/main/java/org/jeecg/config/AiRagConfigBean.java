package org.jeecg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ai Lớp cấu hình; các cấu hình chung có thể đặt vào đây
 *
 * @Author: wangshuai
 * @Date: 2025/12/17 14:00
 */
@Data
@Component
@ConfigurationProperties(prefix = AiRagConfigBean.PREFIX)
public class AiRagConfigBean {
    public static final String PREFIX = "jeecg.airag";
    
    /**
     * Nút nhạy cảm
     * stdio mpc Bật tính năng dòng lệnh, sql: bật nút SQL của quy trình AI
     */
    private String allowSensitiveNodes = "";

    //update-begin---author:wangshuai ---date:2026-04-15  for：Di chuyển cấu hình Brave Search sang AiRagConfigBean, bỏ trường enabled, apiKey trống tức là không bật-----------
    /**
     * Brave Search Cấu hình tìm kiếm liên mạng
     */
    private BraveSearchConfig braveSearch = new BraveSearchConfig();

    @Data
    public static class BraveSearchConfig {
        /** Brave Search API Key; khi để trống thì tìm kiếm liên mạng không có hiệu lực */
        private String apiKey;
        /** API Endpoint, mặc định là địa chỉ chính thức */
        private String endpoint = "https://api.search.brave.com/res/v1/web/search";
        /** Số kết quả trả về mặc định, tối đa  20 */
        private Integer count = 10;
        /** Số giây timeout của yêu cầu */
        private Integer timeout = 15;
        /**
         * Thời gian lưu cache kết quả tìm kiếm (phút).
         * Khi lớn hơn 0 thì bật cache; truy vấn có cùng tham số sẽ trả về trực tiếp kết quả cache mà không gọi lại API.
         * Đặt thành 0 hoặc không cấu hình thì tắt cache.
         */
        private Integer cacheExpireMinutes = 60;
    }
    //update-end---author:wangshuai ---date:2026-04-15  for：Di chuyển cấu hình Brave Search sang AiRagConfigBean, bỏ trường enabled, apiKey trống tức là không bật-----------
}
