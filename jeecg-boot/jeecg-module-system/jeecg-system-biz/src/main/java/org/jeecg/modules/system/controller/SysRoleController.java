package org.jeecg.modules.system.controller;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.model.TreeModel;
import org.jeecg.modules.system.service.*;
import org.jeecg.modules.system.vo.SysUserRoleCountVo;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Bảng vai trò Controller frontend
 * </p>
 *
 * @Author scott
 * @since 2018-12-19
 */
@RestController
@RequestMapping("/sys/role")
@Slf4j
public class SysRoleController {
	@Autowired
	private ISysRoleService sysRoleService;
	
	@Autowired
	private ISysPermissionDataRuleService sysPermissionDataRuleService;
	
	@Autowired
	private ISysRolePermissionService sysRolePermissionService;
	
	@Autowired
	private ISysPermissionService sysPermissionService;

    @Autowired
    private ISysUserRoleService sysUserRoleService;
	@Autowired
	private BaseCommonService baseCommonService;
	@Autowired
	private JeecgRedisClient jeecgRedisClient;
	
	/**
	  * Truy vấn danh sách phân trang [Vai trò hệ thống, không cách ly tenant]
	 * @param role
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequiresPermissions("system:role:list")
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysRole>> list(SysRole role,
									  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									  @RequestParam(name="isMultiTranslate", required = false) Boolean isMultiTranslate,
									  HttpServletRequest req) {
        // Giải thích logic mã: 【issues/7948] Vai trò giải quyết việc truy vấn theo id hiển thị lại không đúng---
        if(null != isMultiTranslate && isMultiTranslate){
            pageSize = 100;
        }
		Result<IPage<SysRole>> result = new Result<IPage<SysRole>>();
		//QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(role, req.getParameterMap());
		//IPage<SysRole> pageList = sysRoleService.page(page, queryWrapper);
		Page<SysRole> page = new Page<SysRole>(pageNo, pageSize);
		//Đổi sang phương thức không thực hiện cách ly tenant, thực tế vẫn còn khiếm khuyết (khiếm khuyết: nếu bật cách ly tenant, tuy có thể thấy vai trò của tenant khác nhưng khi chỉnh sửa sẽ báo lỗi)
		IPage<SysRole> pageList = sysRoleService.listAllSysRole(page, role);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * Truy vấn danh sách phân trang 
	 * @param role
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/listByTenant", method = RequestMethod.GET)
	public Result<IPage<SysRole>> listByTenant(SysRole role,
												@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												HttpServletRequest req) {
		Result<IPage<SysRole>> result = new Result<IPage<SysRole>>();
		//Giao diện này phải truy vấn cách ly theo tenant
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			role.setTenantId(oConvertUtils.getInt(!"0".equals(TenantContext.getTenant()) ? TenantContext.getTenant() : "", -1));
		}
		
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(role, req.getParameterMap());
		Page<SysRole> page = new Page<SysRole>(pageNo, pageSize);
		IPage<SysRole> pageList = sysRoleService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	  *   Thêm
	 * @param role
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
    @RequiresPermissions("system:role:add")
	public Result<SysRole> add(@RequestBody SysRole role) {
		Result<SysRole> result = new Result<SysRole>();
		try {
			//Bật cách ly đa tenant,Id vai trò tự động tạo 10 ký tự
			// Giải thích logic mã: 【TV360X-42】Mã đã đặt khi thêm mới vai trò, sau khi lưu không khớp---
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && oConvertUtils.isEmpty(role.getRoleCode())){
				role.setRoleCode(RandomUtil.randomString(10));
			}
			role.setCreateTime(new Date());
			role.setUpdateTime(new Date());
			sysRoleService.save(role);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}
	
	/**
	  *  Chỉnh sửa
	 * @param role
	 * @return
	 */
    @RequiresPermissions("system:role:edit")
	@RequestMapping(value = "/edit",method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<SysRole> edit(@RequestBody SysRole role) {
		Result<SysRole> result = new Result<SysRole>();
		SysRole sysrole = sysRoleService.getById(role.getId());
		if(sysrole==null) {
			result.error500("Không tìm thấy vai trò tương ứng!");
		}else {
			role.setUpdateTime(new Date());

			//------------------------------------------------------------------
			//Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
			if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
				//Lấy người dùng hiện tại
				LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				Integer tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				String username = "admin";
				if (!tenantId.equals(sysrole.getTenantId()) && !username.equals(sysUser.getUsername())) {
					baseCommonService.addLog("Chưa được ủy quyền, sửa ID vai trò không thuộc tenant này:" + role.getId() + ", người thực hiện:" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_3);
					return Result.error("Chỉnh sửa vai trò thất bại,Vai trò hiện tại không thuộc Tenant này.");
				}
			}
			//------------------------------------------------------------------
			
			boolean ok = sysRoleService.updateById(role);
			if(ok) {
				result.success("Chỉnh sửa thành công!");
			}
		}
		return result;
	}
	
