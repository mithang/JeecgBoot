package org.jeecg.config.vo;

import lombok.Data;

/**
 * @Author taoYan
 * @Date 2022/7/5 21:16
 **/
@Data
public class DomainUrl {

    private String pc;

    private String app;

    /**
     * Backend tự proxy baseUrl: Dùng khi chuyển tiếp originUrl đường dẫn tương đối,
     * Giải quyết Docker / K8s NodePort / Vấn đề cổng vào và cổng lắng nghe không nhất quán trong reverse proxy.
     */
    private String back;
}
