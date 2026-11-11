package org.jeecg.config.mybatis.interceptor;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor chuyển đổi nguồn dữ liệu động
 *
 * Kiểm tra: chặn tham số, tự động chuyển đổi nguồn dữ liệu
 * Kế hoạch tương lai: sau này thông qua cơ chế này, triển khai chức năng chuyển đổi nguồn dữ liệu đa tenant
 * @author zyf
 */
@Slf4j
public class DynamicDatasourceInterceptor implements HandlerInterceptor {

    /**
     * Được gọi trước khi xử lý yêu cầu (trước khi phương thức Controller được gọi)
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        log.info("Đi qua đa nguồn dữ liệu Interceptor,Đường dẫn hiện tại là{}", requestURI);
        //Lấy tên nguồn dữ liệu động
        String dsName = request.getParameter("dsName");
        String dsKey = "master";
        if (StringUtils.isNotEmpty(dsName)) {
            dsKey = dsName;
        }
        DynamicDataSourceContextHolder.push(dsKey);
        return true;
    }

    /**
     * Được gọi sau khi xử lý yêu cầu, nhưng trước khi view được render (sau khi phương thức Controller được gọi)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    /**
     * Được gọi sau khi toàn bộ yêu cầu kết thúc, tức là thực thi sau khi DispatcherServlet đã render view tương ứng (chủ yếu dùng để dọn dẹp tài nguyên)
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DynamicDataSourceContextHolder.clear();
    }

}