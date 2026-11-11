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
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.system.entity.SysUgroup;
import org.jeecg.modules.system.service.ISysUgroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.Date;
 /**
 * @Description: Bảng nhóm người dùng
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Tag(name="Bảng nhóm người dùng")
@RestController
@RequestMapping("/sys/ugroup")
@Slf4j
public class SysUgroupController extends JeecgController<SysUgroup, ISysUgroupService> {
	@Autowired
	private ISysUgroupService sysUgroupService;


	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param sysUgroup
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "Bảng nhóm người dùng-Truy vấn danh sách phân trang")
	@Operation(summary="Bảng nhóm người dùng-Truy vấn danh sách phân trang")
	@GetMapping(value = "/list")
	public Result<IPage<SysUgroup>> queryPageList(SysUgroup sysUgroup,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {


        QueryWrapper<SysUgroup> queryWrapper = QueryGenerator.initQueryWrapper(sysUgroup, req.getParameterMap());
		//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa thuê bao chỉ truy vấn nhóm người dùng của thuê bao hiện tại, tránh đọc xuyên thuê bao-----------
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL) {
			queryWrapper.eq("tenant_id", oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req), 0));
		}
		//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa thuê bao chỉ truy vấn nhóm người dùng của thuê bao hiện tại, tránh đọc xuyên thuê bao-----------
		Page<SysUgroup> page = new Page<SysUgroup>(pageNo, pageSize);
		IPage<SysUgroup> pageList = sysUgroupService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	
	/**
	 *   Thêm
	 *
	 * @param sysUgroup
	 * @return
	 */
	@AutoLog(value = "Bảng nhóm người dùng-Thêm")
	@Operation(summary="Bảng nhóm người dùng-Thêm")
	@RequiresPermissions("system:sys_ugroup:add")
	@PostMapping(value = "/add")
	public Result<SysUgroup> add(@RequestBody SysUgroup sysUgroup) {
		Result<SysUgroup> result = new Result<SysUgroup>();
		try {
			sysUgroup.setCreateTime(new Date());
			sysUgroupService.save(sysUgroup);
			result.success("Thêm thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}
	
	/**
	 *  Chỉnh sửa
	 *
	 * @param sysUgroup
	 * @return
	 */
	@AutoLog(value = "Bảng nhóm người dùng-Chỉnh sửa")
	@Operation(summary="Bảng nhóm người dùng-Chỉnh sửa")
	@RequiresPermissions("system:sys_ugroup:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody SysUgroup sysUgroup) {
		sysUgroupService.updateById(sysUgroup);
		return Result.OK("Chỉnh sửa thành công!");
	}
	
	/**
	 *   Xóa theo id
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "Bảng nhóm người dùng-Xóa theo id")
	@Operation(summary="Bảng nhóm người dùng-Xóa theo id")
	@RequiresPermissions("system:sys_ugroup:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		sysUgroupService.deleteById(id);
		return Result.OK("Xóa thành công!");
	}
	
	/**
	 *  Xóa hàng loạt
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "Bảng nhóm người dùng-Xóa hàng loạt")
	@Operation(summary="Bảng nhóm người dùng-Xóa hàng loạt")
	@RequiresPermissions("system:sys_ugroup:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysUgroupService.deleteByIds(Arrays.asList(ids.split(",")));
		return Result.OK("Xóa hàng loạt thành công!!");
	}
	
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "Bảng nhóm người dùng-Tìm kiếm theo id")
	@Operation(summary="Bảng nhóm người dùng-Tìm kiếm theo id")
	@GetMapping(value = "/queryById")
	public Result<SysUgroup> queryById(@RequestParam(name="id",required=true) String id, HttpServletRequest req) {
		SysUgroup sysUgroup = sysUgroupService.getById(id);
		if(sysUgroup==null) {
			return Result.error("Không tìm thấy dữ liệu tương ứng");
		}
		//update-begin---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa thuê bao chỉ được xem nhóm người dùng của thuê bao hiện tại, tránh đọc xuyên thuê bao theo id-----------
		if (MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL && !java.util.Objects.equals(oConvertUtils.getInt(TokenUtils.getTenantIdByRequest(req), 0), sysUgroup.getTenantId())) {
			return Result.error("Không tìm thấy dữ liệu tương ứng");
		}
		//update-end---author:scott ---date:20261014  for: 【Bảo mật GHSA-2h34-xh29-7w95] Ở chế độ đa thuê bao chỉ được xem nhóm người dùng của thuê bao hiện tại, tránh đọc xuyên thuê bao theo id-----------
		return Result.OK(sysUgroup);
	}

    /**
    * Xuất excel
    *
    * @param request
    * @param sysUgroup
    */
    @RequiresPermissions("system:sys_ugroup:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysUgroup sysUgroup) {
        return super.exportXls(request, sysUgroup, SysUgroup.class, "Bảng nhóm người dùng");
    }

    /**
      * Nhập dữ liệu qua Excel
    *
    * @param request
    * @param response
    * @return
    */
    @RequiresPermissions("system:sys_ugroup:importExcel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, SysUgroup.class);
    }

}
