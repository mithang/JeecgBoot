package org.jeecg.config.sign.interceptor;


import java.io.PrintWriter;
import java.util.SortedMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.sign.util.BodyReaderHttpServletRequestWrapper;
import org.jeecg.config.sign.util.HttpUtils;
import org.jeecg.config.sign.util.SignUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor chữ ký
 * @author qinfeng
 */
@Slf4j
public class SignAuthInterceptor implements HandlerInterceptor {
    /**
     * 5 phút hiệu lực
     */
    private final static long MAX_EXPIRE = 5 * 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("Interceptor chữ ký Interceptor request URI = " + request.getRequestURI());
        
        try {
            // Gọi logic xác thực
            validateSignature(request);
            return true;
        } catch (IllegalArgumentException e) {
            // Xác thực thất bại, trả về phản hồi lỗi
            log.error("Sign Xác thực chữ ký thất bại!{}", e.getMessage());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            PrintWriter out = response.getWriter();
            Result<?> result = Result.error(e.getMessage());
            out.print(JSON.toJSON(result));
            return false;
        }
    }

    /**
     * Logic cốt lõi xác minh chữ ký
     * Tách ra để AOP aspect tái sử dụng
     * @param request HTTP Yêu cầu
     * @throws IllegalArgumentException Ném ngoại lệ khi xác minh thất bại
     */
    public void validateSignature(HttpServletRequest request) throws IllegalArgumentException {
        validateSignature(request, null);
    }

    /**
     * Logic cốt lõi xác minh chữ ký
     * Tách ra để AOP aspect tái sử dụng
     * @param request HTTP Yêu cầu
     * @throws IllegalArgumentException Ném ngoại lệ khi xác minh thất bại
     */
    public void validateSignature(HttpServletRequest request, Object bodyParam) throws IllegalArgumentException {
        try {
            log.debug("Bắt đầu xác thực chữ ký: {} {}", request.getMethod(), request.getRequestURI());
            
            HttpServletRequest requestWrapper = new BodyReaderHttpServletRequestWrapper(request);
            //Lấy tất cả tham số(bao gồm cả trên URL và body)
            SortedMap<String, String> allParams = HttpUtils.getAllParams(requestWrapper, bodyParam);
            log.debug("Trích xuất tham số: {}", allParams);
            
            //Thực hiện xác thực chữ ký đối với tham số
            String headerSign = request.getHeader(CommonConstant.X_SIGN);
            String xTimestamp = request.getHeader(CommonConstant.X_TIMESTAMP);
            
            if(oConvertUtils.isEmpty(xTimestamp)){
                log.error("Sign Kiểm tra chữ ký thất bại, timestamp rỗng!");
                throw new IllegalArgumentException("Sign Kiểm tra chữ ký thất bại, tham số yêu cầu không đầy đủ!");
            }

            //Thời gian của client
            Long clientTimestamp = Long.parseLong(xTimestamp);

            int length = 14;
            int length1000 = 1000;
            //1.Kiểm tra thời gian chữ ký (tương thích X_TIMESTAMP định dạng mới và cũ)
            if (xTimestamp.length() == length) {
                //a. X_TIMESTAMP Định dạng là yyyyMMddHHmmss (Ví dụ:20220308152143)
                long currentTimestamp = DateUtils.getCurrentTimestamp();
                long timeDiff = currentTimestamp - clientTimestamp;
                log.debug("Xác minh timestamp(yyyyMMddHHmmss): Chênh lệch thời gian{}giây", timeDiff);
                
                if (timeDiff > MAX_EXPIRE) {
                    log.error("Timestamp đã hết hạn: {}giây > {}giây", timeDiff, MAX_EXPIRE);
                    throw new IllegalArgumentException("Xác minh chữ ký thất bại, xác minh tính thời hiệu của yêu cầu thất bại!");
                }
            } else {
                //b. X_TIMESTAMP Định dạng là timestamp (Ví dụ:1646552406000)
                long currentTime = System.currentTimeMillis();
                long timeDiff = currentTime - clientTimestamp;
                long maxExpireMs = MAX_EXPIRE * length1000;
                log.debug("Xác minh timestamp(Unix): Chênh lệch thời gian{}ms", timeDiff);
                
                if (timeDiff > maxExpireMs) {
                    log.error("Timestamp đã hết hạn: {}ms > {}ms", timeDiff, maxExpireMs);
                    throw new IllegalArgumentException("Xác minh chữ ký thất bại, xác minh tính thời hiệu của yêu cầu thất bại!");
                }
            }

            //2.Kiểm tra chữ ký
            boolean isSigned = SignUtil.verifySign(allParams,headerSign);

            if (isSigned) {
                log.debug("Xác minh chữ ký thành công");
            } else {
                log.error("Xác minh chữ ký thất bại, Tham số: {}", allParams);
                throw new IllegalArgumentException("Sign Xác thực chữ ký thất bại!");
            }
        } catch (IllegalArgumentException e) {
            // Ném lại ngoại lệ xác thực chữ ký
            throw e;
        } catch (Exception e) {
            // Bọc các ngoại lệ khác (như IOException)
            log.error("Lỗi xác minh chữ ký: {}", e.getMessage());
            throw new IllegalArgumentException("Sign Kiểm tra chữ ký thất bại：" + e.getMessage());
        }
    }

}
