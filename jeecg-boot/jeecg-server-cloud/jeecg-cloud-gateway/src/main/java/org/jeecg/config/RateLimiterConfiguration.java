package org.jeecg.config;

import org.jeecg.filter.GlobalAccessTokenFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * @author scott
 * @date 2020/5/26
 * Cấu hình giới hạn lưu lượng route
 */
@Configuration
public class RateLimiterConfiguration {
    /**
     * IP Giới hạn lưu lượng (Thông qua đối tượng exchange có thể lấy được thông tin yêu cầu, ở đây đã dùng HostName)
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
     * Giới hạn lưu lượng theo người dùng (Thông qua đối tượng exchange có thể lấy được thông tin yêu cầu, lấy người dùng của yêu cầu hiện tại TOKEN)
     */
    @Bean
    public KeyResolver userKeyResolver() {
        //Khi dùng cách này để giới hạn lưu lượng, Header của yêu cầu phải mang theo X-Access-Token Tham số
        return exchange -> Mono.just(exchange.getRequest().getHeaders().getFirst(GlobalAccessTokenFilter.X_ACCESS_TOKEN));
    }

    /**
     * Giới hạn lưu lượng theo API (Lấy uri địa chỉ yêu cầu làm giới hạn lưu lượng key)
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getPath().value());
    }

}
