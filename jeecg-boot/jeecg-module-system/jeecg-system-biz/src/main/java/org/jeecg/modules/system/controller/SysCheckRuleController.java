package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: Quy tắc kiểm tra mã
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Quy tắc kiểm tra mã")
@RestController
@RequestMapping("/sys/checkRule")
public class SysCheckRuleController extends JeecgController<SysCheckRule, ISysCheckRuleService> {

    @Autowired
    private ISysCheckRuleService sysCheckRuleService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysCheckRule
     * @param pageNo
     * @param pageSize
     * @param request
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Truy vấn danh sách phân trang")
    @Operation(summary = "Quy tắc kiểm tra mã-Truy vấn danh sách phân trang")
    @GetMapping(value = "/list")
    public Result queryPageList(
            SysCheckRule sysCheckRule,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest request
    ) {
        QueryWrapper<SysCheckRule> queryWrapper = QueryGenerator.initQueryWrapper(sysCheckRule, request.getParameterMap());
        Page<SysCheckRule> page = new Page<>(pageNo, pageSize);
        IPage<SysCheckRule> pageList = sysCheckRuleService.page(page, queryWrapper);
        return Result.ok(pageList);
    }


    /**
     * Tìm kiếm theo id
     *
     * @param ruleCode
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Kiểm tra giá trị truyền vào qua Code")
    @Operation(summary = "Quy tắc kiểm tra mã-Kiểm tra giá trị truyền vào qua Code")
    @GetMapping(value = "/checkByCode")
    public Result checkByCode(
            @RequestParam(name = "ruleCode") String ruleCode,
            @RequestParam(name = "value") String value
    ) throws UnsupportedEncodingException {
        SysCheckRule sysCheckRule = sysCheckRuleService.getByCode(ruleCode);
        if (sysCheckRule == null) {
            return Result.error("Mã này không tồn tại");
        }
        JSONObject errorResult = sysCheckRuleService.checkValue(sysCheckRule, URLDecoder.decode(value, "UTF-8"));
        if (errorResult == null) {
            return Result.ok();
        } else {
            Result<Object> r = Result.error(errorResult.getString("message"));
            r.setResult(errorResult);
            return r;
        }
    }

    /**
     * Thêm
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Thêm")
    @Operation(summary = "Quy tắc kiểm tra mã-Thêm")
    @RequiresPermissions("system:checkRule:add")
    @PostMapping(value = "/add")
    public Result add(@RequestBody SysCheckRule sysCheckRule) {
        sysCheckRule.setCreateBy(null);
        sysCheckRule.setCreateTime(null);
        sysCheckRuleService.save(sysCheckRule);
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param sysCheckRule
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Chỉnh sửa")
    @Operation(summary = "Quy tắc kiểm tra mã-Chỉnh sửa")
    @RequiresPermissions("system:checkRule:edit")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result edit(@RequestBody SysCheckRule sysCheckRule) {
        sysCheckRuleService.updateById(sysCheckRule);
        return Result.ok("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Xóa theo id")
    @Operation(summary = "Quy tắc kiểm tra mã-Xóa theo id")
    @RequiresPermissions("system:checkRule:delete")
    @DeleteMapping(value = "/delete")
    public Result delete(@RequestParam(name = "id", required = true) String id) {
        sysCheckRuleService.removeById(id);
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Xóa hàng loạt")
    @Operation(summary = "Quy tắc kiểm tra mã-Xóa hàng loạt")
    @RequiresPermissions("system:checkRule:deleteBatch")
    @DeleteMapping(value = "/deleteBatch")
    public Result deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysCheckRuleService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Quy tắc kiểm tra mã-Tìm kiếm theo id")
    @Operation(summary = "Quy tắc kiểm tra mã-Tìm kiếm theo id")
    @GetMapping(value = "/queryById")
    public Result queryById(@RequestParam(name = "id", required = true) String id) {
        SysCheckRule sysCheckRule = sysCheckRuleService.getById(id);
        return Result.ok(sysCheckRule);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param sysCheckRule
     */
    @RequiresPermissions("system:checkRule:exportXls")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysCheckRule sysCheckRule) {
        return super.exportXls(request, sysCheckRule, SysCheckRule.class, "Quy tắc kiểm tra mã");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions("system:checkRule:add")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                return sysCheckRuleService.importExcel(entry.getValue(), params);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("Nhập tệp thất bại:" + e.getMessage());
            }
        }
        return Result.error("Nhập tệp thất bại: không tìm thấy tệp đã tải lên");
    }

}
