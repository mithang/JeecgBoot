package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecg.modules.system.mapper.SysFillRuleMapper;
import org.jeecg.modules.system.service.ISysFillRuleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: Quy tắc điền giá trị
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
@Service("sysFillRuleServiceImpl")
public class SysFillRuleServiceImpl extends ServiceImpl<SysFillRuleMapper, SysFillRule> implements ISysFillRuleService {

    private static final String UNIQUE_RULE_CODE_INDEX = "uk_sfr_rule_code";

    /**
     * Import quy tắc điền giá trị và trả về số dòng Excel nơi mã quy tắc bị trùng.
     *
     * @param file Tệp nhập
     * @param params Tham số nhập
     * @return Kết quả nhập
     * @throws Exception Ngoại lệ khi nhập
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    @Override
    public Result<?> importExcelCheckRuleCode(MultipartFile file, ImportParams params) throws Exception {
        List<SysFillRule> fillRules;
        try (InputStream inputStream = file.getInputStream()) {
            fillRules = ExcelImportUtil.importExcel(inputStream, SysFillRule.class, params);
        }
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < fillRules.size(); i++) {
            SysFillRule fillRule = fillRules.get(i);
            //update-begin---author:wangshuai---date:20260807---for:Khi nhập quy tắc, trường kiểm toán sử dụng người dùng hiện tại và thời gian hiện tại---
            resetImportAuditFields(fillRule);
            //update-end---author:wangshuai---date:20260807---for:Khi nhập quy tắc, trường kiểm toán sử dụng người dùng hiện tại và thời gian hiện tại---
            int excelRowNumber = params.getTitleRows() + params.getHeadRows() + i + 1;
            if (isRuleCodeExists(fillRule.getRuleCode())) {
                errorMessages.add("Dòng Excel " + excelRowNumber + ": Mã quy tắc 【" + fillRule.getRuleCode() + "】Trùng lặp, bỏ qua nhập.");
                continue;
            }
            try {
                if (save(fillRule)) {
                    successLines++;
                } else {
                    errorMessages.add("Dòng Excel " + excelRowNumber + ": lưu dữ liệu thất bại, bỏ qua khi nhập.");
                }
            } catch (Exception e) {
                if (isDuplicateRuleCodeException(e)) {
                    errorMessages.add("Dòng Excel " + excelRowNumber + ": Mã quy tắc 【" + fillRule.getRuleCode() + "】Trùng lặp, bỏ qua nhập.");
                } else {
                    throw e;
                }
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
    }

    /**
     * Xóa các trường kiểm toán trong Excel, để interceptor chèn của MyBatis điền lại theo người dùng hiện tại.
     */
    private void resetImportAuditFields(SysFillRule fillRule) {
        fillRule.setCreateBy(null);
        fillRule.setCreateTime(null);
        fillRule.setUpdateBy(null);
        fillRule.setUpdateTime(null);
    }

    /**
     * Xác định mã quy tắc đã tồn tại hay chưa.
     *
     * @param ruleCode Mã quy tắc
     * @return Có tồn tại không
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    private boolean isRuleCodeExists(String ruleCode) {
        return ruleCode != null && count(Wrappers.<SysFillRule>lambdaQuery().eq(SysFillRule::getRuleCode, ruleCode)) > 0;
    }

    /**
     * Phán đoán xem chuỗi ngoại lệ có chứa xung đột chỉ mục duy nhất của mã quy tắc hay không.
     *
     * @param exception Ngoại lệ khi lưu dữ liệu
     * @return Có phải ngoại lệ trùng lặp mã quy tắc hay không
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    private boolean isDuplicateRuleCodeException(Exception exception) {
        Throwable cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase().contains(UNIQUE_RULE_CODE_INDEX)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
