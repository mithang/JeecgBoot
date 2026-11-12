package org.jeecg.loader.vo;

import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * Tùy chỉnh RouteDefinition
 * @author zyf
 */
public class MyRouteDefinition extends RouteDefinition {
    /**
     * Trạng thái route
     */
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