	/**
	  *   Xóa theo id
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:role:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
    	//Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
    	if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			//Lấy người dùng hiện tại
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
			Long getRoleCount = sysRoleService.getRoleCountByTenantId(id, tenantId);
			String username = "admin";
			if(getRoleCount == 0 && !username.equals(sysUser.getUsername())){
				baseCommonService.addLog("Chưa được ủy quyền, xóa ID vai trò không thuộc tenant này:" + id + ", người thực hiện:" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
				return Result.error("Xóa vai trò thất bại,Vai trò hiện tại không thuộc Tenant này.");
			}
		}
    	
		//Có tồn tại vai trò admin hay không
		sysRoleService.checkAdminRoleRejectDel(id);
    	
		sysRoleService.deleteRole(id);

		return Result.ok("Xóa vai trò thành công");
	}
	
	/**
	  *  Xóa hàng loạt
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:role:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<SysRole> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		baseCommonService.addLog("Thao tác xóa vai trò, ids vai trò:" + ids, CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
		Result<SysRole> result = new Result<SysRole>();
		if(oConvertUtils.isEmpty(ids)) {
			result.error500("Chưa chọn vai trò!");
		}else {
			//Nếu trong trường hợp cách ly SaaS, xác định id tenant hiện tại có thuộc tenant hiện tại hay không
			if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
				int tenantId = oConvertUtils.getInt(TenantContext.getTenant(), 0);
				String[] roleIds = ids.split(SymbolConstant.COMMA);
				LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
				String username = "admin";
				for (String id:roleIds) {
					Long getRoleCount = sysRoleService.getRoleCountByTenantId(id, tenantId);
					//Nếu tồn tại id vai trò bằng 0, tức là không tồn tại, thì xóa vai trò
					if(getRoleCount == 0 && !username.equals(sysUser.getUsername()) ){
						baseCommonService.addLog("Chưa được ủy quyền, xóa ID vai trò không thuộc tenant này:" + id + ", người thực hiện:" + sysUser.getUsername(), CommonConstant.LOG_TYPE_2, CommonConstant.OPERATE_TYPE_4);
						return Result.error("Xóa hàng loạt vai trò thất bại,Tồn tại vai trò không thuộc tenant này, cấm xóa hàng loạt");
					}
				}
			}
			//Xác minh có phải vai trò admin hay không
			sysRoleService.checkAdminRoleRejectDel(ids);
			sysRoleService.deleteBatchRole(ids.split(","));
			result.success("Xóa vai trò thành công!");
		}
		return result;
	}
	
	/**
	  * Tìm kiếm theo id
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<SysRole> queryById(@RequestParam(name="id",required=true) String id) {
		Result<SysRole> result = new Result<SysRole>();
		SysRole sysrole = sysRoleService.getById(id);
		if(sysrole==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			result.setResult(sysrole);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 * Truy vấn tất cả vai trò (tham gia cách ly tenant)
	 * 
	 * @return
	 */
	@RequestMapping(value = "/queryall", method = RequestMethod.GET)
	public Result<List<SysRole>> queryall() {
		Result<List<SysRole>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysRole::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		List<SysRole> list = sysRoleService.list(query);
		if(list==null||list.size()<=0) {
			result.error500("Không tìm thấy thông tin vai trò");
		}else {
			result.setResult(list);
			result.setSuccess(true);
		}
		return result;
	}

	/**
	 * Truy vấn tất cả vai trò hệ thống (không cách ly theo tenant)
	 *
	 * @return
	 */
	@RequiresPermissions("system:role:queryallNoByTenant")
	@RequestMapping(value = "/queryallNoByTenant", method = RequestMethod.GET)
	public Result<List<SysRole>> queryallNoByTenant() {
		Result<List<SysRole>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		List<SysRole> list = sysRoleService.list(query);
		if(list==null||list.size()<=0) {
			result.error500("Không tìm thấy thông tin vai trò");
		}else {
			result.setResult(list);
			result.setSuccess(true);
		}
		return result;
	}
	
