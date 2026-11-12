package org.jeecg.modules.test.feign.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.test.feign.client.JeecgTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * Kiểm thử đơn vị microservice
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@RestController
@RequestMapping("/sys/test")
@Tag(name = "【Microservice】Kiểm thử đơn vị")
public class JeecgTestFeignController {

    @Autowired
    private JeecgTestClient jeecgTestClient;

    /**
     * Circuit breaker: fallbackFactory ưu tiên hơn @SentinelResource
     *
     * @param name
     * @return
     */
    @GetMapping("/getMessage")
    @Operation(summary = "Kiểm thử gọi dịch vụ demo qua Feign 1")
    @SentinelResource(value = "test_more_getMessage", fallback = "getDefaultUser")
    public Result<String> getMessage(@RequestParam(value = "name", required = false) String name) {
        log.info("---------Feign fallbackFactory Mức ưu tiên cao hơn@SentinelResource-----------------");
        String resultMsg = jeecgTestClient.getMessage(" I am jeecg-system nút dịch vụ, đang gọi jeecg-demo!");
        return Result.OK(null, resultMsg);
    }

    /**
     * Phương thức kiểm thử: tắt service demo, truy cập request http://127.0.0.1:9999/sys/test/getMessage
     *
     * @param name
     * @return
     */
    @GetMapping("/getMessage2")
    @Operation(summary = "Kiểm thử gọi dịch vụ demo qua Feign 2")
    public Result<String> getMessage2(@RequestParam(value = "name", required = false) String name) {
        log.info("---------Kiểm tra Feign fallbackFactory-----------------");
        String resultMsg = jeecgTestClient.getMessage(" I am jeecg-system nút dịch vụ, đang gọi jeecg-demo!");
        return Result.OK(null, resultMsg);
    }


    @GetMapping("/fallback")
    @Operation(summary = "Kiểm thử circuit breaker")
    @SentinelResource(value = "test_more_fallback", fallback = "getDefaultUser")
    public Result<Object> test(@RequestParam(value = "name", required = false) String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name param is empty");
        }
        return Result.OK();
    }

    /**
     * Ngắt mạch, hàm callback mặc định
     *
     * @param name
     * @return
     */
    public Result<Object> getDefaultUser(String name) {
        log.info("Ngắt mạch, hàm callback mặc định");
        return Result.error(null, "Truy cập quá thời gian, Tùy chỉnh @SentinelResource Fallback");
    }
}
