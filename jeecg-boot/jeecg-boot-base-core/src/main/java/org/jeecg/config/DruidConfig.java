package org.jeecg.config;

import java.io.IOException;

import com.alibaba.druid.spring.boot4.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot4.autoconfigure.properties.DruidStatProperties;
import jakarta.servlet.*;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.util.Utils;

/**
 * @Description: DruidConfig Lớp cấu hình
 * @author: jeecg-boot
 */
@Configuration
@AutoConfigureAfter(DruidDataSourceAutoConfigure.class)
public class DruidConfig {

    /**
     * có chứa quảng cáo common.js đường dẫn đầy đủ,druid-1.1.14
     */
    private static final String FILE_PATH = "support/http/resources/js/common.js";
    /**
     * Script gốc, câu lệnh kích hoạt việc xây dựng quảng cáo
     */
    private static final String ORIGIN_JS = "this.buildFooter();";
    /**
     * Script sau khi thay thế
     */
    private static final String NEW_JS = "//this.buildFooter();";

    /**
     * Loại bỏ quảng cáo trên trang giám sát Druid
     *
     * @param properties DruidStatProperties Tập hợp thuộc tính
     * @return {@link FilterRegistrationBean}
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "spring.datasource.druid.stat-view-servlet.enabled", havingValue = "true")
    public FilterRegistrationBean<RemoveAdFilter> removeDruidAdFilter(
            DruidStatProperties properties) throws IOException {
        // Lấy tham số của trang giám sát web
        DruidStatProperties.StatViewServlet config = properties.getStatViewServlet();
        // Trích xuất common.js đường dẫn cấu hình
        String pattern = config.getUrlPattern() != null ? config.getUrlPattern() : "/druid/*";
        String commonJsPattern = pattern.replaceAll("\\*", "js/common.js");
        // Lấy common.js
        String text = Utils.readFromResource(FILE_PATH);
        // Chặn this.buildFooter(); không xây dựng quảng cáo
        final String newJs = text.replace(ORIGIN_JS, NEW_JS);
        FilterRegistrationBean<RemoveAdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RemoveAdFilter(newJs));
        registration.addUrlPatterns(commonJsPattern);
        return registration;
    }

    /**
     * Xóa bộ lọc quảng cáo của Druid
     *
     * @author BBF
     */
    private class RemoveAdFilter implements Filter {

        private final String newJs;

        public RemoveAdFilter(String newJs) {
            this.newJs = newJs;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            chain.doFilter(request, response);
            // Đặt lại bộ đệm, header phản hồi sẽ không bị đặt lại
            response.resetBuffer();
            response.getWriter().write(newJs);
        }
    }
}
