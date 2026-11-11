package org.jeecg.modules.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.ImportExcelUtil;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecg.modules.system.mapper.SysCheckRuleMapper;
import org.jeecg.modules.system.service.ISysCheckRuleService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @Description: Quy tắc kiểm tra mã
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
@Service
@Slf4j
public class SysCheckRuleServiceImpl extends ServiceImpl<SysCheckRuleMapper, SysCheckRule> implements ISysCheckRuleService {

    /**
     * Ký tự đặc biệt chỉ số lượng chữ số, dùng để kiểm tra toàn bộ giá trị thay vì cắt một đoạn
     */
    private final String CHECK_ALL_SYMBOL = "*";

    @Override
    public SysCheckRule getByCode(String ruleCode) {
        LambdaQueryWrapper<SysCheckRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysCheckRule::getRuleCode, ruleCode);
        return super.getOne(queryWrapper);
    }

    @Override
    public Result<?> importExcel(MultipartFile file, ImportParams params) throws Exception {
        List<SysCheckRule> checkRules;
        try (InputStream inputStream = file.getInputStream()) {
            checkRules = ExcelImportUtil.importExcel(inputStream, SysCheckRule.class, params);
        }
        //update-begin---wangshuai---date:20260810  for：【LHZP-1278】Nhập vào có trùng lặp thì báo lỗi------------
        List<String> errorMessages = new ArrayList<>();
        int successLines = 0;
        for (int i = 0; i < checkRules.size(); i++) {
            SysCheckRule checkRule = checkRules.get(i);
            try {
                resetImportAuditFields(checkRule);
                if (isRuleCodeExists(checkRule.getRuleCode())) {
                    errorMessages.add("" + (i + 1) + "Dòng: Quy tắc Code[" + checkRule.getRuleCode() + "]đã tồn tại, bỏ qua nhập.");
                    continue;
                }
                if (!save(checkRule)) {
                    throw new JeecgBootException("Lưu dữ liệu thất bại");
                }
                successLines++;
            } catch (Exception e) {
                errorMessages.add("" + (i + 1) + "dòng: " + e.getMessage());
                log.error("Nhập quy tắc kiểm tra hệ thống thất bại, số dòng:{}", i + 1, e);
            }
        }
        return ImportExcelUtil.imporReturnRes(errorMessages.size(), successLines, errorMessages);
        //update-end---author:wangshuai ---date:20260810  for：【LHZP-1278】Nhập vào có trùng lặp thì báo lỗi------------
    }

    /**
     * Kiểm tra Code quy tắc đã tồn tại chưa.
     *
     * @param ruleCode Quy tắc Code
     * @return Có tồn tại không
     */
    private boolean isRuleCodeExists(String ruleCode) {
        return count(new LambdaQueryWrapper<SysCheckRule>().eq(SysCheckRule::getRuleCode, ruleCode)) > 0;
    }

    /**
     * Xóa các trường kiểm toán trong Excel, để interceptor chèn của MyBatis điền lại theo người dùng hiện tại.
     */
    private void resetImportAuditFields(SysCheckRule checkRule) {
        checkRule.setCreateBy(null);
        checkRule.setCreateTime(null);
        checkRule.setUpdateBy(null);
        checkRule.setUpdateTime(null);
    }

    /**
     * Kiểm tra giá trị truyền vào bằng quy tắc kiểm tra tùy chỉnh do người dùng đặt
     *
     * @param checkRule
     * @param value
     * @return Trả về null nghĩa là đã qua kiểm tra, nếu không thì trả về nội dung thông báo lỗi
     */
    @Override
    public JSONObject checkValue(SysCheckRule checkRule, String value) {
        if (checkRule != null && StringUtils.isNotBlank(value)) {
            String ruleJson = checkRule.getRuleJson();
            if (StringUtils.isNotBlank(ruleJson)) {
                // Chỉ số bắt đầu cắt, tăng dần theo thứ tự của quy tắc, nhưng * Ký tự * không tính vào phạm vi tăng dần
                int beginIndex = 0;
                JSONArray rules = JSON.parseArray(ruleJson);
                for (int i = 0; i < rules.size(); i++) {
                    JSONObject result = new JSONObject();
                    JSONObject rule = rules.getJSONObject(i);
                    // Số chữ số
                    String digits = rule.getString("digits");
                    result.put("digits", digits);
                    // Quy tắc xác minh
                    String pattern = rule.getString("pattern");
                    result.put("pattern", pattern);
                    // Văn bản nhắc nhở khi không đạt
                    String message = rule.getString("message");
                    result.put("message", message);

                    // Theo khoảng do người dùng thiết lập, cắt chuỗi để kiểm tra
                    String checkValue;
                    // Có kiểm tra toàn bộ giá trị mà không cắt ngắn hay không
                    if (CHECK_ALL_SYMBOL.equals(digits)) {
                        checkValue = value;
                    } else {
                        int num = Integer.parseInt(digits);
                        int endIndex = beginIndex + num;
                        // Nếu chỉ số kết thúc lớn hơn độ dài của giá trị đã cho thì lấy tới ký tự cuối cùng
                        endIndex = endIndex > value.length() ? value.length() : endIndex;
                        // Nếu chỉ số bắt đầu lớn hơn chỉ số kết thúc thì nghĩa là người dùng chưa nhập đến vị trí đó, gán trực tiếp giá trị rỗng
                        if (beginIndex > endIndex) {
                            checkValue = "";
                        } else {
                            checkValue = value.substring(beginIndex, endIndex);
                        }
                        result.put("beginIndex", beginIndex);
                        result.put("endIndex", endIndex);
                        beginIndex += num;
                    }
                    result.put("checkValue", checkValue);
                    boolean passed = Pattern.matches(pattern, checkValue);
                    result.put("passed", passed);
                    // Nếu không vượt qua kiểm tra thì trả về thông tin lỗi
                    if (!passed) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

}
