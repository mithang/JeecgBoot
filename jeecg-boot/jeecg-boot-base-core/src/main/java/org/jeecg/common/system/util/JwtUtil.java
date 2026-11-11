package org.jeecg.common.system.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SpringContextUtils;
import org.jeecg.common.util.oConvertUtils;

/**
 * @Author Scott
 * @Date 2018-07-12 14:23
 * @Desc JWT Lớp công cụ
 **/
@Slf4j
public class JwtUtil {

	/**PC, Token có hiệu lực 7 ngày (thời gian Token được cache trong Redis gấp đôi)*/
	public static final long EXPIRE_TIME = (7 * 12) * 60 * 60 * 1000L;
	/**APP, Token có hiệu lực 30 ngày (thời gian Token được cache trong Redis gấp đôi)*/
	public static final long APP_EXPIRE_TIME = (30 * 12) * 60 * 60 * 1000L;
	static final String WELL_NUMBER = SymbolConstant.WELL_NUMBER + SymbolConstant.LEFT_CURLY_BRACKET;

    /**
     *
     * @param response
     * @param code
     * @param errorMsg
     */
	public static void responseError(HttpServletResponse response, Integer code, String errorMsg) {
		try {
			Result jsonResult = new Result(code, errorMsg);
			jsonResult.setSuccess(false);
			
			// Đặt response header và loại nội dung
			response.setStatus(code);
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			response.setContentType("application/json;charset=UTF-8");
			// Sử dụng ObjectMapper để tuần tự hóa thành chuỗi JSON
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(jsonResult);
			response.getWriter().write(json);
			response.getWriter().flush();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Kiểm chứng token có đúng không
	 *
	 * @param token  Khóa
	 * @param secret Mật khẩu của người dùng
	 * @return Có đúng không
	 */
	public static boolean verify(String token, String username, String secret) {
		try {
			// Tạo bộ kiểm chứng JWT theo mật khẩu
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm).withClaim("username", username).build();
			// Kiểm chứng TOKEN
			DecodedJWT jwt = verifier.verify(token);
			return true;
		} catch (Exception e) {
			log.warn("Token Xác thực thất bại:" + e.getMessage(),e);
			return false;
		}
	}

	/**
	 * Lấy thông tin trong token không cần giải mã bằng secret cũng có được
	 *
	 * @return token tên người dùng chứa trong đó
	 */
	public static String getUsername(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			return jwt.getClaim("username").asString();
		} catch (JWTDecodeException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Tạo chữ ký,5min hết hạn sau
	 *
	 * @param username Tên người dùng
	 * @param secret   Mật khẩu của người dùng
	 * @return Đã mã hóa token
	 * @deprecated Vui lòng sử dụng sign(String username, String secret, String clientType)phương thức thay thế
	 */
	@Deprecated
	public static String sign(String username, String secret) {
		Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		// Kèm theo thông tin username
		return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);

	}


	/**
	 * Tạo chữ ký,5min hết hạn sau
	 *
	 * @param username Tên người dùng
	 * @param secret   Mật khẩu của người dùng
	 * @param expireTime Thời gian hết hạn
	 * @return Đã mã hóa token
	 * @deprecated Vui lòng sử dụng sign(String username, String secret, String clientType)phương thức thay thế
	 */
	@Deprecated
	public static String sign(String username, String secret, Long expireTime) {
		Date date = new Date(System.currentTimeMillis() + expireTime);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		// Kèm theo thông tin username
		return JWT.create().withClaim("username", username).withExpiresAt(date).sign(algorithm);

	}

	/**
	 * Tạo chữ ký, tự động chọn thời gian hết hạn theo loại client
	 * for [JHHB-1030]【Xác thực】 Thời gian gia hạn sau khi token của người dùng mobile hết hạn trở thành thời lượng của PC
	 *
	 * @param username Tên người dùng
	 * @param secret   Mật khẩu của người dùng
	 * @param clientType Loại client (PC hoặc APP)
	 * @return Đã mã hóa token
	 */
	public static String sign(String username, String secret, String clientType) {
		// Chọn thời gian hết hạn tương ứng theo loại client
		long expireTime = CommonConstant.CLIENT_TYPE_APP.equalsIgnoreCase(clientType) 
			? APP_EXPIRE_TIME 
			: EXPIRE_TIME;
		Date date = new Date(System.currentTimeMillis() + expireTime);
		Algorithm algorithm = Algorithm.HMAC256(secret);
		// Kèm theo thông tin username và clientType
		return JWT.create()
			.withClaim("username", username)
			.withClaim("clientType", clientType)
			.withExpiresAt(date)
			.sign(algorithm);
	}

	/**
	 * Lấy loại client từ token
	 * for [JHHB-1030]【Xác thực】 Thời gian gia hạn sau khi token của người dùng mobile hết hạn trở thành thời lượng của PC
	 *
	 * @param token JWT token
	 * @return Loại client, nếu không tồn tại thì trả về PC (tương thích token cũ)
	 */
	public static String getClientType(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);
			String clientType = jwt.getClaim("clientType").asString();
			// Nếu clientType rỗng, trả về giá trị mặc định PC (tương thích token cũ)
			return oConvertUtils.isNotEmpty(clientType) ? clientType : CommonConstant.CLIENT_TYPE_PC;
		} catch (JWTDecodeException e) {
			log.warn("Phân tích clientType trong token thất bại, sử dụng giá trị mặc định PC:" + e.getMessage());
			return CommonConstant.CLIENT_TYPE_PC;
		}
	}

	/**
	 * Lấy tài khoản người dùng theo token trong request
	 * 
	 * @param request
	 * @return
	 * @throws JeecgBootException
	 */
	public static String getUserNameByToken(HttpServletRequest request) throws JeecgBootException {
		String accessToken = request.getHeader("X-Access-Token");
		String username = getUsername(accessToken);
		if (oConvertUtils.isEmpty(username)) {
			throw new JeecgBootException("Chưa lấy được người dùng");
		}
		return username;
	}
	
	/**
	  *  Lấy biến từ session
	 * @param key
	 * @return
	 */
	public static String getSessionData(String key) {
		//${myVar}%
		//Lấy được${} Giá trị phía sau
		String moshi = "";
		String wellNumber = WELL_NUMBER;

		if(key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		if (key.contains(wellNumber)) {
			key = key.substring(2,key.indexOf("}"));
		}
		if (oConvertUtils.isNotEmpty(key)) {
			HttpSession session = SpringContextUtils.getHttpServletRequest().getSession();
			returnValue = (String) session.getAttribute(key);
		}
		//Kết quả cộng thêm${} Giá trị phía sau
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
	/**
	  * Lấy biến từ người dùng hiện tại
	 * @param key
	 * @param user
	 * @return
	 */
	public static String getUserSystemData(String key, SysUserCacheInfo user) {
		//1.Ưu tiên lấy SysUserCacheInfo
		if(user==null) {
			try {
				user = JeecgDataAutorUtils.loadUserInfo();
			} catch (Exception e) {
				log.warn("Lấy thông tin người dùng bất thường：" + e.getMessage());
			}
		}
		//2.Lấy thông tin người dùng đăng nhập qua shiro
		LoginUser sysUser = null;
		try {
			sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		} catch (Exception e) {
			log.warn("SecurityUtils.getSubject() Lấy thông tin người dùng bất thường：" + e.getMessage());
		}

		//#{sys_user_code}%
		String moshi = "";
        String wellNumber = WELL_NUMBER;
		if(key.indexOf(SymbolConstant.RIGHT_CURLY_BRACKET)!=-1){
			 moshi = key.substring(key.indexOf("}")+1);
		}
		String returnValue = null;
		//Xử lý đối với ký hiệu đặc biệt#{sysOrgCode}, xác định và thay thế
		if (key.contains(wellNumber)) {
			key = key.substring(2,key.indexOf("}"));
		} else {
			key = key;
		}
		// Có tồn tại cờ chuỗi không
		boolean multiStr;
		if(oConvertUtils.isNotEmpty(key) && key.trim().matches("^\\[\\w+]$")){
			key = key.substring(1,key.length()-1);
			multiStr = true;
		} else {
            multiStr = false;
        }
		//Thay thế bằng thời gian hệ thống hiện tại(Năm tháng ngày)
		if (key.equals(DataBaseConstant.SYS_DATE)|| key.toLowerCase().equals(DataBaseConstant.SYS_DATE_TABLE)) {
			returnValue = DateUtils.formatDate();
		}
		//Thay thế bằng thời gian hệ thống hiện tại (năm-tháng-ngày giờ-phút-giây)
		else if (key.equals(DataBaseConstant.SYS_TIME)|| key.toLowerCase().equals(DataBaseConstant.SYS_TIME_TABLE)) {
			returnValue = DateUtils.now();
		}
		//Giá trị mặc định của trạng thái quy trình (mặc định chưa khởi tạo)
		else if (key.equals(DataBaseConstant.BPM_STATUS)|| key.toLowerCase().equals(DataBaseConstant.BPM_STATUS_TABLE)) {
			returnValue = "1";
		}

		//Tác vụ nền lấy thông tin người dùng bị lỗi, khiến chương trình bị gián đoạn
		if(sysUser==null && user==null){
			return null;
		}
		
		//Thay thế bằng tài khoản người dùng đăng nhập hệ thống
		if (key.equals(DataBaseConstant.SYS_USER_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getUsername();
			}else {
				returnValue = user.getSysUserCode();
			}
		}

		// Thay thế bằng người dùng đăng nhập hệ thống ID
		else if (key.equals(DataBaseConstant.SYS_USER_ID) || key.equalsIgnoreCase(DataBaseConstant.SYS_USER_ID_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getId();
			}else {
				returnValue = user.getSysUserId();
			}
		}

		//Thay thế bằng tên thật của người dùng đăng nhập hệ thống
		else if (key.equals(DataBaseConstant.SYS_USER_NAME)|| key.toLowerCase().equals(DataBaseConstant.SYS_USER_NAME_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getRealname();
			}else {
				returnValue = user.getSysUserName();
			}
		}
		
		//Thay thế bằng mã cơ quan mà người dùng hệ thống dùng để đăng nhập
		else if (key.equals(DataBaseConstant.SYS_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_ORG_CODE_TABLE)) {
			if(user==null) {
				returnValue = sysUser.getOrgCode();
			}else {
				returnValue = user.getSysOrgCode();
			}
		}

		// Thay thế bằng cơ quan mà người dùng hệ thống dùng để đăng nhập ID
		else if (key.equals(DataBaseConstant.SYS_ORG_ID) || key.equalsIgnoreCase(DataBaseConstant.SYS_ORG_ID_TABLE)) {
			if (user == null) {
				returnValue = sysUser.getOrgId();
			} else {
				returnValue = user.getSysOrgId();
			}
		}

		//Thay thế bằng tất cả mã cơ quan mà người dùng hệ thống sở hữu
		else if (key.equals(DataBaseConstant.SYS_MULTI_ORG_CODE)|| key.toLowerCase().equals(DataBaseConstant.SYS_MULTI_ORG_CODE_TABLE)) {
			if(user==null){
				//TODO Tạm thời dùng phòng ban đăng nhập của người dùng, tồn tại khiếm khuyết logic, không phải là phòng ban mà người dùng sở hữu
				returnValue = sysUser.getOrgCode();
				// Giải thích logic mã: [QQYUN-10785]Quyền dữ liệu, có vấn đề khi xem quyền của phòng ban mình sở hữu #7288------------
				returnValue = multiStr ? "'" + returnValue + "'" : returnValue;
			}else{
				if(user.isOneDepart()) {
					returnValue = user.getSysMultiOrgCode().get(0);
					// Giải thích logic mã: [QQYUN-10785]Quyền dữ liệu, có vấn đề khi xem quyền của phòng ban mình sở hữu #7288------------
					returnValue = multiStr ? "'" + returnValue + "'" : returnValue;
				}else {
					// Giải thích logic mã: [QQYUN-10785]Quyền dữ liệu, có vấn đề khi xem quyền của phòng ban mình sở hữu #7288------------
					returnValue = user.getSysMultiOrgCode().stream()
							.filter(Objects::nonNull)
							.map(orgCode -> {
								if (multiStr) {
									return "'" + orgCode + "'";
								} else {
									return orgCode;
								}
							})
							.collect(Collectors.joining(", "));
				}
			}
		}

		// Thay thế bằng role code của người dùng hiện đang đăng nhập (nhiều code phân cách bằng dấu phẩy)
		else if (key.equals(DataBaseConstant.SYS_ROLE_CODE) || key.equalsIgnoreCase(DataBaseConstant.SYS_ROLE_CODE_TABLE)) {
			if (user == null) {
				returnValue = sysUser.getRoleCode();
			} else {
				returnValue = user.getSysRoleCode();
			}
		}

		// Giải thích logic mã: ID đa tenant làm biến hệ thống
		else if (key.equals(TenantConstant.TENANT_ID) || key.toLowerCase().equals(TenantConstant.TENANT_ID_TABLE)){
			try {
				returnValue = SpringContextUtils.getHttpServletRequest().getHeader(CommonConstant.TENANT_ID);
			} catch (Exception e) {
				log.warn("Lỗi khi lấy tenant hệ thống:" + e.getMessage());
			}
		}
		if(returnValue!=null){returnValue = returnValue + moshi;}
		return returnValue;
	}
	
//	public static void main(String[] args) {
//		 String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE1NjUzMzY1MTMsInVzZXJuYW1lIjoiYWRtaW4ifQ.xjhud_tWCNYBOg_aRlMgOdlZoWFFKB_givNElHNw3X0";
//		 System.out.println(JwtUtil.getUsername(token));
//	}
}
