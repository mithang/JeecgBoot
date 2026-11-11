package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * API khai báo bằng annotation này sẽ tự động thực hiện dịch danh mục
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2022 năm 01 tháng 05 ngày
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoDict {

	/**
	 * Tạm thời không dùng
	 * @return
	 */
	String value() default "";

}
