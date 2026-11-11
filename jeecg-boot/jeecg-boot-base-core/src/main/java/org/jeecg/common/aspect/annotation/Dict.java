package org.jeecg.common.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation danh mục
 * @author: dangzhenghui
 * @date: 2019 năm 03 tháng 17 ngày-Buổi chiều 9:37:16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    /**
     * Mô tả phương thức:  Dữ liệu code
     * Tác giả: dangzhenghui
     * Ngày: 17 tháng 03 năm 2019-Buổi chiều 9:37:16
     *
     * @return Kiểu trả về: String
     */
    String dicCode();

    /**
     * Mô tả phương thức:  Dữ liệu Text
     * Tác giả: dangzhenghui
     * Ngày: 17 tháng 03 năm 2019-Buổi chiều 9:37:16
     *
     * @return Kiểu trả về: String
     */
    String dicText() default "";

    /**
     * Mô tả phương thức: Bảng từ điển dữ liệu
     * Tác giả: dangzhenghui
     * Ngày: 17 tháng 03 năm 2019-Buổi chiều 9:37:16
     *
     * @return Kiểu trả về: String
     */
    String dictTable() default "";


    /**
     * Mô tả phương thức: Tên nguồn dữ liệu nơi bảng từ điển dữ liệu nằm
     * Tác giả: chenrui
     * Ngày: 20 tháng 12 năm 2023-Buổi chiều 4:58
     *
     * @return Kiểu trả về: String
     */
    String ds() default "";
}
