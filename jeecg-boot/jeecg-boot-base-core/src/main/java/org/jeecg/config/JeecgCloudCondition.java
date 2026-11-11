package org.jeecg.config;

import org.jeecg.common.constant.CommonConstant;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Điều kiện tải môi trường microservice
 * @author: jeecg-boot
 */
public class JeecgCloudCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Object object = context.getEnvironment().getProperty(CommonConstant.CLOUD_SERVER_KEY);
        //Nếu không có cấu hình đăng ký và phát hiện dịch vụ thì là ứng dụng đơn khối (monolith)
        if(object==null){
            return false;
        }
        return true;
    }
}
