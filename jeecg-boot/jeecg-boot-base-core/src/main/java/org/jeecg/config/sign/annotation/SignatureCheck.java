package org.jeecg.config.sign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation kiểm tra chữ ký
 * Dùng để xác thực chữ ký ở cấp phương thức, chức năng tương đương với cấu hình trong yml jeecg.signUrls Cấu hình
 * Tham khảo ý tưởng thiết kế của DragSignatureAspect, triển khai bằng aspect AOP
 * 
 * @author GitHub Copilot
 * @since 2025-12-15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SignatureCheck {
    
    /**
     * Có bật kiểm tra chữ ký hay không
     * @return true-Kích hoạt(Mặc định), false-Vô hiệu hóa
     */
    boolean enabled() default true;
    
    /**
     * Thông báo lỗi khi kiểm tra chữ ký thất bại
     * @return Thông báo lỗi
     */
    String errorMessage() default "Sign Xác thực chữ ký thất bại!";
    
}
