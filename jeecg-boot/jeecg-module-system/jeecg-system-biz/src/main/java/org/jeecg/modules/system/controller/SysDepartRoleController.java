package org.jeecg.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.*;
import org.jeecg.modules.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

 /**
 * @Description: Vai trò phòng ban
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Slf4j
@Tag(name="Vai trò phòng ban")
@RestController
@RequestMapping("/sys/sysDepartRole")
public class SysDepartRoleController extends JeecgController<SysDepartRole, ISysDepartRoleService> {
	@Autowired
	private ISysDepartRoleService sysDepartRoleService;

	@Autowired
	private ISysDepartRoleUserService departRoleUserService;

	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	 @Autowired
	 private ISysDepartService sysDepartService;

	 @Autowired
     private BaseCommonService baseCommonService;
     
	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param sysDepartRole
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="Vai trò phòng ban-Truy vấn danh sách phân trang")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartRole sysDepartRole,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   @RequestParam(name="deptId",required=false) String deptId,
								   HttpServletRequest req) {
		QueryWrapper<SysDepartRole> queryWrapper = QueryGenerator.initQueryWrapper(sysDepartRole, req.getParameterMap());
		Page<SysDepartRole> page = new Page<SysDepartRole>(pageNo, pageSize);
//		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
//		List<String> deptIds = null;
//		if(oConvertUtils.isEmpty(deptId)){
//			if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals(CommonConstant.USER_IDENTITY_2) ){
//				deptIds = sysDepartService.getMySubDepIdsByDepId(user.getDepartIds());
//			}else{
//				return Result.ok(null);
//			}
//		}else{
//			deptIds = sysDepartService.getSubDepIdsByDepId(deptId);
//		}
//		queryWrapper.in("depart_id",deptIds);

		//Phòng ban của tôi, khi chọn phòng ban chỉ được xem vai trò thuộc phòng ban hiện tại
		// Giải thích logic mã: [QQYUN-10775]Mã xác thực có thể tái sử dụng #7674------------
		if(oConvertUtils.isNotEmpty(deptId)){
			queryWrapper.eq("depart_id",deptId);
			IPage<SysDepartRole> pageList = sysDepartRoleService.page(page, queryWrapper);
			return Result.ok(pageList);
		}else{
			return Result.ok(null);
		}
	}
	
	/**
	 * Thêm
	 *
	 * @param sysDepartRole
	 * @return
	 */
    @RequiresPermissions("system:depart:role:add")
	@Operation(summary="Vai trò phòng ban-Thêm")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartRole sysDepartRole) {
		sysDepartRoleService.save(sysDepartRole);
		return Result.ok("Thêm thành công!");
	}
	
	/**
	 * Chỉnh sửa
	 *
	 * @param sysDepartRole
	 * @return
	 */
	@Operation(summary="Vai trò phòng ban-Chỉnh sửa")
    @RequiresPermissions("system:depart:role:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<?> edit(@RequestBody SysDepartRole sysDepartRole) {
		sysDepartRoleService.updateById(sysDepartRole);
		return Result.ok("Chỉnh sửa thành công!");
	}
	
	/**
	 * Xóa theo id
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Vai trò phòng ban-Xóa theo id")
	@Operation(summary="Vai trò phòng ban-Xóa theo id")
    @RequiresPermissions("system:depart:role:delete")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDepartRoleService.removeById(id);
		return Result.ok("Xóa thành công!");
	}
	
	/**
	 * Xóa hàng loạt
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Vai trò phòng ban-Xóa hàng loạt")
	@Operation(summary="Vai trò phòng ban-Xóa hàng loạt")
    @RequiresPermissions("system:depart:role:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDepartRoleService.deleteDepartRole(Arrays.asList(ids.split(",")));
		//this.sysDepartRoleService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("Xóa hàng loạt thành công!");
	}
	
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	@Operation(summary="Vai trò phòng ban-Tìm kiếm theo id")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDepartRole sysDepartRole = sysDepartRoleService.getById(id);
		return Result.ok(sysDepartRole);
	}

	 /**
	  * Lấy vai trò thuộc phòng ban
	  * @param departId
	  * @return
	  */
	@RequestMapping(value = "/getDeptRoleList", method = RequestMethod.GET)
	public Result<List<SysDepartRole>> getDeptRoleList(@RequestParam(value = "departId") String departId,@RequestParam(value = "userId") String userId){
		Result<List<SysDepartRole>> result = new Result<>();
		//Truy vấn vai trò của phòng ban đã chọn
		List<SysDepartRole> deptRoleList = sysDepartRoleService.list(new LambdaQueryWrapper<SysDepartRole>().eq(SysDepartRole::getDepartId,departId));
		result.setSuccess(true);
		result.setResult(deptRoleList);
		return result;
	}

	 /**
	  * Cài đặt
	  * @param json
	  * @return
	  */
     @RequiresPermissions("system:depart:role:userAdd")
	 @RequestMapping(value = "/deptRoleUserAdd", method = RequestMethod.POST)
	 public Result<?> deptRoleAdd(@RequestBody JSONObject json) {
		 String newRoleId = json.getString("newRoleId");
		 String oldRoleId = json.getString("oldRoleId");
		 String userId = json.getString("userId");
		 departRoleUserService.deptRoleUserAdd(userId,newRoleId,oldRoleId);
         // Giải thích logic mã: [VUEN-234]Phân bổ vai trò phòng ban: thêm nhật ký nhạy cảm------------
         LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
         baseCommonService.addLog("Gán vai trò cho ID người dùng phòng ban:"+userId+"Phân bổ vai trò, người thao tác： " +loginUser.getUsername() ,CommonConstant.LOG_TYPE_2, 2);
         return Result.ok("Thêm thành công!");
	 }

	 /**
	  * Lấy vai trò phòng ban đã đặt theo id người dùng
	  * @param userId
	  * @return
	  */
	 @RequestMapping(value = "/getDeptRoleByUserId", method = RequestMethod.GET)
	 public Result<List<SysDepartRoleUser>> getDeptRoleByUserId(@RequestParam(value = "userId") String userId,@RequestParam(value = "departId") String departId){
		 Result<List<SysDepartRoleUser>> result = new Result<>();
		 //Truy vấn vai trò thuộc phòng ban
		 List<SysDepartRole> roleList = sysDepartRoleService.list(new QueryWrapper<SysDepartRole>().eq("depart_id",departId));
		 List<String> roleIds = roleList.stream().map(SysDepartRole::getId).collect(Collectors.toList());
		 //Theo vai trò id,Truy vấn vai trò đã được ủy quyền theo id người dùng
		 List<SysDepartRoleUser> roleUserList = null;
		 if(roleIds!=null && roleIds.size()>0){
			 roleUserList = departRoleUserService.list(new QueryWrapper<SysDepartRoleUser>().eq("user_id",userId).in("drole_id",roleIds));
		 }
		 result.setSuccess(true);
		 result.setResult(roleUserList);
		 return result;
	 }

	 /**
	  * Truy vấn dữ liệu quy tắc dữ liệu
	  */
	 @GetMapping(value = "/datarule/{permissionId}/{departId}/{roleId}")
	 public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("departId") String departId,@PathVariable("roleId") String roleId) {
		//Truy vấn quy tắc phòng ban đã được ủy quyền
	 	List<SysPermissionDataRule> list = sysDepartPermissionService.getPermRuleListByDeptIdAndPermId(departId,permissionId);
		 if(list==null || list.size()==0) {
			 return Result.error("Không tìm thấy thông tin cấu hình quyền hạn");
		 }else {
			 Map<String,Object> map = new HashMap(5);
			 map.put("datarule", list);
			 LambdaQueryWrapper<SysDepartRolePermission> query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, permissionId)
					 .eq(SysDepartRolePermission::getRoleId,roleId);
			 SysDepartRolePermission sysRolePermission = sysDepartRolePermissionService.getOne(query);
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
	 @RequiresPermissions("system:depart:role:edit")
	 @PostMapping(value = "/datarule")
	 public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		 try {
			 String permissionId = jsonObject.getString("permissionId");
			 String roleId = jsonObject.getString("roleId");
			 String dataRuleIds = jsonObject.getString("dataRuleIds");
			 log.info("Lưu quy tắc dữ liệu>>"+"Menu ID:"+permissionId+"Vai trò ID:"+ roleId+"Quyền hạn dữ liệu ID:"+dataRuleIds);
			 LambdaQueryWrapper<SysDepartRolePermission> query = new LambdaQueryWrapper<SysDepartRolePermission>()
					 .eq(SysDepartRolePermission::getPermissionId, permissionId)
					 .eq(SysDepartRolePermission::getRoleId,roleId);
			 SysDepartRolePermission sysRolePermission = sysDepartRolePermissionService.getOne(query);
			 if(sysRolePermission==null) {
				 return Result.error("Vui lòng lưu quyền hạn menu của vai trò trước!!");
			 }else {
				 sysRolePermission.setDataRuleIds(dataRuleIds);
				 this.sysDepartRolePermissionService.updateById(sysRolePermission);
			 }
		 } catch (Exception e) {
			 log.error("SysRoleController.saveDatarule()Đã xảy ra ngoại lệ:" + e.getMessage(),e);
			 return Result.error("Lưu thất bại");
		 }
		 return Result.ok("Lưu thành công!");
	 }

  /**
   * Xuất excel
   *
   * @param request
   * @param sysDepartRole
   */
  @RequiresPermissions("system:depart:role:exportXls")
  @RequestMapping(value = "/exportXls")
  public ModelAndView exportXls(HttpServletRequest request, SysDepartRole sysDepartRole) {
      return super.exportXls(request, sysDepartRole, SysDepartRole.class, "Vai trò phòng ban");
  }

  /**
   * Nhập dữ liệu qua Excel
   *
   * @param request
   * @param response
   * @return
   */
  @RequiresPermissions("system:depart:role:importExcel")
  @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
  public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
      return super.importExcel(request, response, SysDepartRole.class);
  }

}
