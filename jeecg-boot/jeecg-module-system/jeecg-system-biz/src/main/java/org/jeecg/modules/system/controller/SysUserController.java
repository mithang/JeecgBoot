package org.jeecg.modules.system.controller;


import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.PermissionData;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.enums.SysRoleEnum;
import org.jeecg.common.constant.PasswordConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.*;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.excelstyle.ExcelExportSysUserStyle;
import org.jeecg.modules.system.model.DepartIdModel;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.ImportSysUserCache;
import org.jeecg.modules.system.vo.SysDepartUsersVO;
import org.jeecg.modules.system.vo.SysUserExportVo;
import org.jeecg.modules.system.vo.SysUserGroupVO;
import org.jeecg.modules.system.vo.SysUserRoleVO;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng người dùng Controller frontend
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

	@Autowired
	private ISysUserService sysUserService;

    @Autowired
    private ISysDepartService sysDepartService;

	@Autowired
	private ISysUserRoleService sysUserRoleService;

	@Autowired
	private ISysUgroupUserService sysUgroupUserService;

	@Autowired
	private ISysUserDepartService sysUserDepartService;

    @Autowired
    private ISysDepartRoleUserService departRoleUserService;

    @Autowired
    private ISysDepartRoleService departRoleService;

	@Autowired
	private RedisUtil redisUtil;

    @Value("${jeecg.path.upload}")
    private String upLoadPath;

    @Autowired
    private BaseCommonService baseCommonService;

    @Autowired
    private ISysPositionService sysPositionService;

    @Autowired
    private ISysUserTenantService userTenantService;

    @Autowired
    private ISysTenantPackService sysTenantPackService;

    @Autowired
    private JeecgRedisClient jeecgRedisClient;
    @Autowired
    private JeecgBaseConfig jeecgBaseConfig;
    
    /**
     * Lấy dữ liệu người dùng thuộc tenant (hỗ trợ cô lập tenant)
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @PermissionData(pageComponent = "system/UserList")
    @RequiresPermissions(value = {"system:user:list", "system:user:select"}, logical = Logical.OR)
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysUser>> queryPageList(SysUser user,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
        //------------------------------------------------------------------------------------------------
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TenantContext.getTenant(), "-1");
            List<String> userIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (oConvertUtils.listIsNotEmpty(userIds)) {
                queryWrapper.in("id", userIds);
            }else{
                queryWrapper.eq("id", "Không tìm thấy người dùng nào qua tenant");
            }
        }
        //------------------------------------------------------------------------------------------------
        Result<IPage<SysUser>> result = sysUserService.queryPageList(req, queryWrapper, pageSize, pageNo);
        //update-begin---author:scott ---date:20260911  for：【Khắc phục bảo mật GHSA-8cj9-3528-wp9p】/sys/user/list Thiếu kiểm tra quyền ở cấp chức năng, người dùng đăng nhập tùy ý có thể lấy thông tin người dùng của toàn bộ tenant; tại đây khử nhạy cảm theo cấp quyền-----------
        if (!hasUserListAuth() && null != result.getResult()) {
            desensitizeUserContact(result.getResult());
        }
        //update-end---author:scott ---date:20260911  for：【Khắc phục bảo mật GHSA-8cj9-3528-wp9p】-----------
        return result;
	}

    /**
     * Xác định người dùng đang đăng nhập có quyền xem danh sách người dùng hay không (khi không có quyền, phản hồi cần loại bỏ các trường riêng tư)
     */
    private boolean hasUserListAuth() {
        try {
            Subject subject = SecurityUtils.getSubject();
            return subject.isPermitted("system:user:list") || subject.isPermitted("system:user:listAll");
        } catch (Exception e) {
            // Khi không lấy được ngữ cảnh quyền hạn thì xử lý theo quyền tối thiểu, tránh rò rỉ trường nhạy cảm
            log.warn("Lấy quyền hạn danh sách người dùng thất bại, xử lý theo hướng không có quyền hạn:" + e.getMessage());
            return false;
        }
    }

    /**
     * Whitelist các trường hiển thị khi không có quyền xem danh sách người dùng: tập hợp trường tối thiểu cần cho các kịch bản người dùng thông thường như component chọn người, danh bạ
     * Các trường không nằm trong whitelist đều đặt thành rỗng (số điện thoại, email, ngày sinh, tenant, ID nội bộ của vị trí, trường kiểm toán, v.v.),
     * Dùng whitelist thay vì xóa từng trường, có thể tránh bỏ sót khi entity thêm trường mới về sau gây rò rỉ trở lại
     */
    private static final Set<String> USER_LIST_VISIBLE_FIELDS = new HashSet<>(Arrays.asList(
            "id", "username", "realname", "avatar", "sex", "workNo", "sign", "signEnable",
            "post", "postText", "orgCodeTxt", "status", "izHideContact", "sort"
    ));

    /**
     * Thực hiện che giấu dữ liệu cấp trường đối với phản hồi của người dùng không có quyền xem danh sách người dùng: chỉ giữ lại các trường trong whitelist
     */
    private void desensitizeUserContact(IPage<SysUser> page) {
        if (null == page || oConvertUtils.listIsEmpty(page.getRecords())) {
            return;
        }
        for (SysUser user : page.getRecords()) {
            if (null == user) {
                continue;
            }
            for (Field field : oConvertUtils.getAllFields(user)) {
                // Bỏ qua trường tĩnh, kiểu cơ bản (không thể đặt null) và các trường trong whitelist
                if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()
                        || USER_LIST_VISIBLE_FIELDS.contains(field.getName())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    field.set(user, null);
                } catch (Exception e) {
                    log.warn("Khử nhạy cảm danh sách người dùng thất bại, trường:{}，lý do:{}", field.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Lấy dữ liệu người dùng hệ thống (truy vấn tất cả người dùng, không cách ly tenant)
     *
     * @param user
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequiresPermissions("system:user:listAll")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryAllPageList(SysUser user, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(user, req.getParameterMap());
        return sysUserService.queryPageList(req, queryWrapper, pageSize, pageNo);
    }

    @RequiresPermissions("system:user:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<SysUser> add(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		String selectedRoles = jsonObject.getString("selectedroles");
		String selectedDeparts = jsonObject.getString("selecteddeparts");
		try {
			SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
			user.setCreateTime(new Date());//Đặt thời gian tạo
			String salt = oConvertUtils.randomGen(8);
			user.setSalt(salt);
			String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
			user.setPassword(passwordEncode);
			user.setStatus(1);
			user.setDelFlag(CommonConstant.DEL_FLAG_0);
			//Trường bảng người dùng org_code Không thể đặt giá trị của nó tại đây
            user.setOrgCode(null);
            user.setLastPwdUpdateTime(new Date());
			// Lưu người dùng qua một service để đảm bảo transaction
            //Lấy tenant ids
            String relTenantIds = jsonObject.getString("relTenantIds");
            sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, false);
            baseCommonService.addLog("Thêm người dùng, username: " +user.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

    @RequiresPermissions("system:user:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysUser> edit(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		try {
			SysUser sysUser = sysUserService.getById(jsonObject.getString("id"));
			baseCommonService.addLog("Chỉnh sửa người dùng, username: " +sysUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
			if(sysUser==null) {
				result.error500("Không tìm thấy thực thể tương ứng");
			}else {
				SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
				user.setUpdateTime(new Date());
				//String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), sysUser.getSalt());
				user.setPassword(sysUser.getPassword());
				String roles = jsonObject.getString("selectedroles");
                String departs = jsonObject.getString("selecteddeparts");
                if(oConvertUtils.isEmpty(departs)){
                    //vue3.0 Frontend chỉ truyền departIds
                    departs=user.getDepartIds();
                }
                //Trường bảng người dùng org_code Không thể đặt giá trị của nó tại đây
                user.setOrgCode(null);
                // Sửa người dùng qua một service để đảm bảo transaction
                //Lấy tenant ids
                String relTenantIds = jsonObject.getString("relTenantIds");
                String updateFromPage = jsonObject.getString("updateFromPage");
                //update-begin---author:wangshuai---date:2025-11-12---for:【JHHB-776] Chỉnh sửa người dùng, nên tra dữ liệu cũ từ cơ sở dữ liệu; trang truyền trường nào thì dùng các trường đó ghi đè kết quả truy vấn cơ sở dữ liệu, rồi mới cập nhật---
                oConvertUtils.copyNonNullFields(user, sysUser);
                sysUserService.editUser(sysUser, roles, departs, relTenantIds, updateFromPage);
                //update-end---author:wangshuai---date:2025-11-12---for:【JHHB-776] Chỉnh sửa người dùng, nên tra dữ liệu cũ từ cơ sở dữ liệu; trang truyền trường nào thì dùng các trường đó ghi đè kết quả truy vấn cơ sở dữ liệu, rồi mới cập nhật---
				result.success("Chỉnh sửa thành công!");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}

    /**
     * Thêm người dùng [Chỉ dùng cho chế độ tenant ở backend, QiaoQiaoYun không dùng cái này]
     *
     * @param jsonObject
     * @return
     */
    @RequiresPermissions("system:user:addTenantUser")
    @RequestMapping(value = "/addTenantUser", method = RequestMethod.POST)
    public Result<SysUser> addTenantUser(@RequestBody JSONObject jsonObject) {
        Result<SysUser> result = new Result<SysUser>();
        String selectedRoles = jsonObject.getString("selectedroles");
        String selectedDeparts = jsonObject.getString("selecteddeparts");
        try {
            SysUser user = JSON.parseObject(jsonObject.toJSONString(), SysUser.class);
            user.setCreateTime(new Date());//Đặt thời gian tạo
            String salt = oConvertUtils.randomGen(8);
            user.setSalt(salt);
            String passwordEncode = PasswordUtil.encrypt(user.getUsername(), user.getPassword(), salt);
            user.setPassword(passwordEncode);
            user.setStatus(1);
            user.setDelFlag(CommonConstant.DEL_FLAG_0);
            //Trường bảng người dùng org_code Không thể đặt giá trị của nó tại đây
            user.setOrgCode(null);
            // Lưu người dùng qua một service để đảm bảo transaction
            //Lấy tenant ids
            String relTenantIds = jsonObject.getString("relTenantIds");
            sysUserService.saveUser(user, selectedRoles, selectedDeparts, relTenantIds, true);
            baseCommonService.addLog("Thêm người dùng, username: " + user.getUsername(), CommonConstant.LOG_TYPE_2, 2);
            result.success("Thêm thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

	/**
	 * Xóa người dùng
	 */
    @RequiresPermissions("system:user:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		baseCommonService.addLog("Xóa người dùng, id: " +id ,CommonConstant.LOG_TYPE_2, 3);
        List<String> userNameList = sysUserService.userIdToUsername(Arrays.asList(id));
		this.sysUserService.deleteUser(id);

        if (!userNameList.isEmpty()) {
            String joinedString = String.join(",", userNameList);
        }
		return Result.ok("Xóa người dùng thành công");
	}

	/**
	 * Xóa người dùng hàng loạt
	 */
    @RequiresPermissions("system:user:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		baseCommonService.addLog("Xóa hàng loạt người dùng, ids: " +ids ,CommonConstant.LOG_TYPE_2, 3);
        List<String> userNameList = sysUserService.userIdToUsername(Arrays.asList(ids.split(",")));
		this.sysUserService.deleteBatchUsers(ids);
		
        // Người dùng thay đổi, kích hoạt workflow đồng bộ
        if (!userNameList.isEmpty()) {
            String joinedString = String.join(",", userNameList);
        }
		return Result.ok("Xóa hàng loạt người dùng thành công");
	}

	/**
	  * Đóng băng&Mở khóa người dùng
	 * @param jsonObject
	 * @return
	 */
    @RequiresPermissions("system:user:frozenBatch")
	@RequestMapping(value = "/frozenBatch", method = RequestMethod.PUT)
	public Result<SysUser> frozenBatch(@RequestBody JSONObject jsonObject) {
		Result<SysUser> result = new Result<SysUser>();
		try {
			String ids = jsonObject.getString("ids");
			sysUserService.checkUserAdminRejectDel(ids);
			String status = jsonObject.getString("status");
			String[] arr = ids.split(",");
            // update-begin--author:wangshuai---date:20261028---for：【LHZP-1066][Quản lý hệ thống] Quản lý người dùng: người dùng đã bị đóng băng có thể đóng băng lại
			// Người dùng đã bị đóng băng có thể đóng băng lại, người dùng đã ở trạng thái đích thì không được phép thao tác lặp lại------------
			long sameStatusCount = sysUserService.count(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, Arrays.asList(arr)).eq(SysUser::getStatus, Integer.parseInt(status)));
			if (sameStatusCount > 0) {
				return Result.error(CommonConstant.USER_FREEZE.toString().equals(status) ? "Trong số người dùng đã chọn có người dùng đã bị đóng băng, vui lòng không đóng băng lặp lại!" : "Trong số người dùng đã chọn có người dùng chưa bị đóng băng, không cần giải đông!");
			}
            // update-end--author:wangshuai---date:20261028---for：【LHZP-1066][Quản lý hệ thống] Quản lý người dùng: người dùng đã bị đóng băng có thể đóng băng lại
            for (String id : arr) {
				if(oConvertUtils.isNotEmpty(id)) {
                    // Giải thích logic mã: [QQYUN-5577]Danh sách người dùng-Đóng băng người dùng rồi mở đóng băng lại, người dùng vẫn không đăng nhập được, có vấn đề về bộ nhớ đệm #5066------------
                    sysUserService.updateStatus(id,status);
                }
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại"+e.getMessage());
		}
		result.success("Thao tác thành công!");
		return result;

    }
    /**
     * Giao diện đặt lại mật khẩu hệ thống
     * @param usernames
     * @return
     */
    @RequiresRoles({"admin"})
    @RequiresPermissions("system:user:resetPassword")
	@RequestMapping(value = "/resetPassword", method = RequestMethod.PUT)
	public Result<SysUser> resetPassword(@RequestParam(name = "usernames") String usernames) {
		Result<SysUser> result = new Result<SysUser>();
		try {
            sysUserService.resetToSysPassword(usernames);
            result.success("Thao tác thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500(e.getMessage());
		}
		return result;

    }

    @RequiresPermissions("system:user:queryById")
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public Result<SysUser> queryById(@RequestParam(name = "id", required = true) String id) {
        Result<SysUser> result = new Result<SysUser>();
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null) {
            result.error500("Không tìm thấy thực thể tương ứng");
        } else {
            result.setResult(sysUser);
            result.setSuccess(true);
        }
        return result;
    }

    @RequiresPermissions("system:user:queryUserRole")
    @RequestMapping(value = "/queryUserRole", method = RequestMethod.GET)
    public Result<List<String>> queryUserRole(@RequestParam(name = "userid", required = true) String userid) {
        Result<List<String>> result = new Result<>();
        List<String> list = new ArrayList<String>();
        List<SysUserRole> userRole = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, userid));
        if (userRole == null || userRole.size() <= 0) {
            result.error500("Không tìm thấy thông tin vai trò liên quan của người dùng");
        } else {
            for (SysUserRole sysUserRole : userRole) {
                list.add(sysUserRole.getRoleId());
            }
            result.setSuccess(true);
            result.setResult(list);
        }
        return result;
    }


    /**
	  *  Kiểm tra tài khoản người dùng có duy nhất hay không<br>
	  *  Có thể kiểm tra các thứ khác, cần kiểm tra gì thì truyền cái đó...
     *
     * @param sysUser
     * @return
     */
    @RequestMapping(value = "/checkOnlyUser", method = RequestMethod.GET)
    public Result<Boolean> checkOnlyUser(SysUser sysUser) {
        Result<Boolean> result = new Result<>();
        //Nếu tham số này là false thì chương trình xảy ra ngoại lệ
        result.setResult(true);
        try {
            //Truy vấn thông tin người dùng mới qua thông tin truyền vào
            sysUser.setPassword(null);
            SysUser user = sysUserService.getOne(new QueryWrapper<SysUser>(sysUser));
            if (user != null) {
                result.setSuccess(false);
                result.setMessage("Tài khoản người dùng đã tồn tại");
                return result;
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            return result;
        }
        result.setSuccess(true);
        return result;
    }

    /**
     * Đổi mật khẩu
     */
    @RequiresPermissions("system:user:changepwd")
    @RequestMapping(value = "/changePassword", method = RequestMethod.PUT)
    public Result<?> changePassword(@RequestBody SysUser sysUser, HttpServletRequest request) {
        //-------------------------------------------------------------------------------------
        //Thêm check để ngăn chặn việc gửi SMS ác ý
        String clientIp = IpUtils.getIpAddr(request);
        if(!DySmsLimit.canSendSms(clientIp)){
            log.warn("-------- IP Địa chỉ:{}, Yêu cầu giao diện SMS quá nhiều, có rủi ro tấn công!", clientIp);
            return Result.error("Yêu cầu giao diện SMS quá nhiều, vui lòng thử lại sau!");
        }
        //-------------------------------------------------------------------------------------
        SysUser u = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, sysUser.getUsername()));
        if (u == null) {
            return Result.error("Người dùng không tồn tại!");
        }
        sysUser.setId(u.getId());
        // Giải thích logic mã: [VUEN-234]Đổi mật khẩu thêm nhật ký nhạy cảm------------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("Chỉnh sửa người dùng "+sysUser.getUsername()+" mật khẩu, người thực hiện: " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
        return sysUserService.changePassword(sysUser);
    }

    /**
     * Truy vấn dữ liệu liên kết giữa người dùng và phòng ban được chỉ định
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/userDepartList", method = RequestMethod.GET)
    public Result<List<DepartIdModel>> getUserDepartsList(@RequestParam(name = "userId", required = true) String userId) {
        Result<List<DepartIdModel>> result = new Result<>();
        try {
            List<DepartIdModel> depIdModelList = this.sysUserDepartService.queryDepartIdsOfUser(userId);
            if (depIdModelList != null && depIdModelList.size() > 0) {
                result.setSuccess(true);
                result.setMessage("Tra cứu thành công");
                result.setResult(depIdModelList);
            } else {
                result.setSuccess(false);
                result.setMessage("Tra cứu thất bại");
            }
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Đã xảy ra ngoại lệ trong quá trình tìm kiếm: " + e.getMessage());
            return result;
        }

    }

    /**
     * Tạo ra vấn đề không có khóa chính khi thêm người dùng,Trả về cho giao diện,Liên kết dữ liệu phòng ban theo id này
     *
     * @return
     */
    @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    public Result<String> generateUserId() {
        Result<String> result = new Result<>();
        //System.out.println("Tôi đã thực thi,Tạo người dùng ID==============================");
        String userId = UUID.randomUUID().toString().replace("-", "");
        result.setSuccess(true);
        result.setResult(userId);
        return result;
    }

    /**
     * Truy vấn thông tin người dùng theo ID phòng ban
     *
     * @param id
     * @return
     */
    @RequiresPermissions("system:user:select")
    @RequestMapping(value = "/queryUserByDepId", method = RequestMethod.GET)
    public Result<List<SysUser>> queryUserByDepId(@RequestParam(name = "id", required = true) String id,@RequestParam(name="realname",required=false) String realname,
                                                  @RequestParam(name="status",required=false) Integer status) {
        Result<List<SysUser>> result = new Result<>();
        //List<SysUser> userList = sysUserDepartService.queryUserByDepId(id);
        SysDepart sysDepart = sysDepartService.getById(id);
        List<SysUser> userList = sysUserDepartService.queryUserByDepCode(sysDepart.getOrgCode(),realname,status);

        //Truy vấn hàng loạt phòng ban của người dùng
        //step.1 Trước tiên, lấy toàn bộ useids
        //step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua useids
        List<String> userIds = userList.stream().map(SysUser::getId).collect(Collectors.toList());
        if(userIds!=null && userIds.size()>0){
            Map<String,String>  useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            userList.forEach(item->{
                //TODO Tạm mượn trường này để hiển thị trên trang
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }

        try {
            result.setSuccess(true);
            result.setResult(userList);
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            result.setSuccess(false);
            return result;
        }
    }

    /**
     * Component chọn người dùng chuyên dụng, truy vấn phân trang theo tài khoản người dùng hoặc phòng ban
     * @param departId
     * @param username
     * @return
     */
    @RequiresPermissions("system:user:select")
    @RequestMapping(value = "/queryUserComponentData", method = RequestMethod.GET)
    public Result<IPage<SysUser>> queryUserComponentData(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name="realname",required=false) String realname,
            @RequestParam(name="username",required=false) String username,
            @RequestParam(name="isMultiTranslate",required=false) String isMultiTranslate,
            @RequestParam(name="id",required = false) String id) {
        // Giải thích logic mã: VUEN-1702【Vấn đề bị cấm】Lỗ hổng SQL injection
        String[] arr = new String[]{departId, realname, username, id};
        SqlInjectionUtil.filterContent(arr, SymbolConstant.SINGLE_QUOTATION_MARK);
        IPage<SysUser> pageList = sysUserDepartService.queryDepartUserPageList(departId, username, realname, pageSize, pageNo,id,isMultiTranslate);
        return Result.OK(pageList);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param sysUser
     */
    @RequiresPermissions("system:user:export")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(SysUser sysUser,HttpServletRequest request) {
        // Step.1 Lắp ráp điều kiện truy vấn
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, request.getParameterMap());
        queryWrapper.ne("username", "_reserve_user_external");
        //Step.2 AutoPoi Xuất Excel
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        // Giải thích logic mã: [03]Xuất người dùng, nếu chọn dữ liệu thì chỉ xuất dữ liệu liên quan--------------------
        String selections = request.getParameter("selections");
       if(!oConvertUtils.isEmpty(selections)){
           queryWrapper.in("id",selections.split(","));
       }
        //Có tồn tại phòng ban hay không id
        boolean izDepartId = true;
        String departId = request.getParameter("departId");
        if (oConvertUtils.isNotEmpty(departId)) {
            LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
            query.in(SysUserDepart::getDepId, Arrays.asList(departId.split(",")));
            List<SysUserDepart> list = sysUserDepartService.list(query);
            List<String> userIds = list.stream().map(SysUserDepart::getUserId).collect(Collectors.toList());
            if (oConvertUtils.listIsNotEmpty(userIds)) {
                queryWrapper.in("id", userIds);
            }else{
                izDepartId = false;
            }
        }
        List<SysUserExportVo> list = new ArrayList<>();
        // Giải thích logic mã: 【JHHB-762】【Quản lý người dùng】Cần hỗ trợ truy vấn người dùng theo cơ cấu tổ chức---
        if(izDepartId){
            List<SysUser> pageList = sysUserService.list(queryWrapper);
            list  = sysUserService.getDepartAndRoleExportMsg(pageList);
        }
        //Tên file xuất
        mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách người dùng");
        mv.addObject(NormalExcelConstants.CLASS, SysUserExportVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        ExportParams exportParams = new ExportParams("Quy tắc nhập:\n" +
                "1. Tên người dùng là mục bắt buộc, chỉ hỗ trợ nhập dữ liệu mới;\n" +
                "2. Nhiều phòng ban, vai trò hoặc phòng ban phụ trách, vui lòng dùng dấu chấm phẩy tiếng Anh ; phân tách, ví dụ: Phòng Tài chính;Phòng Nghiên cứu và Phát triển；\n" +
                "3. Cấp phòng ban vui lòng dùng dấu gạch chéo tiếng Anh / phân tách, ví dụ: Công ty Bắc Kinh/Phòng Tài chính/Phòng Tài chính 1;\n" +
                "4. Loại phòng ban cần nhất quán với cấp bậc phòng ban, cũng dùng /  phân tách, ví dụ: Công ty/Phòng ban/Phòng ban hoặc 1/3/3，nhiều loại dùng ; Phân tách. Mã loại tổ chức: công ty(1)，công ty con(4), phòng ban(3)；\n" +
                "5. Phòng ban khớp theo tên người dùng; nếu tồn tại nhiều phòng ban thì liên kết với phòng ban mới tạo nhất, nếu không tồn tại thì tự động thêm mới;\n" +
                "6. Quy tắc import phòng ban phụ trách giống với phòng ban trực thuộc; nếu phòng ban trực thuộc không chứa phòng ban phụ trách thì không liên kết phòng ban phụ trách;\n" +
                "7. Khi import vị trí chính của người dùng, hệ thống sẽ tự động tạo vị trí mới dưới phòng ban; khi cấp bậc rỗng thì mặc định không liên kết với vị trí.", "Người xuất:" + user.getRealname(), "Thông tin xuất");
        exportParams.setTitleHeight((short)70);
        exportParams.setStyle(ExcelExportSysUserStyle.class);
        exportParams.setImageBasePath(upLoadPath);
        //Xuất ra xlsx
        exportParams.setType(ExcelType.XSSF);
        mv.addObject(NormalExcelConstants.PARAMS, exportParams);
        mv.addObject(NormalExcelConstants.DATA_LIST, list);
        //Xuất người dùng hỗ trợ trường xuất
        String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
        if(oConvertUtils.isNotEmpty(exportFields)){
            mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
        }
        return mv;
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:user:import")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response)throws IOException {
        //return ImportOldUserUtil.importOldSysUser(request);
        return sysUserService.importSysUser(request);
    }

    /**
	 * @Chức năng: truy vấn hàng loạt theo id
	 * @param userIds
	 * @return
	 */
	@RequestMapping(value = "/queryByIds", method = RequestMethod.GET)
	public Result<Collection<SysUser>> queryByIds(@RequestParam(name = "userIds") String userIds) {
		Result<Collection<SysUser>> result = new Result<>();
		String[] userId = userIds.split(",");
		Collection<String> idList = Arrays.asList(userId);
		Collection<SysUser> userRole = sysUserService.listByIds(idList);
		result.setSuccess(true);
		result.setResult(userRole);
		return result;
	}


    /**
     * @Chức năng: truy vấn hàng loạt theo id
     * @param userNames
     * @return
     */
    @RequestMapping(value = "/queryByNames", method = RequestMethod.GET)
    public Result<List<Map<String, Object>>> queryByNames(@RequestParam(name = "userNames") String userNames) {
        Result<List<Map<String, Object>>> result = new Result<>();
        String[] names = userNames.split(",");
        //update-begin---author:zzl ---date:2026-04-03  for: Chỉ trả về các trường username và realname----
        List<SysUser> userList = sysUserService.lambdaQuery()
                .in(SysUser::getUsername, names)
                .select(SysUser::getUsername, SysUser::getRealname)
                .list();
        List<Map<String, Object>> dataList = userList.stream().map(user -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("username", user.getUsername());
            map.put("realname", user.getRealname());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        result.setSuccess(true);
        result.setResult(dataList);
        //update-end---author:zzl ---date:2026-04-03  for: Chỉ trả về các trường username và realname----
        return result;
    }

    /**
     * @Chức năng: truy vấn người dùng và thông tin phòng ban theo userName
     * @param userName
     * @return
     */
    @RequestMapping(value = "/queryUserAndDeptByName", method = RequestMethod.GET)
    public Result<Map<String,String>> queryUserAndDeptByName(@RequestParam(name = "userName") String userName) {
        Map<String,String> userInfo= sysUserService.queryUserAndDeptByName(userName);
        return Result.ok(userInfo);
    }

	/**
	 * Người dùng trang chủ đặt lại mật khẩu
	 */
    @RequiresPermissions("system:user:updatepwd")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.PUT)
	public Result<?> updatePassword(@RequestBody JSONObject json) {
		String username = json.getString("username");
		String oldpassword = json.getString("oldpassword");
		String password = json.getString("password");
		String confirmpassword = json.getString("confirmpassword");
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        if(!sysUser.getUsername().equals(username)){
            return Result.error("Chỉ được phép sửa mật khẩu của chính mình!");
        }
		SysUser user = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
		if(user==null) {
			return Result.error("Người dùng không tồn tại!");
		}
        // Giải thích logic mã: [VUEN-234]Đổi mật khẩu thêm nhật ký nhạy cảm------------
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        baseCommonService.addLog("Thay đổi mật khẩu, username: " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
		return sysUserService.resetPassword(username,oldpassword,password,confirmpassword);
	}

    @RequestMapping(value = "/userRoleList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> userRoleList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                               @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String roleId = req.getParameter("roleId");
        String username = req.getParameter("username");
        String realname = req.getParameter("realname");
        IPage<SysUser> pageList = sysUserService.getUserByRoleId(page,roleId,username,realname);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * Thêm người dùng cho vai trò được chỉ định
     *
     * @param
     * @return
     */
    @RequiresPermissions("system:user:addUserRole")
    @RequestMapping(value = "/addSysUserRole", method = RequestMethod.POST)
    public Result<String> addSysUserRole(@RequestBody SysUserRoleVO sysUserRoleVO) {
        Result<String> result = new Result<String>();
        //TODO Phán đoán vai trò đang thao tác có thuộc tenant đang đăng nhập hay không
        try {
            String sysRoleId = sysUserRoleVO.getRoleId();
            for(String sysUserId:sysUserRoleVO.getUserIdList()) {
                SysUserRole sysUserRole = new SysUserRole(sysUserId,sysRoleId);
                QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
                queryWrapper.eq("role_id", sysRoleId).eq("user_id",sysUserId);
                SysUserRole one = sysUserRoleService.getOne(queryWrapper);
                if(one==null){
                    sysUserRoleService.save(sysUserRole);
                }

            }
            result.setMessage("Thêm thành công!");
            result.setSuccess(true);
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Đã xảy ra lỗi: " + e.getMessage());
            return result;
        }
    }
    /**
     *   Xóa quan hệ người dùng của vai trò được chỉ định
     * @param
     * @return
     */
    @RequiresPermissions("system:user:deleteRole")
    @RequestMapping(value = "/deleteUserRole", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRole(@RequestParam(name="roleId") String roleId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        Result<SysUserRole> result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
            queryWrapper.eq("role_id", roleId).eq("user_id",userId);
            sysUserRoleService.remove(queryWrapper);
            result.success("Xóa thành công!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }

    /**
     * Xóa hàng loạt quan hệ người dùng của vai trò được chỉ định
     *
     * @param
     * @return
     */
    @RequiresPermissions("system:user:deleteRoleBatch")
    @RequestMapping(value = "/deleteUserRoleBatch", method = RequestMethod.DELETE)
    public Result<SysUserRole> deleteUserRoleBatch(
            @RequestParam(name="roleId") String roleId,
            @RequestParam(name="userIds",required=true) String userIds) {
        Result<SysUserRole> result = new Result<SysUserRole>();
        try {
            QueryWrapper<SysUserRole> queryWrapper = new QueryWrapper<SysUserRole>();
            queryWrapper.eq("role_id", roleId).in("user_id",Arrays.asList(userIds.split(",")));
            sysUserRoleService.remove(queryWrapper);
            result.success("Xóa thành công!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }
    /**
     * Thêm người dùng vào nhóm người dùng chỉ định
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/addSysUserGroup", method = RequestMethod.POST)
    public Result<String> addSysUserGroup(@RequestBody SysUserGroupVO sysUserGroupVO) {
        Result<String> result = new Result<String>();
        try {
            String groupId = sysUserGroupVO.getGroupId();
            for(String sysUserId : sysUserGroupVO.getUserIdList()) {
                SysUgroupUser sysUgroupUser = new SysUgroupUser(sysUserId,groupId);
                QueryWrapper<SysUgroupUser> queryWrapper = new QueryWrapper<SysUgroupUser>();
                queryWrapper.eq("group_id", groupId).eq("user_id",sysUserId);
                SysUgroupUser one = sysUgroupUserService.getOne(queryWrapper);
                if(one==null){
                    sysUgroupUserService.save(sysUgroupUser);
                }

            }
            result.setMessage("Thêm thành công!");
            result.setSuccess(true);
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Đã xảy ra lỗi: " + e.getMessage());
            return result;
        }
    }
    /**
     *   Xóa quan hệ người dùng của nhóm người dùng được chỉ định
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteGroupUser", method = RequestMethod.DELETE)
    public Result<SysUgroupUser> deleteGroupUser(@RequestParam(name="groupId") String groupId,
                                                 @RequestParam(name="userId",required=true) String userId
    ) {
        Result<SysUgroupUser> result = new Result<SysUgroupUser>();
        try {
            QueryWrapper<SysUgroupUser> queryWrapper = new QueryWrapper<SysUgroupUser>();
            queryWrapper.eq("group_id", groupId).eq("user_id",userId);
            sysUgroupUserService.remove(queryWrapper);
            result.success("Xóa thành công!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }
    /**
     * Xóa hàng loạt quan hệ người dùng thuộc nhóm người dùng chỉ định
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/deleteUserGroupBatch", method = RequestMethod.DELETE)
    public Result<SysUgroupUser> deleteUserGroupBatch(
            @RequestParam(name="groupId") String groupId,
            @RequestParam(name="userIds",required=true) String userIds) {
        Result<SysUgroupUser> result = new Result<SysUgroupUser>();
        try {
            QueryWrapper<SysUgroupUser> queryWrapper = new QueryWrapper<SysUgroupUser>();
            queryWrapper.eq("group_id", groupId).in("user_id",Arrays.asList(userIds.split(",")));
            sysUgroupUserService.remove(queryWrapper);
            result.success("Xóa thành công!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }
    /**
     * Truy vấn danh sách phân trang người dùng trong nhóm người dùng
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @RequestMapping(value = "/groupUserList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> groupUserList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                           @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String groupId = req.getParameter("groupId");
        String username = req.getParameter("username");
        String realname = req.getParameter("realname");
        Integer status = oConvertUtils.getInteger(req.getParameter("status"), null);
        IPage<SysUser> pageList = sysUserService.getUserByUgroupId(page,groupId,username,realname,status);
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }
    /**
     * Danh sách người dùng phòng ban
     */
    @RequestMapping(value = "/departUserList", method = RequestMethod.GET)
    public Result<IPage<SysUser>> departUserList(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                 @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
        Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
        Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
        String depId = req.getParameter("depId");
        String username = req.getParameter("username");
		String realname = req.getParameter("realname");
        //Truy vấn theo ID phòng ban,Phòng ban hiện tại và tất cả phòng ban cấp dưới IDS
        List<String> subDepids = new ArrayList<>();
        //Khi id phòng ban trống, truy vấn tất cả người dùng thuộc phòng ban của tôi
        if(oConvertUtils.isEmpty(depId)){
            LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
            int userIdentity = user.getUserIdentity() != null?user.getUserIdentity():CommonConstant.USER_IDENTITY_1;
            // Giải thích logic mã: [QQYUN-10775]Mã xác thực có thể tái sử dụng #7674------------
            if(oConvertUtils.isNotEmpty(userIdentity) && userIdentity == CommonConstant.USER_IDENTITY_2
                    && oConvertUtils.isNotEmpty(user.getDepartIds())) {
                subDepids = sysDepartService.getMySubDepIdsByDepId(user.getDepartIds());
            }
        }else{
            subDepids = sysDepartService.getSubDepIdsByDepId(depId);
        }
        if(subDepids != null && subDepids.size()>0){
            IPage<SysUser> pageList = sysUserService.getUserByDepIds(page,subDepids,username,realname);
            //Truy vấn hàng loạt phòng ban của người dùng
            //step.1 Trước tiên, lấy toàn bộ useids
            //step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua useids
            List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
            if(userIds!=null && userIds.size()>0){
                Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
                pageList.getRecords().forEach(item -> {
                    //Truy vấn hàng loạt phòng ban của người dùng
                    item.setOrgCode(useDepNames.get(item.getId()));
                });
            }
            //Cài đặt tenant id
            //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1141][Quản lý hệ thống] Thông tin liên kết người dùng khi chỉnh sửa phòng ban của tôi chưa hiển thị lại đúng------------
            pageList.setRecords(userTenantService.setUserTenantIds(pageList.getRecords()));
            //update-end---author:wangshuai ---date:20260811  for：[LHZP-1141][Quản lý hệ thống] Thông tin liên kết người dùng khi chỉnh sửa phòng ban của tôi chưa hiển thị lại đúng------------
            result.setSuccess(true);
            result.setResult(pageList);
        }else{
            result.setSuccess(true);
            result.setResult(null);
        }
        return result;
    }


    /**
     * Truy vấn người dùng theo orgCode, bao gồm cả người dùng thuộc các phòng ban con
     * Nếu một người dùng thuộc nhiều phòng ban thì sẽ hiển thị nhiều bản ghi, có thể tự xử lý thành một bản ghi
     */
    @GetMapping("/queryByOrgCode")
    public Result<?> queryByDepartId(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "orgCode") String orgCode,
            SysUser userParams
    ) {
        IPage<SysUserSysDepartModel> pageList = sysUserService.queryUserByOrgCode(orgCode, userParams, new Page(pageNo, pageSize));
        return Result.ok(pageList);
    }

    /**
     * Truy vấn người dùng theo orgCode, bao gồm người dùng thuộc các phòng ban con [không bao gồm người dùng thuộc vị trí]
     * API làm cho module danh bạ, gộp người dùng của nhiều phòng ban thành một bản ghi và chuyển thành định dạng thân thiện với frontend
     */
    @GetMapping("/queryByOrgCodeForAddressList")
    public Result<?> queryByOrgCodeForAddressList(
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "orgCode",required = false) String orgCode,
            SysUser userParams
    ) {
        IPage page = new Page(pageNo, pageSize);
        IPage<SysUserSysDepartModel> pageList = sysUserService.queryUserByOrgCode(orgCode, userParams, page);
        List<SysUserSysDepartModel> list = pageList.getRecords();

        // Ghi lại tất cả user đã xuất hiện user, key = userId
        Map<String, JSONObject> hasUser = new HashMap<>(list.size());

        JSONArray resultJson = new JSONArray(list.size());

        for (SysUserSysDepartModel item : list) {
            String userId = item.getId();
            // userId
            JSONObject getModel = hasUser.get(userId);
            // Trước đây đã tồn tại người dùng này, gộp trực tiếp dữ liệu
            if (getModel != null) {
                String departName = getModel.get("departName").toString();
                getModel.put("departName", (departName + " | " + item.getDepartName()));
            } else {
                // Chuyển đối tượng người dùng sang định dạng JSON và gộp thông tin phòng ban vào JSON
                JSONObject json = JSON.parseObject(JSON.toJSONString(item));
                json.remove("id");
                json.put("userId", userId);
                json.put("departId", item.getDepartId());
                json.put("departName", item.getDepartName());
//                json.put("avatar", item.getSysUser().getAvatar());
                resultJson.add(json);
                hasUser.put(userId, json);
            }
        }

        IPage<JSONObject> result = new Page<>(pageNo, pageSize, pageList.getTotal());
        result.setRecords(resultJson.toJavaList(JSONObject.class));
        return Result.ok(result);
    }

    /**
     * Truy vấn người dùng theo orgCode, bao gồm người dùng thuộc công ty, công ty con, phòng ban của vị trí
     */
    @RequiresPermissions("system:user:select")
    @GetMapping("/queryDepartPostByOrgCode")
    public Result<?> queryDepartPostByOrgCode(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              @RequestParam(name = "orgCode",required = false) String orgCode,
                                              SysUser userParams
    ) {
        IPage page = new Page(pageNo, pageSize);
        IPage<SysUserSysDepPostModel> pageList = sysUserService.queryDepartPostUserByOrgCode(orgCode, userParams, page);
        return Result.ok(pageList);
    }

    /**
     * Truy vấn thông tin người dùng theo orgCode (đường dẫn đầy đủ của phòng ban, thông tin vị trí chính và vị trí kiêm nhiệm), bao gồm công ty, công ty con, phòng ban
     */
    @GetMapping("/queryDepartUserByOrgCode")
    public Result<IPage<SysUserSysDepPostModel>> queryDepartUserByOrgCode(@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              @RequestParam(name = "orgCode",required = false) String orgCode,
                                              SysUser userParams
    ) {
        IPage page = new Page(pageNo, pageSize);
        IPage<SysUserSysDepPostModel> pageList = sysUserService.queryDepartUserByOrgCode(orgCode, userParams, page);
        return Result.ok(pageList);
    }

    /**
     * Trong danh bạ, bấm vào người dùng để lấy chi tiết người dùng (bao gồm thông tin cơ bản của người dùng, đường dẫn đầy đủ của phòng ban, đường dẫn đầy đủ của vị trí chính và vị trí kiêm nhiệm)
     * 
     * @param userId
     * @return
     */
    @GetMapping("/getUserDetailByUserId")
    public Result<SysUserSysDepPostModel> getUserDetailByUserId(@RequestParam(name = "userId") String userId, HttpServletRequest request) {
        Result<SysUserSysDepPostModel> result = new Result<SysUserSysDepPostModel>();
        //update-begin---author:scott ---date:20261014  for: Ở chế độ đa tenant chỉ được xem chi tiết người dùng của tenant hiện tại, ngăn đọc thông tin người dùng xuyên tenant theo userId-----------
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request), 0);
            Integer count = userTenantService.userTenantIzExist(userId, tenantId);
            if (count == null || count == 0) {
                return Result.error("Không tìm thấy người dùng tương ứng");
            }
        }
        //update-end---author:scott ---date:20261014  for: Ở chế độ đa tenant chỉ được xem chi tiết người dùng của tenant hiện tại, ngăn đọc thông tin người dùng xuyên tenant theo userId-----------
        try {
            SysUserSysDepPostModel sysDepPostModel = sysUserService.getUserDetailByUserId(userId);
            result.setSuccess(true);
            result.setResult(sysDepPostModel);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Tìm kiếm thất bại: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Thêm người dùng tương ứng cho phòng ban được chỉ định
     */
    @RequiresPermissions("system:user:editDepartWithUser")
    @RequestMapping(value = "/editSysDepartWithUser", method = RequestMethod.POST)
    public Result<String> editSysDepartWithUser(@RequestBody SysDepartUsersVO sysDepartUsersVO) {
        Result<String> result = new Result<String>();
        try {
            String sysDepId = sysDepartUsersVO.getDepId();
            boolean updated = false;
            for(String sysUserId:sysDepartUsersVO.getUserIdList()) {
                SysUserDepart sysUserDepart = new SysUserDepart(null,sysUserId,sysDepId);
                QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
                queryWrapper.eq("dep_id", sysDepId).eq("user_id",sysUserId);
                SysUserDepart one = sysUserDepartService.getOne(queryWrapper);
                if(one==null){
                    updated = true;
                    sysUserDepartService.save(sysUserDepart);
                }
            }
            // 【JHHB-737】Sau khi cập nhật quan hệ thì xóa bộ nhớ đệm người dùng
            if (updated) {
                redisUtil.removeAll(CacheConstant.SYS_USERS_CACHE);
            }
            result.setMessage("Thêm thành công!");
            result.setSuccess(true);
            return result;
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.setSuccess(false);
            result.setMessage("Đã xảy ra lỗi: " + e.getMessage());
            return result;
        }
    }

    /**
     *   Xóa quan hệ người dùng của tổ chức được chỉ định
     */
    @RequiresPermissions("system:user:deleteUserInDepart")
    @RequestMapping(value = "/deleteUserInDepart", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepart(@RequestParam(name="depId") String depId,
                                                    @RequestParam(name="userId",required=true) String userId
    ) {
        Result<SysUserDepart> result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).eq("user_id",userId);
            boolean b = sysUserDepartService.remove(queryWrapper);
            if(b){
                List<SysDepartRole> sysDepartRoleList = departRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id",depId));
                List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
                if(roleIds != null && roleIds.size()>0){
                    QueryWrapper<SysDepartRoleUser> query = new QueryWrapper<>();
                    query.eq("user_id",userId).in("drole_id",roleIds);
                    departRoleUserService.remove(query);
                }
                result.success("Xóa thành công!");
            }else{
                result.error500("Phòng ban đang chọn không có quan hệ liên kết với người dùng!!");
            }
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }

    /**
     * Xóa hàng loạt quan hệ người dùng của tổ chức được chỉ định
     */
    @RequiresPermissions("system:user:deleteUserInDepartBatch")
    @RequestMapping(value = "/deleteUserInDepartBatch", method = RequestMethod.DELETE)
    public Result<SysUserDepart> deleteUserInDepartBatch(
            @RequestParam(name="depId") String depId,
            @RequestParam(name="userIds",required=true) String userIds) {
        Result<SysUserDepart> result = new Result<SysUserDepart>();
        try {
            QueryWrapper<SysUserDepart> queryWrapper = new QueryWrapper<SysUserDepart>();
            queryWrapper.eq("dep_id", depId).in("user_id",Arrays.asList(userIds.split(",")));
            boolean b = sysUserDepartService.remove(queryWrapper);
            if(b){
                departRoleUserService.removeDeptRoleUser(Arrays.asList(userIds.split(",")),depId);
            }else{
                result.error500("Xóa thất bại, người dùng đích không thuộc phòng ban hiện tại!");
                return result;
            }
            result.success("Xóa thành công!");
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Xóa thất bại!");
        }
        return result;
    }

    /**
     * Quản lý phòng ban-Danh sách người dùng: chuyển người dùng ra khỏi phòng ban hoặc vị trí (chỉ xóa quan hệ liên kết, không xóa người dùng)
     * Chỉ xử lý phòng ban hiện tại/Thành viên trực thuộc của vị trí; phòng ban sẽ đồng thời dọn dẹp các vị trí và phòng ban phụ trách thuộc phòng ban đó, vị trí sẽ xóa vị trí chính và xóa vị trí kiêm nhiệm
     *
     * @param depId   Phòng ban hoặc vị trí id
     * @param userIds ID người dùng, nhiều ID phân tách bằng dấu phẩy
     * @return
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    @RequiresPermissions("system:user:deleteUserInDepartBatch")
    @DeleteMapping("/removeDepartUser")
    public Result<String> removeDepartUser(@RequestParam(name = "depId") String depId,
                                           @RequestParam(name = "userIds") String userIds) {
        try {
            //Loại bỏ người dùng có giá trị trống và trùng lặp id
            List<String> userIdList = new ArrayList<>();
            String[] userIdArr = userIds.split(",");
            for (String userId : userIdArr) {
                if (oConvertUtils.isEmpty(userId) || userIdList.contains(userId)) {
                    continue;
                }
                userIdList.add(userId);
            }
            //Chỉ di chuyển ra khỏi phòng ban hiện tại/Thành viên trực thuộc của vị trí công việc
            List<String> removeUserIds = sysUserService.removeDepartUser(depId, userIdList);
            if (removeUserIds.isEmpty()) {
                return Result.error("Di chuyển ra thất bại, người dùng đích không thuộc phòng ban hiện tại/thành viên trực thuộc của vị trí!");
            }
            //Xóa vai trò phòng ban của người dùng trong phòng ban này
            departRoleUserService.removeDeptRoleUser(removeUserIds, depId);
            //Có người dùng không phải thành viên trực thuộc, chưa di chuyển ra
            int skipCount = userIdList.size() - removeUserIds.size();
            if (skipCount > 0) {
                return Result.OK("Đã loại bỏ" + removeUserIds.size() + "người, còn lại" + skipCount + "người không phải thuộc phòng ban hiện tại/thành viên trực thuộc của chức vụ, chưa được di chuyển ra");
            }
            return Result.OK("Loại bỏ thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("Loại bỏ thất bại!");
        }
    }
    
    /**
         *  Truy vấn tất cả các phòng ban của người dùng hiện tại/Mã phòng ban hiện tại
     * @return
     */
    @RequestMapping(value = "/getCurrentUserDeparts", method = RequestMethod.GET)
    public Result<Map<String,Object>> getCurrentUserDeparts() {
        Result<Map<String,Object>> result = new Result<Map<String,Object>>();
        try {
        	LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
            List<SysDepart> list = this.sysDepartService.queryUserDeparts(sysUser.getId());
            Map<String,Object> map = new HashMap(5);
            map.put("list", list);
            map.put("orgCode", sysUser.getOrgCode());
            result.setSuccess(true);
            result.setResult(map);
        }catch(Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Tìm kiếm thất bại!");
        }
        return result;
    }

    


	/**
	 * Giao diện đăng ký người dùng
	 * 
	 * @param jsonObject
	 * @param user
	 * @return
	 */
	@PostMapping("/register")
	public Result<JSONObject> userRegister(@RequestBody JSONObject jsonObject, SysUser user) {
		Result<JSONObject> result = new Result<JSONObject>();
		String phone = jsonObject.getString("phone");
		String smscode = jsonObject.getString("smscode");

        // Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
		String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);

		String username = jsonObject.getString("username");
		//Nếu chưa đặt tên người dùng thì dùng số điện thoại làm tên người dùng
		if(oConvertUtils.isEmpty(username)){
            username = phone;
        }
        //Chưa đặt mật khẩu thì tạo ngẫu nhiên một mật khẩu
		String password = jsonObject.getString("password");
		if(oConvertUtils.isEmpty(password)){
            password = RandomUtil.randomString(8);
        }
		String email = jsonObject.getString("email");
		SysUser sysUser1 = sysUserService.getUserByName(username);
		if (sysUser1 != null) {
			result.setMessage("Tên người dùng đã được đăng ký");
			result.setSuccess(false);
			return result;
		}
		SysUser sysUser2 = sysUserService.getUserByPhone(phone);
		if (sysUser2 != null) {
			result.setMessage("Số điện thoại này đã được đăng ký");
			result.setSuccess(false);
			return result;
		}

		if(oConvertUtils.isNotEmpty(email)){
            SysUser sysUser3 = sysUserService.getUserByEmail(email);
            if (sysUser3 != null) {
                result.setMessage("Email đã được đăng ký");
                result.setSuccess(false);
                return result;
            }
        }
        if(null == code){
            result.setMessage("Mã xác minh điện thoại đã hết hiệu lực, vui lòng lấy lại");
            result.setSuccess(false);
            return result;
        }
		if (!smscode.equals(code.toString())) {
			result.setMessage("Mã xác minh điện thoại sai");
			result.setSuccess(false);
			return result;
		}

        String realname = jsonObject.getString("realname");
        if(oConvertUtils.isEmpty(realname)){
            realname = username;
        }
        
		try {
			user.setCreateTime(new Date());// Đặt thời gian tạo
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
            user.setLastPwdUpdateTime(new Date());
			sysUserService.addUserWithRole(user,"");//Vai trò tạm thời mặc định test
			result.success("Đăng ký thành công");
		} catch (Exception e) {
			result.error500("Đăng ký thất bại");
		}
		return result;
	}

//	/**
//	 * Truy vấn thông tin người dùng theo tên người dùng hoặc số điện thoại
//	 * @param
//	 * @return
//	 */
//	@GetMapping("/querySysUser")
//	public Result<Map<String, Object>> querySysUser(SysUser sysUser) {
//		String phone = sysUser.getPhone();
//		String username = sysUser.getUsername();
//		Result<Map<String, Object>> result = new Result<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (oConvertUtils.isNotEmpty(phone)) {
//			SysUser user = sysUserService.getUserByPhone(phone);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		if (oConvertUtils.isNotEmpty(username)) {
//			SysUser user = sysUserService.getUserByName(username);
//			if(user!=null) {
//				map.put("username",user.getUsername());
//				map.put("phone",user.getPhone());
//				result.setSuccess(true);
//				result.setResult(map);
//				return result;
//			}
//		}
//		result.setSuccess(false);
//		result.setMessage("Kiểm tra thất bại");
//		return result;
//	}

	/**
	 * Xác minh số điện thoại người dùng
	 */
	@PostMapping("/phoneVerification")
	public Result<Map<String,String>> phoneVerification(@RequestBody JSONObject jsonObject) {
		Result<Map<String,String>> result = new Result<Map<String,String>>();
		String phone = jsonObject.getString("phone");
		String smscode = jsonObject.getString("smscode");
        // Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
		Object code = redisUtil.get(redisKey);
        // Giải thích logic mã: 【issues/8567】Nghiêm trọng: sửa mật khẩu tồn tại vấn đề vượt quyền theo chiều ngang.---
        if (null == code) {
            result.setMessage("Mã xác minh SMS đã hết hạn!");
            result.setSuccess(false);
            return result;
        }
        String smsCode = "";
        if (code.toString().contains("code")) {
            smsCode = JSONObject.parseObject(code.toString()).getString("code");
        } else {
            smsCode = code.toString();
        }
		if (!smscode.equals(smsCode)) {
			result.setMessage("Mã xác minh điện thoại sai");
			result.setSuccess(false);
			return result;
		}
		//Đặt thời gian hiệu lực
		redisUtil.set(redisKey, code,600);

		//Thêm truy vấn tên người dùng
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getPhone,phone);
        SysUser user = sysUserService.getOne(query);
        Map<String,String> map = new HashMap(5);
        map.put("smscode",smscode);
        if(null == user){
            //Frontend phán đoán người dùng có tồn tại hay không dựa trên văn bản, không được sửa
            result.setMessage("Thông tin người dùng không tồn tại");
            result.setSuccess(false);
            return result;
        }
        map.put("username",user.getUsername());
        result.setResult(map);
		result.setSuccess(true);
		return result;
	}
	
	/**
	 * Người dùng đổi mật khẩu
	 */
	@GetMapping("/passwordChange")
	public Result<SysUser> passwordChange(@RequestParam(name="username")String username,
										  @RequestParam(name="password")String password,
			                              @RequestParam(name="smscode")String smscode,
			                              @RequestParam(name="phone") String phone) {
        Result<SysUser> result = new Result<SysUser>();
        if(oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(password) || oConvertUtils.isEmpty(smscode)  || oConvertUtils.isEmpty(phone) ) {
            result.setMessage("Đặt lại mật khẩu thất bại!");
            result.setSuccess(false);
            return result;
        }

        SysUser sysUser=new SysUser();
        // Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
        Object object= redisUtil.get(redisKey);
        if(null==object) {
        	result.setMessage("Mã xác minh SMS đã hết hạn!");
            result.setSuccess(false);
            return result;
        }

        // Giải thích logic mã: 【issues/8567】Nghiêm trọng: sửa mật khẩu tồn tại vấn đề vượt quyền theo chiều ngang.---
        String redisUsername = "";
        if(object.toString().contains("code")){
            JSONObject jsonObject = JSONObject.parseObject(object.toString());
            object = jsonObject.getString("code");
            redisUsername = jsonObject.getString("username");
        }
        //Xác minh có phải của người dùng hiện tại hay không
        if(oConvertUtils.isNotEmpty(redisUsername) && !username.equals(redisUsername)){
            result.setMessage("Mã xác minh này không phải của người dùng hiện tại!");
            result.setSuccess(false);
            return result;
        }
        
        if(!smscode.equals(object.toString())) {
            //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-wv6f-p5mv-2g6f] Mã xác minh sai đủ 5 lần sẽ bị vô hiệu, tránh việc brute-force liệt kê mã xác minh để đặt lại mật khẩu của người khác-----------
            String errKey = redisKey + ":err";
            long errCount = redisUtil.incr(errKey, 1);
            if (errCount == 1) {
                redisUtil.expire(errKey, 600);
            }
            if (errCount >= 5) {
                redisUtil.del(redisKey, errKey);
                result.setMessage("Số lần nhập sai mã xác thực SMS quá nhiều, vui lòng lấy lại!");
            } else {
                result.setMessage("Mã xác minh SMS không khớp!");
            }
            //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-wv6f-p5mv-2g6f] Mã xác minh sai đủ 5 lần sẽ bị vô hiệu, tránh việc brute-force liệt kê mã xác minh để đặt lại mật khẩu của người khác-----------
            result.setSuccess(false);
            return result;
        }
        sysUser = this.sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername,username).eq(SysUser::getPhone,phone));
        if (sysUser == null) {
            result.setMessage("Người dùng hiện tại và số điện thoại đã liên kết không khớp, không thể thay đổi mật khẩu!");
            result.setSuccess(false);
            return result;
        } else {
            String salt = oConvertUtils.randomGen(8);
            sysUser.setSalt(salt);
            String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
            sysUser.setPassword(passwordEncode);
            sysUser.setLastPwdUpdateTime(new Date());
            this.sysUserService.updateById(sysUser);
            // Giải thích logic mã: [VUEN-234]Đặt lại mật khẩu thêm nhật ký nhạy cảm------------
            baseCommonService.addLog("Đặt lại "+username+" mật khẩu, người thực hiện: " +sysUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
            result.setSuccess(true);
            result.setMessage("Đặt lại mật khẩu thành công!");
            //Sau khi sửa mật khẩu xong, xóa redis
            redisUtil.removeAll(redisKey);
            return result;
        }
    }
	

	/**
	 * Lấy một phần thông tin người dùng theo TOKEN (dữ liệu trả về là dữ liệu để trình thiết kế form sử dụng)
	 * 
	 * @return
	 */
	@GetMapping("/getUserSectionInfoByToken")
	public Result<?> getUserSectionInfoByToken(HttpServletRequest request, @RequestParam(name = "token", required = false) String token) {
		try {
			String username = null;
			// Nếu không truyền token thì lấy token từ header và lấy thông tin người dùng
			if (oConvertUtils.isEmpty(token)) {
				 username = JwtUtil.getUserNameByToken(request);
			} else {
				 username = JwtUtil.getUsername(token);				
			}

			log.debug(" ------ Lấy một phần thông tin người dùng qua token, người dùng hiện tại: " + username);

			// Truy vấn thông tin người dùng theo tên người dùng
			SysUser sysUser = sysUserService.getUserByName(username);
			//update-begin---author:zhangdaihao ---date:2026-04-15  for：【issue/9518] Kiểm tra chữ ký của token truyền vào từ bên ngoài, ngăn chặn vượt quyền-----------
			if (oConvertUtils.isNotEmpty(token)) {
				if (sysUser == null || !JwtUtil.verify(token, username, sysUser.getPassword())) {
					return Result.error(401, "token Kiểm tra thất bại");
				}
			}
			//update-end---author:zhangdaihao ---date:2026-04-15  for：【issue/9518] Kiểm tra chữ ký của token truyền vào từ bên ngoài, ngăn chặn vượt quyền-----------
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("sysUserId", sysUser.getId());
			map.put("sysUserCode", sysUser.getUsername()); // Tài khoản đăng nhập của người dùng đang đăng nhập
			map.put("sysUserName", sysUser.getRealname()); // Tên thật của người dùng đang đăng nhập
			map.put("sysOrgCode", sysUser.getOrgCode()); // Mã phòng ban của người dùng đang đăng nhập

            // 【QQYUN-12930】Đặt tên phòng ban
            if (oConvertUtils.isNotEmpty(sysUser.getOrgCode())) {
                SysDepart sysDepart = sysDepartService.lambdaQuery().select(SysDepart::getDepartName).eq(SysDepart::getOrgCode, sysUser.getOrgCode()).one();
                if (sysDepart != null) {
                    map.put("sysOrgName", sysDepart.getDepartName()); // Tên phòng ban của người dùng đang đăng nhập
                }
            }

			log.debug(" ------ Lấy một phần thông tin người dùng qua token, thông tin người dùng đã lấy được: " + map);

			return Result.ok(map);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(500, "Tìm kiếm thất bại:" + e.getMessage());
		}
	}
	
	/**
	 * [API phía APP] Lấy danh sách người dùng, khớp mờ theo tên người dùng và họ tên thật
	 * @param keyword
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	@GetMapping("/appUserList")
	public Result<?> appUserList(@RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "username", required = false) String username,
			@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
			@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "syncFlow", required = false) String syncFlow) {
		try {
			//TODO Xét về hiệu quả truy vấn, đừng dùng truy vấn phân trang page được đóng gói sẵn của mp, nên tự viết câu lệnh phân trang
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>();
			if(oConvertUtils.isNotEmpty(syncFlow)){
                query.eq(SysUser::getActivitiSync, CommonConstant.ACT_SYNC_1);
            }
			query.eq(SysUser::getDelFlag,CommonConstant.DEL_FLAG_0);
			if(oConvertUtils.isNotEmpty(username)){
			    if(username.contains(",")){
                    query.in(SysUser::getUsername,username.split(","));
                }else{
                    query.eq(SysUser::getUsername,username);
                }
            }else{
                query.and(i -> i.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname, keyword));
            }
			Page<SysUser> page = new Page<>(pageNo, pageSize);
			IPage<SysUser> res = this.sysUserService.page(page, query);
			return Result.ok(res);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Result.error(500, "Tìm kiếm thất bại:" + e.getMessage());
		}
		
	}

    /**
     * Lấy danh sách người dùng đã bị xóa logic, không phân trang
     *
     * @return logicDeletedUserList
     */
    @GetMapping("/recycleBin")
    public Result getRecycleBin() {
        List<SysUser> logicDeletedUserList = sysUserService.queryLogicDeleted();
        if (logicDeletedUserList.size() > 0) {
            // Truy vấn hàng loạt phòng ban của người dùng
            // step.1 Trước tiên, lấy toàn bộ userIds
            List<String> userIds = logicDeletedUserList.stream().map(SysUser::getId).collect(Collectors.toList());
            // step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua userIds
            Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            logicDeletedUserList.forEach(item -> item.setOrgCode(useDepNames.get(item.getId())));
        }
        return Result.ok(logicDeletedUserList);
    }

    /**
     * Khôi phục người dùng đã bị xóa logic
     *
     * @param jsonObject
     * @return
     */
    @RequiresPermissions("system:user:putRecycleBin")
    @RequestMapping(value = "/putRecycleBin", method = RequestMethod.PUT)
    public Result putRecycleBin(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        String userIds = jsonObject.getString("userIds");
        if (StringUtils.isNotBlank(userIds)) {
            SysUser updateUser = new SysUser();
            updateUser.setUpdateBy(JwtUtil.getUserNameByToken(request));
            updateUser.setUpdateTime(new Date());
            sysUserService.revertLogicDeleted(Arrays.asList(userIds.split(",")), updateUser);
        }
        return Result.ok("Khôi phục thành công");
    }

    /**
     * Xóa hoàn toàn người dùng
     *
     * @param userIds ID người dùng bị xóa, nhiều id phân tách bằng dấu phẩy nửa chiều rộng
     * @return
     */
    @RequiresPermissions("system:user:deleteRecycleBin")
    @RequestMapping(value = "/deleteRecycleBin", method = RequestMethod.DELETE)
    public Result deleteRecycleBin(@RequestParam("userIds") String userIds) {
        if (StringUtils.isNotBlank(userIds)) {
            sysUserService.removeLogicDeleted(Arrays.asList(userIds.split(",")));
        }
        return Result.ok("Xóa thành công");
    }


    /**
     * Mobile sửa thông tin người dùng
     * @param jsonObject
     * @return
     */
    @RequiresPermissions("system:user:app:edit")
    @RequestMapping(value = "/appEdit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<SysUser> appEdit(HttpServletRequest request,@RequestBody JSONObject jsonObject) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            String username = JwtUtil.getUserNameByToken(request);
            SysUser sysUser = sysUserService.getUserByName(username);
            baseCommonService.addLog("Chỉnh sửa người dùng trên thiết bị di động, id: " +jsonObject.getString("id") ,CommonConstant.LOG_TYPE_2, 2);
            String realname=jsonObject.getString("realname");
            String avatar=jsonObject.getString("avatar");
            String sex=jsonObject.getString("sex");
            String phone=jsonObject.getString("phone");
            String email=jsonObject.getString("email");
            Date birthday=jsonObject.getDate("birthday");
            SysUser userPhone = sysUserService.getUserByPhone(phone);
            if(sysUser==null) {
                result.error500("Không tìm thấy người dùng tương ứng!");
            }else {
                if(userPhone!=null){
                    String userPhonename = userPhone.getUsername();
                    if(!userPhonename.equals(username)){
                        result.error500("Số điện thoại đã tồn tại!");
                        return result;
                    }
                }
                if(StringUtils.isNotBlank(realname)){
                    sysUser.setRealname(realname);
                }
                if(StringUtils.isNotBlank(avatar)){
                    sysUser.setAvatar(avatar);
                }
                if(StringUtils.isNotBlank(sex)){
                    sysUser.setSex(Integer.parseInt(sex));
                }
                if(StringUtils.isNotBlank(phone)){
                    sysUser.setPhone(phone);
                }
                if(StringUtils.isNotBlank(email)){
                    // Giải thích logic mã: [VUEN-1528]Email trang web chính thức của Jimu bị trùng lặp, cần nhắc chính xác------------
                    LambdaQueryWrapper<SysUser> emailQuery = new LambdaQueryWrapper<>();
                    emailQuery.eq(SysUser::getEmail,email);
                    long count = sysUserService.count(emailQuery);
                    if (!email.equals(sysUser.getEmail()) && count!=0) {
                        result.error500("Lưu thất bại, email đã tồn tại!");
                        return result;
                    }
                    sysUser.setEmail(email);
                }
                if(null != birthday){
                    sysUser.setBirthday(birthday);
                }
                sysUser.setUpdateTime(new Date());
                sysUserService.updateById(sysUser);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Lưu thất bại!");
        }
        return result;
    }
    /**
     * Mobile lưu thông tin thiết bị
     * @param clientId
     * @return
     */
    @RequestMapping(value = "/saveClientId", method = RequestMethod.GET)
    public Result<SysUser> saveClientId(HttpServletRequest request,@RequestParam(value = "clientId",required = false)String clientId) {
        Result<SysUser> result = new Result<SysUser>();
        try {
            String username = JwtUtil.getUserNameByToken(request);
            SysUser sysUser = sysUserService.getUserByName(username);
            if(sysUser==null) {
                result.error500("Không tìm thấy người dùng tương ứng!");
            }else {
                sysUserService.updateClientId(clientId,sysUser.getId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại!");
        }
        return result;
    }
    /**
     * Lấy thông tin người dùng và thông tin nhân viên phòng ban theo userid
     *
     * @return Result
     */
    @GetMapping("/queryChildrenByUsername")
    public Result queryChildrenByUsername(@RequestParam("userId") String userId) {
        //Lấy thông tin người dùng
        Map<String,Object> map=new HashMap(5);
        SysUser sysUser = sysUserService.getById(userId);
        String username = sysUser.getUsername();
        Integer identity = sysUser.getUserIdentity();
        map.put("sysUser",sysUser);
        if(identity!=null && identity==2){
            //Lấy thông tin người dùng phòng ban
            String departIds = sysUser.getDepartIds();
            if(StringUtils.isNotBlank(departIds)){
                List<String> departIdList = Arrays.asList(departIds.split(","));
                List<SysUser> childrenUser = sysUserService.queryByDepIds(departIdList,username);
                map.put("children",childrenUser);
            }
        }
        return Result.ok(map);
    }
    /**
     * Truy vấn thông tin người dùng phòng ban trên thiết bị di động
     * @param departId
     * @return
     */
    @GetMapping("/appQueryByDepartId")
    public Result<List<SysUser>> appQueryByDepartId(@RequestParam(name="departId", required = false) String departId) {
        Result<List<SysUser>> result = new Result<List<SysUser>>();
        //update-begin---author:scott ---date:20261014  for: trong chế độ nhiều tenant, phòng ban phải thuộc tenant hiện tại (sys_depart đã thực hiện cách ly thuê bao, không tra được tức là không thuộc thuê bao này), tránh đọc xuyên thuê bao thông tin người dùng-----------
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && (oConvertUtils.isEmpty(departId) || sysDepartService.getById(departId) == null)) {
            result.setResult(new ArrayList<>());
            return result;
        }
        //update-end---author:scott ---date:20261014  for: trong chế độ nhiều tenant, phòng ban phải thuộc tenant hiện tại (sys_depart đã thực hiện cách ly thuê bao, không tra được tức là không thuộc thuê bao này), tránh đọc xuyên thuê bao thông tin người dùng-----------
        List<String> list=new ArrayList<String> ();
        list.add(departId);
        List<SysUser> childrenUser = sysUserService.queryByDepIds(list,null);
        result.setResult(childrenUser);
        return result;
    }
    /**
     * Mobile truy vấn thông tin người dùng(Truy vấn mờ theo tên người dùng)
     * @param keyword
     * @return
     */
    @GetMapping("/appQueryUser")
    public Result<List<SysUser>> appQueryUser(@RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "username", required = false) String username,
                                              @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                              @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest request) {
        Result<List<SysUser>> result = new Result<List<SysUser>>();
        LambdaQueryWrapper<SysUser> queryWrapper =new LambdaQueryWrapper<SysUser>();
        // Tài khoản tạm thời đăng nhập mô phỏng bên ngoài, không hiển thị trong danh sách
        queryWrapper.ne(SysUser::getUsername,"_reserve_user_external");
        //Thêm tham số truyền username
        if(oConvertUtils.isNotEmpty(username)){
            if(username.contains(",")){
                queryWrapper.in(SysUser::getUsername,username.split(","));
            }else{
                queryWrapper.eq(SysUser::getUsername,username);
            }
        }else if(StringUtils.isNotBlank(keyword)){
            queryWrapper.and(i -> i.like(SysUser::getUsername, keyword).or().like(SysUser::getRealname, keyword));
        }
        //------------------------------------------------------------------------------------------------
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            String tenantId = oConvertUtils.getString(TokenUtils.getTenantIdByRequest(request),"-1");
            // Giải thích logic mã: [QQYUN-3371]Cải tạo logic Tenant, đổi sang bảng quan hệ------------
            List<String> userIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (oConvertUtils.listIsNotEmpty(userIds)) {
                queryWrapper.in(SysUser::getId, userIds);
            }
            //update-begin---author:scott ---date:20261014  for: Khi không tra được người dùng trong thuê bao thì không nới lỏng bộ lọc nữa, tránh đọc xuyên thuê bao thông tin người dùng-----------
            else {
                queryWrapper.eq(SysUser::getId, "Không tìm thấy người dùng nào qua tenant");
            }
            //update-end---author:scott ---date:20261014  for: Khi không tra được người dùng trong thuê bao thì không nới lỏng bộ lọc nữa, tránh đọc xuyên thuê bao thông tin người dùng-----------
        }
        //------------------------------------------------------------------------------------------------
        Page<SysUser> page = new Page<>(pageNo, pageSize);

        // Giải thích logic mã: JHHB-812 [Phía di động] Nhân sự hiển thị theo thứ tự sắp xếp; khi chọn nhân sự, danh bạ, v.v. sắp xếp tăng dần 123
        queryWrapper.orderByAsc(SysUser::getSort);
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> pageList = this.sysUserService.page(page, queryWrapper);
        //Truy vấn hàng loạt phòng ban của người dùng
        //step.1 Trước tiên, lấy toàn bộ useids
        //step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua useids
        List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
        if(userIds!=null && userIds.size()>0){
            Map<String,String>  useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            pageList.getRecords().forEach(item->{
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
            });
        }
        result.setResult(pageList.getRecords());
        return result;
    }

    /**
     * Sửa số điện thoại theo tên người dùng[Phương thức này chưa được sử dụng]
     * @param json
     * @return
     */
    @RequestMapping(value = "/updateMobile", method = RequestMethod.PUT)
    public Result<?> changMobile(@RequestBody JSONObject json,HttpServletRequest request) {
        String smscode = json.getString("smscode");
        String phone = json.getString("phone");
        Result<SysUser> result = new Result<SysUser>();
        //Lấy tên đăng nhập
        String username = JwtUtil.getUserNameByToken(request);
        if(oConvertUtils.isEmpty(username) || oConvertUtils.isEmpty(smscode) || oConvertUtils.isEmpty(phone)) {
            result.setMessage("Sửa số điện thoại thất bại!");
            result.setSuccess(false);
            return result;
        }
        // Giải thích logic mã: VUEN-2245 【Lỗ hổng】Phát hiện lỗ hổng mới đang chờ xử lý 20220906
        String redisKey = CommonConstant.PHONE_REDIS_KEY_PRE+phone;
        Object object= redisUtil.get(redisKey);
        if(null==object) {
            result.setMessage("Mã xác minh SMS đã hết hạn!");
            result.setSuccess(false);
            return result;
        }
        if(!smscode.equals(object.toString())) {
            result.setMessage("Mã xác minh SMS không khớp!");
            result.setSuccess(false);
            return result;
        }
        SysUser user = sysUserService.getUserByName(username);
        if(user==null) {
            return Result.error("Người dùng không tồn tại!");
        }
        user.setPhone(phone);
        sysUserService.updateById(user);
        return Result.ok("Đặt số điện thoại thành công!");
    }


    /**
     * Truy vấn in theo giá trị thuộc tính trong đối tượng; thuộc tính có thể thay đổi, component người dùng dùng tới
     * @param sysUser
     * @return
     */
    @GetMapping("/getMultiUser")
    public List<SysUser> getMultiUser(SysUser sysUser){
        QueryWrapper<SysUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUser, null);
        // Giải thích logic mã: [JTC-297]Người dùng đã bị đóng băng vẫn có thể được đặt làm người đại diện------------
        queryWrapper.eq("status",Integer.parseInt(CommonConstant.STATUS_1));
        List<SysUser> ls = this.sysUserService.list(queryWrapper);
        for(SysUser user: ls){
            user.setPassword(null);
            user.setSalt(null);
        }
        return ls;
    }
    
    /**
     * Chat — chuyên dùng để tạo component trò chuyện, truy vấn phân trang theo tài khoản người dùng, tên người dùng, id phòng ban
     * @param departId Phòng ban id
     * @param keyword Giá trị tìm kiếm
     * @return
     */
    @GetMapping(value = "/getUserInformation")
    public Result<IPage<SysUser>> getUserInformation(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name="keyword",required=false) String keyword) {
        //------------------------------------------------------------------------------------------------
        Integer tenantId = null;
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            tenantId = oConvertUtils.getInt(TenantContext.getTenant(),0);
        }
        //------------------------------------------------------------------------------------------------
        IPage<SysUser> pageList = sysUserDepartService.getUserInformation(tenantId,departId, keyword, pageSize, pageNo);
        return Result.OK(pageList);
    }

    /**
     * Component chọn người dùng quy trình bản đơn giản
     * @param departId Phòng ban id
     * @param roleId Vai trò id
     * @param keyword Giá trị tìm kiếm
     * @return
     */
    @GetMapping(value = "/selectUserList")
    public Result<IPage<SysUser>> selectUserList(
            @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
            @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
            @RequestParam(name = "departId", required = false) String departId,
            @RequestParam(name = "roleId", required = false) String roleId,
            @RequestParam(name="keyword",required=false) String keyword,
            @RequestParam(name="excludeUserIdList",required = false) String excludeUserIdList,
            @RequestParam(name="includeUsernameList",required = false) String includeUsernameList,
            HttpServletRequest req) {
        //------------------------------------------------------------------------------------------------
        Integer tenantId = null;
        //Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            String tenantStr = TenantContext.getTenant();
            tenantId = oConvertUtils.getInteger(tenantStr, oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req), -1));
            log.info("---------Trong JianLiu, giao diện chọn người dùng, lọc theo tenant, tenant ID={}", tenantId);
        }
        //------------------------------------------------------------------------------------------------
        IPage<SysUser> pageList = sysUserDepartService.getUserInformation(tenantId, departId,roleId, keyword, pageSize, pageNo,excludeUserIdList,includeUsernameList);
        return Result.OK(pageList);
    }

    /**
     * Lấy danh sách người dùng đã bị xóa logic, không phân trang [Interface chuyên dụng cho ứng dụng low-code]
     *
     * @return List<SysUser>
     */
    @GetMapping("/getQuitList")
    public Result<List<SysUser>> getQuitList(HttpServletRequest req) {
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req),0);
        List<SysUser> quitList = sysUserService.getQuitList(tenantId);
        if (null != quitList && quitList.size() > 0) {
            // Truy vấn hàng loạt phòng ban của người dùng
            // step.1 Trước tiên, lấy toàn bộ userIds
            List<String> userIds = quitList.stream().map(SysUser::getId).collect(Collectors.toList());
            // step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua userIds
            Map<String, String> useDepNames = sysUserService.getDepNamesByUserIds(userIds);
            quitList.forEach(item -> item.setOrgCode(useDepNames.get(item.getId())));
        }
        return Result.ok(quitList);
    }

    /**
     * Cập nhật trạng thái xóa và trạng thái nghỉ việc [Giao diện chuyên dùng cho ứng dụng low-code]
     * @param jsonObject
     * @return Result<String>
     */
    @PutMapping("/putCancelQuit")
    public Result<String> putCancelQuit(@RequestBody JSONObject jsonObject, HttpServletRequest request){
        String userIds = jsonObject.getString("userIds");
        String usernames = jsonObject.getString("usernames");
        Integer tenantId = oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(request),0);
        //Đổi trạng thái thành chưa xóa
        if (StringUtils.isNotBlank(userIds)) {
            userTenantService.putCancelQuit(Arrays.asList(userIds.split(SymbolConstant.COMMA)),tenantId);
        }
        return Result.ok("Hủy nghỉ việc thành công");
    }

    /**
     * Lấy thông tin người dùng(vue3 Dành riêng cho cài đặt người dùng)【Giao diện chuyên dụng cho ứng dụng low-code】
     * @return
     */
    @GetMapping("/login/setting/getUserData")
    public Result<SysUser> getUserData(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        SysUser user = sysUserService.getUserByName(username);
        if(user==null) {
            return Result.error("Không tìm thấy dữ liệu người dùng này");
        }

        //Lấy ID người dùng thông qua dữ liệu chức vụ
        List<SysPosition> sysPositionList = sysPositionService.getPositionList(user.getId());
        if(null != sysPositionList && sysPositionList.size()>0){
            StringBuilder nameBuilder = new StringBuilder();
            StringBuilder idBuilder = new StringBuilder();
            String verticalBar = " | ";
            for (SysPosition sysPosition:sysPositionList){
                nameBuilder.append(sysPosition.getName()).append(verticalBar);
                idBuilder.append(sysPosition.getId()).append(SymbolConstant.COMMA);
            }
            String names = nameBuilder.toString();
            if(oConvertUtils.isNotEmpty(names)){
                names = names.substring(0,names.lastIndexOf(verticalBar));
                user.setPostText(names);
            }
            //Ghép vị trí id
            String ids = idBuilder.toString();
            if(oConvertUtils.isNotEmpty(ids)){
                ids = ids.substring(0,ids.lastIndexOf(SymbolConstant.COMMA));
                user.setPost(ids);
            }
        }
        return Result.ok(user);
    }

    /**
     * Chỉnh sửa người dùng(vue3 Dành riêng cho cài đặt người dùng)【Giao diện chuyên dụng cho ứng dụng low-code】
     * @param sysUser
     * @return
     */
    @PostMapping("/login/setting/userEdit")
    @RequiresPermissions("system:user:setting:edit")
    public Result<String> userEdit(@RequestBody SysUser sysUser, HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        SysUser user = sysUserService.getById(sysUser.getId());
        if(user==null) {
           return Result.error("Không tìm thấy dữ liệu người dùng này");
        }
        if(!username.equals(user.getUsername())){
            return Result.error("Chỉ được sửa dữ liệu của chính mình");
        }
        //update-begin---author:zhangdaihao ---date:2026-05-06  for：【issue/9596] Sửa lỗ hổng vượt quyền userEdit, chỉ cho phép sửa các trường hồ sơ cá nhân, ngăn việc thông qua userIdentity/departIds và các trường nhạy cảm khác bị vượt quyền-----------
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setRealname(sysUser.getRealname());
        updateUser.setAvatar(sysUser.getAvatar());
        updateUser.setBirthday(sysUser.getBirthday());
        updateUser.setSex(sysUser.getSex());
        updateUser.setEmail(sysUser.getEmail());
        updateUser.setPhone(sysUser.getPhone());
        updateUser.setTelephone(sysUser.getTelephone());
        updateUser.setSign(sysUser.getSign());
        updateUser.setSignEnable(sysUser.getSignEnable());
        updateUser.setIzHideContact(sysUser.getIzHideContact());
        sysUserService.updateById(updateUser);
        //update-end---author:zhangdaihao ---date:2026-05-06  for：【issue/9596] Sửa lỗ hổng vượt quyền userEdit, chỉ cho phép sửa các trường hồ sơ cá nhân, ngăn việc thông qua userIdentity/departIds và các trường nhạy cảm khác bị vượt quyền-----------
        return Result.ok("Cập nhật thông tin cá nhân thành công");
    }

    /**
     * Chỉnh sửa hàng loạt 【low-app】
     * @param jsonObject
     * @return
     */
    @PutMapping("/batchEditUsers")
    public Result<SysUser> batchEditUsers(@RequestBody JSONObject jsonObject) {
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-pjwf-2w2c-xc23/GHSA-4w9p-7gw3-q7v4] Sửa hàng loạt phòng ban/chức vụ của người dùng phải là admin hoặc quản trị viên tenant hiện tại, và quản trị viên tenant chỉ được sửa người dùng của tenant này-----------
        // Ở chế độ SaaS QiaoQiaoYun, mọi người dùng mặc định đều có quyền vai trò test, không thể dùng mã quyền để cho qua, chỉ cho qua admin
        if (!SysRoleEnum.hasAnyRole(SysRoleEnum.ADMIN, SysRoleEnum.TENANT_ADMIN)) {
            String tenantId = TenantContext.getTenant();
            if (oConvertUtils.isEmpty(tenantId)) {
                return Result.error("Không có quyền thực hiện thao tác này");
            }
            // Ném ngoại lệ khi không phải quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của thuê bao hiện tại
            sysTenantPackService.izHaveManageUserAuth(tenantId);
            List<String> idList = JSONArray.parseArray(jsonObject.getString("userIds"), String.class);
            List<String> tenantUserIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (idList == null || !tenantUserIds.containsAll(idList)) {
                return Result.error("Chỉ được chỉnh sửa người dùng thuộc tenant hiện tại");
            }
        }
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-pjwf-2w2c-xc23/GHSA-4w9p-7gw3-q7v4] Sửa hàng loạt phòng ban/chức vụ của người dùng phải là admin hoặc quản trị viên tenant hiện tại, và quản trị viên tenant chỉ được sửa người dùng của tenant này-----------
        Result<SysUser> result = new Result<SysUser>();
        try {
            sysUserService.batchEditUsers(jsonObject);
            result.setSuccess(true);
            result.setMessage("Thao tác thành công!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.error500("Thao tác thất bại");
        }
        return result;
    }

    /**
     * Tìm kiếm phòng ban và người dùng theo từ khóa 【low-app】
     * @param keyword
     * @return
     */
    @GetMapping("/searchByKeyword")
    public Result<DepartAndUserInfo> searchByKeyword(@RequestParam(name="keyword",required=false) String keyword) {
        DepartAndUserInfo info = sysUserService.searchByKeyword(keyword);
        return Result.ok(info);
    }

    /**
     * Lấy thông tin liên quan đến phòng ban trước khi chỉnh sửa phòng ban [low-app】
     * @param id
     * @return
     */
    @GetMapping("/getUpdateDepartInfo")
    public Result<UpdateDepartInfo> getUpdateDepartInfo(@RequestParam(name="id",required=false) String id) {
        UpdateDepartInfo info = sysUserService.getUpdateDepartInfo(id);
        return Result.ok(info);
    }

    /**
     * Chỉnh sửa phòng ban 【low-app】
     * @param updateDepartInfo
     * @return
     */
    @PutMapping("/doUpdateDepartInfo")
    public Result<?> doUpdateDepartInfo(@RequestBody UpdateDepartInfo updateDepartInfo) {
        sysUserService.doUpdateDepartInfo(updateDepartInfo);
        return Result.ok();
    }

    /**
     * Đặt người phụ trách / Hủy người phụ trách
     * @param json
     * @return
     */
    @PutMapping("/changeDepartChargePerson")
    public Result<?> changeDepartChargePerson(@RequestBody JSONObject json) {
        //update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-9xxr-rh33-qr3f] Đặt người phụ trách phòng ban phải là admin hoặc quản trị viên tenant hiện tại, và quản trị viên tenant chỉ được đặt phòng ban và người dùng của tenant này-----------
        // Ở chế độ SaaS QiaoQiaoYun, tất cả người dùng mặc định có quyền vai trò test(bao gồm system:depart:edit), không thể dùng mã quyền hạn để cho qua, chỉ cho qua admin
        if (!SysRoleEnum.hasAnyRole(SysRoleEnum.ADMIN, SysRoleEnum.TENANT_ADMIN)) {
            String tenantId = TenantContext.getTenant();
            if (oConvertUtils.isEmpty(tenantId)) {
                return Result.error("Không có quyền thực hiện thao tác này");
            }
            // Ném ngoại lệ khi không phải quản trị viên tài khoản tổ chức hoặc siêu quản trị viên của thuê bao hiện tại
            sysTenantPackService.izHaveManageUserAuth(tenantId);
            SysDepart depart = sysDepartService.getById(json.getString("departId"));
            // Khi bật cách ly tenant của hệ thống, phòng ban phải thuộc tenant hiện tại (khi chưa bật, phòng ban không phân biệt tenant)
            boolean departInTenant = depart != null && (!MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL || tenantId.equals(String.valueOf(depart.getTenantId())));
            List<String> tenantUserIds = userTenantService.getUserIdsByTenantId(Integer.valueOf(tenantId));
            if (!departInTenant || !tenantUserIds.contains(json.getString("userId"))) {
                return Result.error("Chỉ được đặt người phụ trách phòng ban thuộc tenant hiện tại");
            }
        }
        //update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-9xxr-rh33-qr3f] Đặt người phụ trách phòng ban phải là admin hoặc quản trị viên tenant hiện tại, và quản trị viên tenant chỉ được đặt phòng ban và người dùng của tenant này-----------
        sysUserService.changeDepartChargePerson(json);
        return Result.ok();
    }

    /**
     * Sửa người dùng thuộc tenant [Giao diện chuyên dùng cho ứng dụng low-code]
     * @param sysUser
     * @param req
     * @return
     */
    @RequestMapping(value = "/editTenantUser", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<String> editTenantUser(@RequestBody SysUser sysUser,HttpServletRequest req){
        Result<String> result = new Result<>();
        String tenantId = TokenUtils.getTenantIdByRequest(req);
        if(oConvertUtils.isEmpty(tenantId)){
            return result.error500("Không có quyền sửa thông tin của người khác!");
        }
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId,Integer.valueOf(tenantId));
        query.eq(SysUserTenant::getUserId,sysUser.getId());
        SysUserTenant one = userTenantService.getOne(query);
        if(null == one){
            return result.error500("Người dùng không thuộc tenant hiện tại, không được phép sửa đổi!");
        }
        String departs = req.getParameter("selecteddeparts");
        sysUserService.editTenantUser(sysUser,tenantId,departs,null);
        return Result.ok("Chỉnh sửa thành công");
    }

    /**
     * Khi chuyển Tenant cần sửa loginTenantId
     * QQYUN-4491 [Ứng dụng] Một số vấn đề nhỏ 1. Tenant đã chọn khi đăng nhập lần trước, lần đăng nhập sau không ghi nhớ
     * @param sysUser
     * @return
     */
    @PutMapping("/changeLoginTenantId")
    public Result<?> changeLoginTenantId(@RequestBody SysUser sysUser){
        Result<String> result = new Result<>();
        Integer tenantId = sysUser.getLoginTenantId();
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String userId = loginUser.getId();
        
        // Phán đoán xem ID tenant được chỉ định có phải là tenant của người dùng đang đăng nhập hay không
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getTenantId, tenantId);
        query.eq(SysUserTenant::getUserId, userId);
        SysUserTenant one = userTenantService.getOne(query);
        if(null == one){
            return result.error500("Người dùng không thuộc tenant, không được phép chỉnh sửa!");
        }
        
        // Sửa loginTenantId
        LambdaQueryWrapper<SysUser> update = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getId, userId);
        SysUser updateUser = new SysUser();
        updateUser.setLoginTenantId(tenantId);
        sysUserService.update(updateUser, update);
        return Result.ok();
    } 

    /**
     * Xuất người dùng ứng dụng
     * @param request
     * @return
     */
    @RequestMapping(value = "/exportAppUser")
    public ModelAndView exportAppUser(HttpServletRequest request) {
        return sysUserService.exportAppUser(request);
    }
    
   /**
     * Nhập người dùng ứng dụng
     * @param request
     * @return
     */
    @RequestMapping(value = "/importAppUser", method = RequestMethod.POST)
    public Result<?> importAppUser(HttpServletRequest request, HttpServletResponse response)throws IOException {
        return sysUserService.importAppUser(request);
    }

    /**
     * Thay đổi số điện thoại (chỉ dùng cho cài đặt cá nhân của QiaoQiaoYun)
     *
     * @param json
     * @param request
     */
    @PutMapping("/changePhone")
    public Result<String> changePhone(@RequestBody JSONObject json, HttpServletRequest request){
        //Lấy tên đăng nhập
        String username = JwtUtil.getUserNameByToken(request);
        sysUserService.changePhone(json,username);
        return Result.ok("Sửa số điện thoại thành công!");
    }

    /**
     * Giao diện gửi mã xác minh SMS(Thay đổi số điện thoại)
     *
     * @param jsonObject
     * @return
     */
    @PostMapping(value = "/sendChangePhoneSms")
    public Result<String> sendChangePhoneSms(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        //Lấy tên đăng nhập
        String username = JwtUtil.getUserNameByToken(request);
        String ipAddress = IpUtils.getIpAddr(request);
        sysUserService.sendChangePhoneSms(jsonObject, username, ipAddress);
        return Result.ok("Gửi mã xác minh thành công!");
    }

    /**
     * Gửi mật khẩu xác minh số điện thoại để hủy tài khoản người dùng[Dành riêng cho QiaoQiaoYun]
     *
     * @param jsonObject
     * @return
     */
    @PostMapping(value = "/sendLogOffPhoneSms")
    public Result<String> sendLogOffPhoneSms(@RequestBody JSONObject jsonObject, HttpServletRequest request) {
        Result<String> result = new Result<>();
        //Lấy tên đăng nhập
        String username = JwtUtil.getUserNameByToken(request);
        String name = jsonObject.getString("username");
        if (oConvertUtils.isEmpty(name) || !name.equals(username)) {
            result.setSuccess(false);
            result.setMessage("Gửi mã xác minh thất bại, người dùng không khớp!");
            return result;
        }
        String ipAddress = IpUtils.getIpAddr(request);
        sysUserService.sendLogOffPhoneSms(jsonObject, username, ipAddress);
        result.setSuccess(true);
        result.setMessage("Gửi mã xác minh thành công!");
        return result;
    }

    /**
     * Chưa liên kết số điện thoại, đổi mật khẩu trực tiếp
     * @param oldPassword
     * @param password
     * @return
     */
    @PutMapping("/updatePasswordNotBindPhone")
    public Result<String> updatePasswordNotBindPhone(@RequestParam(value="oldPassword") String oldPassword,
                                                     @RequestParam(value="password") String password,
                                                     @RequestParam(value="username") String username){
        sysUserService.updatePasswordNotBindPhone(oldPassword, password, username);
        return Result.OK("Đổi mật khẩu thành công!");
    }

    /**
     * Chọn người dùng theo vị trí phòng ban [Chuyên dùng để chọn người dùng theo vị trí phòng ban]
     * @return
     */
    @GetMapping("/queryDepartPostUserPageList")
    public Result<IPage<SysUser>> queryDepartPostUserPageList( @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                        @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
                                                        @RequestParam(name = "departId", required = false) String departId,
                                                        @RequestParam(name="realname",required=false) String realname,
                                                        @RequestParam(name="username",required=false) String username,
                                                        @RequestParam(name="isMultiTranslate",required=false) String isMultiTranslate,
                                                        @RequestParam(name="id",required = false) String id){
        String[] arr = new String[]{departId, realname, username, id};
        SqlInjectionUtil.filterContent(arr, SymbolConstant.SINGLE_QUOTATION_MARK);
        IPage<SysUser> pageList = sysUserDepartService.queryDepartPostUserPageList(departId, username, realname, pageSize, pageNo,id,isMultiTranslate);
        return Result.OK(pageList);
    }

    /**
     * Lấy tiến độ tải lên file
     * 
     * @param fileKey
     * @param type
     * @return
     */
    @GetMapping("/getUploadFileProgress")
    public Result<Double> getUploadFileProgress(@RequestParam(name = "fileKey") String fileKey,
                                                @RequestParam("type") String type){
        Double progress = ImportSysUserCache.getImportSysUserMap(fileKey, type);
        if(progress == 100){
            ImportSysUserCache.removeImportLowAppMap(fileKey);
        }
        return Result.ok(progress);
    }

    /**
     * Kiểm tra xem người dùng đang đăng nhập có còn đang sử dụng mật khẩu khởi tạo mặc định của hệ thống hay không.
     * Mô tả giá trị trả về:
     *   yes_{URL Mật khẩu mặc định đã mã hóa} -> Mật khẩu hiện tại của người dùng là mật khẩu khởi tạo mặc định, frontend cần hiển thị thông báo bắt buộc đổi mật khẩu
     *   no -> Mật khẩu người dùng không phải mật khẩu mặc định, hoặc chưa bật công tắc kiểm tra mật khẩu mặc định
     */
    @GetMapping("/verifyIzDefaultPwd")
    public Result<String> verifyIzDefaultPwd() throws UnsupportedEncodingException {
        // Chưa cấu hình Firewall hoặc đã tắt công tắc kiểm tra mật khẩu mặc định (enableDefaultPwdCheck=false) thì trả về trực tiếp "no" có nghĩa là không cần nhắc nhở
        if (jeecgBaseConfig.getFirewall() == null || Boolean.FALSE.equals((jeecgBaseConfig.getFirewall().getEnableDefaultPwdCheck()))) {
            return Result.OK("no");
        }
        
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        SysUser user = sysUserService.getById(sysUser.getId());
        String passwordEncode = PasswordUtil.encrypt(user.getUsername(), PasswordConstant.DEFAULT_PASSWORD, user.getSalt());
        if(passwordEncode.equals(user.getPassword())){
            String encode = URLEncoder.encode(PasswordConstant.DEFAULT_PASSWORD, "UTF-8");
            return Result.OK("yes_" + encode);
        }
        return Result.OK("no");
    }
}
