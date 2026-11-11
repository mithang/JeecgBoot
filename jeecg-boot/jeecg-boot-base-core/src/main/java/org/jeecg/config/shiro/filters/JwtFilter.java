package org.jeecg.config.shiro.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.shiro.JwtToken;
import org.jeecg.config.shiro.ignore.InMemoryIgnoreAuth;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @Description: Interceptor xác thực đăng nhập
 * @Author: Scott
 * @Date: 2018/10/7
 **/
@Slf4j
public class JwtFilter extends BasicHttpAuthenticationFilter {

    /**
     * Mặc định bật cài đặt cross-domain (dùng kiến trúc đơn khối)
     * Trong trường hợp microservice, thuộc tính này được đặt thành false
     */
    private boolean allowOrigin = true;

    public JwtFilter(){}
    public JwtFilter(boolean allowOrigin){
        this.allowOrigin = allowOrigin;
    }

    /**
     * Thực hiện xác thực đăng nhập
     *
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            // Kiểm tra xem đường dẫn hiện tại có được chú thích hay không@IngoreAuth đường dẫn, nếu có thì bỏ qua xác thực
            if (InMemoryIgnoreAuth.contains(((HttpServletRequest) request).getServletPath())) {
                return true;
            }
            
            executeLogin(request, response);
            return true;
        } catch (Exception e) {
            // Sử dụng thông báo lỗi cụ thể trong ngoại lệ, giữ lại"Không cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi"và các gợi ý cụ thể khác
            String errorMsg = e.getMessage();
            if (oConvertUtils.isEmpty(errorMsg)) {
                errorMsg = CommonConstant.TOKEN_IS_INVALID_MSG;
            }
            JwtUtil.responseError((HttpServletResponse)response, 401, errorMsg);
            return false;
        }
    }

    /**
     *
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String token = httpServletRequest.getHeader(CommonConstant.X_ACCESS_TOKEN);
        // Giải thích logic mã: JT-355 OA Trò chuyện thêm xác thực token, lấy tham số token
        if (oConvertUtils.isEmpty(token)) {
            token = httpServletRequest.getParameter("token");
        }

        JwtToken jwtToken = new JwtToken(token);
        // Gửi cho realm để đăng nhập, nếu lỗi sẽ ném ra ngoại lệ và bị bắt
        getSubject(request, response).login(jwtToken);
        // Nếu không ném ra ngoại lệ thì đăng nhập thành công, trả về true
        return true;
    }

    /**
     * Hỗ trợ cross-domain
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        if(allowOrigin){
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
            // Cho phép phương thức yêu cầu của client
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,OPTIONS,PUT,DELETE");
            // Cho phép client gửi Header
            String requestHeaders = httpServletRequest.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
            if (StringUtils.isNotEmpty(requestHeaders)) {
                httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
            }
            // Cho phép client mang theo thông tin xác thực(Có cho phép gửi hay không Cookie)
            httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        // Khi cross-domain sẽ gửi trước một yêu cầu option, ở đây chúng ta trả về trực tiếp trạng thái bình thường cho yêu cầu option
        if (RequestMethod.OPTIONS.name().equalsIgnoreCase(httpServletRequest.getMethod())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        // Giải thích logic mã: Dùng cho đa tenant
        String tenantId = httpServletRequest.getHeader(CommonConstant.TENANT_ID);
        TenantContext.setTenant(tenantId);

        return super.preHandle(request, response);
    }

    /**
     * JwtFilter ThreadLocal trong JwtFilter cần được xóa kịp thời #3634
     *
     * @param request
     * @param response
     * @param exception
     * @throws Exception
     */
    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, Exception exception) throws Exception {
        //log.info("------Xóa tenant trong luồng ID={}------",TenantContext.getTenant());
        TenantContext.clear();
    }
}
