package org.jeecg.modules.message.controller;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.message.entity.SysMessage;
import org.jeecg.modules.message.service.ISysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Tin nhắn
 * @author: jeecg-boot
 * @date: 2019-04-09
 * @version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/message/sysMessage")
public class SysMessageController extends JeecgController<SysMessage, ISysMessageService> {
	@Autowired
	private ISysMessageService sysMessageService;

	/**
	 * Truy vấn danh sách phân trang
	 * 
	 * @param sysMessage
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequiresPermissions("system:message:list")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysMessage sysMessage, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<SysMessage> queryWrapper = QueryGenerator.initQueryWrapper(sysMessage, req.getParameterMap());
		Page<SysMessage> page = new Page<SysMessage>(pageNo, pageSize);
		IPage<SysMessage> pageList = sysMessageService.page(page, queryWrapper);
        return Result.ok(pageList);
	}

	/**
	 * Thêm
	 * 
	 * @param sysMessage
	 * @return
	 */
	@RequiresPermissions("system:message:add")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysMessage sysMessage) {
		sysMessageService.save(sysMessage);
		return Result.ok("Thêm thành công!");
	}

	/**
	 * Chỉnh sửa
	 * 
	 * @param sysMessage
	 * @return
	 */
	@RequiresPermissions("system:message:edit")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysMessage sysMessage) {	
		sysMessageService.updateById(sysMessage);
        return Result.ok("Chỉnh sửa thành công!");

	}

	/**
	 * Xóa theo id
	 * 
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:message:delete")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		sysMessageService.removeById(id);
        return Result.ok("Xóa thành công!");
	}

	/**
	 * Xóa hàng loạt
	 * 
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("system:message:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

		this.sysMessageService.removeByIds(Arrays.asList(ids.split(",")));
	    return Result.ok("Xóa hàng loạt thành công!");
	}

	/**
	 * Tìm kiếm theo id
	 * 
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:message:queryById")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		SysMessage sysMessage = sysMessageService.getById(id);
		return Result.ok(sysMessage);
	}

	/**
	 * Xuất excel
	 *
	 * @param request
	 */
	@RequiresPermissions("system:message:exportXls")
	@GetMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SysMessage sysMessage) {
		return super.exportXls(request,sysMessage,SysMessage.class, "Mẫu tin nhắn đẩy");
	}

	/**
	 * excel Import
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("system:message:importExcel")
	@PostMapping(value = "/importExcel")
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, SysMessage.class);
	}

}
