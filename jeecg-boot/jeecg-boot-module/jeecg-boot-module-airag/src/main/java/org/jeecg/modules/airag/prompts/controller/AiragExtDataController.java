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
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.airag.prompts.consts.AiPromptsConsts;
import org.jeecg.modules.airag.prompts.entity.AiragExtData;
import org.jeecg.modules.airag.prompts.service.IAiragExtDataService;
import org.jeecg.modules.airag.prompts.vo.AiragDebugVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: airag_ext_data
 * @Author: jeecg-boot
 * @Date:   2025-12-24
 * @Version: V1.0
 */
@Tag(name="airag_ext_data")
@RestController
@RequestMapping("/airag/extData")
@Slf4j
public class AiragExtDataController extends JeecgController<AiragExtData, IAiragExtDataService> {
	@Autowired
	private IAiragExtDataService airagExtDataService;
	
	/**
	 * Truy vấn danh sách phân trang
	 *
	 * @param airagExtData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "airag_ext_data-Truy vấn danh sách phân trang")
	@Operation(summary="airag_ext_data-Truy vấn danh sách phân trang")
	@GetMapping(value = "/list")
	@RequiresPermissions("airag:extData:list")
	public Result<IPage<AiragExtData>> queryPageList(AiragExtData airagExtData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
        QueryWrapper<AiragExtData> queryWrapper = QueryGenerator.initQueryWrapper(airagExtData, req.getParameterMap());
		Page<AiragExtData> page = new Page<AiragExtData>(pageNo, pageSize);
		queryWrapper.eq("biz_type", AiPromptsConsts.BIZ_TYPE_EVALUATOR);
		IPage<AiragExtData> pageList = airagExtDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}
	/**
	 * Gọi truy vấn danh sách quỹ đạo
	 *
	 * @param airagExtData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@Operation(summary="airag_ext_data-Truy vấn danh sách phân trang")
	@GetMapping(value = "/getTrackList")
	@RequiresPermissions("airag:extData:getTrackList")
	public Result<IPage<AiragExtData>> getTrackList(AiragExtData airagExtData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
        QueryWrapper<AiragExtData> queryWrapper = QueryGenerator.initQueryWrapper(airagExtData, req.getParameterMap());
		Page<AiragExtData> page = new Page<AiragExtData>(pageNo, pageSize);
		queryWrapper.eq("biz_type", AiPromptsConsts.BIZ_TYPE_TRACK);
		String metadata = airagExtData.getMetadata();
		if(oConvertUtils.isEmpty(metadata)){
			return Result.OK();
		}
		IPage<AiragExtData> pageList = airagExtDataService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   Thêm
	 *
	 * @param airagExtData
	 * @return
	 */
	@AutoLog(value = "airag_ext_data-Thêm")
	@Operation(summary="airag_ext_data-Thêm")
	@PostMapping(value = "/add")
	@RequiresPermissions("airag:extData:add")
	public Result<String> add(@RequestBody AiragExtData airagExtData) {
		airagExtData.setBizType(AiPromptsConsts.BIZ_TYPE_EVALUATOR);
		airagExtDataService.save(airagExtData);
		return Result.OK("Thêm thành công!");
	}
	
	/**
	 *  Chỉnh sửa
	 *
	 * @param airagExtData
	 * @return
	 */
	@AutoLog(value = "airag_ext_data-Chỉnh sửa")
	@Operation(summary="airag_ext_data-Chỉnh sửa")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	@RequiresPermissions("airag:extData:edit")
	public Result<String> edit(@RequestBody AiragExtData airagExtData) {
		airagExtDataService.updateById(airagExtData);
		return Result.OK("Chỉnh sửa thành công!");
	}
	
	/**
	 *   Xóa theo id
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "airag_ext_data-Xóa theo id")
	@Operation(summary="airag_ext_data-Xóa theo id")
	@DeleteMapping(value = "/delete")
	@RequiresPermissions("airag:extData:delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		airagExtDataService.removeById(id);
		return Result.OK("Xóa thành công!");
	}
	
	/**
	 *  Xóa hàng loạt
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "airag_ext_data-Xóa hàng loạt")
	@Operation(summary="airag_ext_data-Xóa hàng loạt")
	@DeleteMapping(value = "/deleteBatch")
	@RequiresPermissions("airag:extData:deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.airagExtDataService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("Xóa hàng loạt thành công!!");
	}
	
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "airag_ext_data-Tìm kiếm theo id")
	@Operation(summary="airag_ext_data-Tìm kiếm theo id")
	@GetMapping(value = "/queryById")
	@RequiresPermissions("airag:extData:queryById")
	public Result<AiragExtData> queryById(@RequestParam(name="id",required=true) String id) {
		AiragExtData airagExtData = airagExtDataService.getById(id);
		if(airagExtData==null) {
			return Result.error("Không tìm thấy dữ liệu tương ứng");
		}
		return Result.OK(airagExtData);
	}
	/**
	 * Tìm kiếm theo id
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "airag_ext_data-Tìm kiếm theo id")
	@Operation(summary="airag_ext_data-Tìm kiếm theo id")
	@GetMapping(value = "/queryTrackById")
	@RequiresPermissions("airag:extData:queryTrackById")
	public Result<List<AiragExtData>> queryTrackById(@RequestParam(name="id",required=true) String id) {
		AiragExtData airagExtData = airagExtDataService.getById(id);
		String status = airagExtData.getStatus();
		if(AiPromptsConsts.STATUS_RUNNING.equals(status)) {
			return Result.error("Đang xử lý, vui lòng làm mới sau");
		}
		List<AiragExtData> trackList = airagExtDataService.queryTrackById(id);
		return Result.OK(trackList);
	}
	 /**
	  * Gỡ lỗi constructor
	  *
	  * @param debugVo
	  * @return
	  */
	 @PostMapping(value = "/evaluator/debug")
	 @RequiresPermissions("airag:extData:debug")
	 public Result<?> debugEvaluator(@RequestBody AiragDebugVo debugVo) {
		 return airagExtDataService.debugEvaluator(debugVo);
	 }
    /**
    * Xuất excel
    *
    * @param request
    * @param airagExtData
    */
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("airag:extData:exportXls")
    public ModelAndView exportXls(HttpServletRequest request, AiragExtData airagExtData) {
        return super.exportXls(request, airagExtData, AiragExtData.class, "airag_ext_data");
    }

    /**
      * Nhập dữ liệu qua Excel
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @RequiresPermissions("airag:extData:importExcel")
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, AiragExtData.class);
    }

}
