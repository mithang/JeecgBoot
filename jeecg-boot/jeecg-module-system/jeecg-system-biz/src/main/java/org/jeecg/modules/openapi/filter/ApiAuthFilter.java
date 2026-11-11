package org.jeecg.modules.openapi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.IpUtils;
import org.jeecg.modules.openapi.entity.OpenApi;
import org.jeecg.modules.openapi.entity.OpenApiAuth;
import org.jeecg.modules.openapi.entity.OpenApiLog;
import org.jeecg.modules.openapi.entity.OpenApiPermission;
import org.jeecg.modules.openapi.service.OpenApiAuthService;
import org.jeecg.modules.openapi.service.OpenApiLogService;
import org.jeecg.modules.openapi.service.OpenApiPermissionService;
import org.jeecg.modules.openapi.service.OpenApiService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @date 2024/12/19 16:55
 */
@Slf4j
public class ApiAuthFilter implements Filter {

    private OpenApiLogService openApiLogService;
    private OpenApiAuthService openApiAuthService;
    private OpenApiPermissionService openApiPermissionService;
    private OpenApiService openApiService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        long startTime = System.currentTimeMillis();
        Date callTime = new Date();

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //update-begin---author:jeecg---for：【GHSA-3c5r-mgw9-jq74] Preflight OPTIONS chỉ trả lời CORS, không được vào chuỗi nghiệp vụ tiếp theo, nếu không có thể vượt qua chữ ký/AK/Kiểm tra ủy quyền---
        // OPTIONS Yêu cầu preflight: chỉ ghi lại header phản hồi CORS rồi kết thúc, tuyệt đối không cho đi qua filterChain (ngăn OPTIONS vượt qua xác thực chữ ký OpenApi)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            String origin = request.getHeader("Origin");
            if (StringUtils.hasText(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,PATCH,OPTIONS");
                String reqHeaders = request.getHeader("Access-Control-Request-Headers");
                if (StringUtils.hasText(reqHeaders)) {
                    response.setHeader("Access-Control-Allow-Headers", reqHeaders);
                }
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        //update-end---author:jeecg---for：【GHSA-3c5r-mgw9-jq74] Preflight OPTIONS chỉ trả lời CORS, không được đi vào chuỗi nghiệp vụ tiếp theo---

        String ip = IpUtils.getIpAddr(request);

        String appkey = request.getHeader("appkey");
        String signature = request.getHeader("signature");
        String timestamp = request.getHeader("timestamp");
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();
        String requestParams = buildParamsSummary(request);
        //update-begin---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Thu thập thông tin Header của yêu cầu-----------
        String requestHeaders = buildHeadersSummary(request);
        //update-end---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Thu thập thông tin Header của yêu cầu-----------

        OpenApi openApi = null;
        OpenApiAuth openApiAuth = null;
        int responseCode = HttpServletResponse.SC_OK;
        String errorMsg = null;

        try {
            openApi = findOpenApi(request);
            // IP Kiểm tra danh sách trắng
            checkWhiteList(openApi, ip);
            // Xác minh chữ ký
            checkSignValid(appkey, signature, timestamp);
            openApiAuth = openApiAuthService.getByAppkey(appkey);
            // Kiểm tra thông tin xác thực
            checkSignature(appkey, signature, timestamp, openApiAuth);
            // Kiểm tra nghiệp vụ
            checkPermission(openApi, openApiAuth);

            filterChain.doFilter(servletRequest, servletResponse);
            responseCode = response.getStatus();
        } catch (JeecgBootException e) {
            responseCode = HttpServletResponse.SC_UNAUTHORIZED;
            errorMsg = e.getMessage();
            writeErrorResponse(response, request.getHeader("Origin"), responseCode, errorMsg);
        } catch (Exception e) {
            responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            errorMsg = e.getMessage();
            writeErrorResponse(response, request.getHeader("Origin"), responseCode, "Lỗi nội bộ máy chủ");
        } finally {
            long endTime = System.currentTimeMillis();
            //update-begin---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Truyền vào tham số requestHeaders-----------
            saveLog(openApi, openApiAuth, appkey, requestPath, requestMethod, ip, requestParams,
                    requestHeaders, responseCode, errorMsg, callTime, endTime - startTime);
            //update-end---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Truyền vào tham số requestHeaders-----------
        }
    }

    /**
     * Lưu bản ghi gọi
     * @param openApi
     * @param openApiAuth
     * @param callerAk
     * @param requestPath
     * @param requestMethod
     * @param sourceIp
     * @param requestParams
     * @param responseCode
     * @param errorMsg
     * @param callTime
     * @param usedTime
     */
    //update-begin---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Phương thức saveLog thêm tham số requestHeaders-----------
    private void saveLog(OpenApi openApi, OpenApiAuth openApiAuth, String callerAk,
                         String requestPath, String requestMethod, String sourceIp,
                         String requestParams, String requestHeaders, int responseCode, String errorMsg,
                         Date callTime, long usedTime) {
        try {
            OpenApiLog openApiLog = new OpenApiLog();
            openApiLog.setApiId(openApi != null ? openApi.getId() : null);
            openApiLog.setCallAuthId(openApiAuth != null ? openApiAuth.getId() : null);
            openApiLog.setCallerAk(callerAk);
            openApiLog.setRequestPath(requestPath);
            openApiLog.setRequestMethod(requestMethod);
            openApiLog.setIp(sourceIp);
            openApiLog.setRequestParams(requestParams);
            openApiLog.setRequestHeaders(requestHeaders);
            openApiLog.setResponseCode(responseCode);
            openApiLog.setErrorMsg(errorMsg);
            openApiLog.setCallTime(callTime);
            openApiLog.setUsedTime(usedTime);
            openApiLog.setResponseTime(new Date());
            openApiLogService.save(openApiLog);
        } catch (Exception e) {
            log.error("Lưu nhật ký gọi OpenAPI thất bại:{}",e.getMessage(), e);
        }
    }
    //update-end---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Phương thức saveLog thêm tham số requestHeaders-----------

    /**
     * Ghi body lỗi JSON vào phản hồi, kèm theo header CORS (đảm bảo bên gọi cross-domain như Swagger UI có thể đọc được thông tin lỗi)
     */
    private void writeErrorResponse(HttpServletResponse response, String origin, int status, String message) {
        try {
            if (StringUtils.hasText(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
            }
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            // Escape dấu ngoặc kép để tránh dấu ngoặc trong message phá vỡ JSON
            String safeMsg = message != null ? message.replace("\"", "\\\"") : "";
            response.getWriter().write("{\"success\":false,\"code\":" + status + ",\"message\":\"" + safeMsg + "\"}");
        } catch (IOException ex) {
            log.error("Ghi phản hồi lỗi OpenAPI thất bại", ex);
        }
    }

    private String buildParamsSummary(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            return queryString.length() > 500 ? queryString.substring(0, 500) + "..." : queryString;
        }
        return null;
    }

    //update-begin---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Xây dựng tóm tắt Header của yêu cầu (định dạng JSON, tối đa 2000 ký tự)-----------
    private String buildHeadersSummary(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder("{");
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            boolean first = true;
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = request.getHeader(name);
                if (!first) {
                    sb.append(", ");
                }
                sb.append("\"").append(name).append("\": \"")
                  .append(value != null ? value.replace("\\", "\\\\").replace("\"", "\\\"") : "")
                  .append("\"");
                first = false;
            }
        }
        sb.append("}");
        String result = sb.toString();
        return result.length() > 2000 ? result.substring(0, 2000) + "...}" : result;
    }
    //update-end---author:jeecg ---date:2026-05-27  for: [Thêm ghi log header] Xây dựng tóm tắt Header của yêu cầu (định dạng JSON, tối đa 2000 ký tự)-----------

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        WebApplicationContext applicationContext = (WebApplicationContext)servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        this.openApiService = applicationContext.getBean(OpenApiService.class);
        this.openApiLogService = applicationContext.getBean(OpenApiLogService.class);
        this.openApiAuthService = applicationContext.getBean(OpenApiAuthService.class);
        this.openApiPermissionService = applicationContext.getBean(OpenApiPermissionService.class);
    }

    //update-begin---author:scott ---date:20260416  for：【PR/9083] Tăng cường whitelist OpenAPI, hỗ trợ khớp dải CIDR và ký tự đại diện-----------
    /**
     * IP Kiểm tra whitelist, hỗ trợ IP chính xác, dải CIDR (ví dụ 192.168.1.0/24), ký tự đại diện (ví dụ 10.2.3.*）
     * @param openApi
     * @param ip
     */
    protected void checkWhiteList(OpenApi openApi, String ip) {
        if (!StringUtils.hasText(openApi.getWhiteList())) {
            return;
        }

        List<String> whiteList = Arrays.stream(openApi.getWhiteList().split("[,\\n]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        for (String item : whiteList) {
            if (isIpMatch(ip, item)) {
                return;
            }
        }
        throw new JeecgBootException("IP[" + ip + "]Không nằm trong danh sách trắng, cấm truy cập");
    }

    /**
     * IP Khớp: hỗ trợ khớp chính xác, khớp dải CIDR, khớp ký tự đại diện
     * @param ip Máy khách IP
     * @param pattern Mục trong danh sách trắng (IP/CIDR/ký tự đại diện)
     * @return Có khớp không
     */
    private boolean isIpMatch(String ip, String pattern) {
        if (!ip.contains(".") || !pattern.contains(".")) {
            return ip.equals(pattern);
        }
        if (pattern.contains("/")) {
            return isCidrMatch(ip, pattern);
        }
        if (pattern.contains("*")) {
            return isWildcardMatch(ip, pattern);
        }
        return ip.equals(pattern);
    }

    /**
     * CIDR Khớp dải mạng (chỉ IPv4), ví dụ 192.168.1.0/24
     */
    private boolean isCidrMatch(String ip, String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        try {
            long ipLong = ipToLong(ip);
            long cidrLong = ipToLong(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            if (prefixLength < 0 || prefixLength > 32) {
                return false;
            }
            long mask = prefixLength == 0 ? 0 : (-1L << (32 - prefixLength));
            return (ipLong & mask) == (cidrLong & mask);
        } catch (Exception e) {
            log.warn("CIDR Phân tích cú pháp khớp thất bại: cidr={}, ip={}", cidr, ip);
            return false;
        }
    }

    /**
     * Khớp ký tự đại diện, ví dụ 10.2.3.*
     */
    private boolean isWildcardMatch(String ip, String pattern) {
        String[] ipParts = ip.split("\\.");
        String[] patternParts = pattern.split("\\.");
        if (ipParts.length != 4 || patternParts.length != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            if ("*".equals(patternParts[i])) {
                continue;
            }
            if (!ipParts[i].equals(patternParts[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * IPv4 Chuyển đổi địa chỉ long
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Địa chỉ IPv4 không hợp lệ: " + ip);
        }
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = (result << 8) | (Integer.parseInt(parts[i]) & 0xFF);
        }
        return result;
    }
    //update-end---author:scott ---date:20260416  for：【PR/9083] Tăng cường whitelist OpenAPI, hỗ trợ khớp dải CIDR và ký tự đại diện-----------

    /**
     * Xác thực chữ ký
     * @param appkey
     * @param signature
     * @param timestamp
     * @return
     */
    protected void checkSignValid(String appkey, String signature, String timestamp) {
        if (!StringUtils.hasText(appkey)) {
            throw new JeecgBootException("appkey Trống");
        }
        if (!StringUtils.hasText(signature)) {
            throw new JeecgBootException("signature Trống");
        }
        if (!StringUtils.hasText(timestamp)) {
            throw new JeecgBootException("timastamp dấu thời gian trống");
        }
        if (!timestamp.matches("[0-9]*")) {
            throw new JeecgBootException("timastamp Dấu thời gian không hợp lệ");
        }
        if (System.currentTimeMillis() - Long.parseLong(timestamp) > 5 * 60 * 1000) {
            throw new JeecgBootException("signature chữ ký đã hết hạn(quá năm phút)");
        }
    }

    /**
     * Kiểm tra thông tin xác thực
     * @param appKey
     * @param signature
     * @param timestamp
     * @param openApiAuth
     * @return
     * @throws Exception
     */
    protected void checkSignature(String appKey, String signature, String timestamp, OpenApiAuth openApiAuth) {
        if(openApiAuth==null){
            throw new JeecgBootException("Không tồn tại thông tin xác thực");
        }

        //update-begin---author:scott ---date:20261014  for：【GHSA-qv87-c4w6-699m/GHSA-5jhm-3x8j-wwhr] khi SK trống, chữ ký sẽ suy giảm thành md5(appKey+timestamp), chỉ cần biết AK là có thể giả mạo, SK trống sẽ bị từ chối toàn bộ-----------
        if (!StringUtils.hasText(openApiAuth.getSk())) {
            throw new JeecgBootException("Thông tin xác thực này chưa cấu hình SK, vui lòng vào [OpenAPI-Quản lý ủy quyền】tạo lại AK/SK");
        }
        //update-end---author:scott ---date:20261014  for：【GHSA-qv87-c4w6-699m/GHSA-5jhm-3x8j-wwhr] khi SK trống, chữ ký sẽ suy giảm thành md5(appKey+timestamp), chỉ cần biết AK là có thể giả mạo, SK trống sẽ bị từ chối toàn bộ-----------

        if(!appKey.equals(openApiAuth.getAk())){
            throw new JeecgBootException("appkey Lỗi");
        }

        if (!signature.equals(md5(appKey + openApiAuth.getSk() + timestamp))) {
            throw new JeecgBootException("signature Lỗi chữ ký");
        }
    }

    protected void checkPermission(OpenApi openApi, OpenApiAuth openApiAuth) {
        List<OpenApiPermission> permissionList = openApiPermissionService.findByAuthId(openApiAuth.getId());

        boolean hasPermission = false;
        for (OpenApiPermission permission : permissionList) {
            if (permission.getApiId().equals(openApi.getId())) {
                hasPermission = true;
                break;
            }
        }

        if (!hasPermission) {
            throw new JeecgBootException("appKey này chưa được ủy quyền cho giao diện hiện tại");
        }
    }

    /**
     * @return String    Kiểu trả về
     * @Title: MD5
     * @Description: 【Mã hóa MD5】
     */
    protected static String md5(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes("utf-8"));
            byte[] hash = md.digest();
            int i;
            StringBuffer buf = new StringBuffer(32);
            for (int offset = 0; offset < hash.length; offset++) {
                i = hash[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (Exception e) {
            log.error("sign Lỗi chữ ký", e);
        }
        return result;
    }

    protected OpenApi findOpenApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.lastIndexOf("/") + 1);
        return openApiService.findByPath(path);
    }

    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();
        System.out.println("timestamp:"  + timestamp);
        System.out.println("signature:" + md5("ak-eAU25mrMxhtaZsyS" + "rjxMqB6YyUXpSHAz4DCIz8vZ5aozQQiV" + timestamp));
    }
}
