package org.jeecg.modules.system.controller;


import java.util.Arrays;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 *  Bộ điều khiển frontend
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Tag(name = "Danh mục dữ liệu")
@RestController
@RequestMapping("/sys/dictItem")
@Slf4j
public class SysDictItemController {

	@Autowired
	private ISysDictItemService sysDictItemService;
	@Autowired
	private ISysDictService sysDictService;
	
	/**
	 * @Chức năng: Truy vấn dữ liệu danh mục
	 * @param sysDictItem
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<SysDictItem>> queryPageList(SysDictItem sysDictItem,@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,HttpServletRequest req) {
		Result<IPage<SysDictItem>> result = new Result<IPage<SysDictItem>>();
		QueryWrapper<SysDictItem> queryWrapper = QueryGenerator.initQueryWrapper(sysDictItem, req.getParameterMap());
		queryWrapper.orderByAsc("sort_order");
		Page<SysDictItem> page = new Page<SysDictItem>(pageNo, pageSize);
		IPage<SysDictItem> pageList = sysDictItemService.page(page, queryWrapper);
		result.setSuccess(true);
		result.setResult(pageList);
		return result;
	}
	
	/**
	 * @Chức năng: Thêm mới
	 * @return
	 */
    @RequiresPermissions("system:dict:item:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@CacheEvict(value= {CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> add(@RequestBody SysDictItem sysDictItem) {
		Result<SysDictItem> result = new Result<SysDictItem>();
		try {
			sysDictItem.setCreateTime(new Date());
			sysDictItemService.save(sysDictItem);
			sysDictService.refreshDictVersion();
			result.success("Lưu thành công!");
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			result.error500("Thao tác thất bại");
		}
		return result;
	}
	
	/**
	 * @Chức năng: Chỉnh sửa
	 * @param sysDictItem
	 * @return
	 */
    @RequiresPermissions("system:dict:item:edit")
	@RequestMapping(value = "/edit",  method = { RequestMethod.PUT,RequestMethod.POST })
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> edit(@RequestBody SysDictItem sysDictItem) {
		Result<SysDictItem> result = new Result<SysDictItem>();
		SysDictItem sysdict = sysDictItemService.getById(sysDictItem.getId());
		if(sysdict==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			sysDictItem.setUpdateTime(new Date());
			boolean ok = sysDictItemService.updateById(sysDictItem);
			//TODO Trả về false nghĩa là gì?
			if(ok) {
				sysDictService.refreshDictVersion();
				result.success("Chỉnh sửa thành công!");
			}
		}
		return result;
	}
	
	/**
	 * @Chức năng: Xóa dữ liệu danh mục
	 * @param id
	 * @return
	 */
    @RequiresPermissions("system:dict:item:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> delete(@RequestParam(name="id",required=true) String id) {
		Result<SysDictItem> result = new Result<SysDictItem>();
		SysDictItem joinSystem = sysDictItemService.getById(id);
		if(joinSystem==null) {
			result.error500("Không tìm thấy thực thể tương ứng");
		}else {
			boolean ok = sysDictItemService.removeById(id);
			if(ok) {
				sysDictService.refreshDictVersion();
				result.success("Xóa thành công!");
			}
		}
		return result;
	}
	
	/**
	 * @Chức năng: Xóa hàng loạt dữ liệu danh mục
	 * @param ids
	 * @return
	 */
    @RequiresPermissions("system:dict:item:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	@CacheEvict(value={CacheConstant.SYS_DICT_CACHE, CacheConstant.SYS_ENABLE_DICT_CACHE}, allEntries=true)
	public Result<SysDictItem> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		Result<SysDictItem> result = new Result<SysDictItem>();
		if(ids==null || "".equals(ids.trim())) {
			result.error500("Không nhận dạng được tham số!");
		}else {
			this.sysDictItemService.removeByIds(Arrays.asList(ids.split(",")));
			sysDictService.refreshDictVersion();
			result.success("Xóa thành công!");
		}
		return result;
	}

	/**
	 * Kiểm tra trùng lặp giá trị danh mục
	 * @param sysDictItem
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dictItemCheck", method = RequestMethod.GET)
	@Operation(summary="Giao diện kiểm tra trùng lặp danh mục")
	public Result<Object> doDictItemCheck(SysDictItem sysDictItem, HttpServletRequest request) {
		Long num = Long.valueOf(0);
		LambdaQueryWrapper<SysDictItem> queryWrapper = new LambdaQueryWrapper<SysDictItem>();
		queryWrapper.eq(SysDictItem::getItemValue,sysDictItem.getItemValue());
		queryWrapper.eq(SysDictItem::getDictId,sysDictItem.getDictId());
		if (StringUtils.isNotBlank(sysDictItem.getId())) {
			// Chỉnh sửa kiểm tra trang
			queryWrapper.ne(SysDictItem::getId,sysDictItem.getId());
		}
		num = sysDictItemService.count(queryWrapper);
		if (num == 0) {
			// Giá trị này khả dụng
			return Result.ok("Giá trị này khả dụng!");
		} else {
			// Giá trị này không khả dụng
			log.info("Giá trị này không khả dụng, đã tồn tại trong hệ thống!");
			return Result.error("Giá trị này không khả dụng, đã tồn tại trong hệ thống!");
		}
	}
	
}
