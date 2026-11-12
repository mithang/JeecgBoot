package org.jeecg.modules.test.seata.order.service;


import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;

/**
 * @Description: API đơn hàng
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
public interface SeataOrderService {
    /**
     * Đặt hàng
     *
     * @param placeOrderRequest Tham số yêu cầu đơn hàng
     */
    void placeOrder(PlaceOrderRequest placeOrderRequest);
}
