package org.jeecg.config;

import org.jeecg.common.constant.CommonConstant;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Điều kiện tải cấu hình cross-domain
 * @author: jeecg-boot
 */
public class CorsFilterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Object object = context.getEnvironment().getProperty(CommonConstant.CLOUD_SERVER_KEY);
        //Nếu không có cấu hình đăng ký và phát hiện dịch vụ thì chứng tỏ là ứng dụng đơn thể, lúc đó tải cấu hình cross-domain và trả về true
        if(object==null){
            return true;
        }
        return false;
    }
}
