package org.jeecg.modules.test.seata.order.controller;

/**
 * @Description: TODO
 * @author: zyf
 * @date: 2022/01/24
 * @version: V1.0
 */
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.jeecg.modules.test.seata.order.dto.PlaceOrderRequest;
import org.jeecg.modules.test.seata.order.service.SeataOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/seata/order")
@Tag(name = "seata Kiểm tra")
public class SeataOrderController {

    @Autowired
    private SeataOrderService orderService;

    /**
     * Đặt hàng tự do
     */
    @PostMapping("/placeOrder")
    @Operation(summary = "Đặt hàng tự do")
    public String placeOrder(@Validated @RequestBody PlaceOrderRequest request) {
        orderService.placeOrder(request);
        return "Đặt hàng thành công";
    }

    /**
     * Kiểm thử tồn kho sản phẩm không đủ-Rollback khi xảy ra ngoại lệ
     */
    @PostMapping("/test1")
    @Operation(summary = "Kiểm thử tồn kho sản phẩm không đủ")
    public String test1() {
        //Đơn giá sản phẩm 10 tệ, tồn kho 20 cái,Số dư người dùng 50 tệ, mô phỏng mua một lần 22 cái. Kỳ vọng ngoại lệ sẽ rollback
        orderService.placeOrder(new PlaceOrderRequest(1L, 1L, 22));
        return "Đặt hàng thành công";
    }

    /**
     * Kiểm thử số dư tài khoản người dùng không đủ-Rollback khi xảy ra ngoại lệ
     */
    @PostMapping("/test2")
    @Operation(summary = "Kiểm thử số dư tài khoản người dùng không đủ")
    public String test2() {
        //Đơn giá sản phẩm 10 tệ, tồn kho 20 cái, số dư người dùng 50 tệ, mô phỏng mua một lần 6 cái. Kỳ vọng ngoại lệ sẽ rollback
        orderService.placeOrder(new PlaceOrderRequest(1L, 1L, 6));
        return "Đặt hàng thành công";
    }
}