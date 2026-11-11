package org.jeecg.modules.system.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xkcoding.justauth.AuthRequestFactory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import me.zhyd.oauth.utils.StringUtils;
import me.zhyd.oauth.config.AuthConfig;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.*;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysThirdAccount;
import org.jeecg.modules.system.entity.SysThirdAppConfig;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.ThirdLoginModel;
import org.jeecg.modules.system.service.ISysDictService;
import org.jeecg.modules.system.service.ISysThirdAccountService;
import org.jeecg.modules.system.service.ISysThirdAppConfigService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.impl.ThirdAppDingtalkServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppFeishuServiceImpl;
import org.jeecg.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.vo.DomainUrl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * @Author scott
 * @since 2018-12-17
 */
@Controller
@RequestMapping("/sys/thirdLogin")
@Slf4j
public class ThirdLoginController {
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysThirdAccountService sysThirdAccountService;
	@Autowired
	private ISysDictService sysDictService;
	@Autowired
	private BaseCommonService baseCommonService;
	@Autowired
    private RedisUtil redisUtil;
	@Autowired
	private AuthRequestFactory factory;
	@Autowired
	private ISysDepartService sysDepartService;

	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl thirdAppWechatEnterpriseService;
	@Autowired
	private ThirdAppDingtalkServiceImpl thirdAppDingtalkService;

	@Autowired
	private ISysThirdAppConfigService appConfigService;

	@Autowired
	private ThirdAppFeishuServiceImpl thirdAppFeishuService;

	@Autowired
	private JustAuthProperties justAuthProperties;

