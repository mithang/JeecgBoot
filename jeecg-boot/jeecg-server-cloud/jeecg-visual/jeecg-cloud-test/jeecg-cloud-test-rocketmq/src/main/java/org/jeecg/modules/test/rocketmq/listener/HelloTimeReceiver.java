package org.jeecg.modules.test.rocketmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.jeecg.common.base.BaseMap;
import org.jeecg.modules.test.rocketmq.constant.CloudConstant;
import org.springframework.stereotype.Component;

/**
 * Định nghĩa người nhận (có thể định nghĩa N người nhận, tin nhắn sẽ được gửi đều đến N người nhận)
 * @author: zyf
 * @date: 2022/04/21
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = CloudConstant.MQ_JEECG_PLACE_ORDER_TIME, consumerGroup = "helloTimeReceiver")
public class HelloTimeReceiver implements RocketMQListener<BaseMap> {

    public void onMessage(BaseMap baseMap) {
        log.info("helloTimeReceiver Nhận tin nhắn:" + baseMap);
    }

}