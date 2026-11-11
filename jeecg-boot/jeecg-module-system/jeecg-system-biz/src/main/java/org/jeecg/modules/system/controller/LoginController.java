package org.jeecg.modules.system.controller;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.enums.DySmsEnum;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.jeecg.common.util.encryption.EncryptedString;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.shiro.IgnoreAuth;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.constant.DefIndexConst;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysRoleIndex;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.service.impl.SysBaseApiImpl;
import org.jeecg.modules.system.util.RandImageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Tag(name="Đăng nhập người dùng")
@Slf4j
public class LoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysPermissionService sysPermissionService;
	@Autowired
	private SysBaseApiImpl sysBaseApi;
	@Autowired
	private ISysLogService logService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
    private ISysDepartService sysDepartService;
	@Autowired
    private ISysDictService sysDictService;
	@Autowired
	private ISysUserTenantService sysUserTenantService;
	@Resource
	private BaseCommonService baseCommonService;
	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;
	
	private final String BASE_CHECK_CODES = "qwertyuiplkjhgfdsazxcvbnmQWERTYUPLKJHGFDSAZXCVBNM1234567890";
	/**
	 * Thread pool dùng để gửi biên bản bất đồng bộ
	 */
	public static ExecutorService cachedThreadPool = new ShiroThreadPoolExecutor(0, 1024, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());

	

	@Operation(summary="API đăng nhập")
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Result<JSONObject> login(@RequestBody SysLoginModel sysLoginModel, HttpServletRequest request){
		Result<JSONObject> result = new Result<>();
		String username = sysLoginModel.getUsername();
		// Truyền mật khẩu đã mã hóa(Thử giải mã AES, thất bại thì coi là văn bản thuần)
		String password  = AesEncryptUtil.resolvePassword(sysLoginModel.getPassword());
		log.debug("Mật khẩu đăng nhập, mật khẩu gốc:{}, mật khẩu đã giải mã:{}" , sysLoginModel.getPassword(), password);

		//step.1 Đăng nhập thất bại vượt quá 5 lần sẽ khóa người dùng trong 10 phút
		if(isLoginFailOvertimes(username)){
			return result.error500("Người dùng này đã đăng nhập thất bại quá nhiều lần, vui lòng đăng nhập lại sau 10 phút!");
		}

		// step.2 Kiểm tra mã xác thực
        String realKey = validateCaptcha(sysLoginModel, result);
        if (realKey == null) {
            return result;
        }
		
		//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Kiểm tra mật khẩu trước rồi mới kiểm tra trạng thái người dùng; người dùng không tồn tại và mật khẩu sai dùng chung một thông báo và được tính vào số lần thất bại, ngăn liệt kê người dùng-----------
		// step.3 Kiểm tra tên người dùng hoặc mật khẩu có đúng hay không
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysUser::getUsername,username);
		SysUser sysUser = sysUserService.getOne(queryWrapper);
		if (!sysUserService.verifyLoginPassword(sysUser, username, password)) {
			result.error500("Tên người dùng hoặc mật khẩu sai");
			return result;
		}

		// step.4 Kiểm tra người dùng có hợp lệ không
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Kiểm tra mật khẩu trước rồi mới kiểm tra trạng thái người dùng; người dùng không tồn tại và mật khẩu sai dùng chung một thông báo và được tính vào số lần thất bại, ngăn liệt kê người dùng-----------

		// step.5 Đăng nhập thành công — Lấy thông tin người dùng
		String loginOrgCode = sysLoginModel.getLoginOrgCode();
		sysUser.setLoginOrgCode(loginOrgCode);
		userInfo(sysUser, result, request, CommonConstant.CLIENT_TYPE_PC);

		// step.6  Đăng nhập thành công, xóa mã xác minh
		redisUtil.del(realKey);
		redisUtil.del(CommonConstant.LOGIN_FAIL + username);

		// step.7 Ghi nhật ký đăng nhập người dùng
		LoginUser loginUser = new LoginUser();
		BeanUtils.copyProperties(sysUser, loginUser);
		baseCommonService.addLog("Tên người dùng: " + username + ",Đăng nhập thành công!", CommonConstant.LOG_TYPE_1, null,loginUser);
		return result;
	}


	/**
	 * [Chuyên dùng cho vue3] Lấy thông tin người dùng
	 */
	@GetMapping("/user/getUserInfo")
	public Result<JSONObject> getUserInfo(HttpServletRequest request){
		long start = System.currentTimeMillis();
		Result<JSONObject> result = new Result<JSONObject>();
		String  username = JwtUtil.getUserNameByToken(request);
		if(oConvertUtils.isNotEmpty(username)) {
			// Truy vấn thông tin người dùng theo tên người dùng
			SysUser sysUser = sysUserService.getUserByName(username);
			JSONObject obj=new JSONObject();
			log.debug("1 Thời gian lấy thông tin người dùng (thông tin cơ bản của người dùng)" + (System.currentTimeMillis() - start) + "Mili giây");

			// Giải thích logic mã: vue3 Frontend hỗ trợ trang chủ tùy chỉnh-----------
			String vue3Version = request.getHeader(CommonConstant.VERSION);
			SysRoleIndex roleIndex = sysUserService.getDynamicIndexByUserRole(username, vue3Version);
			if (oConvertUtils.isNotEmpty(vue3Version) && roleIndex != null && oConvertUtils.isNotEmpty(roleIndex.getUrl())) {
				String homePath = roleIndex.getUrl();
				if (!homePath.startsWith(SymbolConstant.SINGLE_SLASH)) {
					homePath = SymbolConstant.SINGLE_SLASH + homePath;
				}
				sysUser.setHomePath(homePath);
			}
			log.debug("2 Thời gian lấy thông tin người dùng (Cấu hình trang đầu)" + (System.currentTimeMillis() - start) + "Mili giây");
			
			obj.put("userInfo",sysUser);
			//update-begin---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
			obj.put("dictVersion", sysDictService.getDictVersion());
			//update-end---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
			obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
			log.debug("3 Thời gian lấy thông tin người dùng (Dữ liệu từ điển)" + (System.currentTimeMillis() - start) + "Mili giây");

			//update-begin---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba, trả về cho frontend-----------
			boolean enableStrongPwd = jeecgBaseConfig.getFirewall() != null
					&& Boolean.TRUE.equals(jeecgBaseConfig.getFirewall().getEnableStrongPwd());
			obj.put("enableStrongPwd", enableStrongPwd);
			//update-end---author:wangshuai ---date:2026-06-29  for：【QQYUN-16619】Công tắc độ mạnh mật khẩu theo cấp bảo mật ba, trả về cho frontend-----------

			result.setResult(obj);
			result.success("");
		}
		log.debug("end Thời gian lấy thông tin người dùng " + (System.currentTimeMillis() - start) + "Mili giây");
		return result;

	}
	
	/**
	 * Đăng xuất
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public Result<Object> logout(HttpServletRequest request,HttpServletResponse response) {
		//Logic thoát của người dùng
	    String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
	    if(oConvertUtils.isEmpty(token)) {
	    	return Result.error("Đăng xuất thất bại!");
	    }
	    String username = JwtUtil.getUsername(token);
		LoginUser sysUser = sysBaseApi.getUserByName(username);
	    if(sysUser!=null) {
			//update-begin---author:zhangdaihao ---date:2026-04-15  for：【issue/9517] Kiểm tra chữ ký token, tránh giả mạo token để ép người khác offline(DoS)-----------
			if (!JwtUtil.verify(token, username, sysUser.getPassword())) {
				return Result.error("Token Không hợp lệ!");
			}
			//update-end---author:zhangdaihao ---date:2026-04-15  for：【issue/9517] Kiểm tra chữ ký token, tránh giả mạo token để ép người khác offline(DoS)-----------
			asyncClearLogoutCache(token, sysUser); // Dọn dẹp không đồng bộ
			SecurityUtils.getSubject().logout();
	    	return Result.ok("Đăng xuất thành công!");
	    }else {
	    	return Result.error("Token Không hợp lệ!");
	    }
	}

	/**
	 * Dọn bộ nhớ đệm người dùng
	 * 
	 * @param token
	 * @param sysUser
	 */
	private void asyncClearLogoutCache(String token, LoginUser sysUser) {
		cachedThreadPool.execute(()->{
			//Xóa bộ nhớ đệm Token đăng nhập của người dùng
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + token);
			//Xóa cache quyền Shiro khi người dùng đăng nhập
			redisUtil.del(CommonConstant.PREFIX_USER_SHIRO_CACHE + sysUser.getId());
			//Xóa thông tin cache của người dùng (bao gồm thông tin phòng ban), ví dụ sys:cache:user::<username>
			redisUtil.del(String.format("%s::%s", CacheConstant.SYS_USERS_CACHE, sysUser.getUsername()));
			//Xóa bộ nhớ đệm cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi (phía PC và phía APP)
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN_PC + sysUser.getUsername());
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN_APP + sysUser.getUsername());
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN_PHONE + sysUser.getUsername());

			// Xóa bộ nhớ đệm trang chủ mặc định của người dùng
			redisUtil.del(DefIndexConst.CACHE_TYPE + sysUser.getUsername());
			baseCommonService.addLog("Tên người dùng: "+sysUser.getRealname()+",Đăng xuất thành công!", CommonConstant.LOG_TYPE_1, null, sysUser);
			log.debug("[Thao tác thoát thành công] Xử lý bất đồng bộ, sau khi thoát thì dọn bộ nhớ đệm người dùng: "+sysUser.getRealname());
		});
	}
	
	/**
	 * Lấy lượng truy cập
	 * @return
	 */
	@GetMapping("loginfo")
	public Result<JSONObject> loginfo() {
		Result<JSONObject> result = new Result<JSONObject>();
		JSONObject obj = new JSONObject();
		// Lấy thời gian bắt đầu và kết thúc của một ngày
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date dayStart = calendar.getTime();
		calendar.add(Calendar.DATE, 1);
		Date dayEnd = calendar.getTime();
		// Lấy bản ghi truy cập hệ thống
		Long totalVisitCount = logService.findTotalVisitCount();
		obj.put("totalVisitCount", totalVisitCount);
		Long todayVisitCount = logService.findTodayVisitCount(dayStart,dayEnd);
		obj.put("todayVisitCount", todayVisitCount);
		Long todayIp = logService.findTodayIp(dayStart,dayEnd);
		obj.put("todayIp", todayIp);
		result.setResult(obj);
		result.success("Đăng nhập thành công");
		return result;
	}
	
	/**
	 * Lấy lượng truy cập
	 * @return
	 */
	@GetMapping("/visitInfo")
	public Result<List<Map<String,Object>>> visitInfo() {
		Result<List<Map<String,Object>>> result = new Result<List<Map<String,Object>>>();
		Calendar calendar = new GregorianCalendar();
		calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date dayEnd = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date dayStart = calendar.getTime();
        List<Map<String,Object>> list = logService.findVisitCount(dayStart, dayEnd);
		result.setResult(oConvertUtils.toLowerCasePageList(list));
		return result;
	}
	
	
	/**
	 * Đăng nhập thành công, chọn phòng ban hiện tại của người dùng
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/selectDepart", method = RequestMethod.PUT)
	public Result<JSONObject> selectDepart(@RequestBody SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		//update-begin---author:zhangdaihao ---date:20260506  for：【issue/9597] Sửa lỗ hổng vượt quyền selectDepart: bắt buộc dùng người dùng đang đăng nhập, kiểm tra orgCode/loginTenantId thuộc về-----------
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (loginUser == null) {
			return Result.error("Người dùng chưa đăng nhập");
		}
		// Bắt buộc dùng username của người dùng đang đăng nhập, bỏ qua username truyền vào trong request body, ngăn sửa trái phép phòng ban hiện tại của người khác/Tenant
		String username = loginUser.getUsername();

		//Lấy phòng ban đăng nhập
		String orgCode = user.getOrgCode();
		//Lấy tenant đăng nhập
		Integer tenantId = user.getLoginTenantId();

		// Kiểm tra orgCode phải thuộc phòng ban của người dùng hiện tại
		if (oConvertUtils.isNotEmpty(orgCode)) {
			List<SysDepart> userDeparts = sysDepartService.queryUserDeparts(loginUser.getId());
			boolean orgOwned = userDeparts != null && userDeparts.stream()
					.anyMatch(d -> orgCode.equals(d.getOrgCode()));
			if (!orgOwned) {
				return Result.error("Không có quyền chuyển sang phòng ban không thuộc về bản thân");
			}
		}

		// Kiểm tra loginTenantId phải thuộc thuê bao của người dùng hiện tại
		if (tenantId != null && tenantId != 0) {
			List<Integer> userTenantIds = sysUserTenantService.getTenantIdsByUserId(loginUser.getId());
			if (userTenantIds == null || !userTenantIds.contains(tenantId)) {
				return Result.error("Không có quyền chuyển sang tenant không thuộc về bản thân");
			}
		}
		//update-end---author:zhangdaihao ---date:20260506  for：【issue/9597] Sửa lỗ hổng vượt quyền selectDepart: bắt buộc dùng người dùng đang đăng nhập, kiểm tra orgCode/loginTenantId thuộc về-----------

		//Thiết lập phòng ban đăng nhập và tenant đăng nhập của người dùng
		this.sysUserService.updateUserDepart(username, orgCode,tenantId);
		SysUser sysUser = sysUserService.getUserByName(username);
		JSONObject obj = new JSONObject();
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		return result;
	}

	/**
	 * API đăng nhập bằng SMS
	 * 
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sms")
	public Result<String> sms(@RequestBody JSONObject jsonObject,HttpServletRequest request) {
		Result<String> result = new Result<String>();
		String clientIp = IpUtils.getIpAddr(request);
		String mobile = jsonObject.get("mobile").toString();
		//Chế độ số điện thoại — Chế độ đăng nhập: "2"  Chế độ đăng ký: "1"
		String smsmode=jsonObject.get("smsmode").toString();
		log.info("-------- IP:{}, Số điện thoại:{}, lấy mã xác minh liên kết", clientIp, mobile);
		
		if(oConvertUtils.isEmpty(mobile)){
			result.setMessage("Số điện thoại không được để trống!");
			result.setSuccess(false);
			return result;
		}
		
		// VUEN-2245【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+mobile;
		Object object = redisUtil.get(redisKey);
		
		if (object != null) {
			result.setMessage("Mã xác minh vẫn còn hiệu lực trong 10 phút!");
			result.setSuccess(false);
			return result;
		}

		//-------------------------------------------------------------------------------------
		//Thêm check để ngăn chặn việc gửi SMS ác ý
		if(!DySmsLimit.canSendSms(clientIp)){
			log.warn("--------[Cảnh báo] IP Địa chỉ:{}, Yêu cầu giao diện SMS quá nhiều-------", clientIp);
			result.setMessage("Yêu cầu giao diện SMS quá nhiều, vui lòng thử lại sau!");
			result.setCode(CommonConstant.PHONE_SMS_FAIL_CODE);
			result.setSuccess(false);
			return result;
		}
		//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn tốc độ theo số điện thoại, không phụ thuộc vào IP máy khách IP-----------
		if(!DySmsLimit.canSendSmsToPhone(mobile)){
			result.setMessage("Gửi SMS quá thường xuyên, vui lòng thử lại sau!");
			result.setCode(CommonConstant.PHONE_SMS_FAIL_CODE);
			result.setSuccess(false);
			return result;
		}
		//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn tốc độ theo số điện thoại, không phụ thuộc vào IP máy khách IP-----------
		//-------------------------------------------------------------------------------------

		//Số ngẫu nhiên
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
    	obj.put("code", captcha);
		try {
			boolean b = false;
			//Mẫu đăng ký
			if (CommonConstant.SMS_TPL_TYPE_1.equals(smsmode)) {
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				if(sysUser!=null) {
					result.error500(" Số điện thoại đã được đăng ký, vui lòng đăng nhập trực tiếp!");
					baseCommonService.addLog("Số điện thoại đã được đăng ký, vui lòng đăng nhập trực tiếp!", CommonConstant.LOG_TYPE_1, null);
					return result;
				}
				b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.REGISTER_TEMPLATE_CODE);
			}else {
				//Chế độ đăng nhập, kiểm tra tính hợp lệ của người dùng
				SysUser sysUser = sysUserService.getUserByPhone(mobile);
				result = sysUserService.checkUserIsEffective(sysUser);
				if(!result.isSuccess()) {
					String message = result.getMessage();
					String userNotExist="Người dùng này không tồn tại, vui lòng đăng ký";
					if(userNotExist.equals(message)){
						result.error500("Người dùng này không tồn tại hoặc chưa liên kết số điện thoại");
					}
					return result;
				}
				
				/**
				 * smsmode Phương thức mẫu SMS  0 .Mẫu đăng nhập、1.Mẫu đăng ký、2.Mẫu quên mật khẩu
				 */
				if (CommonConstant.SMS_TPL_TYPE_0.equals(smsmode)) {
					//Mẫu đăng nhập
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
				} else if(CommonConstant.SMS_TPL_TYPE_2.equals(smsmode)) {
					//Mẫu quên mật khẩu
					b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
                    // Giải thích logic mã: 【issues/8567】Nghiêm trọng: sửa mật khẩu tồn tại vấn đề vượt quyền theo chiều ngang.---
                    if(b){
                        String username = sysUser.getUsername();
                        obj.put("username",username);
                        redisUtil.set(redisKey, obj.toJSONString(), 600);
                        result.setSuccess(true);
                        return result;
                    }
                }
			}

			if (b == false) {
				result.setMessage("Gửi mã xác minh SMS thất bại,Vui lòng thử lại sau");
				result.setSuccess(false);
				return result;
			}
			
			//Mã xác minh có hiệu lực trong 10 phút
			redisUtil.set(redisKey, captcha, 600);
			result.setSuccess(true);

		} catch (ClientException e) {
			e.printStackTrace();
			result.error500(" Giao diện SMS chưa được cấu hình, vui lòng liên hệ quản trị viên!");
			return result;
		}
		return result;
	}
	

	/**
	 * API đăng nhập bằng số điện thoại
	 * 
	 * @param jsonObject
	 * @return
	 */
	@Operation(summary="API đăng nhập bằng số điện thoại")
	@PostMapping("/phoneLogin")
	public Result<JSONObject> phoneLogin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
		Result<JSONObject> result = new Result<JSONObject>();
		String phone = jsonObject.getString("mobile");
		// Khóa người dùng khi người dùng nền tảng đăng nhập thất bại
		if(isLoginFailOvertimes(phone)){
			return result.error500("Người dùng này đã đăng nhập thất bại quá nhiều lần, vui lòng đăng nhập lại sau 10 phút!");
		}
		
		//Kiểm tra tính hợp lệ của người dùng
		SysUser sysUser = sysUserService.getUserByPhone(phone);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		
		String smscode = jsonObject.getString("captcha");

		// Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);

		if (!smscode.equals(code)) {
			addLoginFailOvertimes(phone);
			return Result.error("Mã xác minh điện thoại sai");
		}
		//Thông tin người dùng
		String loginOrgCode = jsonObject.getString("loginOrgCode");
		sysUser.setLoginOrgCode(loginOrgCode);
		userInfo(sysUser, result, request, CommonConstant.CLIENT_TYPE_PHONE);
		//Thêm nhật ký
		baseCommonService.addLog("Tên người dùng: " + sysUser.getUsername() + ",Đăng nhập thành công!", CommonConstant.LOG_TYPE_1, null);
        redisUtil.removeAll(redisKey);
		return result;
	}


	/**
	 * Thông tin người dùng
	 *
	 * @param sysUser
	 * @param result
	 * @return
	 */
	private Result<JSONObject> userInfo(SysUser sysUser, Result<JSONObject> result, HttpServletRequest request, String clientType) {
		String username = sysUser.getUsername();
		String syspassword = sysUser.getPassword();
		JSONObject obj = new JSONObject(new LinkedHashMap<>());

		//1.Tạo token và đặt thời gian timeout
		String token = JwtUtil.sign(username, syspassword, clientType);
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		// Đặt thời gian hết hạn tương ứng theo loại client
		long expireTime = CommonConstant.CLIENT_TYPE_APP.equalsIgnoreCase(clientType) 
			? JwtUtil.APP_EXPIRE_TIME * 2 / 1000 
			: JwtUtil.EXPIRE_TIME * 2 / 1000;
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, expireTime);
		obj.put("token", token);

		// Có cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi hay không, đăng xuất phiên đăng nhập trước đó
		handleSingleSignOn(username, token, clientType);

		//2.Đặt tenant đăng nhập
		Result<JSONObject> loginTenantError = sysUserService.setLoginTenant(sysUser, obj, username,result);
		if (loginTenantError != null) {
			return loginTenantError;
		}

		//3.Đặt thông tin người dùng đăng nhập
		obj.put("userInfo", sysUser);
		
		//4.Đặt phòng ban đăng nhập
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
		obj.put("departs", departs);
		if (departs == null || departs.size() == 0) {
			obj.put("multi_depart", 0);
			sysUserService.updateUserDepart(username, null, null);
		} else if (departs.size() == 1) {
			sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(),null);
			obj.put("multi_depart", 1);
		} else {
			//Truy vấn hiện tại có phòng ban đăng nhập hay không
			SysUser sysUserById = sysUserService.getById(sysUser.getId());
			//Hỗ trợ chọn phòng ban ở trang đăng nhập
			String loginOrgCode = sysUser.getLoginOrgCode();

			// Kiểm tra orgCode của phòng ban đăng nhập lần trước có nằm trong departs hay không
			boolean orgCodeInDeparts = departs.stream().anyMatch(d -> sysUserById.getOrgCode() != null && sysUserById.getOrgCode().equalsIgnoreCase(d.getOrgCode()));
			if (!orgCodeInDeparts) {
				sysUserById.setOrgCode(null);
			}

			// Nếu chưa thiết lập phòng ban đăng nhập, hãy đặt phòng ban đăng nhập thành loginOrgCode mà người dùng đã chọn (ưu tiên), nếu không thì đặt thành phòng ban đầu tiên mặc định
			if(oConvertUtils.isEmpty(sysUserById.getOrgCode())){
				String orgCode = oConvertUtils.isNotEmpty(loginOrgCode) ? loginOrgCode : departs.get(0).getOrgCode();
				sysUserService.updateUserDepart(username, orgCode, null);
			} else {
				// Đã thiết lập phòng ban đăng nhập; nếu lần đăng nhập này người dùng chọn phòng ban khác thì ưu tiên dùng loginOrgCode mà người dùng đã chọn để cập nhật phòng ban đăng nhập
				String orgCode = sysUserById.getOrgCode();
				if(oConvertUtils.isNotEmpty(loginOrgCode) && !orgCode.equalsIgnoreCase(loginOrgCode)){
					sysUserService.updateUserDepart(username, loginOrgCode, null);
				}
			}
			obj.put("multi_depart", 2);
		}

		// 5.vue3 Phiên bản không tải dữ liệu danh mục, dưới vue2 mới tải danh mục
		String vue3Version = request.getHeader(CommonConstant.VERSION);
		if(oConvertUtils.isEmpty(vue3Version)){
			//update-begin---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
			obj.put("dictVersion", sysDictService.getDictVersion());
			//update-end---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
			obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
		}
		
		result.setResult(obj);
		result.success("Đăng nhập thành công");
		return result;
	}

	/**
	 * Một tài khoản chỉ được đăng nhập một lần trên cùng một loại máy khách
	 * 
	 * @author scott
	 * @date 2025-10-31
	 * PC Đăng nhập bằng PC, APP và số điện thoại độc lập với nhau, không ảnh hưởng lẫn nhau
	 * 
	 * @param username Tên người dùng
	 * @param newToken mới được tạo token
	 * @param clientType Loại client (PC, APP, PHONE)
	 */
	private void handleSingleSignOn(String username, String newToken, String clientType) {
		// Kiểm tra có cho phép đăng nhập đồng thời hay không
		if (jeecgBaseConfig.getFirewall() == null || jeecgBaseConfig.getFirewall().getIsConcurrent()==null || Boolean.TRUE.equals(jeecgBaseConfig.getFirewall().getIsConcurrent())) {
			// Cho phép đăng nhập đồng thời, chỉ đặt cache token của người dùng hiện tại, không đá các lần đăng nhập trước ra
			log.debug("Đã bật đăng nhập đồng thời: Người dùng[{}]trên{}cho phép đăng nhập đồng thời nhiều nơi", username, clientType);
			return;
		}
		
		log.info("[Giới hạn đăng nhập đồng thời đã bật] Người dùng[{}]trên{}thiết bị không cho phép đăng nhập đồng thời nhiều nơi", username, clientType);
		// Chọn tiền tố Redis key tương ứng theo loại client
		String redisKeyPrefix;
		if (CommonConstant.CLIENT_TYPE_APP.equalsIgnoreCase(clientType)) {
			redisKeyPrefix = CommonConstant.PREFIX_USER_TOKEN_APP;
		} else if (CommonConstant.CLIENT_TYPE_PHONE.equalsIgnoreCase(clientType)) {
			redisKeyPrefix = CommonConstant.PREFIX_USER_TOKEN_PHONE;
		} else {
			redisKeyPrefix = CommonConstant.PREFIX_USER_TOKEN_PC;
		}
		
		String userTokenKey = redisKeyPrefix + username;
		
		// Lấy token trước đó của người dùng đó dưới loại máy khách hiện tại token
		Object oldTokenObj = redisUtil.get(userTokenKey);
		if (oldTokenObj != null && !oldTokenObj.equals(newToken)) {
			String oldToken = oldTokenObj.toString();
			// Xóa bộ nhớ đệm của token đăng nhập cũ (đặt thời gian hết hạn là 1 giờ)
			redisUtil.del(CommonConstant.PREFIX_USER_TOKEN + oldToken);
			redisUtil.set(CommonConstant.PREFIX_USER_TOKEN_ERROR_MSG + oldToken, "Không cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi, phiên đăng nhập hiện tại đã bị đăng xuất!", 60 * 1 * 60);
			log.info("【Giới hạn đăng nhập đồng thời đã bật】Người dùng[{}]trên{}phiên đăng nhập cũ trên thiết bị đã bị đăng xuất!", username, clientType);
			log.info("【Đã bật giới hạn đăng nhập đồng thời】Người dùng bị đăng xuất, token mới token: {}, token cũ:{}", newToken, oldToken);
		}
		
		// Lưu token mới vào bộ nhớ đệm đăng nhập một lần
		redisUtil.set(userTokenKey, newToken);
		redisUtil.expire(userTokenKey, JwtUtil.EXPIRE_TIME * 2 / 1000);
	}

	/**
	 * Lấy chuỗi đã mã hóa
	 * @return
	 */
	@GetMapping(value = "/getEncryptedString")
	public Result<Map<String,String>> getEncryptedString(){
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		Map<String,String> map = new HashMap(5);
		map.put("key", EncryptedString.key);
		map.put("iv",EncryptedString.iv);
		result.setResult(map);
		return result;
	}

	/**
	 * Hệ thống tạo mã xác minh hình ảnh: hiệu lực
	 * @param response
	 * @param key
	 */
	@Operation(summary="Lấy mã xác thực")
	@GetMapping(value = "/randomImage/{key}")
	public Result<String> randomImage(HttpServletResponse response,@PathVariable("key") String key){
		Result<String> res = new Result<String>();
		try {
			//Tạo mã xác minh, lưu vào redis
			String code = RandomUtil.randomString(BASE_CHECK_CODES,4);
			String lowerCaseCode = code.toLowerCase();
			String keyPrefix = Md5Util.md5Encode(key + jeecgBaseConfig.getSignatureSecret(), "utf-8");
			String realKey = keyPrefix + lowerCaseCode;
			redisUtil.removeAll(keyPrefix);
			redisUtil.set(realKey, lowerCaseCode, 60);
			log.debug("Lấy mã xác minh，Redis key = {}，checkCode = {}", realKey, code);
			String base64 = RandImageUtil.generate(code);
			res.setSuccess(true);
			res.setResult(base64);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			res.error500("Lấy mã xác minh thất bại,Vui lòng kiểm tra cấu hình Redis!");
			return res;
		}
		return res;
	}

