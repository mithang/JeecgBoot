package org.jeecg.common.system.query;

import java.lang.annotation.*;

/**
 * Annotation bỏ qua điều kiện tìm kiếm
 * <p>Trường được đánh dấu bằng annotation này sẽ bị QueryGenerator bỏ qua, không tham gia xây dựng điều kiện tìm kiếm.
 * Áp dụng cho các trường nhạy cảm như mật khẩu, salt, v.v., ngăn việc dò tìm bằng tìm kiếm mờ qua tham số yêu cầu.</p>
 *
 * @see QueryGenerator#initQueryWrapper
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QueryConditionIgnore {
}
