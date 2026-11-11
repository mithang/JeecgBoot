package org.jeecg.config.sign.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.TenantConstant;
import org.jeecg.common.util.PathMatcherUtil;
import org.jeecg.config.JeecgBaseConfig;
import org.jeecg.config.filter.RequestBodyReserveFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

/**
 * Cấu hình interceptor chữ ký
 * @author: jeecg-boot
 */
@Configuration
public class SignAuthConfiguration implements WebMvcConfigurer {
    @Resource
    JeecgBaseConfig jeecgBaseConfig;

    @Bean
    public SignAuthInterceptor signAuthInterceptor() {
        return new SignAuthInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //------------------------------------------------------------
        //Tìm kiếm API cần chặn chữ ký signUrls
        String signUrls = jeecgBaseConfig.getSignUrls();
        String[] signUrlsArray = null;
        if (StringUtils.isNotBlank(signUrls)) {
            signUrlsArray = signUrls.split(",");
        } else {
            signUrlsArray = PathMatcherUtil.SIGN_URL_LIST;
        }
        //------------------------------------------------------------
        registry.addInterceptor(signAuthInterceptor()).addPathPatterns(signUrlsArray);
    }

    // Giải thích logic mã: issues/I53J5E post Yêu cầu X_SIGN Sau khi interceptor chữ ký kiểm tra xong thì báo lỗi, request body Trống
    @Bean
    public RequestBodyReserveFilter requestBodyReserveFilter(){
        return new RequestBodyReserveFilter();
    }

    @Bean
    public FilterRegistrationBean reqBodyFilterRegistrationBean(){
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestBodyReserveFilter());
        registration.setName("requestBodyReserveFilter");
        //------------------------------------------------------------
        //Tìm kiếm API cần chặn chữ ký signUrls
        String signUrls = jeecgBaseConfig.getSignUrls();
        String[] signUrlsArray = null;
        if (StringUtils.isNotBlank(signUrls)) {
            signUrlsArray = signUrls.split(",");
        } else {
            signUrlsArray = PathMatcherUtil.SIGN_URL_LIST;
        }
        //------------------------------------------------------------
        // Khuyến nghị ở đây chỉ thêm địa chỉ yêu cầu post thay vì tất cả đều phải đi qua filter
        registration.addUrlPatterns(signUrlsArray);
        // Thêm yêu cầu chữ ký bằng annotation
        registration.addUrlPatterns(TenantConstant.SIGNATURE_CHECK_POST_URL);
        return registration;
    }

}
