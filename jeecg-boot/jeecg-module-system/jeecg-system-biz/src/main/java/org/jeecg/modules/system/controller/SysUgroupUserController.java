package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysUgroupUser;
import org.jeecg.modules.system.service.ISysUgroupUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
 /**
 * @Description: Bảng quan hệ nhóm người dùng
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Tag(name="Bảng quan hệ nhóm người dùng")
@RestController
@RequestMapping("/system/sysUgroupUser")
@Slf4j
public class SysUgroupUserController extends JeecgController<SysUgroupUser, ISysUgroupUserService> {
	@Autowired
	private ISysUgroupUserService sysUgroupUserService;
	
	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param sysUgroupUser
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "Bảng quan hệ nhóm người dùng-Truy vấn danh sách phân trang")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Truy vấn danh sách phân trang")
	@GetMapping(value = "/list")
	public Result<IPage<SysUgroupUser>> queryPageList(SysUgroupUser sysUgroupUser,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<SysUgroupUser> queryWrapper = QueryGenerator.initQueryWrapper(sysUgroupUser, req.getParameterMap());
		Page<SysUgroupUser> page = new Page<SysUgroupUser>(pageNo, pageSize);
		IPage<SysUgroupUser> pageList = sysUgroupUserService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   Thêm
	 *
	 * @param sysUgroupUser
	 * @return
	 */
	@AutoLog(value = "Bảng quan hệ nhóm người dùng-Thêm")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Thêm")
	@RequiresPermissions("system:sys_ugroup_user:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody SysUgroupUser sysUgroupUser) {
		sysUgroupUserService.save(sysUgroupUser);

		return Result.OK("Thêm thành công!");
	}
	
	/**
	 *  Chỉnh sửa
	 *
	 * @param sysUgroupUser
	 * @return
	 */
	@AutoLog(value = "Bảng quan hệ nhóm người dùng-Chỉnh sửa")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Chỉnh sửa")
	@RequiresPermissions("system:sys_ugroup_user:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody SysUgroupUser sysUgroupUser) {
		sysUgroupUserService.updateById(sysUgroupUser);
		return Result.OK("Chỉnh sửa thành công!");
	}
	
	/**
	 *   Xóa theo id
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Bảng quan hệ nhóm người dùng-Xóa theo id")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Xóa theo id")
	@RequiresPermissions("system:sys_ugroup_user:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		sysUgroupUserService.removeById(id);
		return Result.OK("Xóa thành công!");
	}
	
	/**
	 *  Xóa hàng loạt
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Bảng quan hệ nhóm người dùng-Xóa hàng loạt")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Xóa hàng loạt")
	@RequiresPermissions("system:sys_ugroup_user:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysUgroupUserService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("Xóa hàng loạt thành công!!");
	}
	
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "Bảng quan hệ nhóm người dùng-Tìm kiếm theo id")
	@Operation(summary="Bảng quan hệ nhóm người dùng-Tìm kiếm theo id")
	@GetMapping(value = "/queryById")
	public Result<SysUgroupUser> queryById(@RequestParam(name="id",required=true) String id) {
		SysUgroupUser sysUgroupUser = sysUgroupUserService.getById(id);
		if(sysUgroupUser==null) {
			return Result.error("Không tìm thấy dữ liệu tương ứng");
		}
		return Result.OK(sysUgroupUser);
	}

    /**
    * Xuất excel
    *
    * @param request
    * @param sysUgroupUser
    */
    @RequiresPermissions("system:sys_ugroup_user:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysUgroupUser sysUgroupUser) {
        return super.exportXls(request, sysUgroupUser, SysUgroupUser.class, "Bảng quan hệ nhóm người dùng");
    }

    /**
      * Nhập dữ liệu qua Excel
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("system:sys_ugroup_user:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysUgroupUser.class);
    }

}
