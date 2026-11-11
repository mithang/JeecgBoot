package org.jeecg.common.system.annotation;

import java.lang.annotation.*;

/**
 * Chuyển lớp enum thành dữ liệu từ điển
 * 
 * <<Hướng dẫn sử dụng>>
 * 1. Lớp enum cần kết thúc bằng `Enum` , và thêm trên lớp `@EnumDict` annotation.
 * 2. Cần thủ công thêm đường dẫn package chứa lớp enum** Thêm vào `org.jeecg.common.system.util.ResourceUtil.BASE_SCAN_PACKAGES` vào trong mảng cấu hình.
 * 
 * @Author taoYan
 * @Date 2022/7/8 10:34
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnumDict {

    /**
     * Làm mã duy nhất của dữ liệu từ điển
     */
    String value() default "";
}
