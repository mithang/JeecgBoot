package org.jeecg.common.desensitization.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.desensitization.annotation.SensitiveDecode;
import org.jeecg.common.desensitization.annotation.SensitiveEncode;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Lớp xử lý aspect dữ liệu nhạy cảm
 * @Author taoYan
 * @Date 2022/4/20 17:45
 **/
@Slf4j
@Aspect
@Component
public class SensitiveDataAspect {

    /**
     * Định nghĩa điểm cắt Pointcut
     */
    @Pointcut("@annotation(org.jeecg.common.desensitization.annotation.SensitiveEncode) || @annotation(org.jeecg.common.desensitization.annotation.SensitiveDecode)")
    public void sensitivePointCut() {
    }

    @Around("sensitivePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // Kết quả xử lý
        Object result = point.proceed();
        if(result == null){
            return result;
        }
        Class resultClass = result.getClass();
        log.debug(" resultClass  = {}" , resultClass);

        if(resultClass.isPrimitive()){
            //Là kiểu cơ bản thì trả về trực tiếp, không cần xử lý
            return result;
        }
        // Lấy thông tin annotation của phương thức: là thực thể nào, là mã hóa hay giải mã
        boolean isEncode = true;
        Class entity = null;
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        SensitiveEncode encode = method.getAnnotation(SensitiveEncode.class);
        if(encode==null){
            SensitiveDecode decode = method.getAnnotation(SensitiveDecode.class);
            if(decode!=null){
                entity = decode.entity();
                isEncode = false;
            }
        }else{
            entity = encode.entity();
        }

        long startTime=System.currentTimeMillis();
        if(resultClass.equals(entity) || entity.equals(Object.class)){
            // Thực thể trả về của phương thức giống với entity của annotation; nếu annotation không khai báo thuộc tính entity thì được coi là(Thực thể trả về của phương thức giống với entity của annotation)
            SensitiveInfoUtil.handlerObject(result, isEncode);
        } else if(result instanceof List){
            // Phương thức trả về List<Entity>
            SensitiveInfoUtil.handleList(result, entity, isEncode);
        }else{
            // Phương thức trả về một đối tượng
            SensitiveInfoUtil.handleNestedObject(result, entity, isEncode);
        }
        long endTime=System.currentTimeMillis();
        log.debug((isEncode ? "Thao tác mã hóa," : "Thao tác giải mã,") + "Aspect Thời gian chạy chương trình:" + (endTime - startTime) + "ms");

        return result;
    }

}
