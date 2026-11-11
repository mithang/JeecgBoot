package org.jeecg.common.desensitization.annotation;


import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jeecg.common.desensitization.SensitiveSerialize;
import org.jeecg.common.desensitization.enums.SensitiveEnum;

import java.lang.annotation.*;

/**
 * Định nghĩa trên trường, đánh dấu thông tin mà trường lưu trữ là nhạy cảm
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
public @interface Sensitive {

    /**
     * Các loại khác nhau xử lý khác nhau
     * @return
     */
    SensitiveEnum type() default SensitiveEnum.ENCODE;
}
