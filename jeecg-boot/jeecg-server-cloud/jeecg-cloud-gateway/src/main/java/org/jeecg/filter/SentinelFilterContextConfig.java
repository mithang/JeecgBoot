//package org.jeecg.filter;
//
//import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///** Sau khi nâng cấp lên spring boot 3, không tìm được phương án thay thế
// * @author: zyf
// * @date: 20210715
// */
//@Configuration
//public class SentinelFilterContextConfig {
//    @Bean
//    public FilterRegistrationBean sentinelFilterRegistration() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(new CommonFilter());
//        registration.addUrlPatterns("/*");
//        // Tài nguyên entry đóng tính năng tổng hợp
//        registration.addInitParameter(CommonFilter.WEB_CONTEXT_UNIFY, "false");
//        registration.setName("sentinelFilter");
//        registration.setOrder(1);
//        return registration;
//    }
//}