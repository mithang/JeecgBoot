package org.jeecg.modules.test.rabbitmq.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.jeecg.boot.starter.rabbitmq.client.RabbitMqClient;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rabbitmq.constant.CloudConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;


/**
 * RabbitMqClient Gửi tin nhắn
 * @author: zyf
 * @date: 2022/04/21
 */
@RestController
@RequestMapping("/sys/test")
@Tag(name = "【Microservice】Kiểm thử đơn vị MQ")
public class JeecgMqTestController {

    @Autowired
    private RabbitMqClient rabbitMqClient;


    /**
     * Phương pháp kiểm thử: Nhấp nhanh để gửi tin nhắn MQ
     *  Quan sát cách ba receiver phân phối và xử lý tin nhắn: HelloReceiver1, HelloReceiver2, HelloReceiver3, sẽ phân phối cân bằng
     *
     * @param req
     * @return
     */
    @GetMapping(value = "/rabbitmq")
    @Operation(summary = "Kiểm tra rabbitmq")
    public Result<?> rabbitMqClientTest(HttpServletRequest req) {
        //rabbitmq Kiểm thử hàng đợi tin nhắn
        BaseMap map = new BaseMap();
        map.put("orderId", RandomUtil.randomNumbers(10));
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER, map);
        rabbitMqClient.sendMessage(CloudConstant.MQ_JEECG_PLACE_ORDER_TIME, map,10);
        return Result.OK("MQ Gửi tin nhắn thành công");
    }

    @GetMapping(value = "/rabbitmq2")
    @Operation(summary = "rabbitmq Kiểm thử bus tin nhắn")
    public Result<?> rabbitmq2(HttpServletRequest req) {

        //rabbitmq Kiểm thử bus tin nhắn
        BaseMap params = new BaseMap();
        params.put("orderId", "123456");
        rabbitMqClient.publishEvent(CloudConstant.MQ_DEMO_BUS_EVENT, params);
        return Result.OK("MQ Gửi tin nhắn thành công");
    }
}
