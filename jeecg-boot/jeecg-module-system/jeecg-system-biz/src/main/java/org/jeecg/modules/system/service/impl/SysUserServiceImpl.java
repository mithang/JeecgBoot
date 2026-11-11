package org.jeecg.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.FillRuleConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.constant.PasswordConstant;
import org.jeecg.common.constant.enums.*;
import org.jeecg.common.desensitization.annotation.SensitiveEncode;
import org.jeecg.common.exception.JeecgBootBizTipException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.system.vo.SysUserCacheInfo;
import org.jeecg.common.util.*;
import org.jeecg.common.util.encryption.AesEncryptUtil;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.message.handle.impl.SystemSendMsgHandle;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.mapper.*;
import org.jeecg.modules.system.model.SysUserSysDepPostModel;
import org.jeecg.modules.system.model.SysUserSysDepartModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.util.ImportSysUserCache;
import org.jeecg.modules.system.vo.*;
import org.jeecg.modules.system.vo.lowapp.AppExportUserVo;
import org.jeecg.modules.system.vo.lowapp.DepartAndUserInfo;
import org.jeecg.modules.system.vo.lowapp.DepartInfo;
import org.jeecg.modules.system.vo.lowapp.UpdateDepartInfo;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Bảng người dùng Lớp triển khai Service
 * </p>
 *
 * @Author: scott
 * @Date: 2018-12-20
 */
@Service
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
	
	@Autowired
	private SysUserMapper userMapper;
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Autowired
	private SysUserRoleMapper sysUserRoleMapper;
	@Autowired
	private SysUserDepartMapper sysUserDepartMapper;
	@Autowired
	private SysDepartMapper sysDepartMapper;
	@Autowired
	private SysRoleMapper sysRoleMapper;
	@Autowired
	private SysDepartRoleUserMapper departRoleUserMapper;
	@Autowired
	private SysDepartRoleMapper sysDepartRoleMapper;
	@Resource
	private BaseCommonService baseCommonService;
	@Autowired
	private SysThirdAccountMapper sysThirdAccountMapper;
	@Autowired
	ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;
	@Autowired
	ThirdAppDingtalkServiceImpl dingtalkService;
	@Autowired
	ISysRoleIndexService sysRoleIndexService;
	@Autowired
	SysTenantMapper sysTenantMapper;
	@Autowired
    private SysUserTenantMapper relationMapper;
	@Autowired
    private SysUserTenantMapper userTenantMapper;
	@Autowired
	private SysUserPositionMapper sysUserPositionMapper;
	@Autowired
	private SysPositionMapper sysPositionMapper;
	@Autowired
	private SystemSendMsgHandle systemSendMsgHandle;
	
	@Autowired
	private ISysThirdAccountService sysThirdAccountService;
	@Autowired
	private RedisUtil redisUtil;
    
    @Autowired
    private SysTenantPackUserMapper packUserMapper;
    
    @Autowired
    private SysUserDepPostMapper depPostMapper;

	@Autowired
	private JeecgBaseConfig jeecgBaseConfig;

	/**
	 * Tài khoản quản trị viên
	 */
	public static final String[] ADMIN_ACCOUNT = new String[]{"admin"};

	@Override
	public Result<IPage<SysUser>> queryPageList(HttpServletRequest req, QueryWrapper<SysUser> queryWrapper, Integer pageSize, Integer pageNo) {
		Result<IPage<SysUser>> result = new Result<IPage<SysUser>>();
		//Phòng ban ID
		String departId = req.getParameter("departId");
		if (oConvertUtils.isNotEmpty(departId)) {
            //Giải thích logic mã:【JHHB-762] Quản lý người dùng cần hỗ trợ truy vấn người dùng theo cơ cấu tổ chức (hỗ trợ chọn nhiều)---
            //Tương thích nhiều phòng ban id
            LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<>();
            if(departId.contains(SymbolConstant.COMMA)) {
                query.in(SysUserDepart::getDepId, Arrays.asList(departId.split(SymbolConstant.COMMA)));
            } else {
                query.eq(SysUserDepart::getDepId, departId);
            }
            List<SysUserDepart> list = sysUserDepartMapper.selectList(query);
			List<String> userIds = list.stream().map(SysUserDepart::getUserId).collect(Collectors.toList());
			// Giải thích logic mã: [issues/I4XTYB]Khi truy vấn người dùng, nếu dưới id phòng ban chưa phân bổ người dùng thì interface báo lỗi------------
			if (oConvertUtils.listIsNotEmpty(userIds)) {
				queryWrapper.in("id", userIds);
			} else {
				return Result.OK();
			}
		}
		//Người dùng ID
		String code = req.getParameter("code");
		if (oConvertUtils.isNotEmpty(code)) {
			queryWrapper.in("id", Arrays.asList(code.split(",")));
			pageSize = code.split(",").length;
		}

		// Giải thích logic mã: JTC-372 [Vấn đề đóng băng người dùng] Khi ủy quyền online, component người dùng, lúc chọn người dùng đều có thể thấy người dùng đã bị đóng băng
		String status = req.getParameter("status");
		if (oConvertUtils.isNotEmpty(status)) {
			queryWrapper.eq("status", Integer.parseInt(status));
		}

		// Giải thích logic mã: 【QQYUN-8110】Danh bạ trực tuyến hỗ trợ thiết lập quyền hạn(Chỉ xem được hỗ trợ kỹ thuật được phân công)---
		String tenantId = TokenUtils.getTenantIdByRequest(req);
		String lowAppId = TokenUtils.getLowAppIdByRequest(req);
//		Object bean = ResourceUtil.getImplementationClass(DataEnhanceEnum.getClassPath(tenantId,lowAppId));
//		if(null != bean){
//			UserFilterEnhance userEnhanceService = (UserFilterEnhance) bean;
//			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//			List<String> userIds = userEnhanceService.getUserIds(sysUser.getId());
//			if(CollectionUtil.isNotEmpty(userIds)){
//				queryWrapper.in("id", userIds);
//			}
//		}
		
		//TODO Tài khoản tạm thời đăng nhập mô phỏng bên ngoài, không hiển thị trong danh sách
		queryWrapper.ne("username", "_reserve_user_external");
        // Giải thích logic mã: 【JHHB-765】Cần có thể thiết lập thứ tự sắp xếp---
        // Kiểm tra từng trường xem đã được QueryGenerator thêm vào hay chưa, tránh ORDER BY xuất hiện cột trùng lặp (SQL Server báo lỗi 169)
        String column = req.getParameter("column");
        if (!"sort".equals(column)) {
            queryWrapper.orderByAsc("sort");
        }
        if (!"create_time".equals(column) && !"createTime".equals(column)) {
            queryWrapper.orderByDesc("create_time");
        }
		Page<SysUser> page = new Page<SysUser>(pageNo, pageSize);
		IPage<SysUser> pageList = this.page(page, queryWrapper);

		//Truy vấn hàng loạt phòng ban của người dùng
		//step.1 Trước tiên, lấy toàn bộ useids
		//step.2 Truy vấn một lần tên phòng ban mà người dùng thuộc về thông qua useids
		List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
		if (userIds != null && userIds.size() > 0) {
			Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);

			//update-begin---author:scott ---date:20260701  for: [Tối ưu hiệu năng] giao diện listAll N+1 Cải tạo truy vấn, tải trước hàng loạt dữ liệu liên quan để loại bỏ N+1-----------
			// Truy vấn hàng loạt IDs phòng ban trực thuộc (loại bỏ N+1: N lần queryDepartsByUserId → 1 lần truy vấn hàng loạt)
			List<SysUserDepartIdVo> departIdVos = sysDepartMapper.queryDepartIdVosByUserIds(userIds);
			Map<String, List<String>> userDepartIdsMap = new HashMap<>();
			for (SysUserDepartIdVo vo : departIdVos) {
				if (vo.getUserId() != null && vo.getDepartId() != null) {
					userDepartIdsMap.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(vo.getDepartId());
				}
			}

			// Truy vấn hàng loạt các Tenant ID (loại bỏ N+1: N lần getTenantIdsByUserId → 1 lần truy vấn hàng loạt)
			List<SysUserTenantIdVo> tenantIdVos = userTenantMapper.getTenantIdVosByUserIds(userIds);
			Map<String, List<Integer>> userTenantIdsMap = new HashMap<>();
			for (SysUserTenantIdVo vo : tenantIdVos) {
				if (vo.getUserId() != null && vo.getTenantId() != null) {
					userTenantIdsMap.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(vo.getTenantId());
				}
			}

			// Truy vấn hàng loạt các ID chức vụ (loại bỏ N+1: N lần getPositionIdByUserTenantId → 1 lần truy vấn hàng loạt)
			// Giải thích logic mã: 【QQYUN-7028] sau khi lưu chức vụ của người dùng không hiển thị lại---
			Integer posTenantId = null;
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				posTenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
			}
			List<SysUserPositionVo> positionVos = sysUserPositionMapper.getPositionIdsByUserIds(userIds, posTenantId);
			Map<String, List<String>> userPositionIdsMap = new HashMap<>();
			for (SysUserPositionVo vo : positionVos) {
				if (vo.getUserId() != null && vo.getId() != null) {
					userPositionIdsMap.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(vo.getId());
				}
			}

			// Truy vấn hàng loạt IDs vị trí kiêm nhiệm (loại bỏ N+1: N lần getDepPostByUserId → 1 lần truy vấn hàng loạt)
			List<SysUserDepPostVo> depPostVos = depPostMapper.getDepPostByUserIds(userIds);
			Map<String, List<String>> userDepPostIdsMap = new HashMap<>();
			for (SysUserDepPostVo vo : depPostVos) {
				if (vo.getUserId() != null && vo.getDepId() != null) {
					userDepPostIdsMap.computeIfAbsent(vo.getUserId(), k -> new ArrayList<>()).add(vo.getDepId());
				}
			}

			// Truy vấn hàng loạt trạng thái liên kết DingTalk (loại bỏ N+1：Trong chế độ SAAS thực hiện N lần list → 1 lần truy vấn hàng loạt)
			Map<String, Boolean> userBindThirdMap = new HashMap<>();
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				LambdaQueryWrapper<SysThirdAccount> thirdQuery = new LambdaQueryWrapper<>();
				thirdQuery.in(SysThirdAccount::getSysUserId, userIds);
				thirdQuery.eq(SysThirdAccount::getTenantId, tenantId);
				thirdQuery.eq(SysThirdAccount::getThirdType, MessageTypeEnum.DD.getType());
				List<SysThirdAccount> accounts = sysThirdAccountService.list(thirdQuery);
				for (SysThirdAccount acc : accounts) {
					userBindThirdMap.put(acc.getSysUserId(), true);
				}
			}

			pageList.getRecords().forEach(item -> {
				item.setOrgCodeTxt(useDepNames.get(item.getId()));
				// Phòng ban trực thuộc IDs
				List<String> departs = userDepartIdsMap.getOrDefault(item.getId(), Collections.emptyList());
				if (!departs.isEmpty()) {
					item.setBelongDepIds(String.join(SymbolConstant.COMMA, departs));
				}
				// Tenant IDs
				List<Integer> tenants = userTenantIdsMap.getOrDefault(item.getId(), Collections.emptyList());
				if (!tenants.isEmpty()) {
					item.setRelTenantIds(StringUtils.join(tenants.toArray(), SymbolConstant.COMMA));
				} else {
					item.setRelTenantIds("");
				}
				// Chức vụ IDs
				List<String> positions = userPositionIdsMap.getOrDefault(item.getId(), Collections.emptyList());
				item.setPost(CommonUtils.getSplitText(positions, SymbolConstant.COMMA));
				// Trạng thái liên kết DingTalk
				if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
					item.setIzBindThird(userBindThirdMap.getOrDefault(item.getId(), false));
				}
				// Vị trí kiêm nhiệm IDs
				List<String> depPosts = userDepPostIdsMap.getOrDefault(item.getId(), Collections.emptyList());
				if (!depPosts.isEmpty()) {
					item.setOtherDepPostId(StringUtils.join(depPosts.toArray(), SymbolConstant.COMMA));
				}
			});
			//update-end---author:scott ---date:20260701  for: [Tối ưu hiệu năng] giao diện listAll N+1 Cải tạo truy vấn, tải trước hàng loạt dữ liệu liên quan để loại bỏ N+1-----------
		}

		result.setSuccess(true);
		result.setResult(pageList);
		//log.info(pageList.toString());
		return result;
	}


    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> resetPassword(String username, String oldpassword, String newpassword, String confirmpassword) {
        SysUser user = userMapper.getUserByName(username);
        String passwordEncode = PasswordUtil.encrypt(username, oldpassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            return Result.error("Mật khẩu cũ nhập sai!");
        }
        if (oConvertUtils.isEmpty(newpassword)) {
            return Result.error("Mật khẩu mới không được để trống!");
        }
        if (!newpassword.equals(confirmpassword)) {
            return Result.error("Hai lần nhập mật khẩu không nhất quán!");
        }
        String password = PasswordUtil.encrypt(username, newpassword, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(password), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
        return Result.ok("Đặt lại mật khẩu thành công!!");
    }

    @Override
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public Result<?> changePassword(SysUser sysUser) {
        String salt = oConvertUtils.randomGen(8);
        sysUser.setSalt(salt);
        String password = sysUser.getPassword();
        String passwordEncode = PasswordUtil.encrypt(sysUser.getUsername(), password, salt);
        sysUser.setPassword(passwordEncode);
        sysUser.setLastPwdUpdateTime(new Date());
        this.userMapper.updateById(sysUser);
        return Result.ok("Đổi mật khẩu thành công!!");
    }

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteUser(String userId) {
		//1.Xác minh người dùng hiện tại là tài khoản quản trị viên admin
		//Xác minh người dùng có phải quản trị viên hay không
		this.checkUserAdminRejectDel(userId);
		
		//2.Xóa người dùng
		this.removeById(userId);
		return false;
	}

	@Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteBatchUsers(String userIds) {
		//1.Xác minh người dùng hiện tại là tài khoản quản trị viên admin
		this.checkUserAdminRejectDel(userIds);
		//2.Xóa người dùng
		this.removeByIds(Arrays.asList(userIds.split(",")));
		return false;
	}

	@Override
	public SysUser getUserByName(String username) {
		SysUser sysUser = userMapper.getUserByName(username);
		//Truy vấn các tenant của người dùng ids
		if(sysUser!=null){
			List<Integer> list = userTenantMapper.getTenantIdsByUserId(sysUser.getId());
			if (oConvertUtils.isNotEmpty(list)) {
				sysUser.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
			} else {
				sysUser.setRelTenantIds("");
			}
		}
		return sysUser;
	}
	
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithRole(SysUser user, String roles) {
		this.save(user);
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}

	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	@Transactional(rollbackFor = Exception.class)
	public void editUserWithRole(SysUser user, String roles) {
		this.updateById(user);
		//Xóa trước rồi thêm
		sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(roles)) {
			String[] arr = roles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
	}


	@Override
	public List<String> getRole(String username) {
		return sysUserRoleMapper.getRoleByUserName(username);
	}

	/**
	 * Lấy cấu hình route trang chủ động
	 *
	 * @param username
	 * @param version
	 * @return
	 */
	@Override
	public SysRoleIndex getDynamicIndexByUserRole(String username, String version) {
		SysRoleIndex roleIndex = new SysRoleIndex();
		//Chỉ khi X-Version=v3 khi đó, mới đọc bảng sys_role_index để lấy cấu hình trang chủ theo vai trò sys_role_index Bảng lấy cấu hình trang chủ của vai trò
		boolean isV3 = CommonConstant.VERSION_V3.equals(version);
		if (isV3) {
			if(oConvertUtils.isNotEmpty(username)){
				//update-begin---author:lvdandan ---date:20260901  for：【LHZP-1145][Vue 3] Cấu hình trang chủ, người dùng và vai trò nên hỗ trợ chọn nhiều
				//1.Trước tiên truy vấn tất cả cấu hình trang chủ cấp USER của người dùng
				LambdaQueryWrapper<SysRoleIndex> personalIndexQuery = new LambdaQueryWrapper<>();
				personalIndexQuery.eq(SysRoleIndex::getRelationType, CommonConstant.HOME_RELATION_USER);
				personalIndexQuery.eq(SysRoleIndex::getRoleCode, username);
				personalIndexQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
				personalIndexQuery.orderByAsc(SysRoleIndex::getPriority);
				List<SysRoleIndex> personalIndexList = sysRoleIndexService.list(personalIndexQuery);
				List<SysRoleIndex> list = Collections.emptyList();
				boolean queryRoleIndex = true;
				if (CollectionUtils.isNotEmpty(personalIndexList)) {
					roleIndex = personalIndexList.get(0);
					queryRoleIndex = false;
				} else {
					LambdaQueryWrapper<SysRoleIndex> routeIndexUserQuery = new LambdaQueryWrapper<>();
					//Trạng thái trang chủ vai trò: 0: chưa bật 1: đã bật
					routeIndexUserQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
					routeIndexUserQuery.eq(SysRoleIndex::getRelationType, CommonConstant.HOME_RELATION_USER);
					//Sắp xếp tăng dần theo mức độ ưu tiên
					routeIndexUserQuery.orderByAsc(SysRoleIndex::getPriority);
					list = sysRoleIndexService.list(routeIndexUserQuery).stream()
							.filter(item -> containsAnyRelationCode(item.getRoleCode(), Collections.singleton(username)))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(list)) {
						roleIndex = list.get(0);
						queryRoleIndex = false;
					}
				}
				if (queryRoleIndex) {
				   //2.Người dùng chưa có cấu hình thì truy vấn tiếp tất cả cấu hình trang chủ ở cấp vai trò ROLE
					LambdaQueryWrapper<SysRoleIndex> routeIndexQuery = new LambdaQueryWrapper<>();
					//Trạng thái trang chủ vai trò: 0: chưa bật 1: đã bật
					routeIndexQuery.eq(SysRoleIndex::getStatus, CommonConstant.STATUS_1);
					//Tất cả cấu hình trang chủ của vai trò
					routeIndexQuery.eq(SysRoleIndex::getRelationType, CommonConstant.HOME_RELATION_ROLE);
					//Vai trò của người dùng hiện tại
					List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
					String componentUrl = RoleIndexConfigEnum.getIndexByRoles(roles);
					roleIndex = new SysRoleIndex(componentUrl);
					//Tất cả vai trò của người dùng
					//Sắp xếp tăng dần theo mức độ ưu tiên
					routeIndexQuery.orderByAsc(SysRoleIndex::getPriority);
					list = sysRoleIndexService.list(routeIndexQuery).stream()
							.filter(item -> containsAnyRelationCode(item.getRoleCode(), roles))
							.collect(Collectors.toList());
					//update-end---author:lvdandan ---date:20260901  for：【LHZP-1145][Vue 3] Cấu hình trang chủ, người dùng và vai trò nên hỗ trợ chọn nhiều
					if (CollectionUtils.isNotEmpty(list)) {
						roleIndex = list.get(0);
					}
				}
			}
		}

		if (oConvertUtils.isEmpty(roleIndex.getComponent())) {
			if (isV3) {
				// Nếu vai trò chưa cấu hình trang chủ thì dùng trang chủ mặc định
				return sysRoleIndexService.queryDefaultIndex();
			} else {
				// Không phải v3 thì trả về null
				return null;
			}
		}
		return roleIndex;
	}

	/**
	 * Phán đoán xem mã liên kết phân tách bằng dấu phẩy có giao nhau với mã đích hay không
	 *
	 * @param relationCodes Mã người dùng hoặc vai trò được phân tách bằng dấu phẩy
	 * @param targetCodes Mã người dùng hoặc vai trò đích
	 * @return true: tồn tại mã khớp, false: không tồn tại mã khớp
	 */
	private boolean containsAnyRelationCode(String relationCodes, Collection<String> targetCodes) {
		if (oConvertUtils.isEmpty(relationCodes) || CollectionUtil.isEmpty(targetCodes)) {
			return false;
		}
		Set<String> relationCodeSet = Arrays.stream(relationCodes.split(SymbolConstant.COMMA))
				.map(String::trim)
				.filter(oConvertUtils::isNotEmpty)
				.collect(Collectors.toSet());
		return targetCodes.stream()
				.filter(oConvertUtils::isNotEmpty)
				.map(String::trim)
				.anyMatch(relationCodeSet::contains);
	}

	/**
	 * Lấy tập hợp vai trò người dùng qua tên người dùng
	 * @param username Tên người dùng
     * @return Tập hợp vai trò
	 */
	@Override
	public Set<String> getUserRolesSet(String username) {
		// Truy vấn tập hợp vai trò mà người dùng sở hữu
		List<String> roles = sysUserRoleMapper.getRoleByUserName(username);
		log.info("-------Đọc các vai trò mà người dùng sở hữu qua cơ sở dữ liệu Rules------username： " + username + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}
	
	/**
	 * Lấy tập hợp vai trò người dùng qua tên người dùng
	 * @param userId Người dùng ID
     * @return Tập hợp vai trò
	 */
	@Override
	public Set<String> getUserRoleSetById(String userId) {
		// Truy vấn tập hợp vai trò mà người dùng sở hữu
		List<String> roles = sysUserRoleMapper.getRoleCodeByUserId(userId);
		log.info("-------Đọc các vai trò mà người dùng sở hữu qua cơ sở dữ liệu Rules------userId： " + userId + ",Roles size: " + (roles == null ? 0 : roles.size()));
		return new HashSet<>(roles);
	}

	/**
	 * Lấy tập hợp quyền hạn người dùng qua tên người dùng
	 *
	 * @param userId Người dùng ID
	 * @return Tập hợp quyền hạn
	 */
	@Override
	public Set<String> getUserPermissionsSet(String userId) {
		Set<String> permissionSet = new HashSet<>();
		List<SysPermission> permissionList = sysPermissionMapper.queryByUser(userId);
		//================= begin Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			if (permissionList == null) {
				permissionList = new ArrayList<>();
			}
			List<SysPermission> testRoleList = sysPermissionMapper.queryPermissionByTestRoleId();
			permissionList.addAll(testRoleList);
		}
		//================= end Khi bật thuê bao, nếu không có vai trò test thì mặc định thêm vai trò test================
		for (SysPermission po : permissionList) {
//			// TODO URL Quy tắc có vấn đề?
//			if (oConvertUtils.isNotEmpty(po.getUrl())) {
//				permissionSet.add(po.getUrl());
//			}
			if (oConvertUtils.isNotEmpty(po.getPerms())) {
				permissionSet.add(po.getPerms());
			}
		}
		log.info("-------Đọc các quyền hạn mà người dùng sở hữu qua cơ sở dữ liệu Perms------userId： "+ userId+",Perms size: "+ (permissionSet==null?0:permissionSet.size()) );
		return permissionSet;
	}

	/**
	 * Nâng cấp SpringBoot2.6.6,Không cho phép phụ thuộc vòng tròn
	 * @author:qinfeng
	 * @update: 2022-04-07
	 * @param username
	 * @return
	 */
	@Override
	public SysUserCacheInfo getCacheUser(String username) {
		SysUserCacheInfo info = new SysUserCacheInfo();
		info.setOneDepart(true);
		if(oConvertUtils.isEmpty(username)) {
			return null;
		}

		//Truy vấn thông tin người dùng
		SysUser sysUser = userMapper.getUserByName(username);
		if(sysUser!=null) {
			info.setSysUserCode(sysUser.getUsername());
			info.setSysUserName(sysUser.getRealname());
			info.setSysOrgCode(sysUser.getOrgCode());
		}
		
		//Nhiều phòng ban hỗ trợ truy vấn in
		List<SysDepart> list = sysDepartMapper.queryUserDeparts(sysUser.getId());
		List<String> sysMultiOrgCode = new ArrayList<String>();
		if(list==null || list.size()==0) {
			//Người dùng hiện tại không có phòng ban
			//sysMultiOrgCode.add("0");
		}else if(list.size()==1) {
			sysMultiOrgCode.add(list.get(0).getOrgCode());
		}else {
			info.setOneDepart(false);
			for (SysDepart dpt : list) {
				sysMultiOrgCode.add(dpt.getOrgCode());
			}
		}
		info.setSysMultiOrgCode(sysMultiOrgCode);
		
		return info;
	}

    /**
     * Truy vấn theo Id phòng ban
     * @param page
     * @param departId Phòng ban id
     * @param username Tên tài khoản người dùng
     * @return
     */
	@Override
	public IPage<SysUser> getUserByDepId(Page<SysUser> page, String departId,String username) {
		return userMapper.getUserByDepId(page, departId,username);
	}

	@Override
	public IPage<SysUser> getUserByDepIds(Page<SysUser> page, List<String> departIds, String username, String realname) {
        //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1141][Quản lý hệ thống] Thông tin liên kết người dùng khi chỉnh sửa phòng ban của tôi chưa hiển thị lại đúng------------
        IPage<SysUser> pageList = userMapper.getUserByDepIds(page, departIds, username, realname);
		List<String> userIds = pageList.getRecords().stream().map(SysUser::getId).collect(Collectors.toList());
		if (CollectionUtil.isEmpty(userIds)) {
			return pageList;
		}
		pageList.getRecords().forEach(item -> {
			List<String> depPostList = depPostMapper.getDepPostByUserId(item.getId());
			item.setOtherDepPostId(CollectionUtil.isNotEmpty(depPostList)
					? StringUtils.join(depPostList.toArray(), SymbolConstant.COMMA) : "");
		});
		return pageList;
        //update-end---author:wangshuai ---date:20260811  for：[LHZP-1141][Quản lý hệ thống] Thông tin liên kết người dùng khi chỉnh sửa phòng ban của tôi chưa hiển thị lại đúng------------
    }

	@Override
	public Map<String, String> getDepNamesByUserIds(List<String> userIds) {
		List<SysUserDepVo> list = this.baseMapper.getDepNamesByUserIds(userIds);

		Map<String, String> res = new HashMap(5);
		list.forEach(item -> {
					if (res.get(item.getUserId()) == null) {
						res.put(item.getUserId(), item.getDepartName());
					} else {
						res.put(item.getUserId(), res.get(item.getUserId()) + "," + item.getDepartName());
					}
				}
		);
		return res;
	}

