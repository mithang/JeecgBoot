//package org.jeecg.modules.demo.cloud.controller;
//
//import com.alibaba.csp.sentinel.annotation.SentinelResource;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.common.api.vo.Result;
//import org.jeecg.common.system.api.ISysBaseAPI;
//import org.jeecg.common.system.vo.DictModel;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import jakarta.annotation.Resource;
//import java.util.List;
//
///**
// *
// */
//@Slf4j
//@Tag(name = "【Microservice】Kiểm thử đơn vị")
//@RestController
//@RequestMapping("/test")
//public class JcloudDemoFeignController {
//    @Resource
//    private ISysBaseAPI sysBaseApi;
////    @Autowired
////    private ErpHelloApi erpHelloApi;
//
//    /**
//     * Kiểm tra
//     *
//     * @return
//     */
//    @GetMapping("/callSystem")
//    //@SentinelResource(value = "remoteDict",fallback = "getDefaultHandler")
//    @Operation(summary = "Gọi dịch vụ system qua feign")
//    public Result getRemoteDict() {
//        List<DictModel> list = sysBaseApi.queryAllDict();
//        return Result.OK(list);
//    }
//
//
////    /**
////     * Kiểm tra gọi API microservice erp
////     * 【Cách kiểm tra：tạo mô-đun microservice bằng archetype để kiểm tra tích hợp nhanh】
////     *  https://help.jeecg.com/java/springcloud/archetype.html
////     * @return
////     */
////    @GetMapping("/callErp")
////    @Operation(summary = "Kiểm tra feign erp")
////    public Result callErp() {
////        log.info("call erp Dịch vụ");
////        String res = erpHelloApi.callHello();
////        return Result.OK(res);
////    }
//
//    /**
//     * Ngắt mạch, hàm callback mặc định
//     *
//     * @return
//     */
//    public Result<Object> getDefaultHandler() {
//        log.info("Kiểm tra JcloudDemoController-remoteDict Ngắt mạch giáng cấp");
//        return Result.error("Kiểm tra JcloudDemoController-remoteDict Ngắt mạch giáng cấp");
//    }
//
//}
