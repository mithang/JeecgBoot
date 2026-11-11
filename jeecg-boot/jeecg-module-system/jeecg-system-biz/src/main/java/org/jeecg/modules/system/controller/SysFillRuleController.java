package org.jeecg.modules.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.FillRuleUtil;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * @Description: Quy tắc điền giá trị
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Slf4j
@Tag(name = "Quy tắc điền giá trị")
@RestController
@RequestMapping("/sys/fillRule")
public class SysFillRuleController extends JeecgController<SysFillRule, ISysFillRuleService> {
    @Autowired
    private ISysFillRuleService sysFillRuleService;

    /**
     * Truy vấn danh sách phân trang
     *
     * @param sysFillRule
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Truy vấn danh sách phân trang")
    @Operation(summary = "Quy tắc điền giá trị-Truy vấn danh sách phân trang")
    @RequiresRoles({"admin"})
    @GetMapping(value = "/list")
    public Result<?> queryPageList(SysFillRule sysFillRule,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<SysFillRule> queryWrapper = QueryGenerator.initQueryWrapper(sysFillRule, req.getParameterMap());
        Page<SysFillRule> page = new Page<>(pageNo, pageSize);
        IPage<SysFillRule> pageList = sysFillRuleService.page(page, queryWrapper);
        return Result.ok(pageList);
    }

    /**
     * Kiểm tra ruleCode
     *
     * @param ruleCode
     * @return
     */
    @RequiresRoles({"admin"})
    @GetMapping(value = "/testFillRule")
    public Result testFillRule(@RequestParam("ruleCode") String ruleCode) {
        Object result = FillRuleUtil.executeRule(ruleCode, new JSONObject());
        return Result.ok(result);
    }

    /**
     * Thêm
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Thêm")
    @Operation(summary = "Quy tắc điền giá trị-Thêm")
    @RequiresRoles({"admin"})
    @PostMapping(value = "/add")
    public Result<?> add(@RequestBody SysFillRule sysFillRule) {
        sysFillRuleService.save(sysFillRule);
        return Result.ok("Thêm thành công!");
    }

    /**
     * Chỉnh sửa
     *
     * @param sysFillRule
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Chỉnh sửa")
    @Operation(summary = "Quy tắc điền giá trị-Chỉnh sửa")
    @RequiresRoles({"admin"})
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
    public Result<?> edit(@RequestBody SysFillRule sysFillRule) {
        sysFillRuleService.updateById(sysFillRule);
        return Result.ok("Chỉnh sửa thành công!");
    }

    /**
     * Xóa theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Xóa theo id")
    @Operation(summary = "Quy tắc điền giá trị-Xóa theo id")
    @RequiresRoles({"admin"})
    @DeleteMapping(value = "/delete")
    public Result<?> delete(@RequestParam(name = "id", required = true) String id) {
        sysFillRuleService.removeById(id);
        return Result.ok("Xóa thành công!");
    }

    /**
     * Xóa hàng loạt
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Xóa hàng loạt")
    @Operation(summary = "Quy tắc điền giá trị-Xóa hàng loạt")
    @RequiresRoles({"admin"})
    @DeleteMapping(value = "/deleteBatch")
    public Result<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.sysFillRuleService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.ok("Xóa hàng loạt thành công!");
    }

    /**
     * Tìm kiếm theo id
     *
     * @param id
     * @return
     */
    @AutoLog(value = "Quy tắc điền giá trị-Tìm kiếm theo id")
    @Operation(summary = "Quy tắc điền giá trị-Tìm kiếm theo id")
    @RequiresRoles({"admin"})
    @GetMapping(value = "/queryById")
    public Result<?> queryById(@RequestParam(name = "id", required = true) String id) {
        SysFillRule sysFillRule = sysFillRuleService.getById(id);
        return Result.ok(sysFillRule);
    }

    /**
     * Xuất excel
     *
     * @param request
     * @param sysFillRule
     */
    @RequiresRoles({"admin"})
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SysFillRule sysFillRule) {
        return super.exportXls(request, sysFillRule, SysFillRule.class, "Quy tắc điền giá trị");
    }

    /**
     * Nhập dữ liệu qua Excel
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresRoles({"admin"})
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        //update-begin---author:wangshuai ---date:20260803  for：【LHZP-1265] Khi import quy tắc mã hệ thống bị trùng mã, hiển thị số dòng Excel cụ thể-----------
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                return sysFillRuleService.importExcelCheckRuleCode(entry.getValue(), params);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return Result.error("Nhập tệp thất bại:" + e.getMessage());
            }
        }
        return Result.error("Nhập tệp thất bại: không tìm thấy tệp đã tải lên");
        //update-end---author:wangshuai ---date:20260803  for：【LHZP-1265] Khi import quy tắc mã hệ thống bị trùng mã, hiển thị số dòng Excel cụ thể-----------
    }

    /**
     * Thực thi quy tắc điền giá trị tùy chỉnh qua ruleCode
     *
     * @param ruleCode Mã quy tắc điền giá trị cần thực thi
     * @param formData Dữ liệu biểu mẫu, có thể sinh các kết quả điền giá trị khác nhau tùy theo dữ liệu biểu mẫu
     * @return Kết quả sau khi chạy
     */
    @PutMapping("/executeRuleByCode/{ruleCode}")
    public Result executeByRuleCode(@PathVariable("ruleCode") String ruleCode, @RequestBody JSONObject formData) {
        Object result = FillRuleUtil.executeRule(ruleCode, formData);
        return Result.ok(result);
    }


    /**
     * Thực thi hàng loạt quy tắc điền giá trị tùy chỉnh theo ruleCode
     *
     * @param ruleData Mảng JSON quy tắc điền giá trị cần thực thi:
     *                 Ví dụ: { "commonFormData": {}, rules: [ { "ruleCode": "xxx", "formData": null } ] }
     * @return Kết quả sau khi chạy, ví dụ trả về: [{"ruleCode": "order_num_rule", "result": "CN2019111117212984"}]
     *
     */
    @PutMapping("/executeRuleByCodeBatch")
    public Result executeByRuleCodeBatch(@RequestBody JSONObject ruleData) {
        JSONObject commonFormData = ruleData.getJSONObject("commonFormData");
        JSONArray rules = ruleData.getJSONArray("rules");
        // Duyệt rules, thực thi hàng loạt quy tắc
        JSONArray results = new JSONArray(rules.size());
        for (int i = 0; i < rules.size(); i++) {
            JSONObject rule = rules.getJSONObject(i);
            String ruleCode = rule.getString("ruleCode");
            JSONObject formData = rule.getJSONObject("formData");
            // Nếu không truyền formData thì dùng cái chung (common)
            if (formData == null) {
                formData = commonFormData;
            }
            // Thực thi quy tắc điền giá trị
            Object result = FillRuleUtil.executeRule(ruleCode, formData);
            JSONObject obj = new JSONObject(rules.size());
            obj.put("ruleCode", ruleCode);
            obj.put("result", result);
            results.add(obj);
        }
        return Result.ok(results);
    }

}
