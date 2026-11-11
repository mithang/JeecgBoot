package org.jeecg.modules.aop;

import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.ModuleType;

import java.lang.annotation.*;

/**
 * Chú thích nhật ký hệ thống
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 14/01/2019
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantLog {

	/**
	 * Loại nhật ký thao tác (1: truy vấn, 2: thêm mới, 3: sửa, 4: xóa)
	 * 
	 * @return
	 */
	int value() default 0;

}
