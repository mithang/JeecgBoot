package org.jeecg.common.aspect.annotation;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;

import java.lang.annotation.*;

/**
 * Chú thích nhật ký hệ thống
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019 năm 1 tháng 14 ngày
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoLog {

	/**
	 * Nội dung nhật ký
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * Loại nhật ký
	 * 
	 * @return 0:Nhật ký thao tác;1:Nhật ký đăng nhập;2:Tác vụ định kỳ;
	 */
	int logType() default CommonConstant.LOG_TYPE_2;
	
	/**
	 * Loại nhật ký thao tác
	 * 
	 * @return (1 Tìm kiếm, 2 Thêm, 3 Sửa, 4 Xóa)
	 */
	int operateType() default 0;

	/**
	 * Loại module mặc định là common
	 * @return
	 */
	ModuleType module() default ModuleType.COMMON;
}
