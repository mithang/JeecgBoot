package org.jeecg.common.desensitization.annotation;

import java.lang.annotation.*;

/**
 * Annotation mã hóa
 *
 * Khai báo trên phương thức để mã hóa các trường nhạy cảm trong đối tượng trả về của phương thức/Định dạng
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SensitiveEncode {

    /**
     * Chỉ rõ lớp entity cần khử nhạy cảm class
     * @return
     */
    Class entity() default Object.class;
}
