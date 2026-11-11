package org.jeecg.modules.system.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.model.DuplicateCheckVo;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @Title: DuplicateCheckAction
 * @Description: Công cụ kiểm tra trùng lặp
 * @Author 张代浩
 * @Date 2019-03-25
 * @Version V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/duplicate")
@Tag(name="Kiểm tra trùng lặp")
public class DuplicateCheckController {

	@Autowired
	ISysDictService sysDictService;

	/**
	 * Kiểm tra dữ liệu có tồn tại trong hệ thống hay không
	 * 
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	@Operation(summary="Giao diện kiểm tra trùng lặp")
	public Result<String> doDuplicateCheck(DuplicateCheckVo duplicateCheckVo, HttpServletRequest request) {
		log.debug("----duplicate check------："+ duplicateCheckVo.toString());
		
		// 1.Giá trị điền vào trống, trả về trực tiếp
		if(StringUtils.isEmpty(duplicateCheckVo.getFieldVal())){
			Result rs = new Result();
			rs.setCode(500);
			rs.setSuccess(true);
			rs.setMessage("Dữ liệu trống,Không xử lý!");
			return rs;
		}
		
		// 2.Trả về kết quả
		if (sysDictService.duplicateCheckData(duplicateCheckVo)) {
			// Giá trị này khả dụng
			return Result.ok("Giá trị này khả dụng!");
		} else {
			// Giá trị này không khả dụng
			log.info("Giá trị này không khả dụng, đã tồn tại trong hệ thống!");
			return Result.error("Giá trị này không khả dụng, đã tồn tại trong hệ thống!");
		}
	}


}
