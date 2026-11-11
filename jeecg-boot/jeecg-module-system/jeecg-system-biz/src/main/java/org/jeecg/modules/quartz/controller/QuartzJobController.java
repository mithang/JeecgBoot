package org.jeecg.modules.quartz.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.quartz.entity.QuartzJob;
import org.jeecg.modules.quartz.service.IQuartzJobService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Description: Quản lý trực tuyến tác vụ định kỳ
 * @Author: jeecg-boot
 * @Date: 2019-01-02
 * @Version:V1.0
 */
@RestController
@RequestMapping("/sys/quartzJob")
@Slf4j
@Tag(name = "Giao diện nhiệm vụ định kỳ")
public class QuartzJobController {
	@Autowired
	private IQuartzJobService quartzJobService;
	@Autowired
	private Scheduler scheduler;

	/**
	 * Truy vấn danh sách phân trang
	 * 
	 * @param quartzJob
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<?> queryPageList(QuartzJob quartzJob, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, req.getParameterMap());
		Page<QuartzJob> page = new Page<QuartzJob>(pageNo, pageSize);
		IPage<QuartzJob> pageList = quartzJobService.page(page, queryWrapper);
        return Result.ok(pageList);

	}

	/**
	 * Thêm nhiệm vụ định kỳ
	 * 
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:add")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public Result<?> add(@RequestBody QuartzJob quartzJob) {
		quartzJobService.saveAndScheduleJob(quartzJob);
		return Result.ok("Tạo tác vụ định kỳ thành công");
	}

	/**
	 * Cập nhật nhiệm vụ định kỳ
	 * 
	 * @param quartzJob
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:edit")
	@RequestMapping(value = "/edit", method ={RequestMethod.PUT, RequestMethod.POST})
	public Result<?> eidt(@RequestBody QuartzJob quartzJob) {
		try {
			quartzJobService.editAndScheduleJob(quartzJob);
		} catch (SchedulerException e) {
			log.error(e.getMessage(),e);
			return Result.error("Cập nhật tác vụ định kỳ thất bại!");
		}
	    return Result.ok("Cập nhật tác vụ định kỳ thành công!");
	}

	/**
	 * Xóa theo id
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:delete")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("Không tìm thấy thực thể tương ứng");
		}
		quartzJobService.deleteAndStopJob(quartzJob);
        return Result.ok("Xóa thành công!");

	}

	/**
	 * Xóa hàng loạt
	 * 
	 * @param ids
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:deleteBatch")
	@RequestMapping(value = "/deleteBatch", method = RequestMethod.DELETE)
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		if (ids == null || "".equals(ids.trim())) {
			return Result.error("Không nhận dạng được tham số!");
		}
		for (String id : Arrays.asList(ids.split(SymbolConstant.COMMA))) {
			QuartzJob job = quartzJobService.getById(id);
			quartzJobService.deleteAndStopJob(job);
		}
        return Result.ok("Xóa tác vụ định kỳ thành công!");
	}

	/**
	 * Tạm dừng nhiệm vụ định kỳ
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:pause")
	@GetMapping(value = "/pause")
	@Operation(summary = "Dừng nhiệm vụ định kỳ")
	public Result<Object> pauseJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("Tác vụ định kỳ không tồn tại!");
		}
		quartzJobService.pause(job);
		return Result.ok("Dừng nhiệm vụ định kỳ thành công");
	}

	/**
	 * Khởi động nhiệm vụ định kỳ
	 * 
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:resume")
	@GetMapping(value = "/resume")
	@Operation(summary = "Khởi động nhiệm vụ định kỳ")
	public Result<Object> resumeJob(@RequestParam(name = "id") String id) {
		QuartzJob job = quartzJobService.getById(id);
		if (job == null) {
			return Result.error("Tác vụ định kỳ không tồn tại!");
		}
		quartzJobService.resumeJob(job);
		//scheduler.resumeJob(JobKey.jobKey(job.getJobClassName().trim()));
		return Result.ok("Khởi động tác vụ định kỳ thành công");
	}

	/**
	 * Tìm kiếm theo id
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/queryById", method = RequestMethod.GET)
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
        return Result.ok(quartzJob);
	}

	/**
	 * Xuất excel
	 * 
	 * @param request
	 * @param quartzJob
	 */
    @RequiresPermissions("system:quartzJob:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, QuartzJob quartzJob) {
		// Step.1 Lắp ráp điều kiện truy vấn
		QueryWrapper<QuartzJob> queryWrapper = QueryGenerator.initQueryWrapper(quartzJob, request.getParameterMap());
		// Lọc dữ liệu đã chọn
		String selections = request.getParameter("selections");
		if (oConvertUtils.isNotEmpty(selections)) {
			List<String> selectionList = Arrays.asList(selections.split(","));
			queryWrapper.in("id",selectionList);
		}
		// Step.2 AutoPoi Xuất Excel
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		List<QuartzJob> pageList = quartzJobService.list(queryWrapper);
		// Tên file xuất
		mv.addObject(NormalExcelConstants.FILE_NAME, "Danh sách tác vụ định kỳ");
		mv.addObject(NormalExcelConstants.CLASS, QuartzJob.class);
        //Lấy người dùng đang đăng nhập
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("Dữ liệu danh sách tác vụ định kỳ", "Người xuất:"+user.getRealname(), "Thông tin xuất"));
        mv.addObject(NormalExcelConstants.DATA_LIST, pageList);
		return mv;
	}

	/**
	 * Nhập dữ liệu qua Excel
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("system:quartzJob:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		// Thông tin lỗi
		List<String> errorMessage = new ArrayList<>();
		int successLines = 0, errorLines = 0;
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            // Lấy đối tượng tệp tải lên
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<QuartzJob> listQuartzJobs = ExcelImportUtil.importExcel(file.getInputStream(), QuartzJob.class, params);
				//add-begin-author:taoyan date:20210909 for:Import tác vụ định kỳ sẽ không được khởi động và lập lịch, cần bấm khởi động thủ công mới được đưa vào tác vụ lập lịch #2986
				for(QuartzJob job: listQuartzJobs){
					job.setStatus(CommonConstant.STATUS_DISABLE);
				}
				List<String> list = ImportExcelUtil.importDateSave(listQuartzJobs, IQuartzJobService.class, errorMessage,CommonConstant.SQL_INDEX_UNIQ_JOB_CLASS_NAME);
				//add-end-author:taoyan date:20210909 for:Import tác vụ định kỳ sẽ không được khởi động và lập lịch, cần bấm khởi động thủ công mới được đưa vào tác vụ lập lịch #2986
				errorLines+=list.size();
				successLines+=(listQuartzJobs.size()-errorLines);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("Nhập tệp thất bại!");
			} finally {
				try {
					file.getInputStream().close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ImportExcelUtil.imporReturnRes(errorLines,successLines,errorMessage);
	}

	/**
	 * Thực thi ngay
	 * @param id
	 * @return
	 */
	//@RequiresRoles("admin")
    @RequiresPermissions("system:quartzJob:execute")
	@GetMapping("/execute")
	public Result<?> execute(@RequestParam(name = "id", required = true) String id) {
		QuartzJob quartzJob = quartzJobService.getById(id);
		if (quartzJob == null) {
			return Result.error("Không tìm thấy thực thể tương ứng");
		}
		try {
			quartzJobService.execute(quartzJob);
		} catch (Exception e) {
			//e.printStackTrace();
			log.info("Tác vụ định kỳ thực thi ngay thất bại>>"+e.getMessage());
			return Result.error("Thực thi thất bại!");
		}
		return Result.ok("Thực thi thành công!");
	}

}
