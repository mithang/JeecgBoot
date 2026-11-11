package org.jeecg.config.mybatis.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.jeecg.common.aspect.annotation.DynamicTable;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.config.mybatis.ThreadLocalDataHelper;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Xử lý aspect chuyển đổi table động
 *
 * @author :zyf
 * @date:2020-04-25
 */
@Aspect
@Component
public class DynamicTableAspect {


    /**
     * Định nghĩa điểm cắt (pointcut) chặn của aspect
     */
    @Pointcut("@annotation(org.jeecg.common.aspect.annotation.DynamicTable)")
    public void dynamicTable() {
    }


    @Around("dynamicTable()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DynamicTable dynamicTable = method.getAnnotation(DynamicTable.class);
        HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
        //Lấy dấu phiên bản mà frontend truyền sang
        String version = request.getHeader(CommonConstant.VERSION);
        //Lưu số phiên bản vào biến cục bộ của luồng
        ThreadLocalDataHelper.put(CommonConstant.VERSION, version);
        //Lưu tên bảng vào biến cục bộ của luồng
        ThreadLocalDataHelper.put(CommonConstant.DYNAMIC_TABLE_NAME, dynamicTable.value());
        //Thực thi phương thức
        Object result = point.proceed();
        //Xóa biến cục bộ
        ThreadLocalDataHelper.clear();
        return result;
    }

}
