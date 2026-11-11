package org.jeecg.common.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  *  Annotation quyền dữ liệu
 * @Author taoyan
 * @Date 2019 năm 4 tháng 11 ngày
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface PermissionData {
	/**
	 * Tạm thời chưa dùng
	 * @return
	 */
	String value() default "";
	
	
	/**
	 * Đường dẫn component của menu đã cấu hình,Dùng cho quyền dữ liệu
	 */
	String pageComponent() default "";
}