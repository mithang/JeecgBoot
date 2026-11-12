//package org.jeecg.fallback;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.server.HandlerFunction;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Mono;
//
//import java.util.Optional;
//
//import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;
//
///**
// * @author scott
// * @date 2020/05/26
// * Hystrix Xử lý giáng cấp
// */
//@Slf4j
//@Component
//public class HystrixFallbackHandler implements HandlerFunction<ServerResponse> {
//    @Override
//    public Mono<ServerResponse> handle(ServerRequest serverRequest) {
//        Optional<Object> originalUris = serverRequest.attribute(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
//
//        originalUris.ifPresent(originalUri -> log.error("Gateway thực thi yêu cầu:{}Thất bại,hystrix Xử lý giáng cấp dịch vụ", originalUri));
//
//        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .header("Content-Type","text/plain; charset=utf-8").body(BodyInserters.fromObject("Truy cập quá thời gian,Vui lòng thử lại sau"));
//    }
//}