	@Autowired
	public ISysBaseAPI sysBaseAPI;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	@RequestMapping("/render/{source}")
    public void render(@PathVariable("source") String source, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Đăng nhập bên thứ ba vào render:" + source);
        //update-begin---author:liusq ---date:2026-05-13  for：【QQYUN-12767] Tích hợp Feishu, gọi thủ công API OAuth2 phiên bản mới của Feishu (bỏ qua API đã lỗi thời của JustAuth)-----------
        if (CommonConstant.FEISHU.equalsIgnoreCase(source) || ThirdAppFeishuServiceImpl.THIRD_TYPE.equalsIgnoreCase(source)) {
            SysThirdAppConfig feishuConfig = buildFeishuConfigFromYaml();
            if (feishuConfig == null) {
                feishuConfig = appConfigService.getThirdConfigByThirdType(CommonConstant.TENANT_ID_DEFAULT_VALUE, ThirdAppFeishuServiceImpl.THIRD_TYPE);
            }
            if (feishuConfig == null) {
                response.setContentType("text/html;charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Ứng dụng Feishu chưa được cấu hình, vui lòng liên hệ quản trị viên");
                return;
            }
            // Mã hóa ID tenant vào state, định dạng:{tenantId}_{uuid}, khi callback sẽ phân tích ID thuê bao để truy vấn chính xác đa thuê bao
            //update-begin---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, tương thích trường hợp tenantId là null-----------
            Integer renderTenantId = feishuConfig.getTenantId() != null ? feishuConfig.getTenantId() : CommonConstant.TENANT_ID_DEFAULT_VALUE;
            String state = renderTenantId + "_" + AuthStateUtils.createState();
            //update-end---author:liusq ---date:2026-05-15  for：【QQYUN-12767] Đăng nhập quét mã Feishu đổi sang dùng cấu hình yml, tương thích trường hợp tenantId là null-----------
            String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/feishu/callback";
            String authorizeUrl = thirdAppFeishuService.buildAuthorizeUrl(feishuConfig, redirectUri, state);
            log.info("Địa chỉ xác thực đăng nhập Feishu：{}", authorizeUrl);
            response.sendRedirect(authorizeUrl);
            return;
        }
        //update-end---author:liusq ---date:2026-05-13  for：【QQYUN-12767] Tích hợp Feishu, gọi thủ công API OAuth2 phiên bản mới của Feishu (bỏ qua API đã lỗi thời của JustAuth)-----------
        //update-begin---author:scott ---date:2026-06-04  for: 【Gia cố NPE đăng nhập bên thứ ba】source chưa có trong justauth.type khi chưa được cấu hình sẽ chặn trực tiếp, để tránh JustAuth-starter 1.4.0 get()Chưa kiểm tra null getExtend()Ném con trỏ null-----------
        if (justAuthProperties.getType() == null || !justAuthProperties.getType().containsKey(source.toUpperCase())) {
            log.warn("Phương thức đăng nhập bên thứ ba[{}]Chưa cấu hình (justauth.type không tồn tại), đã chặn yêu cầu render", source);
            response.setContentType("text/html;charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-4c5v-rq3g-j3pc] Trước khi hiển thị lại source, thực hiện escape HTML để tránh XSS phản xạ XSS-----------
            response.getWriter().write("Phương thức đăng nhập bên thứ ba[" + HtmlUtils.htmlEscape(source) + "]chưa được cấu hình, vui lòng liên hệ quản trị viên");
            //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-4c5v-rq3g-j3pc] Trước khi hiển thị lại source, thực hiện escape HTML để tránh XSS phản xạ XSS-----------
            return;
        }
        //update-end---author:scott ---date:2026-06-04  for: 【Gia cố NPE đăng nhập bên thứ ba】source chưa có trong justauth.type khi chưa được cấu hình sẽ chặn trực tiếp, để tránh JustAuth-starter 1.4.0 get()Chưa kiểm tra null getExtend()Ném con trỏ null-----------
        AuthRequest authRequest = factory.get(source);
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        log.info("Địa chỉ xác thực đăng nhập bên thứ ba:" + authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }

	@RequestMapping("/{source}/callback")
    public String loginThird(@PathVariable("source") String source, AuthCallback callback,ModelMap modelMap, HttpServletRequest request) {
		log.info("Đăng nhập bên thứ ba vào callback:" + source + " params：" + JSONObject.toJSONString(callback));
        //update-begin---author:jeecg ---date:2026-05-13  for：【QQYUN-12767] Tích hợp Feishu, gọi thủ công API OAuth2 phiên bản mới của Feishu (bỏ qua API đã lỗi thời của JustAuth)-----------
        if (CommonConstant.FEISHU.equalsIgnoreCase(source) || ThirdAppFeishuServiceImpl.THIRD_TYPE.equalsIgnoreCase(source)) {
            String code = request.getParameter("code");
            log.info("【Feishu】Callback đăng nhập bằng quét mã code={}", code);
            try {
                // Phân tích ID thuê bao từ state (khi render được mã hóa thành {tenantId}_{uuid} định dạng)
                Integer feishuTenantId = CommonConstant.TENANT_ID_DEFAULT_VALUE;
                String stateParam = request.getParameter("state");
                if (stateParam != null && stateParam.contains("_")) {
                    try {
                        feishuTenantId = Integer.parseInt(stateParam.split("_", 2)[0]);
                    } catch (NumberFormatException ex) {
                        log.warn("[Feishu] Phân tích ID thuê bao trong state thất bại, dùng thuê bao mặc định,state={}", stateParam);
                    }
                }
                log.info("【Feishu】Callback đăng nhập bằng quét mã tenantId={}", feishuTenantId);
                // Gọi trực tiếp API phiên bản mới của Feishu để hoàn thành code → token → Thông tin người dùng toàn quy trình
                String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/feishu/callback";
                SysThirdAppConfig feishuYamlConfig = buildFeishuConfigFromYaml();
                SysUser loginUser = thirdAppFeishuService.oauth2Login(code, feishuTenantId, redirectUri, feishuYamlConfig);
                String token = saveToken(loginUser);
                modelMap.addAttribute("token", token);
            } catch (Exception e) {
                log.error("Callback đăng nhập Feishu xảy ra ngoại lệ", e);
                if (e.getMessage() != null && e.getMessage().contains("openId=")) {
                    String openId = e.getMessage().substring(e.getMessage().indexOf("openId=") + 7);
                    modelMap.addAttribute("token", "Liên kết số điện thoại," + openId);
                } else {
                    modelMap.addAttribute("token", "Đăng nhập thất bại");
                }
            }
            return "thirdLogin";
        }
        //update-end---author:liusq ---date:2026-05-13  for：【QQYUN-12767] Tích hợp Feishu, gọi thủ công API OAuth2 phiên bản mới của Feishu (bỏ qua API đã lỗi thời của JustAuth)-----------
        AuthRequest authRequest = factory.get(source);
        AuthResponse response = authRequest.login(callback);
        log.info(JSONObject.toJSONString(response));
        Result<JSONObject> result = new Result<JSONObject>();
        if(response.getCode()==2000) {

        	JSONObject data = JSONObject.parseObject(JSONObject.toJSONString(response.getData()));
        	String username = data.getString("username");
        	String avatar = data.getString("avatar");
        	String uuid = data.getString("uuid");
        	//Xây dựng đối tượng lưu trữ thông tin đăng nhập bên thứ ba
			ThirdLoginModel tlm = new ThirdLoginModel(source, uuid, username, avatar);
        	//Kiểm tra xem có người này không
			// Giải thích logic mã: Đổi thành truy vấn bảng tài khoản bên thứ ba
        	LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<SysThirdAccount>();
        	query.eq(SysThirdAccount::getThirdType, source);
			// Giải thích logic mã: 【QQYUN-6667] QiaoQiaoYun, hủy liên kết trực tuyến rồi liên kết lại vẫn luôn hiển thị thông báo này---
        	query.eq(SysThirdAccount::getTenantId, CommonConstant.TENANT_ID_DEFAULT_VALUE);
			query.and(q -> q.eq(SysThirdAccount::getThirdUserUuid, uuid).or().eq(SysThirdAccount::getThirdUserId, uuid));
        	List<SysThirdAccount> thridList = sysThirdAccountService.list(query);
			SysThirdAccount user = null;
        	if(thridList==null || thridList.size()==0) {
				//Ngược lại tạo trực tiếp tài khoản mới
				user = sysThirdAccountService.saveThirdUser(tlm,CommonConstant.TENANT_ID_DEFAULT_VALUE);
        	}else {
        		//Đã tồn tại thì chỉ đặt tên người dùng, không đặt ảnh đại diện
        		user = thridList.get(0);
        	}
        	// Tạo token
			// Giải thích logic mã: Truy vấn từ đăng nhập bên thứ ba xem có tồn tại id người dùng hay không; nếu không tồn tại thì liên kết số điện thoại
			if(oConvertUtils.isNotEmpty(user.getSysUserId())) {
				String sysUserId = user.getSysUserId();
				SysUser sysUser = sysUserService.getById(sysUserId);
				String token = saveToken(sysUser);
    			modelMap.addAttribute("token", token);
			}else{
				modelMap.addAttribute("token", "Liên kết số điện thoại,"+""+uuid);
			}
        }else{
			modelMap.addAttribute("token", "Đăng nhập thất bại");
		}
        result.setSuccess(false);
        result.setMessage("Đăng nhập bên thứ ba bất thường,Vui lòng liên hệ quản trị viên");
        return "thirdLogin";
    }

	/**
	 * Tạo tài khoản mới
	 * @param model
	 * @return
	 */
	@PostMapping("/user/create")
	@ResponseBody
	public Result<String> thirdUserCreate(@RequestBody ThirdLoginModel model) {
		log.info("Đăng nhập bên thứ ba tạo tài khoản mới:" );
		Result<String> res = new Result<>();
		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(operateCode==null || !operateCode.toString().equals(model.getOperateCode())){
			res.setSuccess(false);
			res.setMessage("Kiểm tra thất bại");
			return res;
		}
		//Tạo tài khoản mới
		// Giải thích logic mã: Sửa thành lấy từ kết quả tra cứu đăng nhập bên thứ ba user_id，tiến hành token khi truy vấn bảng người dùng token
		SysThirdAccount user = sysThirdAccountService.saveThirdUser(model,CommonConstant.TENANT_ID_DEFAULT_VALUE);
		if(oConvertUtils.isNotEmpty(user.getSysUserId())){
			String sysUserId = user.getSysUserId();
			SysUser sysUser = sysUserService.getById(sysUserId);
			// Tạo token
			String token = saveToken(sysUser);
			res.setResult(token);
			res.setSuccess(true);
		}
		return res;
	}

	/**
	 * Liên kết tài khoản: cần đặt mật khẩu, cần đi qua một lượt kiểm tra
	 * @param json
	 * @return
	 */
	@PostMapping("/user/checkPassword")
	@ResponseBody
	public Result<String> checkPassword(@RequestBody JSONObject json) {
		Result<String> result = new Result<>();
		Object operateCode = redisUtil.get(CommonConstant.THIRD_LOGIN_CODE);
		if(operateCode==null || !operateCode.toString().equals(json.getString("operateCode"))){
			result.setSuccess(false);
			result.setMessage("Kiểm tra thất bại");
			return result;
		}
		String username = json.getString("uuid");
		SysUser user = this.sysUserService.getUserByName(username);
		if(user==null){
			result.setMessage("Không tìm thấy người dùng");
			result.setSuccess(false);
			return result;
		}
		String password = json.getString("password");
		String salt = user.getSalt();
		String passwordEncode = PasswordUtil.encrypt(user.getUsername(), password, salt);
		if(!passwordEncode.equals(user.getPassword())){
			result.setMessage("Mật khẩu không đúng");
			result.setSuccess(false);
			return result;
		}

		sysUserService.updateById(user);
		result.setSuccess(true);
		// Tạo token
		String token = saveToken(user);
		result.setResult(token);
		return result;
	}

	/**
	 * Đọc Feishu từ cấu hình justauth yml app_id/app_secret, xây dựng SysThirdAppConfig tạm thời.
	 * client-id Trống hoặc là placeholder "??" sẽ trả về null, phía gọi sẽ quay lại cấu hình cơ sở dữ liệu.
	 */
	private SysThirdAppConfig buildFeishuConfigFromYaml() {
		if (justAuthProperties == null || justAuthProperties.getType() == null) {
			return null;
		}
		AuthConfig authConfig = justAuthProperties.getType().get(CommonConstant.FEISHU);
		if (authConfig == null || authConfig.getClientId() == null || "??".equals(authConfig.getClientId().trim())) {
			return null;
		}
		SysThirdAppConfig config = new SysThirdAppConfig();
		config.setClientId(authConfig.getClientId());
		config.setClientSecret(authConfig.getClientSecret());
		config.setTenantId(CommonConstant.TENANT_ID_DEFAULT_VALUE);
		return config;
	}

	private String saveToken(SysUser user) {
		// Tạo token
		String token = JwtUtil.sign(user.getUsername(), user.getPassword(), CommonConstant.CLIENT_TYPE_PC);
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		// Đặt thời gian chờ
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);
		return token;
	}

	/**
	 * Giao diện callback đăng nhập bên thứ ba
	 * @param token
	 * @param thirdType
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/getLoginUser/{token}/{thirdType}/{tenantId}", method = RequestMethod.GET)
	@ResponseBody
	public Result<JSONObject> getThirdLoginUser(@PathVariable("token") String token,@PathVariable("thirdType") String thirdType,@PathVariable("tenantId") String tenantId) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		// Giải thích logic mã: Chặn nhanh token không đúng định dạng JWT (ví dụ token do SDK Feishu phát ra [tea-sdk]ready), tránh JWTDecodeException kích hoạt log ERROR
		if (token == null || token.split("\\.").length != 3) {
			log.debug("[Đăng nhập bên thứ ba] Nhận được token không đúng định dạng JWT, bỏ qua: {}", token);
			return Result.noauth("token Định dạng không hợp lệ");
		}
		String username = JwtUtil.getUsername(token);
		// Giải thích logic mã: [QQYUN-11021]Sửa lỗ hổng interface đăng nhập bên thứ ba lấy thông tin người dùng qua token------------
		if (!TokenUtils.verifyToken(token, sysBaseAPI, redisUtil)) {
			return Result.noauth("token Kiểm tra thất bại");
		}
		//1. Kiểm tra người dùng có hợp lệ không
		SysUser sysUser = sysUserService.getUserByName(username);
		result = sysUserService.checkUserIsEffective(sysUser);
		if(!result.isSuccess()) {
			return result;
		}
		// Giải thích logic mã: Nếu tên thật và ảnh đại diện không tồn tại thì lấy từ đăng nhập bên thứ ba
		LambdaQueryWrapper<SysThirdAccount> query = new LambdaQueryWrapper<>();
		query.eq(SysThirdAccount::getSysUserId,sysUser.getId());
		query.eq(SysThirdAccount::getThirdType,thirdType);
		query.eq(SysThirdAccount::getTenantId,oConvertUtils.getInt(tenantId,CommonConstant.TENANT_ID_DEFAULT_VALUE));
		// Giải thích logic mã: [QQYUN-4883]Đăng nhập auth DingTalk có cùng một người dùng trong cùng một tenant id------------
		List<SysThirdAccount> accountList = sysThirdAccountService.list(query);
		SysThirdAccount account = new SysThirdAccount();
		if(CollectionUtil.isNotEmpty(accountList)){
			account = accountList.get(0);
		}
		if(oConvertUtils.isEmpty(sysUser.getRealname())){
			sysUser.setRealname(account.getRealname());
		}
		if(oConvertUtils.isEmpty(sysUser.getAvatar())){
			sysUser.setAvatar(account.getAvatar());
		}
		JSONObject obj = new JSONObject();
		//Đăng nhập bên thứ ba xác định logic tenant và phòng ban đăng nhập
		this.setUserTenantAndDepart(sysUser,obj,result);		
		//Thông tin đăng nhập người dùng
		obj.put("userInfo", sysUser);
		//Lấy cache danh mục【Giải quyết #jeecg-boot/issues/3998】
		//update-begin---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
		obj.put("dictVersion", sysDictService.getDictVersion());
		//update-end---author:scott ---date:20261013  for：【issues/9838] Trả về số phiên bản danh mục; lấy số phiên bản trước rồi mới tra danh mục, tránh trong khoảng đó danh mục thay đổi khiến frontend nhận số phiên bản mới+Dữ liệu cũ-----------
		obj.put("sysAllDictItems", sysDictService.queryAllDictItems());
		//token Thông tin
		obj.put("token", token);
		result.setResult(obj);
		result.setSuccess(true);
		result.setCode(200);
		baseCommonService.addLog("Tên người dùng: " + username + ",Đăng nhập thành công[Người dùng bên thứ ba]！", CommonConstant.LOG_TYPE_1, null);
		return result;
	}
	/**
	 * Liên kết số điện thoại bên thứ ba trả về token
	 *
	 * @param jsonObject
	 * @return
	 */
	@Operation(summary="API đăng nhập bằng số điện thoại")
	@PostMapping("/bindingThirdPhone")
	@ResponseBody
	public Result<String> bindingThirdPhone(@RequestBody JSONObject jsonObject) {
		Result<String> result = new Result<String>();
		String phone = jsonObject.getString("mobile");
		String thirdUserUuid = jsonObject.getString("thirdUserUuid");
		// Kiểm tra mã xác minh
		String captcha = jsonObject.getString("captcha");
		// Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object captchaCache = redisUtil.get(redisKey);
		if (oConvertUtils.isEmpty(captcha) || !captcha.equals(captchaCache)) {
			result.setMessage("Mã xác minh không đúng");
			result.setSuccess(false);
			return result;
		}
		//Kiểm tra tính hợp lệ của người dùng
		SysUser sysUser = sysUserService.getUserByPhone(phone);
		if(sysUser != null){
			// Đã tồn tại người dùng, liên kết trực tiếp
			sysThirdAccountService.updateThirdUserId(sysUser,thirdUserUuid);
		}else{
			// Không tồn tại số điện thoại, tạo người dùng
			sysUser = sysThirdAccountService.createUser(phone,thirdUserUuid,CommonConstant.TENANT_ID_DEFAULT_VALUE);
		}
		String token = saveToken(sysUser);
		result.setSuccess(true);
		result.setResult(token);
		return result;
	}

	/**
	 * WeCom/Đăng nhập OAuth2 DingTalk
	 *
	 * @param source
	 * @param state
	 * @return
	 */
	@ResponseBody
	@GetMapping("/oauth2/{source}/login")
	public String oauth2LoginCallback(@PathVariable("source") String source, @RequestParam("state") String state, HttpServletRequest request, HttpServletResponse response,
									  @RequestParam(value = "tenantId",required = false,defaultValue = "0") String tenantId) throws Exception {
		String url;
		//id ứng dụng trống, nghĩa là chưa được cấu hình lowAppId
		if(oConvertUtils.isEmpty(tenantId)){
			return "Mã tenant chưa được cấu hình";
		}
		if (CommonConstant.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
			//Đổi sang bảng cấu hình app bên thứ ba
			SysThirdAppConfig config = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.QYWX.getType());
			if(null == config){
				return "Chưa cấu hình ứng dụng WeChat doanh nghiệp, vui lòng cấu hình ứng dụng WeChat doanh nghiệp";
			}
			StringBuilder builder = new StringBuilder();
			// Tạo địa chỉ ủy quyền đăng nhập OAuth2 của WeChat Doanh nghiệp
			builder.append("https://open.weixin.qq.com/connect/oauth2/authorize");
			// của doanh nghiệp CorpID
			builder.append("?appid=").append(config.getClientId());
			// Địa chỉ liên kết callback chuyển hướng sau khi ủy quyền, vui lòng dùng urlencode để xử lý liên kết
			String redirectUri = CommonUtils.getBaseUrl(request)  + "/sys/thirdLogin/oauth2/wechat_enterprise/callback?tenantId="+tenantId;;
			builder.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
			// Kiểu trả về, lúc này cố định là:code
			builder.append("&response_type=code");
			// Phạm vi ủy quyền của ứng dụng.
			// snsapi_base: Ủy quyền im lặng, có thể lấy thông tin cơ bản của thành viên (UserId và DeviceId);
			builder.append("&scope=snsapi_base");
			// Sau khi chuyển hướng sẽ mang theo tham số state, độ dài không được vượt quá 128 byte
			builder.append("&state=").append(state);
			// Thiết bị đầu cuối dùng tham số này để xác định có cần mang theo thông tin danh tính hay không
			builder.append("#wechat_redirect");
			url = builder.toString();
		} else if (CommonConstant.DINGTALK.equalsIgnoreCase(source)) {
			//Đổi sang bảng cấu hình app bên thứ ba
			SysThirdAppConfig appConfig = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.DD.getType());
			if(null == appConfig){
				return "Chưa cấu hình ứng dụng DingTalk, vui lòng cấu hình ứng dụng DingTalk";
			}
			StringBuilder builder = new StringBuilder();
			// Tạo địa chỉ ủy quyền đăng nhập OAuth2 của DingTalk
			builder.append("https://login.dingtalk.com/oauth2/auth");
			// Ủy quyền thành công/Địa chỉ callback sau khi từ chối.
			// Lưu ý: Cần giữ nhất quán với tên miền đã đăng ký khi đăng ký ứng dụng.
			String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/oauth2/dingtalk/callback?tenantId="+tenantId;
			builder.append("?redirect_uri=").append(URLEncoder.encode(redirectUri, "UTF-8"));
			// Giá trị cố định là code.
			// Sau khi ủy quyền thành công, trả về authCode.
			builder.append("&response_type=code");
			// Lấy từ chi tiết ứng dụng đã tạo trong bước một.
			// Ứng dụng nội bộ doanh nghiệp:client_id là AppKey của ứng dụng.
			builder.append("&client_id=").append(appConfig.getClientId());
			// Phạm vi ủy quyền; thông tin ủy quyền hiển thị trên trang ủy quyền lấy theo cấu hình lúc đăng ký ứng dụng.
			// openid：sau khi ủy quyền có thể lấy userid người dùng userid
			builder.append("&scope=openid");
			// Trả về nguyên dạng theo authCode.
			builder.append("&state=").append(state);
            // Giải thích logic mã: [issues/I5BOUF]oauth2 DingTalk không thể đăng nhập------------
            builder.append("&prompt=").append("consent");
            url = builder.toString();
		}
		else if (CommonConstant.FEISHU.equalsIgnoreCase(source) || "feishu".equalsIgnoreCase(source)) {
			SysThirdAppConfig feishuConfig = appConfigService.getThirdConfigByThirdType(Integer.valueOf(tenantId), MessageTypeEnum.FS.getType());
			if (null == feishuConfig) {
				return "Chưa cấu hình ứng dụng Feishu, vui lòng cấu hình ứng dụng Feishu";
			}
			String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/oauth2/feishu/callback?tenantId=" + tenantId;
			url = thirdAppFeishuService.buildAuthorizeUrl(feishuConfig, redirectUri, state);
		}
		else {
			return "Không hỗ trợ source";
		}
		log.info("oauth2 login url:" + url);
		response.sendRedirect(url);
		return "login…";
	}

    /**
     * WeCom/Callback đăng nhập OAuth2 của DingTalk
     *
     * @param code
     * @param state
     * @param response
     * @return
     */
	@ResponseBody
	@GetMapping("/oauth2/{source}/callback")
	public String oauth2LoginCallback(
			@PathVariable("source") String source,
			// Code do WeChat doanh nghiệp trả về code
			@RequestParam(value = "code", required = false) String code,
			// DingTalk trả về code
			@RequestParam(value = "authCode", required = false) String authCode,
			@RequestParam("state") String state,
			@RequestParam(name = "tenantId",defaultValue = "0") String tenantId,
			HttpServletRequest request,
			HttpServletResponse response) {
        SysUser loginUser;
        if (CommonConstant.WECHAT_ENTERPRISE.equalsIgnoreCase(source)) {
            log.info("[WeChat Work] Đăng nhập OAuth2 vào callback:code=" + code + ", state=" + state);
            loginUser = thirdAppWechatEnterpriseService.oauth2Login(code,Integer.valueOf(tenantId));
            if (loginUser == null) {
                return "Đăng nhập thất bại";
            }
        } else if (CommonConstant.DINGTALK.equalsIgnoreCase(source)) {
			log.info("[DingTalk] Đăng nhập OAuth2 vào callback:authCode=" + authCode + ", state=" + state);
			loginUser = thirdAppDingtalkService.oauth2Login(authCode,Integer.valueOf(tenantId));
			if (loginUser == null) {
				return "Đăng nhập thất bại";
			}
        }
        else if (CommonConstant.FEISHU.equalsIgnoreCase(source) || "feishu".equalsIgnoreCase(source)) {
			log.info("[Feishu] Đăng nhập OAuth2 vào callback:code={}, state={}", code, state);
			String redirectUri = CommonUtils.getBaseUrl(request) + "/sys/thirdLogin/oauth2/feishu/callback?tenantId=" + tenantId;
			loginUser = thirdAppFeishuService.oauth2Login(code, Integer.valueOf(tenantId), redirectUri);
			if (loginUser == null) {
				return "Đăng nhập thất bại";
			}
		}
        else {
            return "Không hỗ trợ source";
        }
        try {
			// Giải thích logic mã: Workflow gửi tin nhắn, nhấn liên kết tin nhắn để chuyển đến trang xử lý
			String redirect = "";
			if (state.indexOf("?") > 0) {
				String[] arr = state.split("\\?");
				state = arr[0];
				if(arr.length>1){
					redirect = arr[1];
				}
			}
			//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-2x3m-cj6f-5894/GHSA-ffqp-583g-3c3w/GHSA-jj4x-xwpm-5vx8] Địa chỉ chuyển hướng phải nằm trong whitelist, tránh token đăng nhập bị đưa sang trang bên ngoài-----------
			if (!isSafeRedirect(state, request)) {
				return "Địa chỉ chuyển hướng không hợp lệ";
			}
			//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-2x3m-cj6f-5894/GHSA-ffqp-583g-3c3w/GHSA-jj4x-xwpm-5vx8] Địa chỉ chuyển hướng phải nằm trong whitelist, tránh token đăng nhập bị đưa sang trang bên ngoài-----------

			String token = saveToken(loginUser);
			state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(token, "UTF-8") + "&tenantId=" + URLEncoder.encode(tenantId, "UTF-8");
			// Giải thích logic mã: [issues/I5BOUF]oauth2 DingTalk không thể đăng nhập------------
			state += "&thirdType=" + source;
			//state += "&thirdType=" + "wechat_enterprise";
			if (redirect != null && redirect.length() > 0) {
				state += "&" + redirect;
			}

			log.info("OAuth2 Địa chỉ chuyển hướng đăng nhập: " + state);
            try {
                response.sendRedirect(state);
                return "ok";
            } catch (IOException e) {
                e.printStackTrace();
                return "Chuyển hướng thất bại";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "Giải mã thất bại";
        }
    }

	/**
	 * Kiểm tra địa chỉ chuyển hướng sau khi đăng nhập OAuth2 thành công(state): chỉ cho phép đường dẫn tương đối, hoặc tên miền là backend hiện tại,jeecg.domainUrl.pc/app/back tên miền được cấu hình trong đó
	 *
	 * @author scott
	 * @since 2026-10-13 【Bảo mật GHSA-2x3m-cj6f-5894/GHSA-ffqp-583g-3c3w/GHSA-jj4x-xwpm-5vx8】Ngăn token đăng nhập bị mang sang trang bên ngoài
	 */
	private boolean isSafeRedirect(String state, HttpServletRequest request) {
		if (state.startsWith("/")) {
			return !state.startsWith("//") && !state.startsWith("/\\");
		}
		DomainUrl domainUrl = jeecgBaseConfig.getDomainUrl();
		String[] allowUrls = domainUrl == null ? new String[]{CommonUtils.getBaseUrl(request)}
				: new String[]{CommonUtils.getBaseUrl(request), domainUrl.getPc(), domainUrl.getApp(), domainUrl.getBack()};
		try {
			URI uri = new URI(state);
			if (uri.getHost() == null || !uri.getScheme().matches("(?i)https?")) {
				return false;
			}
			for (String url : allowUrls) {
				if (oConvertUtils.isNotEmpty(url) && uri.getHost().equalsIgnoreCase(new URI(url).getHost())) {
					return true;
				}
			}
		} catch (URISyntaxException e) {
			log.warn("OAuth2 Địa chỉ chuyển hướng đăng nhập không hợp lệ: {}", state);
		}
		return false;
	}

	/**
	 * Đăng ký tài khoản và liên kết tài khoản bên thứ ba [Interface chuyên dụng cho ứng dụng low-code]
	 * @param jsonObject
	 * @param user
	 * @return
	 */
	@ResponseBody
	@PutMapping("/registerBindThirdAccount")
	public Result<String> registerBindThirdAccount(@RequestBody JSONObject jsonObject, SysUser user) {
		//Số điện thoại
		String phone = jsonObject.getString("phone");
		//Mã xác thực
		String smscode = jsonObject.getString("smscode");
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE + phone;
		Object code = redisUtil.get(redisKey);
		//Bên thứ ba uuid
		String thirdUserUuid = jsonObject.getString("thirdUserUuid");
		String username = jsonObject.getString("username");
		//Nếu chưa đặt tên người dùng thì dùng số điện thoại làm tên người dùng
		if (oConvertUtils.isEmpty(username)) {
			username = phone;
		}
		//Chưa đặt mật khẩu thì tạo ngẫu nhiên một mật khẩu
		String password = jsonObject.getString("password");
		if (oConvertUtils.isEmpty(password)) {
			password = RandomUtil.randomString(8);
		}
		String email = jsonObject.getString("email");
		SysUser sysUser1 = sysUserService.getUserByName(username);
		if (sysUser1 != null) {
			return Result.error("Tên người dùng đã được đăng ký");
		}
		SysUser sysUser2 = sysUserService.getUserByPhone(phone);
		if (sysUser2 != null) {
			return Result.error("Số điện thoại này đã được đăng ký");
		}
		if (oConvertUtils.isNotEmpty(email)) {
			SysUser sysUser3 = sysUserService.getUserByEmail(email);
			if (sysUser3 != null) {
				return Result.error("Email đã được đăng ký");
			}
		}
		if (null == code) {
			return Result.error("Mã xác minh điện thoại đã hết hiệu lực, vui lòng lấy lại");
		}
		if (!smscode.equals(code.toString())) {
			return Result.error("Mã xác minh điện thoại sai");
		}
		String realname = jsonObject.getString("realname");
		if (oConvertUtils.isEmpty(realname)) {
			realname = username;
		}
		try {
			//Lưu bảng người dùng
			user.setCreateTime(new Date());
			String salt = oConvertUtils.randomGen(8);
			String passwordEncode = PasswordUtil.encrypt(username, password, salt);
			user.setSalt(salt);
			user.setUsername(username);
			user.setRealname(realname);
			user.setPassword(passwordEncode);
			user.setEmail(email);
			user.setPhone(phone);
			user.setStatus(CommonConstant.USER_UNFREEZE);
			user.setDelFlag(CommonConstant.DEL_FLAG_0);
			user.setActivitiSync(CommonConstant.ACT_SYNC_1);
			sysUserService.addUserWithRole(user, "");
			//Lưu bảng người dùng bên thứ ba
			sysThirdAccountService.updateThirdUserId(user, thirdUserUuid);
			String token = saveToken(user);
			return Result.ok(token);
		} catch (Exception e) {
			return Result.error("Đăng ký thất bại");
		}
	}

	/**
	 * Thiết lập thông tin Tenant và phòng ban của người dùng
	 *
	 * @param sysUser
	 * @param obj
	 * @param result
	 */
	private void setUserTenantAndDepart(SysUser sysUser, JSONObject obj, Result<JSONObject> result) {
		//1.Đặt tenant đăng nhập
		sysUserService.setLoginTenant(sysUser, obj, sysUser.getUsername(), result);
		//2.Đặt phòng ban đăng nhập
		String orgCode = sysUser.getOrgCode();
		//Phòng ban không trống thì vẫn dùng phòng ban ban đầu code
		if(StringUtils.isEmpty(orgCode)){
			List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
			//Nếu phòng ban không trống thì lấy phòng ban đầu tiên làm phòng ban đăng nhập hiện tại
			if(CollectionUtil.isNotEmpty(departs)){
				orgCode = departs.get(0).getOrgCode();
				sysUser.setOrgCode(orgCode);
				this.sysUserService.updateUserDepart(sysUser.getUsername(), orgCode,null);
			}
		}
	}

	/**
	 * Đăng nhập DingTalk phiên bản mới
	 *
	 * @param authCode
	 * @param state
	 * @param tenantId
	 * @param response
	 * @return
	 */
	@ResponseBody
	@GetMapping("/oauth2/dingding/login")
	public String OauthDingDingLogin(@RequestParam(value = "authCode", required = false) String authCode,
									 @RequestParam("state") String state,
									 @RequestParam(name = "tenantId",defaultValue = "0") String tenantId,
									 HttpServletRequest request,
									 HttpServletResponse response) {
		SysUser loginUser = thirdAppDingtalkService.oauthDingDingLogin(authCode,Integer.valueOf(tenantId));
		try {
			String redirect = "";
			if (state.indexOf("?") > 0) {
				String[] arr = state.split("\\?");
				state = arr[0];
				if(arr.length>1){
					redirect = arr[1];
				}
			}
			//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-2x3m-cj6f-5894/GHSA-jj4x-xwpm-5vx8] Địa chỉ chuyển hướng phải nằm trong whitelist, tránh token đăng nhập bị đưa sang trang bên ngoài-----------
			if (!isSafeRedirect(state, request)) {
				return "Địa chỉ chuyển hướng không hợp lệ";
			}
			//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-2x3m-cj6f-5894/GHSA-jj4x-xwpm-5vx8] Địa chỉ chuyển hướng phải nằm trong whitelist, tránh token đăng nhập bị đưa sang trang bên ngoài-----------
			String token = saveToken(loginUser);
			state += "/oauth2-app/login?oauth2LoginToken=" + URLEncoder.encode(token, "UTF-8") + "&tenantId=" + URLEncoder.encode(tenantId, "UTF-8");
			state += "&thirdType=DINGTALK";
			if (redirect != null && redirect.length() > 0) {
				state += "&" + redirect;
			}
			log.info("OAuth2 Địa chỉ chuyển hướng đăng nhập: " + state);
			try {
				response.sendRedirect(state);
				return "ok";
			} catch (IOException e) {
				log.error(e.getMessage(),e);
				return "Chuyển hướng thất bại";
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(),e);
			return "Giải mã thất bại";
		}
	}

	/**
	 * Lấy id doanh nghiệp và id ứng dụng id
	 * @param tenantId
	 * @return
	 */
	@ResponseBody
	@GetMapping("/get/corpId/clientId")
	public Result<SysThirdAppConfig> getCorpIdClientId(@RequestParam(value = "tenantId", defaultValue = "0") String tenantId){
		Result<SysThirdAppConfig> result = new Result<>();
		SysThirdAppConfig sysThirdAppConfig = thirdAppDingtalkService.getCorpIdClientId(Integer.valueOf(tenantId));
		result.setSuccess(true);
		result.setResult(sysThirdAppConfig);
		return result;
	}
}