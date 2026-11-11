package org.jeecg.common.desensitization.annotation;


import org.jeecg.common.desensitization.enums.SensitiveEnum;

import java.lang.annotation.*;

/**
 * Định nghĩa trên trường, đánh dấu thông tin mà trường lưu trữ là nhạy cảm
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SensitiveField {

    /**
     * Các loại khác nhau xử lý khác nhau
     * @return
     */
    SensitiveEnum type() default SensitiveEnum.ENCODE;
}
