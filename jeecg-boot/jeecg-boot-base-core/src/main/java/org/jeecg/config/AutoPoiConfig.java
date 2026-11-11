package org.jeecg.config;

import org.jeecgframework.core.util.ApplicationContextUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * @Author: Scott
 * @Date: 2018/2/7
 * @description: autopoi Lớp cấu hình
 */
@Lazy(false)
@Configuration
public class AutoPoiConfig {

    /**
     * excel Hỗ trợ tham số từ điển cho annotation(Nhập/xuất giá trị từ điển, tự động dịch)
     * Ví dụ: @Excel(name = "Giới tính", width = 15, dicCode = "sex")
     * 1、Khi xuất sẽ căn cứ cấu hình danh mục để chuyển giá trị 1,2 Dịch thành：Nam, Nữ;
     * 2、Khi nhập sẽ dịch Nam, Nữ thành 1,2 Lưu vào cơ sở dữ liệu;
     * @return
     */
    @Bean
    public ApplicationContextUtil applicationContextUtil() {
        return new org.jeecgframework.core.util.ApplicationContextUtil();
    }

}
