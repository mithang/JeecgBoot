package org.jeecg.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.system.entity.SysCheckRule;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description: Quy tắc kiểm tra mã
 * @Author: jeecg-boot
 * @Date: 2020-02-04
 * @Version: V1.0
 */
public interface ISysCheckRuleService extends IService<SysCheckRule> {

    /**
     * Lấy quy tắc qua code
     *
     * @param ruleCode
     * @return
     */
    SysCheckRule getByCode(String ruleCode);


    /**
     * Kiểm tra giá trị truyền vào bằng quy tắc kiểm tra tùy chỉnh do người dùng đặt
     *
     * @param checkRule
     * @param value
     * @return Trả về null nghĩa là đã qua kiểm tra, nếu không thì trả về nội dung thông báo lỗi
     */
    JSONObject checkValue(SysCheckRule checkRule, String value);

    /**
     * Nhập quy tắc kiểm tra hệ thống.
     */
    Result<?> importExcel(MultipartFile file, ImportParams params) throws Exception;

}
