package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * online Annotation chuyên dùng chặn yêu cầu
 * @author: jeecg-boot
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface OnlineAuth {

    /**
     * Từ khóa yêu cầu, nằm ở xxx/code chuỗi trước đó
     * @return
     */
    String value();
}