//	/**
//	 * Chuyển bảng menu thành bảng của vue3
//	 */
//	@RequiresRoles({"admin"})
//	@GetMapping(value = "/switchVue3Menu")
//	public Result<String> switchVue3Menu(HttpServletResponse response) {
//		Result<String> res = new Result<String>();	
//		sysPermissionService.switchVue3Menu();
//		return res;
//	}
	
	/**
	 * app Đăng nhập
	 * @param sysLoginModel
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/mLogin", method = RequestMethod.POST)
	public Result<JSONObject> mLogin(@RequestBody SysLoginModel sysLoginModel, HttpServletRequest request) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		String username = sysLoginModel.getUsername();
		// Truyền mật khẩu đã mã hóa(Thử giải mã AES, thất bại thì coi là văn bản thuần)
		String password  = AesEncryptUtil.resolvePassword(sysLoginModel.getPassword());
		log.debug("Mật khẩu đăng nhập, mật khẩu gốc:{}, mật khẩu đã giải mã:{}" , sysLoginModel.getPassword(), password);

		JSONObject obj = new JSONObject();
		
		// 1.Khóa người dùng khi người dùng nền tảng đăng nhập thất bại
		if(isLoginFailOvertimes(username)){
			return result.error500("Người dùng này đã đăng nhập thất bại quá nhiều lần, vui lòng đăng nhập lại sau 10 phút!");
		}
		//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Kiểm tra mật khẩu trước rồi mới kiểm tra trạng thái người dùng; người dùng không tồn tại và mật khẩu sai dùng chung một thông báo và được tính vào số lần thất bại, ngăn liệt kê người dùng-----------
		// 2.Kiểm tra tên người dùng hoặc mật khẩu có đúng hay không
		SysUser sysUser = sysUserService.getUserByName(username);
		if (!sysUserService.verifyLoginPassword(sysUser, username, password)) {
			result.error500("Tên người dùng hoặc mật khẩu sai");
			return result;
		}
		String syspassword = sysUser.getPassword();

		// 3.Kiểm tra người dùng có hợp lệ không
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Kiểm tra mật khẩu trước rồi mới kiểm tra trạng thái người dùng; người dùng không tồn tại và mật khẩu sai dùng chung một thông báo và được tính vào số lần thất bại, ngăn liệt kê người dùng-----------
		
		//4.Đặt phòng ban đăng nhập
		String orgCode = sysUser.getOrgCode();
		//Tổ chức được đặt khi đăng nhập
		String loginOrgCode = sysLoginModel.getLoginOrgCode();
		if(oConvertUtils.isEmpty(orgCode)) {
			//Nếu người dùng hiện tại chưa chọn phòng ban thì xem thông tin liên kết phòng ban
			if(oConvertUtils.isNotEmpty(loginOrgCode)){
				sysUser.setOrgCode(loginOrgCode);
				this.sysUserService.updateUserDepart(username, loginOrgCode,null);
			}else{
				List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
				if (departs != null && !departs.isEmpty()) {
					orgCode = departs.get(0).getOrgCode();
					sysUser.setOrgCode(orgCode);
					this.sysUserService.updateUserDepart(username, orgCode,null);
				}
			}
		}else{
			if(oConvertUtils.isNotEmpty(loginOrgCode) && !orgCode.equalsIgnoreCase(loginOrgCode)){
				sysUser.setOrgCode(loginOrgCode);
				sysUserService.updateUserDepart(username, loginOrgCode,null);
			}
		}

		//5. Đặt tenant đăng nhập
		Result<JSONObject> loginTenantError = sysUserService.setLoginTenant(sysUser, obj, username, result);
		if (loginTenantError != null) {
			return loginTenantError;
		}
		// Đặt thông tin người dùng đăng nhập
		obj.put("userInfo", sysUser);

		//6. Tạo token và đặt thời gian timeout
		String token = JwtUtil.sign(username, syspassword, CommonConstant.CLIENT_TYPE_APP);
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.APP_EXPIRE_TIME*2 / 1000);
		obj.put("token", token);
		result.setResult(obj);
		result.setSuccess(true);
		result.setCode(200);

		// 7.Có cho phép cùng một tài khoản đăng nhập đồng thời ở nhiều nơi không(APP đăng nhập ở đầu, đá phiên đăng nhập APP trước đó ra)
		handleSingleSignOn(username, token, CommonConstant.CLIENT_TYPE_APP);
		
		// 8.Đăng nhập thành công, ghi nhật ký
		baseCommonService.addLog("Tên người dùng: " + username + ",Đăng nhập thành công[Thiết bị di động]！", CommonConstant.LOG_TYPE_1, null);
		return result;
	}

	/**
	 * Mã xác minh hình ảnh
	 * @param sysLoginModel
	 * @return
	 */
	@RequestMapping(value = "/checkCaptcha", method = RequestMethod.POST)
	public Result<?> checkCaptcha(@RequestBody SysLoginModel sysLoginModel){
		String captcha = sysLoginModel.getCaptcha();
		String checkKey = sysLoginModel.getCheckKey();
		if(captcha==null){
			return Result.error("Mã xác minh không hợp lệ");
		}
		String lowerCaseCaptcha = captcha.toLowerCase();
		String realKey = Md5Util.md5Encode(lowerCaseCaptcha+checkKey, "utf-8");
		Object checkCode = redisUtil.get(realKey);
		if(checkCode==null || !checkCode.equals(lowerCaseCaptcha)) {
			return Result.error("Mã xác minh không đúng");
		}
		return Result.ok();
	}
	/**
	 * Mã QR đăng nhập
	 */
	@Operation(summary = "Mã QR đăng nhập")
	@GetMapping("/getLoginQrcode")
	public Result<?>  getLoginQrcode() {
		//update-begin---author:jeecg---for：【GHSA-gm36-7vvp-c5jh] qrcodeId đổi sang dùng chuỗi ngẫu nhiên không thể đoán trước (ID tuyết hoa gốc có thể bị liệt kê để đánh cắp token của người khác)---
		String qrcodeId = CommonConstant.LOGIN_QRCODE_PRE + java.util.UUID.randomUUID().toString().replace("-", "");
		//update-end---author:jeecg---for：【GHSA-gm36-7vvp-c5jh] qrcodeId đổi sang dùng chuỗi ngẫu nhiên không thể dự đoán---
		//Định nghĩa tham số mã QR
		Map params = new HashMap(5);
		params.put("qrcodeId", qrcodeId);
		//Lưu định danh duy nhất của mã QR, hiệu lực 30 giây
		redisUtil.set(CommonConstant.LOGIN_QRCODE + qrcodeId, qrcodeId, 30);
		return Result.OK(params);
	}
	/**
	 * Quét mã QR
	 */
	@Operation(summary = "Mã QR đăng nhập bằng quét mã")
	@PostMapping("/scanLoginQrcode")
	public Result<?> scanLoginQrcode(@RequestParam String qrcodeId, @RequestParam String token) {
		Object check = redisUtil.get(CommonConstant.LOGIN_QRCODE + qrcodeId);
		if (oConvertUtils.isNotEmpty(check)) {
			//Lưu token để giao diện đọc
			redisUtil.set(CommonConstant.LOGIN_QRCODE_TOKEN+qrcodeId, token, 60);
		} else {
			return Result.error("Mã QR đã hết hạn,Vui lòng làm mới rồi thử lại");
		}
		return Result.OK("Quét mã thành công");
	}


	/**
	 * Lấy token đã lưu sau khi người dùng quét mã token
	 */
	@Operation(summary = "Lấy token đã lưu sau khi người dùng quét mã token")
	@GetMapping("/getQrcodeToken")
	public Result getQrcodeToken(@RequestParam String qrcodeId) {
		Object token = redisUtil.get(CommonConstant.LOGIN_QRCODE_TOKEN + qrcodeId);
		Map result = new HashMap(5);
		Object qrcodeIdExpire = redisUtil.get(CommonConstant.LOGIN_QRCODE + qrcodeId);
		if (oConvertUtils.isEmpty(qrcodeIdExpire)) {
			//Mã QR hết hạn, thông báo cho giao diện làm mới
			result.put("token", "-2");
			return Result.OK(result);
		}
		if (oConvertUtils.isNotEmpty(token)) {
			//update-begin---author:jeecg---for：【GHSA-gm36-7vvp-c5jh] Token chỉ đọc một lần, đọc được là xóa ngay, tránh bị đánh cắp do polling lặp lại---
			redisUtil.del(CommonConstant.LOGIN_QRCODE_TOKEN + qrcodeId);
			redisUtil.del(CommonConstant.LOGIN_QRCODE + qrcodeId);
			//update-end---author:jeecg---for：【GHSA-gm36-7vvp-c5jh】Đọc token một lần---
			result.put("success", true);
			result.put("token", token);
		} else {
			result.put("token", "-1");
		}
		return Result.OK(result);
	}

	/**
	 * Đăng nhập thất bại vượt quá 5 lần, trả về true
	 * @param username
	 * @return
	 */
	//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Chuyển logic đếm số lần thất bại sang service, dùng chung với interface tra cứu phòng ban trước khi đăng nhập-----------
	private boolean isLoginFailOvertimes(String username){
		return sysUserService.isLoginFailOvertimes(username);
	}

	/**
	 * Ghi số lần đăng nhập thất bại
	 * @param username
	 */
	private void addLoginFailOvertimes(String username){
		sysUserService.addLoginFailOvertimes(username);
	}
	//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Chuyển logic đếm số lần thất bại sang service, dùng chung với interface tra cứu phòng ban trước khi đăng nhập-----------

	/**
	 * Giao diện gửi mã xác minh SMS(Đổi mật khẩu)
	 *
	 * @param jsonObject
	 * @return
	 */
	@PostMapping(value = "/sendChangePwdSms")
	public Result<String> sendSms(@RequestBody JSONObject jsonObject) {
		Result<String> result = new Result<>();
		String mobile = jsonObject.get("mobile").toString();
		if (oConvertUtils.isEmpty(mobile)) {
			result.setMessage("Số điện thoại không được để trống!");
			result.setSuccess(false);
			return result;
		}
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = sysUser.getUsername();
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.eq(SysUser::getUsername, username).eq(SysUser::getPhone, mobile);
		SysUser user = sysUserService.getOne(query);
		if (null == user) {
			return Result.error("Người dùng đang đăng nhập và số điện thoại đã liên kết không khớp, không thể đổi mật khẩu!");
		}
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + mobile;
		Object object = redisUtil.get(redisKey);
		if (object != null) {
			result.setMessage("Mã xác minh vẫn còn hiệu lực trong 10 phút!");
			result.setSuccess(false);
			return result;
		}
		//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn theo số điện thoại-----------
		if (!DySmsLimit.canSendSmsToPhone(mobile)) {
			result.setMessage("Gửi SMS quá thường xuyên, vui lòng thử lại sau!");
			result.setSuccess(false);
			return result;
		}
		//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn theo số điện thoại-----------
		//Số ngẫu nhiên
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
		obj.put("code", captcha);
		try {
			boolean b = DySmsHelper.sendSms(mobile, obj, DySmsEnum.CHANGE_PASSWORD_TEMPLATE_CODE);
			if (!b) {
				result.setMessage("Gửi mã xác minh SMS thất bại,Vui lòng thử lại sau");
				result.setSuccess(false);
				return result;
			}
            //【issues/8567】Nghiêm trọng: sửa mật khẩu tồn tại vấn đề vượt quyền theo chiều ngang
            obj.put("username",username);
            redisUtil.set(redisKey, obj.toJSONString(), 300);
			result.setSuccess(true);
		} catch (ClientException e) {
			e.printStackTrace();
			result.error500(" Giao diện SMS chưa được cấu hình, vui lòng liên hệ quản trị viên!");
			return result;
		}
		return result;
	}

	
	/**
	 * Mã xác minh hình ảnh
	 * @param sysLoginModel
	 * @return
	 */
	@RequestMapping(value = "/smsCheckCaptcha", method = RequestMethod.POST)
	public Result<?> smsCheckCaptcha(@RequestBody SysLoginModel sysLoginModel, HttpServletRequest request){
		String captcha = sysLoginModel.getCaptcha();
		String checkKey = sysLoginModel.getCheckKey();
		if(captcha==null){
			return Result.error("Mã xác minh không hợp lệ");
		}
		String lowerCaseCaptcha = captcha.toLowerCase();
		String realKey = Md5Util.md5Encode(lowerCaseCaptcha+checkKey+jeecgBaseConfig.getSignatureSecret(), "utf-8");
		Object checkCode = redisUtil.get(realKey);
		if(checkCode==null || !checkCode.equals(lowerCaseCaptcha)) {
			return Result.error("Mã xác minh không đúng");
		}
		String clientIp = IpUtils.getIpAddr(request);
		//Xóa số lượng bản ghi SMS
		DySmsLimit.clearSendSmsCount(clientIp);
		redisUtil.removeAll(realKey);
		return Result.ok();
	}
	/**
	 * Đăng nhập — Lấy thông tin phòng ban của người dùng
	 *
	 * @param jsonObject
	 * @return
	 */
	@IgnoreAuth
	@RequestMapping(value = "/loginGetUserDeparts", method = RequestMethod.POST)
	public Result<JSONObject> loginGetUserDeparts(@RequestBody JSONObject jsonObject, HttpServletRequest request){
		return sysUserService.loginGetUserDeparts(jsonObject);
	}
	
	/**
     * Phương thức tiện ích kiểm tra mã xác minh; kiểm tra thất bại thì trả về trực tiếp Result, kiểm tra thành công thì trả về realKey
     */
    private String validateCaptcha(SysLoginModel sysLoginModel, Result<JSONObject> result) {
		// Xác định có bật kiểm tra mã xác minh đăng nhập hay không
		if (jeecgBaseConfig.getFirewall() != null && Boolean.FALSE.equals(jeecgBaseConfig.getFirewall().getEnableLoginCaptcha())) {
			log.warn("Đã tắt kiểm tra mã xác minh đăng nhập, bỏ qua kiểm tra mã xác minh!");
			return "LoginWithoutVerifyCode";
		}
		
        String captcha = sysLoginModel.getCaptcha();
        if (captcha == null) {
            result.error500("Mã xác minh không hợp lệ");
            return null;
        }
        String lowerCaseCaptcha = captcha.toLowerCase();
        String keyPrefix = Md5Util.md5Encode(sysLoginModel.getCheckKey() + jeecgBaseConfig.getSignatureSecret(), "utf-8");
        String realKey = keyPrefix + lowerCaseCaptcha;
        Object checkCode = redisUtil.get(realKey);
        if (checkCode == null || !checkCode.toString().equals(lowerCaseCaptcha)) {
            log.warn("Mã xác minh không đúng，key= {} , Ui checkCode= {}, Redis checkCode = {}", sysLoginModel.getCheckKey(), lowerCaseCaptcha, checkCode);
            result.error500("Mã xác minh không đúng");
            result.setCode(HttpStatus.PRECONDITION_FAILED.value());
            return null;
        }
        //update-begin---author:scott ---date:20261013  for: [Bảo mật] Sau khi mã xác minh hình ảnh được kiểm tra thành công thì hủy ngay (dùng một lần), ngăn cùng một mã xác minh bị dùng lặp lại để thử đăng nhập-----------
        redisUtil.del(realKey);
        //update-end---author:scott ---date:20261013  for: [Bảo mật] Sau khi mã xác minh hình ảnh được kiểm tra thành công thì hủy ngay (dùng một lần), ngăn cùng một mã xác minh bị dùng lặp lại để thử đăng nhập-----------
        return realKey;
    }
}