/*	@Override
	public IPage<SysUser> getUserByDepartIdAndQueryWrapper(Page<SysUser> page, String departId, QueryWrapper<SysUser> queryWrapper) {
		LambdaQueryWrapper<SysUser> lambdaQueryWrapper = queryWrapper.lambda();

		lambdaQueryWrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
        lambdaQueryWrapper.inSql(SysUser::getId, "SELECT user_id FROM sys_user_depart WHERE dep_id = '" + departId + "'");

        return userMapper.selectPage(page, lambdaQueryWrapper);
	}*/

	@Override
	public IPage<SysUserSysDepartModel> queryUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
		List<SysUserSysDepartModel> list = baseMapper.getUserByOrgCode(page, orgCode, userParams);
		//Truy vấn phòng ban theo orgCode của phòng ban, cần truyền id vị trí
		for (SysUserSysDepartModel model:list) {
			List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(model.getId());
			model.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
		}
		Integer total = baseMapper.getUserByOrgCodeTotal(orgCode, userParams);

		IPage<SysUserSysDepartModel> result = new Page<>(page.getCurrent(), page.getSize(), total);
		result.setRecords(list);

		return result;
	}

    /**
     * Truy vấn theo Id vai trò
     * @param page
     * @param roleId Vai trò id
     * @param username Tên tài khoản người dùng
     * @param realname Họ tên người dùng
     * @return
     */
	@Override
	public IPage<SysUser> getUserByRoleId(Page<SysUser> page, String roleId, String username, String realname) {
		// Giải thích logic mã: [QQYUN-3980]Trong quản lý tổ chức, chức năng vị trí: bảng vị trí thêm tenant id, thêm vị trí-Bảng liên kết người dùng------------
		IPage<SysUser> userRoleList = userMapper.getUserByRoleId(page, roleId, username,realname);
		List<SysUser> records = userRoleList.getRecords();
		if (null != records && records.size() > 0) {
			List<String> userIds = records.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
			for (SysUser sysUser : userRoleList.getRecords()) {
				//Cài đặt phòng ban
				sysUser.setOrgCodeTxt(useDepNames.get(sysUser.getId()));
				//Đặt vị trí người dùng id
				this.userPositionId(sysUser);
			}
		}
		return userRoleList;
	}


	@Override
	@CacheEvict(value= {CacheConstant.SYS_USERS_CACHE}, key="#username")
	public void updateUserDepart(String username,String orgCode,Integer loginTenantId) {
		baseMapper.updateUserDepart(username, orgCode,loginTenantId);
	}


	@Override
	public SysUser getUserByPhone(String phone) {
		return userMapper.getUserByPhone(phone);
	}


	@Override
	public SysUser getUserByEmail(String email) {
		return userMapper.getUserByEmail(email);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addUserWithDepart(SysUser user, String selectedParts) {
//		this.save(user);  //Khi lưu vai trò đã thêm một lần rồi
		if(oConvertUtils.isNotEmpty(selectedParts)) {
			String[] arr = selectedParts.split(",");
			for (String deaprtId : arr) {
				SysUserDepart userDeaprt = new SysUserDepart(user.getId(), deaprtId);
				sysUserDepartMapper.insert(userDeaprt);
			}
		}
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUserWithDepart(SysUser user, String departs) {
        //Khi cập nhật vai trò đã cập nhật một lần rồi, có thể cập nhật thêm một lần nữa
		this.updateById(user);
		String[] arr = {};
		if(oConvertUtils.isNotEmpty(departs)){
			arr = departs.split(",");
		}
		//Truy vấn phòng ban đã liên kết
		List<SysUserDepart> userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(userDepartList != null && userDepartList.size()>0){
			for(SysUserDepart depart : userDepartList ){
				//Sửa quan hệ người dùng - vai trò của phòng ban khi xóa phòng ban đã liên kết
				if(!Arrays.asList(arr).contains(depart.getDepId())){
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(roleIds != null && roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId,roleIds));
					}
				}
			}
		}
		//Xóa trước rồi thêm
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(departs)) {
			for (String departId : arr) {
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
	}


	/**
	   * Kiểm tra người dùng có hợp lệ không
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<?> checkUserIsEffective(SysUser sysUser) {
		Result<?> result = new Result<Object>();
		//Trường hợp 1: Truy vấn theo thông tin người dùng, người dùng này không tồn tại
		if (sysUser == null) {
			result.error500("Người dùng này không tồn tại, vui lòng đăng ký");
			baseCommonService.addLog("Người dùng đăng nhập thất bại, người dùng không tồn tại!", CommonConstant.LOG_TYPE_1, null);
			return result;
		}
		//Trường hợp 2: Truy vấn theo thông tin người dùng, người dùng này đã bị hủy
		// Giải thích logic mã: if Điều kiện luôn là falsebug------------
		if (CommonConstant.DEL_FLAG_1.equals(sysUser.getDelFlag())) {
			baseCommonService.addLog("Đăng nhập người dùng thất bại, tên người dùng:" + sysUser.getUsername() + "Đã bị hủy!", CommonConstant.LOG_TYPE_1, null);
			result.error500("Người dùng này đã bị hủy tài khoản");
			return result;
		}
		//Trường hợp 3: Truy vấn theo thông tin người dùng, người dùng này đã bị đóng băng
		if (CommonConstant.USER_FREEZE.equals(sysUser.getStatus())) {
			baseCommonService.addLog("Đăng nhập người dùng thất bại, tên người dùng:" + sysUser.getUsername() + "Đã bị đóng băng!", CommonConstant.LOG_TYPE_1, null);
			result.error500("Người dùng này đã bị đóng băng");
			return result;
		}
		return result;
	}

	//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Thống nhất việc đếm lần đăng nhập thất bại với kiểm tra mật khẩu; người dùng không tồn tại cũng được tính vào số lần thất bại, ngăn liệt kê người dùng-----------
	@Override
	public boolean verifyLoginPassword(SysUser sysUser, String username, String password) {
		if (sysUser == null) {
			baseCommonService.addLog("Người dùng đăng nhập thất bại, người dùng không tồn tại!", CommonConstant.LOG_TYPE_1, null);
			addLoginFailOvertimes(username);
			return false;
		}
		String userpassword = PasswordUtil.encrypt(username, password, sysUser.getSalt());
		if (!userpassword.equals(sysUser.getPassword())) {
			addLoginFailOvertimes(username);
			return false;
		}
		return true;
	}

	@Override
	public boolean isLoginFailOvertimes(String key) {
		Object failTime = redisUtil.get(CommonConstant.LOGIN_FAIL + key);
		return failTime != null && Integer.parseInt(failTime.toString()) > 5;
	}

	@Override
	public void addLoginFailOvertimes(String key) {
		String redisKey = CommonConstant.LOGIN_FAIL + key;
		Object failTime = redisUtil.get(redisKey);
		int val = failTime != null ? Integer.parseInt(failTime.toString()) : 0;
		// 10 phút, một phút là 60s
		redisUtil.set(redisKey, ++val, 600);
	}
	//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] Thống nhất việc đếm lần đăng nhập thất bại với kiểm tra mật khẩu; người dùng không tồn tại cũng được tính vào số lần thất bại, ngăn liệt kê người dùng-----------

	@Override
	public List<SysUser> queryLogicDeleted() {
        // Giải thích logic mã: Thùng rác truy vấn người chưa nghỉ việc------------
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(SysUser::getStatus, CommonConstant.USER_QUIT);
        return this.queryLogicDeleted(wrapper);
    }

	@Override
	public List<SysUser> queryLogicDeleted(LambdaQueryWrapper<SysUser> wrapper) {
		if (wrapper == null) {
			wrapper = new LambdaQueryWrapper<>();
		}
		wrapper.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_1);
		return userMapper.selectLogicDeleted(wrapper);
	}

	@Override
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public boolean revertLogicDeleted(List<String> userIds, SysUser updateEntity) {
		return userMapper.revertLogicDeleted(userIds, updateEntity) > 0;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean removeLogicDeleted(List<String> userIds) {
		// 1. Xóa người dùng
		int line = userMapper.deleteLogicDeleted(userIds);
		// 2. Xóa quan hệ phòng ban người dùng
		line += sysUserDepartMapper.delete(new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getUserId, userIds));
		//3. Xóa quan hệ vai trò người dùng
		line += sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
		//4.Đồng bộ xóa người dùng của App bên thứ ba
		try {
			dingtalkService.removeThirdAppUser(userIds);
			wechatEnterpriseService.removeThirdAppUser(userIds);
		} catch (Exception e) {
			log.error("Đồng bộ xóa người dùng của App bên thứ ba thất bại:", e);
		}
		//5. Xóa bảng người dùng bên thứ ba (vì bước 4 cần dùng đến bảng người dùng bên thứ ba nên xóa sau bước đó)
		line += sysThirdAccountMapper.delete(new LambdaQueryWrapper<SysThirdAccount>().in(SysThirdAccount::getSysUserId, userIds));

		//6. Xóa dữ liệu bảng trung gian người dùng tenant
		line += userTenantMapper.delete(new LambdaQueryWrapper<SysUserTenant>().in(SysUserTenant::getUserId,userIds));
		
		return line != 0;
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNullPhoneEmail() {
        userMapper.updateNullByEmptyString("email");
        userMapper.updateNullByEmptyString("phone");
        return true;
    }

	@Override
	public void saveThirdUser(SysUser sysUser) {
		//Lưu người dùng
		String userid = UUIDGenerator.generate();
		sysUser.setId(userid);
		baseMapper.insert(sysUser);
		//Lấy vai trò bên thứ ba
		SysRole sysRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "third_role"));
		//Lưu vai trò người dùng
		SysUserRole userRole = new SysUserRole();
		userRole.setRoleId(sysRole.getId());
		userRole.setUserId(userid);
		sysUserRoleMapper.insert(userRole);
	}

	@Override
	public List<SysUser> queryByDepIds(List<String> departIds, String username) {
		return userMapper.queryByDepIds(departIds,username);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveUser(SysUser user, String selectedRoles, String selectedDeparts, String relTenantIds, boolean izSyncPack) {
        if(null == user.getSort()){
            user.setSort(CommonConstant.DEFAULT_USER_SORT);
        }
        //step.1 Lưu người dùng
		this.save(user);
		//Lấy id tenant do frontend truyền qua khi người dùng lưu và thêm vào tenant
        this.saveUserTenant(user.getId(),relTenantIds, izSyncPack);
		//step.2 Lưu vai trò
		if(oConvertUtils.isNotEmpty(selectedRoles)) {
			String[] arr = selectedRoles.split(",");
			for (String roleId : arr) {
				SysUserRole userRole = new SysUserRole(user.getId(), roleId);
				sysUserRoleMapper.insert(userRole);
			}
		}
		
		//step.3 Lưu phòng ban trực thuộc
		if(oConvertUtils.isNotEmpty(selectedDeparts)) {
			String[] arr = selectedDeparts.split(",");
			for (String deaprtId : arr) {
				SysUserDepart userDeaprt = new SysUserDepart(user.getId(), deaprtId);
				sysUserDepartMapper.insert(userDeaprt);
			}
		}

		//step.4 Lưu vị trí
		this.saveUserPosition(user.getId(),user.getPost());
        //step5 Lưu vị trí công việc kiêm nhiệm
        this.saveUserOtherDepPost(user.getId(),user.getOtherDepPostId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editUser(SysUser user, String roles, String departs, String relTenantIds, String updateFromPage) {
		//Lấy tenant mà giao diện truyền tới khi người dùng chỉnh sửa id
        this.editUserTenants(user.getId(),relTenantIds);
        if(null == user.getSort()){
            user.setSort(CommonConstant.DEFAULT_USER_SORT);
        }
		//step.1 Sửa thông tin cơ bản của người dùng
		this.updateById(user);
		//step.2 Chỉnh sửa vai trò
		if (oConvertUtils.isEmpty(updateFromPage) || !"deptUsers".equalsIgnoreCase(updateFromPage)) {
			// Xử lý vai trò người dùng: xóa trước rồi thêm , Nếu đang sửa người dùng trên trang người dùng của phòng ban,Không xử lý vai trò người dùng,Vì trang này không thể chỉnh sửa vai trò người dùng.
			sysUserRoleMapper.delete(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getUserId, user.getId()));
			if (oConvertUtils.isNotEmpty(roles)) {
				String[] arr = roles.split(",");
				for (String roleId : arr) {
					SysUserRole userRole = new SysUserRole(user.getId(), roleId);
					sysUserRoleMapper.insert(userRole);
				}
			}
		}

		//step.3 Chỉnh sửa phòng ban
		String[] arr = {};
		if(oConvertUtils.isNotEmpty(departs)){
			arr = departs.split(",");
		}
		//Truy vấn phòng ban đã liên kết
		List<SysUserDepart> userDepartList = sysUserDepartMapper.selectList(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(userDepartList != null && userDepartList.size()>0){
			for(SysUserDepart depart : userDepartList ){
				//Sửa quan hệ người dùng - vai trò của phòng ban khi xóa phòng ban đã liên kết
				if(!Arrays.asList(arr).contains(depart.getDepId())){
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId,depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if(roleIds != null && roleIds.size()>0){
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId,roleIds));
					}
				}
			}
		}
		//Xóa trước rồi thêm
		sysUserDepartMapper.delete(new QueryWrapper<SysUserDepart>().lambda().eq(SysUserDepart::getUserId, user.getId()));
		if(oConvertUtils.isNotEmpty(departs)) {
			for (String departId : arr) {
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
		//step.4 Sửa số điện thoại và email
		// Cập nhật số điện thoại, email từ chuỗi trống thành null
		userMapper.updateNullByEmptyString("email");
		userMapper.updateNullByEmptyString("phone");

		//step.5 Chỉnh sửa vị trí
		this.editUserPosition(user.getId(),user.getPost());

        //Giải thích logic mã: Chuyển đổi vị trí kiêm nhiệm sang dạng bảng trung gian---
        //step6 Chỉnh sửa vị trí kiêm nhiệm
        //Xóa trước rồi thêm
        depPostMapper.delete(new QueryWrapper<SysUserDepPost>().lambda().eq(SysUserDepPost::getUserId, user.getId()));
        this.saveUserOtherDepPost(user.getId(),user.getOtherDepPostId());
	}


    /**
     * Lưu vị trí công việc kiêm nhiệm
     *
     * @param userId
     * @param otherDepPostId
     */
    private void saveUserOtherDepPost(String userId, String otherDepPostId) {
        if (oConvertUtils.isNotEmpty(otherDepPostId)) {
            String[] depPostId = otherDepPostId.split(SymbolConstant.COMMA);
            for (String postId : depPostId) {
                SysUserDepPost userPosition = new SysUserDepPost(userId, postId);
                depPostMapper.insert(userPosition);
            }
        }
    }

	@Override
	public List<String> userIdToUsername(Collection<String> userIdList) {
		LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.in(SysUser::getId, userIdList);
		List<SysUser> userList = super.list(queryWrapper);
		return userList.stream().map(SysUser::getUsername).collect(Collectors.toList());
	}

	@Override
	@Cacheable(cacheNames=CacheConstant.SYS_USERS_CACHE, key="#username")
	@SensitiveEncode
	public LoginUser getEncodeUserInfo(String username){
		if(oConvertUtils.isEmpty(username)) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		SysUser sysUser = userMapper.getUserByName(username);
		//Truy vấn các tenant của người dùng ids
		this.setUserTenantIds(sysUser);
		//Cài đặt chức vụ id
		this.userPositionId(sysUser);
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		// Truy vấn phòng ban của người dùng đang đăng nhập id
		loginUser.setOrgId(this.getDepartIdByOrCode(sysUser.getOrgCode()));
		// Truy vấn role code của người dùng đang đăng nhập (nhiều cái phân tách bằng dấu phẩy)
		loginUser.setRoleCode(this.getJoinRoleCodeByUserId(sysUser.getId()));
		return loginUser;
	}

    @Override
    @CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
    @Transactional(rollbackFor = Exception.class)
    public void userQuit(String username) {
        SysUser sysUser = userMapper.getUserByName(username);
        if(null == sysUser){
            throw new JeecgBootException("Nghỉ việc thất bại, người dùng này không còn tồn tại");
        }
		// Giải thích logic mã: [QQYUN-3951]Tái cấu trúc người dùng tenant rời khỏi------------
		int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
		//Cập nhật trạng thái bảng tenant của người dùng thành trạng thái đã nghỉ việc
		if(tenantId==0){
			throw new JeecgBootException("Nghỉ việc thất bại, Tenant không tồn tại");
		}
		LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
		query.eq(SysUserTenant::getUserId,sysUser.getId());
		query.eq(SysUserTenant::getTenantId,tenantId);
		SysUserTenant userTenant = new SysUserTenant();
		userTenant.setStatus(CommonConstant.USER_TENANT_QUIT);
		userTenantMapper.update(userTenant,query);
    }

    @Override
    public List<SysUser> getQuitList(Integer tenantId) {
        return userMapper.getTenantQuitList(tenantId);
    }

    @Override
    public void updateStatusAndFlag(List<String> userIds, SysUser sysUser) {
        userMapper.updateStatusAndFlag(userIds,sysUser);
    }

	/**
	 * Đặt tenant đăng nhập
	 * @param sysUser
	 * @return
	 */
	@Override
	public Result<JSONObject>  setLoginTenant(SysUser sysUser, JSONObject obj, String username, Result<JSONObject> result){
		//Người dùng có những tenant nào
//		List<SysTenant> tenantList = null;
		// Giải thích logic mã: [QQYUN-3371]Cải tạo logic Tenant, đổi sang bảng quan hệ
		List<SysTenant> tenantList = relationMapper.getTenantNoCancel(sysUser.getId());
		obj.put("tenantList", tenantList);
//		if (null!=tenantIdList && tenantIdList.size()>0) {
//			//-------------------------------------------------------------------------------------
//			//Truy vấn tập hợp Tenant hợp lệ
//			LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
//			queryWrapper.in(SysTenant::getId, tenantIdList);
//			queryWrapper.eq(SysTenant::getStatus, Integer.valueOf(CommonConstant.STATUS_1));
//			tenantList = sysTenantMapper.selectList(queryWrapper);
//			//-------------------------------------------------------------------------------------
//
//			if (tenantList.size() == 0) {
//				return result.error500("Các tenant liên kết với người dùng này đều đã bị đóng băng, không thể đăng nhập!");
//			} else {
//				obj.put("tenantList", tenantList);
//			}
//		}


		//ID tenant của phiên đăng nhập, đặt lại tính hiệu lực
		if (tenantList != null && tenantList.size() > 0) {
			if (tenantList.size() == 1) {
				sysUser.setLoginTenantId(tenantList.get(0).getId());
			} else {
				List<SysTenant> listAfterFilter = tenantList.stream().filter(s -> s.getId().equals(sysUser.getLoginTenantId())).collect(Collectors.toList());
				if (listAfterFilter == null || listAfterFilter.size() == 0) {
					//Nếu ID tenant của lần đăng nhập trước không còn trong tập hợp tenant mà người dùng sở hữu nữa thì lấy ngẫu nhiên tenant đầu tiên mà người dùng sở hữu ID
					sysUser.setLoginTenantId(tenantList.get(0).getId());
				}
			}
		} else {
			//Khi không có Tenant, đặt thành 0
			sysUser.setLoginTenantId(0);
		}
		//Đặt Tenant cache khi người dùng đăng nhập
		this.updateUserDepart(username, null,sysUser.getLoginTenantId());
		log.debug(" Tenant của người dùng giao diện đăng nhập ID = {}", sysUser.getLoginTenantId());
		if(sysUser.getLoginTenantId()!=null){
			//Khi đăng nhập cần đặt thủ công tenant trong phiên ID,Nếu không, giao diện đăng nhập không thể tra được dữ liệu qua cách ly tenant
			TenantContext.setTenant(sysUser.getLoginTenantId()+"");
		}
		return null;
	}


    /**
     * Lấy tenant id
     * @param sysUser
     */
    private void setUserTenantIds(SysUser sysUser) {
		if(ObjectUtils.isNotEmpty(sysUser)) {
			List<Integer> list  = relationMapper.getTenantIdsNoStatus(sysUser.getId());
			if(null!=list && list.size()>0){
				sysUser.setRelTenantIds(StringUtils.join(list.toArray(), ","));
			}else{
				sysUser.setRelTenantIds("");
			}
		}
    }

    /**
     * Lưu tenant
     *
     * @param userId
     * @param relTenantIds
     * @param izSyncPack Có cần đồng bộ người dùng vào gói sản phẩm hiện tại không
     */
    private void saveUserTenant(String userId, String relTenantIds, boolean izSyncPack) {
        if (oConvertUtils.isNotEmpty(relTenantIds)) {
            String[] tenantIds = relTenantIds.split(SymbolConstant.COMMA);
            for (String tenantId : tenantIds) {
                SysUserTenant relation = new SysUserTenant();
                relation.setUserId(userId);
                relation.setTenantId(Integer.valueOf(tenantId));
                relation.setStatus(CommonConstant.STATUS_1);
                
				LambdaQueryWrapper sysUserTenantQueryWrapper = new LambdaQueryWrapper<SysUserTenant>()
						.eq(SysUserTenant::getUserId, userId)
						.eq(SysUserTenant::getTenantId,Integer.valueOf(tenantId));
				SysUserTenant tenantPresent = relationMapper.selectOne(sysUserTenantQueryWrapper);
				if (tenantPresent != null) {
					tenantPresent.setStatus(CommonConstant.STATUS_1);
					relationMapper.updateById(tenantPresent);
				}else{
					relationMapper.insert(relation);
                    ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
                    //Mặc định thêm người dùng hiện tại vào gói tenant
                    currentService.addPackUser(userId,tenantId);
				}
            }
        }else{
			//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				// Giải thích logic mã: Kiểm tra người dùng hiện tại có nằm trong thuê bao hiện tại hay không, nếu không tồn tại thì thêm mới------------
				String tenantId = TenantContext.getTenant();
				if(oConvertUtils.isNotEmpty(tenantId)){
					Integer count = relationMapper.userTenantIzExist(userId, Integer.parseInt(tenantId));
					if(count == 0){
						SysUserTenant relation = new SysUserTenant();
						relation.setUserId(userId);
						relation.setTenantId(Integer.parseInt(tenantId));
						relation.setStatus(CommonConstant.STATUS_1);
						relationMapper.insert(relation);
                        if(izSyncPack){
                            ISysTenantService currentService = SpringContextUtils.getApplicationContext().getBean(ISysTenantService.class);
                            //Tự động thêm tất cả gói cước thuộc tenant cho người dùng
                            currentService.addPackUser(userId,tenantId);
                        }
					}
				}
			}
		}
    }

    /**
     * Chỉnh sửa tenant
     * @param userId
     * @param relTenantIds
     */
    private void editUserTenants(String userId, String relTenantIds) {
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId, userId);
        //tenant của cơ sở dữ liệu id
		List<Integer> oldTenantIds = relationMapper.getTenantIdsByUserId(userId);
        //Nếu id tenant truyền qua rỗng thì xóa tenant
        if (oConvertUtils.isEmpty(relTenantIds) && CollectionUtils.isNotEmpty(oldTenantIds)) {
            this.deleteTenantByUserId(userId, null);
        } else if (oConvertUtils.isNotEmpty(relTenantIds) && CollectionUtils.isEmpty(oldTenantIds)) {
            //Nếu id tenant truyền sang không rỗng nhưng id tenant trong cơ sở dữ liệu rỗng thì thêm mới
            this.saveUserTenant(userId, relTenantIds, false);
        } else {
			//Cả hai đều không trống thì cần so sánh để thêm hoặc xóa
			if(oConvertUtils.isNotEmpty(relTenantIds) && CollectionUtils.isNotEmpty(oldTenantIds)){
				//Tìm điểm khác nhau giữa id thuê bao mới và id thuê bao cũ, rồi thực hiện xóa
				String[] relTenantIdArray = relTenantIds.split(SymbolConstant.COMMA);
				List<String> relTenantIdList = Arrays.asList(relTenantIdArray);
				
				List<Integer> deleteTenantIdList = oldTenantIds.stream().filter(item -> !relTenantIdList.contains(item.toString())).collect(Collectors.toList());
				for (Integer tenantId : deleteTenantIdList) {
					this.deleteTenantByUserId(userId, tenantId);
				}
				//Tìm điểm khác nhau giữa id người dùng của thuê bao cũ và id thuê bao mới, rồi thực hiện thêm mới
				String tenantIds = relTenantIdList.stream().filter(item -> !oldTenantIds.contains(Integer.valueOf(item))).collect(Collectors.joining(","));
				this.saveUserTenant(userId, tenantIds, false);
			}
        }
    }

    /**
     * Xóa tenant theo id người dùng id
     * @param tenantId
     * @param userId
     */
    private void deleteTenantByUserId(String userId,Integer tenantId){
        LambdaQueryWrapper<SysUserTenant> query = new LambdaQueryWrapper<>();
        query.eq(SysUserTenant::getUserId, userId);
        if(oConvertUtils.isNotEmpty(tenantId)){
            query.eq(SysUserTenant::getTenantId, tenantId);
        }
        relationMapper.delete(query);
        //Xóa liên kết người dùng gói sản phẩm
        LambdaQueryWrapper<SysTenantPackUser> packUserQuery = new LambdaQueryWrapper<>();
        packUserQuery.eq(SysTenantPackUser::getUserId, userId);
        if(oConvertUtils.isNotEmpty(tenantId)){
            packUserQuery.eq(SysTenantPackUser::getTenantId, tenantId);
        }
        packUserMapper.delete(packUserQuery);
    }



	@Override
	public void batchEditUsers(JSONObject json) {
		String userIds = json.getString("userIds");
		List<String> idList = JSONArray.parseArray(userIds, String.class);
		//Phòng ban
		String selecteddeparts = json.getString("selecteddeparts");
		//Chức vụ
		String post = json.getString("post");
		//Địa điểm làm việc? Không có trường này
		String workAddress = json.getString("workAddress");
		//Chỉnh sửa hàng loạt chức vụ người dùng
		if(oConvertUtils.isNotEmpty(post)) {
			//Sửa bảng quan hệ người dùng chức vụ
			for (String userId:idList) {
				this.editUserPosition(userId,post);
			}
		}
		if(oConvertUtils.isNotEmpty(selecteddeparts)) {
			//Truy vấn danh sách phòng ban của Tenant hiện tại
			Integer currentTenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
			LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<SysDepart>()
					.eq(SysDepart::getTenantId, currentTenantId);
			List<SysDepart> departList = sysDepartMapper.selectList(departQuery);
			if(departList==null || departList.size()==0){
				log.error("batchEditUsers Không tìm thấy phòng ban theo ID Tenant>"+currentTenantId);
				return;
			}
			List<String> departIdList = new ArrayList<String>();
			for(SysDepart depart: departList){
				if(depart!=null){
					String id = depart.getId();
					if(oConvertUtils.isNotEmpty(id)){
						departIdList.add(id);
					}
				}
			}
			//Xóa liên kết phòng ban của nhân viên
			LambdaQueryWrapper<SysUserDepart> query = new LambdaQueryWrapper<SysUserDepart>()
					.in(SysUserDepart::getUserId, idList)
					.in(SysUserDepart::getDepId, departIdList);
			sysUserDepartMapper.delete(query);
			
			String[] arr = selecteddeparts.split(",");
			
			//Thêm mới lại
			for (String deaprtId : arr) {
				for(String userId: idList){
					SysUserDepart userDepart = new SysUserDepart(userId, deaprtId);
					sysUserDepartMapper.insert(userDepart);
				}
			}
		}
	}

	@Override
	public DepartAndUserInfo searchByKeyword(String keyword) {
		DepartAndUserInfo departAndUserInfo = new DepartAndUserInfo();
		if(oConvertUtils.isNotEmpty(keyword)){
			LambdaQueryWrapper<SysUser> query1 = new LambdaQueryWrapper<SysUser>()
					.like(SysUser::getRealname, keyword);
			String str = oConvertUtils.getString(TenantContext.getTenant(), "0");
			Integer tenantId = Integer.valueOf(str);
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				List<String> userIds = userTenantMapper.getUserIdsByTenantId(tenantId);
				if (oConvertUtils.listIsNotEmpty(userIds)) {
					query1.in(SysUser::getId, userIds);
				}else{
					query1.eq(SysUser::getId, "");
				}
			}
			List<SysUser> list1 = this.baseMapper.selectList(query1);
			if(list1!=null && list1.size()>0){
				List<UserAvatar> userList = list1.stream().map(v -> new UserAvatar(v)).collect(Collectors.toList());
				departAndUserInfo.setUserList(userList);
			}

			LambdaQueryWrapper<SysDepart> query2 = new LambdaQueryWrapper<SysDepart>()
					.like(SysDepart::getDepartName, keyword);
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				query2.eq(SysDepart::getTenantId, tenantId);
			}
			List<SysDepart> list2 = sysDepartMapper.selectList(query2);
			if(list2!=null && list2.size()>0){
				List<DepartInfo> departList = new ArrayList<>();
				for(SysDepart depart: list2){
					List<String> orgName = new ArrayList<>();
					List<String> orgId = new ArrayList<>();
					getParentDepart(depart, orgName, orgId);
					DepartInfo departInfo = new DepartInfo();
					departInfo.setId(depart.getId());
					departInfo.setOrgId(orgId);
					departInfo.setOrgName(orgName);
					departList.add(departInfo);
				}
				departAndUserInfo.setDepartList(departList);
			}
		}
		return departAndUserInfo;
	}

	@Override
	public UpdateDepartInfo getUpdateDepartInfo(String departId) {
		SysDepart depart = sysDepartMapper.selectById(departId);
		if(depart!=null){
			UpdateDepartInfo info = new UpdateDepartInfo(depart);
			List<SysDepart> subList = sysDepartMapper.queryDeptByPid(departId);
			if(subList!=null && subList.size()>0){
				info.setHasSub(true);
			}
			//Lấy thông tin người phụ trách phòng ban
			LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<SysUser>()
					.eq(SysUser::getUserIdentity, 2)
					.like(SysUser::getDepartIds, depart.getId());
			List<SysUser> userList = this.baseMapper.selectList(query);
			if(userList!=null && userList.size()>0){
				List<String> idList = userList.stream().map(i -> i.getId()).collect(Collectors.toList());
				info.setChargePersonList(idList);
			}
			return info;
		}
		return null;
	}

	@Override
	public void doUpdateDepartInfo(UpdateDepartInfo info) {
		String departId = info.getDepartId();
		SysDepart depart = sysDepartMapper.selectById(departId);
		if(depart!=null){
			//Chỉnh sửa thông tin phòng ban-Tên cấp trên và phòng ban
			if(!depart.getParentId().equals(info.getParentId())){
				String pid = info.getParentId();
				SysDepart parentDepart = sysDepartMapper.selectById(pid);
				if(parentDepart!=null){
					String orgCode = getNextOrgCode(pid);
					depart.setOrgCode(orgCode);
					depart.setParentId(pid);
				}
			}
			depart.setDepartName(info.getDepartName());
			sysDepartMapper.updateById(depart);
			//Truy vấn trước người phụ trách của phòng ban này
			List<SysUser> departChargeUsers = queryDepartChargePersons(departId);
			List<String> departChargeUserIdList = departChargeUsers.stream().map(i -> i.getId()).collect(Collectors.toList());
			//Sửa người phụ trách phòng ban
			List<String> userIdList = info.getChargePersonList();
			if(userIdList!=null && userIdList.size()>0){
				for(String userId: userIdList){
					SysUser user = this.baseMapper.selectById(userId);
					if(user!=null){
						departChargeUserIdList.remove(user.getId());
						user.setUserIdentity(2);
						String departIds = user.getDepartIds();
						if(oConvertUtils.isEmpty(departIds)){
							user.setDepartIds(departId);
						}else{
							List<String> list = new ArrayList<String>(Arrays.asList(departIds.split(",")));
							if(list.indexOf(departId)>=0){
								continue;
							}else{
								list.add(departId);
								String newDepartIds = String.join(",", list);
								user.setDepartIds(newDepartIds);
							}
						}
						this.baseMapper.updateById(user);
					}
				}
			// Giải thích logic mã: Người phụ trách phòng ban không thể bị xóa------------
				this.removeDepartmentManager(departChargeUserIdList,departChargeUsers,departId);
			}else{
				if(CollectionUtil.isNotEmpty(departChargeUsers)){
					//Danh sách id người dùng mà frontend truyền sang rỗng, nghĩa là toàn bộ nhân viên của phòng ban phụ trách trong cơ sở dữ liệu đều cần xóa
					this.removeDepartmentManager(departChargeUserIdList,departChargeUsers,departId);
				}
				
			}
		}
	}

	private List<SysUser> queryDepartChargePersons(String departId){
    	List<SysUser> result = new ArrayList<>();
		// Giải thích logic mã: Người phụ trách phòng ban không thể bị xóa------------
		LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
		userQuery.like(SysUser::getDepartIds,departId);
		List<SysUser> userList = userMapper.selectList(userQuery);
		if(userList!=null && userList.size()>0){
			for(SysUser user: userList){
				Integer identity = user.getUserIdentity();
				String deps = user.getDepartIds();
				if(identity!=null && identity==2){
					if(oConvertUtils.isNotEmpty(deps)){
						if(deps.indexOf(departId)>=0){
							result.add(user);
						}
					}
				}
			}
		}
    	return result;
	}
	
	/**
	 * Thay đổi phòng ban cấp trên, sửa mã
	 * @param parentId
	 * @return
	 */
	private String getNextOrgCode(String parentId){
		JSONObject formData = new JSONObject();
		formData.put("parentId",parentId);
		String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
		return codeArray[0];
	}

	@Override
	public void changeDepartChargePerson(JSONObject json) {
		String userId = json.getString("userId");
		String departId = json.getString("departId");
		boolean status  = json.getBoolean("status");
		SysUser user = this.getById(userId);
		if(user!=null){
			String ids = user.getDepartIds();
			if(status==true){
				//Đặt người phụ trách phòng ban
				if(oConvertUtils.isEmpty(ids)){
					//Đặt làm cấp trên
					user.setUserIdentity(CommonConstant.USER_IDENTITY_2);
					user.setDepartIds(departId);
				}else{
					List<String> list = new ArrayList<String>(Arrays.asList(ids.split(",")));
					if(list.indexOf(departId)>=0){
						//Không làm gì cả
					}else{
						list.add(departId);
						String newIds = String.join(",", list);
						//Đặt làm cấp trên
						user.setUserIdentity(CommonConstant.USER_IDENTITY_2);
						user.setDepartIds(newIds);
					}
				}
			}else{
				// Hủy người phụ trách
				if(oConvertUtils.isNotEmpty(ids)){
					List<String> list = new ArrayList<String>();
					for(String temp: ids.split(",")){
						if(oConvertUtils.isEmpty(temp)){
							continue;
						}
						if(!temp.equals(departId)){
							list.add(temp);
						}
					}
					String newIds = "";
					if(list.size()>0){
						newIds = String.join(",", list);
					}else{
						//Khi phòng ban phụ trách trống, nghĩa là đã là người dùng thông thường
						user.setUserIdentity(CommonConstant.USER_IDENTITY_1);
					}
					user.setDepartIds(newIds);
				}
			}
			this.updateById(user);
		}
	}

	/**
	 * Tìm phòng ban cấp trên
	 * @param depart
	 * @param orgName
	 * @param orgId
	 */
	private void getParentDepart(SysDepart depart,List<String> orgName,List<String> orgId){
		String pid = depart.getParentId();
		orgName.add(0, depart.getDepartName());
		orgId.add(0, depart.getId());
		if(oConvertUtils.isNotEmpty(pid)){
			SysDepart temp = sysDepartMapper.selectById(pid);
			getParentDepart(temp, orgName, orgId);
		}
	}
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void editTenantUser(SysUser sysUser, String tenantId, String departs, String roles) {
		SysUser user = new SysUser();
		user.setWorkNo(sysUser.getWorkNo());
		user.setId(sysUser.getId());
		this.updateById(user);
		// Giải thích logic mã: 【QQYUN-5251】Nhân sự và phòng ban: không xóa được phòng ban------------
		if(oConvertUtils.isEmpty(departs)){
			//Xóa trực tiếp phòng ban tenant thuộc người dùng
			sysUserDepartMapper.deleteUserDepart(user.getId(),tenantId);
		}else{
			//Sửa phòng ban thuộc người dùng Tenant
			this.updateTenantDepart(user, tenantId, departs);
		}
		//Sửa chức vụ thuộc người dùng
		this.editUserPosition(sysUser.getId(),sysUser.getPost());
	}

	/**
	 * Chỉnh sửa trạng thái tài khoản
	 * @param id Tài khoản id
	 * @param status Trạng thái tài khoản
	 */
	@Override
	@CacheEvict(value={CacheConstant.SYS_USERS_CACHE}, allEntries=true)
	public void updateStatus(String id, String status) {
		userMapper.update(new SysUser().setStatus(Integer.parseInt(status)),
				new UpdateWrapper<SysUser>().lambda().eq(SysUser::getId,id));
	}

	/**
	 * Sửa phòng ban thuộc Tenant
	 * @param departs
	 */
	public void updateTenantDepart(SysUser user, String tenantId, String departs) {
		List<String> departList = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		if (oConvertUtils.isNotEmpty(departs)) {
			//Lấy phòng ban thuộc Tenant hiện tại id,theo giao diện
			departList = sysUserDepartMapper.getTenantDepart(Arrays.asList(departs.split(SymbolConstant.COMMA)), tenantId);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Thời gian truy vấn phòng ban người dùng：" + (endTime - startTime) + "ms");
		//Truy vấn các phòng ban mà phòng ban và người dùng thuộc tenant hiện tại đã liên kết
		List<SysUserDepart> userDepartList = sysUserDepartMapper.getTenantUserDepart(user.getId(), tenantId);
		if (userDepartList != null && userDepartList.size() > 0 && departList.size() > 0) {
			for (SysUserDepart depart : userDepartList) {
				//Sửa quan hệ người dùng - vai trò của phòng ban khi xóa phòng ban đã liên kết
				if (!departList.contains(depart.getDepId())) {
					List<SysDepartRole> sysDepartRoleList = sysDepartRoleMapper.selectList(
							new QueryWrapper<SysDepartRole>().lambda().eq(SysDepartRole::getDepartId, depart.getDepId()));
					List<String> roleIds = sysDepartRoleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
					if (roleIds.size() > 0) {
						departRoleUserMapper.delete(new QueryWrapper<SysDepartRoleUser>().lambda().eq(SysDepartRoleUser::getUserId, user.getId())
								.in(SysDepartRoleUser::getDroleId, roleIds));
					}
				}
			}
		}
		long endTime1 = System.currentTimeMillis();
		System.out.println("Thời gian sửa vai trò phòng ban：" + (endTime1 - startTime) + "ms");
		
		if (departList.size() > 0) {
			//Xóa phòng ban của người dùng
			sysUserDepartMapper.deleteUserDepart(user.getId(), tenantId);
			for (String departId : departList) {
				//Thêm phòng ban
				SysUserDepart userDepart = new SysUserDepart(user.getId(), departId);
				sysUserDepartMapper.insert(userDepart);
			}
		}
		long endTime2 = System.currentTimeMillis();
		System.out.println("Thời gian sửa phòng ban người dùng：" + (endTime2 - startTime) + "ms");
	}

	/**
	 * Lưu vị trí công việc của người dùng
	 *
	 * @param userId
	 * @param positionIds
	 */
	private void saveUserPosition(String userId, String positionIds) {
		if (oConvertUtils.isNotEmpty(positionIds)) {
			String[] positionIdArray = positionIds.split(SymbolConstant.COMMA);
			for (String postId : positionIdArray) {
				SysUserPosition userPosition = new SysUserPosition();
				userPosition.setUserId(userId);
				userPosition.setPositionId(postId);
				sysUserPositionMapper.insert(userPosition);
			}
		}
	}

	/**
	 * Chỉnh sửa chức vụ người dùng
	 *
	 * @param userId
	 * @param positionIds
	 */
	private void editUserPosition(String userId, String positionIds) {
		//Xóa trước
		LambdaQueryWrapper<SysUserPosition> query = new LambdaQueryWrapper<>();
		query.eq(SysUserPosition::getUserId, userId);
		sysUserPositionMapper.delete(query);
		//Dữ liệu mới thêm sau
		this.saveUserPosition(userId, positionIds);
	}

	/**
	 * Đặt vị trí người dùng id(Đã nối bằng dấu phẩy)
	 * @param sysUser
	 */
	private void userPositionId(SysUser sysUser) {
		if(null != sysUser){
			List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(sysUser.getId());
			sysUser.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
		}
	}

	/**
	 * Truy vấn ID phòng ban đăng nhập hiện tại của người dùng id
	 *
	 * @param orgCode
	 */
	private @Nullable String getDepartIdByOrCode(String orgCode) {
		if (oConvertUtils.isEmpty(orgCode)) {
			return null;
		}
		LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDepart::getOrgCode, orgCode);
		queryWrapper.select(SysDepart::getId);
		SysDepart depart = sysDepartMapper.selectOne(queryWrapper);
		if (depart == null || oConvertUtils.isEmpty(depart.getId())) {
			return null;
		}
		return depart.getId();
	}

	/**
	 * Truy vấn code vai trò của người dùng (nhiều giá trị phân tách bằng dấu phẩy)
	 *
	 * @param userId
	 */
	private @Nullable String getJoinRoleCodeByUserId(String userId) {
		if (oConvertUtils.isEmpty(userId)) {
			return null;
		}
		// Phán đoán có bật chế độ SaaS hay không, lọc theo id tenant
		Integer tenantId = null;
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			// Đã bật nhưng không có tenant ID, mặc định-1, để không truy vấn được bất kỳ dữ liệu nào
			tenantId = oConvertUtils.getInt(TenantContext.getTenant(), -1);
		}
		List<SysRole> roleList = sysRoleMapper.getRoleCodeListByUserId(userId, tenantId);
		if (CollectionUtils.isEmpty(roleList)) {
			return null;
		}
		return roleList.stream().map(SysRole::getRoleCode).collect(Collectors.joining(SymbolConstant.COMMA));
	}

	/**
	 * Gỡ bỏ người phụ trách phòng ban
	 * @param departChargeUserIdList
	 * @param departChargeUsers
	 * @param departId
	 */
	private void removeDepartmentManager(List<String> departChargeUserIdList,List<SysUser> departChargeUsers,String departId){
		//Gỡ bỏ người phụ trách phòng ban
		for(String chargeUserId: departChargeUserIdList){
			for(SysUser chargeUser: departChargeUsers){
				if(chargeUser.getId().equals(chargeUserId)){
					String departIds = chargeUser.getDepartIds();
					List<String> list = new ArrayList<String>(Arrays.asList(departIds.split(",")));
					list.remove(departId);
					String newDepartIds = String.join(",", list);
					chargeUser.setDepartIds(newDepartIds);
					this.baseMapper.updateById(chargeUser);
					break;
				}
			}
		}
	}

	//======================================= begin Xuất danh sách người dùng của người dùng và phòng ban =========================================
	@Override
	public ModelAndView exportAppUser(HttpServletRequest request) {
		Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant());
		// Step.1 Ghép điều kiện truy vấn, xuất dữ liệu id phòng ban đã chọn
		String departIds = request.getParameter("departIds");
		List<String> list = new ArrayList<>();
		if(oConvertUtils.isNotEmpty(departIds)){
			list = Arrays.asList(departIds.split(SymbolConstant.COMMA));
		}
		//Truy vấn dữ liệu người dùng
		List<SysUser> userList = userMapper.getUserByDepartsTenantId(list, tenantId);
        //Lấy tên phòng ban
        List<SysUserDepVo> userDepVos = new ArrayList<>();
        if(CollectionUtil.isNotEmpty(userList)){
            userDepVos = sysDepartMapper.getUserDepartByTenantUserId(userList, tenantId);
        }
		//Lấy chức vụ
		List<SysUserPositionVo> positionVos = sysUserPositionMapper.getPositionIdByUsersTenantId(userList, tenantId);
		// step2 Phân loại theo id người dùng
		//Lặp dữ liệu người dùng để tổng hợp và xuất dữ liệu
		List<AppExportUserVo> exportUserVoList = new ArrayList<>();
		for (SysUser sysUser : userList) {
			AppExportUserVo exportUserVo = new AppExportUserVo();
			BeanUtils.copyProperties(sysUser, exportUserVo);
            // Giải thích logic mã: 【QQYUN-10926] Quản lý tổ chức — khi xuất người dùng, phòng ban không xuất quan hệ cấp trên - cấp dưới---
            Map<String, String> departMap = this.getDepartNamesAndCategory(userDepVos, sysUser);
            String departNames = departMap.get("departNames");
            exportUserVo.setDepart(departNames.toString());
			String posNames = positionVos.stream().filter(item -> item.getUserId().equals(sysUser.getId())).map(SysUserPositionVo::getName).collect(Collectors.joining(SymbolConstant.SEMICOLON));
			exportUserVo.setPosition(posNames);
			exportUserVoList.add(exportUserVo);
		}
		//step3 Đóng gói tham số xuất excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		//Tên file xuất
		mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách người dùng");
		mv.addObject(NormalExcelConstants.CLASS, AppExportUserVo.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		ExportParams exportParams = new ExportParams("Quy tắc nhập:\n" +
				"1, khi tồn tại mã số người dùng, dữ liệu sẽ khớp theo mã số người dùng; sau khi khớp thành công chỉ cập nhật chức vụ và mã nhân viên;\n" +
				"2, khi không tồn tại mã số người dùng, hỗ trợ import theo số điện thoại, email, họ tên, phòng ban, chức vụ, mã nhân viên,trong đó số điện thoại di động là bắt buộc;\n" +
				"3, các phòng ban cấp trên/cấp dưới dùng ký tự tiếng Anh / nối bằng /, ví dụ: Phòng Tài chính/Phòng Tài chính 1, nhiều phòng ban hoặc chức vụ dùng ký tự tiếng Anh ; Tiến hành kết nối,như Phòng Tài chính;Phòng Nghiên cứu và Phát triển", "Người xuất:" + user.getRealname(), "Thông tin xuất");
		mv.addObject(NormalExcelConstants.PARAMS, exportParams);
		mv.addObject(NormalExcelConstants.DATA_LIST, exportUserVoList);
		return mv;
	}

    /**
     * Lấy tên phòng ban và loại phòng ban
     * for：【QQYUN-10926] Quản lý tổ chức — khi xuất người dùng, phòng ban không xuất quan hệ cấp trên - cấp dưới
     *
     * @param userDepVos
     * @param sysUser
     * @return
     */
    private Map<String,String> getDepartNamesAndCategory(List<SysUserDepVo> userDepVos, SysUser sysUser) {
        List<SysUserDepVo> SysUserDepVoList = userDepVos.stream().filter(item -> item.getUserId().equals(sysUser.getId()))
                .map(item -> {
                    SysUserDepVo userDepVo = new SysUserDepVo();
                    userDepVo.setUserId(item.getUserId());
                    userDepVo.setDeptId(item.getDeptId());
                    userDepVo.setDepartName(item.getDepartName());
                    userDepVo.setParentId(item.getParentId());
                    userDepVo.setOrgCategory(DepartCategoryEnum.getNameByValue(item.getOrgCategory()));
                    return userDepVo;
                }).collect(Collectors.toList());
        //Lặp lại SysUserDepVoList,Trong trường hợp tồn tại id cấp cha, cần truy vấn tên phòng ban của id cấp cha
        StringBuilder departNames = new StringBuilder();
        StringBuilder departOrgCategorys = new StringBuilder();
        for (SysUserDepVo sysUserDepVo : SysUserDepVoList) {
            if(oConvertUtils.isEmpty(sysUserDepVo.getDepartName())){
                continue;
            }
            //Dùng để truy vấn tên phòng ban cấp cha
            List<String> departNameList = new LinkedList<>();
            //Dùng để truy vấn loại phòng ban cấp cha
            List<String> departOrgCategoryList = new LinkedList<>();
            departNameList.add(sysUserDepVo.getDepartName());
            departOrgCategoryList.add(sysUserDepVo.getOrgCategory());
            if (StringUtils.isNotEmpty(sysUserDepVo.getParentId())) {
                //Truy vấn đệ quy tên phòng ban
                this.getDepartNameByParentId(sysUserDepVo.getParentId(), departNameList, departOrgCategoryList);
            }
            Collections.reverse(departNameList);
            Collections.reverse(departOrgCategoryList);
            String departName = departNameList.stream().collect(Collectors.joining(SymbolConstant.SINGLE_SLASH));
            if (StringUtils.isNotEmpty(departNames.toString())) {
                departNames.append(SymbolConstant.SEMICOLON);
            }
            departNames.append(departName);
            String orgCatrgory = departOrgCategoryList.stream().collect(Collectors.joining(SymbolConstant.SINGLE_SLASH));
            if (StringUtils.isNotEmpty(departOrgCategorys.toString())) {
                departOrgCategorys.append(SymbolConstant.SEMICOLON);
            }
            departOrgCategorys.append(orgCatrgory);
        }
        // Giải thích logic mã: 【QQYUN-13617] Khi nhập, cấp bậc thêm phòng ban không đúng---
        Map<String,String> map = new HashMap<>();
        map.put("departNames", departNames.toString());
        map.put("departOrgCategorys",departOrgCategorys.toString());
        return map;
    }

    /**
     * Truy vấn tên phòng ban và loại phòng ban của cấp trên theo id cấp trên
     * for：【QQYUN-10926] Quản lý tổ chức — khi xuất người dùng, phòng ban không xuất quan hệ cấp trên - cấp dưới
     *
     * @param parentId
     * @param departNameList
     * @param departOrgCategoryList
     */
    private void getDepartNameByParentId(String parentId, List<String> departNameList, List<String> departOrgCategoryList) {
        SysDepart parentDepartId = sysDepartMapper.getDepartById(parentId);
        if (null != parentDepartId) {
            departNameList.add(parentDepartId.getDepartName());
            departOrgCategoryList.add(DepartCategoryEnum.getNameByValue(parentDepartId.getOrgCategory()));
            if (StringUtils.isNotEmpty(parentDepartId.getParentId())) {
                this.getDepartNameByParentId(parentDepartId.getParentId(), departNameList, departOrgCategoryList);
            }
        }
    }

    //======================================= end Xuất danh sách người dùng của người dùng và phòng ban =========================================

	//======================================= begin Nhập danh sách người dùng của người dùng và phòng ban =========================================
	@Override
	public Result<?> importAppUser(HttpServletRequest request) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant());
		SysTenant sysTenant = sysTenantMapper.selectById(tenantId);
		// Thông tin lỗi
		List<String> errorMessage = new ArrayList<>();
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			//Lưu trữ vị trí công việc map;key là tên, value là id vị trí. Tránh import và truy vấn nhiều lần
			Map<String, String> positionMap = new HashMap<>();
			//Lưu trữ phòng ban map;key là tên, value là đối tượng SysDepart. Tránh import và truy vấn nhiều lần
			Map<String, SysDepart> departMap = new HashMap<>();
			try {
				List<AppExportUserVo> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), AppExportUserVo.class, params);
				for (int i = 0; i < listSysUsers.size(); i++) {
					//Ghi lại hiện tại là dòng thứ mấy
					int lineNumber = i + 1;
					//Ghi lại là chỉnh sửa hay thêm mới
					boolean isEdit = false;
					AppExportUserVo sysUserExcel = listSysUsers.get(i);
					String id = sysUserExcel.getId();
					String workNo = sysUserExcel.getWorkNo();
					String email = sysUserExcel.getEmail();
					String phone = sysUserExcel.getPhone();
					String realname = sysUserExcel.getRealname();
					String depart = sysUserExcel.getDepart();
					String position = sysUserExcel.getPosition();
					SysUser sysUser = new SysUser();
					//Phán đoán id có tồn tại hay không, nếu tồn tại thì là cập nhật
					if (oConvertUtils.isNotEmpty(id)) {
						SysUser user = userMapper.selectById(id);
						if (null == user) {
							errorLines++;
							errorMessage.add(" " + lineNumber + " Dòng: người dùng không tồn tại, vui lòng kiểm tra xem mã số đã bị sửa hay chưa, bỏ qua import.");
							continue;
						}
						isEdit = true;
						sysUser.setId(id);
					} else {
						//Xử lý việc thuê bao đã tồn tại hay chưa, người dùng đã tồn tại hay chưa; người dùng đã tồn tại thì cập nhật trực tiếp
						isEdit = false;
					}
					if (oConvertUtils.isNotEmpty(workNo)) {
						sysUser.setWorkNo(workNo);
					}
					try {
						if (isEdit) {
							userMapper.updateById(sysUser);
						} else {
							if (oConvertUtils.isEmpty(phone)) {
								errorMessage.add(" " + lineNumber + " Dòng: số điện thoại trống, bỏ qua khi nhập.");
								errorLines++;
								continue;
							}
							SysUser userByPhone = userMapper.getUserByPhone(phone);
							if (null != userByPhone) {
								//Kiểm tra xem đã tồn tại trong thuê bao này hay chưa; nếu đã tồn tại thì cấm import, nếu không thì cập nhật trực tiếp
								Integer tenantCount = userTenantMapper.userTenantIzExist(userByPhone.getId(), tenantId);
								if (tenantCount > 0) {
									errorMessage.add(" " + lineNumber + "  dòng: Thành viên đã tồn tại trong tổ chức này, nếu không có trong danh sách, vui lòng xác nhận thành viên đó có đang trong quá trình xét duyệt hay đã nghỉ việc không, bỏ qua khi import.");
									errorLines++;
									continue;
								}
								sysUser.setId(userByPhone.getId());
								userMapper.updateById(sysUser);
								this.addUserTenant(sysUser.getId(), tenantId, userByPhone.getUsername(),sysTenant.getName());
							} else {
								// Mật khẩu mặc định là "số nhà của tenant+Số điện thoại”
								String password = sysTenant.getHouseNumber()+phone;
								String salt = oConvertUtils.randomGen(8);
								sysUser.setSalt(salt);
								// Mã hóa mật khẩu kèm salt
								String passwordEncode = PasswordUtil.encrypt(phone, password, salt);
								sysUser.setPassword(passwordEncode);
								sysUser.setUsername(phone);
								sysUser.setRealname(oConvertUtils.getString(realname,phone));
								sysUser.setEmail(email);
								sysUser.setPhone(phone);
								sysUser.setStatus(CommonConstant.DEL_FLAG_1);
								sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
								sysUser.setCreateTime(new Date());
								userMapper.insert(sysUser);
								this.addUserTenant(sysUser.getId(), tenantId, sysUser.getUsername(),sysTenant.getName());
							}
						}
						//Thêm mới hoặc chỉnh sửa vị trí công việc
						if (oConvertUtils.isNotEmpty(position)) {
							this.addOrEditPosition(sysUser.getId(), position, isEdit, tenantId, positionMap);
						}
						//Chỉ khi thêm mới mới được thêm phòng ban
						if (!isEdit) {
							//Thêm mới hoặc chỉnh sửa phòng ban
							this.addOrEditDepart(sysUser.getId(), depart, tenantId, departMap);
						}
						successLines++;
					} catch (Exception e) {
						errorLines++;
						String message = e.getMessage().toLowerCase();

						// Xác định thông tin lỗi qua tên chỉ mục
						if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
							errorMessage.add(" " + lineNumber + " Dòng: Tên người dùng đã tồn tại, bỏ qua nhập.");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
							errorMessage.add(" " + lineNumber + " Dòng: mã nhân viên đã tồn tại, bỏ qua khi nhập.");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
							errorMessage.add(" " + lineNumber + " Dòng: Số điện thoại đã tồn tại, bỏ qua nhập.");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
							errorMessage.add(" " + lineNumber + " Dòng: Email đã tồn tại, bỏ qua nhập.");
						} else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
							errorMessage.add(" " + lineNumber + " Dòng: Vi phạm ràng buộc duy nhất của bảng.");
						} else {
							errorMessage.add(" " + lineNumber + " Dòng: Lỗi không xác định, bỏ qua việc nhập");
							log.error(e.getMessage(), e);
						}
					}
				}
			} catch (Exception e) {
				errorMessage.add("Đã xảy ra ngoại lệ:" + e.getMessage());
				log.error(e.getMessage(), e);
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		try {
			return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Thêm mới hoặc chỉnh sửa chức vụ
	 *
	 * @param userId      Người dùng id
	 * @param position    Tên chức vụ đã/đã ghép nối
	 * @param isEdit      Thêm mới hoặc chỉnh sửa
	 * @param positionMap Map vị trí có key là name, value là vị trí id
	 */
	private void addOrEditPosition(String userId, String position, Boolean isEdit, Integer tenantId, Map<String, String> positionMap) {
		Page<SysPosition> page = new Page<>(1, 1);
		String[] positions = position.split(SymbolConstant.SEMICOLON);
		List<String> positionList = Arrays.asList(positions);
		positionList = positionList.stream().distinct().collect(Collectors.toList());
		//Xóa vị trí thuộc thuê bao hiện tại, dựa theo tên vị trí, id thuê bao, người dùng id
		sysUserPositionMapper.deleteUserPosByNameAndTenantId(positionList, tenantId, userId);
		//Lặp qua dữ liệu cần thêm hoặc sửa
		for (String pos : positionList) {
			String posId = "";
			if (positionMap.containsKey(pos)) {
				posId = positionMap.get(pos);
			} else {
				List<String> namePage = sysPositionMapper.getPositionIdByName(pos, tenantId, page);
				if (CollectionUtil.isNotEmpty(namePage)) {
					posId = namePage.get(0);
					positionMap.put(pos, posId);
				}
			}

			//Nếu id chức vụ không trống thì thêm mới trực tiếp
			if (oConvertUtils.isNotEmpty(posId)) {
				this.addSysUserPosition(userId, posId);
				continue;
			}

			//Chỉ khi không phải trường hợp chỉnh sửa thì chức vụ mới được thêm
			if (!isEdit) {
				//Thêm chức vụ và quan hệ người dùng-chức vụ
				SysPosition sysPosition = new SysPosition();
				sysPosition.setName(pos);
				sysPosition.setCode(RandomUtil.randomString(10));
				sysPosition.setTenantId(tenantId);
				sysPositionMapper.insert(sysPosition);
				positionMap.put(pos, sysPosition.getId());
				this.addSysUserPosition(userId, sysPosition.getId());
			}
		}
	}

	/**
	 * Thêm chức vụ cho người dùng
	 */
	private void addSysUserPosition(String userId, String positionId) {
		Long count = sysUserPositionMapper.getUserPositionCount(userId, positionId);
		if(count == 0){
			SysUserPosition userPosition = new SysUserPosition();
			userPosition.setUserId(userId);
			userPosition.setPositionId(positionId);
			sysUserPositionMapper.insert(userPosition);
		}
	}

	/**
	 * Thêm mới hoặc chỉnh sửa phòng ban
	 *
	 * @param userId    Người dùng id
	 * @param depart    Tên phòng ban
	 * @param tenantId  Tenant id
	 * @param departMap Lưu trữ phòng ban map;key là tên, value là đối tượng SysDepart.
	 */
	private void addOrEditDepart(String userId, String depart, Integer tenantId, Map<String, SysDepart> departMap) {
		//Thiết lập quan hệ liên kết giữa phòng ban và thông tin người dùng hàng loạt
		if (StringUtils.isNotEmpty(depart)) {
			Page<SysDepart> page = new Page<>(1, 1);
			//Tách nhiều phòng ban ra
			String[] departNames = depart.split(SymbolConstant.SEMICOLON);
			List<String> departNameList = Arrays.asList(departNames);
			departNameList = departNameList.stream().distinct().collect(Collectors.toList());
			for (String departName : departNameList) {
                //Phòng ban id
                String parentId = "";
				String[] names = departName.split(SymbolConstant.SINGLE_SLASH);
				//Nối tên phòng ban
				String nameStr = "";
				for (int i = 0; i < names.length; i++) {
					String name = names[i];
					//đã ghép nối name
					if (oConvertUtils.isNotEmpty(nameStr)) {
						nameStr = nameStr + SymbolConstant.SINGLE_SLASH + name;
					} else {
						nameStr = name;
					}
					SysDepart sysDepart = null;
					//Kiểm tra trong map có tồn tại tên phòng ban đó không
					if (departMap.containsKey(nameStr)) {
						sysDepart = departMap.get(nameStr);
					} else {
						//Không tồn tại cần đi truy vấn
						List<SysDepart> departPageByName = sysDepartMapper.getDepartPageByName(page, name, tenantId, parentId);
						//Phòng ban trống, cần thêm mới phòng ban
						if (CollectionUtil.isEmpty(departPageByName)) {
							JSONObject formData = new JSONObject();
							formData.put("parentId", parentId);
							String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
							sysDepart = new SysDepart();
							sysDepart.setParentId(parentId);
							sysDepart.setOrgCode(codeArray[0]);
							sysDepart.setOrgType(codeArray[1]);
							sysDepart.setTenantId(tenantId);
							sysDepart.setDepartName(name);
							sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
							sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
							sysDepart.setStatus(CommonConstant.STATUS_1);
							sysDepartMapper.insert(sysDepart);
						} else {
							sysDepart = departPageByName.get(0);
						}
						//Nếu id cấp trên không rỗng thì đổi phòng ban cấp trên thành không phải nút lá
						if (oConvertUtils.isNotEmpty(parentId)) {
							sysDepartMapper.setMainLeaf(parentId, CommonConstant.NOT_LEAF);
						}
						parentId = sysDepart.getId();
						departMap.put(nameStr, sysDepart);
					}
					//Vị trí cuối cùng thêm bảng quan hệ người dùng phòng ban
					if (i == names.length - 1) {
						Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId, sysDepart.getId());
						if(count == 0){
							SysUserDepart userDepart = new SysUserDepart(userId, sysDepart.getId());
							sysUserDepartMapper.insert(userDepart);
						}
					}
				}
			}
		}

	}

	/**
	 * Thêm tenant cho người dùng
	 *
	 * @param userId
	 * @param tenantId
	 * @param invitedUsername Tài khoản của người được mời
	 * @param tenantName Tên tenant 
	 */
	private void addUserTenant(String userId, Integer tenantId, String invitedUsername, String tenantName) {
		SysUserTenant userTenant = new SysUserTenant();
		userTenant.setTenantId(tenantId);
		userTenant.setUserId(userId);
		userTenant.setStatus(CommonConstant.USER_TENANT_INVITE);
		userTenantMapper.insert(userTenant);
		//Gửi thông báo tin nhắn hệ thống
		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		MessageDTO messageDTO = new MessageDTO();
		String title = sysUser.getRealname() + " mời bạn tham gia " + tenantName + "。";
		messageDTO.setTitle(title);
		Map<String, Object> data = new HashMap<>();
		// Giải thích logic mã: 【QQYUN-8425] Sau khi nhập người dùng thành công, nhắc nhở bằng tin nhắn và chuyển đến trang đồng ý---
		data.put(CommonConstant.NOTICE_MSG_BUS_TYPE,SysAnnmentTypeEnum.TENANT_INVITE.getType());
		messageDTO.setData(data);
		messageDTO.setContent(title);
		messageDTO.setToUser(invitedUsername);
		messageDTO.setFromUser("system");
		systemSendMsgHandle.sendMessage(messageDTO);
	}
	//======================================= end Nhập danh sách người dùng của người dùng và phòng ban =========================================
	
	@Override
	public void checkUserAdminRejectDel(String userIds) {
		LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
		query.in(SysUser::getId,Arrays.asList(userIds.split(SymbolConstant.COMMA)));
		query.eq(SysUser::getUsername,"admin");
		Long adminRoleCount = this.baseMapper.selectCount(query);
		//Lớn hơn 0 nghĩa là tồn tại người dùng quản trị viên, không cho phép xóa
		if(adminRoleCount>0){
			throw new JeecgBootException("admin người dùng, không được phép xóa!");
		}
	}

	@Override
	public void changePhone(JSONObject json, String username) {
		String smscode = json.getString("smscode");
		String phone = json.getString("phone");
		String type = json.getString("type");
		if(oConvertUtils.isEmpty(phone)){
			throw new JeecgBootException("Vui lòng điền số điện thoại gốc!");
		}
		if(oConvertUtils.isEmpty(smscode)){
			throw new JeecgBootException("Vui lòng nhập mã xác minh!");
		}
		//step1 Xác minh số điện thoại gốc có khớp với người dùng hiện tại không
		SysUser sysUser = userMapper.getUserByNameAndPhone(phone,username);
		if (null == sysUser){
			throw new JeecgBootException("Số điện thoại gốc không khớp, không thể thay đổi mật khẩu!");
		}
		//step2 Dựa theo loại để xác định là xác minh mã của số điện thoại cũ hay mã của số điện thoại mới
		//Xác minh số điện thoại gốc
		if(CommonConstant.VERIFY_ORIGINAL_PHONE.equals(type)){
			this.verifyPhone(phone, smscode);
		}else if(CommonConstant.UPDATE_PHONE.equals(type)){
			//Thay đổi số điện thoại
			String newPhone = json.getString("newPhone");
			//Cần xác minh số điện thoại mới và số điện thoại cũ có giống nhau không, nếu giống nhau thì không cho sửa
			if(newPhone.equals(phone)){
				throw new JeecgBootException("Số điện thoại mới trùng với số điện thoại gốc, không thể sửa!");
			}
			this.verifyPhone(newPhone, smscode);
			//step3 Sau khi mã xác minh của số điện thoại mới được xác thực thành công thì có thể thay đổi số điện thoại
			sysUser.setPhone(newPhone);
			userMapper.updateById(sysUser);
		}
	}
	
	/**
	 * Xác minh số điện thoại
	 *
	 * @param phone
	 * @param smsCode
	 * @return
	 */
	public void verifyPhone(String phone, String smsCode){
		String phoneKey = CommonConstant.CHANGE_PHONE_REDIS_KEY_PRE + phone;
		Object phoneCode = redisUtil.get(phoneKey);
		if(null == phoneCode){
			throw new JeecgBootException("Mã xác minh đã hết hạn, vui lòng gửi lại mã xác minh!");
		}
		if(!smsCode.equals(phoneCode.toString())) {
			throw new JeecgBootException("Mã xác minh SMS không khớp!");
		}
		//Sau khi xác minh xong thì xóa mã xác minh điện thoại
		redisUtil.removeAll(phoneKey);
	}
	
	@Override
	public void sendChangePhoneSms(JSONObject jsonObject, String username, String ipAddress) {
		String type = jsonObject.getString("type");
		String phone = jsonObject.getString("phone");
		if(oConvertUtils.isEmpty(phone)){
			throw new JeecgBootException("Vui lòng nhập số điện thoại!");
		}
		//step1 Dựa theo loại để xác định là gửi mã xác minh của số điện thoại cũ hay số điện thoại mới
		if(CommonConstant.VERIFY_ORIGINAL_PHONE.equals(type)){
			//step2 Mã xác thực của số điện thoại cũ cần kiểm tra xem số điện thoại có khớp hay không
			SysUser sysUser = userMapper.getUserByNameAndPhone(phone, username);
			if(null == sysUser){
				throw new JeecgBootException("Số điện thoại cũ không khớp, không thể thay đổi số điện thoại!");
			}
		}else if(CommonConstant.UPDATE_PHONE.equals(type)){
			//step3 Số điện thoại mới cần kiểm tra xem số điện thoại đã được đăng ký hay chưa
			SysUser userByPhone = userMapper.getUserByPhone(phone);
			if(null != userByPhone){
				throw new JeecgBootException("Số điện thoại đã được đăng ký, vui lòng thử số điện thoại khác!");
			}
		}
		//step4 Gửi mã xác minh SMS
		String redisKey = CommonConstant.CHANGE_PHONE_REDIS_KEY_PRE+phone;
		this.sendPhoneSms(phone, ipAddress,redisKey);
	}

	@Override
	public void sendLogOffPhoneSms(JSONObject jsonObject, String username, String ipAddress) {
		String phone = jsonObject.getString("phone");
		//Truy vấn số điện thoại trong cơ sở dữ liệu theo tên người dùng
		SysUser userByNameAndPhone = userMapper.getUserByNameAndPhone(phone, username);
		if (null == userByNameAndPhone) {
			throw new JeecgBootException("Số điện thoại của người dùng hiện tại không khớp, không thể sửa!");
		}
		String code = CommonConstant.LOG_OFF_PHONE_REDIS_KEY_PRE + phone;
		this.sendPhoneSms(phone, ipAddress, code);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void userLogOff(JSONObject jsonObject, String username) {
		String phone = jsonObject.getString("phone");
		String smsCode = jsonObject.getString("smscode");
		//Truy vấn số điện thoại trong cơ sở dữ liệu theo tên người dùng
		SysUser userByNameAndPhone = userMapper.getUserByNameAndPhone(phone, username);
		if (null == userByNameAndPhone) {
			throw new JeecgBootException("Số điện thoại của người dùng hiện tại không khớp, không thể hủy tài khoản!");
		}
		String code = CommonConstant.LOG_OFF_PHONE_REDIS_KEY_PRE + phone;
		Object redisSmdCode = redisUtil.get(code);
		if (null == redisSmdCode) {
			throw new JeecgBootException("Mã xác minh đã hết hạn, không thể hủy tài khoản!");
		}
		if (!redisSmdCode.toString().equals(smsCode)) {
			throw new JeecgBootException("Mã xác minh không khớp, không thể hủy tài khoản!");
		}
		this.deleteUser(userByNameAndPhone.getId());
		redisUtil.removeAll(code);
		redisUtil.removeAll(CacheConstant.SYS_USERS_CACHE + phone);
	}

	/**
	 * Gửi mã xác minh SMS
	 * @param phone
	 */
	private void sendPhoneSms(String phone, String clientIp,String redisKey) {
		Object object = redisUtil.get(redisKey);

		if (object != null) {
			throw new JeecgBootException("Mã xác minh vẫn còn hiệu lực trong 10 phút!");
		}

		//Thêm check để ngăn chặn việc gửi SMS ác ý
		if(!DySmsLimit.canSendSms(clientIp)){
			log.warn("--------[Cảnh báo] IP Địa chỉ:{}, Yêu cầu giao diện SMS quá nhiều-------", clientIp);
			throw new JeecgBootException("Yêu cầu giao diện SMS quá nhiều, vui lòng thử lại sau!", CommonConstant.PHONE_SMS_FAIL_CODE);
		}
		//update-begin---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn theo số điện thoại-----------
		if(!DySmsLimit.canSendSmsToPhone(phone)){
			throw new JeecgBootException("Gửi SMS quá thường xuyên, vui lòng thử lại sau!", CommonConstant.PHONE_SMS_FAIL_CODE);
		}
		//update-end---author:scott ---date:20261013  for: 【Bảo mật GHSA-5qr3-c98m-9767】Thêm giới hạn theo số điện thoại-----------
		
		//Số ngẫu nhiên
		String captcha = RandomUtil.randomNumbers(6);
		JSONObject obj = new JSONObject();
		obj.put("code", captcha);
		try {
			boolean sendSmsSuccess = DySmsHelper.sendSms(phone, obj, DySmsEnum.LOGIN_TEMPLATE_CODE);
			if(!sendSmsSuccess){
				throw new JeecgBootException("Gửi mã xác minh SMS thất bại,Vui lòng thử lại sau!");
			}
			//Mã xác minh có hiệu lực trong 10 phút
			redisUtil.set(redisKey, captcha, 600);
		} catch (ClientException e) {
			log.error(e.getMessage(),e);
			throw new JeecgBootException("Giao diện SMS chưa được cấu hình, vui lòng liên hệ quản trị viên!");
		}
	}

    //================================================= begin Nhập/xuất phòng ban low-code ================================================================
    @Override
    public List<SysUserExportVo> getDepartAndRoleExportMsg(List<SysUser> userList) {
        List<SysUserExportVo> list = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userList)) {
            //Lấy phòng ban
            List<SysUserDepVo> userDepVos = sysDepartMapper.getUserDepartByUserId(userList);
            //Lấy vai trò
            List<SysUserPositionVo> sysRoles = sysRoleMapper.getUserRoleByUserId(userList);
            //Map lưu tên vị trí, key: id của vị trí chính id value: Tên cấp bậc
            Map<String, String> postNameMap = new HashMap<>();
            //Lắp ráp dữ liệu rồi trả về
            for (SysUser sysUser : userList) {
                SysUserExportVo userExportVo = new SysUserExportVo();
                BeanUtils.copyProperties(sysUser, userExportVo);
                // Giải thích logic mã: 【QQYUN-13617] Khi nhập, cấp bậc thêm phòng ban không đúng---
                Map<String, String> departMap = this.getDepartNamesAndCategory(userDepVos, sysUser);
                String departNames = departMap.get("departNames");
                userExportVo.setDepartNames(departNames);
                userExportVo.setOrgCategorys(departMap.get("departOrgCategorys"));
                String departIds = sysUser.getDepartIds();
                if (oConvertUtils.isNotEmpty(departIds)) {
                    List<SysUserDepVo> depVoList = sysDepartMapper.getDepartByIds(Arrays.asList(departIds.split(",")));
                    Map<String, String> departMaps = this.getDepartNamesAndCategory(userDepVos, sysUser);
                    userExportVo.setDepartIds(departMaps.get("departNames"));
                }
                String posNames = sysRoles.stream().filter(item -> item.getUserId().equals(sysUser.getId())).map(SysUserPositionVo::getName).collect(Collectors.joining(SymbolConstant.SEMICOLON));
                userExportVo.setRoleNames(posNames);
                if (null != sysUser.getMainDepPostId()) {
                    String postName = "";
                    if (null != postNameMap && postNameMap.containsKey(sysUser.getMainDepPostId())) {
                        postName = postNameMap.get(sysUser.getMainDepPostId());
                    } else {
                        postName = sysDepartMapper.getPostNameByPostId(sysUser.getMainDepPostId());
                    }
                    userExportVo.setPostName(postName);
                    postNameMap.put(sysUser.getMainDepPostId(), postName);
                }
                // Giải thích logic mã: Chuyển đổi vị trí kiêm nhiệm sang dạng bảng trung gian---
                List<String> depPost = depPostMapper.getDepPostByUserId(sysUser.getId());
                if(CollectionUtil.isNotEmpty(depPost)){
                    userExportVo.setOtherDepPostId(String.join(SymbolConstant.COMMA, depPost));
                }
                list.add(userExportVo);
            }
        }
        return list;
    }

    @Override
    public Result<?> importSysUser(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        String fileKey = multipartRequest.getParameter("fileKey");
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        // Thông tin lỗi
        List<String> errorMessage = new ArrayList<>();
        int successLines = 0, errorLines = 0;
        //Lưu trữ phòng ban map;key là tên, value là đối tượng SysDepart. Tránh import và truy vấn nhiều lần
        Map<String, SysDepart> departMap = new HashMap<>();
        //Cấp bậc map key: Tên cấp bậc value: Cấp bậc id
        Map<String, String> positionMap = new HashMap<>();
        //map vị trí key: tên vị trí + value id phòng ban: vị trí (id phòng ban)
        Map<String,String> postMap = new HashMap<>();
        String tenantId = TokenUtils.getTenantIdByRequest(request);
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();// Lấy đối tượng tệp tải lên
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<SysUserImportVo> listSysUsers = ExcelImportUtil.importExcel(file.getInputStream(), SysUserImportVo.class, params);
                ImportSysUserCache.setImportSysUserMap(fileKey,0,listSysUsers.size(),"user");
                for (int i = 0; i < listSysUsers.size(); i++) {
                    SysUserImportVo sysUserExcel = listSysUsers.get(i);
                    SysUser sysUser = new SysUser();
                    BeanUtils.copyProperties(sysUserExcel, sysUser);
                    if (oConvertUtils.isEmpty(sysUser.getUsername())) {
                        errorLines += 1;
                        int lineNumber = i + 1;
                        errorMessage.add(" " + lineNumber + " Dòng: tài khoản người dùng trống, bỏ qua khi nhập.");
                        continue;
                    }
                    try {
                        String username = sysUser.getUsername();
                        //Theo tên người dùng, nếu trống thì thêm người dùng
                        SysUser userByName = userMapper.getUserByName(username);
                        if (null != userByName) {
                            errorLines += 1;
                            int lineNumber = i + 1;
                            errorMessage.add(" " + lineNumber + " Dòng: Tên người dùng đã tồn tại, bỏ qua nhập.");
                            continue;
                        } else {
                            // Mật khẩu mặc định là “123456”
                            sysUser.setPassword(PasswordConstant.DEFAULT_PASSWORD);
                            // Mã hóa mật khẩu kèm salt
                            String salt = oConvertUtils.randomGen(8);
                            sysUser.setSalt(salt);
                            String passwordEncode = PasswordUtil.encrypt(sysUserExcel.getUsername(), sysUser.getPassword(), salt);
                            sysUser.setPassword(passwordEncode);
                            sysUser.setActivitiSync(CommonConstant.ACT_SYNC_1);
                            if(null == sysUser.getDelFlag()){
                                sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
                            }
                            if(null == sysUser.getStatus()){
                                sysUser.setStatus(CommonConstant.STATUS_1_INT);
                            }
							if(null == sysUser.getSort()){
								sysUser.setSort(CommonConstant.DEFAULT_USER_SORT);
							}
							//update-begin---author:wangshuai---date:20260805---for:【LHZP-1185】Nhập người dùng: ẩn thông tin liên hệ mặc định nên là không---
							if (oConvertUtils.isEmpty(sysUser.getIzHideContact())) {
								sysUser.setIzHideContact(CommonConstant.STATUS_0);
							}
							//update-end---author:wangshuai---date:20260805---for:【LHZP-1185】Nhập người dùng: ẩn thông tin liên hệ mặc định nên là không---
                            this.save(sysUser);
                        }
                        //Thêm phòng ban
                        String departNames = sysUserExcel.getDepartNames();
                        String orgCategorys = sysUserExcel.getOrgCategorys();
                        //Thêm mới hoặc chỉnh sửa phòng ban
                        Integer tenantIdInt = 0;
                        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
                            tenantIdInt = oConvertUtils.getInt(tenantId, 0);
                        }
                        this.lowAddOrEditDepart(sysUser.getId(), departNames, tenantIdInt, departMap, orgCategorys, sysUserExcel.getPostName(), sysUserExcel.getMainDepPostId(),postMap,positionMap, sysUserExcel.getOtherDepPostId());
                        //Thêm mới hoặc chỉnh sửa vai trò
                        String roleNames = sysUserExcel.getRoleNames();
                        this.saveOrEditRole(sysUser.getId(), roleNames, tenantIdInt);
                        //Thêm mới hoặc chỉnh sửa vị trí công việc
                       /* String position = sysUserExcel.getPost();
                        if (oConvertUtils.isNotEmpty(position)) {
                            this.addOrEditPosition(sysUser.getId(), position, false, tenantIdInt, positionMap);
                        }*/
                        //Thêm phòng ban phụ trách
                        this.saveChargeDepart(sysUser, sysUserExcel.getDepartIds(), departMap);
                        successLines++;
                    } catch (Exception e) {
                        errorLines++;
                        String message = e.getMessage().toLowerCase();
                        int lineNumber = i + 1;
                        // Xác định thông tin lỗi qua tên chỉ mục
                        if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_USERNAME)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Tên người dùng đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_WORK_NO)) {
                            errorMessage.add(" " + lineNumber + " Dòng: mã nhân viên đã tồn tại, bỏ qua khi nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_PHONE)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Số điện thoại đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER_EMAIL)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Email đã tồn tại, bỏ qua nhập.");
                        } else if (message.contains(CommonConstant.SQL_INDEX_UNIQ_SYS_USER)) {
                            errorMessage.add(" " + lineNumber + " Dòng: Vi phạm ràng buộc duy nhất của bảng.");
                        } else {
                            errorMessage.add(" " + lineNumber + " Dòng: Lỗi không xác định, bỏ qua việc nhập");
                            log.error(e.getMessage(), e);
                        }
                    }
                    ImportSysUserCache.setImportSysUserMap(fileKey,i,listSysUsers.size(),"user");
                }
            } catch (Exception e) {
                ImportSysUserCache.removeImportLowAppMap(fileKey);
                errorMessage.add("Đã xảy ra ngoại lệ:" + e.getMessage());
                log.error(e.getMessage(), e);
            } finally {
                try {
                    file.getInputStream().close();
                } catch (IOException e) {
                    ImportSysUserCache.removeImportLowAppMap(fileKey);
                    log.error(e.getMessage(), e);
                }
            }
        }
        try {
            departMap.clear();
            departMap = null;
            //Nhập cuối cùng hoàn tất
            ImportSysUserCache.setImportSysUserMap(fileKey,1,1,"user");
            return ImportExcelUtil.imporReturnRes(errorLines, successLines, errorMessage);
        } catch (IOException e) {
            ImportSysUserCache.removeImportLowAppMap(fileKey);
            throw new RuntimeException(e);
        }
    }

    //================================================================ begin 【Nhập người dùng】Khi nhập, cấp của phòng ban được thêm không đúng======================================================================
    /**
     * Thêm phòng ban và người dùng trong low-code
     *
     * @param userId          Người dùng id
     * @param depart          Tên phòng ban
     * @param tenantId        Tenant id
     * @param departMap       Lưu trữ phòng ban map;key là tên, value là đối tượng SysDepart.
     * @param orgCategorys    Loại phòng ban
     * @param postName        Tên cấp bậc
     * @param mainDepPostName Tên vị trí chính
     * @param postMap         key: Tên vị trí + value id phòng ban: vị trí (id phòng ban)
     * @param positionMap     key: Tên cấp bậc  value: Cấp bậc id
     * @param otherDepPostName Tên vị trí kiêm nhiệm
     * @Description Xử lý tách riêng với QiaoQiaoYun, nguyên nhân: do cải tạo vị trí low-code v.v. có cấp bậc nên thêm phòng ban để xử lý tách riêng
     */
    private void lowAddOrEditDepart(String userId, String depart, Integer tenantId, Map<String, SysDepart> departMap, String orgCategorys, String postName, String mainDepPostName, Map<String, String> postMap, Map<String, String> positionMap, String otherDepPostName) {
        //Thiết lập quan hệ liên kết giữa phòng ban và thông tin người dùng hàng loạt
        if (StringUtils.isNotEmpty(depart)) {
            Page<SysDepart> page = new Page<>(1, 1);
            //Tách nhiều phòng ban ra
            String[] departNames = depart.split(SymbolConstant.SEMICOLON);
            List<String> departNameList = Arrays.asList(departNames);
            //Loại phòng ban
            List<String> categoryList = new ArrayList<>();
            if (oConvertUtils.isNotEmpty(orgCategorys)) {
                categoryList = Arrays.asList(orgCategorys.split(SymbolConstant.SEMICOLON));
            }
            departNameList = departNameList.stream().distinct().collect(Collectors.toList());
            //Chỉ số vòng lặp của phòng ban hiện tại
            int index = 0;
            //Đã nhập vị trí hay chưa, vị trí chỉ nhập dưới phòng ban đầu tiên
            boolean izImportPost = false;
            for (String departName : departNameList) {
                //Phòng ban id
                String parentId = "";
                String[] names = departName.split(SymbolConstant.SINGLE_SLASH);
                //Nối tên phòng ban
                String nameStr = "";
                //Loại phòng ban
                String[] orgCategory = null;
                if (categoryList != null && categoryList.size() > index) {
                    orgCategory = categoryList.get(index).split(SymbolConstant.SINGLE_SLASH);
                }
                for (int i = 0; i < names.length; i++) {
                    String name = names[i];
                    //đã ghép nối name
                    if (oConvertUtils.isNotEmpty(nameStr)) {
                        nameStr = nameStr + SymbolConstant.SINGLE_SLASH + name;
                    } else {
                        nameStr = name;
                    }
                    SysDepart sysDepart = null;
                    //Phòng ban mặc định
                    String category = DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue();
                    if (null != orgCategory && orgCategory.length > i) {
                        category = orgCategory[i];
                    }
                    //Kiểm tra trong map có tồn tại tên phòng ban đó không
                    if (departMap.containsKey(nameStr)) {
                        sysDepart = departMap.get(nameStr);
                        parentId = sysDepart.getId();
                    } else {
                        //Không tồn tại cần đi truy vấn
						//update-begin---author:wangshuai---date:20260805---for:【LHZP-1071】Nhập người dùng hệ thống, tái sử dụng công ty và phòng ban đã có---
						SysDepart importDepart = this.getImportDepartByName(name, tenantId, parentId);
                        //Phòng ban trống, cần thêm mới phòng ban
						if (null == importDepart) {
                            JSONObject formData = new JSONObject();
                            formData.put("parentId", parentId);
                            String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
                            sysDepart = new SysDepart();
                            sysDepart.setParentId(parentId);
                            sysDepart.setOrgCode(codeArray[0]);
                            sysDepart.setOrgType(codeArray[1]);
                            sysDepart.setTenantId(tenantId);
                            sysDepart.setDepartName(name);
                            sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
                            sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
                            sysDepart.setStatus(CommonConstant.STATUS_1);
                            sysDepart.setOrgCategory(DepartCategoryEnum.getValueByName(category));
                            sysDepartMapper.insert(sysDepart);
                        } else {
							sysDepart = importDepart;
                        }
						//update-end---author:wangshuai---date:20260805---for:【LHZP-1071】Nhập người dùng hệ thống, tái sử dụng công ty và phòng ban đã có---
                        //Nếu id cấp trên không rỗng thì đổi phòng ban cấp trên thành không phải nút lá
                        if (oConvertUtils.isNotEmpty(parentId)) {
                            sysDepartMapper.setMainLeaf(parentId, CommonConstant.NOT_LEAF);
                        }
                        parentId = sysDepart.getId();
                        departMap.put(nameStr, sysDepart);
                    }
                    //Vị trí cuối cùng thêm bảng quan hệ người dùng phòng ban
                    if (i == names.length - 1 && !izImportPost) {
                        Long count = sysUserDepartMapper.getCountByDepartIdAndUserId(userId, sysDepart.getId());
                        if (count == 0) {
                            SysUserDepart userDepart = new SysUserDepart(userId, sysDepart.getId());
                            sysUserDepartMapper.insert(userDepart);
                        }
                        //Thêm vị trí công việc
                        if (oConvertUtils.isNotEmpty(mainDepPostName)) {
                            this.insertDepartPost(userId, parentId ,postName, mainDepPostName, postMap, tenantId, positionMap);
                        }
                        //Thêm vị trí kiêm nhiệm
                        if(oConvertUtils.isNotEmpty(otherDepPostName)){
                            this.insertOtherDepartPost(userId,parentId,postName, otherDepPostName, postMap, tenantId, positionMap);
                        }
                        izImportPost = true;
                    }
                }
                index++;
            }
        }
    }

    /**
     * Thêm vị trí phòng ban
     *
     * @param mainDepPost Tên vị trí
     * @param userId      Người dùng id
     * @param departId    ID phòng ban [ID phòng ban cấp trên]
     * @param postName    Tên cấp bậc
     * @param mainDepPostName    Tên vị trí
     * @param postMap     map vị trí key: tên vị trí + value id phòng ban: vị trí (id phòng ban)
     * @param tenantId    Tenant id
     * @param postionMap  Cấp bậc map key: Tên cấp bậc value: Cấp bậc id
     */
    private void insertDepartPost(String userId, String depId, String postName, String mainDepPostName, Map<String, String> postMap, Integer tenantId, Map<String, String> postionMap) {
        if(mainDepPostName.contains(SymbolConstant.COMMA)){
            mainDepPostName = mainDepPostName.split(SymbolConstant.COMMA)[0];
        }
        //update-begin---wangshuai---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
        String departId = this.getOrCreateDepartPost(depId, tenantId, mainDepPostName, postName, postMap, postionMap);
        if(oConvertUtils.isNotEmpty(departId)){
            //Dù vị trí là thêm mới hay tái sử dụng thì đều phải đặt vị trí chính cho người dùng hiện tại
            SysUser user = new SysUser();
            user.setId(userId);
            user.setMainDepPostId(departId);
            userMapper.updateById(user);
        }
        //update-end---author:wangshuai ---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
    }

    /**
     * Lấy hoặc tạo vị trí của phòng ban; khi tái sử dụng vị trí đã có thì giữ quan hệ vị trí cấp trên của nó và đồng bộ cấp bậc.
     */
    private String getOrCreateDepartPost(String depId, Integer tenantId, String departPostName, String postName, Map<String, String> postMap, Map<String, String> positionMap) {
        String postMapKey = departPostName + depId;
        String departPostId = postMap.get(postMapKey);
        if (oConvertUtils.isEmpty(departPostId)) {
            //Truy vấn vị trí phòng ban theo id phòng ban và tên vị trí id
			//update-begin---author:wangshuai---date:20260805---for:【LHZP-1071] Nhập người dùng hệ thống tái sử dụng vị trí đã có theo tenant và phòng ban---
			departPostId = this.getImportDepartPostId(depId, departPostName, tenantId);
			//update-end---author:wangshuai---date:20260805---for:【LHZP-1071] Nhập người dùng hệ thống tái sử dụng vị trí đã có theo tenant và phòng ban---
        }
        if (oConvertUtils.isEmpty(departPostId)) {
            //Thêm vị trí phòng ban
            return this.addCommontDepartPost(depId, tenantId, departPostName, positionMap, postName, postMap);
        }
        postMap.put(postMapKey, departPostId);
        //Lấy cấp bậc rồi cập nhật
        String positionId = this.getPositionId(tenantId, postName, positionMap);
        //Cập nhật cấp bậc phòng ban
        if (oConvertUtils.isNotEmpty(positionId)) {
            SysDepart departPost = new SysDepart();
            departPost.setId(departPostId);
            departPost.setPositionId(positionId);
            sysDepartMapper.updateById(departPost);
        }
        return departPostId;
    }

    /**
     * Lấy ID cấp bậc theo tên cấp bậc và lưu cache kết quả nhập lần này.
     * @param positionMap Lưu trữ cấp bậc map
     * @param postName Tên vị trí
     * @param tenantId Tenant id
     */
    private String getPositionId(Integer tenantId, String postName, Map<String, String> positionMap) {
		//update-begin---author:wangshuai---date:20260805---for:【LHZP-1071】Nhập người dùng hệ thống, tên cấp bậc bỏ khoảng trắng đầu và cuối---
		if (oConvertUtils.isEmpty(postName)) {
			return "";
		}
		//update-end---author:wangshuai---date:20260805---for:【LHZP-1071】Nhập người dùng hệ thống, tên cấp bậc bỏ khoảng trắng đầu và cuối---
        if (positionMap.containsKey(postName)) {
            return positionMap.get(postName);
        }
        String positionId = this.getSysPosition(tenantId, postName);
        positionMap.put(postName, positionId);
        return positionId;
    }

    /**
     * Nhập phương thức chung thêm vị trí phòng ban
     *
     * @param depId
     * @param tenantId
     * @param mainDepPostName
     * @param postionMap
     * @param postName
     * @param map
     * @param postMap
     * @return
     */
    private String addCommontDepartPost(String depId, Integer tenantId, String mainDepPostName, Map<String, String> postionMap, String postName, Map<String, String> postMap) {
        //Thêm vị trí
        SysDepart sysDepart = new SysDepart();
        JSONObject formData = new JSONObject();
        formData.put("parentId", depId);
        String[] codeArray = (String[]) FillRuleUtil.executeRule(FillRuleConstant.DEPART, formData);
        sysDepart.setParentId(depId);
        sysDepart.setOrgCode(codeArray[0]);
        sysDepart.setOrgType(codeArray[1]);
        sysDepart.setTenantId(tenantId);
        sysDepart.setDepartName(mainDepPostName);
        sysDepart.setIzLeaf(CommonConstant.IS_LEAF);
        sysDepart.setDelFlag(String.valueOf(CommonConstant.DEL_FLAG_0));
        sysDepart.setStatus(CommonConstant.STATUS_1);
        sysDepart.setOrgCategory(DepartCategoryEnum.DEPART_CATEGORY_POST.getValue());
        //Lấy cấp bậc id
        //update-begin---wangshuai---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
        String positionId = this.getPositionId(tenantId, postName, postionMap);
        //update-end---wangshuai---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
        sysDepart.setPositionId(positionId);
        sysDepartMapper.insert(sysDepart);
        sysDepartMapper.setMainLeaf(depId, CommonConstant.NOT_LEAF);
        postMap.put(mainDepPostName + depId, sysDepart.getId());
        //Cần liên kết vị trí chính trong bảng người dùng
        return sysDepart.getId();
    }

    /**
     * Thêm vị trí kiêm nhiệm
     *
     * @param userId           Người dùng id
     * @param departId         ID phòng ban [ID phòng ban cấp trên]
     * @param postName         Tên cấp bậc
     * @param otherDepPostName Tên vị trí kiêm nhiệm
     * @param postMap          map vị trí key: tên vị trí + value id phòng ban: vị trí (id phòng ban)
     * @param tenantId         Tenant id
     * @param postionMap       Cấp bậc map key: Tên cấp bậc value: Cấp bậc id
     */
    private void insertOtherDepartPost(String userId, String depId, String postName, String otherDepPostName, Map<String, String> postMap, Integer tenantId, Map<String, String> positionMap) {
        String[] otherDepPostNames = otherDepPostName.split(SymbolConstant.SEMICOLON);
        for (int i = 0; i < otherDepPostNames.length; i++) {
            //update-begin---wangshuai---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
            String departId = this.getOrCreateDepartPost(depId, tenantId, otherDepPostNames[i], postName, postMap, positionMap);
            //update-end---wangshuai---date:20260804  for：[LHZP-1071]1.Công ty và phòng ban đã có lại được tạo mới 2.Cấp bậc trên vị trí và vị trí cấp trên chưa được ghép đôi------------
            //Chèn vào bảng trung gian bên thứ ba của chức vụ
            if (oConvertUtils.isNotEmpty(departId)) {
                try {
                    SysUserDepPost depPost = new SysUserDepPost(userId, departId);
                    depPostMapper.insert(depPost);
                } catch (Exception e) {
                    log.error("Chèn chức vụ hiện tại thất bại：" + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Lấy thông tin chức vụ
     *
     * @param tenantId
     * @param postName
     * @return
     */
    private String getSysPosition(Integer tenantId, String postName) {
		//update-begin---author:wangshuai---date:20260805---for:【LHZP-1071] Nhập người dùng hệ thống khớp tổ chức, vị trí và cấp bậc theo tenant---
		Integer normalizedTenantId = oConvertUtils.getInt(tenantId,0);
        Page<SysPosition> page = new Page<>(1, 1);
		LambdaQueryWrapper<SysPosition> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysPosition::getName, postName).orderByDesc(SysPosition::getCreateTime);
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			queryWrapper.eq(SysPosition::getTenantId, normalizedTenantId);
		} else {
			queryWrapper.and(wrapper -> wrapper.eq(SysPosition::getTenantId, normalizedTenantId).or().isNull(SysPosition::getTenantId));
		}
		IPage<SysPosition> positionPage = sysPositionMapper.selectPage(page, queryWrapper);
		if (CollectionUtil.isNotEmpty(positionPage.getRecords())) {
			return positionPage.getRecords().get(0).getId();
        //update-end---author:wangshuai---date:20260805---for:【LHZP-1071] Nhập người dùng hệ thống khớp tổ chức, vị trí và cấp bậc theo tenant---
        }
        return "";
    }

	/**
	 * Khớp công ty hoặc phòng ban đã có theo thuê bao thuộc về khi import và theo đầy đủ cấp cha - con.
	 */
	private SysDepart getImportDepartByName(String departName, Integer tenantId, String parentId) {
		LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysDepart::getDepartName, departName)
				.eq(SysDepart::getDelFlag, String.valueOf(CommonConstant.DEL_FLAG_0))
				.and(wrapper -> wrapper.ne(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue()).or().isNull(SysDepart::getOrgCategory))
				.orderByDesc(SysDepart::getCreateTime);
		if (oConvertUtils.isNotEmpty(parentId)) {
			queryWrapper.eq(SysDepart::getParentId, parentId);
		} else {
			queryWrapper.and(wrapper -> wrapper.isNull(SysDepart::getParentId).or().eq(SysDepart::getParentId, ""));
		}
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			queryWrapper.eq(SysDepart::getTenantId, tenantId);
		} else {
			queryWrapper.and(wrapper -> wrapper.eq(SysDepart::getTenantId, tenantId).or().isNull(SysDepart::getTenantId));
		}
		IPage<SysDepart> departPage = sysDepartMapper.selectPage(new Page<>(1, 1), queryWrapper);
		return CollectionUtil.isEmpty(departPage.getRecords()) ? null : departPage.getRecords().get(0);
	}

	/**
	 * Khớp vị trí đã có theo tenant, phòng ban và tên vị trí thuộc về dữ liệu nhập.
	 */
	private String getImportDepartPostId(String parentId, String postName, Integer tenantId) {
		LambdaQueryWrapper<SysDepart> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.select(SysDepart::getId)
				.eq(SysDepart::getParentId, parentId)
				.eq(SysDepart::getDepartName, postName)
				.eq(SysDepart::getOrgCategory, DepartCategoryEnum.DEPART_CATEGORY_POST.getValue())
				.eq(SysDepart::getDelFlag, String.valueOf(CommonConstant.DEL_FLAG_0))
				.orderByDesc(SysDepart::getCreateTime);
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			queryWrapper.eq(SysDepart::getTenantId, tenantId);
		} else {
			queryWrapper.and(wrapper -> wrapper.eq(SysDepart::getTenantId, tenantId).or().isNull(SysDepart::getTenantId));
		}
		IPage<SysDepart> postPage = sysDepartMapper.selectPage(new Page<>(1, 1), queryWrapper);
		return CollectionUtil.isEmpty(postPage.getRecords()) ? "" : postPage.getRecords().get(0).getId();
	}
    //================================================================ end 【Nhập người dùng】Khi nhập, cấp của phòng ban được thêm không đúng======================================================================
    
    private void saveChargeDepart(SysUser sysUser, String departIds, Map<String, SysDepart> departMap) {
        //Kiểm tra những phòng ban nào chưa có, tức là chưa được thêm vào phòng ban, thì không thể trở thành người phụ trách phòng ban
        if (oConvertUtils.isEmpty(departIds)) {
            return;
        }
        //Nhiều phòng ban dùng;phân tách bằng
        String[] split = departIds.split(SymbolConstant.SEMICOLON);
        //Phòng ban phụ trách id
        StringBuilder departIdBulider = new StringBuilder();
        for (String name : split) {
            if (departMap.containsKey(name)) {
                SysDepart sysDepart = departMap.get(name);
                departIdBulider.append(sysDepart.getId()).append(",");
            }
        }
        // Kiểm tra và xóa dấu phẩy cuối cùng
        if (departIdBulider.length() > 0 && departIdBulider.charAt(departIdBulider.length() - 1) == ',') {
            departIdBulider.deleteCharAt(departIdBulider.length() - 1);
        }
        SysUser user = new SysUser();
        user.setId(sysUser.getId());
        user.setDepartIds(departIdBulider.toString());
        this.updateById(user);
    }

    /**
     * Lưu hoặc chỉnh sửa vai trò
     *
     * @param userId
     * @param roleNames
     * @param tenantIdInt
     */
    private void saveOrEditRole(String userId, String roleNames, Integer tenantIdInt) {
        if (oConvertUtils.isEmpty(roleNames)) {
            return;
        }
        String[] roleNameArray = roleNames.split(SymbolConstant.SEMICOLON);
        //Xóa vai trò của người dùng
        LambdaQueryWrapper<SysUserRole> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(deleteQuery);
        //Lấy vai trò theo tên
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.orderByDesc(SysRole::getCreateTime);
        if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
            roleQuery.eq(SysRole::getTenantId, tenantIdInt);
        }
        for (String roleName : roleNameArray) {
            roleQuery.eq(SysRole::getRoleName, roleName);
            List<SysRole> sysRoles = sysRoleMapper.selectList(roleQuery);
            String roleId = "";
            if (CollectionUtil.isNotEmpty(sysRoles)) {
                roleId = sysRoles.get(0).getId();
            } else {
                SysRole sysRole = new SysRole();
                sysRole.setRoleName(roleName);
                sysRole.setRoleCode(RandomUtil.randomString(10));
                sysRoleMapper.insert(sysRole);
                roleId = sysRole.getId();
            }
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setUserId(userId);
            sysUserRole.setRoleId(roleId);
            sysUserRoleMapper.insert(sysUserRole);
        }
    }
    //================================================= end Nhập/xuất phòng ban low-code ================================================================

    @Override
    public void updatePasswordNotBindPhone(String oldPassword, String password, String username) {
        LoginUser sysUser = (LoginUser)SecurityUtils.getSubject().getPrincipal();
        //step1 Chỉ được sửa mật khẩu của chính mình
        if(!sysUser.getUsername().equals(username)){
            throw new JeecgBootBizTipException("Chỉ được phép sửa mật khẩu của chính mình!");
        }
        //step2 Người dùng không tồn tại, cấm đổi mật khẩu
        SysUser user = this.getUserByName(username);
        if(null == user){
            throw new JeecgBootBizTipException("Người dùng không tồn tại, không thể đổi mật khẩu!");
        }
        //setp3 Nếu số điện thoại tồn tại thì cần dùng cách đổi mật khẩu bằng số điện thoại
        if(oConvertUtils.isNotEmpty(user.getPhone())){
            throw new JeecgBootBizTipException("Số điện thoại không rỗng, vui lòng thực hiện thao tác đổi mật khẩu theo số điện thoại!");
        }
        //step4 Kiểm tra mật khẩu cũ có đúng hay không
        String passwordEncode = PasswordUtil.encrypt(username, oldPassword, user.getSalt());
        if (!user.getPassword().equals(passwordEncode)) {
            throw new JeecgBootBizTipException("Mật khẩu cũ nhập sai!");
        }
        if (oConvertUtils.isEmpty(password)) {
            throw new JeecgBootBizTipException("Mật khẩu mới không được để trống!");
        }
        //step5 Đổi mật khẩu
        String newPassWord = PasswordUtil.encrypt(username, password, user.getSalt());
        this.userMapper.update(new SysUser().setPassword(newPassWord).setLastPwdUpdateTime(new Date()), new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, user.getId()));
    }

	/**
	 *
	 * @param userName
	 * @return
	 */
	@Override
	public Map<String, String> queryUserAndDeptByName(String userName) {
		// Trả về thông tin người dùng và phòng ban (điều chỉnh theo yêu cầu)
		Map<String, String> result = new HashMap<>();
		SysUser user = this.getUserByName(userName);
		result.put("userId", user.getId());
		result.put("username", user.getUsername());
		//Thông tin phòng ban của người dùng
		String orgCode = user.getOrgCode();
		if (oConvertUtils.isEmpty(orgCode)) {
			return result;
		}

		// Truy vấn phòng ban công ty
		String companyName  = Optional.ofNullable(sysDepartMapper.queryCompByOrgCode(orgCode))
				.map(SysDepart::getDepartName)
				.orElse("");

		// Truy vấn phòng ban của người dùng và khớp
		String userDeptName = sysDepartMapper.queryDepartsByUsername(userName).stream()
				.filter(depart -> orgCode.equals(depart.getOrgCode()))
				.findFirst()
				.map(SysDepart::getDepartName)
				.orElse("");

		// Đặt văn bản hiển thị phòng ban
		String compDepart;
		if (StringUtils.isNotEmpty(companyName) && StringUtils.isNotEmpty(userDeptName)) {
			compDepart = companyName.equals(userDeptName)
					? companyName
					: companyName + "-" + userDeptName;
		} else {
			compDepart = StringUtils.isNotEmpty(companyName) ? companyName : userDeptName;
		}
		result.put("compDepart", compDepart);
		return result;
	}

    /**
     * Truy vấn người dùng thuộc phòng ban, vị trí, bao gồm cả người dùng thuộc phòng ban cấp dưới
     * 
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    @Override
    public IPage<SysUserSysDepPostModel> queryDepartPostUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
        List<SysUserSysDepPostModel> sysDepartModels = baseMapper.queryDepartPostUserByOrgCode(page, orgCode, userParams);
        if(CollectionUtil.isNotEmpty(sysDepartModels)){
            List<String> userIds = sysDepartModels.stream().map(SysUserSysDepPostModel::getId).toList();
            //Lấy tên phòng ban
            Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
            //update-begin---author:wangshuai ---date:20261030  for：【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không-----------
            //Phòng ban hiện tại/Người dùng thuộc vị trí (danh sách còn bao gồm người dùng của phòng ban, vị trí cấp dưới)
            Set<String> currentDepartPostUserIds = this.getCurrentDepartPostUserIdsByOrgCode(orgCode, userIds);
            //update-end---author:wangshuai ---date:20261030  for：【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không-----------
            sysDepartModels.forEach(item -> {
                //update-begin---author:wangshuai ---date:20261030  for：【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không-----------
                //Thêm vào thông tin có phải là người dùng thuộc phòng ban hiện tại hoặc chức vụ hiện tại hay không, frontend mới hiển thị nút thao tác di chuyển ra
                item.setIzCurrentDepartPost(currentDepartPostUserIds.contains(item.getId()));
                //update-end---author:wangshuai ---date:20261030  for：【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không-----------
                List<String> positionList = sysUserPositionMapper.getPositionIdByUserId(item.getId());
                item.setPost(CommonUtils.getSplitText(positionList,SymbolConstant.COMMA));
                item.setOrgCodeTxt(useDepNames.get(item.getId()));
                //Truy vấn các tenant của người dùng ids
                List<Integer> list = userTenantMapper.getTenantIdsByUserId(item.getId());
                if (oConvertUtils.isNotEmpty(list)) {
                    item.setRelTenantIds(StringUtils.join(list.toArray(), SymbolConstant.COMMA));
                } else {
                    item.setRelTenantIds("");
                }
                //Vị trí kiêm nhiệm
                List<String> depPostList = depPostMapper.getDepPostByUserId(item.getId());
                if(CollectionUtil.isNotEmpty(depPostList)){
                    item.setOtherDepPostId(StringUtils.join(depPostList.toArray(), SymbolConstant.COMMA));
                }
            });
        }
        return page.setRecords(sysDepartModels);
    }

    /**
     * Theo phòng ban/Lấy phòng ban hiện tại theo mã chức vụ/Id người dùng thuộc vị trí (không bao gồm người dùng của phòng ban cấp dưới, vị trí)
     *
     * @param orgCode Phòng ban/Mã vị trí
     * @param userIds Người dùng id
     * @return
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    private Set<String> getCurrentDepartPostUserIdsByOrgCode(String orgCode, List<String> userIds) {
        if (oConvertUtils.isEmpty(orgCode)) {
            return new HashSet<>();
        }
        LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
        departQuery.eq(SysDepart::getOrgCode, orgCode);
        SysDepart depart = sysDepartMapper.selectOne(departQuery);
        if (depart == null) {
            return new HashSet<>();
        }
        ///Lấy người dùng thuộc phòng ban và vị trí hiện tại id
        return this.getCurrentDepartPostUserIds(depart, userIds);
    }

    /**
     * Lấy phòng ban hiện tại/Id người dùng thuộc vị trí (không bao gồm người dùng của phòng ban cấp dưới, vị trí)
     * Công ty, công ty con, phòng ban: phòng ban trực thuộc là phòng ban hiện tại; vị trí: vị trí chính hoặc vị trí kiêm nhiệm là vị trí hiện tại
     *
     * @param depart  Phòng ban/Vị trí công tác
     * @param userIds Người dùng id
     * @return
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    private Set<String> getCurrentDepartPostUserIds(SysDepart depart, List<String> userIds) {
        Set<String> currentDepartPostUserIds = new HashSet<>();
        if (CollectionUtil.isEmpty(userIds)) {
            return currentDepartPostUserIds;
        }
        String depId = depart.getId();
        boolean isPost = DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(depart.getOrgCategory());
        if (isPost) {
            //Chức vụ: chức vụ chính là chức vụ hiện tại
            LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
            userQuery.select(SysUser::getId);
            userQuery.eq(SysUser::getMainDepPostId, depId);
            userQuery.in(SysUser::getId, userIds);
            List<SysUser> userList = userMapper.selectList(userQuery);
            for (SysUser user : userList) {
                currentDepartPostUserIds.add(user.getId());
            }
            //Chức vụ: chức vụ kiêm nhiệm là chức vụ hiện tại
            LambdaQueryWrapper<SysUserDepPost> depPostQuery = new LambdaQueryWrapper<>();
            depPostQuery.select(SysUserDepPost::getUserId);
            depPostQuery.eq(SysUserDepPost::getDepId, depId);
            depPostQuery.in(SysUserDepPost::getUserId, userIds);
            List<String> depPostUserIds = depPostMapper.selectObjs(depPostQuery);
            for (String depPostUserId : depPostUserIds) {
                currentDepartPostUserIds.add(depPostUserId);
            }
        } else {
            //Công ty, công ty con, phòng ban: phòng ban thuộc về là phòng ban hiện tại
            LambdaQueryWrapper<SysUserDepart> userDepartQuery = new LambdaQueryWrapper<>();
            userDepartQuery.select(SysUserDepart::getUserId);
            userDepartQuery.eq(SysUserDepart::getDepId, depId);
            userDepartQuery.in(SysUserDepart::getUserId, userIds);
            List<String> departUserIds = sysUserDepartMapper.selectObjs(userDepartQuery);
            for (String departUserId : departUserIds) {
                currentDepartPostUserIds.add(departUserId);
            }
        }
        return currentDepartPostUserIds;
    }

    /**
     * Chuyển người dùng ra khỏi phòng ban hoặc vị trí (chỉ xóa quan hệ liên kết, không xóa người dùng)
     *
     * @param depId   Phòng ban hoặc vị trí id
     * @param userIds Người dùng id
     * @return Người dùng thực tế bị di chuyển ra id
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {CacheConstant.SYS_USERS_CACHE}, allEntries = true)
    public List<String> removeDepartUser(String depId, List<String> userIds) {
        List<String> removeUserIds = new ArrayList<>();
        SysDepart depart = sysDepartMapper.selectById(depId);
        if (depart == null) {
            return removeUserIds;
        }
        //Chỉ di chuyển ra khỏi phòng ban hiện tại/Người dùng thuộc vị trí, người dùng của phòng ban cấp dưới và vị trí không xử lý
        Set<String> currentDepartPostUserIds = this.getCurrentDepartPostUserIds(depart, userIds);
        if (currentDepartPostUserIds.isEmpty()) {
            return removeUserIds;
        }
        removeUserIds.addAll(currentDepartPostUserIds);
        boolean isPost = DepartCategoryEnum.DEPART_CATEGORY_POST.getValue().equals(depart.getOrgCategory());
        //Vị trí công tác
        if (isPost) {
            //step.1 Rời khỏi chức vụ: Xóa chức vụ chính
            //Dùng LambdaUpdateWrapper để tránh khi rỗng bị ảnh hưởng bởi updateStrategy toàn cục dẫn đến không cập nhật
            LambdaUpdateWrapper<SysUser> userUpdate = new LambdaUpdateWrapper<>();
            userUpdate.in(SysUser::getId, removeUserIds);
            userUpdate.eq(SysUser::getMainDepPostId, depId);
            userUpdate.set(SysUser::getMainDepPostId, null);
            userMapper.update(userUpdate);
            //step.2 Rời khỏi chức vụ: Xóa chức vụ kiêm nhiệm
            LambdaQueryWrapper<SysUserDepPost> depPostQuery = new LambdaQueryWrapper<>();
            depPostQuery.in(SysUserDepPost::getUserId, removeUserIds);
            depPostQuery.eq(SysUserDepPost::getDepId, depId);
            depPostMapper.delete(depPostQuery);
        } else {
            //step.1 Di chuyển ra khỏi công ty, công ty con, phòng ban: xóa quan hệ phòng ban thuộc về
            LambdaQueryWrapper<SysUserDepart> userDepartQuery = new LambdaQueryWrapper<>();
            userDepartQuery.eq(SysUserDepart::getDepId, depId);
            userDepartQuery.in(SysUserDepart::getUserId, removeUserIds);
            sysUserDepartMapper.delete(userDepartQuery);
            //step.2 Dọn dẹp vị trí chính và vị trí kiêm nhiệm đã không còn thuộc các phòng ban trực thuộc khác (khớp với quy tắc khi sửa phòng ban trực thuộc tại trang chỉnh sửa người dùng), phải thực thi sau khi xóa quan hệ phòng ban trực thuộc
            //Đã không còn ở phòng ban, công ty hiện tại nữa thì các vị trí bên dưới cần xóa, vì vị trí được xác định theo công ty và phòng ban
            this.removeDepPostOutOfDepart(depart, removeUserIds);
            //step.3 Hủy người phụ trách của phòng ban này (phòng ban phụ trách chỉ được chọn trong các phòng ban trực thuộc)
            this.removeChargeDepart(depId, removeUserIds);
        }
        return removeUserIds;
    }

    /**
     * Sau khi rời khỏi phòng ban, dọn dẹp vị trí chính và vị trí kiêm nhiệm của người dùng tại phòng ban đó mà đã không còn thuộc các phòng ban trực thuộc khác
     *
     * @param depart  Phòng ban đã di chuyển ra
     * @param userIds Người dùng id
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    private void removeDepPostOutOfDepart(SysDepart depart, List<String> userIds) {
        //step.1 Truy vấn vị trí chính của người dùng
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.select(SysUser::getId, SysUser::getMainDepPostId);
        userQuery.in(SysUser::getId, userIds);
        List<SysUser> userList = userMapper.selectList(userQuery);
        //step.2 Truy vấn chức vụ kiêm nhiệm của người dùng
        LambdaQueryWrapper<SysUserDepPost> depPostQuery = new LambdaQueryWrapper<>();
        depPostQuery.select(SysUserDepPost::getId, SysUserDepPost::getUserId, SysUserDepPost::getDepId);
        depPostQuery.in(SysUserDepPost::getUserId, userIds);
        List<SysUserDepPost> depPostList = depPostMapper.selectList(depPostQuery);
        //step.3 Truy vấn các phòng ban còn thuộc về sau khi người dùng được di chuyển ra
        LambdaQueryWrapper<SysUserDepart> userDepartQuery = new LambdaQueryWrapper<>();
        userDepartQuery.select(SysUserDepart::getUserId, SysUserDepart::getDepId);
        userDepartQuery.in(SysUserDepart::getUserId, userIds);
        List<SysUserDepart> userDepartList = sysUserDepartMapper.selectList(userDepartQuery);
        //step.4 Truy vấn mã số của vị trí và các phòng ban còn thuộc về
        Set<String> departIds = new HashSet<>();
        //Vị trí chính
        for (SysUser user : userList) {
            if (oConvertUtils.isNotEmpty(user.getMainDepPostId())) {
                departIds.add(user.getMainDepPostId());
            }
        }
        //Vị trí kiêm nhiệm
        for (SysUserDepPost depPost : depPostList) {
            departIds.add(depPost.getDepId());
        }
        //Phòng ban của người dùng
        for (SysUserDepart userDepart : userDepartList) {
            departIds.add(userDepart.getDepId());
        }
        if (departIds.isEmpty()) {
            return;
        }
        //Truy vấn mã phòng ban theo id phòng ban cần xóa
        LambdaQueryWrapper<SysDepart> departQuery = new LambdaQueryWrapper<>();
        departQuery.select(SysDepart::getId, SysDepart::getOrgCode);
        departQuery.in(SysDepart::getId, departIds);
        List<SysDepart> departList = sysDepartMapper.selectList(departQuery);
        //key: phòng ban/id vị trí, value: mã
        Map<String, String> orgCodeMap = new HashMap<>();
        for (SysDepart sysDepart : departList) {
            orgCodeMap.put(sysDepart.getId(), sysDepart.getOrgCode());
        }
        //key: id người dùng, value: mã của phòng ban còn lại mà người dùng thuộc về
        Map<String, List<String>> userOrgCodeMap = new HashMap<>();
        for (SysUserDepart userDepart : userDepartList) {
            String orgCode = orgCodeMap.get(userDepart.getDepId());
            if (orgCode == null) {
                continue;
            }
            List<String> userOrgCodes = userOrgCodeMap.get(userDepart.getUserId());
            if (userOrgCodes == null) {
                userOrgCodes = new ArrayList<>();
                userOrgCodeMap.put(userDepart.getUserId(), userOrgCodes);
            }
            userOrgCodes.add(orgCode);
        }
        //step.5 Xóa vị trí chính
        List<String> clearMainPostUserIds = new ArrayList<>();
        for (SysUser user : userList) {
            if (oConvertUtils.isEmpty(user.getMainDepPostId())) {
                continue;
            }
            String postOrgCode = orgCodeMap.get(user.getMainDepPostId());
            List<String> userOrgCodes = userOrgCodeMap.get(user.getId());
            if (this.isDepPostOutOfDepart(postOrgCode, depart.getOrgCode(), userOrgCodes)) {
                clearMainPostUserIds.add(user.getId());
            }
        }
        if (!clearMainPostUserIds.isEmpty()) {
            //Dùng LambdaUpdateWrapper để tránh khi rỗng bị ảnh hưởng bởi updateStrategy toàn cục dẫn đến không cập nhật
            LambdaUpdateWrapper<SysUser> userUpdate = new LambdaUpdateWrapper<>();
            userUpdate.in(SysUser::getId, clearMainPostUserIds);
            userUpdate.set(SysUser::getMainDepPostId, null);
            userMapper.update(userUpdate);
        }
        //step.6 Xóa vị trí kiêm nhiệm
        List<String> removeDepPostIds = new ArrayList<>();
        for (SysUserDepPost depPost : depPostList) {
            String postOrgCode = orgCodeMap.get(depPost.getDepId());
            List<String> userOrgCodes = userOrgCodeMap.get(depPost.getUserId());
            if (this.isDepPostOutOfDepart(postOrgCode, depart.getOrgCode(), userOrgCodes)) {
                removeDepPostIds.add(depPost.getId());
            }
        }
        if (!removeDepPostIds.isEmpty()) {
            LambdaQueryWrapper<SysUserDepPost> removeDepPostQuery = new LambdaQueryWrapper<>();
            removeDepPostQuery.in(SysUserDepPost::getId, removeDepPostIds);
            depPostMapper.delete(removeDepPostQuery);
        }
    }

    /**
     * Vị trí có thuộc phòng ban bị chuyển ra hay không, và đã không còn thuộc các phòng ban khác của người dùng nữa hay chưa
     *
     * @param postOrgCode   Mã vị trí A01A01
     * @param departOrgCode Mã phòng ban bị di chuyển ra A01
     * @param userOrgCodes  Mã phòng ban còn lại mà người dùng thuộc về
     * @return
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    private boolean isDepPostOutOfDepart(String postOrgCode, String departOrgCode, List<String> userOrgCodes) {
        //Vị trí không thuộc phòng ban bị di chuyển ra, không xử lý
        if (postOrgCode == null || !postOrgCode.startsWith(departOrgCode)) {
            return false;
        }
        //Người dùng đã không còn phòng ban trực thuộc nào khác
        if (userOrgCodes == null) {
            return true;
        }
        //Vị trí vẫn còn thuộc phòng ban khác của người dùng, giữ lại
        for (String userOrgCode : userOrgCodes) {
            //Mã được nối từng cấp; mã vị trí bắt đầu bằng mã của phòng ban trực thuộc thì nghĩa là vị trí đó thuộc phòng ban này
            //Ví dụ: công ty A01 có phòng ban A01A01, dưới phòng ban có vị trí A01A01A01, người dùng rời khỏi phòng ban A01A01
            //Người dùng vẫn còn ở công ty A01: A01A01A01 bắt đầu bằng A01, vị trí vẫn nằm trong công ty, trả về false, vị trí được giữ lại
            //Người dùng chỉ còn phòng ban A01A02: A01A01A01 không bắt đầu bằng A01A02, sau khi vòng lặp kết thúc trả về true, vị trí bị gỡ bỏ
            if (postOrgCode.startsWith(userOrgCode)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Hủy người phụ trách phòng ban của người dùng trong phòng ban này
     *
     * @param depId   Phòng ban id
     * @param userIds Người dùng id
     * @author wangshuai
     * @since 2026-10-30 【LHZP-1194] Quản lý phòng ban, danh sách người dùng có phải thiếu một thao tác di chuyển ra không
     */
    private void removeChargeDepart(String depId, List<String> userIds) {
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.select(SysUser::getId, SysUser::getDepartIds);
        userQuery.in(SysUser::getId, userIds);
        userQuery.like(SysUser::getDepartIds, depId);
        List<SysUser> userList = userMapper.selectList(userQuery);
        for (SysUser user : userList) {
            //Phòng ban phụ trách sau khi loại bỏ phòng ban hiện tại
            List<String> newDepartIds = new ArrayList<>();
            boolean isChargeDepart = false;
            String[] departIds = user.getDepartIds().split(SymbolConstant.COMMA);
            for (String departId : departIds) {
                if (oConvertUtils.isEmpty(departId)) {
                    continue;
                }
                if (departId.equals(depId)) {
                    isChargeDepart = true;
                } else {
                    newDepartIds.add(departId);
                }
            }
            //Ngăn like khớp nhầm sang id phòng ban khác; khi không phải người phụ trách của phòng ban hiện tại thì bỏ qua
            if (!isChargeDepart) {
                continue;
            }
            //Dùng LambdaUpdateWrapper để tránh khi rỗng bị ảnh hưởng bởi updateStrategy toàn cục dẫn đến không cập nhật
            LambdaUpdateWrapper<SysUser> userUpdate = new LambdaUpdateWrapper<>();
            userUpdate.eq(SysUser::getId, user.getId());
            if (newDepartIds.isEmpty()) {
                //Khi phòng ban phụ trách trống, nghĩa là đã là người dùng thông thường
                userUpdate.set(SysUser::getDepartIds, null);
                userUpdate.set(SysUser::getUserIdentity, CommonConstant.USER_IDENTITY_1);
            } else {
                userUpdate.set(SysUser::getDepartIds, String.join(SymbolConstant.COMMA, newDepartIds));
            }
            userMapper.update(userUpdate);
        }
    }

    /**
     * Truy vấn thông tin người dùng theo orgCode (đường dẫn đầy đủ của phòng ban, thông tin vị trí chính và vị trí kiêm nhiệm), bao gồm công ty, công ty con, phòng ban
     *
     * @param orgCode
     * @param userParams
     * @param page
     * @return
     */
    @Override
    public IPage<SysUserSysDepPostModel> queryDepartUserByOrgCode(String orgCode, SysUser userParams, IPage page) {
        List<SysUserSysDepPostModel> sysDepartModels = baseMapper.queryDepartUserByOrgCode(page, orgCode, userParams);
        //Người dùng id
        List<String> userIdList = sysDepartModels.stream().map(SysUserSysDepPostModel::getId).toList();
        if (CollectionUtil.isNotEmpty(userIdList)) {
            //Lấy tên phòng ban theo ids người dùng; key là id người dùng, value là tên phòng ban
            Map<String, String> departNameMap = this.getDepartNamesByUserIds(userIdList, SymbolConstant.COMMA);
            //Lấy vị trí kiêm nhiệm
            Map<String, String> departPostMap = this.getDepartOtherPostByUserIds(userIdList, SymbolConstant.COMMA);
            ISysDepartService service = SpringContextUtils.getBean(SysDepartServiceImpl.class);
            sysDepartModels.forEach(item -> {
                item.setDepartName(departNameMap.get(item.getId()));
                item.setOtherPostName(departPostMap.get(item.getId()));
                //Lấy toàn bộ đường dẫn vị trí chính
                if (oConvertUtils.isNotEmpty(item.getMainDepPostId())) {
                    SysDepart departById = sysDepartMapper.getDepartById(item.getMainDepPostId());
                    if (null != departById) {
                        String departPathName = service.getDepartPathNameByOrgCode(departById.getOrgCode(), "");
                        item.setPostName(departPathName);
                    }
                }
            });
        }
        return page.setRecords(sysDepartModels);
    }

    /**
     * Trong danh bạ, bấm vào người dùng để lấy chi tiết người dùng (bao gồm thông tin cơ bản của người dùng, đường dẫn đầy đủ của phòng ban, đường dẫn đầy đủ của vị trí chính và vị trí kiêm nhiệm)
     *
     * @param userId
     * @return
     */
    @Override
    public SysUserSysDepPostModel getUserDetailByUserId(String userId) {
        SysUser sysUser = baseMapper.selectById(userId);
        if (null != sysUser) {
            SysUserSysDepPostModel userModel = new SysUserSysDepPostModel();
            BeanUtils.copyProperties(sysUser, userModel);
            //Lấy tên phòng ban
            List<String> userIds = new ArrayList<>();
            userIds.add(userId);
            Map<String, String> departNameMap = this.getDepartNamesByUserIds(userIds, "__");
            userModel.setDepartName(departNameMap.get(userId));
            ISysDepartService service = SpringContextUtils.getBean(SysDepartServiceImpl.class);
            //Lấy toàn bộ đường dẫn vị trí chính
            if (oConvertUtils.isNotEmpty(sysUser.getMainDepPostId())) {
                SysDepart departById = sysDepartMapper.getDepartById(sysUser.getMainDepPostId());
                if (null != departById) {
                    String departPathName = service.getDepartPathNameByOrgCode(departById.getOrgCode(), "");
                    userModel.setPostName(departPathName);
                }
            }
            //Lấy toàn bộ đường dẫn chức vụ kiêm nhiệm
            Map<String, String> departPostMap = this.getDepartOtherPostByUserIds(userIds, "__");
            userModel.setOtherPostName(departPostMap.get(userId));
            return userModel;
        }
        return null;
    }

	/**
	 * Đăng nhập — Lấy thông tin phòng ban của người dùng
	 * @param jsonObject
	 * @return
	 */
	@Override
	public Result loginGetUserDeparts(JSONObject jsonObject) {
		Result result = new Result<>();
		//Nội dung trả về
		JSONObject obj = new JSONObject(new LinkedHashMap<>());
		// Phương thức đăng nhập phone:Điện thoại account:Mật khẩu tài khoản
		String loginType = jsonObject.getString("loginType");
		String username = jsonObject.getString("username");
		String source = oConvertUtils.getString(jsonObject.getString("source"),"PC");
		//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] API tra phòng ban trước khi đăng nhập có thể gọi ẩn danh: thêm đếm thất bại và khóa, người dùng không tồn tại và mật khẩu/Lỗi mã xác minh dùng chung một thông báo, ngăn đoán mật khẩu vô hạn lần và tấn công brute-force mã xác minh SMS; phía APP (không có mã xác minh hình ảnh) giữ việc bỏ qua mã xác minh, nhánh điện thoại tương thích tên tham số captcha-----------
		// Kiểm tra đăng nhập bằng số điện thoại
		if("phone".equalsIgnoreCase(loginType)){
			String phone = jsonObject.getString("mobile");
			if (oConvertUtils.isEmpty(phone)) {
				return Result.error("Mã xác minh điện thoại sai");
			}
			if (isLoginFailOvertimes(phone)) {
				return Result.error("Người dùng này đã đăng nhập thất bại quá nhiều lần, vui lòng đăng nhập lại sau 10 phút!");
			}
			//1.Kiểm tra mã xác minh (tên tham số mã xác minh SMS phía APP là captcha)
			SysUser sysUser = baseMapper.getUserByPhone(phone);
			String smscode = oConvertUtils.getString(jsonObject.getString("smscode"), jsonObject.getString("captcha"));
			Object code = redisUtil.get(CommonConstant.PHONE_REDIS_KEY_PRE + phone);
			if (sysUser == null || oConvertUtils.isEmpty(smscode) || !smscode.equals(code)) {
				addLoginFailOvertimes(phone);
				return Result.error("Mã xác minh điện thoại sai");
			}
			//2.Kiểm tra tính hợp lệ của người dùng
			result = this.checkUserIsEffective(sysUser);
			if(!result.isSuccess()) {
				return result;
			}
			//3.Tài khoản hiện đang đăng nhập
			username = sysUser.getUsername();
			String orgCode = sysUser.getOrgCode();
			obj.put("currentOrgCode", orgCode);
		}else{
			if (oConvertUtils.isEmpty(username)) {
				return Result.error("Tên người dùng hoặc mật khẩu sai");
			}
			if (isLoginFailOvertimes(username)) {
				return Result.error("Người dùng này đã đăng nhập thất bại quá nhiều lần, vui lòng đăng nhập lại sau 10 phút!");
			}
			String password  = AesEncryptUtil.resolvePassword(jsonObject.getString("password"));
			log.debug("Mật khẩu đăng nhập, mật khẩu gốc:{}, mật khẩu đã giải mã:{}" , jsonObject.getString("password"), password);
			String realKey = null;
			// Phía mobile không có mã xác minh, không kiểm tra (dựa vào đếm số lần thất bại và khóa để chống brute-force, giống với/sys/mLogin nhất quán)
			if(!"APP".equalsIgnoreCase(source)){
				// step.1 Kiểm tra mã xác thực
				String inputCode = jsonObject.getString("inputCode");
				String checkKey = jsonObject.getString("checkKey");
				if (inputCode == null) {
					result.error500("Mã xác minh không hợp lệ");
					return result;
				}
				String lowerCaseCaptcha = inputCode.toLowerCase();
				String keyPrefix = Md5Util.md5Encode(checkKey + jeecgBaseConfig.getSignatureSecret(), "utf-8");
				realKey = keyPrefix + lowerCaseCaptcha;
				Object checkCode = redisUtil.get(realKey);
				if (checkCode == null || !checkCode.toString().equals(lowerCaseCaptcha)) {
					log.warn("Mã xác minh không đúng，key= {} , Ui checkCode= {}, Redis checkCode = {}", checkKey, lowerCaseCaptcha, checkCode);
					result.error500("Mã xác minh không đúng");
					result.setCode(HttpStatus.PRECONDITION_FAILED.value());
					return result;
				}
			}

			// step.2 Kiểm tra tên người dùng hoặc mật khẩu có đúng không (khi mật khẩu sai thì hủy mã xác minh; khi mật khẩu đúng thì giữ lại, API đăng nhập tiếp theo còn dùng đến)
			SysUser sysUser = baseMapper.getUserByName(username);
			if (!verifyLoginPassword(sysUser, username, password)) {
				if (realKey != null) {
					redisUtil.del(realKey);
				}
				result.error500("Tên người dùng hoặc mật khẩu sai");
				return result;
			}

			// step.3 Kiểm tra người dùng có hợp lệ không
			result = this.checkUserIsEffective(sysUser);
			if(!result.isSuccess()) {
				return result;
			}
			String orgCode = sysUser.getOrgCode();
			obj.put("currentOrgCode", orgCode);
		}
		//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-6mmg-j26w-hxfx] API tra phòng ban trước khi đăng nhập có thể gọi ẩn danh: thêm đếm thất bại và khóa, người dùng không tồn tại và mật khẩu/Lỗi mã xác minh dùng chung một thông báo, ngăn đoán mật khẩu vô hạn lần và tấn công brute-force mã xác minh SMS; phía APP (không có mã xác minh hình ảnh) giữ việc bỏ qua mã xác minh, nhánh điện thoại tương thích tên tham số captcha-----------
		//  step.4 Lấy thông tin phòng ban của người dùng, chỉ giới hạn ở phòng ban
		List<SysDepart> departList = sysDepartMapper.queryDeptByUserAndCategory(username,DepartCategoryEnum.DEPART_CATEGORY_DEPART.getValue());
		if (CollectionUtil.isNotEmpty(departList)) {
			ISysDepartService service = SpringContextUtils.getBean(SysDepartServiceImpl.class);
			List<Map<String, String>> departs = departList.stream()
				.filter(depart -> oConvertUtils.isNotEmpty(depart) &&
						oConvertUtils.isNotEmpty(depart.getOrgCode()))
				.map(depart -> {
					String departName = depart.getDepartNameAbbr();
					//Trong trường hợp tên viết tắt trống, truy vấn tên đường dẫn đầy đủ
					if(oConvertUtils.isEmpty(departName)){
						departName = service.getDepartPathNameByOrgCode(depart.getOrgCode(), "");
					}
					Map<String, String> map = new HashMap<>();
					map.put("orgCode", depart.getOrgCode());
					map.put("departName", departName);
					return map;
				})
				.collect(Collectors.toList());
			obj.put("departs", departs);
		}
		result.setResult(obj);
		return result;
	}

	/**
	 * Đặt lại mật khẩu hàng loạt thành mật khẩu hệ thống
	 * @param usernames
	 */
	@Override
	public void resetToSysPassword(String usernames) {
		//1.Xác định có tồn tại tài khoản admin hay không
		if(hasAdminIntersection(usernames)){
			throw new JeecgBootException("Trong số người dùng đã chọn có quản trị viên, tài khoản quản trị viên không được phép đặt lại mật khẩu!!");
		}
		List<String> userArr = Arrays.asList(usernames.split(","));
		userArr.stream().forEach(username -> {
			if(oConvertUtils.isNotEmpty(username)){
				String salt = oConvertUtils.randomGen(8);
				String passwordEncode = PasswordUtil.encrypt(username, PasswordConstant.DEFAULT_PASSWORD, salt);
				//Đặt lại mật khẩu
				UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
				updateWrapper
						.eq("username", username)
						.set("last_pwd_update_time", new Date())
						.set("password", passwordEncode)
						.set("salt", salt);
				this.baseMapper.update(null, updateWrapper);
			}
		});
	}

	/**
	 * Cập nhật thông tin thiết bị
	 * @param clientId
	 * @param userId
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateClientId(String clientId,String userId) {
		//Hủy liên kết tài khoản thiết bị trước đó
		if(oConvertUtils.isNotEmpty(clientId)){
			UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
			updateWrapper
					.eq("client_id", clientId)
					.set("client_id", null);
			this.baseMapper.update(null, updateWrapper);
		}
		//Đặt liên kết mới
		SysUser sysUser = new SysUser();
		sysUser.setClientId(clientId);
		sysUser.setId(userId);
		this.baseMapper.updateById(sysUser);
	}

	/**
	 * Truy vấn danh sách người dùng theo nhóm người dùng
	 * @param page
	 * @param groupId
	 * @param username
	 * @param realname
	 * @return
	 */
	@Override
	public IPage<SysUser> getUserByUgroupId(Page<SysUser> page, String groupId, String username, String realname, Integer status) {
		IPage<SysUser> userGroupList = userMapper.getUserByUgroupId(page, groupId, username,realname,status);
		List<SysUser> records = userGroupList.getRecords();
		if (null != records && records.size() > 0) {
			List<String> userIds = records.stream().map(SysUser::getId).collect(Collectors.toList());
			Map<String, String> useDepNames = this.getDepNamesByUserIds(userIds);
			for (SysUser sysUser : userGroupList.getRecords()) {
				//Cài đặt phòng ban
				sysUser.setOrgCodeTxt(useDepNames.get(sysUser.getId()));
				//Đặt vị trí người dùng id
				this.userPositionId(sysUser);
			}
		}
		return userGroupList;
	}

	/**
	 * Có giao nhau không
	 */
	public static boolean hasAdminIntersection(String usernames) {
		if (oConvertUtils.isEmpty(usernames)) {
			return false;
		}
		// Sử dụng HashSet để nâng cao hiệu quả tra cứu
		Set<String> adminSet = Arrays.stream(ADMIN_ACCOUNT)
				.map(String::toLowerCase)
				.collect(Collectors.toSet());

		return Arrays.stream(usernames.split(SymbolConstant.COMMA))
				.map(String::trim)
				.map(String::toLowerCase)
				.anyMatch(adminSet::contains);
	}
	/**
     * Lấy tên phòng ban theo ids người dùng
     *
     * @param userIdList
     * @param symbol
     * @return
     */
    private Map<String, String> getDepartOtherPostByUserIds(List<String> userIdList, String symbol) {
        Map<String, String> departPostMap = new HashMap<>();
        List<SysUserSysDepPostModel> departPost = sysDepartMapper.getDepartOtherPostByUserIds(userIdList);
        if (CollectionUtil.isNotEmpty(departPost)) {
            ISysDepartService service = SpringContextUtils.getBean(SysDepartServiceImpl.class);
            departPost.forEach(item -> {
                if (oConvertUtils.isNotEmpty(item.getId()) && oConvertUtils.isNotEmpty(item.getOtherDepPostId())) {
                    String postName = service.getDepartPathNameByOrgCode(item.getOrgCode(), "");
                    if (departPostMap.containsKey(item.getId())) {
                        departPostMap.put(item.getId(), departPostMap.get(item.getId()) + symbol + postName);
                    } else {
                        departPostMap.put(item.getId(), postName);
                    }
                }
            });
        }
        return departPostMap;
    }

    /**
     * Lấy tên phòng ban theo ids người dùng
     *
     * @param userIdList
     * @param symbol
     * @return
     */
    private Map<String, String> getDepartNamesByUserIds(List<String> userIdList, String symbol) {
        Map<String, String> userOrgCodeMap = new HashMap<>();
        if (CollectionUtil.isNotEmpty(userIdList)) {
            List<SysUserSysDepPostModel> userDepPosts = sysUserDepartMapper.getUserDepPostByUserIds(userIdList);
            if (CollectionUtil.isNotEmpty(userDepPosts)) {
                ISysDepartService service = SpringContextUtils.getBean(SysDepartServiceImpl.class);
                userDepPosts.forEach(item -> {
                    if (oConvertUtils.isNotEmpty(item.getId()) && oConvertUtils.isNotEmpty(item.getOrgCode())) {
                        String departNamePath = service.getDepartPathNameByOrgCode(item.getOrgCode(), "");
                        if (userOrgCodeMap.containsKey(item.getId())) {
                            userOrgCodeMap.put(item.getId(), userOrgCodeMap.get(item.getId()) + symbol + departNamePath);
                        } else {
                            userOrgCodeMap.put(item.getId(), departNamePath);
                        }
                    }
                });
            }
        }
        return userOrgCodeMap;
    }
}
