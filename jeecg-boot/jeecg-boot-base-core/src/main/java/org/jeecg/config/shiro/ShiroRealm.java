package org.jeecg.config.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @Description: Xác thực đăng nhập người dùng và lấy quyền ủy quyền của người dùng
 * @Author: Scott
 * @Date: 2019-4-23 8:13
 * @Version: 1.1
 */
@Component
@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class ShiroRealm extends AuthorizingRealm {
	@Lazy
    @Resource
    private CommonAPI commonApi;

    @Lazy
    @Resource
    private RedisUtil redisUtil;

    /**
     * Phải ghi đè phương thức này, nếu không Shiro sẽ báo lỗi
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * Xác thực thông tin quyền hạn(bao gồm vai trò và quyền hạn)chỉ khi người dùng truy cập controller mới tiến hành xác thực(redis thông tin quyền hạn được lưu trữ tại đây)
     * Chỉ khi kích hoạt kiểm tra quyền hạn của người dùng mới gọi phương thức này, ví dụ checkRole,checkPermission
     *
     * @param principals Thông tin danh tính
     * @return AuthorizationInfo Thông tin quyền hạn
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        log.debug("===============Shiro Bắt đầu xác thực quyền hạn============ [ roles、permissions]==========");
        String username = null;
        String userId = null;
        if (principals != null) {
            LoginUser sysUser = (LoginUser) principals.getPrimaryPrincipal();
            username = sysUser.getUsername();
            userId = sysUser.getId();
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // Đặt tập hợp vai trò mà người dùng sở hữu, ví dụ “admin,test”
        Set<String> roleSet = commonApi.queryUserRolesById(userId);
        //System.out.println(roleSet.toString());
        info.setRoles(roleSet);

        // Đặt tập hợp quyền hạn mà người dùng sở hữu, ví dụ “sys:role:add,sys:user:add”
        Set<String> permissionSet = commonApi.queryUserAuths(userId);
        info.addStringPermissions(permissionSet);
        //System.out.println(permissionSet);
        log.debug("===============Shiro Xác thực quyền hạn thành công==============");
        return info;
    }

    /**
     * Xác thực thông tin người dùng được tiến hành khi người dùng đăng nhập(không lưu redis)
     * Tức là kiểm tra tài khoản và mật khẩu người dùng nhập có đúng hay không, nếu sai sẽ ném ngoại lệ
     *
     * @param auth Thông tin tài khoản và mật khẩu đăng nhập của người dùng
     * @return Trả về thể hiện AuthenticationInfo đã đóng gói thông tin người dùng
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        log.debug("===============Shiro Bắt đầu xác thực danh tính============doGetAuthenticationInfo==========");
        String token = (String) auth.getCredentials();
        if (token == null) {
            HttpServletRequest req = SpringContextUtils.getHttpServletRequest();
            log.info("————————Xác thực danh tính thất bại——————————Địa chỉ IP:  "+ oConvertUtils.getIpAddrByRequest(req) +"，URL:"+req.getRequestURI());
            throw new AuthenticationException("token Trống!");
        }
        // Kiểm chứng tính hợp lệ của token
        LoginUser loginUser = null;
        try {
            loginUser = this.checkUserTokenIsEffect(token);
        } catch (AuthenticationException e) {
            log.error("—————Kiểm tra check token thất bại——————————"+ e.getMessage(), e);
            // Ném lại ngoại lệ, để JwtFilter xử lý thống nhất, tránh trả về hai lần phản hồi lỗi
            throw e;
        }
        return new SimpleAuthenticationInfo(loginUser, token, getName());
    }

    /**
     * Kiểm tra tính hợp lệ của token
     *
     * @param token
     */
    public LoginUser checkUserTokenIsEffect(String token) throws AuthenticationException {
        // Giải mã để lấy username, dùng để đối chiếu với cơ sở dữ liệu
        String username = JwtUtil.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("Token Không hợp lệ!");
        }

        // Truy vấn thông tin người dùng
        log.debug("———Kiểm tra token có hợp lệ hay không————checkUserTokenIsEffect——————— "+ token);
        LoginUser loginUser = TokenUtils.getLoginUser(username, commonApi, redisUtil);
        //LoginUser loginUser = commonApi.getUserByName(username);
        if (loginUser == null) {
            throw new AuthenticationException("Người dùng không tồn tại!");
        }
        // Kiểm tra trạng thái người dùng
        if (loginUser.getStatus() != 1) {
            throw new AuthenticationException("Tài khoản đã bị khóa,Vui lòng liên hệ quản trị viên!");
        }
        // Kiểm chứng token có hết hạn hay không & Hoặc tài khoản/mật khẩu có sai hay không
        if (!jwtTokenRefresh(token, username, loginUser.getPassword())) {
            // Thông tin nhắc nhở token đăng nhập của người dùng đã hết hạn
            String userLoginTokenErrorMsg = oConvertUtils.getString(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN_ERROR_MSG + token));
            throw new AuthenticationException(oConvertUtils.isEmpty(userLoginTokenErrorMsg)? CommonConstant.TOKEN_IS_INVALID_MSG: userLoginTokenErrorMsg);
        }
        // Giải thích logic mã: Kiểm tra tenant_id của người dùng có khớp với dữ liệu frontend truyền sang hay không
        String userTenantIds = loginUser.getRelTenantIds();
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isNotEmpty(userTenantIds)){
            String contextTenantId = TenantContext.getTenant();
            log.debug("Tenant đăng nhập:" + contextTenantId);
            log.debug("Những tenant mà người dùng sở hữu:" + userTenantIds);
             //Người dùng đăng nhập không có tenant, giá trị ID tenant trong header của frontend là 0
            String str ="0";
            if(oConvertUtils.isNotEmpty(contextTenantId) && !str.equals(contextTenantId)){
                // Giải thích logic mã: /issues/I4O14W Lỗ hổng phán đoán thay đổi thông tin tenant của người dùng
                String[] arr = userTenantIds.split(",");
                if(!oConvertUtils.isIn(contextTenantId, arr)){
                    boolean isAuthorization = false;
                    //========================================================================
                    // Truy vấn thông tin người dùng (nếu tenant không khớp thì truy vấn lại thông tin người dùng từ cơ sở dữ liệu một lần nữa)
                    String loginUserKey = CacheConstant.SYS_USERS_CACHE + "::" + username;
                    redisUtil.del(loginUserKey);
                    LoginUser loginUserFromDb = commonApi.getUserByName(username);
                    if (oConvertUtils.isNotEmpty(loginUserFromDb.getRelTenantIds())) {
                        String[] newArray = loginUserFromDb.getRelTenantIds().split(",");
                        if (oConvertUtils.isIn(contextTenantId, newArray)) { 
                            isAuthorization = true;
                        }
                    }
                    //========================================================================

                    //*********************************************
                    if(!isAuthorization){
                        log.info("Tenant bất thường——Tenant đăng nhập:" + contextTenantId);
                        log.info("Tenant bất thường——Nhóm tenant mà người dùng sở hữu:" + userTenantIds);
                        throw new AuthenticationException("Quyền ủy quyền tenant đăng nhập đã thay đổi, vui lòng đăng nhập lại!");
                    }
                    //*********************************************
                }
            }
        }
        return loginUser;
    }

    /**
     * JWTToken Làm mới vòng đời (thực hiện: chức năng người dùng thao tác trực tuyến không bị ngắt kết nối)
     * 1, sau khi đăng nhập thành công, lưu Token JWT đã tạo của người dùng làm k, v vào trong cache(lúc này giá trị k, v giống nhau), thời gian hiệu lực của cache được đặt bằng 2 lần thời gian hiệu lực của Jwt
     * 2, khi người dùng đó yêu cầu lần nữa, sau khi qua từng lớp kiểm tra của JWTFilter sẽ vào doGetAuthenticationInfo để xác thực danh tính
     * 3, khi token do jwt tạo ra trong lần yêu cầu này của người dùng đã hết hạn, nhưng k tương ứng với token đó trong cache vẫn còn tồn tại, thì nghĩa là người dùng vẫn đang thao tác chỉ là token JWT đã hết hiệu lực, chương trình sẽ tạo lại JWTToken cho giá trị v được ánh xạ bởi k tương ứng với token và ghi đè giá trị v, vòng đời của cache được tính lại
     * 4, khi token do jwt tạo ra trong lần yêu cầu này của người dùng đã hết hạn, và trong cache không tồn tại k tương ứng, thì nghĩa là tài khoản người dùng đã hết hạn do không hoạt động, trả về thông tin người dùng đã hết hiệu lực, vui lòng đăng nhập lại.
     * Lưu ý: Header yêu cầu của frontend đặt Authorization giữ nguyên không đổi, việc kiểm tra tính hợp lệ dựa trên token trong cache.
     *       Thời gian hết hạn của người dùng = Jwt thời gian hiệu lực * 2。
     *
     * @param userName
     * @param passWord
     * @return
     */
    public boolean jwtTokenRefresh(String token, String userName, String passWord) {
        String cacheToken = String.valueOf(redisUtil.get(CommonConstant.PREFIX_USER_TOKEN + token));
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
                log.debug("——————————Người dùng thao tác trực tuyến, cập nhật token để đảm bảo không bị ngắt kết nối—————————jwtTokenRefresh——————— "+ token);
            }
//			else {
//				// Đặt thời gian chờ
//				redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, cacheToken);
//				redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME / 1000);
//			}
            return true;
        }

        //redis trong đó không tồn tại TOEKN này, nghĩa là token không hợp lệ, trả về false
        return false;
    }

    /**
     * Xóa cache xác thực quyền hạn của người dùng hiện tại
     *
     * @param principals Thông tin quyền hạn
     */
    @Override
    public void clearCache(PrincipalCollection principals) {
        super.clearCache(principals);
        // Giải thích logic mã: 【TV360X-1320】Việc phân quyền phải thoát ra và đăng nhập lại mới có hiệu lực, gây nhiều phiền phức cho người dùng---
        super.clearCachedAuthorizationInfo(principals);
    }
}
