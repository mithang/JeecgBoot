package org.jeecg.config.filter;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * websocket Frontend đặt token vào sub-protocol để truyền vào; khi thiết lập kết nối với backend cần dùng giao thức HTTP, tại đây dùng để kiểm tra tính hợp lệ của token
 * @Author taoYan
 * @Date 2022/4/21 17:01
 **/
@Slf4j
public class WebsocketFilter implements Filter {

    private static final String TOKEN_KEY = "Sec-WebSocket-Protocol";

    /**
     * Endpoint websocket thông báo hệ thống /websocket/{userId}(chỉ đường dẫn một đoạn, không bao gồm /websocket/drag/{chartId} các API đường dẫn con khác)
     */
    private static final Pattern SYS_NOTICE_WS_PATTERN = Pattern.compile("^/websocket/([^/]+)/?$");

    private static CommonAPI commonApi;

    private static RedisUtil redisUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (commonApi == null) {
            commonApi = SpringContextUtils.getBean(CommonAPI.class);
        }
        if (redisUtil == null) {
            redisUtil = SpringContextUtils.getBean(RedisUtil.class);
        }
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        String token = request.getHeader(TOKEN_KEY);

        log.debug("Websocket Kết nối kiểm tra an toàn Token,Path = {}，token:{}", request.getRequestURI(), token);

        try {
            TokenUtils.verifyToken(token, commonApi, redisUtil);
        } catch (Exception exception) {
            //log.error("Websocket Kết nối, kiểm chứng an toàn Token thất bại，IP:{}, Token:{}, Path = {}，Ngoại lệ：{}", oConvertUtils.getIpAddrByRequest(request), token, request.getRequestURI(), exception.getMessage());
            log.debug("Websocket Kết nối, kiểm chứng an toàn Token thất bại，IP:{}, Token:{}, Path = {}，Ngoại lệ：{}", oConvertUtils.getIpAddrByRequest(request), token, request.getRequestURI(), exception.getMessage());
            return;
        }
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-5j5w-pm5g-5wr8】Thông báo hệ thống websocket(/websocket/{userId})Ràng buộc danh tính token, cấm đăng ký tin nhắn thời gian thực của người khác-----------
        String uri = request.getRequestURI();
        //update-begin---author:scott ---date:20261026  for：【issues/4830】Ràng buộc danh tính userId chỉ áp dụng cho endpoint thông báo hệ thống/websocket/{userId}，để tránh chặn nhầm màn hình lớn/websocket/drag/{chartId}và các API websocket khác-----------
        // Loại bỏ contextPath và gộp các dấu gạch chéo liên tiếp, thống nhất với quy tắc định tuyến endpoint của Tomcat, ngăn chặn /websocket//{userId} bypass kiểm tra
        String path = uri.substring(request.getContextPath().length()).replaceAll("/{2,}", "/");
        Matcher sysNoticeMatcher = SYS_NOTICE_WS_PATTERN.matcher(path);
        if (sysNoticeMatcher.matches()) {
            String pathUserId = sysNoticeMatcher.group(1);
        //update-end---author:scott ---date:20261026  for：【issues/4830】Ràng buộc danh tính userId chỉ áp dụng cho endpoint thông báo hệ thống/websocket/{userId}，để tránh chặn nhầm màn hình lớn/websocket/drag/{chartId}và các API websocket khác-----------
            // Định dạng userId trong đường dẫn frontend là {thực userId}_{wsClientId}(md5(token)), lấy userId thực trước dấu gạch dưới để so sánh; đồng thời tương thích với userId thuần không có hậu tố userId
            int underline = pathUserId.indexOf('_');
            String realPathUserId = underline >= 0 ? pathUserId.substring(0, underline) : pathUserId;
            LoginUser loginUser = commonApi.getUserByName(JwtUtil.getUsername(token));
            if (loginUser == null || !loginUser.getId().equals(realPathUserId)) {
                log.warn("Websocket Kết nối: danh tính người dùng không khớp với userId trong đường dẫn, đã từ chối.Path = {}", uri);
                return;
            }
        }
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-5j5w-pm5g-5wr8】Thông báo hệ thống websocket(/websocket/{userId})Ràng buộc danh tính token, cấm đăng ký tin nhắn thời gian thực của người khác-----------
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.setHeader(TOKEN_KEY, token);
        filterChain.doFilter(servletRequest, servletResponse);
    }

}
