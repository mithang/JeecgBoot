//package org.jeecg.fallback;
//
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//
///**
// * Bộ xử lý ngắt mạch khi phản hồi timeout 【Nâng cấp springboot2.6.6 xong, class này bị loại bỏ】
// *
// * @author zyf
// */
//@RestController
//public class FallbackController {
//
//    /**
//     * Xử lý ngắt mạch toàn cục
//     * @return
//     */
//    @RequestMapping("/fallback")
//    public Mono<String> fallback() {
//        return Mono.just("Truy cập quá thời gian chờ, vui lòng thử lại sau!");
//    }
//
//    /**
//     * demo Xử lý ngắt mạch
//     * @return
//     */
//    @RequestMapping("/demo/fallback")
//    public Mono<String> fallback2() {
//        return Mono.just("Truy cập quá thời gian chờ, vui lòng thử lại sau!");
//    }
//}
