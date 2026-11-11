package org.jeecg.common.desensitization.annotation;

import java.lang.annotation.*;

/**
 * Annotation giải mã
 *
 * Định nghĩa trên phương thức để giải mã các trường nhạy cảm trong đối tượng trả về của phương thức; cần lưu ý nếu chưa từng được mã hóa thì việc giải mã sẽ gặp vấn đề và trả về chuỗi gốc
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface SensitiveDecode {

    /**
     * Chỉ rõ lớp entity cần khử nhạy cảm class
     * @return
     */
    Class entity() default Object.class;
}
