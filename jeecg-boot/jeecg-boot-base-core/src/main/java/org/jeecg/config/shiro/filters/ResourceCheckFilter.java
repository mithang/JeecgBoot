package org.jeecg.config.shiro.filters;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Scott
 * @create 2019-02-01 15:56
 * @desc Interceptor chặn quyền truy cập URL của yêu cầu xác thực
 */
@Slf4j
public class ResourceCheckFilter extends AccessControlFilter {

    private String errorUrl;

    public String getErrorUrl() {
        return errorUrl;
    }

    public void setErrorUrl(String errorUrl) {
        this.errorUrl = errorUrl;
    }

    /**
     * Cho biết có cho phép truy cập hay không, nếu cho phép truy cập trả về true, ngược lại trả về false;
     *
     * @param servletRequest
     * @param servletResponse
     * @param o               Cho biết chuỗi mappedValue được viết trong dấu ngoặc của interceptor chính là [urls] phần tham số interceptor trong cấu hình [urls]
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        Subject subject = getSubject(servletRequest, servletResponse);
        String url = getPathWithinApplication(servletRequest);
        log.info("URL người dùng hiện tại đang truy cập url => " + url);
        return subject.isPermitted(url);
    }

    /**
     * onAccessDenied：Cho biết khi truy cập bị từ chối đã được xử lý hay chưa; nếu trả về true nghĩa là cần tiếp tục xử lý; nếu trả về false
     * Cho biết instance interceptor này đã xử lý xong, sẽ trả về trực tiếp.
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        log.info("Chỉ khi isAccessAllowed trả về false mới thực thi method onAccessDenied ");

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.sendRedirect(request.getContextPath() + this.errorUrl);

        // Trả về false nghĩa là đã xử lý, ví dụ như chuyển trang, nghĩa là không đi qua các interceptor tiếp theo nữa (nếu còn cấu hình)
        return false;
    }

}