	/**
	  * Kiểm tra mã vai trò duy nhất
	 */
	@RequestMapping(value = "/checkRoleCode", method = RequestMethod.GET)
	public Result<Boolean> checkUsername(String id,String roleCode) {
		Result<Boolean> result = new Result<>();
        //Nếu tham số này là false thì chương trình xảy ra ngoại lệ
		result.setResult(true);
		log.info("--Xác minh mã vai trò có duy nhất hay không---id:"+id+"--roleCode:"+roleCode);
		try {
			SysRole role = null;
			if(oConvertUtils.isNotEmpty(id)) {
				role = sysRoleService.getById(id);
			}
			//SysRole newRole = sysRoleService.getOne(new QueryWrapper<SysRole>().lambda().eq(SysRole::getRoleCode, roleCode));
			SysRole newRole = sysRoleService.getRoleNoTenant(roleCode);
			if(newRole!=null) {
				//Nếu truy vấn được thông tin theo roleCode truyền vào thì cần thực hiện kiểm tra.
				if(role==null) {
					//role Trống=>Chế độ thêm mới=>Chỉ cần roleCode tồn tại thì trả về false
					result.setSuccess(false);
					result.setMessage("Mã vai trò đã tồn tại");
					return result;
				}else if(!id.equals(newRole.getId())) {
					//Nếu không=>Chế độ chỉnh sửa=>Xác định ID của hai bên có giống nhau hay không-
					result.setSuccess(false);
					result.setMessage("Mã vai trò đã tồn tại");
					return result;
				}
			}
		} catch (Exception e) {
			result.setSuccess(false);
			result.setResult(false);
			result.setMessage(e.getMessage());
			return result;
		}
		result.setSuccess(true);
		return result;
	}

	/**
	 * Xuất excel
	 * @param request
	 */
	@RequiresPermissions("system:role:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(SysRole sysRole,HttpServletRequest request) {
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			sysRole.setTenantId(oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
		
		// Step.1 Lắp ráp điều kiện truy vấn
		QueryWrapper<SysRole> queryWrapper = QueryGenerator.initQueryWrapper(sysRole, request.getParameterMap());
		//Step.2 AutoPoi Xuất Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<SysRole> pageList = sysRoleService.list(queryWrapper);
		//Tên file xuất
		mv.addObject(NormalExcelConstants.FILE_NAME,"Danh sách vai trò");
		mv.addObject(NormalExcelConstants.CLASS,SysRole.class);
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        //Xuất hỗ trợ xlsx
		mv.addObject(NormalExcelConstants.PARAMS,new ExportParams("Dữ liệu danh sách vai trò","Người xuất:"+user.getRealname(),"Thông tin xuất", ExcelType.XSSF));
		mv.addObject(NormalExcelConstants.DATA_LIST,pageList);
        //Vai trò hỗ trợ xuất trường chỉ định
        String exportFields = request.getParameter(NormalExcelConstants.EXPORT_FIELDS);
        if(oConvertUtils.isNotEmpty(exportFields)){
            mv.addObject(NormalExcelConstants.EXPORT_FIELDS, exportFields);
        }
		return mv;
	}

	/**
	 * Nhập dữ liệu qua Excel
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("system:role:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				return sysRoleService.importExcelCheckRoleCode(file, params);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("Nhập file thất bại:" + e.getMessage());
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return Result.error("Nhập tệp thất bại!");
	}
	
	/**
	 * Truy vấn dữ liệu quy tắc dữ liệu
	 */
	@GetMapping(value = "/datarule/{permissionId}/{roleId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("roleId") String roleId) {
		List<SysPermissionDataRule> list = sysPermissionDataRuleService.getPermRuleListByPermId(permissionId);
		if(list==null || list.size()==0) {
			return Result.error("Không tìm thấy thông tin cấu hình quyền hạn");
		}else {
			Map<String,Object> map = new HashMap(5);
			map.put("datarule", list);
			LambdaQueryWrapper<SysRolePermission> query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, permissionId)
					.isNotNull(SysRolePermission::getDataRuleIds)
					.eq(SysRolePermission::getRoleId,roleId);
			SysRolePermission sysRolePermission = sysRolePermissionService.getOne(query);
			if(sysRolePermission==null) {
				//return Result.error("Không tìm thấy thông tin cấu hình menu vai trò");
			}else {
				String drChecked = sysRolePermission.getDataRuleIds();
				if(oConvertUtils.isNotEmpty(drChecked)) {
					map.put("drChecked", drChecked.endsWith(",")?drChecked.substring(0, drChecked.length()-1):drChecked);
				}
			}
			return Result.ok(map);
			//TODO Về sau truy vấn quyền nút bấm cũng đi qua yêu cầu này, chẳng qua chỉ cần thêm hai key vào map key
		}
	}
	
