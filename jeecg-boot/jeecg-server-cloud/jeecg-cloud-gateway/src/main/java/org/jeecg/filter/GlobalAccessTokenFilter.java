package org.jeecg.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Arrays;
import java.util.stream.Collectors;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
* Interceptor toàn cục, áp dụng cho tất cả các microservice
*
* 1.Viết lại StripPrefix(Lấy URL)
* 2.Thêm danh tính hiện tại vào request hiện tại
* @author: scott
* @date: 2022/4/8 10:55
*/
@Slf4j
@Component
public class GlobalAccessTokenFilter implements GlobalFilter, Ordered {
    public final static String X_ACCESS_TOKEN = "X-Access-Token";
    public final static String X_GATEWAY_BASE_PATH = "X_GATEWAY_BASE_PATH";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String scheme = exchange.getRequest().getURI().getScheme();
        String host = exchange.getRequest().getURI().getHost();
        int port = exchange.getRequest().getURI().getPort();
        // Giải thích logic mã: Địa chỉ không mang theo cổng(http/https Mặc định) thì port là-1------------
        String basePath = scheme + "://" + host;
        if (port != -1) {
            basePath += ":" + port;
        }
        // 1. Viết lại StripPrefix(Lấy URL)
        addOriginalRequestUrl(exchange, exchange.getRequest().getURI());
        String rawPath = exchange.getRequest().getURI().getRawPath();
        String newPath = "/" + Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/")).skip(1L).collect(Collectors.joining("/"));
        ServerHttpRequest newRequest = exchange.getRequest().mutate().path(newPath).build();
        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());
        //2.Thêm danh tính hiện tại vào request hiện tại
        ServerHttpRequest mutableReq = exchange.getRequest().mutate().header("Authorization-UserName", "").header(X_GATEWAY_BASE_PATH,basePath).build();
        ServerWebExchange mutableExchange = exchange.mutate().request(mutableReq).build();
        return chain.filter(mutableExchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }

}
