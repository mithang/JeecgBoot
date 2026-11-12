package org.jeecg.modules.airag.prompts.controller;

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
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.airag.prompts.entity.AiragPrompts;
import org.jeecg.modules.airag.prompts.service.IAiragPromptsService;
import org.jeecg.modules.airag.prompts.vo.AiragExperimentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
 /**
 * @Description: airag_prompts
 * @Author: jeecg-boot
 * @Date:   2025-12-24
 * @Version: V1.0
 */
@Tag(name="airag_prompts")
@RestController
@RequestMapping("/airag/prompts")
@Slf4j
public class AiragPromptsController extends JeecgController<AiragPrompts, IAiragPromptsService> {
	@Autowired
	private IAiragPromptsService airagPromptsService;
	
	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param airagPrompts
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "airag_prompts-Truy vấn danh sách phân trang")
	@Operation(summary="airag_prompts-Truy vấn danh sách phân trang")
	@GetMapping(value = "/list")
	@RequiresPermissions("airag:prompts:list")
	public Result<IPage<AiragPrompts>> queryPageList(AiragPrompts airagPrompts,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
        QueryWrapper<AiragPrompts> queryWrapper = QueryGenerator.initQueryWrapper(airagPrompts, req.getParameterMap());
		Page<AiragPrompts> page = new Page<AiragPrompts>(pageNo, pageSize);
		IPage<AiragPrompts> pageList = airagPromptsService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param airagPrompts
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="airag_prompts-Truy vấn danh sách phân trang thùng rác")
	@GetMapping(value = "/recycleBinList")
	@RequiresPermissions("airag:prompts:recycleBinList")
	//update-begin---author:chenrui ---date:2026-04-07  for：【QQYUN-14643】Sửa logic truy vấn thùng rác, bỏ qua@TableLogic Lọc-----------
	public Result<IPage<AiragPrompts>> recycleBinList(AiragPrompts airagPrompts,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Page<AiragPrompts> page = new Page<>(pageNo, pageSize);
		IPage<AiragPrompts> pageList = airagPromptsService.recycleBinPage(page);
		return Result.OK(pageList);
	}
	//update-end---author:chenrui ---date:2026-04-07  for：【QQYUN-14643】Sửa logic truy vấn thùng rác, bỏ qua@TableLogic Lọc-----------

	/**
	 *   Thêm
	 *
	 * @param airagPrompts
	 * @return
	 */
	@AutoLog(value = "airag_prompts-Thêm")
	@Operation(summary="airag_prompts-Thêm")
	@PostMapping(value = "/add")
	@RequiresPermissions("airag:prompts:add")
	public Result<String> add(@RequestBody AiragPrompts airagPrompts) {
		airagPrompts.setDelFlag(CommonConstant.DEL_FLAG_0);
		airagPrompts.setStatus("0");
		airagPromptsService.save(airagPrompts);
		return Result.OK("Thêm thành công!");
	}
	
	/**
	 *  Chỉnh sửa
	 *
	 * @param airagPrompts
	 * @return
	 */
	@AutoLog(value = "airag_prompts-Chỉnh sửa")
	@Operation(summary="airag_prompts-Chỉnh sửa")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	@RequiresPermissions("airag:prompts:edit")
	public Result<String> edit(@RequestBody AiragPrompts airagPrompts) {
		airagPromptsService.updateById(airagPrompts);
		return Result.OK("Chỉnh sửa thành công!");
	}
	
	/**
	 *   Xóa theo id
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "airag_prompts-Xóa theo id")
	@Operation(summary="airag_prompts-Xóa theo id")
	@DeleteMapping(value = "/delete")
	@RequiresPermissions("airag:prompts:delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		airagPromptsService.removeById(id);
		return Result.OK("Xóa thành công!");
	}
	
	/**
	 *  Xóa hàng loạt
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "airag_prompts-Xóa hàng loạt")
	@Operation(summary="airag_prompts-Xóa hàng loạt")
	@DeleteMapping(value = "/deleteBatch")
	@RequiresPermissions("airag:prompts:deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.airagPromptsService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("Xóa hàng loạt thành công!!");
	}
	
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "airag_prompts-Tìm kiếm theo id")
	@Operation(summary="airag_prompts-Tìm kiếm theo id")
	@GetMapping(value = "/queryById")
	@RequiresPermissions("airag:prompts:queryById")
	public Result<AiragPrompts> queryById(@RequestParam(name="id",required=true) String id) {
		AiragPrompts airagPrompts = airagPromptsService.getById(id);
		if(airagPrompts==null) {
			return Result.error("Không tìm thấy dữ liệu tương ứng");
		}
		return Result.OK(airagPrompts);
	}

	 /**
	  * Thu hồi từ thùng rác (hỗ trợ nhiều id, phân tách bằng dấu phẩy)
	  */
	 @Operation(summary = "Prompt-Thu hồi từ thùng rác")
	 @PutMapping(value = "/revertRecycleBin")
	 @RequiresPermissions("airag:prompts:revertRecycleBin")
	 public Result<?> revertRecycleBin(@RequestParam(name = "ids", required = true) String ids) {
		 airagPromptsService.revertRecycleBin(Arrays.asList(ids.split(",")));
		 return Result.OK("Đã thu hồi từ thùng rác!");
	 }
	 /**
	  * Xóa hoàn toàn khỏi thùng rác (hỗ trợ nhiều id, phân tách bằng dấu phẩy)
	  */
	 @Operation(summary = "Prompt-Xóa hoàn toàn khỏi thùng rác")
	 @DeleteMapping(value = "/deleteRecycleBin")
	 @RequiresPermissions("airag:prompts:deleteRecycleBin")
	 public Result<?> deleteRecycleBin(@RequestParam(name = "ids", required = true) String ids) {
		 airagPromptsService.deleteRecycleBin(Arrays.asList(ids.split(",")));
		 return Result.OK("Xóa hoàn toàn khỏi thùng rác!");
	 }
	 /**
	  * Gỡ lỗi constructor
	  *
	  * @param experimentVo
	  * @return
	  */
	 @PostMapping(value = "/experiment")
	 @RequiresPermissions("airag:prompts:experiment")
	 public Result<?> promptExperiment(@RequestBody AiragExperimentVo experimentVo, HttpServletRequest request) {
		 return airagPromptsService.promptExperiment(experimentVo,request);
	 }
    /**
    * Xuất excel
    *
    * @param request
    * @param airagPrompts
    */
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("airag:prompts:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragPrompts airagPrompts) {
        return super.exportXls(request, airagPrompts, AiragPrompts.class, "airag_prompts");
    }

    /**
      * Nhập dữ liệu qua Excel
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @RequiresPermissions("airag:prompts:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AiragPrompts.class);
    }

}