	/**
	 * Lưu quy tắc dữ liệu vào bảng liên kết menu vai trò
	 */
	@RequiresPermissions("system:permission:saveRole")
	@PostMapping(value = "/datarule")
	public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		try {
			String permissionId = jsonObject.getString("permissionId");
			String roleId = jsonObject.getString("roleId");
			String dataRuleIds = jsonObject.getString("dataRuleIds");
			log.info("Lưu quy tắc dữ liệu>>"+"Menu ID:"+permissionId+"Vai trò ID:"+ roleId+"Quyền hạn dữ liệu ID:"+dataRuleIds);
			LambdaQueryWrapper<SysRolePermission> query = new LambdaQueryWrapper<SysRolePermission>()
					.eq(SysRolePermission::getPermissionId, permissionId)
					.eq(SysRolePermission::getRoleId,roleId);
			SysRolePermission sysRolePermission = sysRolePermissionService.getOne(query);
			if(sysRolePermission==null) {
				return Result.error("Vui lòng lưu quyền hạn menu của vai trò trước!!");
			}else {
				sysRolePermission.setDataRuleIds(dataRuleIds);
				this.sysRolePermissionService.updateById(sysRolePermission);
			}
		} catch (Exception e) {
			log.error("SysRoleController.saveDatarule()Đã xảy ra ngoại lệ:" + e.getMessage(),e);
			return Result.error("Lưu thất bại");
		}
		return Result.ok("Lưu thành công!");
	}
	
	
	/**
	 * Chức năng phân quyền vai trò người dùng, truy vấn cây quyền menu
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/queryTreeList", method = RequestMethod.GET)
	public Result<Map<String,Object>> queryTreeList(HttpServletRequest request) {
		Result<Map<String,Object>> result = new Result<>();
		//Tất cả quyền hạn ids
		List<String> ids = new ArrayList<>();
		try {
			LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			query.orderByAsc(SysPermission::getSortNo);
			List<SysPermission> list = sysPermissionService.list(query);
			for(SysPermission sysPer : list) {
				ids.add(sysPer.getId());
			}
			List<TreeModel> treeList = new ArrayList<>();
			getTreeModelList(treeList, list, null);
			Map<String,Object> resMap = new HashMap(5);
            //Toàn bộ dữ liệu nút cây
			resMap.put("treeList", treeList);
            //Toàn bộ cây ids
			resMap.put("ids", ids);
			result.setResult(resMap);
			result.setSuccess(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return result;
	}
	
	private void getTreeModelList(List<TreeModel> treeList,List<SysPermission> metaList,TreeModel temp) {
		for (SysPermission permission : metaList) {
			String tempPid = permission.getParentId();
			TreeModel tree = new TreeModel(permission.getId(), tempPid, permission.getName(),permission.getRuleFlag(), permission.isLeaf());
			if(temp==null && oConvertUtils.isEmpty(tempPid)) {
				treeList.add(tree);
				if(!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}else if(temp!=null && tempPid!=null && tempPid.equals(temp.getKey())){
				temp.getChildren().add(tree);
				if(!tree.getIsLeaf()) {
					getTreeModelList(treeList, metaList, tree);
				}
			}
			
		}
	}

    /**
     * Lấy danh sách phân trang tất cả vai trò (bao gồm số lượng của mỗi vai trò)
     * @return
     */
    @RequestMapping(value = "/queryPageRoleCount", method = RequestMethod.GET)
    public Result<IPage<SysUserRoleCountVo>> queryPageRoleCount(@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
        Result<IPage<SysUserRoleCountVo>> result = new Result<>();
		LambdaQueryWrapper<SysRole> query = new LambdaQueryWrapper<SysRole>();
		//------------------------------------------------------------------------------------------------
		//Có bật cách ly dữ liệu đa thuê bao của module quản lý hệ thống hay không [Chế độ đa thuê bao SAAS]
		if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
			query.eq(SysRole::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
		}
		//------------------------------------------------------------------------------------------------
        Page<SysRole> page = new Page<>(pageNo, pageSize);
        IPage<SysRole> pageList = sysRoleService.page(page, query);
        List<SysRole> records = pageList.getRecords();
        IPage<SysUserRoleCountVo> sysRoleCountPage = new PageDTO<>();
        List<SysUserRoleCountVo> sysCountVoList = new ArrayList<>();
        //Lặp dữ liệu vai trò để lấy số lượng vai trò tương ứng dưới mỗi vai trò
        for (SysRole role:records) {
            LambdaQueryWrapper<SysUserRole> countQuery = new LambdaQueryWrapper<>();
			countQuery.eq(SysUserRole::getRoleId,role.getId());
            long count = sysUserRoleService.count(countQuery);
            SysUserRoleCountVo countVo = new SysUserRoleCountVo();
            BeanUtils.copyProperties(role,countVo);
            countVo.setCount(count);
            sysCountVoList.add(countVo);
        }
        sysRoleCountPage.setRecords(sysCountVoList);
        sysRoleCountPage.setTotal(pageList.getTotal());
        sysRoleCountPage.setSize(pageList.getSize());
        result.setSuccess(true);
        result.setResult(sysRoleCountPage);
        return result;
    }

}
