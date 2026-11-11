package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * Chuyển đổi table động
 *
 * @author :zyf
 * @date:2020-04-25
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicTable {
    /**
     * Tên bảng cần phân tích động
     * @return
     */
    String value();
}
