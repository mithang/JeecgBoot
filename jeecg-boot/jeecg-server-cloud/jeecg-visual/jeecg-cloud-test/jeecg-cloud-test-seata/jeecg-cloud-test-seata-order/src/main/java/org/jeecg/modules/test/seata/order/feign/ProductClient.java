package org.jeecg.modules.test.seata.order.feign;

import org.jeecg.common.api.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * Client Feign sản phẩm giao dịch phân tán
 * @author: zyf
 * @date: 2022/04/21
 */
@FeignClient(value ="seata-product")
public interface ProductClient {
    /**
     * Trừ kho
     *
     * @param productId
     * @param count
     * @return
     */
    @PostMapping("/test/seata/product/reduceStock")
    Result<BigDecimal> reduceStock(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}
