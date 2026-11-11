package org.jeecg.config.init;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.config.JeecgCloudCondition;
import org.jeecg.modules.system.service.ISysGatewayRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @desc: Khởi động chương trình, khởi tạo cấu hình route
 * @author: flyme
 */
@Slf4j
@Component
@Lazy(false)
@Conditional(JeecgCloudCondition.class)
public class SystemInitListener implements ApplicationListener<ApplicationReadyEvent>, Ordered {


    @Autowired
    private ISysGatewayRouteService sysGatewayRouteService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

        log.info(" Dịch vụ đã khởi động, khởi tạo cấu hình route ###################");
        String context = "AnnotationConfigServletWebServerApplicationContext";
        if (applicationReadyEvent.getApplicationContext().getDisplayName().indexOf(context) > -1) {
            sysGatewayRouteService.addRoute2Redis(CacheConstant.GATEWAY_ROUTES);
        }

    }

    @Override
    public int getOrder() {
        return 1;
    }
}
