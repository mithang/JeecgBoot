package org.jeecg.config.shiro;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation miễn xác thực Token
 * 
 * Hệ thống xác thực kết hợp với@RequestMapping Lấy đường dẫn yêu cầu để cấu hình miễn đăng nhập
 * @author eightmonth
 * @date 2024/2/28 9:58
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreAuth {
}
