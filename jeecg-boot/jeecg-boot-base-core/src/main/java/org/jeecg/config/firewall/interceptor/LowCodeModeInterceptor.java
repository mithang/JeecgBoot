package org.jeecg.config.firewall.interceptor;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * Chế độ low-code (dev:Chế độ phát triển,prod:Chế độ phát hành — tắt toàn bộ khả năng phát triển trực tuyến)
 * <p>
 * prod Sau khi bật sẽ tắt các chức năng sau, chỉ giữ lại kiểm tra chức năng (tài khoản có vai trò admin có thể sử dụng khả năng cấu hình)
 * 1.online Tất cả chức năng cấu hình của biểu mẫu, chức năng sinh mã và nhập bảng
 * 2.online Tất cả chức năng cấu hình của báo cáo, và phân tích SQL
 * 3.online Tất cả chức năng cấu hình của biểu đồ, và phân tích SQL
 * 4.Chức năng cấu hình trực tuyến của Dashboard, và phân tích SQL
 * 5.Chức năng cấu hình trực tuyến của màn hình lớn, và phân tích SQL
 * 
 * Logic của Jimu được xử lý riêng
 * 1.Chức năng cấu hình trực tuyến của báo cáo Jimu, và phân tích SQL
 *
 * @author qinfeng
 * @date 20230904
 */
@Slf4j
@Component
public class LowCodeModeInterceptor implements HandlerInterceptor {
    /**
     * Chế độ phát triển low-code
     */
    public static final String LOW_CODE_MODE_DEV = "dev";
    public static final String LOW_CODE_MODE_PROD = "prod";

    @Resource
    private JeecgBaseConfig jeecgBaseConfig;
    
    /**
     * Được gọi trước khi xử lý yêu cầu
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        CommonAPI commonAPI = null;
        log.debug("Chế độ low-code, chặn đường dẫn yêu cầu：" + request.getRequestURI());
        
        //1, kiểm tra xem có bật kiểm soát chế độ phát triển low-code hay không
        if (jeecgBaseConfig == null) {
            jeecgBaseConfig = SpringContextUtils.getBean(JeecgBaseConfig.class);
        }
        if (commonAPI == null) {
            commonAPI = SpringContextUtils.getBean(CommonAPI.class);
        }

        if (jeecgBaseConfig.getFirewall()!=null && LowCodeModeInterceptor.LOW_CODE_MODE_PROD.equals(jeecgBaseConfig.getFirewall().getLowCodeMode())) {
            String requestURI = request.getRequestURI().substring(request.getContextPath().length());
            log.info("Chế độ low-code, chặn đường dẫn yêu cầu：" + requestURI);
            LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            Set<String> hasRoles = null;
            if (loginUser == null) {
                loginUser = commonAPI.getUserByName(JwtUtil.getUserNameByToken(SpringContextUtils.getHttpServletRequest()));
            }

            if (loginUser != null) {
                //Các vai trò mà người đang đăng nhập sở hữu
                hasRoles = commonAPI.queryUserRolesById(loginUser.getId());
            }
            
            log.info("get loginUser info: {}", loginUser);
            log.info("get loginRoles info: {}", hasRoles != null ? hasRoles.toArray() : "Trống");
            
            //Các vai trò sở hữu và các vai trò được phép phát triển có giao nhau
            boolean hasIntersection = CommonUtils.hasIntersection(hasRoles, CommonConstant.allowDevRoles);
            //Nếu là quản trị viên cấp cao hoặc vai trò được phép phát triển thì không giới hạn
            if (loginUser!=null && ("admin".equals(loginUser.getUsername()) || hasIntersection)) {
                return true;
            }
            
            this.returnErrorMessage(response);
            return false;
        }
        return true;
    }


    /**
     * Trả về kết quả
     *
     * @param response
     */
    private void returnErrorMessage(HttpServletResponse response) {
        //Kiểm tra thất bại thì trả về frontend
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            Result<?> result = Result.error("Chế độ phát triển low-code đang ở chế độ phát hành, không được phép sử dụng cấu hình trực tuyến!!");
            out.print(JSON.toJSON(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

