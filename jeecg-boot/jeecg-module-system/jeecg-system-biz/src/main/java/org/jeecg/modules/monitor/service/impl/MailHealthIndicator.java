package org.jeecg.modules.monitor.service.impl;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Mô tả chức năng:Kiểm tra email tùy chỉnh
 *
 * @author: 李波
 * @email: 503378406@qq.com
 * @date: 2019-06-29
 */
@Component
public class MailHealthIndicator implements HealthIndicator {


    @Override public Health health() {
        int errorCode = check();
        if (errorCode != 0) {
            return Health.down().withDetail("Error Code", errorCode) .build();
        }
        return Health.up().build();
    }
    int check(){
        //Có thể triển khai logic kiểm tra cơ sở dữ liệu tùy chỉnh
        return 0;
    }
}
