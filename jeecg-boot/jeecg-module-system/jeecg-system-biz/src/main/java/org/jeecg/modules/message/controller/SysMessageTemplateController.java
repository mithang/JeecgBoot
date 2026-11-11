package org.jeecg.modules.message.controller;

import java.util.Arrays;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.dto.message.MessageDTO;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.entity.MsgParams;
import org.jeecg.modules.message.entity.SysMessageTemplate;
import org.jeecg.modules.message.service.ISysMessageTemplateService;
import org.jeecg.modules.message.util.PushMsgUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description: Mẫu tin nhắn
 * @Author: jeecg-boot
 * @Sate: 2019-04-09
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/message/sysMessageTemplate")
public class SysMessageTemplateController extends JeecgController<SysMessageTemplate, ISysMessageTemplateService> {
	@Autowired
	private ISysMessageTemplateService sysMessageTemplateService;
	@Autowired
	private PushMsgUtil pushMsgUtil;

	@Autowired
	private ISysBaseAPI sysBaseApi;

	/**
	 * Truy vấn danh sách phân trang
	 * 
	 * @param sysMessageTemplate
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysMessageTemplate sysMessageTemplate, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
		QueryWrapper<SysMessageTemplate> queryWrapper = QueryGenerator.initQueryWrapper(sysMessageTemplate, req.getParameterMap());
		Page<SysMessageTemplate> page = new Page<SysMessageTemplate>(pageNo, pageSize);
		IPage<SysMessageTemplate> pageList = sysMessageTemplateService.page(page, queryWrapper);
        return Result.ok(pageList);
	}

	/**
	 * Thêm
	 * 
	 * @param sysMessageTemplate
	 * @return
	 */
	@RequiresPermissions("system:messageTemplate:add")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysMessageTemplate sysMessageTemplate) {
		sysMessageTemplateService.save(sysMessageTemplate);
        return Result.ok("Thêm thành công!");
	}

	/**
	 * Chỉnh sửa
	 * 
	 * @param sysMessageTemplate
	 * @return
	 */
	@RequiresPermissions("system:messageTemplate:edit")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysMessageTemplate sysMessageTemplate) {
		sysMessageTemplateService.updateById(sysMessageTemplate);
        return Result.ok("Cập nhật thành công!");
	}

	/**
	 * Xóa theo id
	 * 
	 * @param id
	 * @return
	 */
	@RequiresPermissions("system:messageTemplate:delete")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
		sysMessageTemplateService.removeById(id);
        return Result.ok("Xóa thành công!");
	}

	/**
	 * Xóa hàng loạt
	 * 
	 * @param ids
	 * @return
	 */
	@RequiresPermissions("system:messageTemplate:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.sysMessageTemplateService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!");
	}

	/**
	 * Tìm kiếm theo id
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
		SysMessageTemplate sysMessageTemplate = sysMessageTemplateService.getById(id);
        return Result.ok(sysMessageTemplate);
	}

	/**
	 * Xuất excel
	 *
	 * @param request
	 */
	@RequiresPermissions("system:messageTemplate:exportXls")
	@GetMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request,SysMessageTemplate sysMessageTemplate) {
		return super.exportXls(request, sysMessageTemplate, SysMessageTemplate.class,"Mẫu tin nhắn đẩy");
	}

	/**
	 * excel Import
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("system:messageTemplate:importExcel")
	@PostMapping(value = "/importExcel")
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				return sysMessageTemplateService.importExcelCheckTemplateCode(entry.getValue(), params);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				return Result.error("Nhập tệp thất bại:" + e.getMessage());
			}
		}
		return Result.error("Nhập tệp thất bại: không tìm thấy tệp đã tải lên");
	}

	/**
	 * Gửi tin nhắn
	 */
	@RequiresPermissions("system:messageTemplate:sendMsg")
	@PostMapping(value = "/sendMsg")
	public Result<SysMessageTemplate> sendMessage(@RequestBody MsgParams msgParams) {
		Result<SysMessageTemplate> result = new Result<SysMessageTemplate>();
		try {
			MessageDTO md = new MessageDTO();
			md.setToAll(false);
			md.setTitle("Kiểm thử gửi tin nhắn");
			md.setTemplateCode(msgParams.getTemplateCode());
			md.setToUser(msgParams.getReceiver());
			md.setType(msgParams.getMsgType());
			String testData = msgParams.getTestData();
			if(oConvertUtils.isNotEmpty(testData)){
				Map<String, Object> data = JSON.parseObject(testData, Map.class);
				md.setData(data);
			}
			sysBaseApi.sendTemplateMessage(md);
			return result.success("Gửi tin nhắn thành công!");
		} catch (Exception e) {
			log.error("Gửi tin nhắn bị lỗi:" + e.getMessage(), e);
			return result.error500("Gửi tin nhắn bị lỗi!");
		}
	}
}
