package org.jeecg.config.sign.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.config.sign.annotation.SignatureCheck;
import org.jeecg.config.sign.interceptor.SignAuthInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect xác thực chữ ký dựa trên AOP
 * Tái sử dụng logic xác thực chữ ký đã hoàn thiện của SignAuthInterceptor
 * 
 * @author GitHub Copilot
 * @since 2025-12-15
 */
@Aspect
@Slf4j
@Component("signatureCheckAspect")
public class SignatureCheckAspect {
    
    /**
     * Tái sử dụng logic xác thực chữ ký của SignAuthInterceptor
     */
    private final SignAuthInterceptor signAuthInterceptor = new SignAuthInterceptor();

    /**
     * Điểm cắt kiểm tra chữ ký: Chặn tất cả các phương thức được đánh dấu@SignatureCheck bằng annotation
     */
    @Pointcut("@annotation(org.jeecg.config.sign.annotation.SignatureCheck)")
    private void signatureCheckPointCut() {
    }

    /**
     * Bắt đầu kiểm tra chữ ký
     */
    @Before("signatureCheckPointCut()")
    public void doSignatureValidation(JoinPoint point) throws Exception {
        // Lấy annotation trên phương thức
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        SignatureCheck signatureCheck = method.getAnnotation(SignatureCheck.class);
        
        log.info("AOP Xác thực chữ ký: {}.{}", method.getDeclaringClass().getSimpleName(), method.getName());
        
        // Nếu annotation bị vô hiệu hóa, trả về trực tiếp
        if (!signatureCheck.enabled()) {
            log.info("Xác thực chữ ký đã bị vô hiệu hóa, bỏ qua");
            return;
        }

        // update-begin---author:sjlei---date:20260115 for: Tìm tham số mang@RequestBody annotation, giải quyết vấn đề đọc request body bị trống khi kiểm chứng chữ ký
        Object bodyParam = null;
        Object[] args = point.getArgs();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Annotation[] annotations = parameterAnnotations[i];
            boolean hasRequestBodyAnnotation = Arrays.stream(annotations).anyMatch(annotation -> annotation.annotationType().equals(RequestBody.class));
            if (hasRequestBodyAnnotation) {
                // Bắt tham số mang@RequestBody annotation, dùng cho kiểm tra chữ ký
                bodyParam = arg;
            }
        }
        // update-end-----author:sjlei---date:20260115 for: Tìm tham số mang@RequestBody annotation, giải quyết vấn đề đọc request body bị trống khi kiểm chứng chữ ký

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("Không thể lấy ngữ cảnh yêu cầu");
            throw new IllegalArgumentException("Không thể lấy ngữ cảnh yêu cầu");
        }
        
        HttpServletRequest request = attributes.getRequest();
        log.info("X-SIGN: {}, X-TIMESTAMP: {}", request.getHeader("X-SIGN"), request.getHeader("X-TIMESTAMP"));
        
        try {
            // Gọi trực tiếp logic xác thực của SignAuthInterceptor
            signAuthInterceptor.validateSignature(request, bodyParam);
            log.info("AOP Xác minh chữ ký thành công");
            
        } catch (IllegalArgumentException e) {
            // Sử dụng thông báo lỗi được cấu hình trong annotation, hoặc giữ lại thông báo lỗi gốc
            String errorMessage = signatureCheck.errorMessage();
            log.error("AOP Xác minh chữ ký thất bại: {}", e.getMessage());
            
            if ("Sign Xác thực chữ ký thất bại!".equals(errorMessage)) {
                // Nếu là thông báo lỗi mặc định, sử dụng thông tin lỗi chi tiết gốc
                throw e;
            } else {
                // Nếu là thông báo lỗi tùy chỉnh, sử dụng thông báo tùy chỉnh
                throw new IllegalArgumentException(errorMessage, e);
            }
        } catch (Exception e) {
            // Bọc các ngoại lệ khác
            String errorMessage = signatureCheck.errorMessage();
            log.error("AOP Lỗi xác minh chữ ký: {}", e.getMessage());
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}
