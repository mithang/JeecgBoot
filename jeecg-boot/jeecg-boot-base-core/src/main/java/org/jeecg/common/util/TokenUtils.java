package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.desensitization.util.SensitiveInfoUtil;
import org.jeecg.common.exception.JeecgBoot401Exception;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Author scott
 * @Date 2019/9/23 14:12
 * @Description: Kiểm chứng tính hợp lệ của token bằng mã
 */
@Slf4j
public class TokenUtils {

    /**
     * Lấy dữ liệu được truyền trong request token
     *
     * @param request
     * @return
     */
    public static String getTokenByRequest(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String token = request.getParameter("token");
        if (token == null) {
            token = request.getHeader("X-Access-Token");
        }
        return token;
    }
    
    /**
     * Lấy dữ liệu được truyền trong request token
     * @return
     */
    public static String getTokenByRequest() {
        String token = null;
        try {
            HttpServletRequest request = SpringContextUtils.getHttpServletRequest();
            token = TokenUtils.getTokenByRequest(request);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return token;
    }

    /**
     * Lấy dữ liệu được truyền trong request tenantId (Tenant ID)
     *
     * @param request
     * @return
     */
    public static String getTenantIdByRequest(HttpServletRequest request) {
        String tenantId = request.getParameter(TenantConstant.TENANT_ID);
        if (tenantId == null) {
            tenantId = oConvertUtils.getString(request.getHeader(CommonConstant.TENANT_ID));
        }

        if (oConvertUtils.isNotEmpty(tenantId) && "undefined".equals(tenantId)) {
            return null;
        }
        //update-begin---author:scott ---date:20261015  for: 【Bảo mật GHSA-2h34-xh29-7w95】Tham số URL tenantId chưa được kiểm chứng khi đăng nhập, ở chế độ đa tenant phải thuộc về người dùng hiện đang đăng nhập, nếu không sẽ bỏ qua tham số và dùng request header (request header đã được ShiroRealm kiểm chứng), để ngăn truyền tham số giả mạo tenant khác-----------
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isNotEmpty(tenantId)) {
            String headerTenantId = oConvertUtils.getString(request.getHeader(CommonConstant.TENANT_ID));
            if (!tenantId.equals(headerTenantId) && !"0".equals(tenantId) && !isLoginUserTenant(tenantId)) {
                log.warn("Tenant ID trong tham số yêu cầu không thuộc về người dùng hiện đang đăng nhập, đã bỏ qua tham số và sử dụng tenant ID trong request header. Tenant tham số:{}, tenant trong request header:{}, yêu cầu:{}", tenantId, headerTenantId, request.getRequestURI());
                return oConvertUtils.isNotEmpty(headerTenantId) && !"undefined".equals(headerTenantId) ? headerTenantId : null;
            }
        }
        //update-end---author:scott ---date:20261015  for: 【Bảo mật GHSA-2h34-xh29-7w95】Tham số URL tenantId chưa được kiểm chứng khi đăng nhập, ở chế độ đa tenant phải thuộc về người dùng hiện đang đăng nhập, nếu không sẽ bỏ qua tham số và dùng request header (request header đã được ShiroRealm kiểm chứng), để ngăn truyền tham số giả mạo tenant khác-----------
        return tenantId;
    }

    //update-begin---author:scott ---date:20261015  for: 【Bảo mật GHSA-2h34-xh29-7w95】Kiểm chứng tenant có thuộc về người dùng hiện đang đăng nhập hay không-----------
    /**
     * Tenant có thuộc về người dùng hiện đang đăng nhập hay không (khi chưa đăng nhập trả về true, giữ nguyên logic cũ, do từng API tự xác thực)
     *
     * @param tenantId Tenant ID
     * @return
     */
    private static boolean isLoginUserTenant(String tenantId) {
        LoginUser loginUser;
        try {
            loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        } catch (Exception e) {
            return true;
        }
        if (loginUser == null) {
            return true;
        }
        String relTenantIds = loginUser.getRelTenantIds();
        return oConvertUtils.isNotEmpty(relTenantIds) && oConvertUtils.isIn(tenantId, relTenantIds.split(","));
    }
    //update-end---author:scott ---date:20261015  for: 【Bảo mật GHSA-2h34-xh29-7w95】Kiểm chứng tenant có thuộc về người dùng hiện đang đăng nhập hay không-----------

