package org.jeecg.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysFillRule;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Quy tắc điền giá trị
 * @Author: jeecg-boot
 * @Date: 2019-11-07
 * @Version: V1.0
 */
public interface ISysFillRuleService extends IService<SysFillRule> {

    /**
     * Nhập quy tắc điền giá trị và kiểm tra xem mã quy tắc có bị trùng lặp hay không.
     *
     * @param file Tệp nhập
     * @param params Tham số nhập
     * @return Kết quả nhập
     * @throws Exception Ngoại lệ khi nhập
     * @author wangshuai
     * @since 2026-08-03 LHZP-1265
     */
    Result<?> importExcelCheckRuleCode(MultipartFile file, ImportParams params) throws Exception;
}
