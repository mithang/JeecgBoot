package org.jeecg.config.firewall.interceptor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Đăng ký interceptor API nội bộ ở chế độ đơn khối {@link InnerApiGuardInterceptor}
 * <p>
 * Chỉ có hiệu lực khi trong classpath không tồn tại jeecg-boot-starter-cloud (chế độ đơn khối) mới có hiệu lực; ở chế độ microservice, API nội bộ cần phục vụ cho Feign gọi, gateway sẽ chặn truy cập từ bên ngoài.
 *
 * @author scott
 * @since 2026-10-14 Ở chế độ đơn thể, API nội bộ có thể bị bất kỳ người dùng đã đăng nhập nào gọi trực tiếp (GHSA-vrqv-3gwg-423j / GHSA-v952-qq2h-377j / GHSA-qj27-82m3-9q8q / GHSA-2xf5-w883-78m9）
 */
@Configuration
@ConditionalOnMissingClass("org.jeecg.config.JeecgCloudBaseConfig")
public class InnerApiGuardConfiguration implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InnerApiGuardInterceptor()).addPathPatterns("/**").order(Ordered.HIGHEST_PRECEDENCE);
    }
}
