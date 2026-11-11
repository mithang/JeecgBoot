package org.jeecg.common.handler;

import com.alibaba.fastjson.JSONObject;

/**
 * Interface quy tắc điền giá trị
 *
 * @author Yan_东
 * Nếu cần sử dụng chức năng quy tắc điền giá trị, lớp triển khai quy tắc phải implements interface này
 */
public interface IFillRuleHandler {

    /**
     * Quy tắc điền giá trị
     * @param params Tham số cố định cấu hình trang
     * @param formData  Tham số biểu mẫu động
     * @return
     */
    public Object execute(JSONObject params, JSONObject formData);

}

