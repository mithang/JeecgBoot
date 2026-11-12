package org.jeecg.modules.test.seata.product.service;

import org.jeecg.common.api.vo.Result;

import java.math.BigDecimal;

/**
 * @Description: API sản phẩm
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataProductService {
    /**
     * Trừ kho
     *
     * @param productId Sản phẩm ID
     * @param count    Số lượng trừ
     * @return Tổng giá sản phẩm
     */
    Result<BigDecimal> reduceStock(Long productId, Integer count);
}