    /**
     * Lấy dữ liệu được truyền trong request lowAppId (Ứng dụng low-code ID)
     *
     * @param request
     * @return
     */
    public static String getLowAppIdByRequest(HttpServletRequest request) {
        String lowAppId = request.getParameter(TenantConstant.FIELD_LOW_APP_ID);
        if (lowAppId == null) {
            lowAppId = oConvertUtils.getString(request.getHeader(TenantConstant.X_LOW_APP_ID));
        }
        return lowAppId;
    }

    /**
     * Xác minh Token
     */
    public static boolean verifyToken(HttpServletRequest request, CommonAPI commonApi, RedisUtil redisUtil) {
        log.debug(" -- url --" + request.getRequestURL());
        String token = getTokenByRequest(request);
        return TokenUtils.verifyToken(token, commonApi, redisUtil);
    }

    /**
     * Xác minh Token
     */
    public static boolean verifyToken(String token, CommonAPI commonApi, RedisUtil redisUtil) {
        if (StringUtils.isBlank(token)) {
            throw new JeecgBoot401Exception("token Không được trống!");
        }

        // Giải mã để lấy username, dùng để đối chiếu với cơ sở dữ liệu
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new JeecgBoot401Exception("token Không hợp lệ!");
        }

        // Truy vấn thông tin người dùng
        LoginUser user = TokenUtils.getLoginUser(username, commonApi, redisUtil);
        //LoginUser user = commonApi.getUserByName(username);
        if (user == null) {
            throw new JeecgBoot401Exception("Người dùng không tồn tại!");
        }
        // Kiểm tra trạng thái người dùng
        if (user.getStatus() != 1) {
            throw new JeecgBoot401Exception("Tài khoản đã bị khóa,Vui lòng liên hệ quản trị viên!");
        }
        // Kiểm chứng token có hết hạn hay không & Hoặc tài khoản/mật khẩu có sai hay không
        if (!jwtTokenRefresh(token, username, user.getPassword(), redisUtil)) {
            // Thông tin nhắc nhở token đăng nhập của người dùng đã hết hạn
            String userLoginTokenErrorMsg = oConvertUtils.getString(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN_ERROR_MSG + token));
            throw new JeecgBoot401Exception(oConvertUtils.isEmpty(userLoginTokenErrorMsg)? CommonConstant.TOKEN_IS_INVALID_MSG: userLoginTokenErrorMsg);
        }
        return true;
    }

    /**
     * Làm mới token (đảm bảo người dùng không bị mất kết nối khi đang thao tác trực tuyến)
     * @param token
     * @param userName
     * @param passWord
     * @param redisUtil
     * @return
     */
    private static boolean jwtTokenRefresh(String token, String userName, String passWord, RedisUtil redisUtil) {
        String cacheToken = oConvertUtils.getString(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
        if (oConvertUtils.isNotEmpty(cacheToken)) {
            // Kiểm chứng tính hợp lệ của token
            if (!JwtUtil.verify(cacheToken, userName, passWord)) {
                // Phân tích loại client từ token, giữ nguyên loại client đó khi gia hạn
                String clientType = JwtUtil.getClientType(token);
                String newAuthorization = JwtUtil.sign(userName, passWord, clientType);
                // Đặt thời gian hiệu lực cache tương ứng theo loại client
                long expireTime = CommonConstant.CLIENT_TYPE_APP.equalsIgnoreCase(clientType) 
                    ? JwtUtil.APP_EXPIRE_TIME * 2 / 1000 
                    : JwtUtil.EXPIRE_TIME * 2 / 1000;
                redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, newAuthorization);
                redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, expireTime);
            }
            return true;
        }
        return false;
    }

    /**
     * Lấy người dùng đăng nhập
     *
     * @param commonApi
     * @param username
     * @return
     */
    public static LoginUser getLoginUser(String username, CommonAPI commonApi, RedisUtil redisUtil) {
        LoginUser loginUser = null;
        String loginUserKey = CacheConstant.SYS_USERS_CACHE + "::" + username;
        //【Quan trọng】 Tại đây lấy người dùng đã lưu trong cache trực tiếp qua Redis nhằm giải quyết vấn đề khi dịch vụ system bị sập trong môi trường microservice, các dịch vụ khác không thể gọi lẫn nhau---
        if (redisUtil.hasKey(loginUserKey)) {
            try {
                loginUser = (LoginUser) redisUtil.get(loginUserKey);
                //Giải mã người dùng
                SensitiveInfoUtil.handlerObject(loginUser, false);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            // Truy vấn thông tin người dùng
            loginUser = commonApi.getUserByName(username);
        }
        return loginUser;
    }
}
