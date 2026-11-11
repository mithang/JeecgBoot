package org.jeecg.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.handler.IFillRuleHandler;
import org.jeecg.common.system.query.QueryGenerator;



/**
 * Lớp công cụ tự động tạo giá trị quy tắc
 *
 * @author qinfeng
 * @Ví dụ: tự động tạo mã đơn hàng; tự động tạo ngày hiện tại
 */
@Slf4j
public class FillRuleUtil {

    /**
     * @param ruleCode ruleCode
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object executeRule(String ruleCode, JSONObject formData) {
        if (!StringUtils.isEmpty(ruleCode)) {
            try {
                // Lấy Service
                ServiceImpl impl = (ServiceImpl) SpringContextUtils.getBean("sysFillRuleServiceImpl");
                // Truy vấn thực thể theo ruleCode
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("rule_code", ruleCode);
                JSONObject entity = JSON.parseObject(JSON.toJSONString(impl.getOne(queryWrapper)));
                if (entity == null) {
                    log.warn("Quy tắc điền giá trị:" + ruleCode + " Không tồn tại");
                    return null;
                }
                // Lấy các tham số cần thiết
                String ruleClass = entity.getString("ruleClass");
                JSONObject params = entity.getJSONObject("ruleParams");
                if (params == null) {
                    params = new JSONObject();
                }

                HttpServletRequest request = SpringContextUtils.getHttpServletRequest();

                // Phân tích biến trong params
                // Mức độ ưu tiên:queryString > Biến hệ thống > Giá trị mặc định
                for (String key : params.keySet()) {
                    // 1. Kiểm tra queryString có tham số đó không, nếu có sẽ ưu tiên lấy giá trị
                    //noinspection ConstantValue
                    if (request != null) {
                        String parameter = request.getParameter(key);
                        if (oConvertUtils.isNotEmpty(parameter)) {
                            params.put(key, parameter);
                            continue;
                        }
                    }

                    String value = params.getString(key);
                    // 2. Dùng để thay thế giá trị của biến hệ thống #{sys_user_code}
                    if (value != null && value.contains(SymbolConstant.SYS_VAR_PREFIX)) {
                        value = QueryGenerator.getSqlRuleValue(value);
                        params.put(key, value);
                    }
                }

                if (formData == null) {
                    formData = new JSONObject();
                }
                // Kiểm chứng whitelist đường dẫn gói, ngăn lỗ hổng tải lớp tùy ý
                if (!ruleClass.startsWith("org.jeecg.")) {
                    log.error("Phát hiện nỗ lực tải lớp quy tắc điền giá trị không hợp lệ: {}", ruleClass);
                    throw new SecurityException("Không cho phép tải lớp không thuộc org.jeecg lớp quy tắc điền giá trị dưới đường dẫn gói: " + ruleClass);
                }

                // Thực thi phương thức trong lớp đã cấu hình qua reflection (tải lớp và kiểm chứng giao diện trước, sau đó khởi tạo)
                //update-begin---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
                Class<?> clazz = Class.forName(ruleClass, true, Thread.currentThread().getContextClassLoader());
                //update-end---author:scott ---date:20260416  for：【PR#9538】Class.forName Dùng trình tải lớp theo ngữ cảnh, tăng khả năng tương thích khi triển khai-----------
                if (!IFillRuleHandler.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Lớp " + ruleClass + " Chưa triển khai giao diện IFillRuleHandler");
                }
                IFillRuleHandler ruleHandler = (IFillRuleHandler) clazz.getDeclaredConstructor().newInstance();
                return ruleHandler.execute(params, formData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
